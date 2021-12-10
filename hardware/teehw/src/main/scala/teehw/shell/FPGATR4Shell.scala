package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.util.ResetCatchAndSync
import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import uec.teehardware.macros._
import uec.teehardware._

class HSMCTR4 extends Bundle {
  val CLKIN0 = Input(Bool())
  val CLKIN_n1 = Input(Bool())
  val CLKIN_n2 = Input(Bool())
  val CLKIN_p1 = Input(Bool())
  val CLKIN_p2 = Input(Bool())
  val D = Vec(4, Analog(1.W))
  val OUT0 = Analog(1.W)
  val OUT_n1 = Analog(1.W)
  val OUT_n2 = Analog(1.W)
  val OUT_p1 = Analog(1.W)
  val OUT_p2 = Analog(1.W)
  val RX_n = Vec(17, Analog(1.W))
  val RX_p = Vec(17, Analog(1.W))
  val TX_n = Vec(17, Analog(1.W))
  val TX_p = Vec(17, Analog(1.W))
}

trait FPGATR4ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters

  ///////// LED /////////
  val LED = IO(Output(Bits((3 + 1).W)))

  ///////// SW /////////
  val SW = IO(Input(Bits((3 + 1).W)))

  ///////// FAN /////////
  val FAN_CTRL = IO(Output(Bool()))
  FAN_CTRL := true.B

  //////////// HSMC_A //////////
  val HSMA = IO(new HSMCTR4)

  //////////// HSMC_B //////////
  val HSMB = IO(new HSMCTR4)

  //////////// HSMC_C / GPIO //////////
  val GPIO0_D = IO(Vec(35+1, Analog(1.W)))
  val GPIO1_D = IO(Vec(35+1, Analog(1.W)))

  //////////// HSMC_D //////////
  val HSMD = IO(new HSMCTR4)

  //////////// HSMC_E //////////
  val HSME = IO(new HSMCTR4)

  //////////// HSMC_F //////////
  val HSMF = IO(new HSMCTR4)
}

trait FPGATR4ClockAndResetsAndDDR {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters

  ///////// CLOCKS /////////
  val OSC_50_BANK1 = IO(Input(Clock()))
  val OSC_50_BANK3 = IO(Input(Clock()))
  val OSC_50_BANK4 = IO(Input(Clock()))
  val OSC_50_BANK7 = IO(Input(Clock()))
  val OSC_50_BANK8 = IO(Input(Clock()))
  val SMA_CLKIN = IO(Input(Clock()))
  val SMA_CLKOUT = IO(Analog(1.W))
  val SMA_CLKOUT_n = IO(Analog(1.W))
  val SMA_CLKOUT_p = IO(Analog(1.W))

  ///////// BUTTON /////////
  val BUTTON = IO(Input(Bits((3 + 1).W)))

  //////////// mem //////////
  val mem_a = IO(Output(Bits((15 + 1).W)))
  val mem_ba = IO(Output(Bits((2 + 1).W)))
  val mem_cas_n = IO(Output(Bool()))
  val mem_cke = IO(Output(Bits((1 + 1).W)))
  val mem_ck = IO(Output(Bits((0 + 1).W))) // NOTE: Is impossible to do [0:0]
  val mem_ck_n = IO(Output(Bits((0 + 1).W))) // NOTE: Is impossible to do [0:0]
  val mem_cs_n = IO(Output(Bits((1 + 1).W)))
  val mem_dm = IO(Output(Bits((7 + 1).W)))
  val mem_dq = IO(Analog((63 + 1).W))
  val mem_dqs = IO(Analog((7 + 1).W))
  val mem_dqs_n = IO(Analog((7 + 1).W))
  val mem_odt = IO(Output(Bits((1 + 1).W)))
  val mem_ras_n = IO(Output(Bool()))
  val mem_reset_n = IO(Output(Bool()))
  val mem_we_n = IO(Output(Bool()))
  val mem_oct_rdn = IO(Input(Bool()))
  val mem_oct_rup = IO(Input(Bool()))
  //val mem_scl = IO(Output(Bool()))
  //val mem_sda = IO(Analog(1.W))
  //val mem_event_n = IO(Input(Bool())) // NOTE: This also appeared, but is not used
}

class FPGATR4Shell(implicit val p :Parameters) extends RawModule
  with FPGATR4ChipShell
  with FPGATR4ClockAndResetsAndDDR {
}

class FPGATR4Internal(chip: Option[WithTEEHWbaseShell with WithTEEHWbaseConnect])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGATR4ClockAndResetsAndDDR {
  def outer = chip
  override def otherId: Option[Int] = Some(6)

  val mem_status_local_cal_fail = IO(Output(Bool()))
  val mem_status_local_cal_success = IO(Output(Bool()))
  val mem_status_local_init_done = IO(Output(Bool()))

  val clock = Wire(Clock())
  val reset = Wire(Bool())

