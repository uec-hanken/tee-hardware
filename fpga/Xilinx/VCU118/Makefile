#########################################################################################
# vcu118 FPGA build
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
teehw_dir=$(abspath ../../..)
base_dir=$(teehw_dir)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/generated_src

SUB_PROJECT ?= FPGAVCU118
BOARD?=VCU118
ISACONF?=RV64GC
#FPGAVCU118 can only use MBus64 #TODO
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
# Common rules for the Vivado
#########################################################################################
BUILD_DIR=$(build_dir)
FPGA_DIR=$(base_dir)/hardware/fpga-shells/xilinx
export FPGA_BOARD=vcu118
XDC_SHELL=$(sim_dir)/VCU118.shell.xdc
TCL_SHELL=$(sim_dir)/VCU118.shell.vivado.tcl
include $(base_dir)/fpga/Xilinx/common.mk

