package uec.teehardware

import chisel3._
import chisel3.util._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.system._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import freechips.rocketchip.prci._
import sifive.blocks.devices.pinctrl.{BasePin, EnhancedPin, EnhancedPinCtrl, Pin, PinCtrl}
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._
import sifive.blocks.devices.i2c._
import sifive.blocks.devices.jtag._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import sifive.fpgashells.ip.xilinx.xdma._
import sifive.fpgashells.devices.xilinx.xdma.{XDMAClocks, XDMAPads, _}
import sifive.fpgashells.ip.xilinx.IBUFDS_GTE4
import sifive.fpgashells.shell.xilinx.XDMATopPads
import testchipip.SerialAdapter.SERIAL_TSI_WIDTH
import uec.teehardware.devices.aes._
import uec.teehardware.devices.ed25519._
import uec.teehardware.devices.random._
import uec.teehardware.devices.chacha._
import uec.teehardware.devices.poly._
import uec.teehardware.devices.sha3._
import uec.teehardware.devices.usb11hs._
import uec.teehardware.devices.opentitan.aes._
import uec.teehardware.devices.opentitan.alert._
import uec.teehardware.devices.opentitan.flash_ctrl._
import uec.teehardware.devices.opentitan.hmac._
import uec.teehardware.devices.opentitan.keymgr._
import uec.teehardware.devices.opentitan.kmac._
import uec.teehardware.devices.opentitan.otp_ctrl._
import testchipip.{CanHavePeripheryTLSerial, ClockedIO, SerialAdapter, SerialIO, SerialTLKey, TLSerdes}
import uec.teehardware.devices.clockctrl._

import java.lang.reflect.InvocationTargetException

class SlowMemIsland(blockBytes: Int, val crossing: ClockCrossingType = AsynchronousCrossing(8))(implicit p: Parameters)
    extends LazyModule
    with CrossesToOnlyOneClockDomain {

  val node = TLBuffer()

  lazy val module = new LazyRawModuleImp(this) {
    val io = IO(new Bundle {
      val ChildClock = Input(Clock())
      val ChildReset = Input(Bool())
    })

    childClock := io.ChildClock
    childReset := io.ChildReset
  }
}

