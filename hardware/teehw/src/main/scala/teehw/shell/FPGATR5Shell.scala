package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.util._
import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import uec.teehardware.macros._
import uec.teehardware._

class FMCTR5(val ext: Boolean = false, val xcvr: Boolean = false) extends Bundle {
  val CLK_M2C_p = Vec(2, Input(Bool()))
  val CLK_M2C_n = Vec(2, Input(Bool()))
  val HA_RX_CLK_p = ext.option(Input(Bool()))
  val HA_RX_CLK_n = ext.option(Input(Bool()))
  val HB_RX_CLK_p = ext.option(Input(Bool()))
  val HB_RX_CLK_n = ext.option(Input(Bool()))
  val LA_RX_CLK_p = Input(Bool())
  val LA_RX_CLK_n = Input(Bool())
  val HA_TX_CLK_p = ext.option(Analog(1.W))
  val HA_TX_CLK_n = ext.option(Analog(1.W))
  val HB_TX_CLK_p = ext.option(Analog(1.W))
  val HB_TX_CLK_n = ext.option(Analog(1.W))
  val LA_TX_CLK_p = Analog(1.W)
  val LA_TX_CLK_n = Analog(1.W)
  val HA_TX_p = ext.option(Vec(11, Analog(1.W)))
  val HA_TX_n = ext.option(Vec(11, Analog(1.W)))
  val HA_RX_p = ext.option(Vec(11, Analog(1.W)))
  val HA_RX_n = ext.option(Vec(11, Analog(1.W)))
  val HB_TX_p = ext.option(Vec(11, Analog(1.W)))
  val HB_TX_n = ext.option(Vec(11, Analog(1.W)))
  val HB_RX_p = ext.option(Vec(11, Analog(1.W)))
  val HB_RX_n = ext.option(Vec(11, Analog(1.W)))
  val LA_TX_p = Vec(11, Analog(1.W))
  val LA_TX_n = Vec(11, Analog(1.W))
  val LA_RX_p = Vec(11, Analog(1.W))
  val LA_RX_n = Vec(11, Analog(1.W))

  val GBTCLK_M2C_p = ext.option(Vec(2, Input(Bool())))
  val ONBOARD_REFCLK_p = ext.option(Vec(2, Input(Bool())))
  val DP_C2M_p = ext.option(Vec(10, Analog(1.W)))
  val DP_M2C_p = ext.option(Vec(10, Input(Bool())))

  val GA = Vec(2, Analog(1.W))
  val SCL = Analog(1.W)
  val SDA = Analog(1.W)
}

trait FPGATR5ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters

  //////////// GPIO //////////
  val GPIO = IO(Vec(36, Analog(1.W)))

  //////////// FMCA //////////
  val FMCA = IO(new FMCTR5(ext = true))

  //////////// FMCB //////////
  val FMCB = IO(new FMCTR5(ext = false))

  //////////// FMCC //////////
  val FMCC = IO(new FMCTR5(ext = false))

  //////////// FMCD //////////
  val FMCD = IO(new FMCTR5(ext = true))

  ///////// SW /////////
  val SW = IO(Input(Bits(4.W)))

  ///////// LED /////////
  val LED = IO(Output(Bits(4.W)))

  ///////// FAN /////////
  val FAN_ALERT_n = IO(Input(Bool()))

  //////////// SD Card //////////
  val SD_CLK = IO(Output(Bool()))
  val SD_DATA = IO(Vec(4, Analog(1.W)))
  val SD_CMD = IO(Analog(1.W))

  //////////// Uart to USB //////////
  val UART_RX = IO(Analog(1.W))
  val UART_TX = IO(Analog(1.W))
}

trait FPGATR5ClockAndResetsAndDDR {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters

  ///////// CLOCKS /////////
  val OSC_50_B3B = IO(Input(Clock()))
  val OSC_50_B4A = IO(Input(Clock()))
  val OSC_50_B4D = IO(Input(Clock()))
  val OSC_50_B7A = IO(Input(Clock()))
  val OSC_50_B7D = IO(Input(Clock()))
  val OSC_50_B8A = IO(Input(Clock()))
  val OSC_50_B8D = IO(Input(Clock()))

  ///////// KEY /////////
  val CPU_RESET_n = IO(Input(Bool()))
  val BUTTON = IO(Input(Bits((3 + 1).W)))

