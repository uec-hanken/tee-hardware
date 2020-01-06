module NEDOchip( // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347783.2]
  /*16 pins gpios*/
  input  [7:0]  gpio_in, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347784.4]
  output [7:0]  gpio_out, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347785.4]
  /*5 pins jtag*/
  input         jrst_n, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347798.4]
  input         jtag_jtag_TDI, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347786.4]
  output        jtag_jtag_TDO, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347786.4]
  input         jtag_jtag_TCK, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347786.4]
  input         jtag_jtag_TMS, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347786.4]
  /*4 pins sd card*/
  output        sdio_sdio_clk, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347787.4]
  output        sdio_sdio_cmd, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347787.4]
  input         sdio_sdio_dat_0, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347787.4]
  //inout         sdio_sdio_dat_1, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347787.4]
  //inout         sdio_sdio_dat_2, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347787.4]
  output        sdio_sdio_dat_3, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347787.4]
  /*6 pins flash*/
  output        qspi_qspi_cs, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347788.4]
  output        qspi_qspi_sck, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347788.4]
  input         qspi_qspi_miso, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347788.4]
  output        qspi_qspi_mosi, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347788.4]
  output        qspi_qspi_wp, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347788.4]
  output        qspi_qspi_hold, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347788.4]
  /*2 pins uart*/
  output        uart_txd, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347789.4]
  input         uart_rxd, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347790.4]
  //output        uart_rtsn, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347791.4]
  //input         uart_ctsn, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347792.4]
  /*12 pins usb*/
  input         usb11hs_usbClk, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347793.4]
  input  [1:0]  usb11hs_USBWireDataIn, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347793.4]
  output [1:0]  usb11hs_USBWireDataOut, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347793.4]
  output        usb11hs_USBWireDataOutTick, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347793.4]
  output        usb11hs_USBWireDataInTick, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347793.4]
  output        usb11hs_USBWireCtrlOut, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347793.4]
  output        usb11hs_USBFullSpeed, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347793.4]
  output        usb11hs_USBDPlusPullup, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347793.4]
  output        usb11hs_USBDMinusPullup, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347793.4]
  input         usb11hs_vBusDetect, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347793.4]
  /*4 pins clk & reset*/
  input         ChildClock, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347794.4]
  input         ChildReset, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347795.4]
  input         sys_clk, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347796.4]
  input         rst_n, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347797.4]
  /*82 pins tlport a*/
  input         tlport_a_ready, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  output        tlport_a_valid, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  output [2:0]  tlport_a_bits_opcode, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  output [2:0]  tlport_a_bits_param, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  output [2:0]  tlport_a_bits_size, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  output [1:0]  tlport_a_bits_source, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  output [31:0] tlport_a_bits_address, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  output [3:0]  tlport_a_bits_mask, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  output [31:0] tlport_a_bits_data, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  output        tlport_a_bits_corrupt, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  /*47 pins tlport d*/
  output        tlport_d_ready, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  input         tlport_d_valid, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  input  [2:0]  tlport_d_bits_opcode, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  input  [1:0]  tlport_d_bits_param, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  input  [2:0]  tlport_d_bits_size, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  input  [1:0]  tlport_d_bits_source, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  input         tlport_d_bits_sink, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  input         tlport_d_bits_denied, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  input  [31:0] tlport_d_bits_data, // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
  input         tlport_d_bits_corrupt // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347799.4]
);
  wire  NEDOPlatform_clock; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_reset; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_jtag_TCK_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_jtag_TMS_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_jtag_TDI_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_jtag_TDO_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_0_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_1_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_2_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_3_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_4_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_5_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_6_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_7_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_8_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_9_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_10_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_11_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_12_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_13_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_14_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_gpio_pins_15_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_qspi_sck_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_qspi_dq_0_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_qspi_dq_1_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_qspi_dq_2_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_qspi_dq_3_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_qspi_cs_0_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_uart_rxd_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_uart_txd_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_spi_sck_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_spi_dq_0_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_spi_dq_1_i_ival; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_pins_spi_cs_0_o_oval; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_usb11hs_usbClk; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [1:0] NEDOPlatform_io_usb11hs_USBWireDataIn; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [1:0] NEDOPlatform_io_usb11hs_USBWireDataOut; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_usb11hs_USBWireDataOutTick; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_usb11hs_USBWireDataInTick; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_usb11hs_USBWireCtrlOut; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_usb11hs_USBFullSpeed; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_usb11hs_USBDPlusPullup; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_usb11hs_USBDMinusPullup; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_usb11hs_vBusDetect; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_jtag_reset; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_ndreset; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_tlport_a_ready; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_tlport_a_valid; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [2:0] NEDOPlatform_io_tlport_a_bits_opcode; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [2:0] NEDOPlatform_io_tlport_a_bits_param; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [2:0] NEDOPlatform_io_tlport_a_bits_size; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [1:0] NEDOPlatform_io_tlport_a_bits_source; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [31:0] NEDOPlatform_io_tlport_a_bits_address; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [3:0] NEDOPlatform_io_tlport_a_bits_mask; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [31:0] NEDOPlatform_io_tlport_a_bits_data; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_tlport_a_bits_corrupt; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_tlport_d_ready; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_tlport_d_valid; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [2:0] NEDOPlatform_io_tlport_d_bits_opcode; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [1:0] NEDOPlatform_io_tlport_d_bits_param; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [2:0] NEDOPlatform_io_tlport_d_bits_size; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [1:0] NEDOPlatform_io_tlport_d_bits_source; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_tlport_d_bits_sink; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_tlport_d_bits_denied; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire [31:0] NEDOPlatform_io_tlport_d_bits_data; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_tlport_d_bits_corrupt; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_ChildClock; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  NEDOPlatform_io_ChildReset; // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
  wire  _T_9_1; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347846.4]
  wire  _T_9_0; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347844.4]
  wire  _T_9_3; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347850.4]
  wire  _T_9_2; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347848.4]
  wire [3:0] _T_12; // @[NEDOwrapper.scala 206:33:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347829.4]
  wire  _T_9_5; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347854.4]
  wire  _T_9_4; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347852.4]
  wire  _T_9_7; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347858.4]
  wire  _T_9_6; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347856.4]
  wire [3:0] _T_15; // @[NEDOwrapper.scala 206:33:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347832.4]
  wire  _T_18; // @[NEDOwrapper.scala 266:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347910.4]
  wire  ndreset; // @[NEDOwrapper.scala 190:21:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347804.4 NEDOwrapper.scala 200:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347808.4]
  NEDOPlatform_inNEDOchip NEDOPlatform ( // @[NEDOwrapper.scala 199:24:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347805.4]
    .clock(NEDOPlatform_clock),
    .reset(NEDOPlatform_reset),
    .io_pins_jtag_TCK_i_ival(NEDOPlatform_io_pins_jtag_TCK_i_ival),
    .io_pins_jtag_TMS_i_ival(NEDOPlatform_io_pins_jtag_TMS_i_ival),
    .io_pins_jtag_TDI_i_ival(NEDOPlatform_io_pins_jtag_TDI_i_ival),
    .io_pins_jtag_TDO_o_oval(NEDOPlatform_io_pins_jtag_TDO_o_oval),
    .io_pins_gpio_pins_0_i_ival(NEDOPlatform_io_pins_gpio_pins_0_i_ival),
    .io_pins_gpio_pins_1_i_ival(NEDOPlatform_io_pins_gpio_pins_1_i_ival),
    .io_pins_gpio_pins_2_i_ival(NEDOPlatform_io_pins_gpio_pins_2_i_ival),
    .io_pins_gpio_pins_3_i_ival(NEDOPlatform_io_pins_gpio_pins_3_i_ival),
    .io_pins_gpio_pins_4_i_ival(NEDOPlatform_io_pins_gpio_pins_4_i_ival),
    .io_pins_gpio_pins_5_i_ival(NEDOPlatform_io_pins_gpio_pins_5_i_ival),
    .io_pins_gpio_pins_6_i_ival(NEDOPlatform_io_pins_gpio_pins_6_i_ival),
    .io_pins_gpio_pins_7_i_ival(NEDOPlatform_io_pins_gpio_pins_7_i_ival),
    .io_pins_gpio_pins_8_o_oval(NEDOPlatform_io_pins_gpio_pins_8_o_oval),
    .io_pins_gpio_pins_9_o_oval(NEDOPlatform_io_pins_gpio_pins_9_o_oval),
    .io_pins_gpio_pins_10_o_oval(NEDOPlatform_io_pins_gpio_pins_10_o_oval),
    .io_pins_gpio_pins_11_o_oval(NEDOPlatform_io_pins_gpio_pins_11_o_oval),
    .io_pins_gpio_pins_12_o_oval(NEDOPlatform_io_pins_gpio_pins_12_o_oval),
    .io_pins_gpio_pins_13_o_oval(NEDOPlatform_io_pins_gpio_pins_13_o_oval),
    .io_pins_gpio_pins_14_o_oval(NEDOPlatform_io_pins_gpio_pins_14_o_oval),
    .io_pins_gpio_pins_15_o_oval(NEDOPlatform_io_pins_gpio_pins_15_o_oval),
    .io_pins_qspi_sck_o_oval(NEDOPlatform_io_pins_qspi_sck_o_oval),
    .io_pins_qspi_dq_0_o_oval(NEDOPlatform_io_pins_qspi_dq_0_o_oval),
    .io_pins_qspi_dq_1_i_ival(NEDOPlatform_io_pins_qspi_dq_1_i_ival),
    .io_pins_qspi_dq_2_o_oval(NEDOPlatform_io_pins_qspi_dq_2_o_oval),
    .io_pins_qspi_dq_3_o_oval(NEDOPlatform_io_pins_qspi_dq_3_o_oval),
    .io_pins_qspi_cs_0_o_oval(NEDOPlatform_io_pins_qspi_cs_0_o_oval),
    .io_pins_uart_rxd_i_ival(NEDOPlatform_io_pins_uart_rxd_i_ival),
    .io_pins_uart_txd_o_oval(NEDOPlatform_io_pins_uart_txd_o_oval),
    .io_pins_spi_sck_o_oval(NEDOPlatform_io_pins_spi_sck_o_oval),
    .io_pins_spi_dq_0_o_oval(NEDOPlatform_io_pins_spi_dq_0_o_oval),
    .io_pins_spi_dq_1_i_ival(NEDOPlatform_io_pins_spi_dq_1_i_ival),
    .io_pins_spi_cs_0_o_oval(NEDOPlatform_io_pins_spi_cs_0_o_oval),
    .io_usb11hs_usbClk(NEDOPlatform_io_usb11hs_usbClk),
    .io_usb11hs_USBWireDataIn(NEDOPlatform_io_usb11hs_USBWireDataIn),
    .io_usb11hs_USBWireDataOut(NEDOPlatform_io_usb11hs_USBWireDataOut),
    .io_usb11hs_USBWireDataOutTick(NEDOPlatform_io_usb11hs_USBWireDataOutTick),
    .io_usb11hs_USBWireDataInTick(NEDOPlatform_io_usb11hs_USBWireDataInTick),
    .io_usb11hs_USBWireCtrlOut(NEDOPlatform_io_usb11hs_USBWireCtrlOut),
    .io_usb11hs_USBFullSpeed(NEDOPlatform_io_usb11hs_USBFullSpeed),
    .io_usb11hs_USBDPlusPullup(NEDOPlatform_io_usb11hs_USBDPlusPullup),
    .io_usb11hs_USBDMinusPullup(NEDOPlatform_io_usb11hs_USBDMinusPullup),
    .io_usb11hs_vBusDetect(NEDOPlatform_io_usb11hs_vBusDetect),
    .io_jtag_reset(NEDOPlatform_io_jtag_reset),
    .io_ndreset(NEDOPlatform_io_ndreset),
    .io_tlport_a_ready(NEDOPlatform_io_tlport_a_ready),
    .io_tlport_a_valid(NEDOPlatform_io_tlport_a_valid),
    .io_tlport_a_bits_opcode(NEDOPlatform_io_tlport_a_bits_opcode),
    .io_tlport_a_bits_param(NEDOPlatform_io_tlport_a_bits_param),
    .io_tlport_a_bits_size(NEDOPlatform_io_tlport_a_bits_size),
    .io_tlport_a_bits_source(NEDOPlatform_io_tlport_a_bits_source),
    .io_tlport_a_bits_address(NEDOPlatform_io_tlport_a_bits_address),
    .io_tlport_a_bits_mask(NEDOPlatform_io_tlport_a_bits_mask),
    .io_tlport_a_bits_data(NEDOPlatform_io_tlport_a_bits_data),
    .io_tlport_a_bits_corrupt(NEDOPlatform_io_tlport_a_bits_corrupt),
    .io_tlport_d_ready(NEDOPlatform_io_tlport_d_ready),
    .io_tlport_d_valid(NEDOPlatform_io_tlport_d_valid),
    .io_tlport_d_bits_opcode(NEDOPlatform_io_tlport_d_bits_opcode),
    .io_tlport_d_bits_param(NEDOPlatform_io_tlport_d_bits_param),
    .io_tlport_d_bits_size(NEDOPlatform_io_tlport_d_bits_size),
    .io_tlport_d_bits_source(NEDOPlatform_io_tlport_d_bits_source),
    .io_tlport_d_bits_sink(NEDOPlatform_io_tlport_d_bits_sink),
    .io_tlport_d_bits_denied(NEDOPlatform_io_tlport_d_bits_denied),
    .io_tlport_d_bits_data(NEDOPlatform_io_tlport_d_bits_data),
    .io_tlport_d_bits_corrupt(NEDOPlatform_io_tlport_d_bits_corrupt),
    .io_ChildClock(NEDOPlatform_io_ChildClock),
    .io_ChildReset(NEDOPlatform_io_ChildReset)
  );
  assign _T_9_1 = NEDOPlatform_io_pins_gpio_pins_9_o_oval; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347846.4]
  assign _T_9_0 = NEDOPlatform_io_pins_gpio_pins_8_o_oval; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347844.4]
  assign _T_9_3 = NEDOPlatform_io_pins_gpio_pins_11_o_oval; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347850.4]
  assign _T_9_2 = NEDOPlatform_io_pins_gpio_pins_10_o_oval; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347848.4]
  assign _T_12 = {_T_9_3,_T_9_2,_T_9_1,_T_9_0}; // @[NEDOwrapper.scala 206:33:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347829.4]
  assign _T_9_5 = NEDOPlatform_io_pins_gpio_pins_13_o_oval; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347854.4]
  assign _T_9_4 = NEDOPlatform_io_pins_gpio_pins_12_o_oval; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347852.4]
  assign _T_9_7 = NEDOPlatform_io_pins_gpio_pins_15_o_oval; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347858.4]
  assign _T_9_6 = NEDOPlatform_io_pins_gpio_pins_14_o_oval; // @[NEDOwrapper.scala 205:25:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347826.4 NEDOwrapper.scala 212:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347856.4]
  assign _T_15 = {_T_9_7,_T_9_6,_T_9_5,_T_9_4}; // @[NEDOwrapper.scala 206:33:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347832.4]
  assign _T_18 = rst_n == 1'h0; // @[NEDOwrapper.scala 266:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347910.4]
  assign ndreset = NEDOPlatform_io_ndreset; // @[NEDOwrapper.scala 190:21:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347804.4 NEDOwrapper.scala 200:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347808.4]
  assign gpio_out = {_T_15,_T_12}; // @[NEDOwrapper.scala 206:14:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347834.4]
  assign jtag_jtag_TDO = NEDOPlatform_io_pins_jtag_TDO_o_oval; // @[NEDOwrapper.scala 219:19:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347863.4]
  assign sdio_sdio_clk = NEDOPlatform_io_pins_spi_sck_o_oval; // @[NEDOwrapper.scala 232:19:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347881.4]
  assign sdio_sdio_cmd = NEDOPlatform_io_pins_spi_dq_0_o_oval; // @[NEDOwrapper.scala 233:19:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347883.4]
  assign sdio_sdio_dat_3 = NEDOPlatform_io_pins_spi_cs_0_o_oval; // @[NEDOwrapper.scala 231:21:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347879.4]
  assign qspi_qspi_cs = NEDOPlatform_io_pins_qspi_cs_0_o_oval; // @[NEDOwrapper.scala 223:18:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347868.4]
  assign qspi_qspi_sck = NEDOPlatform_io_pins_qspi_sck_o_oval; // @[NEDOwrapper.scala 224:19:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347870.4]
  assign qspi_qspi_mosi = NEDOPlatform_io_pins_qspi_dq_0_o_oval; // @[NEDOwrapper.scala 225:20:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347872.4]
  assign qspi_qspi_wp = NEDOPlatform_io_pins_qspi_dq_2_o_oval; // @[NEDOwrapper.scala 227:18:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347875.4]
  assign qspi_qspi_hold = NEDOPlatform_io_pins_qspi_dq_3_o_oval; // @[NEDOwrapper.scala 228:20:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347877.4]
  assign uart_txd = NEDOPlatform_io_pins_uart_txd_o_oval; // @[NEDOwrapper.scala 240:14:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347889.4]
  //assign uart_rtsn = 1'h0; // @[NEDOwrapper.scala 241:15:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347890.4]
  assign usb11hs_USBWireDataOut = NEDOPlatform_io_usb11hs_USBWireDataOut; // @[NEDOwrapper.scala 244:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347898.4]
  assign usb11hs_USBWireDataOutTick = NEDOPlatform_io_usb11hs_USBWireDataOutTick; // @[NEDOwrapper.scala 244:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347897.4]
  assign usb11hs_USBWireDataInTick = NEDOPlatform_io_usb11hs_USBWireDataInTick; // @[NEDOwrapper.scala 244:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347896.4]
  assign usb11hs_USBWireCtrlOut = NEDOPlatform_io_usb11hs_USBWireCtrlOut; // @[NEDOwrapper.scala 244:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347895.4]
  assign usb11hs_USBFullSpeed = NEDOPlatform_io_usb11hs_USBFullSpeed; // @[NEDOwrapper.scala 244:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347894.4]
  assign usb11hs_USBDPlusPullup = NEDOPlatform_io_usb11hs_USBDPlusPullup; // @[NEDOwrapper.scala 244:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347893.4]
  assign usb11hs_USBDMinusPullup = NEDOPlatform_io_usb11hs_USBDMinusPullup; // @[NEDOwrapper.scala 244:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347892.4]
  assign tlport_a_valid = NEDOPlatform_io_tlport_a_valid; // @[NEDOwrapper.scala 262:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347904.4]
  assign tlport_a_bits_opcode = NEDOPlatform_io_tlport_a_bits_opcode; // @[NEDOwrapper.scala 262:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347903.4]
  assign tlport_a_bits_param = NEDOPlatform_io_tlport_a_bits_param; // @[NEDOwrapper.scala 262:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347903.4]
  assign tlport_a_bits_size = NEDOPlatform_io_tlport_a_bits_size; // @[NEDOwrapper.scala 262:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347903.4]
  assign tlport_a_bits_source = NEDOPlatform_io_tlport_a_bits_source; // @[NEDOwrapper.scala 262:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347903.4]
  assign tlport_a_bits_address = NEDOPlatform_io_tlport_a_bits_address; // @[NEDOwrapper.scala 262:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347903.4]
  assign tlport_a_bits_mask = NEDOPlatform_io_tlport_a_bits_mask; // @[NEDOwrapper.scala 262:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347903.4]
  assign tlport_a_bits_data = NEDOPlatform_io_tlport_a_bits_data; // @[NEDOwrapper.scala 262:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347903.4]
  assign tlport_a_bits_corrupt = NEDOPlatform_io_tlport_a_bits_corrupt; // @[NEDOwrapper.scala 262:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347903.4]
  assign tlport_d_ready = NEDOPlatform_io_tlport_d_ready; // @[NEDOwrapper.scala 263:17:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347908.4]
  assign NEDOPlatform_clock = sys_clk; // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347806.4]
  assign NEDOPlatform_reset = _T_18 | ndreset; // @[:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347807.4]
  assign NEDOPlatform_io_pins_jtag_TCK_i_ival = jtag_jtag_TCK; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347860.4]
  assign NEDOPlatform_io_pins_jtag_TMS_i_ival = jtag_jtag_TMS; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347859.4]
  assign NEDOPlatform_io_pins_jtag_TDI_i_ival = jtag_jtag_TDI; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347861.4]
  assign NEDOPlatform_io_pins_gpio_pins_0_i_ival = gpio_in[0]; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347835.4]
  assign NEDOPlatform_io_pins_gpio_pins_1_i_ival = gpio_in[1]; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347836.4]
  assign NEDOPlatform_io_pins_gpio_pins_2_i_ival = gpio_in[2]; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347837.4]
  assign NEDOPlatform_io_pins_gpio_pins_3_i_ival = gpio_in[3]; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347838.4]
  assign NEDOPlatform_io_pins_gpio_pins_4_i_ival = gpio_in[4]; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347839.4]
  assign NEDOPlatform_io_pins_gpio_pins_5_i_ival = gpio_in[5]; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347840.4]
  assign NEDOPlatform_io_pins_gpio_pins_6_i_ival = gpio_in[6]; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347841.4]
  assign NEDOPlatform_io_pins_gpio_pins_7_i_ival = gpio_in[7]; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347842.4]
  assign NEDOPlatform_io_pins_qspi_dq_1_i_ival = qspi_qspi_miso; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347873.4]
  assign NEDOPlatform_io_pins_uart_rxd_i_ival = uart_rxd; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347887.4]
  assign NEDOPlatform_io_pins_spi_dq_1_i_ival = sdio_sdio_dat_0; // @[ChipMacros.scala 75:16:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347884.4]
  assign NEDOPlatform_io_usb11hs_usbClk = usb11hs_usbClk; // @[NEDOwrapper.scala 244:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347900.4]
  assign NEDOPlatform_io_usb11hs_USBWireDataIn = usb11hs_USBWireDataIn; // @[NEDOwrapper.scala 244:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347899.4]
  assign NEDOPlatform_io_usb11hs_vBusDetect = usb11hs_vBusDetect; // @[NEDOwrapper.scala 244:13:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347891.4]
  assign NEDOPlatform_io_jtag_reset = jrst_n == 1'h0; // @[NEDOwrapper.scala 220:26:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347864.4]
  assign NEDOPlatform_io_tlport_a_ready = tlport_a_ready; // @[NEDOwrapper.scala 262:12:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347905.4]
  assign NEDOPlatform_io_tlport_d_valid = tlport_d_valid; // @[NEDOwrapper.scala 263:17:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347907.4]
  assign NEDOPlatform_io_tlport_d_bits_opcode = tlport_d_bits_opcode; // @[NEDOwrapper.scala 263:17:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347906.4]
  assign NEDOPlatform_io_tlport_d_bits_param = tlport_d_bits_param; // @[NEDOwrapper.scala 263:17:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347906.4]
  assign NEDOPlatform_io_tlport_d_bits_size = tlport_d_bits_size; // @[NEDOwrapper.scala 263:17:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347906.4]
  assign NEDOPlatform_io_tlport_d_bits_source = tlport_d_bits_source; // @[NEDOwrapper.scala 263:17:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347906.4]
  assign NEDOPlatform_io_tlport_d_bits_sink = tlport_d_bits_sink; // @[NEDOwrapper.scala 263:17:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347906.4]
  assign NEDOPlatform_io_tlport_d_bits_denied = tlport_d_bits_denied; // @[NEDOwrapper.scala 263:17:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347906.4]
  assign NEDOPlatform_io_tlport_d_bits_data = tlport_d_bits_data; // @[NEDOwrapper.scala 263:17:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347906.4]
  assign NEDOPlatform_io_tlport_d_bits_corrupt = tlport_d_bits_corrupt; // @[NEDOwrapper.scala 263:17:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347906.4]
  assign NEDOPlatform_io_ChildClock = ChildClock; // @[NEDOwrapper.scala 249:26:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347901.4]
  assign NEDOPlatform_io_ChildReset = ChildReset; // @[NEDOwrapper.scala 250:26:uec.keystoneAcc.nedochip.NEDOFPGAQuartus.ChipConfigDE4.fir@347902.4]
endmodule