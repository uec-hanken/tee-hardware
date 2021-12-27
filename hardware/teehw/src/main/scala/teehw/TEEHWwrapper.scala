package uec.teehardware

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.{BaseSubsystem, ExtMem, MemoryBusKey, SbusToMbusXTypeKey}
import freechips.rocketchip.tilelink.{TLBundleD, TLBundleParameters}
import freechips.rocketchip.util._
import sifive.blocks.devices.pinctrl._
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.fpgashells.clocks._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import uec.teehardware.devices.usb11hs._
import freechips.rocketchip.util._
import sifive.fpgashells.shell.xilinx.XDMATopPads
import testchipip.SerialIO
import uec.teehardware.devices.clockctrl.ClockCtrlPortIO
import uec.teehardware.shell._

// **********************************************************************
// **TEEHW chip - for doing the only-input/output chip
// **********************************************************************

class TEEHWQSPIBundle(val csWidth: Int = 1) extends Bundle {
  val qspi_cs = (Output(UInt(csWidth.W)))
  val qspi_sck = (Output(Bool()))
  val qspi_miso = (Input(Bool()))
  val qspi_mosi = (Output(Bool()))
  val qspi_wp = (Output(Bool()))
  val qspi_hold = (Output(Bool()))
}

class  WithTEEHWbaseShell(implicit val p :Parameters) extends RawModule {
  // The actual pins of this module.
  val gpio_in = IO(Input(UInt(p(GPIOInKey).W)))
  val gpio_out = IO(Output(UInt((p(PeripheryGPIOKey).head.width-p(GPIOInKey)).W)))
  val jtag = IO(new Bundle {
    val jtag_TDI = (Input(Bool()))
    val jtag_TDO = (Output(Bool()))
    val jtag_TCK = (Input(Bool()))
    val jtag_TMS = (Input(Bool()))
  })
  val sdio = IO(new Bundle {
    val sdio_clk = (Output(Bool()))
    val sdio_cmd = (Output(Bool()))
    val sdio_dat_0 = (Input(Bool()))
    val sdio_dat_1 = (Analog(1.W))
    val sdio_dat_2 = (Analog(1.W))
    val sdio_dat_3 = (Output(Bool()))
  })
  val uart_txd = IO(Output(Bool()))
  val uart_rxd = IO(Input(Bool()))
  val usb11hs = p(PeripheryUSB11HSKey).map{ _ => IO(new USB11HSPortIO)}
  val pciePorts = p(IncludePCIe).option(IO(new XilinxVC707PCIeX1IO))
  val xdmaPorts = p(XDMAPCIe).map(A => IO(new XDMATopPadswReset(A.lanes)))
  // Memory port serialized
  val memser = p(ExtSerMem).map(A => IO(new SerialIO(A.serWidth)))
  // Ext port serialized
  val extser = p(ExtSerBus).map(A => IO(new SerialIO(A.serWidth)))
  // Clocks and resets
  val ChildClock = p(DDRPortOther).option(IO(Input(Clock())))
  val ChildReset = p(DDRPortOther).option(IO(Input(Bool())))
  val sys_clk = IO(Input(Clock()))
  val rst_n = IO(Input(Bool()))
  val jrst_n = IO(Input(Bool()))
  // An option to dynamically assign
  var qspi: Option[TEEHWQSPIBundle] = None // QSPI gets added progresively using both PeripherySPIKey and PeripherySPIFlashKey
  var aclocks: Option[Vec[Clock]] = None // Async clocks depends on a node of clocks named "globalClocksNode"
  var tlport: Option[TLUL] = None // The TL port depends of a node, and a edge, for parameters
}

trait WithTEEHWbaseConnect {
  this: WithTEEHWbaseShell =>
  implicit val p: Parameters

  val clock : Clock
  val reset : Bool // System reset (for cores)
  val system: WithTEEHWPlatformConnect

  val cacheBlockBytes = system.sys.outer.asInstanceOf[BaseSubsystem].mbus.blockBytes

  // Merge all the gpio vector
  val vgpio_in = VecInit(gpio_in.toBools)
  val vgpio_out = Wire(Vec(p(PeripheryGPIOKey).head.width-p(GPIOInKey), Bool()))
  gpio_out := vgpio_out.asUInt()
  val gpio = vgpio_in ++ vgpio_out
  // GPIOs
  (gpio zip system.io.pins.gpio.pins).zipWithIndex.foreach {
    case ((g: Bool, pin: BasePin), i: Int) =>
      if(i < p(GPIOInKey)) BasePinToRegular(pin, g)
      else g := BasePinToRegular(pin)
  }

  // JTAG
  BasePinToRegular(system.io.pins.jtag.TMS, jtag.jtag_TMS)
  BasePinToRegular(system.io.pins.jtag.TCK, jtag.jtag_TCK)
  BasePinToRegular(system.io.pins.jtag.TDI, jtag.jtag_TDI)
  jtag.jtag_TDO := BasePinToRegular(system.io.pins.jtag.TDO)

