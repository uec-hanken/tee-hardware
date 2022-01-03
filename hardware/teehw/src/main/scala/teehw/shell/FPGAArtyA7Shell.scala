package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import sifive.fpgashells.clocks._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.ip.xilinx.arty100tmig._
import uec.teehardware.macros._
import uec.teehardware._
import uec.teehardware.devices.clockctrl.ClockCtrlPortIO

trait FPGAArtyA7ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters
  //-----------------------------------------------------------------------
  // Interface
  //-----------------------------------------------------------------------

  // Green LEDs
  val led_0        = IO(Analog(1.W))
  val led_1        = IO(Analog(1.W))
  val led_2        = IO(Analog(1.W))
  val led_3        = IO(Analog(1.W))

  // RGB LEDs, 3 pins each
  val led0_r       = IO(Analog(1.W))
  val led0_g       = IO(Analog(1.W))
  val led0_b       = IO(Analog(1.W))

  val led1_r       = IO(Analog(1.W))
  val led1_g       = IO(Analog(1.W))
  val led1_b       = IO(Analog(1.W))

  val led2_r       = IO(Analog(1.W))
  val led2_g       = IO(Analog(1.W))
  val led2_b       = IO(Analog(1.W))

  // Sliding switches
  val sw_0         = IO(Analog(1.W))
  val sw_1         = IO(Analog(1.W))
  val sw_2         = IO(Analog(1.W))
  val sw_3         = IO(Analog(1.W))

  // Buttons. First 3 used as GPIO, the last is used as wakeup
  val btn_0        = IO(Analog(1.W))
  val btn_1        = IO(Analog(1.W))
  val btn_2        = IO(Analog(1.W))
  val btn_3        = IO(Analog(1.W))

  // Dedicated QSPI interface
  val qspi_cs      = IO(Analog(1.W))
  val qspi_sck     = IO(Analog(1.W))
  val qspi_dq      = IO(Vec(4, Analog(1.W)))

  // UART0
  val uart_rxd_out = IO(Analog(1.W))
  val uart_txd_in  = IO(Analog(1.W))

  // JA (Used for more generic GPIOs)
  val ja_0         = IO(Analog(1.W))
  val ja_1         = IO(Analog(1.W))
  val ja_2         = IO(Analog(1.W))
  val ja_3         = IO(Analog(1.W))
  val ja_4         = IO(Analog(1.W))
  val ja_5         = IO(Analog(1.W))
  val ja_6         = IO(Analog(1.W))
  val ja_7         = IO(Analog(1.W))

  // JC (used for additional debug/trace connection)
  val jc           = IO(Vec(8, Analog(1.W)))

  // JD (used for JTAG connection)
  val jd_0         = IO(Analog(1.W))  // TDO
  val jd_1         = IO(Analog(1.W))  // TRST_n
  val jd_2         = IO(Analog(1.W))  // TCK
  val jd_4         = IO(Analog(1.W))  // TDI
  val jd_5         = IO(Analog(1.W))  // TMS
  val jd_6         = IO(Analog(1.W))  // SRST_n

  // ChipKit Digital I/O Pins
  val ck_io        = IO(Vec(20, Analog(1.W)))

  // ChipKit SPI
  val ck_miso      = IO(Analog(1.W))
  val ck_mosi      = IO(Analog(1.W))
  val ck_ss        = IO(Analog(1.W))
  val ck_sck       = IO(Analog(1.W))
}

trait FPGAArtyA7ClockAndResetsAndDDR {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters

  // Clock & Reset
  val CLK100MHZ    = IO(Input(Clock()))
  val ck_rst       = IO(Input(Bool()))
  // DDR
  var ddr: Option[Arty100TMIGIODDR] = None
}

class FPGAArtyA7Shell(implicit val p :Parameters) extends RawModule
  with FPGAArtyA7ChipShell
  with FPGAArtyA7ClockAndResetsAndDDR {
}

class FPGAArtyA7Internal(chip: Option[WithTEEHWbaseShell with WithTEEHWbaseConnect])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGAArtyA7ClockAndResetsAndDDR {
  def outer = chip

