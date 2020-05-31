package uec.teehardware

import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import sifive.blocks.devices.pinctrl._
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.fpgashells.clocks._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import uec.teehardware.devices.usb11hs._

// *********************************************************************************
// TEEHW wrapper - for doing a wrapper with actual ports (tri-state buffers at least)
// *********************************************************************************

class TEEHWwrapper(implicit p :Parameters) extends RawModule {
  // The actual pins of this module.
  // This is a list of the ports 'to be wirebonded' / 'from the package'
  val clk_p = IO(Analog(1.W))
  val clk_n = IO(Analog(1.W))
  val rst_n = IO(Analog(1.W))
  val gpio = IO(Vec(p(PeripheryGPIOKey).head.width, Analog(1.W)))
  val jtag_tdi = IO(Analog(1.W))
  val jtag_tdo = IO(Analog(1.W))
  val jtag_tck = IO(Analog(1.W))
  val jtag_tms = IO(Analog(1.W))
  val jtag_rst = IO(Analog(1.W))
  val spi_cs = IO(Vec(p(PeripherySPIKey).head.csWidth, Analog(1.W)))
  val spi_sck = IO(Analog(1.W))
  val spi_dq = IO(Vec(4, Analog(1.W)))
  val qspi_cs =  IO(Vec(p(PeripherySPIFlashKey).head.csWidth, Analog(1.W)))
  val qspi_sck = IO(Analog(1.W))
  val qspi_dq = IO(Vec(4, Analog(1.W)))
  //val i2c_sda = IO(Analog(1.W))
  //val i2c_scl = IO(Analog(1.W))
  val uart_txd = IO(Analog(1.W))
  val uart_rxd = IO(Analog(1.W))

  // This clock and reset are only declared. We soon connect them
  val clock = Wire(Clock())
  val reset = Wire(Bool())

  // An option to dynamically assign
  var tlport : Option[TLUL] = None

  // All the modules declared here have this clock and reset
  withClockAndReset(clock, reset) {
    // Main clock
    val clock_XTAL = Module(new XTAL_DRV)
    clock := clock_XTAL.io.C
    clock_XTAL.io.E := true.B // Always enabled
    attach(clk_p, clock_XTAL.io.XP)
    attach(clk_n, clock_XTAL.io.XN)

    // Main reset
    val rst_gpio = Module(new GPIO_24_A)
    reset := !PinToGPIO_24_A.asInput(rst_gpio.io)
    attach(rst_n, rst_gpio.io.PAD)

    // The platform module
    val system = Module(new TEEHWPlatform)

    // GPIOs
    (gpio zip system.io.pins.gpio.pins).foreach {
      case (g, i) =>
        val GPIO = Module(new GPIO_24_A)
        attach(g, GPIO.io.PAD)
        PinToGPIO_24_A(GPIO.io, i)
    }

    // JTAG
    val JTAG_TMS = Module(new GPIO_24_A)
    PinToGPIO_24_A(JTAG_TMS.io, system.io.pins.jtag.TMS)
    attach(jtag_tck, JTAG_TMS.io.PAD)
    val JTAG_TCK = Module(new GPIO_24_A)
    PinToGPIO_24_A(JTAG_TCK.io, system.io.pins.jtag.TCK)
    attach(jtag_tck, JTAG_TCK.io.PAD)
    val JTAG_TDI = Module(new GPIO_24_A)
    PinToGPIO_24_A(JTAG_TDI.io, system.io.pins.jtag.TDI)
    attach(jtag_tck, JTAG_TDI.io.PAD)
    val JTAG_TDO = Module(new GPIO_24_A)
    PinToGPIO_24_A(JTAG_TDO.io, system.io.pins.jtag.TDO)
    attach(jtag_tck, JTAG_TDO.io.PAD)
    val JTAG_RST = Module(new GPIO_24_A)
    system.io.jtag_reset := PinToGPIO_24_A.asInput(JTAG_RST.io)
    attach(jtag_rst, JTAG_RST.io.PAD)

    // QSPI (SPI as flash memory) TODO: Should be optional
    (qspi_cs zip system.io.pins.qspi.get.cs).foreach {
      case (g, i) =>
        val QSPI_CS = Module(new GPIO_24_A)
        PinToGPIO_24_A(QSPI_CS.io, i)
        attach(g, QSPI_CS.io.PAD)
    }
    (qspi_dq zip system.io.pins.qspi.get.dq).foreach {
      case (g, i) =>
        val QSPI_DQ = Module(new GPIO_24_A)
        PinToGPIO_24_A(QSPI_DQ.io, i)
        attach(g, QSPI_DQ.io.PAD)
    }
    val QSPI_SCK = Module(new GPIO_24_A)
    PinToGPIO_24_A(QSPI_SCK.io, system.io.pins.qspi.get.sck)
    attach(qspi_sck, QSPI_SCK.io.PAD)

    // SPI (SPI as SD?)
    (spi_cs zip system.io.pins.spi.cs).foreach {
      case (g, i) =>
        val SPI_CS = Module(new GPIO_24_A)
        PinToGPIO_24_A(SPI_CS.io, i)
        attach(g, SPI_CS.io.PAD)
    }
    (spi_dq zip system.io.pins.spi.dq).foreach {
      case (g, i) =>
        val SPI_DQ = Module(new GPIO_24_A)
        PinToGPIO_24_A(SPI_DQ.io, i)
        attach(g, SPI_DQ.io.PAD)
    }
    val SPI_SCK = Module(new GPIO_24_A)
    PinToGPIO_24_A(SPI_SCK.io, system.io.pins.spi.sck)
    attach(spi_sck, SPI_SCK.io.PAD)

    // UART
    val UART_RXD = Module(new GPIO_24_A)
    PinToGPIO_24_A(UART_RXD.io, system.io.pins.uart.rxd)
    attach(uart_rxd, UART_RXD.io.PAD)
    val UART_TXD = Module(new GPIO_24_A)
    PinToGPIO_24_A(UART_TXD.io, system.io.pins.uart.txd)
    attach(uart_txd, UART_TXD.io.PAD)

    // I2C
    //val I2C_SDA = Module(new GPIO_24_A)
    //PinToGPIO_24_A(I2C_SDA.io, system.io.pins.i2c.sda)
    //attach(i2c_sda, I2C_SDA.io.PAD)
    //val I2C_SCL = Module(new GPIO_24_A)
    //PinToGPIO_24_A(I2C_SCL.io, system.io.pins.i2c.scl)
    //attach(i2c_scl, I2C_SCL.io.PAD)

    // The memory port
    // TODO: This is awfully dirty. I mean it should get the work done
    //system.io.tlport.getElements.head
    tlport = Some(IO(new TLUL(system.sys.outer.memTLNode.in.head._1.params)))
    tlport.get <> system.io.tlport
  }
}

