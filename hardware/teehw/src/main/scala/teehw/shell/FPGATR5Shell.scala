package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing, LazyModule}
import freechips.rocketchip.util._
import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import uec.teehardware.macros._
import uec.teehardware._

class FMCTR5(val ext: Boolean = false, val xcvr: Boolean = false) extends Bundle {
  val CLK_M2C_p = Vec(2, Analog(1.W))
  val CLK_M2C_n = Vec(2, Analog(1.W))
  val HA_RX_CLK_p = ext.option(Analog(1.W))
  val HA_RX_CLK_n = ext.option(Analog(1.W))
  val HB_RX_CLK_p = ext.option(Analog(1.W))
  val HB_RX_CLK_n = ext.option(Analog(1.W))
  val LA_RX_CLK_p = Analog(1.W)
  val LA_RX_CLK_n = Analog(1.W)
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
  val LA_TX_p = Vec(17, Analog(1.W))
  val LA_TX_n = Vec(17, Analog(1.W))
  val LA_RX_p = Vec(15, Analog(1.W))
  val LA_RX_n = Vec(15, Analog(1.W))

  val GBTCLK_M2C_p = xcvr.option(Vec(2, Input(Bool())))
  val ONBOARD_REFCLK_p = xcvr.option(Vec(2, Input(Bool())))
  val DP_C2M_p = xcvr.option(Vec(10, Output(Bool())))
  DP_C2M_p.foreach(_.foreach(_ := false.B))
  val DP_M2C_p = xcvr.option(Vec(10, Input(Bool())))

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
  def memEnable: Boolean = true
  val DDR3_REFCLK_p = memEnable.option(IO(Input(Clock())))
  val DDR3_A = memEnable.option(IO(Output(Bits((15 + 1).W))))
  val DDR3_BA = memEnable.option(IO(Output(Bits((2 + 1).W))))
  val DDR3_CK = memEnable.option(IO(Output(Bits((1 + 1).W))))
  val DDR3_CK_n = memEnable.option(IO(Output(Bits((1 + 1).W))))
  val DDR3_CKE = memEnable.option(IO(Output(Bits((1 + 1).W))))
  val DDR3_DQS = memEnable.option(IO(Analog((7 + 1).W)))
  val DDR3_DQS_n = memEnable.option(IO(Analog((7 + 1).W)))
  val DDR3_DQ = memEnable.option(IO(Analog((63 + 1).W)))
  val DDR3_DM = memEnable.option(IO(Output(Bits((7 + 1).W))))
  val DDR3_CS_n = memEnable.option(IO(Output(Bits((1 + 1).W))))
  val DDR3_WE_n = memEnable.option(IO(Output(Bool())))
  val DDR3_RAS_n = memEnable.option(IO(Output(Bool())))
  val DDR3_CAS_n = memEnable.option(IO(Output(Bool())))
  val DDR3_RESET_n = memEnable.option(IO(Output(Bool())))
  val DDR3_ODT = memEnable.option(IO(Output(Bits((1 + 1).W))))
  val DDR3_EVENT_n = memEnable.option(IO(Input(Bool())))
  //val DDR3_SCL = memEnable.option(IO(Analog(1.W)))
  //val DDR3_SDA_n = memEnable.option(IO(Analog(1.W)))
  val RZQ_DDR3 = memEnable.option(IO(Input(Bool())))

  val SMA_CLKIN_p = IO(Input(Clock()))
  val SMA_CLKIN_n = IO(Input(Clock()))
  val SMA_CLKOUT_p = IO(Analog(1.W))
  val SMA_CLKOUT_n = IO(Analog(1.W))
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

      mod_io_ckrst.ddr_ref_clk := OSC_50_B3B.asUInt()
      mod_io_ckrst.qsys_ref_clk := OSC_50_B4A.asUInt() // TODO: This is okay?
      mod_io_ckrst.system_reset_n := BUTTON(2)
    }

    // Helper function to connect the DDR from the Quartus Platform
    def ConnectDDRUtil(mod_io_qport: QuartusIO) = {
      DDR3_A.foreach(_ := mod_io_qport.memory_mem_a)
      DDR3_BA.foreach(_ := mod_io_qport.memory_mem_ba)
      DDR3_CK.foreach(_ := mod_io_qport.memory_mem_ck)
      DDR3_CK_n.foreach(_ := mod_io_qport.memory_mem_ck_n)
      DDR3_CKE.foreach(_ := mod_io_qport.memory_mem_cke)
      DDR3_CS_n.foreach(_ := mod_io_qport.memory_mem_cs_n)
      DDR3_DM.foreach(_ := mod_io_qport.memory_mem_dm)
      DDR3_RAS_n.foreach(_ := mod_io_qport.memory_mem_ras_n)
      DDR3_CAS_n.foreach(_ := mod_io_qport.memory_mem_cas_n)
      DDR3_WE_n.foreach(_ := mod_io_qport.memory_mem_we_n)
      DDR3_DQ.foreach(attach(_, mod_io_qport.memory_mem_dq))
      DDR3_DQS.foreach(attach(_, mod_io_qport.memory_mem_dqs))
      DDR3_DQS_n.foreach(attach(_, mod_io_qport.memory_mem_dqs_n))
      DDR3_ODT.foreach(_ := mod_io_qport.memory_mem_odt)
      DDR3_RESET_n.foreach(_ := mod_io_qport.memory_mem_reset_n.getOrElse(true.B))
      (mod_io_qport.oct.rzqin zip RZQ_DDR3).foreach{case (a,b) => a := b}
    }

    val ddrcfg = QuartusDDRConfig(
      size_ck = 2,
      is_reset = true,
      size_cke = 2,
      size_csn = 2,
      size_odt = 2,
      addrbit = 16,
      octmode = 1,
      size = 0x80000000L)

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
      val mod = Module(LazyModule(new FPGAMiniSystemDummy(sourceBits)).module)

      // Serial port
      mod.serport.flipConnect(es)
    }
  }
}

class FPGATR5InternalNoChip
(
  val idBits: Int = 6,
  val widthBits: Int = 32,
  val sinkBits: Int = 1
)(implicit p :Parameters) extends FPGATR5Internal(None)(p) {
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

trait WithFPGATR5InternCreate {
  this: FPGATR5Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGATR5Internal(Some(chip)))
}

trait WithFPGATR5InternNoChipCreate {
  this: FPGATR5Shell =>
  def idBits = 6
  def widthBits = 32
  def sinkBits = 1
  val intern = Module(new FPGATR5InternalNoChip(idBits, widthBits, sinkBits))
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
  intern.SMA_CLKIN_n := SMA_CLKIN_n
  attach(SMA_CLKOUT_p, intern.SMA_CLKOUT_p)
  attach(SMA_CLKOUT_n, intern.SMA_CLKOUT_n)

  (DDR3_REFCLK_p zip intern.DDR3_REFCLK_p).foreach{case (a,b) => b := a} // TODO: Not actually used
  (DDR3_EVENT_n zip intern.DDR3_EVENT_n).foreach{case (a,b) => b := a} // TODO: Not actually used
  (DDR3_A zip intern.DDR3_A).foreach{case (a,b) => a := b}
  (DDR3_BA zip intern.DDR3_BA).foreach{case (a,b) => a := b}
  (DDR3_CK zip intern.DDR3_CK).foreach{case (a,b) => a := b}
  (DDR3_CK_n zip intern.DDR3_CK_n).foreach{case (a,b) => a := b}
  (DDR3_CKE zip intern.DDR3_CKE).foreach{case (a,b) => a := b}
  (DDR3_CS_n zip intern.DDR3_CS_n).foreach{case (a,b) => a := b}
  (DDR3_DM zip intern.DDR3_DM).foreach{case (a,b) => a := b}
  (DDR3_RAS_n zip intern.DDR3_RAS_n).foreach{case (a,b) => a := b}
  (DDR3_CAS_n zip intern.DDR3_CAS_n).foreach{case (a,b) => a := b}
  (DDR3_WE_n zip intern.DDR3_WE_n).foreach{case (a,b) => a := b}
  (DDR3_DQ zip intern.DDR3_DQ).foreach{case (a,b) => attach(a,b)}
  (DDR3_DQS zip intern.DDR3_DQS).foreach{case (a,b) => attach(a,b)}
  (DDR3_DQS_n zip intern.DDR3_DQS_n).foreach{case (a,b) => attach(a,b)}
  (DDR3_ODT zip intern.DDR3_ODT).foreach{case (a,b) => a := b}
  (DDR3_RESET_n zip intern.DDR3_RESET_n).foreach{case (a,b) => a := b}
  (RZQ_DDR3 zip intern.RZQ_DDR3).foreach{case (a,b) => b := a}
}

