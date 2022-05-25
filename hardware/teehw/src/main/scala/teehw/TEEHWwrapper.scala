package uec.teehardware

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sifive.blocks.devices.pinctrl._
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.fpgashells.clocks._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import uec.teehardware.devices.usb11hs._
import uec.teehardware.devices.sifiveblocks._
import uec.teehardware.devices.tlmemext._
import freechips.rocketchip.util._
import sifive.fpgashells.shell.xilinx.XDMATopPads
import testchipip.SerialIO
import uec.teehardware.devices.clockctrl.ClockCtrlPortIO
import uec.teehardware.devices.sdram._
import uec.teehardware.shell._

// **********************************************************************
// **TEEHW chip - for doing the only-input/output chip
// **********************************************************************

class TEEHWQSPIBundle(val csWidth: Int = 1) extends Bundle {
  val qspi_cs = (Output(UInt(csWidth.W)))
  val qspi_sck = (Output(Bool()))
  val qspi_miso = (Input(Bool()))
  val qspi_mosi = (Output(Bool()))
}

// Deprecated SD bundle
class TEEHWSDBundle extends Bundle {
  val sdio_clk = (Output(Bool()))
  val sdio_cmd = (Output(Bool()))
  val sdio_dat_0 = (Input(Bool()))
  val sdio_dat_1 = (Analog(1.W))
  val sdio_dat_2 = (Analog(1.W))
  val sdio_dat_3 = (Output(Bool()))
  def connectFrom(from: TEEHWQSPIBundle) = {
    sdio_clk := from.qspi_sck
    sdio_dat_3 := from.qspi_cs(0)
    from.qspi_miso := sdio_dat_0
    sdio_cmd := from.qspi_mosi
  }
}

class  WithTEEHWbaseShell(implicit val p :Parameters) extends RawModule {
  // The actual pins of this module.
  val ngpio_in = p(GPIOInKey)
  val ngpio_out = p(PeripheryGPIOKey).head.width-p(GPIOInKey)
  val gpio_in = (ngpio_in != 0).option( IO(Input(UInt(ngpio_in.W))) )
  val gpio_out = (ngpio_out != 0).option( IO(Output(UInt(ngpio_out.W))) )
  val jtag = IO(new Bundle {
    val jtag_TDI = (Input(Bool()))
    val jtag_TDO = (Output(Bool()))
    val jtag_TCK = (Input(Bool()))
    val jtag_TMS = (Input(Bool()))
  })
  def allspicfg = p(PeripherySPIKey) ++ p(PeripherySPIFlashKey) ++ p(DummySPIFlashKey)
  val qspi = IO(MixedVec( allspicfg.map{cfg => new TEEHWQSPIBundle(cfg.csWidth)} ))
  val uart_txd = IO(Output(Bool()))
  val uart_rxd = IO(Input(Bool()))
  val usb11hs = p(PeripheryUSB11HSKey).map{ _ => IO(new USB11HSPortIO)}
  val pciePorts = p(XilinxVC707PCIe).map(A => IO(new XilinxVC707PCIeX1IO))
  val xdmaPorts = p(XDMAPCIe).map(A => IO(new XDMATopPadswReset(A.lanes)))
  // Memory port serialized
  val memser = p(ExtSerMem).map(A => IO(new SerialIO(A.serWidth)))
  // Ext port serialized
  val extser = p(ExtSerBus).map(A => IO(new SerialIO(A.serWidth)))
  // SDRAM port
  val sdram = p(SDRAMKey).map{ A => IO(new SDRAMIf(A.sdcfg))}
  // Clocks and resets
  val isChildClock = !p(ExposeClocks) && (p(SbusToMbusXTypeKey) match {
    case _: AsynchronousCrossing => true
    case _ => false
  })
  val ChildClock = isChildClock.option(IO(Input(Clock())))
  val sys_clk = IO(Input(Clock()))
  val rst_n = IO(Input(Bool()))
  val jrst_n = IO(Input(Bool()))
  val issdramclock = !p(ExposeClocks) && p(SDRAMKey).nonEmpty
  val sdramclock = issdramclock.option(IO(Input(Clock())))
  val isRTCclock = !p(ExposeClocks) && p(RTCPort)
  val RTCclock = isRTCclock.option(IO(Input(Clock())))
  // An option to dynamically assign
  var aclocks: Option[Vec[Clock]] = None // Async clocks depends on a node of clocks named "globalClocksNode"
  var tlport: Option[TLUL] = None // The TL port depends of a node, and a edge, for parameters
}

