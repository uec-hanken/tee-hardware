package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import sifive.blocks.devices.spi._
import sifive.blocks.devices.gpio.PeripheryGPIOKey
import sifive.fpgashells.clocks._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.ip.xilinx.vc707mig._
import sifive.fpgashells.shell.xilinx.XDMATopPads
import uec.teehardware._
import uec.teehardware.macros._
import uec.teehardware.devices.clockctrl._
import uec.teehardware.devices.sdram.SDRAMKey
import uec.teehardware.devices.usb11hs._
import uec.teehardware.devices.sifiveblocks._
import uec.teehardware.devices.tlmemext._

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
  val gpio_in = IO(Vec(8, Analog(1.W)))
  val gpio_out = IO(Vec(8, Analog(1.W)))
  val jtag = IO(new Bundle {
    val jtag_TDI = Analog(1.W) // J19_17 / XADC_GPIO_1
    val jtag_TDO = Analog(1.W) // J19_20 / XADC_GPIO_2
    val jtag_TCK = Analog(1.W) // J19_19 / XADC_GPIO_3
    val jtag_TMS = Analog(1.W) // J19_18 / XADC_GPIO_0
  })
  val sdio = IO(new Bundle{
    val sdio_clk = Analog(1.W)
    val sdio_cmd = Analog(1.W)
    val sdio_dat_0 = Analog(1.W)
    val sdio_dat_1 = Analog(1.W)
    val sdio_dat_2 = Analog(1.W)
    val sdio_dat_3 = Analog(1.W)
  })
  val uart_txd = IO(Analog(1.W))
  val uart_rxd = IO(Analog(1.W))

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

