package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.util.ResetCatchAndSync
import chipsalliance.rocketchip.config.Parameters
import uec.teehardware.macros._
import uec.teehardware._

class HSMCDE4 extends Bundle {
  val CLKIN_n1 = Input(Bool())
  val CLKIN_n2 = Input(Bool())
  val CLKIN_p1 = Input(Bool())
  val CLKIN_p2 = Input(Bool())
  val CLKIN0 = Input(Bool())
  val CLKOUT_n2 = Output(Bool())
  val CLKOUT_p2 = Output(Bool())
  val D = Vec(4, Analog(1.W))
  //val GXB_RX_p = Input(Bits((3+1).W))
  //val GXB_TX_p = Output(Bits((3+1).W))
  val OUT_n1 = Analog(1.W)
  val OUT_p1 = Analog(1.W)
  val OUT0 = Analog(1.W)
  //val REFCLK_p = Input(Bool())
  val RX_n = Vec(17, Analog(1.W))
  val RX_p = Vec(17, Analog(1.W))
  val TX_n = Vec(17, Analog(1.W))
  val TX_p = Vec(17, Analog(1.W))
}

trait FPGADE4ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters

  ///////// LED /////////
  val LED = IO(Output(Bits((7 + 1).W)))

  //////////// 7-Segment Display //////////
  /*val SEG0_D = IO(Output(Bits((6+1).W)))
  val SEG1_D = IO(Output(Bits((6+1).W)))
  val SEG0_DP = IO(Output(Bool()))
  val SEG1_DP = IO(Output(Bool()))*/

  ///////// BUTTON /////////
  val BUTTON = IO(Input(Bits((3 + 1).W)))

  ///////// SW /////////
  val SW = IO(Input(Bits((7 + 1).W)))

  //////////// SLIDE SWITCH x 4 //////////
  val SLIDE_SW = IO(Input(Bits((3 + 1).W)))

  ///////// FAN /////////
  val FAN_CTRL = IO(Output(Bool()))
  FAN_CTRL := true.B

  //////////// SDCARD //////////
  val SD_CLK = IO(Output(Bool()))
  val SD_MOSI = IO(Output(Bool()))
  val SD_MISO = IO(Input(Bool()))
  val SD_CS_N = IO(Output(Bool()))
  //val SD_INTERUPT_N = IO(Input(Bool()))
  //val SD_WP_n = IO(Input(Bool()))

  ////////////// PCIe x 8 //////////
  /*	val PCIE_PREST_n = IO(Input(Bool()))
    val PCIE_REFCLK_p = IO(Input(Bool()))
    val PCIE_RX_p = IO(Input(Bits((7+1).W)))
    val PCIE_SMBCLK = IO(Input(Bool()))
    val PCIE_SMBDAT = IO(Analog(1.W))
    val PCIE_TX_p = IO(Output(Bits((7+1).W)))
    val PCIE_WAKE_n = IO(Output(Bool()))  */

  ///////// GPIO /////////
  val GPIO0_D = IO(Vec(35+1, Analog(1.W)))
  val GPIO1_D = IO(Vec(35+1, Analog(1.W)))

  ///////////  EXT_IO /////////
  //val EXT_IO = IO(Analog(1.W))

  //////////// HSMC_A //////////
  val HSMA = IO(new HSMCDE4)

  //////////// HSMC_B //////////
  val HSMB = IO(new HSMCDE4)

  //////////// HSMC I2C //////////
  /*val HSMC_SCL = IO(Output(Bool()))
  val HSMC_SDA = IO(Analog(1.W))*/

  //////////// 7-Segment Display //////////
  /*val SEG0_D = IO(Output(Bits((6+1).W)))
  val SEG1_D = IO(Output(Bits((6+1).W)))
  val SEG0_DP = IO(Output(Bool()))
  val SEG1_DP = IO(Output(Bool()))*/

  //////////// Uart //////////
  //val UART_CTS = IO(Output(Bool()))
  //val UART_RTS = IO(Input(Bool()))
  val UART_RXD = IO(Input(Bool()))
  val UART_TXD = IO(Output(Bool()))
}

trait FPGADE4ClockAndResetsAndDDR {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters

  ///////// CLOCKS /////////
  val OSC_50_BANK2 = IO(Input(Clock())) //HSMA + UART + ext_pll
  val OSC_50_BANK3 = IO(Input(Clock())) //DIMM1		<-- most used
  val OSC_50_BANK4 = IO(Input(Clock())) //SDCARD
  val OSC_50_BANK5 = IO(Input(Clock())) //GPIO0 + GPIO1
  val OSC_50_BANK6 = IO(Input(Clock())) //HSMB + Ethernet
  val OSC_50_BANK7 = IO(Input(Clock())) //DIMM2 + USB + FSM + Flash
  val GCLKIN = IO(Input(Clock()))
  //val GCLKOUT_FPGA = IO(Output(Clock()))
  //val SMA_CLKOUT_p = IO(Output(Clock()))

