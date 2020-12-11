#########################################################################################
# DE4 FPGA build
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
teehw_dir=$(abspath ../../..)
base_dir=$(teehw_dir)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/generated_src

bootrom_dir=$(base_dir)/software/freedom-u540-c000-bootloader
xip_dir=$(base_dir)/software/xip
SUB_PROJECT ?= FPGADE4
ROMGEN ?= $(base_dir)/hardware/vlsi_rom_gen_real

BOARD?=DE4
ISACONF?=RV64GC
MBUS?=MBus64
BOOTSRC?=BOOTROM
PCIE?=WoPCIe
DDRCLK?=WoSepaDDRClk
HYBRID?=Ibex2Rocket
SEPARE?=BoomTile,RocketTile,USB11HS,TLAES,TLed25519,TLSHA3

#########################################################################################
# Common rules for FPGA
#########################################################################################
include $(base_dir)/fpga/common.mk

#########################################################################################
# Common rules for the Quartus
#########################################################################################
BUILD_DIR=$(build_dir)
FPGA_DIR=$(base_dir)/hardware/fpga-shells/altera
export FPGA_BOARD=DE4
TCL_SHELL=$(sim_dir)/DE4.shell.quartus.tcl
TCL_QSYS_MAIN=$(sim_dir)/DE4.qsys.main.tcl
include $(base_dir)/fpga/Altera/common.mk

