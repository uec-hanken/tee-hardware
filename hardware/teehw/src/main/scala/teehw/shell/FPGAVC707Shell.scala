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
import sifive.fpgashells.ip.xilinx.vc707mig._
import sifive.fpgashells.shell.xilinx.XDMATopPads
import uec.teehardware._
import uec.teehardware.macros._
import uec.teehardware.devices.clockctrl._
import uec.teehardware.devices.usb11hs._

class FMCVC707(val ext: Boolean = false, val xcvr: Boolean = false) extends Bundle {
  val CLK_M2C_P = Vec(2, Analog(1.W))
  val CLK_M2C_N = Vec(2, Analog(1.W))
  val HA_P = Vec(24, Analog(1.W))
  val HA_N = Vec(24, Analog(1.W))
  val HB_P = ext.option(Vec(22, Analog(1.W)))
  val HB_N = ext.option(Vec(22, Analog(1.W)))
  val LA_P = Vec(34, Analog(1.W))
  val LA_N = Vec(34, Analog(1.W))
  val GBTCLK_M2C_P = xcvr.option(Vec(2, Input(Bool())))
  val GBTCLK_M2C_N = xcvr.option(Vec(2, Input(Bool())))
  val DP_C2M_P = xcvr.option(Vec(8, Output(Bool())))
  val DP_C2M_N = xcvr.option(Vec(8, Output(Bool())))
  val DP_M2C_P = xcvr.option(Vec(8, Input(Bool())))
  val DP_M2C_N = xcvr.option(Vec(8, Input(Bool())))
}

