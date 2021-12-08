package uec.teehardware

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.{BaseSubsystem, SbusToMbusXTypeKey}
import freechips.rocketchip.tilelink.TLBundleParameters
import freechips.rocketchip.util._
import sifive.blocks.devices.pinctrl._
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.fpgashells.clocks._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import uec.teehardware.devices.usb11hs._
import freechips.rocketchip.util._
import sifive.fpgashells.shell.xilinx.XDMATopPads
import testchipip.SerialIO
import uec.teehardware.devices.clockctrl.ClockCtrlPortIO

// **********************************************************************
// **TEEHW chip - for doing the only-input/output chip
// **********************************************************************

class TEEHWQSPIBundle(val csWidth: Int = 1) extends Bundle {
  val qspi_cs = (Output(UInt(csWidth.W)))
  val qspi_sck = (Output(Bool()))
  val qspi_miso = (Input(Bool()))
  val qspi_mosi = (Output(Bool()))
  val qspi_wp = (Output(Bool()))
  val qspi_hold = (Output(Bool()))
}

class  WithTEEHWbaseShell(implicit val p :Parameters) extends RawModule {
  // The actual pins of this module.
  val gpio_in = IO(Input(UInt(p(GPIOInKey).W)))
  val gpio_out = IO(Output(UInt((p(PeripheryGPIOKey).head.width-p(GPIOInKey)).W)))
  val jtag = IO(new Bundle {
    val jtag_TDI = (Input(Bool()))
    val jtag_TDO = (Output(Bool()))
    val jtag_TCK = (Input(Bool()))
    val jtag_TMS = (Input(Bool()))
  })
  val sdio = IO(new Bundle {
    val sdio_clk = (Output(Bool()))
    val sdio_cmd = (Output(Bool()))
    val sdio_dat_0 = (Input(Bool()))
    val sdio_dat_1 = (Analog(1.W))
    val sdio_dat_2 = (Analog(1.W))
    val sdio_dat_3 = (Output(Bool()))
  })
  val uart_txd = IO(Output(Bool()))
  val uart_rxd = IO(Input(Bool()))
  val usb11hs = p(PeripheryUSB11HSKey).map{ _ => IO(new USB11HSPortIO)}
  val pciePorts = p(IncludePCIe).option(IO(new XilinxVC707PCIeX1IO))
  val xdmaPorts = p(XDMAPCIe).map(A => IO(new XDMATopPadswReset(A.lanes)))
  // Memory port serialized
  val memser = p(ExtSerMem).map(A => IO(new SerialIO(A.serWidth)))
  // Ext port serialized
  val extser = p(ExtSerBus).map(A => IO(new SerialIO(A.serWidth)))
  // Clocks and resets
  val ChildClock = p(DDRPortOther).option(IO(Input(Clock())))
  val ChildReset = p(DDRPortOther).option(IO(Input(Bool())))
  val sys_clk = IO(Input(Clock()))
  val rst_n = IO(Input(Bool()))
  val jrst_n = IO(Input(Bool()))
  // An option to dynamically assign
  var qspi: Option[TEEHWQSPIBundle] = None // QSPI gets added progresively using both PeripherySPIKey and PeripherySPIFlashKey
  var aclocks: Option[Vec[Clock]] = None // Async clocks depends on a node of clocks named "globalClocksNode"
  var tlport: Option[TLUL] = None // The TL port depends of a node, and a edge, for parameters
}

trait WithTEEHWbaseConnect {
  this: WithTEEHWbaseShell =>
  implicit val p: Parameters

  val clock : Clock
  val reset : Bool // System reset (for cores)
  val system: WithTEEHWPlatformConnect

  val cacheBlockBytes = system.sys.outer.asInstanceOf[BaseSubsystem].mbus.blockBytes

  // Merge all the gpio vector
  val vgpio_in = VecInit(gpio_in.toBools)
  val vgpio_out = Wire(Vec(p(PeripheryGPIOKey).head.width-p(GPIOInKey), Bool()))
  gpio_out := vgpio_out.asUInt()
  val gpio = vgpio_in ++ vgpio_out
  // GPIOs
  (gpio zip system.io.pins.gpio.pins).zipWithIndex.foreach {
    case ((g: Bool, pin: BasePin), i: Int) =>
      if(i < p(GPIOInKey)) BasePinToRegular(pin, g)
      else g := BasePinToRegular(pin)
  }

  // JTAG
  BasePinToRegular(system.io.pins.jtag.TMS, jtag.jtag_TMS)
  BasePinToRegular(system.io.pins.jtag.TCK, jtag.jtag_TCK)
  BasePinToRegular(system.io.pins.jtag.TDI, jtag.jtag_TDI)
  jtag.jtag_TDO := BasePinToRegular(system.io.pins.jtag.TDO)

  // QSPI (SPI as flash memory)
  qspi = (system.io.pins.spi.size >= 2).option( IO ( new TEEHWQSPIBundle(system.io.pins.spi(1).cs.size) ) )
  qspi.foreach { portspi =>
    portspi.qspi_cs := BasePinToRegular(system.io.pins.spi(1).cs.head)
    portspi.qspi_sck := BasePinToRegular(system.io.pins.spi(1).sck)
    portspi.qspi_mosi := BasePinToRegular(system.io.pins.spi(1).dq(0))
    BasePinToRegular(system.io.pins.spi(1).dq(1), portspi.qspi_miso)
    portspi.qspi_wp := BasePinToRegular(system.io.pins.spi(1).dq(2))
    portspi.qspi_hold := BasePinToRegular(system.io.pins.spi(1).dq(3))
  }

  // SPI (SPI as SD?)
  sdio.sdio_dat_3 := BasePinToRegular(system.io.pins.spi(0).cs.head)
  sdio.sdio_clk := BasePinToRegular(system.io.pins.spi(0).sck)
  sdio.sdio_cmd := BasePinToRegular(system.io.pins.spi(0).dq(0))
  BasePinToRegular(system.io.pins.spi(0).dq(1), sdio.sdio_dat_0)
  BasePinToRegular(system.io.pins.spi(0).dq(2)) // Ignored
  BasePinToRegular(system.io.pins.spi(0).dq(3)) // Ignored

  // UART
  BasePinToRegular(system.io.pins.uart.rxd, uart_rxd)
  uart_txd := BasePinToRegular(system.io.pins.uart.txd)

  // USB11
  (usb11hs zip system.io.usb11hs).foreach{ case (port, sysport) => port <> sysport }

  // The memory port
  val memdevice = Some(new MemoryDevice)
  (ChildClock zip system.io.ChildClock).foreach{ case (port, sysport) => sysport := port }
  (ChildReset zip system.io.ChildReset).foreach{ case (port, sysport) => sysport := port }