  //////////// DDR3 //////////
  val DDR3_REFCLK_p = IO(Input(Clock()))
  val DDR3_A = IO(Output(Bits((15 + 1).W)))
  val DDR3_BA = IO(Output(Bits((2 + 1).W)))
  val DDR3_CK = IO(Output(Bits((1 + 1).W)))
  val DDR3_CK_n = IO(Output(Bits((1 + 1).W)))
  val DDR3_CKE = IO(Output(Bits((1 + 1).W)))
  val DDR3_DQS = IO(Analog((7 + 1).W))
  val DDR3_DQS_n = IO(Analog((7 + 1).W))
  val DDR3_DQ = IO(Analog((63 + 1).W))
  val DDR3_DM = IO(Output(Bits((7 + 1).W)))
  val DDR3_CS_n = IO(Output(Bits((1 + 1).W)))
  val DDR3_WE_n = IO(Output(Bool()))
  val DDR3_RAS_n = IO(Output(Bool()))
  val DDR3_CAS_n = IO(Output(Bool()))
  val DDR3_RESET_n = IO(Output(Bool()))
  val DDR3_ODT = IO(Output(Bits((1 + 1).W)))
  val DDR3_EVENT_n = IO(Input(Bool()))
  //val DDR3_SCL = IO(Analog(1.W))
  //val DDR3_SDA_n = IO(Analog(1.W))
  val RZQ_DDR3 = IO(Input(Bool()))

  val SMA_CLKIN_p = IO(Input(Clock()))
  val SMA_CLKOUT_p = IO(Analog(1.W))
}

class FPGATR5Shell(implicit val p :Parameters) extends RawModule
  with FPGATR5ChipShell
  with FPGATR5ClockAndResetsAndDDR {
}

class FPGATR5Internal(chip: Option[WithTEEHWbaseShell with WithTEEHWbaseConnect])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGATR5ClockAndResetsAndDDR {
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

