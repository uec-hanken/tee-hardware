#########################################################################################
# SakuraX FPGA build
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
teehw_dir=$(abspath ../../..)
base_dir=$(teehw_dir)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/generated_src

SUB_PROJECT ?= FPGASakuraX
BOARD?=SakuraX
ISACONF?=RV64GC
#VC707 can only use MBus64 #TODO
MBUS?=MBus64
BOOTSRC?=BOOTROM
PCIE?=WoPCIe
DDRCLK?=NoCrossMBusClk
HYBRID?=Rocket
SEPARE?=RocketTile,TLAES,TLed25519,TLSHA3

#########################################################################################
# Common rules for FPGA
#########################################################################################
include $(base_dir)/fpga/common.mk

#########################################################################################
# Common rules for the Vivado
#########################################################################################
BUILD_DIR=$(build_dir)
FPGA_DIR=$(base_dir)/hardware/fpga-shells/xilinx
export FPGA_BOARD=sakurax
XDC_SHELL=$(sim_dir)/SakuraX.shell.xdc
TCL_SHELL=$(sim_dir)/SakuraX.shell.vivado.tcl
include $(base_dir)/fpga/Xilinx/common.mk

