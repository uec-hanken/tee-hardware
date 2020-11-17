#########################################################################################
# TR4 FPGA build (verilog only for now)
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
base_dir=$(abspath ../../..)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/generated_src

bootrom_dir=$(base_dir)/software/freedom-u540-c000-bootloader
xip_dir=$(base_dir)/software/xip
SUB_PROJECT ?= FPGATR4
ROMGEN ?= $(base_dir)/hardware/vlsi_rom_gen_real

BOARD?=TR4
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
export FPGA_BOARD=TR4
TCL_SHELL=$(sim_dir)/TR4.shell.quartus.tcl
TCL_QSYS_MAIN=$(sim_dir)/TR4.qsys.main.tcl
include $(base_dir)/fpga/Altera/common.mk