trait HasTEEHWSystem
  // The components that are directly instantiated here. Needed to be re-factored from the original
  //    with HasPeripheryI2C
  //    with HasPeripheryUART // NOTE: Already included
  //    with HasPeripherySPIFlash // NOTE: Already included
  //    with HasPeripherySPI // NOTE: Already included
  //    with HasPeripheryMaskROMSlave // NOTE: This is already included inside the RocketSubsystem
  //    with CanHaveMasterAXI4MemPort // NOTE: A TL->Axi4 is already done outside the system
  //    with CanHaveMasterTLMemPort // NOTE: Manually created the TL port
  // This is intended only for simulations, but does not affect the fpga/chip versions
  extends CanHavePeripheryTLSerial // ONLY for simulations
{ this: TEEHWBaseSubsystem =>
  // The clock resource. This is just for put in the DTS the tlclock
  // TODO: Now the clock is derived from the bus that is connected
  // TODO: We need a way now to extract that one in the makefiles
  // Letting this ONLY for compatibility
  val tlclock = new FixedClockResource("tlclk", p(FreqKeyMHz))

  // Main memory controller (TL memory controller)
  val memctl: Option[(TLManagerNode, Option[SlowMemIsland])] = p(ExtMem).map { A =>
    val memdevice = new MemoryDevice
    val mainMemParam = TLSlavePortParameters.v1(
      managers = Seq(TLSlaveParameters.v1(
        address = AddressSet.misaligned(A.master.base, A.master.size),
        resources = memdevice.reg,
        regionType = RegionType.UNCACHED, // cacheable
        executable = true,
        supportsGet = TransferSizes(1, mbus.blockBytes),
        supportsPutFull = TransferSizes(1, mbus.blockBytes),
        supportsPutPartial = TransferSizes(1, mbus.blockBytes),
        fifoId = Some(0),
        mayDenyPut = true,
        mayDenyGet = true
      )),
      beatBytes = A.master.beatBytes
    )
    val memTLNode = TLManagerNode(Seq(mainMemParam))
    val island = if (p(DDRPortOther)) {
      //val source = LazyModule(new TLAsyncCrossingSource())
      //val sink = LazyModule(new TLAsyncCrossingSink(AsyncQueueParams(depth = 1, sync = 3, safe = true, narrow = false)))
      //val buffer  = LazyModule(new TLBuffer) // We removed a buffer in the TOP
      val island = LazyModule(new SlowMemIsland(mbus.blockBytes))
      //memTLNode := buffer.node := island.node := mbus.toDRAMController(Some("tl"))()
      island.crossTLIn(island.node) := mbus.toDRAMController(Some("tl"))()
      memTLNode := island.node
      Some(island)
    } else {
      val buffer = LazyModule(new TLBuffer)
      memTLNode := buffer.node := mbus.toDRAMController(Some("tl"))()
      None
    }
    (memTLNode, island)
  }
  val memserctl = p(ExtSerMem).map {A =>
    val memdevice = new MemoryDevice
    val mainMemParam = Seq(TLSlaveParameters.v1(
        address = AddressSet.misaligned(A.master.base, A.master.size),
        resources = memdevice.reg,
        regionType = RegionType.UNCACHED, // cacheable
        executable = true,
        supportsGet = TransferSizes(1, mbus.blockBytes),
        supportsPutFull = TransferSizes(1, mbus.blockBytes),
        supportsPutPartial = TransferSizes(1, mbus.blockBytes),
        fifoId = Some(0),
        mayDenyPut = true,
        mayDenyGet = true))
    println(s"SERDES added to the system ${mbus.blockBytes}")
    val serdes = LazyModule(new TLSerdes(
      w = A.serWidth,
      params = mainMemParam,
      beatBytes = A.master.beatBytes))
    serdes.node := TLBuffer() := mbus.toDRAMController(Some("ser"))()
    // TODO: The clock separation for this is obviously not done
    serdes
  }

  val extserctl = p(ExtSerBus).map {A =>
    val device = new SimpleBus("ext_mmio".kebab, Nil)
    val mainMemParam = Seq(TLSlaveParameters.v1(
      address = AddressSet.misaligned(A.master.base, A.master.size),
      resources = device.ranges,
      regionType = RegionType.GET_EFFECTS, // Not cacheable
      executable = true,
      supportsGet = TransferSizes(1, cbus.blockBytes),
      supportsPutFull = TransferSizes(1, cbus.blockBytes),
      supportsPutPartial = TransferSizes(1, cbus.blockBytes),
      fifoId = Some(0),
      mayDenyPut = true,
      mayDenyGet = true))
    println(s"SERDES added to the system ${cbus.blockBytes}")
    val serdes = LazyModule(new TLSerdes(
      w = A.serWidth,
      params = mainMemParam,
      beatBytes = A.master.beatBytes))
    cbus.coupleTo("ser") {
      serdes.node := TLBuffer() := TLWidthWidget(cbus.beatBytes) := _
    }
    // TODO: The clock separation for this is obviously not done
    serdes
  }

  // UART implementation. This is the same as HasPeripheryUART
  // TODO: This is done this way instead of "HasPeripheryUART" because we need to do a bind to tlclock
  val uartDevs = p(PeripheryUARTKey).map {
    val divinit = (p(PeripheryBusKey).dtsFrequency.get / 115200).toInt
    ps => UARTAttachParams(ps).attachTo(this)
  }
  val uartNodes = uartDevs.map { ps => ps.ioNode.makeSink }
  uartDevs.foreach { case ps =>
    //tlclock.bind(ps.device)
  }

  // SPI to MMC conversion.
  // TODO: There is an intention from Sifive to do MMC, but has to be manual
  // QSPI flash implementation. This is the same as HasPeripherySPIFlash
  // TODO: This is done this way instead of "HasPeripherySPIFlash" because we need to do a bind to tlclock
  val allspicfg = p(PeripherySPIKey) ++ p(PeripherySPIFlashKey)
  val spiDevs = allspicfg.map {
    case ps: SPIParams =>
      SPIAttachParams(ps).attachTo(this)
    case ps: SPIFlashParams =>
      SPIFlashAttachParams(ps, fBufferDepth = 8).attachTo(this)
    case _ =>
      throw new RuntimeException("We cannot cast a configuration of SPI?")
  }
  val spiNodes = spiDevs.map { ps => ps.ioNode.makeSink() }

  spiDevs.zipWithIndex.foreach { case (ps, i) =>
    i match {
      case 0 => {
        val mmc = new MMCDevice(ps.device, p(SDCardMHz)) // Only the first one is mmc
        ResourceBinding {
          Resource(mmc, "reg").bind(ResourceAddress(0))
        }
      }
      case 1 => {
        val flash = new FlashDevice(ps.device, maxMHz = p(QSPICardMHz))
        ResourceBinding {
          Resource(flash, "reg").bind(ResourceAddress(0))
        }
      }
      case _ =>
    }
    //tlclock.bind(ps.device)
  }

  // GPIO implementation
  val (gpioNodes, gpioIofs) = p(PeripheryGPIOKey).map { ps =>
    val gpio = GPIOAttachParams(ps).attachTo(this)
    (gpio.ioNode.makeSink(), gpio.iofPort)
  }.unzip

  // PCIe port export
  val pcie = if (p(IncludePCIe)) {
    val pcie = LazyModule(new XilinxVC707PCIeX1)
    val nodeSlave = TLIdentityNode()
    val nodeMaster = TLIdentityNode()

    // Attach to the PCIe. NOTE: For some reason, they use TLIdentityNode here. Not sure why tho.
    // Maybe is the fact of just doing a crosstalk here
    pcie.crossTLIn(pcie.slave) := nodeSlave
    pcie.crossTLIn(pcie.control) := nodeSlave
    nodeMaster := pcie.crossTLOut(pcie.master)

    val pciename = Some(s"pcie_0")
    sbus.fromMaster(pciename) {
      nodeMaster
    }
    sbus.toFixedWidthSlave(pciename) {
      nodeSlave
    }
    ibus.fromSync := pcie.intnode

    Some(pcie)
  }
  else None

  val xdma = p(XDMAPCIe).map { cfg =>
    val xdma = LazyModule(new XDMA(cfg))
    val nodeSlave = TLIdentityNode()
    val nodeMaster = TLIdentityNode()

    // Attach to the PCIe. NOTE: For some reason, they use TLIdentityNode here. Not sure why tho.
    // Maybe is the fact of just doing a crosstalk here
    xdma.crossTLIn(xdma.slave) := nodeSlave
    xdma.crossTLIn(xdma.control) := nodeSlave
    nodeMaster := xdma.crossTLOut(xdma.master)

    val pciename = Some(s"xdma_0")
    sbus.fromMaster(pciename) {
      nodeMaster
    }
    sbus.toFixedWidthSlave(pciename) {
      nodeSlave
    }
    ibus.fromSync := xdma.intnode

    xdma
  }

  // add ROM devices
  val maskROMs = p(MaskROMLocated(location)).map { MaskROM.attach(_, this, CBUS) }

  // NOTE: I do not know how this work yet. Now the clock is VERY important, for knowing where the
  // clock domains came from. You can assign it to different nodes, and create new ones.
  // Eventually, this will create even their own dts for reference purposes.
  // so, you DEFINITELLY need to define your clocks from now on. This will be assigned to asyncClockGroupsNode
  // and the "SubsystemDriveAsyncClockGroupsKey" key needs to be None'd to avoid default clocks
  // There should be a easier way, but right now also the Sifive peripherals and the TEE peripherals
  // uses all of that. So, there is no way.
  // (Code analyzed from: Clocks.scala:61, inside chipyard)
  // (Code analyzed from: ClockGroup.scala:63, inside rocketChip. And yes... I know I can just do a SimpleGroup.)

  // PRC domains:
  // The final analysis of the clock domains is just accumulated in the asyncClockGroupsNode
  // Everytime a clock is needed, the node just gets populated using "clockNode := (...) := asyncClockGroupsNode"
  // This means all the solicited clocks are going to be accumulated in the asyncClockGroupsNode
  // We can use the clock aggregator (ClockGroupAggregator) which will take a single clock and a reset, then
  // replicate it for all asyncClockGroupsNode. This requires a ClockGroup with only 1 group.
  // Then we iterate them using node.out.unzip. Unfortunately, will not have names.

  // Create the ClockGroupSource (only 1...)
  val clockGroup = ClockGroupSourceNode(List.fill(1) { ClockGroupSourceParameters() })
  // Create the Aggregator. This will just take the SourceNode, then just replicate it in a Nexus
  val clocksAggregator = LazyModule(new ClockGroupAggregator("allClocks")).node
  // Connect it to the asyncClockGroupsNode, with the aggregator
  asyncClockGroupsNode :*= clocksAggregator := clockGroup
}