trait WithFPGATR5PureConnect {
  this: FPGATR5Shell =>
  val chip: WithTEEHWbaseShell with WithTEEHWbaseConnect

  def namedclocks: Seq[String] = chip.system.sys.asInstanceOf[HasTEEHWSystemModule].namedclocks
  // This trait connects the chip to all essentials. This assumes no DDR is connected yet
  LED := Cat(chip.gpio_out, BUTTON(2))
  chip.gpio_in := Cat(BUTTON(3), BUTTON(1,0), SW(1,0))
  chip.jtag.jtag_TDI := ALT_IOBUF(GPIO(4))
  chip.jtag.jtag_TMS := ALT_IOBUF(GPIO(6))
  chip.jtag.jtag_TCK := ALT_IOBUF(GPIO(8))
  ALT_IOBUF(GPIO(10), chip.jtag.jtag_TDO)
  chip.uart_rxd := ALT_IOBUF(UART_RX)
  ALT_IOBUF(UART_TX, chip.uart_txd) // UART_RXD
  SD_CLK := chip.sdio.sdio_clk
  chip.sdio.sdio_dat_0 := ALT_IOBUF(SD_DATA(0))
  ALT_IOBUF(SD_DATA(3), chip.sdio.sdio_dat_3)
  ALT_IOBUF(SD_CMD, chip.sdio.sdio_cmd)

  // USB phy connections
  chip.usb11hs.foreach{ case chipport =>
    ALT_IOBUF(GPIO(17), chipport.USBFullSpeed)
    chipport.USBWireDataIn := Cat(ALT_IOBUF(GPIO(24)), ALT_IOBUF(GPIO(26)))
    ALT_IOBUF(GPIO(28), chipport.USBWireCtrlOut)
    ALT_IOBUF(GPIO(16), chipport.USBWireDataOut(0))
    ALT_IOBUF(GPIO(18), chipport.USBWireDataOut(1))
  }
  
  chip.qspi.foreach{A =>
    A.qspi_miso := ALT_IOBUF(GPIO(1))
    ALT_IOBUF(GPIO(3), A.qspi_mosi)
    ALT_IOBUF(GPIO(5), A.qspi_cs(0))
    ALT_IOBUF(GPIO(7), A.qspi_sck)
  }
}

trait WithFPGATR5Connect extends WithFPGATR5PureConnect 
  with WithFPGATR5InternCreate 
  with WithFPGATR5InternConnect {
  this: FPGATR5Shell =>

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // The rest of the platform connections
  LED := Cat(
    intern.mem_status_local_cal_fail,
    intern.mem_status_local_cal_success,
    intern.mem_status_local_init_done,
    BUTTON(2)
  )
}

