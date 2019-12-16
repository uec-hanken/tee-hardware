#########################################################################################
# vc707 FPGA build (verilog only for now)
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
base_dir=$(abspath ../..)
sim_dir=$(abspath .)
SUB_PROJECT ?= NEDOFPGAQuartus
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

PERMISSIVE_ON=
PERMISSIVE_OFF=

WAVEFORM_FLAG=-v$(sim_out_name).vcd

.PHONY: default
default: verilog

verilog: $(TOP_FILE) $(HARNESS_FILE) $(ROM_FILE)

#########################################################################################
# import other necessary rules and variables
#########################################################################################
include $(base_dir)/hardware/chipyard/common.mk
include $(sim_dir)/stratixIV.mk

#########################################################################################
# ROM generation
#########################################################################################
include $(sim_dir)/sdload.mk
include $(sim_dir)/jumpqspi.mk
.PHONY: $(BOOT_FILE) $(SDLOAD_FILE)

$(ROM_FILE): $(ROM_CONF_FILE) $(BOOT_FILE) $(ROMGEN) $(SDLOAD_FILE)
	$(ROMGEN) $(ROM_CONF_FILE) $(BOOT_FILE) > $(ROM_FILE)

#########################################################################################
# general cleanup rule
#########################################################################################
.PHONY: clean
clean:
	rm -rf $(gen_dir) $(sim_prefix)-*