class TEEHWSystem(implicit p: Parameters) extends TEEHWSubsystem
  with HasTEEHWSystem
  // The TEEHW components
  with HasPeripherySHA3
  with HasPeripheryed25519
  with HasPeripheryAES
  with HasPeripheryUSB11HS
  with HasPeripheryRandom
  with HasPeripheryChacha
  with HasPeripheryPoly
  // The opentitan components
  with HasPeripheryAESOT
  with HasPeripheryHMAC
  with HasPeripheryOTPCtrl
{
  // System module creation
  override lazy val module = new TEEHWSystemModule(this)
}

trait HasTEEHWSystemModule extends HasRTCModuleImp
  // The components that are directly instantiated here. Needed to be re-factored from the original
  //    with HasPeripheryI2CModuleImp
  //    with HasPeripheryUARTModuleImp // NOTE: Already included
  //    with HasPeripherySPIFlashModuleImp // NOTE: Already included
  //    with HasPeripherySPIModuleImp // NOTE: Already included
  //    with CanHaveMasterAXI4MemPortModuleImp // NOTE: A TL->Axi4 is already done outside the system
  //    with CanHaveMasterTLMemPortModuleImp // NOTE: Manually created the TL port
  // This is intended only for simulations, but does not affect the fpga/chip versions
  with DontTouch
{
  val outer: TEEHWBaseSubsystem with HasTEEHWSystem with CanHavePeripheryCLINT

  // Main memory controller
  val memPorts = outer.memctl.map { A =>
    val (memTLnode: TLManagerNode, island: Option[SlowMemIsland]) = A
    val (cclk, crst) = island.map { island =>
      val ChildClock = IO(Input(Clock()))
      val ChildReset = IO(Input(Bool()))
      island.module.io.ChildClock := ChildClock
      island.module.io.ChildReset := ChildReset
      (ChildClock, ChildReset)
    }.unzip
    val mem_tl = IO(HeterogeneousBag.fromNode(memTLnode.in))
    (mem_tl zip memTLnode.in).foreach { case (io, (bundle, _)) => io <> bundle }
    (mem_tl, cclk, crst)
  }
  val mem_tl = memPorts.map(_._1) // For making work HeterogeneousBag
  val mem_ChildClock = memPorts.map(_._2) // For making work HeterogeneousBag
  val mem_ChildReset = memPorts.map(_._3) // For making work HeterogeneousBag

  // Main memory serial controller
  val (memSerPorts, serSourceBits) = outer.memserctl.map { A =>
    val ser = IO(new SerialIO(A.module.io.ser.head.w))
    ser <> A.module.io.ser.head
    (ser, A.node.in.head._1.params.sourceBits)
  }.unzip

  // MMIO external serial controller
  val (extSerPorts, extSourceBits) = outer.extserctl.map { A =>
    val ser = IO(new SerialIO(A.module.io.ser.head.w))
    ser <> A.module.io.ser.head
    (ser, A.node.in.head._1.params.sourceBits)
  }.unzip

  // UART implementation
  val uart = outer.uartNodes.zipWithIndex.map { case(n,i) => n.makeIO()(ValName(s"uart_$i")).asInstanceOf[UARTPortIO] }

  // SPI to MMC conversion
  val spi  = outer.spiNodes.zipWithIndex.map  { case(n,i) => n.makeIO()(ValName(s"spi_$i")).asInstanceOf[SPIPortIO] }

  // GPIO implementation
  val gpio = outer.gpioNodes.zipWithIndex.map { case(n,i) => n.makeIO()(ValName(s"gpio_$i")).asInstanceOf[GPIOPortIO] }
  // The IOFs. Why Sifive? Why do you hurt me this way?
  // TODO: We can... you know... assign here, or in the platform... or in the chip... for now is defaulted
  outer.gpioIofs.foreach{iofOpt => iofOpt.foreach{iof =>
    iof.getWrappedValue.iof_0.foreach(_.default())
    iof.getWrappedValue.iof_1.foreach(_.default())
  }}
  
  val pciePorts = outer.pcie.map { pcie =>
    val port = IO(new XilinxVC707PCIeX1IO)
    port <> pcie.module.io.port
    port
  }

  val xdmaPorts = outer.xdma.map { xdma =>
    // Exteriorize and connect ports
    val io = IO(new XDMATopPadswReset(p(XDMAPCIe).get.lanes))
    val ibufds = Module(new IBUFDS_GTE4)
    ibufds.suggestName(s"${name}_refclk_ibufds")
    ibufds.io.CEB := false.B
    ibufds.io.I   := io.refclk.p
    ibufds.io.IB  := io.refclk.n
    xdma.module.io.clocks.sys_clk_gt := ibufds.io.O
    xdma.module.io.clocks.sys_clk := ibufds.io.ODIV2
    xdma.module.io.clocks.sys_rst_n := io.erst_n
    io.lanes <> xdma.module.io.pads

    // Attach the child clock and reset
    // We do not need to use ChildClock and ChildReset for this one
    // I know.. weird...
    xdma.module.clock := xdma.module.io.clocks.axi_aclk
    xdma.module.reset := !io.erst_n // TODO: Not sure if works. Needs to be wrangled

    // Put this as the public member
    io
  }

  // NOTE: Continuation of the clock assignation
  // Extract the number of clocks. According to the clockGroup definition, there is only one clockGroup
  val numClocks: Int = outer.clockGroup.out.map(_._1.member.data.size).sum
  // Create the actual port
  val aclocks = IO(Vec(numClocks, Flipped(new ClockBundle(ClockBundleParameters()))))
  val extclocks = outer.clockGroup.out.flatMap(_._1.member.data)
  val namedclocks = outer.clocksAggregator.out.flatMap(_._1.member.elements).map(A => A._1)
  // Connect the clocks in the hardware
  (extclocks zip aclocks).foreach{ case (o, ai) =>
    o.clock := ai.clock
    o.reset := ai.reset
  }

  // Explicitly export the tlserial port
  val serial_tl = outer.serial_tl
}

