// See LICENSE.SiFive for license details.

package uec.teehardware

import chisel3._
import chisel3.experimental.attach
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
import uec.teehardware.devices.sdram._
import uec.teehardware.devices.sifiveblocks._
import uec.teehardware.devices.tlmemext._
import uec.teehardware.devices.usb11hs.HasPeripheryUSB11HSChipImp

object ChangeDetector{
  def applyBool(signal: Bool, name: String = "Unknown"): Bool = {
    val sig = RegInit(false.B)
    sig := signal
    when(sig =/= signal) {
      printf(s"Change for ${name}: %b", signal)
    }
    sig
  }
  def apply(signal: UInt, name: String = "Unknown"): UInt = {
    val sig = RegInit(0.U(signal.getWidth.W))
    sig := signal
    when(sig =/= signal) {
      printf(s"Change for ${name}: %x\n", signal)
    }
    sig
  }
}

// There is no simulation resource for TileLink buses, only for axi (for... some reason)
// this is a LazyModule which instances the SimDRAM in axi4 mode from the TL bus
class TLULtoSimDRAM
(
  cacheBlockBytes: Int,
  TLparams: TLBundleParameters
)(implicit p :Parameters)
  extends LazyModule {
  val beatBytes = if(p(MemoryBusKey).blockBytes > 1024) 8 else p(ExtMem).head.master.beatBytes

  // Create a dummy node where we can attach our TL port
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 1 << TLparams.sourceBits),
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
      supportsWrite = TransferSizes(1, p(MemoryBusKey).blockBytes),
      supportsRead  = TransferSizes(1, p(MemoryBusKey).blockBytes))),
    beatBytes = beatBytes
  )))

  // Attach to the axi4 node (Hopefully does not explode)
  if(p(ExtMem).head.master.beatBytes != beatBytes)
    axinode := yank.node := deint.node := indexer.node := toaxi4.node := buffer.node :=
      TLWidthWidget(p(ExtSerMem).head.master.beatBytes) := node
  else
    axinode := yank.node := deint.node := indexer.node := toaxi4.node := buffer.node :=
      node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlport = Flipped(new TLBundle(TLparams))
    })

    // External TL port connection to the node
    node.out.foreach { case  (bundle, _) => bundle <> io.tlport }

    // axinode connection to the simulated memory provided by chipyard
    val clockFrequency = p(PeripheryBusKey).dtsFrequency.get // TODO: Should be MemoryBusKey
    val memSize = p(ExtMem).get.master.size
    val lineSize = cacheBlockBytes
    axinode.in.foreach { case (io, edge) =>
      val mem = Module(new SimDRAM(memSize, lineSize, clockFrequency, edge.bundle))
      mem.io.axi <> io
      mem.io.clock := clock
      mem.io.reset := reset
    }
  }
}