class FPGAVC707Internal(chip: Option[Any])(implicit val p :Parameters) extends RawModule
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
      val mod = Module(LazyModule(new TLULtoMIG(chiptl.params)).module)

      // DDR port only
      ddr = Some(IO(new VC707MIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport

      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := sys_clk_i.asUInt()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.ui_clk_sync_rst)
      mod.clock := pll.io.clk_out1.get
      mod.reset := reset_to_sys

      // TileLink Interface from platform
      mod.io.tlport <> chiptl

      if (isMBusClk) {
        println("Island connected to clk_out2 (10MHz)")
        mod.clock := pll.io.clk_out2.get
        mod.reset := reset_to_child
      } else {
        mod.clock := pll.io.clk_out1.get
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
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.ui_clk_sync_rst)

      if (isExtSerMemClk) {
        println("Island connected to clk_out2 (10MHz)")
        mod.clock := pll.io.clk_out2.get
        mod.reset := reset_to_child
      } else {
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
    usbClk.foreach(_ := pll.io.clk_out3.get)

    println(s"Connecting ${aclkn} async clocks by default =>")
    (aclocks zip namedclocks).foreach { case (aclk, nam) =>
      println(s"  Detected clock ${nam}")
      aclk := pll.io.clk_out2.get
      println("    Connected to clk_out2 (10 MHz)")
    }
    DefaultRTC

    // Clock controller
    (extser zip extserSourceBits).foreach { case(es, sourceBits) =>
      val mod = Module(LazyModule(new FPGAMiniSystem(sourceBits)).module)

      // Serial port
      mod.serport.flipConnect(es)

      if (isExtSerBusClk) {
        println("Island connected to clk_out2 (10MHz)")
        mod.clock := pll.io.clk_out2.get
        mod.reset := reset_to_child
      }

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

object ConnectFMCXilinxGPIO {
  def npmap(FMC: FMCVC707): Map[(Int, Int), Analog] = {
    val default = Map(
      (0, 0) -> FMC.CLK_M2C_P(0),
      (0, 1) -> FMC.CLK_M2C_N(0),
      (0, 2) -> FMC.LA_P(1),
      (0, 3) -> FMC.LA_N(1),
      (0, 4) -> FMC.LA_P(11),
      (0, 5) -> FMC.LA_N(11),
      (0, 6) -> FMC.LA_P(15),
      (0, 7) -> FMC.LA_N(15),
      (0, 8) -> FMC.LA_P(9),
      (0, 9) -> FMC.LA_N(9),
      (0, 10) -> FMC.LA_P(13),
      (0, 11) -> FMC.LA_N(13),
      (0, 12) -> FMC.LA_P(17),
      (0, 13) -> FMC.LA_N(17),
      (0, 14) -> FMC.LA_P(19),
      (0, 15) -> FMC.LA_N(19),
      (0, 16) -> FMC.LA_P(21),
      (0, 17) -> FMC.LA_N(21),
      (0, 18) -> FMC.LA_P(23),
      (0, 19) -> FMC.LA_N(23),
      (0, 20) -> FMC.LA_P(7),
      (0, 21) -> FMC.LA_N(7),
      (0, 22) -> FMC.LA_P(4),
      (0, 23) -> FMC.LA_N(4),
      (0, 24) -> FMC.LA_P(2),
      (0, 25) -> FMC.LA_N(2),
      (0, 26) -> FMC.LA_P(5),
      (0, 27) -> FMC.LA_N(5),
      (0, 28) -> FMC.LA_P(24),
      (0, 29) -> FMC.LA_N(24),
      (0, 30) -> FMC.LA_P(26),
      (0, 31) -> FMC.LA_N(26),
      (0, 32) -> FMC.LA_P(28),
      (0, 33) -> FMC.LA_N(28),
      (0, 34) -> FMC.LA_P(30),
      (0, 35) -> FMC.LA_N(30),
      (1, 0) -> FMC.LA_P(12),
      (1, 1) -> FMC.LA_N(12),
      (1, 2) -> FMC.LA_P(0),
      (1, 3) -> FMC.LA_N(0),
      (1, 4) -> FMC.LA_P(8),
      (1, 5) -> FMC.LA_N(8),
      (1, 6) -> FMC.LA_P(3),
      (1, 7) -> FMC.LA_N(3),
      (1, 8) -> FMC.HA_P(2),
      (1, 9) -> FMC.HA_N(2),
      (1, 10) -> FMC.LA_P(6),
      (1, 11) -> FMC.LA_N(6),
      (1, 12) -> FMC.LA_P(10),
      (1, 13) -> FMC.LA_N(10),
      (1, 14) -> FMC.LA_P(16),
      (1, 15) -> FMC.LA_N(16),
      (1, 16) -> FMC.LA_P(14),
      (1, 17) -> FMC.LA_N(14),
      (1, 18) -> FMC.LA_P(20),
      (1, 19) -> FMC.LA_N(20),
      (1, 20) -> FMC.LA_P(18),
      (1, 21) -> FMC.LA_N(18),
      (1, 22) -> FMC.LA_P(22),
      (1, 23) -> FMC.LA_N(22),
      (1, 24) -> FMC.LA_P(25),
      (1, 25) -> FMC.LA_N(25),
      (1, 26) -> FMC.LA_P(27),
      (1, 27) -> FMC.LA_N(27),
      (1, 28) -> FMC.LA_P(29),
      (1, 29) -> FMC.LA_N(29),
      (1, 30) -> FMC.LA_P(31),
      (1, 31) -> FMC.LA_N(31),
      (1, 32) -> FMC.LA_P(33),
      (1, 33) -> FMC.LA_N(33),
      (1, 34) -> FMC.LA_P(32),
      (1, 35) -> FMC.LA_N(32),
      (2, 0) -> FMC.HA_P(1),
      (2, 1) -> FMC.HA_N(1),
      (2, 2) -> FMC.HA_P(0),
      (2, 3) -> FMC.HA_N(0),
      (2, 4) -> FMC.HA_P(5),
      (2, 5) -> FMC.HA_N(5),
      (2, 6) -> FMC.HA_P(9),
      (2, 7) -> FMC.HA_N(9),
      (2, 8) -> FMC.HA_P(13),
      (2, 9) -> FMC.HA_N(13),
      (2, 10) -> FMC.HA_P(16),
      (2, 11) -> FMC.HA_N(16),
      (2, 12) -> FMC.HA_P(20),
      (2, 13) -> FMC.HA_N(20),
      (2, 14) -> FMC.HA_P(3),
      (2, 15) -> FMC.HA_N(3),
      (2, 16) -> FMC.HA_P(11),
      (2, 17) -> FMC.HA_N(11),
      (2, 18) -> FMC.HA_P(18),
      (2, 19) -> FMC.HA_N(18),
      (2, 20) -> FMC.HA_P(4),
      (2, 21) -> FMC.HA_N(4),
      (2, 22) -> FMC.HA_P(8),
      (2, 23) -> FMC.HA_N(8),
      (2, 24) -> FMC.HA_P(12),
      (2, 25) -> FMC.HA_N(12),
      (2, 26) -> FMC.HA_P(15),
      (2, 27) -> FMC.HA_N(15),
      (2, 28) -> FMC.HA_P(19),
      (2, 29) -> FMC.HA_N(19),
      // NOTE: These ones really do not exist in the VC707. J2 and J3 are NC
      //  (2, 30) -> FMC.CLK_P(3),
      //  (2, 31) -> FMC.CLK_N(3),
      (2, 32) -> FMC.HA_P(7),
      (2, 33) -> FMC.HA_N(7),
      (2, 34) -> FMC.HA_P(14),
      (2, 35) -> FMC.HA_N(14),
      (3, 14) -> FMC.HA_P(22),
      (3, 15) -> FMC.HA_N(22))
    val ext = if(FMC.ext) Map(
      (3, 0) -> FMC.HB_P.get(21),
      (3, 1) -> FMC.HB_N.get(21),
      (3, 2) -> FMC.HB_P.get(20),
      (3, 3) -> FMC.HB_N.get(20),
      (3, 4) -> FMC.HB_P.get(3),
      (3, 5) -> FMC.HB_N.get(3),
      (3, 6) -> FMC.HB_P.get(5),
      (3, 7) -> FMC.HB_N.get(5),
      (3, 8) -> FMC.HB_P.get(9),
      (3, 9) -> FMC.HB_N.get(9),
      (3, 10) -> FMC.HB_P.get(13),
      (3, 11) -> FMC.HB_N.get(13),
      (3, 12) -> FMC.HB_P.get(19),
      (3, 13) -> FMC.HB_N.get(19),
      (3, 16) -> FMC.HB_P.get(7),
      (3, 17) -> FMC.HB_N.get(7),
      (3, 18) -> FMC.HB_P.get(15),
      (3, 19) -> FMC.HB_N.get(15),
      (3, 20) -> FMC.HB_P.get(2),
      (3, 21) -> FMC.HB_N.get(2),
      (3, 22) -> FMC.HB_P.get(4),
      (3, 23) -> FMC.HB_N.get(4),
      (3, 24) -> FMC.HB_P.get(8),
      (3, 25) -> FMC.HB_N.get(8),
      (3, 26) -> FMC.HB_P.get(12),
      (3, 27) -> FMC.HB_N.get(12),
      (3, 28) -> FMC.HB_P.get(16),
      (3, 29) -> FMC.HB_N.get(16),
      (3, 30) -> FMC.HB_P.get(1),
      (3, 31) -> FMC.HB_N.get(1),
      (3, 32) -> FMC.HB_P.get(11),
      (3, 33) -> FMC.HB_N.get(11),
      (3, 34) -> FMC.HB_P.get(18),
      (3, 35) -> FMC.HB_N.get(18),
    ) else Map()
    default ++ ext
  }
  def apply (n: Int, pu: Int, FMC: FMCVC707): Analog = {
    val p:Int = pu match {
      case it if 1 to 10 contains it => pu - 1
      case it if 13 to 28 contains it => pu - 3
      case it if 31 to 40 contains it => pu - 5
      case _ => throw new RuntimeException(s"J${n}_${pu} is a VDD or a GND")
    }
    npmap(FMC)((n, p))
  }
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
          case _ => if(get) c := IOBUF(npmap(FMC)((n,p))) else IOBUF(npmap(FMC)((n,p)), c)
        }
      case _ => if(get) c := IOBUF(npmap(FMC)((n,p))) else IOBUF(npmap(FMC)((n,p)), c)
    }
  }

  def npmap_debug(FMC: FMCVC707): Map[(Int, Int), Analog] = {
    val default = Map(
      (20, 1) -> FMC.LA_P(20),
      (20, 2) -> FMC.LA_P(24),
      (20, 3) -> FMC.LA_N(20),
      (20, 4) -> FMC.LA_N(24),
      (20, 5) -> FMC.LA_P(21),
      (20, 6) -> FMC.LA_P(25),
      (20, 7) -> FMC.LA_N(21),
      (20, 8) -> FMC.LA_N(25),
      (20, 9) -> FMC.LA_P(22),
      (20, 10) -> FMC.LA_P(26),
      (20, 11) -> FMC.LA_N(22),
      (20, 12) -> FMC.LA_N(26),
      (20, 13) -> FMC.LA_P(23),
      (20, 14) -> FMC.LA_P(27),
      (20, 15) -> FMC.LA_N(23),
      (20, 16) -> FMC.LA_N(27),
      (23, 5) -> FMC.HA_P(20),
      (23, 6) -> FMC.HA_P(22),
      (23, 7) -> FMC.HA_N(20),
      (23, 8) -> FMC.HA_N(22),
      (23, 9) -> FMC.HA_P(21),
      (23, 10) -> FMC.HA_P(23),
      (23, 11) -> FMC.HA_N(21),
      (23, 12) -> FMC.HA_N(23),
      (16, 5) -> FMC.LA_P(28),
      (16, 6) -> FMC.LA_P(30),
      (16, 7) -> FMC.LA_N(28),
      (16, 8) -> FMC.LA_N(30),
      (16, 9) -> FMC.LA_P(29),
      (16, 10) -> FMC.LA_P(31),
      (16, 11) -> FMC.LA_N(29),
      (16, 12) -> FMC.LA_N(31),
      (15, 3) -> FMC.LA_P(32),
      (15, 4) -> FMC.LA_N(32),
      (15, 5) -> FMC.LA_P(33),
      (15, 6) -> FMC.LA_N(33),
      (1, 1) -> FMC.LA_P(0),
      (1, 2) -> FMC.LA_P(10),
      (1, 3) -> FMC.LA_N(0),
      (1, 4) -> FMC.LA_N(10),
      (1, 5) -> FMC.LA_P(1),
      (1, 6) -> FMC.LA_P(11),
      (1, 7) -> FMC.LA_N(1),
      (1, 8) -> FMC.LA_N(11),
      (1, 9) -> FMC.LA_P(2),
      (1, 10) -> FMC.LA_P(12),
      (1, 11) -> FMC.LA_N(2),
      (1, 12) -> FMC.LA_N(12),
      (1, 13) -> FMC.LA_P(3),
      (1, 14) -> FMC.LA_P(13),
      (1, 15) -> FMC.LA_N(3),
      (1, 16) -> FMC.LA_N(13),
      (1, 17) -> FMC.LA_P(4),
      (1, 18) -> FMC.LA_P(14),
      (1, 19) -> FMC.LA_N(4),
      (1, 20) -> FMC.LA_N(14),
      (1, 21) -> FMC.LA_P(5),
      (1, 22) -> FMC.LA_P(15),
      (1, 23) -> FMC.LA_N(5),
      (1, 24) -> FMC.LA_N(15),
      (1, 25) -> FMC.LA_P(6),
      (1, 26) -> FMC.LA_P(16),
      (1, 27) -> FMC.LA_N(6),
      (1, 28) -> FMC.LA_N(16),
      (1, 29) -> FMC.LA_P(7),
      (1, 30) -> FMC.LA_P(17),
      (1, 31) -> FMC.LA_N(7),
      (1, 32) -> FMC.LA_N(17),
      (1, 33) -> FMC.LA_P(8),
      (1, 34) -> FMC.LA_P(18),
      (1, 35) -> FMC.LA_N(8),
      (1, 36) -> FMC.LA_N(18),
      (1, 37) -> FMC.LA_P(9),
      (1, 38) -> FMC.LA_P(19),
      (1, 39) -> FMC.LA_N(9),
      (1, 40) -> FMC.LA_N(19),
      (0, 1) -> FMC.HA_P(0),
      (0, 2) -> FMC.HA_P(10),
      (0, 3) -> FMC.HA_N(0),
      (0, 4) -> FMC.HA_N(10),
      (0, 5) -> FMC.HA_P(1),
      (0, 6) -> FMC.HA_P(11),
      (0, 7) -> FMC.HA_N(1),
      (0, 8) -> FMC.HA_N(11),
      (0, 9) -> FMC.HA_P(2),
      (0, 10) -> FMC.HA_P(12),
      (0, 11) -> FMC.HA_N(2),
      (0, 12) -> FMC.HA_N(12),
      (0, 13) -> FMC.HA_P(3),
      (0, 14) -> FMC.HA_P(13),
      (0, 15) -> FMC.HA_N(3),
      (0, 16) -> FMC.HA_N(13),
      (0, 17) -> FMC.HA_P(4),
      (0, 18) -> FMC.HA_P(14),
      (0, 19) -> FMC.HA_N(4),
      (0, 20) -> FMC.HA_N(14),
      (0, 21) -> FMC.HA_P(5),
      (0, 22) -> FMC.HA_P(15),
      (0, 23) -> FMC.HA_N(5),
      (0, 24) -> FMC.HA_N(15),
      (0, 25) -> FMC.HA_P(6),
      (0, 26) -> FMC.HA_P(16),
      (0, 27) -> FMC.HA_N(6),
      (0, 28) -> FMC.HA_N(16),
      (0, 29) -> FMC.HA_P(7),
      (0, 30) -> FMC.HA_P(17),
      (0, 31) -> FMC.HA_N(7),
      (0, 32) -> FMC.HA_N(17),
      (0, 33) -> FMC.HA_P(8),
      (0, 34) -> FMC.HA_P(18),
      (0, 35) -> FMC.HA_N(8),
      (0, 36) -> FMC.HA_N(18),
      (0, 37) -> FMC.HA_P(9),
      (0, 38) -> FMC.HA_P(19),
      (0, 39) -> FMC.HA_N(9),
      (0, 40) -> FMC.HA_N(19),
    )
    val ext = if(FMC.ext) Map(
      (23, 1) -> FMC.HB_P.get(20),
      (23, 2) -> FMC.HB_P.get(21),
      (23, 3) -> FMC.HB_N.get(20),
      (23, 4) -> FMC.HB_N.get(21),
      (2, 1) -> FMC.HB_P.get(0),
      (2, 2) -> FMC.HB_P.get(10),
      (2, 3) -> FMC.HB_N.get(0),
      (2, 4) -> FMC.HB_N.get(10),
      (2, 5) -> FMC.HB_P.get(1),
      (2, 6) -> FMC.HB_P.get(11),
      (2, 7) -> FMC.HB_N.get(1),
      (2, 8) -> FMC.HB_N.get(11),
      (2, 9) -> FMC.HB_P.get(2),
      (2, 10) -> FMC.HB_P.get(12),
      (2, 11) -> FMC.HB_N.get(2),
      (2, 12) -> FMC.HB_N.get(12),
      (2, 13) -> FMC.HB_P.get(3),
      (2, 14) -> FMC.HB_P.get(13),
      (2, 15) -> FMC.HB_N.get(3),
      (2, 16) -> FMC.HB_N.get(13),
      (2, 17) -> FMC.HB_P.get(4),
      (2, 18) -> FMC.HB_P.get(14),
      (2, 19) -> FMC.HB_N.get(4),
      (2, 20) -> FMC.HB_N.get(14),
      (2, 21) -> FMC.HB_P.get(5),
      (2, 22) -> FMC.HB_P.get(15),
      (2, 23) -> FMC.HB_N.get(5),
      (2, 24) -> FMC.HB_N.get(15),
      (2, 25) -> FMC.HB_P.get(6),
      (2, 26) -> FMC.HB_P.get(16),
      (2, 27) -> FMC.HB_N.get(6),
      (2, 28) -> FMC.HB_N.get(16),
      (2, 29) -> FMC.HB_P.get(7),
      (2, 30) -> FMC.HB_P.get(17),
      (2, 31) -> FMC.HB_N.get(7),
      (2, 32) -> FMC.HB_N.get(17),
      (2, 33) -> FMC.HB_P.get(8),
      (2, 34) -> FMC.HB_P.get(18),
      (2, 35) -> FMC.HB_N.get(8),
      (2, 36) -> FMC.HB_N.get(18),
      (2, 37) -> FMC.HB_P.get(9),
      (2, 38) -> FMC.HB_P.get(19),
      (2, 39) -> FMC.HB_N.get(9),
      (2, 40) -> FMC.HB_N.get(19),
    ) else Map()
    default ++ ext
  }
  def debug(n: Int, p: Int, FMC: FMCVC707): Analog = {
    npmap_debug(FMC)((n, p))
  }
  def debug(n: Int, p: Int, c: Bool, get: Boolean, FMC: FMCVC707) = {
    if(get) c := IOBUF(npmap_debug(FMC)((n, p))) else IOBUF(npmap_debug(FMC)((n, p)), c)
  }
}

