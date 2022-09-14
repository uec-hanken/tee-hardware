package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing, LazyModule}
import freechips.rocketchip.util._
import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import sifive.blocks.devices.spi.{SPIFlashParams, SPIParams}
import uec.teehardware.macros._
import uec.teehardware._
import uec.teehardware.devices.sdram.SDRAMKey
import uec.teehardware.devices.sifiveblocks._
import uec.teehardware.devices.tlmemext._
import uec.teehardware.devices.usb11hs.HasPeripheryUSB11HSChipImp

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

  ///////// LED /////////
  val LED = IO(Vec(4, Analog(1.W)))

  ///////// SW /////////
  val SW = IO(Vec(4, Analog(1.W)))

  ///////// FAN /////////
  val FAN_ALERT_n = IO(Input(Bool()))

  //////////// SD Card //////////
  val SD_CLK = IO(Analog(1.W))
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

class FPGATR5Internal(chip: Option[Any])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGATR5ClockAndResetsAndDDR {
  def outer = chip

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
      mod_clock := mod_io_ckrst.qsys_clk
      mod_reset := reset_to_sys
      rst_n := !reset_to_sys
      usbClk.foreach(_ := mod_io_ckrst.usb_clk)

      // Async clock connections
      println(s"Connecting ${aclkn} async clocks by default =>")
      (aclocks zip namedclocks).foreach { case (aclk, nam) =>
        println(s"  Detected clock ${nam}")
        if(nam.contains("mbus")) {
          aclk := mod_io_ckrst.io_clk
          println("    Connected to io_clk")
          mod_clock := mod_io_ckrst.io_clk
          mod_reset := reset_to_child
          println("    Quartus Island clock also connected to io_clk")
        }
        else {
          aclk := mod_io_ckrst.qsys_clk
          println("    Connected to qsys_clk")
        }
      }
      DefaultRTC

      mod_io_ckrst.ddr_ref_clk := OSC_50_B3B.asUInt
      mod_io_ckrst.qsys_ref_clk := OSC_50_B4A.asUInt // TODO: This is okay?
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

class FPGATR5InternalNoChip()(implicit p :Parameters) extends FPGATR5Internal(None)(p)
  with FPGAInternalNoChipDef

trait WithFPGATR5InternCreate {
  this: FPGATR5Shell =>
  val chip: Any
  val intern = Module(new FPGATR5Internal(Some(chip)))
}

trait WithFPGATR5InternNoChipCreate {
  this: FPGATR5Shell =>
  val intern = Module(new FPGATR5InternalNoChip())
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
  val chip: Any

  def namedclocks: Seq[String] = chip.asInstanceOf[HasTEEHWClockGroupChipImp].system.namedclocks

  // GPIO
  val gpport = SW ++ LED.slice(0, 1)
  chip.asInstanceOf[HasTEEHWPeripheryGPIOChipImp].gpio.zip(gpport).foreach{case(gp, i) =>
    attach(gp, i)
  }

  // JTAG
  chip.asInstanceOf[DebugJTAGOnlyChipImp].jtag.foreach{ chipjtag =>
    attach(chipjtag.TDI, GPIO(4))
    attach(chipjtag.TMS, GPIO(6))
    attach(chipjtag.TCK, GPIO(8))
    attach(GPIO(10), chipjtag.TDO)
    attach(chipjtag.TRSTn, GPIO(12))
  }

  // QSPI
  (chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].spi zip chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].allspicfg).zipWithIndex.foreach {
    case ((qspiport: SPIPIN, _: SPIParams), i: Int) =>
      if (i == 0) {
        // SD IO
        attach(SD_CLK, qspiport.SCK)
        attach(SD_CMD, qspiport.DQ(0))
        attach(qspiport.DQ(1), SD_DATA(0))
        attach(SD_DATA(3), qspiport.CS(0))
        attach(SD_DATA(1), qspiport.DQ(2))
        attach(SD_DATA(2), qspiport.DQ(2))
      }
    case ((qspiport: SPIPIN, _: SPIFlashParams), _: Int) =>
      attach(qspiport.DQ(1), GPIO(1))
      attach(GPIO(3), qspiport.DQ(0))
      attach(GPIO(5), qspiport.CS(0))
      attach(GPIO(7), qspiport.SCK)
      attach(GPIO(9), qspiport.DQ(2))
      attach(GPIO(11), qspiport.DQ(3))
  }

  // UART
  chip.asInstanceOf[HasTEEHWPeripheryUARTChipImp].uart.foreach { uart =>
    attach(uart.RXD, UART_RX)
    attach(UART_TX, uart.TXD)
  }

  // USB phy connections
  chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs.foreach{ case chipport=>
    attach(GPIO(17), chipport.USBFullSpeed)
    attach(GPIO(24), chipport.USBWireDataIn(0))
    attach(GPIO(26), chipport.USBWireDataIn(1))
    attach(GPIO(28), chipport.USBWireCtrlOut)
    attach(GPIO(16), chipport.USBWireDataOut(0))
    attach(GPIO(18), chipport.USBWireDataOut(1))
  }

  // TODO Nullify sdram
}

trait WithFPGATR5Connect extends WithFPGATR5PureConnect 
  with WithFPGATR5InternCreate 
  with WithFPGATR5InternConnect {
  this: FPGATR5Shell =>

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // The rest of the platform connections
  PUT(intern.mem_status_local_cal_fail, LED(3))
  PUT(intern.mem_status_local_cal_success, LED(2))
  PUT(intern.mem_status_local_init_done, LED(1))
}

