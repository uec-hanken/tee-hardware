// See LICENSE.SiFive for license details.

package uec.teehardware

import chisel3._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.debug.Debug
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.util.AsyncResetReg
import freechips.rocketchip.system._
import freechips.rocketchip.tilelink._
import sifive.blocks.devices.gpio.GPIOPortIO
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._
import testchipip.{SerialAdapter, SimDRAM, SimSPIFlashModel}

// There is no simulation resource for TileLink buses, only for axi (for... some reason)
// this is a LazyModule which instances the SimDRAM in axi4 mode from the TL bus
class TLULtoSimDRAM( cacheBlockBytes: Int,
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
  val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("mem"), stripBits = 1))
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
  axinode := yank.node := deint.node := indexer.node := toaxi4.node := node

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

class TEEHWHarness()(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val ldut = LazyModule(new TEEHWSystem)
  val dut = Module(ldut.module)

  // IMPORTANT NOTE:
  // The old version yields the debugger and loads using the fevsr. This does not work anymore
  // now, the people from berkeley just decided to directly load the program into a SimDRAM
  // that is just a wrapper for a simulated memory that loads the program there. So, we are
  // going to do exactly the same. That means that debugging the simulation is no longer
  // available unless you re-activate the code below. We are putting it commented, but just
  // notice that this kind of configuration only works on Rocket-only based systems.

  // This is the new way (just look at chipyard.IOBinders package about details)

  // Simulated memory.
  dut.memPorts.foreach{
    case (ioh, cclk, crst) =>
      ioh.foreach { case ioi: TLBundle =>
        // Step 1: Our conversion
        val simdram = LazyModule(new TLULtoSimDRAM(ldut.p(CacheBlockBytes), ioi.params))
        val simdrammod = Module(simdram.module)
        // Step 2: Perform the conversion from TL to our TLUL port (Remember we do in this way because chip)

        // Connect outside the ones that can be untied
        simdrammod.io.tlport.a.valid := ioi.a.valid
        ioi.a.ready := simdrammod.io.tlport.a.ready
        simdrammod.io.tlport.a.bits := ioi.a.bits

        ioi.d.valid := simdrammod.io.tlport.d.valid
        simdrammod.io.tlport.d.ready := ioi.d.ready
        ioi.d.bits := simdrammod.io.tlport.d.bits

        // Tie off the channels we dont need...
        ioi.b.bits := 0.U.asTypeOf(new TLBundleB(ioi.params))
        ioi.b.valid := false.B
        ioi.c.ready := false.B
        ioi.e.ready := false.B
        // REMEMBER: no usage of channels B, C and E (except for some TL Monitors)

        // If the other-clock-memory is activated, we need to associate the clock and the reset
        // NOTE: Please consider that supporting other-clock is not on the boundaries of this
        // simulation. Please refrain of activating DDRPortOther
        (cclk zip crst).foreach {
          case (ck,rst) =>
            ck := clock
            rst := reset
        }
      }
  }

  // Debug connections (This also handles the reset system)
  val debug_success = WireInit(false.B)
  if(dut.debug.nonEmpty) {
    // Debug tie off, only if there is dmi
    if(dut.debug.get.systemjtag.isEmpty) {
      Debug.tieoffDebug(dut.debug, dut.resetctrl, Some(dut.psd))
      dut.debug.foreach { d =>
        d.clockeddmi.foreach({ cdmi => cdmi.dmi.req.bits := DontCare; cdmi.dmiClock := clock })
        d.dmactiveAck := DontCare
        d.clock := clock
      }
    }

    // If the debug have JTAG, then connect it
    else {
      Debug.connectDebug(dut.debug, dut.resetctrl, dut.psd, clock, reset.asBool(), debug_success)
    }
  }

  // Don't touch stuff (avoids firrtl of cutting down hardware because Dead Code Elimination)
  dut.dontTouchPorts()

  // Serial interface (if existent) will be connected here
  io.success := false.B
  val ser_success = SerialAdapter.connectSimSerial(dut.serial, clock, reset)
  when (ser_success) { io.success := true.B }
  when (debug_success) { io.success := true.B }

  // Tie down USB11HS
  dut.usb11hs.foreach{ case usb =>
    usb.USBWireDataIn := 0.U
    //usb.vBusDetect := true.B
    usb.usbClk := clock // TODO: Do an actual 48MHz clock?
  }

  // Tie down UART
  // NOTE: Why UART does not have a function for tie down that?
  // Only exist testchipip.UARTAdapter.connect
  UARTAdapter.connect(uart = dut.uart, baudrate = BigInt(115200)) // If you want it, just uncomment it
  /*dut.uart.foreach{ case uart:UARTPortIO =>
      uart.rxd := true.B
      // uart.txd ignored
  }*/ // If you want to activate the tie down, uncomment these

  // Tie down qspi and spi
  // NOTE: Those also does not have a function for tie down
  // but we are totally doing the qspi black box binding from testchipip
  // NOTE2: AS s for the qspi blackbox from testchipip, we just copy it, because
  // It only supports their precious SPIChipIO instead of the mainstream SPIPortIO
  dut.spi.foreach {case port:SPIPortIO =>
    port.dq.foreach(_.i := false.B)
  }
  dut.qspi.zip(ldut.p(PeripherySPIFlashKey)).zipWithIndex.foreach { case ((port: SPIPortIO, params), i) =>
    val spi_mem = Module(new SimSPIFlashModel(params.fSize, i, true))
    spi_mem.suggestName(s"spi_mem_${i}")
    spi_mem.io.sck := port.sck
    require(params.csWidth == 1, "I don't know what to do with your extra CS bits. Fix me please.")
    spi_mem.io.cs(0) := port.cs(0)
    spi_mem.io.dq.zip(port.dq).foreach { case (x, y) => x <> y }
    spi_mem.io.reset := reset.asBool
  }

  // GPIO tie down
  dut.gpio.foreach{case gpio:GPIOPortIO =>
    gpio.pins.foreach{ case pin =>
      pin.i.ival := false.B
    }
    gpio.iof_0.foreach{ case iof =>
      iof.foreach{ case u =>
        u.default()
      }
    }
    gpio.iof_1.foreach{ case iof =>
      iof.foreach{ case u =>
        u.default()
      }
    }
  }

  // PCIE tie down, but seriously, who is going to use it?
  // Answer: please don't
  dut.pciePorts.foreach{
    case pcie =>
      pcie.axi_aresetn := false.B
      pcie.pci_exp_rxp := false.B
      pcie.pci_exp_rxn := false.B
      pcie.REFCLK_rxp := false.B
      pcie.REFCLK_rxn := false.B
  }

}