  //////// CPU RESET //////////
  val CPU_RESET_n = IO(Input(Bool()))

  //////////// DDR2 SODIMM //////////
  val M1_DDR2_addr = IO(Output(Bits((15 + 1).W)))
  val M1_DDR2_ba = IO(Output(Bits((2 + 1).W)))
  val M1_DDR2_cas_n = IO(Output(Bool()))
  val M1_DDR2_cke = IO(Output(Bits((1 + 1).W)))
  val M1_DDR2_clk = IO(Output(Bits((1 + 1).W)))
  val M1_DDR2_clk_n = IO(Output(Bits((1 + 1).W)))
  val M1_DDR2_cs_n = IO(Output(Bits((1 + 1).W)))
  val M1_DDR2_dm = IO(Output(Bits((7 + 1).W)))
  val M1_DDR2_dq = IO(Analog((63 + 1).W))
  val M1_DDR2_dqs = IO(Analog((7 + 1).W))
  val M1_DDR2_dqsn = IO(Analog((7 + 1).W))
  val M1_DDR2_odt = IO(Output(Bits((1 + 1).W)))
  val M1_DDR2_ras_n = IO(Output(Bool()))
  //val M1_DDR2_SA = IO(Output(Bits((1+1).W)))
  //val M1_DDR2_SCL = IO(Output(Bool()))
  //val M1_DDR2_SDA = IO(Analog(1.W))
  val M1_DDR2_we_n = IO(Output(Bool()))
  val M1_DDR2_oct_rdn = IO(Input(Bool()))
  val M1_DDR2_oct_rup = IO(Input(Bool()))

  //////////// DDR2 SODIMM //////////
  /*	val M2_DDR2_addr = IO(Output(Bits((15+1).W)))
    val M2_DDR2_ba = IO(Output(Bits((2+1).W)))
    val M2_DDR2_cas_n = IO(Output(Bool()))
    val M2_DDR2_cke = IO(Output(Bits((1+1).W)))
    val M2_DDR2_clk = IO(Analog((1+1).W))
    val M2_DDR2_clk_n = IO(Analog((1+1).W))
    val M2_DDR2_cs_n = IO(Output(Bits((1+1).W)))
    val M2_DDR2_dm = IO(Output(Bits((7+1).W)))
    val M2_DDR2_dq = IO(Analog((63+1).W))
    val M2_DDR2_dqs = IO(Analog((7+1).W))
    val M2_DDR2_dqsn = IO(Analog((7+1).W))
    val M2_DDR2_odt = IO(Output(Bits((1+1).W)))
    val M2_DDR2_ras_n = IO(Output(Bool()))
    val M2_DDR2_SA = IO(Output(Bits((1+1).W)))
    val M2_DDR2_SCL = IO(Output(Bool()))
    val M2_DDR2_SDA = IO(Analog(1.W))
    val M2_DDR2_we_n = IO(Output(Bool()))
    val M2_DDR2_oct_rdn = IO(Input(Bool()))
    val M2_DDR2_oct_rup = IO(Input(Bool()))  */
}

class FPGADE4Shell(implicit val p :Parameters) extends RawModule
  with FPGADE4ChipShell
  with FPGADE4ClockAndResetsAndDDR {
}

class FPGADE4Internal(chip: Option[WithTEEHWbaseShell with WithTEEHWbaseConnect])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGADE4ClockAndResetsAndDDR {
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
      sdramclock.foreach(_ := mod_io_ckrst.qsys_clk)
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
      mod_io_ckrst.ddr_ref_clk := OSC_50_BANK3.asUInt()
      mod_io_ckrst.qsys_ref_clk := OSC_50_BANK5.asUInt()
      mod_io_ckrst.system_reset_n := CPU_RESET_n
    }

    // Helper function to connect the DDR from the Quartus Platform
    def ConnectDDRUtil(mod_io_qport: QuartusIO, mod_io_ckrst: Bundle with QuartusClocksReset) = {
      M1_DDR2_addr := mod_io_qport.memory_mem_a
      M1_DDR2_ba := mod_io_qport.memory_mem_ba
      M1_DDR2_clk := mod_io_qport.memory_mem_ck
      M1_DDR2_clk_n := mod_io_qport.memory_mem_ck_n
      M1_DDR2_cke := mod_io_qport.memory_mem_cke
      M1_DDR2_cs_n := mod_io_qport.memory_mem_cs_n
      M1_DDR2_dm := mod_io_qport.memory_mem_dm
      M1_DDR2_ras_n := mod_io_qport.memory_mem_ras_n
      M1_DDR2_cas_n := mod_io_qport.memory_mem_cas_n
      M1_DDR2_we_n := mod_io_qport.memory_mem_we_n
      attach(M1_DDR2_dq, mod_io_qport.memory_mem_dq)
      attach(M1_DDR2_dqs, mod_io_qport.memory_mem_dqs)
      attach(M1_DDR2_dqsn, mod_io_qport.memory_mem_dqs_n)
      M1_DDR2_odt := mod_io_qport.memory_mem_odt
      mod_io_qport.oct.rdn.foreach(_ := M1_DDR2_oct_rdn)
      mod_io_qport.oct.rup.foreach(_ := M1_DDR2_oct_rup)
    }
    
    tlport.foreach { chiptl =>
      // Instance our converter, and connect everything
      val mod = Module(LazyModule(new TLULtoQuartusPlatform(chiptl.params)).module)

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
      val mod = Module(LazyModule(new SertoQuartusPlatform(ms.w, sourceBits)).module)

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
      val mod = Module(LazyModule(new FPGAMiniSystemDummy(sourceBits)).module)

      // Serial port
      mod.serport.flipConnect(es)
    }
  }
}

