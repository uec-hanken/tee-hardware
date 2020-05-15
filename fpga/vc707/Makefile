#########################################################################################
# vc707 FPGA build (verilog only for now)
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
base_dir=$(abspath ../..)
sim_dir=$(abspath .)
bootrom_dir=$(base_dir)/software/freedom-u540-c000-bootloader
SUB_PROJECT ?= NEDOFPGA
ROMGEN ?= $(base_dir)/vlsi/vlsi_rom_gen_real

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
BOOT_FILE = $(bootrom_dir)/vc707zsbl.hex

PERMISSIVE_ON=
PERMISSIVE_OFF=

WAVEFORM_FLAG=-v$(sim_out_name).vcd

test_dir=$(sim_dir)/chip/

# This file is for simulation only. VLSI flows should replace this file with one containing hard SRAMs
MACROCOMPILER_MODE ?= --mode synflops

.PHONY: default
# NOTE: We are going to add the true tapeout stuff here
default: verilog
	mkdir -p $(test_dir)
	# We already had a compilation where the MODEL is the harness and the RocketTile is the top
	# In this case: The TOP is the harness, the RocketTile is the top
	cd $(base_dir) && $(SBT) "project keystoneAcc" "runMain uec.keystoneAcc.uecutils.GenerateTopAndHarness -o $(test_dir)/RocketTile.v -tho $(test_dir)/$(TOP).v -i $(FIRRTL_FILE) --syn-top BoomTile --harness-top $(TOP) -faf $(ANNO_FILE) -tsaof $(test_dir)/top.anno.json -tdf $(test_dir)/RocketTile.top.f -tsf $(test_dir)/RocketTile.fir -thaof $(test_dir)/RocketTile.anno.json -hdf $(test_dir)/top.harness.f -thf $(test_dir)/top.fir --infer-rw --repl-seq-mem -c:$(TOP):-o:$(test_dir)/RocketTile.mems.conf -thconf $(test_dir)/top.mems.conf -td $(test_dir)"
	# We also want to generate our memories
	cd $(base_dir) && $(SBT) "project barstoolsMacros" "runMain barstools.macros.MacroCompiler -n $(test_dir)/RocketTile.mems.conf -v $(test_dir)/RocketTile.mems.v -f $(test_dir)/RocketTile.mems.fir $(MACROCOMPILER_MODE)"
	cd $(base_dir) && $(SBT) "project barstoolsMacros" "runMain barstools.macros.MacroCompiler -n $(test_dir)/top.mems.conf -v $(test_dir)/top.mems.v -f $(test_dir)/top.mems.fir $(MACROCOMPILER_MODE)"

verilog: $(TOP_FILE) $(HARNESS_FILE) $(ROM_FILE)

#########################################################################################
# import other necessary rules and variables
#########################################################################################
include $(base_dir)/hardware/chipyard/common.mk
include $(sim_dir)/vc707.mk

#########################################################################################
# ROM generation
#########################################################################################
include $(sim_dir)/sdload.mk
#include $(sim_dir)/jumpqspi.mk
.PHONY: $(SDLOAD_FILE)

$(ROM_FILE): $(ROM_CONF_FILE) $(SDLOAD_FILE) $(ROMGEN)
	$(ROMGEN) $(ROM_CONF_FILE) $(SDLOAD_FILE) > $(ROM_FILE)

#########################################################################################
# general cleanup rule
#########################################################################################
.PHONY: clean
clean:
	rm -rf $(gen_dir) $(sim_prefix)-* $(test_dir) 
