package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import sifive.blocks.devices.gpio.PeripheryGPIOKey
import sifive.fpgashells.clocks._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.ip.xilinx.sakuraxmig._
import sifive.fpgashells.shell.xilinx.XDMATopPads
import uec.teehardware._
import uec.teehardware.macros._
import uec.teehardware.devices.clockctrl._
import uec.teehardware.devices.sdram.SDRAMKey
import uec.teehardware.devices.usb11hs._

class FMCSakuraX extends Bundle {
  val CLK_M2C_P = Vec(2, Analog(1.W))
  val CLK_M2C_N = Vec(2, Analog(1.W))
  val LA_P = Vec(34, Analog(1.W))
  val LA_N = Vec(34, Analog(1.W))
}

trait FPGASakuraXChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters

  // Table 19
  val K_HEADER = IO(Vec(10, Analog(1.W)))
  val K_CLK_EXT_N = IO(Vec(2, Analog(1.W)))
  val K_CLK_EXT_P = IO(Vec(2, Analog(1.W)))
  val K_RSVIO_N = IO(Vec(1, Analog(1.W)))
  val K_RSVIO_P = IO(Vec(1, Analog(1.W)))

  // Table 17
  val K_DIPSW = IO(Vec(8, Input(Bool())))

  // Table 20
  val K_LED = IO(Vec(8, Analog(1.W)))

  // Table 18
  val K_FMC = IO(new FMCSakuraX)
}

trait FPGASakuraXClockAndResetsAndDDR {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters

  val sys_clock_p = IO(Input(Clock()))
  val sys_clock_n = IO(Input(Clock()))
  val rst_0 = IO(Input(Bool()))  // Connect to SW4

  var ddr: Option[SakuraXMIGIODDR] = None
}

class FPGASakuraXShell(implicit val p :Parameters) extends RawModule
  with FPGASakuraXChipShell
  with FPGASakuraXClockAndResetsAndDDR {
}

class FPGASakuraXInternal(chip: Option[WithTEEHWbaseShell with WithTEEHWbaseConnect])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGASakuraXClockAndResetsAndDDR {
  def outer = chip

  val init_calib_complete = IO(Output(Bool()))
  var depth = BigInt(0)

  // Some connections for having the clocks
  val sys_clock_ibufds = Module(new IBUFDS())
  val sys_clk_i = IBUFG(sys_clock_ibufds.io.O)
  sys_clock_ibufds.io.I := sys_clock_p
  sys_clock_ibufds.io.IB := sys_clock_n
  val reset_0 = IBUF(rst_0)   // use for connect the "rst_2" in previous designs

  val clock = Wire(Clock())
  val reset = Wire(Bool())

  val isOtherClk = isChildClock || (p(SbusToMbusXTypeKey) match {
    case _: AsynchronousCrossing => true
    case _ => false
  }) || p(PeripheryUSB11HSKey).nonEmpty

  // PLL instance
  val c = new PLLParameters(
    name = "pll",
    input = PLLInClockParameters(freqMHz = 200.0, feedback = true),
    req = Seq(
      PLLOutClockParameters(freqMHz = p(FreqKeyMHz))
    ) ++ (if (isOtherClk) Seq(PLLOutClockParameters(freqMHz = 10.0), PLLOutClockParameters(freqMHz = 48.0)) else Seq())
  )
  val pll = Module(new Series7MMCM(c))
  pll.io.clk_in1 := sys_clk_i
  pll.io.reset := reset_0