object ConnectFMCGPIO {
  def npmap(FMC: FMCTR5): Map[(Int, Int), Analog] = {
    val default = Map(
      (0, 0) -> FMC.CLK_M2C_p(0),
      (0, 1) -> FMC.CLK_M2C_n(0),
      (0, 2) -> FMC.LA_TX_CLK_p,
      (0, 3) -> FMC.LA_TX_CLK_n,
      (0, 4) -> FMC.LA_TX_p(5),
      (0, 5) -> FMC.LA_TX_n(5),
      (0, 6) -> FMC.LA_TX_p(7),
      (0, 7) -> FMC.LA_TX_n(7),
      (0, 8) -> FMC.LA_TX_p(4),
      (0, 9) -> FMC.LA_TX_n(4),
      (0, 10) -> FMC.LA_TX_p(6),
      (0, 11) -> FMC.LA_TX_n(6),
      (0, 12) -> FMC.LA_TX_p(8),
      (0, 13) -> FMC.LA_TX_n(8),
      (0, 14) -> FMC.LA_TX_p(9),
      (0, 15) -> FMC.LA_TX_n(9),
      (0, 16) -> FMC.LA_TX_p(10),
      (0, 17) -> FMC.LA_TX_n(10),
      (0, 18) -> FMC.LA_TX_p(11),
      (0, 19) -> FMC.LA_TX_n(11),
      (0, 20) -> FMC.LA_TX_p(3),
      (0, 21) -> FMC.LA_TX_n(3),
      (0, 22) -> FMC.LA_TX_p(1),
      (0, 23) -> FMC.LA_TX_n(1),
      (0, 24) -> FMC.LA_TX_p(0),
      (0, 25) -> FMC.LA_TX_n(0),
      (0, 26) -> FMC.LA_TX_p(2),
      (0, 27) -> FMC.LA_TX_n(2),
      (0, 28) -> FMC.LA_TX_p(12),
      (0, 29) -> FMC.LA_TX_n(12),
      (0, 30) -> FMC.LA_TX_p(13),
      (0, 31) -> FMC.LA_TX_n(13),
      (0, 32) -> FMC.LA_TX_p(14),
      (0, 33) -> FMC.LA_TX_n(14),
      (0, 34) -> FMC.LA_TX_p(15),
      (0, 35) -> FMC.LA_TX_n(15),
      (1, 0) -> FMC.LA_RX_p(4),
      (1, 1) -> FMC.LA_RX_n(4),
      (1, 2) -> FMC.LA_RX_CLK_p,
      (1, 3) -> FMC.LA_RX_CLK_n,
      (1, 4) -> FMC.LA_RX_p(2),
      (1, 5) -> FMC.LA_RX_n(2),
      (1, 6) -> FMC.LA_RX_p(0),
      (1, 7) -> FMC.LA_RX_n(0),
      (1, 10) -> FMC.LA_RX_p(1),
      (1, 11) -> FMC.LA_RX_n(1),
      (1, 12) -> FMC.LA_RX_p(3),
      (1, 13) -> FMC.LA_RX_n(3),
      (1, 14) -> FMC.LA_RX_p(6),
      (1, 15) -> FMC.LA_RX_n(6),
      (1, 16) -> FMC.LA_RX_p(5),
      (1, 17) -> FMC.LA_RX_n(5),
      (1, 18) -> FMC.LA_RX_p(8),
      (1, 19) -> FMC.LA_RX_n(8),
      (1, 20) -> FMC.LA_RX_p(7),
      (1, 21) -> FMC.LA_RX_n(7),
      (1, 22) -> FMC.LA_RX_p(9),
      (1, 23) -> FMC.LA_RX_n(9),
      (1, 24) -> FMC.LA_RX_p(10),
      (1, 25) -> FMC.LA_RX_n(10),
      (1, 26) -> FMC.LA_RX_p(11),
      (1, 27) -> FMC.LA_RX_n(11),
      (1, 28) -> FMC.LA_RX_p(12),
      (1, 29) -> FMC.LA_RX_n(12),
      (1, 30) -> FMC.LA_RX_p(13),
      (1, 31) -> FMC.LA_RX_n(13),
      (1, 32) -> FMC.LA_RX_p(14),
      (1, 33) -> FMC.LA_RX_n(14),
      (1, 34) -> FMC.LA_TX_p(16),
      (1, 35) -> FMC.LA_TX_n(16),
    )
    val ext = if(FMC.ext) Map (
      (1, 8) -> FMC.HA_RX_p.get(8),
      (1, 9) -> FMC.HA_RX_n.get(8),
      (2, 0) -> FMC.HA_RX_CLK_p.get,
      (2, 1) -> FMC.HA_RX_CLK_n.get,
      (2, 2) -> FMC.HA_TX_CLK_p.get,
      (2, 3) -> FMC.HA_TX_CLK_n.get,
      (2, 4) -> FMC.HA_RX_p.get(0),
      (2, 5) -> FMC.HA_RX_n.get(0),
      (2, 6) -> FMC.HA_RX_p.get(1),
      (2, 7) -> FMC.HA_RX_n.get(1),
      (2, 8) -> FMC.HA_RX_p.get(2),
      (2, 9) -> FMC.HA_RX_n.get(2),
      (2, 10) -> FMC.HA_RX_p.get(3),
      (2, 11) -> FMC.HA_RX_n.get(3),
      (2, 12) -> FMC.HA_RX_p.get(4),
      (2, 13) -> FMC.HA_RX_n.get(4),
      (2, 14) -> FMC.HA_RX_p.get(5),
      (2, 15) -> FMC.HA_RX_n.get(5),
      (2, 16) -> FMC.HA_RX_p.get(6),
      (2, 17) -> FMC.HA_RX_n.get(6),
      (2, 18) -> FMC.HA_RX_p.get(7),
      (2, 19) -> FMC.HA_RX_n.get(7),
      (2, 20) -> FMC.HA_TX_p.get(0),
      (2, 21) -> FMC.HA_TX_n.get(0),
      (2, 22) -> FMC.HA_TX_p.get(1),
      (2, 23) -> FMC.HA_TX_n.get(1),
      (2, 24) -> FMC.HA_TX_p.get(2),
      (2, 25) -> FMC.HA_TX_n.get(2),
      (2, 26) -> FMC.HA_TX_p.get(3),
      (2, 27) -> FMC.HA_TX_n.get(3),
      (2, 28) -> FMC.HA_TX_p.get(4),
      (2, 29) -> FMC.HA_TX_n.get(4),
      (2, 30) -> FMC.HA_TX_p.get(5),
      (2, 31) -> FMC.HA_TX_n.get(5),
      (2, 32) -> FMC.HA_TX_p.get(6),
      (2, 33) -> FMC.HA_TX_n.get(6),
      (2, 34) -> FMC.HA_TX_p.get(7),
      (2, 35) -> FMC.HA_TX_n.get(7),
      (2, 0) -> FMC.HB_RX_CLK_p.get,
      (2, 1) -> FMC.HB_RX_CLK_n.get,
      (2, 2) -> FMC.HB_TX_CLK_p.get,
      (2, 3) -> FMC.HB_TX_CLK_n.get,
      (2, 4) -> FMC.HB_RX_p.get(0),
      (2, 5) -> FMC.HB_RX_n.get(0),
      (2, 6) -> FMC.HB_RX_p.get(1),
      (2, 7) -> FMC.HB_RX_n.get(1),
      (2, 8) -> FMC.HB_RX_p.get(2),
      (2, 9) -> FMC.HB_RX_n.get(2),
      (2, 10) -> FMC.HB_RX_p.get(3),
      (2, 11) -> FMC.HB_RX_n.get(3),
      (2, 12) -> FMC.HB_RX_p.get(4),
      (2, 13) -> FMC.HB_RX_n.get(4),
      (2, 14) -> FMC.HB_RX_p.get(5),
      (2, 15) -> FMC.HB_RX_n.get(5),
      (2, 16) -> FMC.HB_RX_p.get(6),
      (2, 17) -> FMC.HB_RX_n.get(6),
      (2, 18) -> FMC.HB_RX_p.get(7),
      (2, 19) -> FMC.HB_RX_n.get(7),
      (2, 20) -> FMC.HB_TX_p.get(0),
      (2, 21) -> FMC.HB_TX_n.get(0),
      (2, 22) -> FMC.HB_TX_p.get(1),
      (2, 23) -> FMC.HB_TX_n.get(1),
      (2, 24) -> FMC.HB_TX_p.get(2),
      (2, 25) -> FMC.HB_TX_n.get(2),
      (2, 26) -> FMC.HB_TX_p.get(3),
      (2, 27) -> FMC.HB_TX_n.get(3),
      (2, 28) -> FMC.HB_TX_p.get(4),
      (2, 29) -> FMC.HB_TX_n.get(4),
      (2, 30) -> FMC.HB_TX_p.get(5),
      (2, 31) -> FMC.HB_TX_n.get(5),
      (2, 32) -> FMC.HB_TX_p.get(6),
      (2, 33) -> FMC.HB_TX_n.get(6),
      (2, 34) -> FMC.HB_TX_p.get(7),
      (2, 35) -> FMC.HB_TX_n.get(7),
    ) else Map()
    default ++ ext
  }
  def apply(n: Int, pu: Int, FMC: FMCTR5): Analog = {
    val p:Int = pu match {
      case it if 1 to 10 contains it => pu - 1
      case it if 13 to 28 contains it => pu - 3
      case it if 31 to 40 contains it => pu - 5
      case _ => throw new RuntimeException(s"J${n}_${pu} is a VDD or a GND")
    }
    npmap(FMC)((n, p))
  }
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
          case 0 => if (get) c := GET(FMC.CLK_M2C_p(0)) else PUT(c, FMC.CLK_M2C_p(0))
          case 1 => if (get) c := GET(FMC.CLK_M2C_n(0)) else PUT(c, FMC.CLK_M2C_n(0))
          case _ => if (get) c := ALT_IOBUF(npmap(FMC)((n, p))) else ALT_IOBUF(npmap(FMC)((n, p)), c)
        }
      case _ => if (get) c := ALT_IOBUF(npmap(FMC)((n, p))) else ALT_IOBUF(npmap(FMC)((n, p)), c)
    }
  }
}

