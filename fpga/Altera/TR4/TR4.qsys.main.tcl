# TODO: This is not the correct way to do this
# We actually plan to export the files, one by one, using qsys tcl
# Then get all together from the synthesis generated
# The connections will be made in Scala.
# For now, this is just a replica of the qsys file.

# qsys scripting (.tcl) file for main
package require -exact qsys 16.0

create_system {main}

set_project_property DEVICE_FAMILY {Stratix IV}
set_project_property DEVICE {EP4SGX230KF40C2}
set_project_property HIDE_FROM_IP_CATALOG {false}

# Instances and instance parameters
# (disabled instances are intentionally culled)
add_instance AXI_Bridge altera_axi_bridge 20.1
set_instance_parameter_value AXI_Bridge {ADDR_WIDTH} {30}
set_instance_parameter_value AXI_Bridge {AXI_VERSION} {AXI4}
set_instance_parameter_value AXI_Bridge {COMBINED_ACCEPTANCE_CAPABILITY} {16}
set_instance_parameter_value AXI_Bridge {COMBINED_ISSUING_CAPABILITY} {16}
set_instance_parameter_value AXI_Bridge {DATA_WIDTH} {32}
set_instance_parameter_value AXI_Bridge {M0_ID_WIDTH} {4}
set_instance_parameter_value AXI_Bridge {READ_ACCEPTANCE_CAPABILITY} {16}
set_instance_parameter_value AXI_Bridge {READ_ADDR_USER_WIDTH} {32}
set_instance_parameter_value AXI_Bridge {READ_DATA_REORDERING_DEPTH} {1}
set_instance_parameter_value AXI_Bridge {READ_DATA_USER_WIDTH} {32}
set_instance_parameter_value AXI_Bridge {READ_ISSUING_CAPABILITY} {16}
set_instance_parameter_value AXI_Bridge {S0_ID_WIDTH} {4}
set_instance_parameter_value AXI_Bridge {USE_M0_ARBURST} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_ARCACHE} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_ARID} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_ARLEN} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_ARLOCK} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_ARQOS} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_ARREGION} {0}
set_instance_parameter_value AXI_Bridge {USE_M0_ARSIZE} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_ARUSER} {0}
set_instance_parameter_value AXI_Bridge {USE_M0_AWBURST} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_AWCACHE} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_AWID} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_AWLEN} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_AWLOCK} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_AWQOS} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_AWREGION} {0}
set_instance_parameter_value AXI_Bridge {USE_M0_AWSIZE} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_AWUSER} {0}
set_instance_parameter_value AXI_Bridge {USE_M0_BID} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_BRESP} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_BUSER} {0}
set_instance_parameter_value AXI_Bridge {USE_M0_RID} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_RLAST} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_RRESP} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_RUSER} {0}
set_instance_parameter_value AXI_Bridge {USE_M0_WSTRB} {1}
set_instance_parameter_value AXI_Bridge {USE_M0_WUSER} {0}
set_instance_parameter_value AXI_Bridge {USE_PIPELINE} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_ARCACHE} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_ARLOCK} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_ARPROT} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_ARQOS} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_ARREGION} {0}
set_instance_parameter_value AXI_Bridge {USE_S0_ARUSER} {0}
set_instance_parameter_value AXI_Bridge {USE_S0_AWCACHE} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_AWLOCK} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_AWPROT} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_AWQOS} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_AWREGION} {0}
set_instance_parameter_value AXI_Bridge {USE_S0_AWUSER} {0}
set_instance_parameter_value AXI_Bridge {USE_S0_BRESP} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_BUSER} {0}
set_instance_parameter_value AXI_Bridge {USE_S0_RRESP} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_RUSER} {0}
set_instance_parameter_value AXI_Bridge {USE_S0_WLAST} {1}
set_instance_parameter_value AXI_Bridge {USE_S0_WUSER} {0}
set_instance_parameter_value AXI_Bridge {WRITE_ACCEPTANCE_CAPABILITY} {16}
set_instance_parameter_value AXI_Bridge {WRITE_ADDR_USER_WIDTH} {32}
set_instance_parameter_value AXI_Bridge {WRITE_DATA_USER_WIDTH} {32}
set_instance_parameter_value AXI_Bridge {WRITE_ISSUING_CAPABILITY} {16}
set_instance_parameter_value AXI_Bridge {WRITE_RESP_USER_WIDTH} {32}

add_instance AvalonMM_ClockCrossing altera_avalon_mm_clock_crossing_bridge 20.1
set_instance_parameter_value AvalonMM_ClockCrossing {ADDRESS_UNITS} {SYMBOLS}
set_instance_parameter_value AvalonMM_ClockCrossing {ADDRESS_WIDTH} {30}
set_instance_parameter_value AvalonMM_ClockCrossing {COMMAND_FIFO_DEPTH} {4}
set_instance_parameter_value AvalonMM_ClockCrossing {DATA_WIDTH} {32}
set_instance_parameter_value AvalonMM_ClockCrossing {MASTER_SYNC_DEPTH} {2}
set_instance_parameter_value AvalonMM_ClockCrossing {MAX_BURST_SIZE} {1}
set_instance_parameter_value AvalonMM_ClockCrossing {RESPONSE_FIFO_DEPTH} {4}
set_instance_parameter_value AvalonMM_ClockCrossing {SLAVE_SYNC_DEPTH} {2}
set_instance_parameter_value AvalonMM_ClockCrossing {SYMBOL_WIDTH} {8}
set_instance_parameter_value AvalonMM_ClockCrossing {USE_AUTO_ADDRESS_WIDTH} {1}