  // The serialized memory port
  (memser zip system.io.memser).foreach{ case (port, sysport) => port <> sysport }

  // The serialized external port
  (extser zip system.io.extser).foreach{ case (port, sysport) => port <> sysport }

  // PCIe port (if available)
  (pciePorts zip system.io.pciePorts).foreach{ case (port, sysport) => port <> sysport }
  (xdmaPorts zip system.io.xdmaPorts).foreach{ case (port, sysport) => port <> sysport }

  // TL external memory port
  val tlparam = system.io.tlport.map(tl => tl.params)
  tlport = tlparam.map{tl => IO(new TLUL(tl))}
  // TL port connection
  (system.io.tlport zip tlport).foreach{case (base, chip) =>
    chip.a <> base.a
    base.d <> chip.d
  }

  // Clock and reset connection
  val aclkn = p(ExposeClocks).option(system.io.aclocks.size)
  aclocks = aclkn.map(A => IO(Vec(A, Input(Clock()))))
  clock := sys_clk
  if(p(ExposeClocks)) system.io.aclocks := aclocks.get
  else {
    system.io.aclocks.foreach(_ := sys_clk)
  }
  reset := !rst_n || system.io.ndreset // This connects the debug reset and the general reset together
  system.io.jtag_reset := !jrst_n
}

trait HasTEEHWbase {
  this: RawModule =>
  implicit val p: Parameters
  // All the modules declared here have this clock and reset
  val clock = Wire(Clock())
  val reset = Wire(Bool()) // System reset (for cores)
  val system = withClockAndReset(clock, reset) {
    // The platform module
    Module(new TEEHWPlatform)
  }
  system.suggestName("system")
}

class TEEHWSoC(implicit p :Parameters) extends WithTEEHWbaseShell()(p) with HasTEEHWbase with WithTEEHWbaseConnect {
}

trait HasTEEHWChip {
  implicit val p: Parameters
  val chip = Module(new TEEHWSoC)
  chip.suggestName("chip")
}

// ********************************************************************
// General traits for FPGA connections to the chip
// ********************************************************************

trait FPGAInternals {
  implicit val p: Parameters
  val outer : WithTEEHWbaseShell with WithTEEHWbaseConnect
  // Parameters that depends on the generated system (not only the config)
  val tlparam = outer.tlparam
  val aclkn = outer.aclkn
  val memserSourceBits = outer.system.sys.asInstanceOf[HasTEEHWSystemModule].serSourceBits
  val extserSourceBits = outer.system.sys.asInstanceOf[HasTEEHWSystemModule].extSourceBits
  // Clocks and resets
  val ChildClock = p(DDRPortOther).option(IO(Output(Clock())))
  val ChildReset = p(DDRPortOther).option(IO(Output(Bool())))
  val sys_clk = IO(Output(Clock()))
  val rst_n = IO(Output(Bool()))
  val jrst_n = IO(Output(Bool()))
  val usbClk = p(PeripheryUSB11HSKey).map(A => IO(Output(Clock())))
  // Memory port serialized
  val memser = p(ExtSerMem).map(A => IO(Flipped(new SerialIO(A.serWidth))))
  // Ext port serialized
  val extser = p(ExtSerBus).map(A => IO(Flipped(new SerialIO(A.serWidth))))
  // Memory port
  var tlport = tlparam.map(A => IO(Flipped(new TLUL(A))))
  // Asyncrhonoys clocks
  var aclocks = aclkn.map(A => IO(Vec(A, Output(Clock()))))
}

// ********************************************************************
// FPGAVC707 - Demo on VC707 FPGA board
// ********************************************************************
import sifive.fpgashells.ip.xilinx.vc707mig._
import sifive.fpgashells.ip.xilinx._

trait FPGAVC707ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters
  val gpio_in = IO(Input(UInt(p(GPIOInKey).W)))
  val gpio_out = IO(Output(UInt((p(PeripheryGPIOKey).head.width-p(GPIOInKey)).W)))
  val jtag = IO(new Bundle {
    val jtag_TDI = (Input(Bool())) // J19_20 / XADC_GPIO_2
    val jtag_TDO = (Output(Bool())) // J19_17 / XADC_GPIO_1
    val jtag_TCK = (Input(Bool())) // J19_19 / XADC_GPIO_3
    val jtag_TMS = (Input(Bool())) // J19_18 / XADC_GPIO_0
  })
  val sdio = IO(new Bundle {
    val sdio_clk = (Output(Bool()))
    val sdio_cmd = (Output(Bool()))
    val sdio_dat_0 = (Input(Bool()))
    val sdio_dat_1 = (Analog(1.W))
    val sdio_dat_2 = (Analog(1.W))
    val sdio_dat_3 = (Output(Bool()))
  })
  val uart_txd = IO(Output(Bool()))
  val uart_rxd = IO(Input(Bool()))

  var qspi: Option[TEEHWQSPIBundle] = None

  val USB = p(PeripheryUSB11HSKey).map{_ => IO(new Bundle {
    val FullSpeed = Output(Bool()) // D12 / LA05_N / J1_23
    val WireDataIn = Input(Bits(2.W)) // H7 / LA02_P / J1_9 // H8 / LA02_N / J1_11
    val WireCtrlOut = Output(Bool()) // D11 / LA05_P / J1_21
    val WireDataOut = Output(Bits(2.W)) // G9 / LA03_P / J1_13 // G10 / LA03_N / J1_15
  })}

  val pciePorts = p(IncludePCIe).option(IO(new XilinxVC707PCIeX1Pads))
  val xdmaPorts = p(XDMAPCIe).map(A => IO(new XDMATopPads(A.lanes)))

}

trait FPGAVC707ClockAndResetsAndDDR {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters

  val sys_clock_p = IO(Input(Clock()))
  val sys_clock_n = IO(Input(Clock()))
  val rst_0 = IO(Input(Bool()))
  val rst_1 = IO(Input(Bool()))
  val rst_2 = IO(Input(Bool()))
  val rst_3 = IO(Input(Bool()))

  var ddr: Option[VC707MIGIODDR] = None
}

class FPGAVC707Shell(implicit val p :Parameters) extends RawModule
  with FPGAVC707ChipShell
  with FPGAVC707ClockAndResetsAndDDR {
}