trait FPGAVC707ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters
  val gpio_in = IO(Input(UInt(p(GPIOInKey).W)))
  val gpio_out = IO(Output(UInt((p(PeripheryGPIOKey).head.width-p(GPIOInKey)).W)))
  val jtag = IO(new Bundle {
    val jtag_TDI = (Input(Bool())) // J19_20 / XADC_GPIO_2
    val jtag_TDO = (Output(Bool())) // J19_17 / XADC_GPIO_1
    val jtag_TCK = (Input(Bool())) // J19_19 / XADC_GPIO_3
    val jtag_TMS = (Input(Bool())) // J19_18 / XADC_GPIO_0
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

  val FMC1_HPC = IO(new FMCVC707(true, true))
  FMC1_HPC.DP_C2M_P.foreach(_.foreach(_ := false.B))
  FMC1_HPC.DP_C2M_N.foreach(_.foreach(_ := false.B))
  val FMC2_HPC = IO(new FMCVC707(false, true))
  FMC2_HPC.DP_C2M_P.foreach(_.foreach(_ := false.B))
  FMC2_HPC.DP_C2M_N.foreach(_.foreach(_ := false.B))

  val USER_SMA_CLOCK_P = IO(Analog(1.W))
  val USER_SMA_CLOCK_N = IO(Analog(1.W))

  val USER_CLOCK_P = IO(Analog(1.W))
  val USER_CLOCK_N = IO(Analog(1.W))
}

trait FPGAVC707ClockAndResetsAndDDR {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters

  val sys_clock_p = IO(Input(Clock()))
  val sys_clock_n = IO(Input(Clock()))
  val rst_0 = IO(Input(Bool()))
  val rst_1 = IO(Input(Bool()))
  val rst_2 = IO(Input(Bool()))
  val rst_3 = IO(Input(Bool()))

  var ddr: Option[VC707MIGIODDR] = None
}

class FPGAVC707Shell(implicit val p :Parameters) extends RawModule
  with FPGAVC707ChipShell
  with FPGAVC707ClockAndResetsAndDDR {
}

class FPGAVC707Internal(chip: Option[WithTEEHWbaseShell with WithTEEHWbaseConnect])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGAVC707ClockAndResetsAndDDR {
  def outer = chip

  val init_calib_complete = IO(Output(Bool()))
  var depth = BigInt(0)

  // Some connections for having the clocks
  val sys_clock_ibufds = Module(new IBUFDS())
  val sys_clk_i = IBUFG(sys_clock_ibufds.io.O)
  sys_clock_ibufds.io.I := sys_clock_p
  sys_clock_ibufds.io.IB := sys_clock_n
  val reset_0 = IBUF(rst_0)
  //val reset_1 = IBUF(rst_1)
  //val reset_2 = IBUF(rst_2)
  val reset_3 = IBUF(rst_3)

  val clock = Wire(Clock())
  val reset = Wire(Bool())

  // PLL instance
  val c = new PLLParameters(
    name = "pll",
    input = PLLInClockParameters(freqMHz = 200.0, feedback = true),
    req = Seq(
      PLLOutClockParameters(freqMHz = 48.0),
      PLLOutClockParameters(freqMHz = 10.0),
      PLLOutClockParameters(freqMHz = p(FreqKeyMHz))
    )
  )
  val pll = Module(new Series7MMCM(c))
  pll.io.clk_in1 := sys_clk_i
  pll.io.reset := reset_0

  withClockAndReset(clock, reset) {
    val aresetn = !reset_0 // Reset that goes to the MMCM inside of the DDR MIG
    val sys_rst = ResetCatchAndSync(pll.io.clk_out3.get, !pll.io.locked) // Catched system clock
    val reset_to_sys = WireInit(!pll.io.locked) // If DDR is not present, this is the system reset
    val reset_to_child = WireInit(!pll.io.locked) // If DDR is not present, this is the child reset

    // The DDR port
    init_calib_complete := false.B
    tlport.foreach{ chiptl =>
      val mod = Module(LazyModule(new TLULtoMIG(chiptl.params)).module)

      // DDR port only
      ddr = Some(IO(new VC707MIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport

      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := sys_clk_i.asUInt()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out3.get, mod.io.ddrport.ui_clk_sync_rst)
      ChildClock.foreach(_ := pll.io.clk_out3.getOrElse(false.B))
      ChildReset.foreach(_ := reset_to_sys)
      mod.clock := pll.io.clk_out3.getOrElse(false.B)
      mod.reset := reset_to_sys
      reset_to_child := ResetCatchAndSync(pll.io.clk_out2.get, !pll.io.locked)

      // TileLink Interface from platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      // Legacy ChildClock
      if(p(DDRPortOther)) {
        println("[Legacy] Quartus Island and Child Clock connected to clk_out2")
        ChildClock.foreach(_ := pll.io.clk_out2.getOrElse(false.B))
        ChildReset.foreach(_ := reset_to_sys)
        mod.clock := pll.io.clk_out2.getOrElse(false.B)
        mod.reset := reset_to_child
      }

      init_calib_complete := mod.io.ddrport.init_calib_complete
      depth = mod.depth
    }
    (memser zip memserSourceBits).foreach { case(ms, sourceBits) =>
      val mod = Module(LazyModule(new SertoMIG(ms.w, sourceBits)).module)

      // Serial port
      mod.io.serport.flipConnect(ms)

      // DDR port only
      ddr = Some(IO(new VC707MIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport

      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := sys_clk_i.asUInt()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out3.get, mod.io.ddrport.ui_clk_sync_rst)
      reset_to_child := ResetCatchAndSync(pll.io.clk_out2.get, !pll.io.locked)

      p(SbusToMbusXTypeKey) match {
        case _: AsynchronousCrossing =>
          println("[Legacy] Quartus Island connected to clk_out2 (10MHz)")
          mod.clock := pll.io.clk_out2.getOrElse(false.B)
          mod.reset := reset_to_child
        case _ =>
          mod.clock := pll.io.clk_out3.getOrElse(false.B)
      }

      init_calib_complete := mod.io.ddrport.init_calib_complete
      depth = mod.depth
    }

    // Main clock and reset assignments
    clock := pll.io.clk_out3.get
    reset := reset_to_sys
    sys_clk := pll.io.clk_out3.get
    rst_n := !reset_to_sys
    jrst_n := !reset_to_sys
    usbClk.foreach(_ := pll.io.clk_out1.getOrElse(false.B))

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
              aclk := pll.io.clk_out3.get
              println("    Connected to clk_out3")
          }
        }
        else {
          aclk := pll.io.clk_out3.get
          println("    Connected to clk_out3")
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

object ConnectFMCXilinxGPIO {
  def apply (n: Int, pu: Int, c: Bool, get: Boolean, FMC: FMCVC707) = {
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
          case 8 => if(get) c := IOBUF(FMC.HA_P(2)) else IOBUF(FMC.HA_P(2), c)
          case 9 => if(get) c := IOBUF(FMC.HA_N(2)) else IOBUF(FMC.HA_N(2), c)
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
      case 2 =>
        p match {
          case 0 => if(get) c := IOBUF(FMC.HA_P(1)) else IOBUF(FMC.HA_P(1), c)
          case 1 => if(get) c := IOBUF(FMC.HA_N(1)) else IOBUF(FMC.HA_N(1), c)
          case 2 => if(get) c := IOBUF(FMC.HA_P(0)) else IOBUF(FMC.HA_P(0), c)
          case 3 => if(get) c := IOBUF(FMC.HA_N(0)) else IOBUF(FMC.HA_N(0), c)
          case 4 => if(get) c := IOBUF(FMC.HA_P(5)) else IOBUF(FMC.HA_P(5), c)
          case 5 => if(get) c := IOBUF(FMC.HA_N(5)) else IOBUF(FMC.HA_N(5), c)
          case 6 => if(get) c := IOBUF(FMC.HA_P(9)) else IOBUF(FMC.HA_P(9), c)
          case 7 => if(get) c := IOBUF(FMC.HA_N(9)) else IOBUF(FMC.HA_N(9), c)
          case 8 => if(get) c := IOBUF(FMC.HA_P(13)) else IOBUF(FMC.HA_P(13), c)
          case 9 => if(get) c := IOBUF(FMC.HA_N(13)) else IOBUF(FMC.HA_N(13), c)
          case 10 => if(get) c := IOBUF(FMC.HA_P(16)) else IOBUF(FMC.HA_P(16), c)
          case 11 => if(get) c := IOBUF(FMC.HA_N(16)) else IOBUF(FMC.HA_N(16), c)
          case 12 => if(get) c := IOBUF(FMC.HA_P(20)) else IOBUF(FMC.HA_P(20), c)
          case 13 => if(get) c := IOBUF(FMC.HA_N(20)) else IOBUF(FMC.HA_N(20), c)
          case 14 => if(get) c := IOBUF(FMC.HA_P(3)) else IOBUF(FMC.HA_P(3), c)
          case 15 => if(get) c := IOBUF(FMC.HA_N(3)) else IOBUF(FMC.HA_N(3), c)
          case 16 => if(get) c := IOBUF(FMC.HA_P(11)) else IOBUF(FMC.HA_P(11), c)
          case 17 => if(get) c := IOBUF(FMC.HA_N(11)) else IOBUF(FMC.HA_N(11), c)
          case 18 => if(get) c := IOBUF(FMC.HA_P(18)) else IOBUF(FMC.HA_P(18), c)
          case 19 => if(get) c := IOBUF(FMC.HA_N(18)) else IOBUF(FMC.HA_N(18), c)
          case 20 => if(get) c := IOBUF(FMC.HA_P(4)) else IOBUF(FMC.HA_P(4), c)
          case 21 => if(get) c := IOBUF(FMC.HA_N(4)) else IOBUF(FMC.HA_N(4), c)
          case 22 => if(get) c := IOBUF(FMC.HA_P(8)) else IOBUF(FMC.HA_P(8), c)
          case 23 => if(get) c := IOBUF(FMC.HA_N(8)) else IOBUF(FMC.HA_N(8), c)
          case 24 => if(get) c := IOBUF(FMC.HA_P(12)) else IOBUF(FMC.HA_P(12), c)
          case 25 => if(get) c := IOBUF(FMC.HA_N(12)) else IOBUF(FMC.HA_N(12), c)
          case 26 => if(get) c := IOBUF(FMC.HA_P(15)) else IOBUF(FMC.HA_P(15), c)
          case 27 => if(get) c := IOBUF(FMC.HA_N(15)) else IOBUF(FMC.HA_N(15), c)
          case 28 => if(get) c := IOBUF(FMC.HA_P(19)) else IOBUF(FMC.HA_P(19), c)
          case 29 => if(get) c := IOBUF(FMC.HA_N(19)) else IOBUF(FMC.HA_N(19), c)
            // TODO: These ones really do not exist in the VC707. J2 and J3 are NC
          //case 30 => if(get) c := IOBUF(FMC.CLK_P(3)) else IOBUF(FMC.CLK_P(3), c)
          //case 31 => if(get) c := IOBUF(FMC.CLK_N(3)) else IOBUF(FMC.CLK_N(3), c)
          case 32 => if(get) c := IOBUF(FMC.HA_P(7)) else IOBUF(FMC.HA_P(7), c)
          case 33 => if(get) c := IOBUF(FMC.HA_N(7)) else IOBUF(FMC.HA_N(7), c)
          case 34 => if(get) c := IOBUF(FMC.HA_P(14)) else IOBUF(FMC.HA_P(14), c)
          case 35 => if(get) c := IOBUF(FMC.HA_N(14)) else IOBUF(FMC.HA_N(14), c)
          case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
        }
      case 3 =>
        p match {
          case 0 => if(get) c := IOBUF(FMC.HB_P.get(21)) else IOBUF(FMC.HB_P.get(21), c)
          case 1 => if(get) c := IOBUF(FMC.HB_N.get(21)) else IOBUF(FMC.HB_N.get(21), c)
          case 2 => if(get) c := IOBUF(FMC.HB_P.get(20)) else IOBUF(FMC.HB_P.get(20), c)
          case 3 => if(get) c := IOBUF(FMC.HB_N.get(20)) else IOBUF(FMC.HB_N.get(20), c)
          case 4 => if(get) c := IOBUF(FMC.HB_P.get(3)) else IOBUF(FMC.HB_P.get(3), c)
          case 5 => if(get) c := IOBUF(FMC.HB_N.get(3)) else IOBUF(FMC.HB_N.get(3), c)
          case 6 => if(get) c := IOBUF(FMC.HB_P.get(5)) else IOBUF(FMC.HB_P.get(5), c)
          case 7 => if(get) c := IOBUF(FMC.HB_N.get(5)) else IOBUF(FMC.HB_N.get(5), c)
          case 8 => if(get) c := IOBUF(FMC.HB_P.get(9)) else IOBUF(FMC.HB_P.get(9), c)
          case 9 => if(get) c := IOBUF(FMC.HB_N.get(9)) else IOBUF(FMC.HB_N.get(9), c)
          case 10 => if(get) c := IOBUF(FMC.HB_P.get(13)) else IOBUF(FMC.HB_P.get(13), c)
          case 11 => if(get) c := IOBUF(FMC.HB_N.get(13)) else IOBUF(FMC.HB_N.get(13), c)
          case 12 => if(get) c := IOBUF(FMC.HB_P.get(19)) else IOBUF(FMC.HB_P.get(19), c)
          case 13 => if(get) c := IOBUF(FMC.HB_N.get(19)) else IOBUF(FMC.HB_N.get(19), c)
          case 14 => if(get) c := IOBUF(FMC.HA_P(22)) else IOBUF(FMC.HA_P(22), c)
          case 15 => if(get) c := IOBUF(FMC.HA_N(22)) else IOBUF(FMC.HA_N(22), c)
          case 16 => if(get) c := IOBUF(FMC.HB_P.get(7)) else IOBUF(FMC.HB_P.get(7), c)
          case 17 => if(get) c := IOBUF(FMC.HB_N.get(7)) else IOBUF(FMC.HB_N.get(7), c)
          case 18 => if(get) c := IOBUF(FMC.HB_P.get(15)) else IOBUF(FMC.HB_P.get(15), c)
          case 19 => if(get) c := IOBUF(FMC.HB_N.get(15)) else IOBUF(FMC.HB_N.get(15), c)
          case 20 => if(get) c := IOBUF(FMC.HB_P.get(2)) else IOBUF(FMC.HB_P.get(2), c)
          case 21 => if(get) c := IOBUF(FMC.HB_N.get(2)) else IOBUF(FMC.HB_N.get(2), c)
          case 22 => if(get) c := IOBUF(FMC.HB_P.get(4)) else IOBUF(FMC.HB_P.get(4), c)
          case 23 => if(get) c := IOBUF(FMC.HB_N.get(4)) else IOBUF(FMC.HB_N.get(4), c)
          case 24 => if(get) c := IOBUF(FMC.HB_P.get(8)) else IOBUF(FMC.HB_P.get(8), c)
          case 25 => if(get) c := IOBUF(FMC.HB_N.get(8)) else IOBUF(FMC.HB_N.get(8), c)
          case 26 => if(get) c := IOBUF(FMC.HB_P.get(12)) else IOBUF(FMC.HB_P.get(12), c)
          case 27 => if(get) c := IOBUF(FMC.HB_N.get(12)) else IOBUF(FMC.HB_N.get(12), c)
          case 28 => if(get) c := IOBUF(FMC.HB_P.get(16)) else IOBUF(FMC.HB_P.get(16), c)
          case 29 => if(get) c := IOBUF(FMC.HB_N.get(16)) else IOBUF(FMC.HB_N.get(16), c)
          case 30 => if(get) c := IOBUF(FMC.HB_P.get(1)) else IOBUF(FMC.HB_P.get(1), c)
          case 31 => if(get) c := IOBUF(FMC.HB_N.get(1)) else IOBUF(FMC.HB_N.get(1), c)
          case 32 => if(get) c := IOBUF(FMC.HB_P.get(11)) else IOBUF(FMC.HB_P.get(11), c)
          case 33 => if(get) c := IOBUF(FMC.HB_N.get(11)) else IOBUF(FMC.HB_N.get(11), c)
          case 34 => if(get) c := IOBUF(FMC.HB_P.get(18)) else IOBUF(FMC.HB_P.get(18), c)
          case 35 => if(get) c := IOBUF(FMC.HB_N.get(18)) else IOBUF(FMC.HB_N.get(18), c)
          case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
        }
      case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
    }
  }
  def debug(n: Int, p: Int, c: Bool, get: Boolean, FMC: FMCVC707) = {

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
      case 23 =>
        p match {
          case 1 => if(get) c := IOBUF(FMC.HB_P.get(20)) else IOBUF(FMC.HB_P.get(20), c)
          case 2 => if(get) c := IOBUF(FMC.HB_P.get(21)) else IOBUF(FMC.HB_P.get(21), c)
          case 3 => if(get) c := IOBUF(FMC.HB_N.get(20)) else IOBUF(FMC.HB_N.get(20), c)
          case 4 => if(get) c := IOBUF(FMC.HB_N.get(21)) else IOBUF(FMC.HB_N.get(21), c)
          case 5 => if(get) c := IOBUF(FMC.HA_P(20)) else IOBUF(FMC.HA_P(20), c)
          case 6 => if(get) c := IOBUF(FMC.HA_P(22)) else IOBUF(FMC.HA_P(22), c)
          case 7 => if(get) c := IOBUF(FMC.HA_N(20)) else IOBUF(FMC.HA_N(20), c)
          case 8 => if(get) c := IOBUF(FMC.HA_N(22)) else IOBUF(FMC.HA_N(22), c)
          case 9 => if(get) c := IOBUF(FMC.HA_P(21)) else IOBUF(FMC.HA_P(21), c)
          case 10 => if(get) c := IOBUF(FMC.HA_P(23)) else IOBUF(FMC.HA_P(23), c)
          case 11 => if(get) c := IOBUF(FMC.HA_N(21)) else IOBUF(FMC.HA_N(21), c)
          case 12 => if(get) c := IOBUF(FMC.HA_N(23)) else IOBUF(FMC.HA_N(23), c)
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
      case 2 =>
        p match {
          case 1 => if(get) c := IOBUF(FMC.HB_P.get(0)) else IOBUF(FMC.HB_P.get(0), c)
          case 2 => if(get) c := IOBUF(FMC.HB_P.get(10)) else IOBUF(FMC.HB_P.get(10), c)
          case 3 => if(get) c := IOBUF(FMC.HB_N.get(0)) else IOBUF(FMC.HB_N.get(0), c)
          case 4 => if(get) c := IOBUF(FMC.HB_N.get(10)) else IOBUF(FMC.HB_N.get(10), c)
          case 5 => if(get) c := IOBUF(FMC.HB_P.get(1)) else IOBUF(FMC.HB_P.get(1), c)
          case 6 => if(get) c := IOBUF(FMC.HB_P.get(11)) else IOBUF(FMC.HB_P.get(11), c)
          case 7 => if(get) c := IOBUF(FMC.HB_N.get(1)) else IOBUF(FMC.HB_N.get(1), c)
          case 8 => if(get) c := IOBUF(FMC.HB_N.get(11)) else IOBUF(FMC.HB_N.get(11), c)
          case 9 => if(get) c := IOBUF(FMC.HB_P.get(2)) else IOBUF(FMC.HB_P.get(2), c)
          case 10 => if(get) c := IOBUF(FMC.HB_P.get(12)) else IOBUF(FMC.HB_P.get(12), c)
          case 11 => if(get) c := IOBUF(FMC.HB_N.get(2)) else IOBUF(FMC.HB_N.get(2), c)
          case 12 => if(get) c := IOBUF(FMC.HB_N.get(12)) else IOBUF(FMC.HB_N.get(12), c)
          case 13 => if(get) c := IOBUF(FMC.HB_P.get(3)) else IOBUF(FMC.HB_P.get(3), c)
          case 14 => if(get) c := IOBUF(FMC.HB_P.get(13)) else IOBUF(FMC.HB_P.get(13), c)
          case 15 => if(get) c := IOBUF(FMC.HB_N.get(3)) else IOBUF(FMC.HB_N.get(3), c)
          case 16 => if(get) c := IOBUF(FMC.HB_N.get(13)) else IOBUF(FMC.HB_N.get(13), c)
          case 17 => if(get) c := IOBUF(FMC.HB_P.get(4)) else IOBUF(FMC.HB_P.get(4), c)
          case 18 => if(get) c := IOBUF(FMC.HB_P.get(14)) else IOBUF(FMC.HB_P.get(14), c)
          case 19 => if(get) c := IOBUF(FMC.HB_N.get(4)) else IOBUF(FMC.HB_N.get(4), c)
          case 20 => if(get) c := IOBUF(FMC.HB_N.get(14)) else IOBUF(FMC.HB_N.get(14), c)
          case 21 => if(get) c := IOBUF(FMC.HB_P.get(5)) else IOBUF(FMC.HB_P.get(5), c)
          case 22 => if(get) c := IOBUF(FMC.HB_P.get(15)) else IOBUF(FMC.HB_P.get(15), c)
          case 23 => if(get) c := IOBUF(FMC.HB_N.get(5)) else IOBUF(FMC.HB_N.get(5), c)
          case 24 => if(get) c := IOBUF(FMC.HB_N.get(15)) else IOBUF(FMC.HB_N.get(15), c)
          case 25 => if(get) c := IOBUF(FMC.HB_P.get(6)) else IOBUF(FMC.HB_P.get(6), c)
          case 26 => if(get) c := IOBUF(FMC.HB_P.get(16)) else IOBUF(FMC.HB_P.get(16), c)
          case 27 => if(get) c := IOBUF(FMC.HB_N.get(6)) else IOBUF(FMC.HB_N.get(6), c)
          case 28 => if(get) c := IOBUF(FMC.HB_N.get(16)) else IOBUF(FMC.HB_N.get(16), c)
          case 29 => if(get) c := IOBUF(FMC.HB_P.get(7)) else IOBUF(FMC.HB_P.get(7), c)
          case 30 => if(get) c := IOBUF(FMC.HB_P.get(17)) else IOBUF(FMC.HB_P.get(17), c)
          case 31 => if(get) c := IOBUF(FMC.HB_N.get(7)) else IOBUF(FMC.HB_N.get(7), c)
          case 32 => if(get) c := IOBUF(FMC.HB_N.get(17)) else IOBUF(FMC.HB_N.get(17), c)
          case 33 => if(get) c := IOBUF(FMC.HB_P.get(8)) else IOBUF(FMC.HB_P.get(8), c)
          case 34 => if(get) c := IOBUF(FMC.HB_P.get(18)) else IOBUF(FMC.HB_P.get(18), c)
          case 35 => if(get) c := IOBUF(FMC.HB_N.get(8)) else IOBUF(FMC.HB_N.get(8), c)
          case 36 => if(get) c := IOBUF(FMC.HB_N.get(18)) else IOBUF(FMC.HB_N.get(18), c)
          case 37 => if(get) c := IOBUF(FMC.HB_P.get(9)) else IOBUF(FMC.HB_P.get(9), c)
          case 38 => if(get) c := IOBUF(FMC.HB_P.get(19)) else IOBUF(FMC.HB_P.get(19), c)
          case 39 => if(get) c := IOBUF(FMC.HB_N.get(9)) else IOBUF(FMC.HB_N.get(9), c)
          case 40 => if(get) c := IOBUF(FMC.HB_N.get(19)) else IOBUF(FMC.HB_N.get(19), c)
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
      case 3 =>
        p match {
          case 1 => if(get) c := IOBUF(FMC.HA_P(0)) else IOBUF(FMC.HA_P(0), c)
          case 2 => if(get) c := IOBUF(FMC.HA_P(10)) else IOBUF(FMC.HA_P(10), c)
          case 3 => if(get) c := IOBUF(FMC.HA_N(0)) else IOBUF(FMC.HA_N(0), c)
          case 4 => if(get) c := IOBUF(FMC.HA_N(10)) else IOBUF(FMC.HA_N(10), c)
          case 5 => if(get) c := IOBUF(FMC.HA_P(1)) else IOBUF(FMC.HA_P(1), c)
          case 6 => if(get) c := IOBUF(FMC.HA_P(11)) else IOBUF(FMC.HA_P(11), c)
          case 7 => if(get) c := IOBUF(FMC.HA_N(1)) else IOBUF(FMC.HA_N(1), c)
          case 8 => if(get) c := IOBUF(FMC.HA_N(11)) else IOBUF(FMC.HA_N(11), c)
          case 9 => if(get) c := IOBUF(FMC.HA_P(2)) else IOBUF(FMC.HA_P(2), c)
          case 10 => if(get) c := IOBUF(FMC.HA_P(12)) else IOBUF(FMC.HA_P(12), c)
          case 11 => if(get) c := IOBUF(FMC.HA_N(2)) else IOBUF(FMC.HA_N(2), c)
          case 12 => if(get) c := IOBUF(FMC.HA_N(12)) else IOBUF(FMC.HA_N(12), c)
          case 13 => if(get) c := IOBUF(FMC.HA_P(3)) else IOBUF(FMC.HA_P(3), c)
          case 14 => if(get) c := IOBUF(FMC.HA_P(13)) else IOBUF(FMC.HA_P(13), c)
          case 15 => if(get) c := IOBUF(FMC.HA_N(3)) else IOBUF(FMC.HA_N(3), c)
          case 16 => if(get) c := IOBUF(FMC.HA_N(13)) else IOBUF(FMC.HA_N(13), c)
          case 17 => if(get) c := IOBUF(FMC.HA_P(4)) else IOBUF(FMC.HA_P(4), c)
          case 18 => if(get) c := IOBUF(FMC.HA_P(14)) else IOBUF(FMC.HA_P(14), c)
          case 19 => if(get) c := IOBUF(FMC.HA_N(4)) else IOBUF(FMC.HA_N(4), c)
          case 20 => if(get) c := IOBUF(FMC.HA_N(14)) else IOBUF(FMC.HA_N(14), c)
          case 21 => if(get) c := IOBUF(FMC.HA_P(5)) else IOBUF(FMC.HA_P(5), c)
          case 22 => if(get) c := IOBUF(FMC.HA_P(15)) else IOBUF(FMC.HA_P(15), c)
          case 23 => if(get) c := IOBUF(FMC.HA_N(5)) else IOBUF(FMC.HA_N(5), c)
          case 24 => if(get) c := IOBUF(FMC.HA_N(15)) else IOBUF(FMC.HA_N(15), c)
          case 25 => if(get) c := IOBUF(FMC.HA_P(6)) else IOBUF(FMC.HA_P(6), c)
          case 26 => if(get) c := IOBUF(FMC.HA_P(16)) else IOBUF(FMC.HA_P(16), c)
          case 27 => if(get) c := IOBUF(FMC.HA_N(6)) else IOBUF(FMC.HA_N(6), c)
          case 28 => if(get) c := IOBUF(FMC.HA_N(16)) else IOBUF(FMC.HA_N(16), c)
          case 29 => if(get) c := IOBUF(FMC.HA_P(7)) else IOBUF(FMC.HA_P(7), c)
          case 30 => if(get) c := IOBUF(FMC.HA_P(17)) else IOBUF(FMC.HA_P(17), c)
          case 31 => if(get) c := IOBUF(FMC.HA_N(7)) else IOBUF(FMC.HA_N(7), c)
          case 32 => if(get) c := IOBUF(FMC.HA_N(17)) else IOBUF(FMC.HA_N(17), c)
          case 33 => if(get) c := IOBUF(FMC.HA_P(8)) else IOBUF(FMC.HA_P(8), c)
          case 34 => if(get) c := IOBUF(FMC.HA_P(18)) else IOBUF(FMC.HA_P(18), c)
          case 35 => if(get) c := IOBUF(FMC.HA_N(8)) else IOBUF(FMC.HA_N(8), c)
          case 36 => if(get) c := IOBUF(FMC.HA_N(18)) else IOBUF(FMC.HA_N(18), c)
          case 37 => if(get) c := IOBUF(FMC.HA_P(9)) else IOBUF(FMC.HA_P(9), c)
          case 38 => if(get) c := IOBUF(FMC.HA_P(19)) else IOBUF(FMC.HA_P(19), c)
          case 39 => if(get) c := IOBUF(FMC.HA_N(9)) else IOBUF(FMC.HA_N(9), c)
          case 40 => if(get) c := IOBUF(FMC.HA_N(19)) else IOBUF(FMC.HA_N(19), c)
          case _ => throw new RuntimeException(s"J${n}_${p} does not exist")
        }
      case _ => throw new RuntimeException(s"J${n}_${p} does not exist")
    }
  }
}

class FPGAVC707InternalNoChip
(
  val idBits: Int = 4,
  val widthBits: Int = 32,
  val sinkBits: Int = 1
)(implicit p :Parameters) extends FPGAVC707Internal(None)(p) {
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
}

trait WithFPGAVC707InternCreate {
  this: FPGAVC707Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGAVC707Internal(Some(chip)))
}

trait WithFPGAVC707InternNoChipCreate {
  this: FPGAVC707Shell =>
  def idBits = 4
  def widthBits = 32
  def sinkBits = 1
  val intern = Module(new FPGAVC707InternalNoChip(idBits, widthBits, sinkBits))
}

trait WithFPGAVC707InternConnect {
  this: FPGAVC707Shell =>
  val intern: FPGAVC707Internal

  // To intern = Clocks and resets
  intern.sys_clock_p := sys_clock_p
  intern.sys_clock_n := sys_clock_n
  intern.rst_0 := rst_0
  intern.rst_1 := rst_1
  intern.rst_2 := rst_2
  intern.rst_3 := rst_3
  ddr = intern.ddr.map{ A =>
    val port = IO(new VC707MIGIODDR(intern.depth))
    port <> A
    port
  }
}

trait WithFPGAVC707PureConnect {
  this: FPGAVC707Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  def namedclocks: Seq[String] = chip.system.sys.asInstanceOf[HasTEEHWSystemModule].namedclocks
  // This trait connects the chip to all essentials. This assumes no DDR is connected yet

  def PCIPORT = FMC1_HPC
  def MISCPORT = FMC2_HPC
  
  gpio_out := chip.gpio_out
  chip.gpio_in := gpio_in
  jtag <> chip.jtag
  chip.jtag.jtag_TCK := IBUFG(jtag.jtag_TCK.asClock).asUInt
  chip.uart_rxd := uart_rxd	  // UART_TXD
  uart_txd := chip.uart_txd 	// UART_RXD
  sdio <> chip.sdio

  // Connected to MISCPORT
  chip.usb11hs.foreach{ case chipport =>
    val USBWireDataIn = Wire(Vec(2, Bool()))
    ConnectFMCXilinxGPIO.debug(1, 1, USBWireDataIn(0), true, MISCPORT)
    ConnectFMCXilinxGPIO(1, 2, USBWireDataIn(1), true, MISCPORT)
    chipport.USBWireDataIn := USBWireDataIn.asUInt()
    ConnectFMCXilinxGPIO(1, 3, chipport.USBWireDataOut(0), false, MISCPORT)
    ConnectFMCXilinxGPIO(1, 4, chipport.USBWireDataOut(1), false, MISCPORT)
    ConnectFMCXilinxGPIO(1, 5, chipport.USBWireCtrlOut, false, MISCPORT)
    ConnectFMCXilinxGPIO(1, 6, chipport.USBFullSpeed, false, MISCPORT)
  }

  chip.qspi.foreach { case qspi =>
    ConnectFMCXilinxGPIO(1, 7, qspi.qspi_cs(0), false, MISCPORT)
    ConnectFMCXilinxGPIO(1, 8, qspi.qspi_sck, false, MISCPORT)
    ConnectFMCXilinxGPIO(1, 9, qspi.qspi_miso, true, MISCPORT)
    ConnectFMCXilinxGPIO(1, 10, qspi.qspi_mosi, false, MISCPORT)
    ConnectFMCXilinxGPIO(1, 11, qspi.qspi_wp, false, MISCPORT)
    ConnectFMCXilinxGPIO(1, 12, qspi.qspi_hold, false, MISCPORT)
  }
}

trait WithFPGAVC707Connect extends WithFPGAVC707PureConnect 
  with WithFPGAVC707InternCreate 
  with WithFPGAVC707InternConnect {
  this: FPGAVC707Shell =>

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // Platform connections (override)
  gpio_out := Cat(chip.gpio_out(chip.gpio_out.getWidth-1, 1), intern.init_calib_complete)
  (chip.usb11hs zip intern.usbClk).foreach { case (chipport, uclk) =>
    chipport.usbClk := uclk
  }

  // PCIe (if available)
  chip.pciePorts.foreach{ case chipport =>
    chipport.REFCLK_rxp := PCIPORT.GBTCLK_M2C_P.get(0)
    chipport.REFCLK_rxn := PCIPORT.GBTCLK_M2C_N.get(0)
    PCIPORT.DP_C2M_P.get(5) := chipport.pci_exp_txp
    PCIPORT.DP_C2M_N.get(5) := chipport.pci_exp_txn
    chipport.pci_exp_rxp := PCIPORT.DP_M2C_P.get(5)
    chipport.pci_exp_rxn := PCIPORT.DP_M2C_N.get(5)
    chipport.axi_aresetn := intern.rst_n
    chipport.axi_ctl_aresetn := intern.rst_n
  }
  chip.xdmaPorts.foreach{ port =>
    // Nothing
  }
}

// Trait which connects the FPGA as is going to be connected to the chip
// Based on layout of the TR5.sch done by Duy
// Also based on the xdc created by Ahn-Dao
trait WithFPGAVC707ToChipConnect extends WithFPGAVC707InternNoChipCreate with WithFPGAVC707InternConnect {
  this: FPGAVC707Shell =>

  // ******* Duy section ******
  // NOTES:
  // JP18 -> JP1
  def JP18 = 1 // GPIO0
  def JP19 = 0 // GPIO1
  def JP20 = 2 // GPIO2
  def JP21 = 3 // GPIO3
  def FMC = FMC1_HPC

  // From intern = Clocks and resets
  intern.ChildClock.foreach{ a =>
    //ConnectFMCXilinxGPIO(JP21, 2, a.asBool(), false, FMC) // Is useless anyway
  }
  intern.ChildReset.foreach{ a =>
    ConnectFMCXilinxGPIO(JP18, 5, a, false, FMC)
  }
  //ConnectFMCXilinxGPIO(JP21, 4, intern.sys_clk.asBool(), false, FMC)
  ConnectFMCXilinxGPIO(JP18, 2, intern.rst_n, false, FMC)
  ConnectFMCXilinxGPIO(JP18, 6, intern.jrst_n, false, FMC)
  // Memory port serialized
  intern.memser.foreach{ a => a } // NOTHING
  // Ext port serialized
  intern.extser.foreach{ a => a } // NOTHING
  // Memory port
  intern.tlport.foreach{ case tlport =>
    ConnectFMCXilinxGPIO(JP18, 1, tlport.a.valid, true, FMC)
    ConnectFMCXilinxGPIO(JP18, 4, tlport.a.ready, false, FMC)
    require(tlport.a.bits.opcode.getWidth == 3, s"${tlport.a.bits.opcode.getWidth}")
    val a_opcode = Wire(Vec(3, Bool()))
    ConnectFMCXilinxGPIO(JP18, 3, a_opcode(2), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 7, a_opcode(1), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 8, a_opcode(0), true, FMC)
    tlport.a.bits.opcode := a_opcode.asUInt()
    require(tlport.a.bits.param.getWidth == 3, s"${tlport.a.bits.param.getWidth}")
    val a_param = Wire(Vec(3, Bool()))
    ConnectFMCXilinxGPIO(JP18, 9, a_param(2), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 10, a_param(1), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 13, a_param(0), true, FMC)
    tlport.a.bits.param := a_param.asUInt()
    val a_size = Wire(Vec(3, Bool()))
    require(tlport.a.bits.size.getWidth == 3, s"${tlport.a.bits.size.getWidth}")
    ConnectFMCXilinxGPIO(JP18, 14, a_size(2), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 15, a_size(1), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 16, a_size(0), true, FMC)
    tlport.a.bits.size := a_size.asUInt()
    require(tlport.a.bits.source.getWidth == 6, s"${tlport.a.bits.source.getWidth}")
    val a_source = Wire(Vec(6, Bool()))
    ConnectFMCXilinxGPIO(JP18, 17, a_source(5), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 18, a_source(4), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 19, a_source(3), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 20, a_source(2), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 21, a_source(1), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 22, a_source(0), true, FMC)
    tlport.a.bits.source := a_source.asUInt()
    require(tlport.a.bits.address.getWidth == 32, s"${tlport.a.bits.address.getWidth}")
    val a_address = Wire(Vec(32, Bool()))
    ConnectFMCXilinxGPIO(JP18, 23, a_address(31), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 24, a_address(30), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 25, a_address(29), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 26, a_address(28), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 27, a_address(27), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 28, a_address(26), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 31, a_address(25), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 32, a_address(24), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 33, a_address(23), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 34, a_address(22), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 35, a_address(21), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 36, a_address(20), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 37, a_address(19), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 38, a_address(18), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 39, a_address(17), true, FMC)
    ConnectFMCXilinxGPIO(JP18, 40, a_address(16), true, FMC)
    ConnectFMCXilinxGPIO(JP19,  1, a_address(15), true, FMC)
    ConnectFMCXilinxGPIO(JP19,  2, a_address(14), true, FMC)
    ConnectFMCXilinxGPIO(JP19,  2, a_address(13), true, FMC)
    ConnectFMCXilinxGPIO(JP19,  4, a_address(12), true, FMC)
    ConnectFMCXilinxGPIO(JP19,  5, a_address(11), true, FMC)
    ConnectFMCXilinxGPIO(JP19,  6, a_address(10), true, FMC)
    ConnectFMCXilinxGPIO(JP19,  7, a_address( 9), true, FMC)
    ConnectFMCXilinxGPIO(JP19,  8, a_address( 8), true, FMC)
    ConnectFMCXilinxGPIO(JP19,  9, a_address( 7), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 10, a_address( 6), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 13, a_address( 5), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 14, a_address( 4), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 15, a_address( 3), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 16, a_address( 2), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 17, a_address( 1), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 18, a_address( 0), true, FMC)
    tlport.a.bits.address := a_address.asUInt()
    require(tlport.a.bits.mask.getWidth == 4, s"${tlport.a.bits.mask.getWidth}")
    val a_mask = Wire(Vec(4, Bool()))
    ConnectFMCXilinxGPIO(JP19, 19, a_mask(3), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 20, a_mask(2), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 21, a_mask(1), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 22, a_mask(0), true, FMC)
    tlport.a.bits.mask := a_mask.asUInt()
    require(tlport.a.bits.data.getWidth == 32, s"${tlport.a.bits.data.getWidth}")
    val a_data = Wire(Vec(32, Bool()))
    ConnectFMCXilinxGPIO(JP19, 23, a_data(31), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 24, a_data(30), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 25, a_data(29), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 26, a_data(28), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 27, a_data(27), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 28, a_data(26), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 31, a_data(25), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 32, a_data(24), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 33, a_data(23), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 34, a_data(22), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 35, a_data(21), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 36, a_data(20), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 37, a_data(19), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 38, a_data(18), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 39, a_data(17), true, FMC)
    ConnectFMCXilinxGPIO(JP19, 40, a_data(16), true, FMC)
    ConnectFMCXilinxGPIO(JP20, 10, a_data(15), true, FMC)
    ConnectFMCXilinxGPIO(JP20,  9, a_data(14), true, FMC)
    ConnectFMCXilinxGPIO(JP20,  8, a_data(13), true, FMC)
    ConnectFMCXilinxGPIO(JP20,  7, a_data(12), true, FMC)
    ConnectFMCXilinxGPIO(JP20,  6, a_data(11), true, FMC)
    ConnectFMCXilinxGPIO(JP20,  5, a_data(10), true, FMC)
    ConnectFMCXilinxGPIO(JP20,  4, a_data( 9), true, FMC)
    ConnectFMCXilinxGPIO(JP20,  3, a_data( 8), true, FMC)
    ConnectFMCXilinxGPIO(JP20,  2, a_data( 7), true, FMC)
    ConnectFMCXilinxGPIO(JP20,  1, a_data( 6), true, FMC)
    ConnectFMCXilinxGPIO(JP20, 13, a_data( 5), true, FMC)
    ConnectFMCXilinxGPIO(JP20, 14, a_data( 4), true, FMC)
    ConnectFMCXilinxGPIO(JP20, 15, a_data( 3), true, FMC)
    ConnectFMCXilinxGPIO(JP20, 16, a_data( 2), true, FMC)
    ConnectFMCXilinxGPIO(JP20, 17, a_data( 1), true, FMC)
    ConnectFMCXilinxGPIO(JP20, 18, a_data( 0), true, FMC)
    tlport.a.bits.data := a_data.asUInt()
    ConnectFMCXilinxGPIO(JP20, 19, tlport.a.bits.corrupt, true, FMC)
    ConnectFMCXilinxGPIO(JP20, 20, tlport.d.ready, true, FMC)
    ConnectFMCXilinxGPIO(JP20, 21, tlport.d.valid, false, FMC)
    require(tlport.d.bits.opcode.getWidth == 3, s"${tlport.d.bits.opcode.getWidth}")
    ConnectFMCXilinxGPIO(JP20, 22, tlport.d.bits.opcode(2), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 23, tlport.d.bits.opcode(1), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 24, tlport.d.bits.opcode(0), false, FMC)
    require(tlport.d.bits.param.getWidth == 2, s"${tlport.d.bits.param.getWidth}")
    ConnectFMCXilinxGPIO(JP20, 25, tlport.d.bits.param(1), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 26, tlport.d.bits.param(0), false, FMC)
    require(tlport.d.bits.size.getWidth == 3, s"${tlport.d.bits.size.getWidth}")
    ConnectFMCXilinxGPIO(JP20, 27, tlport.d.bits.size(2), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 28, tlport.d.bits.size(1), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 31, tlport.d.bits.size(0), false, FMC)
    require(tlport.d.bits.source.getWidth == 6, s"${tlport.d.bits.source.getWidth}")
    ConnectFMCXilinxGPIO(JP20, 32, tlport.d.bits.source(5), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 33, tlport.d.bits.source(4), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 34, tlport.d.bits.source(3), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 35, tlport.d.bits.source(2), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 36, tlport.d.bits.source(1), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 37, tlport.d.bits.source(0), false, FMC)
    require(tlport.d.bits.sink.getWidth == 1, s"${tlport.d.bits.sink.getWidth}")
    ConnectFMCXilinxGPIO(JP20, 38, tlport.d.bits.sink(0), false, FMC)
    ConnectFMCXilinxGPIO(JP20, 39, tlport.d.bits.denied, false, FMC)
    ConnectFMCXilinxGPIO(JP20, 40, tlport.d.bits.corrupt, false, FMC)
    require(tlport.d.bits.data.getWidth == 32, s"${tlport.d.bits.data.getWidth}")
    ConnectFMCXilinxGPIO(JP21, 5, tlport.d.bits.data(31), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 6, tlport.d.bits.data(30), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 7, tlport.d.bits.data(29), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 8, tlport.d.bits.data(28), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 9, tlport.d.bits.data(27), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 10, tlport.d.bits.data(26), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 13, tlport.d.bits.data(25), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 14, tlport.d.bits.data(24), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 15, tlport.d.bits.data(23), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 16, tlport.d.bits.data(22), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 17, tlport.d.bits.data(21), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 18, tlport.d.bits.data(20), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 19, tlport.d.bits.data(19), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 20, tlport.d.bits.data(18), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 21, tlport.d.bits.data(17), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 22, tlport.d.bits.data(16), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 23, tlport.d.bits.data(15), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 24, tlport.d.bits.data(14), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 25, tlport.d.bits.data(13), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 26, tlport.d.bits.data(12), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 27, tlport.d.bits.data(11), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 28, tlport.d.bits.data(10), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 31, tlport.d.bits.data( 9), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 32, tlport.d.bits.data( 8), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 33, tlport.d.bits.data( 7), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 34, tlport.d.bits.data( 6), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 35, tlport.d.bits.data( 5), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 36, tlport.d.bits.data( 4), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 37, tlport.d.bits.data( 3), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 38, tlport.d.bits.data( 2), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 39, tlport.d.bits.data( 1), false, FMC)
    ConnectFMCXilinxGPIO(JP21, 40, tlport.d.bits.data( 0), false, FMC)
  }

  // ******* Ahn-Dao section ******
  def FMCDAO = FMC2_HPC
  ConnectFMCXilinxGPIO(0, 1, intern.sys_clk.asBool(), false, FMCDAO)
  intern.ChildClock.foreach{ a => PUT(a.asBool(), FMCDAO.CLK_M2C_N(1))}
  ConnectFMCXilinxGPIO(0, 27, intern.jrst_n, false, FMC)
  ConnectFMCXilinxGPIO(1, 15, intern.rst_n, false, FMCDAO)
  intern.aclocks.foreach{ aclocks =>
    // Only some of the aclocks are actually connected.
    println("Connecting orphan clocks =>")
    (aclocks zip intern.namedclocks).foreach{ case (aclk, nam) =>
      println(s"  Detected clock ${nam}")
      if(nam.contains("cryptobus")) {
        println("    Connected to CLK_M2C_P(1) or SMA_CLK_P")
        PUT(aclk.asBool(), FMCDAO.CLK_M2C_P(1))
      }
      if(nam.contains("tile_0")) {
        println("    Connected to USER_SMA_CLOCK_P")
        PUT(aclk.asBool(), USER_SMA_CLOCK_P)
      }
      if(nam.contains("tile_1")) {
        println("    Connected to USER_SMA_CLOCK_N")
        PUT(aclk.asBool(), USER_SMA_CLOCK_N)
      }
    }
  }
  // ExtSerMem
  intern.memser.foreach { memser =>
    ConnectFMCXilinxGPIO(0, 31, memser.in.bits(7), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 33, memser.in.bits(6), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 35, memser.in.bits(5), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 37, memser.in.bits(4), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 39, memser.in.bits(3), false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 1, memser.in.bits(2), false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 5, memser.in.bits(1), false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 7, memser.in.bits(0), false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 9, memser.in.valid, false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 13, memser.out.ready, false, FMCDAO)
    ConnectFMCXilinxGPIO(2, 5, memser.in.ready, true, FMCDAO)
    val out_bits = Wire(Vec(8, Bool()))
    ConnectFMCXilinxGPIO(2, 7, out_bits(7), true, FMCDAO)
    ConnectFMCXilinxGPIO(2, 9, out_bits(6), true, FMCDAO)
    ConnectFMCXilinxGPIO(2, 13, out_bits(5), true, FMCDAO)
    ConnectFMCXilinxGPIO(2, 15, out_bits(4), true, FMCDAO)
    ConnectFMCXilinxGPIO(2, 17, out_bits(3), true, FMCDAO)
    ConnectFMCXilinxGPIO(2, 19, out_bits(2), true, FMCDAO)
    ConnectFMCXilinxGPIO(2, 21, out_bits(1), true, FMCDAO)
    ConnectFMCXilinxGPIO(2, 23, out_bits(0), true, FMCDAO)
    memser.out.bits := out_bits.asUInt()
    ConnectFMCXilinxGPIO(2, 25, memser.out.valid, true, FMCDAO)
  }
  // ExtSerBus
  intern.extser.foreach{ extser =>
    ConnectFMCXilinxGPIO(0, 5, extser.in.bits(7), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 7, extser.in.bits(6), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 9, extser.in.bits(5), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 13, extser.in.bits(4), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 15, extser.in.bits(3), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 17, extser.in.bits(2), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 19, extser.in.bits(1), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 21, extser.in.bits(0), false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 23, extser.in.valid, false, FMCDAO)
    ConnectFMCXilinxGPIO(0, 25, extser.out.ready, false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 17, extser.in.ready, true, FMCDAO)
    val out_bits = Wire(Vec(8, Bool()))
    ConnectFMCXilinxGPIO(1, 19, out_bits(7), true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 21, out_bits(6), true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 25, out_bits(5), true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 27, out_bits(4), true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 31, out_bits(3), true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 33, out_bits(2), true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 35, out_bits(1), true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 37, out_bits(0), true, FMCDAO)
    extser.out.bits := out_bits.asUInt()
    ConnectFMCXilinxGPIO(1, 39, extser.out.valid, true, FMCDAO)
  }

  // ******** Misc part ********

  // LEDs
  gpio_out := intern.init_calib_complete
  
  // Nullify these ports. Not needed
  jtag.jtag_TDO := false.B
  sdio.sdio_dat_3 := false.B
  sdio.sdio_cmd := false.B
  sdio.sdio_clk := false.B
  uart_txd := false.B
}

// Trait which connects the FPGA the chip
// Based on layout of the TR5.sch done by Duy
// Also based on the xdc created by Ahn-Dao

trait WithFPGAVC707FromChipConnect extends WithFPGAVC707PureConnect {
  this: FPGAVC707Shell =>

  // ******* Duy section ******
  // NOTES:
  // JP18 -> JP1
  def JP18 = 1 // GPIO0
  def JP19 = 0 // GPIO1
  def JP20 = 2 // GPIO2
  def JP21 = 3 // GPIO3
  def FMC = FMC1_HPC

  // From intern = Clocks and resets
  chip.ChildReset.foreach{ a =>
    ConnectFMCXilinxGPIO(JP18, 5, a,  true, FMC)
  }
  //ConnectFMCXilinxGPIO(JP18, 2, chip.rst_n,  true, FMC) // TODO: This is not connected in DUY form
  //ConnectFMCXilinxGPIO(JP18, 6, chip.jrst_n,  true, FMC) // TODO: This is not connected in DUY form
  // Memory port
  chip.tlport.foreach{ case tlport =>
    ConnectFMCXilinxGPIO(JP18, 1, tlport.a.valid,  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 4, tlport.a.ready,  true, FMC)
    require(tlport.a.bits.opcode.getWidth == 3, s"${tlport.a.bits.opcode.getWidth}")
    val a_opcode = Wire(Vec(3, Bool()))
    ConnectFMCXilinxGPIO(JP18, 3, a_opcode(2),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 7, a_opcode(1),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 8, a_opcode(0),  false, FMC)
    a_opcode := tlport.a.bits.opcode.toBools()
    require(tlport.a.bits.param.getWidth == 3, s"${tlport.a.bits.param.getWidth}")
    val a_param = Wire(Vec(3, Bool()))
    ConnectFMCXilinxGPIO(JP18, 9, a_param(2),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 10, a_param(1),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 13, a_param(0),  false, FMC)
    a_param := tlport.a.bits.param.toBools()
    val a_size = Wire(Vec(3, Bool()))
    require(tlport.a.bits.size.getWidth == 3, s"${tlport.a.bits.size.getWidth}")
    ConnectFMCXilinxGPIO(JP18, 14, a_size(2),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 15, a_size(1),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 16, a_size(0),  false, FMC)
    a_size := tlport.a.bits.size.toBools()
    require(tlport.a.bits.source.getWidth == 6, s"${tlport.a.bits.source.getWidth}")
    val a_source = Wire(Vec(6, Bool()))
    ConnectFMCXilinxGPIO(JP18, 17, a_source(5),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 18, a_source(4),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 19, a_source(3),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 20, a_source(2),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 21, a_source(1),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 22, a_source(0),  false, FMC)
    a_source := tlport.a.bits.source.toBools()
    require(tlport.a.bits.address.getWidth == 32, s"${tlport.a.bits.address.getWidth}")
    val a_address = Wire(Vec(32, Bool()))
    ConnectFMCXilinxGPIO(JP18, 23, a_address(31),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 24, a_address(30),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 25, a_address(29),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 26, a_address(28),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 27, a_address(27),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 28, a_address(26),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 31, a_address(25),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 32, a_address(24),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 33, a_address(23),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 34, a_address(22),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 35, a_address(21),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 36, a_address(20),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 37, a_address(19),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 38, a_address(18),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 39, a_address(17),  false, FMC)
    ConnectFMCXilinxGPIO(JP18, 40, a_address(16),  false, FMC)
    ConnectFMCXilinxGPIO(JP19,  1, a_address(15),  false, FMC)
    ConnectFMCXilinxGPIO(JP19,  2, a_address(14),  false, FMC)
    ConnectFMCXilinxGPIO(JP19,  2, a_address(13),  false, FMC)
    ConnectFMCXilinxGPIO(JP19,  4, a_address(12),  false, FMC)
    ConnectFMCXilinxGPIO(JP19,  5, a_address(11),  false, FMC)
    ConnectFMCXilinxGPIO(JP19,  6, a_address(10),  false, FMC)
    ConnectFMCXilinxGPIO(JP19,  7, a_address( 9),  false, FMC)
    ConnectFMCXilinxGPIO(JP19,  8, a_address( 8),  false, FMC)
    ConnectFMCXilinxGPIO(JP19,  9, a_address( 7),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 10, a_address( 6),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 13, a_address( 5),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 14, a_address( 4),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 15, a_address( 3),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 16, a_address( 2),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 17, a_address( 1),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 18, a_address( 0),  false, FMC)
    a_address := tlport.a.bits.address.toBools()
    require(tlport.a.bits.mask.getWidth == 4, s"${tlport.a.bits.mask.getWidth}")
    val a_mask = Wire(Vec(4, Bool()))
    ConnectFMCXilinxGPIO(JP19, 19, a_mask(3),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 20, a_mask(2),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 21, a_mask(1),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 22, a_mask(0),  false, FMC)
    a_mask := tlport.a.bits.mask.toBools()
    require(tlport.a.bits.data.getWidth == 32, s"${tlport.a.bits.data.getWidth}")
    val a_data = Wire(Vec(32, Bool()))
    ConnectFMCXilinxGPIO(JP19, 23, a_data(31),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 24, a_data(30),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 25, a_data(29),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 26, a_data(28),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 27, a_data(27),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 28, a_data(26),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 31, a_data(25),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 32, a_data(24),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 33, a_data(23),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 34, a_data(22),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 35, a_data(21),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 36, a_data(20),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 37, a_data(19),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 38, a_data(18),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 39, a_data(17),  false, FMC)
    ConnectFMCXilinxGPIO(JP19, 40, a_data(16),  false, FMC)
    ConnectFMCXilinxGPIO(JP20, 10, a_data(15),  false, FMC)
    ConnectFMCXilinxGPIO(JP20,  9, a_data(14),  false, FMC)
    ConnectFMCXilinxGPIO(JP20,  8, a_data(13),  false, FMC)
    ConnectFMCXilinxGPIO(JP20,  7, a_data(12),  false, FMC)
    ConnectFMCXilinxGPIO(JP20,  6, a_data(11),  false, FMC)
    ConnectFMCXilinxGPIO(JP20,  5, a_data(10),  false, FMC)
    ConnectFMCXilinxGPIO(JP20,  4, a_data( 9),  false, FMC)
    ConnectFMCXilinxGPIO(JP20,  3, a_data( 8),  false, FMC)
    ConnectFMCXilinxGPIO(JP20,  2, a_data( 7),  false, FMC)
    ConnectFMCXilinxGPIO(JP20,  1, a_data( 6),  false, FMC)
    ConnectFMCXilinxGPIO(JP20, 13, a_data( 5),  false, FMC)
    ConnectFMCXilinxGPIO(JP20, 14, a_data( 4),  false, FMC)
    ConnectFMCXilinxGPIO(JP20, 15, a_data( 3),  false, FMC)
    ConnectFMCXilinxGPIO(JP20, 16, a_data( 2),  false, FMC)
    ConnectFMCXilinxGPIO(JP20, 17, a_data( 1),  false, FMC)
    ConnectFMCXilinxGPIO(JP20, 18, a_data( 0),  false, FMC)
    a_data := tlport.a.bits.data.toBools()
    ConnectFMCXilinxGPIO(JP20, 19, tlport.a.bits.corrupt,  false, FMC)
    ConnectFMCXilinxGPIO(JP20, 20, tlport.d.ready,  false, FMC)
    ConnectFMCXilinxGPIO(JP20, 21, tlport.d.valid,  true, FMC)
    require(tlport.d.bits.opcode.getWidth == 3, s"${tlport.d.bits.opcode.getWidth}")
    val d_opcode = Wire(Vec(3, Bool()))
    ConnectFMCXilinxGPIO(JP20, 22, d_opcode(2),  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 23, d_opcode(1),  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 24, d_opcode(0),  true, FMC)
    tlport.d.bits.opcode := d_opcode.asUInt()
    require(tlport.d.bits.param.getWidth == 2, s"${tlport.d.bits.param.getWidth}")
    val d_param = Wire(Vec(2, Bool()))
    ConnectFMCXilinxGPIO(JP20, 25, d_param(1),  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 26, d_param(0),  true, FMC)
    tlport.d.bits.param := d_param.asUInt()
    require(tlport.d.bits.size.getWidth == 3, s"${tlport.d.bits.size.getWidth}")
    val d_size = Wire(Vec(2, Bool()))
    ConnectFMCXilinxGPIO(JP20, 27, d_size(2),  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 28, d_size(1),  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 31, d_size(0),  true, FMC)
    tlport.d.bits.size := d_size.asUInt()
    require(tlport.d.bits.source.getWidth == 6, s"${tlport.d.bits.source.getWidth}")
    val d_source = Wire(Vec(6, Bool()))
    ConnectFMCXilinxGPIO(JP20, 32, d_source(5),  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 33, d_source(4),  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 34, d_source(3),  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 35, d_source(2),  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 36, d_source(1),  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 37, d_source(0),  true, FMC)
    tlport.d.bits.source := d_source.asUInt()
    require(tlport.d.bits.sink.getWidth == 1, s"${tlport.d.bits.sink.getWidth}")
    val d_sink = Wire(Vec(1, Bool()))
    ConnectFMCXilinxGPIO(JP20, 38, d_sink(0),  true, FMC)
    tlport.d.bits.sink := d_sink.asUInt()
    ConnectFMCXilinxGPIO(JP20, 39, tlport.d.bits.denied,  true, FMC)
    ConnectFMCXilinxGPIO(JP20, 40, tlport.d.bits.corrupt,  true, FMC)
    require(tlport.d.bits.data.getWidth == 32, s"${tlport.d.bits.data.getWidth}")
    val d_data = Wire(Vec(32, Bool()))
    ConnectFMCXilinxGPIO(JP21, 5, d_data(31),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 6, d_data(30),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 7, d_data(29),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 8, d_data(28),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 9, d_data(27),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 10, d_data(26),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 13, d_data(25),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 14, d_data(24),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 15, d_data(23),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 16, d_data(22),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 17, d_data(21),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 18, d_data(20),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 19, d_data(19),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 20, d_data(18),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 21, d_data(17),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 22, d_data(16),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 23, d_data(15),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 24, d_data(14),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 25, d_data(13),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 26, d_data(12),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 27, d_data(11),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 28, d_data(10),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 31, d_data( 9),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 32, d_data( 8),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 33, d_data( 7),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 34, d_data( 6),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 35, d_data( 5),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 36, d_data( 4),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 37, d_data( 3),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 38, d_data( 2),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 39, d_data( 1),  true, FMC)
    ConnectFMCXilinxGPIO(JP21, 40, d_data( 0),  true, FMC)
    tlport.d.bits.data := d_data.asUInt()
  }

  // ******* Ahn-Dao section ******
  def FMCDAO = FMC2_HPC
  val sysclk = Wire(Bool())
  ConnectFMCXilinxGPIO(0, 1, sysclk,  true, FMCDAO)
  chip.sys_clk := sysclk.asClock()
  chip.ChildClock.foreach{ a => a := GET(FMCDAO.CLK_M2C_N(1))}
  ConnectFMCXilinxGPIO(0, 27, chip.jrst_n,  true, FMCDAO)
  ConnectFMCXilinxGPIO(1, 15, chip.rst_n,  true, FMCDAO)
  chip.aclocks.foreach{ aclocks =>
    // Only some of the aclocks are actually connected.
    println("Connecting orphan clocks =>")
    (aclocks zip namedclocks).foreach{ case (aclk, nam) =>
      println(s"  Detected clock ${nam}")
      aclk := sysclk.asClock()
      if(nam.contains("cryptobus")) {
        println("    Connected to CLK_M2C_P(1) or SMA_CLK_P")
        aclk := GET(FMCDAO.CLK_M2C_P(1)).asClock()
      }
      if(nam.contains("tile_0")) {
        println("    Connected to USER_SMA_CLOCK_P")
        aclk := GET(USER_SMA_CLOCK_P).asClock()
      }
      if(nam.contains("tile_1")) {
        println("    Connected to USER_SMA_CLOCK_N")
        aclk := GET(USER_SMA_CLOCK_N).asClock()
      }
    }
  }
  // ExtSerMem
  chip.memser.foreach { memser =>
    val in_bits = Wire(Vec(8, Bool()))
    ConnectFMCXilinxGPIO(0, 31, in_bits(7),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 33, in_bits(6),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 35, in_bits(5),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 37, in_bits(4),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 39, in_bits(3),  true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 1, in_bits(2),  true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 5, in_bits(1),  true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 7, in_bits(0),  true, FMCDAO)
    memser.in.bits := in_bits.asUInt()
    ConnectFMCXilinxGPIO(1, 9, memser.in.valid,  true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 13, memser.out.ready,  true, FMCDAO)
    ConnectFMCXilinxGPIO(2, 5, memser.in.ready,  false, FMCDAO)
    ConnectFMCXilinxGPIO(2, 7, memser.out.bits(7),  false, FMCDAO)
    ConnectFMCXilinxGPIO(2, 9, memser.out.bits(6),  false, FMCDAO)
    ConnectFMCXilinxGPIO(2, 13, memser.out.bits(5),  false, FMCDAO)
    ConnectFMCXilinxGPIO(2, 15, memser.out.bits(4),  false, FMCDAO)
    ConnectFMCXilinxGPIO(2, 17, memser.out.bits(3),  false, FMCDAO)
    ConnectFMCXilinxGPIO(2, 19, memser.out.bits(2),  false, FMCDAO)
    ConnectFMCXilinxGPIO(2, 21, memser.out.bits(1),  false, FMCDAO)
    ConnectFMCXilinxGPIO(2, 23, memser.out.bits(0),  false, FMCDAO)
    ConnectFMCXilinxGPIO(2, 25, memser.out.valid,  false, FMCDAO)
  }
  // ExtSerBus
  chip.extser.foreach{ extser =>
    val in_bits = Wire(Vec(8, Bool()))
    ConnectFMCXilinxGPIO(0, 5, in_bits(7),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 7, in_bits(6),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 9, in_bits(5),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 13, in_bits(4),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 15, in_bits(3),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 17, in_bits(2),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 19, in_bits(1),  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 21, in_bits(0),  true, FMCDAO)
    extser.in.bits := in_bits.asUInt()
    ConnectFMCXilinxGPIO(0, 23, extser.in.valid,  true, FMCDAO)
    ConnectFMCXilinxGPIO(0, 25, extser.out.ready,  true, FMCDAO)
    ConnectFMCXilinxGPIO(1, 17, extser.in.ready,  false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 19, extser.out.bits(7),  false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 21, extser.out.bits(6),  false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 25, extser.out.bits(5),  false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 27, extser.out.bits(4),  false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 31, extser.out.bits(3),  false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 33, extser.out.bits(2),  false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 35, extser.out.bits(1),  false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 37, extser.out.bits(0),  false, FMCDAO)
    ConnectFMCXilinxGPIO(1, 39, extser.out.valid,  false, FMCDAO)
  }

  // ******** Misc part ********
}