  // QSPI (SPI as flash memory)
  qspi = (system.io.pins.spi.size >= 2).option( IO ( new TEEHWQSPIBundle(system.io.pins.spi(1).cs.size) ) )
  qspi.foreach { portspi =>
    portspi.qspi_cs := BasePinToRegular(system.io.pins.spi(1).cs.head)
    portspi.qspi_sck := BasePinToRegular(system.io.pins.spi(1).sck)
    portspi.qspi_mosi := BasePinToRegular(system.io.pins.spi(1).dq(0))
    BasePinToRegular(system.io.pins.spi(1).dq(1), portspi.qspi_miso)
    portspi.qspi_wp := BasePinToRegular(system.io.pins.spi(1).dq(2))
    portspi.qspi_hold := BasePinToRegular(system.io.pins.spi(1).dq(3))
  }

  // SPI (SPI as SD?)
  sdio.sdio_dat_3 := BasePinToRegular(system.io.pins.spi(0).cs.head)
  sdio.sdio_clk := BasePinToRegular(system.io.pins.spi(0).sck)
  sdio.sdio_cmd := BasePinToRegular(system.io.pins.spi(0).dq(0))
  BasePinToRegular(system.io.pins.spi(0).dq(1), sdio.sdio_dat_0)
  BasePinToRegular(system.io.pins.spi(0).dq(2)) // Ignored
  BasePinToRegular(system.io.pins.spi(0).dq(3)) // Ignored

  // UART
  BasePinToRegular(system.io.pins.uart.rxd, uart_rxd)
  uart_txd := BasePinToRegular(system.io.pins.uart.txd)

  // USB11
  (usb11hs zip system.io.usb11hs).foreach{ case (port, sysport) => port <> sysport }

  // The memory port
  val memdevice = Some(new MemoryDevice)
  (ChildClock zip system.io.ChildClock).foreach{ case (port, sysport) => sysport := port }
  (ChildReset zip system.io.ChildReset).foreach{ case (port, sysport) => sysport := port }

  // The serialized memory port
  (memser zip system.io.memser).foreach{ case (port, sysport) => port <> sysport }

  // The serialized external port
  (extser zip system.io.extser).foreach{ case (port, sysport) => port <> sysport }

  // PCIe port (if available)
  (pciePorts zip system.io.pciePorts).foreach{ case (port, sysport) => port <> sysport }
  (xdmaPorts zip system.io.xdmaPorts).foreach{ case (port, sysport) => port <> sysport }

  // TL external memory port
  val tlparam = system.io.tlport.map(tl => tl.params)
  tlport = tlparam.map{tl => IO(new TLUL(tl))}
  // TL port connection
  (system.io.tlport zip tlport).foreach{case (base, chip) =>
    chip.a <> base.a
    base.d <> chip.d
  }
  // A helper function for alterate the number of bits of the id
  def tlparamsOtherId(n: Int) = {
    tlparam.map{param =>
      param.copy(sourceBits = n)
    }
  }

  // Clock and reset connection
  val aclkn = p(ExposeClocks).option(system.io.aclocks.size)
  aclocks = aclkn.map(A => IO(Vec(A, Input(Clock()))))
  clock := sys_clk
  if(p(ExposeClocks)) system.io.aclocks := aclocks.get
  else {
    system.io.aclocks.foreach(_ := sys_clk)
  }
  reset := !rst_n || system.io.ndreset // This connects the debug reset and the general reset together
  system.io.jtag_reset := !jrst_n
}

trait HasTEEHWbase {
  this: RawModule =>
  implicit val p: Parameters
  // All the modules declared here have this clock and reset
  val clock = Wire(Clock())
  val reset = Wire(Bool()) // System reset (for cores)
  val system = withClockAndReset(clock, reset) {
    // The platform module
    Module(new TEEHWPlatform)
  }
  system.suggestName("system")
}

class TEEHWSoC(implicit p :Parameters) extends WithTEEHWbaseShell()(p) with HasTEEHWbase with WithTEEHWbaseConnect {
}

trait HasTEEHWChip {
  implicit val p: Parameters
  val chip = Module(new TEEHWSoC)
  chip.suggestName("chip")
}

// ********************************************************************
// General traits for FPGA connections to the chip
// ********************************************************************