class TEEHWSystemModule[+L <: TEEHWSystem](_outer: L)
  extends TEEHWSubsystemModuleImp(_outer)
    with HasTEEHWSystemModule
    // The TEEHW components
    with HasPeripherySHA3ModuleImp
    with HasPeripheryed25519ModuleImp
    with HasPeripheryAESModuleImp
    with HasPeripheryUSB11HSModuleImp
    with HasPeripheryRandomModuleImp
    with HasPeripheryChachaModuleImp
    with HasPeripheryPolyModuleImp
    // The opentitan components
    with HasPeripheryAESOTModuleImp
    with HasPeripheryHMACModuleImp
    with HasPeripheryOTPCtrlModuleImp

object PinGen {
  def apply(): BasePin =  {
    val pin = new BasePin()
    pin
  }
}

//-------------------------------------------------------------------------
// TEEHWPlatformIO
//-------------------------------------------------------------------------

// TODO: Why this does not exist?
class TLUL(val params: TLBundleParameters) extends Bundle {
  val a = Decoupled(new TLBundleA(params))
  val d = Flipped(Decoupled(new TLBundleD(params)))
}
// NOTE: We need an external reset for this PCIe
class XDMATopPadswReset(n: Int) extends XDMATopPads(n) {
  val erst_n = Input(Bool())
}