class FPGAVC707InternalNoChip()(implicit p :Parameters) extends FPGAVC707Internal(None)(p)
  with FPGAInternalNoChipDef

trait WithFPGAVC707InternCreate {
  this: FPGAVC707Shell =>
  val chip: Any
  val intern = Module(new FPGAVC707Internal(Some(chip)))
}

trait WithFPGAVC707InternNoChipCreate {
  this: FPGAVC707Shell =>
  val intern = Module(new FPGAVC707InternalNoChip())
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
  val chip : Any
  def namedclocks: Seq[String] = chip.asInstanceOf[HasTEEHWClockGroupChipImp].system.namedclocks
  // This trait connects the chip to all essentials. This assumes no DDR is connected yet

  def PCIPORT = FMC1_HPC
  def MISCPORT = FMC1_HPC

  // GPIO
  val gpport = gpio_out ++ gpio_in
  chip.asInstanceOf[HasTEEHWPeripheryGPIOChipImp].gpio.zip(gpport).foreach{case(gp, i) =>
    attach(gp, i)
  }

  // JTAG
  chip.asInstanceOf[DebugJTAGOnlyChipImp].jtag.foreach{ chipjtag =>
    attach(chipjtag.TCK, jtag.jtag_TCK)
    attach(chipjtag.TDI, jtag.jtag_TDI)
    PULLUP(jtag.jtag_TDI)
    attach(jtag.jtag_TDO, chipjtag.TDO)
    attach(chipjtag.TMS, jtag.jtag_TMS)
    PULLUP(jtag.jtag_TMS)
    attach(ConnectFMCXilinxGPIO.debug(1, 13, MISCPORT), chipjtag.TRSTn)
    PULLUP(ConnectFMCXilinxGPIO.debug(1, 13, MISCPORT))
  }

