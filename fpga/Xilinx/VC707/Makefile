#########################################################################################
# vc707 FPGA build (verilog only for now)
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
base_dir=$(abspath ../../..)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/VC707.srcs/sources_2

bootrom_dir=$(base_dir)/software/freedom-u540-c000-bootloader
SUB_PROJECT ?= FPGAVC707
ROMGEN ?= $(base_dir)/hardware/vlsi_rom_gen_real

BOARD=VC707
ISACONF=RV64GC
MBUS=MBus64
BOOTSRC=BOOTROM
PCIE=WoPCIe
DDRCLK=WoSepaDDRClk
HYBRID=BoomRocket

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

.PHONY: default
# NOTE: We are going to add the true tapeout stuff here
default: verilog
	cp -rf $(TEEHW_DIR)/optvsrc/* $(build_dir)
	# We already had a compilation where the MODEL is the harness and the RocketTile is the top
	# In this case: The TOP is the harness, the RocketTile is the top
	cd $(base_dir) && $(SBT) "project $(SBT_PROJECT)" "runMain $(MODEL_PACKAGE).uecutils.GenerateTopAndHarness -o $(build_dir)/RocketTile.v -tho $(build_dir)/$(TOP).v -i $(FIRRTL_FILE) --syn-top RocketTile --harness-top $(TOP) -faf $(ANNO_FILE) -tsaof $(build_dir)/top.anno.json -tdf $(build_dir)/RocketTile.top.f -tsf $(build_dir)/RocketTile.fir -thaof $(build_dir)/RocketTile.anno.json -hdf $(build_dir)/top.harness.f -thf $(build_dir)/top.fir --infer-rw --repl-seq-mem -c:$(TOP):-o:$(build_dir)/RocketTile.mems.conf -thconf $(build_dir)/top.mems.conf -td $(build_dir)"
	rename.ul $(MODEL_PACKAGE).$(MODEL).$(CONFIG) $(MODEL) $(build_dir)/*
	# We also want to generate our memories
	cd $(base_dir) && $(SBT) "project barstoolsMacros" "runMain barstools.macros.MacroCompiler -n $(build_dir)/RocketTile.mems.conf -v $(build_dir)/RocketTile.mems.v -f $(build_dir)/RocketTile.mems.fir $(MACROCOMPILER_MODE)"
	cd $(base_dir) && $(SBT) "project barstoolsMacros" "runMain barstools.macros.MacroCompiler -n $(build_dir)/top.mems.conf -v $(build_dir)/top.mems.v -f $(build_dir)/top.mems.fir $(MACROCOMPILER_MODE)"

verilog: $(TOP_FILE) $(HARNESS_FILE) $(ROM_FILE)

#########################################################################################
# import other necessary rules and variables
#########################################################################################
include $(base_dir)/hardware/chipyard/common.mk

#########################################################################################
# ROM generation
#########################################################################################
$(ROM_FILE): $(ROM_CONF_FILE) $(ROMGEN)
	make -C $(bootrom_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) BOARD=$(BOARD) ROMGEN=$(ROMGEN) ROM_CONF_FILE=$(ROM_CONF_FILE) ROM_FILE=$(ROM_FILE) TEEHW=1 ISACONF=$(ISACONF) romgen

#########################################################################################
# general cleanup rule
#########################################################################################
.PHONY: clean
clean:
	rm -rf $(build_dir) $(sim_prefix)-*
	make -C $(bootrom_dir) clean
