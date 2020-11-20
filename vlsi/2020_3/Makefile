#########################################################################################
# vc707 FPGA build (verilog only for now)
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
base_dir=$(abspath ../..)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/generated_src

bootrom_dir=$(base_dir)/software/freedom-u540-c000-bootloader
xip_dir=$(base_dir)/software/xip
SUB_PROJECT ?= FPGAVC707
ROMGEN ?= $(base_dir)/hardware/vlsi_rom_gen_real

BOARD=Chip2020ROHM18R3
ISACONF=RV64IMAC
MBUS=MBus32
BOOTSRC=QSPI
PCIE=WoPCIe
DDRCLK=WSepaDDRClk
HYBRID=IbexRocketBoomReduced
SEPARE=BoomTile,RocketTile,IbexTile

#########################################################################################
# Common rules for FPGA
#########################################################################################
include $(base_dir)/fpga/common.mk

#########################################################################################
# Common rules for the Vivado
#########################################################################################
BUILD_DIR=$(build_dir)
FPGA_DIR=$(base_dir)/hardware/fpga-shells/xilinx
export FPGA_BOARD=vc707
XDC_SHELL=$(base_dir)/fpga/Xilinx/VC707/VC707.shell.xdc
TCL_SHELL=$(base_dir)/fpga/Xilinx/VC707/VC707.shell.vivado.tcl
include $(base_dir)/fpga/Xilinx/common.mk

