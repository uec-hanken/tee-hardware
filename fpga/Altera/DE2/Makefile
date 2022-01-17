#########################################################################################
# DE2 FPGA build
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
teehw_dir=$(abspath ../../..)
base_dir=$(teehw_dir)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/generated_src

SUB_PROJECT ?= FPGADE2
BOARD?=DE2
ISACONF?=RV32IMAC
BOOTSRC?=BOOTROM
PCIE?=WoPCIe
DDRCLK?=WoSepaDDRClk
HYBRID?=Rocket1
SEPARE?=Nothing

# Not compatible
MBUS=MBusNone
#PERIPHERALS?=TEEHWPeripherals
PERIPHERALS=NoSecurityPeripherals

#########################################################################################
# Common rules for FPGA
#########################################################################################
include $(base_dir)/fpga/common.mk

#########################################################################################
# Common rules for the Quartus
#########################################################################################
BUILD_DIR=$(build_dir)
FPGA_DIR=$(base_dir)/hardware/fpga-shells/altera
export FPGA_BOARD=DE2
TCL_SHELL=$(sim_dir)/DE2.shell.quartus.tcl
TCL_QSYS_MAIN=$(sim_dir)/main.qsys
SDC_FILE=$(sim_dir)/constraints.sdc
include $(base_dir)/fpga/Altera/common.mk