// **********************************************************************
// **TEEHW chip - for doing the only-input/output chip
// **********************************************************************

class TEEHWbase(implicit val p :Parameters) extends RawModule {
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
  val qspi = if(p(PeripherySPIFlashKey).nonEmpty)
    Some(IO(new Bundle {
      val qspi_cs = (Output(UInt(p(PeripherySPIFlashKey).head.csWidth.W)))
      val qspi_sck = (Output(Bool()))
      val qspi_miso = (Input(Bool()))
      val qspi_mosi = (Output(Bool()))
      val qspi_wp = (Output(Bool()))
      val qspi_hold = (Output(Bool()))
    }))
  else None
  val uart_txd = IO(Output(Bool()))
  val uart_rxd = IO(Input(Bool()))
  val usb11hs = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(new USB11HSPortIO)) else None
  val ChildClock = if(p(DDRPortOther)) Some(IO(Input(Clock()))) else None
  val ChildReset = if(p(DDRPortOther)) Some(IO(Input(Bool()))) else None
  val pciePorts =
    if(p(IncludePCIe)) Some(IO(new XilinxVC707PCIeX1IO))
    else None
  // These are later connected
  val clock = Wire(Clock())
  val reset = Wire(Bool()) // System reset (for cores)
  val areset = Wire(Bool()) // Global reset (a BUFFd version of the reset from the button)
  val ndreset = Wire(Bool()) // Debug reset (The reset you can trigger from JTAG)
  // An option to dynamically assign
  var tlportw : Option[TLUL] = None
  var cacheBlockBytesOpt: Option[Int] = None
  var memdevice: Option[MemoryDevice] = None

  // All the modules declared here have this clock and reset
  withClockAndReset(clock, reset) {
    // The platform module
    val system = Module(new TEEHWPlatform)
    ndreset := system.io.ndreset
    cacheBlockBytesOpt = Some(system.sys.outer.mbus.blockBytes)

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
    system.io.jtag_reset := areset

    // QSPI (SPI as flash memory)
    if(p(PeripherySPIFlashKey).nonEmpty) {
      qspi.get.qspi_cs := BasePinToRegular(system.io.pins.qspi.get.cs)
      qspi.get.qspi_sck := BasePinToRegular(system.io.pins.qspi.get.sck)
      qspi.get.qspi_mosi := BasePinToRegular(system.io.pins.qspi.get.dq(0))
      BasePinToRegular(system.io.pins.qspi.get.dq(1), qspi.get.qspi_miso)
      qspi.get.qspi_wp := BasePinToRegular(system.io.pins.qspi.get.dq(2))
      qspi.get.qspi_hold := BasePinToRegular(system.io.pins.qspi.get.dq(3))
    }

    // SPI (SPI as SD?)
    sdio.sdio_dat_3 := BasePinToRegular(system.io.pins.spi.cs.head)
    sdio.sdio_clk := BasePinToRegular(system.io.pins.spi.sck)
    sdio.sdio_cmd := BasePinToRegular(system.io.pins.spi.dq(0))
    BasePinToRegular(system.io.pins.spi.dq(1), sdio.sdio_dat_0)
    BasePinToRegular(system.io.pins.spi.dq(2)) // Ignored
    BasePinToRegular(system.io.pins.spi.dq(3)) // Ignored

    // UART
    BasePinToRegular(system.io.pins.uart.rxd, uart_rxd)
    uart_txd := BasePinToRegular(system.io.pins.uart.txd)

    // USB11
    if(p(PeripheryUSB11HSKey).nonEmpty) usb11hs.get <> system.io.usb11hs.get

    // The memory port
    tlportw = Some(system.io.tlport)
    memdevice = Some(system.sys.outer.memdevice)
    if(p(DDRPortOther)) {
      system.io.ChildClock.get := ChildClock.get
      system.io.ChildReset.get := ChildReset.get
    }

    // PCIe port (if available)
    if(p(IncludePCIe)) {
      pciePorts.get <> system.io.pciePorts.get
    }
  }
  val cacheBlockBytes = cacheBlockBytesOpt.get
}

