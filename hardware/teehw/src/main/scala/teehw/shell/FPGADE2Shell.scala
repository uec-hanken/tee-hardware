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
import uec.teehardware.devices.sifiveblocks._
import uec.teehardware.devices.tlmemext._

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
    }).asUInt
  }
  def from_SDRAMIO(io: SDRAMIO) = {
    CLK := GET(io.sdram_clk_o)
    CKE := GET(io.sdram_cke_o)
    CS_N := GET(io.sdram_cs_o)
    RAS_N := GET(io.sdram_ras_o)
    CAS_N := GET(io.sdram_cas_o)
    WE_N := GET(io.sdram_we_o)
    DQM := VecInit(io.sdram_dqm_o.map(GET(_))).asUInt
    ADDR := VecInit(io.sdram_addr_o.map(GET(_))).asUInt
    BA := VecInit(io.sdram_ba_o.map(GET(_))).asUInt
    (io.sdram_data zip DQ).foreach{ case (o, an) => attach(o, an)}
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
  val LEDR = IO(Vec(18, Analog(1.W)))
  val LEDG = IO(Vec(9, Analog(1.W)))
  val SW = IO(Vec(18, Analog(1.W)))

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

class FPGADE2Internal(chip: Option[Any])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGADE2ClockAndResetsAndSDRAM {
  def outer = chip
  override def otherId: Option[Int] = Some(6)

  val pll = Module(new pll)
  pll.io.inclk0 := CLOCK_50
  pll.io.areset := !KEY(0)

  val reset_to_sys = ResetCatchAndSync(pll.io.inclk0, !pll.io.locked)

  sys_clk := pll.io.c0
  aclocks.foreach(_ := pll.io.c0) // TODO: Connect your clocks here
  rst_n := !reset_to_sys
  usbClk.foreach(_ := pll.io.c0)
}

trait WithFPGADE2Connect {
  this: FPGADE2Shell =>
  val chip: Any
  val intern = Module(new FPGADE2Internal(Some(chip)))

  // To intern = Clocks and resets
  intern.CLOCK_50 := CLOCK_50
  intern.KEY := KEY

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  chip.asInstanceOf[HasSDRAMChipImp].sdramio.foreach(DRAM.from_SDRAMIO)

  val gpport = LEDR ++ SW.slice(1, 4)
  chip.asInstanceOf[HasTEEHWPeripheryGPIOChipImp].gpio.zip(gpport).foreach{case(gp, i) =>
    attach(gp, i)
  }
  chip.asInstanceOf[DebugJTAGOnlyChipImp].jtag.foreach { jtag =>
    attach(jtag.TDI, GPIO(0))
    attach(jtag.TMS, GPIO(2))
    attach(jtag.TCK, GPIO(4))
    attach(GPIO(6), jtag.TDO)
  }

  // QSPI
  (chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].spi zip chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].allspicfg).zipWithIndex.foreach {
    case ((qspiport: SPIPIN, _: SPIParams), i: Int) =>
      if (i == 0) {
        // SD IO
        attach(SD_DAT(3), qspiport.CS(0))
        attach(SD_CMD, qspiport.DQ(0))
        attach(qspiport.DQ(1), SD_DAT(0))
        attach(SD_CLK, qspiport.SCK)
      }
    case ((qspiport: SPIPIN, _: SPIFlashParams), _: Int) =>
      attach(qspiport.DQ(1), GPIO(1))
      attach(GPIO(3), qspiport.DQ(0))
      attach(GPIO(5), qspiport.CS(0))
      attach(GPIO(7), qspiport.SCK)
  }

  chip.asInstanceOf[HasTEEHWPeripheryUARTChipImp].uart.foreach { uart =>
    attach(uart.RXD, GPIO(8))
    attach(GPIO(10), uart.TXD)
  }
}
