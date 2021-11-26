#########################################################################################
# vc707 FPGA build
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
teehw_dir=$(abspath ../../..)
base_dir=$(teehw_dir)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/generated_src

SUB_PROJECT ?= FPGAArtyA7
BOARD?=ArtyA7
ISACONF?=RV32IMAC
HYBRID?=Rocket1

# Not supported
MBUS?=MBusNone
BOOTSRC?=QSPI
PCIE?=WoPCIe
DDRCLK?=WoSepaDDRClk
SEPARE?=Nothing

#########################################################################################
# Common rules for FPGA
#########################################################################################
include $(base_dir)/fpga/common.mk

#########################################################################################
# Common rules for the Vivado
#########################################################################################
BUILD_DIR=$(build_dir)
FPGA_DIR=$(base_dir)/hardware/fpga-shells/xilinx
export FPGA_BOARD=arty_a7_100
XDC_SHELL=$(sim_dir)/Arty_A7.shell.xdc
TCL_SHELL=$(sim_dir)/Arty_A7.shell.vivado.tcl
include $(base_dir)/fpga/Xilinx/common.mk