trait WithTEEHWbaseConnect {
  this: WithTEEHWbaseShell =>
  implicit val p: Parameters

  val clock : Clock
  val reset : Bool // System reset (for cores)
  val system: Any
  val syncStages = 3

  // Merge all the gpio vector
  val vgpio_in = gpio_in.map( gpi => VecInit(gpi.asBools) )
  val vgpio_out = gpio_out.map( gpo => Wire(Vec(ngpio_out, Bool())))
  (gpio_out zip vgpio_out).foreach{ case (u, v) => u := v.asUInt() }
  val gpio = (vgpio_in ++ vgpio_out).flatten

  // GPIOs
  system.asInstanceOf[HasTEEHWPeripheryGPIOModuleImp].gpio.foreach{sysgpio =>
    (gpio zip sysgpio.pins).zipWithIndex.foreach {
      case ((g: Bool, pin: EnhancedPin), i: Int) =>
        if(i < ngpio_in) BasePinToRegular(pin.toBasePin(), g)
        else g := BasePinToRegular(pin.toBasePin())
    }
  }

  // JTAG
  system.asInstanceOf[DebugJTAGOnlyModuleImp].jtag.foreach{sysjtag =>
    sysjtag.TMS := jtag.jtag_TMS
    sysjtag.TCK := jtag.jtag_TCK.asClock
    sysjtag.TDI := jtag.jtag_TDI
    jtag.jtag_TDO := sysjtag.TDO.data
    sysjtag.TRSTn.foreach(_ := jrst_n)
  }
  reset := !rst_n || system.asInstanceOf[DebugJTAGOnlyModuleImp].ndreset.getOrElse(false.B) // This connects the debug reset and the general reset together

  // QSPI
  (qspi zip system.asInstanceOf[HasTEEHWPeripherySPIModuleImp].spi).foreach {case (portspi, sys) =>
    portspi.qspi_cs  := sys.cs.asUInt
    portspi.qspi_sck := sys.sck
    portspi.qspi_mosi := sys.dq(0).o
    sys.dq(1).i := withClockAndReset(clock, reset) { SynchronizerShiftReg(portspi.qspi_miso, syncStages, name = Some(s"spi_dq_1_sync")) }
    sys.dq(0).i := false.B
    sys.dq(2).i := false.B
    sys.dq(3).i := false.B
  }

  // UART
  system.asInstanceOf[HasTEEHWPeripheryUARTModuleImp].uart.foreach{sysuart =>
    sysuart.rxd := withClockAndReset(clock, reset) { SynchronizerShiftReg(uart_rxd, syncStages, name = Some(s"uart_rxd_sync")) }
    uart_txd := sysuart.txd
  }

  // USB11

  (usb11hs zip system.asInstanceOf[HasPeripheryUSB11HSModuleImp].usb11hs).foreach{ case (port, sysport) => port <> sysport }

  // The memory port
  val memdevice = Some(new MemoryDevice)

  // The serialized memory port
  (memser zip system.asInstanceOf[HasTEEHWPeripheryExtSerMemModuleImp].memSerPorts).foreach{ case (port, sysport) => port <> sysport }

  // The serialized external port
  (extser zip system.asInstanceOf[HasTEEHWPeripheryExtSerBusModuleImp].extSerPorts).foreach{ case (port, sysport) => port <> sysport }