  withClockAndReset(clock, reset) {
    val aresetn = !reset_0 // Reset that goes to the MMCM inside of the DDR MIG
    val sys_rst = ResetCatchAndSync(pll.io.clk_out1.get, !pll.io.locked) // Catched system clock
    val reset_to_sys = WireInit(!pll.io.locked) // If DDR is not present, this is the system reset
    val reset_to_child = WireInit(!pll.io.locked) // If DDR is not present, this is the child reset
    pll.io.clk_out2.foreach(reset_to_child := ResetCatchAndSync(_, !pll.io.locked))

    // The DDR port
    init_calib_complete := false.B
    tlport.foreach{ chiptl =>
      val mod = Module(LazyModule(new TLULtoMIGSakuraX(chiptl.params)).module)

      // DDR port only
      ddr = Some(IO(new SakuraXMIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport

      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := sys_clk_i.asUInt()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.ui_clk_sync_rst)
      mod.clock := pll.io.clk_out1.get
      mod.reset := reset_to_sys

      // TileLink Interface from platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      // Legacy ChildClock
      ChildClock.foreach { cclk =>
        println("Shell Island and Child Clock connected to clk_out2")
        cclk := pll.io.clk_out2.get
        mod.clock := pll.io.clk_out2.get
        mod.reset := reset_to_child
      }
      ChildReset.foreach { crst =>
        crst := reset_to_child
      }

      init_calib_complete := mod.io.ddrport.init_calib_complete
      depth = mod.depth
    }
    (memser zip memserSourceBits).foreach { case(ms, sourceBits) =>
      val mod = Module(LazyModule(new SertoMIGSakuraX(ms.w, sourceBits)).module)

      // Serial port
      mod.io.serport.flipConnect(ms)

      // DDR port only
      ddr = Some(IO(new SakuraXMIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport

      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := sys_clk_i.asUInt()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.ui_clk_sync_rst)

      p(SbusToMbusXTypeKey) match {
        case _: AsynchronousCrossing =>
          println("Shell Island connected to clk_out2 (10MHz)")
          ChildClock.foreach(_ := pll.io.clk_out2.get)
          mod.clock := pll.io.clk_out2.get
          mod.reset := reset_to_child
        case _ =>
          mod.clock := pll.io.clk_out1.get
      }

      init_calib_complete := mod.io.ddrport.init_calib_complete
      depth = mod.depth
    }

    // Main clock and reset assignments
    clock := pll.io.clk_out1.get
    reset := reset_to_sys
    sys_clk := pll.io.clk_out1.get
    rst_n := !reset_to_sys
    jrst_n := !reset_to_sys
    usbClk.foreach(_ := pll.io.clk_out3.get)
    sdramclock.foreach(_ := pll.io.clk_out1.get)
    DefaultRTC

    aclocks.foreach { aclocks =>
      println(s"Connecting async clocks by default =>")
      (aclocks zip namedclocks).foreach { case (aclk, nam) =>
        println(s"  Detected clock ${nam}")
        if(nam.contains("mbus")) {
          p(SbusToMbusXTypeKey) match {
            case _: AsynchronousCrossing =>
              aclk := pll.io.clk_out2.get
              println("    Connected to clk_out2 (10 MHz)")
            case _ =>
              aclk := pll.io.clk_out1.get
              println("    Connected to clk_out1")
          }
        }
        else {
          aclk := pll.io.clk_out1.get
          println("    Connected to clk_out1")
        }
      }
    }

    // Clock controller
    (extser zip extserSourceBits).foreach { case(es, sourceBits) =>
      val mod = Module(LazyModule(new FPGAMiniSystem(sourceBits)).module)

      // Serial port
      mod.serport.flipConnect(es)

      aclocks.foreach{ aclocks =>
        println(s"Connecting clock for CryptoBus from clock controller =>")
        (aclocks zip namedclocks).foreach{ case (aclk, nam) =>
          println(s"  Detected clock ${nam}")
          if(nam.contains("cryptobus") && mod.clockctrl.size >= 1) {
            aclk := mod.clockctrl(0).asInstanceOf[ClockCtrlPortIO].clko
            println("    Connected to first clock control")
          }
          if(nam.contains("tile_0") && mod.clockctrl.size >= 2) {
            aclk := mod.clockctrl(1).asInstanceOf[ClockCtrlPortIO].clko
            println("    Connected to second clock control")
          }
          if(nam.contains("tile_1") && mod.clockctrl.size >= 3) {
            aclk := mod.clockctrl(2).asInstanceOf[ClockCtrlPortIO].clko
            println("    Connected to third clock control")
          }
        }
      }
    }
  }
}

object ConnectFMCLPCXilinxGPIO {
  def apply (n: Int, pu: Int, c: Bool, get: Boolean, FMC: FMCSakuraX) = {
    val p:Int = pu match {
      case it if 1 to 10 contains it => pu - 1
      case it if 13 to 28 contains it => pu - 3
      case it if 31 to 40 contains it => pu - 5
      case _ => throw new RuntimeException(s"J${n}_${pu} is a VDD or a GND")
    }
    n match {
      case 0 =>
        p match {
          case 0 => if(get) c := GET(FMC.CLK_M2C_P(0)) else PUT(c, FMC.CLK_M2C_P(0))
          case 1 => if(get) c := GET(FMC.CLK_M2C_N(0)) else PUT(c, FMC.CLK_M2C_N(0))
          case 2 => if(get) c := IOBUF(FMC.LA_P(1)) else IOBUF(FMC.LA_P(1), c)
          case 3 => if(get) c := IOBUF(FMC.LA_N(1)) else IOBUF(FMC.LA_N(1), c)
          case 4 => if(get) c := IOBUF(FMC.LA_P(11)) else IOBUF(FMC.LA_P(11), c)
          case 5 => if(get) c := IOBUF(FMC.LA_N(11)) else IOBUF(FMC.LA_N(11), c)
          case 6 => if(get) c := IOBUF(FMC.LA_P(15)) else IOBUF(FMC.LA_P(15), c)
          case 7 => if(get) c := IOBUF(FMC.LA_N(15)) else IOBUF(FMC.LA_N(15), c)
          case 8 => if(get) c := IOBUF(FMC.LA_P(9)) else IOBUF(FMC.LA_P(9), c)
          case 9 => if(get) c := IOBUF(FMC.LA_N(9)) else IOBUF(FMC.LA_N(9), c)
          case 10 => if(get) c := IOBUF(FMC.LA_P(13)) else IOBUF(FMC.LA_P(13), c)
          case 11 => if(get) c := IOBUF(FMC.LA_N(13)) else IOBUF(FMC.LA_N(13), c)
          case 12 => if(get) c := IOBUF(FMC.LA_P(17)) else IOBUF(FMC.LA_P(17), c)
          case 13 => if(get) c := IOBUF(FMC.LA_N(17)) else IOBUF(FMC.LA_N(17), c)
          case 14 => if(get) c := IOBUF(FMC.LA_P(19)) else IOBUF(FMC.LA_P(19), c)
          case 15 => if(get) c := IOBUF(FMC.LA_N(19)) else IOBUF(FMC.LA_N(19), c)
          case 16 => if(get) c := IOBUF(FMC.LA_P(21)) else IOBUF(FMC.LA_P(21), c)
          case 17 => if(get) c := IOBUF(FMC.LA_N(21)) else IOBUF(FMC.LA_N(21), c)
          case 18 => if(get) c := IOBUF(FMC.LA_P(23)) else IOBUF(FMC.LA_P(23), c)
          case 19 => if(get) c := IOBUF(FMC.LA_N(23)) else IOBUF(FMC.LA_N(23), c)
          case 20 => if(get) c := IOBUF(FMC.LA_P(7)) else IOBUF(FMC.LA_P(7), c)
          case 21 => if(get) c := IOBUF(FMC.LA_N(7)) else IOBUF(FMC.LA_N(7), c)
          case 22 => if(get) c := IOBUF(FMC.LA_P(4)) else IOBUF(FMC.LA_P(4), c)
          case 23 => if(get) c := IOBUF(FMC.LA_N(4)) else IOBUF(FMC.LA_N(4), c)
          case 24 => if(get) c := IOBUF(FMC.LA_P(2)) else IOBUF(FMC.LA_P(2), c)
          case 25 => if(get) c := IOBUF(FMC.LA_N(2)) else IOBUF(FMC.LA_N(2), c)
          case 26 => if(get) c := IOBUF(FMC.LA_P(5)) else IOBUF(FMC.LA_P(5), c)
          case 27 => if(get) c := IOBUF(FMC.LA_N(5)) else IOBUF(FMC.LA_N(5), c)
          case 28 => if(get) c := IOBUF(FMC.LA_P(24)) else IOBUF(FMC.LA_P(24), c)
          case 29 => if(get) c := IOBUF(FMC.LA_N(24)) else IOBUF(FMC.LA_N(24), c)
          case 30 => if(get) c := IOBUF(FMC.LA_P(26)) else IOBUF(FMC.LA_P(26), c)
          case 31 => if(get) c := IOBUF(FMC.LA_N(26)) else IOBUF(FMC.LA_N(26), c)
          case 32 => if(get) c := IOBUF(FMC.LA_P(28)) else IOBUF(FMC.LA_P(28), c)
          case 33 => if(get) c := IOBUF(FMC.LA_N(28)) else IOBUF(FMC.LA_N(28), c)
          case 34 => if(get) c := IOBUF(FMC.LA_P(30)) else IOBUF(FMC.LA_P(30), c)
          case 35 => if(get) c := IOBUF(FMC.LA_N(30)) else IOBUF(FMC.LA_N(30), c)
          case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
        }
      case 1 =>
        p match {
          case 0 => if(get) c := IOBUF(FMC.LA_P(12)) else IOBUF(FMC.LA_P(12), c)
          case 1 => if(get) c := IOBUF(FMC.LA_N(12)) else IOBUF(FMC.LA_N(12), c)
          case 2 => if(get) c := IOBUF(FMC.LA_P(0)) else IOBUF(FMC.LA_P(0), c)
          case 3 => if(get) c := IOBUF(FMC.LA_N(0)) else IOBUF(FMC.LA_N(0), c)
          case 4 => if(get) c := IOBUF(FMC.LA_P(8)) else IOBUF(FMC.LA_P(8), c)
          case 5 => if(get) c := IOBUF(FMC.LA_N(8)) else IOBUF(FMC.LA_N(8), c)
          case 6 => if(get) c := IOBUF(FMC.LA_P(3)) else IOBUF(FMC.LA_P(3), c)
          case 7 => if(get) c := IOBUF(FMC.LA_N(3)) else IOBUF(FMC.LA_N(3), c)
          //case 8 => if(get) c := IOBUF(FMC.HA_P(2)) else IOBUF(FMC.HA_P(2), c)
          //case 9 => if(get) c := IOBUF(FMC.HA_N(2)) else IOBUF(FMC.HA_N(2), c)
          case 10 => if(get) c := IOBUF(FMC.LA_P(6)) else IOBUF(FMC.LA_P(6), c)
          case 11 => if(get) c := IOBUF(FMC.LA_N(6)) else IOBUF(FMC.LA_N(6), c)
          case 12 => if(get) c := IOBUF(FMC.LA_P(10)) else IOBUF(FMC.LA_P(10), c)
          case 13 => if(get) c := IOBUF(FMC.LA_N(10)) else IOBUF(FMC.LA_N(10), c)
          case 14 => if(get) c := IOBUF(FMC.LA_P(16)) else IOBUF(FMC.LA_P(16), c)
          case 15 => if(get) c := IOBUF(FMC.LA_N(16)) else IOBUF(FMC.LA_N(16), c)
          case 16 => if(get) c := IOBUF(FMC.LA_P(14)) else IOBUF(FMC.LA_P(14), c)
          case 17 => if(get) c := IOBUF(FMC.LA_N(14)) else IOBUF(FMC.LA_N(14), c)
          case 18 => if(get) c := IOBUF(FMC.LA_P(20)) else IOBUF(FMC.LA_P(20), c)
          case 19 => if(get) c := IOBUF(FMC.LA_N(20)) else IOBUF(FMC.LA_N(20), c)
          case 20 => if(get) c := IOBUF(FMC.LA_P(18)) else IOBUF(FMC.LA_P(18), c)
          case 21 => if(get) c := IOBUF(FMC.LA_N(18)) else IOBUF(FMC.LA_N(18), c)
          case 22 => if(get) c := IOBUF(FMC.LA_P(22)) else IOBUF(FMC.LA_P(22), c)
          case 23 => if(get) c := IOBUF(FMC.LA_N(22)) else IOBUF(FMC.LA_N(22), c)
          case 24 => if(get) c := IOBUF(FMC.LA_P(25)) else IOBUF(FMC.LA_P(25), c)
          case 25 => if(get) c := IOBUF(FMC.LA_N(25)) else IOBUF(FMC.LA_N(25), c)
          case 26 => if(get) c := IOBUF(FMC.LA_P(27)) else IOBUF(FMC.LA_P(27), c)
          case 27 => if(get) c := IOBUF(FMC.LA_N(27)) else IOBUF(FMC.LA_N(27), c)
          case 28 => if(get) c := IOBUF(FMC.LA_P(29)) else IOBUF(FMC.LA_P(29), c)
          case 29 => if(get) c := IOBUF(FMC.LA_N(29)) else IOBUF(FMC.LA_N(29), c)
          case 30 => if(get) c := IOBUF(FMC.LA_P(31)) else IOBUF(FMC.LA_P(31), c)
          case 31 => if(get) c := IOBUF(FMC.LA_N(31)) else IOBUF(FMC.LA_N(31), c)
          case 32 => if(get) c := IOBUF(FMC.LA_P(33)) else IOBUF(FMC.LA_P(33), c)
          case 33 => if(get) c := IOBUF(FMC.LA_N(33)) else IOBUF(FMC.LA_N(33), c)
          case 34 => if(get) c := IOBUF(FMC.LA_P(32)) else IOBUF(FMC.LA_P(32), c)
          case 35 => if(get) c := IOBUF(FMC.LA_N(32)) else IOBUF(FMC.LA_N(32), c)
          case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
        }
      case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
    }
  }
  def debug(n: Int, p: Int, c: Bool, get: Boolean, FMC: FMCSakuraX) = {

    n match {
      case 20 =>
        p match {
          case 1 => if(get) c := IOBUF(FMC.LA_P(20)) else IOBUF(FMC.LA_P(20), c)
          case 2 => if(get) c := IOBUF(FMC.LA_P(24)) else IOBUF(FMC.LA_P(24), c)
          case 3 => if(get) c := IOBUF(FMC.LA_N(20)) else IOBUF(FMC.LA_N(20), c)
          case 4 => if(get) c := IOBUF(FMC.LA_N(24)) else IOBUF(FMC.LA_N(24), c)
          case 5 => if(get) c := IOBUF(FMC.LA_P(21)) else IOBUF(FMC.LA_P(21), c)
          case 6 => if(get) c := IOBUF(FMC.LA_P(25)) else IOBUF(FMC.LA_P(25), c)
          case 7 => if(get) c := IOBUF(FMC.LA_N(21)) else IOBUF(FMC.LA_N(21), c)
          case 8 => if(get) c := IOBUF(FMC.LA_N(25)) else IOBUF(FMC.LA_N(25), c)
          case 9 => if(get) c := IOBUF(FMC.LA_P(22)) else IOBUF(FMC.LA_P(22), c)
          case 10 => if(get) c := IOBUF(FMC.LA_P(26)) else IOBUF(FMC.LA_P(26), c)
          case 11 => if(get) c := IOBUF(FMC.LA_N(22)) else IOBUF(FMC.LA_N(22), c)
          case 12 => if(get) c := IOBUF(FMC.LA_N(26)) else IOBUF(FMC.LA_N(26), c)
          case 13 => if(get) c := IOBUF(FMC.LA_P(23)) else IOBUF(FMC.LA_P(23), c)
          case 14 => if(get) c := IOBUF(FMC.LA_P(27)) else IOBUF(FMC.LA_P(27), c)
          case 15 => if(get) c := IOBUF(FMC.LA_N(23)) else IOBUF(FMC.LA_N(23), c)
          case 16 => if(get) c := IOBUF(FMC.LA_N(27)) else IOBUF(FMC.LA_N(27), c)
          case _ => throw new RuntimeException(s"J${n}_${p} does not exist")
        }
      case 16 =>
        p match {
          case 5 => if(get) c := IOBUF(FMC.LA_P(28)) else IOBUF(FMC.LA_P(28), c)
          case 6 => if(get) c := IOBUF(FMC.LA_P(30)) else IOBUF(FMC.LA_P(30), c)
          case 7 => if(get) c := IOBUF(FMC.LA_N(28)) else IOBUF(FMC.LA_N(28), c)
          case 8 => if(get) c := IOBUF(FMC.LA_N(30)) else IOBUF(FMC.LA_N(30), c)
          case 9 => if(get) c := IOBUF(FMC.LA_P(29)) else IOBUF(FMC.LA_P(29), c)
          case 10 => if(get) c := IOBUF(FMC.LA_P(31)) else IOBUF(FMC.LA_P(31), c)
          case 11 => if(get) c := IOBUF(FMC.LA_N(29)) else IOBUF(FMC.LA_N(29), c)
          case 12 => if(get) c := IOBUF(FMC.LA_N(31)) else IOBUF(FMC.LA_N(31), c)
          case _ => throw new RuntimeException(s"J${n}_${p} does not exist")
        }
      case 15 =>
        p match {
          case 3 => if(get) c := IOBUF(FMC.LA_P(32)) else IOBUF(FMC.LA_P(32), c)
          case 4 => if(get) c := IOBUF(FMC.LA_N(32)) else IOBUF(FMC.LA_N(32), c)
          case 5 => if(get) c := IOBUF(FMC.LA_P(33)) else IOBUF(FMC.LA_P(33), c)
          case 6 => if(get) c := IOBUF(FMC.LA_N(33)) else IOBUF(FMC.LA_N(33), c)
          case _ => throw new RuntimeException(s"J${n}_${p} does not exist")
        }
      case 1 =>
        p match {
          case 1 => if(get) c := IOBUF(FMC.LA_P(0)) else IOBUF(FMC.LA_P(0), c)
          case 2 => if(get) c := IOBUF(FMC.LA_P(10)) else IOBUF(FMC.LA_P(10), c)
          case 3 => if(get) c := IOBUF(FMC.LA_N(0)) else IOBUF(FMC.LA_N(0), c)
          case 4 => if(get) c := IOBUF(FMC.LA_N(10)) else IOBUF(FMC.LA_N(10), c)
          case 5 => if(get) c := IOBUF(FMC.LA_P(1)) else IOBUF(FMC.LA_P(1), c)
          case 6 => if(get) c := IOBUF(FMC.LA_P(11)) else IOBUF(FMC.LA_P(11), c)
          case 7 => if(get) c := IOBUF(FMC.LA_N(1)) else IOBUF(FMC.LA_N(1), c)
          case 8 => if(get) c := IOBUF(FMC.LA_N(11)) else IOBUF(FMC.LA_N(11), c)
          case 9 => if(get) c := IOBUF(FMC.LA_P(2)) else IOBUF(FMC.LA_P(2), c)
          case 10 => if(get) c := IOBUF(FMC.LA_P(12)) else IOBUF(FMC.LA_P(12), c)
          case 11 => if(get) c := IOBUF(FMC.LA_N(2)) else IOBUF(FMC.LA_N(2), c)
          case 12 => if(get) c := IOBUF(FMC.LA_N(12)) else IOBUF(FMC.LA_N(12), c)
          case 13 => if(get) c := IOBUF(FMC.LA_P(3)) else IOBUF(FMC.LA_P(3), c)
          case 14 => if(get) c := IOBUF(FMC.LA_P(13)) else IOBUF(FMC.LA_P(13), c)
          case 15 => if(get) c := IOBUF(FMC.LA_N(3)) else IOBUF(FMC.LA_N(3), c)
          case 16 => if(get) c := IOBUF(FMC.LA_N(13)) else IOBUF(FMC.LA_N(13), c)
          case 17 => if(get) c := IOBUF(FMC.LA_P(4)) else IOBUF(FMC.LA_P(4), c)
          case 18 => if(get) c := IOBUF(FMC.LA_P(14)) else IOBUF(FMC.LA_P(14), c)
          case 19 => if(get) c := IOBUF(FMC.LA_N(4)) else IOBUF(FMC.LA_N(4), c)
          case 20 => if(get) c := IOBUF(FMC.LA_N(14)) else IOBUF(FMC.LA_N(14), c)
          case 21 => if(get) c := IOBUF(FMC.LA_P(5)) else IOBUF(FMC.LA_P(5), c)
          case 22 => if(get) c := IOBUF(FMC.LA_P(15)) else IOBUF(FMC.LA_P(15), c)
          case 23 => if(get) c := IOBUF(FMC.LA_N(5)) else IOBUF(FMC.LA_N(5), c)
          case 24 => if(get) c := IOBUF(FMC.LA_N(15)) else IOBUF(FMC.LA_N(15), c)
          case 25 => if(get) c := IOBUF(FMC.LA_P(6)) else IOBUF(FMC.LA_P(6), c)
          case 26 => if(get) c := IOBUF(FMC.LA_P(16)) else IOBUF(FMC.LA_P(16), c)
          case 27 => if(get) c := IOBUF(FMC.LA_N(6)) else IOBUF(FMC.LA_N(6), c)
          case 28 => if(get) c := IOBUF(FMC.LA_N(16)) else IOBUF(FMC.LA_N(16), c)
          case 29 => if(get) c := IOBUF(FMC.LA_P(7)) else IOBUF(FMC.LA_P(7), c)
          case 30 => if(get) c := IOBUF(FMC.LA_P(17)) else IOBUF(FMC.LA_P(17), c)
          case 31 => if(get) c := IOBUF(FMC.LA_N(7)) else IOBUF(FMC.LA_N(7), c)
          case 32 => if(get) c := IOBUF(FMC.LA_N(17)) else IOBUF(FMC.LA_N(17), c)
          case 33 => if(get) c := IOBUF(FMC.LA_P(8)) else IOBUF(FMC.LA_P(8), c)
          case 34 => if(get) c := IOBUF(FMC.LA_P(18)) else IOBUF(FMC.LA_P(18), c)
          case 35 => if(get) c := IOBUF(FMC.LA_N(8)) else IOBUF(FMC.LA_N(8), c)
          case 36 => if(get) c := IOBUF(FMC.LA_N(18)) else IOBUF(FMC.LA_N(18), c)
          case 37 => if(get) c := IOBUF(FMC.LA_P(9)) else IOBUF(FMC.LA_P(9), c)
          case 38 => if(get) c := IOBUF(FMC.LA_P(19)) else IOBUF(FMC.LA_P(19), c)
          case 39 => if(get) c := IOBUF(FMC.LA_N(9)) else IOBUF(FMC.LA_N(9), c)
          case 40 => if(get) c := IOBUF(FMC.LA_N(19)) else IOBUF(FMC.LA_N(19), c)
          case _ => throw new RuntimeException(s"J${n}_${p} does not exist")
        }
      case _ => throw new RuntimeException(s"J${n}_${p} does not exist")
    }
  }
}

class FPGASakuraXInternalNoChip
(
  val idBits: Int = 4,
  val widthBits: Int = 32,
  val sinkBits: Int = 1
)(implicit p :Parameters) extends FPGASakuraXInternal(None)(p) {
  // TODO: Reconfirm all of this
  override def otherId = Some(idBits)
  override def tlparam = p(ExtMem).map { A =>
    TLBundleParameters(
      widthBits,
      A.master.beatBytes * 8,
      idBits,
      sinkBits,
      log2Up(log2Ceil(p(MemoryBusKey).blockBytes)+1),
      Seq(),
      Seq(),
      Seq(),
      false)}
  override def aclkn: Option[Int] = p(ExposeClocks).option(3)
  override def memserSourceBits: Option[Int] = p(ExtSerMem).map( A => idBits )
  override def extserSourceBits: Option[Int] = p(ExtSerBus).map( A => idBits )
  override def namedclocks: Seq[String] = if(p(ExposeClocks)) Seq("cryptobus", "tile_0", "tile_1") else Seq()
  override def issdramclock: Boolean = p(SDRAMKey).nonEmpty
  override def isChildClock: Boolean = (p(SbusToMbusXTypeKey) match {
    case _: AsynchronousCrossing => true
    case _ => false
  })
  override def isRTCclock: Boolean = p(RTCPort)
}

trait WithFPGASakuraXInternCreate {
  this: FPGASakuraXShell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGASakuraXInternal(Some(chip)))
}

trait WithFPGASakuraXInternNoChipCreate {
  this: FPGASakuraXShell =>
  def idBits = 4
  def widthBits = 32
  def sinkBits = 1
  val intern = Module(new FPGASakuraXInternalNoChip(idBits, widthBits, sinkBits))
}

trait WithFPGASakuraXInternConnect {
  this: FPGASakuraXShell =>
  val intern: FPGASakuraXInternal

