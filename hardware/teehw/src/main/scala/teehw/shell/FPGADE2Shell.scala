package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.util.ResetCatchAndSync
import chipsalliance.rocketchip.config.Parameters
import sifive.blocks.devices.spi.{SPIFlashParams, SPIParams}
import uec.teehardware.devices.sdram._
import uec.teehardware.macros._
import uec.teehardware._

class DE2SDRAM extends Bundle {
  val CLK = Output(Bool())
  val CKE = Output(Bool())
  val CS_N = Output(Bool())
  val RAS_N = Output(Bool())
  val CAS_N = Output(Bool())
  val WE_N = Output(Bool())
  val DQM = Output(UInt(4.W))
  val ADDR = Output(UInt(13.W))
  val BA = Output(UInt(2.W))
  val DQ = Vec(32, Analog(1.W))
  def from_SDRAMIf(io: SDRAMIf) = {
    CLK := io.sdram_clk_o
    CKE := io.sdram_cke_o
    CS_N := io.sdram_cs_o
    RAS_N := io.sdram_ras_o
    CAS_N := io.sdram_cas_o
    WE_N := io.sdram_we_o
    DQM := io.sdram_dqm_o
    ADDR := io.sdram_addr_o
    BA := io.sdram_ba_o
    io.sdram_data_i := VecInit((io.sdram_data_o.asBools() zip DQ).map{
      case (o, an) =>
        val b = Module(new ALT_IOBUF)
        b.io.oe := io.sdram_drive_o
        b.io.i := o
        attach(b.io.io, an)
        b.io.o
    }).asUInt()
  }
}

class AUD_CODEC_DE2_PORT extends Bundle {
  val XCK = Output(Bool())
  val BCLK = Analog(1.W)
  val DACDAT = Output(Bool())
  val DACLRCK = Analog(1.W)
  val ADCDAT = Input(Bool())
  val ADCLRCK = Analog(1.W)
  val I2C_SCLK = Analog(1.W)
  val I2C_SDAT = Analog(1.W)
}

class pll extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val inclk0 = Input(Clock())
    val areset = Input(Bool())
    val c0 = Output(Clock())
    val c1 = Output(Clock())
    val locked = Output(Bool())
  })
  addResource("/Altera_DE2/pll/pll.v")
}

trait FPGADE2ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters
  val LEDR = IO(Vec(18, Output(Bool())))
  val LEDG = IO(Vec(9, Output(Bool())))
  val SW = IO(Vec(18, Input(Bool())))

  //val HSMC_D = IO(Vec(4, Analog(1.W)))
  //val HSMC_RX_D_P = IO(Vec(17, Analog(1.W)))
  //val HSMC_TX_D_P = IO(Vec(17, Analog(1.W)))
  //val HSMC_RX_D_N = IO(Vec(17, Analog(1.W)))
  //val HSMC_TX_D_N = IO(Vec(17, Analog(1.W)))

  val GPIO = IO(Vec(36, Analog(1.W)))

  //val AUD = IO(new AUD_CODEC_DE2_PORT)
  val DRAM = IO(new DE2SDRAM)

  val UART_TXD = IO(Analog(1.W))
  val UART_RXD = IO(Analog(1.W))

  val SD_CMD = IO(Analog(1.W))
  val SD_CLK = IO(Analog(1.W))
  val SD_WP_N = IO(Analog(1.W))
  val SD_DAT = IO(Vec(4, Analog(1.W)))
}

trait FPGADE2ClockAndResetsAndSDRAM {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters
  val CLOCK_50 = IO(Input(Clock()))
  val KEY = IO(Vec(4, Input(Bool())))
}

class FPGADE2Shell(implicit val p :Parameters) extends RawModule
  with FPGADE2ChipShell
  with FPGADE2ClockAndResetsAndSDRAM {
}

class FPGADE2Internal(chip: Option[WithTEEHWbaseShell with WithTEEHWbaseConnect])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGADE2ClockAndResetsAndSDRAM {
  def outer = chip
  override def otherId: Option[Int] = Some(6)

  val pll = Module(new pll)
  pll.io.inclk0 := CLOCK_50
  pll.io.areset := !KEY(0)

  val reset_to_sys = ResetCatchAndSync(pll.io.inclk0, !pll.io.locked)

  sys_clk := pll.io.c0
  aclocks.foreach(_.foreach(_ := pll.io.c0)) // TODO: Connect your clocks here
  sdramclock.foreach(_ := pll.io.c0)
  ChildClock.foreach(_ := pll.io.c0)
  ChildReset.foreach(_ := reset_to_sys)
  rst_n := !reset_to_sys
  jrst_n := !reset_to_sys
  usbClk.foreach(_ := pll.io.c0)
}

trait WithFPGADE2Connect {
  this: FPGADE2Shell =>
  val chip: WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGADE2Internal(Some(chip)))

  // To intern = Clocks and resets
  intern.CLOCK_50 := CLOCK_50
  intern.KEY := KEY

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  chip.sdram.foreach(DRAM.from_SDRAMIf)

  LEDR.foreach(_ := false.B)
  LEDG.foreach(_ := false.B)
  chip.gpio_out.foreach{gpo =>
    (LEDR zip gpo.asBools()).foreach{case (a,b) => a := b}
  }
  chip.gpio_in.foreach(gpi => gpi := VecInit(KEY.slice(1, 4) ++ SW).asUInt())
  chip.jtag.jtag_TDI := ALT_IOBUF(GPIO(0))
  chip.jtag.jtag_TMS := ALT_IOBUF(GPIO(2))
  chip.jtag.jtag_TCK := ALT_IOBUF(GPIO(4))
  ALT_IOBUF(GPIO(6), chip.jtag.jtag_TDO)

  // QSPI
  (chip.qspi zip chip.allspicfg).zipWithIndex.foreach {
    case ((qspiport: TEEHWQSPIBundle, _: SPIParams), i: Int) =>
      if (i == 0) {
        // SD IO
        ALT_IOBUF(SD_CLK, qspiport.qspi_sck)
        ALT_IOBUF(SD_CMD, qspiport.qspi_mosi)
        qspiport.qspi_miso := ALT_IOBUF(SD_DAT(0))
        ALT_IOBUF(SD_DAT(3), qspiport.qspi_cs(0))
      } else {
        // Non-valid qspi. Just zero it
        qspiport.qspi_miso := false.B
      }
    case ((qspiport: TEEHWQSPIBundle, _: SPIFlashParams), _: Int) =>
      qspiport.qspi_miso := ALT_IOBUF(GPIO(1))
      ALT_IOBUF(GPIO(3), qspiport.qspi_mosi)
      ALT_IOBUF(GPIO(5), qspiport.qspi_cs(0))
      ALT_IOBUF(GPIO(7), qspiport.qspi_sck)
  }

  chip.uart_rxd := ALT_IOBUF(GPIO(8))
  ALT_IOBUF(GPIO(10), chip.uart_txd)
}
