package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.diplomacy._
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

class HSMCTR4(val on1: Boolean = true, val on2: Boolean = true) extends Bundle {
  val CLKIN0 = Input(Bool())
  val CLKIN_n1 = Input(Bool())
  val CLKIN_n2 = Input(Bool())
  val CLKIN_p1 = Input(Bool())
  val CLKIN_p2 = Input(Bool())
  val D = Vec(4, Analog(1.W))
  val OUT0 = Analog(1.W)
  val OUT_n1 = on1.option(Analog(1.W))
  val OUT_p1 = on1.option(Analog(1.W))
  val OUT_n2 = on2.option(Analog(1.W))
  val OUT_p2 = on2.option(Analog(1.W))
  val RX_n = Vec(17, Analog(1.W))
  val RX_p = Vec(17, Analog(1.W))
  val TX_n = Vec(17, Analog(1.W))
  val TX_p = Vec(17, Analog(1.W))
}

trait FPGATR4ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters

  ///////// LED /////////
  val LED = IO(Vec(4, Analog(1.W)))

  ///////// SW /////////
  val SW = IO(Vec(4, Analog(1.W)))

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
  val HSMD = IO(new HSMCTR4(on1 = false))

  //////////// HSMC_E //////////
  val HSME = IO(new HSMCTR4(on1 = false))

  //////////// HSMC_F //////////
  val HSMF = IO(new HSMCTR4(on1 = false, on2 = false))
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
  def memEnable: Boolean = true
  val mem_a = memEnable.option(IO(Output(Bits((15 + 1).W))))
  val mem_ba = memEnable.option(IO(Output(Bits((2 + 1).W))))
  val mem_cas_n = memEnable.option(IO(Output(Bool())))
  val mem_cke = memEnable.option(IO(Output(Bits((1 + 1).W))))
  val mem_ck = memEnable.option(IO(Output(Bits((0 + 1).W)))) // NOTE: Is impossible to do [0:0]
  val mem_ck_n = memEnable.option(IO(Output(Bits((0 + 1).W)))) // NOTE: Is impossible to do [0:0]
  val mem_cs_n = memEnable.option(IO(Output(Bits((1 + 1).W))))
  val mem_dm = memEnable.option(IO(Output(Bits((7 + 1).W))))
  val mem_dq = memEnable.option(IO(Analog((63 + 1).W)))
  val mem_dqs = memEnable.option(IO(Analog((7 + 1).W)))
  val mem_dqs_n = memEnable.option(IO(Analog((7 + 1).W)))
  val mem_odt = memEnable.option(IO(Output(Bits((1 + 1).W))))
  val mem_ras_n = memEnable.option(IO(Output(Bool())))
  val mem_reset_n = memEnable.option(IO(Output(Bool())))
  val mem_we_n = memEnable.option(IO(Output(Bool())))
  val mem_oct_rdn = memEnable.option(IO(Input(Bool())))
  val mem_oct_rup = memEnable.option(IO(Input(Bool())))
  //val mem_scl = IO(Output(Bool()))
  //val mem_sda = IO(Analog(1.W))
  //val mem_event_n = IO(Input(Bool())) // NOTE: This also appeared, but is not used
}

class FPGATR4Shell(implicit val p :Parameters) extends RawModule
  with FPGATR4ChipShell
  with FPGATR4ClockAndResetsAndDDR {
}