class FPGAVC707Internal(val outer: WithTEEHWbaseShell with WithTEEHWbaseConnect)(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGAVC707ClockAndResetsAndDDR {
  val namedclocks = outer.system.sys.asInstanceOf[HasTEEHWSystemModule].namedclocks

  val init_calib_complete = IO(Output(Bool()))
  var depth = BigInt(0)

  // Some connections for having the clocks
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

  // PLL instance
  val c = new PLLParameters(
    name = "pll",
    input = PLLInClockParameters(freqMHz = 200.0, feedback = true),
    req = Seq(
      PLLOutClockParameters(freqMHz = 48.0),
      PLLOutClockParameters(freqMHz = 10.0),
      PLLOutClockParameters(freqMHz = p(FreqKeyMHz))
    )
  )
  val pll = Module(new Series7MMCM(c))
  pll.io.clk_in1 := sys_clk_i
  pll.io.reset := reset_0

  withClockAndReset(clock, reset) {
    val aresetn = !reset_0 // Reset that goes to the MMCM inside of the DDR MIG
    val sys_rst = ResetCatchAndSync(pll.io.clk_out3.get, !pll.io.locked) // Catched system clock
    val reset_2 = WireInit(!pll.io.locked) // If DDR is not present, this is the system reset

    // The DDR port
    init_calib_complete := false.B
    tlport.foreach{ chiptl =>
      val mod = Module(LazyModule(new TLULtoMIG(chiptl.params)).module)

      // DDR port only
      ddr = Some(IO(new VC707MIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport
      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := sys_clk_i.asUInt()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_2 := ResetCatchAndSync(pll.io.clk_out3.get, mod.io.ddrport.ui_clk_sync_rst)

      // TileLink Interface from platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      if(p(DDRPortOther)) {
        ChildClock.foreach(_ := pll.io.clk_out2.getOrElse(false.B))
        ChildReset.foreach(_ := reset_2)
        mod.clock := pll.io.clk_out2.getOrElse(false.B)
      }
      else {
        ChildClock.foreach(_ := pll.io.clk_out3.getOrElse(false.B))
        ChildReset.foreach(_ := reset_2)
        mod.clock := pll.io.clk_out3.getOrElse(false.B)
      }

      init_calib_complete := mod.io.ddrport.init_calib_complete
      depth = mod.depth
    }
    (memser zip memserSourceBits).foreach { case(ms, sourceBits) =>
      val mod = Module(LazyModule(new SertoMIG(ms.w, sourceBits)).module)

      // Serial port
      mod.io.serport.flipConnect(ms)

      // DDR port only
      ddr = Some(IO(new VC707MIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport
      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := sys_clk_i.asUInt()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_2 := ResetCatchAndSync(pll.io.clk_out3.get, mod.io.ddrport.ui_clk_sync_rst)

      p(SbusToMbusXTypeKey) match {
        case _: AsynchronousCrossing =>
          mod.clock := pll.io.clk_out2.getOrElse(false.B)
        case _ =>
          mod.clock := pll.io.clk_out3.getOrElse(false.B)
      }

      init_calib_complete := mod.io.ddrport.init_calib_complete
      depth = mod.depth
    }

    // Main clock and reset assignments
    clock := pll.io.clk_out3.get
    reset := reset_2
    sys_clk := pll.io.clk_out3.get
    aclocks.foreach(_.foreach(_ := pll.io.clk_out3.get)) // Connecting all aclocks to the default sysclock.
    rst_n := !reset_2
    jrst_n := !reset_2
    usbClk.foreach(_ := pll.io.clk_out1.getOrElse(false.B))

    // Clock controller
    (extser zip extserSourceBits).foreach { case(es, sourceBits) =>
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

    // Connect any possible mbus clock into clock_2, only if clock is separated
    p(SbusToMbusXTypeKey) match {
      case _: AsynchronousCrossing =>
        // Search for the aclock that belongs to mbus
        aclocks.foreach{ aclocks =>
          println(s"Connecting clock for MBus to clock2 =>")
          (aclocks zip namedclocks).foreach { case (aclk, nam) =>
            println(s"  Detected clock ${nam}")
            if(nam.contains("mbus")) {
              aclk := pll.io.clk_out2.getOrElse(false.B)
              println("    Connected!")
            }
          }
        }
      case _ =>
      // Nothing
    }
  }
}

trait WithFPGAVC707Connect {
  this: FPGAVC707Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGAVC707Internal(chip))

  // To intern = Clocks and resets
  intern.sys_clock_p := sys_clock_p
  intern.sys_clock_n := sys_clock_n
  intern.rst_0 := rst_0
  intern.rst_1 := rst_1
  intern.rst_2 := rst_2
  intern.rst_3 := rst_3
  ddr = intern.ddr.map{ A =>
    val port = IO(new VC707MIGIODDR(intern.depth))
    port <> A
    port
  }

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
}

class FPGAVC707(implicit p :Parameters) extends FPGAVC707Shell()(p)
  with HasTEEHWChip with WithFPGAVC707Connect {
}

// ********************************************************************
// FPGAVCU118 - Demo on VCU118 FPGA board
// ********************************************************************
import sifive.fpgashells.ip.xilinx.vcu118mig._
import sifive.fpgashells.ip.xilinx._

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

class FPGAVCU118Internal(val outer: WithTEEHWbaseShell with WithTEEHWbaseConnect)(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGAVCU118ClockAndResetsAndDDR {
  val namedclocks = outer.system.sys.asInstanceOf[HasTEEHWSystemModule].namedclocks

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

  withClockAndReset(clock, reset) {
    // PLL instance
    val c = new PLLParameters(
      name = "pll",
      input = PLLInClockParameters(freqMHz = 250.0, feedback = true),
      req = Seq(
        PLLOutClockParameters(freqMHz = 48.0),
        PLLOutClockParameters(freqMHz = 50.0),
        PLLOutClockParameters(freqMHz = p(FreqKeyMHz))
      )
    )
    val pll = Module(new Series7MMCM(c))
    pll.io.clk_in1 := sys_clk_i
    pll.io.reset := reset_0

    val aresetn = !reset_0 // Reset that goes to the MMCM inside of the DDR MIG
    val sys_rst = ResetCatchAndSync(pll.io.clk_out3.get, !pll.io.locked) // Catched system clock
    val reset_2 = WireInit(!pll.io.locked) // If DDR is not present, this is the system reset

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

      // TileLink Interface from platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      if (p(DDRPortOther)) {
        ChildClock.foreach(_ := pll.io.clk_out2.getOrElse(false.B))
        ChildReset.foreach(_ := reset_2)
        mod.clock := pll.io.clk_out2.getOrElse(false.B)
      }
      else {
        ChildClock.foreach(_ := pll.io.clk_out3.getOrElse(false.B))
        ChildReset.foreach(_ := reset_2)
        mod.clock := pll.io.clk_out3.getOrElse(false.B)
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

      init_calib_complete := mod.io.ddrport.c0_init_calib_complete
      depth = mod.depth
    }

    // Main clock and reset assignments
    clock := pll.io.clk_out3.get
    reset := reset_2
    sys_clk := pll.io.clk_out3.get
    aclocks.foreach(_.foreach(_ := pll.io.clk_out3.get)) // TODO: Connect your clocks here
    rst_n := !reset_2
    jrst_n := !reset_2
    usbClk.foreach(_ := pll.io.clk_out1.getOrElse(false.B))

    // Clock controller
    (extser zip extserSourceBits).foreach { case (es, sourceBits) =>
      val mod = Module(LazyModule(new FPGAMiniSystem(sourceBits)).module)

      // Serial port
      mod.serport.flipConnect(es)
    }
  }
}

trait WithFPGAVCU118Connect {
  this: FPGAVCU118Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGAVCU118Internal(chip))

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
}

class FPGAVCU118(implicit p :Parameters) extends FPGAVCU118Shell()(p)
  with HasTEEHWChip with WithFPGAVCU118Connect {
}

// ********************************************************************
// FPGAArtyA7 - Demo on Arty A7 100 FPGA board
// ********************************************************************
import sifive.fpgashells.ip.xilinx.arty100tmig._

trait FPGAArtyA7ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters
  //-----------------------------------------------------------------------
  // Interface
  //-----------------------------------------------------------------------

  // Green LEDs
  val led_0        = IO(Analog(1.W))
  val led_1        = IO(Analog(1.W))
  val led_2        = IO(Analog(1.W))
  val led_3        = IO(Analog(1.W))

  // RGB LEDs, 3 pins each
  val led0_r       = IO(Analog(1.W))
  val led0_g       = IO(Analog(1.W))
  val led0_b       = IO(Analog(1.W))

  val led1_r       = IO(Analog(1.W))
  val led1_g       = IO(Analog(1.W))
  val led1_b       = IO(Analog(1.W))

  val led2_r       = IO(Analog(1.W))
  val led2_g       = IO(Analog(1.W))
  val led2_b       = IO(Analog(1.W))

  // Sliding switches
  val sw_0         = IO(Analog(1.W))
  val sw_1         = IO(Analog(1.W))
  val sw_2         = IO(Analog(1.W))
  val sw_3         = IO(Analog(1.W))

  // Buttons. First 3 used as GPIO, the last is used as wakeup
  val btn_0        = IO(Analog(1.W))
  val btn_1        = IO(Analog(1.W))
  val btn_2        = IO(Analog(1.W))
  val btn_3        = IO(Analog(1.W))

  // Dedicated QSPI interface
  val qspi_cs      = IO(Analog(1.W))
  val qspi_sck     = IO(Analog(1.W))
  val qspi_dq      = IO(Vec(4, Analog(1.W)))

  // UART0
  val uart_rxd_out = IO(Analog(1.W))
  val uart_txd_in  = IO(Analog(1.W))

  // JA (Used for more generic GPIOs)
  val ja_0         = IO(Analog(1.W))
  val ja_1         = IO(Analog(1.W))
  val ja_2         = IO(Analog(1.W))
  val ja_3         = IO(Analog(1.W))
  val ja_4         = IO(Analog(1.W))
  val ja_5         = IO(Analog(1.W))
  val ja_6         = IO(Analog(1.W))
  val ja_7         = IO(Analog(1.W))

  // JC (used for additional debug/trace connection)
  val jc           = IO(Vec(8, Analog(1.W)))

  // JD (used for JTAG connection)
  val jd_0         = IO(Analog(1.W))  // TDO
  val jd_1         = IO(Analog(1.W))  // TRST_n
  val jd_2         = IO(Analog(1.W))  // TCK
  val jd_4         = IO(Analog(1.W))  // TDI
  val jd_5         = IO(Analog(1.W))  // TMS
  val jd_6         = IO(Analog(1.W))  // SRST_n

  // ChipKit Digital I/O Pins
  val ck_io        = IO(Vec(20, Analog(1.W)))

  // ChipKit SPI
  val ck_miso      = IO(Analog(1.W))
  val ck_mosi      = IO(Analog(1.W))
  val ck_ss        = IO(Analog(1.W))
  val ck_sck       = IO(Analog(1.W))
}

trait FPGAArtyA7ClockAndResetsAndDDR {
  // This trait only contains clocks and resets exclusive for the FPGA
  implicit val p: Parameters

  // Clock & Reset
  val CLK100MHZ    = IO(Input(Clock()))
  val ck_rst       = IO(Input(Bool()))
  // DDR
  var ddr: Option[Arty100TMIGIODDR] = None
}

class FPGAArtyA7Shell(implicit val p :Parameters) extends RawModule
  with FPGAArtyA7ChipShell
  with FPGAArtyA7ClockAndResetsAndDDR {
}

class FPGAArtyA7Internal(val outer: WithTEEHWbaseShell with WithTEEHWbaseConnect)(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGAArtyA7ClockAndResetsAndDDR {

  val namedclocks = outer.system.sys.asInstanceOf[HasTEEHWSystemModule].namedclocks

  val init_calib_complete = IO(Output(Bool()))
  var depth = BigInt(0)

  // Some connections for having the clocks
  val clock = Wire(Clock())
  val reset = Wire(Bool())

  //-----------------------------------------------------------------------
  // Clock Generator
  //-----------------------------------------------------------------------
  // Mixed-mode clock generator
  //val clock_8MHz     = Wire(Clock())
  //val clock_32MHz    = Wire(Clock())
  //val clock_65MHz    = Wire(Clock())
  //val mmcm_locked    = Wire(Bool())
  if(false) { // NOTE: Changed by using just a Series7 DDR
    val ip_mmcm = Module(new mmcm())

    ip_mmcm.io.clk_in1 := CLK100MHZ
    //clock_8MHz         := ip_mmcm.io.clk_out1  // 8.388 MHz = 32.768 kHz * 256
    //clock_65MHz        := ip_mmcm.io.clk_out2  // 65 Mhz
    //clock_32MHz        := ip_mmcm.io.clk_out3  // 65/2 Mhz
    ip_mmcm.io.resetn  := ck_rst
    //mmcm_locked        := ip_mmcm.io.locked
  }

  withClockAndReset(clock, reset) {
    // PLL instance
    val c = new PLLParameters(
      name = "pll",
      input = PLLInClockParameters(freqMHz = 100.0, feedback = true),
      req = Seq(
        PLLOutClockParameters(freqMHz = p(FreqKeyMHz)),
        PLLOutClockParameters(freqMHz = 166.666), // For sys_clk_i
        PLLOutClockParameters(freqMHz = 200.0) // For ref_clk
      ) ++ (if (p(DDRPortOther)) Seq(PLLOutClockParameters(freqMHz = 10.0)) else Seq())
    )
    val pll = Module(new Series7MMCM(c))
    pll.io.clk_in1 := CLK100MHZ
    pll.io.reset := !ck_rst

    val aresetn = pll.io.locked // Reset that goes to the MMCM inside of the DDR MIG
    val sys_rst = ResetCatchAndSync(pll.io.clk_out2.get, !pll.io.locked) // Catched system clock
    val reset_2 = WireInit(!pll.io.locked) // If DDR is not present, this is the system reset
    // The DDR port
    tlport.foreach{ chiptl =>
      val mod = Module(LazyModule(new TLULtoMIGArtyA7(chiptl.params)).module)

      // DDR port only
      ddr = Some(IO(new Arty100TMIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport
      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := pll.io.clk_out2.get.asBool()
      mod.io.ddrport.clk_ref_i := pll.io.clk_out3.get.asBool()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_2 := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.ui_clk_sync_rst)

      // TileLink Interface from platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      if (p(DDRPortOther)) {
        ChildClock.foreach(_ := pll.io.clk_out4.getOrElse(false.B))
        ChildReset.foreach(_ := reset_2)
        mod.clock := pll.io.clk_out4.getOrElse(false.B)
      }
      else {
        ChildClock.foreach(_ := pll.io.clk_out1.getOrElse(false.B))
        ChildReset.foreach(_ := reset_2)
        mod.clock := pll.io.clk_out1.getOrElse(false.B)
      }

      init_calib_complete := mod.io.ddrport.init_calib_complete
      depth = mod.depth
    }
    (memser zip memserSourceBits).foreach { case(ms, sourceBits) =>
      val mod = Module(LazyModule(new SertoMIGArtyA7(ms.w, sourceBits)).module)

      // Serial port
      mod.io.serport.flipConnect(ms)

      // DDR port only
      ddr = Some(IO(new Arty100TMIGIODDR(mod.depth)))
      ddr.get <> mod.io.ddrport
      // MIG connections, like resets and stuff
      mod.io.ddrport.sys_clk_i := pll.io.clk_out2.get.asBool()
      mod.io.ddrport.clk_ref_i := pll.io.clk_out3.get.asBool()
      mod.io.ddrport.aresetn := aresetn
      mod.io.ddrport.sys_rst := sys_rst
      reset_2 := ResetCatchAndSync(pll.io.clk_out1.get, mod.io.ddrport.ui_clk_sync_rst)

      p(SbusToMbusXTypeKey) match {
        case _: AsynchronousCrossing =>
          mod.clock := pll.io.clk_out4.getOrElse(false.B)
        case _ =>
          mod.clock := pll.io.clk_out1.getOrElse(false.B)
      }

      init_calib_complete := mod.io.ddrport.init_calib_complete
      depth = mod.depth
    }

    // Main clock and reset assignments
    clock := pll.io.clk_out1.get
    reset := reset_2
    sys_clk := pll.io.clk_out1.get
    aclocks.foreach(_.foreach(_ := pll.io.clk_out1.get)) // Connecting all aclocks to the default sysclock.
    rst_n := !reset_2
    jrst_n := !reset_2
    usbClk.foreach(_ := false.B.asClock())

    // NOTE: No extser
    //chip.extser
  }
}

trait WithFPGAArtyA7Connect {
  this: FPGAArtyA7Shell =>
  val chip: WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGAArtyA7Internal(chip))
  // To intern = Clocks and resets
  intern.CLK100MHZ := CLK100MHZ
  intern.ck_rst := ck_rst
  ddr = intern.ddr.map{ A =>
    val port = IO(new Arty100TMIGIODDR(intern.depth))
    port <> A
    port
  }

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

  // GPIO
  IOBUF(led_0, chip.gpio_out(0))
  IOBUF(led_1, chip.gpio_out(1))
  IOBUF(led_2, chip.gpio_out(2))
  IOBUF(led_3, intern.init_calib_complete) //chip.gpio_out(3)
  chip.gpio_in := Cat(IOBUF(sw_3), IOBUF(sw_2), IOBUF(sw_1), IOBUF(sw_0))

  // JTAG
  chip.jtag.jtag_TCK := IBUFG(IOBUF(jd_2).asClock()).asBool()
  chip.jtag.jtag_TDI := IOBUF(jd_4)
  PULLUP(jd_4)
  IOBUF(jd_0, chip.jtag.jtag_TDO)
  chip.jtag.jtag_TMS := IOBUF(jd_5)
  PULLUP(jd_5)
  chip.jrst_n := IOBUF(jd_6)
  PULLUP(jd_6)

  // QSPI (assuming only one)
  chip.qspi.foreach{ qspi =>
    IOBUF(qspi_sck, qspi.qspi_sck)
    IOBUF(qspi_cs,  qspi.qspi_cs(0))

    IOBUF(qspi_dq(0), qspi.qspi_mosi)
    qspi.qspi_miso := IOBUF(qspi_dq(1))
    IOBUF(qspi_dq(2), qspi.qspi_wp)
    IOBUF(qspi_dq(3), qspi.qspi_hold)
  }

  // UART
  chip.uart_rxd := IOBUF(uart_txd_in)	  // UART_TXD
  IOBUF(uart_rxd_out, chip.uart_txd) 	  // UART_RXD

  // SD IO
  IOBUF(ja_0, chip.sdio.sdio_dat_3)
  IOBUF(ja_1, chip.sdio.sdio_cmd)
  chip.sdio.sdio_dat_0 := IOBUF(ja_2)
  IOBUF(ja_3, chip.sdio.sdio_clk)

  // USB phy connections
  // TODO: Not possible to create the 48MHz
}

class FPGAArtyA7(implicit p :Parameters) extends FPGAArtyA7Shell()(p)
  with HasTEEHWChip with WithFPGAArtyA7Connect {
}

// ********************************************************************
// FPGADE4 - Demo on DE4 FPGA board
// ********************************************************************

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
  val HSMA_CLKIN_n1 = IO(Input(Bool()))
  val HSMA_CLKIN_n2 = IO(Input(Bool()))
  val HSMA_CLKIN_p1 = IO(Input(Bool()))
  val HSMA_CLKIN_p2 = IO(Input(Bool()))
  val HSMA_CLKIN0 = IO(Input(Bool()))
  val HSMA_CLKOUT_n2 = IO(Output(Bool()))
  val HSMA_CLKOUT_p2 = IO(Output(Bool()))
  val HSMA_D = IO(Vec(4, Analog(1.W)))
  //val HSMA_GXB_RX_p = IO(Input(Bits((3+1).W)))
  //val HSMA_GXB_TX_p = IO(Output(Bits((3+1).W)))
  val HSMA_OUT_n1 = IO(Analog(1.W))
  val HSMA_OUT_p1 = IO(Analog(1.W))
  val HSMA_OUT0 = IO(Analog(1.W))
  //val HSMA_REFCLK_p = IO(Input(Bool()))
  val HSMA_RX_n = IO(Vec(17, Analog(1.W)))
  val HSMA_RX_p = IO(Vec(17, Analog(1.W)))
  val HSMA_TX_n = IO(Vec(17, Analog(1.W)))
  val HSMA_TX_p = IO(Vec(17, Analog(1.W)))

  //////////// HSMC_B //////////
  val HSMB_CLKIN_n1 = IO(Input(Bool()))
  val HSMB_CLKIN_n2 = IO(Input(Bool()))
  val HSMB_CLKIN_p1 = IO(Input(Bool()))
  val HSMB_CLKIN_p2 = IO(Input(Bool()))
  val HSMB_CLKIN0 = IO(Input(Bool()))
  val HSMB_CLKOUT_n2 = IO(Output(Bool()))
  val HSMB_CLKOUT_p2 = IO(Output(Bool()))
  val HSMB_D = IO(Vec(4, Analog(1.W)))
  //val HSMB_GXB_RX_p = IO(Input(Bits((7+1).W)))
  //val HSMB_GXB_TX_p = IO(Output(Bits((7+1).W)))
  val HSMB_OUT_n1 = IO(Analog(1.W))
  val HSMB_OUT_p1 = IO(Analog(1.W))
  val HSMB_OUT0 = IO(Analog(1.W))
  //val HSMB_REFCLK_p = IO(Input(Bool()))
  val HSMB_RX_n = IO(Vec(17, Analog(1.W)))
  val HSMB_RX_p = IO(Vec(17, Analog(1.W)))
  val HSMB_TX_n = IO(Vec(17, Analog(1.W)))
  val HSMB_TX_p = IO(Vec(17, Analog(1.W)))

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

class FPGADE4Internal(val outer: WithTEEHWbaseShell with WithTEEHWbaseConnect)(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGADE4ClockAndResetsAndDDR {

  val namedclocks = outer.system.sys.asInstanceOf[HasTEEHWSystemModule].namedclocks

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
    tlport.foreach { chiptl =>
      // Instance our converter, and connect everything
      val mod = Module(LazyModule(new TLULtoQuartusPlatform(chiptl.params)).module)

      // Quartus Platform connections
      M1_DDR2_addr := mod.io.qport.memory_mem_a
      M1_DDR2_ba := mod.io.qport.memory_mem_ba
      M1_DDR2_clk := mod.io.qport.memory_mem_ck
      M1_DDR2_clk_n := mod.io.qport.memory_mem_ck_n
      M1_DDR2_cke := mod.io.qport.memory_mem_cke
      M1_DDR2_cs_n := mod.io.qport.memory_mem_cs_n
      M1_DDR2_dm := mod.io.qport.memory_mem_dm
      M1_DDR2_ras_n := mod.io.qport.memory_mem_ras_n
      M1_DDR2_cas_n := mod.io.qport.memory_mem_cas_n
      M1_DDR2_we_n := mod.io.qport.memory_mem_we_n
      attach(M1_DDR2_dq, mod.io.qport.memory_mem_dq)
      attach(M1_DDR2_dqs, mod.io.qport.memory_mem_dqs)
      attach(M1_DDR2_dqsn, mod.io.qport.memory_mem_dqs_n)
      M1_DDR2_odt := mod.io.qport.memory_mem_odt
      mod.io.qport.oct_rdn := M1_DDR2_oct_rdn
      mod.io.qport.oct_rup := M1_DDR2_oct_rup
      mod.io.ckrst.ddr_ref_clk := OSC_50_BANK3.asUInt()
      mod.io.ckrst.qsys_ref_clk := OSC_50_BANK5.asUInt()
      mod.io.ckrst.system_reset_n := CPU_RESET_n

      // TileLink Interface from platform
      // TODO: Make the DDR optional. Need to stop using the Quartus Platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      val reset_to_sys = ResetCatchAndSync(mod.io.ckrst.qsys_clk, !mod.io.qport.mem_status_local_init_done)
      val reset_to_child = ResetCatchAndSync(mod.io.ckrst.io_clk, !mod.io.qport.mem_status_local_init_done)
      mem_status_local_cal_fail := mod.io.qport.mem_status_local_cal_fail
      mem_status_local_cal_success := mod.io.qport.mem_status_local_cal_success
      mem_status_local_init_done := mod.io.qport.mem_status_local_init_done

      // Clock and reset (for TL stuff)
      clock := mod.io.ckrst.qsys_clk
      reset := reset_to_sys
      sys_clk := mod.io.ckrst.qsys_clk
      aclocks.foreach(_.foreach(_ := mod.io.ckrst.qsys_clk)) // TODO: Connect your clocks here
      rst_n := !reset_to_sys
      jrst_n := !reset_to_sys
      usbClk.foreach(_ := mod.io.ckrst.usb_clk)
      if(p(DDRPortOther)) {
        ChildClock.foreach(_ := mod.io.ckrst.io_clk)
        ChildReset.foreach(_ := reset_to_child)
        mod.clock := mod.io.ckrst.io_clk
      }
      else {
        ChildClock.foreach(_ := mod.io.ckrst.qsys_clk)
        ChildReset.foreach(_ := reset_to_sys)
        mod.clock := mod.io.ckrst.qsys_clk
      }
    }
    // The external bus (TODO: Doing nothing)
    (extser zip extserSourceBits).foreach { case (es, sourceBits) =>
      val mod = Module(LazyModule(new FPGAMiniSystem(sourceBits)).module)

      // Serial port
      mod.serport.flipConnect(es)
    }
  }
}

trait WithFPGADE4Connect {
  this: FPGADE4Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGADE4Internal(chip))

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
  ALT_IOBUF(GPIO1_D(10), chip.jtag.jtag_TDI)
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
    chipport.USBWireDataIn := ALT_IOBUF(GPIO1_D(24))
    ALT_IOBUF(GPIO1_D(24), chipport.USBWireCtrlOut(0))
    ALT_IOBUF(GPIO1_D(26), chipport.USBWireCtrlOut(1))
    ALT_IOBUF(GPIO1_D(16), chipport.USBWireDataOut(0))
    ALT_IOBUF(GPIO1_D(18), chipport.USBWireDataOut(1))

    chipport.usbClk := uclk
  }
}

