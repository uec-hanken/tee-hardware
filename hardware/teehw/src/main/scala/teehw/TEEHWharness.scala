// See LICENSE.SiFive for license details.

package uec.teehardware

import chisel3._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.debug.{Debug, SimJTAG}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.util.{AsyncResetReg, PlusArg, ResetCatchAndSync}
import freechips.rocketchip.system._
import freechips.rocketchip.tilelink._
import sifive.blocks.devices.gpio.GPIOPortIO
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._
import testchipip.{SerialAdapter, SerialIO, SimDRAM, SimSPIFlashModel, TLDesser}

// There is no simulation resource for TileLink buses, only for axi (for... some reason)
// this is a LazyModule which instances the SimDRAM in axi4 mode from the TL bus
class TLULtoSimDRAM
(
  cacheBlockBytes: Int,
  TLparams: TLBundleParameters
)(implicit p :Parameters)
  extends LazyModule {

  // Create a dummy node where we can attach our TL port
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 64), // CKDUR: The maximum ID possible goes here.
      ))
    )
  })

  // Create the AXI4 Helper nodes to do connection
  val buffer  = LazyModule(new TLBuffer)
  val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("mem")))
  val indexer = LazyModule(new AXI4IdIndexer(idBits = 4))
  val deint   = LazyModule(new AXI4Deinterleaver(p(CacheBlockBytes)))
  val yank    = LazyModule(new AXI4UserYanker)
  val device = new MemoryDevice // I Still dont know why I need to create a device, but is not being added to the DTC
  val axinode = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = AddressSet.misaligned(
        p(ExtMem).get.master.base,
        p(ExtMem).get.master.size
      ),
      resources     = device.reg,
      regionType    = RegionType.UNCACHED,
      executable    = true,
      supportsWrite = TransferSizes(1, cacheBlockBytes),
      supportsRead  = TransferSizes(1, cacheBlockBytes))),
    beatBytes = p(ExtMem).head.master.beatBytes
  )))

  // Attach to the axi4 node (Hopefully does not explode)
  axinode := yank.node := deint.node := indexer.node := toaxi4.node := buffer.node := node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlport = Flipped(new TLUL(TLparams))
    })

    // External TL port connection to the node
    node.out.foreach {
      case  (bundle, _) =>
        bundle.a.valid := io.tlport.a.valid
        io.tlport.a.ready := bundle.a.ready
        bundle.a.bits := io.tlport.a.bits

        io.tlport.d.valid := bundle.d.valid
        bundle.d.ready := io.tlport.d.ready
        io.tlport.d.bits := bundle.d.bits

        // Unused ports
        //bundle.b.bits := (new TLBundleB(TLparams)).fromBits(0.U)
        bundle.b.ready := true.B
        bundle.c.valid := false.B
        //bundle.c.bits := 0.U.asTypeOf(new TLBundleC(TLparams))
        bundle.e.valid := false.B
        //bundle.e.bits := 0.U.asTypeOf(new TLBundleE(TLparams))
    }

    // axinode connection to the simulated memory provided by chipyard
    val memSize = p(ExtMem).get.master.size
    val lineSize = cacheBlockBytes
    axinode.in.foreach { case (io, edge) =>
      val mem = Module(new SimDRAM(memSize, lineSize, edge.bundle))
      mem.io.axi <> io
      mem.io.clock := clock
      mem.io.reset := reset
    }
  }
}