class FPGATR4Internal(chip: Option[Any])(implicit val p :Parameters) extends RawModule
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
      mod_clock := mod_io_ckrst.qsys_clk
      mod_reset := reset_to_sys
      rst_n := !reset_to_sys
      usbClk.foreach(_ := mod_io_ckrst.usb_clk)
      DefaultRTC

      // Async clock connections
      println(s"Connecting ${aclkn} async clocks by default =>")
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

      p(SbusToMbusXTypeKey) match {
        case _: AsynchronousCrossing =>
          println("Quartus Island and Child Clock connected to io_clk")
          mod_clock := mod_io_ckrst.io_clk
          mod_reset := reset_to_child
      }

      mod_io_ckrst.ddr_ref_clk := OSC_50_BANK1.asUInt()
      mod_io_ckrst.qsys_ref_clk := OSC_50_BANK4.asUInt() // TODO: This is okay?
      mod_io_ckrst.system_reset_n := BUTTON(2)
    }

    // Helper function to connect the DDR from the Quartus Platform
    def ConnectDDRUtil(mod_io_qport: QuartusIO) = {
      mem_a.foreach(_ := mod_io_qport.memory_mem_a)
      mem_ba.foreach(_ := mod_io_qport.memory_mem_ba)
      mem_ck.foreach(_ := mod_io_qport.memory_mem_ck(0)) // Force only 1 line (although the config forces 1 line)
      mem_ck_n.foreach(_ := mod_io_qport.memory_mem_ck_n(0)) // Force only 1 line (although the config forces 1 line)
      mem_cke.foreach(_ := mod_io_qport.memory_mem_cke)
      mem_cs_n.foreach(_ := mod_io_qport.memory_mem_cs_n)
      mem_dm.foreach(_ := mod_io_qport.memory_mem_dm)
      mem_ras_n.foreach(_ := mod_io_qport.memory_mem_ras_n)
      mem_cas_n.foreach(_ := mod_io_qport.memory_mem_cas_n)
      mem_we_n.foreach(_ := mod_io_qport.memory_mem_we_n)
      mem_dq.foreach(attach(_, mod_io_qport.memory_mem_dq))
      mem_dqs.foreach(attach(_, mod_io_qport.memory_mem_dqs))
      mem_dqs_n.foreach( attach(_, mod_io_qport.memory_mem_dqs_n))
      mem_odt.foreach(_ := mod_io_qport.memory_mem_odt)
      mem_reset_n.foreach(_ := mod_io_qport.memory_mem_reset_n.getOrElse(true.B))
      (mod_io_qport.oct.rdn zip mem_oct_rdn).foreach{case(a,b) => a := b}
      (mod_io_qport.oct.rup zip mem_oct_rup).foreach{case(a,b) => a := b}
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
      val mod = Module(LazyModule(new FPGAMiniSystemDummy(sourceBits)).module)

      // Serial port
      mod.serport.flipConnect(es)
    }
  }
}

class FPGATR4InternalNoChip
(
  val idBits: Int = 6,
  val idExtBits: Int = 6,
  val widthBits: Int = 32,
  val sinkBits: Int = 1
)(implicit p :Parameters) extends FPGATR4Internal(None)(p) {
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
  override def aclkn: Int = 0
  override def memserSourceBits: Option[Int] = p(ExtSerMem).map( A => idBits )
  override def extserSourceBits: Option[Int] = p(ExtSerBus).map( A => idExtBits )
  override def namedclocks: Seq[String] = Seq()
}

trait WithFPGATR4InternCreate {
  this: FPGATR4Shell =>
  val chip: Any
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

  (mem_a zip intern.mem_a).foreach{case (a,b) => a := b}
  (mem_ba zip intern.mem_ba).foreach{case (a,b) => a := b}
  (mem_ck zip intern.mem_ck).foreach{case (a,b) => a := b}
  (mem_ck_n zip intern.mem_ck_n).foreach{case (a,b) => a := b}
  (mem_cke zip intern.mem_cke).foreach{case (a,b) => a := b}
  (mem_cs_n zip intern.mem_cs_n).foreach{case (a,b) => a := b}
  (mem_dm zip intern.mem_dm).foreach{case (a,b) => a := b}
  (mem_ras_n zip intern.mem_ras_n).foreach{case (a,b) => a := b}
  (mem_cas_n zip intern.mem_cas_n).foreach{case (a,b) => a := b}
  (mem_we_n zip intern.mem_we_n).foreach{case (a,b) => a := b}
  (mem_dq zip intern.mem_dq).foreach{case (a,b) => attach(a,b)}
  (mem_dqs zip intern.mem_dqs).foreach{case (a,b) => attach(a,b)}
  (mem_dqs_n zip intern.mem_dqs_n).foreach{case (a,b) => attach(a,b)}
  (mem_odt zip intern.mem_odt).foreach{case (a,b) => a := b}
  (mem_reset_n zip intern.mem_reset_n).foreach{case (a,b) => a := b}
  (mem_oct_rdn zip intern.mem_oct_rdn).foreach{case (a,b) => b := a}
  (mem_oct_rup zip intern.mem_oct_rup).foreach{case (a,b) => b := a}
}

