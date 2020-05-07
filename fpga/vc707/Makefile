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
ROMGEN ?= $(base_dir)/hardware/vlsi_rom_gen_real

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

.PHONY: default
# NOTE: We are going to add the true tapeout stuff here
default: verilog
	mkdir -p $(test_dir)
	# We already had a compilation where the MODEL is the harness and the RocketTile is the top
	# In this case: The TOP is the harness, the RocketTile is the top
	cd $(base_dir) && $(SBT) "project keystoneAcc" "runMain uec.keystoneAcc.uecutils.GenerateTopAndHarness -o $(test_dir)/RocketTile.v -tho $(test_dir)/$(TOP).v -i $(FIRRTL_FILE) --syn-top RocketTile --harness-top $(TOP) -faf $(ANNO_FILE) -tsaof $(test_dir)/top.anno.json -tdf $(test_dir)/RocketTile.top.f -tsf $(test_dir)/RocketTile.fir -thaof $(test_dir)/RocketTile.anno.json -hdf $(test_dir)/top.harness.f -thf $(test_dir)/top.fir --infer-rw --repl-seq-mem -c:$(TOP):-o:$(test_dir)/RocketTile.mems.conf -thconf $(test_dir)/top.mems.conf -td $(test_dir)"

verilog: $(TOP_FILE) $(HARNESS_FILE) $(ROM_FILE)

#########################################################################################
# import other necessary rules and variables
#########################################################################################
include $(base_dir)/hardware/chipyard/common.mk
include $(sim_dir)/vc707.mk

#########################################################################################
# ROM generation
#########################################################################################
#include $(sim_dir)/sdload.mk
#include $(sim_dir)/jumpqspi.mk
#.PHONY: $(SDLOAD_FILE)

#$(ROM_FILE): $(ROM_CONF_FILE) $(SDLOAD_FILE) $(ROMGEN)
#	$(ROMGEN) $(ROM_CONF_FILE) $(SDLOAD_FILE) > $(ROM_FILE)

export BUILD_DIR = $(build_dir)
export CONFIG
export CONFIG_PROJECT = $(CONFIG_PACKAGE).$(MODEL)
export BOARD = vc707
export ROMGEN
export ROM_CONF_FILE
export ROM_FILE
export TEEHW = 1

$(ROM_FILE): $(ROM_CONF_FILE) $(ROMGEN)
	make -C $(bootrom_dir) romgen

#########################################################################################
# general cleanup rule
#########################################################################################
.PHONY: clean
clean:
	rm -rf $(gen_dir) $(sim_prefix)-* $(test_dir)
	make -C $(bootrom_dir) clean