class SertoSimDRAM(w: Int, cacheBlockBytes: Int)(implicit p :Parameters)
  extends LazyModule {

  // Create the desser
  val idBits = 6
  val params = Seq(TLMasterParameters.v1(
    name = "tl-desser",
    sourceId = IdRange(0, 1 << idBits)))
  val desser = LazyModule(new TLDesser(w, params, true))

  // Create the AXI4 Helper nodes to do connection
  val buffer  = LazyModule(new TLBuffer)
  val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("mem")))
  val indexer = LazyModule(new AXI4IdIndexer(idBits = 4))
  val deint   = LazyModule(new AXI4Deinterleaver(p(CacheBlockBytes)))
  val yank    = LazyModule(new AXI4UserYanker)
  val device = new MemoryDevice // I Still dont know why I need to create a device, but is not being added to the DTC
  val axinode = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = AddressSet.misaligned(
        p(ExtSerMem).get.master.base,
        p(ExtSerMem).get.master.size
      ),
      resources     = device.reg,
      regionType    = RegionType.UNCACHED,
      executable    = true,
      supportsWrite = TransferSizes(1, cacheBlockBytes),
      supportsRead  = TransferSizes(1, cacheBlockBytes))),
    beatBytes = p(ExtSerMem).head.master.beatBytes
  )))

  // Attach to the axi4 node (Hopefully does not explode)
  axinode := yank.node := deint.node := indexer.node := toaxi4.node := buffer.node := desser.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val serport = new SerialIO(w)
    })

    // Connect the serport
    io.serport <> desser.module.io.ser.head

    // axinode connection to the simulated memory provided by chipyard
    val memSize = p(ExtSerMem).get.master.size
    val lineSize = cacheBlockBytes
    axinode.in.foreach { case (io, edge) =>
      val mem = Module(new SimDRAM(memSize, lineSize, edge.bundle))
      mem.io.axi <> io
      mem.io.clock := clock
      mem.io.reset := reset
    }
  }
}

class TEEHWHarnessBundle extends Bundle {
  val success = Output(Bool())
}

trait WithTEEHWHarnessConnect {
  implicit val p: Parameters
  val clock: Clock
  val reset: Reset
  val io: TEEHWHarnessBundle

  val dut: WithTEEHWPlatformConnect

  // A delayed reset to be handled properly, as the internal reset is embedded with the same sync
  val del_reset = ResetCatchAndSync(clock, reset.asBool, 5)

  // IMPORTANT NOTE:
  // The old version yields the debugger and loads using the fevsr. This does not work anymore
  // now, the people from berkeley just decided to directly load the program into a SimDRAM
  // that is just a wrapper for a simulated memory that loads the program there. So, we are
  // going to do exactly the same. That means that debugging the simulation is no longer
  // available unless you re-activate the code below. We are putting it commented, but just
  // notice that this kind of configuration only works on Rocket-only based systems.

  // This is the new way (just look at chipyard.IOBinders package about details)

  // Async clocks and resets
  dut.io.aclocks.foreach( _ := clock)

  // Simulated memory.
  dut.io.tlport.foreach{
    case ioi: TLUL =>
      // Step 1: Our conversion
      val simdram = LazyModule(new uec.teehardware.TLULtoSimDRAM(dut.p(CacheBlockBytes), ioi.params))
      val simdrammod = Module(simdram.module)
      // Step 2: Perform the conversion from TL to our TLUL port (Remember we do in this way because chip)

      // Connect outside the ones that can be untied
      simdrammod.io.tlport.a.valid := ioi.a.valid
      ioi.a.ready := simdrammod.io.tlport.a.ready
      simdrammod.io.tlport.a.bits := ioi.a.bits

      ioi.d.valid := simdrammod.io.tlport.d.valid
      simdrammod.io.tlport.d.ready := ioi.d.ready
      ioi.d.bits := simdrammod.io.tlport.d.bits
      // REMEMBER: no usage of channels B, C and E (except for some TL Monitors)

      // If the other-clock-memory is activated, we need to associate the clock and the reset
      // NOTE: Please consider that supporting other-clock is not on the boundaries of this
      // simulation. Please refrain of activating DDRPortOther
      (dut.io.ChildClock zip dut.io.ChildReset).foreach {
        case (ck,rst) =>
          ck := clock
          rst := reset // NOTE: Normal reset
      }
  }

  dut.io.memser.foreach{ A =>
    // Step 1: Our conversion
    val simdram = LazyModule(new uec.teehardware.SertoSimDRAM(p(ExtSerMem).get.serWidth, dut.p(CacheBlockBytes)))
    val simdrammod = Module(simdram.module)

    simdrammod.io.serport.flipConnect(A)
  }

