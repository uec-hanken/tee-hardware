package uec.keystoneAcc.nedochip

import chisel3._
import chisel3.util._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.system._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sifive.blocks.devices.pinctrl.{BasePin, EnhancedPin, EnhancedPinCtrl, Pin, PinCtrl}
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._
import sifive.blocks.devices.i2c._
import sifive.blocks.devices.jtag._
import uec.rocketchip.subsystem._

class NEDOSystem(implicit p: Parameters) extends RocketSubsystem
    with HasHierarchicalBusTopology
//    with HasPeripheryMaskROMSlave // NOTE: This is already included inside the RocketSubsystem
    with HasPeripheryDebug
    with HasPeripheryUART
//    with HasPeripherySPI
    with HasPeripherySPIFlash
    with HasPeripheryGPIO
//    with HasPeripheryI2C
//    with CanHaveMasterAXI4MemPort
//    with CanHaveMasterTLMemPort
{
  // Main memory controller

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
  memTLNode := mbus.toDRAMController(Some("tl"))()

  // SPI to MMC conversion. TODO: There is an intention from Sifive to do MMC, but has to be manual
  val spiDevs = p(PeripherySPIKey).map { ps => SPI.attach(SPIAttachParams(ps, pbus, ibus.fromAsync))}
  val spiNodes = spiDevs.map { ps => ps.ioNode.makeSink() }
  val mmc = new MMCDevice(spiDevs.head.device)
  ResourceBinding {
    Resource(mmc, "reg").bind(ResourceAddress(0))
  }
  val tlclock = new FixedClockResource("tlclk", 50) // 50 is in MHz

  // Regular module creation
  override lazy val module = new NEDOSystemModule(this)
}

class NEDOSystemModule[+L <: NEDOSystem](_outer: L)
  extends RocketSubsystemModuleImp(_outer)
    with HasPeripheryDebugModuleImp
    with HasPeripheryUARTModuleImp
//    with HasPeripherySPIModuleImp
    with HasPeripherySPIFlashModuleImp
    with HasPeripheryGPIOModuleImp
//    with HasPeripheryI2CModuleImp
//    with CanHaveMasterAXI4MemPortModuleImp
//    with CanHaveMasterTLMemPortModuleImp
{
  // Main memory controller
  val mem_tl = IO(HeterogeneousBag.fromNode(outer.memTLNode.in))
  (mem_tl zip outer.memTLNode.in).foreach { case (io, (bundle, _)) => io <> bundle }

  // SPI to MMC conversion
  val spi  = outer.spiNodes.zipWithIndex.map  { case(n,i) => n.makeIO()(ValName(s"spi_$i")) }

  // Regular module creation
  // Reset vector is set to the location of the mask rom
  val maskROMParams = p(PeripheryMaskROMKey)
  global_reset_vector := maskROMParams(0).address.U
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

class NEDOPlatformIO(val params: TLBundleParameters)(implicit val p: Parameters) extends Bundle {
  val pins = new Bundle {
    val jtag = new JTAGPins(() => PinGen(), false)
    val gpio = new GPIOPins(() => PinGen(), p(PeripheryGPIOKey)(0))
    val qspi = new SPIPins(() => PinGen(), p(PeripherySPIFlashKey)(0))
    val uart = new UARTPins(() => PinGen())
    //val i2c = new I2CPins(() => PinGen())
    val spi = new SPIPins(() => PinGen(), p(PeripherySPIKey)(0))
  }
  val jtag_reset = Input(Bool())
  val ndreset = Output(Bool())
  val tlport = new TLUL(params)
}


class NEDOPlatform(implicit val p: Parameters) extends Module {
  val sys = Module(LazyModule(new NEDOSystem).module)

  // Not actually sure if "sys.outer.memTLNode.head.in.head._1.params" is the
  // correct way to get the params... TODO: Get the correct way
  val io = IO(new NEDOPlatformIO(sys.outer.memTLNode.in.head._1.params) )

  // Add in debug-controlled reset.
  sys.reset := ResetCatchAndSync(clock, reset.toBool, 20)

  // The AXI4 memory port. This is a configurable one for the address space
  // and the ports are exposed inside the "foreach". Do not worry, there is
  // only one memory (unless you configure multiple memories).
  /*sys.mem_axi4.foreach{ case i =>
    i.foreach{ case io: AXI4Bundle =>
    }
  }*/

  // The TL memory port. This is a configurable one for the address space
  // and the ports are exposed inside the "foreach". Do not worry, there is
  // only one memory (unless you configure multiple memories).
  sys.mem_tl.foreach{
    case ioi:TLBundle =>
      // Connect outside the ones that can be untied
      io.tlport <> ioi
      // Tie off the channels we dont need...
      // ... I mean, we did tell the TLNodeParams that we only want Get and Put

      ioi.b.bits := 0.U.asTypeOf(new TLBundleB(sys.outer.memTLNode.in.head._1.params))
      ioi.b.valid := false.B
      ioi.c.ready := false.B
      ioi.e.ready := false.B
      // Important NOTE: We did check connections until the mbus in verilog
      // and there is no usage of channels B, C and E (except for some TL Monitors)
  }

  //-----------------------------------------------------------------------
  // Check for unsupported rocket-chip connections
  //-----------------------------------------------------------------------

  require (p(NExtTopInterrupts) == 0, "No Top-level interrupts supported");

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
  SPIPinsFromPort(io.pins.qspi, sys.qspi(0), clock = sys.clock, reset = sys.reset.toBool, syncStages = 3)
  SPIPinsFromPort(io.pins.spi, sys.spi(0), clock = sys.clock, reset = sys.reset.toBool, syncStages = 3)

  // JTAG Debug Interface
  val sjtag = sys.debug.systemjtag.get
  JTAGPinsFromPort(io.pins.jtag, sjtag.jtag)
  sjtag.reset := io.jtag_reset
  sjtag.mfr_id := p(JtagDTMKey).idcodeManufId.U(11.W)

  io.ndreset := sys.debug.ndreset
}