      mod_io_ckrst.ddr_ref_clk := OSC_50_B3B.asUInt()
      mod_io_ckrst.qsys_ref_clk := OSC_50_B4A.asUInt() // TODO: This is okay?
      mod_io_ckrst.system_reset_n := CPU_RESET_n
    }

    // Helper function to connect the DDR from the Quartus Platform
    def ConnectDDRUtil(mod_io_qport: QuartusIO, mod_io_ckrst: Bundle with QuartusClocksReset) = {
      DDR3_A := mod_io_qport.memory_mem_a
      DDR3_BA := mod_io_qport.memory_mem_ba
      DDR3_CK := mod_io_qport.memory_mem_ck
      DDR3_CK_n := mod_io_qport.memory_mem_ck_n
      DDR3_CKE := mod_io_qport.memory_mem_cke
      DDR3_CS_n := mod_io_qport.memory_mem_cs_n
      DDR3_DM := mod_io_qport.memory_mem_dm
      DDR3_RAS_n := mod_io_qport.memory_mem_ras_n
      DDR3_CAS_n := mod_io_qport.memory_mem_cas_n
      DDR3_WE_n := mod_io_qport.memory_mem_we_n
      attach(DDR3_DQ, mod_io_qport.memory_mem_dq)
      attach(DDR3_DQS, mod_io_qport.memory_mem_dqs)
      attach(DDR3_DQS_n, mod_io_qport.memory_mem_dqs_n)
      DDR3_ODT := mod_io_qport.memory_mem_odt
      DDR3_RESET_n := mod_io_qport.memory_mem_reset_n.getOrElse(true.B)
      mod_io_qport.oct.rzqin.foreach(_ := RZQ_DDR3)
    }

    val ddrcfg = QuartusDDRConfig(
      size_ck = 2,
      is_reset = true,
      size_cke = 2,
      size_csn = 2,
      size_odt = 2,
      addrbit = 15,
      octmode = 1,
      size = 0x80000000L)

    tlport.foreach { chiptl =>
      // Instance our converter, and connect everything
      val mod = Module(LazyModule(new TLULtoQuartusPlatform(chiptl.params, ddrcfg)).module)

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
      val mod = Module(LazyModule(new SertoQuartusPlatform(ms.w, sourceBits, ddrcfg)).module)

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

class FPGATR5InternalNoChip()(implicit p :Parameters) extends FPGATR5Internal(None)(p) {
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

trait WithFPGATR5InternCreate {
  this: FPGATR5Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGATR5Internal(Some(chip)))
}

trait WithFPGATR5InternNoChipCreate {
  this: FPGATR5Shell =>
  val intern = Module(new FPGATR5InternalNoChip)
}

trait WithFPGATR5InternConnect {
  this: FPGATR5Shell =>
  val intern: FPGATR5Internal

  // To intern = Clocks and resets
  intern.OSC_50_B3B := OSC_50_B3B
  intern.OSC_50_B4A := OSC_50_B4A
  intern.OSC_50_B4D := OSC_50_B4D
  intern.OSC_50_B7A := OSC_50_B7A
  intern.OSC_50_B7D := OSC_50_B7D
  intern.OSC_50_B8A := OSC_50_B8A
  intern.OSC_50_B8D := OSC_50_B8D
  intern.CPU_RESET_n := CPU_RESET_n
  intern.BUTTON := BUTTON
  intern.SMA_CLKIN_p := SMA_CLKIN_p
  attach(SMA_CLKOUT_p, intern.SMA_CLKOUT_p)

  intern.DDR3_REFCLK_p := DDR3_REFCLK_p // TODO: Not actually used
  intern.DDR3_EVENT_n := DDR3_EVENT_n // TODO: Not actually used
  DDR3_A := intern.DDR3_A
  DDR3_BA := intern.DDR3_BA
  DDR3_CK := intern.DDR3_CK
  DDR3_CK_n := intern.DDR3_CK_n
  DDR3_CKE := intern.DDR3_CKE
  DDR3_CS_n := intern.DDR3_CS_n
  DDR3_DM := intern.DDR3_DM
  DDR3_RAS_n := intern.DDR3_RAS_n
  DDR3_CAS_n := intern.DDR3_CAS_n
  DDR3_WE_n := intern.DDR3_WE_n
  attach(DDR3_DQ, intern.DDR3_DQ)
  attach(DDR3_DQS, intern.DDR3_DQS)
  attach(DDR3_DQS_n, intern.DDR3_DQS_n)
  DDR3_ODT := intern.DDR3_ODT
  DDR3_RESET_n := intern.DDR3_RESET_n
  intern.RZQ_DDR3 := RZQ_DDR3
}

trait WithFPGATR5Connect extends WithFPGATR5InternCreate with WithFPGATR5InternConnect {
  this: FPGATR5Shell =>

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
    CPU_RESET_n
  )
  chip.gpio_in := Cat(BUTTON(3), BUTTON(1,0), SW(1,0))
  chip.jtag.jtag_TDI := ALT_IOBUF(GPIO(4))
  chip.jtag.jtag_TMS := ALT_IOBUF(GPIO(6))
  chip.jtag.jtag_TCK := ALT_IOBUF(GPIO(8))
  ALT_IOBUF(GPIO(10), chip.jtag.jtag_TDO)
  chip.qspi.foreach{A =>
    A.qspi_miso := ALT_IOBUF(GPIO(1))
    ALT_IOBUF(GPIO(3), A.qspi_mosi)
    ALT_IOBUF(GPIO(5), A.qspi_cs(0))
    ALT_IOBUF(GPIO(7), A.qspi_sck)
  }
  chip.uart_rxd := ALT_IOBUF(UART_RX)
  ALT_IOBUF(UART_TX, chip.uart_txd) // UART_RXD
  SD_CLK := chip.sdio.sdio_clk
  chip.sdio.sdio_dat_0 := ALT_IOBUF(SD_DATA(0))
  ALT_IOBUF(SD_DATA(3), chip.sdio.sdio_dat_3)
  ALT_IOBUF(SD_CMD, chip.sdio.sdio_cmd)

  // USB phy connections
  (chip.usb11hs zip intern.usbClk).foreach{ case (chipport, uclk) =>
    ALT_IOBUF(GPIO(17), chipport.USBFullSpeed)
    chipport.USBWireDataIn := ALT_IOBUF(GPIO(24))
    ALT_IOBUF(GPIO(24), chipport.USBWireCtrlOut(0))
    ALT_IOBUF(GPIO(26), chipport.USBWireCtrlOut(1))
    ALT_IOBUF(GPIO(16), chipport.USBWireDataOut(0))
    ALT_IOBUF(GPIO(18), chipport.USBWireDataOut(1))

    chipport.usbClk := uclk
  }
}