// Based on layout of the TR5.sch done by Duy
trait WithFPGATR5ToChipConnect extends WithFPGATR5InternNoChipCreate with WithFPGATR5InternConnect {
  this: FPGATR5Shell =>

  // ******* Ahn-Dao section ******
  def FMC = FMCA
  p(ExternConn).version match {
    case 1 =>
      val MEMSER_GPIO = 0
      val EXTSER_GPIO = 1
      PUT(intern.sys_clk.asBool, FMC.CLK_M2C_p(1))
      ConnectFMCGPIO(MEMSER_GPIO, 5, intern.rst_n, false, FMC)
      // ExtSerMem
      intern.memser.foreach { memser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(MEMSER_GPIO, 9, in_bits(7), false, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 10, in_bits(6), false, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 13, in_bits(5), false, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 14, in_bits(4), false, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 15, in_bits(3), false, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 16, in_bits(2), false, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 17, in_bits(1), false, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 18, in_bits(0), false, FMC)
        in_bits := memser.in.bits.asBools
        ConnectFMCGPIO(MEMSER_GPIO, 19, memser.in.valid, false, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 20, memser.out.ready, false, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 21, memser.in.ready, true, FMC)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(MEMSER_GPIO, 22, out_bits(7), true, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 23, out_bits(6), true, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 24, out_bits(5), true, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 25, out_bits(4), true, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 26, out_bits(3), true, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 27, out_bits(2), true, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 28, out_bits(1), true, FMC)
        ConnectFMCGPIO(MEMSER_GPIO, 31, out_bits(0), true, FMC)
        memser.out.bits := out_bits.asUInt
        ConnectFMCGPIO(MEMSER_GPIO, 32, memser.out.valid, true, FMC)
      }
      // ExtSerBus
      intern.extser.foreach{ extser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(EXTSER_GPIO, 9, in_bits(7), false, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 10, in_bits(6), false, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 13, in_bits(5), false, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 14, in_bits(4), false, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 15, in_bits(3), false, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 16, in_bits(2), false, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 17, in_bits(1), false, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 18, in_bits(0), false, FMC)
        in_bits := extser.in.bits.asBools
        ConnectFMCGPIO(EXTSER_GPIO, 19, extser.in.valid, false, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 20, extser.out.ready, false, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 21, extser.in.ready, true, FMC)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(EXTSER_GPIO, 22, out_bits(7), true, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 23, out_bits(6), true, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 24, out_bits(5), true, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 25, out_bits(4), true, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 26, out_bits(3), true, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 27, out_bits(2), true, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 28, out_bits(1), true, FMC)
        ConnectFMCGPIO(EXTSER_GPIO, 31, out_bits(0), true, FMC)
        extser.out.bits := out_bits.asUInt
        ConnectFMCGPIO(EXTSER_GPIO, 32, extser.out.valid, true, FMC)
      }
    case 0 =>
      ConnectFMCGPIO(0, 1, intern.sys_clk.asBool, false, FMC)
      // PUT(intern.sys_clk.asBool, FMCSER.CLK_M2C_n(1)) // Previous OtherClock
      intern.usbClk.foreach{ a => PUT(a.asBool, FMC.CLK_M2C_n(1)) }
      ConnectFMCGPIO(1, 15, intern.rst_n, false, FMC)
      // ExtSerMem
      intern.memser.foreach { memser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(0, 31, in_bits(7), false, FMC)
        ConnectFMCGPIO(0, 33, in_bits(6), false, FMC)
        ConnectFMCGPIO(0, 35, in_bits(5), false, FMC)
        ConnectFMCGPIO(0, 37, in_bits(4), false, FMC)
        ConnectFMCGPIO(0, 39, in_bits(3), false, FMC)
        ConnectFMCGPIO(1, 1, in_bits(2), false, FMC)
        ConnectFMCGPIO(1, 5, in_bits(1), false, FMC)
        ConnectFMCGPIO(1, 7, in_bits(0), false, FMC)
        in_bits := memser.in.bits.asBools
        ConnectFMCGPIO(1, 9, memser.in.valid, false, FMC)
        ConnectFMCGPIO(1, 13, memser.out.ready, false, FMC)
        ConnectFMCGPIO(2, 5, memser.in.ready, true, FMC)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(2, 7, out_bits(7), true, FMC)
        ConnectFMCGPIO(2, 9, out_bits(6), true, FMC)
        ConnectFMCGPIO(2, 13, out_bits(5), true, FMC)
        ConnectFMCGPIO(2, 15, out_bits(4), true, FMC)
        ConnectFMCGPIO(2, 17, out_bits(3), true, FMC)
        ConnectFMCGPIO(2, 19, out_bits(2), true, FMC)
        ConnectFMCGPIO(2, 21, out_bits(1), true, FMC)
        ConnectFMCGPIO(2, 23, out_bits(0), true, FMC)
        memser.out.bits := out_bits.asUInt
        ConnectFMCGPIO(2, 25, memser.out.valid, true, FMC)
      }
      // ExtSerBus
      intern.extser.foreach{ extser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(0, 5, in_bits(7), false, FMC)
        ConnectFMCGPIO(0, 7, in_bits(6), false, FMC)
        ConnectFMCGPIO(0, 9, in_bits(5), false, FMC)
        ConnectFMCGPIO(0, 13, in_bits(4), false, FMC)
        ConnectFMCGPIO(0, 15, in_bits(3), false, FMC)
        ConnectFMCGPIO(0, 17, in_bits(2), false, FMC)
        ConnectFMCGPIO(0, 19, in_bits(1), false, FMC)
        ConnectFMCGPIO(0, 21, in_bits(0), false, FMC)
        in_bits := extser.in.bits.asBools
        ConnectFMCGPIO(0, 23, extser.in.valid, false, FMC)
        ConnectFMCGPIO(0, 25, extser.out.ready, false, FMC)
        ConnectFMCGPIO(1, 17, extser.in.ready, true, FMC)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(1, 19, out_bits(7), true, FMC)
        ConnectFMCGPIO(1, 21, out_bits(6), true, FMC)
        ConnectFMCGPIO(1, 25, out_bits(5), true, FMC)
        ConnectFMCGPIO(1, 27, out_bits(4), true, FMC)
        ConnectFMCGPIO(1, 31, out_bits(3), true, FMC)
        ConnectFMCGPIO(1, 33, out_bits(2), true, FMC)
        ConnectFMCGPIO(1, 35, out_bits(1), true, FMC)
        ConnectFMCGPIO(1, 37, out_bits(0), true, FMC)
        extser.out.bits := out_bits.asUInt
        ConnectFMCGPIO(1, 39, extser.out.valid, true, FMC)
      }
    case 2 =>
      // ROHM 180 2021 4 period 2nd chip (214R4252) (TEE HW "TRASIO" chip)
      def GPIO1 = 1 // GPIO 1 of PCB is connected to GPIO-1 of F2G
      def GPIO2 = 3 // GPIO 2 of PCB is connected to GPIO-3 of F2G

      // Phase 1 - Clocks and Resets
      ConnectFMCGPIO(GPIO2, 39, intern.sys_clk.asBool, false, FMC)
      (intern.aclocks zip intern.namedclocks).filter(_._2.contains("mbus")).foreach {
        case (aclk, anam) =>
          ConnectFMCGPIO(GPIO2, 40, aclk.asBool, false, FMC) // For ChildClock
      }
      (intern.aclocks zip intern.namedclocks).filter(_._2.contains("rtc_clock")).foreach {
        case (aclk, anam) =>
          ConnectFMCGPIO(GPIO2, 38, aclk.asBool, false, FMC)
      }
      ConnectFMCGPIO(GPIO2, 37, intern.sys_clk.asBool, false, FMC) // For SDRAMClock
      ConnectFMCGPIO(GPIO2, 36, intern.rst_n, false, FMC) // For jrst_n
      ConnectFMCGPIO(GPIO2, 35, intern.rst_n, false, FMC)

      // Phase 2 - ExtSerMem
      intern.memser.foreach { memser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(GPIO1, 5, in_bits(7), false, FMC)
        ConnectFMCGPIO(GPIO1, 6, in_bits(6), false, FMC)
        ConnectFMCGPIO(GPIO1, 7, in_bits(5), false, FMC)
        ConnectFMCGPIO(GPIO1, 8, in_bits(4), false, FMC)
        ConnectFMCGPIO(GPIO1, 9, in_bits(3), false, FMC)
        ConnectFMCGPIO(GPIO1, 10, in_bits(2), false, FMC)
        ConnectFMCGPIO(GPIO1, 13, in_bits(1), false, FMC)
        ConnectFMCGPIO(GPIO1, 14, in_bits(0), false, FMC)
        in_bits := memser.in.bits.asBools
        ConnectFMCGPIO(GPIO1, 4, memser.in.valid, false, FMC)
        ConnectFMCGPIO(GPIO1, 2, memser.in.ready, true, FMC)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(GPIO1, 17, out_bits(7), true, FMC)
        ConnectFMCGPIO(GPIO1, 18, out_bits(6), true, FMC)
        ConnectFMCGPIO(GPIO1, 19, out_bits(5), true, FMC)
        ConnectFMCGPIO(GPIO1, 20, out_bits(4), true, FMC)
        ConnectFMCGPIO(GPIO1, 21, out_bits(3), true, FMC)
        ConnectFMCGPIO(GPIO1, 22, out_bits(2), true, FMC)
        ConnectFMCGPIO(GPIO1, 23, out_bits(1), true, FMC)
        ConnectFMCGPIO(GPIO1, 24, out_bits(0), true, FMC)
        memser.out.bits := out_bits.asUInt
        ConnectFMCGPIO(GPIO1, 15, memser.out.ready, false, FMC)
        ConnectFMCGPIO(GPIO1, 16, memser.out.valid, true, FMC)
      }
      // Phase 3 - ExtSerBus
      intern.extser.foreach{ extser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(GPIO1, 27, in_bits(7), false, FMC)
        ConnectFMCGPIO(GPIO1, 28, in_bits(6), false, FMC)
        ConnectFMCGPIO(GPIO1, 31, in_bits(5), false, FMC)
        ConnectFMCGPIO(GPIO1, 32, in_bits(4), false, FMC)
        ConnectFMCGPIO(GPIO1, 33, in_bits(3), false, FMC)
        ConnectFMCGPIO(GPIO1, 34, in_bits(2), false, FMC)
        ConnectFMCGPIO(GPIO1, 35, in_bits(1), false, FMC)
        ConnectFMCGPIO(GPIO1, 36, in_bits(0), false, FMC)
        in_bits := extser.in.bits.asBools
        ConnectFMCGPIO(GPIO1, 25, extser.in.ready, true, FMC)
        ConnectFMCGPIO(GPIO1, 26, extser.in.valid, false, FMC)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectFMCGPIO(GPIO1, 39, out_bits(7), true, FMC)
        ConnectFMCGPIO(GPIO1, 40, out_bits(6), true, FMC)
        ConnectFMCGPIO(GPIO2, 2, out_bits(5), true, FMC)
        ConnectFMCGPIO(GPIO2, 4, out_bits(4), true, FMC)
        ConnectFMCGPIO(GPIO2, 5, out_bits(3), true, FMC)
        ConnectFMCGPIO(GPIO2, 6, out_bits(2), true, FMC)
        ConnectFMCGPIO(GPIO2, 7, out_bits(1), true, FMC)
        ConnectFMCGPIO(GPIO2, 8, out_bits(0), true, FMC)
        extser.out.bits := out_bits.asUInt
        ConnectFMCGPIO(GPIO1, 37, extser.out.ready, false, FMC)
        ConnectFMCGPIO(GPIO1, 38, extser.out.valid, true, FMC)
      }
    case _ =>// ******* Duy section ******
      // NOTES:
      def GPIO0 = 0
      def GPIO1 = 1
      def GPIO2 = 2
      def GPIO3 = 3

      // From intern = Clocks and resets
      ConnectFMCGPIO(GPIO2, 2, intern.sys_clk.asBool, false, FMC) // Previous ChildClock
      ConnectFMCGPIO(GPIO3, 2, !intern.rst_n, false, FMC) // Previous ChildReset
      ConnectFMCGPIO(GPIO2, 4, intern.sys_clk.asBool, false, FMC)
      ConnectFMCGPIO(GPIO3, 4, intern.rst_n, false, FMC)
      ConnectFMCGPIO(GPIO3, 5, intern.rst_n, false, FMC) // Previous jrst_n
      // Memory port
      intern.tlport.foreach{ case tlport =>
        ConnectFMCGPIO(GPIO3, 1, tlport.a.valid, true, FMC)
        ConnectFMCGPIO(GPIO3, 6, tlport.a.ready, false, FMC)
        require(tlport.a.bits.opcode.getWidth == 3, s"${tlport.a.bits.opcode.getWidth}")
        val a_opcode = Wire(Vec(3, Bool()))
        ConnectFMCGPIO(GPIO3, 3, a_opcode(2), true, FMC)
        ConnectFMCGPIO(GPIO3, 7, a_opcode(1), true, FMC)
        ConnectFMCGPIO(GPIO3, 8, a_opcode(0), true, FMC)
        tlport.a.bits.opcode := a_opcode.asUInt
        require(tlport.a.bits.param.getWidth == 3, s"${tlport.a.bits.param.getWidth}")
        val a_param = Wire(Vec(3, Bool()))
        ConnectFMCGPIO(GPIO3, 9, a_param(2), true, FMC)
        ConnectFMCGPIO(GPIO3, 10, a_param(1), true, FMC)
        ConnectFMCGPIO(GPIO3, 13, a_param(0), true, FMC)
        tlport.a.bits.param := a_param.asUInt
        val a_size = Wire(Vec(3, Bool()))
        require(tlport.a.bits.size.getWidth == 3, s"${tlport.a.bits.size.getWidth}")
        ConnectFMCGPIO(GPIO3, 14, a_size(2), true, FMC)
        ConnectFMCGPIO(GPIO3, 15, a_size(1), true, FMC)
        ConnectFMCGPIO(GPIO3, 16, a_size(0), true, FMC)
        tlport.a.bits.size := a_size.asUInt
        require(tlport.a.bits.source.getWidth == 6, s"${tlport.a.bits.source.getWidth}")
        val a_source = Wire(Vec(6, Bool()))
        ConnectFMCGPIO(GPIO3, 17, a_source(5), true, FMC)
        ConnectFMCGPIO(GPIO3, 18, a_source(4), true, FMC)
        ConnectFMCGPIO(GPIO3, 19, a_source(3), true, FMC)
        ConnectFMCGPIO(GPIO3, 20, a_source(2), true, FMC)
        ConnectFMCGPIO(GPIO3, 21, a_source(1), true, FMC)
        ConnectFMCGPIO(GPIO3, 22, a_source(0), true, FMC)
        tlport.a.bits.source := a_source.asUInt
        require(tlport.a.bits.address.getWidth == 32, s"${tlport.a.bits.address.getWidth}")
        val a_address = Wire(Vec(32, Bool()))
        ConnectFMCGPIO(GPIO3, 23, a_address(31), true, FMC)
        ConnectFMCGPIO(GPIO3, 24, a_address(30), true, FMC)
        ConnectFMCGPIO(GPIO3, 25, a_address(29), true, FMC)
        ConnectFMCGPIO(GPIO3, 26, a_address(28), true, FMC)
        ConnectFMCGPIO(GPIO3, 27, a_address(27), true, FMC)
        ConnectFMCGPIO(GPIO3, 28, a_address(26), true, FMC)
        ConnectFMCGPIO(GPIO3, 31, a_address(25), true, FMC)
        ConnectFMCGPIO(GPIO3, 32, a_address(24), true, FMC)
        ConnectFMCGPIO(GPIO3, 33, a_address(23), true, FMC)
        ConnectFMCGPIO(GPIO3, 34, a_address(22), true, FMC)
        ConnectFMCGPIO(GPIO3, 35, a_address(21), true, FMC)
        ConnectFMCGPIO(GPIO3, 36, a_address(20), true, FMC)
        ConnectFMCGPIO(GPIO3, 37, a_address(19), true, FMC)
        ConnectFMCGPIO(GPIO3, 38, a_address(18), true, FMC)
        ConnectFMCGPIO(GPIO3, 39, a_address(17), true, FMC)
        ConnectFMCGPIO(GPIO3, 40, a_address(16), true, FMC)
        ConnectFMCGPIO(GPIO1,  1, a_address(15), true, FMC)
        ConnectFMCGPIO(GPIO1,  2, a_address(14), true, FMC)
        ConnectFMCGPIO(GPIO1,  3, a_address(13), true, FMC)
        ConnectFMCGPIO(GPIO1,  4, a_address(12), true, FMC)
        ConnectFMCGPIO(GPIO1,  5, a_address(11), true, FMC)
        ConnectFMCGPIO(GPIO1,  6, a_address(10), true, FMC)
        ConnectFMCGPIO(GPIO1,  7, a_address( 9), true, FMC)
        ConnectFMCGPIO(GPIO1,  8, a_address( 8), true, FMC)
        ConnectFMCGPIO(GPIO1,  9, a_address( 7), true, FMC)
        ConnectFMCGPIO(GPIO1, 10, a_address( 6), true, FMC)
        ConnectFMCGPIO(GPIO1, 13, a_address( 5), true, FMC)
        ConnectFMCGPIO(GPIO1, 14, a_address( 4), true, FMC)
        ConnectFMCGPIO(GPIO1, 15, a_address( 3), true, FMC)
        ConnectFMCGPIO(GPIO1, 16, a_address( 2), true, FMC)
        ConnectFMCGPIO(GPIO1, 17, a_address( 1), true, FMC)
        ConnectFMCGPIO(GPIO1, 18, a_address( 0), true, FMC)
        tlport.a.bits.address := a_address.asUInt
        require(tlport.a.bits.mask.getWidth == 4, s"${tlport.a.bits.mask.getWidth}")
        val a_mask = Wire(Vec(4, Bool()))
        ConnectFMCGPIO(GPIO1, 19, a_mask(3), true, FMC)
        ConnectFMCGPIO(GPIO1, 20, a_mask(2), true, FMC)
        ConnectFMCGPIO(GPIO1, 21, a_mask(1), true, FMC)
        ConnectFMCGPIO(GPIO1, 22, a_mask(0), true, FMC)
        tlport.a.bits.mask := a_mask.asUInt
        require(tlport.a.bits.data.getWidth == 32, s"${tlport.a.bits.data.getWidth}")
        val a_data = Wire(Vec(32, Bool()))
        ConnectFMCGPIO(GPIO1, 23, a_data(31), true, FMC)
        ConnectFMCGPIO(GPIO1, 24, a_data(30), true, FMC)
        ConnectFMCGPIO(GPIO1, 25, a_data(29), true, FMC)
        ConnectFMCGPIO(GPIO1, 26, a_data(28), true, FMC)
        ConnectFMCGPIO(GPIO1, 27, a_data(27), true, FMC)
        ConnectFMCGPIO(GPIO1, 28, a_data(26), true, FMC)
        ConnectFMCGPIO(GPIO1, 31, a_data(25), true, FMC)
        ConnectFMCGPIO(GPIO1, 32, a_data(24), true, FMC)
        ConnectFMCGPIO(GPIO1, 33, a_data(23), true, FMC)
        ConnectFMCGPIO(GPIO1, 34, a_data(22), true, FMC)
        ConnectFMCGPIO(GPIO1, 35, a_data(21), true, FMC)
        ConnectFMCGPIO(GPIO1, 36, a_data(20), true, FMC)
        ConnectFMCGPIO(GPIO1, 37, a_data(19), true, FMC)
        ConnectFMCGPIO(GPIO1, 38, a_data(18), true, FMC)
        ConnectFMCGPIO(GPIO1, 39, a_data(17), true, FMC)
        ConnectFMCGPIO(GPIO1, 40, a_data(16), true, FMC)
        ConnectFMCGPIO(GPIO0,  1, a_data(15), true, FMC)
        ConnectFMCGPIO(GPIO0,  2, a_data(14), true, FMC)
        ConnectFMCGPIO(GPIO0,  3, a_data(13), true, FMC)
        ConnectFMCGPIO(GPIO0,  4, a_data(12), true, FMC)
        ConnectFMCGPIO(GPIO0,  5, a_data(11), true, FMC)
        ConnectFMCGPIO(GPIO0,  6, a_data(10), true, FMC)
        ConnectFMCGPIO(GPIO0,  7, a_data( 9), true, FMC)
        ConnectFMCGPIO(GPIO0,  8, a_data( 8), true, FMC)
        ConnectFMCGPIO(GPIO0,  9, a_data( 7), true, FMC)
        ConnectFMCGPIO(GPIO0, 10, a_data( 6), true, FMC)
        ConnectFMCGPIO(GPIO0, 13, a_data( 5), true, FMC)
        ConnectFMCGPIO(GPIO0, 14, a_data( 4), true, FMC)
        ConnectFMCGPIO(GPIO0, 15, a_data( 3), true, FMC)
        ConnectFMCGPIO(GPIO0, 16, a_data( 2), true, FMC)
        ConnectFMCGPIO(GPIO0, 17, a_data( 1), true, FMC)
        ConnectFMCGPIO(GPIO0, 18, a_data( 0), true, FMC)
        tlport.a.bits.data := a_data.asUInt
        ConnectFMCGPIO(GPIO0, 19, tlport.a.bits.corrupt, true, FMC)
        ConnectFMCGPIO(GPIO0, 20, tlport.d.ready, true, FMC)
        ConnectFMCGPIO(GPIO0, 21, tlport.d.valid, false, FMC)
        require(tlport.d.bits.opcode.getWidth == 3, s"${tlport.d.bits.opcode.getWidth}")
        ConnectFMCGPIO(GPIO0, 22, tlport.d.bits.opcode(2), false, FMC)
        ConnectFMCGPIO(GPIO0, 23, tlport.d.bits.opcode(1), false, FMC)
        ConnectFMCGPIO(GPIO0, 24, tlport.d.bits.opcode(0), false, FMC)
        require(tlport.d.bits.param.getWidth == 2, s"${tlport.d.bits.param.getWidth}")
        ConnectFMCGPIO(GPIO0, 25, tlport.d.bits.param(1), false, FMC)
        ConnectFMCGPIO(GPIO0, 26, tlport.d.bits.param(0), false, FMC)
        require(tlport.d.bits.size.getWidth == 3, s"${tlport.d.bits.size.getWidth}")
        ConnectFMCGPIO(GPIO0, 27, tlport.d.bits.size(2), false, FMC)
        ConnectFMCGPIO(GPIO0, 40, tlport.d.bits.size(1), false, FMC)
        ConnectFMCGPIO(GPIO0, 39, tlport.d.bits.size(0), false, FMC)
        require(tlport.d.bits.source.getWidth == 6, s"${tlport.d.bits.source.getWidth}")
        ConnectFMCGPIO(GPIO0, 37, tlport.d.bits.source(5), false, FMC)
        ConnectFMCGPIO(GPIO0, 38, tlport.d.bits.source(4), false, FMC)
        ConnectFMCGPIO(GPIO0, 35, tlport.d.bits.source(3), false, FMC)
        ConnectFMCGPIO(GPIO0, 36, tlport.d.bits.source(2), false, FMC)
        ConnectFMCGPIO(GPIO0, 33, tlport.d.bits.source(1), false, FMC)
        ConnectFMCGPIO(GPIO0, 34, tlport.d.bits.source(0), false, FMC)
        require(tlport.d.bits.sink.getWidth == 1, s"${tlport.d.bits.sink.getWidth}")
        ConnectFMCGPIO(GPIO0, 32, tlport.d.bits.sink(0), false, FMC)
        ConnectFMCGPIO(GPIO0, 31, tlport.d.bits.denied, false, FMC)
        ConnectFMCGPIO(GPIO0, 28, tlport.d.bits.corrupt, false, FMC)
        require(tlport.d.bits.data.getWidth == 32, s"${tlport.d.bits.data.getWidth}")
        ConnectFMCGPIO(GPIO2, 5, tlport.d.bits.data(31), false, FMC)
        ConnectFMCGPIO(GPIO2, 6, tlport.d.bits.data(30), false, FMC)
        ConnectFMCGPIO(GPIO2, 7, tlport.d.bits.data(29), false, FMC)
        ConnectFMCGPIO(GPIO2, 8, tlport.d.bits.data(28), false, FMC)
        ConnectFMCGPIO(GPIO2, 9, tlport.d.bits.data(27), false, FMC)
        ConnectFMCGPIO(GPIO2, 10, tlport.d.bits.data(26), false, FMC)
        ConnectFMCGPIO(GPIO2, 13, tlport.d.bits.data(25), false, FMC)
        ConnectFMCGPIO(GPIO2, 14, tlport.d.bits.data(24), false, FMC)
        ConnectFMCGPIO(GPIO2, 15, tlport.d.bits.data(23), false, FMC)
        ConnectFMCGPIO(GPIO2, 16, tlport.d.bits.data(22), false, FMC)
        ConnectFMCGPIO(GPIO2, 17, tlport.d.bits.data(21), false, FMC)
        ConnectFMCGPIO(GPIO2, 18, tlport.d.bits.data(20), false, FMC)
        ConnectFMCGPIO(GPIO2, 19, tlport.d.bits.data(19), false, FMC)
        ConnectFMCGPIO(GPIO2, 20, tlport.d.bits.data(18), false, FMC)
        ConnectFMCGPIO(GPIO2, 21, tlport.d.bits.data(17), false, FMC)
        ConnectFMCGPIO(GPIO2, 22, tlport.d.bits.data(16), false, FMC)
        ConnectFMCGPIO(GPIO2, 23, tlport.d.bits.data(15), false, FMC)
        ConnectFMCGPIO(GPIO2, 24, tlport.d.bits.data(14), false, FMC)
        ConnectFMCGPIO(GPIO2, 25, tlport.d.bits.data(13), false, FMC)
        ConnectFMCGPIO(GPIO2, 26, tlport.d.bits.data(12), false, FMC)
        ConnectFMCGPIO(GPIO2, 27, tlport.d.bits.data(11), false, FMC)
        ConnectFMCGPIO(GPIO2, 28, tlport.d.bits.data(10), false, FMC)
        ConnectFMCGPIO(GPIO2, 31, tlport.d.bits.data( 9), false, FMC)
        ConnectFMCGPIO(GPIO2, 32, tlport.d.bits.data( 8), false, FMC)
        ConnectFMCGPIO(GPIO2, 33, tlport.d.bits.data( 7), false, FMC)
        ConnectFMCGPIO(GPIO2, 34, tlport.d.bits.data( 6), false, FMC)
        ConnectFMCGPIO(GPIO2, 35, tlport.d.bits.data( 5), false, FMC)
        ConnectFMCGPIO(GPIO2, 36, tlport.d.bits.data( 4), false, FMC)
        ConnectFMCGPIO(GPIO2, 37, tlport.d.bits.data( 3), false, FMC)
        ConnectFMCGPIO(GPIO2, 38, tlport.d.bits.data( 2), false, FMC)
        ConnectFMCGPIO(GPIO2, 39, tlport.d.bits.data( 1), false, FMC)
        ConnectFMCGPIO(GPIO2, 40, tlport.d.bits.data( 0), false, FMC)
      }
  }

