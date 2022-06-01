#########################################################################################
# include shared variables
#########################################################################################
include $(base_dir)/variables.mk

#########################################################################################
# name of simulator (used to generate *.f arguments file)
#########################################################################################
sim_name = verilator

#########################################################################################
# vcs simulator types and rules
#########################################################################################
sim_prefix = simulator
sim = $(sim_dir)/$(sim_prefix)-$(MODEL_PACKAGE)-$(CONFIG)
sim_debug = $(sim_dir)/$(sim_prefix)-$(MODEL_PACKAGE)-$(CONFIG)-debug
ROM_FILE = $(build_dir)/$(long_name).rom.v
ROM_CONF_FILE = $(build_dir)/$(long_name).rom.conf
DTS_FILE = $(build_dir)/$(long_name).dts

PERMISSIVE_ON=
PERMISSIVE_OFF=

# This is for compiling all into the VRCS
TOP_VERILOGS = $(TOP_FILE) $(HARNESS_FILE)
TOP_VERILOG_MEMS = $(TOP_SMEMS_FILE) $(HARNESS_SMEMS_FILE)
TOP_F = $(sim_top_blackboxes) $(sim_harness_blackboxes)

VSRCS := \
	$(TOP_VERILOGS) \
	$(TOP_VERILOG_MEMS) \
	$(ROM_FILE)

#########################################################################################
# simulaton requirements (TODO: This is for SIMULATING, not for FPGA)
#########################################################################################
SIM_FILE_REQS += \
	$(CHIPYARD_RSRCS_DIR)/csrc/emulator.cc \
	$(ROCKETCHIP_RSRCS_DIR)/csrc/verilator.h \

# the following files are needed for emulator.cc to compile
SIM_FILE_REQS += \
	$(TESTCHIP_RSRCS_DIR)/testchipip/csrc/SimSerial.cc \
	$(TESTCHIP_RSRCS_DIR)/testchipip/csrc/testchip_tsi.cc \
	$(TESTCHIP_RSRCS_DIR)/testchipip/csrc/testchip_tsi.h \
	$(TESTCHIP_RSRCS_DIR)/testchipip/csrc/SimDRAM.cc \
	$(TESTCHIP_RSRCS_DIR)/testchipip/csrc/mm.h \
	$(TESTCHIP_RSRCS_DIR)/testchipip/csrc/mm.cc \
	$(TESTCHIP_RSRCS_DIR)/testchipip/csrc/mm_dramsim2.h \
	$(TESTCHIP_RSRCS_DIR)/testchipip/csrc/mm_dramsim2.cc \
	$(ROCKETCHIP_RSRCS_DIR)/csrc/SimDTM.cc \
	$(ROCKETCHIP_RSRCS_DIR)/csrc/SimJTAG.cc \
	$(ROCKETCHIP_RSRCS_DIR)/csrc/remote_bitbang.h \
	$(ROCKETCHIP_RSRCS_DIR)/csrc/remote_bitbang.cc

# copy files and add -FI for *.h files in *.f
$(sim_files): $(SIM_FILE_REQS) | $(build_dir)
	cp -f $^ $(build_dir)
	$(foreach file,\
		$^,\
		$(if $(filter %.h,$(file)),\
			echo "-FI $(addprefix $(build_dir)/, $(notdir $(file)))" >> $@;,\
			echo "$(addprefix $(build_dir)/, $(notdir $(file)))" >> $@;))

#########################################################################################
# import other necessary rules and variables
#########################################################################################
include $(teehw_dir)/common.mk

#########################################################################################
# ROM generation
#########################################################################################
ROMGEN=$(teehw_dir)/hardware/vlsi_rom_gen_real
xip_dir=$(teehw_dir)/software/xip
sdboot_dir=$(teehw_dir)/software/sdboot
ifeq ($(BOOTSRC),BOOTROM)
HEXFILE?=$(build_dir)/sdboot.hex $(build_dir)/sdboot.hex
else #QSPI
HEXFILE?=$(build_dir)/xip.hex $(build_dir)/sdboot.hex
endif

$(build_dir)/xip.hex:
	make -C $(xip_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) ISACONF=$(ISACONF) XIP_TARGET_ADDR=0x20000000 ADD_OPTS=-DSKIP_HANG clean
	make -C $(xip_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) ISACONF=$(ISACONF) XIP_TARGET_ADDR=0x20000000 ADD_OPTS=-DSKIP_HANG hex

$(build_dir)/sdboot.hex:
	make -C $(sdboot_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) ISACONF=$(ISACONF) SDBOOT_TARGET_ADDR=0x82000000UL clean
	make -C $(sdboot_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) ISACONF=$(ISACONF) SDBOOT_TARGET_ADDR=0x82000000UL hex

$(ROM_FILE): $(ROMGEN) $(HEXFILE)
	$(ROMGEN) $(ROM_CONF_FILE) $(HEXFILE) > $(ROM_FILE)

# If compiling only the ROM files (for measure), is just enough the FIRRTL file (to induce creating the dts)
rom: $(FIRRTL_FILE) $(ROM_FILE)

#########################################################################################
# general cleanup rule
#########################################################################################
.PHONY: clean
clean:
	rm -rf $(build_dir) $(sim_prefix)-*
	make -C $(sdboot_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) BOARD=$(BOARD) TEEHW=1 ISACONF=$(ISACONF) clean
	make -C $(xip_dir) ISACONF=$(ISACONF) XIP_TARGET_ADDR=0x20000000 ADD_OPTS=-DSKIP_HANG clean