  // QSPI
  (chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].spi zip chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].allspicfg).zipWithIndex.foreach {
    case ((qspiport: SPIPIN, _: SPIParams), i: Int) =>
      if (i == 0) {
        // SD IO
        attach(sdio.sdio_clk, qspiport.SCK)
        attach(sdio.sdio_cmd, qspiport.DQ(0))
        attach(qspiport.DQ(1), sdio.sdio_dat_0)
        attach(sdio.sdio_dat_3, qspiport.CS(0))
        attach(ConnectFMCXilinxGPIO.debug(1, 14, MISCPORT), qspiport.DQ(2))
        attach(ConnectFMCXilinxGPIO.debug(1, 15, MISCPORT), qspiport.DQ(3))
      }
    case ((qspi: SPIPIN, _: SPIFlashParams), _: Int) =>
      attach(ConnectFMCXilinxGPIO.debug(1, 7, MISCPORT), qspi.CS(0))
      attach(ConnectFMCXilinxGPIO.debug(1, 8, MISCPORT), qspi.SCK)
      attach(ConnectFMCXilinxGPIO.debug(1, 9, MISCPORT), qspi.DQ(1))
      attach(ConnectFMCXilinxGPIO.debug(1, 10, MISCPORT), qspi.DQ(0))
      attach(ConnectFMCXilinxGPIO.debug(1, 11, MISCPORT), qspi.DQ(2))
      attach(ConnectFMCXilinxGPIO.debug(1, 12, MISCPORT), qspi.DQ(3))
  }

  // UART
  chip.asInstanceOf[HasTEEHWPeripheryUARTChipImp].uart.foreach { uart =>
    attach(uart.RXD, uart_rxd)
    attach(uart_txd, uart.TXD)
  }

  // Connected to MISCPORT
  chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs.foreach{ case chipport =>
    attach(ConnectFMCXilinxGPIO.debug(1, 1, MISCPORT), chipport.USBWireDataIn(0))
    attach(ConnectFMCXilinxGPIO.debug(1, 2, MISCPORT), chipport.USBWireDataIn(1))
    attach(ConnectFMCXilinxGPIO.debug(1, 3, MISCPORT), chipport.USBWireDataOut(0))
    attach(ConnectFMCXilinxGPIO.debug(1, 4, MISCPORT), chipport.USBWireDataOut(1))
    attach(ConnectFMCXilinxGPIO.debug(1, 5, MISCPORT), chipport.USBWireCtrlOut)
    attach(ConnectFMCXilinxGPIO.debug(1, 6, MISCPORT), chipport.USBFullSpeed)
  }

  // TODO Nullify sdram
}