object ConnectFMCGPIO {
  def apply (n: Int, pu: Int, c: Bool, get: Boolean, FMC: FMCTR5) = {
    val p:Int = pu match {
      case it if 1 to 10 contains it => pu - 1
      case it if 13 to 28 contains it => pu - 3
      case it if 31 to 40 contains it => pu - 5
      case _ => throw new RuntimeException(s"J${n}_${pu} is a VDD or a GND")
    }
    n match {
      case 0 =>
        p match {
          case 0 => if(get) c := GET(FMC.CLK_M2C_p(0)) else PUT(c, FMC.CLK_M2C_p(0)) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 1 => if(get) c := GET(FMC.CLK_M2C_n(0)) else PUT(c, FMC.CLK_M2C_n(0)) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 2 => if(get) c := ALT_IOBUF(FMC.LA_TX_CLK_p) else ALT_IOBUF(FMC.LA_TX_CLK_p, c) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 3 => if(get) c := ALT_IOBUF(FMC.LA_TX_CLK_n) else ALT_IOBUF(FMC.LA_TX_CLK_n, c) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 4 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(5)) else ALT_IOBUF(FMC.LA_TX_p(5), c)
          case 5 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(5)) else ALT_IOBUF(FMC.LA_TX_n(5), c)
          case 6 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(7)) else ALT_IOBUF(FMC.LA_TX_p(7), c)
          case 7 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(7)) else ALT_IOBUF(FMC.LA_TX_n(7), c)
          case 8 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(4)) else ALT_IOBUF(FMC.LA_TX_p(4), c)
          case 9 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(4)) else ALT_IOBUF(FMC.LA_TX_n(4), c)
          case 10 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(6)) else ALT_IOBUF(FMC.LA_TX_p(6), c)
          case 11 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(6)) else ALT_IOBUF(FMC.LA_TX_n(6), c)
          case 12 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(8)) else ALT_IOBUF(FMC.LA_TX_p(8), c)
          case 13 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(8)) else ALT_IOBUF(FMC.LA_TX_n(8), c)
          case 14 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(9)) else ALT_IOBUF(FMC.LA_TX_p(9), c)
          case 15 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(9)) else ALT_IOBUF(FMC.LA_TX_n(9), c)
          case 16 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(10)) else ALT_IOBUF(FMC.LA_TX_p(10), c)
          case 17 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(10)) else ALT_IOBUF(FMC.LA_TX_n(10), c)
          case 18 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(11)) else ALT_IOBUF(FMC.LA_TX_p(11), c)
          case 19 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(11)) else ALT_IOBUF(FMC.LA_TX_n(11), c)
          case 20 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(3)) else ALT_IOBUF(FMC.LA_TX_p(3), c)
          case 21 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(3)) else ALT_IOBUF(FMC.LA_TX_n(3), c)
          case 22 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(1)) else ALT_IOBUF(FMC.LA_TX_p(1), c)
          case 23 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(1)) else ALT_IOBUF(FMC.LA_TX_n(1), c)
          case 24 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(0)) else ALT_IOBUF(FMC.LA_TX_p(0), c)
          case 25 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(0)) else ALT_IOBUF(FMC.LA_TX_n(0), c)
          case 26 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(2)) else ALT_IOBUF(FMC.LA_TX_p(2), c)
          case 27 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(2)) else ALT_IOBUF(FMC.LA_TX_n(2), c)
          case 28 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(12)) else ALT_IOBUF(FMC.LA_TX_p(12), c)
          case 29 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(12)) else ALT_IOBUF(FMC.LA_TX_n(12), c)
          case 30 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(13)) else ALT_IOBUF(FMC.LA_TX_p(13), c)
          case 31 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(13)) else ALT_IOBUF(FMC.LA_TX_n(13), c)
          case 32 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(14)) else ALT_IOBUF(FMC.LA_TX_p(14), c)
          case 33 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(14)) else ALT_IOBUF(FMC.LA_TX_n(14), c)
          case 34 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(15)) else ALT_IOBUF(FMC.LA_TX_p(15), c)
          case 35 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(15)) else ALT_IOBUF(FMC.LA_TX_n(15), c)
          case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
        }
      case 1 =>
        p match {
          case 0 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(4)) else ALT_IOBUF(FMC.LA_RX_p(4), c)
          case 1 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(4)) else ALT_IOBUF(FMC.LA_RX_n(4), c)
          case 2 => if(get) c := ALT_IOBUF(FMC.LA_RX_CLK_p) else ALT_IOBUF(FMC.LA_RX_CLK_p, c) //throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 3 => if(get) c := ALT_IOBUF(FMC.LA_RX_CLK_n) else ALT_IOBUF(FMC.LA_RX_CLK_n, c) //throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 4 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(2)) else ALT_IOBUF(FMC.LA_RX_p(2), c)
          case 5 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(2)) else ALT_IOBUF(FMC.LA_RX_n(2), c)
          case 6 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(0)) else ALT_IOBUF(FMC.LA_RX_p(0), c)
          case 7 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(0)) else ALT_IOBUF(FMC.LA_RX_n(0), c)
          case 8 => if(get) c := ALT_IOBUF(FMC.HA_RX_p.get(8)) else ALT_IOBUF(FMC.HA_RX_p.get(8), c)
          case 9 => if(get) c := ALT_IOBUF(FMC.HA_RX_n.get(8)) else ALT_IOBUF(FMC.HA_RX_n.get(8), c)
          case 10 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(1)) else ALT_IOBUF(FMC.LA_RX_p(1), c)
          case 11 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(1)) else ALT_IOBUF(FMC.LA_RX_n(1), c)
          case 12 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(3)) else ALT_IOBUF(FMC.LA_RX_p(3), c)
          case 13 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(3)) else ALT_IOBUF(FMC.LA_RX_n(3), c)
          case 14 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(6)) else ALT_IOBUF(FMC.LA_RX_p(6), c)
          case 15 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(6)) else ALT_IOBUF(FMC.LA_RX_n(6), c)
          case 16 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(5)) else ALT_IOBUF(FMC.LA_RX_p(5), c)
          case 17 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(5)) else ALT_IOBUF(FMC.LA_RX_n(5), c)
          case 18 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(8)) else ALT_IOBUF(FMC.LA_RX_p(8), c)
          case 19 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(8)) else ALT_IOBUF(FMC.LA_RX_n(8), c)
          case 20 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(7)) else ALT_IOBUF(FMC.LA_RX_p(7), c)
          case 21 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(7)) else ALT_IOBUF(FMC.LA_RX_n(7), c)
          case 22 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(9)) else ALT_IOBUF(FMC.LA_RX_p(9), c)
          case 23 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(9)) else ALT_IOBUF(FMC.LA_RX_n(9), c)
          case 24 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(10)) else ALT_IOBUF(FMC.LA_RX_p(10), c)
          case 25 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(10)) else ALT_IOBUF(FMC.LA_RX_n(10), c)
          case 26 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(11)) else ALT_IOBUF(FMC.LA_RX_p(11), c)
          case 27 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(11)) else ALT_IOBUF(FMC.LA_RX_n(11), c)
          case 28 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(12)) else ALT_IOBUF(FMC.LA_RX_p(12), c)
          case 29 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(12)) else ALT_IOBUF(FMC.LA_RX_n(12), c)
          case 30 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(13)) else ALT_IOBUF(FMC.LA_RX_p(13), c)
          case 31 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(13)) else ALT_IOBUF(FMC.LA_RX_n(13), c)
          case 32 => if(get) c := ALT_IOBUF(FMC.LA_RX_p(14)) else ALT_IOBUF(FMC.LA_RX_p(14), c)
          case 33 => if(get) c := ALT_IOBUF(FMC.LA_RX_n(14)) else ALT_IOBUF(FMC.LA_RX_n(14), c)
          case 34 => if(get) c := ALT_IOBUF(FMC.LA_TX_p(16)) else ALT_IOBUF(FMC.LA_TX_p(16), c)
          case 35 => if(get) c := ALT_IOBUF(FMC.LA_TX_n(16)) else ALT_IOBUF(FMC.LA_TX_n(16), c)
          case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
        }
      case 2 =>
        p match {
          case 0 => if(get) c := ALT_IOBUF(FMC.HA_RX_CLK_p.get) else ALT_IOBUF(FMC.HA_RX_CLK_p.get, c) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 1 => if(get) c := ALT_IOBUF(FMC.HA_RX_CLK_n.get) else ALT_IOBUF(FMC.HA_RX_CLK_n.get, c) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 2 => if(get) c := ALT_IOBUF(FMC.HA_TX_CLK_p.get) else ALT_IOBUF(FMC.HA_TX_CLK_p.get, c) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 3 => if(get) c := ALT_IOBUF(FMC.HA_TX_CLK_n.get) else ALT_IOBUF(FMC.HA_TX_CLK_n.get, c) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 4 => if(get) c := ALT_IOBUF(FMC.HA_RX_p.get(0)) else ALT_IOBUF(FMC.HA_RX_p.get(0), c)
          case 5 => if(get) c := ALT_IOBUF(FMC.HA_RX_n.get(0)) else ALT_IOBUF(FMC.HA_RX_n.get(0), c)
          case 6 => if(get) c := ALT_IOBUF(FMC.HA_RX_p.get(1)) else ALT_IOBUF(FMC.HA_RX_p.get(1), c)
          case 7 => if(get) c := ALT_IOBUF(FMC.HA_RX_n.get(1)) else ALT_IOBUF(FMC.HA_RX_n.get(1), c)
          case 8 => if(get) c := ALT_IOBUF(FMC.HA_RX_p.get(2)) else ALT_IOBUF(FMC.HA_RX_p.get(2), c)
          case 9 => if(get) c := ALT_IOBUF(FMC.HA_RX_n.get(2)) else ALT_IOBUF(FMC.HA_RX_n.get(2), c)
          case 10 => if(get) c := ALT_IOBUF(FMC.HA_RX_p.get(3)) else ALT_IOBUF(FMC.HA_RX_p.get(3), c)
          case 11 => if(get) c := ALT_IOBUF(FMC.HA_RX_n.get(3)) else ALT_IOBUF(FMC.HA_RX_n.get(3), c)
          case 12 => if(get) c := ALT_IOBUF(FMC.HA_RX_p.get(4)) else ALT_IOBUF(FMC.HA_RX_p.get(4), c)
          case 13 => if(get) c := ALT_IOBUF(FMC.HA_RX_n.get(4)) else ALT_IOBUF(FMC.HA_RX_n.get(4), c)
          case 14 => if(get) c := ALT_IOBUF(FMC.HA_RX_p.get(5)) else ALT_IOBUF(FMC.HA_RX_p.get(5), c)
          case 15 => if(get) c := ALT_IOBUF(FMC.HA_RX_n.get(5)) else ALT_IOBUF(FMC.HA_RX_n.get(5), c)
          case 16 => if(get) c := ALT_IOBUF(FMC.HA_RX_p.get(6)) else ALT_IOBUF(FMC.HA_RX_p.get(6), c)
          case 17 => if(get) c := ALT_IOBUF(FMC.HA_RX_n.get(6)) else ALT_IOBUF(FMC.HA_RX_n.get(6), c)
          case 18 => if(get) c := ALT_IOBUF(FMC.HA_RX_p.get(7)) else ALT_IOBUF(FMC.HA_RX_p.get(7), c)
          case 19 => if(get) c := ALT_IOBUF(FMC.HA_RX_n.get(7)) else ALT_IOBUF(FMC.HA_RX_n.get(7), c)
          case 20 => if(get) c := ALT_IOBUF(FMC.HA_TX_p.get(0)) else ALT_IOBUF(FMC.HA_TX_p.get(0), c)
          case 21 => if(get) c := ALT_IOBUF(FMC.HA_TX_n.get(0)) else ALT_IOBUF(FMC.HA_TX_n.get(0), c)
          case 22 => if(get) c := ALT_IOBUF(FMC.HA_TX_p.get(1)) else ALT_IOBUF(FMC.HA_TX_p.get(1), c)
          case 23 => if(get) c := ALT_IOBUF(FMC.HA_TX_n.get(1)) else ALT_IOBUF(FMC.HA_TX_n.get(1), c)
          case 24 => if(get) c := ALT_IOBUF(FMC.HA_TX_p.get(2)) else ALT_IOBUF(FMC.HA_TX_p.get(2), c)
          case 25 => if(get) c := ALT_IOBUF(FMC.HA_TX_n.get(2)) else ALT_IOBUF(FMC.HA_TX_n.get(2), c)
          case 26 => if(get) c := ALT_IOBUF(FMC.HA_TX_p.get(3)) else ALT_IOBUF(FMC.HA_TX_p.get(3), c)
          case 27 => if(get) c := ALT_IOBUF(FMC.HA_TX_n.get(3)) else ALT_IOBUF(FMC.HA_TX_n.get(3), c)
          case 28 => if(get) c := ALT_IOBUF(FMC.HA_TX_p.get(4)) else ALT_IOBUF(FMC.HA_TX_p.get(4), c)
          case 29 => if(get) c := ALT_IOBUF(FMC.HA_TX_n.get(4)) else ALT_IOBUF(FMC.HA_TX_n.get(4), c)
          case 30 => if(get) c := ALT_IOBUF(FMC.HA_TX_p.get(5)) else ALT_IOBUF(FMC.HA_TX_p.get(5), c)
          case 31 => if(get) c := ALT_IOBUF(FMC.HA_TX_n.get(5)) else ALT_IOBUF(FMC.HA_TX_n.get(5), c)
          case 32 => if(get) c := ALT_IOBUF(FMC.HA_TX_p.get(6)) else ALT_IOBUF(FMC.HA_TX_p.get(6), c)
          case 33 => if(get) c := ALT_IOBUF(FMC.HA_TX_n.get(6)) else ALT_IOBUF(FMC.HA_TX_n.get(6), c)
          case 34 => if(get) c := ALT_IOBUF(FMC.HA_TX_p.get(7)) else ALT_IOBUF(FMC.HA_TX_p.get(7), c)
          case 35 => if(get) c := ALT_IOBUF(FMC.HA_TX_n.get(7)) else ALT_IOBUF(FMC.HA_TX_n.get(7), c)
          case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
        }
      case 3 =>
        p match {
          case 0 => if(get) c := ALT_IOBUF(FMC.HB_RX_CLK_p.get) else ALT_IOBUF(FMC.HB_RX_CLK_p.get, c) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 1 => if(get) c := ALT_IOBUF(FMC.HB_RX_CLK_n.get) else ALT_IOBUF(FMC.HB_RX_CLK_n.get, c) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 2 => if(get) c := ALT_IOBUF(FMC.HB_TX_CLK_p.get) else ALT_IOBUF(FMC.HB_TX_CLK_p.get, c) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 3 => if(get) c := ALT_IOBUF(FMC.HB_TX_CLK_n.get) else ALT_IOBUF(FMC.HB_TX_CLK_n.get, c) // throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 4 => if(get) c := ALT_IOBUF(FMC.HB_RX_p.get(0)) else ALT_IOBUF(FMC.HB_RX_p.get(0), c)
          case 5 => if(get) c := ALT_IOBUF(FMC.HB_RX_n.get(0)) else ALT_IOBUF(FMC.HB_RX_n.get(0), c)
          case 6 => if(get) c := ALT_IOBUF(FMC.HB_RX_p.get(1)) else ALT_IOBUF(FMC.HB_RX_p.get(1), c)
          case 7 => if(get) c := ALT_IOBUF(FMC.HB_RX_n.get(1)) else ALT_IOBUF(FMC.HB_RX_n.get(1), c)
          case 8 => if(get) c := ALT_IOBUF(FMC.HB_RX_p.get(2)) else ALT_IOBUF(FMC.HB_RX_p.get(2), c)
          case 9 => if(get) c := ALT_IOBUF(FMC.HB_RX_n.get(2)) else ALT_IOBUF(FMC.HB_RX_n.get(2), c)
          case 10 => if(get) c := ALT_IOBUF(FMC.HB_RX_p.get(3)) else ALT_IOBUF(FMC.HB_RX_p.get(3), c)
          case 11 => if(get) c := ALT_IOBUF(FMC.HB_RX_n.get(3)) else ALT_IOBUF(FMC.HB_RX_n.get(3), c)
          case 12 => if(get) c := ALT_IOBUF(FMC.HB_RX_p.get(4)) else ALT_IOBUF(FMC.HB_RX_p.get(4), c)
          case 13 => if(get) c := ALT_IOBUF(FMC.HB_RX_n.get(4)) else ALT_IOBUF(FMC.HB_RX_n.get(4), c)
          case 14 => if(get) c := ALT_IOBUF(FMC.HB_RX_p.get(5)) else ALT_IOBUF(FMC.HB_RX_p.get(5), c)
          case 15 => if(get) c := ALT_IOBUF(FMC.HB_RX_n.get(5)) else ALT_IOBUF(FMC.HB_RX_n.get(5), c)
          case 16 => if(get) c := ALT_IOBUF(FMC.HB_RX_p.get(6)) else ALT_IOBUF(FMC.HB_RX_p.get(6), c)
          case 17 => if(get) c := ALT_IOBUF(FMC.HB_RX_n.get(6)) else ALT_IOBUF(FMC.HB_RX_n.get(6), c)
          case 18 => if(get) c := ALT_IOBUF(FMC.HB_RX_p.get(7)) else ALT_IOBUF(FMC.HB_RX_p.get(7), c)
          case 19 => if(get) c := ALT_IOBUF(FMC.HB_RX_n.get(7)) else ALT_IOBUF(FMC.HB_RX_n.get(7), c)
          case 20 => if(get) c := ALT_IOBUF(FMC.HB_TX_p.get(0)) else ALT_IOBUF(FMC.HB_TX_p.get(0), c)
          case 21 => if(get) c := ALT_IOBUF(FMC.HB_TX_n.get(0)) else ALT_IOBUF(FMC.HB_TX_n.get(0), c)
          case 22 => if(get) c := ALT_IOBUF(FMC.HB_TX_p.get(1)) else ALT_IOBUF(FMC.HB_TX_p.get(1), c)
          case 23 => if(get) c := ALT_IOBUF(FMC.HB_TX_n.get(1)) else ALT_IOBUF(FMC.HB_TX_n.get(1), c)
          case 24 => if(get) c := ALT_IOBUF(FMC.HB_TX_p.get(2)) else ALT_IOBUF(FMC.HB_TX_p.get(2), c)
          case 25 => if(get) c := ALT_IOBUF(FMC.HB_TX_n.get(2)) else ALT_IOBUF(FMC.HB_TX_n.get(2), c)
          case 26 => if(get) c := ALT_IOBUF(FMC.HB_TX_p.get(3)) else ALT_IOBUF(FMC.HB_TX_p.get(3), c)
          case 27 => if(get) c := ALT_IOBUF(FMC.HB_TX_n.get(3)) else ALT_IOBUF(FMC.HB_TX_n.get(3), c)
          case 28 => if(get) c := ALT_IOBUF(FMC.HB_TX_p.get(4)) else ALT_IOBUF(FMC.HB_TX_p.get(4), c)
          case 29 => if(get) c := ALT_IOBUF(FMC.HB_TX_n.get(4)) else ALT_IOBUF(FMC.HB_TX_n.get(4), c)
          case 30 => if(get) c := ALT_IOBUF(FMC.HB_TX_p.get(5)) else ALT_IOBUF(FMC.HB_TX_p.get(5), c)
          case 31 => if(get) c := ALT_IOBUF(FMC.HB_TX_n.get(5)) else ALT_IOBUF(FMC.HB_TX_n.get(5), c)
          case 32 => if(get) c := ALT_IOBUF(FMC.HB_TX_p.get(6)) else ALT_IOBUF(FMC.HB_TX_p.get(6), c)
          case 33 => if(get) c := ALT_IOBUF(FMC.HB_TX_n.get(6)) else ALT_IOBUF(FMC.HB_TX_n.get(6), c)
          case 34 => if(get) c := ALT_IOBUF(FMC.HB_TX_p.get(7)) else ALT_IOBUF(FMC.HB_TX_p.get(7), c)
          case 35 => if(get) c := ALT_IOBUF(FMC.HB_TX_n.get(7)) else ALT_IOBUF(FMC.HB_TX_n.get(7), c)
          case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
        }
      case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
    }
  }
}

