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
import sifive.blocks.devices.spi.{SPIFlashParams, SPIParams}
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
import uec.teehardware.devices.sifiveblocks._
import uec.teehardware.devices.tlmemext._

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
  val K_DIPSW = IO(Vec(8, Analog(1.W)))

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

class FPGASakuraXInternal(chip: Option[Any])(implicit val p :Parameters) extends RawModule
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
      mod.io.tlport <> chiptl

      // Legacy ChildClock
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
    DefaultRTC

    println(s"Connecting ${aclkn} async clocks by default =>")
    (aclocks zip namedclocks).foreach { case (aclk, nam) =>
      println(s"  Detected clock ${nam}")
      aclk := pll.io.clk_out2.get
      println("    Connected to clk_out2 (10 MHz)")
    }

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

object ConnectFMCLPCXilinxGPIO {
  def npmap(FMC: FMCSakuraX): Map[(Int, Int), Analog]  = Map(
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
    //  (1, 8) -> FMC.HA_P(2),
    //  (1, 9) -> FMC.HA_N(2),
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
  )
  def apply (n: Int, pu: Int, FMC: FMCSakuraX): Analog = {
    val p:Int = pu match {
      case it if 1 to 10 contains it => pu - 1
      case it if 13 to 28 contains it => pu - 3
      case it if 31 to 40 contains it => pu - 5
      case _ => throw new RuntimeException(s"J${n}_${pu} is a VDD or a GND")
    }
    npmap(FMC)((n, p))
  }
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
          case _ => if(get) c := IOBUF(npmap(FMC)((n,p))) else IOBUF(npmap(FMC)((n,p)), c)
        }
      case _ => if(get) c := IOBUF(npmap(FMC)((n,p))) else IOBUF(npmap(FMC)((n,p)), c)
    }
  }
  def npmap_debug(FMC: FMCSakuraX): Map[(Int, Int), Analog]  = Map(
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
  )
  def debug(n: Int, p: Int, FMC: FMCSakuraX) = {
    npmap_debug(FMC)((n,p))
  }
  def debug(n: Int, p: Int, c: Bool, get: Boolean, FMC: FMCSakuraX) = {
    if(get) c := IOBUF(npmap_debug(FMC)((n,p))) else IOBUF(npmap_debug(FMC)((n,p)), c)
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
  override def aclkn: Int = if(p(ExposeClocks)) 3 else 0
  override def memserSourceBits: Option[Int] = p(ExtSerMem).map( A => idBits )
  override def extserSourceBits: Option[Int] = p(ExtSerBus).map( A => idBits )
  override def namedclocks: Seq[String] = if(p(ExposeClocks)) Seq("cryptobus", "tile_0", "tile_1") else Seq()
}

trait WithFPGASakuraXInternCreate {
  this: FPGASakuraXShell =>
  val chip : Any
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
  val chip: Any
  def namedclocks: Seq[String] = chip.asInstanceOf[HasTEEHWClockGroupChipImp].system.namedclocks
  // This trait connects the chip to all essentials. This assumes no DDR is connected yet

  // GPIO
  val gpport = K_LED ++ K_DIPSW
  chip.asInstanceOf[HasTEEHWPeripheryGPIOChipImp].gpio.zip(gpport).foreach{case(gp, i) =>
    attach(gp, i)
  }

  // JTAG
  chip.asInstanceOf[DebugJTAGOnlyChipImp].jtag.foreach{ jtag =>
    attach(jtag.TCK, K_HEADER(5))
    attach(jtag.TDI, K_HEADER(4))
    PULLUP(K_HEADER(4))
    attach(K_HEADER(3), jtag.TDO)
    attach(jtag.TMS, K_HEADER(2))
    PULLUP(K_HEADER(2))
    attach(ConnectFMCLPCXilinxGPIO.debug(1, 13, K_FMC), jtag.TRSTn)
    PULLUP(ConnectFMCLPCXilinxGPIO.debug(1, 13, K_FMC))
  }

  // QSPI
  (chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].spi zip chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].allspicfg).zipWithIndex.foreach {
    case ((qspiport: SPIPIN, _: SPIParams), i: Int) =>
      if (i == 0) {
        // SD IO
        attach(K_HEADER(6), qspiport.SCK)
        attach(K_HEADER(7), qspiport.DQ(0))
        attach(qspiport.DQ(1), K_HEADER(8))
        attach(K_HEADER(9), qspiport.CS(0))
      }
    case ((qspi: SPIPIN, _: SPIFlashParams), _: Int) =>
      attach(ConnectFMCLPCXilinxGPIO.debug(1, 7, K_FMC), qspi.CS(0))
      attach(ConnectFMCLPCXilinxGPIO.debug(1, 8, K_FMC), qspi.SCK)
      attach(ConnectFMCLPCXilinxGPIO.debug(1, 9, K_FMC), qspi.DQ(1))
      attach(ConnectFMCLPCXilinxGPIO.debug(1, 10, K_FMC), qspi.DQ(0))
      attach(ConnectFMCLPCXilinxGPIO.debug(1, 11, K_FMC), qspi.DQ(2))
      attach(ConnectFMCLPCXilinxGPIO.debug(1, 12, K_FMC), qspi.DQ(3))
  }

  // UART
  chip.asInstanceOf[HasTEEHWPeripheryUARTChipImp].uart.foreach { uart =>
    attach(uart.RXD, K_HEADER(1))
    attach(K_HEADER(0), uart.TXD)
  }

  // Connected to K_FMC
  chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs.foreach{ case chipport =>
    attach(ConnectFMCLPCXilinxGPIO.debug(1, 1, K_FMC), chipport.USBWireDataIn(0))
    attach(ConnectFMCLPCXilinxGPIO.debug(1, 2, K_FMC), chipport.USBWireDataIn(1))
    attach(ConnectFMCLPCXilinxGPIO.debug(1, 3, K_FMC), chipport.USBWireDataOut(0))
    attach(ConnectFMCLPCXilinxGPIO.debug(1, 4, K_FMC), chipport.USBWireDataOut(1))
    attach(ConnectFMCLPCXilinxGPIO.debug(1, 5, K_FMC), chipport.USBWireCtrlOut)
    attach(ConnectFMCLPCXilinxGPIO.debug(1, 6, K_FMC), chipport.USBFullSpeed)
  }

  // TODO Nullify sdram
}

trait WithFPGASakuraXConnect extends WithFPGASakuraXPureConnect
  with WithFPGASakuraXInternCreate
  with WithFPGASakuraXInternConnect {
  this: FPGASakuraXShell =>

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // Platform connections (override)
  (chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs zip intern.usbClk).foreach { case (chipport, uclk) =>
    PUT(uclk.asBool, chipport.usbClk)
  }
}
