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

SUB_PROJECT ?= FPGANexys4DDR
BOARD?=Nexys4DDR
ISACONF?=RV32IMAC
HYBRID?=Rocket1
PERIPHERALS?=TEEHWPeripherals
MBUS?=MBus32

# Not supported
BOOTSRC?=QSPI
PCIE?=WoPCIe
DDRCLK?=NoCrossMBusClk
SEPARE?=Nothing
BASE?=MCUWithLinuxConfig

#########################################################################################
# Common rules for FPGA
#########################################################################################
include $(base_dir)/fpga/common.mk

#########################################################################################
# Common rules for the Vivado
#########################################################################################
BUILD_DIR=$(build_dir)
FPGA_DIR=$(base_dir)/hardware/fpga-shells/xilinx
export FPGA_BOARD=nexys4ddr
XDC_SHELL=$(sim_dir)/Nexys4DDR.shell.xdc
TCL_SHELL=$(sim_dir)/Nexys4DDR.shell.vivado.tcl
include $(base_dir)/fpga/Xilinx/common.mk