class TEEHWPlatformIO(val params: Option[TLBundleParameters] = None, val numClocks: Int = 1)
                    (implicit val p: Parameters) extends Bundle {
  val allspicfg = p(PeripherySPIKey) ++ p(PeripherySPIFlashKey)
  val pins = new Bundle {
    val jtag = new JTAGPins(() => PinGen(), false)
    val gpio = new GPIOPins(() => PinGen(), p(PeripheryGPIOKey)(0))
    val uart = new UARTPins(() => PinGen())
    //val i2c = new I2CPins(() => PinGen())
    val spi = MixedVec( allspicfg.map{A => new SPIPins(() => PinGen(), A)} )
  }
  val usb11hs = Vec( p(PeripheryUSB11HSKey).size,  new USB11HSPortIO )
  val jtag_reset = Input(Bool())
  val ndreset = Output(Bool())
  val tlport = params.map{par => new TLUL(par)}
  val ChildClock = p(DDRPortOther).option(Input(Clock()))
  val ChildReset = p(DDRPortOther).option(Input(Bool()))
  val pciePorts = p(IncludePCIe).option(new XilinxVC707PCIeX1IO)
  val xdmaPorts = p(XDMAPCIe).map(A => new XDMATopPadswReset(A.lanes))
  val aclocks = Vec(numClocks, Input(Clock()))
  val tlserial = p(SerialTLKey).map(A => new SerialIO(SERIAL_TSI_WIDTH))
  val memser = p(ExtSerMem).map(A => new SerialIO(A.serWidth))
  val extser = p(ExtSerBus).map(A => new SerialIO(A.serWidth))
}

