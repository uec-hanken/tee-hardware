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
import sifive.blocks.devices.pinctrl.{BasePin, EnhancedPin, EnhancedPinCtrl, Pin, PinCtrl}
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._
import sifive.blocks.devices.i2c._
import sifive.blocks.devices.jtag._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import uec.teehardware.devices.aes._
import uec.teehardware.devices.ed25519._
import uec.teehardware.devices.random._
import uec.teehardware.devices.sha3._
import uec.teehardware.devices.usb11hs._
import uec.rocketchip.subsystem._
import chipyard._

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

class TEEHWSystem(implicit p: Parameters) extends BaseSubsystem
    with HasChipyardTiles
    with HasPeripheryDebug
    with HasPeripheryGPIO
    with HasPeripherySHA3
    with HasPeripheryed25519
    with HasPeripheryAES
    with HasPeripheryUSB11HS
    with HasPeripheryRandom
    //    with HasPeripheryI2C
    //    with HasPeripheryUART // NOTE: Already included
    //    with HasPeripherySPIFlash // NOTE: Already included
    //    with HasPeripherySPI // NOTE: Already included
    //    with HasPeripheryMaskROMSlave // NOTE: This is already included inside the RocketSubsystem
    //    with CanHaveMasterAXI4MemPort // NOTE: A TL->Axi4 is already done outside the system
    //    with CanHaveMasterTLMemPort // NOTE: Manually created the TL port
{
  // The clock resource. This is just for put in the DTS the tlclock
  // TODO: Now the clock is derived from the bus that is connected
  // TODO: We need a way now to extract that one in the makefiles
  // Letting this ONLY for compatibility
  val tlclock = new FixedClockResource("tlclk", p(FreqKeyMHz))

  // Main memory controller (TL memory controller)
  val memdevice = new MemoryDevice
  val mainMemParam = TLManagerPortParameters(
    managers = Seq(TLManagerParameters(
      address = AddressSet.misaligned(p(ExtMem).get.master.base,  p(ExtMem).get.master.size),
      resources = memdevice.reg,
      regionType = RegionType.UNCACHED, // cacheable
      executable = true,
      supportsGet = TransferSizes(1, mbus.blockBytes),
      supportsPutFull = TransferSizes(1, mbus.blockBytes),
      supportsPutPartial = TransferSizes(1, mbus.blockBytes),
      fifoId             = Some(0),
      mayDenyPut         = true,
      mayDenyGet         = true
    )),
    beatBytes = p(ExtMem).get.master.beatBytes
  )
  val memTLNode = TLManagerNode(Seq(mainMemParam))
  val island = if(p(DDRPortOther)) {
    //val source = LazyModule(new TLAsyncCrossingSource())
    //val sink = LazyModule(new TLAsyncCrossingSink(AsyncQueueParams(depth = 1, sync = 3, safe = true, narrow = false)))
    //val buffer  = LazyModule(new TLBuffer) // We removed a buffer in the TOP
    val island = LazyModule(new SlowMemIsland(mbus.blockBytes))
    //memTLNode := buffer.node := island.node := mbus.toDRAMController(Some("tl"))()
    island.crossTLIn(island.node) := mbus.toDRAMController(Some("tl"))()
    memTLNode := island.node
    Some(island)
  } else {
    val buffer  = LazyModule(new TLBuffer)
    memTLNode := buffer.node := mbus.toDRAMController(Some("tl"))()
    None
  }


  // SPI to MMC conversion.
  // TODO: There is an intention from Sifive to do MMC, but has to be manual
  // TODO: Also the tlclock binding is manual
  val spiDevs = p(PeripherySPIKey).map { ps => SPIAttachParams(ps).attachTo(this)}
  val spiNodes = spiDevs.map { ps => ps.ioNode.makeSink() }
  val mmc = new MMCDevice(spiDevs.head.device) // Only the first one is mmc
  ResourceBinding {
    Resource(mmc, "reg").bind(ResourceAddress(0))
  }
  spiDevs.foreach { case ps =>
    //tlclock.bind(ps.device)
  }

  // UART implementation. This is the same as HasPeripheryUART
  // TODO: This is done this way instead of "HasPeripheryUART" because we need to do a bind to tlclock
  val uartDevs = p(PeripheryUARTKey).map{
    val divinit = (p(PeripheryBusKey).dtsFrequency.get / 115200).toInt
    ps => UARTAttachParams(ps).attachTo(this)
  }
  val uartNodes = uartDevs.map { ps => ps.ioNode.makeSink }
  uartDevs.foreach{ case ps =>
    //tlclock.bind(ps.device)
  }

  // QSPI flash implementation. This is the same as HasPeripherySPIFlash
  // TODO: This is done this way instead of "HasPeripherySPIFlash" because we need to do a bind to tlclock
  val qspiDevs = p(PeripherySPIFlashKey).map { ps =>
    SPIFlashAttachParams(ps, fBufferDepth = 8).attachTo(this)
  }
  val qspiNodes = qspiDevs.map { ps => ps.ioNode.makeSink() }
  ResourceBinding {
    qspiDevs.foreach{ case ps =>
      val flash = new FlashDevice(ps.device)
      Resource(flash, "reg").bind(ResourceAddress(0)) // NOTE: This is new. Maybe is not intended in this way.
    }
  }
  qspiDevs.foreach { case ps =>
    //tlclock.bind(ps.device)
  }

  // PCIe port export
  val pcie = if(p(IncludePCIe)) {
    val pcie = LazyModule(new XilinxVC707PCIeX1)
    val nodeSlave = TLIdentityNode()
    val nodeMaster = TLIdentityNode()

    // Attach to the PCIe. NOTE: For some reason, they use TLIdentitiNode here. Not sure why tho.
    // Maybe is the fact of just doing a crosstalk here
    pcie.crossTLIn(pcie.slave) := nodeSlave
    pcie.crossTLIn(pcie.control) := nodeSlave
    nodeMaster := pcie.crossTLOut(pcie.master)

    val pciename = Some(s"pcie_0")
    sbus.fromMaster(pciename) { nodeMaster }
    sbus.toFixedWidthSlave(pciename) { nodeSlave }
    ibus.fromSync := pcie.intnode

    Some(pcie)
  }
  else None

  // add Mask ROM devices
  val maskROMs = p(PeripheryMaskROMKey).map { MaskROM.attach(_, cbus) }

  // System module creation
  override lazy val module = new TEEHWSystemModule(this)
}