// Based on layout of the TR5.sch done by Duy
trait WithFPGATR5ToChipConnect extends WithFPGATR5InternNoChipCreate with WithFPGATR5InternConnect {
  this: FPGATR5Shell =>

  // ******* Duy section ******
  // NOTES:
  def JP18 = 2
  def JP19 = 0
  def JP20 = 3
  def JP21 = 1
  def FMC = FMCA

  // From intern = Clocks and resets
  intern.ChildClock.foreach{ a =>
    //ConnectFMCGPIO(JP21, 2, a.asBool(), false, FMC) // Is useless anyway
  }
  intern.ChildReset.foreach{ a =>
    ConnectFMCGPIO(JP18, 5, a, false, FMC)
  }
  //ConnectFMCGPIO(JP21, 4, intern.sys_clk.asBool(), false, FMC)
  ConnectFMCGPIO(JP18, 2, intern.rst_n, false, FMC)
  ConnectFMCGPIO(JP18, 6, intern.jrst_n, false, FMC)
  // Memory port
  intern.tlport.foreach{ case tlport =>
    ConnectFMCGPIO(JP18, 1, tlport.a.valid, true, FMC)
    ConnectFMCGPIO(JP18, 4, tlport.a.ready, false, FMC)
    require(tlport.a.bits.opcode.getWidth == 3, s"${tlport.a.bits.opcode.getWidth}")
    val a_opcode = Wire(Vec(3, Bool()))
    ConnectFMCGPIO(JP18, 3, a_opcode(2), true, FMC)
    ConnectFMCGPIO(JP18, 7, a_opcode(1), true, FMC)
    ConnectFMCGPIO(JP18, 8, a_opcode(0), true, FMC)
    tlport.a.bits.opcode := a_opcode.asUInt()
    require(tlport.a.bits.param.getWidth == 3, s"${tlport.a.bits.param.getWidth}")
    val a_param = Wire(Vec(3, Bool()))
    ConnectFMCGPIO(JP18, 9, a_param(2), true, FMC)
    ConnectFMCGPIO(JP18, 10, a_param(1), true, FMC)
    ConnectFMCGPIO(JP18, 13, a_param(0), true, FMC)
    tlport.a.bits.param := a_param.asUInt()
    val a_size = Wire(Vec(3, Bool()))
    require(tlport.a.bits.size.getWidth == 3, s"${tlport.a.bits.size.getWidth}")
    ConnectFMCGPIO(JP18, 14, a_size(2), true, FMC)
    ConnectFMCGPIO(JP18, 15, a_size(1), true, FMC)
    ConnectFMCGPIO(JP18, 16, a_size(0), true, FMC)
    tlport.a.bits.size := a_size.asUInt()
    require(tlport.a.bits.source.getWidth == 6, s"${tlport.a.bits.source.getWidth}")
    val a_source = Wire(Vec(6, Bool()))
    ConnectFMCGPIO(JP18, 17, a_source(5), true, FMC)
    ConnectFMCGPIO(JP18, 18, a_source(4), true, FMC)
    ConnectFMCGPIO(JP18, 19, a_source(3), true, FMC)
    ConnectFMCGPIO(JP18, 20, a_source(2), true, FMC)
    ConnectFMCGPIO(JP18, 21, a_source(1), true, FMC)
    ConnectFMCGPIO(JP18, 22, a_source(0), true, FMC)
    tlport.a.bits.source := a_source.asUInt()
    require(tlport.a.bits.address.getWidth == 32, s"${tlport.a.bits.address.getWidth}")
    val a_address = Wire(Vec(32, Bool()))
    ConnectFMCGPIO(JP18, 23, a_address(31), true, FMC)
    ConnectFMCGPIO(JP18, 24, a_address(30), true, FMC)
    ConnectFMCGPIO(JP18, 25, a_address(29), true, FMC)
    ConnectFMCGPIO(JP18, 26, a_address(28), true, FMC)
    ConnectFMCGPIO(JP18, 27, a_address(27), true, FMC)
    ConnectFMCGPIO(JP18, 28, a_address(26), true, FMC)
    ConnectFMCGPIO(JP18, 31, a_address(25), true, FMC)
    ConnectFMCGPIO(JP18, 32, a_address(24), true, FMC)
    ConnectFMCGPIO(JP18, 33, a_address(23), true, FMC)
    ConnectFMCGPIO(JP18, 34, a_address(22), true, FMC)
    ConnectFMCGPIO(JP18, 35, a_address(21), true, FMC)
    ConnectFMCGPIO(JP18, 36, a_address(20), true, FMC)
    ConnectFMCGPIO(JP18, 37, a_address(19), true, FMC)
    ConnectFMCGPIO(JP18, 38, a_address(18), true, FMC)
    ConnectFMCGPIO(JP18, 39, a_address(17), true, FMC)
    ConnectFMCGPIO(JP18, 40, a_address(16), true, FMC)
    ConnectFMCGPIO(JP19,  1, a_address(15), true, FMC)
    ConnectFMCGPIO(JP19,  2, a_address(14), true, FMC)
    ConnectFMCGPIO(JP19,  2, a_address(13), true, FMC)
    ConnectFMCGPIO(JP19,  4, a_address(12), true, FMC)
    ConnectFMCGPIO(JP19,  5, a_address(11), true, FMC)
    ConnectFMCGPIO(JP19,  6, a_address(10), true, FMC)
    ConnectFMCGPIO(JP19,  7, a_address( 9), true, FMC)
    ConnectFMCGPIO(JP19,  8, a_address( 8), true, FMC)
    ConnectFMCGPIO(JP19,  9, a_address( 7), true, FMC)
    ConnectFMCGPIO(JP19, 10, a_address( 6), true, FMC)
    ConnectFMCGPIO(JP19, 13, a_address( 5), true, FMC)
    ConnectFMCGPIO(JP19, 14, a_address( 4), true, FMC)
    ConnectFMCGPIO(JP19, 15, a_address( 3), true, FMC)
    ConnectFMCGPIO(JP19, 16, a_address( 2), true, FMC)
    ConnectFMCGPIO(JP19, 17, a_address( 1), true, FMC)
    ConnectFMCGPIO(JP19, 18, a_address( 0), true, FMC)
    tlport.a.bits.address := a_address.asUInt()
    require(tlport.a.bits.mask.getWidth == 4, s"${tlport.a.bits.mask.getWidth}")
    val a_mask = Wire(Vec(4, Bool()))
    ConnectFMCGPIO(JP19, 19, a_mask(3), true, FMC)
    ConnectFMCGPIO(JP19, 20, a_mask(2), true, FMC)
    ConnectFMCGPIO(JP19, 21, a_mask(1), true, FMC)
    ConnectFMCGPIO(JP19, 22, a_mask(0), true, FMC)
    tlport.a.bits.mask := a_mask.asUInt()
    require(tlport.a.bits.data.getWidth == 32, s"${tlport.a.bits.data.getWidth}")
    val a_data = Wire(Vec(32, Bool()))
    ConnectFMCGPIO(JP19, 23, a_data(31), true, FMC)
    ConnectFMCGPIO(JP19, 24, a_data(30), true, FMC)
    ConnectFMCGPIO(JP19, 25, a_data(29), true, FMC)
    ConnectFMCGPIO(JP19, 26, a_data(28), true, FMC)
    ConnectFMCGPIO(JP19, 27, a_data(27), true, FMC)
    ConnectFMCGPIO(JP19, 28, a_data(26), true, FMC)
    ConnectFMCGPIO(JP19, 31, a_data(25), true, FMC)
    ConnectFMCGPIO(JP19, 32, a_data(24), true, FMC)
    ConnectFMCGPIO(JP19, 33, a_data(23), true, FMC)
    ConnectFMCGPIO(JP19, 34, a_data(22), true, FMC)
    ConnectFMCGPIO(JP19, 35, a_data(21), true, FMC)
    ConnectFMCGPIO(JP19, 36, a_data(20), true, FMC)
    ConnectFMCGPIO(JP19, 37, a_data(19), true, FMC)
    ConnectFMCGPIO(JP19, 38, a_data(18), true, FMC)
    ConnectFMCGPIO(JP19, 39, a_data(17), true, FMC)
    ConnectFMCGPIO(JP19, 40, a_data(16), true, FMC)
    ConnectFMCGPIO(JP20, 10, a_data(15), true, FMC)
    ConnectFMCGPIO(JP20,  9, a_data(14), true, FMC)
    ConnectFMCGPIO(JP20,  8, a_data(13), true, FMC)
    ConnectFMCGPIO(JP20,  7, a_data(12), true, FMC)
    ConnectFMCGPIO(JP20,  6, a_data(11), true, FMC)
    ConnectFMCGPIO(JP20,  5, a_data(10), true, FMC)
    ConnectFMCGPIO(JP20,  4, a_data( 9), true, FMC)
    ConnectFMCGPIO(JP20,  3, a_data( 8), true, FMC)
    ConnectFMCGPIO(JP20,  2, a_data( 7), true, FMC)
    ConnectFMCGPIO(JP20,  1, a_data( 6), true, FMC)
    ConnectFMCGPIO(JP20, 13, a_data( 5), true, FMC)
    ConnectFMCGPIO(JP20, 14, a_data( 4), true, FMC)
    ConnectFMCGPIO(JP20, 15, a_data( 3), true, FMC)
    ConnectFMCGPIO(JP20, 16, a_data( 2), true, FMC)
    ConnectFMCGPIO(JP20, 17, a_data( 1), true, FMC)
    ConnectFMCGPIO(JP20, 18, a_data( 0), true, FMC)
    tlport.a.bits.data := a_data.asUInt()
    ConnectFMCGPIO(JP20, 19, tlport.a.bits.corrupt, true, FMC)
    ConnectFMCGPIO(JP20, 20, tlport.d.ready, true, FMC)
    ConnectFMCGPIO(JP20, 21, tlport.d.valid, false, FMC)
    require(tlport.d.bits.opcode.getWidth == 3, s"${tlport.d.bits.opcode.getWidth}")
    ConnectFMCGPIO(JP20, 22, tlport.d.bits.opcode(2), false, FMC)
    ConnectFMCGPIO(JP20, 23, tlport.d.bits.opcode(1), false, FMC)
    ConnectFMCGPIO(JP20, 24, tlport.d.bits.opcode(0), false, FMC)
    require(tlport.d.bits.param.getWidth == 2, s"${tlport.d.bits.param.getWidth}")
    ConnectFMCGPIO(JP20, 25, tlport.d.bits.param(1), false, FMC)
    ConnectFMCGPIO(JP20, 26, tlport.d.bits.param(0), false, FMC)
    require(tlport.d.bits.size.getWidth == 3, s"${tlport.d.bits.size.getWidth}")
    ConnectFMCGPIO(JP20, 27, tlport.d.bits.size(2), false, FMC)
    ConnectFMCGPIO(JP20, 28, tlport.d.bits.size(1), false, FMC)
    ConnectFMCGPIO(JP20, 31, tlport.d.bits.size(0), false, FMC)
    require(tlport.d.bits.source.getWidth == 6, s"${tlport.d.bits.source.getWidth}")
    ConnectFMCGPIO(JP20, 32, tlport.d.bits.source(5), false, FMC)
    ConnectFMCGPIO(JP20, 33, tlport.d.bits.source(4), false, FMC)
    ConnectFMCGPIO(JP20, 34, tlport.d.bits.source(3), false, FMC)
    ConnectFMCGPIO(JP20, 35, tlport.d.bits.source(2), false, FMC)
    ConnectFMCGPIO(JP20, 36, tlport.d.bits.source(1), false, FMC)
    ConnectFMCGPIO(JP20, 37, tlport.d.bits.source(0), false, FMC)
    require(tlport.d.bits.sink.getWidth == 1, s"${tlport.d.bits.sink.getWidth}")
    ConnectFMCGPIO(JP20, 38, tlport.d.bits.sink(0), false, FMC)
    ConnectFMCGPIO(JP20, 39, tlport.d.bits.denied, false, FMC)
    ConnectFMCGPIO(JP20, 40, tlport.d.bits.corrupt, false, FMC)
    require(tlport.d.bits.data.getWidth == 32, s"${tlport.d.bits.data.getWidth}")
    ConnectFMCGPIO(JP21, 5, tlport.d.bits.data(31), false, FMC)
    ConnectFMCGPIO(JP21, 6, tlport.d.bits.data(30), false, FMC)
    ConnectFMCGPIO(JP21, 7, tlport.d.bits.data(29), false, FMC)
    ConnectFMCGPIO(JP21, 8, tlport.d.bits.data(28), false, FMC)
    ConnectFMCGPIO(JP21, 9, tlport.d.bits.data(27), false, FMC)
    ConnectFMCGPIO(JP21, 10, tlport.d.bits.data(26), false, FMC)
    ConnectFMCGPIO(JP21, 13, tlport.d.bits.data(25), false, FMC)
    ConnectFMCGPIO(JP21, 14, tlport.d.bits.data(24), false, FMC)
    ConnectFMCGPIO(JP21, 15, tlport.d.bits.data(23), false, FMC)
    ConnectFMCGPIO(JP21, 16, tlport.d.bits.data(22), false, FMC)
    ConnectFMCGPIO(JP21, 17, tlport.d.bits.data(21), false, FMC)
    ConnectFMCGPIO(JP21, 18, tlport.d.bits.data(20), false, FMC)
    ConnectFMCGPIO(JP21, 19, tlport.d.bits.data(19), false, FMC)
    ConnectFMCGPIO(JP21, 20, tlport.d.bits.data(18), false, FMC)
    ConnectFMCGPIO(JP21, 21, tlport.d.bits.data(17), false, FMC)
    ConnectFMCGPIO(JP21, 22, tlport.d.bits.data(16), false, FMC)
    ConnectFMCGPIO(JP21, 23, tlport.d.bits.data(15), false, FMC)
    ConnectFMCGPIO(JP21, 24, tlport.d.bits.data(14), false, FMC)
    ConnectFMCGPIO(JP21, 25, tlport.d.bits.data(13), false, FMC)
    ConnectFMCGPIO(JP21, 26, tlport.d.bits.data(12), false, FMC)
    ConnectFMCGPIO(JP21, 27, tlport.d.bits.data(11), false, FMC)
    ConnectFMCGPIO(JP21, 28, tlport.d.bits.data(10), false, FMC)
    ConnectFMCGPIO(JP21, 31, tlport.d.bits.data( 9), false, FMC)
    ConnectFMCGPIO(JP21, 32, tlport.d.bits.data( 8), false, FMC)
    ConnectFMCGPIO(JP21, 33, tlport.d.bits.data( 7), false, FMC)
    ConnectFMCGPIO(JP21, 34, tlport.d.bits.data( 6), false, FMC)
    ConnectFMCGPIO(JP21, 35, tlport.d.bits.data( 5), false, FMC)
    ConnectFMCGPIO(JP21, 36, tlport.d.bits.data( 4), false, FMC)
    ConnectFMCGPIO(JP21, 37, tlport.d.bits.data( 3), false, FMC)
    ConnectFMCGPIO(JP21, 38, tlport.d.bits.data( 2), false, FMC)
    ConnectFMCGPIO(JP21, 39, tlport.d.bits.data( 1), false, FMC)
    ConnectFMCGPIO(JP21, 40, tlport.d.bits.data( 0), false, FMC)
  }

