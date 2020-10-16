#########################################################################################
# vc707 FPGA build (verilog only for now)
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
base_dir=$(abspath ../../..)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/generated_src/

bootrom_dir=$(base_dir)/software/freedom-u540-c000-bootloader
xip_dir=$(base_dir)/software/xip
SUB_PROJECT ?= FPGAVC707
ROMGEN ?= $(base_dir)/hardware/vlsi_rom_gen_real

BOARD?=VC707
ISACONF?=RV64GC
#VC707 can only use MBus64 #TODO
MBUS?=MBus64
BOOTSRC?=BOOTROM
PCIE?=WoPCIe
DDRCLK?=WoSepaDDRClk
HYBRID?=BoomRocket
SEPARE?=BoomTile,RocketTile,TLAES,TLed25519,TLSHA3

#########################################################################################
# Common rules for FPGA
#########################################################################################
include $(base_dir)/fpga/common.mk

#########################################################################################
# Common rules for the Vivado
#########################################################################################
BUILD_DIR=$(build_dir)
FPGA_DIR=$(base_dir)/fpga-shells/xilinx
export FPGA_BOARD=vc707
XDC=$(sim_dir)/VC707.xdc
include $(base_dir)/fpga/Xilinx/common.mk