class TEEHWSystemModule[+L <: TEEHWSystem](_outer: L)
  extends BaseSubsystemModuleImp(_outer)
    with HasChipyardTilesModuleImp
    with HasRTCModuleImp
    with HasPeripheryDebugModuleImp
    with HasPeripheryGPIOModuleImp
    with HasPeripherySHA3ModuleImp
    with HasPeripheryed25519ModuleImp
    with HasPeripheryAESModuleImp
    with HasPeripheryUSB11HSModuleImp
    with HasPeripheryRandomModuleImp
    with HasResetVectorWire
    //    with HasPeripheryI2CModuleImp
    //    with HasPeripheryUARTModuleImp // NOTE: Already included
    //    with HasPeripherySPIFlashModuleImp // NOTE: Already included
    //    with HasPeripherySPIModuleImp // NOTE: Already included
    //    with CanHaveMasterAXI4MemPortModuleImp // NOTE: A TL->Axi4 is already done outside the system
    //    with CanHaveMasterTLMemPortModuleImp // NOTE: Manually created the TL port
{
  // Main memory controller
  val slowmemck = if(p(DDRPortOther)) {
    val slowmemck = IO(new Bundle {
      val ChildClock = Input(Clock())
      val ChildReset = Input(Bool())
    })
    outer.island.get.module.io.ChildClock := slowmemck.ChildClock
    outer.island.get.module.io.ChildReset := slowmemck.ChildReset
    Some(slowmemck)
  }
  else None

  val mem_tl = IO(HeterogeneousBag.fromNode(outer.memTLNode.in))
  (mem_tl zip outer.memTLNode.in).foreach { case (io, (bundle, _)) => io <> bundle }

  // SPI to MMC conversion
  val spi  = outer.spiNodes.zipWithIndex.map  { case(n,i) => n.makeIO()(ValName(s"spi_$i")) }

  // UART implementation
  val uart = outer.uartNodes.zipWithIndex.map { case(n,i) => n.makeIO()(ValName(s"uart_$i")) }

  // QSPI flash implementation.
  val qspi = outer.qspiNodes.zipWithIndex.map { case(n,i) => n.makeIO()(ValName(s"qspi_$i")) }

  val pciePorts = if(p(IncludePCIe)) {
    val p = IO(new XilinxVC707PCIeX1IO)
    p <> outer.pcie.get.module.io.port
    Some(p)
  } else None

  // System module creation
  // Reset vector is set to the location of the first mask rom
  val maskROMParams = p(PeripheryMaskROMKey)
  global_reset_vector := maskROMParams(0).address.U

  tile_inputs.zip(outer.hartIdList).foreach { case(wire, i) =>
    wire.hartid := i.U
    wire.reset_vector := global_reset_vector
  }

  // create file with boom params
  ElaborationArtefacts.add("""core.config""", outer.tiles.map(x => x.module.toString).mkString("\n"))
}

object PinGen {
  def apply(): BasePin =  {
    val pin = new BasePin()
    pin
  }
}

//-------------------------------------------------------------------------
// E300ArtyDevKitPlatformIO
//-------------------------------------------------------------------------

// TODO: Why this does not exist?
class TLUL(val params: TLBundleParameters) extends Bundle {
  val a = Decoupled(new TLBundleA(params))
  val d = Flipped(Decoupled(new TLBundleD(params)))
}