class TEEHWSoC(implicit override val p :Parameters) extends TEEHWbase {
  // Some additional ports to connect to the chip
  val sys_clk = IO(Input(Clock()))
  val rst_n = IO(Input(Bool()))
  val jrst_n = IO(Input(Bool()))
  val tlport = IO(new TLUL(tlportw.get.params))
  // TL port connection
  tlport.a <> tlportw.get.a
  tlportw.get.d <> tlport.d
  // Clock and reset connection
  clock := sys_clk
  reset := !rst_n || ndreset
  areset := !jrst_n
}

// ********************************************************************
// FPGAVC707 - Demo on VC707 FPGA board
// ********************************************************************
import sifive.fpgashells.ip.xilinx.vc707mig._
import sifive.fpgashells.ip.xilinx._

class FPGAVC707(implicit val p :Parameters) extends RawModule {
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
  val uart_rtsn = IO(Output(Bool()))
  val uart_ctsn = IO(Input(Bool()))

  val USBFullSpeed = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // D12 / LA05_N / J1_23
  val USBWireDataIn = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Input(Bits(2.W)))) else None // H7 / LA02_P / J1_9 // H8 / LA02_N / J1_11
  val USBWireCtrlOut = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // D11 / LA05_P / J1_21
  val USBWireDataOut = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bits(2.W)))) else None // G9 / LA03_P / J1_13 // G10 / LA03_N / J1_15
  
  // Removed by Thuc
  //val USBWireDataOutTick = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // H10 / LA04_P / J1_17
  //val USBWireDataInTick = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // H11 / LA04_N / J1_19
  //val USBDPlusPullup = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // C10 / LA06_P / J1_25
  //val USBDMinusPullup = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // C11 / LA06_N / J1_27
  //val vBusDetect = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Input(Bool()))) else None // H13 / LA07_P / J1_29

  var ddr: Option[VC707MIGIODDR] = None
  val sys_clock_p = IO(Input(Clock()))
  val sys_clock_n = IO(Input(Clock()))
  val sys_clock_ibufds = Module(new IBUFDS())
  val sys_clk_i = IBUFG(sys_clock_ibufds.io.O)
  sys_clock_ibufds.io.I := sys_clock_p
  sys_clock_ibufds.io.IB := sys_clock_n
  val rst_0 = IO(Input(Bool()))
  val rst_1 = IO(Input(Bool()))
  val rst_2 = IO(Input(Bool()))
  val rst_3 = IO(Input(Bool()))
  val reset_0 = IBUF(rst_0)
  val reset_1 = IBUF(rst_1)
  val reset_2 = IBUF(rst_2)
  val reset_3 = IBUF(rst_3)

  val clock = Wire(Clock())
  val reset = Wire(Bool())

  val pciePorts =
    if(p(IncludePCIe)) Some(IO(new XilinxVC707PCIeX1Pads))
    else None

  withClockAndReset(clock, reset) {
    // Instance our converter, and connect everything
    val chip = Module(new TEEHWSoC)
    val mod = Module(LazyModule(new TLULtoMIG(chip.cacheBlockBytes, chip.tlportw.get.params)).module)

    // DDR port only
    ddr = Some(IO(new VC707MIGIODDR(mod.depth)))
    ddr.get <> mod.io.ddrport
    // MIG connections, like resets and stuff
    mod.io.ddrport.sys_clk_i := sys_clk_i.asUInt()
    mod.io.ddrport.aresetn := !reset_0
    mod.io.ddrport.sys_rst := reset_1

    // TileLink Interface from platform
    mod.io.tlport.a <> chip.tlport.a
    chip.tlport.d <> mod.io.tlport.d

    // PLL instance
    val c = new PLLParameters(
      name ="pll",
      input = PLLInClockParameters(
        freqMHz = 200.0,
        feedback = true
      ),
      req = Seq(
        PLLOutClockParameters(
          freqMHz = 48.0
        ),
        PLLOutClockParameters(
          freqMHz = 50.0
        ),
        PLLOutClockParameters(
          freqMHz = p(FreqKeyMHz)
        )
      )
    )
    val pll = Module(new Series7MMCM(c))
    pll.io.clk_in1 := sys_clk_i
    pll.io.reset := reset_0

    // Main clock and reset assignments
    clock := pll.io.clk_out3.get
    reset := reset_2
    chip.sys_clk := pll.io.clk_out3.get
    chip.rst_n := !reset_2

    // The rest of the platform connections
    gpio_out := Cat(reset_0, reset_1, reset_2, reset_3, mod.io.ddrport.init_calib_complete)
    chip.gpio_in := gpio_in
    jtag <> chip.jtag
    chip.jtag.jtag_TCK := IBUFG(jtag.jtag_TCK.asClock).asUInt
    chip.uart_rxd := uart_rxd	  // UART_TXD
    uart_txd := chip.uart_txd 	// UART_RXD
    sdio <> chip.sdio
    chip.jrst_n := !reset_3

    // USB phy connections
    if(p(PeripheryUSB11HSKey).nonEmpty){
      USBFullSpeed.get := chip.usb11hs.get.USBFullSpeed
      chip.usb11hs.get.USBWireDataIn := USBWireDataIn.get
      USBWireCtrlOut.get := chip.usb11hs.get.USBWireCtrlOut
      USBWireDataOut.get := chip.usb11hs.get.USBWireDataOut
      
      chip.usb11hs.get.usbClk := pll.io.clk_out1.getOrElse(false.B)
      
      // Deleted by Thuc
      //USBWireDataOutTick.get := chip.usb11hs.get.USBWireDataOutTick
      //USBWireDataInTick.get := chip.usb11hs.get.USBWireDataInTick
      //USBDPlusPullup.get := chip.usb11hs.get.USBDPlusPullup
      //USBDMinusPullup.get := chip.usb11hs.get.USBDMinusPullup
      //chip.usb11hs.get.vBusDetect := vBusDetect.get
    }

    if(p(DDRPortOther)) {
      chip.ChildClock.get := pll.io.clk_out2.getOrElse(false.B)
      chip.ChildReset.get := reset_2
      mod.clock := pll.io.clk_out2.getOrElse(false.B)
    }
    else mod.clock := pll.io.clk_out3.get

    if(p(IncludePCIe)) {
      chip.pciePorts.get.REFCLK_rxp := pciePorts.get.REFCLK_rxp
      chip.pciePorts.get.REFCLK_rxn := pciePorts.get.REFCLK_rxn
      pciePorts.get.pci_exp_txp := chip.pciePorts.get.pci_exp_txp
      pciePorts.get.pci_exp_txn := chip.pciePorts.get.pci_exp_txn
      chip.pciePorts.get.pci_exp_rxp := pciePorts.get.pci_exp_rxp
      chip.pciePorts.get.pci_exp_rxn := pciePorts.get.pci_exp_rxn
      chip.pciePorts.get.axi_aresetn := !reset_0
      chip.pciePorts.get.axi_ctl_aresetn := !reset_0
    }
  }
}

