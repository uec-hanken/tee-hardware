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

WAVEFORM_FLAG=-v$(sim_out_name).vcd

# This file is for simulation only. VLSI flows should replace this file with one containing hard SRAMs
MACROCOMPILER_MODE ?= --mode synflops

# The macro-compiler command creator. Just attach a buch of them, to avoid multiple-execution of sbt
ALL_TOPS := $(TOP) $(MODEL) $(shell echo $(SEPARE) | sed "s/,/ /g")
MACROCOMPILER_COMMANDS = $(foreach T, $(ALL_TOPS),"runMain barstools.macros.MacroCompiler -n $(build_dir)/$T.mems.conf -v $(build_dir)/$T.mems.v -f $(build_dir)/$T.mems.fir $(MACROCOMPILER_MODE)") 

# This is for compiling all into the VRCS
TOP_VERILOGS = $(addprefix $(build_dir)/,$(ALL_TOPS:=.v))
TOP_VERILOG_MEMS = $(addprefix $(build_dir)/,$(ALL_TOPS:=.mems.v))
TOP_F = $(addprefix $(build_dir)/,$(ALL_TOPS:=.f))

VSRCS := \
	$(build_dir)/EICG_wrapper.v \
	$(TOP_VERILOGS) \
	$(TOP_VERILOG_MEMS) \
	$(ROM_FILE)

# NOTE: We are going to add the true tapeout stuff here
default: $(FIRRTL_FILE) $(ROM_FILE)
	# We are going to separate the different tops here
	cd $(base_dir) && $(SBT) "project $(SBT_PROJECT)" "runMain uec.teehardware.uecutils.MultiTopAndHarness -o $(build_dir)/SHOULDNT.v -i $(FIRRTL_FILE) --syn-tops $(SEPARE) --chip-top $(TOP) --harness-top $(MODEL) -faf $(ANNO_FILE) --infer-rw --repl-seq-mem -c:$(TOP):-o:$(build_dir)/SHOULDNT.mems.conf -td $(build_dir)"
	# - rename.ul $(MODEL_PACKAGE).$(MODEL).$(CONFIG) $(MODEL) $(build_dir)/*
	# We also want to generate our memories
	cd $(base_dir) && $(SBT) "project $(SBT_PROJECT)" $(MACROCOMPILER_COMMANDS)

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
	make -C $(sdboot_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) ISACONF=$(ISACONF) SDBOOT_TARGET_ADDR=0x8C000000UL clean
	make -C $(sdboot_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) ISACONF=$(ISACONF) SDBOOT_TARGET_ADDR=0x8C000000UL hex

$(ROM_FILE): $(ROMGEN) $(HEXFILE)
	$(ROMGEN) $(ROM_CONF_FILE) $(HEXFILE) > $(ROM_FILE)

#########################################################################################
# general cleanup rule
#########################################################################################
.PHONY: clean
clean:
	rm -rf $(build_dir) $(sim_prefix)-*
	make -C $(sdboot_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) BOARD=$(BOARD) TEEHW=1 ISACONF=$(ISACONF) clean
	make -C $(xip_dir) ISACONF=$(ISACONF) XIP_TARGET_ADDR=0x20000000 ADD_OPTS=-DSKIP_HANG clean