class TEEHWPlatformIO(val params: TLBundleParameters)
                    (implicit val p: Parameters) extends Bundle {
  val pins = new Bundle {
    val jtag = new JTAGPins(() => PinGen(), false)
    val gpio = new GPIOPins(() => PinGen(), p(PeripheryGPIOKey)(0))
    val uart = new UARTPins(() => PinGen())
    //val i2c = new I2CPins(() => PinGen())
    val spi = new SPIPins(() => PinGen(), p(PeripherySPIKey)(0))
    val qspi =
      if(p(PeripherySPIFlashKey).isEmpty) None
      else Some(new SPIPins(() => PinGen(), p(PeripherySPIFlashKey)(0)))
  }
  val usb11hs = if(p(PeripheryUSB11HSKey).nonEmpty) Some(new USB11HSPortIO) else None
  val jtag_reset = Input(Bool())
  val ndreset = Output(Bool())
  val tlport = new TLUL(params)
  val ChildClock = if(p(DDRPortOther)) Some(Input(Clock())) else None
  val ChildReset = if(p(DDRPortOther)) Some(Input(Bool())) else None
  val pciePorts =
    if(p(IncludePCIe)) Some(new XilinxVC707PCIeX1IO)
    else None
}


class TEEHWPlatform(implicit val p: Parameters) extends Module {
  val sys = Module(LazyModule(new TEEHWSystem).module)

  // Not actually sure if "sys.outer.memTLNode.head.in.head._1.params" is the
  // correct way to get the params... TODO: Get the correct way
  val io = IO(new TEEHWPlatformIO(sys.outer.memTLNode.in.head._1.params) )

  // Add in debug-controlled reset.
  sys.reset := ResetCatchAndSync(clock, reset.toBool, 20)

  // NEW: Reset system nows connects each core's reset independently
  sys.resetctrl.map { rcio => rcio.hartIsInReset.map { _ := sys.reset.asBool() }}

  // NEW: The debug system now manually connects the clock and reset
  // Actually this helper only connects the clock, and the reset is from the DMI
  // NOTE: This also connects the new sys.debug.get.dmactiveAck
  Debug.connectDebugClockAndReset(sys.debug, clock)

  // The TL memory port. This is a configurable one for the address space
  // and the ports are exposed inside the "foreach". Do not worry, there is
  // only one memory (unless you configure multiple memories).
  if(p(DDRPortOther)) {
    sys.slowmemck.get.ChildClock := io.ChildClock.get
    sys.slowmemck.get.ChildReset := io.ChildReset.get
  }
  sys.mem_tl.foreach{
    case ioi:TLBundle =>
      // Connect outside the ones that can be untied
      io.tlport.a.valid := ioi.a.valid
      ioi.a.ready := io.tlport.a.ready
      io.tlport.a.bits := ioi.a.bits

      ioi.d.valid := io.tlport.d.valid
      io.tlport.d.ready := ioi.d.ready
      ioi.d.bits := io.tlport.d.bits

      // Tie off the channels we dont need...
      // ... I mean, we did tell the TLNodeParams that we only want Get and Put
      ioi.b.bits := 0.U.asTypeOf(new TLBundleB(sys.outer.memTLNode.in.head._1.params))
      ioi.b.valid := false.B
      ioi.c.ready := false.B
      ioi.e.ready := false.B
      // Important NOTE: We did check connections until the mbus in verilog
      // and there is no usage of channels B, C and E (except for some TL Monitors)
  }

  // Connect the USB to the outside (only the first one)
  if(sys.usb11hs.nonEmpty) io.usb11hs.get <> sys.usb11hs(0)

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
  if(p(PeripherySPIFlashKey).nonEmpty)
    SPIPinsFromPort(io.pins.qspi.get, sys.qspi(0), clock = sys.clock, reset = sys.reset.toBool, syncStages = 3)
  SPIPinsFromPort(io.pins.spi, sys.spi(0), clock = sys.clock, reset = sys.reset.toBool, syncStages = 3)

  // JTAG Debug Interface
  // TODO: Now the debug is optional? The get will fail if the debug is disabled
  val sjtag = sys.debug.get.systemjtag.get
  JTAGPinsFromPort(io.pins.jtag, sjtag.jtag)
  sjtag.reset := io.jtag_reset
  sjtag.mfr_id := p(JtagDTMKey).idcodeManufId.U(11.W)
  sjtag.part_number := p(JtagDTMKey).idcodePartNum.U(16.W)
  sjtag.version := p(JtagDTMKey).idcodeVersion.U(4.W)

  sys.debug.foreach { case debug =>
    io.ndreset := debug.ndreset
  }

  // PCIe port connection
  if(p(IncludePCIe)) {
    io.pciePorts.get <> sys.pciePorts.get
  }
}