// ********************************************************************
// FPGADE4 - Demo on DE4 FPGA board
// ********************************************************************
class FPGADE4(implicit val p :Parameters) extends RawModule {
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

  ///////// LED /////////
  val LED = IO(Output(Bits((7+1).W)))

  //////////// 7-Segment Display //////////
  /*val SEG0_D = IO(Output(Bits((6+1).W)))
  val SEG1_D = IO(Output(Bits((6+1).W)))
  val SEG0_DP = IO(Output(Bool()))
  val SEG1_DP = IO(Output(Bool()))*/

  ///////// BUTTON /////////
  val BUTTON = IO(Input(Bits((3+1).W)))

  ///////// SW /////////
  val SW = IO(Input(Bits((7+1).W)))

  //////////// SLIDE SWITCH x 4 //////////
  val SLIDE_SW = IO(Input(Bits((3+1).W)))

  ///////// FAN /////////
  val FAN_CTRL = IO(Output(Bool()))

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

  //////////// DDR2 SODIMM //////////
  val M1_DDR2_addr = IO(Output(Bits((15+1).W)))
  val M1_DDR2_ba = IO(Output(Bits((2+1).W)))
  val M1_DDR2_cas_n = IO(Output(Bool()))
  val M1_DDR2_cke = IO(Output(Bits((1+1).W)))
  val M1_DDR2_clk = IO(Output(Bits((1+1).W)))
  val M1_DDR2_clk_n = IO(Output(Bits((1+1).W)))
  val M1_DDR2_cs_n = IO(Output(Bits((1+1).W)))
  val M1_DDR2_dm = IO(Output(Bits((7+1).W)))
  val M1_DDR2_dq = IO(Analog((63+1).W))
  val M1_DDR2_dqs = IO(Analog((7+1).W))
  val M1_DDR2_dqsn = IO(Analog((7+1).W))
  val M1_DDR2_odt = IO(Output(Bits((1+1).W)))
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