object TEEHWPlatform {
  def connect
  (
    sys: HasTEEHWSystemModule with HasTilesModuleImp with HasPeripheryUSB11HSModuleImp,
    io: TEEHWPlatformIO,
    clock: Clock,
    reset: Reset)(implicit p: Parameters): Unit = {

    // Add in debug-controlled reset.
    // TODO: Now this is okay? We also use a lot of sys.clock
    sys.reset := ResetCatchAndSync(clock, reset.toBool, 5)

    // Connect the clocks and the resets that are asynchronous
    sys.aclocks.zip(io.aclocks).foreach{ case(sysaclock, ioaclock) =>
      sysaclock.clock := ioaclock
      sysaclock.reset := ResetCatchAndSync(ioaclock, reset.toBool, 5)
    }

    // JTAG & Debug Interface
    sys.debug.foreach({ debug =>
      // We never use the PSDIO, so tie it off on-chip
      sys.psd.psd.foreach { _ <> 0.U.asTypeOf(new PSDTestMode) }
      sys.resetctrl.foreach { rcio => rcio.hartIsInReset.map { _ := reset.toBool } }
      sys.debug.foreach { d =>
        // Tie off extTrigger
        d.extTrigger.foreach { t =>
          t.in.req := false.B
          t.out.ack := t.out.req
        }
        // Tie off dmi
        d.clockeddmi.foreach { d =>
          d.dmi.req.valid := false.B
          d.dmi.resp.ready := true.B
          d.dmiClock := false.B.asClock
          d.dmiReset := true.B.asAsyncReset
        }
        // Tie off APB
        d.apb.foreach { apb =>
          apb.tieoff()
          apb.clock := false.B.asClock
          apb.reset := true.B.asAsyncReset
          apb.psel := false.B
          apb.penable := false.B
        }
        // Tie off disableDebug
        d.disableDebug.foreach { d => d := false.B }
        // Drive JTAG on-chip IOs
        d.systemjtag.foreach { j =>
          j.reset := io.jtag_reset.asAsyncReset // Was only reset
          JTAGPinsFromPort(io.pins.jtag, j.jtag)
          j.mfr_id := p(JtagDTMKey).idcodeManufId.U(11.W)
          j.part_number := p(JtagDTMKey).idcodePartNum.U(16.W)
          j.version := p(JtagDTMKey).idcodeVersion.U(4.W)
        }
        io.ndreset := d.ndreset
      }
      Debug.connectDebugClockAndReset(Some(debug), clock)
    })

    // The TL memory port. This is a configurable one for the address space
    // and the ports are exposed inside the "foreach". Do not worry, there is
    // only one memory (unless you configure multiple memories).

    (sys.memPorts zip io.tlport).foreach{
      case (iohf, tlport: TLUL) =>
        val ioh = iohf._1
        val ChildClock = iohf._2
        val ChildReset = iohf._3
        ioh.foreach{ case ioi: TLBundle =>
          // Connect outside the ones that can be untied
          tlport.a.valid := ioi.a.valid
          ioi.a.ready := tlport.a.ready
          tlport.a.bits := ioi.a.bits

          ioi.d.valid := tlport.d.valid
          tlport.d.ready := ioi.d.ready
          ioi.d.bits := tlport.d.bits

          // Tie off the channels we dont need...
          // ... I mean, we did tell the TLNodeParams that we only want Get and Put
          ioi.b.bits := 0.U.asTypeOf(new TLBundleB(ioi.params))
          ioi.b.valid := false.B
          ioi.c.ready := false.B
          ioi.e.ready := false.B
          // Important NOTE: We did check connections until the mbus in verilog
          // and there is no usage of channels B, C and E (except for some TL Monitors)
        }

        (((ChildClock zip ChildReset) zip io.ChildClock) zip io.ChildReset).map{ case (((cck, crst), ck), rst) =>
          cck := ck
          crst := rst
        }
    }

    // Serialized memory
    (sys.memSerPorts zip io.memser).foreach { case(sysport, ioport) => ioport <> sysport }

    // Serialized external bus
    (sys.extSerPorts zip io.extser).foreach { case(sysport, ioport) => ioport <> sysport }

    // Connect the USB to the outside (only the first one)
    (sys.usb11hs zip io.usb11hs).foreach{ case (sysusb, usbport) => sysusb <> usbport }

    //-----------------------------------------------------------------------
    // Check for unsupported rocket-chip connections
    //-----------------------------------------------------------------------

    require (p(NExtTopInterrupts) == 0, "No Top-level interrupts supported")

    // I2C
    //I2CPinsFromPort(io.pins.i2c, sys.i2c(0), clock = sys.clock, reset = sys.reset.toBool, syncStages = 0)

    // UART0
    UARTPinsFromPort(io.pins.uart, sys.uart(0), clock = sys.clock, reset = sys.reset.toBool, syncStages = 0)

    //-----------------------------------------------------------------------
    // Drive actual Pads
    //-----------------------------------------------------------------------

    // Result of Pin Mux
    GPIOPinsFromPort(io.pins.gpio, sys.gpio(0))

    // Dedicated SPI Pads
    (io.pins.spi zip sys.spi).foreach { case (pins_spi, sys_spi) =>
      SPIPinsFromPort(pins_spi, sys_spi, clock = sys.clock, reset = sys.reset.toBool, syncStages = 3)
    }

    // PCIe port connection
    (io.pciePorts zip sys.pciePorts).foreach{ case (a, b) => a <> b }
    (io.xdmaPorts zip sys.xdmaPorts).foreach{ case (a, b) => a <> b }

    // TL serial
    (io.tlserial zip sys.serial_tl).foreach{ case (a, b) =>
      val serdesser = sys.outer.asInstanceOf[HasTEEHWSystem].serdesser.get
      val ram = SerialAdapter.connectHarnessRAM(serdesser, b, reset)
      a <> ram.module.io.tsi_ser
    }
  }
}

trait WithTEEHWPlatformConnect {
  implicit val p: Parameters
  val clock: Clock
  val reset: Reset
  val io: TEEHWPlatformIO
  val sys: HasTEEHWSystemModule with HasTilesModuleImp with HasPeripheryUSB11HSModuleImp

  TEEHWPlatform.connect(sys, io, clock, reset)(p)
}

trait HasTEEHWPlatform {
  this: Module =>
  implicit val p: Parameters
  val sys: TEEHWSystemModule[TEEHWSystem] = Module(LazyModule(new TEEHWSystem).module)
  val io = IO(new TEEHWPlatformIO(sys.outer.memctl.map{A => A._1.in.head._1.params}, sys.numClocks ) )
}

class TEEHWPlatform(implicit val p: Parameters) extends Module with HasTEEHWPlatform with WithTEEHWPlatformConnect {
}
