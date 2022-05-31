#============================================================
# CLOCK
#============================================================
set_instance_assignment -name IO_STANDARD "1.5 V" -to OSC_50_B3B
set_instance_assignment -name IO_STANDARD "1.5 V" -to OSC_50_B4A
set_instance_assignment -name IO_STANDARD "2.5 V" -to OSC_50_B4D
set_instance_assignment -name IO_STANDARD "2.5 V" -to OSC_50_B7A
set_instance_assignment -name IO_STANDARD "2.5 V" -to OSC_50_B7D
set_instance_assignment -name IO_STANDARD "2.5 V" -to OSC_50_B8A
set_instance_assignment -name IO_STANDARD "2.5 V" -to OSC_50_B8D
set_location_assignment PIN_AW35 -to OSC_50_B3B
set_location_assignment PIN_AP10 -to OSC_50_B4A
set_location_assignment PIN_AY18 -to OSC_50_B4D
set_location_assignment PIN_M8 -to OSC_50_B7A
set_location_assignment PIN_J18 -to OSC_50_B7D
set_location_assignment PIN_R36 -to OSC_50_B8A
set_location_assignment PIN_R25 -to OSC_50_B8D

#============================================================
# KEY
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to CPU_RESET_n
set_instance_assignment -name IO_STANDARD "1.5 V" -to BUTTON[0]
set_instance_assignment -name IO_STANDARD "1.5 V" -to BUTTON[1]
set_instance_assignment -name IO_STANDARD "1.5 V" -to BUTTON[2]
set_instance_assignment -name IO_STANDARD "1.5 V" -to BUTTON[3]
set_location_assignment PIN_BC37 -to CPU_RESET_n
set_location_assignment PIN_BC7 -to BUTTON[0]
set_location_assignment PIN_BD7 -to BUTTON[1]
set_location_assignment PIN_BB8 -to BUTTON[2]
set_location_assignment PIN_BB9 -to BUTTON[3]

#============================================================
# SW
#============================================================
set_instance_assignment -name IO_STANDARD "1.5 V" -to SW_0
set_instance_assignment -name IO_STANDARD "1.5 V" -to SW_1
set_instance_assignment -name IO_STANDARD "1.5 V" -to SW_2
set_instance_assignment -name IO_STANDARD "1.5 V" -to SW_3
set_location_assignment PIN_AT9 -to SW_0
set_location_assignment PIN_AU8 -to SW_1
set_location_assignment PIN_AK9 -to SW_2
set_location_assignment PIN_AL9 -to SW_3

#============================================================
# LED
#============================================================
set_instance_assignment -name IO_STANDARD "1.5 V" -to LED_0
set_instance_assignment -name IO_STANDARD "1.5 V" -to LED_1
set_instance_assignment -name IO_STANDARD "1.5 V" -to LED_2
set_instance_assignment -name IO_STANDARD "1.5 V" -to LED_3
set_location_assignment PIN_AT32 -to LED_0
set_location_assignment PIN_BA31 -to LED_1
set_location_assignment PIN_AN27 -to LED_2
set_location_assignment PIN_AH27 -to LED_3

#============================================================
# FAN
#============================================================
set_instance_assignment -name IO_STANDARD "1.5 V" -to FAN_ALERT_n
set_location_assignment PIN_AM11 -to FAN_ALERT_n

#============================================================
# SSRAM
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to SSRAM_CLK
set_instance_assignment -name IO_STANDARD "2.5 V" -to SSRAM_CKE_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to SSRAM_CE_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to SSRAM_WE_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to SSRAM_OE_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to SSRAM_ADV
set_instance_assignment -name IO_STANDARD "2.5 V" -to SSRAM_BWA_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to SSRAM_BWB_n
set_location_assignment PIN_AP13 -to SSRAM_CLK
set_location_assignment PIN_AW24 -to SSRAM_CKE_n
set_location_assignment PIN_AP24 -to SSRAM_CE_n
set_location_assignment PIN_AV11 -to SSRAM_WE_n
set_location_assignment PIN_AU10 -to SSRAM_OE_n
set_location_assignment PIN_BC26 -to SSRAM_ADV
set_location_assignment PIN_AY25 -to SSRAM_BWA_n
set_location_assignment PIN_BA24 -to SSRAM_BWB_n

#============================================================
# FLASH
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to FLASH_CLK
set_instance_assignment -name IO_STANDARD "2.5 V" -to FLASH_CE_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FLASH_WE_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FLASH_OE_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FLASH_ADV_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FLASH_RESET_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FLASH_RDY_BSY_n
set_location_assignment PIN_AU11 -to FLASH_CLK
set_location_assignment PIN_AU24 -to FLASH_CE_n
set_location_assignment PIN_AT12 -to FLASH_WE_n
set_location_assignment PIN_AP12 -to FLASH_OE_n
set_location_assignment PIN_BD26 -to FLASH_ADV_n
set_location_assignment PIN_AV25 -to FLASH_RESET_n
set_location_assignment PIN_AU25 -to FLASH_RDY_BSY_n

#============================================================
# FSM
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_15
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_16
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_17
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_18
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_19
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_20
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_21
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_22
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_23
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_24
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_25
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_A_26
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FSM_D_15
set_location_assignment PIN_AE11 -to FSM_A_1
set_location_assignment PIN_AD14 -to FSM_A_2
set_location_assignment PIN_AE14 -to FSM_A_3
set_location_assignment PIN_AE10 -to FSM_A_4
set_location_assignment PIN_AF10 -to FSM_A_5
set_location_assignment PIN_AE12 -to FSM_A_6
set_location_assignment PIN_AF11 -to FSM_A_7
set_location_assignment PIN_AG13 -to FSM_A_8
set_location_assignment PIN_AJ10 -to FSM_A_9
set_location_assignment PIN_AF13 -to FSM_A_10
set_location_assignment PIN_AE13 -to FSM_A_11
set_location_assignment PIN_AJ11 -to FSM_A_12
set_location_assignment PIN_BD11 -to FSM_A_13
set_location_assignment PIN_AW10 -to FSM_A_14
set_location_assignment PIN_AF14 -to FSM_A_15
set_location_assignment PIN_AY12 -to FSM_A_16
set_location_assignment PIN_AY10 -to FSM_A_17
set_location_assignment PIN_BD10 -to FSM_A_18
set_location_assignment PIN_BB12 -to FSM_A_19
set_location_assignment PIN_BA12 -to FSM_A_20
set_location_assignment PIN_BA10 -to FSM_A_21
set_location_assignment PIN_BC11 -to FSM_A_22
set_location_assignment PIN_AE9 -to FSM_A_23
set_location_assignment PIN_AW11 -to FSM_A_24
set_location_assignment PIN_BC10 -to FSM_A_25
set_location_assignment PIN_BB11 -to FSM_A_26
set_location_assignment PIN_AG10 -to FSM_D_0
set_location_assignment PIN_AH10 -to FSM_D_1
set_location_assignment PIN_AG11 -to FSM_D_2
set_location_assignment PIN_AK12 -to FSM_D_3
set_location_assignment PIN_AV10 -to FSM_D_4
set_location_assignment PIN_AR12 -to FSM_D_5
set_location_assignment PIN_AL12 -to FSM_D_6
set_location_assignment PIN_AR13 -to FSM_D_7
set_location_assignment PIN_AG9 -to FSM_D_8
set_location_assignment PIN_AH12 -to FSM_D_9
set_location_assignment PIN_AG12 -to FSM_D_10
set_location_assignment PIN_AL11 -to FSM_D_11
set_location_assignment PIN_AN12 -to FSM_D_12
set_location_assignment PIN_AU9 -to FSM_D_13
set_location_assignment PIN_AM13 -to FSM_D_14
set_location_assignment PIN_AJ12 -to FSM_D_15

#============================================================
# SD Card
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to SD_CLK
set_instance_assignment -name IO_STANDARD "2.5 V" -to SD_DATA_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to SD_DATA_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to SD_DATA_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to SD_DATA_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to SD_CMD
set_location_assignment PIN_BB39 -to SD_CLK
set_location_assignment PIN_AV37 -to SD_DATA_0
set_location_assignment PIN_AY37 -to SD_DATA_1
set_location_assignment PIN_BB36 -to SD_DATA_2
set_location_assignment PIN_AW37 -to SD_DATA_3
set_location_assignment PIN_BA36 -to SD_CMD