  ///////// GPIO /////////
  //val GPIO0_D = IO(Output(Bits((35+1).W)))
  //val GPIO1_D = IO(Analog((35+1).W))
  val jtag = IO(new Bundle {
    val jtag_TDI = (Input(Bool()))
    val jtag_TMS = (Input(Bool()))
    val jtag_TCK = (Input(Bool()))
    val jtag_TDO = (Output(Bool()))
  })
  val qspi = if(p(PeripherySPIFlashKey).nonEmpty)
    Some(IO(new Bundle {
      val qspi_cs = (Output(UInt(p(PeripherySPIFlashKey).head.csWidth.W)))
      val qspi_sck = (Output(Bool()))
      val qspi_miso = (Input(Bool()))
      val qspi_mosi = (Output(Bool()))
      val qspi_wp = (Output(Bool()))
      val qspi_hold = (Output(Bool()))
    }))
    else None
  val USBFullSpeed = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // GPIO0_D[7]
  val USBWireDataIn = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Input(Bits(2.W)))) else None // GPIO0_D[1:0]
  val USBWireCtrlOut = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // GPIO0_D[6]
  val USBWireDataOut = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bits(2.W)))) else None // GPIO0_D[3:2]
  
  // Removed by Thuc
  //val USBWireDataOutTick = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // GPIO0_D[4]
  //val USBWireDataInTick = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // GPIO0_D[5]
  //val USBDPlusPullup = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // GPIO0_D[8]
  //val USBDMinusPullup = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // GPIO0_D[9]
  //val vBusDetect = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Input(Bool()))) else None // GPIO0_D[10]

  ///////////  EXT_IO /////////
  //val EXT_IO = IO(Analog(1.W))

  //////////// HSMC_A //////////
  /*val HSMA_CLKIN_n1 = IO(Input(Bool()))
  val HSMA_CLKIN_n2 = IO(Input(Bool()))
  val HSMA_CLKIN_p1 = IO(Input(Bool()))
  val HSMA_CLKIN_p2 = IO(Input(Bool()))
  val HSMA_CLKIN0 = IO(Input(Bool()))
  val HSMA_CLKOUT_n2 = IO(Output(Bool()))
  val HSMA_CLKOUT_p2 = IO(Output(Bool()))
  val HSMA_D = IO(Analog((3+1).W))
  //val HSMA_GXB_RX_p = IO(Input(Bits((3+1).W)))
  //val HSMA_GXB_TX_p = IO(Output(Bits((3+1).W)))
  val HSMA_OUT_n1 = IO(Analog(1.W))
  val HSMA_OUT_p1 = IO(Analog(1.W))
  val HSMA_OUT0 = IO(Analog(1.W))
  //val HSMA_REFCLK_p = IO(Input(Bool()))
  val HSMA_RX_n = IO(Analog((16+1).W))
  val HSMA_RX_p = IO(Analog((16+1).W))
  val HSMA_TX_n = IO(Analog((16+1).W))
  val HSMA_TX_p = IO(Analog((16+1).W))*/

  //////////// HSMC_B //////////
  /*val HSMB_CLKIN_n1 = IO(Input(Bool()))
  val HSMB_CLKIN_n2 = IO(Input(Bool()))
  val HSMB_CLKIN_p1 = IO(Input(Bool()))
  val HSMB_CLKIN_p2 = IO(Input(Bool()))
  val HSMB_CLKIN0 = IO(Input(Bool()))
  val HSMB_CLKOUT_n2 = IO(Output(Bool()))
  val HSMB_CLKOUT_p2 = IO(Output(Bool()))
  val HSMB_D = IO(Analog((3+1).W))
  //val HSMB_GXB_RX_p = IO(Input(Bits((7+1).W)))
  //val HSMB_GXB_TX_p = IO(Output(Bits((7+1).W)))
  val HSMB_OUT_n1 = IO(Analog(1.W))
  val HSMB_OUT_p1 = IO(Analog(1.W))
  val HSMB_OUT0 = IO(Analog(1.W))
  //val HSMB_REFCLK_p = IO(Input(Bool()))
  val HSMB_RX_n = IO(Analog((16+1).W))
  val HSMB_RX_p = IO(Analog((16+1).W))
  val HSMB_TX_n = IO(Analog((16+1).W))
  val HSMB_TX_p = IO(Analog((16+1).W))*/

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

  FAN_CTRL := true.B

  val clock = Wire(Clock())
  val reset = Wire(Bool())

  withClockAndReset(clock, reset) {
    // Instance our converter, and connect everything
    val chip = Module(new TEEHWSoC)
    val mod = Module(LazyModule(
      new TLULtoQuartusPlatform(
        chip.cacheBlockBytes,
        chip.tlportw.get.params
      )
    ).module)

    // Clock and reset (for TL stuff)
    clock := mod.io.ckrst.dimmclk_clk
    reset := SLIDE_SW(3)
    chip.sys_clk := mod.io.ckrst.dimmclk_clk
    chip.rst_n := !SLIDE_SW(3)
    if(p(DDRPortOther)) {
      chip.ChildClock.get := mod.io.ckrst.ext_clk_clk
      chip.ChildReset.get := SLIDE_SW(3)
      mod.clock := mod.io.ckrst.ext_clk_clk
    }
    else mod.clock := mod.io.ckrst.dimmclk_clk

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
    mod.io.ckrst.refclk_sys_clk := OSC_50_BANK3.asUInt()
    mod.io.ckrst.refclk_usb_clk := OSC_50_BANK5.asUInt()
    mod.io.ckrst.reset_sys_reset_n := CPU_RESET_n
    mod.io.ckrst.reset_usb_reset_n := CPU_RESET_n

    // TileLink Interface from platform
    mod.io.tlport.a <> chip.tlport.a
    chip.tlport.d <> mod.io.tlport.d

    // The rest of the platform connections
    val chipshell_led = chip.gpio_out 	// output [7:0]
    LED := Cat(
      mod.io.qport.mem_status_local_cal_fail,
      mod.io.qport.mem_status_local_cal_success,
      mod.io.qport.mem_status_local_init_done,
      SLIDE_SW(3),
      chipshell_led(3,0)
    )
    chip.gpio_in := SW(7,0)			// input  [7:0]
    jtag <> chip.jtag
    if(p(PeripherySPIFlashKey).nonEmpty) qspi.get <> chip.qspi.get
    chip.uart_rxd := UART_RXD	// UART_TXD
    UART_TXD := chip.uart_txd 	// UART_RXD
    SD_CLK := chip.sdio.sdio_clk 	// output
    SD_MOSI := chip.sdio.sdio_cmd 	// output
    chip.sdio.sdio_dat_0 := SD_MISO 	// input
    SD_CS_N := chip.sdio.sdio_dat_3 	// output
    chip.jrst_n := !SLIDE_SW(2)

    // USB phy connections
    if(p(PeripheryUSB11HSKey).nonEmpty) {
      USBFullSpeed.get := chip.usb11hs.get.USBFullSpeed
      chip.usb11hs.get.USBWireDataIn := USBWireDataIn.get
      USBWireCtrlOut.get := chip.usb11hs.get.USBWireCtrlOut
      USBWireDataOut.get := chip.usb11hs.get.USBWireDataOut

      chip.usb11hs.get.usbClk := mod.io.ckrst.usb_clk_clk
      
      // Removed by Thuc
      //USBWireDataOutTick.get := chip.usb11hs.get.USBWireDataOutTick
      //USBWireDataInTick.get := chip.usb11hs.get.USBWireDataInTick
      //USBDPlusPullup.get := chip.usb11hs.get.USBDPlusPullup
      //USBDMinusPullup.get := chip.usb11hs.get.USBDMinusPullup
      //chip.usb11hs.get.vBusDetect := vBusDetect.get
    }
  }
}

