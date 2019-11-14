#########################################################################################
# vc707 FPGA build (verilog only for now)
#########################################################################################

#########################################################################################
# general path variables
#########################################################################################
base_dir=$(abspath ../..)
sim_dir=$(abspath .)
bootrom_dir=$(base_dir)/software/freedom-u540-c000-bootloader
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
BOOT_FILE = $(bootrom_dir)/vc707zsbl.hex

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

$(ROM_FILE): $(ROM_CONF_FILE) $(BOOT_FILE) $(ROMGEN)
	$(ROMGEN) $(ROM_CONF_FILE) $(BOOT_FILE) > $(ROM_FILE)

.PHONY: $(BOOT_FILE)
$(BOOT_FILE): $(bootrom_dir)/Makefile
	awk '/tlclk {/ && !f{f=1; next}; f && match($$0, /^.*clock-frequency.*<(.*)>.*/, arr) { print "#define TL_CLK " arr[1] "UL"}' $(DTS_FILE) > $(bootrom_dir)/tl_clock.h
	cp $(DTS_FILE) $(bootrom_dir)/zsbl/ux00_zsbl.dts
	cp $(DTS_FILE) $(bootrom_dir)/fsbl/ux00_fsbl.dts
	echo "#define vc707 1" > $(bootrom_dir)/board.h
	echo "#define SKIP_ECC_WIPEDOWN 1" >> $(bootrom_dir)/board.h
	sed -i -e 's/INCLUDE\smemory.lds/INCLUDE memory_vc707.lds/g' $(bootrom_dir)/ux00_zsbl.lds
	sed -i -e 's/INCLUDE\smemory.lds/INCLUDE memory_vc707.lds/g' $(bootrom_dir)/ux00_fsbl.lds
	make -C $(bootrom_dir) vc707zsbl.hex vc707fsbl.bin

#########################################################################################
# general cleanup rule
#########################################################################################
.PHONY: clean
clean:
	rm -rf $(gen_dir) $(sim_prefix)-*
