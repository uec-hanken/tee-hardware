set_property CFGBVS GND                               [current_design]
set_property CONFIG_VOLTAGE 1.8                       [current_design]

create_clock -name sys_diff_clk -period 5.0 [get_ports sys_clock_p]
set_input_jitter [get_clocks -of_objects [get_ports sys_clock_p]] 0.5
create_clock -name JTCK -period 100.0 [get_ports {jtag_jtag_TCK}]
set_input_jitter JTCK 0.5
create_clock -name pcie_ref_clk -period 10.0 [get_ports {pciePorts_REFCLK_rxp}]
set_input_jitter pcie_ref_clk 0.5

set_clock_groups -asynchronous \
  -group [list [get_clocks { \
      sys_diff_clk \
    }]] \
  -group [list [get_clocks { \
      txoutclk \
      userclk1 \
    }]] \
  -group [list [get_clocks -of_objects [get_pins { \
      pll/clk_out1 \
    }]]] \
  -group [list [get_clocks -of_objects [get_pins { \
      pll/clk_out2 \
    }]]] \
  -group [list [get_clocks -of_objects [get_pins { \
      pll/clk_out3 \
    }]]] \
  -group [list [get_clocks { \
      JTCK \
    }]] \
  -group [list [get_clocks -of_objects [get_pins { \
      mod_1/clockctrlClockDomainWrapper/clockctrl_0/mmcme2_adv_inst/CLKOUT0 \
    }]]]

# Nah, definitelly quit here. Going to put the false path in the clock


set_property CLOCK_DEDICATED_ROUTE {FALSE} [get_nets [get_ports {jtag_jtag_TCK}]]
set_property PACKAGE_PIN {F22} [get_ports {jtag_jtag_TCK}]
set_property IOSTANDARD {LVCMOS25} [get_ports jtag_jtag_TCK]
set_property PULLUP true [get_ports jtag_jtag_TCK]
set_property PACKAGE_PIN {N16} [get_ports {jtag_jtag_TMS}]
set_property IOSTANDARD {LVCMOS25} [get_ports jtag_jtag_TMS]
set_property PULLUP true [get_ports jtag_jtag_TMS]
set_property PACKAGE_PIN {E23} [get_ports {jtag_jtag_TDI}]
set_property IOSTANDARD {LVCMOS25} [get_ports jtag_jtag_TDI]
set_property PULLUP true [get_ports jtag_jtag_TDI]
set_property PACKAGE_PIN {P24} [get_ports {jtag_jtag_TDO}]
set_property IOSTANDARD {LVCMOS25} [get_ports jtag_jtag_TDO]
set_property PULLUP true [get_ports jtag_jtag_TDO]

# set_property PACKAGE_PIN {AT32} [get_ports {uart_ctsn}]
# set_property IOSTANDARD {LVCMOS25} [get_ports {uart_ctsn}]
# set_property IOB {TRUE} [get_ports {uart_ctsn}]
# set_property PACKAGE_PIN {AR34} [get_ports {uart_rtsn}]
# set_property IOSTANDARD {LVCMOS25} [get_ports {uart_rtsn}]
# set_property IOB {TRUE} [ get_ports {uart_rtsn}]

set_property PACKAGE_PIN {N17} [get_ports {uart_rxd}]
set_property IOSTANDARD {LVCMOS25} [get_ports uart_rxd]
set_property IOB TRUE [get_ports uart_rxd]
set_property PACKAGE_PIN {D19} [get_ports {uart_txd}]
set_property IOSTANDARD {LVCMOS25} [get_ports uart_txd]
set_property IOB TRUE [get_ports uart_txd]

set_property PACKAGE_PIN {F23} [get_ports {sdio_sdio_clk}]
set_property IOSTANDARD {LVCMOS25} [get_ports sdio_sdio_clk]
set_property IOB TRUE [get_ports sdio_sdio_clk]
set_property PACKAGE_PIN {G22} [get_ports {sdio_sdio_cmd}]
set_property IOSTANDARD {LVCMOS25} [get_ports sdio_sdio_cmd]
set_property IOB TRUE [get_ports sdio_sdio_cmd]
set_property PACKAGE_PIN {F24} [get_ports {sdio_sdio_dat_0}]
set_property IOSTANDARD {LVCMOS25} [get_ports sdio_sdio_dat_0]
set_property IOB TRUE [get_ports sdio_sdio_dat_0]
# set_property PACKAGE_PIN {AU31} [get_ports {sdio_sdio_dat_1}]
# set_property IOSTANDARD {LVCMOS25} [get_ports sdio_sdio_dat_1]
# set_property IOB TRUE [get_ports sdio_sdio_dat_1]
# set_property PACKAGE_PIN {AV31} [get_ports {sdio_sdio_dat_2}]
# set_property IOSTANDARD {LVCMOS25} [get_ports sdio_sdio_dat_2]
# set_property IOB TRUE [get_ports sdio_sdio_dat_2]
set_property PACKAGE_PIN {G24} [get_ports {sdio_sdio_dat_3}]
set_property IOSTANDARD {LVCMOS25} [get_ports sdio_sdio_dat_3]
set_property IOB TRUE [get_ports sdio_sdio_dat_3]
set_property PULLUP {TRUE} [get_ports {sdio_sdio_cmd}]
set_property PULLUP {TRUE} [get_ports {sdio_sdio_dat_0}]
# set_property PULLUP {TRUE} [get_ports {sdio_sdio_dat_1}]
# set_property PULLUP {TRUE} [get_ports {sdio_sdio_dat_2}]
set_property PULLUP {TRUE} [get_ports {sdio_sdio_dat_3}]