trait WithFPGATR4PureConnect {
  this: FPGATR4Shell =>
  val chip: Any
  
  def namedclocks: Seq[String] = chip.asInstanceOf[HasTEEHWClockGroupChipImp].system.namedclocks

  // GPIO
  val gpport = SW ++ LED.slice(0, 1)
  chip.asInstanceOf[HasTEEHWPeripheryGPIOChipImp].gpio.zip(gpport).foreach{case(gp, i) =>
    attach(gp, i)
  }

  // JTAG
  chip.asInstanceOf[DebugJTAGOnlyChipImp].jtag.foreach{ chipjtag =>
    attach(chipjtag.TDI, GPIO1_D(4))
    attach(chipjtag.TMS, GPIO1_D(6))
    attach(chipjtag.TCK, GPIO1_D(8))
    attach(GPIO1_D(10), chipjtag.TDO)
    attach(chipjtag.TRSTn, GPIO1_D(12))
  }

  // QSPI
  (chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].spi zip chip.asInstanceOf[HasTEEHWPeripherySPIChipImp].allspicfg).zipWithIndex.foreach {
    case ((qspiport: SPIPIN, _: SPIParams), i: Int) =>
      if (i == 0) {
        // SD IO
        attach(GPIO0_D(28), qspiport.SCK)
        attach(GPIO0_D(30), qspiport.DQ(0))
        attach(qspiport.DQ(1), GPIO0_D(32))
        attach(GPIO0_D(34), qspiport.CS(0))
        attach(GPIO0_D(13), qspiport.DQ(2))
        attach(GPIO0_D(15), qspiport.DQ(2))
      }
    case ((qspiport: SPIPIN, _: SPIFlashParams), _: Int) =>
      attach(qspiport.DQ(1), GPIO1_D(1))
      attach(GPIO1_D(3), qspiport.DQ(0))
      attach(GPIO1_D(5), qspiport.CS(0))
      attach(GPIO1_D(7), qspiport.SCK)
      attach(GPIO1_D(9), qspiport.DQ(2))
      attach(GPIO1_D(11), qspiport.DQ(3))
  }

  // UART
  chip.asInstanceOf[HasTEEHWPeripheryUARTChipImp].uart.foreach { uart =>
    attach(uart.RXD, GPIO1_D(35))
    attach(GPIO1_D(34), uart.TXD)
  }
  
  // USB phy connections
  chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs.foreach{ case chipport=>
    attach(GPIO1_D(17), chipport.USBFullSpeed)
    attach(GPIO1_D(24), chipport.USBWireDataIn(0))
    attach(GPIO1_D(26), chipport.USBWireDataIn(1))
    attach(GPIO1_D(28), chipport.USBWireCtrlOut)
    attach(GPIO1_D(16), chipport.USBWireDataOut(0))
    attach(GPIO1_D(18), chipport.USBWireDataOut(1))
  }

  // TODO Nullify sdram
}

trait WithFPGATR4Connect extends WithFPGATR4PureConnect 
  with WithFPGATR4InternCreate 
  with WithFPGATR4InternConnect {
  this: FPGATR4Shell =>

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // The rest of the platform connections
  PUT(intern.mem_status_local_cal_fail, LED(3))
  PUT(intern.mem_status_local_cal_success, LED(2))
  PUT(intern.mem_status_local_init_done, LED(1))
}