  // The SDRAM port
  (sdram zip system.asInstanceOf[HasSDRAMModuleImp].sdramio).foreach{ case (port, sysport) => port <> sysport }

  // PCIe port (if available)
  (pciePorts zip system.asInstanceOf[HasTEEHWPeripheryXilinxVC707PCIeX1ModuleImp].pciePorts).foreach{ case (port, sysport) => port <> sysport }
  (xdmaPorts zip system.asInstanceOf[HasTEEHWPeripheryXDMAModuleImp].xdmaPorts).foreach{ case (port, sysport) => port <> sysport }

  // TL external memory port
  val tlparam = system.asInstanceOf[HasTEEHWPeripheryExtMemModuleImp].mem_tl.map(tl => tl.params)
  tlport = tlparam.map{tl => IO(new TLUL(tl))}
  // TL port connection
  (system.asInstanceOf[HasTEEHWPeripheryExtMemModuleImp].mem_tl zip tlport).foreach{case (base, chip) =>
    chip.ConnectTLOut(base)
  }
  // A helper function for alterate the number of bits of the id
  def tlparamsOtherId(n: Int) = {
    tlparam.map{param =>
      param.copy(sourceBits = n)
    }
  }

  // Clock and reset connection
  val aclkn = p(ExposeClocks).option(system.asInstanceOf[HasTEEHWClockGroupModuleImp].aclocks.size)
  aclocks = aclkn.map(A => IO(Vec(A, Input(Clock()))))
  clock := sys_clk
  if(p(ExposeClocks)) system.asInstanceOf[HasTEEHWClockGroupModuleImp].aclocks := aclocks.get
  else {
    system.asInstanceOf[HasTEEHWClockGroupModuleImp].aclocks.foreach(_ := sys_clk)
  }

  // Connecting the SD clock if the clocks are not exposed
  sdramclock.foreach{ sdclk =>
    (system.asInstanceOf[HasTEEHWClockGroupModuleImp].aclocks zip system.asInstanceOf[HasTEEHWClockGroupModuleImp].namedclocks).filter(_._2.contains("sdramClockGroup")).foreach{ case (aclk, anam) =>
      println(s"  [BASE] ${anam} attached to the sdramclock")
      aclk := sdclk
    }
  }

  // Connecting the MEM clock if the clocks are not exposed
  ChildClock.foreach{ cclk =>
    (system.asInstanceOf[HasTEEHWClockGroupModuleImp].aclocks zip system.asInstanceOf[HasTEEHWClockGroupModuleImp].namedclocks).filter(_._2.contains("mbus")).foreach{ case (aclk, anam) =>
      println(s"  [BASE] ${anam} attached to the ChildClock")
      aclk := cclk
    }
  }

  // Connecting the RTC clock if the clocks are not exposed
  RTCclock.foreach{ rtcclk =>
    (system.asInstanceOf[HasTEEHWClockGroupModuleImp].aclocks zip system.asInstanceOf[HasTEEHWClockGroupModuleImp].namedclocks).filter(_._2.contains("rtc_clock")).foreach{ case (aclk, anam) =>
      println(s"  [BASE] ${anam} attached to the RTCclock")
      aclk := rtcclk
    }
  }
}