class FPGADE4(implicit p :Parameters) extends FPGADE4Shell()(p)
  with HasTEEHWChip with WithFPGADE4Connect {
}

// ********************************************************************
// FPGATR4 - Demo on TR4 FPGA board
// ********************************************************************

trait FPGATR4ChipShell {
  // This trait only contains the connections that are supposed to be handled by the chip
  implicit val p: Parameters

  ///////// LED /////////
  val LED = IO(Output(Bits((3 + 1).W)))

  ///////// SW /////////
  val SW = IO(Input(Bits((3 + 1).W)))

  ///////// FAN /////////
  val FAN_CTRL = IO(Output(Bool()))

  //////////// HSMC_A //////////
  val HSMA_CLKIN0 = IO(Input(Bool()))
  val HSMA_CLKIN_n1 = IO(Input(Bool()))
  val HSMA_CLKIN_n2 = IO(Input(Bool()))
  val HSMA_CLKIN_p1 = IO(Input(Bool()))
  val HSMA_CLKIN_p2 = IO(Input(Bool()))
  val HSMA_D = IO(Vec(4, Analog(1.W)))
  val HSMA_OUT0 = IO(Analog(1.W))
  val HSMA_OUT_n1 = IO(Analog(1.W))
  val HSMA_OUT_n2 = IO(Analog(1.W))
  val HSMA_OUT_p1 = IO(Analog(1.W))
  val HSMA_OUT_p2 = IO(Analog(1.W))
  val HSMA_RX_n = IO(Vec(17, Analog(1.W)))
  val HSMA_RX_p = IO(Vec(17, Analog(1.W)))
  val HSMA_TX_n = IO(Vec(17, Analog(1.W)))
  val HSMA_TX_p = IO(Vec(17, Analog(1.W)))