  withClockAndReset(clock, reset) {
    // The DDR port
    mem_status_local_cal_fail := false.B
    mem_status_local_cal_success := false.B
    mem_status_local_init_done := false.B

    // Helper function to connect clocks from the Quartus Platform
    def ConnectClockUtil(mod_clock: Clock, mod_io_qport: QuartusIO, mod_io_ckrst: Bundle with QuartusClocksReset) = {
      val reset_to_sys = ResetCatchAndSync(mod_io_ckrst.qsys_clk, !mod_io_qport.mem_status_local_init_done)
      val reset_to_child = ResetCatchAndSync(mod_io_ckrst.io_clk, !mod_io_qport.mem_status_local_init_done)

      // Clock and reset (for TL stuff)
      clock := mod_io_ckrst.qsys_clk
      reset := reset_to_sys
      sys_clk := mod_io_ckrst.qsys_clk
      aclocks.foreach(_.foreach(_ := mod_io_ckrst.qsys_clk)) // TODO: Connect your clocks here
      rst_n := !reset_to_sys
      jrst_n := !reset_to_sys
      usbClk.foreach(_ := mod_io_ckrst.usb_clk)
      if(p(DDRPortOther)) {
        ChildClock.foreach(_ := mod_io_ckrst.io_clk)
        ChildReset.foreach(_ := reset_to_child)
        mod_clock := mod_io_ckrst.io_clk
      }
      else {
        ChildClock.foreach(_ := mod_io_ckrst.qsys_clk)
        ChildReset.foreach(_ := reset_to_sys)
        mod_clock := mod_io_ckrst.qsys_clk
      }
      mod_io_ckrst.ddr_ref_clk := OSC_50_BANK1.asUInt()
      mod_io_ckrst.qsys_ref_clk := OSC_50_BANK4.asUInt() // TODO: This is okay?
      mod_io_ckrst.system_reset_n := BUTTON(2)
    }

    // Helper function to connect the DDR from the Quartus Platform
    def ConnectDDRUtil(mod_io_qport: QuartusIO, mod_io_ckrst: Bundle with QuartusClocksReset) = {
      mem_a := mod_io_qport.memory_mem_a
      mem_ba := mod_io_qport.memory_mem_ba
      mem_ck := mod_io_qport.memory_mem_ck(0) // Force only 1 line (although the config forces 1 line)
      mem_ck_n := mod_io_qport.memory_mem_ck_n(0) // Force only 1 line (although the config forces 1 line)
      mem_cke := mod_io_qport.memory_mem_cke
      mem_cs_n := mod_io_qport.memory_mem_cs_n
      mem_dm := mod_io_qport.memory_mem_dm
      mem_ras_n := mod_io_qport.memory_mem_ras_n
      mem_cas_n := mod_io_qport.memory_mem_cas_n
      mem_we_n := mod_io_qport.memory_mem_we_n
      attach(mem_dq, mod_io_qport.memory_mem_dq)
      attach(mem_dqs, mod_io_qport.memory_mem_dqs)
      attach(mem_dqs_n, mod_io_qport.memory_mem_dqs_n)
      mem_odt := mod_io_qport.memory_mem_odt
      mem_reset_n := mod_io_qport.memory_mem_reset_n.getOrElse(true.B)
      mod_io_qport.oct.rdn.foreach(_ := mem_oct_rdn)
      mod_io_qport.oct.rup.foreach(_ := mem_oct_rup)
    }
    
    tlport.foreach { chiptl =>
      // Instance our converter, and connect everything
      val mod = Module(LazyModule(new TLULtoQuartusPlatform(
        chiptl.params,
        QuartusDDRConfig(size_ck = 1, is_reset = true)
      )).module)

      // Quartus Platform connections
      ConnectDDRUtil(mod.io.qport, mod.io.ckrst)

      // TileLink Interface from platform
      // TODO: Make the DDR optional. Need to stop using the Quartus Platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      mem_status_local_cal_fail := mod.io.qport.mem_status_local_cal_fail
      mem_status_local_cal_success := mod.io.qport.mem_status_local_cal_success
      mem_status_local_init_done := mod.io.qport.mem_status_local_init_done

      // Clock and reset (for TL stuff)
      ConnectClockUtil(mod.clock, mod.io.qport, mod.io.ckrst)
    }
    (memser zip memserSourceBits).foreach { case(ms, sourceBits) =>
      // Instance our converter, and connect everything
      val mod = Module(LazyModule(new SertoQuartusPlatform(ms.w, sourceBits,
        QuartusDDRConfig(size_ck = 1, is_reset = true))).module)

      // Serial port
      mod.io.serport.flipConnect(ms)

      // Quartus Platform connections
      ConnectDDRUtil(mod.io.qport, mod.io.ckrst)

      mem_status_local_cal_fail := mod.io.qport.mem_status_local_cal_fail
      mem_status_local_cal_success := mod.io.qport.mem_status_local_cal_success
      mem_status_local_init_done := mod.io.qport.mem_status_local_init_done

      // Clock and reset (for TL stuff)
      ConnectClockUtil(mod.clock, mod.io.qport, mod.io.ckrst)
    }
    // The external bus (TODO: Doing nothing)
    (extser zip extserSourceBits).foreach { case (es, sourceBits) =>
      val mod = Module(LazyModule(new FPGAMiniSystem(sourceBits)).module)

      // Serial port
      mod.serport.flipConnect(es)
    }
  }
}

class FPGATR4InternalNoChip()(implicit p :Parameters) extends FPGATR4Internal(None)(p) {
  override def otherId = Some(6)
  override def tlparam = p(ExtMem).map { A =>
    TLBundleParameters(
      32,
      A.master.beatBytes * 8,
      6,
      1,
      log2Up(log2Ceil(p(MemoryBusKey).blockBytes)+1),
      Seq(),
      Seq(),
      Seq(),
      false)}
  override def aclkn: Option[Int] = None
  override def memserSourceBits: Option[Int] = None
  override def extserSourceBits: Option[Int] = None
  override def namedclocks: Seq[String] = Seq()
}

