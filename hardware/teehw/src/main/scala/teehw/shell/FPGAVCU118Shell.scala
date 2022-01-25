package uec.teehardware.shell

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import chipsalliance.rocketchip.config.Parameters
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import sifive.blocks.devices.gpio.PeripheryGPIOKey
import sifive.fpgashells.clocks._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import uec.teehardware._
import sifive.fpgashells.ip.xilinx.vcu118mig._
import sifive.fpgashells.ip.xilinx._
import sifive.fpgashells.shell.xilinx.XDMATopPads
import uec.teehardware.devices.clockctrl.ClockCtrlPortIO
import uec.teehardware.devices.usb11hs.PeripheryUSB11HSKey

trait FPGAVCU118ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters
  val gpio_in = IO(Input(UInt(p(GPIOInKey).W)))
  val gpio_out = IO(Output(UInt((p(PeripheryGPIOKey).head.width - p(GPIOInKey)).W)))
  val jtag = IO(new Bundle {
    val jtag_TDI = (Input(Bool())) // J53.6
    val jtag_TDO = (Output(Bool())) // J53.8
    val jtag_TCK = (Input(Bool())) // J53.2
    val jtag_TMS = (Input(Bool())) // J53.4
  })
  val sdio = IO(new Bundle {
    val sdio_clk = (Output(Bool())) // J52.7
    val sdio_cmd = (Output(Bool())) // J52.3
    val sdio_dat_0 = (Input(Bool())) // J52.5
    val sdio_dat_1 = (Analog(1.W)) // J52.2
    val sdio_dat_2 = (Analog(1.W)) // J52.4
    val sdio_dat_3 = (Output(Bool())) // J52.1
  })
  val uart_txd = IO(Output(Bool()))
  val uart_rxd = IO(Input(Bool()))

  var qspi: Option[TEEHWQSPIBundle] = None

  val USB = p(PeripheryUSB11HSKey).map { _ =>
    IO(new Bundle {
      val FullSpeed = Output(Bool()) // NC
      val WireDataIn = Input(Bits(2.W)) // NC // NC
      val WireCtrlOut = Output(Bool()) // NC
      val WireDataOut = Output(Bits(2.W)) // NC // NC
    })
  }

  val pciePorts = p(IncludePCIe).option(IO(new XilinxVC707PCIeX1Pads))
  val xdmaPorts = p(XDMAPCIe).map(A => IO(new XDMATopPads(A.lanes)))
}

trait FPGAVCU118ClockAndResetsAndDDR {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters

  val sys_clock_p = IO(Input(Clock()))
  val sys_clock_n = IO(Input(Clock()))
  val rst_0 = IO(Input(Bool()))
  val rst_1 = IO(Input(Bool()))
  val rst_2 = IO(Input(Bool()))
  val rst_3 = IO(Input(Bool()))

  var ddr: Option[VCU118MIGIODDR] = None
}

class FPGAVCU118Shell(implicit val p :Parameters) extends RawModule
  with FPGAVCU118ChipShell
  with FPGAVCU118ClockAndResetsAndDDR {
}