add_instance DDR3_IP altera_mem_if_ddr3_emif 20.1
set_instance_parameter_value DDR3_IP {ABSTRACT_REAL_COMPARE_TEST} {0}
set_instance_parameter_value DDR3_IP {ABS_RAM_MEM_INIT_FILENAME} {meminit}
set_instance_parameter_value DDR3_IP {ACV_PHY_CLK_ADD_FR_PHASE} {0.0}
set_instance_parameter_value DDR3_IP {AC_PACKAGE_DESKEW} {0}
set_instance_parameter_value DDR3_IP {AC_ROM_USER_ADD_0} {0_0000_0000_0000}
set_instance_parameter_value DDR3_IP {AC_ROM_USER_ADD_1} {0_0000_0000_1000}
set_instance_parameter_value DDR3_IP {ADDR_ORDER} {0}
set_instance_parameter_value DDR3_IP {ADD_EFFICIENCY_MONITOR} {0}
set_instance_parameter_value DDR3_IP {ADD_EXTERNAL_SEQ_DEBUG_NIOS} {0}
set_instance_parameter_value DDR3_IP {ADVANCED_CK_PHASES} {0}
set_instance_parameter_value DDR3_IP {ADVERTIZE_SEQUENCER_SW_BUILD_FILES} {0}
set_instance_parameter_value DDR3_IP {AFI_DEBUG_INFO_WIDTH} {32}
set_instance_parameter_value DDR3_IP {ALTMEMPHY_COMPATIBLE_MODE} {0}
set_instance_parameter_value DDR3_IP {AP_MODE} {0}
set_instance_parameter_value DDR3_IP {AP_MODE_EN} {0}
set_instance_parameter_value DDR3_IP {AUTO_PD_CYCLES} {0}
set_instance_parameter_value DDR3_IP {AUTO_POWERDN_EN} {0}
set_instance_parameter_value DDR3_IP {AVL_DATA_WIDTH_PORT} {32 32 32 32 32 32}
set_instance_parameter_value DDR3_IP {AVL_MAX_SIZE} {8}
set_instance_parameter_value DDR3_IP {BYTE_ENABLE} {1}
set_instance_parameter_value DDR3_IP {C2P_WRITE_CLOCK_ADD_PHASE} {0.0}
set_instance_parameter_value DDR3_IP {CALIBRATION_MODE} {Skip}
set_instance_parameter_value DDR3_IP {CALIB_REG_WIDTH} {8}
set_instance_parameter_value DDR3_IP {CFG_DATA_REORDERING_TYPE} {INTER_BANK}
set_instance_parameter_value DDR3_IP {CFG_REORDER_DATA} {1}
set_instance_parameter_value DDR3_IP {CFG_TCCD_NS} {2.5}
set_instance_parameter_value DDR3_IP {COMMAND_PHASE} {0.0}
set_instance_parameter_value DDR3_IP {CONTROLLER_LATENCY} {5}
set_instance_parameter_value DDR3_IP {CORE_DEBUG_CONNECTION} {EXPORT}
set_instance_parameter_value DDR3_IP {CPORT_TYPE_PORT} {Bidirectional Bidirectional Bidirectional Bidirectional Bidirectional Bidirectional}
set_instance_parameter_value DDR3_IP {CTL_AUTOPCH_EN} {0}
set_instance_parameter_value DDR3_IP {CTL_CMD_QUEUE_DEPTH} {8}
set_instance_parameter_value DDR3_IP {CTL_CSR_CONNECTION} {INTERNAL_JTAG}
set_instance_parameter_value DDR3_IP {CTL_CSR_ENABLED} {0}
set_instance_parameter_value DDR3_IP {CTL_CSR_READ_ONLY} {1}
set_instance_parameter_value DDR3_IP {CTL_DEEP_POWERDN_EN} {0}
set_instance_parameter_value DDR3_IP {CTL_DYNAMIC_BANK_ALLOCATION} {0}
set_instance_parameter_value DDR3_IP {CTL_DYNAMIC_BANK_NUM} {4}
set_instance_parameter_value DDR3_IP {CTL_ECC_AUTO_CORRECTION_ENABLED} {0}
set_instance_parameter_value DDR3_IP {CTL_ECC_ENABLED} {0}
set_instance_parameter_value DDR3_IP {CTL_ENABLE_BURST_INTERRUPT} {0}
set_instance_parameter_value DDR3_IP {CTL_ENABLE_BURST_TERMINATE} {0}
set_instance_parameter_value DDR3_IP {CTL_HRB_ENABLED} {0}
set_instance_parameter_value DDR3_IP {CTL_LOOK_AHEAD_DEPTH} {4}
set_instance_parameter_value DDR3_IP {CTL_SELF_REFRESH_EN} {0}
set_instance_parameter_value DDR3_IP {CTL_USR_REFRESH_EN} {0}
set_instance_parameter_value DDR3_IP {CTL_ZQCAL_EN} {0}
set_instance_parameter_value DDR3_IP {CUT_NEW_FAMILY_TIMING} {1}
set_instance_parameter_value DDR3_IP {DAT_DATA_WIDTH} {32}
set_instance_parameter_value DDR3_IP {DEBUG_MODE} {0}
set_instance_parameter_value DDR3_IP {DEVICE_DEPTH} {1}
set_instance_parameter_value DDR3_IP {DEVICE_FAMILY_PARAM} {}
set_instance_parameter_value DDR3_IP {DISABLE_CHILD_MESSAGING} {0}
set_instance_parameter_value DDR3_IP {DISCRETE_FLY_BY} {1}
set_instance_parameter_value DDR3_IP {DLL_SHARING_MODE} {None}
set_instance_parameter_value DDR3_IP {DQS_DQSN_MODE} {DIFFERENTIAL}
set_instance_parameter_value DDR3_IP {DQ_INPUT_REG_USE_CLKN} {0}
set_instance_parameter_value DDR3_IP {DUPLICATE_AC} {0}
set_instance_parameter_value DDR3_IP {ED_EXPORT_SEQ_DEBUG} {0}
set_instance_parameter_value DDR3_IP {ENABLE_ABS_RAM_MEM_INIT} {0}
set_instance_parameter_value DDR3_IP {ENABLE_BONDING} {0}
set_instance_parameter_value DDR3_IP {ENABLE_BURST_MERGE} {0}
set_instance_parameter_value DDR3_IP {ENABLE_CTRL_AVALON_INTERFACE} {1}
set_instance_parameter_value DDR3_IP {ENABLE_DELAY_CHAIN_WRITE} {0}
set_instance_parameter_value DDR3_IP {ENABLE_EMIT_BFM_MASTER} {0}
set_instance_parameter_value DDR3_IP {ENABLE_EXPORT_SEQ_DEBUG_BRIDGE} {0}
set_instance_parameter_value DDR3_IP {ENABLE_EXTRA_REPORTING} {0}
set_instance_parameter_value DDR3_IP {ENABLE_ISS_PROBES} {0}
set_instance_parameter_value DDR3_IP {ENABLE_NON_DESTRUCTIVE_CALIB} {0}
set_instance_parameter_value DDR3_IP {ENABLE_NON_DES_CAL} {0}
set_instance_parameter_value DDR3_IP {ENABLE_NON_DES_CAL_TEST} {0}
set_instance_parameter_value DDR3_IP {ENABLE_SEQUENCER_MARGINING_ON_BY_DEFAULT} {0}
set_instance_parameter_value DDR3_IP {ENABLE_USER_ECC} {0}
set_instance_parameter_value DDR3_IP {EXPORT_AFI_HALF_CLK} {0}
set_instance_parameter_value DDR3_IP {EXTRA_SETTINGS} {}
set_instance_parameter_value DDR3_IP {FIX_READ_LATENCY} {8}
set_instance_parameter_value DDR3_IP {FORCED_NON_LDC_ADDR_CMD_MEM_CK_INVERT} {0}
set_instance_parameter_value DDR3_IP {FORCED_NUM_WRITE_FR_CYCLE_SHIFTS} {0}
set_instance_parameter_value DDR3_IP {FORCE_DQS_TRACKING} {AUTO}
set_instance_parameter_value DDR3_IP {FORCE_MAX_LATENCY_COUNT_WIDTH} {0}
set_instance_parameter_value DDR3_IP {FORCE_SEQUENCER_TCL_DEBUG_MODE} {0}
set_instance_parameter_value DDR3_IP {FORCE_SHADOW_REGS} {AUTO}
set_instance_parameter_value DDR3_IP {FORCE_SYNTHESIS_LANGUAGE} {}
set_instance_parameter_value DDR3_IP {HARD_EMIF} {0}
set_instance_parameter_value DDR3_IP {HCX_COMPAT_MODE} {0}
set_instance_parameter_value DDR3_IP {HHP_HPS} {0}
set_instance_parameter_value DDR3_IP {HHP_HPS_SIMULATION} {0}
set_instance_parameter_value DDR3_IP {HHP_HPS_VERIFICATION} {0}
set_instance_parameter_value DDR3_IP {HPS_PROTOCOL} {DEFAULT}
set_instance_parameter_value DDR3_IP {INCLUDE_BOARD_DELAY_MODEL} {0}
set_instance_parameter_value DDR3_IP {INCLUDE_MULTIRANK_BOARD_DELAY_MODEL} {0}
set_instance_parameter_value DDR3_IP {IS_ES_DEVICE} {0}
set_instance_parameter_value DDR3_IP {LOCAL_ID_WIDTH} {8}
set_instance_parameter_value DDR3_IP {LRDIMM_EXTENDED_CONFIG} {0x000000000000000000}
set_instance_parameter_value DDR3_IP {MARGIN_VARIATION_TEST} {0}
set_instance_parameter_value DDR3_IP {MAX_PENDING_RD_CMD} {32}
set_instance_parameter_value DDR3_IP {MAX_PENDING_WR_CMD} {16}
set_instance_parameter_value DDR3_IP {MEM_ASR} {Manual}
set_instance_parameter_value DDR3_IP {MEM_ATCL} {Disabled}
set_instance_parameter_value DDR3_IP {MEM_AUTO_LEVELING_MODE} {1}
set_instance_parameter_value DDR3_IP {MEM_BANKADDR_WIDTH} {3}
set_instance_parameter_value DDR3_IP {MEM_BL} {OTF}
set_instance_parameter_value DDR3_IP {MEM_BT} {Sequential}
set_instance_parameter_value DDR3_IP {MEM_CK_PHASE} {0.0}
set_instance_parameter_value DDR3_IP {MEM_CK_WIDTH} {2}
set_instance_parameter_value DDR3_IP {MEM_CLK_EN_WIDTH} {1}
set_instance_parameter_value DDR3_IP {MEM_CLK_FREQ} {300.0}
set_instance_parameter_value DDR3_IP {MEM_CLK_FREQ_MAX} {533.333}
set_instance_parameter_value DDR3_IP {MEM_COL_ADDR_WIDTH} {10}
set_instance_parameter_value DDR3_IP {MEM_CS_WIDTH} {1}
set_instance_parameter_value DDR3_IP {MEM_DEVICE} {MISSING_MODEL}
set_instance_parameter_value DDR3_IP {MEM_DLL_EN} {1}
set_instance_parameter_value DDR3_IP {MEM_DQ_PER_DQS} {8}
set_instance_parameter_value DDR3_IP {MEM_DQ_WIDTH} {64}
set_instance_parameter_value DDR3_IP {MEM_DRV_STR} {RZQ/6}
set_instance_parameter_value DDR3_IP {MEM_FORMAT} {UNBUFFERED}
set_instance_parameter_value DDR3_IP {MEM_GUARANTEED_WRITE_INIT} {0}
set_instance_parameter_value DDR3_IP {MEM_IF_BOARD_BASE_DELAY} {10}
set_instance_parameter_value DDR3_IP {MEM_IF_DM_PINS_EN} {1}
set_instance_parameter_value DDR3_IP {MEM_IF_DQSN_EN} {1}
set_instance_parameter_value DDR3_IP {MEM_IF_SIM_VALID_WINDOW} {0}
set_instance_parameter_value DDR3_IP {MEM_INIT_EN} {0}
set_instance_parameter_value DDR3_IP {MEM_INIT_FILE} {}
set_instance_parameter_value DDR3_IP {MEM_MIRROR_ADDRESSING} {0}
set_instance_parameter_value DDR3_IP {MEM_NUMBER_OF_DIMMS} {1}
set_instance_parameter_value DDR3_IP {MEM_NUMBER_OF_RANKS_PER_DEVICE} {1}
set_instance_parameter_value DDR3_IP {MEM_NUMBER_OF_RANKS_PER_DIMM} {1}
set_instance_parameter_value DDR3_IP {MEM_PD} {DLL off}
set_instance_parameter_value DDR3_IP {MEM_RANK_MULTIPLICATION_FACTOR} {1}
set_instance_parameter_value DDR3_IP {MEM_ROW_ADDR_WIDTH} {14}
set_instance_parameter_value DDR3_IP {MEM_RTT_NOM} {ODT Disabled}
set_instance_parameter_value DDR3_IP {MEM_RTT_WR} {Dynamic ODT off}
set_instance_parameter_value DDR3_IP {MEM_SRT} {Normal}
set_instance_parameter_value DDR3_IP {MEM_TCL} {7}
set_instance_parameter_value DDR3_IP {MEM_TFAW_NS} {37.5}
set_instance_parameter_value DDR3_IP {MEM_TINIT_US} {200}
set_instance_parameter_value DDR3_IP {MEM_TMRD_CK} {4}
set_instance_parameter_value DDR3_IP {MEM_TRAS_NS} {37.5}
set_instance_parameter_value DDR3_IP {MEM_TRCD_NS} {13.13}
set_instance_parameter_value DDR3_IP {MEM_TREFI_US} {7.8}
set_instance_parameter_value DDR3_IP {MEM_TRFC_NS} {110.0}
set_instance_parameter_value DDR3_IP {MEM_TRP_NS} {13.125}
set_instance_parameter_value DDR3_IP {MEM_TRRD_NS} {7.5}
set_instance_parameter_value DDR3_IP {MEM_TRTP_NS} {7.5}
set_instance_parameter_value DDR3_IP {MEM_TWR_NS} {15.0}
set_instance_parameter_value DDR3_IP {MEM_TWTR} {4}
set_instance_parameter_value DDR3_IP {MEM_USER_LEVELING_MODE} {Leveling}
set_instance_parameter_value DDR3_IP {MEM_VENDOR} {Hynix}
set_instance_parameter_value DDR3_IP {MEM_VERBOSE} {1}
set_instance_parameter_value DDR3_IP {MEM_VOLTAGE} {1.5V DDR3}
set_instance_parameter_value DDR3_IP {MEM_WTCL} {6}
set_instance_parameter_value DDR3_IP {MRS_MIRROR_PING_PONG_ATSO} {0}
set_instance_parameter_value DDR3_IP {MULTICAST_EN} {0}
set_instance_parameter_value DDR3_IP {NEXTGEN} {1}
set_instance_parameter_value DDR3_IP {NIOS_ROM_DATA_WIDTH} {32}
set_instance_parameter_value DDR3_IP {NUM_DLL_SHARING_INTERFACES} {1}
set_instance_parameter_value DDR3_IP {NUM_EXTRA_REPORT_PATH} {10}
set_instance_parameter_value DDR3_IP {NUM_OCT_SHARING_INTERFACES} {1}
set_instance_parameter_value DDR3_IP {NUM_OF_PORTS} {1}
set_instance_parameter_value DDR3_IP {NUM_PLL_SHARING_INTERFACES} {1}
set_instance_parameter_value DDR3_IP {OCT_SHARING_MODE} {None}
set_instance_parameter_value DDR3_IP {P2C_READ_CLOCK_ADD_PHASE} {0.0}
set_instance_parameter_value DDR3_IP {PACKAGE_DESKEW} {0}
set_instance_parameter_value DDR3_IP {PARSE_FRIENDLY_DEVICE_FAMILY_PARAM} {}
set_instance_parameter_value DDR3_IP {PARSE_FRIENDLY_DEVICE_FAMILY_PARAM_VALID} {0}
set_instance_parameter_value DDR3_IP {PHY_CSR_CONNECTION} {INTERNAL_JTAG}
set_instance_parameter_value DDR3_IP {PHY_CSR_ENABLED} {0}
set_instance_parameter_value DDR3_IP {PHY_ONLY} {0}
set_instance_parameter_value DDR3_IP {PINGPONGPHY_EN} {0}
set_instance_parameter_value DDR3_IP {PLL_ADDR_CMD_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_ADDR_CMD_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_ADDR_CMD_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_ADDR_CMD_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_ADDR_CMD_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_ADDR_CMD_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_AFI_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_AFI_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_AFI_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_AFI_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_AFI_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_AFI_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_AFI_HALF_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_AFI_HALF_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_AFI_HALF_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_AFI_HALF_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_AFI_HALF_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_AFI_HALF_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_AFI_PHY_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_AFI_PHY_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_AFI_PHY_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_AFI_PHY_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_AFI_PHY_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_AFI_PHY_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_C2P_WRITE_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_C2P_WRITE_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_C2P_WRITE_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_C2P_WRITE_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_C2P_WRITE_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_C2P_WRITE_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_CLK_PARAM_VALID} {0}
set_instance_parameter_value DDR3_IP {PLL_CONFIG_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_CONFIG_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_CONFIG_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_CONFIG_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_CONFIG_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_CONFIG_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_DR_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_DR_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_DR_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_DR_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_DR_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_DR_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_HR_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_HR_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_HR_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_HR_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_HR_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_HR_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_LOCATION} {Top_Bottom}
set_instance_parameter_value DDR3_IP {PLL_MEM_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_MEM_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_MEM_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_MEM_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_MEM_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_MEM_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_NIOS_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_NIOS_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_NIOS_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_NIOS_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_NIOS_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_NIOS_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_P2C_READ_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_P2C_READ_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_P2C_READ_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_P2C_READ_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_P2C_READ_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_P2C_READ_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_SHARING_MODE} {None}
set_instance_parameter_value DDR3_IP {PLL_WRITE_CLK_DIV_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_WRITE_CLK_FREQ_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {PLL_WRITE_CLK_FREQ_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {PLL_WRITE_CLK_MULT_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_WRITE_CLK_PHASE_PS_PARAM} {0}
set_instance_parameter_value DDR3_IP {PLL_WRITE_CLK_PHASE_PS_SIM_STR_PARAM} {}
set_instance_parameter_value DDR3_IP {POWER_OF_TWO_BUS} {0}
set_instance_parameter_value DDR3_IP {PRIORITY_PORT} {1 1 1 1 1 1}
set_instance_parameter_value DDR3_IP {RATE} {Half}
set_instance_parameter_value DDR3_IP {RDIMM_CONFIG} {0000000000000000}
set_instance_parameter_value DDR3_IP {READ_DQ_DQS_CLOCK_SOURCE} {INVERTED_DQS_BUS}
set_instance_parameter_value DDR3_IP {READ_FIFO_SIZE} {8}
set_instance_parameter_value DDR3_IP {REFRESH_BURST_VALIDATION} {0}
set_instance_parameter_value DDR3_IP {REFRESH_INTERVAL} {15000}
set_instance_parameter_value DDR3_IP {REF_CLK_FREQ} {50.0}
set_instance_parameter_value DDR3_IP {REF_CLK_FREQ_MAX_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {REF_CLK_FREQ_MIN_PARAM} {0.0}
set_instance_parameter_value DDR3_IP {REF_CLK_FREQ_PARAM_VALID} {0}
set_instance_parameter_value DDR3_IP {SEQUENCER_TYPE} {NIOS}
set_instance_parameter_value DDR3_IP {SEQ_MODE} {0}
set_instance_parameter_value DDR3_IP {SKIP_MEM_INIT} {1}
set_instance_parameter_value DDR3_IP {SOPC_COMPAT_RESET} {0}
set_instance_parameter_value DDR3_IP {SPEED_GRADE} {2}
set_instance_parameter_value DDR3_IP {STARVE_LIMIT} {10}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_AC_EYE_REDUCTION_H} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_AC_EYE_REDUCTION_SU} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_AC_SKEW} {0.02}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_AC_SLEW_RATE} {1.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_AC_TO_CK_SKEW} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_CK_CKN_SLEW_RATE} {2.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_DELTA_DQS_ARRIVAL_TIME} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_DELTA_READ_DQS_ARRIVAL_TIME} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_DERATE_METHOD} {AUTO}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_DQS_DQSN_SLEW_RATE} {2.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_DQ_EYE_REDUCTION} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_DQ_SLEW_RATE} {1.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_DQ_TO_DQS_SKEW} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_ISI_METHOD} {AUTO}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_MAX_CK_DELAY} {0.6}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_MAX_DQS_DELAY} {0.6}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_READ_DQ_EYE_REDUCTION} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_SKEW_BETWEEN_DIMMS} {0.05}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_SKEW_BETWEEN_DQS} {0.02}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_SKEW_CKDQS_DIMM_MAX} {0.01}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_SKEW_CKDQS_DIMM_MIN} {-0.01}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_SKEW_WITHIN_DQS} {0.02}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_TDH} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_TDS} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_TIH} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_BOARD_TIS} {0.0}
set_instance_parameter_value DDR3_IP {TIMING_TDH} {100}
set_instance_parameter_value DDR3_IP {TIMING_TDQSCK} {300}
set_instance_parameter_value DDR3_IP {TIMING_TDQSCKDL} {1200}
set_instance_parameter_value DDR3_IP {TIMING_TDQSCKDM} {900}
set_instance_parameter_value DDR3_IP {TIMING_TDQSCKDS} {450}
set_instance_parameter_value DDR3_IP {TIMING_TDQSQ} {150}
set_instance_parameter_value DDR3_IP {TIMING_TDQSS} {0.25}
set_instance_parameter_value DDR3_IP {TIMING_TDS} {25}
set_instance_parameter_value DDR3_IP {TIMING_TDSH} {0.2}
set_instance_parameter_value DDR3_IP {TIMING_TDSS} {0.2}
set_instance_parameter_value DDR3_IP {TIMING_TIH} {200}
set_instance_parameter_value DDR3_IP {TIMING_TIS} {125}
set_instance_parameter_value DDR3_IP {TIMING_TQH} {0.38}
set_instance_parameter_value DDR3_IP {TIMING_TQSH} {0.38}
set_instance_parameter_value DDR3_IP {TRACKING_ERROR_TEST} {0}
set_instance_parameter_value DDR3_IP {TRACKING_WATCH_TEST} {0}
set_instance_parameter_value DDR3_IP {TREFI} {35100}
set_instance_parameter_value DDR3_IP {TRFC} {350}
set_instance_parameter_value DDR3_IP {USER_DEBUG_LEVEL} {1}
set_instance_parameter_value DDR3_IP {USE_AXI_ADAPTOR} {0}
set_instance_parameter_value DDR3_IP {USE_FAKE_PHY} {0}
set_instance_parameter_value DDR3_IP {USE_MEM_CLK_FREQ} {0}
set_instance_parameter_value DDR3_IP {USE_MM_ADAPTOR} {1}
set_instance_parameter_value DDR3_IP {USE_SEQUENCER_BFM} {0}
set_instance_parameter_value DDR3_IP {WEIGHT_PORT} {0 0 0 0 0 0}
set_instance_parameter_value DDR3_IP {WRBUFFER_ADDR_WIDTH} {6}