  // ******** Misc part ********

  // LEDs
  PUT(intern.mem_status_local_cal_fail, LED(3))
  PUT(intern.mem_status_local_cal_success, LED(2))
  PUT(intern.mem_status_local_init_done, LED(1))
  // Clocks to the outside
  ALT_IOBUF(SMA_CLKOUT_p, intern.sys_clk.asBool)
  ALT_IOBUF(SMA_CLKOUT_n, intern.sys_clk.asBool) // For async clock
  // NOTE: usbClk cannot exist
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
      attach(SMA_CLKOUT_p, chip.asInstanceOf[HasTEEHWClockGroupChipImp].clockxi)
      chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs.foreach{ a =>
        attach(a.usbClk, SMA_CLKOUT_n)
      }
      attach(ConnectFMCGPIO(MEMSER_GPIO, 5, FMCSER), chip.asInstanceOf[HasTEEHWClockGroupChipImp].rstn)
      // ExtSerMem
      chip.asInstanceOf[HasTEEHWPeripheryExtSerMemChipImp].memser.foreach { memser =>
        attach(ConnectFMCGPIO(MEMSER_GPIO, 9, FMCSER), memser.in.bits(7))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 10, FMCSER), memser.in.bits(6))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 13, FMCSER), memser.in.bits(5))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 14, FMCSER), memser.in.bits(4))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 15, FMCSER), memser.in.bits(3))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 16, FMCSER), memser.in.bits(2))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 17, FMCSER), memser.in.bits(1))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 18, FMCSER), memser.in.bits(0))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 19, FMCSER), memser.in.valid)
        attach(ConnectFMCGPIO(MEMSER_GPIO, 20, FMCSER), memser.out.ready)
        attach(ConnectFMCGPIO(MEMSER_GPIO, 21, FMCSER), memser.in.ready)
        attach(ConnectFMCGPIO(MEMSER_GPIO, 22, FMCSER), memser.out.bits(7))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 23, FMCSER), memser.out.bits(6))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 24, FMCSER), memser.out.bits(5))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 25, FMCSER), memser.out.bits(4))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 26, FMCSER), memser.out.bits(3))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 27, FMCSER), memser.out.bits(2))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 28, FMCSER), memser.out.bits(1))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 31, FMCSER), memser.out.bits(0))
        attach(ConnectFMCGPIO(MEMSER_GPIO, 32, FMCSER), memser.out.valid)
      }
      // ExtSerBus
      chip.asInstanceOf[HasTEEHWPeripheryExtSerBusChipImp].extser.foreach{ extser =>
        val in_bits = Wire(Vec(8, Bool()))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 9, FMCSER), extser.in.bits(7))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 10, FMCSER), extser.in.bits(6))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 13, FMCSER), extser.in.bits(5))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 14, FMCSER), extser.in.bits(4))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 15, FMCSER), extser.in.bits(3))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 16, FMCSER), extser.in.bits(2))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 17, FMCSER), extser.in.bits(1))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 18, FMCSER), extser.in.bits(0))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 19, FMCSER), extser.in.valid)
        attach(ConnectFMCGPIO(EXTSER_GPIO, 20, FMCSER), extser.out.ready)
        attach(ConnectFMCGPIO(EXTSER_GPIO, 21, FMCSER), extser.in.ready)
        attach(ConnectFMCGPIO(EXTSER_GPIO, 22, FMCSER), extser.out.bits(7))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 23, FMCSER), extser.out.bits(6))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 24, FMCSER), extser.out.bits(5))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 25, FMCSER), extser.out.bits(4))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 26, FMCSER), extser.out.bits(3))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 27, FMCSER), extser.out.bits(2))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 28, FMCSER), extser.out.bits(1))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 31, FMCSER), extser.out.bits(0))
        attach(ConnectFMCGPIO(EXTSER_GPIO, 32, FMCSER), extser.out.valid)
      }
    case _ =>
      attach(ConnectFMCGPIO(0, 1, FMCSER), chip.asInstanceOf[HasTEEHWClockGroupChipImp].clockxi)
      chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs.foreach{ a => attach(a.usbClk, FMCSER.CLK_M2C_n(1)) }
      attach(ConnectFMCGPIO(1, 15, FMCSER), chip.asInstanceOf[HasTEEHWClockGroupChipImp].rstn)
      // ExtSerMem
      chip.asInstanceOf[HasTEEHWPeripheryExtSerMemChipImp].memser.foreach { memser =>
        val in_bits = Wire(Vec(8, Bool()))
        attach(ConnectFMCGPIO(0, 31, FMCSER), memser.in.bits(7))
        attach(ConnectFMCGPIO(0, 33, FMCSER), memser.in.bits(6))
        attach(ConnectFMCGPIO(0, 35, FMCSER), memser.in.bits(5))
        attach(ConnectFMCGPIO(0, 37, FMCSER), memser.in.bits(4))
        attach(ConnectFMCGPIO(0, 39, FMCSER), memser.in.bits(3))
        attach(ConnectFMCGPIO(1, 1, FMCSER), memser.in.bits(2))
        attach(ConnectFMCGPIO(1, 5, FMCSER), memser.in.bits(1))
        attach(ConnectFMCGPIO(1, 7, FMCSER), memser.in.bits(0))
        attach(ConnectFMCGPIO(1, 9, FMCSER), memser.in.valid)
        attach(ConnectFMCGPIO(1, 13, FMCSER), memser.out.ready)
        attach(ConnectFMCGPIO(2, 5, FMCSER), memser.in.ready)
        attach(ConnectFMCGPIO(2, 7, FMCSER), memser.out.bits(7))
        attach(ConnectFMCGPIO(2, 9, FMCSER), memser.out.bits(6))
        attach(ConnectFMCGPIO(2, 13, FMCSER), memser.out.bits(5))
        attach(ConnectFMCGPIO(2, 15, FMCSER), memser.out.bits(4))
        attach(ConnectFMCGPIO(2, 17, FMCSER), memser.out.bits(3))
        attach(ConnectFMCGPIO(2, 19, FMCSER), memser.out.bits(2))
        attach(ConnectFMCGPIO(2, 21, FMCSER), memser.out.bits(1))
        attach(ConnectFMCGPIO(2, 23, FMCSER), memser.out.bits(0))
        attach(ConnectFMCGPIO(2, 25, FMCSER), memser.out.valid)
      }
      // ExtSerBus
      chip.asInstanceOf[HasTEEHWPeripheryExtSerBusChipImp].extser.foreach{ extser =>
        val in_bits = Wire(Vec(8, Bool()))
        attach(ConnectFMCGPIO(0, 5, FMCSER), extser.in.bits(7))
        attach(ConnectFMCGPIO(0, 7, FMCSER), extser.in.bits(6))
        attach(ConnectFMCGPIO(0, 9, FMCSER), extser.in.bits(5))
        attach(ConnectFMCGPIO(0, 13, FMCSER), extser.in.bits(4))
        attach(ConnectFMCGPIO(0, 15, FMCSER), extser.in.bits(3))
        attach(ConnectFMCGPIO(0, 17, FMCSER), extser.in.bits(2))
        attach(ConnectFMCGPIO(0, 19, FMCSER), extser.in.bits(1))
        attach(ConnectFMCGPIO(0, 21, FMCSER), extser.in.bits(0))
        attach(ConnectFMCGPIO(0, 23, FMCSER), extser.in.valid)
        attach(ConnectFMCGPIO(0, 25, FMCSER), extser.out.ready)
        attach(ConnectFMCGPIO(1, 17, FMCSER), extser.in.ready)
        attach(ConnectFMCGPIO(1, 19, FMCSER), extser.out.bits(7))
        attach(ConnectFMCGPIO(1, 21, FMCSER), extser.out.bits(6))
        attach(ConnectFMCGPIO(1, 25, FMCSER), extser.out.bits(5))
        attach(ConnectFMCGPIO(1, 27, FMCSER), extser.out.bits(4))
        attach(ConnectFMCGPIO(1, 31, FMCSER), extser.out.bits(3))
        attach(ConnectFMCGPIO(1, 33, FMCSER), extser.out.bits(2))
        attach(ConnectFMCGPIO(1, 35, FMCSER), extser.out.bits(1))
        attach(ConnectFMCGPIO(1, 37, FMCSER), extser.out.bits(0))
        attach(ConnectFMCGPIO(1, 39, FMCSER), extser.out.valid)
      }
  }
}