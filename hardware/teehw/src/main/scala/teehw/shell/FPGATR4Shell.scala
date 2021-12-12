package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing, LazyModule}
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
    def ConnectClockUtil(mod_clock: Clock, mod_reset: Reset, mod_io_qport: QuartusIO, mod_io_ckrst: Bundle with QuartusClocksReset) = {
      val reset_to_sys = ResetCatchAndSync(mod_io_ckrst.qsys_clk, !mod_io_qport.mem_status_local_init_done)
      val reset_to_child = ResetCatchAndSync(mod_io_ckrst.io_clk, !mod_io_qport.mem_status_local_init_done)

      // Clock and reset (for TL stuff)
      clock := mod_io_ckrst.qsys_clk
      reset := reset_to_sys
      sys_clk := mod_io_ckrst.qsys_clk
      ChildClock.foreach(_ := mod_io_ckrst.qsys_clk)
      ChildReset.foreach(_ := reset_to_sys)
      mod_clock := mod_io_ckrst.qsys_clk
      mod_reset := reset_to_sys
      rst_n := !reset_to_sys
      jrst_n := !reset_to_sys
      usbClk.foreach(_ := mod_io_ckrst.usb_clk)

      // Async clock connections
      aclocks.foreach { aclocks =>
        println(s"Connecting async clocks by default =>")
        (aclocks zip namedclocks).foreach { case (aclk, nam) =>
          println(s"  Detected clock ${nam}")
          if(nam.contains("mbus")) {
            p(SbusToMbusXTypeKey) match {
              case _: AsynchronousCrossing =>
                aclk := mod_io_ckrst.io_clk
                println("    Connected to io_clk")
                mod_clock := mod_io_ckrst.io_clk
                mod_reset := reset_to_child
                println("    Quartus Island clock also connected to io_clk")
              case _ =>
                aclk := mod_io_ckrst.qsys_clk
                println("    Connected to qsys_clk")
            }
          }
          else {
            aclk := mod_io_ckrst.qsys_clk
            println("    Connected to qsys_clk")
          }
        }
      }

      // Legacy ChildClock
      if(p(DDRPortOther)) {
        println("[Legacy] Quartus Island and Child Clock connected to io_clk")
        ChildClock.foreach(_ := mod_io_ckrst.io_clk)
        ChildReset.foreach(_ := reset_to_child)
        mod_clock := mod_io_ckrst.io_clk
        mod_reset := reset_to_child
      }

      mod_io_ckrst.ddr_ref_clk := OSC_50_BANK1.asUInt()
      mod_io_ckrst.qsys_ref_clk := OSC_50_BANK4.asUInt() // TODO: This is okay?
      mod_io_ckrst.system_reset_n := BUTTON(2)
    }

    // Helper function to connect the DDR from the Quartus Platform
    def ConnectDDRUtil(mod_io_qport: QuartusIO) = {
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

    val ddrcfg = QuartusDDRConfig(size_ck = 1, is_reset = true)
    
    tlport.foreach { chiptl =>
      // Instance our converter, and connect everything
      val mod = Module(LazyModule(new TLULtoQuartusPlatform(chiptl.params, ddrcfg)).module)

      // Quartus Platform connections
      ConnectDDRUtil(mod.io.qport)

      // TileLink Interface from platform
      // TODO: Make the DDR optional. Need to stop using the Quartus Platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      mem_status_local_cal_fail := mod.io.qport.mem_status_local_cal_fail
      mem_status_local_cal_success := mod.io.qport.mem_status_local_cal_success
      mem_status_local_init_done := mod.io.qport.mem_status_local_init_done

      // Clock and reset (for TL stuff)
      ConnectClockUtil(mod.clock, mod.reset, mod.io.qport, mod.io.ckrst)
    }
    (memser zip memserSourceBits).foreach { case(ms, sourceBits) =>
      // Instance our converter, and connect everything
      val mod = Module(LazyModule(new SertoQuartusPlatform(ms.w, sourceBits, ddrcfg)).module)

      // Serial port
      mod.io.serport.flipConnect(ms)

      // Quartus Platform connections
      ConnectDDRUtil(mod.io.qport)

      mem_status_local_cal_fail := mod.io.qport.mem_status_local_cal_fail
      mem_status_local_cal_success := mod.io.qport.mem_status_local_cal_success
      mem_status_local_init_done := mod.io.qport.mem_status_local_init_done

      // Clock and reset (for TL stuff)
      ConnectClockUtil(mod.clock, mod.reset, mod.io.qport, mod.io.ckrst)
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

object ConnectHSMCGPIO {
  def apply (n: Int, pu: Int, c: Bool, get: Boolean, HSMC: HSMCTR4) = {
    val p:Int = pu match {
      case it if 1 to 10 contains it => pu - 1
      case it if 13 to 28 contains it => pu - 3
      case it if 31 to 40 contains it => pu - 5
      case _ => throw new RuntimeException(s"J${n}_${pu} is a VDD or a GND")
    }
    n match {
      case 0 =>
        p match {
          case 0 => if(get) c := HSMC.CLKIN_n2 else throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 1 => if(get) c := ALT_IOBUF(HSMC.RX_n(16)) else ALT_IOBUF(HSMC.RX_n(16), c)
          case 2 => if(get) c := HSMC.CLKIN_p2 else throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 3 => if(get) c := ALT_IOBUF(HSMC.RX_p(16)) else ALT_IOBUF(HSMC.RX_p(16), c)
          case 4 => if(get) c := ALT_IOBUF(HSMC.TX_n(16)) else ALT_IOBUF(HSMC.TX_n(16), c)
          case 5 => if(get) c := ALT_IOBUF(HSMC.RX_n(15)) else ALT_IOBUF(HSMC.TX_n(15), c)
          case 6 => if(get) c := ALT_IOBUF(HSMC.TX_p(16)) else ALT_IOBUF(HSMC.TX_p(16), c)
          case 7 => if(get) c := ALT_IOBUF(HSMC.RX_p(15)) else ALT_IOBUF(HSMC.TX_p(15), c)
          case 8 => if(get) c := ALT_IOBUF(HSMC.TX_n(15)) else ALT_IOBUF(HSMC.TX_n(15), c)
          case 9 => if(get) c := ALT_IOBUF(HSMC.RX_n(14)) else ALT_IOBUF(HSMC.TX_n(14), c)
          case 10 => if(get) c := ALT_IOBUF(HSMC.TX_p(15)) else ALT_IOBUF(HSMC.TX_p(15), c)
          case 11 => if(get) c := ALT_IOBUF(HSMC.RX_p(14)) else ALT_IOBUF(HSMC.TX_p(14), c)
          case 12 => if(get) c := ALT_IOBUF(HSMC.TX_n(14)) else ALT_IOBUF(HSMC.TX_n(14), c)
          case 13 => if(get) c := ALT_IOBUF(HSMC.RX_n(13)) else ALT_IOBUF(HSMC.TX_n(13), c)
          case 14 => if(get) c := ALT_IOBUF(HSMC.TX_p(14)) else ALT_IOBUF(HSMC.TX_p(14), c)
          case 15 => if(get) c := ALT_IOBUF(HSMC.RX_p(13)) else ALT_IOBUF(HSMC.TX_p(13), c)
          case 16 => if(get) c := ALT_IOBUF(HSMC.OUT_n2) else ALT_IOBUF(HSMC.OUT_n2, c)
          case 17 => if(get) c := ALT_IOBUF(HSMC.RX_n(12)) else ALT_IOBUF(HSMC.TX_n(12), c)
          case 18 => if(get) c := ALT_IOBUF(HSMC.OUT_p2) else ALT_IOBUF(HSMC.OUT_p2, c)
          case 19 => if(get) c := ALT_IOBUF(HSMC.RX_p(12)) else ALT_IOBUF(HSMC.TX_p(12), c)
          case 20 => if(get) c := ALT_IOBUF(HSMC.TX_n(13)) else ALT_IOBUF(HSMC.TX_n(13), c)
          case 21 => if(get) c := ALT_IOBUF(HSMC.RX_n(11)) else ALT_IOBUF(HSMC.TX_n(11), c)
          case 22 => if(get) c := ALT_IOBUF(HSMC.TX_p(13)) else ALT_IOBUF(HSMC.TX_p(13), c)
          case 23 => if(get) c := ALT_IOBUF(HSMC.RX_p(11)) else ALT_IOBUF(HSMC.TX_p(11), c)
          case 24 => if(get) c := ALT_IOBUF(HSMC.TX_n(12)) else ALT_IOBUF(HSMC.TX_n(12), c)
          case 25 => if(get) c := ALT_IOBUF(HSMC.RX_n(10)) else ALT_IOBUF(HSMC.TX_n(10), c)
          case 26 => if(get) c := ALT_IOBUF(HSMC.TX_p(12)) else ALT_IOBUF(HSMC.TX_p(12), c)
          case 27 => if(get) c := ALT_IOBUF(HSMC.RX_p(10)) else ALT_IOBUF(HSMC.TX_p(10), c)
          case 28 => if(get) c := ALT_IOBUF(HSMC.TX_n(11)) else ALT_IOBUF(HSMC.TX_n(11), c)
          case 29 => if(get) c := ALT_IOBUF(HSMC.RX_n(9)) else ALT_IOBUF(HSMC.TX_n(9), c)
          case 30 => if(get) c := ALT_IOBUF(HSMC.TX_p(11)) else ALT_IOBUF(HSMC.TX_p(11), c)
          case 31 => if(get) c := ALT_IOBUF(HSMC.RX_p(9)) else ALT_IOBUF(HSMC.TX_p(9), c)
          case 32 => if(get) c := ALT_IOBUF(HSMC.TX_n(10)) else ALT_IOBUF(HSMC.TX_n(10), c)
          case 33 => if(get) c := ALT_IOBUF(HSMC.TX_n(9)) else ALT_IOBUF(HSMC.TX_n(9), c)
          case 34 => if(get) c := ALT_IOBUF(HSMC.TX_p(10)) else ALT_IOBUF(HSMC.TX_p(10), c)
          case 35 => if(get) c := ALT_IOBUF(HSMC.TX_p(9)) else ALT_IOBUF(HSMC.TX_p(9), c)
          case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
        }
      case 1 =>
        p match {
          case 0 => if(get) c := HSMC.CLKIN_n1 else throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 1 => if(get) c := ALT_IOBUF(HSMC.RX_n(7)) else ALT_IOBUF(HSMC.RX_n(7), c)
          case 2 => if(get) c := HSMC.CLKIN_p1 else throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 3 => if(get) c := ALT_IOBUF(HSMC.RX_p(7)) else ALT_IOBUF(HSMC.RX_p(7), c)
          case 4 => if(get) c := ALT_IOBUF(HSMC.TX_n(7)) else ALT_IOBUF(HSMC.TX_n(7), c)
          case 5 => if(get) c := ALT_IOBUF(HSMC.RX_n(6)) else ALT_IOBUF(HSMC.TX_n(6), c)
          case 6 => if(get) c := ALT_IOBUF(HSMC.TX_p(7)) else ALT_IOBUF(HSMC.TX_p(7), c)
          case 7 => if(get) c := ALT_IOBUF(HSMC.RX_p(6)) else ALT_IOBUF(HSMC.TX_p(6), c)
          case 8 => if(get) c := ALT_IOBUF(HSMC.TX_n(6)) else ALT_IOBUF(HSMC.TX_n(6), c)
          case 9 => if(get) c := ALT_IOBUF(HSMC.RX_n(5)) else ALT_IOBUF(HSMC.TX_n(5), c)
          case 10 => if(get) c := ALT_IOBUF(HSMC.TX_p(6)) else ALT_IOBUF(HSMC.TX_p(6), c)
          case 11 => if(get) c := ALT_IOBUF(HSMC.RX_p(5)) else ALT_IOBUF(HSMC.TX_p(5), c)
          case 12 => if(get) c := ALT_IOBUF(HSMC.TX_n(5)) else ALT_IOBUF(HSMC.TX_n(5), c)
          case 13 => if(get) c := ALT_IOBUF(HSMC.RX_n(4)) else ALT_IOBUF(HSMC.TX_n(4), c)
          case 14 => if(get) c := ALT_IOBUF(HSMC.TX_p(5)) else ALT_IOBUF(HSMC.TX_p(5), c)
          case 15 => if(get) c := ALT_IOBUF(HSMC.RX_p(4)) else ALT_IOBUF(HSMC.TX_p(4), c)
          case 16 => if(get) c := ALT_IOBUF(HSMC.OUT_n1) else ALT_IOBUF(HSMC.OUT_n1, c)
          case 17 => if(get) c := ALT_IOBUF(HSMC.RX_n(3)) else ALT_IOBUF(HSMC.TX_n(3), c)
          case 18 => if(get) c := ALT_IOBUF(HSMC.OUT_p1) else ALT_IOBUF(HSMC.OUT_p1, c)
          case 19 => if(get) c := ALT_IOBUF(HSMC.RX_p(3)) else ALT_IOBUF(HSMC.TX_p(3), c)
          case 20 => if(get) c := ALT_IOBUF(HSMC.TX_n(4)) else ALT_IOBUF(HSMC.TX_n(4), c)
          case 21 => if(get) c := ALT_IOBUF(HSMC.RX_n(2)) else ALT_IOBUF(HSMC.TX_n(2), c)
          case 22 => if(get) c := ALT_IOBUF(HSMC.TX_p(4)) else ALT_IOBUF(HSMC.TX_p(4), c)
          case 23 => if(get) c := ALT_IOBUF(HSMC.RX_p(2)) else ALT_IOBUF(HSMC.TX_p(2), c)
          case 24 => if(get) c := ALT_IOBUF(HSMC.TX_n(3)) else ALT_IOBUF(HSMC.TX_n(3), c)
          case 25 => if(get) c := ALT_IOBUF(HSMC.RX_n(1)) else ALT_IOBUF(HSMC.TX_n(1), c)
          case 26 => if(get) c := ALT_IOBUF(HSMC.TX_p(3)) else ALT_IOBUF(HSMC.TX_p(3), c)
          case 27 => if(get) c := ALT_IOBUF(HSMC.RX_p(1)) else ALT_IOBUF(HSMC.TX_p(1), c)
          case 28 => if(get) c := ALT_IOBUF(HSMC.TX_n(2)) else ALT_IOBUF(HSMC.TX_n(2), c)
          case 29 => if(get) c := ALT_IOBUF(HSMC.RX_n(0)) else ALT_IOBUF(HSMC.TX_n(0), c)
          case 30 => if(get) c := ALT_IOBUF(HSMC.TX_p(2)) else ALT_IOBUF(HSMC.TX_p(2), c)
          case 31 => if(get) c := ALT_IOBUF(HSMC.RX_p(0)) else ALT_IOBUF(HSMC.TX_p(0), c)
          case 32 => if(get) c := ALT_IOBUF(HSMC.TX_n(1)) else ALT_IOBUF(HSMC.TX_n(1), c)
          case 33 => if(get) c := ALT_IOBUF(HSMC.TX_n(0)) else ALT_IOBUF(HSMC.TX_n(0), c)
          case 34 => if(get) c := ALT_IOBUF(HSMC.TX_p(1)) else ALT_IOBUF(HSMC.TX_p(1), c)
          case 35 => if(get) c := ALT_IOBUF(HSMC.TX_p(0)) else ALT_IOBUF(HSMC.TX_p(0), c)
          case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
        }
      case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
    }
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
  def JP18 = 1 // GPIO1 (J3)
  def JP19 = 0 // GPIO0 (J2)
  def JP20 = 0 // GPIO0 (J2)
  def JP21 = 1 // GPIO1 (J3)

  // From intern = Clocks and resets
  intern.ChildClock.foreach{ a =>
    ConnectHSMCGPIO(JP21, 2, a.asBool(), false, HSMC_JP20_21)
  }
  intern.ChildReset.foreach{ a =>
    ConnectHSMCGPIO(JP18, 5, a, false, HSMC_JP19_18)
  }
  ConnectHSMCGPIO(JP21, 4, intern.sys_clk.asBool(), false, HSMC_JP20_21)
  ConnectHSMCGPIO(JP18, 2, intern.rst_n, false, HSMC_JP19_18)
  ConnectHSMCGPIO(JP18, 6, intern.jrst_n, false, HSMC_JP19_18)
  // Memory port serialized
  intern.memser.foreach{ a => a } // NOTHING
  // Ext port serialized
  intern.extser.foreach{ a => a } // NOTHING
  // Memory port
  intern.tlport.foreach{ case tlport =>
    ConnectHSMCGPIO(JP18, 1, tlport.a.valid, true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 4, tlport.a.ready, false, HSMC_JP19_18)
    require(tlport.a.bits.opcode.getWidth == 3, s"${tlport.a.bits.opcode.getWidth}")
    val a_opcode = Wire(Vec(3, Bool()))
    ConnectHSMCGPIO(JP18, 3, a_opcode(2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 7, a_opcode(1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 8, a_opcode(0), true, HSMC_JP19_18)
    tlport.a.bits.opcode := a_opcode.asUInt()
    require(tlport.a.bits.param.getWidth == 3, s"${tlport.a.bits.param.getWidth}")
    val a_param = Wire(Vec(3, Bool()))
    ConnectHSMCGPIO(JP18, 9, a_param(2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 10, a_param(1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 13, a_param(0), true, HSMC_JP19_18)
    tlport.a.bits.param := a_param.asUInt()
    val a_size = Wire(Vec(3, Bool()))
    require(tlport.a.bits.size.getWidth == 3, s"${tlport.a.bits.size.getWidth}")
    ConnectHSMCGPIO(JP18, 14, a_size(2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 15, a_size(1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 16, a_size(0), true, HSMC_JP19_18)
    tlport.a.bits.size := a_size.asUInt()
    require(tlport.a.bits.source.getWidth == 6, s"${tlport.a.bits.source.getWidth}")
    val a_source = Wire(Vec(6, Bool()))
    ConnectHSMCGPIO(JP18, 17, a_source(5), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 18, a_source(4), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 19, a_source(3), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 20, a_source(2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 21, a_source(1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 22, a_source(0), true, HSMC_JP19_18)
    tlport.a.bits.source := a_source.asUInt()
    require(tlport.a.bits.address.getWidth == 32, s"${tlport.a.bits.address.getWidth}")
    val a_address = Wire(Vec(32, Bool()))
    ConnectHSMCGPIO(JP18, 23, a_address(31), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 24, a_address(30), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 25, a_address(29), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 26, a_address(28), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 27, a_address(27), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 28, a_address(26), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 31, a_address(25), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 32, a_address(24), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 33, a_address(23), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 34, a_address(22), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 35, a_address(21), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 36, a_address(20), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 37, a_address(19), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 38, a_address(18), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 39, a_address(17), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 40, a_address(16), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19,  1, a_address(15), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19,  2, a_address(14), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19,  2, a_address(13), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19,  4, a_address(12), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19,  5, a_address(11), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19,  6, a_address(10), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19,  7, a_address( 9), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19,  8, a_address( 8), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19,  9, a_address( 7), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 10, a_address( 6), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 13, a_address( 5), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 14, a_address( 4), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 15, a_address( 3), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 16, a_address( 2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 17, a_address( 1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 18, a_address( 0), true, HSMC_JP19_18)
    tlport.a.bits.address := a_address.asUInt()
    require(tlport.a.bits.mask.getWidth == 4, s"${tlport.a.bits.mask.getWidth}")
    val a_mask = Wire(Vec(4, Bool()))
    ConnectHSMCGPIO(JP19, 19, a_mask(3), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 20, a_mask(2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 21, a_mask(1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 22, a_mask(0), true, HSMC_JP19_18)
    tlport.a.bits.mask := a_mask.asUInt()
    require(tlport.a.bits.data.getWidth == 32, s"${tlport.a.bits.data.getWidth}")
    val a_data = Wire(Vec(32, Bool()))
    ConnectHSMCGPIO(JP19, 23, a_data(31), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 24, a_data(30), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 25, a_data(29), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 26, a_data(28), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 27, a_data(27), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 28, a_data(26), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 31, a_data(25), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 32, a_data(24), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 33, a_data(23), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 34, a_data(22), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 35, a_data(21), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 36, a_data(20), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 37, a_data(19), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 38, a_data(18), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 39, a_data(17), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 40, a_data(16), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP20, 10, a_data(15), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20,  9, a_data(14), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20,  8, a_data(13), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20,  7, a_data(12), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20,  6, a_data(11), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20,  5, a_data(10), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20,  4, a_data( 9), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20,  3, a_data( 8), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20,  2, a_data( 7), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20,  1, a_data( 6), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 13, a_data( 5), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 14, a_data( 4), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 15, a_data( 3), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 16, a_data( 2), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 17, a_data( 1), true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 18, a_data( 0), true, HSMC_JP20_21)
    tlport.a.bits.data := a_data.asUInt()
    ConnectHSMCGPIO(JP20, 19, tlport.a.bits.corrupt, true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 20, tlport.d.ready, true, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 21, tlport.d.valid, false, HSMC_JP20_21)
    require(tlport.d.bits.opcode.getWidth == 3, s"${tlport.d.bits.opcode.getWidth}")
    ConnectHSMCGPIO(JP20, 22, tlport.d.bits.opcode(2), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 23, tlport.d.bits.opcode(1), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 24, tlport.d.bits.opcode(0), false, HSMC_JP20_21)
    require(tlport.d.bits.param.getWidth == 2, s"${tlport.d.bits.param.getWidth}")
    ConnectHSMCGPIO(JP20, 25, tlport.d.bits.param(1), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 26, tlport.d.bits.param(0), false, HSMC_JP20_21)
    require(tlport.d.bits.size.getWidth == 3, s"${tlport.d.bits.size.getWidth}")
    ConnectHSMCGPIO(JP20, 27, tlport.d.bits.size(2), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 28, tlport.d.bits.size(1), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 31, tlport.d.bits.size(0), false, HSMC_JP20_21)
    require(tlport.d.bits.source.getWidth == 6, s"${tlport.d.bits.source.getWidth}")
    ConnectHSMCGPIO(JP20, 32, tlport.d.bits.source(5), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 33, tlport.d.bits.source(4), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 34, tlport.d.bits.source(3), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 35, tlport.d.bits.source(2), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 36, tlport.d.bits.source(1), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 37, tlport.d.bits.source(0), false, HSMC_JP20_21)
    require(tlport.d.bits.sink.getWidth == 1, s"${tlport.d.bits.sink.getWidth}")
    ConnectHSMCGPIO(JP20, 38, tlport.d.bits.sink(0), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 39, tlport.d.bits.denied, false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP20, 40, tlport.d.bits.corrupt, false, HSMC_JP20_21)
    require(tlport.d.bits.data.getWidth == 32, s"${tlport.d.bits.data.getWidth}")
    ConnectHSMCGPIO(JP21, 5, tlport.d.bits.data(31), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 6, tlport.d.bits.data(30), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 7, tlport.d.bits.data(29), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 8, tlport.d.bits.data(28), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 9, tlport.d.bits.data(27), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 10, tlport.d.bits.data(26), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 13, tlport.d.bits.data(25), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 14, tlport.d.bits.data(24), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 15, tlport.d.bits.data(23), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 16, tlport.d.bits.data(22), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 17, tlport.d.bits.data(21), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 18, tlport.d.bits.data(20), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 19, tlport.d.bits.data(19), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 20, tlport.d.bits.data(18), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 21, tlport.d.bits.data(17), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 22, tlport.d.bits.data(16), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 23, tlport.d.bits.data(15), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 24, tlport.d.bits.data(14), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 25, tlport.d.bits.data(13), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 26, tlport.d.bits.data(12), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 27, tlport.d.bits.data(11), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 28, tlport.d.bits.data(10), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 31, tlport.d.bits.data( 9), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 32, tlport.d.bits.data( 8), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 33, tlport.d.bits.data( 7), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 34, tlport.d.bits.data( 6), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 35, tlport.d.bits.data( 5), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 36, tlport.d.bits.data( 4), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 37, tlport.d.bits.data( 3), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 38, tlport.d.bits.data( 2), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 39, tlport.d.bits.data( 1), false, HSMC_JP20_21)
    ConnectHSMCGPIO(JP21, 40, tlport.d.bits.data( 0), false, HSMC_JP20_21)
  }

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