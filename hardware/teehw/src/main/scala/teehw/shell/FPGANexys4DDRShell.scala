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
import sifive.fpgashells.clocks._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.ip.xilinx.nexys4ddrmig._
import uec.teehardware.macros._
import uec.teehardware._
import uec.teehardware.devices.clockctrl.ClockCtrlPortIO
import uec.teehardware.devices.sifiveblocks._
import uec.teehardware.devices.tlmemext._

trait FPGANexys4DDRChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters
  //-----------------------------------------------------------------------
  // Interface
  //-----------------------------------------------------------------------

  // Green LEDs
  val led          = IO(Vec(16, Analog(1.W)))

  // RGB LEDs, 3 pins each
  val led0_r       = IO(Analog(1.W))
  val led0_g       = IO(Analog(1.W))
  val led0_b       = IO(Analog(1.W))

  val led1_r       = IO(Analog(1.W))
  val led1_g       = IO(Analog(1.W))
  val led1_b       = IO(Analog(1.W))

  // 7 segment display (Mapped to GPIO)
  val cat          = IO(Vec(8, Analog(1.W)))
  val an           = IO(Vec(8, Analog(1.W)))

  // Sliding switches
  val sw           = IO(Vec(16, Analog(1.W)))

  // Buttons. First 2 used as GPIO, 1 as fallback, the last as wakeup
  val btn          = IO(Vec(5, Analog(1.W)))

  // UART0
  val uart_rxd_out = IO(Analog(1.W))
  val uart_txd_in  = IO(Analog(1.W))

  // Jx
  val ja           = IO(Vec(8, Analog(1.W)))
  val jb           = IO(Vec(8, Analog(1.W)))
  val jc           = IO(Vec(8, Analog(1.W)))
  val jd           = IO(Vec(8, Analog(1.W)))
}

trait FPGANexys4DDRClockAndResetsAndDDR {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters

  // Clock & Reset
  val CLK100MHZ    = IO(Input(Clock()))
  val ck_rst       = IO(Input(Bool()))
  // DDR
  var ddr: Option[Nexys4DDRMIGIODDR] = None
}

class FPGANexys4DDRShell(implicit val p :Parameters) extends RawModule
  with FPGANexys4DDRChipShell
  with FPGANexys4DDRClockAndResetsAndDDR {
}

class FPGANexys4DDRInternal(chip: Option[WithTEEHWbaseShell with WithTEEHWbaseConnect])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGANexys4DDRClockAndResetsAndDDR {
  def outer = chip

  val init_calib_complete = IO(Output(Bool()))
  init_calib_complete := false.B
  var depth = BigInt(0)

  // Some connections for having the clocks
  val clock = Wire(Clock())
  val reset = Wire(Bool())

  val isOtherClk = isChildClock || (p(SbusToMbusXTypeKey) match {
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
      val mod = Module(LazyModule(new TLULtoMIGNexys4DDR(chiptl.params)).module)

      // DDR port only
      ddr = Some(IO(new Nexys4DDRMIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport
      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := pll.io.clk_out2.get.asBool
      mod.io.ddrport.clk_ref_i := pll.io.clk_out3.get.asBool
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.ui_clk_sync_rst)
      mod.clock := pll.io.clk_out1.get
      mod.reset := reset_to_sys
      pll.io.clk_out4.foreach(reset_to_child := ResetCatchAndSync(_, !pll.io.locked))

      // TileLink Interface from platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      // Legacy ChildClock
      ChildClock.foreach { cclk =>
        println("Shell Island and Child Clock connected to clk_out4 (10MHz)")
        cclk := pll.io.clk_out4.get
        mod.clock := pll.io.clk_out4.get
        mod.reset := reset_to_child
      }

      init_calib_complete := mod.io.ddrport.init_calib_complete
      depth = mod.depth
    }
    (memser zip memserSourceBits).foreach { case(ms, sourceBits) =>
      val mod = Module(LazyModule(new SertoMIGNexys4DDR(ms.w, sourceBits)).module)

      // Serial port
      mod.io.serport.flipConnect(ms)

      // DDR port only
      ddr = Some(IO(new Nexys4DDRMIGIODDR(mod.depth)))
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
          println("Shell Island connected to clk_out4 (10MHz)")
          ChildClock.foreach(_ := pll.io.clk_out4.get)
          mod.clock := pll.io.clk_out4.get
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
    usbClk.foreach(_ := false.B.asClock())
    sdramclock.foreach(_ := pll.io.clk_out1.get)
    DefaultRTC

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

trait WithFPGANexys4DDRConnect {
  this: FPGANexys4DDRShell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGANexys4DDRInternal(Some(chip)))
  // To intern = Clocks and resets
  intern.CLK100MHZ := CLK100MHZ
  intern.ck_rst := ck_rst
  ddr = intern.ddr.map{ A =>
    val port = IO(new Nexys4DDRMIGIODDR(intern.depth))
    port <> A
    port
  }

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // GPIO
  chip.gpio_out.foreach{gpo =>
    if(gpo.getWidth >= 1) IOBUF(led(0), gpo(0))
    if(gpo.getWidth >= 2) IOBUF(led(1), gpo(1))
    if(gpo.getWidth >= 3) IOBUF(led(2), gpo(2))
  }
  IOBUF(led(3), intern.init_calib_complete) //chip.gpio_out(3)
  chip.gpio_in.foreach(gpi => gpi := Cat(IOBUF(sw(3)), IOBUF(sw(2)), IOBUF(sw(1)), IOBUF(sw(0))))

  // JTAG
  chip.jtag.jtag_TCK := IBUFG(IOBUF(jd(2)).asClock).asBool
  chip.jtag.jtag_TDI := IOBUF(jd(4))
  PULLUP(jd(4))
  IOBUF(jd(0), chip.jtag.jtag_TDO)
  chip.jtag.jtag_TMS := IOBUF(jd(5))
  PULLUP(jd(5))
  chip.jrst_n := IOBUF(jd(6))
  PULLUP(jd(6))

  // QSPI
  (chip.qspi zip chip.allspicfg).zipWithIndex.foreach {
    case ((qspiport: TEEHWQSPIBundle, _: SPIParams), i: Int) =>
      if (i == 0) {
        // SD IO
        IOBUF(ja(0), qspiport.qspi_cs(0))
        IOBUF(ja(1), qspiport.qspi_mosi)
        qspiport.qspi_miso := IOBUF(ja(2))
        IOBUF(ja(3), qspiport.qspi_sck)
      } else {
        // Non-valid qspi. Just zero it
        qspiport.qspi_miso := false.B
      }
    case ((qspiport: TEEHWQSPIBundle, _: SPIFlashParams), _: Int) =>
      IOBUF(jc(3), qspiport.qspi_sck)
      IOBUF(jc(0),  qspiport.qspi_cs(0))

      IOBUF(jc(1), qspiport.qspi_mosi)
      qspiport.qspi_miso := IOBUF(jc(2))
      IOBUF(jc(6), true.B) // qspi_dq(2)
      IOBUF(jc(7), true.B) // qspi_dq(3)
  }

  // UART
  chip.uart_rxd := IOBUF(uart_txd_in)	  // UART_TXD
  IOBUF(uart_rxd_out, chip.uart_txd) 	  // UART_RXD

  // USB phy connections
  // TODO: Not possible to create the 48MHz

  // TODO Nullify this for now
  chip.sdram.foreach{ sdram =>
    sdram.sdram_data_i := 0.U
  }
}