#============================================================
# DDR3
#============================================================
set_instance_assignment -name IO_STANDARD LVDS -to DDR3_REFCLK_p
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[8] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[9] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[10] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[11] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[12] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[13] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[14]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_A[15]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_BA[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_BA[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_BA[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_CK[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_CK[1]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_CK_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_CK_n[1]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_CKE[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_CKE[1]
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS_n[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS_n[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS_n[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS_n[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS_n[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS_n[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "DIFFERENTIAL 1.5-V SSTL CLASS I" -to DDR3_DQS_n[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[8] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[9] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[10] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[11] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[12] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[13] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[14] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[15] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[16] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[17] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[18] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[19] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[20] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[21] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[22] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[23] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[24] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[25] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[26] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[27] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[28] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[29] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[30] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[31] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[32] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[33] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[34] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[35] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[36] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[37] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[38] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[39] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[40] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[41] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[42] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[43] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[44] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[45] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[46] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[47] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[48] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[49] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[50] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[51] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[52] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[53] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[54] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[55] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[56] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[57] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[58] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[59] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[60] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[61] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[62] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DQ[63] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DM[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DM[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DM[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DM[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DM[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DM[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DM[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_DM[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_CS_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_CS_n[1]
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_WE_n -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_CAS_n -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_RAS_n -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD 1.5V -to DDR3_RESET_n -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_ODT[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "SSTL-15 CLASS I" -to DDR3_ODT[1]
set_instance_assignment -name IO_STANDARD "1.5 V" -to DDR3_EVENT_n
set_instance_assignment -name IO_STANDARD "1.5 V" -to DDR3_SCL
set_instance_assignment -name IO_STANDARD "1.5 V" -to DDR3_SDA
set_location_assignment PIN_BB33 -to DDR3_REFCLK_p
set_location_assignment PIN_AM32 -to DDR3_A[0]
set_location_assignment PIN_AF31 -to DDR3_A[1]
set_location_assignment PIN_AJ33 -to DDR3_A[2]
set_location_assignment PIN_AE31 -to DDR3_A[3]
set_location_assignment PIN_AP33 -to DDR3_A[4]
set_location_assignment PIN_AG32 -to DDR3_A[5]
set_location_assignment PIN_AN33 -to DDR3_A[6]
set_location_assignment PIN_AK33 -to DDR3_A[7]
set_location_assignment PIN_AF32 -to DDR3_A[8]
set_location_assignment PIN_AH33 -to DDR3_A[9]
set_location_assignment PIN_AE30 -to DDR3_A[10]
set_location_assignment PIN_BA33 -to DDR3_A[11]
set_location_assignment PIN_AG33 -to DDR3_A[12]
set_location_assignment PIN_AD32 -to DDR3_A[13]
set_location_assignment PIN_BA34 -to DDR3_A[14]
set_location_assignment PIN_AY33 -to DDR3_A[15]
set_location_assignment PIN_AE29 -to DDR3_BA[0]
set_location_assignment PIN_AK32 -to DDR3_BA[1]
set_location_assignment PIN_AE34 -to DDR3_BA[2]
set_location_assignment PIN_AR31 -to DDR3_CK[0]
set_location_assignment PIN_AV34 -to DDR3_CK[1]
set_location_assignment PIN_AR32 -to DDR3_CK_n[0]
set_location_assignment PIN_AW33 -to DDR3_CK_n[1]
set_location_assignment PIN_AF34 -to DDR3_CKE[0]
set_location_assignment PIN_AY34 -to DDR3_CKE[1]
set_location_assignment PIN_AL30 -to DDR3_DQS[0]
set_location_assignment PIN_AK30 -to DDR3_DQS[1]
set_location_assignment PIN_AE27 -to DDR3_DQS[2]
set_location_assignment PIN_AY30 -to DDR3_DQS[3]
set_location_assignment PIN_BC28 -to DDR3_DQS[4]
set_location_assignment PIN_AT26 -to DDR3_DQS[5]
set_location_assignment PIN_AR27 -to DDR3_DQS[6]
set_location_assignment PIN_AJ25 -to DDR3_DQS[7]
set_location_assignment PIN_AL31 -to DDR3_DQS_n[0]
set_location_assignment PIN_AL29 -to DDR3_DQS_n[1]
set_location_assignment PIN_AE28 -to DDR3_DQS_n[2]
set_location_assignment PIN_BA29 -to DDR3_DQS_n[3]
set_location_assignment PIN_BD28 -to DDR3_DQS_n[4]
set_location_assignment PIN_AU26 -to DDR3_DQS_n[5]
set_location_assignment PIN_AR28 -to DDR3_DQS_n[6]
set_location_assignment PIN_AJ26 -to DDR3_DQS_n[7]
set_location_assignment PIN_AH31 -to DDR3_DQ[0]
set_location_assignment PIN_AJ31 -to DDR3_DQ[1]
set_location_assignment PIN_AN30 -to DDR3_DQ[2]
set_location_assignment PIN_AP30 -to DDR3_DQ[3]
set_location_assignment PIN_AH30 -to DDR3_DQ[4]
set_location_assignment PIN_AJ30 -to DDR3_DQ[5]
set_location_assignment PIN_AR30 -to DDR3_DQ[6]
set_location_assignment PIN_AT30 -to DDR3_DQ[7]
set_location_assignment PIN_AM29 -to DDR3_DQ[8]
set_location_assignment PIN_AN28 -to DDR3_DQ[9]
set_location_assignment PIN_AP28 -to DDR3_DQ[10]
set_location_assignment PIN_AR29 -to DDR3_DQ[11]
set_location_assignment PIN_AU31 -to DDR3_DQ[12]
set_location_assignment PIN_AV32 -to DDR3_DQ[13]
set_location_assignment PIN_AW32 -to DDR3_DQ[14]
set_location_assignment PIN_AV31 -to DDR3_DQ[15]
set_location_assignment PIN_AF28 -to DDR3_DQ[16]
set_location_assignment PIN_AF29 -to DDR3_DQ[17]
set_location_assignment PIN_AG30 -to DDR3_DQ[18]
set_location_assignment PIN_AG29 -to DDR3_DQ[19]
set_location_assignment PIN_AG28 -to DDR3_DQ[20]
set_location_assignment PIN_AG27 -to DDR3_DQ[21]
set_location_assignment PIN_AG26 -to DDR3_DQ[22]
set_location_assignment PIN_AG25 -to DDR3_DQ[23]
set_location_assignment PIN_BC31 -to DDR3_DQ[24]
set_location_assignment PIN_BC32 -to DDR3_DQ[25]
set_location_assignment PIN_BB30 -to DDR3_DQ[26]
set_location_assignment PIN_BD31 -to DDR3_DQ[27]
set_location_assignment PIN_BD32 -to DDR3_DQ[28]
set_location_assignment PIN_BA30 -to DDR3_DQ[29]
set_location_assignment PIN_AY31 -to DDR3_DQ[30]
set_location_assignment PIN_AW30 -to DDR3_DQ[31]
set_location_assignment PIN_BB29 -to DDR3_DQ[32]
set_location_assignment PIN_BB27 -to DDR3_DQ[33]
set_location_assignment PIN_BA27 -to DDR3_DQ[34]
set_location_assignment PIN_AW27 -to DDR3_DQ[35]
set_location_assignment PIN_AY28 -to DDR3_DQ[36]
set_location_assignment PIN_BA28 -to DDR3_DQ[37]
set_location_assignment PIN_AW29 -to DDR3_DQ[38]
set_location_assignment PIN_AY27 -to DDR3_DQ[39]
set_location_assignment PIN_AT27 -to DDR3_DQ[40]
set_location_assignment PIN_AN25 -to DDR3_DQ[41]
set_location_assignment PIN_AM25 -to DDR3_DQ[42]
set_location_assignment PIN_AL25 -to DDR3_DQ[43]
set_location_assignment PIN_AW26 -to DDR3_DQ[44]
set_location_assignment PIN_AV26 -to DDR3_DQ[45]
set_location_assignment PIN_AU27 -to DDR3_DQ[46]
set_location_assignment PIN_AM26 -to DDR3_DQ[47]
set_location_assignment PIN_AU28 -to DDR3_DQ[48]
set_location_assignment PIN_AU29 -to DDR3_DQ[49]
set_location_assignment PIN_AM28 -to DDR3_DQ[50]
set_location_assignment PIN_AL27 -to DDR3_DQ[51]
set_location_assignment PIN_AV28 -to DDR3_DQ[52]
set_location_assignment PIN_AV29 -to DDR3_DQ[53]
set_location_assignment PIN_AL28 -to DDR3_DQ[54]
set_location_assignment PIN_AK27 -to DDR3_DQ[55]
set_location_assignment PIN_AK24 -to DDR3_DQ[56]
set_location_assignment PIN_AJ24 -to DDR3_DQ[57]
set_location_assignment PIN_AH24 -to DDR3_DQ[58]
set_location_assignment PIN_AH25 -to DDR3_DQ[59]
set_location_assignment PIN_AH28 -to DDR3_DQ[60]
set_location_assignment PIN_AJ28 -to DDR3_DQ[61]
set_location_assignment PIN_AL26 -to DDR3_DQ[62]
set_location_assignment PIN_AK26 -to DDR3_DQ[63]
set_location_assignment PIN_AU32 -to DDR3_DM[0]
set_location_assignment PIN_AU30 -to DDR3_DM[1]
set_location_assignment PIN_AK29 -to DDR3_DM[2]
set_location_assignment PIN_BB32 -to DDR3_DM[3]
set_location_assignment PIN_BD29 -to DDR3_DM[4]
set_location_assignment PIN_AR26 -to DDR3_DM[5]
set_location_assignment PIN_AP27 -to DDR3_DM[6]
set_location_assignment PIN_AJ27 -to DDR3_DM[7]
set_location_assignment PIN_AP31 -to DDR3_CS_n[0]
set_location_assignment PIN_AD33 -to DDR3_CS_n[1]
set_location_assignment PIN_AE32 -to DDR3_WE_n
set_location_assignment PIN_AE33 -to DDR3_CAS_n
set_location_assignment PIN_AJ32 -to DDR3_RAS_n
set_location_assignment PIN_AR33 -to DDR3_RESET_n
set_location_assignment PIN_AN31 -to DDR3_ODT[0]
set_location_assignment PIN_AM31 -to DDR3_ODT[1]
set_location_assignment PIN_AU35 -to DDR3_EVENT_n
set_location_assignment PIN_AT29 -to DDR3_SCL
set_location_assignment PIN_AJ29 -to DDR3_SDA

set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[8] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[8] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[9] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[9] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[10] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[10] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[11] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[11] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[12] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[12] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[13] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[13] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[14] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[14] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[15] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[15] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[16] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[16] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[17] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[17] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[18] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[18] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[19] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[19] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[20] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[20] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[21] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[21] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[22] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[22] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[23] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[23] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[24] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[24] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[25] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[25] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[26] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[26] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[27] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[27] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[28] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[28] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[29] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[29] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[30] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[30] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[31] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[31] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[32] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[32] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[33] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[33] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[34] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[34] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[35] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[35] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[36] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[36] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[37] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[37] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[38] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[38] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[39] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[39] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[40] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[40] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[41] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[41] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[42] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[42] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[43] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[43] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[44] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[44] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[45] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[45] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[46] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[46] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[47] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[47] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[48] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[48] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[49] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[49] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[50] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[50] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[51] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[51] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[52] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[52] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[53] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[53] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[54] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[54] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[55] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[55] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[56] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[56] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[57] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[57] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[58] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[58] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[59] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[59] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[60] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[60] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[61] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[61] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[62] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[62] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQ[63] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQ[63] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name INPUT_TERMINATION "PARALLEL 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DQS_n[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_CK[0] -tag __main_DDR3_IP_p0
#set_instance_assignment -name TERMINATION_CONTROL_BLOCK "intern|mod|ddr|island|blackbox|DDR3_IP|oct0|sd1a_0" -to DDR3_CK[0] -tag __main_DDR3_IP_p0
#set_instance_assignment -name TERMINATION_CONTROL_BLOCK "intern|mod|ddr|island|blackbox|DDR3_IP|oct0|sd1a_0" -to DDR3_CK_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_CK_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[10] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[11] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[12] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[13] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[8] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_A[9] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_BA[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_BA[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_BA[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_CS_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_WE_n -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_RAS_n -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_CAS_n -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_CKE[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_ODT[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name CURRENT_STRENGTH_NEW "MAXIMUM CURRENT" -to DDR3_RESET_n -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DM[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DM[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DM[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DM[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DM[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DM[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DM[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name OUTPUT_TERMINATION "SERIES 50 OHM WITH CALIBRATION" -to DDR3_DM[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[8] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[9] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[10] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[11] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[12] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[13] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[14] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[15] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[16] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[17] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[18] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[19] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[20] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[21] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[22] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[23] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[24] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[25] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[26] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[27] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[28] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[29] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[30] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[31] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[32] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[33] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[34] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[35] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[36] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[37] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[38] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[39] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[40] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[41] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[42] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[43] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[44] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[45] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[46] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[47] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[48] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[49] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[50] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[51] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[52] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[53] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[54] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[55] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[56] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[57] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[58] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[59] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[60] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[61] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[62] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQ[63] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DM[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DM[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DM[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DM[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DM[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DM[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DM[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DM[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS_n[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS_n[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS_n[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS_n[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS_n[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS_n[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_DQS_n[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[10] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[11] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[12] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[13] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[8] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_A[9] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_BA[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_BA[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_BA[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_CS_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_WE_n -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_RAS_n -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_CAS_n -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_CKE[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_ODT[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_RESET_n -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_CK[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name PACKAGE_SKEW_COMPENSATION ON -to DDR3_CK_n[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL "DUAL-REGIONAL CLOCK" -to intern|mod|ddr|island|blackbox|DDR3_IP|pll0|pll_avl_clk -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL "DUAL-REGIONAL CLOCK" -to intern|mod|ddr|island|blackbox|DDR3_IP|pll0|pll_config_clk -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL "GLOBAL CLOCK" -to intern|mod|ddr|island|blackbox|DDR3_IP|pll0|afi_clk -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL "DUAL-REGIONAL CLOCK" -to intern|mod|ddr|island|blackbox|DDR3_IP|pll0|pll_hr_clk -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL "GLOBAL CLOCK" -to intern|mod|ddr|island|blackbox|DDR3_IP|pll0|pll_p2c_read_clk -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL OFF -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|ureset|phy_reset_n -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL OFF -to intern|mod|ddr|island|blackbox|DDR3_IP|s0|sequencer_rw_mgr_inst|rw_mgr_inst|rw_mgr_core_inst|rw_soft_reset_n -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL OFF -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|uread_datapath|reset_n_fifo_wraddress[0] -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL OFF -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|uread_datapath|reset_n_fifo_wraddress[1] -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL OFF -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|uread_datapath|reset_n_fifo_wraddress[2] -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL OFF -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|uread_datapath|reset_n_fifo_wraddress[3] -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL OFF -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|uread_datapath|reset_n_fifo_wraddress[4] -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL OFF -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|uread_datapath|reset_n_fifo_wraddress[5] -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL OFF -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|uread_datapath|reset_n_fifo_wraddress[6] -tag __main_DDR3_IP_p0
set_instance_assignment -name GLOBAL_SIGNAL OFF -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|uread_datapath|reset_n_fifo_wraddress[7] -tag __main_DDR3_IP_p0
set_instance_assignment -name ENABLE_BENEFICIAL_SKEW_OPTIMIZATION_FOR_NON_GLOBAL_CLOCKS ON -to intern|mod|ddr|island|blackbox|DDR3_IP -tag __main_DDR3_IP_p0
set_instance_assignment -name PLL_COMPENSATION_MODE DIRECT -to intern|mod|ddr|island|blackbox|DDR3_IP|pll0|fbout -tag __main_DDR3_IP_p0
set_instance_assignment -name MAX_FANOUT 4 -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|uio_pads|wrdata_en_qr_to_hr|dataout_r[*][*] -tag __main_DDR3_IP_p0
set_instance_assignment -name FORM_DDR_CLUSTERING_CLIQUE ON -to intern|mod|ddr|island|blackbox|DDR3_IP|p0|umemphy|*qr_to_hr* -tag __main_DDR3_IP_p0

#============================================================
# RZQ
#============================================================
set_instance_assignment -name IO_STANDARD "SSTL-15" -to RZQ_DDR3 -tag __main_DDR3_IP_p0
set_instance_assignment -name IO_STANDARD "2.5 V" -to RZQ_FMC
set_location_assignment PIN_AR8 -to RZQ_DDR3
set_location_assignment PIN_H9 -to RZQ_FMC

#============================================================
# Uart to USB
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to UART_TX
set_instance_assignment -name IO_STANDARD "2.5 V" -to UART_RX
set_location_assignment PIN_T26 -to UART_TX
set_location_assignment PIN_T25 -to UART_RX

#============================================================
# TPS40422
#============================================================
set_instance_assignment -name IO_STANDARD "1.5 V" -to TPS40422_CLK
set_instance_assignment -name IO_STANDARD "1.5 V" -to TPS40422_DATA
set_instance_assignment -name IO_STANDARD "1.5 V" -to TPS40422_ALERT
set_location_assignment PIN_AR34 -to TPS40422_CLK
set_location_assignment PIN_BD34 -to TPS40422_DATA
set_location_assignment PIN_AY36 -to TPS40422_ALERT

#============================================================
# External PLL
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to LMK04906_CLK
set_instance_assignment -name IO_STANDARD "2.5 V" -to LMK04906_DATAIN
set_instance_assignment -name IO_STANDARD "1.5 V" -to LMK04906_DATAOUT
set_instance_assignment -name IO_STANDARD "1.5 V" -to LMK04906_LE
set_location_assignment PIN_AT24 -to LMK04906_CLK
set_location_assignment PIN_BD25 -to LMK04906_DATAIN
set_location_assignment PIN_BC29 -to LMK04906_DATAOUT
set_location_assignment PIN_AT33 -to LMK04906_LE

#============================================================
# I2C
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to CLOCK_SCL
set_instance_assignment -name IO_STANDARD "2.5 V" -to CLOCK_SDA
set_location_assignment PIN_AR25 -to CLOCK_SCL
set_location_assignment PIN_BC25 -to CLOCK_SDA

#============================================================
# Shared I2C
#============================================================
set_instance_assignment -name IO_STANDARD "1.5 V" -to FPGA_I2C_SCL
set_instance_assignment -name IO_STANDARD "1.5 V" -to FPGA_I2C_SDA
set_location_assignment PIN_AN11 -to FPGA_I2C_SCL
set_location_assignment PIN_AP9 -to FPGA_I2C_SDA

#============================================================
# Temperature
#============================================================
set_instance_assignment -name IO_STANDARD "1.5 V" -to TEMP_INT_n
set_instance_assignment -name IO_STANDARD "1.5 V" -to TEMP_OVERT_n
set_location_assignment PIN_AT8 -to TEMP_INT_n
set_location_assignment PIN_AR9 -to TEMP_OVERT_n

#============================================================
# POWER Monitor
#============================================================
set_instance_assignment -name IO_STANDARD "1.5 V" -to POWER_MONITOR_ALERT
set_location_assignment PIN_AY9 -to POWER_MONITOR_ALERT

#============================================================
# GPIO
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_15
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_16
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_17
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_18
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_19
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_20
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_21
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_22
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_23
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_24
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_25
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_26
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_27
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_28
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_29
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_30
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_31
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_32
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_33
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_34
set_instance_assignment -name IO_STANDARD "2.5 V" -to GPIO_35
set_location_assignment PIN_AU36 -to GPIO_0
set_location_assignment PIN_AE36 -to GPIO_1
set_location_assignment PIN_AF35 -to GPIO_2
set_location_assignment PIN_AE35 -to GPIO_3
set_location_assignment PIN_AN36 -to GPIO_4
set_location_assignment PIN_AP36 -to GPIO_5
set_location_assignment PIN_AG34 -to GPIO_6
set_location_assignment PIN_AK35 -to GPIO_7
set_location_assignment PIN_AN34 -to GPIO_8
set_location_assignment PIN_AH34 -to GPIO_9
set_location_assignment PIN_AL35 -to GPIO_10
set_location_assignment PIN_AH22 -to GPIO_11
set_location_assignment PIN_AP34 -to GPIO_12
set_location_assignment PIN_AJ23 -to GPIO_13
set_location_assignment PIN_AJ34 -to GPIO_14
set_location_assignment PIN_AJ22 -to GPIO_15
set_location_assignment PIN_AK23 -to GPIO_16
set_location_assignment PIN_AL23 -to GPIO_17
set_location_assignment PIN_AL24 -to GPIO_18
set_location_assignment PIN_AK21 -to GPIO_19
set_location_assignment PIN_AM23 -to GPIO_20
set_location_assignment PIN_AL21 -to GPIO_21
set_location_assignment PIN_AN23 -to GPIO_22
set_location_assignment PIN_AU23 -to GPIO_23
set_location_assignment PIN_AR24 -to GPIO_24
set_location_assignment PIN_BA25 -to GPIO_25
set_location_assignment PIN_AR23 -to GPIO_26
set_location_assignment PIN_BB24 -to GPIO_27
set_location_assignment PIN_BC23 -to GPIO_28
set_location_assignment PIN_AT23 -to GPIO_29
set_location_assignment PIN_AV23 -to GPIO_30
set_location_assignment PIN_BD23 -to GPIO_31
set_location_assignment PIN_BB26 -to GPIO_32
set_location_assignment PIN_AW23 -to GPIO_33
set_location_assignment PIN_AY24 -to GPIO_34
set_location_assignment PIN_BB23 -to GPIO_35

#============================================================
# FMCA
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_CLK_M2C_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_CLK_M2C_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_CLK_M2C_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_CLK_M2C_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_TX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HA_RX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_TX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_HB_RX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_15
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_p_16
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_15
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_TX_n_16
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_p_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_LA_RX_n_14
set_instance_assignment -name IO_STANDARD LVDS -to FMCA_GBTCLK_M2C_p_0
set_instance_assignment -name IO_STANDARD LVDS -to FMCA_GBTCLK_M2C_p_1
set_instance_assignment -name IO_STANDARD LVDS -to FMCA_ONBOARD_REFCLK_p_0
set_instance_assignment -name IO_STANDARD LVDS -to FMCA_ONBOARD_REFCLK_p_1
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_C2M_p_0
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_C2M_p_1
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_C2M_p_2
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_C2M_p_3
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_C2M_p_4
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_C2M_p_5
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_C2M_p_6
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_C2M_p_7
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_C2M_p_8
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_C2M_p_9
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_M2C_p_0
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_M2C_p_1
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_M2C_p_2
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_M2C_p_3
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_M2C_p_4
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_M2C_p_5
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_M2C_p_6
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_M2C_p_7
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_M2C_p_8
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCA_DP_M2C_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_GA_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_GA_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_SCL
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCA_SDA
set_location_assignment PIN_G23 -to FMCA_CLK_M2C_p_0
set_location_assignment PIN_G29 -to FMCA_CLK_M2C_p_1
set_location_assignment PIN_F23 -to FMCA_CLK_M2C_n_0
set_location_assignment PIN_G28 -to FMCA_CLK_M2C_n_1
set_location_assignment PIN_N25 -to FMCA_HA_RX_CLK_p
set_location_assignment PIN_M25 -to FMCA_HA_RX_CLK_n
set_location_assignment PIN_T31 -to FMCA_HA_TX_CLK_p
set_location_assignment PIN_R31 -to FMCA_HA_TX_CLK_n
set_location_assignment PIN_E32 -to FMCA_HB_RX_CLK_p
set_location_assignment PIN_D32 -to FMCA_HB_RX_CLK_n
set_location_assignment PIN_B23 -to FMCA_HB_TX_CLK_p
set_location_assignment PIN_A23 -to FMCA_HB_TX_CLK_n
set_location_assignment PIN_G16 -to FMCA_LA_RX_CLK_p
set_location_assignment PIN_F16 -to FMCA_LA_RX_CLK_n
set_location_assignment PIN_B16 -to FMCA_LA_TX_CLK_p
set_location_assignment PIN_A16 -to FMCA_LA_TX_CLK_n
set_location_assignment PIN_T27 -to FMCA_HA_TX_p_0
set_location_assignment PIN_Y32 -to FMCA_HA_TX_p_1
set_location_assignment PIN_W28 -to FMCA_HA_TX_p_2
set_location_assignment PIN_R27 -to FMCA_HA_TX_p_3
set_location_assignment PIN_P31 -to FMCA_HA_TX_p_4
set_location_assignment PIN_W18 -to FMCA_HA_TX_p_5
set_location_assignment PIN_P23 -to FMCA_HA_TX_p_6
set_location_assignment PIN_J25 -to FMCA_HA_TX_p_7
set_location_assignment PIN_V29 -to FMCA_HA_TX_p_8
set_location_assignment PIN_L30 -to FMCA_HA_TX_p_9
set_location_assignment PIN_H32 -to FMCA_HA_TX_p_10
set_location_assignment PIN_U27 -to FMCA_HA_TX_n_0
set_location_assignment PIN_W31 -to FMCA_HA_TX_n_1
set_location_assignment PIN_V28 -to FMCA_HA_TX_n_2
set_location_assignment PIN_P27 -to FMCA_HA_TX_n_3
set_location_assignment PIN_P30 -to FMCA_HA_TX_n_4
set_location_assignment PIN_V18 -to FMCA_HA_TX_n_5
set_location_assignment PIN_N23 -to FMCA_HA_TX_n_6
set_location_assignment PIN_J24 -to FMCA_HA_TX_n_7
set_location_assignment PIN_U29 -to FMCA_HA_TX_n_8
set_location_assignment PIN_L29 -to FMCA_HA_TX_n_9
set_location_assignment PIN_H31 -to FMCA_HA_TX_n_10
set_location_assignment PIN_Y30 -to FMCA_HA_RX_p_0
set_location_assignment PIN_W32 -to FMCA_HA_RX_p_1
set_location_assignment PIN_U30 -to FMCA_HA_RX_p_2
set_location_assignment PIN_T30 -to FMCA_HA_RX_p_3
set_location_assignment PIN_M27 -to FMCA_HA_RX_p_4
set_location_assignment PIN_Y28 -to FMCA_HA_RX_p_5
set_location_assignment PIN_L23 -to FMCA_HA_RX_p_6
set_location_assignment PIN_H27 -to FMCA_HA_RX_p_7
set_location_assignment PIN_L27 -to FMCA_HA_RX_p_8
set_location_assignment PIN_G32 -to FMCA_HA_RX_p_9
set_location_assignment PIN_E29 -to FMCA_HA_RX_p_10
set_location_assignment PIN_Y29 -to FMCA_HA_RX_n_0
set_location_assignment PIN_V31 -to FMCA_HA_RX_n_1
set_location_assignment PIN_T29 -to FMCA_HA_RX_n_2
set_location_assignment PIN_R30 -to FMCA_HA_RX_n_3
set_location_assignment PIN_L26 -to FMCA_HA_RX_n_4
set_location_assignment PIN_Y27 -to FMCA_HA_RX_n_5
set_location_assignment PIN_K24 -to FMCA_HA_RX_n_6
set_location_assignment PIN_G26 -to FMCA_HA_RX_n_7
set_location_assignment PIN_K27 -to FMCA_HA_RX_n_8
set_location_assignment PIN_F32 -to FMCA_HA_RX_n_9
set_location_assignment PIN_D29 -to FMCA_HA_RX_n_10
set_location_assignment PIN_N28 -to FMCA_HB_TX_p_0
set_location_assignment PIN_M18 -to FMCA_HB_TX_p_1
set_location_assignment PIN_K18 -to FMCA_HB_TX_p_2
set_location_assignment PIN_H17 -to FMCA_HB_TX_p_3
set_location_assignment PIN_G17 -to FMCA_HB_TX_p_4
set_location_assignment PIN_C30 -to FMCA_HB_TX_p_5
set_location_assignment PIN_B26 -to FMCA_HB_TX_p_6
set_location_assignment PIN_E18 -to FMCA_HB_TX_p_7
set_location_assignment PIN_E30 -to FMCA_HB_TX_p_8
set_location_assignment PIN_A29 -to FMCA_HB_TX_p_9
set_location_assignment PIN_B17 -to FMCA_HB_TX_p_10
set_location_assignment PIN_M28 -to FMCA_HB_TX_n_0
set_location_assignment PIN_M17 -to FMCA_HB_TX_n_1
set_location_assignment PIN_K17 -to FMCA_HB_TX_n_2
set_location_assignment PIN_H16 -to FMCA_HB_TX_n_3
set_location_assignment PIN_F17 -to FMCA_HB_TX_n_4
set_location_assignment PIN_B29 -to FMCA_HB_TX_n_5
set_location_assignment PIN_A26 -to FMCA_HB_TX_n_6
set_location_assignment PIN_D18 -to FMCA_HB_TX_n_7
set_location_assignment PIN_D30 -to FMCA_HB_TX_n_8
set_location_assignment PIN_A28 -to FMCA_HB_TX_n_9
set_location_assignment PIN_A17 -to FMCA_HB_TX_n_10
set_location_assignment PIN_R24 -to FMCA_HB_RX_p_0
set_location_assignment PIN_U18 -to FMCA_HB_RX_p_1
set_location_assignment PIN_N19 -to FMCA_HB_RX_p_2
set_location_assignment PIN_J19 -to FMCA_HB_RX_p_3
set_location_assignment PIN_C19 -to FMCA_HB_RX_p_4
set_location_assignment PIN_D27 -to FMCA_HB_RX_p_5
set_location_assignment PIN_B25 -to FMCA_HB_RX_p_6
set_location_assignment PIN_K26 -to FMCA_HB_RX_p_7
set_location_assignment PIN_F26 -to FMCA_HB_RX_p_8
set_location_assignment PIN_C25 -to FMCA_HB_RX_p_9
set_location_assignment PIN_H24 -to FMCA_HB_RX_p_10
set_location_assignment PIN_P24 -to FMCA_HB_RX_n_0
set_location_assignment PIN_T18 -to FMCA_HB_RX_n_1
set_location_assignment PIN_M20 -to FMCA_HB_RX_n_2
set_location_assignment PIN_H19 -to FMCA_HB_RX_n_3
set_location_assignment PIN_C18 -to FMCA_HB_RX_n_4
set_location_assignment PIN_C27 -to FMCA_HB_RX_n_5
set_location_assignment PIN_A25 -to FMCA_HB_RX_n_6
set_location_assignment PIN_K25 -to FMCA_HB_RX_n_7
set_location_assignment PIN_E27 -to FMCA_HB_RX_n_8
set_location_assignment PIN_C24 -to FMCA_HB_RX_n_9
set_location_assignment PIN_H23 -to FMCA_HB_RX_n_10
set_location_assignment PIN_R19 -to FMCA_LA_TX_p_0
set_location_assignment PIN_M23 -to FMCA_LA_TX_p_1
set_location_assignment PIN_W29 -to FMCA_LA_TX_p_2
set_location_assignment PIN_K28 -to FMCA_LA_TX_p_3
set_location_assignment PIN_V27 -to FMCA_LA_TX_p_4
set_location_assignment PIN_H29 -to FMCA_LA_TX_p_5
set_location_assignment PIN_P28 -to FMCA_LA_TX_p_6
set_location_assignment PIN_G31 -to FMCA_LA_TX_p_7
set_location_assignment PIN_N29 -to FMCA_LA_TX_p_8
set_location_assignment PIN_E26 -to FMCA_LA_TX_p_9
set_location_assignment PIN_C28 -to FMCA_LA_TX_p_10
set_location_assignment PIN_P20 -to FMCA_LA_TX_p_11
set_location_assignment PIN_F25 -to FMCA_LA_TX_p_12
set_location_assignment PIN_L20 -to FMCA_LA_TX_p_13
set_location_assignment PIN_E24 -to FMCA_LA_TX_p_14
set_location_assignment PIN_E23 -to FMCA_LA_TX_p_15
set_location_assignment PIN_B19 -to FMCA_LA_TX_p_16
set_location_assignment PIN_R18 -to FMCA_LA_TX_n_0
set_location_assignment PIN_L24 -to FMCA_LA_TX_n_1
set_location_assignment PIN_V30 -to FMCA_LA_TX_n_2
set_location_assignment PIN_J28 -to FMCA_LA_TX_n_3
set_location_assignment PIN_V26 -to FMCA_LA_TX_n_4
set_location_assignment PIN_H28 -to FMCA_LA_TX_n_5
set_location_assignment PIN_P29 -to FMCA_LA_TX_n_6
set_location_assignment PIN_F31 -to FMCA_LA_TX_n_7
set_location_assignment PIN_M30 -to FMCA_LA_TX_n_8
set_location_assignment PIN_D26 -to FMCA_LA_TX_n_9
set_location_assignment PIN_B28 -to FMCA_LA_TX_n_10
set_location_assignment PIN_N20 -to FMCA_LA_TX_n_11
set_location_assignment PIN_F24 -to FMCA_LA_TX_n_12
set_location_assignment PIN_K19 -to FMCA_LA_TX_n_13
set_location_assignment PIN_D24 -to FMCA_LA_TX_n_14
set_location_assignment PIN_D23 -to FMCA_LA_TX_n_15
set_location_assignment PIN_A19 -to FMCA_LA_TX_n_16
set_location_assignment PIN_K30 -to FMCA_LA_RX_p_0
set_location_assignment PIN_V25 -to FMCA_LA_RX_p_1
set_location_assignment PIN_J30 -to FMCA_LA_RX_p_2
set_location_assignment PIN_T28 -to FMCA_LA_RX_p_3
set_location_assignment PIN_J27 -to FMCA_LA_RX_p_4
set_location_assignment PIN_P26 -to FMCA_LA_RX_p_5
set_location_assignment PIN_U24 -to FMCA_LA_RX_p_6
set_location_assignment PIN_T20 -to FMCA_LA_RX_p_7
set_location_assignment PIN_C31 -to FMCA_LA_RX_p_8
set_location_assignment PIN_A32 -to FMCA_LA_RX_p_9
set_location_assignment PIN_H25 -to FMCA_LA_RX_p_10
set_location_assignment PIN_P19 -to FMCA_LA_RX_p_11
set_location_assignment PIN_N17 -to FMCA_LA_RX_p_12
set_location_assignment PIN_G19 -to FMCA_LA_RX_p_13
set_location_assignment PIN_E17 -to FMCA_LA_RX_p_14
set_location_assignment PIN_K29 -to FMCA_LA_RX_n_0
set_location_assignment PIN_U26 -to FMCA_LA_RX_n_1
set_location_assignment PIN_H30 -to FMCA_LA_RX_n_2
set_location_assignment PIN_R28 -to FMCA_LA_RX_n_3
set_location_assignment PIN_H26 -to FMCA_LA_RX_n_4
set_location_assignment PIN_N26 -to FMCA_LA_RX_n_5
set_location_assignment PIN_T24 -to FMCA_LA_RX_n_6
set_location_assignment PIN_T19 -to FMCA_LA_RX_n_7
set_location_assignment PIN_B31 -to FMCA_LA_RX_n_8
set_location_assignment PIN_A31 -to FMCA_LA_RX_n_9
set_location_assignment PIN_G25 -to FMCA_LA_RX_n_10
set_location_assignment PIN_P18 -to FMCA_LA_RX_n_11
set_location_assignment PIN_P17 -to FMCA_LA_RX_n_12
set_location_assignment PIN_F19 -to FMCA_LA_RX_n_13
set_location_assignment PIN_D17 -to FMCA_LA_RX_n_14
set_location_assignment PIN_AB39 -to FMCA_GBTCLK_M2C_p_0
set_location_assignment PIN_V39 -to FMCA_GBTCLK_M2C_p_1
set_location_assignment PIN_Y38 -to FMCA_ONBOARD_REFCLK_p_0
set_location_assignment PIN_T38 -to FMCA_ONBOARD_REFCLK_p_1
set_location_assignment PIN_W41 -to FMCA_DP_C2M_p_0
set_location_assignment PIN_U41 -to FMCA_DP_C2M_p_1
set_location_assignment PIN_R41 -to FMCA_DP_C2M_p_2
set_location_assignment PIN_N41 -to FMCA_DP_C2M_p_3
set_location_assignment PIN_J41 -to FMCA_DP_C2M_p_4
set_location_assignment PIN_K39 -to FMCA_DP_C2M_p_5
set_location_assignment PIN_H39 -to FMCA_DP_C2M_p_6
set_location_assignment PIN_G41 -to FMCA_DP_C2M_p_7
set_location_assignment PIN_E41 -to FMCA_DP_C2M_p_8
set_location_assignment PIN_D39 -to FMCA_DP_C2M_p_9
set_location_assignment PIN_AB43 -to FMCA_DP_M2C_p_0
set_location_assignment PIN_Y43 -to FMCA_DP_M2C_p_1
set_location_assignment PIN_V43 -to FMCA_DP_M2C_p_2
set_location_assignment PIN_T43 -to FMCA_DP_M2C_p_3
set_location_assignment PIN_M43 -to FMCA_DP_M2C_p_4
set_location_assignment PIN_K43 -to FMCA_DP_M2C_p_5
set_location_assignment PIN_H43 -to FMCA_DP_M2C_p_6
set_location_assignment PIN_F43 -to FMCA_DP_M2C_p_7
set_location_assignment PIN_D43 -to FMCA_DP_M2C_p_8
set_location_assignment PIN_C41 -to FMCA_DP_M2C_p_9
set_location_assignment PIN_H18 -to FMCA_GA_0
set_location_assignment PIN_T23 -to FMCA_GA_1
set_location_assignment PIN_F29 -to FMCA_SCL
set_location_assignment PIN_F28 -to FMCA_SDA

#============================================================
# FMCB
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_CLK_M2C_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_CLK_M2C_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_CLK_M2C_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_CLK_M2C_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_15
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_p_16
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_15
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_TX_n_16
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_p_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_LA_RX_n_14
set_instance_assignment -name IO_STANDARD LVDS -to FMCB_GBTCLK_M2C_p_0
set_instance_assignment -name IO_STANDARD LVDS -to FMCB_ONBOARD_REFCLK_p_0
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCB_DP_C2M_p_0
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCB_DP_M2C_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_GA_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_GA_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_SCL
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCB_SDA
set_location_assignment PIN_B39 -to FMCB_CLK_M2C_p_0
set_location_assignment PIN_B37 -to FMCB_CLK_M2C_p_1
set_location_assignment PIN_A38 -to FMCB_CLK_M2C_n_0
set_location_assignment PIN_A37 -to FMCB_CLK_M2C_n_1
set_location_assignment PIN_U36 -to FMCB_LA_RX_CLK_p
set_location_assignment PIN_T36 -to FMCB_LA_RX_CLK_n
set_location_assignment PIN_G37 -to FMCB_LA_TX_CLK_p
set_location_assignment PIN_F36 -to FMCB_LA_TX_CLK_n
set_location_assignment PIN_P33 -to FMCB_LA_TX_p_0
set_location_assignment PIN_V34 -to FMCB_LA_TX_p_1
set_location_assignment PIN_W35 -to FMCB_LA_TX_p_2
set_location_assignment PIN_R33 -to FMCB_LA_TX_p_3
set_location_assignment PIN_T34 -to FMCB_LA_TX_p_4
set_location_assignment PIN_P39 -to FMCB_LA_TX_p_5
set_location_assignment PIN_N38 -to FMCB_LA_TX_p_6
set_location_assignment PIN_M34 -to FMCB_LA_TX_p_7
set_location_assignment PIN_M39 -to FMCB_LA_TX_p_8
set_location_assignment PIN_M37 -to FMCB_LA_TX_p_9
set_location_assignment PIN_J34 -to FMCB_LA_TX_p_10
set_location_assignment PIN_K35 -to FMCB_LA_TX_p_11
set_location_assignment PIN_E33 -to FMCB_LA_TX_p_12
set_location_assignment PIN_G34 -to FMCB_LA_TX_p_13
set_location_assignment PIN_C37 -to FMCB_LA_TX_p_14
set_location_assignment PIN_D36 -to FMCB_LA_TX_p_15
set_location_assignment PIN_C33 -to FMCB_LA_TX_p_16
set_location_assignment PIN_P32 -to FMCB_LA_TX_n_0
set_location_assignment PIN_V33 -to FMCB_LA_TX_n_1
set_location_assignment PIN_V36 -to FMCB_LA_TX_n_2
set_location_assignment PIN_P34 -to FMCB_LA_TX_n_3
set_location_assignment PIN_R34 -to FMCB_LA_TX_n_4
set_location_assignment PIN_P38 -to FMCB_LA_TX_n_5
set_location_assignment PIN_N37 -to FMCB_LA_TX_n_6
set_location_assignment PIN_L33 -to FMCB_LA_TX_n_7
set_location_assignment PIN_M38 -to FMCB_LA_TX_n_8
set_location_assignment PIN_M36 -to FMCB_LA_TX_n_9
set_location_assignment PIN_J33 -to FMCB_LA_TX_n_10
set_location_assignment PIN_K34 -to FMCB_LA_TX_n_11
set_location_assignment PIN_D33 -to FMCB_LA_TX_n_12
set_location_assignment PIN_F34 -to FMCB_LA_TX_n_13
set_location_assignment PIN_B38 -to FMCB_LA_TX_n_14
set_location_assignment PIN_D35 -to FMCB_LA_TX_n_15
set_location_assignment PIN_B32 -to FMCB_LA_TX_n_16
set_location_assignment PIN_W34 -to FMCB_LA_RX_p_0
set_location_assignment PIN_U33 -to FMCB_LA_RX_p_1
set_location_assignment PIN_U35 -to FMCB_LA_RX_p_2
set_location_assignment PIN_T33 -to FMCB_LA_RX_p_3
set_location_assignment PIN_N32 -to FMCB_LA_RX_p_4
set_location_assignment PIN_L32 -to FMCB_LA_RX_p_5
set_location_assignment PIN_L36 -to FMCB_LA_RX_p_6
set_location_assignment PIN_J36 -to FMCB_LA_RX_p_7
set_location_assignment PIN_K37 -to FMCB_LA_RX_p_8
set_location_assignment PIN_H35 -to FMCB_LA_RX_p_9
set_location_assignment PIN_H34 -to FMCB_LA_RX_p_10
set_location_assignment PIN_F35 -to FMCB_LA_RX_p_11
set_location_assignment PIN_C36 -to FMCB_LA_RX_p_12
set_location_assignment PIN_C34 -to FMCB_LA_RX_p_13
set_location_assignment PIN_A35 -to FMCB_LA_RX_p_14
set_location_assignment PIN_V35 -to FMCB_LA_RX_n_0
set_location_assignment PIN_U32 -to FMCB_LA_RX_n_1
set_location_assignment PIN_T35 -to FMCB_LA_RX_n_2
set_location_assignment PIN_T32 -to FMCB_LA_RX_n_3
set_location_assignment PIN_M33 -to FMCB_LA_RX_n_4
set_location_assignment PIN_K32 -to FMCB_LA_RX_n_5
set_location_assignment PIN_L35 -to FMCB_LA_RX_n_6
set_location_assignment PIN_H36 -to FMCB_LA_RX_n_7
set_location_assignment PIN_K36 -to FMCB_LA_RX_n_8
set_location_assignment PIN_G35 -to FMCB_LA_RX_n_9
set_location_assignment PIN_H33 -to FMCB_LA_RX_n_10
set_location_assignment PIN_E35 -to FMCB_LA_RX_n_11
set_location_assignment PIN_B35 -to FMCB_LA_RX_n_12
set_location_assignment PIN_B34 -to FMCB_LA_RX_n_13
set_location_assignment PIN_A34 -to FMCB_LA_RX_n_14
set_location_assignment PIN_AF38 -to FMCB_GBTCLK_M2C_p_0
set_location_assignment PIN_AD39 -to FMCB_ONBOARD_REFCLK_p_0
set_location_assignment PIN_AL41 -to FMCB_DP_C2M_p_0
set_location_assignment PIN_AP43 -to FMCB_DP_M2C_p_0
set_location_assignment PIN_J37 -to FMCB_GA_0
set_location_assignment PIN_H37 -to FMCB_GA_1
set_location_assignment PIN_E36 -to FMCB_SCL
set_location_assignment PIN_D37 -to FMCB_SDA

#============================================================
# FMCC
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_CLK_M2C_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_CLK_M2C_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_CLK_M2C_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_CLK_M2C_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_15
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_p_16
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_15
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_TX_n_16
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_p_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_LA_RX_n_14
set_instance_assignment -name IO_STANDARD LVDS -to FMCC_GBTCLK_M2C_p_0
set_instance_assignment -name IO_STANDARD LVDS -to FMCC_ONBOARD_REFCLK_p_0
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCC_DP_C2M_p_0
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCC_DP_M2C_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_GA_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_GA_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_SCL
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCC_SDA
set_location_assignment PIN_BC19 -to FMCC_CLK_M2C_p_0
set_location_assignment PIN_AJ15 -to FMCC_CLK_M2C_p_1
set_location_assignment PIN_BD19 -to FMCC_CLK_M2C_n_0
set_location_assignment PIN_AJ16 -to FMCC_CLK_M2C_n_1
set_location_assignment PIN_BB18 -to FMCC_LA_RX_CLK_p
set_location_assignment PIN_BB17 -to FMCC_LA_RX_CLK_n
set_location_assignment PIN_AY19 -to FMCC_LA_TX_CLK_p
set_location_assignment PIN_BA19 -to FMCC_LA_TX_CLK_n
set_location_assignment PIN_AU20 -to FMCC_LA_TX_p_0
set_location_assignment PIN_AU16 -to FMCC_LA_TX_p_1
set_location_assignment PIN_AG17 -to FMCC_LA_TX_p_2
set_location_assignment PIN_AJ17 -to FMCC_LA_TX_p_3
set_location_assignment PIN_AJ19 -to FMCC_LA_TX_p_4
set_location_assignment PIN_AU17 -to FMCC_LA_TX_p_5
set_location_assignment PIN_AL18 -to FMCC_LA_TX_p_6
set_location_assignment PIN_AV19 -to FMCC_LA_TX_p_7
set_location_assignment PIN_AW20 -to FMCC_LA_TX_p_8
set_location_assignment PIN_BC20 -to FMCC_LA_TX_p_9
set_location_assignment PIN_AU21 -to FMCC_LA_TX_p_10
set_location_assignment PIN_AY21 -to FMCC_LA_TX_p_11
set_location_assignment PIN_AR17 -to FMCC_LA_TX_p_12
set_location_assignment PIN_AL20 -to FMCC_LA_TX_p_13
set_location_assignment PIN_AN19 -to FMCC_LA_TX_p_14
set_location_assignment PIN_AP21 -to FMCC_LA_TX_p_15
set_location_assignment PIN_AM22 -to FMCC_LA_TX_p_16
set_location_assignment PIN_AV20 -to FMCC_LA_TX_n_0
set_location_assignment PIN_AV16 -to FMCC_LA_TX_n_1
set_location_assignment PIN_AG18 -to FMCC_LA_TX_n_2
set_location_assignment PIN_AK17 -to FMCC_LA_TX_n_3
set_location_assignment PIN_AJ20 -to FMCC_LA_TX_n_4
set_location_assignment PIN_AV17 -to FMCC_LA_TX_n_5
set_location_assignment PIN_AL19 -to FMCC_LA_TX_n_6
set_location_assignment PIN_AW19 -to FMCC_LA_TX_n_7
set_location_assignment PIN_AW21 -to FMCC_LA_TX_n_8
set_location_assignment PIN_BD20 -to FMCC_LA_TX_n_9
set_location_assignment PIN_AU22 -to FMCC_LA_TX_n_10
set_location_assignment PIN_BA21 -to FMCC_LA_TX_n_11
set_location_assignment PIN_AT17 -to FMCC_LA_TX_n_12
set_location_assignment PIN_AM20 -to FMCC_LA_TX_n_13
set_location_assignment PIN_AP19 -to FMCC_LA_TX_n_14
set_location_assignment PIN_AR21 -to FMCC_LA_TX_n_15
set_location_assignment PIN_AN22 -to FMCC_LA_TX_n_16
set_location_assignment PIN_AY22 -to FMCC_LA_RX_p_0
set_location_assignment PIN_AG21 -to FMCC_LA_RX_p_1
set_location_assignment PIN_AG19 -to FMCC_LA_RX_p_2
set_location_assignment PIN_AK20 -to FMCC_LA_RX_p_3
set_location_assignment PIN_BB20 -to FMCC_LA_RX_p_4
set_location_assignment PIN_BC22 -to FMCC_LA_RX_p_5
set_location_assignment PIN_AM19 -to FMCC_LA_RX_p_6
set_location_assignment PIN_AP18 -to FMCC_LA_RX_p_7
set_location_assignment PIN_AN17 -to FMCC_LA_RX_p_8
set_location_assignment PIN_AR18 -to FMCC_LA_RX_p_9
set_location_assignment PIN_AR20 -to FMCC_LA_RX_p_10
set_location_assignment PIN_AU18 -to FMCC_LA_RX_p_11
set_location_assignment PIN_AW16 -to FMCC_LA_RX_p_12
set_location_assignment PIN_AR22 -to FMCC_LA_RX_p_13
set_location_assignment PIN_AV22 -to FMCC_LA_RX_p_14
set_location_assignment PIN_BA22 -to FMCC_LA_RX_n_0
set_location_assignment PIN_AH21 -to FMCC_LA_RX_n_1
set_location_assignment PIN_AG20 -to FMCC_LA_RX_n_2
set_location_assignment PIN_AJ21 -to FMCC_LA_RX_n_3
set_location_assignment PIN_BB21 -to FMCC_LA_RX_n_4
set_location_assignment PIN_BD22 -to FMCC_LA_RX_n_5
set_location_assignment PIN_AN20 -to FMCC_LA_RX_n_6
set_location_assignment PIN_AR19 -to FMCC_LA_RX_n_7
set_location_assignment PIN_AP16 -to FMCC_LA_RX_n_8
set_location_assignment PIN_AT18 -to FMCC_LA_RX_n_9
set_location_assignment PIN_AT20 -to FMCC_LA_RX_n_10
set_location_assignment PIN_AU19 -to FMCC_LA_RX_n_11
set_location_assignment PIN_AW17 -to FMCC_LA_RX_n_12
set_location_assignment PIN_AT21 -to FMCC_LA_RX_n_13
set_location_assignment PIN_AW22 -to FMCC_LA_RX_n_14
set_location_assignment PIN_AF7 -to FMCC_GBTCLK_M2C_p_0
set_location_assignment PIN_AD6 -to FMCC_ONBOARD_REFCLK_p_0
set_location_assignment PIN_AL4 -to FMCC_DP_C2M_p_0
set_location_assignment PIN_AP2 -to FMCC_DP_M2C_p_0
set_location_assignment PIN_AY16 -to FMCC_GA_0
set_location_assignment PIN_BA16 -to FMCC_GA_1
set_location_assignment PIN_AH18 -to FMCC_SCL
set_location_assignment PIN_AH19 -to FMCC_SDA

#============================================================
# FMCD
#============================================================
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_CLK_M2C_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_CLK_M2C_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_CLK_M2C_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_CLK_M2C_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_CLK_p
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_CLK_n
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_TX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HA_RX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_TX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_HB_RX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_15
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_p_16
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_15
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_TX_n_16
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_p_14
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_2
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_3
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_4
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_5
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_6
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_7
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_8
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_10
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_11
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_12
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_13
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_LA_RX_n_14
set_instance_assignment -name IO_STANDARD LVDS -to FMCD_GBTCLK_M2C_p_0
set_instance_assignment -name IO_STANDARD LVDS -to FMCD_GBTCLK_M2C_p_1
set_instance_assignment -name IO_STANDARD LVDS -to FMCD_ONBOARD_REFCLK_p_0
set_instance_assignment -name IO_STANDARD LVDS -to FMCD_ONBOARD_REFCLK_p_1
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_C2M_p_0
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_C2M_p_1
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_C2M_p_2
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_C2M_p_3
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_C2M_p_4
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_C2M_p_5
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_C2M_p_6
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_C2M_p_7
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_C2M_p_8
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_C2M_p_9
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_M2C_p_0
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_M2C_p_1
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_M2C_p_2
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_M2C_p_3
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_M2C_p_4
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_M2C_p_5
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_M2C_p_6
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_M2C_p_7
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_M2C_p_8
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to FMCD_DP_M2C_p_9
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_GA_0
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_GA_1
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_SCL
set_instance_assignment -name IO_STANDARD "2.5 V" -to FMCD_SDA
set_location_assignment PIN_Y16 -to FMCD_CLK_M2C_p_0
set_location_assignment PIN_B8 -to FMCD_CLK_M2C_p_1
set_location_assignment PIN_W16 -to FMCD_CLK_M2C_n_0
set_location_assignment PIN_A8 -to FMCD_CLK_M2C_n_1
set_location_assignment PIN_E8 -to FMCD_HA_RX_CLK_p
set_location_assignment PIN_D9 -to FMCD_HA_RX_CLK_n
set_location_assignment PIN_T17 -to FMCD_HA_TX_CLK_p
set_location_assignment PIN_T16 -to FMCD_HA_TX_CLK_n
set_location_assignment PIN_AH13 -to FMCD_HB_RX_CLK_p
set_location_assignment PIN_AJ13 -to FMCD_HB_RX_CLK_n
set_location_assignment PIN_AU14 -to FMCD_HB_TX_CLK_p
set_location_assignment PIN_AV14 -to FMCD_HB_TX_CLK_n
set_location_assignment PIN_M9 -to FMCD_LA_RX_CLK_p
set_location_assignment PIN_L9 -to FMCD_LA_RX_CLK_n
set_location_assignment PIN_F9 -to FMCD_LA_TX_CLK_p
set_location_assignment PIN_E9 -to FMCD_LA_TX_CLK_n
set_location_assignment PIN_G13 -to FMCD_HA_TX_p_0
set_location_assignment PIN_L21 -to FMCD_HA_TX_p_1
set_location_assignment PIN_M14 -to FMCD_HA_TX_p_2
set_location_assignment PIN_R21 -to FMCD_HA_TX_p_3
set_location_assignment PIN_U21 -to FMCD_HA_TX_p_4
set_location_assignment PIN_G20 -to FMCD_HA_TX_p_5
set_location_assignment PIN_B13 -to FMCD_HA_TX_p_6
set_location_assignment PIN_N16 -to FMCD_HA_TX_p_7
set_location_assignment PIN_D14 -to FMCD_HA_TX_p_8
set_location_assignment PIN_K16 -to FMCD_HA_TX_p_9
set_location_assignment PIN_T22 -to FMCD_HA_TX_p_10
set_location_assignment PIN_F13 -to FMCD_HA_TX_n_0
set_location_assignment PIN_K20 -to FMCD_HA_TX_n_1
set_location_assignment PIN_L14 -to FMCD_HA_TX_n_2
set_location_assignment PIN_P21 -to FMCD_HA_TX_n_3
set_location_assignment PIN_T21 -to FMCD_HA_TX_n_4
set_location_assignment PIN_F20 -to FMCD_HA_TX_n_5
set_location_assignment PIN_A13 -to FMCD_HA_TX_n_6
set_location_assignment PIN_M15 -to FMCD_HA_TX_n_7
set_location_assignment PIN_C13 -to FMCD_HA_TX_n_8
set_location_assignment PIN_J16 -to FMCD_HA_TX_n_9
set_location_assignment PIN_R22 -to FMCD_HA_TX_n_10
set_location_assignment PIN_F22 -to FMCD_HA_RX_p_0
set_location_assignment PIN_E15 -to FMCD_HA_RX_p_1
set_location_assignment PIN_H21 -to FMCD_HA_RX_p_2
set_location_assignment PIN_K15 -to FMCD_HA_RX_p_3
set_location_assignment PIN_V19 -to FMCD_HA_RX_p_4
set_location_assignment PIN_H22 -to FMCD_HA_RX_p_5
set_location_assignment PIN_F14 -to FMCD_HA_RX_p_6
set_location_assignment PIN_N22 -to FMCD_HA_RX_p_7
set_location_assignment PIN_C16 -to FMCD_HA_RX_p_8
set_location_assignment PIN_K21 -to FMCD_HA_RX_p_9
set_location_assignment PIN_Y17 -to FMCD_HA_RX_p_10
set_location_assignment PIN_F21 -to FMCD_HA_RX_n_0
set_location_assignment PIN_D15 -to FMCD_HA_RX_n_1
set_location_assignment PIN_H20 -to FMCD_HA_RX_n_2
set_location_assignment PIN_J15 -to FMCD_HA_RX_n_3
set_location_assignment PIN_U20 -to FMCD_HA_RX_n_4
set_location_assignment PIN_G22 -to FMCD_HA_RX_n_5
set_location_assignment PIN_E14 -to FMCD_HA_RX_n_6
set_location_assignment PIN_M22 -to FMCD_HA_RX_n_7
set_location_assignment PIN_C15 -to FMCD_HA_RX_n_8
set_location_assignment PIN_J21 -to FMCD_HA_RX_n_9
set_location_assignment PIN_W17 -to FMCD_HA_RX_n_10
set_location_assignment PIN_AE15 -to FMCD_HB_TX_p_0
set_location_assignment PIN_AJ14 -to FMCD_HB_TX_p_1
set_location_assignment PIN_AN15 -to FMCD_HB_TX_p_2
set_location_assignment PIN_AT14 -to FMCD_HB_TX_p_3
set_location_assignment PIN_AW13 -to FMCD_HB_TX_p_4
set_location_assignment PIN_AK14 -to FMCD_HB_TX_p_5
set_location_assignment PIN_BC16 -to FMCD_HB_TX_p_6
set_location_assignment PIN_AY13 -to FMCD_HB_TX_p_7
set_location_assignment PIN_AF16 -to FMCD_HB_TX_p_8
set_location_assignment PIN_AR15 -to FMCD_HB_TX_p_9
set_location_assignment PIN_BB14 -to FMCD_HB_TX_p_10
set_location_assignment PIN_AE16 -to FMCD_HB_TX_n_0
set_location_assignment PIN_AK15 -to FMCD_HB_TX_n_1
set_location_assignment PIN_AM16 -to FMCD_HB_TX_n_2
set_location_assignment PIN_AU13 -to FMCD_HB_TX_n_3
set_location_assignment PIN_AW14 -to FMCD_HB_TX_n_4
set_location_assignment PIN_AL14 -to FMCD_HB_TX_n_5
set_location_assignment PIN_BD16 -to FMCD_HB_TX_n_6
set_location_assignment PIN_BA13 -to FMCD_HB_TX_n_7
set_location_assignment PIN_AG15 -to FMCD_HB_TX_n_8
set_location_assignment PIN_AR14 -to FMCD_HB_TX_n_9
set_location_assignment PIN_BB15 -to FMCD_HB_TX_n_10
set_location_assignment PIN_AG16 -to FMCD_HB_RX_p_0
set_location_assignment PIN_AG14 -to FMCD_HB_RX_p_1
set_location_assignment PIN_AM14 -to FMCD_HB_RX_p_2
set_location_assignment PIN_AT15 -to FMCD_HB_RX_p_3
set_location_assignment PIN_AY15 -to FMCD_HB_RX_p_4
set_location_assignment PIN_AE17 -to FMCD_HB_RX_p_5
set_location_assignment PIN_AP15 -to FMCD_HB_RX_p_6
set_location_assignment PIN_BC13 -to FMCD_HB_RX_p_7
set_location_assignment PIN_AL15 -to FMCD_HB_RX_p_8
set_location_assignment PIN_AU12 -to FMCD_HB_RX_p_9
set_location_assignment PIN_BC14 -to FMCD_HB_RX_p_10
set_location_assignment PIN_AF17 -to FMCD_HB_RX_n_0
set_location_assignment PIN_AH15 -to FMCD_HB_RX_n_1
set_location_assignment PIN_AN14 -to FMCD_HB_RX_n_2
set_location_assignment PIN_AU15 -to FMCD_HB_RX_n_3
set_location_assignment PIN_BA15 -to FMCD_HB_RX_n_4
set_location_assignment PIN_AE18 -to FMCD_HB_RX_n_5
set_location_assignment PIN_AR16 -to FMCD_HB_RX_n_6
set_location_assignment PIN_BD13 -to FMCD_HB_RX_n_7
set_location_assignment PIN_AL16 -to FMCD_HB_RX_n_8
set_location_assignment PIN_AV13 -to FMCD_HB_RX_n_9
set_location_assignment PIN_BD14 -to FMCD_HB_RX_n_10
set_location_assignment PIN_D11 -to FMCD_LA_TX_p_0
set_location_assignment PIN_B10 -to FMCD_LA_TX_p_1
set_location_assignment PIN_D10 -to FMCD_LA_TX_p_2
set_location_assignment PIN_B7 -to FMCD_LA_TX_p_3
set_location_assignment PIN_E12 -to FMCD_LA_TX_p_4
set_location_assignment PIN_K13 -to FMCD_LA_TX_p_5
set_location_assignment PIN_H12 -to FMCD_LA_TX_p_6
set_location_assignment PIN_K12 -to FMCD_LA_TX_p_7
set_location_assignment PIN_K8 -to FMCD_LA_TX_p_8
set_location_assignment PIN_K10 -to FMCD_LA_TX_p_9
set_location_assignment PIN_P8 -to FMCD_LA_TX_p_10
set_location_assignment PIN_M12 -to FMCD_LA_TX_p_11
set_location_assignment PIN_T10 -to FMCD_LA_TX_p_12
set_location_assignment PIN_N11 -to FMCD_LA_TX_p_13
set_location_assignment PIN_U12 -to FMCD_LA_TX_p_14
set_location_assignment PIN_P12 -to FMCD_LA_TX_p_15
set_location_assignment PIN_U14 -to FMCD_LA_TX_p_16
set_location_assignment PIN_C10 -to FMCD_LA_TX_n_0
set_location_assignment PIN_A10 -to FMCD_LA_TX_n_1
set_location_assignment PIN_C9 -to FMCD_LA_TX_n_2
set_location_assignment PIN_A7 -to FMCD_LA_TX_n_3
set_location_assignment PIN_E11 -to FMCD_LA_TX_n_4
set_location_assignment PIN_J13 -to FMCD_LA_TX_n_5
set_location_assignment PIN_H11 -to FMCD_LA_TX_n_6
set_location_assignment PIN_J12 -to FMCD_LA_TX_n_7
set_location_assignment PIN_J9 -to FMCD_LA_TX_n_8
set_location_assignment PIN_K9 -to FMCD_LA_TX_n_9
set_location_assignment PIN_N8 -to FMCD_LA_TX_n_10
set_location_assignment PIN_L12 -to FMCD_LA_TX_n_11
set_location_assignment PIN_R10 -to FMCD_LA_TX_n_12
set_location_assignment PIN_M11 -to FMCD_LA_TX_n_13
set_location_assignment PIN_U11 -to FMCD_LA_TX_n_14
set_location_assignment PIN_R12 -to FMCD_LA_TX_n_15
set_location_assignment PIN_T14 -to FMCD_LA_TX_n_16
set_location_assignment PIN_H15 -to FMCD_LA_RX_p_0
set_location_assignment PIN_H14 -to FMCD_LA_RX_p_1
set_location_assignment PIN_D12 -to FMCD_LA_RX_p_2
set_location_assignment PIN_B11 -to FMCD_LA_RX_p_3
set_location_assignment PIN_G11 -to FMCD_LA_RX_p_4
set_location_assignment PIN_G10 -to FMCD_LA_RX_p_5
set_location_assignment PIN_L15 -to FMCD_LA_RX_p_6
set_location_assignment PIN_K11 -to FMCD_LA_RX_p_7
set_location_assignment PIN_J10 -to FMCD_LA_RX_p_8
set_location_assignment PIN_P14 -to FMCD_LA_RX_p_9
set_location_assignment PIN_T12 -to FMCD_LA_RX_p_10
set_location_assignment PIN_P13 -to FMCD_LA_RX_p_11
set_location_assignment PIN_U9 -to FMCD_LA_RX_p_12
set_location_assignment PIN_V10 -to FMCD_LA_RX_p_13
set_location_assignment PIN_V12 -to FMCD_LA_RX_p_14
set_location_assignment PIN_G14 -to FMCD_LA_RX_n_0
set_location_assignment PIN_H13 -to FMCD_LA_RX_n_1
set_location_assignment PIN_C12 -to FMCD_LA_RX_n_2
set_location_assignment PIN_A11 -to FMCD_LA_RX_n_3
set_location_assignment PIN_F11 -to FMCD_LA_RX_n_4
set_location_assignment PIN_F10 -to FMCD_LA_RX_n_5
set_location_assignment PIN_K14 -to FMCD_LA_RX_n_6
set_location_assignment PIN_L11 -to FMCD_LA_RX_n_7
set_location_assignment PIN_H10 -to FMCD_LA_RX_n_8
set_location_assignment PIN_N14 -to FMCD_LA_RX_n_9
set_location_assignment PIN_T11 -to FMCD_LA_RX_n_10
set_location_assignment PIN_N13 -to FMCD_LA_RX_n_11
set_location_assignment PIN_T9 -to FMCD_LA_RX_n_12
set_location_assignment PIN_V9 -to FMCD_LA_RX_n_13
set_location_assignment PIN_V11 -to FMCD_LA_RX_n_14
set_location_assignment PIN_AB6 -to FMCD_GBTCLK_M2C_p_0
set_location_assignment PIN_V6 -to FMCD_GBTCLK_M2C_p_1
set_location_assignment PIN_Y7 -to FMCD_ONBOARD_REFCLK_p_0
set_location_assignment PIN_T7 -to FMCD_ONBOARD_REFCLK_p_1
set_location_assignment PIN_W4 -to FMCD_DP_C2M_p_0
set_location_assignment PIN_U4 -to FMCD_DP_C2M_p_1
set_location_assignment PIN_R4 -to FMCD_DP_C2M_p_2
set_location_assignment PIN_N4 -to FMCD_DP_C2M_p_3
set_location_assignment PIN_J4 -to FMCD_DP_C2M_p_4
set_location_assignment PIN_K6 -to FMCD_DP_C2M_p_5
set_location_assignment PIN_H6 -to FMCD_DP_C2M_p_6
set_location_assignment PIN_G4 -to FMCD_DP_C2M_p_7
set_location_assignment PIN_E4 -to FMCD_DP_C2M_p_8
set_location_assignment PIN_D6 -to FMCD_DP_C2M_p_9
set_location_assignment PIN_AB2 -to FMCD_DP_M2C_p_0
set_location_assignment PIN_Y2 -to FMCD_DP_M2C_p_1
set_location_assignment PIN_V2 -to FMCD_DP_M2C_p_2
set_location_assignment PIN_T2 -to FMCD_DP_M2C_p_3
set_location_assignment PIN_M2 -to FMCD_DP_M2C_p_4
set_location_assignment PIN_K2 -to FMCD_DP_M2C_p_5
set_location_assignment PIN_H2 -to FMCD_DP_M2C_p_6
set_location_assignment PIN_F2 -to FMCD_DP_M2C_p_7
set_location_assignment PIN_D2 -to FMCD_DP_M2C_p_8
set_location_assignment PIN_C4 -to FMCD_DP_M2C_p_9
set_location_assignment PIN_C22 -to FMCD_GA_0
set_location_assignment PIN_C21 -to FMCD_GA_1
set_location_assignment PIN_A20 -to FMCD_SCL
set_location_assignment PIN_B20 -to FMCD_SDA

#============================================================
# SMA
#============================================================
set_instance_assignment -name IO_STANDARD "1.5 V" -to SMA_CLKIN_p
set_instance_assignment -name IO_STANDARD "1.5 V" -to SMA_CLKIN_n
set_instance_assignment -name IO_STANDARD "1.5 V" -to SMA_CLKOUT_p
set_instance_assignment -name IO_STANDARD "1.5 V" -to SMA_CLKOUT_n
set_location_assignment PIN_BC8 -to SMA_CLKIN_p
set_location_assignment PIN_BD8 -to SMA_CLKIN_n
set_location_assignment PIN_AV8 -to SMA_CLKOUT_p
set_location_assignment PIN_AW9 -to SMA_CLKOUT_n

#============================================================
# PCIE
#============================================================
set_instance_assignment -name IO_STANDARD LVDS -to PCIE_ONBOARD_REFCLK_p
set_instance_assignment -name IO_STANDARD HCSL -to PCIE_REFCLK_p
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE_TX_p_0
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE_TX_p_1
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE_TX_p_2
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE_TX_p_3
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE_RX_p_0
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE_RX_p_1
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE_RX_p_2
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to PCIE_RX_p_3
set_instance_assignment -name IO_STANDARD "1.5 V" -to PCIE_PERST_n
set_instance_assignment -name IO_STANDARD "1.5 V" -to PCIE_WAKE_n
set_location_assignment PIN_AH39 -to PCIE_ONBOARD_REFCLK_p
set_location_assignment PIN_AK38 -to PCIE_REFCLK_p
set_location_assignment PIN_AY39 -to PCIE_TX_p_0
set_location_assignment PIN_AV39 -to PCIE_TX_p_1
set_location_assignment PIN_AT39 -to PCIE_TX_p_2
set_location_assignment PIN_AU41 -to PCIE_TX_p_3
set_location_assignment PIN_BB43 -to PCIE_RX_p_0
set_location_assignment PIN_BA41 -to PCIE_RX_p_1
set_location_assignment PIN_AW41 -to PCIE_RX_p_2
set_location_assignment PIN_AY43 -to PCIE_RX_p_3
set_location_assignment PIN_AU33 -to PCIE_PERST_n
set_location_assignment PIN_BD35 -to PCIE_WAKE_n

#============================================================
# SATA
#============================================================
set_instance_assignment -name IO_STANDARD LVDS -to SATA_HOST_REFCLK_p
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to SATA_HOST_TX_p
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to SATA_HOST_RX_p
set_instance_assignment -name IO_STANDARD LVDS -to SATA_DEVICE_REFCLK_p
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to SATA_DEVICE_TX_p
set_instance_assignment -name IO_STANDARD "1.4-V PCML" -to SATA_DEVICE_RX_p
set_location_assignment PIN_AH6 -to SATA_HOST_REFCLK_p
set_location_assignment PIN_AU4 -to SATA_HOST_TX_p
set_location_assignment PIN_AY2 -to SATA_HOST_RX_p
set_location_assignment PIN_AK7 -to SATA_DEVICE_REFCLK_p
set_location_assignment PIN_AY6 -to SATA_DEVICE_TX_p
set_location_assignment PIN_BB2 -to SATA_DEVICE_RX_p