  // To intern = Clocks and resets
  intern.sys_clock_p := sys_clock_p
  intern.sys_clock_n := sys_clock_n
  intern.rst_0 := rst_0
  ddr = intern.ddr.map{ A =>
    val port = IO(new SakuraXMIGIODDR(intern.depth))
    port <> A
    port
  }
}

trait WithFPGASakuraXPureConnect {
  this: FPGASakuraXShell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  def namedclocks: Seq[String] = chip.system.sys.asInstanceOf[HasTEEHWSystemModule].namedclocks
  // This trait connects the chip to all essentials. This assumes no DDR is connected yet

  (chip.gpio_out.asBools() zip K_LED).foreach{ case(gpo, led) =>
    IOBUF(led, gpo)
  }
  chip.gpio_in := K_DIPSW.asUInt()
  chip.jtag.jtag_TCK := IBUFG(IOBUF(K_HEADER(5)).asClock()).asBool()
  chip.jtag.jtag_TDI := IOBUF(K_HEADER(4))
  PULLUP(K_HEADER(4))
  IOBUF(K_HEADER(3), chip.jtag.jtag_TDO)
  chip.jtag.jtag_TMS := IOBUF(K_HEADER(2))
  PULLUP(K_HEADER(2))
  chip.uart_rxd := IOBUF(K_HEADER(1))
  IOBUF(K_HEADER(0), chip.uart_txd)
  IOBUF(K_HEADER(6), chip.sdio.sdio_clk)
  IOBUF(K_HEADER(7), chip.sdio.sdio_cmd)
  chip.sdio.sdio_dat_0 := IOBUF(K_HEADER(8))
  IOBUF(K_HEADER(9), chip.sdio.sdio_dat_3)