trait WithFPGADE4Connect {
  this: FPGADE4Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGADE4Internal(Some(chip)))

  // To intern = Clocks and resets
  intern.OSC_50_BANK2 := OSC_50_BANK2
  intern.OSC_50_BANK3 := OSC_50_BANK3
  intern.OSC_50_BANK4 := OSC_50_BANK4
  intern.OSC_50_BANK5 := OSC_50_BANK5
  intern.OSC_50_BANK6 := OSC_50_BANK6
  intern.OSC_50_BANK7 := OSC_50_BANK7
  intern.GCLKIN := GCLKIN
  intern.CPU_RESET_n := CPU_RESET_n

  M1_DDR2_addr := intern.M1_DDR2_addr
  M1_DDR2_ba := intern.M1_DDR2_ba
  M1_DDR2_clk := intern.M1_DDR2_clk
  M1_DDR2_clk_n := intern.M1_DDR2_clk_n
  M1_DDR2_cke := intern.M1_DDR2_cke
  M1_DDR2_cs_n := intern.M1_DDR2_cs_n
  M1_DDR2_dm := intern.M1_DDR2_dm
  M1_DDR2_ras_n := intern.M1_DDR2_ras_n
  M1_DDR2_cas_n := intern.M1_DDR2_cas_n
  M1_DDR2_we_n := intern.M1_DDR2_we_n
  attach(M1_DDR2_dq, intern.M1_DDR2_dq)
  attach(M1_DDR2_dqs, intern.M1_DDR2_dqs)
  attach(M1_DDR2_dqsn, intern.M1_DDR2_dqsn)
  M1_DDR2_odt := intern.M1_DDR2_odt
  M1_DDR2_we_n := intern.M1_DDR2_we_n
  intern.M1_DDR2_oct_rdn := M1_DDR2_oct_rdn
  intern.M1_DDR2_oct_rup := M1_DDR2_oct_rup

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // The rest of the platform connections
  val chipshell_led = chip.gpio_out 	// output [7:0]
  LED := Cat(
    intern.mem_status_local_cal_fail,
    intern.mem_status_local_cal_success,
    intern.mem_status_local_init_done,
    CPU_RESET_n,
    chipshell_led(3,0)
  )
  chip.gpio_in := SW(7,0)			// input  [7:0]
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
  chip.uart_rxd := UART_RXD	// UART_TXD
  UART_TXD := chip.uart_txd 	// UART_RXD
  SD_CLK := chip.sdio.sdio_clk 	// output
  SD_MOSI := chip.sdio.sdio_cmd 	// output
  chip.sdio.sdio_dat_0 := SD_MISO 	// input
  SD_CS_N := chip.sdio.sdio_dat_3 	// output

  // USB phy connections
  (chip.usb11hs zip intern.usbClk).foreach{ case (chipport, uclk) =>
    ALT_IOBUF(GPIO1_D(17), chipport.USBFullSpeed)
    chipport.USBWireDataIn := Cat(ALT_IOBUF(GPIO1_D(24)), ALT_IOBUF(GPIO1_D(26)))
    ALT_IOBUF(GPIO1_D(28), chipport.USBWireCtrlOut)
    ALT_IOBUF(GPIO1_D(16), chipport.USBWireDataOut(0))
    ALT_IOBUF(GPIO1_D(18), chipport.USBWireDataOut(1))

    chipport.usbClk := uclk
  }

  // TODO Nullify this for now
  chip.sdram.foreach{ sdram =>
    sdram.sdram_data_i := 0.U
  }
}