  // ******* Ahn-Dao section ******
  def FMCSER = FMCA
  def versionSer = 1
  versionSer match {
    case 1 =>
      val MEMSER_GPIO = 0
      val EXTSER_GPIO = 1
      PUT(intern.sys_clk.asBool(), FMCSER.CLK_M2C_p(1))
      //ConnectFMCGPIO(MEMSER_GPIO, 1, intern.sys_clk.asBool(), false, FMCSER) //
      //intern.ChildClock.foreach{ a => ConnectFMCGPIO(MEMSER_GPIO, 2, a.asBool(), false, FMCSER) }
      //intern.usbClk.foreach{ a => ConnectFMCGPIO(MEMSER_GPIO, 3, a.asBool(), false, FMCSER) }
      ConnectFMCGPIO(MEMSER_GPIO, 4, intern.jrst_n, false, FMCSER)
      ConnectFMCGPIO(MEMSER_GPIO, 5, intern.rst_n, false, FMCSER)
      // ExtSerMem
      intern.memser.foreach { memser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(MEMSER_GPIO, 9, in_bits(7), false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 10, in_bits(6), false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 13, in_bits(5), false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 14, in_bits(4), false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 15, in_bits(3), false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 16, in_bits(2), false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 17, in_bits(1), false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 18, in_bits(0), false, FMCSER)
        in_bits := memser.in.bits.asBools()
        ConnectFMCGPIO(MEMSER_GPIO, 19, memser.in.valid, false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 20, memser.out.ready, false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 21, memser.in.ready, true, FMCSER)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(MEMSER_GPIO, 22, out_bits(7), true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 23, out_bits(6), true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 24, out_bits(5), true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 25, out_bits(4), true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 26, out_bits(3), true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 27, out_bits(2), true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 28, out_bits(1), true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 31, out_bits(0), true, FMCSER)
        memser.out.bits := out_bits.asUInt()
        ConnectFMCGPIO(MEMSER_GPIO, 32, memser.out.valid, true, FMCSER)
      }
      // ExtSerBus
      intern.extser.foreach{ extser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(EXTSER_GPIO, 9, in_bits(7), false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 10, in_bits(6), false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 13, in_bits(5), false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 14, in_bits(4), false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 15, in_bits(3), false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 16, in_bits(2), false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 17, in_bits(1), false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 18, in_bits(0), false, FMCSER)
        in_bits := extser.in.bits.asBools()
        ConnectFMCGPIO(EXTSER_GPIO, 19, extser.in.valid, false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 20, extser.out.ready, false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 21, extser.in.ready, true, FMCSER)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(EXTSER_GPIO, 22, out_bits(7), true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 23, out_bits(6), true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 24, out_bits(5), true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 25, out_bits(4), true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 26, out_bits(3), true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 27, out_bits(2), true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 28, out_bits(1), true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 31, out_bits(0), true, FMCSER)
        extser.out.bits := out_bits.asUInt()
        ConnectFMCGPIO(EXTSER_GPIO, 32, extser.out.valid, true, FMCSER)
      }
    case _ =>
      ConnectFMCGPIO(0, 1, intern.sys_clk.asBool(), false, FMCSER)
      intern.ChildClock.foreach{ a => PUT(a.asBool(), FMCSER.CLK_M2C_n(1)) }
      intern.usbClk.foreach{ a => PUT(a.asBool(), FMCSER.CLK_M2C_n(1)) }
      ConnectFMCGPIO(0, 27, intern.jrst_n, false, FMCSER)
      ConnectFMCGPIO(1, 15, intern.rst_n, false, FMCSER)
      // ExtSerMem
      intern.memser.foreach { memser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(0, 31, in_bits(7), false, FMCSER)
        ConnectFMCGPIO(0, 33, in_bits(6), false, FMCSER)
        ConnectFMCGPIO(0, 35, in_bits(5), false, FMCSER)
        ConnectFMCGPIO(0, 37, in_bits(4), false, FMCSER)
        ConnectFMCGPIO(0, 39, in_bits(3), false, FMCSER)
        ConnectFMCGPIO(1, 1, in_bits(2), false, FMCSER)
        ConnectFMCGPIO(1, 5, in_bits(1), false, FMCSER)
        ConnectFMCGPIO(1, 7, in_bits(0), false, FMCSER)
        in_bits := memser.in.bits.asBools()
        ConnectFMCGPIO(1, 9, memser.in.valid, false, FMCSER)
        ConnectFMCGPIO(1, 13, memser.out.ready, false, FMCSER)
        ConnectFMCGPIO(2, 5, memser.in.ready, true, FMCSER)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(2, 7, out_bits(7), true, FMCSER)
        ConnectFMCGPIO(2, 9, out_bits(6), true, FMCSER)
        ConnectFMCGPIO(2, 13, out_bits(5), true, FMCSER)
        ConnectFMCGPIO(2, 15, out_bits(4), true, FMCSER)
        ConnectFMCGPIO(2, 17, out_bits(3), true, FMCSER)
        ConnectFMCGPIO(2, 19, out_bits(2), true, FMCSER)
        ConnectFMCGPIO(2, 21, out_bits(1), true, FMCSER)
        ConnectFMCGPIO(2, 23, out_bits(0), true, FMCSER)
        memser.out.bits := out_bits.asUInt()
        ConnectFMCGPIO(2, 25, memser.out.valid, true, FMCSER)
      }
      // ExtSerBus
      intern.extser.foreach{ extser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(0, 5, in_bits(7), false, FMCSER)
        ConnectFMCGPIO(0, 7, in_bits(6), false, FMCSER)
        ConnectFMCGPIO(0, 9, in_bits(5), false, FMCSER)
        ConnectFMCGPIO(0, 13, in_bits(4), false, FMCSER)
        ConnectFMCGPIO(0, 15, in_bits(3), false, FMCSER)
        ConnectFMCGPIO(0, 17, in_bits(2), false, FMCSER)
        ConnectFMCGPIO(0, 19, in_bits(1), false, FMCSER)
        ConnectFMCGPIO(0, 21, in_bits(0), false, FMCSER)
        in_bits := extser.in.bits.asBools()
        ConnectFMCGPIO(0, 23, extser.in.valid, false, FMCSER)
        ConnectFMCGPIO(0, 25, extser.out.ready, false, FMCSER)
        ConnectFMCGPIO(1, 17, extser.in.ready, true, FMCSER)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(1, 19, out_bits(7), true, FMCSER)
        ConnectFMCGPIO(1, 21, out_bits(6), true, FMCSER)
        ConnectFMCGPIO(1, 25, out_bits(5), true, FMCSER)
        ConnectFMCGPIO(1, 27, out_bits(4), true, FMCSER)
        ConnectFMCGPIO(1, 31, out_bits(3), true, FMCSER)
        ConnectFMCGPIO(1, 33, out_bits(2), true, FMCSER)
        ConnectFMCGPIO(1, 35, out_bits(1), true, FMCSER)
        ConnectFMCGPIO(1, 37, out_bits(0), true, FMCSER)
        extser.out.bits := out_bits.asUInt()
        ConnectFMCGPIO(1, 39, extser.out.valid, true, FMCSER)
      }
  }

