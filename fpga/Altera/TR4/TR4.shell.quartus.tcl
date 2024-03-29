#============================================================
# FAN
#============================================================
set_instance_assignment -name IO_STANDARD 1.5V -to FAN_CTRL
set_location_assignment PIN_B17 -to FAN_CTRL
#============================================================
# BUTTON
#============================================================
set_instance_assignment -name IO_STANDARD 1.5V -to BUTTON[0]
set_instance_assignment -name IO_STANDARD 1.5V -to BUTTON[1]
set_instance_assignment -name IO_STANDARD 1.5V -to BUTTON[2]
set_instance_assignment -name IO_STANDARD 1.5V -to BUTTON[3]
set_location_assignment PIN_L19 -to BUTTON[0]
set_location_assignment PIN_M19 -to BUTTON[1]
set_location_assignment PIN_A19 -to BUTTON[2]
set_location_assignment PIN_P20 -to BUTTON[3]
#============================================================
# SW
#============================================================
set_instance_assignment -name IO_STANDARD 2.5V -to SW_0
set_instance_assignment -name IO_STANDARD 2.5V -to SW_1
set_instance_assignment -name IO_STANDARD 2.5V -to SW_2
set_instance_assignment -name IO_STANDARD 2.5V -to SW_3
set_location_assignment PIN_AH18 -to SW_0
set_location_assignment PIN_AH19 -to SW_1
set_location_assignment PIN_D6 -to SW_2
set_location_assignment PIN_C6 -to SW_3
#============================================================
# LED
#============================================================
set_instance_assignment -name IO_STANDARD 1.5V -to LED_0
set_instance_assignment -name IO_STANDARD 1.5V -to LED_1
set_instance_assignment -name IO_STANDARD 1.5V -to LED_2
set_instance_assignment -name IO_STANDARD 1.5V -to LED_3
set_location_assignment PIN_B19 -to LED_0
set_location_assignment PIN_A18 -to LED_1
set_location_assignment PIN_D19 -to LED_2
set_location_assignment PIN_C19 -to LED_3
#============================================================
# OSC
#============================================================
set_instance_assignment -name IO_STANDARD 1.5V -to LOOP_CLKIN0
set_instance_assignment -name IO_STANDARD 1.5V -to LOOP_CLKOUT0
set_instance_assignment -name IO_STANDARD 1.5V -to LOOP_CLKIN1
set_instance_assignment -name IO_STANDARD 1.5V -to LOOP_CLKOUT1
set_location_assignment PIN_B22 -to LOOP_CLKIN0
set_location_assignment PIN_M20 -to LOOP_CLKOUT0
set_location_assignment PIN_B20 -to LOOP_CLKIN1
set_location_assignment PIN_L20 -to LOOP_CLKOUT1
set_instance_assignment -name IO_STANDARD 2.5V -to OSC_50_BANK1
set_instance_assignment -name IO_STANDARD 2.5V -to OSC_50_BANK3
set_instance_assignment -name IO_STANDARD 2.5V -to OSC_50_BANK4
set_instance_assignment -name IO_STANDARD 1.5V -to OSC_50_BANK7
set_instance_assignment -name IO_STANDARD 1.5V -to OSC_50_BANK8
set_location_assignment PIN_AB34 -to OSC_50_BANK1
set_location_assignment PIN_AW22 -to OSC_50_BANK3
set_location_assignment PIN_AV19 -to OSC_50_BANK4
set_location_assignment PIN_A21 -to OSC_50_BANK7
set_location_assignment PIN_B23 -to OSC_50_BANK8
#============================================================
# FLASH
#============================================================
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FLASH_ADV_n
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FLASH_CE_n
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FLASH_CLK
set_instance_assignment -name IO_STANDARD 1.5V -to FLASH_RDY_BSY_n
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FLASH_RESET_n
set_instance_assignment -name IO_STANDARD 1.5V -to FLASH_WP_n
set_location_assignment PIN_AT15 -to FLASH_ADV_n
set_location_assignment PIN_AP16 -to FLASH_CE_n
set_location_assignment PIN_AU15 -to FLASH_CLK
set_location_assignment PIN_A23 -to FLASH_RDY_BSY_n
set_location_assignment PIN_AV16 -to FLASH_RESET_n
set_location_assignment PIN_A20 -to FLASH_WP_n
#============================================================
# FSM
#============================================================
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[1]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[2]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[3]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[4]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[5]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[6]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[7]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[8]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[9]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[10]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[11]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[12]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[13]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[14]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[15]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[16]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[17]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[18]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[19]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[20]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[21]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[22]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[23]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[24]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_A[25]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[0]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[1]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[2]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[3]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[4]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[5]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[6]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[7]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[8]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[9]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[10]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[11]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[12]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[13]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[14]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[15]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[16]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[17]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[18]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[19]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[20]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[21]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[22]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[23]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[24]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[25]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[26]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[27]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[28]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[29]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[30]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_D[31]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_OE_n
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to FSM_WE_n
set_location_assignment PIN_L31 -to FSM_A[1]
set_location_assignment PIN_F34 -to FSM_A[2]
set_location_assignment PIN_D35 -to FSM_A[3]
set_location_assignment PIN_D34 -to FSM_A[4]
set_location_assignment PIN_E34 -to FSM_A[5]
set_location_assignment PIN_C35 -to FSM_A[6]
set_location_assignment PIN_C34 -to FSM_A[7]
set_location_assignment PIN_F33 -to FSM_A[8]
set_location_assignment PIN_G35 -to FSM_A[9]
set_location_assignment PIN_H35 -to FSM_A[10]
set_location_assignment PIN_J32 -to FSM_A[11]
set_location_assignment PIN_J33 -to FSM_A[12]
set_location_assignment PIN_K32 -to FSM_A[13]
set_location_assignment PIN_K31 -to FSM_A[14]
set_location_assignment PIN_AH17 -to FSM_A[15]
set_location_assignment PIN_AH16 -to FSM_A[16]
set_location_assignment PIN_AE17 -to FSM_A[17]
set_location_assignment PIN_AG16 -to FSM_A[18]
set_location_assignment PIN_H32 -to FSM_A[19]
set_location_assignment PIN_H34 -to FSM_A[20]
set_location_assignment PIN_G33 -to FSM_A[21]
set_location_assignment PIN_F35 -to FSM_A[22]
set_location_assignment PIN_N31 -to FSM_A[23]
set_location_assignment PIN_M31 -to FSM_A[24]
set_location_assignment PIN_M30 -to FSM_A[25]
set_location_assignment PIN_B32 -to FSM_D[0]
set_location_assignment PIN_C32 -to FSM_D[1]
set_location_assignment PIN_C31 -to FSM_D[2]
set_location_assignment PIN_F32 -to FSM_D[3]
set_location_assignment PIN_J30 -to FSM_D[4]
set_location_assignment PIN_K29 -to FSM_D[5]
set_location_assignment PIN_K30 -to FSM_D[6]
set_location_assignment PIN_L29 -to FSM_D[7]
set_location_assignment PIN_M29 -to FSM_D[8]
set_location_assignment PIN_N29 -to FSM_D[9]
set_location_assignment PIN_P29 -to FSM_D[10]
set_location_assignment PIN_T27 -to FSM_D[11]
set_location_assignment PIN_AM17 -to FSM_D[12]
set_location_assignment PIN_AL17 -to FSM_D[13]
set_location_assignment PIN_AK16 -to FSM_D[14]
set_location_assignment PIN_AJ16 -to FSM_D[15]
set_location_assignment PIN_AK17 -to FSM_D[16]
set_location_assignment PIN_T28 -to FSM_D[17]
set_location_assignment PIN_R27 -to FSM_D[18]
set_location_assignment PIN_R28 -to FSM_D[19]
set_location_assignment PIN_R29 -to FSM_D[20]
set_location_assignment PIN_N30 -to FSM_D[21]
set_location_assignment PIN_N28 -to FSM_D[22]
set_location_assignment PIN_M28 -to FSM_D[23]
set_location_assignment PIN_H31 -to FSM_D[24]
set_location_assignment PIN_G31 -to FSM_D[25]
set_location_assignment PIN_D31 -to FSM_D[26]
set_location_assignment PIN_E31 -to FSM_D[27]
set_location_assignment PIN_F31 -to FSM_D[28]
set_location_assignment PIN_E32 -to FSM_D[29]
set_location_assignment PIN_C33 -to FSM_D[30]
set_location_assignment PIN_D33 -to FSM_D[31]
set_location_assignment PIN_AT16 -to FSM_OE_n
set_location_assignment PIN_AL16 -to FSM_WE_n
#============================================================
# HSMA
#============================================================
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_CLKIN0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_CLKIN_n1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_CLKIN_n2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_CLKIN_p1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_CLKIN_p2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_D_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_D_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_D_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_D_3
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_RX_p[0]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_RX_p[1]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_RX_p[2]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_RX_p[3]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_RX_p[4]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_RX_p[5]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_RX_p[6]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_RX_p[7]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_TX_p[0]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_TX_p[1]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_TX_p[2]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_TX_p[3]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_TX_p[4]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_TX_p[5]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_TX_p[6]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_GXB_TX_p[7]
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_OUT0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_OUT_n1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_OUT_n2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_OUT_p1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_OUT_p2
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSMA_REFCLK_p
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_n_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_RX_p_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_n_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMA_TX_p_16
set_location_assignment PIN_C10 -to HSMA_CLKIN0
set_location_assignment PIN_AE5 -to HSMA_CLKIN_n1
set_location_assignment PIN_AC5 -to HSMA_CLKIN_n2
set_location_assignment PIN_AF6 -to HSMA_CLKIN_p1
set_location_assignment PIN_AC6 -to HSMA_CLKIN_p2
set_location_assignment PIN_AK8 -to HSMA_D_0
set_location_assignment PIN_AP6 -to HSMA_D_1
set_location_assignment PIN_AK7 -to HSMA_D_2
set_location_assignment PIN_AP5 -to HSMA_D_3
set_location_assignment PIN_AE2 -to HSMA_GXB_RX_p[0]
set_location_assignment PIN_AC2 -to HSMA_GXB_RX_p[1]
set_location_assignment PIN_U2 -to HSMA_GXB_RX_p[2]
set_location_assignment PIN_R2 -to HSMA_GXB_RX_p[3]
set_location_assignment PIN_N2 -to HSMA_GXB_RX_p[4]
set_location_assignment PIN_L2 -to HSMA_GXB_RX_p[5]
set_location_assignment PIN_E2 -to HSMA_GXB_RX_p[6]
set_location_assignment PIN_C2 -to HSMA_GXB_RX_p[7]
set_location_assignment PIN_AD4 -to HSMA_GXB_TX_p[0]
set_location_assignment PIN_AB4 -to HSMA_GXB_TX_p[1]
set_location_assignment PIN_T4 -to HSMA_GXB_TX_p[2]
set_location_assignment PIN_P4 -to HSMA_GXB_TX_p[3]
set_location_assignment PIN_M4 -to HSMA_GXB_TX_p[4]
set_location_assignment PIN_K4 -to HSMA_GXB_TX_p[5]
set_location_assignment PIN_D4 -to HSMA_GXB_TX_p[6]
set_location_assignment PIN_B4 -to HSMA_GXB_TX_p[7]
set_location_assignment PIN_D10 -to HSMA_OUT0
set_location_assignment PIN_R11 -to HSMA_OUT_n1
set_location_assignment PIN_G10 -to HSMA_OUT_n2
set_location_assignment PIN_R12 -to HSMA_OUT_p1
set_location_assignment PIN_H10 -to HSMA_OUT_p2
set_location_assignment PIN_AA2 -to HSMA_REFCLK_p
set_location_assignment PIN_AN5 -to HSMA_RX_n_0
set_location_assignment PIN_AM5 -to HSMA_RX_n_1
set_location_assignment PIN_AL5 -to HSMA_RX_n_2
set_location_assignment PIN_AK5 -to HSMA_RX_n_3
set_location_assignment PIN_AJ5 -to HSMA_RX_n_4
set_location_assignment PIN_AH5 -to HSMA_RX_n_5
set_location_assignment PIN_AG5 -to HSMA_RX_n_6
set_location_assignment PIN_AC8 -to HSMA_RX_n_7
set_location_assignment PIN_E10 -to HSMA_RX_n_8
set_location_assignment PIN_F9 -to HSMA_RX_n_9
set_location_assignment PIN_C9 -to HSMA_RX_n_10
set_location_assignment PIN_F6 -to HSMA_RX_n_11
set_location_assignment PIN_F5 -to HSMA_RX_n_12
set_location_assignment PIN_E7 -to HSMA_RX_n_13
set_location_assignment PIN_C8 -to HSMA_RX_n_14
set_location_assignment PIN_C5 -to HSMA_RX_n_15
set_location_assignment PIN_C7 -to HSMA_RX_n_16
set_location_assignment PIN_AN6 -to HSMA_RX_p_0
set_location_assignment PIN_AM6 -to HSMA_RX_p_1
set_location_assignment PIN_AL6 -to HSMA_RX_p_2
set_location_assignment PIN_AK6 -to HSMA_RX_p_3
set_location_assignment PIN_AJ6 -to HSMA_RX_p_4
set_location_assignment PIN_AH6 -to HSMA_RX_p_5
set_location_assignment PIN_AG6 -to HSMA_RX_p_6
set_location_assignment PIN_AB9 -to HSMA_RX_p_7
set_location_assignment PIN_F10 -to HSMA_RX_p_8
set_location_assignment PIN_G9 -to HSMA_RX_p_9
set_location_assignment PIN_D9 -to HSMA_RX_p_10
set_location_assignment PIN_G6 -to HSMA_RX_p_11
set_location_assignment PIN_G5 -to HSMA_RX_p_12
set_location_assignment PIN_F7 -to HSMA_RX_p_13
set_location_assignment PIN_D8 -to HSMA_RX_p_14
set_location_assignment PIN_D5 -to HSMA_RX_p_15
set_location_assignment PIN_D7 -to HSMA_RX_p_16
set_location_assignment PIN_AG9 -to HSMA_TX_n_0
set_location_assignment PIN_AH8 -to HSMA_TX_n_1
set_location_assignment PIN_AG7 -to HSMA_TX_n_2
set_location_assignment PIN_AF10 -to HSMA_TX_n_3
set_location_assignment PIN_AD9 -to HSMA_TX_n_4
set_location_assignment PIN_AB12 -to HSMA_TX_n_5
set_location_assignment PIN_AB10 -to HSMA_TX_n_6
set_location_assignment PIN_T12 -to HSMA_TX_n_7
set_location_assignment PIN_P13 -to HSMA_TX_n_8
set_location_assignment PIN_N10 -to HSMA_TX_n_9
set_location_assignment PIN_M12 -to HSMA_TX_n_10
set_location_assignment PIN_L10 -to HSMA_TX_n_11
set_location_assignment PIN_L11 -to HSMA_TX_n_12
set_location_assignment PIN_J8 -to HSMA_TX_n_13
set_location_assignment PIN_J9 -to HSMA_TX_n_14
set_location_assignment PIN_G7 -to HSMA_TX_n_15
set_location_assignment PIN_J10 -to HSMA_TX_n_16
set_location_assignment PIN_AG10 -to HSMA_TX_p_0
set_location_assignment PIN_AH9 -to HSMA_TX_p_1
set_location_assignment PIN_AG8 -to HSMA_TX_p_2
set_location_assignment PIN_AF11 -to HSMA_TX_p_3
set_location_assignment PIN_AD10 -to HSMA_TX_p_4
set_location_assignment PIN_AB13 -to HSMA_TX_p_5
set_location_assignment PIN_AB11 -to HSMA_TX_p_6
set_location_assignment PIN_T13 -to HSMA_TX_p_7
set_location_assignment PIN_R13 -to HSMA_TX_p_8
set_location_assignment PIN_N11 -to HSMA_TX_p_9
set_location_assignment PIN_N12 -to HSMA_TX_p_10
set_location_assignment PIN_M10 -to HSMA_TX_p_11
set_location_assignment PIN_M11 -to HSMA_TX_p_12
set_location_assignment PIN_K8 -to HSMA_TX_p_13
set_location_assignment PIN_K9 -to HSMA_TX_p_14
set_location_assignment PIN_H7 -to HSMA_TX_p_15
set_location_assignment PIN_K10 -to HSMA_TX_p_16
#============================================================
# HSMB
#============================================================
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_CLKIN0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_CLKIN_n1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_CLKIN_n2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_CLKIN_p1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_CLKIN_p2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_D_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_D_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_D_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_D_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_OUT0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_OUT_n1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_OUT_n2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_OUT_p1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_OUT_p2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_n_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_RX_p_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_SCL
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_SDA
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_n_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMB_TX_p_16
set_location_assignment PIN_AP15 -to HSMB_CLKIN0
set_location_assignment PIN_AU7 -to HSMB_CLKIN_n1
set_location_assignment PIN_AW14 -to HSMB_CLKIN_n2
set_location_assignment PIN_AT7 -to HSMB_CLKIN_p1
set_location_assignment PIN_AV14 -to HSMB_CLKIN_p2
set_location_assignment PIN_AD15 -to HSMB_D_0
set_location_assignment PIN_AV13 -to HSMB_D_1
set_location_assignment PIN_AE15 -to HSMB_D_2
set_location_assignment PIN_AW13 -to HSMB_D_3
set_location_assignment PIN_AN15 -to HSMB_OUT0
set_location_assignment PIN_AP10 -to HSMB_OUT_n1
set_location_assignment PIN_AJ10 -to HSMB_OUT_n2
set_location_assignment PIN_AN10 -to HSMB_OUT_p1
set_location_assignment PIN_AH10 -to HSMB_OUT_p2
set_location_assignment PIN_AW10 -to HSMB_RX_n_0
set_location_assignment PIN_AU9 -to HSMB_RX_n_1
set_location_assignment PIN_AW7 -to HSMB_RX_n_2
set_location_assignment PIN_AW5 -to HSMB_RX_n_3
set_location_assignment PIN_AW4 -to HSMB_RX_n_4
set_location_assignment PIN_AW8 -to HSMB_RX_n_5
set_location_assignment PIN_AT5 -to HSMB_RX_n_6
set_location_assignment PIN_AU6 -to HSMB_RX_n_7
set_location_assignment PIN_AR8 -to HSMB_RX_n_8
set_location_assignment PIN_AU8 -to HSMB_RX_n_9
set_location_assignment PIN_AU10 -to HSMB_RX_n_10
set_location_assignment PIN_AV11 -to HSMB_RX_n_11
set_location_assignment PIN_AT13 -to HSMB_RX_n_12
set_location_assignment PIN_AK13 -to HSMB_RX_n_13
set_location_assignment PIN_AJ14 -to HSMB_RX_n_14
set_location_assignment PIN_AF14 -to HSMB_RX_n_15
set_location_assignment PIN_AM13 -to HSMB_RX_n_16
set_location_assignment PIN_AV10 -to HSMB_RX_p_0
set_location_assignment PIN_AT9 -to HSMB_RX_p_1
set_location_assignment PIN_AV7 -to HSMB_RX_p_2
set_location_assignment PIN_AW6 -to HSMB_RX_p_3
set_location_assignment PIN_AV5 -to HSMB_RX_p_4
set_location_assignment PIN_AV8 -to HSMB_RX_p_5
set_location_assignment PIN_AR5 -to HSMB_RX_p_6
set_location_assignment PIN_AT6 -to HSMB_RX_p_7
set_location_assignment PIN_AP8 -to HSMB_RX_p_8
set_location_assignment PIN_AT8 -to HSMB_RX_p_9
set_location_assignment PIN_AT10 -to HSMB_RX_p_10
set_location_assignment PIN_AU11 -to HSMB_RX_p_11
set_location_assignment PIN_AR13 -to HSMB_RX_p_12
set_location_assignment PIN_AJ13 -to HSMB_RX_p_13
set_location_assignment PIN_AH14 -to HSMB_RX_p_14
set_location_assignment PIN_AE14 -to HSMB_RX_p_15
set_location_assignment PIN_AL13 -to HSMB_RX_p_16
set_location_assignment PIN_AE16 -to HSMB_SCL
set_location_assignment PIN_AF16 -to HSMB_SDA
set_location_assignment PIN_AL15 -to HSMB_TX_n_0
set_location_assignment PIN_AU14 -to HSMB_TX_n_1
set_location_assignment PIN_AW11 -to HSMB_TX_n_2
set_location_assignment PIN_AM14 -to HSMB_TX_n_3
set_location_assignment PIN_AU12 -to HSMB_TX_n_4
set_location_assignment PIN_AN14 -to HSMB_TX_n_5
set_location_assignment PIN_AG15 -to HSMB_TX_n_6
set_location_assignment PIN_AP9 -to HSMB_TX_n_7
set_location_assignment PIN_AM8 -to HSMB_TX_n_8
set_location_assignment PIN_AL9 -to HSMB_TX_n_9
set_location_assignment PIN_AM10 -to HSMB_TX_n_10
set_location_assignment PIN_AJ11 -to HSMB_TX_n_11
set_location_assignment PIN_AH12 -to HSMB_TX_n_12
set_location_assignment PIN_AE12 -to HSMB_TX_n_13
set_location_assignment PIN_AG13 -to HSMB_TX_n_14
set_location_assignment PIN_AD12 -to HSMB_TX_n_15
set_location_assignment PIN_AP7 -to HSMB_TX_n_16
set_location_assignment PIN_AN13 -to HSMB_TX_p_0
set_location_assignment PIN_AT14 -to HSMB_TX_p_1
set_location_assignment PIN_AW12 -to HSMB_TX_p_2
set_location_assignment PIN_AL14 -to HSMB_TX_p_3
set_location_assignment PIN_AT12 -to HSMB_TX_p_4
set_location_assignment PIN_AP13 -to HSMB_TX_p_5
set_location_assignment PIN_AG14 -to HSMB_TX_p_6
set_location_assignment PIN_AN9 -to HSMB_TX_p_7
set_location_assignment PIN_AL8 -to HSMB_TX_p_8
set_location_assignment PIN_AK9 -to HSMB_TX_p_9
set_location_assignment PIN_AL10 -to HSMB_TX_p_10
set_location_assignment PIN_AH11 -to HSMB_TX_p_11
set_location_assignment PIN_AG12 -to HSMB_TX_p_12
set_location_assignment PIN_AE13 -to HSMB_TX_p_13
set_location_assignment PIN_AF13 -to HSMB_TX_p_14
set_location_assignment PIN_AD13 -to HSMB_TX_p_15
set_location_assignment PIN_AN7 -to HSMB_TX_p_16
#============================================================
# HSMC
#============================================================
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_0
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_1
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_2
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_3
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_4
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_5
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_6
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_7
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_8
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_9
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_10
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_11
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_12
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_13
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_14
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_15
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_16
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_17
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_18
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_19
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_20
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_21
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_22
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_23
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_24
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_25
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_26
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_27
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_28
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_29
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_30
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_31
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_32
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_33
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_34
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO0_D_35
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_0
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_1
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_2
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_3
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_4
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_5
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_6
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_7
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_8
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_9
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_10
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_11
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_12
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_13
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_14
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_15
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_16
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_17
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_18
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_19
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_20
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_21
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_22
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_23
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_24
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_25
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_26
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_27
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_28
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_29
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_30
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_31
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_32
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_33
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_34
set_instance_assignment -name IO_STANDARD "1.8 V" -to GPIO1_D_35
set_location_assignment PIN_AF34 -to GPIO0_D_0
set_location_assignment PIN_AG34 -to GPIO0_D_1
set_location_assignment PIN_AE35 -to GPIO0_D_2
set_location_assignment PIN_AG35 -to GPIO0_D_3
set_location_assignment PIN_AC31 -to GPIO0_D_4
set_location_assignment PIN_AH32 -to GPIO0_D_5
set_location_assignment PIN_AC32 -to GPIO0_D_6
set_location_assignment PIN_AH33 -to GPIO0_D_7
set_location_assignment PIN_AH34 -to GPIO0_D_8
set_location_assignment PIN_AJ34 -to GPIO0_D_9
set_location_assignment PIN_AH35 -to GPIO0_D_10
set_location_assignment PIN_AJ35 -to GPIO0_D_11
set_location_assignment PIN_AK34 -to GPIO0_D_12
set_location_assignment PIN_AL34 -to GPIO0_D_13
set_location_assignment PIN_AK35 -to GPIO0_D_14
set_location_assignment PIN_AL35 -to GPIO0_D_15
set_location_assignment PIN_AM34 -to GPIO0_D_16
set_location_assignment PIN_AN34 -to GPIO0_D_17
set_location_assignment PIN_AM35 -to GPIO0_D_18
set_location_assignment PIN_AN35 -to GPIO0_D_19
set_location_assignment PIN_AJ32 -to GPIO0_D_20
set_location_assignment PIN_AJ26 -to GPIO0_D_21
set_location_assignment PIN_AK33 -to GPIO0_D_22
set_location_assignment PIN_AK26 -to GPIO0_D_23
set_location_assignment PIN_AF25 -to GPIO0_D_24
set_location_assignment PIN_AV29 -to GPIO0_D_25
set_location_assignment PIN_AG25 -to GPIO0_D_26
set_location_assignment PIN_AW30 -to GPIO0_D_27
set_location_assignment PIN_AV32 -to GPIO0_D_28
set_location_assignment PIN_AT28 -to GPIO0_D_29
set_location_assignment PIN_AW32 -to GPIO0_D_30
set_location_assignment PIN_AU28 -to GPIO0_D_31
set_location_assignment PIN_AV28 -to GPIO0_D_32
set_location_assignment PIN_AP28 -to GPIO0_D_33
set_location_assignment PIN_AW29 -to GPIO0_D_34
set_location_assignment PIN_AR28 -to GPIO0_D_35
set_location_assignment PIN_AB27 -to GPIO1_D_0
set_location_assignment PIN_AE25 -to GPIO1_D_1
set_location_assignment PIN_AB28 -to GPIO1_D_2
set_location_assignment PIN_AD25 -to GPIO1_D_3
set_location_assignment PIN_AP27 -to GPIO1_D_4
set_location_assignment PIN_AU29 -to GPIO1_D_5
set_location_assignment PIN_AN27 -to GPIO1_D_6
set_location_assignment PIN_AT29 -to GPIO1_D_7
set_location_assignment PIN_AL25 -to GPIO1_D_8
set_location_assignment PIN_AW33 -to GPIO1_D_9
set_location_assignment PIN_AP26 -to GPIO1_D_10
set_location_assignment PIN_AW34 -to GPIO1_D_11
set_location_assignment PIN_AW31 -to GPIO1_D_12
set_location_assignment PIN_AH24 -to GPIO1_D_13
set_location_assignment PIN_AV31 -to GPIO1_D_14
set_location_assignment PIN_AG24 -to GPIO1_D_15
set_location_assignment PIN_AL27 -to GPIO1_D_16
set_location_assignment PIN_AW27 -to GPIO1_D_17
set_location_assignment PIN_AH26 -to GPIO1_D_18
set_location_assignment PIN_AW28 -to GPIO1_D_19
set_location_assignment PIN_AK27 -to GPIO1_D_20
set_location_assignment PIN_AD30 -to GPIO1_D_21
set_location_assignment PIN_AE24 -to GPIO1_D_22
set_location_assignment PIN_AD31 -to GPIO1_D_23
set_location_assignment PIN_AB30 -to GPIO1_D_24
set_location_assignment PIN_AE30 -to GPIO1_D_25
set_location_assignment PIN_AB31 -to GPIO1_D_26
set_location_assignment PIN_AE31 -to GPIO1_D_27
set_location_assignment PIN_AG31 -to GPIO1_D_28
set_location_assignment PIN_AE28 -to GPIO1_D_29
set_location_assignment PIN_AG32 -to GPIO1_D_30
set_location_assignment PIN_AE29 -to GPIO1_D_31
set_location_assignment PIN_AF29 -to GPIO1_D_32
set_location_assignment PIN_AD28 -to GPIO1_D_33
set_location_assignment PIN_AG30 -to GPIO1_D_34
set_location_assignment PIN_AD29 -to GPIO1_D_35