object ConnectHSMCGPIO {
  def npmap(HSMC: HSMCTR4): Map[(Int, Int), Analog] = {
    val default = Map(
      (0, 1) -> HSMC.RX_n(16),
      (0, 3) -> HSMC.RX_p(16),
      (0, 4) -> HSMC.TX_n(16),
      (0, 5) -> HSMC.RX_n(15),
      (0, 6) -> HSMC.TX_p(16),
      (0, 7) -> HSMC.RX_p(15),
      (0, 8) -> HSMC.TX_n(15),
      (0, 9) -> HSMC.RX_n(14),
      (0, 10) -> HSMC.TX_p(15),
      (0, 11) -> HSMC.RX_p(14),
      (0, 12) -> HSMC.TX_n(14),
      (0, 13) -> HSMC.RX_n(13),
      (0, 14) -> HSMC.TX_p(14),
      (0, 15) -> HSMC.RX_p(13),
      (0, 17) -> HSMC.RX_n(12),
      (0, 19) -> HSMC.RX_p(12),
      (0, 20) -> HSMC.TX_n(13),
      (0, 21) -> HSMC.RX_n(11),
      (0, 22) -> HSMC.TX_p(13),
      (0, 23) -> HSMC.RX_p(11),
      (0, 24) -> HSMC.TX_n(12),
      (0, 25) -> HSMC.RX_n(10),
      (0, 26) -> HSMC.TX_p(12),
      (0, 27) -> HSMC.RX_p(10),
      (0, 28) -> HSMC.TX_n(11),
      (0, 29) -> HSMC.RX_n(9),
      (0, 30) -> HSMC.TX_p(11),
      (0, 31) -> HSMC.RX_p(9),
      (0, 32) -> HSMC.TX_n(10),
      (0, 33) -> HSMC.TX_n(9),
      (0, 34) -> HSMC.TX_p(10),
      (0, 35) -> HSMC.TX_p(9),
      (1, 1) -> HSMC.RX_n(7),
      (1, 3) -> HSMC.RX_p(7),
      (1, 4) -> HSMC.TX_n(7),
      (1, 5) -> HSMC.RX_n(6),
      (1, 6) -> HSMC.TX_p(7),
      (1, 7) -> HSMC.RX_p(6),
      (1, 8) -> HSMC.TX_n(6),
      (1, 9) -> HSMC.RX_n(5),
      (1, 10) -> HSMC.TX_p(6),
      (1, 11) -> HSMC.RX_p(5),
      (1, 12) -> HSMC.TX_n(5),
      (1, 13) -> HSMC.RX_n(4),
      (1, 14) -> HSMC.TX_p(5),
      (1, 15) -> HSMC.RX_p(4),
      (1, 17) -> HSMC.RX_n(3),
      (1, 19) -> HSMC.RX_p(3),
      (1, 20) -> HSMC.TX_n(4),
      (1, 21) -> HSMC.RX_n(2),
      (1, 22) -> HSMC.TX_p(4),
      (1, 23) -> HSMC.RX_p(2),
      (1, 24) -> HSMC.TX_n(3),
      (1, 25) -> HSMC.RX_n(1),
      (1, 26) -> HSMC.TX_p(3),
      (1, 27) -> HSMC.RX_p(1),
      (1, 28) -> HSMC.TX_n(2),
      (1, 29) -> HSMC.RX_n(0),
      (1, 30) -> HSMC.TX_p(2),
      (1, 31) -> HSMC.RX_p(0),
      (1, 32) -> HSMC.TX_n(1),
      (1, 33) -> HSMC.TX_n(0),
      (1, 34) -> HSMC.TX_p(1),
      (1, 35) -> HSMC.TX_p(0))
    val on1: Map[(Int, Int), Analog] = if(HSMC.on1) Map (
      (1, 16) -> HSMC.OUT_n1.get,
      (1, 18) -> HSMC.OUT_p1.get,
    ) else Map()

    val on2: Map[(Int, Int), Analog] = if(HSMC.on2) Map (
      (0, 16) -> HSMC.OUT_n2.get,
      (0, 18) -> HSMC.OUT_p2.get,
    ) else Map()

    default ++ on1 ++ on2
  }
  def apply (n: Int, pu: Int, HSMC: HSMCTR4): Analog = {
    val p:Int = pu match {
      case it if 1 to 10 contains it => pu - 1
      case it if 13 to 28 contains it => pu - 3
      case it if 31 to 40 contains it => pu - 5
      case _ => throw new RuntimeException(s"J${n}_${pu} is a VDD or a GND")
    }
    npmap(HSMC)((n, p))
  }
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
          case 2 => if(get) c := HSMC.CLKIN_p2 else throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case _ => if(get) c := ALT_IOBUF(npmap(HSMC)((n,p))) else ALT_IOBUF(npmap(HSMC)((n,p)), c)
        }
      case 1 =>
        p match {
          case 0 => if(get) c := HSMC.CLKIN_n1 else throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case 2 => if(get) c := HSMC.CLKIN_p1 else throw new RuntimeException(s"GPIO${n}_${p} can only be input")
          case _ => if(get) c := ALT_IOBUF(npmap(HSMC)((n,p))) else ALT_IOBUF(npmap(HSMC)((n,p)), c)
        }
      case _ => throw new RuntimeException(s"GPIO${n}_${p} does not exist")
    }
  }
}