// ********************************************************************
// FPGATR4 - Demo on TR4 FPGA board
// ********************************************************************
class FPGATR4(implicit val p :Parameters) extends RawModule {
  ///////// CLOCKS /////////
  val OSC_50_BANK1 = IO(Input(Clock()))
  val OSC_50_BANK3 = IO(Input(Clock()))
  val OSC_50_BANK4 = IO(Input(Clock()))
  val OSC_50_BANK7 = IO(Input(Clock()))
  val OSC_50_BANK8 = IO(Input(Clock()))

  ///////// BUTTON /////////
  val BUTTON = IO(Input(Bits((3+1).W)))

  ///////// LED /////////
  val LED = IO(Output(Bits((3+1).W)))

  ///////// SW /////////
  val SW = IO(Input(Bits((3+1).W)))

  ///////// FAN /////////
  val FAN_CTRL = IO(Output(Bool()))

  //////////// HSMC_A //////////
  /*val HSMA_CLKIN0 = IO(Input(Bool()))
  val HSMA_CLKIN_n1 = IO(Input(Bool()))
  val HSMA_CLKIN_n2 = IO(Input(Bool()))
  val HSMA_CLKIN_p1 = IO(Input(Bool()))
  val HSMA_CLKIN_p2 = IO(Input(Bool()))
  val HSMA_D = IO(Analog((3+1).W))
  val HSMA_OUT0 = IO(Analog(1.W))
  val HSMA_OUT_n1 = IO(Analog(1.W))
  val HSMA_OUT_n2 = IO(Analog(1.W))
  val HSMA_OUT_p1 = IO(Analog(1.W))
  val HSMA_OUT_p2 = IO(Analog(1.W))
  val HSMA_RX_n = IO(Analog((16+1).W))
  val HSMA_RX_p = IO(Analog((16+1).W))
  val HSMA_TX_n = IO(Analog((16+1).W))
  val HSMA_TX_p = IO(Analog((16+1).W))*/

  //////////// HSMC_B //////////
  /*val HSMB_CLKIN0 = IO(Input(Bool()))
  val HSMB_CLKIN_n1 = IO(Input(Bool()))
  val HSMB_CLKIN_n2 = IO(Input(Bool()))
  val HSMB_CLKIN_p1 = IO(Input(Bool()))
  val HSMB_CLKIN_p2 = IO(Input(Bool()))
  val HSMB_D = IO(Analog((3+1).W))
  val HSMB_OUT0 = IO(Analog(1.W))
  val HSMB_OUT_n1 = IO(Analog(1.W))
  val HSMB_OUT_n2 = IO(Analog(1.W))
  val HSMB_OUT_p1 = IO(Analog(1.W))
  val HSMB_OUT_p2 = IO(Analog(1.W))
  val HSMB_RX_n = IO(Analog((16+1).W))
  val HSMB_RX_p = IO(Analog((16+1).W))
  val HSMB_TX_n = IO(Analog((16+1).W))
  val HSMB_TX_p = IO(Analog((16+1).W))*/

  //////////// HSMC_C //////////
  //val GPIO0_D = IO(Output(Bits((35+1).W)))
  //val GPIO1_D = IO(Analog((35+1).W))