  // Connected to K_FMC
  chip.usb11hs.foreach{ case chipport =>
    val USBWireDataIn = Wire(Vec(2, Bool()))
    ConnectFMCLPCXilinxGPIO.debug(1, 1, USBWireDataIn(0), true, K_FMC)
    ConnectFMCLPCXilinxGPIO.debug(1, 2, USBWireDataIn(1), true, K_FMC)
    chipport.USBWireDataIn := USBWireDataIn.asUInt()
    ConnectFMCLPCXilinxGPIO.debug(1, 3, chipport.USBWireDataOut(0), false, K_FMC)
    ConnectFMCLPCXilinxGPIO.debug(1, 4, chipport.USBWireDataOut(1), false, K_FMC)
    ConnectFMCLPCXilinxGPIO.debug(1, 5, chipport.USBWireCtrlOut, false, K_FMC)
    ConnectFMCLPCXilinxGPIO.debug(1, 6, chipport.USBFullSpeed, false, K_FMC)
  }

  chip.qspi.foreach { case qspi =>
    ConnectFMCLPCXilinxGPIO.debug(1, 7, qspi.qspi_cs(0), false, K_FMC)
    ConnectFMCLPCXilinxGPIO.debug(1, 8, qspi.qspi_sck, false, K_FMC)
    ConnectFMCLPCXilinxGPIO.debug(1, 9, qspi.qspi_miso, true, K_FMC)
    ConnectFMCLPCXilinxGPIO.debug(1, 10, qspi.qspi_mosi, false, K_FMC)
    ConnectFMCLPCXilinxGPIO.debug(1, 11, qspi.qspi_wp, false, K_FMC)
    ConnectFMCLPCXilinxGPIO.debug(1, 12, qspi.qspi_hold, false, K_FMC)
  }

  // TODO Nullify this for now
  chip.sdram.foreach{ sdram =>
    sdram.sdram_data_i := 0.U
  }
}

trait WithFPGASakuraXConnect extends WithFPGASakuraXPureConnect
  with WithFPGASakuraXInternCreate
  with WithFPGASakuraXInternConnect {
  this: FPGASakuraXShell =>

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // Platform connections (override)
  // gpio_out := Cat(chip.gpio_out(chip.gpio_out.getWidth-1, 1), intern.init_calib_complete)
  (chip.usb11hs zip intern.usbClk).foreach { case (chipport, uclk) =>
    chipport.usbClk := uclk
  }
}