  val init_calib_complete = IO(Output(Bool()))
  init_calib_complete := false.B
  var depth = BigInt(0)

  // Some connections for having the clocks
  val clock = Wire(Clock())
  val reset = Wire(Bool())

  //-----------------------------------------------------------------------
  // Clock Generator
  //-----------------------------------------------------------------------
  // Mixed-mode clock generator
  //val clock_8MHz     = Wire(Clock())
  //val clock_32MHz    = Wire(Clock())
  //val clock_65MHz    = Wire(Clock())
  //val mmcm_locked    = Wire(Bool())
  if(false) { // NOTE: Changed by using just a Series7 DDR
    val ip_mmcm = Module(new mmcm())

    ip_mmcm.io.clk_in1 := CLK100MHZ
    //clock_8MHz         := ip_mmcm.io.clk_out1  // 8.388 MHz = 32.768 kHz * 256
    //clock_65MHz        := ip_mmcm.io.clk_out2  // 65 Mhz
    //clock_32MHz        := ip_mmcm.io.clk_out3  // 65/2 Mhz
    ip_mmcm.io.resetn  := ck_rst
    //mmcm_locked        := ip_mmcm.io.locked
  }

  val isOtherClk = p(DDRPortOther) || (p(SbusToMbusXTypeKey) match {
    case _: AsynchronousCrossing => true
    case _ => false
  })