class FPGAVCU118Internal(chip: Option[WithTEEHWbaseShell with WithTEEHWbaseConnect])(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGAVCU118ClockAndResetsAndDDR {
  def outer = chip

  val init_calib_complete = IO(Output(Bool()))
  var depth = BigInt(0)

  val sys_clock_ibufds = Module(new IBUFDS())
  val sys_clk_i = IBUFG(sys_clock_ibufds.io.O)
  sys_clock_ibufds.io.I := sys_clock_p
  sys_clock_ibufds.io.IB := sys_clock_n
  val reset_0 = IBUF(rst_0)
  //val reset_1 = IBUF(rst_1)
  //val reset_2 = IBUF(rst_2)
  val reset_3 = IBUF(rst_3)

  val clock = Wire(Clock())
  val reset = Wire(Bool())

  val isOtherClk = isChildClock || (p(SbusToMbusXTypeKey) match {
    case _: AsynchronousCrossing => true
    case _ => false
  }) || p(PeripheryUSB11HSKey).nonEmpty

  withClockAndReset(clock, reset) {
    // PLL instance
    val c = new PLLParameters(
      name = "pll",
      input = PLLInClockParameters(freqMHz = 250.0, feedback = true),
      req = Seq(
        PLLOutClockParameters(freqMHz = p(FreqKeyMHz))
      ) ++ (if (isOtherClk) Seq(PLLOutClockParameters(freqMHz = 10.0), PLLOutClockParameters(freqMHz = 48.0)) else Seq())
    )
    val pll = Module(new Series7MMCM(c))
    pll.io.clk_in1 := sys_clk_i
    pll.io.reset := reset_0

    val aresetn = !reset_0 // Reset that goes to the MMCM inside of the DDR MIG
    val sys_rst = ResetCatchAndSync(pll.io.clk_out3.get, !pll.io.locked) // Catched system clock
    val reset_to_sys = WireInit(!pll.io.locked) // If DDR is not present, this is the system reset
    val reset_to_child = WireInit(!pll.io.locked) // If DDR is not present, this is the child reset
    pll.io.clk_out2.foreach(reset_to_child := ResetCatchAndSync(_, !pll.io.locked))

    // The DDR port
    tlport.foreach { chiptl =>
      val mod = Module(LazyModule(new TLULtoMIGUltra(chiptl.params)).module)

      // DDR port only
      ddr = Some(IO(new VCU118MIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport

      // MIG connections, like resets and stuff
      mod.io.ddrport.c0_sys_clk_i := sys_clk_i.asUInt()
      mod.io.ddrport.c0_ddr4_aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.c0_ddr4_ui_clk_sync_rst)
      mod.clock := pll.io.clk_out1.getOrElse(false.B)
      mod.reset := reset_to_sys

      // TileLink Interface from platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      // Legacy ChildClock
      ChildClock.foreach { cclk =>
        println("Shell Island and Child Clock connected to clk_out2")
        cclk := pll.io.clk_out2.get
        mod.clock := pll.io.clk_out2.get
        mod.reset := reset_to_child
      }
      ChildReset.foreach { crst =>
        crst := reset_to_child
      }

      init_calib_complete := mod.io.ddrport.c0_init_calib_complete
      depth = mod.depth
    }
    (memser zip memserSourceBits).foreach { case (ms, sourceBits) =>
      val mod = Module(LazyModule(new SertoMIGUltra(ms.w, sourceBits)).module)

      // Serial port
      mod.io.serport.flipConnect(ms)

      // DDR port only
      ddr = Some(IO(new VCU118MIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport

      // MIG connections, like resets and stuff
      mod.io.ddrport.c0_sys_clk_i := sys_clk_i.asUInt()
      mod.io.ddrport.c0_ddr4_aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_to_sys := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.c0_ddr4_ui_clk_sync_rst)

      p(SbusToMbusXTypeKey) match {
        case _: AsynchronousCrossing =>
          println("Shell Island connected to clk_out2 (10MHz)")
          ChildClock.foreach(_ := pll.io.clk_out2.get)
          mod.clock := pll.io.clk_out2.get
          mod.reset := reset_to_child
        case _ =>
          mod.clock := pll.io.clk_out1.get
      }

      init_calib_complete := mod.io.ddrport.c0_init_calib_complete
      depth = mod.depth
    }

    // Main clock and reset assignments
    clock := pll.io.clk_out1.get
    reset := reset_to_sys
    sys_clk := pll.io.clk_out1.get
    rst_n := !reset_to_sys
    jrst_n := !reset_to_sys
    usbClk.foreach(_ := pll.io.clk_out3.get)
    sdramclock.foreach(_ := pll.io.clk_out1.get)
    DefaultRTC

    aclocks.foreach { aclocks =>
      println(s"Connecting async clocks by default =>")
      (aclocks zip namedclocks).foreach { case (aclk, nam) =>
        println(s"  Detected clock ${nam}")
        if(nam.contains("mbus")) {
          p(SbusToMbusXTypeKey) match {
            case _: AsynchronousCrossing =>
              aclk := pll.io.clk_out2.get
              println("    Connected to clk_out2 (10 MHz)")
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
    (extser zip extserSourceBits).foreach { case (es, sourceBits) =>
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

trait WithFPGAVCU118Connect {
  this: FPGAVCU118Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGAVCU118Internal(Some(chip)))

  // To intern = Clocks and resets
  intern.sys_clock_p := sys_clock_p
  intern.sys_clock_n := sys_clock_n
  intern.rst_0 := rst_0
  intern.rst_1 := rst_1
  intern.rst_2 := rst_2
  intern.rst_3 := rst_3
  ddr = intern.ddr.map{ A =>
    val port = IO(new VCU118MIGIODDR(intern.depth))
    port <> A
    port
  }

  // From intern = Clocks and resets
  intern.connectChipInternals(chip)

  // Platform connections
  gpio_out := Cat(chip.gpio_out(chip.gpio_out.getWidth-1, 1), intern.init_calib_complete)
  chip.gpio_in := gpio_in
  jtag <> chip.jtag
  qspi = chip.qspi.map(A => IO ( new TEEHWQSPIBundle(A.csWidth) ) )
  (chip.qspi zip qspi).foreach { case (sysqspi, portspi) => portspi <> sysqspi}
  chip.jtag.jtag_TCK := IBUFG(jtag.jtag_TCK.asClock).asUInt
  chip.uart_rxd := uart_rxd	  // UART_TXD
  uart_txd := chip.uart_txd 	// UART_RXD
  sdio <> chip.sdio

  // USB phy connections
  ((chip.usb11hs zip USB) zip intern.usbClk).foreach{ case ((chipport, port), uclk) =>
    port.FullSpeed := chipport.USBFullSpeed
    chipport.USBWireDataIn := port.WireDataIn
    port.WireCtrlOut := chipport.USBWireCtrlOut
    port.WireDataOut := chipport.USBWireDataOut

    chipport.usbClk := uclk
  }

  // PCIe (if available)
  (pciePorts zip chip.pciePorts).foreach{ case (port, chipport) =>
    chipport.REFCLK_rxp := port.REFCLK_rxp
    chipport.REFCLK_rxn := port.REFCLK_rxn
    port.pci_exp_txp := chipport.pci_exp_txp
    port.pci_exp_txn := chipport.pci_exp_txn
    chipport.pci_exp_rxp := port.pci_exp_rxp
    chipport.pci_exp_rxn := port.pci_exp_rxn
    chipport.axi_aresetn := intern.rst_n
    chipport.axi_ctl_aresetn := intern.rst_n
  }
  (xdmaPorts zip chip.xdmaPorts).foreach{
    case (port, sysport) =>
      port.lanes <> sysport.lanes
      port.refclk <> sysport.refclk
      sysport.erst_n := intern.rst_n
  }

  // TODO Nullify this for now
  chip.sdram.foreach{ sdram =>
    sdram.sdram_data_i := 0.U
  }
}