  // Debug connections (JTAG)
  dut.io.jtag_reset := reset // NOTE: Normal reset
  val simjtag = Module(new SimJTAG(tickDelay=3))
  // Equivalent of simjtag.connect
  BasePinToRegular(dut.io.pins.jtag.TCK, simjtag.io.jtag.TCK.asBool)
  BasePinToRegular(dut.io.pins.jtag.TMS, simjtag.io.jtag.TMS)
  BasePinToRegular(dut.io.pins.jtag.TDI, simjtag.io.jtag.TDI)
  simjtag.io.jtag.TDO.data := BasePinToRegular(dut.io.pins.jtag.TDO)
  simjtag.io.jtag.TDO.driven := dut.io.pins.jtag.TDO.o.oe
  simjtag.io.clock := clock
  simjtag.io.reset := del_reset
  simjtag.io.enable := PlusArg("jtag_rbb_enable", 0, "Enable SimJTAG for JTAG Connections. Simulation will pause until connection is made.")
  simjtag.io.init_done := !del_reset.asBool
  when (simjtag.io.exit === 1.U) { io.success := true.B }
  when (simjtag.io.exit >= 2.U) {
    printf("*** FAILED *** (exit code = %d)\n", simjtag.io.exit >> 1.U)
    stop(1)
  }

  // Don't touch stuff (avoids firrtl of cutting down hardware because Dead Code Elimination)
  //dut.dontTouchPorts()

  // Serial interface (if existent) will be connected here
  io.success := false.B
  dut.io.tlserial.foreach{ port =>
    val ser_success = SerialAdapter.connectSimSerial(port, clock, del_reset)
    when (ser_success) { io.success := true.B }
  }

  // Tie down USB11HS
  dut.io.usb11hs.foreach{ case usb =>
    usb.USBWireDataIn := 0.U
    //usb.vBusDetect := true.B
    usb.usbClk := clock // TODO: Do an actual 48MHz clock?
  }

  // Tie down UART
  // NOTE: Why UART does not have a function for tie down that?
  // Only exist testchipip.UARTAdapter.connect


  val baudrate = BigInt(115200)
  val clockFrequency = dut.p(PeripheryBusKey).dtsFrequency.get
  val div = (clockFrequency / baudrate).toInt
  val uart_sim = Module(new UARTAdapter(0, div))
  uart_sim.suggestName(s"uart_sim_0")
  uart_sim.io.uart.txd := BasePinToRegular(dut.io.pins.uart.txd)
  BasePinToRegular(dut.io.pins.uart.rxd, uart_sim.io.uart.rxd)

  // Tie down qspi and spi
  // NOTE: Those also does not have a function for tie down
  // but we are totally doing the qspi black box binding from testchipip
  // NOTE2: AS s for the qspi blackbox from testchipip, we just copy it, because
  // It only supports their precious SPIChipIO instead of the mainstream SPIPortIO
  dut.io.pins.spi.zipWithIndex.foreach {
    case (port, i: Int) =>
      i match {
        case 1 =>
          val spi_mem = Module(new SimSPIFlashModel(0x20000000, i, true))
          spi_mem.suggestName(s"spi_mem_${i}")
          spi_mem.io.sck := BasePinToRegular(port.sck)
          //require(params.csWidth == 1, "I don't know what to do with your extra CS bits. Fix me please.")
          spi_mem.io.cs(0) := BasePinToRegular(port.cs(0))
          /*spi_mem.io.dq.zip(port.dq).foreach { case (x, y) =>
            x <> y
          }*/
          spi_mem.io.reset := del_reset.asBool
        case _ =>
          BasePinToRegular(port.sck)
          BasePinToRegular(port.cs)
          BasePinToRegular(port.dq)
      }
  }

  // GPIO tie down
  Option(dut.io.pins.gpio).foreach { case gpio =>
    gpio.pins.foreach {
      BasePinToRegular(_)
    }
  }

  // PCIE tie down, but seriously, who is going to use it?
  // Answer: please don't
  dut.io.pciePorts.foreach{
    case pcie =>
      pcie.axi_aresetn := false.B
      pcie.pci_exp_rxp := false.B
      pcie.pci_exp_rxn := false.B
      pcie.REFCLK_rxp := false.B
      pcie.REFCLK_rxn := false.B
  }
}

trait HasTEEHWHarness {
  this: Module =>
  implicit val p: Parameters

  val io = IO(new TEEHWHarnessBundle)
  val dut = Module(new TEEHWPlatform)
}

class TEEHWHarness()(implicit val p: Parameters) extends Module with HasTEEHWHarness with WithTEEHWHarnessConnect {
}