  //////////// HSMC_B //////////
  val HSMB_CLKIN0 = IO(Input(Bool()))
  val HSMB_CLKIN_n1 = IO(Input(Bool()))
  val HSMB_CLKIN_n2 = IO(Input(Bool()))
  val HSMB_CLKIN_p1 = IO(Input(Bool()))
  val HSMB_CLKIN_p2 = IO(Input(Bool()))
  val HSMB_D = IO(Vec(4, Analog(1.W)))
  val HSMB_OUT0 = IO(Analog(1.W))
  val HSMB_OUT_n1 = IO(Analog(1.W))
  val HSMB_OUT_n2 = IO(Analog(1.W))
  val HSMB_OUT_p1 = IO(Analog(1.W))
  val HSMB_OUT_p2 = IO(Analog(1.W))
  val HSMB_RX_n = IO(Vec(17, Analog(1.W)))
  val HSMB_RX_p = IO(Vec(17, Analog(1.W)))
  val HSMB_TX_n = IO(Vec(17, Analog(1.W)))
  val HSMB_TX_p = IO(Vec(17, Analog(1.W)))

  //////////// HSMC_C //////////
  val GPIO0_D = IO(Vec(35+1, Analog(1.W)))
  val GPIO1_D = IO(Vec(35+1, Analog(1.W)))

