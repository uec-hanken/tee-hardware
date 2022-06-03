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

class TEEHWbase(implicit val p :Parameters) extends RawModule {
  // All the modules declared here have this clock and reset
  val clock = Wire(Clock())
  val reset = Wire(Bool()) // System reset (for cores)
  val system = withClockAndReset(clock, reset) {
    // The platform module
    Module(LazyModule(new TEEHWSystem).module)
  }
  system.suggestName("system")
  val IOGen: GenericIOLibraryParams = p(IOLibrary)
}

class TEEHWSoC(implicit p :Parameters) extends TEEHWbase()(p)
  with HasTEEHWClockGroupChipImp
  with DebugJTAGOnlyChipImp
  with HasTEEHWPeripheryExtMemChipImp
  with HasTEEHWPeripheryExtSerMemChipImp
  with HasTEEHWPeripheryExtSerBusChipImp
  with HasTEEHWPeripheryGPIOChipImp
  with HasTEEHWPeripheryI2CChipImp
  with HasTEEHWPeripheryUARTChipImp
  with HasTEEHWPeripherySPIChipImp
  with HasPeripheryUSB11HSChipImp
  with HasSDRAMChipImp
  with HasTEEHWPeripheryXilinxVC707PCIeX1ChipImp
  with HasTEEHWPeripheryXDMAChipImp
  with CanHavePeripheryTLSerialChipImp

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
  def outer : Option [Any]
  def otherId : Option[Int] = None
  // Parameters that depends on the generated system (not only the config)
  def tlparam: Option[TLBundleParameters] =
    otherId.map(outer.get.asInstanceOf[HasTEEHWPeripheryExtMemChipImp].tlparamsOtherId).
    getOrElse(outer.get.asInstanceOf[HasTEEHWPeripheryExtMemChipImp].tlparam)
  def aclkn: Int =
    outer.get.asInstanceOf[HasTEEHWClockGroupChipImp].aclkn
  def memserSourceBits: Option[Int] =
    outer.get.asInstanceOf[HasTEEHWPeripheryExtSerMemChipImp].system.serSourceBits
  def extserSourceBits: Option[Int] =
    outer.get.asInstanceOf[HasTEEHWPeripheryExtSerBusChipImp].system.extSourceBits
  def namedclocks: Seq[String] =
    outer.get.asInstanceOf[HasTEEHWClockGroupChipImp].system.namedclocks
  // Clocks and resets
  val sys_clk = IO(Output(Clock()))
  val rst_n = IO(Output(Bool()))
  val usbClk = p(PeripheryUSB11HSKey).map(A => IO(Output(Clock())))
  // Memory port serialized
  val memser = p(ExtSerMem).map(A => IO(Flipped(new SerialIO(A.serWidth))))
  // Ext port serialized
  val extser = p(ExtSerBus).map(A => IO(Flipped(new SerialIO(A.serWidth))))
  // Memory port
  var tlport = tlparam.map(A => IO(Flipped(new TLBundle(A))))
  // Asyncrhonoys clocks
  val aclocks = IO(Vec(aclkn, Output(Clock())))

  def connectChipInternals(chip: Any) = {
    PUT(sys_clk.asBool, chip.asInstanceOf[HasTEEHWClockGroupChipImp].clockxi)
    PUT(rst_n, chip.asInstanceOf[HasTEEHWClockGroupChipImp].rstn)
    // Memory port serialized
    (chip.asInstanceOf[HasTEEHWPeripheryExtSerMemChipImp].memser zip memser).foreach{ case (a, b) => a.ConnectIn(b) }
    // Ext port serialized
    (chip.asInstanceOf[HasTEEHWPeripheryExtSerBusChipImp].extser zip extser).foreach{ case (a, b) => a.ConnectIn(b) }
    // Memory port
    (chip.asInstanceOf[HasTEEHWPeripheryExtMemChipImp].mem_tl zip tlport).foreach{ case (a, b) => a.ConnectTLIn(b) }
    // Asyncrhonoys clocks
    (chip.asInstanceOf[HasTEEHWClockGroupChipImp].aclockxi zip aclocks).foreach{ case (a, b) => PUT(b.asBool, a) }
    // USB clock
    (chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs zip usbClk).foreach { case (chipport, uclk) =>
      PUT(uclk.asBool, chipport.usbClk)
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
      (aclocks zip namedclocks).filter(_._2.contains("rtc_clock")).foreach{ case (aclk, anam) =>
        println(s"  [BASE] ${anam} attached to the default RTC clock, period = ${internalPeriod}")
        aclk := RTCActualClock.asClock
      }
    }
  }

  def isOtherClk: Boolean = namedclocks.exists(p => !p.contains("rtc_clock"))
  def isMBusClk: Boolean = namedclocks.exists(p => p.contains("mbus"))
  def isCBusClk: Boolean = namedclocks.exists(p => p.contains("cbus"))
  def isExtSerMemClk: Boolean = namedclocks.exists(p => p.contains("extsermem")) || isMBusClk
  def isExtSerBusClk: Boolean = namedclocks.exists(p => p.contains("extserbus")) || isCBusClk
  println(s"isOtherClk = ${isOtherClk}")
  println(s"isMBusClk = ${isMBusClk}")
  println(s"isExtSerMemClk = ${isExtSerMemClk}")
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