set_property PACKAGE_PIN {J21} [get_ports {gpio_in[0]}]
set_property PACKAGE_PIN {N19} [get_ports {gpio_in[1]}]
set_property PACKAGE_PIN {M16} [get_ports {gpio_in[2]}]
set_property PACKAGE_PIN {M20} [get_ports {gpio_in[3]}]
set_property PACKAGE_PIN {L17} [get_ports {gpio_in[4]}]
set_property PACKAGE_PIN {N24} [get_ports {gpio_in[5]}]
set_property PACKAGE_PIN {K21} [get_ports {gpio_in[6]}]
set_property PACKAGE_PIN {E21} [get_ports {gpio_in[7]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_in[0]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_in[1]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_in[2]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_in[3]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_in[4]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_in[5]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_in[6]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_in[7]}]

set_property PACKAGE_PIN {T18} [get_ports {gpio_out[7]}]
set_property PACKAGE_PIN {T19} [get_ports {gpio_out[6]}]
set_property PACKAGE_PIN {P16} [get_ports {gpio_out[5]}]
set_property PACKAGE_PIN {K15} [get_ports {gpio_out[4]}]
set_property PACKAGE_PIN {H19} [get_ports {gpio_out[3]}]
set_property PACKAGE_PIN {K18} [get_ports {gpio_out[2]}]
set_property PACKAGE_PIN {L19} [get_ports {gpio_out[1]}]
set_property PACKAGE_PIN {G20} [get_ports {gpio_out[0]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_out[7]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_out[6]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_out[5]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_out[4]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_out[3]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_out[2]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_out[1]}]
set_property IOSTANDARD {LVCMOS25} [get_ports {gpio_out[0]}]

set_property PACKAGE_PIN {L23} [get_ports {rst_0}]
set_property IOSTANDARD {LVCMOS25} [get_ports {rst_0}]

# _____________TODO: Package_Pin for QSPI and USB in FMC are not correct yet. _________
# _____________This is just a copy of VC707.shell.xdc__________________________________
#set_property PACKAGE_PIN {N38} [get_ports {qspi_qspi_cs}] # FMC1_HPC_LA_P_10
#set_property PACKAGE_PIN {M39} [get_ports {qspi_qspi_sck}] # FMC1_HPC_LA_N_10
#set_property PACKAGE_PIN {F40} [get_ports {qspi_qspi_miso}] # FMC1_HPC_LA_P_11
#set_property PACKAGE_PIN {F41} [get_ports {qspi_qspi_mosi}] # FMC1_HPC_LA_N_11
#set_property PACKAGE_PIN {R40} [get_ports {qspi_qspi_wp}] # FMC1_HPC_LA_P_12
#set_property PACKAGE_PIN {P40} [get_ports {qspi_qspi_hold}] # FMC1_HPC_LA_N_12
#set_property IOSTANDARD {LVCMOS25} [get_ports {qspi_qspi_cs}]
#set_property IOSTANDARD {LVCMOS25} [get_ports {qspi_qspi_sck}]
#set_property IOSTANDARD {LVCMOS25} [get_ports {qspi_qspi_miso}]
#set_property IOSTANDARD {LVCMOS25} [get_ports {qspi_qspi_mosi}]
#set_property IOSTANDARD {LVCMOS25} [get_ports {qspi_qspi_wp}]
#set_property IOSTANDARD {LVCMOS25} [get_ports {qspi_qspi_hold}]
#set_property PACKAGE_PIN {AK39} [get_ports {USB_0_WireDataIn[0]}] # FMC2_HPC_LA_P_2
#set_property PACKAGE_PIN {AL39} [get_ports {USB_0_WireDataIn[1]}] # FMC2_HPC_LA_N_2
#set_property PACKAGE_PIN {AJ42} [get_ports {USB_0_WireDataOut[0]}] # FMC2_HPC_LA_P_3
#set_property PACKAGE_PIN {AK42} [get_ports {USB_0_WireDataOut[1]}] # FMC2_HPC_LA_N_3
#set_property PACKAGE_PIN {AF42} [get_ports {USB_0_WireCtrlOut}] # FMC2_HPC_LA_P_5
#set_property PACKAGE_PIN {AG42} [get_ports {USB_0_FullSpeed}] # FMC2_HPC_LA_N_5
#set_property IOSTANDARD {LVCMOS25} [get_ports {USB_0_WireDataIn[0]}]
#set_property IOSTANDARD {LVCMOS25} [get_ports {USB_0_WireDataIn[1]}]
#set_property IOSTANDARD {LVCMOS25} [get_ports {USB_0_WireDataOut[0]}]
#set_property IOSTANDARD {LVCMOS25} [get_ports {USB_0_WireDataOut[1]}]
#set_property IOSTANDARD {LVCMOS25} [get_ports {USB_0_WireCtrlOut}]
#set_property IOSTANDARD {LVCMOS25} [get_ports {USB_0_FullSpeed}]

set_property PACKAGE_PIN {AB2} [get_ports {sys_clock_p}]
set_property PACKAGE_PIN {AC2} [get_ports {sys_clock_n}]
set_property IOSTANDARD DIFF_HSTL_I [get_ports {sys_clock_p}]
set_property IOSTANDARD DIFF_HSTL_I [get_ports {sys_clock_n}]