// Based on layout of the TR4.sch done by Duy
trait WithFPGATR4ToChipConnect extends WithFPGATR4InternNoChipCreate with WithFPGATR4InternConnect {
  this: FPGATR4Shell =>

  // ******* Duy section ******
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
  ConnectHSMCGPIO(JP21, 2, intern.sys_clk.asBool, false, HSMC_JP20_21) // Previous ChildClock
  ConnectHSMCGPIO(JP18, 5, !intern.rst_n, false, HSMC_JP20_21) // Previous ChildReset
  ConnectHSMCGPIO(JP21, 4, intern.sys_clk.asBool, false, HSMC_JP20_21)
  ConnectHSMCGPIO(JP18, 2, intern.rst_n, false, HSMC_JP19_18)
  ConnectHSMCGPIO(JP18, 6, intern.rst_n, false, HSMC_JP19_18) // Previous jrst_n
  // Memory port
  intern.tlport.foreach{ case tlport =>
    ConnectHSMCGPIO(JP18, 1, tlport.a.valid, true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 4, tlport.a.ready, false, HSMC_JP19_18)
    require(tlport.a.bits.opcode.getWidth == 3, s"${tlport.a.bits.opcode.getWidth}")
    val a_opcode = Wire(Vec(3, Bool()))
    ConnectHSMCGPIO(JP18, 3, a_opcode(2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 7, a_opcode(1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 8, a_opcode(0), true, HSMC_JP19_18)
    tlport.a.bits.opcode := a_opcode.asUInt
    require(tlport.a.bits.param.getWidth == 3, s"${tlport.a.bits.param.getWidth}")
    val a_param = Wire(Vec(3, Bool()))
    ConnectHSMCGPIO(JP18, 9, a_param(2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 10, a_param(1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 13, a_param(0), true, HSMC_JP19_18)
    tlport.a.bits.param := a_param.asUInt
    val a_size = Wire(Vec(3, Bool()))
    require(tlport.a.bits.size.getWidth == 3, s"${tlport.a.bits.size.getWidth}")
    ConnectHSMCGPIO(JP18, 14, a_size(2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 15, a_size(1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 16, a_size(0), true, HSMC_JP19_18)
    tlport.a.bits.size := a_size.asUInt
    require(tlport.a.bits.source.getWidth == 6, s"${tlport.a.bits.source.getWidth}")
    val a_source = Wire(Vec(6, Bool()))
    ConnectHSMCGPIO(JP18, 17, a_source(5), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 18, a_source(4), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 19, a_source(3), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 20, a_source(2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 21, a_source(1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP18, 22, a_source(0), true, HSMC_JP19_18)
    tlport.a.bits.source := a_source.asUInt
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
    tlport.a.bits.address := a_address.asUInt
    require(tlport.a.bits.mask.getWidth == 4, s"${tlport.a.bits.mask.getWidth}")
    val a_mask = Wire(Vec(4, Bool()))
    ConnectHSMCGPIO(JP19, 19, a_mask(3), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 20, a_mask(2), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 21, a_mask(1), true, HSMC_JP19_18)
    ConnectHSMCGPIO(JP19, 22, a_mask(0), true, HSMC_JP19_18)
    tlport.a.bits.mask := a_mask.asUInt
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
    tlport.a.bits.data := a_data.asUInt
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
  
  // ******* Ahn-Dao section ******
  def HSMCSER = HSMA
  def versionSer = 1
  versionSer match {
    case _ => // TODO: There is no such thing as versions in TR4
      val MEMSER_GPIO = 0
      val EXTSER_GPIO = 1
      //ConnectHSMCGPIO(MEMSER_GPIO, 1, intern.sys_clk.asBool(), false, HSMCSER)
      //intern.ChildClock.foreach{ a => ConnectHSMCGPIO(MEMSER_GPIO, 2, a.asBool(), false, HSMCSER) }
      //intern.usbClk.foreach{ a => ConnectHSMCGPIO(MEMSER_GPIO, 3, a.asBool(), false, HSMCSER) }
      ConnectHSMCGPIO(MEMSER_GPIO, 4, intern.rst_n, false, HSMCSER)
      ConnectHSMCGPIO(MEMSER_GPIO, 5, intern.rst_n, false, HSMCSER)
      // ExtSerMem
      intern.memser.foreach { memser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectHSMCGPIO(MEMSER_GPIO, 9, in_bits(7), false, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 10, in_bits(6), false, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 13, in_bits(5), false, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 14, in_bits(4), false, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 15, in_bits(3), false, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 16, in_bits(2), false, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 17, in_bits(1), false, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 18, in_bits(0), false, HSMCSER)
        in_bits := memser.in.bits.asBools
        ConnectHSMCGPIO(MEMSER_GPIO, 19, memser.in.valid, false, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 20, memser.out.ready, false, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 21, memser.in.ready, true, HSMCSER)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectHSMCGPIO(MEMSER_GPIO, 22, out_bits(7), true, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 23, out_bits(6), true, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 24, out_bits(5), true, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 25, out_bits(4), true, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 26, out_bits(3), true, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 27, out_bits(2), true, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 28, out_bits(1), true, HSMCSER)
        ConnectHSMCGPIO(MEMSER_GPIO, 31, out_bits(0), true, HSMCSER)
        memser.out.bits := out_bits.asUInt
        ConnectHSMCGPIO(MEMSER_GPIO, 32, memser.out.valid, true, HSMCSER)
      }
      // ExtSerBus
      intern.extser.foreach{ extser =>
        val in_bits = Wire(Vec(8, Bool()))
        ConnectHSMCGPIO(EXTSER_GPIO, 9, in_bits(7), false, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 10, in_bits(6), false, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 13, in_bits(5), false, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 14, in_bits(4), false, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 15, in_bits(3), false, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 16, in_bits(2), false, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 17, in_bits(1), false, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 18, in_bits(0), false, HSMCSER)
        in_bits := extser.in.bits.asBools
        ConnectHSMCGPIO(EXTSER_GPIO, 19, extser.in.valid, false, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 20, extser.out.ready, false, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 21, extser.in.ready, true, HSMCSER)
        val out_bits = Wire(Vec(8, Bool()))
        ConnectHSMCGPIO(EXTSER_GPIO, 22, out_bits(7), true, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 23, out_bits(6), true, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 24, out_bits(5), true, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 25, out_bits(4), true, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 26, out_bits(3), true, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 27, out_bits(2), true, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 28, out_bits(1), true, HSMCSER)
        ConnectHSMCGPIO(EXTSER_GPIO, 31, out_bits(0), true, HSMCSER)
        extser.out.bits := out_bits.asUInt
        ConnectHSMCGPIO(EXTSER_GPIO, 32, extser.out.valid, true, HSMCSER)
      }
  }

  // ******** Misc part ********

  // LEDs
  PUT(intern.mem_status_local_cal_fail, LED(3))
  PUT(intern.mem_status_local_cal_success, LED(2))
  PUT(intern.mem_status_local_init_done, LED(1))
  // Clocks to the outside
  ALT_IOBUF(SMA_CLKOUT, intern.sys_clk.asBool)
  ALT_IOBUF(SMA_CLKOUT_p, intern.sys_clk.asBool) // For async clock
  intern.usbClk.foreach(A => ALT_IOBUF(SMA_CLKOUT_n, A.asBool))
}

// Trait which connects the FPGA the chip
trait WithFPGATR4FromChipConnect extends WithFPGATR4PureConnect {
  this: FPGATR4Shell =>

  override def memEnable = false // No memory interface in this version

  // ******* Ahn-Dao section ******
  def HSMCSER = HSMA
  def versionSer = 1
  versionSer match {
    case _ => // TODO: There is no such thing as versions in TR4
      val MEMSER_GPIO = 0
      val EXTSER_GPIO = 1
      PUT(SMA_CLKIN.asBool, chip.asInstanceOf[HasTEEHWClockGroupChipImp].clockxi)
      chip.asInstanceOf[HasPeripheryUSB11HSChipImp].usb11hs.foreach{ a =>
        attach(a.usbClk, SMA_CLKOUT_n)
      }
      attach(ConnectHSMCGPIO(MEMSER_GPIO, 5, HSMCSER), chip.asInstanceOf[HasTEEHWClockGroupChipImp].rstn)
      // Only some of the aclocks are actually connected.
      println("Connecting orphan clocks =>")
      (chip.asInstanceOf[HasTEEHWClockGroupChipImp].aclockxi zip namedclocks).foreach { case (aclk, nam) =>
        println(s"  Detected clock ${nam}")
        if(nam.contains("mbus")) {
          println("    Connected to SMA_CLKOUT_p")
          attach(aclk, SMA_CLKOUT_p)
        }
        else
          println("    WARNING: This clock is not handled")
      }
      // ExtSerMem
      chip.asInstanceOf[HasTEEHWPeripheryExtSerMemChipImp].memser.foreach { memser =>
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 9, HSMCSER), memser.in.bits(7))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 10, HSMCSER), memser.in.bits(6))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 13, HSMCSER), memser.in.bits(5))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 14, HSMCSER), memser.in.bits(4))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 15, HSMCSER), memser.in.bits(3))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 16, HSMCSER), memser.in.bits(2))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 17, HSMCSER), memser.in.bits(1))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 18, HSMCSER), memser.in.bits(0))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 19, HSMCSER), memser.in.valid)
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 20, HSMCSER), memser.out.ready)
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 21, HSMCSER), memser.in.ready)
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 22, HSMCSER), memser.out.bits(7))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 23, HSMCSER), memser.out.bits(6))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 24, HSMCSER), memser.out.bits(5))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 25, HSMCSER), memser.out.bits(4))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 26, HSMCSER), memser.out.bits(3))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 27, HSMCSER), memser.out.bits(2))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 28, HSMCSER), memser.out.bits(1))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 31, HSMCSER), memser.out.bits(0))
        attach(ConnectHSMCGPIO(MEMSER_GPIO, 32, HSMCSER), memser.out.valid)
      }
      // ExtSerBus
      chip.asInstanceOf[HasTEEHWPeripheryExtSerBusChipImp].extser.foreach{ extser =>
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 9, HSMCSER), extser.in.bits(7))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 10, HSMCSER), extser.in.bits(6))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 13, HSMCSER), extser.in.bits(5))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 14, HSMCSER), extser.in.bits(4))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 15, HSMCSER), extser.in.bits(3))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 16, HSMCSER), extser.in.bits(2))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 17, HSMCSER), extser.in.bits(1))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 18, HSMCSER), extser.in.bits(0))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 19, HSMCSER), extser.in.valid)
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 20, HSMCSER), extser.out.ready)
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 21, HSMCSER), extser.in.ready)
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 22, HSMCSER), extser.out.bits(7))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 23, HSMCSER), extser.out.bits(6))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 24, HSMCSER), extser.out.bits(5))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 25, HSMCSER), extser.out.bits(4))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 26, HSMCSER), extser.out.bits(3))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 27, HSMCSER), extser.out.bits(2))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 28, HSMCSER), extser.out.bits(1))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 31, HSMCSER), extser.out.bits(0))
        attach(ConnectHSMCGPIO(EXTSER_GPIO, 32, HSMCSER), extser.out.valid)
      }
  }
}