trait HasTEEHWbase {
  this: RawModule =>
  implicit val p: Parameters
  // All the modules declared here have this clock and reset
  val clock = Wire(Clock())
  val reset = Wire(Bool()) // System reset (for cores)
  val system = withClockAndReset(clock, reset) {
    // The platform module
    Module(LazyModule(new TEEHWSystem).module)
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
  def memserSourceBits: Option[Int] = outer.get.system.asInstanceOf[HasTEEHWPeripheryExtSerMemModuleImp].serSourceBits
  def extserSourceBits: Option[Int] = outer.get.system.asInstanceOf[HasTEEHWPeripheryExtSerBusModuleImp].extSourceBits
  def namedclocks: Seq[String] = outer.get.system.asInstanceOf[HasTEEHWClockGroupModuleImp].namedclocks
  def isChildClock = outer.get.isChildClock
  def isChildReset = isChildClock
  def issdramclock = outer.get.issdramclock
  def isRTCclock = outer.get.isRTCclock
  // Clocks and resets
  val ChildClock = isChildClock.option(IO(Output(Clock())))
  val ChildReset = isChildReset.option(IO(Output(Bool())))
  val sys_clk = IO(Output(Clock()))
  val rst_n = IO(Output(Bool()))
  val jrst_n = IO(Output(Bool()))
  val usbClk = p(PeripheryUSB11HSKey).map(A => IO(Output(Clock())))
  val RTCclock = isRTCclock.option(IO(Output(Clock())))
  // Memory port serialized
  val memser = p(ExtSerMem).map(A => IO(Flipped(new SerialIO(A.serWidth))))
  // Ext port serialized
  val extser = p(ExtSerBus).map(A => IO(Flipped(new SerialIO(A.serWidth))))
  // Memory port
  var tlport = tlparam.map(A => IO(Flipped(new TLUL(A))))
  // Asyncrhonoys clocks
  val aclocks = aclkn.map(A => IO(Vec(A, Output(Clock()))))
  // SDRAM clock
  val sdramclock = isChildClock.option(IO(Output(Clock())))

  def connectChipInternals(chip: WithTEEHWbaseShell with WithTEEHWbaseConnect) = {
    (chip.ChildClock zip ChildClock).foreach{ case (a, b) => a := b }
    (chip.sdramclock zip sdramclock).foreach{ case (a, b) => a := b }
    (chip.RTCclock zip RTCclock).foreach{ case (a, b) => a := b }
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
    // USB clock
    (chip.usb11hs zip usbClk).foreach { case (chipport, uclk) =>
      chipport.usbClk := uclk
    }
  }

  def DefaultRTC = {
    // Generalization of the RTC clock. Applies as long as the sys_clk is the DTS
    val pbusFreq = BigDecimal(p(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt
    val rtcFreq = p(DTSTimebase)
    val internalPeriod: BigInt = pbusFreq / rtcFreq

    // check whether pbusFreq >= rtcFreq
    require(internalPeriod > 0)
    // check wehther the integer division is within 5% of the real division
    require((pbusFreq - rtcFreq * internalPeriod) * 100 / pbusFreq <= 5)

    // Use the static period to toggle the RTC
    chisel3.withClockAndReset(sys_clk, !rst_n) {
      val (_, int_rtc_tick) = Counter(true.B, (internalPeriod/2).toInt)
      val RTCActualClock = RegInit(false.B)
      when(int_rtc_tick) {
        RTCActualClock := !RTCActualClock
      }
      RTCclock.foreach(_ := RTCActualClock.asClock())
    }
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
// FPGANexys4DDR - Demo on Nexys 4 DDR FPGA board
// ********************************************************************
class FPGANexys4DDR(implicit p :Parameters) extends FPGANexys4DDRShell()(p)
  with HasTEEHWChip with WithFPGANexys4DDRConnect {
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

class FPGATR4FromChip(implicit p :Parameters) extends FPGATR4Shell()(p)
  with HasTEEHWChip with WithFPGATR4FromChipConnect {
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

class FPGATR5FromChip(implicit p :Parameters) extends FPGATR5Shell()(p)
  with HasTEEHWChip with WithFPGATR5FromChipConnect {
}

// ********************************************************************
// FPGASakuraX - Demo on Sakura-X FPGA board
// ********************************************************************
class FPGASakuraX(implicit p :Parameters) extends FPGASakuraXShell()(p)
  with HasTEEHWChip with WithFPGASakuraXConnect {
}

// ********************************************************************
// FPGADE2 - Demo on DE2 FPGA board
// ********************************************************************
class FPGADE2(implicit p :Parameters) extends FPGADE2Shell()(p)
  with HasTEEHWChip with WithFPGADE2Connect {
}