  //////////// HSMC_D //////////
  val HSMD_CLKIN0 = IO(Input(Bool()))
  val HSMD_CLKIN_n1 = IO(Input(Bool()))
  val HSMD_CLKIN_n2 = IO(Input(Bool()))
  val HSMD_CLKIN_p1 = IO(Input(Bool()))
  val HSMD_CLKIN_p2 = IO(Input(Bool()))
  val HSMD_CLKOUT_n1 = IO(Analog(1.W))
  val HSMD_CLKOUT_p1 = IO(Analog(1.W))
  val HSMD_D = IO(Vec(4, Analog(1.W)))
  val HSMD_OUT0 = IO(Analog(1.W))
  val HSMD_OUT_n2 = IO(Analog(1.W))
  val HSMD_OUT_p2 = IO(Analog(1.W))
  val HSMD_RX_n = IO(Vec(17, Analog(1.W)))
  val HSMD_RX_p = IO(Vec(17, Analog(1.W)))
  val HSMD_TX_n = IO(Vec(17, Analog(1.W)))
  val HSMD_TX_p = IO(Vec(17, Analog(1.W)))

  //////////// HSMC_E //////////
  val HSME_CLKIN0 = IO(Input(Bool()))
  val HSME_CLKIN_n1 = IO(Input(Bool()))
  val HSME_CLKIN_n2 = IO(Input(Bool()))
  val HSME_CLKIN_p1 = IO(Input(Bool()))
  val HSME_CLKIN_p2 = IO(Input(Bool()))
  val HSME_CLKOUT_n1 = IO(Analog(1.W))
  val HSME_CLKOUT_p1 = IO(Analog(1.W))
  val HSME_D = IO(Vec(4, Analog(1.W)))
  val HSME_OUT0 = IO(Analog(1.W))
  val HSME_OUT_n2 = IO(Analog(1.W))
  val HSME_OUT_p2 = IO(Analog(1.W))
  val HSME_RX_n = IO(Vec(17, Analog(1.W)))
  val HSME_RX_p = IO(Vec(17, Analog(1.W)))
  val HSME_TX_n = IO(Vec(17, Analog(1.W)))
  val HSME_TX_p = IO(Vec(17, Analog(1.W)))