trait FPGAInternals {
  implicit val p: Parameters
  def outer : Option [WithTEEHWbaseShell with WithTEEHWbaseConnect]
  def otherId : Option[Int] = None
  // Parameters that depends on the generated system (not only the config)
  def tlparam: Option[TLBundleParameters] = otherId.map(outer.get.tlparamsOtherId).getOrElse(outer.get.tlparam)
  def aclkn: Option[Int] = outer.get.aclkn
  def memserSourceBits: Option[Int] = outer.get.system.sys.asInstanceOf[HasTEEHWSystemModule].serSourceBits
  def extserSourceBits: Option[Int] = outer.get.system.sys.asInstanceOf[HasTEEHWSystemModule].extSourceBits
  def namedclocks: Seq[String] = outer.get.system.sys.asInstanceOf[HasTEEHWSystemModule].namedclocks
  // Clocks and resets
  val ChildClock = p(DDRPortOther).option(IO(Output(Clock())))
  val ChildReset = p(DDRPortOther).option(IO(Output(Bool())))
  val sys_clk = IO(Output(Clock()))
  val rst_n = IO(Output(Bool()))
  val jrst_n = IO(Output(Bool()))
  val usbClk = p(PeripheryUSB11HSKey).map(A => IO(Output(Clock())))
  // Memory port serialized
  val memser = p(ExtSerMem).map(A => IO(Flipped(new SerialIO(A.serWidth))))
  // Ext port serialized
  val extser = p(ExtSerBus).map(A => IO(Flipped(new SerialIO(A.serWidth))))
  // Memory port
  var tlport = tlparam.map(A => IO(Flipped(new TLUL(A))))
  // Asyncrhonoys clocks
  var aclocks = aclkn.map(A => IO(Vec(A, Output(Clock()))))

  def connectChipInternals(chip: WithTEEHWbaseShell with WithTEEHWbaseConnect) = {
    (chip.ChildClock zip ChildClock).foreach{ case (a, b) => a := b }
    (chip.ChildReset zip ChildReset).foreach{ case (a, b) => a := b }
    chip.sys_clk := sys_clk
    chip.rst_n := rst_n
    chip.jrst_n := jrst_n
    // Memory port serialized
    (chip.memser zip memser).foreach{ case (a, b) => a <> b }
    // Ext port serialized
    (chip.extser zip extser).foreach{ case (a, b) => a <> b }
    // Memory port
    (chip.tlport zip tlport).foreach{ case (a, b) => b.a <> a.a; a.d <> b.d }
    // Asyncrhonoys clocks
    (chip.aclocks zip aclocks).foreach{ case (a, b) => (a zip b).foreach{ case (c, d) => c := d} }
  }
}

// ********************************************************************
// FPGAVC707 - Demo on VC707 FPGA board
// ********************************************************************
class FPGAVC707(implicit p :Parameters) extends FPGAVC707Shell()(p)
  with HasTEEHWChip with WithFPGAVC707Connect {
}

class FPGAVC707ToChip(implicit p :Parameters) extends FPGAVC707Shell()(p)
  with WithFPGAVC707ToChipConnect {
}

class FPGAVC707FromChip(implicit p :Parameters) extends FPGAVC707Shell()(p)
  with HasTEEHWChip with WithFPGAVC707FromChipConnect {
}

// ********************************************************************
// FPGAVCU118 - Demo on VCU118 FPGA board
// ********************************************************************
class FPGAVCU118(implicit p :Parameters) extends FPGAVCU118Shell()(p)
  with HasTEEHWChip with WithFPGAVCU118Connect {
}

// ********************************************************************
// FPGAArtyA7 - Demo on Arty A7 100 FPGA board
// ********************************************************************
class FPGAArtyA7(implicit p :Parameters) extends FPGAArtyA7Shell()(p)
  with HasTEEHWChip with WithFPGAArtyA7Connect {
}

// ********************************************************************
// FPGADE4 - Demo on DE4 FPGA board
// ********************************************************************
class FPGADE4(implicit p :Parameters) extends FPGADE4Shell()(p)
  with HasTEEHWChip with WithFPGADE4Connect {
}

// ********************************************************************
// FPGATR4 - Demo on TR4 FPGA board
// ********************************************************************
class FPGATR4(implicit p :Parameters) extends FPGATR4Shell()(p)
  with HasTEEHWChip with WithFPGATR4Connect {
}

class FPGATR4ToChip(implicit p :Parameters) extends FPGATR4Shell()(p)
  with WithFPGATR4ToChipConnect {
}

// ********************************************************************
// FPGATR5 - Demo on TR5 FPGA board
// ********************************************************************
class FPGATR5(implicit p :Parameters) extends FPGATR5Shell()(p)
  with HasTEEHWChip with WithFPGATR5Connect {
}

class FPGATR5ToChip(implicit p :Parameters) extends FPGATR5Shell()(p)
  with WithFPGATR5ToChipConnect {
}

// ********************************************************************
// FPGASakuraX - Demo on Sakura-X FPGA board
// ********************************************************************
class FPGASakuraX(implicit p :Parameters) extends FPGASakuraXShell()(p)
  with HasTEEHWChip with WithFPGASakuraXConnect {
}