trait WithFPGATR4InternCreate {
  this: FPGATR4Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGATR4Internal(Some(chip)))
}

trait WithFPGATR4InternNoChipCreate {
  this: FPGATR4Shell =>
  val intern = Module(new FPGATR4InternalNoChip)
}

trait WithFPGATR4InternConnect {
  this: FPGATR4Shell =>
  val intern: FPGATR4Internal

  // To intern = Clocks and resets
  intern.OSC_50_BANK1 := OSC_50_BANK1
  intern.OSC_50_BANK3 := OSC_50_BANK3
  intern.OSC_50_BANK4 := OSC_50_BANK4
  intern.OSC_50_BANK7 := OSC_50_BANK7
  intern.OSC_50_BANK8 := OSC_50_BANK8
  intern.BUTTON := BUTTON
  intern.SMA_CLKIN := SMA_CLKIN
  attach(SMA_CLKOUT, intern.SMA_CLKOUT)
  attach(SMA_CLKOUT_n, intern.SMA_CLKOUT_n)
  attach(SMA_CLKOUT_p, intern.SMA_CLKOUT_p)

  mem_a := intern.mem_a
  mem_ba := intern.mem_ba
  mem_ck := intern.mem_ck
  mem_ck_n := intern.mem_ck_n
  mem_cke := intern.mem_cke
  mem_cs_n := intern.mem_cs_n
  mem_dm := intern.mem_dm
  mem_ras_n := intern.mem_ras_n
  mem_cas_n := intern.mem_cas_n
  mem_we_n := intern.mem_we_n
  attach(mem_dq, intern.mem_dq)
  attach(mem_dqs, intern.mem_dqs)
  attach(mem_dqs_n, intern.mem_dqs_n)
  mem_odt := intern.mem_odt
  mem_reset_n := intern.mem_reset_n
  intern.mem_oct_rdn := mem_oct_rdn
  intern.mem_oct_rup := mem_oct_rup

}

trait WithFPGATR4Connect extends WithFPGATR4InternCreate with WithFPGATR4InternConnect {
  this: FPGATR4Shell =>

  // From intern = Clocks and resets
  (chip.ChildClock zip intern.ChildClock).foreach{ case (a, b) => a := b }
  (chip.ChildReset zip intern.ChildReset).foreach{ case (a, b) => a := b }
  chip.sys_clk := intern.sys_clk
  chip.rst_n := intern.rst_n
  chip.jrst_n := intern.jrst_n
  // Memory port serialized
  (chip.memser zip intern.memser).foreach{ case (a, b) => a <> b }
  // Ext port serialized
  (chip.extser zip intern.extser).foreach{ case (a, b) => a <> b }
  // Memory port
  (chip.tlport zip intern.tlport).foreach{ case (a, b) => b.a <> a.a; a.d <> b.d }
  // Asyncrhonoys clocks
  (chip.aclocks zip intern.aclocks).foreach{ case (a, b) => (a zip b).foreach{ case (c, d) => c := d} }

  // The rest of the platform connections
  val chipshell_led = chip.gpio_out 	// TODO: Not used! LED [3:0]
  LED := Cat(
    intern.mem_status_local_cal_fail,
    intern.mem_status_local_cal_success,
    intern.mem_status_local_init_done,
    BUTTON(2)
  )
  chip.gpio_in := Cat(BUTTON(3), BUTTON(1,0), SW(1,0))
  chip.jtag.jtag_TDI := ALT_IOBUF(GPIO1_D(4))
  chip.jtag.jtag_TMS := ALT_IOBUF(GPIO1_D(6))
  chip.jtag.jtag_TCK := ALT_IOBUF(GPIO1_D(8))
  ALT_IOBUF(GPIO1_D(10), chip.jtag.jtag_TDO)
  chip.qspi.foreach{A =>
    A.qspi_miso := ALT_IOBUF(GPIO1_D(1))
    ALT_IOBUF(GPIO1_D(3), A.qspi_mosi)
    ALT_IOBUF(GPIO1_D(5), A.qspi_cs(0))
    ALT_IOBUF(GPIO1_D(7), A.qspi_sck)
  }
  chip.uart_rxd := ALT_IOBUF(GPIO1_D(35))	// UART_TXD
  ALT_IOBUF(GPIO1_D(34), chip.uart_txd) // UART_RXD
  ALT_IOBUF(GPIO0_D(28), chip.sdio.sdio_clk)
  ALT_IOBUF(GPIO0_D(30), chip.sdio.sdio_cmd)
  chip.sdio.sdio_dat_0 := ALT_IOBUF(GPIO0_D(32))
  ALT_IOBUF(GPIO0_D(34), chip.sdio.sdio_dat_3)

  // USB phy connections
  (chip.usb11hs zip intern.usbClk).foreach{ case (chipport, uclk) =>
    ALT_IOBUF(GPIO1_D(17), chipport.USBFullSpeed)
    chipport.USBWireDataIn := ALT_IOBUF(GPIO1_D(24))
    ALT_IOBUF(GPIO1_D(24), chipport.USBWireCtrlOut(0))
    ALT_IOBUF(GPIO1_D(26), chipport.USBWireCtrlOut(1))
    ALT_IOBUF(GPIO1_D(16), chipport.USBWireDataOut(0))
    ALT_IOBUF(GPIO1_D(18), chipport.USBWireDataOut(1))

    chipport.usbClk := uclk
  }
}