  //////////// HSMC_D //////////
  /*val HSMD_CLKIN0 = IO(Input(Bool()))
  val HSMD_CLKIN_n1 = IO(Input(Bool()))
  val HSMD_CLKIN_n2 = IO(Input(Bool()))
  val HSMD_CLKIN_p1 = IO(Input(Bool()))
  val HSMD_CLKIN_p2 = IO(Input(Bool()))
  val HSMD_CLKOUT_n1 = IO(Analog(1.W))
  val HSMD_CLKOUT_p1 = IO(Analog(1.W))
  val HSMD_D = IO(Analog((3+1).W))
  val HSMD_OUT0 = IO(Analog(1.W))
  val HSMD_OUT_n2 = IO(Analog(1.W))
  val HSMD_OUT_p2 = IO(Analog(1.W))
  val HSMD_RX_n = IO(Analog((16+1).W))
  val HSMD_RX_p = IO(Analog((16+1).W))
  val HSMD_TX_n = IO(Analog((16+1).W))
  val HSMD_TX_p = IO(Analog((16+1).W))*/

  //////////// HSMC_E //////////
  /*val HSME_CLKIN0 = IO(Input(Bool()))
  val HSME_CLKIN_n1 = IO(Input(Bool()))
  val HSME_CLKIN_n2 = IO(Input(Bool()))
  val HSME_CLKIN_p1 = IO(Input(Bool()))
  val HSME_CLKIN_p2 = IO(Input(Bool()))
  val HSME_CLKOUT_n1 = IO(Analog(1.W))
  val HSME_CLKOUT_p1 = IO(Analog(1.W))
  val HSME_D = IO(Analog((3+1).W))
  val HSME_OUT0 = IO(Analog(1.W))
  val HSME_OUT_n2 = IO(Analog(1.W))
  val HSME_OUT_p2 = IO(Analog(1.W))
  val HSME_RX_n = IO(Analog((16+1).W))
  val HSME_RX_p = IO(Analog((16+1).W))
  val HSME_TX_n = IO(Analog((16+1).W))
  val HSME_TX_p = IO(Analog((16+1).W))*/

  //////////// HSMC_F //////////
  /*val HSMF_CLKIN0 = IO(Input(Bool()))
  val HSMF_CLKIN_n1 = IO(Input(Bool()))
  val HSMF_CLKIN_n2 = IO(Input(Bool()))
  val HSMF_CLKIN_p1 = IO(Input(Bool()))
  val HSMF_CLKIN_p2 = IO(Input(Bool()))
  val HSMF_CLKOUT_n1 = IO(Analog(1.W))
  val HSMF_CLKOUT_p1 = IO(Analog(1.W))
  val HSMF_D = IO(Analog((3+1).W))
  val HSMF_OUT0 = IO(Analog(1.W))
  val HSMF_OUT_n2 = IO(Analog(1.W))
  val HSMF_OUT_p2 = IO(Analog(1.W))
  val HSMF_RX_n = IO(Analog((16+1).W))
  val HSMF_RX_p = IO(Analog((16+1).W))
  val HSMF_TX_n = IO(Analog((16+1).W))
  val HSMF_TX_p = IO(Analog((16+1).W))*/

  //////////// mem //////////
  val mem_a = IO(Output(Bits((15+1).W)))
  val mem_ba = IO(Output(Bits((2+1).W)))
  val mem_cas_n = IO(Output(Bool()))
  val mem_cke = IO(Output(Bits((1+1).W)))
  val mem_ck = IO(Output(Bits((0+1).W))) // NOTE: Is impossible to do [0:0]
  val mem_ck_n = IO(Output(Bits((0+1).W))) // NOTE: Is impossible to do [0:0]
  val mem_cs_n = IO(Output(Bits((1+1).W)))
  val mem_dm = IO(Output(Bits((7+1).W)))
  val mem_dq = IO(Analog((63+1).W))
  val mem_dqs = IO(Analog((7+1).W))
  val mem_dqs_n = IO(Analog((7+1).W))
  val mem_odt = IO(Output(Bits((1+1).W)))
  val mem_ras_n = IO(Output(Bool()))
  val mem_reset_n = IO(Output(Bool()))
  val mem_we_n = IO(Output(Bool()))
  val mem_oct_rdn = IO(Input(Bool()))
  val mem_oct_rup = IO(Input(Bool()))
  //val mem_scl = IO(Output(Bool()))
  //val mem_sda = IO(Analog(1.W))
  //val mem_event_n = IO(Input(Bool())) // NOTE: This also appeared, but is not used