trait WithFPGAVC707Connect extends WithFPGAVC707PureConnect 
  with WithFPGAVC707InternCreate 
  with WithFPGAVC707InternConnect {
  this: FPGAVC707Shell =>

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // PCIe (if available)
  chip.asInstanceOf[HasTEEHWPeripheryXilinxVC707PCIeX1ChipImp].pcie.foreach{ case chipport =>
    chipport.REFCLK_rxp := PCIPORT.GBTCLK_M2C_P.get(0)
    chipport.REFCLK_rxn := PCIPORT.GBTCLK_M2C_N.get(0)
    PCIPORT.DP_C2M_P.get(5) := chipport.pci_exp_txp
    PCIPORT.DP_C2M_N.get(5) := chipport.pci_exp_txn
    chipport.pci_exp_rxp := PCIPORT.DP_M2C_P.get(5)
    chipport.pci_exp_rxn := PCIPORT.DP_M2C_N.get(5)
    chipport.axi_aresetn := intern.rst_n
    chipport.axi_ctl_aresetn := intern.rst_n
  }
  chip.asInstanceOf[HasTEEHWPeripheryXDMAChipImp].xdma.foreach{ port =>
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
  ConnectFMCXilinxGPIO(JP21, 2, intern.sys_clk.asBool, false, FMC) // Previous ChildClock
  ConnectFMCXilinxGPIO(JP18, 5, !intern.rst_n, false, FMC) // Previous ChildReset
  //ConnectFMCXilinxGPIO(JP21, 4, intern.sys_clk.asBool(), false, FMC)
  ConnectFMCXilinxGPIO(JP18, 2, intern.rst_n, false, FMC)
  ConnectFMCXilinxGPIO(JP18, 6, intern.rst_n, false, FMC) // Previous jrst_n
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
  ConnectFMCXilinxGPIO(0, 1, intern.sys_clk.asBool, false, FMCDAO)
  PUT(intern.sys_clk.asBool, FMCDAO.CLK_M2C_N(1)) // Previous ChildClock
  intern.usbClk.foreach{ a => PUT(a.asBool, FMCDAO.CLK_M2C_N(1))}
  ConnectFMCXilinxGPIO(0, 27, intern.rst_n, false, FMC) // Previous jrst_n
  ConnectFMCXilinxGPIO(1, 15, intern.rst_n, false, FMCDAO)
  // Only some of the aclocks are actually connected.
  println("Connecting orphan clocks =>")
  (intern.aclocks zip intern.namedclocks).foreach{ case (aclk, nam) =>
    println(s"  Detected clock ${nam}")
    if(nam.contains("cryptobus")) {
      println("    Connected to CLK_M2C_P(1) or SMA_CLK_P")
      PUT(aclk.asBool, FMCDAO.CLK_M2C_P(1))
    }
    if(nam.contains("tile_0")) {
      println("    Connected to USER_SMA_CLOCK_P")
      PUT(aclk.asBool, USER_SMA_CLOCK_P)
    }
    if(nam.contains("tile_1")) {
      println("    Connected to USER_SMA_CLOCK_N")
      PUT(aclk.asBool, USER_SMA_CLOCK_N)
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
  gpio_out.foreach(_ := intern.init_calib_complete)
  
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
  //ConnectFMCXilinxGPIO(JP18, 2, chip.rst_n,  true, FMC) // TODO: This is not connected in DUY form
  //ConnectFMCXilinxGPIO(JP18, 6, chip.rst_n,  true, FMC) // TODO: This is not connected in DUY form
  // Memory port
  chip.asInstanceOf[HasTEEHWPeripheryExtMemChipImp].mem_tl.foreach{ case tlport =>
    attach(ConnectFMCXilinxGPIO(JP18, 1, FMC), tlport.a.valid)
    attach(ConnectFMCXilinxGPIO(JP18, 4, FMC), tlport.a.ready)
    require(tlport.a.opcode.size == 3, s"${tlport.a.opcode.size}")
    attach(ConnectFMCXilinxGPIO(JP18, 3, FMC), tlport.a.opcode(2))
    attach(ConnectFMCXilinxGPIO(JP18, 7, FMC), tlport.a.opcode(1))
    attach(ConnectFMCXilinxGPIO(JP18, 8, FMC), tlport.a.opcode(0))
    require(tlport.a.param.size == 3, s"${tlport.a.param.size}")
    attach(ConnectFMCXilinxGPIO(JP18, 9, FMC), tlport.a.param(2))
    attach(ConnectFMCXilinxGPIO(JP18, 10, FMC), tlport.a.param(1))
    attach(ConnectFMCXilinxGPIO(JP18, 13, FMC), tlport.a.param(0))
    require(tlport.a.size.size == 3, s"${tlport.a.size.size}")
    attach(ConnectFMCXilinxGPIO(JP18, 14, FMC), tlport.a.size(2))
    attach(ConnectFMCXilinxGPIO(JP18, 15, FMC), tlport.a.size(1))
    attach(ConnectFMCXilinxGPIO(JP18, 16, FMC), tlport.a.size(0))
    require(tlport.a.source.size == 6, s"${tlport.a.source.size}")
    attach(ConnectFMCXilinxGPIO(JP18, 17, FMC), tlport.a.source(5))
    attach(ConnectFMCXilinxGPIO(JP18, 18, FMC), tlport.a.source(4))
    attach(ConnectFMCXilinxGPIO(JP18, 19, FMC), tlport.a.source(3))
    attach(ConnectFMCXilinxGPIO(JP18, 20, FMC), tlport.a.source(2))
    attach(ConnectFMCXilinxGPIO(JP18, 21, FMC), tlport.a.source(1))
    attach(ConnectFMCXilinxGPIO(JP18, 22, FMC), tlport.a.source(0))
    require(tlport.a.address.size == 32, s"${tlport.a.address.size}")
    attach(ConnectFMCXilinxGPIO(JP18, 23, FMC), tlport.a.address(31))
    attach(ConnectFMCXilinxGPIO(JP18, 24, FMC), tlport.a.address(30))
    attach(ConnectFMCXilinxGPIO(JP18, 25, FMC), tlport.a.address(29))
    attach(ConnectFMCXilinxGPIO(JP18, 26, FMC), tlport.a.address(28))
    attach(ConnectFMCXilinxGPIO(JP18, 27, FMC), tlport.a.address(27))
    attach(ConnectFMCXilinxGPIO(JP18, 28, FMC), tlport.a.address(26))
    attach(ConnectFMCXilinxGPIO(JP18, 31, FMC), tlport.a.address(25))
    attach(ConnectFMCXilinxGPIO(JP18, 32, FMC), tlport.a.address(24))
    attach(ConnectFMCXilinxGPIO(JP18, 33, FMC), tlport.a.address(23))
    attach(ConnectFMCXilinxGPIO(JP18, 34, FMC), tlport.a.address(22))
    attach(ConnectFMCXilinxGPIO(JP18, 35, FMC), tlport.a.address(21))
    attach(ConnectFMCXilinxGPIO(JP18, 36, FMC), tlport.a.address(20))
    attach(ConnectFMCXilinxGPIO(JP18, 37, FMC), tlport.a.address(19))
    attach(ConnectFMCXilinxGPIO(JP18, 38, FMC), tlport.a.address(18))
    attach(ConnectFMCXilinxGPIO(JP18, 39, FMC), tlport.a.address(17))
    attach(ConnectFMCXilinxGPIO(JP18, 40, FMC), tlport.a.address(16))
    attach(ConnectFMCXilinxGPIO(JP19, 1, FMC), tlport.a.address(15))
    attach(ConnectFMCXilinxGPIO(JP19, 2, FMC), tlport.a.address(14))
    attach(ConnectFMCXilinxGPIO(JP19, 2, FMC), tlport.a.address(13))
    attach(ConnectFMCXilinxGPIO(JP19, 4, FMC), tlport.a.address(12))
    attach(ConnectFMCXilinxGPIO(JP19, 5, FMC), tlport.a.address(11))
    attach(ConnectFMCXilinxGPIO(JP19, 6, FMC), tlport.a.address(10))
    attach(ConnectFMCXilinxGPIO(JP19, 7, FMC), tlport.a.address( 9))
    attach(ConnectFMCXilinxGPIO(JP19, 8, FMC), tlport.a.address( 8))
    attach(ConnectFMCXilinxGPIO(JP19, 9, FMC), tlport.a.address( 7))
    attach(ConnectFMCXilinxGPIO(JP19, 10, FMC), tlport.a.address( 6))
    attach(ConnectFMCXilinxGPIO(JP19, 13, FMC), tlport.a.address( 5))
    attach(ConnectFMCXilinxGPIO(JP19, 14, FMC), tlport.a.address( 4))
    attach(ConnectFMCXilinxGPIO(JP19, 15, FMC), tlport.a.address( 3))
    attach(ConnectFMCXilinxGPIO(JP19, 16, FMC), tlport.a.address( 2))
    attach(ConnectFMCXilinxGPIO(JP19, 17, FMC), tlport.a.address( 1))
    attach(ConnectFMCXilinxGPIO(JP19, 18, FMC), tlport.a.address( 0))
    require(tlport.a.mask.size == 4, s"${tlport.a.mask.size}")
    attach(ConnectFMCXilinxGPIO(JP19, 19, FMC), tlport.a.mask(3))
    attach(ConnectFMCXilinxGPIO(JP19, 20, FMC), tlport.a.mask(2))
    attach(ConnectFMCXilinxGPIO(JP19, 21, FMC), tlport.a.mask(1))
    attach(ConnectFMCXilinxGPIO(JP19, 22, FMC), tlport.a.mask(0))
    require(tlport.a.data.size == 32, s"${tlport.a.data.size}")
    attach(ConnectFMCXilinxGPIO(JP19, 23, FMC), tlport.a.data(31))
    attach(ConnectFMCXilinxGPIO(JP19, 24, FMC), tlport.a.data(30))
    attach(ConnectFMCXilinxGPIO(JP19, 25, FMC), tlport.a.data(29))
    attach(ConnectFMCXilinxGPIO(JP19, 26, FMC), tlport.a.data(28))
    attach(ConnectFMCXilinxGPIO(JP19, 27, FMC), tlport.a.data(27))
    attach(ConnectFMCXilinxGPIO(JP19, 28, FMC), tlport.a.data(26))
    attach(ConnectFMCXilinxGPIO(JP19, 31, FMC), tlport.a.data(25))
    attach(ConnectFMCXilinxGPIO(JP19, 32, FMC), tlport.a.data(24))
    attach(ConnectFMCXilinxGPIO(JP19, 33, FMC), tlport.a.data(23))
    attach(ConnectFMCXilinxGPIO(JP19, 34, FMC), tlport.a.data(22))
    attach(ConnectFMCXilinxGPIO(JP19, 35, FMC), tlport.a.data(21))
    attach(ConnectFMCXilinxGPIO(JP19, 36, FMC), tlport.a.data(20))
    attach(ConnectFMCXilinxGPIO(JP19, 37, FMC), tlport.a.data(19))
    attach(ConnectFMCXilinxGPIO(JP19, 38, FMC), tlport.a.data(18))
    attach(ConnectFMCXilinxGPIO(JP19, 39, FMC), tlport.a.data(17))
    attach(ConnectFMCXilinxGPIO(JP19, 40, FMC), tlport.a.data(16))
    attach(ConnectFMCXilinxGPIO(JP20, 10, FMC), tlport.a.data(15))
    attach(ConnectFMCXilinxGPIO(JP20, 9, FMC), tlport.a.data(14))
    attach(ConnectFMCXilinxGPIO(JP20, 8, FMC), tlport.a.data(13))
    attach(ConnectFMCXilinxGPIO(JP20, 7, FMC), tlport.a.data(12))
    attach(ConnectFMCXilinxGPIO(JP20, 6, FMC), tlport.a.data(11))
    attach(ConnectFMCXilinxGPIO(JP20, 5, FMC), tlport.a.data(10))
    attach(ConnectFMCXilinxGPIO(JP20, 4, FMC), tlport.a.data( 9))
    attach(ConnectFMCXilinxGPIO(JP20, 3, FMC), tlport.a.data( 8))
    attach(ConnectFMCXilinxGPIO(JP20, 2, FMC), tlport.a.data( 7))
    attach(ConnectFMCXilinxGPIO(JP20, 1, FMC), tlport.a.data( 6))
    attach(ConnectFMCXilinxGPIO(JP20, 13, FMC), tlport.a.data( 5))
    attach(ConnectFMCXilinxGPIO(JP20, 14, FMC), tlport.a.data( 4))
    attach(ConnectFMCXilinxGPIO(JP20, 15, FMC), tlport.a.data( 3))
    attach(ConnectFMCXilinxGPIO(JP20, 16, FMC), tlport.a.data( 2))
    attach(ConnectFMCXilinxGPIO(JP20, 17, FMC), tlport.a.data( 1))
    attach(ConnectFMCXilinxGPIO(JP20, 18, FMC), tlport.a.data( 0))
    attach(ConnectFMCXilinxGPIO(JP20, 19, FMC), tlport.a.corrupt)
    attach(ConnectFMCXilinxGPIO(JP20, 20, FMC), tlport.d.ready)
    attach(ConnectFMCXilinxGPIO(JP20, 21, FMC), tlport.d.valid)
    require(tlport.d.opcode.size == 3, s"${tlport.d.opcode.size}")
    attach(ConnectFMCXilinxGPIO(JP20, 22, FMC), tlport.d.opcode(2))
    attach(ConnectFMCXilinxGPIO(JP20, 23, FMC), tlport.d.opcode(1))
    attach(ConnectFMCXilinxGPIO(JP20, 24, FMC), tlport.d.opcode(0))
    require(tlport.d.param.size == 2, s"${tlport.d.param.size}")
    attach(ConnectFMCXilinxGPIO(JP20, 25, FMC), tlport.d.param(1))
    attach(ConnectFMCXilinxGPIO(JP20, 26, FMC), tlport.d.param(0))
    require(tlport.d.size.size == 3, s"${tlport.d.size.size}")
    attach(ConnectFMCXilinxGPIO(JP20, 27, FMC), tlport.d.size(2))
    attach(ConnectFMCXilinxGPIO(JP20, 28, FMC), tlport.d.size(1))
    attach(ConnectFMCXilinxGPIO(JP20, 31, FMC), tlport.d.size(0))
    require(tlport.d.source.size == 6, s"${tlport.d.source.size}")
    attach(ConnectFMCXilinxGPIO(JP20, 32, FMC), tlport.d.source(5))
    attach(ConnectFMCXilinxGPIO(JP20, 33, FMC), tlport.d.source(4))
    attach(ConnectFMCXilinxGPIO(JP20, 34, FMC), tlport.d.source(3))
    attach(ConnectFMCXilinxGPIO(JP20, 35, FMC), tlport.d.source(2))
    attach(ConnectFMCXilinxGPIO(JP20, 36, FMC), tlport.d.source(1))
    attach(ConnectFMCXilinxGPIO(JP20, 37, FMC), tlport.d.source(0))
    require(tlport.d.sink.size == 1, s"${tlport.d.sink.size}")
    attach(ConnectFMCXilinxGPIO(JP20, 38, FMC), tlport.d.sink(0))
    attach(ConnectFMCXilinxGPIO(JP20, 39, FMC), tlport.d.denied)
    attach(ConnectFMCXilinxGPIO(JP20, 40, FMC), tlport.d.corrupt)
    require(tlport.d.data.size == 32, s"${tlport.d.data.size}")
    attach(ConnectFMCXilinxGPIO(JP21, 5, FMC), tlport.d.data(31))
    attach(ConnectFMCXilinxGPIO(JP21, 6, FMC), tlport.d.data(30))
    attach(ConnectFMCXilinxGPIO(JP21, 7, FMC), tlport.d.data(29))
    attach(ConnectFMCXilinxGPIO(JP21, 8, FMC), tlport.d.data(28))
    attach(ConnectFMCXilinxGPIO(JP21, 9, FMC), tlport.d.data(27))
    attach(ConnectFMCXilinxGPIO(JP21, 10, FMC), tlport.d.data(26))
    attach(ConnectFMCXilinxGPIO(JP21, 13, FMC), tlport.d.data(25))
    attach(ConnectFMCXilinxGPIO(JP21, 14, FMC), tlport.d.data(24))
    attach(ConnectFMCXilinxGPIO(JP21, 15, FMC), tlport.d.data(23))
    attach(ConnectFMCXilinxGPIO(JP21, 16, FMC), tlport.d.data(22))
    attach(ConnectFMCXilinxGPIO(JP21, 17, FMC), tlport.d.data(21))
    attach(ConnectFMCXilinxGPIO(JP21, 18, FMC), tlport.d.data(20))
    attach(ConnectFMCXilinxGPIO(JP21, 19, FMC), tlport.d.data(19))
    attach(ConnectFMCXilinxGPIO(JP21, 20, FMC), tlport.d.data(18))
    attach(ConnectFMCXilinxGPIO(JP21, 21, FMC), tlport.d.data(17))
    attach(ConnectFMCXilinxGPIO(JP21, 22, FMC), tlport.d.data(16))
    attach(ConnectFMCXilinxGPIO(JP21, 23, FMC), tlport.d.data(15))
    attach(ConnectFMCXilinxGPIO(JP21, 24, FMC), tlport.d.data(14))
    attach(ConnectFMCXilinxGPIO(JP21, 25, FMC), tlport.d.data(13))
    attach(ConnectFMCXilinxGPIO(JP21, 26, FMC), tlport.d.data(12))
    attach(ConnectFMCXilinxGPIO(JP21, 27, FMC), tlport.d.data(11))
    attach(ConnectFMCXilinxGPIO(JP21, 28, FMC), tlport.d.data(10))
    attach(ConnectFMCXilinxGPIO(JP21, 31, FMC), tlport.d.data( 9))
    attach(ConnectFMCXilinxGPIO(JP21, 32, FMC), tlport.d.data( 8))
    attach(ConnectFMCXilinxGPIO(JP21, 33, FMC), tlport.d.data( 7))
    attach(ConnectFMCXilinxGPIO(JP21, 34, FMC), tlport.d.data( 6))
    attach(ConnectFMCXilinxGPIO(JP21, 35, FMC), tlport.d.data( 5))
    attach(ConnectFMCXilinxGPIO(JP21, 36, FMC), tlport.d.data( 4))
    attach(ConnectFMCXilinxGPIO(JP21, 37, FMC), tlport.d.data( 3))
    attach(ConnectFMCXilinxGPIO(JP21, 38, FMC), tlport.d.data( 2))
    attach(ConnectFMCXilinxGPIO(JP21, 39, FMC), tlport.d.data( 1))
    attach(ConnectFMCXilinxGPIO(JP21, 40, FMC), tlport.d.data( 0))
  }

  // ******* Serial section ******
  def FMCSER = FMC2_HPC
  attach(ConnectFMCXilinxGPIO(0, 1, FMCSER), chip.asInstanceOf[HasTEEHWClockGroupChipImp].clockxi)
  chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs.foreach{ a => attach(a.usbClk, FMCSER.CLK_M2C_N(1)) }
  attach(ConnectFMCXilinxGPIO(1, 15, FMCSER), chip.asInstanceOf[HasTEEHWClockGroupChipImp].rstn)
  // Only some of the aclocks are actually connected.
  println("Connecting orphan clocks =>")
  (chip.asInstanceOf[HasTEEHWClockGroupChipImp].aclockxi zip namedclocks).foreach{ case (aclk, nam) =>
    println(s"  Detected clock ${nam}")
    if(nam.contains("cryptobus")) {
      println("    Connected to CLK_M2C_P(1) or SMA_CLK_P")
      attach(aclk, FMCSER.CLK_M2C_P(1))
    }
    else if(nam.contains("tile_0")) {
      println("    Connected to USER_SMA_CLOCK_P")
      attach(aclk, USER_SMA_CLOCK_P)
    }
    else if(nam.contains("tile_1")) {
      println("    Connected to USER_SMA_CLOCK_N")
      attach(aclk, USER_SMA_CLOCK_N)
    }
    else if(nam.contains("mbus")) {
      println("    Connected to CLK_M2C_P(1) or SMA_CLK_P")
      attach(aclk, FMCSER.CLK_M2C_P(1))
    }
    else
      println("    WARNING: This clock is not handled")
  }
  // ExtSerMem
  chip.asInstanceOf[HasTEEHWPeripheryExtSerMemChipImp].memser.foreach { memser =>
    attach(ConnectFMCXilinxGPIO(0, 31, FMCSER), memser.in.bits(7))
    attach(ConnectFMCXilinxGPIO(0, 33, FMCSER), memser.in.bits(6))
    attach(ConnectFMCXilinxGPIO(0, 35, FMCSER), memser.in.bits(5))
    attach(ConnectFMCXilinxGPIO(0, 37, FMCSER), memser.in.bits(4))
    attach(ConnectFMCXilinxGPIO(0, 39, FMCSER), memser.in.bits(3))
    attach(ConnectFMCXilinxGPIO(1, 1, FMCSER), memser.in.bits(2))
    attach(ConnectFMCXilinxGPIO(1, 5, FMCSER), memser.in.bits(1))
    attach(ConnectFMCXilinxGPIO(1, 7, FMCSER), memser.in.bits(0))
    attach(ConnectFMCXilinxGPIO(1, 9, FMCSER), memser.in.valid)
    attach(ConnectFMCXilinxGPIO(1, 13, FMCSER), memser.out.ready)
    attach(ConnectFMCXilinxGPIO(2, 5, FMCSER), memser.in.ready)
    attach(ConnectFMCXilinxGPIO(2, 7, FMCSER), memser.out.bits(7))
    attach(ConnectFMCXilinxGPIO(2, 9, FMCSER), memser.out.bits(6))
    attach(ConnectFMCXilinxGPIO(2, 13, FMCSER), memser.out.bits(5))
    attach(ConnectFMCXilinxGPIO(2, 15, FMCSER), memser.out.bits(4))
    attach(ConnectFMCXilinxGPIO(2, 17, FMCSER), memser.out.bits(3))
    attach(ConnectFMCXilinxGPIO(2, 19, FMCSER), memser.out.bits(2))
    attach(ConnectFMCXilinxGPIO(2, 21, FMCSER), memser.out.bits(1))
    attach(ConnectFMCXilinxGPIO(2, 23, FMCSER), memser.out.bits(0))
    attach(ConnectFMCXilinxGPIO(2, 25, FMCSER), memser.out.valid)
  }
  // ExtSerBus
  chip.asInstanceOf[HasTEEHWPeripheryExtSerBusChipImp].extser.foreach{ extser =>
    attach(ConnectFMCXilinxGPIO(0, 5, FMCSER), extser.in.bits(7))
    attach(ConnectFMCXilinxGPIO(0, 7, FMCSER), extser.in.bits(6))
    attach(ConnectFMCXilinxGPIO(0, 9, FMCSER), extser.in.bits(5))
    attach(ConnectFMCXilinxGPIO(0, 13, FMCSER), extser.in.bits(4))
    attach(ConnectFMCXilinxGPIO(0, 15, FMCSER), extser.in.bits(3))
    attach(ConnectFMCXilinxGPIO(0, 17, FMCSER), extser.in.bits(2))
    attach(ConnectFMCXilinxGPIO(0, 19, FMCSER), extser.in.bits(1))
    attach(ConnectFMCXilinxGPIO(0, 21, FMCSER), extser.in.bits(0))
    attach(ConnectFMCXilinxGPIO(0, 23, FMCSER), extser.in.valid)
    attach(ConnectFMCXilinxGPIO(0, 25, FMCSER), extser.out.ready)
    attach(ConnectFMCXilinxGPIO(1, 17, FMCSER), extser.in.ready)
    attach(ConnectFMCXilinxGPIO(1, 19, FMCSER), extser.out.bits(7))
    attach(ConnectFMCXilinxGPIO(1, 21, FMCSER), extser.out.bits(6))
    attach(ConnectFMCXilinxGPIO(1, 25, FMCSER), extser.out.bits(5))
    attach(ConnectFMCXilinxGPIO(1, 27, FMCSER), extser.out.bits(4))
    attach(ConnectFMCXilinxGPIO(1, 31, FMCSER), extser.out.bits(3))
    attach(ConnectFMCXilinxGPIO(1, 33, FMCSER), extser.out.bits(2))
    attach(ConnectFMCXilinxGPIO(1, 35, FMCSER), extser.out.bits(1))
    attach(ConnectFMCXilinxGPIO(1, 37, FMCSER), extser.out.bits(0))
    attach(ConnectFMCXilinxGPIO(1, 39, FMCSER), extser.out.valid)
  }

  // ******** Misc part ********
  ElaborationArtefacts.add("false.xci", "")
}