  // ******** Misc part ********

  // LEDs
  LED := Cat(
    intern.mem_status_local_cal_fail,
    intern.mem_status_local_cal_success,
    intern.mem_status_local_init_done,
    BUTTON(2)
  )
  // Clocks to the outside
  ALT_IOBUF(SMA_CLKOUT_p, intern.sys_clk.asBool())
  // NOTE: ChildClock and usbClk cannot exist at the same time
  intern.ChildClock.foreach(A => ALT_IOBUF(SMA_CLKOUT_n, A.asBool()))
  intern.usbClk.foreach(A => ALT_IOBUF(SMA_CLKOUT_n, A.asBool()))
  SD_CLK := false.B
}

// Trait which connects the FPGA the chip
trait WithFPGATR5FromChipConnect extends WithFPGATR5PureConnect {
  this: FPGATR5Shell =>
  
  // ******* Ahn-Dao section ******
  def FMCSER = FMCA
  def versionSer = 1
  versionSer match {
    case 1 =>
      val MEMSER_GPIO = 0
      val EXTSER_GPIO = 1
      val sysclk = Wire(Bool())
      sysclk := ALT_IOBUF(SMA_CLKOUT_p) // ConnectFMCGPIO(MEMSER_GPIO, 1, sysclk,  true, FMCSER)
      chip.sys_clk := sysclk.asClock()
      chip.ChildClock.foreach{ a =>
        val clkwire = Wire(Bool())
        clkwire := ALT_IOBUF(SMA_CLKOUT_n) // ConnectFMCGPIO(MEMSER_GPIO, 2, clkwire,  true, FMCSER)
        a := clkwire.asClock()
      }
      chip.usb11hs.foreach{ a =>
        val clkwire = Wire(Bool())
        clkwire := ALT_IOBUF(SMA_CLKOUT_n) // ConnectFMCGPIO(MEMSER_GPIO, 3, clkwire,  true, FMCSER)
        a.usbClk := clkwire.asClock()
      }
      ConnectFMCGPIO(MEMSER_GPIO, 4, chip.jrst_n,  true, FMCSER)
      ConnectFMCGPIO(MEMSER_GPIO, 5, chip.rst_n,  true, FMCSER)
      // ExtSerMem
      chip.memser.foreach { memser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(MEMSER_GPIO, 9, in_bits(7),  true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 10, in_bits(6),  true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 13, in_bits(5),  true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 14, in_bits(4),  true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 15, in_bits(3),  true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 16, in_bits(2),  true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 17, in_bits(1),  true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 18, in_bits(0),  true, FMCSER)
        in_bits := memser.in.bits.asBools()
        ConnectFMCGPIO(MEMSER_GPIO, 19, memser.in.valid,  true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 20, memser.out.ready,  true, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 21, memser.in.ready,  false, FMCSER)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(MEMSER_GPIO, 22, out_bits(7),  false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 23, out_bits(6),  false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 24, out_bits(5),  false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 25, out_bits(4),  false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 26, out_bits(3),  false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 27, out_bits(2),  false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 28, out_bits(1),  false, FMCSER)
        ConnectFMCGPIO(MEMSER_GPIO, 31, out_bits(0),  false, FMCSER)
        memser.out.bits := out_bits.asUInt()
        ConnectFMCGPIO(MEMSER_GPIO, 32, memser.out.valid,  false, FMCSER)
      }
      // ExtSerBus
      chip.extser.foreach{ extser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(EXTSER_GPIO, 9, in_bits(7),  true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 10, in_bits(6),  true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 13, in_bits(5),  true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 14, in_bits(4),  true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 15, in_bits(3),  true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 16, in_bits(2),  true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 17, in_bits(1),  true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 18, in_bits(0),  true, FMCSER)
        in_bits := extser.in.bits.asBools()
        ConnectFMCGPIO(EXTSER_GPIO, 19, extser.in.valid,  true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 20, extser.out.ready,  true, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 21, extser.in.ready,  false, FMCSER)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(EXTSER_GPIO, 22, out_bits(7),  false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 23, out_bits(6),  false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 24, out_bits(5),  false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 25, out_bits(4),  false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 26, out_bits(3),  false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 27, out_bits(2),  false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 28, out_bits(1),  false, FMCSER)
        ConnectFMCGPIO(EXTSER_GPIO, 31, out_bits(0),  false, FMCSER)
        extser.out.bits := out_bits.asUInt()
        ConnectFMCGPIO(EXTSER_GPIO, 32, extser.out.valid,  false, FMCSER)
      }
    case _ =>
      val sysclk = Wire(Bool())
      ConnectFMCGPIO(0, 1, sysclk,  true, FMCSER)
      chip.sys_clk := sysclk.asClock()
      chip.ChildClock.foreach{ a => a := GET(FMCSER.CLK_M2C_n(1)).asClock() }
      chip.usb11hs.foreach{ a => a.usbClk := GET(FMCSER.CLK_M2C_n(1)).asClock() }
      ConnectFMCGPIO(0, 27, chip.jrst_n,  true, FMCSER)
      ConnectFMCGPIO(1, 15, chip.rst_n,  true, FMCSER)
      // ExtSerMem
      chip.memser.foreach { memser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(0, 31, in_bits(7),  true, FMCSER)
        ConnectFMCGPIO(0, 33, in_bits(6),  true, FMCSER)
        ConnectFMCGPIO(0, 35, in_bits(5),  true, FMCSER)
        ConnectFMCGPIO(0, 37, in_bits(4),  true, FMCSER)
        ConnectFMCGPIO(0, 39, in_bits(3),  true, FMCSER)
        ConnectFMCGPIO(1, 1, in_bits(2),  true, FMCSER)
        ConnectFMCGPIO(1, 5, in_bits(1),  true, FMCSER)
        ConnectFMCGPIO(1, 7, in_bits(0),  true, FMCSER)
        in_bits := memser.in.bits.asBools()
        ConnectFMCGPIO(1, 9, memser.in.valid,  true, FMCSER)
        ConnectFMCGPIO(1, 13, memser.out.ready,  true, FMCSER)
        ConnectFMCGPIO(2, 5, memser.in.ready,  false, FMCSER)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(2, 7, out_bits(7),  false, FMCSER)
        ConnectFMCGPIO(2, 9, out_bits(6),  false, FMCSER)
        ConnectFMCGPIO(2, 13, out_bits(5),  false, FMCSER)
        ConnectFMCGPIO(2, 15, out_bits(4),  false, FMCSER)
        ConnectFMCGPIO(2, 17, out_bits(3),  false, FMCSER)
        ConnectFMCGPIO(2, 19, out_bits(2),  false, FMCSER)
        ConnectFMCGPIO(2, 21, out_bits(1),  false, FMCSER)
        ConnectFMCGPIO(2, 23, out_bits(0),  false, FMCSER)
        memser.out.bits := out_bits.asUInt()
        ConnectFMCGPIO(2, 25, memser.out.valid,  false, FMCSER)
      }
      // ExtSerBus
      chip.extser.foreach{ extser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(0, 5, in_bits(7),  true, FMCSER)
        ConnectFMCGPIO(0, 7, in_bits(6),  true, FMCSER)
        ConnectFMCGPIO(0, 9, in_bits(5),  true, FMCSER)
        ConnectFMCGPIO(0, 13, in_bits(4),  true, FMCSER)
        ConnectFMCGPIO(0, 15, in_bits(3),  true, FMCSER)
        ConnectFMCGPIO(0, 17, in_bits(2),  true, FMCSER)
        ConnectFMCGPIO(0, 19, in_bits(1),  true, FMCSER)
        ConnectFMCGPIO(0, 21, in_bits(0),  true, FMCSER)
        in_bits := extser.in.bits.asBools()
        ConnectFMCGPIO(0, 23, extser.in.valid,  true, FMCSER)
        ConnectFMCGPIO(0, 25, extser.out.ready,  true, FMCSER)
        ConnectFMCGPIO(1, 17, extser.in.ready,  false, FMCSER)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(1, 19, out_bits(7),  false, FMCSER)
        ConnectFMCGPIO(1, 21, out_bits(6),  false, FMCSER)
        ConnectFMCGPIO(1, 25, out_bits(5),  false, FMCSER)
        ConnectFMCGPIO(1, 27, out_bits(4),  false, FMCSER)
        ConnectFMCGPIO(1, 31, out_bits(3),  false, FMCSER)
        ConnectFMCGPIO(1, 33, out_bits(2),  false, FMCSER)
        ConnectFMCGPIO(1, 35, out_bits(1),  false, FMCSER)
        ConnectFMCGPIO(1, 37, out_bits(0),  false, FMCSER)
        extser.out.bits := out_bits.asUInt()
        ConnectFMCGPIO(1, 39, extser.out.valid,  false, FMCSER)
      }
  }
}