#============================================================
# HSMD
#============================================================
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_CLKIN0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_CLKIN_n1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_CLKIN_n2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_CLKIN_p1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_CLKIN_p2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_CLKOUT_n1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_CLKOUT_p1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_D_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_D_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_D_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_D_3
set_instance_assignment -name IO_STANDARD 1.5V -to HSMD_OUT0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_OUT_n2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_OUT_p2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_n_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_RX_p_16
set_instance_assignment -name IO_STANDARD 1.5V -to HSMD_SCL
set_instance_assignment -name IO_STANDARD 1.5V -to HSMD_SDA
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_n_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMD_TX_p_16
set_location_assignment PIN_AA35 -to HSMD_CLKIN0
set_location_assignment PIN_W35 -to HSMD_CLKIN_n1
set_location_assignment PIN_J35 -to HSMD_CLKIN_n2
set_location_assignment PIN_W34 -to HSMD_CLKIN_p1
set_location_assignment PIN_J34 -to HSMD_CLKIN_p2
set_location_assignment PIN_W33 -to HSMD_CLKOUT_n1
set_location_assignment PIN_W32 -to HSMD_CLKOUT_p1
set_location_assignment PIN_AJ29 -to HSMD_D_0
set_location_assignment PIN_AR31 -to HSMD_D_1
set_location_assignment PIN_AK29 -to HSMD_D_2
set_location_assignment PIN_AT30 -to HSMD_D_3
set_location_assignment PIN_P19 -to HSMD_OUT0
set_location_assignment PIN_L32 -to HSMD_OUT_n2
set_location_assignment PIN_M32 -to HSMD_OUT_p2
set_location_assignment PIN_AU31 -to HSMD_RX_n_0
set_location_assignment PIN_AU32 -to HSMD_RX_n_1
set_location_assignment PIN_AU33 -to HSMD_RX_n_2
set_location_assignment PIN_AV34 -to HSMD_RX_n_3
set_location_assignment PIN_AP34 -to HSMD_RX_n_4
set_location_assignment PIN_AR34 -to HSMD_RX_n_5
set_location_assignment PIN_AR35 -to HSMD_RX_n_6
set_location_assignment PIN_AP33 -to HSMD_RX_n_7
set_location_assignment PIN_AN31 -to HSMD_RX_n_8
set_location_assignment PIN_AP30 -to HSMD_RX_n_9
set_location_assignment PIN_AR32 -to HSMD_RX_n_10
set_location_assignment PIN_U35 -to HSMD_RX_n_11
set_location_assignment PIN_V31 -to HSMD_RX_n_12
set_location_assignment PIN_N34 -to HSMD_RX_n_13
set_location_assignment PIN_M34 -to HSMD_RX_n_14
set_location_assignment PIN_L35 -to HSMD_RX_n_15
set_location_assignment PIN_K35 -to HSMD_RX_n_16
set_location_assignment PIN_AT31 -to HSMD_RX_p_0
set_location_assignment PIN_AT32 -to HSMD_RX_p_1
set_location_assignment PIN_AT33 -to HSMD_RX_p_2
set_location_assignment PIN_AU34 -to HSMD_RX_p_3
set_location_assignment PIN_AN33 -to HSMD_RX_p_4
set_location_assignment PIN_AT34 -to HSMD_RX_p_5
set_location_assignment PIN_AP35 -to HSMD_RX_p_6
set_location_assignment PIN_AN32 -to HSMD_RX_p_7
set_location_assignment PIN_AM31 -to HSMD_RX_p_8
set_location_assignment PIN_AN30 -to HSMD_RX_p_9
set_location_assignment PIN_AP32 -to HSMD_RX_p_10
set_location_assignment PIN_V34 -to HSMD_RX_p_11
set_location_assignment PIN_U31 -to HSMD_RX_p_12
set_location_assignment PIN_N33 -to HSMD_RX_p_13
set_location_assignment PIN_M33 -to HSMD_RX_p_14
set_location_assignment PIN_L34 -to HSMD_RX_p_15
set_location_assignment PIN_K34 -to HSMD_RX_p_16
set_location_assignment PIN_G21 -to HSMD_SCL
set_location_assignment PIN_F21 -to HSMD_SDA
set_location_assignment PIN_AM29 -to HSMD_TX_n_0
set_location_assignment PIN_AL30 -to HSMD_TX_n_1
set_location_assignment PIN_AL32 -to HSMD_TX_n_2
set_location_assignment PIN_AH30 -to HSMD_TX_n_3
set_location_assignment PIN_AH27 -to HSMD_TX_n_4
set_location_assignment PIN_AH29 -to HSMD_TX_n_5
set_location_assignment PIN_AH28 -to HSMD_TX_n_6
set_location_assignment PIN_AE27 -to HSMD_TX_n_7
set_location_assignment PIN_AD26 -to HSMD_TX_n_8
set_location_assignment PIN_AF26 -to HSMD_TX_n_9
set_location_assignment PIN_V30 -to HSMD_TX_n_10
set_location_assignment PIN_V28 -to HSMD_TX_n_11
set_location_assignment PIN_T31 -to HSMD_TX_n_12
set_location_assignment PIN_R33 -to HSMD_TX_n_13
set_location_assignment PIN_P32 -to HSMD_TX_n_14
set_location_assignment PIN_R31 -to HSMD_TX_n_15
set_location_assignment PIN_AL31 -to HSMD_TX_n_16
set_location_assignment PIN_AL29 -to HSMD_TX_p_0
set_location_assignment PIN_AK30 -to HSMD_TX_p_1
set_location_assignment PIN_AK32 -to HSMD_TX_p_2
set_location_assignment PIN_AJ31 -to HSMD_TX_p_3
set_location_assignment PIN_AG27 -to HSMD_TX_p_4
set_location_assignment PIN_AG29 -to HSMD_TX_p_5
set_location_assignment PIN_AG28 -to HSMD_TX_p_6
set_location_assignment PIN_AD27 -to HSMD_TX_p_7
set_location_assignment PIN_AC26 -to HSMD_TX_p_8
set_location_assignment PIN_AE26 -to HSMD_TX_p_9
set_location_assignment PIN_V29 -to HSMD_TX_p_10
set_location_assignment PIN_W28 -to HSMD_TX_p_11
set_location_assignment PIN_T30 -to HSMD_TX_p_12
set_location_assignment PIN_R32 -to HSMD_TX_p_13
set_location_assignment PIN_P31 -to HSMD_TX_p_14
set_location_assignment PIN_R30 -to HSMD_TX_p_15
set_location_assignment PIN_AK31 -to HSMD_TX_p_16
#============================================================
# HSME
#============================================================
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_CLKIN0
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_CLKIN_n1
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_CLKIN_n2
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_CLKIN_p1
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_CLKIN_p2
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_CLKOUT_n1
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_CLKOUT_p1
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_D_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_D_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_D_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_D_3
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_RX_p[0]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_RX_p[1]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_RX_p[2]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_RX_p[3]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_RX_p[4]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_RX_p[5]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_RX_p[6]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_RX_p[7]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_TX_p[0]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_TX_p[1]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_TX_p[2]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_TX_p[3]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_TX_p[4]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_TX_p[5]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_TX_p[6]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_GXB_TX_p[7]
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_OUT0
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_OUT_n2
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_OUT_p2
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to HSME_REFCLK_p
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_n_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_RX_p_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_n_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSME_TX_p_16
set_location_assignment PIN_C13 -to HSME_CLKIN0
set_location_assignment PIN_W5 -to HSME_CLKIN_n1
set_location_assignment PIN_AA5 -to HSME_CLKIN_n2
set_location_assignment PIN_W6 -to HSME_CLKIN_p1
set_location_assignment PIN_AB6 -to HSME_CLKIN_p2
set_location_assignment PIN_W11 -to HSME_CLKOUT_n1
set_location_assignment PIN_W12 -to HSME_CLKOUT_p1
set_location_assignment PIN_V12 -to HSME_D_0
set_location_assignment PIN_W8 -to HSME_D_1
set_location_assignment PIN_V11 -to HSME_D_2
set_location_assignment PIN_W7 -to HSME_D_3
set_location_assignment PIN_AE38 -to HSME_GXB_RX_p[0]
set_location_assignment PIN_AC38 -to HSME_GXB_RX_p[1]
set_location_assignment PIN_U38 -to HSME_GXB_RX_p[2]
set_location_assignment PIN_R38 -to HSME_GXB_RX_p[3]
set_location_assignment PIN_N38 -to HSME_GXB_RX_p[4]
set_location_assignment PIN_L38 -to HSME_GXB_RX_p[5]
set_location_assignment PIN_E38 -to HSME_GXB_RX_p[6]
set_location_assignment PIN_C38 -to HSME_GXB_RX_p[7]
set_location_assignment PIN_AD36 -to HSME_GXB_TX_p[0]
set_location_assignment PIN_AB36 -to HSME_GXB_TX_p[1]
set_location_assignment PIN_T36 -to HSME_GXB_TX_p[2]
set_location_assignment PIN_P36 -to HSME_GXB_TX_p[3]
set_location_assignment PIN_M36 -to HSME_GXB_TX_p[4]
set_location_assignment PIN_K36 -to HSME_GXB_TX_p[5]
set_location_assignment PIN_D36 -to HSME_GXB_TX_p[6]
set_location_assignment PIN_B36 -to HSME_GXB_TX_p[7]
set_location_assignment PIN_C12 -to HSME_OUT0
set_location_assignment PIN_N15 -to HSME_OUT_n2
set_location_assignment PIN_R14 -to HSME_OUT_p2
set_location_assignment PIN_AA38 -to HSME_REFCLK_p
set_location_assignment PIN_U5 -to HSME_RX_n_0
set_location_assignment PIN_R5 -to HSME_RX_n_1
set_location_assignment PIN_P6 -to HSME_RX_n_2
set_location_assignment PIN_N5 -to HSME_RX_n_3
set_location_assignment PIN_N7 -to HSME_RX_n_4
set_location_assignment PIN_L5 -to HSME_RX_n_5
set_location_assignment PIN_K5 -to HSME_RX_n_6
set_location_assignment PIN_J5 -to HSME_RX_n_7
set_location_assignment PIN_N14 -to HSME_RX_n_8
set_location_assignment PIN_K13 -to HSME_RX_n_9
set_location_assignment PIN_K14 -to HSME_RX_n_10
set_location_assignment PIN_G13 -to HSME_RX_n_11
set_location_assignment PIN_E13 -to HSME_RX_n_12
set_location_assignment PIN_A11 -to HSME_RX_n_13
set_location_assignment PIN_E14 -to HSME_RX_n_14
set_location_assignment PIN_A13 -to HSME_RX_n_15
set_location_assignment PIN_C14 -to HSME_RX_n_16
set_location_assignment PIN_V6 -to HSME_RX_p_0
set_location_assignment PIN_R6 -to HSME_RX_p_1
set_location_assignment PIN_R7 -to HSME_RX_p_2
set_location_assignment PIN_N6 -to HSME_RX_p_3
set_location_assignment PIN_N8 -to HSME_RX_p_4
set_location_assignment PIN_M6 -to HSME_RX_p_5
set_location_assignment PIN_K6 -to HSME_RX_p_6
set_location_assignment PIN_J6 -to HSME_RX_p_7
set_location_assignment PIN_P14 -to HSME_RX_p_8
set_location_assignment PIN_L13 -to HSME_RX_p_9
set_location_assignment PIN_L14 -to HSME_RX_p_10
set_location_assignment PIN_H13 -to HSME_RX_p_11
set_location_assignment PIN_F13 -to HSME_RX_p_12
set_location_assignment PIN_B11 -to HSME_RX_p_13
set_location_assignment PIN_F14 -to HSME_RX_p_14
set_location_assignment PIN_B13 -to HSME_RX_p_15
set_location_assignment PIN_D14 -to HSME_RX_p_16
set_location_assignment PIN_V9 -to HSME_TX_n_0
set_location_assignment PIN_R10 -to HSME_TX_n_1
set_location_assignment PIN_T9 -to HSME_TX_n_2
set_location_assignment PIN_R8 -to HSME_TX_n_3
set_location_assignment PIN_P8 -to HSME_TX_n_4
set_location_assignment PIN_M7 -to HSME_TX_n_5
set_location_assignment PIN_L7 -to HSME_TX_n_6
set_location_assignment PIN_J7 -to HSME_TX_n_7
set_location_assignment PIN_M13 -to HSME_TX_n_8
set_location_assignment PIN_K12 -to HSME_TX_n_9
set_location_assignment PIN_B10 -to HSME_TX_n_10
set_location_assignment PIN_C11 -to HSME_TX_n_11
set_location_assignment PIN_J13 -to HSME_TX_n_12
set_location_assignment PIN_D13 -to HSME_TX_n_13
set_location_assignment PIN_A14 -to HSME_TX_n_14
set_location_assignment PIN_G14 -to HSME_TX_n_15
set_location_assignment PIN_J15 -to HSME_TX_n_16
set_location_assignment PIN_V10 -to HSME_TX_p_0
set_location_assignment PIN_T10 -to HSME_TX_p_1
set_location_assignment PIN_U10 -to HSME_TX_p_2
set_location_assignment PIN_R9 -to HSME_TX_p_3
set_location_assignment PIN_N9 -to HSME_TX_p_4
set_location_assignment PIN_M8 -to HSME_TX_p_5
set_location_assignment PIN_L8 -to HSME_TX_p_6
set_location_assignment PIN_K7 -to HSME_TX_p_7
set_location_assignment PIN_N13 -to HSME_TX_p_8
set_location_assignment PIN_M14 -to HSME_TX_p_9
set_location_assignment PIN_D11 -to HSME_TX_p_10
set_location_assignment PIN_A10 -to HSME_TX_p_11
set_location_assignment PIN_J12 -to HSME_TX_p_12
set_location_assignment PIN_F12 -to HSME_TX_p_13
set_location_assignment PIN_B14 -to HSME_TX_p_14
set_location_assignment PIN_H14 -to HSME_TX_p_15
set_location_assignment PIN_K15 -to HSME_TX_p_16
#============================================================
# HSMF
#============================================================
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_CLKIN0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_CLKIN_n1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_CLKIN_n2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_CLKIN_p1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_CLKIN_p2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_CLKOUT_n1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_CLKOUT_n2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_CLKOUT_p1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_CLKOUT_p2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_D_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_D_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_D_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_D_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_OUT0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_n_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_RX_p_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_n_16
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_0
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_1
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_2
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_3
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_4
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_5
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_6
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_7
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_8
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_9
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_10
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_11
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_12
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_13
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_14
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_15
set_instance_assignment -name IO_STANDARD 2.5V -to HSMF_TX_p_16
set_location_assignment PIN_AV22 -to HSMF_CLKIN0
set_location_assignment PIN_AW21 -to HSMF_CLKIN_n1
set_location_assignment PIN_AT21 -to HSMF_CLKIN_n2
set_location_assignment PIN_AW20 -to HSMF_CLKIN_p1
set_location_assignment PIN_AR22 -to HSMF_CLKIN_p2
set_location_assignment PIN_AP21 -to HSMF_CLKOUT_n1
set_location_assignment PIN_AJ20 -to HSMF_CLKOUT_n2
set_location_assignment PIN_AN21 -to HSMF_CLKOUT_p1
set_location_assignment PIN_AH20 -to HSMF_CLKOUT_p2
set_location_assignment PIN_AU25 -to HSMF_D_0
set_location_assignment PIN_AV26 -to HSMF_D_1
set_location_assignment PIN_AT25 -to HSMF_D_2
set_location_assignment PIN_AW26 -to HSMF_D_3
set_location_assignment PIN_AP20 -to HSMF_OUT0
set_location_assignment PIN_AU26 -to HSMF_RX_n_0
set_location_assignment PIN_AU24 -to HSMF_RX_n_1
set_location_assignment PIN_AP24 -to HSMF_RX_n_2
set_location_assignment PIN_AU23 -to HSMF_RX_n_3
set_location_assignment PIN_AT20 -to HSMF_RX_n_4
set_location_assignment PIN_AU22 -to HSMF_RX_n_5
set_location_assignment PIN_AV20 -to HSMF_RX_n_6
set_location_assignment PIN_AU19 -to HSMF_RX_n_7
set_location_assignment PIN_AU18 -to HSMF_RX_n_8
set_location_assignment PIN_AV17 -to HSMF_RX_n_9
set_location_assignment PIN_AN22 -to HSMF_RX_n_10
set_location_assignment PIN_AP18 -to HSMF_RX_n_11
set_location_assignment PIN_AL23 -to HSMF_RX_n_12
set_location_assignment PIN_AJ23 -to HSMF_RX_n_13
set_location_assignment PIN_AG22 -to HSMF_RX_n_14
set_location_assignment PIN_AF20 -to HSMF_RX_n_15
set_location_assignment PIN_AF19 -to HSMF_RX_n_16
set_location_assignment PIN_AT26 -to HSMF_RX_p_0
set_location_assignment PIN_AT24 -to HSMF_RX_p_1
set_location_assignment PIN_AN24 -to HSMF_RX_p_2
set_location_assignment PIN_AT23 -to HSMF_RX_p_3
set_location_assignment PIN_AR20 -to HSMF_RX_p_4
set_location_assignment PIN_AT22 -to HSMF_RX_p_5
set_location_assignment PIN_AU20 -to HSMF_RX_p_6
set_location_assignment PIN_AT19 -to HSMF_RX_p_7
set_location_assignment PIN_AT18 -to HSMF_RX_p_8
set_location_assignment PIN_AU17 -to HSMF_RX_p_9
set_location_assignment PIN_AM22 -to HSMF_RX_p_10
set_location_assignment PIN_AN18 -to HSMF_RX_p_11
set_location_assignment PIN_AK23 -to HSMF_RX_p_12
set_location_assignment PIN_AH23 -to HSMF_RX_p_13
set_location_assignment PIN_AF22 -to HSMF_RX_p_14
set_location_assignment PIN_AE20 -to HSMF_RX_p_15
set_location_assignment PIN_AE19 -to HSMF_RX_p_16
set_location_assignment PIN_AW25 -to HSMF_TX_n_0
set_location_assignment PIN_AP25 -to HSMF_TX_n_1
set_location_assignment PIN_AW23 -to HSMF_TX_n_2
set_location_assignment PIN_AR23 -to HSMF_TX_n_3
set_location_assignment PIN_AN23 -to HSMF_TX_n_4
set_location_assignment PIN_AM25 -to HSMF_TX_n_5
set_location_assignment PIN_AL21 -to HSMF_TX_n_6
set_location_assignment PIN_AP19 -to HSMF_TX_n_7
set_location_assignment PIN_AW18 -to HSMF_TX_n_8
set_location_assignment PIN_AM19 -to HSMF_TX_n_9
set_location_assignment PIN_AK24 -to HSMF_TX_n_10
set_location_assignment PIN_AH22 -to HSMF_TX_n_11
set_location_assignment PIN_AE22 -to HSMF_TX_n_12
set_location_assignment PIN_AE21 -to HSMF_TX_n_13
set_location_assignment PIN_AG20 -to HSMF_TX_n_14
set_location_assignment PIN_AE18 -to HSMF_TX_n_15
set_location_assignment PIN_AG19 -to HSMF_TX_n_16
set_location_assignment PIN_AV25 -to HSMF_TX_p_0
set_location_assignment PIN_AR25 -to HSMF_TX_p_1
set_location_assignment PIN_AV23 -to HSMF_TX_p_2
set_location_assignment PIN_AP23 -to HSMF_TX_p_3
set_location_assignment PIN_AM23 -to HSMF_TX_p_4
set_location_assignment PIN_AN25 -to HSMF_TX_p_5
set_location_assignment PIN_AL22 -to HSMF_TX_p_6
set_location_assignment PIN_AR19 -to HSMF_TX_p_7
set_location_assignment PIN_AT17 -to HSMF_TX_p_8
set_location_assignment PIN_AN19 -to HSMF_TX_p_9
set_location_assignment PIN_AJ22 -to HSMF_TX_p_10
set_location_assignment PIN_AE23 -to HSMF_TX_p_11
set_location_assignment PIN_AF23 -to HSMF_TX_p_12
set_location_assignment PIN_AG21 -to HSMF_TX_p_13
set_location_assignment PIN_AD21 -to HSMF_TX_p_14
set_location_assignment PIN_AG18 -to HSMF_TX_p_15
set_location_assignment PIN_AD19 -to HSMF_TX_p_16
#============================================================
# memory
#============================================================
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[0]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[1]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[2]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[3]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[4]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[5]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[6]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[7]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[8]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[9]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[10]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[11]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[12]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[13]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[14]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_a[15]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_ba[0]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_ba[1]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_ba[2]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_cas_n
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_cke[0]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_cke[1]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_cs_n[0]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_cs_n[1]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dm[0]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dm[1]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dm[2]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dm[3]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dm[4]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dm[5]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dm[6]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dm[7]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[0]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[1]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[2]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[3]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[4]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[5]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[6]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[7]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[8]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[9]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[10]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[11]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[12]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[13]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[14]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[15]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[16]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[17]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[18]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[19]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[20]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[21]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[22]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[23]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[24]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[25]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[26]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[27]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[28]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[29]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[30]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[31]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[32]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[33]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[34]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[35]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[36]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[37]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[38]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[39]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[40]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[41]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[42]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[43]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[44]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[45]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[46]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[47]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[48]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[49]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[50]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[51]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[52]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[53]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[54]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[55]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[56]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[57]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[58]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[59]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[60]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[61]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[62]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_dq[63]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs[0]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs[1]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs[2]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs[3]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs[4]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs[5]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs[6]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs[7]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs_n[0]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs_n[1]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs_n[2]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs_n[3]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs_n[4]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs_n[5]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs_n[6]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_dqs_n[7]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_event_n
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_odt[0]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_odt[1]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_ras_n
set_instance_assignment -name IO_STANDARD 1.5V -to mem_reset_n
set_instance_assignment -name IO_STANDARD 1.5V -to mem_scl
set_instance_assignment -name IO_STANDARD 1.5V -to mem_sda
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to mem_we_n
set_instance_assignment -name IO_STANDARD 1.5V -to mem_oct_rdn
set_instance_assignment -name IO_STANDARD 1.5V -to mem_oct_rup
set_location_assignment PIN_N23 -to mem_a[0]
set_location_assignment PIN_C22 -to mem_a[1]
set_location_assignment PIN_M22 -to mem_a[2]
set_location_assignment PIN_D21 -to mem_a[3]
set_location_assignment PIN_P24 -to mem_a[4]
set_location_assignment PIN_A24 -to mem_a[5]
set_location_assignment PIN_M21 -to mem_a[6]
set_location_assignment PIN_D17 -to mem_a[7]
set_location_assignment PIN_A25 -to mem_a[8]
set_location_assignment PIN_N25 -to mem_a[9]
set_location_assignment PIN_C24 -to mem_a[10]
set_location_assignment PIN_N21 -to mem_a[11]
set_location_assignment PIN_M25 -to mem_a[12]
set_location_assignment PIN_K26 -to mem_a[13]
set_location_assignment PIN_F16 -to mem_a[14]
set_location_assignment PIN_R20 -to mem_a[15]
set_location_assignment PIN_B26 -to mem_ba[0]
set_location_assignment PIN_A29 -to mem_ba[1]
set_location_assignment PIN_R24 -to mem_ba[2]
set_location_assignment PIN_L26 -to mem_cas_n
set_location_assignment PIN_P25 -to mem_cke[0]
set_location_assignment PIN_M16 -to mem_cke[1]
set_location_assignment PIN_L25 -to mem_ck[1]
set_location_assignment PIN_K28 -to mem_ck_n[1]
set_location_assignment PIN_D23 -to mem_cs_n[0]
set_location_assignment PIN_G28 -to mem_cs_n[1]
set_location_assignment PIN_G16 -to mem_dm[0]
set_location_assignment PIN_N16 -to mem_dm[1]
set_location_assignment PIN_P23 -to mem_dm[2]
set_location_assignment PIN_B29 -to mem_dm[3]
set_location_assignment PIN_H28 -to mem_dm[4]
set_location_assignment PIN_E17 -to mem_dm[5]
set_location_assignment PIN_C26 -to mem_dm[6]
set_location_assignment PIN_E23 -to mem_dm[7]
set_location_assignment PIN_G15 -to mem_dq[0]
set_location_assignment PIN_F15 -to mem_dq[1]
set_location_assignment PIN_C16 -to mem_dq[2]
set_location_assignment PIN_B16 -to mem_dq[3]
set_location_assignment PIN_G17 -to mem_dq[4]
set_location_assignment PIN_A16 -to mem_dq[5]
set_location_assignment PIN_D16 -to mem_dq[6]
set_location_assignment PIN_E16 -to mem_dq[7]
set_location_assignment PIN_N17 -to mem_dq[8]
set_location_assignment PIN_M17 -to mem_dq[9]
set_location_assignment PIN_K17 -to mem_dq[10]
set_location_assignment PIN_L16 -to mem_dq[11]
set_location_assignment PIN_P16 -to mem_dq[12]
set_location_assignment PIN_P17 -to mem_dq[13]
set_location_assignment PIN_J17 -to mem_dq[14]
set_location_assignment PIN_H17 -to mem_dq[15]
set_location_assignment PIN_N22 -to mem_dq[16]
set_location_assignment PIN_M23 -to mem_dq[17]
set_location_assignment PIN_J25 -to mem_dq[18]
set_location_assignment PIN_M24 -to mem_dq[19]
set_location_assignment PIN_R22 -to mem_dq[20]
set_location_assignment PIN_P22 -to mem_dq[21]
set_location_assignment PIN_K24 -to mem_dq[22]
set_location_assignment PIN_J24 -to mem_dq[23]
set_location_assignment PIN_A27 -to mem_dq[24]
set_location_assignment PIN_A28 -to mem_dq[25]
set_location_assignment PIN_C29 -to mem_dq[26]
set_location_assignment PIN_C30 -to mem_dq[27]
set_location_assignment PIN_C27 -to mem_dq[28]
set_location_assignment PIN_D27 -to mem_dq[29]
set_location_assignment PIN_A31 -to mem_dq[30]
set_location_assignment PIN_B31 -to mem_dq[31]
set_location_assignment PIN_G27 -to mem_dq[32]
set_location_assignment PIN_G29 -to mem_dq[33]
set_location_assignment PIN_F28 -to mem_dq[34]
set_location_assignment PIN_F27 -to mem_dq[35]
set_location_assignment PIN_E28 -to mem_dq[36]
set_location_assignment PIN_D28 -to mem_dq[37]
set_location_assignment PIN_H26 -to mem_dq[38]
set_location_assignment PIN_J26 -to mem_dq[39]
set_location_assignment PIN_F19 -to mem_dq[40]
set_location_assignment PIN_G19 -to mem_dq[41]
set_location_assignment PIN_F20 -to mem_dq[42]
set_location_assignment PIN_G20 -to mem_dq[43]
set_location_assignment PIN_C17 -to mem_dq[44]
set_location_assignment PIN_F17 -to mem_dq[45]
set_location_assignment PIN_C18 -to mem_dq[46]
set_location_assignment PIN_D18 -to mem_dq[47]
set_location_assignment PIN_D25 -to mem_dq[48]
set_location_assignment PIN_C25 -to mem_dq[49]
set_location_assignment PIN_G24 -to mem_dq[50]
set_location_assignment PIN_G25 -to mem_dq[51]
set_location_assignment PIN_B25 -to mem_dq[52]
set_location_assignment PIN_A26 -to mem_dq[53]
set_location_assignment PIN_D26 -to mem_dq[54]
set_location_assignment PIN_F24 -to mem_dq[55]
set_location_assignment PIN_F23 -to mem_dq[56]
set_location_assignment PIN_G23 -to mem_dq[57]
set_location_assignment PIN_J22 -to mem_dq[58]
set_location_assignment PIN_H22 -to mem_dq[59]
set_location_assignment PIN_K22 -to mem_dq[60]
set_location_assignment PIN_D22 -to mem_dq[61]
set_location_assignment PIN_G22 -to mem_dq[62]
set_location_assignment PIN_E22 -to mem_dq[63]
set_location_assignment PIN_D15 -to mem_dqs[0]
set_location_assignment PIN_K16 -to mem_dqs[1]
set_location_assignment PIN_L23 -to mem_dqs[2]
set_location_assignment PIN_C28 -to mem_dqs[3]
set_location_assignment PIN_E29 -to mem_dqs[4]
set_location_assignment PIN_G18 -to mem_dqs[5]
set_location_assignment PIN_F25 -to mem_dqs[6]
set_location_assignment PIN_J23 -to mem_dqs[7]
set_location_assignment PIN_C15 -to mem_dqs_n[0]
set_location_assignment PIN_J16 -to mem_dqs_n[1]
set_location_assignment PIN_K23 -to mem_dqs_n[2]
set_location_assignment PIN_B28 -to mem_dqs_n[3]
set_location_assignment PIN_D29 -to mem_dqs_n[4]
set_location_assignment PIN_F18 -to mem_dqs_n[5]
set_location_assignment PIN_E25 -to mem_dqs_n[6]
set_location_assignment PIN_H23 -to mem_dqs_n[7]
set_location_assignment PIN_R18 -to mem_event_n
set_location_assignment PIN_F26 -to mem_odt[0]
set_location_assignment PIN_G26 -to mem_odt[1]
set_location_assignment PIN_D24 -to mem_ras_n
set_location_assignment PIN_J18 -to mem_reset_n
set_location_assignment PIN_H19 -to mem_scl
set_location_assignment PIN_P18 -to mem_sda
set_location_assignment PIN_M27 -to mem_we_n
set_location_assignment PIN_N26 -to mem_oct_rdn
set_location_assignment PIN_P26 -to mem_oct_rup
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[0]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[0]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[1]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[1]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[2]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[2]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[3]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[3]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[4]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[4]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[5]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[5]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[6]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[6]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[7]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[7]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[8]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[8]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[9]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[9]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[10]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[10]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[11]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[11]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[12]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[12]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[13]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[13]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[14]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[14]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[15]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[15]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[16]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[16]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[17]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[17]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[18]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[18]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[19]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[19]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[20]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[20]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[21]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[21]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[22]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[22]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[23]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[23]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[24]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[24]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[25]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[25]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[26]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[26]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[27]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[27]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[28]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[28]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[29]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[29]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[30]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[30]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[31]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[31]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[32]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[32]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[33]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[33]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[34]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[34]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[35]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[35]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[36]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[36]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[37]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[37]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[38]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[38]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[39]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[39]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[40]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[40]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[41]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[41]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[42]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[42]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[43]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[43]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[44]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[44]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[45]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[45]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[46]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[46]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[47]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[47]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[48]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[48]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[49]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[49]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[50]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[50]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[51]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[51]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[52]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[52]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[53]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[53]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[54]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[54]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[55]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[55]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[56]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[56]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[57]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[57]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[58]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[58]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[59]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[59]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[60]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[60]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[61]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[61]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[62]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[62]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dq[63]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dq[63]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs[0]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs[0]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs[1]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs[1]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs[2]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs[2]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs[3]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs[3]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs[4]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs[4]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs[5]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs[5]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs[6]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs[6]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs[7]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs[7]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs_n[0]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs_n[0]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs_n[1]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs_n[1]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs_n[2]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs_n[2]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs_n[3]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs_n[3]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs_n[4]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs_n[4]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs_n[5]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs_n[5]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs_n[6]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs_n[6]
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to mem_dqs_n[7]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dqs_n[7]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[0]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[10]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[11]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[12]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[13]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[1]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[2]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[3]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[4]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[5]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[6]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[7]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[8]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_a[9]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_ba[0]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_ba[1]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_ba[2]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_cs_n[0]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_we_n
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_ras_n
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_cas_n
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_cke[0]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_odt[0]
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to mem_reset_n
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dm[0]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dm[1]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dm[2]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dm[3]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dm[4]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dm[5]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dm[6]
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to mem_dm[7]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[0] -to mem_dq[0]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[0] -to mem_dq[1]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[0] -to mem_dq[2]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[0] -to mem_dq[3]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[0] -to mem_dq[4]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[0] -to mem_dq[5]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[0] -to mem_dq[6]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[0] -to mem_dq[7]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[1] -to mem_dq[8]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[1] -to mem_dq[9]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[1] -to mem_dq[10]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[1] -to mem_dq[11]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[1] -to mem_dq[12]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[1] -to mem_dq[13]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[1] -to mem_dq[14]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[1] -to mem_dq[15]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[2] -to mem_dq[16]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[2] -to mem_dq[17]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[2] -to mem_dq[18]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[2] -to mem_dq[19]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[2] -to mem_dq[20]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[2] -to mem_dq[21]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[2] -to mem_dq[22]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[2] -to mem_dq[23]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[3] -to mem_dq[24]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[3] -to mem_dq[25]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[3] -to mem_dq[26]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[3] -to mem_dq[27]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[3] -to mem_dq[28]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[3] -to mem_dq[29]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[3] -to mem_dq[30]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[3] -to mem_dq[31]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[4] -to mem_dq[32]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[4] -to mem_dq[33]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[4] -to mem_dq[34]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[4] -to mem_dq[35]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[4] -to mem_dq[36]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[4] -to mem_dq[37]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[4] -to mem_dq[38]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[4] -to mem_dq[39]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[5] -to mem_dq[40]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[5] -to mem_dq[41]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[5] -to mem_dq[42]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[5] -to mem_dq[43]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[5] -to mem_dq[44]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[5] -to mem_dq[45]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[5] -to mem_dq[46]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[5] -to mem_dq[47]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[6] -to mem_dq[48]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[6] -to mem_dq[49]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[6] -to mem_dq[50]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[6] -to mem_dq[51]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[6] -to mem_dq[52]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[6] -to mem_dq[53]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[6] -to mem_dq[54]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[6] -to mem_dq[55]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[7] -to mem_dq[56]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[7] -to mem_dq[57]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[7] -to mem_dq[58]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[7] -to mem_dq[59]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[7] -to mem_dq[60]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[7] -to mem_dq[61]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[7] -to mem_dq[62]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[7] -to mem_dq[63]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[0] -to mem_dm[0]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[1] -to mem_dm[1]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[2] -to mem_dm[2]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[3] -to mem_dm[3]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[4] -to mem_dm[4]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[5] -to mem_dm[5]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[6] -to mem_dm[6]
set_instance_assignment -name DQ_GROUP 9 -from mem_dqs[7] -to mem_dm[7]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[0]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[1]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[2]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[3]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[4]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[5]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[6]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[7]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[8]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[9]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[10]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[11]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[12]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[13]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[14]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[15]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[16]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[17]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[18]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[19]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[20]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[21]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[22]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[23]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[24]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[25]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[26]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[27]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[28]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[29]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[30]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[31]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[32]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[33]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[34]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[35]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[36]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[37]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[38]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[39]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[40]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[41]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[42]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[43]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[44]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[45]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[46]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[47]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[48]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[49]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[50]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[51]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[52]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[53]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[54]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[55]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[56]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[57]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[58]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[59]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[60]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[61]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[62]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dq[63]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dm[0]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dm[1]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dm[2]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dm[3]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dm[4]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dm[5]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dm[6]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dm[7]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs[0]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs[1]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs[2]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs[3]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs[4]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs[5]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs[6]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs[7]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs_n[0]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs_n[1]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs_n[2]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs_n[3]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs_n[4]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs_n[5]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs_n[6]
set_instance_assignment -name MEM_INTERFACE_DELAY_CHAIN_CONFIG FLEXIBLE_TIMING -to mem_dqs_n[7]
set_instance_assignment -name ENABLE_BENEFICIAL_SKEW_OPTIMIZATION_FOR_NON_GLOBAL_CLOCKS ON -to u0|uniphy_ddr3
set_instance_assignment -name PLL_ENFORCE_USER_PHASE_SHIFT ON -to u0|uniphy_ddr3|pll0|upll_memphy|auto_generated|pll1
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_ck
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to mem_ck_n
set_location_assignment PIN_K27 -to mem_ck
set_location_assignment PIN_J27 -to mem_ck_n
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITHOUT CALIBRATION" -to mem_ck
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITHOUT CALIBRATION" -to mem_ck_n
#============================================================
# MAX
#============================================================
set_instance_assignment -name IO_STANDARD 1.5V -to MAX2_CS_n
set_instance_assignment -name IO_STANDARD 1.5V -to MAX2_I2C_SCL
set_instance_assignment -name IO_STANDARD 1.5V -to MAX2_I2C_SDA
set_location_assignment PIN_D20 -to MAX2_CS_n
set_location_assignment PIN_C20 -to MAX2_I2C_SCL
set_location_assignment PIN_A17 -to MAX2_I2C_SDA
#============================================================
# PCIE
#============================================================
set_instance_assignment -name IO_STANDARD 2.5V -to PCIE0_pREST_n
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE0_REFCLK_p
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE0_RX_p[0]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE0_RX_p[1]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE0_RX_p[2]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE0_RX_p[3]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE0_TX_p[0]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE0_TX_p[1]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE0_TX_p[2]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE0_TX_p[3]
set_instance_assignment -name IO_STANDARD 2.5V -to PCIE0_WAKE_n
set_instance_assignment -name IO_STANDARD 2.5V -to PCIE1_pREST_n
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE1_REFCLK_p
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE1_RX_p[0]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE1_RX_p[1]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE1_RX_p[2]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE1_RX_p[3]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE1_TX_p[0]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE1_TX_p[1]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE1_TX_p[2]
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE1_TX_p[3]
set_instance_assignment -name IO_STANDARD 2.5V -to PCIE1_WAKE_n
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PGM_GXBCLK_p1
set_location_assignment PIN_F8 -to PCIE0_pREST_n
set_location_assignment PIN_AN38 -to PCIE0_REFCLK_p
set_location_assignment PIN_AU38 -to PCIE0_RX_p[0]
set_location_assignment PIN_AR38 -to PCIE0_RX_p[1]
set_location_assignment PIN_AJ38 -to PCIE0_RX_p[2]
set_location_assignment PIN_AG38 -to PCIE0_RX_p[3]
set_location_assignment PIN_AT36 -to PCIE0_TX_p[0]
set_location_assignment PIN_AP36 -to PCIE0_TX_p[1]
set_location_assignment PIN_AH36 -to PCIE0_TX_p[2]
set_location_assignment PIN_AF36 -to PCIE0_TX_p[3]
set_location_assignment PIN_AE10 -to PCIE0_WAKE_n
set_location_assignment PIN_G8 -to PCIE1_pREST_n
set_location_assignment PIN_AN2 -to PCIE1_REFCLK_p
set_location_assignment PIN_AU2 -to PCIE1_RX_p[0]
set_location_assignment PIN_AR2 -to PCIE1_RX_p[1]
set_location_assignment PIN_AJ2 -to PCIE1_RX_p[2]
set_location_assignment PIN_AG2 -to PCIE1_RX_p[3]
set_location_assignment PIN_AT4 -to PCIE1_TX_p[0]
set_location_assignment PIN_AP4 -to PCIE1_TX_p[1]
set_location_assignment PIN_AH4 -to PCIE1_TX_p[2]
set_location_assignment PIN_AF4 -to PCIE1_TX_p[3]
set_location_assignment PIN_AE11 -to PCIE1_WAKE_n
set_location_assignment PIN_J2 -to PGM_GXBCLK_p1
#============================================================
# SMA
#============================================================
set_instance_assignment -name IO_STANDARD 2.5V -to SMA_CLKIN
set_instance_assignment -name IO_STANDARD 2.5V -to SMA_CLKOUT
set_instance_assignment -name IO_STANDARD 2.5V -to SMA_CLKOUT_n
set_instance_assignment -name IO_STANDARD 2.5V -to SMA_CLKOUT_p
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to SMA_GXBCLK_p
set_location_assignment PIN_AW19 -to SMA_CLKIN
set_location_assignment PIN_AN20 -to SMA_CLKOUT
set_location_assignment PIN_AC10 -to SMA_CLKOUT_n
set_location_assignment PIN_AC11 -to SMA_CLKOUT_p
set_location_assignment PIN_J38 -to SMA_GXBCLK_p
#============================================================
# SSRAM
#============================================================
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to SSRAM_ADSC_n
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to SSRAM_ADSP_n
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to SSRAM_ADV_n
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to SSRAM_BE_n[0]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to SSRAM_BE_n[1]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to SSRAM_BE_n[2]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to SSRAM_BE_n[3]
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to SSRAM_CE1_n
set_instance_assignment -name IO_STANDARD "3.0-V PCI-X" -to SSRAM_CLK
set_location_assignment PIN_AP17 -to SSRAM_ADSC_n
set_location_assignment PIN_AR17 -to SSRAM_ADSP_n
set_location_assignment PIN_AW16 -to SSRAM_ADV_n
set_location_assignment PIN_AN16 -to SSRAM_BE_n[0]
set_location_assignment PIN_AN17 -to SSRAM_BE_n[1]
set_location_assignment PIN_AR16 -to SSRAM_BE_n[2]
set_location_assignment PIN_AU16 -to SSRAM_BE_n[3]
set_location_assignment PIN_AF17 -to SSRAM_CE1_n
set_location_assignment PIN_AG17 -to SSRAM_CLK
#============================================================
# TEMP
#============================================================
set_instance_assignment -name IO_STANDARD 2.5V -to TEMP_CLK
set_instance_assignment -name IO_STANDARD 2.5V -to TEMP_DATA
set_instance_assignment -name IO_STANDARD 2.5V -to TEMP_INT_n
set_instance_assignment -name IO_STANDARD 2.5V -to TEMP_OVERT_n
set_location_assignment PIN_AR14 -to TEMP_CLK
set_location_assignment PIN_AP14 -to TEMP_DATA
set_location_assignment PIN_AH13 -to TEMP_INT_n
set_location_assignment PIN_AK14 -to TEMP_OVERT_n