  ///////// GPIO /////////
  val jtag = IO(new Bundle {
    val jtag_TDI = (Input(Bool()))
    val jtag_TMS = (Input(Bool()))
    val jtag_TCK = (Input(Bool()))
    val jtag_TDO = (Output(Bool()))
  })
  val sdio = IO(new Bundle {
    val sdio_clk = (Output(Bool()))
    val sdio_cmd = (Output(Bool()))
    val sdio_dat_0 = (Input(Bool()))
    val sdio_dat_1 = (Analog(1.W))
    val sdio_dat_2 = (Analog(1.W))
    val sdio_dat_3 = (Output(Bool()))
  })
  val qspi = if(p(PeripherySPIFlashKey).nonEmpty)
    Some(IO(new Bundle {
      val qspi_cs = (Output(UInt(p(PeripherySPIFlashKey).head.csWidth.W)))
      val qspi_sck = (Output(Bool()))
      val qspi_miso = (Input(Bool()))
      val qspi_mosi = (Output(Bool()))
      val qspi_wp = (Output(Bool()))
      val qspi_hold = (Output(Bool()))
    }))
  else None
  val USBFullSpeed = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // HSMC_TX_p[10] / PIN_AW27 / GPIO1_D17 GPIO1[20]
  val USBWireDataIn = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Input(Bits(2.W)))) else None // HSMC_TX_p[7] HSMC_TX_n[7] / PIN_AB30 PIN_AB31 / GPIO1_D24 GPIO1_D26 GPIO1[27,31]
  val USBWireCtrlOut = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // HSMC_TX_n[10] / PIN_AW28 / GPIO1_D19 GPIO[22]
  val USBWireDataOut = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bits(2.W)))) else None // HSMC_TX_p[8] HSMC_TX_n[8] / PIN_AL27 PIN_AH26 / GPIO1_D16 GPIO1_D18 GPIO1[19,21]
  
  // Removed by Thuc
  //val USBWireDataOutTick = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // HSMC_TX_n[9] / PIN_AE24 / GPIO1_D22 GPIO1[25]
  //val USBWireDataInTick = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // HSMC_TX_p[9] / PIN_AK27 / GPIO1_D20 GPIO1[23]
  //val USBDPlusPullup = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // HSMC_TX_n[11] / PIN_AG24 / GPIO1_D15 GPIO1[18]
  //val USBDMinusPullup = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Output(Bool()))) else None // HSMC_TX_p[11] / PIN_AH24 / GPIO1_D13 GPIO1[16]
  //val vBusDetect = if(p(PeripheryUSB11HSKey).nonEmpty) Some(IO(Input(Bool()))) else None // HSMC_TX_n[12] / PIN_AV31 / GPIO1_D14 GPIO1[17]

  //////////// Uart //////////
  val UART_TXD = IO(Output(Bool()))
  val UART_RXD = IO(Input(Bool()))

  FAN_CTRL := true.B

  val clock = Wire(Clock())
  val reset = Wire(Bool())

  withClockAndReset(clock, reset) {
    // Instance our converter, and connect everything
    val chip = Module(new TEEHWSoC)
    val mod = Module(LazyModule(
      new TLULtoQuartusPlatform(
        chip.cacheBlockBytes,
        chip.tlportw.get.params,
        QuartusDDRConfig(size_ck = 1, is_reset = true)
      )
    ).module)

    // Clock and reset (for TL stuff)
    clock := mod.io.ckrst.dimmclk_clk
    reset := SW(1)
    chip.sys_clk := mod.io.ckrst.dimmclk_clk
    chip.rst_n := !SW(2)
    if(p(DDRPortOther)) {
      chip.ChildClock.get := mod.io.ckrst.ext_clk_clk
      chip.ChildReset.get := SW(3)
      mod.clock := mod.io.ckrst.ext_clk_clk
    }
    else mod.clock := mod.io.ckrst.dimmclk_clk

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
    mod.io.ckrst.refclk_sys_clk := OSC_50_BANK1.asUInt()
    mod.io.ckrst.refclk_usb_clk := OSC_50_BANK4.asUInt() // TODO: This is okay?
    mod.io.ckrst.reset_sys_reset_n := BUTTON(2)
    mod.io.ckrst.reset_usb_reset_n := BUTTON(3)

    // TileLink Interface from platform
    mod.io.tlport.a <> chip.tlport.a
    chip.tlport.d <> mod.io.tlport.d

    // The rest of the platform connections
    val chipshell_led = chip.gpio_out 	// TODO: Not used! LED [3:0]
    LED := Cat(
      mod.io.qport.mem_status_local_cal_fail,
      mod.io.qport.mem_status_local_cal_success,
      mod.io.qport.mem_status_local_init_done,
      SW(3)
    )
    chip.gpio_in := Cat(BUTTON(3), BUTTON(1,0), SW(1,0))
    jtag <> chip.jtag
    if(p(PeripherySPIFlashKey).nonEmpty) qspi.get <> chip.qspi.get
    chip.uart_rxd := UART_RXD	// UART_TXD
    UART_TXD := chip.uart_txd 	// UART_RXD
    sdio <> chip.sdio
    chip.jrst_n := !SW(0)

    // USB phy connections
    if(p(PeripheryUSB11HSKey).nonEmpty) {
      USBFullSpeed.get := chip.usb11hs.get.USBFullSpeed
      chip.usb11hs.get.USBWireDataIn := USBWireDataIn.get
      USBWireCtrlOut.get := chip.usb11hs.get.USBWireCtrlOut
      USBWireDataOut.get := chip.usb11hs.get.USBWireDataOut

      chip.usb11hs.get.usbClk := mod.io.ckrst.usb_clk_clk
      
      //USBWireDataOutTick.get := chip.usb11hs.get.USBWireDataOutTick
      //USBWireDataInTick.get := chip.usb11hs.get.USBWireDataInTick
      //USBDPlusPullup.get := chip.usb11hs.get.USBDPlusPullup
      //USBDMinusPullup.get := chip.usb11hs.get.USBDMinusPullup
      //chip.usb11hs.get.vBusDetect := vBusDetect.get
    }
  }
}