// Based on layout of the TR4.sch done by Duy
trait WithFPGATR4ToChipConnect extends WithFPGATR4InternNoChipCreate with WithFPGATR4InternConnect {
  this: FPGATR4Shell =>

  // NOTES:
  // JP19 -> J2 / JP18 -> J3 belongs to HSMB
  // JP20 -> J2 / JP21 -> J3 belongs to HSMA
  def HSMC_JP19_18 = HSMB
  def HSMC_JP20_21 = HSMA

  // From intern = Clocks and resets
  intern.ChildClock.foreach{ a =>
    ALT_IOBUF(HSMC_JP20_21.RX_n(7), a.asBool()) // JP21 - J3 2 / HSMC_JP20_21.RX_n(7)
  }
  intern.ChildReset.foreach{ a =>
    ALT_IOBUF(HSMC_JP19_18.TX_n(7), a) // JP18 - J3 5 / HSMC_JP19_18.TX_n(7)
  }
  ALT_IOBUF(HSMC_JP20_21.RX_n(8), intern.sys_clk.asBool()) // JP21 - J3 4 / HSMC_JP20_21.RX_n(8)
  ALT_IOBUF(HSMC_JP19_18.RX_n(7), intern.rst_n) // JP18 - J3 2 / HSMC_JP19_18.RX_n(7)
  ALT_IOBUF(HSMC_JP19_18.RX_n(6), intern.jrst_n) // JP18 - J3 6 / HSMC_JP19_18.RX_n(6)
  // Memory port serialized
  intern.memser.foreach{ a => a } // NOTHING
  // Ext port serialized
  intern.extser.foreach{ a => a } // NOTHING
  // Memory port
  intern.tlport.foreach{ tlport =>
    tlport.a.valid := HSMC_JP19_18.CLKIN_n1 // JP18 - J3 1 / HSMC_JP19_18.CLKIN_n1
    ALT_IOBUF(HSMC_JP19_18.RX_p(7), tlport.a.ready) // JP18 - J3 4 / HSMC_JP19_18.RX_p(7)
    require(tlport.a.bits.opcode.getWidth == 3, s"${tlport.a.bits.opcode.getWidth}")
    tlport.a.bits.opcode := Cat(
      HSMC_JP19_18.CLKIN_p1, // JP18 - J3 3 / HSMC_JP19_18.CLKIN_p1
      ALT_IOBUF(HSMC_JP19_18.TX_p(7)), // JP18 - J3 7 / HSMC_JP19_18.TX_p(7)
      ALT_IOBUF(HSMC_JP19_18.RX_p(6)), // JP18 - J3 8 / HSMC_JP19_18.RX_p(6)
    )
    require(tlport.a.bits.param.getWidth == 3, s"${tlport.a.bits.param.getWidth}")
    tlport.a.bits.param := Cat(
      ALT_IOBUF(HSMC_JP19_18.TX_n(6)), // JP18 - J3 9 / HSMC_JP19_18.TX_n(6)
      ALT_IOBUF(HSMC_JP19_18.RX_n(5)), // JP18 - J3 10 / HSMC_JP19_18.RX_n(5)
      ALT_IOBUF(HSMC_JP19_18.TX_p(6)), // JP18 - J3 13 / HSMC_JP19_18.TX_p(6)
    )
    require(tlport.a.bits.size.getWidth == 3, s"${tlport.a.bits.size.getWidth}")
    tlport.a.bits.size := Cat(
      ALT_IOBUF(HSMC_JP19_18.RX_p(5)), // JP18 - J3 14 / HSMC_JP19_18.RX_p(5)
      ALT_IOBUF(HSMC_JP19_18.TX_n(5)), // JP18 - J3 15 / HSMC_JP19_18.TX_n(5)
      ALT_IOBUF(HSMC_JP19_18.RX_n(4)), // JP18 - J3 16 / HSMC_JP19_18.RX_n(4)
    )
    require(tlport.a.bits.source.getWidth == 6, s"${tlport.a.bits.source.getWidth}")
    tlport.a.bits.source := Cat(
      ALT_IOBUF(HSMC_JP19_18.TX_p(5)), // JP18 - J3 17 / HSMC_JP19_18.TX_p(5)
      ALT_IOBUF(HSMC_JP19_18.RX_p(4)), // JP18 - J3 18 / HSMC_JP19_18.RX_p(4)
      ALT_IOBUF(HSMC_JP19_18.OUT_n1), // JP18 - J3 19 / HSMC_JP19_18.OUT_n1
      ALT_IOBUF(HSMC_JP19_18.RX_n(3)), // JP18 - J3 20 / HSMC_JP19_18.RX_n(3)
      ALT_IOBUF(HSMC_JP19_18.OUT_p1), // JP18 - J3 21 / HSMC_JP19_18.OUT_p1
      ALT_IOBUF(HSMC_JP19_18.RX_p(3)), // JP18 - J3 22 / HSMC_JP19_18.RX_p(3)
    )
    require(tlport.a.bits.address.getWidth == 32, s"${tlport.a.bits.address.getWidth}")
    tlport.a.bits.address := Cat(
      ALT_IOBUF(HSMC_JP19_18.TX_n(4)), // [31] JP18 - J3 23 / HSMC_JP19_18.TX_n(4)
      ALT_IOBUF(HSMC_JP19_18.RX_n(2)), // [30] JP18 - J3 24 / HSMC_JP19_18.RX_n(2)
      ALT_IOBUF(HSMC_JP19_18.TX_p(4)), // [29] JP18 - J3 25 / HSMC_JP19_18.TX_p(4)
      ALT_IOBUF(HSMC_JP19_18.RX_p(2)), // [28] JP18 - J3 26 / HSMC_JP19_18.RX_p(2)
      ALT_IOBUF(HSMC_JP19_18.TX_n(3)), // [27] JP18 - J3 27 / HSMC_JP19_18.TX_n(3)
      ALT_IOBUF(HSMC_JP19_18.RX_n(1)), // [26] JP18 - J3 28 / HSMC_JP19_18.RX_n(1)
      ALT_IOBUF(HSMC_JP19_18.TX_p(3)), // [25] JP18 - J3 31 / HSMC_JP19_18.TX_p(3)
      ALT_IOBUF(HSMC_JP19_18.RX_p(1)), // [24] JP18 - J3 32 / HSMC_JP19_18.RX_p(1)
      ALT_IOBUF(HSMC_JP19_18.TX_n(2)), // [23] JP18 - J3 33 / HSMC_JP19_18.TX_n(2)
      ALT_IOBUF(HSMC_JP19_18.RX_n(0)), // [22] JP18 - J3 34 / HSMC_JP19_18.RX_n(0)
      ALT_IOBUF(HSMC_JP19_18.TX_p(2)), // [21] JP18 - J3 35 / HSMC_JP19_18.TX_p(2)
      ALT_IOBUF(HSMC_JP19_18.RX_p(0)), // [20] JP18 - J3 36 / HSMC_JP19_18.RX_p(0)
      ALT_IOBUF(HSMC_JP19_18.TX_n(1)), // [19] JP18 - J3 37 / HSMC_JP19_18.TX_n(1)
      ALT_IOBUF(HSMC_JP19_18.TX_n(0)), // [18] JP18 - J3 38 / HSMC_JP19_18.TX_n(0)
      ALT_IOBUF(HSMC_JP19_18.TX_p(1)), // [17] JP18 - J3 39 / HSMC_JP19_18.TX_p(1)
      ALT_IOBUF(HSMC_JP19_18.TX_p(0)), // [16] JP18 - J3 40 / HSMC_JP19_18.TX_p(0)

      HSMC_JP19_18.CLKIN_n2, // [15] JP19 - J2 1 / HSMC_JP19_18.CLKIN_n2
      ALT_IOBUF(HSMC_JP19_18.RX_n(16)), // [14] JP19 - J2 2 / HSMC_JP19_18.RX_n(16)
      HSMC_JP19_18.CLKIN_p2, // [13] JP19 - J2 3 / HSMC_JP19_18.CLKIN_p2
      ALT_IOBUF(HSMC_JP19_18.RX_p(16)), // [12] JP19 - J2 4 / HSMC_JP19_18.RX_p(16)
      ALT_IOBUF(HSMC_JP19_18.TX_n(16)), // [11] JP19 - J2 5 / HSMC_JP19_18.TX_n(16)
      ALT_IOBUF(HSMC_JP19_18.RX_n(15)), // [10] JP19 - J2 6 / HSMC_JP19_18.RX_n(15)
      ALT_IOBUF(HSMC_JP19_18.TX_p(16)), // [9] JP19 - J2 7 / HSMC_JP19_18.TX_p(16)
      ALT_IOBUF(HSMC_JP19_18.RX_p(15)), // [8] JP19 - J2 8 / HSMC_JP19_18.RX_p(15)
      ALT_IOBUF(HSMC_JP19_18.TX_n(15)), // [7] JP19 - J2 9 / HSMC_JP19_18.TX_n(15)
      ALT_IOBUF(HSMC_JP19_18.RX_n(14)), // [6] JP19 - J2 10 / HSMC_JP19_18.RX_n(14)
      ALT_IOBUF(HSMC_JP19_18.TX_p(15)), // [5] JP19 - J2 13 / HSMC_JP19_18.TX_p(15)
      ALT_IOBUF(HSMC_JP19_18.RX_p(14)), // [4] JP19 - J2 14 / HSMC_JP19_18.RX_p(14)
      ALT_IOBUF(HSMC_JP19_18.TX_n(14)), // [3] JP19 - J2 15 / HSMC_JP19_18.TX_n(14)
      ALT_IOBUF(HSMC_JP19_18.RX_n(13)), // [2] JP19 - J2 16 / HSMC_JP19_18.RX_n(13)
      ALT_IOBUF(HSMC_JP19_18.TX_p(14)), // [1] JP19 - J2 17 / HSMC_JP19_18.TX_p(14)
      ALT_IOBUF(HSMC_JP19_18.RX_p(13)), // [0] JP19 - J2 18 / HSMC_JP19_18.RX_p(13)
    )
    require(tlport.a.bits.mask.getWidth == 4, s"${tlport.a.bits.mask.getWidth}")
    tlport.a.bits.mask := Cat(
      ALT_IOBUF(HSMC_JP19_18.OUT_n2), // [3] JP19 - J2 19 / HSMC_JP19_18.OUT_n2
      ALT_IOBUF(HSMC_JP19_18.RX_n(12)), // [2] JP19 - J2 20 / HSMC_JP19_18.RX_n(12)
      ALT_IOBUF(HSMC_JP19_18.OUT_p2), // [1] JP19 - J2 21 / HSMC_JP19_18.OUT_p2
      ALT_IOBUF(HSMC_JP19_18.RX_p(12)), // [0] JP19 - J2 22 / HSMC_JP19_18.RX_p(12)
    )
    require(tlport.a.bits.data.getWidth == 32, s"${tlport.a.bits.data.getWidth}")
    tlport.a.bits.data := Cat(
      ALT_IOBUF(HSMC_JP19_18.TX_n(13)), // [31] JP19 - J2 23 / HSMC_JP19_18.TX_n(13)
      ALT_IOBUF(HSMC_JP19_18.RX_n(11)), // [30] JP19 - J2 24 / HSMC_JP19_18.RX_n(11)
      ALT_IOBUF(HSMC_JP19_18.TX_p(13)), // [29] JP19 - J2 25 / HSMC_JP19_18.TX_p(13)
      ALT_IOBUF(HSMC_JP19_18.RX_p(11)), // [28] JP19 - J2 26 / HSMC_JP19_18.RX_p(11)
      ALT_IOBUF(HSMC_JP19_18.TX_n(12)), // [27] JP19 - J2 27 / HSMC_JP19_18.TX_n(12)
      ALT_IOBUF(HSMC_JP19_18.RX_n(10)), // [26] JP19 - J2 28 / HSMC_JP19_18.RX_n(10)
      ALT_IOBUF(HSMC_JP19_18.TX_p(12)), // [25] JP19 - J2 31 / HSMC_JP19_18.TX_p(12)
      ALT_IOBUF(HSMC_JP19_18.RX_p(10)), // [24] JP19 - J2 32 / HSMC_JP19_18.RX_p(10)
      ALT_IOBUF(HSMC_JP19_18.TX_n(11)), // [23] JP19 - J2 33 / HSMC_JP19_18.TX_n(11)
      ALT_IOBUF(HSMC_JP19_18.RX_n(9)), // [22] JP19 - J2 34 / HSMC_JP19_18.RX_n(9)
      ALT_IOBUF(HSMC_JP19_18.TX_p(11)), // [21] JP19 - J2 35 / HSMC_JP19_18.TX_p(11)
      ALT_IOBUF(HSMC_JP19_18.RX_p(9)), // [20] JP19 - J2 36 / HSMC_JP19_18.RX_p(9)
      ALT_IOBUF(HSMC_JP19_18.TX_n(10)), // [19] JP19 - J2 37 / HSMC_JP19_18.TX_n(10)
      ALT_IOBUF(HSMC_JP19_18.TX_n(9)), // [18] JP19 - J2 38 / HSMC_JP19_18.TX_n(9)
      ALT_IOBUF(HSMC_JP19_18.TX_p(10)), // [17] JP19 - J2 39 / HSMC_JP19_18.TX_p(10)
      ALT_IOBUF(HSMC_JP19_18.TX_p(9)), // [16] JP19 - J2 40 / HSMC_JP19_18.TX_p(9)

      ALT_IOBUF(HSMC_JP20_21.RX_n(14)), // [15] JP20 - J2 10 / HSMC_JP20_21.RX_n(14)
      ALT_IOBUF(HSMC_JP20_21.TX_n(15)), // [14] JP20 - J2 9 / HSMC_JP20_21.TX_n(15)
      ALT_IOBUF(HSMC_JP20_21.RX_p(15)), // [13] JP20 - J2 8 / HSMC_JP20_21.RX_p(15)
      ALT_IOBUF(HSMC_JP20_21.TX_p(16)), // [12] JP20 - J2 7 / HSMC_JP20_21.TX_p(16)
      ALT_IOBUF(HSMC_JP20_21.RX_n(15)), // [11] JP20 - J2 6 / HSMC_JP20_21.RX_n(15)
      ALT_IOBUF(HSMC_JP20_21.TX_n(16)), // [10] JP20 - J2 5 / HSMC_JP20_21.TX_n(16)
      ALT_IOBUF(HSMC_JP20_21.RX_p(16)), // [9] JP20 - J2 4 / HSMC_JP20_21.RX_p(16)
      HSMC_JP20_21.CLKIN_p2, // [8] JP20 - J2 3 / HSMC_JP20_21.CLKIN_p2
      ALT_IOBUF(HSMC_JP20_21.RX_n(16)), // [7] JP20 - J2 2 / HSMC_JP20_21.RX_n(16)
      HSMC_JP20_21.CLKIN_n2, // [6] JP20 - J2 1 / HSMC_JP20_21.CLKIN_n2

      ALT_IOBUF(HSMC_JP20_21.TX_p(15)), // [5] JP20 - J2 13 / HSMC_JP20_21.TX_p(15)
      ALT_IOBUF(HSMC_JP20_21.RX_p(14)), // [4] JP20 - J2 14 / HSMC_JP20_21.RX_p(14)
      ALT_IOBUF(HSMC_JP20_21.TX_n(14)), // [3] JP20 - J2 15 / HSMC_JP20_21.TX_n(14)
      ALT_IOBUF(HSMC_JP20_21.RX_n(13)), // [2] JP20 - J2 16 / HSMC_JP20_21.RX_n(13)
      ALT_IOBUF(HSMC_JP20_21.TX_p(14)), // [1] JP20 - J2 17 / HSMC_JP20_21.TX_p(14)
      ALT_IOBUF(HSMC_JP20_21.RX_p(13)), // [0] JP20 - J2 18 / HSMC_JP20_21.RX_p(13)
    )
    tlport.a.bits.corrupt := ALT_IOBUF(HSMC_JP20_21.OUT_n2) // JP20 - J2 19 / HSMC_JP20_21.OUT_n2

    tlport.d.ready := ALT_IOBUF(HSMC_JP20_21.RX_n(12)) // JP20 - J2 20 / HSMC_JP20_21.RX_n(12)
    ALT_IOBUF(HSMC_JP20_21.OUT_p2, tlport.d.valid) // JP20 - J2 21 / HSMC_JP20_21.OUT_p2

    require(tlport.d.bits.opcode.getWidth == 3, s"${tlport.d.bits.opcode.getWidth}")
    ALT_IOBUF(HSMC_JP20_21.RX_p(12), tlport.d.bits.opcode(2)) // JP20 - J2 22 / HSMC_JP20_21.RX_p(12)
    ALT_IOBUF(HSMC_JP20_21.TX_n(13), tlport.d.bits.opcode(1)) // JP20 - J2 23 / HSMC_JP20_21.TX_n(13)
    ALT_IOBUF(HSMC_JP20_21.RX_n(11), tlport.d.bits.opcode(0)) // JP20 - J2 24 / HSMC_JP20_21.RX_n(11)

    require(tlport.d.bits.param.getWidth == 2, s"${tlport.d.bits.param.getWidth}")
    ALT_IOBUF(HSMC_JP20_21.TX_p(13), tlport.d.bits.param(1)) // JP20 - J2 25 / HSMC_JP20_21.TX_p(13)
    ALT_IOBUF(HSMC_JP20_21.RX_p(11), tlport.d.bits.param(0)) // JP20 - J2 26 / HSMC_JP20_21.RX_p(11)

    require(tlport.d.bits.size.getWidth == 3, s"${tlport.d.bits.size.getWidth}")
    ALT_IOBUF(HSMC_JP20_21.TX_n(12), tlport.d.bits.size(2)) // JP20 - J2 27 / HSMC_JP20_21.TX_n(12)
    ALT_IOBUF(HSMC_JP20_21.RX_n(10), tlport.d.bits.size(1)) // JP20 - J2 28 / HSMC_JP20_21.RX_n(10)
    ALT_IOBUF(HSMC_JP20_21.TX_p(12), tlport.d.bits.size(0)) // JP20 - J2 31 / HSMC_JP20_21.TX_p(12)

    require(tlport.d.bits.source.getWidth == 6, s"${tlport.d.bits.source.getWidth}")
    ALT_IOBUF(HSMC_JP20_21.RX_p(10), tlport.d.bits.source(5)) // JP20 - J2 32 / HSMC_JP20_21.RX_p(10)
    ALT_IOBUF(HSMC_JP20_21.TX_n(11), tlport.d.bits.source(4)) // JP20 - J2 33 / HSMC_JP20_21.TX_n(11)
    ALT_IOBUF(HSMC_JP20_21.RX_n(9), tlport.d.bits.source(3)) // JP20 - J2 34 / HSMC_JP20_21.RX_n(9)
    ALT_IOBUF(HSMC_JP20_21.TX_p(11), tlport.d.bits.source(2)) // JP20 - J2 35 / HSMC_JP20_21.TX_p(11)
    ALT_IOBUF(HSMC_JP20_21.RX_p(9), tlport.d.bits.source(1)) // JP20 - J2 36 / HSMC_JP20_21.RX_p(9)
    ALT_IOBUF(HSMC_JP20_21.TX_n(10), tlport.d.bits.source(0)) // JP20 - J2 37 / HSMC_JP20_21.TX_n(10)

    require(tlport.d.bits.sink.getWidth == 1, s"${tlport.d.bits.sink.getWidth}")
    ALT_IOBUF(HSMC_JP20_21.TX_n(9), tlport.d.bits.sink(0)) // JP20 - J2 38 / HSMC_JP20_21.TX_n(9)
    ALT_IOBUF(HSMC_JP20_21.TX_p(10), tlport.d.bits.denied) // JP20 - J2 39 / HSMC_JP20_21.TX_p(10)
    ALT_IOBUF(HSMC_JP20_21.TX_p(9), tlport.d.bits.corrupt) // JP20 - J2 40 / HSMC_JP20_21.TX_p(9)

    require(tlport.d.bits.data.getWidth == 32, s"${tlport.d.bits.data.getWidth}")
    ALT_IOBUF(HSMC_JP20_21.TX_n(7), tlport.d.bits.data(31)) // JP21 - J3 5 / HSMC_JP20_21.TX_n(7)
    ALT_IOBUF(HSMC_JP20_21.RX_n(6), tlport.d.bits.data(30)) // JP21 - J3 6 / HSMC_JP20_21.RX_n(6)
    ALT_IOBUF(HSMC_JP20_21.TX_p(7), tlport.d.bits.data(29)) // JP21 - J3 7 / HSMC_JP20_21.TX_p(7)
    ALT_IOBUF(HSMC_JP20_21.RX_p(6), tlport.d.bits.data(28)) // JP21 - J3 8 / HSMC_JP20_21.RX_p(6)
    ALT_IOBUF(HSMC_JP20_21.TX_n(6), tlport.d.bits.data(27)) // JP21 - J3 9 / HSMC_JP20_21.TX_n(6)
    ALT_IOBUF(HSMC_JP20_21.RX_n(5), tlport.d.bits.data(26)) // JP21 - J3 10 / HSMC_JP20_21.RX_n(5)
    ALT_IOBUF(HSMC_JP20_21.TX_p(6), tlport.d.bits.data(25)) // JP21 - J3 13 / HSMC_JP20_21.TX_p(6)
    ALT_IOBUF(HSMC_JP20_21.RX_p(5), tlport.d.bits.data(24)) // JP21 - J3 14 / HSMC_JP20_21.RX_p(5)
    ALT_IOBUF(HSMC_JP20_21.TX_n(5), tlport.d.bits.data(23)) // JP21 - J3 15 / HSMC_JP20_21.TX_n(5)
    ALT_IOBUF(HSMC_JP20_21.RX_n(4), tlport.d.bits.data(22)) // JP21 - J3 16 / HSMC_JP20_21.RX_n(4)
    ALT_IOBUF(HSMC_JP20_21.TX_p(5), tlport.d.bits.data(21)) // JP21 - J3 17 / HSMC_JP20_21.TX_p(5)
    ALT_IOBUF(HSMC_JP20_21.RX_p(4), tlport.d.bits.data(20)) // JP21 - J3 18 / HSMC_JP20_21.RX_p(4)
    ALT_IOBUF(HSMC_JP20_21.OUT_n1, tlport.d.bits.data(19)) // JP21 - J3 19 / HSMC_JP20_21.OUT_n1
    ALT_IOBUF(HSMC_JP20_21.RX_n(3), tlport.d.bits.data(18)) // JP21 - J3 20 / HSMC_JP20_21.RX_n(3)
    ALT_IOBUF(HSMC_JP20_21.OUT_p1, tlport.d.bits.data(17)) // JP21 - J3 21 / HSMC_JP20_21.OUT_p1
    ALT_IOBUF(HSMC_JP20_21.RX_p(3), tlport.d.bits.data(16)) // JP21 - J3 22 / HSMC_JP20_21.RX_p(3)
    ALT_IOBUF(HSMC_JP20_21.TX_n(4), tlport.d.bits.data(15)) // JP21 - J3 23 / HSMC_JP20_21.TX_n(4)
    ALT_IOBUF(HSMC_JP20_21.RX_n(2), tlport.d.bits.data(14)) // JP21 - J3 24 / HSMC_JP20_21.RX_n(2)
    ALT_IOBUF(HSMC_JP20_21.TX_p(4), tlport.d.bits.data(13)) // JP21 - J3 25 / HSMC_JP20_21.TX_p(4)
    ALT_IOBUF(HSMC_JP20_21.RX_p(2), tlport.d.bits.data(12)) // JP21 - J3 26 / HSMC_JP20_21.RX_p(2)
    ALT_IOBUF(HSMC_JP20_21.TX_n(3), tlport.d.bits.data(11)) // JP21 - J3 27 / HSMC_JP20_21.TX_n(3)
    ALT_IOBUF(HSMC_JP20_21.RX_n(1), tlport.d.bits.data(10)) // JP21 - J3 28 / HSMC_JP20_21.RX_n(1)
    ALT_IOBUF(HSMC_JP20_21.TX_p(3), tlport.d.bits.data(9)) // JP21 - J3 31 / HSMC_JP20_21.TX_p(3)
    ALT_IOBUF(HSMC_JP20_21.RX_p(1), tlport.d.bits.data(8)) // JP21 - J3 32 / HSMC_JP20_21.RX_p(1)
    ALT_IOBUF(HSMC_JP20_21.TX_n(2), tlport.d.bits.data(7)) // JP21 - J3 33 / HSMC_JP20_21.TX_n(2)
    ALT_IOBUF(HSMC_JP20_21.RX_n(0), tlport.d.bits.data(6)) // JP21 - J3 34 / HSMC_JP20_21.RX_n(0)
    ALT_IOBUF(HSMC_JP20_21.TX_p(2), tlport.d.bits.data(5)) // JP21 - J3 35 / HSMC_JP20_21.TX_p(2)
    ALT_IOBUF(HSMC_JP20_21.RX_p(0), tlport.d.bits.data(4)) // JP21 - J3 36 / HSMC_JP20_21.RX_p(0)
    ALT_IOBUF(HSMC_JP20_21.TX_n(1), tlport.d.bits.data(3)) // JP21 - J3 37 / HSMC_JP20_21.TX_n(1)
    ALT_IOBUF(HSMC_JP20_21.TX_n(0), tlport.d.bits.data(2)) // JP21 - J3 38 / HSMC_JP20_21.TX_n(0)
    ALT_IOBUF(HSMC_JP20_21.TX_p(1), tlport.d.bits.data(1)) // JP21 - J3 39 / HSMC_JP20_21.TX_p(1)
    ALT_IOBUF(HSMC_JP20_21.TX_p(0), tlport.d.bits.data(0)) // JP21 - J3 40 / HSMC_JP20_21.TX_p(0)
  }
  // Asyncrhonoys clocks
  intern.aclocks.foreach{ a => a } // NOTHING

  // LEDs
  LED := Cat(
    intern.mem_status_local_cal_fail,
    intern.mem_status_local_cal_success,
    intern.mem_status_local_init_done,
    BUTTON(2)
  )
  // Clocks to the outside
  ALT_IOBUF(SMA_CLKOUT, intern.sys_clk.asBool())
  intern.ChildClock.foreach(A => ALT_IOBUF(SMA_CLKOUT_p, A.asBool()))
  intern.usbClk.foreach(A => ALT_IOBUF(SMA_CLKOUT_n, A.asBool()))
}