class SertoSimDRAM(w: Int, cacheBlockBytes: Int, idBits: Int = 6)(implicit p :Parameters)
  extends LazyModule {

  val beatBytes = if(p(MemoryBusKey).blockBytes > 1024) 8 else p(ExtSerMem).head.master.beatBytes

  // Create the desser
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
      supportsWrite = TransferSizes(1, p(MemoryBusKey).blockBytes),
      supportsRead  = TransferSizes(1, p(MemoryBusKey).blockBytes))),
    beatBytes = beatBytes
  )))

  // Attach to the axi4 node (Hopefully does not explode)
  if(p(ExtSerMem).head.master.beatBytes != beatBytes)
     axinode := yank.node := deint.node := indexer.node := toaxi4.node := buffer.node :=
       TLWidthWidget(p(ExtSerMem).head.master.beatBytes) := desser.node
  else
    axinode := yank.node := deint.node := indexer.node := toaxi4.node := buffer.node :=
      desser.node


  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val serport = Flipped(new SerialIO(w))
    })

    // Connect the serport
    io.serport <> desser.module.io.ser.head

    // axinode connection to the simulated memory provided by chipyard
    val clockFrequency = p(PeripheryBusKey).dtsFrequency.get // TODO: Should be MemoryBusKey
    val memSize = p(ExtSerMem).get.master.size
    val lineSize = cacheBlockBytes
    axinode.in.foreach { case (io, edge) =>
      val mem = Module(new SimDRAM(memSize, lineSize, clockFrequency, edge.bundle))
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

  val chip: Any

  // Initial association with io.success
  io.success := false.B

  // IMPORTANT NOTE:
  // The old version yields the debugger and loads using the fevsr. This does not work anymore
  // now, the people from berkeley just decided to directly load the program into a SimDRAM
  // that is just a wrapper for a simulated memory that loads the program there. So, we are
  // going to do exactly the same. That means that debugging the simulation is no longer
  // available unless you re-activate the code below. We are putting it commented, but just
  // notice that this kind of configuration only works on Rocket-only based systems.

  // This is the new way (just look at chipyard.IOBinders package about details)

  // Clocks and resets
  PUT(clock.asBool, chip.asInstanceOf[HasTEEHWClockGroupChipImp].clockxi)
  chip.asInstanceOf[HasTEEHWClockGroupChipImp].aclockxi.foreach( PUT(clock.asBool, _) )
  PUT(!reset.asBool, chip.asInstanceOf[HasTEEHWClockGroupChipImp].rstn)

  // Simulated memory.
  chip.asInstanceOf[HasTEEHWPeripheryExtMemChipImp].mem_tl.foreach{ ioi =>
    // Step 1: Our conversion
    val simdram = LazyModule(new uec.teehardware.TLULtoSimDRAM(p(CacheBlockBytes), ioi.params))
    val simdrammod = Module(simdram.module)
    simdrammod.reset := reset
    // Step 2: Perform the conversion from TL to our TLBundle port (Remember we do in this way because chip)
    ioi.ConnectTLIn(simdrammod.io.tlport)
  }

  chip.asInstanceOf[HasTEEHWPeripheryExtSerMemChipImp].memser.foreach{ A =>
    // Step 1: Our conversion
    val simdram = LazyModule(new SertoSimDRAM(
      p(ExtSerMem).get.serWidth,
      p(CacheBlockBytes),
      A.w))
    val simdrammod = Module(simdram.module)
    simdrammod.reset := reset
    simdram.suggestName("simdrammod")
    A.ConnectIn(simdrammod.io.serport)
  }

  // Debug connections (JTAG)
  chip.asInstanceOf[DebugJTAGOnlyChipImp].jtag.foreach{ jtag =>
    val simjtag = Module(new SimJTAG(tickDelay=3))
    PUT(!reset.asBool, jtag.TRSTn) // NOTE: Normal reset
    PUT(simjtag.io.jtag.TMS, jtag.TMS)
    PUT(simjtag.io.jtag.TDI, jtag.TDI)
    PUT(simjtag.io.jtag.TCK.asBool, jtag.TCK)
    simjtag.io.jtag.TDO.data := GET(jtag.TDO)
    simjtag.io.jtag.TDO.driven := true.B // TODO: We do not have access to this signal anymore

    simjtag.io.clock := clock
    simjtag.io.reset := reset
    simjtag.io.enable := PlusArg("jtag_rbb_enable", 0, "Enable SimJTAG for JTAG Connections. Simulation will pause until connection is made.")
    simjtag.io.init_done := !reset.asBool
    when (simjtag.io.exit === 1.U) { io.success := true.B }
    when (simjtag.io.exit >= 2.U) {
      printf("*** FAILED *** (exit code = %d)\n", simjtag.io.exit >> 1.U)
      assert(false.B)
    }
    // Equivalent of simjtag.connect
  }

  // Serial interface (if existent) will be connected here
  def connectSimSerial(serial: SerialIO, clock: Clock, reset: Reset): Bool
  val tlserial = chip.asInstanceOf[CanHavePeripheryTLSerialChipImp].tlserial
  val serdesser = chip.asInstanceOf[CanHavePeripheryTLSerialChipImp].serdesser
  (tlserial zip serdesser).foreach{ case(port, serdesser) =>
    val bits = SerialAdapter.asyncQueue(port, clock, reset)
    val ram = withClockAndReset(clock, reset) {
      SerialAdapter.connectHarnessRAM(serdesser, bits, reset)
    }
    println(s"Connected SerialAdapter Async with, iwidth=${port.bits.w} bwidth=${bits.w} tsiwidth=${ram.module.io.tsi_ser.w}")

    val ser_success = connectSimSerial(ram.module.io.tsi_ser, clock, reset)
    when (ser_success) { io.success := true.B }
  }

  // Tie down USB11HS
  chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs.foreach{ case usb =>
    usb.USBWireDataIn.foreach(PUT(false.B, _))
    PUT(clock.asBool, usb.usbClk) // TODO: Do an actual 48MHz clock?
  }

  // Connect UART
  chip.asInstanceOf[HasTEEHWPeripheryUARTChipImp].uart.zipWithIndex.foreach{ case(uart, i) =>
    val baudrate = BigInt(115200)
    val clockFrequency = p(PeripheryBusKey).dtsFrequency.get
    val div = (clockFrequency / baudrate).toInt
    val uart_sim = Module(new UARTAdapter(0, div))
    uart_sim.suggestName(s"uart_sim_${i}")
    uart_sim.io.uart.txd := GET(uart.TXD)
    PUT(uart_sim.io.uart.rxd, uart.RXD)
    println(s"UART${i} put into ${baudrate} rate with ${clockFrequency} frequency")
  }

  // Connect QSPI
  // NOTE: Those also does not have a function for tie down
  // but we are totally doing the qspi black box binding from testchipip
  // NOTE2: AS s for the qspi blackbox from testchipip, we just copy it, because
  // It only supports their precious SPIChipIO instead of the mainstream SPIPortIO
  chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].spi.zipWithIndex.foreach { case (port, i: Int) =>
    i match {
      case 1 =>
        println(s"QSPI connection on port ${i}: SimSPIFlashModel(0x20000000)")
        val spi_mem = Module(new SimSPIFlashModel(0x20000000, i, true))
        spi_mem.suggestName(s"spi_mem_${i}")
        spi_mem.io.sck := GET(port.SCK)
        require(port.csWidth == 1, "I don't know what to do with your extra CS bits. Fix me please.")
        (spi_mem.io.cs zip port.CS).foreach{case(a, b) => a := GET(b)}
        spi_mem.io.dq.zip(port.DQ).foreach { case (x, y) =>
          attach(x, y)
        }
        spi_mem.io.reset := reset.asBool
      case _ =>
        println(s"QSPI connection on port ${i} ignored")
    }
  }

  // GPIO monitoring
  chip.asInstanceOf[HasTEEHWPeripheryGPIOChipImp].gpio.zipWithIndex.foreach { case (gpio, i) =>
    val value = GET(gpio)
    ChangeDetector(value, s"GPIO${i}")
  }

  // TODO: Tie down SDRAM
}

class TEEHWHarnessShell(implicit val p: Parameters) extends Module {
  val io = IO(new TEEHWHarnessBundle)
}

class TEEHWHarness()(implicit p: Parameters) extends TEEHWHarnessShell()(p)
  with HasTEEHWChip with WithTEEHWHarnessConnect {

  // NOTE: This function will be overrided by the full version if the serial is isolated
  def connectSimSerial(serial: SerialIO, clock: Clock, reset: Reset): Bool = {
    println("Connecting regular SimSerial")
    SerialAdapter.connectSimSerial(serial, clock, reset)
  }
}