add_instance DDR3_refclk clock_source 20.1
set_instance_parameter_value DDR3_refclk {clockFrequency} {50000000.0}
set_instance_parameter_value DDR3_refclk {clockFrequencyKnown} {1}
set_instance_parameter_value DDR3_refclk {resetSynchronousEdges} {NONE}

add_instance QSys_PLL altpll 20.1
set_instance_parameter_value QSys_PLL {AVALON_USE_SEPARATE_SYSCLK} {NO}
set_instance_parameter_value QSys_PLL {BANDWIDTH} {}
set_instance_parameter_value QSys_PLL {BANDWIDTH_TYPE} {AUTO}
set_instance_parameter_value QSys_PLL {CLK0_DIVIDE_BY} {1}
set_instance_parameter_value QSys_PLL {CLK0_DUTY_CYCLE} {50}
set_instance_parameter_value QSys_PLL {CLK0_MULTIPLY_BY} {1}
set_instance_parameter_value QSys_PLL {CLK0_PHASE_SHIFT} {0}
set_instance_parameter_value QSys_PLL {CLK1_DIVIDE_BY} {25}
set_instance_parameter_value QSys_PLL {CLK1_DUTY_CYCLE} {50}
set_instance_parameter_value QSys_PLL {CLK1_MULTIPLY_BY} {24}
set_instance_parameter_value QSys_PLL {CLK1_PHASE_SHIFT} {0}
set_instance_parameter_value QSys_PLL {CLK2_DIVIDE_BY} {1}
set_instance_parameter_value QSys_PLL {CLK2_DUTY_CYCLE} {50}
set_instance_parameter_value QSys_PLL {CLK2_MULTIPLY_BY} {2}
set_instance_parameter_value QSys_PLL {CLK2_PHASE_SHIFT} {0}
set_instance_parameter_value QSys_PLL {CLK3_DIVIDE_BY} {1}
set_instance_parameter_value QSys_PLL {CLK3_DUTY_CYCLE} {50}
set_instance_parameter_value QSys_PLL {CLK3_MULTIPLY_BY} {2}
set_instance_parameter_value QSys_PLL {CLK3_PHASE_SHIFT} {0}
set_instance_parameter_value QSys_PLL {CLK4_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {CLK4_DUTY_CYCLE} {}
set_instance_parameter_value QSys_PLL {CLK4_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {CLK4_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {CLK5_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {CLK5_DUTY_CYCLE} {}
set_instance_parameter_value QSys_PLL {CLK5_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {CLK5_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {CLK6_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {CLK6_DUTY_CYCLE} {}
set_instance_parameter_value QSys_PLL {CLK6_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {CLK6_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {CLK7_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {CLK7_DUTY_CYCLE} {}
set_instance_parameter_value QSys_PLL {CLK7_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {CLK7_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {CLK8_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {CLK8_DUTY_CYCLE} {}
set_instance_parameter_value QSys_PLL {CLK8_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {CLK8_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {CLK9_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {CLK9_DUTY_CYCLE} {}
set_instance_parameter_value QSys_PLL {CLK9_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {CLK9_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {COMPENSATE_CLOCK} {CLK0}
set_instance_parameter_value QSys_PLL {DOWN_SPREAD} {}
set_instance_parameter_value QSys_PLL {DPA_DIVIDER} {}
set_instance_parameter_value QSys_PLL {DPA_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {DPA_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {ENABLE_SWITCH_OVER_COUNTER} {}
set_instance_parameter_value QSys_PLL {EXTCLK0_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {EXTCLK0_DUTY_CYCLE} {}
set_instance_parameter_value QSys_PLL {EXTCLK0_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {EXTCLK0_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {EXTCLK1_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {EXTCLK1_DUTY_CYCLE} {}
set_instance_parameter_value QSys_PLL {EXTCLK1_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {EXTCLK1_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {EXTCLK2_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {EXTCLK2_DUTY_CYCLE} {}
set_instance_parameter_value QSys_PLL {EXTCLK2_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {EXTCLK2_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {EXTCLK3_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {EXTCLK3_DUTY_CYCLE} {}
set_instance_parameter_value QSys_PLL {EXTCLK3_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {EXTCLK3_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {FEEDBACK_SOURCE} {}
set_instance_parameter_value QSys_PLL {GATE_LOCK_COUNTER} {}
set_instance_parameter_value QSys_PLL {GATE_LOCK_SIGNAL} {}
set_instance_parameter_value QSys_PLL {HIDDEN_CONSTANTS} {CT#CLK2_DIVIDE_BY 1 CT#PORT_clk9 PORT_UNUSED CT#PORT_clk8 PORT_UNUSED CT#PORT_clk7 PORT_UNUSED CT#PORT_clk6 PORT_UNUSED CT#PORT_clk5 PORT_UNUSED CT#PORT_clk4 PORT_UNUSED CT#PORT_clk3 PORT_USED CT#PORT_clk2 PORT_USED CT#PORT_clk1 PORT_USED CT#PORT_clk0 PORT_USED CT#CLK0_MULTIPLY_BY 1 CT#PORT_SCANWRITE PORT_UNUSED CT#PORT_SCANACLR PORT_UNUSED CT#PORT_PFDENA PORT_UNUSED CT#CLK3_DUTY_CYCLE 50 CT#CLK3_DIVIDE_BY 1 CT#PORT_PLLENA PORT_UNUSED CT#PORT_SCANDATA PORT_UNUSED CT#CLK3_PHASE_SHIFT 0 CT#PORT_SCANCLKENA PORT_UNUSED CT#WIDTH_CLOCK 10 CT#PORT_SCANDATAOUT PORT_UNUSED CT#LPM_TYPE altpll CT#PLL_TYPE AUTO CT#CLK0_PHASE_SHIFT 0 CT#CLK1_DUTY_CYCLE 50 CT#PORT_PHASEDONE PORT_UNUSED CT#OPERATION_MODE NORMAL CT#PORT_CONFIGUPDATE PORT_UNUSED CT#CLK1_MULTIPLY_BY 24 CT#COMPENSATE_CLOCK CLK0 CT#PORT_CLKSWITCH PORT_UNUSED CT#INCLK0_INPUT_FREQUENCY 20000 CT#PORT_SCANDONE PORT_UNUSED CT#PORT_CLKLOSS PORT_UNUSED CT#PORT_INCLK1 PORT_UNUSED CT#AVALON_USE_SEPARATE_SYSCLK NO CT#PORT_INCLK0 PORT_USED CT#PORT_clkena5 PORT_UNUSED CT#PORT_clkena4 PORT_UNUSED CT#PORT_clkena3 PORT_UNUSED CT#PORT_clkena2 PORT_UNUSED CT#PORT_clkena1 PORT_UNUSED CT#PORT_FBOUT PORT_UNUSED CT#PORT_clkena0 PORT_UNUSED CT#CLK1_PHASE_SHIFT 0 CT#PORT_ARESET PORT_USED CT#BANDWIDTH_TYPE AUTO CT#CLK2_MULTIPLY_BY 2 CT#INTENDED_DEVICE_FAMILY {Stratix IV} CT#PORT_SCANREAD PORT_UNUSED CT#CLK2_DUTY_CYCLE 50 CT#PORT_PHASESTEP PORT_UNUSED CT#PORT_SCANCLK PORT_UNUSED CT#PORT_CLKBAD1 PORT_UNUSED CT#PORT_CLKBAD0 PORT_UNUSED CT#PORT_FBIN PORT_UNUSED CT#PORT_PHASEUPDOWN PORT_UNUSED CT#PORT_PHASECOUNTERSELECT PORT_UNUSED CT#PORT_ACTIVECLOCK PORT_UNUSED CT#CLK2_PHASE_SHIFT 0 CT#CLK0_DUTY_CYCLE 50 CT#CLK0_DIVIDE_BY 1 CT#CLK1_DIVIDE_BY 25 CT#CLK3_MULTIPLY_BY 2 CT#USING_FBMIMICBIDIR_PORT OFF CT#PORT_LOCKED PORT_USED}
set_instance_parameter_value QSys_PLL {HIDDEN_CUSTOM_ELABORATION} {altpll_avalon_elaboration}
set_instance_parameter_value QSys_PLL {HIDDEN_CUSTOM_POST_EDIT} {altpll_avalon_post_edit}
set_instance_parameter_value QSys_PLL {HIDDEN_IF_PORTS} {IF#phasecounterselect {input 4} IF#locked {output 0} IF#reset {input 0} IF#clk {input 0} IF#phaseupdown {input 0} IF#scandone {output 0} IF#readdata {output 32} IF#write {input 0} IF#scanclk {input 0} IF#phasedone {output 0} IF#c3 {output 0} IF#address {input 2} IF#c2 {output 0} IF#c1 {output 0} IF#c0 {output 0} IF#writedata {input 32} IF#read {input 0} IF#areset {input 0} IF#scanclkena {input 0} IF#scandataout {output 0} IF#configupdate {input 0} IF#phasestep {input 0} IF#scandata {input 0}}
set_instance_parameter_value QSys_PLL {HIDDEN_IS_FIRST_EDIT} {0}
set_instance_parameter_value QSys_PLL {HIDDEN_IS_NUMERIC} {IN#WIDTH_CLOCK 1 IN#CLK0_DUTY_CYCLE 1 IN#CLK2_DIVIDE_BY 1 IN#PLL_TARGET_HARCOPY_CHECK 1 IN#CLK3_DIVIDE_BY 1 IN#CLK1_MULTIPLY_BY 1 IN#CLK3_DUTY_CYCLE 1 IN#SWITCHOVER_COUNT_EDIT 1 IN#INCLK0_INPUT_FREQUENCY 1 IN#PLL_LVDS_PLL_CHECK 1 IN#PLL_AUTOPLL_CHECK 1 IN#PLL_FASTPLL_CHECK 1 IN#CLK1_DUTY_CYCLE 1 IN#PLL_ENHPLL_CHECK 1 IN#CLK2_MULTIPLY_BY 1 IN#DIV_FACTOR3 1 IN#DIV_FACTOR2 1 IN#DIV_FACTOR1 1 IN#DIV_FACTOR0 1 IN#LVDS_MODE_DATA_RATE_DIRTY 1 IN#GLOCK_COUNTER_EDIT 1 IN#CLK2_DUTY_CYCLE 1 IN#CLK0_DIVIDE_BY 1 IN#CLK3_MULTIPLY_BY 1 IN#MULT_FACTOR3 1 IN#MULT_FACTOR2 1 IN#MULT_FACTOR1 1 IN#MULT_FACTOR0 1 IN#CLK0_MULTIPLY_BY 1 IN#USE_MIL_SPEED_GRADE 1 IN#CLK1_DIVIDE_BY 1}
set_instance_parameter_value QSys_PLL {HIDDEN_MF_PORTS} {MF#areset 1 MF#clk 1 MF#locked 1 MF#inclk 1}
set_instance_parameter_value QSys_PLL {HIDDEN_PRIVATES} {PT#GLOCKED_FEATURE_ENABLED 0 PT#SPREAD_FEATURE_ENABLED 0 PT#BANDWIDTH_FREQ_UNIT MHz PT#CUR_DEDICATED_CLK c0 PT#INCLK0_FREQ_EDIT 50.000 PT#BANDWIDTH_PRESET Low PT#PLL_LVDS_PLL_CHECK 0 PT#BANDWIDTH_USE_PRESET 0 PT#AVALON_USE_SEPARATE_SYSCLK NO PT#OUTPUT_FREQ_UNIT3 MHz PT#PLL_ENHPLL_CHECK 0 PT#OUTPUT_FREQ_UNIT2 MHz PT#OUTPUT_FREQ_UNIT1 MHz PT#OUTPUT_FREQ_UNIT0 MHz PT#PHASE_RECONFIG_FEATURE_ENABLED 1 PT#CREATE_CLKBAD_CHECK 0 PT#CLKSWITCH_CHECK 0 PT#INCLK1_FREQ_EDIT 100.000 PT#NORMAL_MODE_RADIO 1 PT#SRC_SYNCH_COMP_RADIO 0 PT#PLL_ARESET_CHECK 1 PT#LONG_SCAN_RADIO 1 PT#SCAN_FEATURE_ENABLED 1 PT#USE_CLK3 1 PT#USE_CLK2 1 PT#PHASE_RECONFIG_INPUTS_CHECK 0 PT#USE_CLK1 1 PT#USE_CLK0 1 PT#PRIMARY_CLK_COMBO inclk0 PT#BANDWIDTH 1.000 PT#GLOCKED_COUNTER_EDIT_CHANGED 1 PT#PLL_FASTPLL_CHECK 0 PT#SPREAD_FREQ_UNIT KHz PT#LVDS_PHASE_SHIFT_UNIT3 deg PT#PLL_AUTOPLL_CHECK 1 PT#LVDS_PHASE_SHIFT_UNIT2 deg PT#OUTPUT_FREQ_MODE3 1 PT#LVDS_PHASE_SHIFT_UNIT1 deg PT#OUTPUT_FREQ_MODE2 1 PT#LVDS_PHASE_SHIFT_UNIT0 deg PT#OUTPUT_FREQ_MODE1 1 PT#SWITCHOVER_FEATURE_ENABLED 0 PT#MIG_DEVICE_SPEED_GRADE Any PT#OUTPUT_FREQ_MODE0 1 PT#BANDWIDTH_FEATURE_ENABLED 1 PT#INCLK0_FREQ_UNIT_COMBO MHz PT#ZERO_DELAY_RADIO 0 PT#OUTPUT_FREQ3 100.00000000 PT#OUTPUT_FREQ2 100.00000000 PT#OUTPUT_FREQ1 48.00000000 PT#OUTPUT_FREQ0 50.00000000 PT#SHORT_SCAN_RADIO 0 PT#LVDS_MODE_DATA_RATE_DIRTY 0 PT#CUR_FBIN_CLK c0 PT#PLL_ADVANCED_PARAM_CHECK 0 PT#CLKBAD_SWITCHOVER_CHECK 0 PT#PHASE_SHIFT_STEP_ENABLED_CHECK 0 PT#DEVICE_SPEED_GRADE Any PT#PLL_FBMIMIC_CHECK 0 PT#LVDS_MODE_DATA_RATE {Not Available} PT#LOCKED_OUTPUT_CHECK 1 PT#SPREAD_PERCENT 0.500 PT#PHASE_SHIFT3 0.00000000 PT#PHASE_SHIFT2 0.00000000 PT#DIV_FACTOR3 1 PT#PHASE_SHIFT1 0.00000000 PT#DIV_FACTOR2 1 PT#PHASE_SHIFT0 0.00000000 PT#DIV_FACTOR1 1 PT#DIV_FACTOR0 1 PT#CNX_NO_COMPENSATE_RADIO 0 PT#CREATE_INCLK1_CHECK 0 PT#GLOCK_COUNTER_EDIT 1048575 PT#INCLK1_FREQ_UNIT_COMBO MHz PT#EFF_OUTPUT_FREQ_VALUE3 100.000000 PT#EFF_OUTPUT_FREQ_VALUE2 100.000000 PT#EFF_OUTPUT_FREQ_VALUE1 48.000000 PT#EFF_OUTPUT_FREQ_VALUE0 50.000000 PT#SPREAD_FREQ 50.000 PT#USE_MIL_SPEED_GRADE 0 PT#EXPLICIT_SWITCHOVER_COUNTER 0 PT#STICKY_CLK3 1 PT#STICKY_CLK2 1 PT#STICKY_CLK1 1 PT#STICKY_CLK0 1 PT#EXT_FEEDBACK_RADIO 0 PT#SWITCHOVER_COUNT_EDIT 1 PT#SELF_RESET_LOCK_LOSS 0 PT#PLL_PFDENA_CHECK 0 PT#INT_FEEDBACK__MODE_RADIO 1 PT#INCLK1_FREQ_EDIT_CHANGED 1 PT#SYNTH_WRAPPER_GEN_POSTFIX 0 PT#CLKLOSS_CHECK 0 PT#PHASE_SHIFT_UNIT3 deg PT#PHASE_SHIFT_UNIT2 deg PT#PHASE_SHIFT_UNIT1 deg PT#PHASE_SHIFT_UNIT0 deg PT#BANDWIDTH_USE_AUTO 1 PT#HAS_MANUAL_SWITCHOVER 1 PT#MULT_FACTOR3 1 PT#MULT_FACTOR2 1 PT#MULT_FACTOR1 1 PT#MULT_FACTOR0 1 PT#SPREAD_USE 0 PT#GLOCKED_MODE_CHECK 0 PT#DUTY_CYCLE3 50.00000000 PT#DUTY_CYCLE2 50.00000000 PT#SACN_INPUTS_CHECK 0 PT#DUTY_CYCLE1 50.00000000 PT#INTENDED_DEVICE_FAMILY {Stratix IV} PT#DUTY_CYCLE0 50.00000000 PT#PLL_TARGET_HARCOPY_CHECK 0 PT#INCLK1_FREQ_UNIT_CHANGED 1 PT#RECONFIG_FILE ALTPLL1587635788856559.mif PT#ACTIVECLK_CHECK 0}
set_instance_parameter_value QSys_PLL {HIDDEN_USED_PORTS} {UP#locked used UP#c3 used UP#c2 used UP#c1 used UP#c0 used UP#areset used UP#inclk0 used}
set_instance_parameter_value QSys_PLL {INCLK0_INPUT_FREQUENCY} {20000}
set_instance_parameter_value QSys_PLL {INCLK1_INPUT_FREQUENCY} {}
set_instance_parameter_value QSys_PLL {INTENDED_DEVICE_FAMILY} {Stratix IV}
set_instance_parameter_value QSys_PLL {INVALID_LOCK_MULTIPLIER} {}
set_instance_parameter_value QSys_PLL {LOCK_HIGH} {}
set_instance_parameter_value QSys_PLL {LOCK_LOW} {}
set_instance_parameter_value QSys_PLL {OPERATION_MODE} {NORMAL}
set_instance_parameter_value QSys_PLL {PLL_TYPE} {AUTO}
set_instance_parameter_value QSys_PLL {PORT_ACTIVECLOCK} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_ARESET} {PORT_USED}
set_instance_parameter_value QSys_PLL {PORT_CLKBAD0} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_CLKBAD1} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_CLKLOSS} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_CLKSWITCH} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_CONFIGUPDATE} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_ENABLE0} {}
set_instance_parameter_value QSys_PLL {PORT_ENABLE1} {}
set_instance_parameter_value QSys_PLL {PORT_FBIN} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_FBOUT} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_INCLK0} {PORT_USED}
set_instance_parameter_value QSys_PLL {PORT_INCLK1} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_LOCKED} {PORT_USED}
set_instance_parameter_value QSys_PLL {PORT_PFDENA} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_PHASECOUNTERSELECT} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_PHASEDONE} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_PHASESTEP} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_PHASEUPDOWN} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_PLLENA} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_SCANACLR} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_SCANCLK} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_SCANCLKENA} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_SCANDATA} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_SCANDATAOUT} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_SCANDONE} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_SCANREAD} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_SCANWRITE} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_SCLKOUT0} {}
set_instance_parameter_value QSys_PLL {PORT_SCLKOUT1} {}
set_instance_parameter_value QSys_PLL {PORT_VCOOVERRANGE} {}
set_instance_parameter_value QSys_PLL {PORT_VCOUNDERRANGE} {}
set_instance_parameter_value QSys_PLL {PORT_clk0} {PORT_USED}
set_instance_parameter_value QSys_PLL {PORT_clk1} {PORT_USED}
set_instance_parameter_value QSys_PLL {PORT_clk2} {PORT_USED}
set_instance_parameter_value QSys_PLL {PORT_clk3} {PORT_USED}
set_instance_parameter_value QSys_PLL {PORT_clk4} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clk5} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clk6} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clk7} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clk8} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clk9} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clkena0} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clkena1} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clkena2} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clkena3} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clkena4} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_clkena5} {PORT_UNUSED}
set_instance_parameter_value QSys_PLL {PORT_extclk0} {}
set_instance_parameter_value QSys_PLL {PORT_extclk1} {}
set_instance_parameter_value QSys_PLL {PORT_extclk2} {}
set_instance_parameter_value QSys_PLL {PORT_extclk3} {}
set_instance_parameter_value QSys_PLL {PORT_extclkena0} {}
set_instance_parameter_value QSys_PLL {PORT_extclkena1} {}
set_instance_parameter_value QSys_PLL {PORT_extclkena2} {}
set_instance_parameter_value QSys_PLL {PORT_extclkena3} {}
set_instance_parameter_value QSys_PLL {PRIMARY_CLOCK} {}
set_instance_parameter_value QSys_PLL {QUALIFY_CONF_DONE} {}
set_instance_parameter_value QSys_PLL {SCAN_CHAIN} {}
set_instance_parameter_value QSys_PLL {SCAN_CHAIN_MIF_FILE} {}
set_instance_parameter_value QSys_PLL {SCLKOUT0_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {SCLKOUT1_PHASE_SHIFT} {}
set_instance_parameter_value QSys_PLL {SELF_RESET_ON_GATED_LOSS_LOCK} {}
set_instance_parameter_value QSys_PLL {SELF_RESET_ON_LOSS_LOCK} {}
set_instance_parameter_value QSys_PLL {SKIP_VCO} {}
set_instance_parameter_value QSys_PLL {SPREAD_FREQUENCY} {}
set_instance_parameter_value QSys_PLL {SWITCH_OVER_COUNTER} {}
set_instance_parameter_value QSys_PLL {SWITCH_OVER_ON_GATED_LOCK} {}
set_instance_parameter_value QSys_PLL {SWITCH_OVER_ON_LOSSCLK} {}
set_instance_parameter_value QSys_PLL {SWITCH_OVER_TYPE} {}
set_instance_parameter_value QSys_PLL {USING_FBMIMICBIDIR_PORT} {OFF}
set_instance_parameter_value QSys_PLL {VALID_LOCK_MULTIPLIER} {}
set_instance_parameter_value QSys_PLL {VCO_DIVIDE_BY} {}
set_instance_parameter_value QSys_PLL {VCO_FREQUENCY_CONTROL} {}
set_instance_parameter_value QSys_PLL {VCO_MULTIPLY_BY} {}
set_instance_parameter_value QSys_PLL {VCO_PHASE_SHIFT_STEP} {}
set_instance_parameter_value QSys_PLL {WIDTH_CLOCK} {10}
set_instance_parameter_value QSys_PLL {WIDTH_PHASECOUNTERSELECT} {}

add_instance QSys_clk clock_source 20.1
set_instance_parameter_value QSys_clk {clockFrequency} {50000000.0}
set_instance_parameter_value QSys_clk {clockFrequencyKnown} {1}
set_instance_parameter_value QSys_clk {resetSynchronousEdges} {NONE}

add_instance QSys_refclk clock_source 20.1
set_instance_parameter_value QSys_refclk {clockFrequency} {50000000.0}
set_instance_parameter_value QSys_refclk {clockFrequencyKnown} {1}
set_instance_parameter_value QSys_refclk {resetSynchronousEdges} {NONE}

# exported interfaces
add_interface axi4 altera_axi4 slave
set_interface_property axi4 EXPORT_OF AXI_Bridge.s0
add_interface ddr_ref clock sink
set_interface_property ddr_ref EXPORT_OF DDR3_refclk.clk_in
add_interface io clock source
set_interface_property io EXPORT_OF QSys_PLL.c2
add_interface locked conduit end
set_interface_property locked EXPORT_OF QSys_PLL.locked_conduit
add_interface mem_if_ddr3_emif_0_pll_sharing conduit end
set_interface_property mem_if_ddr3_emif_0_pll_sharing EXPORT_OF DDR3_IP.pll_sharing
add_interface mem_status conduit end
set_interface_property mem_status EXPORT_OF DDR3_IP.status
add_interface memory conduit end
set_interface_property memory EXPORT_OF DDR3_IP.memory
add_interface oct conduit end
set_interface_property oct EXPORT_OF DDR3_IP.oct
add_interface qsys clock source
set_interface_property qsys EXPORT_OF QSys_clk.clk
add_interface qsys_ref clock sink
set_interface_property qsys_ref EXPORT_OF QSys_refclk.clk_in
add_interface system reset sink
set_interface_property system EXPORT_OF QSys_refclk.clk_in_reset
add_interface usb clock source
set_interface_property usb EXPORT_OF QSys_PLL.c1

# connections and connection parameters
add_connection AXI_Bridge.m0 AvalonMM_ClockCrossing.s0
set_connection_parameter_value AXI_Bridge.m0/AvalonMM_ClockCrossing.s0 arbitrationPriority {1}
set_connection_parameter_value AXI_Bridge.m0/AvalonMM_ClockCrossing.s0 baseAddress {0x0000}
set_connection_parameter_value AXI_Bridge.m0/AvalonMM_ClockCrossing.s0 defaultConnection {0}

add_connection AvalonMM_ClockCrossing.m0 DDR3_IP.avl
set_connection_parameter_value AvalonMM_ClockCrossing.m0/DDR3_IP.avl arbitrationPriority {1}
set_connection_parameter_value AvalonMM_ClockCrossing.m0/DDR3_IP.avl baseAddress {0x0000}
set_connection_parameter_value AvalonMM_ClockCrossing.m0/DDR3_IP.avl defaultConnection {0}

add_connection DDR3_IP.afi_clk AvalonMM_ClockCrossing.m0_clk

add_connection DDR3_refclk.clk DDR3_IP.pll_ref_clk

add_connection QSys_PLL.c3 AXI_Bridge.clk

add_connection QSys_PLL.c3 AvalonMM_ClockCrossing.s0_clk

add_connection QSys_PLL.c3 QSys_clk.clk_in

add_connection QSys_refclk.clk QSys_PLL.inclk_interface

add_connection QSys_refclk.clk_reset AXI_Bridge.clk_reset

add_connection QSys_refclk.clk_reset AvalonMM_ClockCrossing.m0_reset

add_connection QSys_refclk.clk_reset AvalonMM_ClockCrossing.s0_reset

add_connection QSys_refclk.clk_reset DDR3_IP.global_reset

add_connection QSys_refclk.clk_reset DDR3_IP.soft_reset

add_connection QSys_refclk.clk_reset DDR3_refclk.clk_in_reset

add_connection QSys_refclk.clk_reset QSys_PLL.inclk_interface_reset

add_connection QSys_refclk.clk_reset QSys_clk.clk_in_reset

# interconnect requirements
set_interconnect_requirement {$system} {qsys_mm.clockCrossingAdapter} {HANDSHAKE}
set_interconnect_requirement {$system} {qsys_mm.enableEccProtection} {FALSE}
set_interconnect_requirement {$system} {qsys_mm.insertDefaultSlave} {FALSE}
set_interconnect_requirement {$system} {qsys_mm.maxAdditionalLatency} {1}

save_system {main.qsys}