  //////////// HSMC_F //////////
  val HSMF_CLKIN0 = IO(Input(Bool()))
  val HSMF_CLKIN_n1 = IO(Input(Bool()))
  val HSMF_CLKIN_n2 = IO(Input(Bool()))
  val HSMF_CLKIN_p1 = IO(Input(Bool()))
  val HSMF_CLKIN_p2 = IO(Input(Bool()))
  val HSMF_CLKOUT_n1 = IO(Analog(1.W))
  val HSMF_CLKOUT_p1 = IO(Analog(1.W))
  val HSMF_D = IO(Vec(4, Analog(1.W)))
  val HSMF_OUT0 = IO(Analog(1.W))
  val HSMF_OUT_n2 = IO(Analog(1.W))
  val HSMF_OUT_p2 = IO(Analog(1.W))
  val HSMF_RX_n = IO(Vec(17, Analog(1.W)))
  val HSMF_RX_p = IO(Vec(17, Analog(1.W)))
  val HSMF_TX_n = IO(Vec(17, Analog(1.W)))
  val HSMF_TX_p = IO(Vec(17, Analog(1.W)))

  ///////// GPIO /////////
  /*val jtag = IO(new Bundle {
    val jtag_TDI = (Input(Bool()))  // PIN_AP27 / GPIO1_D4 / JP10 5
    val jtag_TMS = (Input(Bool()))  // PIN_AN27 / GPIO1_D6 / JP10 7
    val jtag_TCK = (Input(Bool()))  // PIN_AL25 / GPIO1_D8 / JP10 9
    val jtag_TDO = (Output(Bool())) // PIN_AP26 / GPIO1_D10 / JP10 13
  })
  val sdio = IO(new Bundle {
    val sdio_clk = (Output(Bool())) // PIN_AV32 / GPIO0_D28 / JP9 33
    val sdio_cmd = (Output(Bool())) // PIN_AW32 / GPIO0_D30 / JP9 35
    val sdio_dat_0 = (Input(Bool())) // PIN_AV28 / GPIO0_D32 / JP9 37
    val sdio_dat_1 = (Analog(1.W))
    val sdio_dat_2 = (Analog(1.W))
    val sdio_dat_3 = (Output(Bool())) // PIN_AW29 / GPIO0_D34 / JP9 39
  })

  var qspi: Option[TEEHWQSPIBundle] = None

  val USB = p(PeripheryUSB11HSKey).map { _ =>
    IO(new Bundle {
      val FullSpeed = Output(Bool()) // HSMC_TX_p[10] / PIN_AW27 / GPIO1_D17 GPIO1[20]
      val WireDataIn = Input(Bits(2.W)) // HSMC_TX_p[7] HSMC_TX_n[7] / PIN_AB30 PIN_AB31 / GPIO1_D24 GPIO1_D26 GPIO1[27,31]
      val WireCtrlOut = Output(Bool()) // HSMC_TX_n[10] / PIN_AW28 / GPIO1_D19 GPIO[22]
      val WireDataOut = Output(Bits(2.W)) // HSMC_TX_p[8] HSMC_TX_n[8] / PIN_AL27 PIN_AH26 / GPIO1_D16 GPIO1_D18 GPIO1[19,21]
    })
  }

  //////////// Uart //////////
  val UART_TXD = IO(Output(Bool())) // GPIO1_D34 / PIN_AG30 / JP10 39
  val UART_RXD = IO(Input(Bool())) // GPIO1_D35 / PIN_AD29 / JP10 40
*/
  FAN_CTRL := true.B
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

class FPGATR4Internal(val outer: WithTEEHWbaseShell with WithTEEHWbaseConnect)(implicit val p :Parameters) extends RawModule
  with FPGAInternals
  with FPGATR4ClockAndResetsAndDDR {

  val namedclocks = outer.system.sys.asInstanceOf[HasTEEHWSystemModule].namedclocks

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
    tlport.foreach { chiptl =>
      // Instance our converter, and connect everything
      val mod = Module(LazyModule(new TLULtoQuartusPlatform(
        chiptl.params,
        QuartusDDRConfig(size_ck = 1, is_reset = true)
      )).module)

      // Quartus Platform connections
      mem_a := mod.io.qport.memory_mem_a
      mem_ba := mod.io.qport.memory_mem_ba
      mem_ck := mod.io.qport.memory_mem_ck(0) // Force only 1 line (although the config forces 1 line)
      mem_ck_n := mod.io.qport.memory_mem_ck_n(0) // Force only 1 line (although the config forces 1 line)
      mem_cke := mod.io.qport.memory_mem_cke
      mem_cs_n := mod.io.qport.memory_mem_cs_n
      mem_dm := mod.io.qport.memory_mem_dm
      mem_ras_n := mod.io.qport.memory_mem_ras_n
      mem_cas_n := mod.io.qport.memory_mem_cas_n
      mem_we_n := mod.io.qport.memory_mem_we_n
      attach(mem_dq, mod.io.qport.memory_mem_dq)
      attach(mem_dqs, mod.io.qport.memory_mem_dqs)
      attach(mem_dqs_n, mod.io.qport.memory_mem_dqs_n)
      mem_odt := mod.io.qport.memory_mem_odt
      mem_reset_n := mod.io.qport.memory_mem_reset_n.getOrElse(true.B)
      mod.io.qport.oct_rdn := mem_oct_rdn
      mod.io.qport.oct_rup := mem_oct_rup

      mod.io.ckrst.ddr_ref_clk := OSC_50_BANK1.asUInt()
      mod.io.ckrst.qsys_ref_clk := OSC_50_BANK4.asUInt() // TODO: This is okay?
      mod.io.ckrst.system_reset_n := BUTTON(2)

      // TileLink Interface from platform
      // TODO: Make the DDR optional. Need to stop using the Quartus Platform
      mod.io.tlport.a <> chiptl.a
      chiptl.d <> mod.io.tlport.d

      val reset_to_sys = ResetCatchAndSync(mod.io.ckrst.qsys_clk, !mod.io.qport.mem_status_local_init_done)
      val reset_to_child = ResetCatchAndSync(mod.io.ckrst.io_clk, !mod.io.qport.mem_status_local_init_done)
      mem_status_local_cal_fail := mod.io.qport.mem_status_local_cal_fail
      mem_status_local_cal_success := mod.io.qport.mem_status_local_cal_success
      mem_status_local_init_done := mod.io.qport.mem_status_local_init_done

      // Clock and reset (for TL stuff)
      clock := mod.io.ckrst.qsys_clk
      reset := reset_to_sys
      sys_clk := mod.io.ckrst.qsys_clk
      aclocks.foreach(_.foreach(_ := mod.io.ckrst.qsys_clk)) // TODO: Connect your clocks here
      rst_n := !reset_to_sys
      jrst_n := !reset_to_sys
      usbClk.foreach(_ := mod.io.ckrst.usb_clk)
      if(p(DDRPortOther)) {
        ChildClock.foreach(_ := mod.io.ckrst.io_clk)
        ChildReset.foreach(_ := reset_to_child)
        mod.clock := mod.io.ckrst.io_clk
      }
      else {
        ChildClock.foreach(_ := mod.io.ckrst.qsys_clk)
        ChildReset.foreach(_ := reset_to_sys)
        mod.clock := mod.io.ckrst.qsys_clk
      }
    }
    // The external bus (TODO: Doing nothing)
    (extser zip extserSourceBits).foreach { case (es, sourceBits) =>
      val mod = Module(LazyModule(new FPGAMiniSystem(sourceBits)).module)

      // Serial port
      mod.serport.flipConnect(es)
    }
  }
}

trait WithFPGATR4Connect {
  this: FPGATR4Shell =>
  val chip : WithTEEHWbaseShell with WithTEEHWbaseConnect
  val intern = Module(new FPGATR4Internal(chip))

  // To intern = Clocks and resets
  intern.OSC_50_BANK1 := OSC_50_BANK1
  intern.OSC_50_BANK3 := OSC_50_BANK3
  intern.OSC_50_BANK4 := OSC_50_BANK4
  intern.OSC_50_BANK7 := OSC_50_BANK7
  intern.OSC_50_BANK8 := OSC_50_BANK8
  intern.BUTTON := BUTTON

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
  ALT_IOBUF(GPIO1_D(10), chip.jtag.jtag_TDI)
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

class FPGATR4(implicit p :Parameters) extends FPGATR4Shell()(p)
  with HasTEEHWChip with WithFPGATR4Connect {
}