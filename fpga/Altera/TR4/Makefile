#########################################################################################
# TR4 FPGA build
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
teehw_dir=$(abspath ../../..)
base_dir=$(teehw_dir)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/generated_src

SUB_PROJECT ?= FPGATR4
BOARD?=TR4
ISACONF?=RV64GC
MBUS?=MBus64
BOOTSRC?=BOOTROM
PCIE?=WoPCIe
DDRCLK?=NoCrossMBusClk
HYBRID?=Rocket
SEPARE?=Nothing

#########################################################################################
# Common rules for FPGA
#########################################################################################
include $(base_dir)/fpga/common.mk

#########################################################################################
# Common rules for the Quartus
#########################################################################################
BUILD_DIR=$(build_dir)
FPGA_DIR=$(base_dir)/hardware/fpga-shells/altera
export FPGA_BOARD=TR4
TCL_SHELL=$(sim_dir)/TR4.shell.quartus.tcl
TCL_QSYS_MAIN=$(sim_dir)/main.qsys
SDC_FILE=$(sim_dir)/constraints.sdc
include $(base_dir)/fpga/Altera/common.mk