  withClockAndReset(clock, reset) {
    // PLL instance
    val c = new PLLParameters(
      name = "pll",
      input = PLLInClockParameters(freqMHz = 100.0, feedback = true),
      req = Seq(
        PLLOutClockParameters(freqMHz = p(FreqKeyMHz)),
        PLLOutClockParameters(freqMHz = 166.666), // For sys_clk_i
        PLLOutClockParameters(freqMHz = 200.0) // For ref_clk
      ) ++ (if (isOtherClk) Seq(PLLOutClockParameters(freqMHz = 10.0)) else Seq())
    )
    val pll = Module(new Series7MMCM(c))
    pll.io.clk_in1 := CLK100MHZ
    pll.io.reset := !ck_rst

    val aresetn = pll.io.locked // Reset that goes to the MMCM inside of the DDR MIG
    val sys_rst = ResetCatchAndSync(pll.io.clk_out2.get, !pll.io.locked) // Catched system clock
    val reset_to_sys = WireInit(!pll.io.locked) // If DDR is not present, this is the system reset
    val reset_to_child = WireInit(!pll.io.locked) // If DDR is not present, this is the child reset
    // The DDR port
    tlport.foreach{ chiptl =>
      val mod = Module(LazyModule(new TLULtoMIGArtyA7(chiptl.params)).module)

      // DDR port only
      ddr = Some(IO(new Arty100TMIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport
      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := pll.io.clk_out2.get.asBool()
      mod.io.ddrport.clk_ref_i := pll.io.clk_out3.get.asBool()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.ui_clk_sync_rst)
      ChildClock.foreach(_ := pll.io.clk_out1.getOrElse(false.B))
      ChildReset.foreach(_ := reset_to_sys)
      mod.clock := pll.io.clk_out1.getOrElse(false.B)
      mod.reset := reset_to_sys
      pll.io.clk_out4.foreach(reset_to_child := ResetCatchAndSync(_, !pll.io.locked))

      // TileLink Interface from platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      // Legacy ChildClock
      if (p(DDRPortOther)) {
        println("[Legacy] Quartus Island and Child Clock connected to clk_out4 (10MHz)")
        ChildClock.foreach(_ := pll.io.clk_out4.getOrElse(false.B))
        ChildReset.foreach(_ := reset_to_child)
        mod.clock := pll.io.clk_out4.getOrElse(false.B)
        mod.reset := reset_to_child
      }

      init_calib_complete := mod.io.ddrport.init_calib_complete
      depth = mod.depth
    }
    (memser zip memserSourceBits).foreach { case(ms, sourceBits) =>
      val mod = Module(LazyModule(new SertoMIGArtyA7(ms.w, sourceBits)).module)

      // Serial port
      mod.io.serport.flipConnect(ms)

      // DDR port only
      ddr = Some(IO(new Arty100TMIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport
      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := pll.io.clk_out2.get.asBool()
      mod.io.ddrport.clk_ref_i := pll.io.clk_out3.get.asBool()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.ui_clk_sync_rst)
      pll.io.clk_out4.foreach(reset_to_child := ResetCatchAndSync(_, !pll.io.locked))

      p(SbusToMbusXTypeKey) match {
        case _: AsynchronousCrossing =>
          println("[Legacy] Quartus Island connected to clk_out4 (10MHz)")
          mod.clock := pll.io.clk_out4.getOrElse(false.B)
          mod.reset := reset_to_child
        case _ =>
          mod.clock := pll.io.clk_out1.getOrElse(false.B)
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
    usbClk.foreach(_ := false.B.asClock())

    aclocks.foreach { aclocks =>
      println(s"Connecting async clocks by default =>")
      (aclocks zip namedclocks).foreach { case (aclk, nam) =>
        println(s"  Detected clock ${nam}")
        if(nam.contains("mbus")) {
          p(SbusToMbusXTypeKey) match {
            case _: AsynchronousCrossing =>
              aclk := pll.io.clk_out4.get
              println("    Connected to clk_out4 (10 MHz)")
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

trait WithFPGAArtyA7Connect {
  this: FPGAArtyA7Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGAArtyA7Internal(Some(chip)))
  // To intern = Clocks and resets
  intern.CLK100MHZ := CLK100MHZ
  intern.ck_rst := ck_rst
  ddr = intern.ddr.map{ A =>
    val port = IO(new Arty100TMIGIODDR(intern.depth))
    port <> A
    port
  }

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // GPIO
  if(chip.gpio_out.getWidth >= 1) IOBUF(led_0, chip.gpio_out(0))
  if(chip.gpio_out.getWidth >= 2) IOBUF(led_1, chip.gpio_out(1))
  if(chip.gpio_out.getWidth >= 3) IOBUF(led_2, chip.gpio_out(2))
  IOBUF(led_3, intern.init_calib_complete) //chip.gpio_out(3)
  chip.gpio_in := Cat(IOBUF(sw_3), IOBUF(sw_2), IOBUF(sw_1), IOBUF(sw_0))

  // JTAG
  chip.jtag.jtag_TCK := IBUFG(IOBUF(jd_2).asClock()).asBool()
  chip.jtag.jtag_TDI := IOBUF(jd_4)
  PULLUP(jd_4)
  IOBUF(jd_0, chip.jtag.jtag_TDO)
  chip.jtag.jtag_TMS := IOBUF(jd_5)
  PULLUP(jd_5)
  chip.jrst_n := IOBUF(jd_6)
  PULLUP(jd_6)

  // QSPI (assuming only one)
  chip.qspi.foreach{ qspi =>
    IOBUF(qspi_sck, qspi.qspi_sck)
    IOBUF(qspi_cs,  qspi.qspi_cs(0))

    IOBUF(qspi_dq(0), qspi.qspi_mosi)
    qspi.qspi_miso := IOBUF(qspi_dq(1))
    IOBUF(qspi_dq(2), qspi.qspi_wp)
    IOBUF(qspi_dq(3), qspi.qspi_hold)
  }

  // UART
  chip.uart_rxd := IOBUF(uart_txd_in)	  // UART_TXD
  IOBUF(uart_rxd_out, chip.uart_txd) 	  // UART_RXD

  // SD IO
  IOBUF(ja_0, chip.sdio.sdio_dat_3)
  IOBUF(ja_1, chip.sdio.sdio_cmd)
  chip.sdio.sdio_dat_0 := IOBUF(ja_2)
  IOBUF(ja_3, chip.sdio.sdio_clk)

  // USB phy connections
  // TODO: Not possible to create the 48MHz
}