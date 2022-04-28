#########################################################################################
# verilator makefile (Copied from chipyard)
#########################################################################################
ifeq ($(shell which verilator),)
$(error Did not find Verilator in PATH. Make sure all requirements are installed)
endif

#########################################################################################
# general path and config variables
#########################################################################################
teehw_dir=$(abspath ../..)
base_dir=$(teehw_dir)
sim_dir=$(abspath .)
build_dir=$(sim_dir)/generated-src/$(long_name)
xip_dir=$(teehw_dir)/software/xip

BOARD?=TR4
ISACONF?=RV64GC
MBUS?=MBus64
BOOTSRC?=BOOTROM
PCIE?=WoPCIe
DDRCLK?=NoCrossMBusClk
HYBRID?=Rocket1
PERIPHERALS?=TEEHWPeripherals
BASE?=MCUWithLinuxConfig

#########################################################################################
# simulaton requirements
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
# include shared variables
#########################################################################################
include $(base_dir)/variables.mk
# ROM files
ROM_FILE = $(build_dir)/$(long_name).rom.v
ROM_CONF_FILE = $(build_dir)/$(long_name).rom.conf
# Little fix for the files to compile
sim_vsrcs = \
	$(TOP_FILE) \
	$(HARNESS_FILE) \
	$(TOP_SMEMS_FILE) \
	$(HARNESS_SMEMS_FILE) \
	$(ROM_FILE)

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

WAVEFORM_FLAG=-v$(sim_out_name).vcd

.PHONY: default debug
default: $(sim)
debug: $(sim_debug)

#########################################################################################
# import other necessary rules and variables
#########################################################################################
include $(base_dir)/common.mk

#########################################################################################
# ROM rules
#########################################################################################
ROMGEN=$(teehw_dir)/hardware/vlsi_rom_gen_real
HEXFILE=$(build_dir)/xip.hex
$(ROM_FILE): $(ROMGEN)
	make -C $(xip_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) ISACONF=$(ISACONF) XIP_TARGET_ADDR=0x80000000 ADD_OPTS=-DSKIP_HANG clean
	make -C $(xip_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) ISACONF=$(ISACONF) XIP_TARGET_ADDR=0x80000000 ADD_OPTS=-DSKIP_HANG hex
	$(ROMGEN) $(ROM_CONF_FILE) $(HEXFILE) > $(ROM_FILE)

#########################################################################################
# verilator binary and flags
#########################################################################################
VERILATOR := verilator --cc --exe

CXXFLAGS := \
	$(CXXFLAGS) -O1 -std=c++11 \
	-I$(RISCV)/include \
	-I$(dramsim_dir) \
	-D__STDC_FORMAT_MACROS \
	$(EXTRA_SIM_CC_FLAGS)

LDFLAGS := \
	$(LDFLAGS) \
	-L$(sim_dir) \
	-lpthread

VERILATOR_CC_OPTS = \
	-O3 \
	-CFLAGS "$(CXXFLAGS) -DTEST_HARNESS=V$(VLOG_MODEL) -DVERILATOR" \
	-CFLAGS "-I$(build_dir) -include $(build_dir)/$(long_name).plusArgs -include $(build_dir)/verilator.h" \
	-LDFLAGS "$(LDFLAGS)" \
	$(RISCV)/lib/libfesvr.a \
	$(dramsim_lib)

# normal flags used for chipyard builds
CHIPYARD_VERILATOR_FLAGS = \
	--unroll-count 256 \
	-Werror-PINMISSING \
	-Werror-IMPLICIT \
	-Wno-PINCONNECTEMPTY \
	-Wno-ASSIGNDLY \
	-Wno-DECLFILENAME \
	-Wno-UNUSED \
	-Wno-UNOPTFLAT \
	-Wno-BLKANDNBLK \
	-Wno-REDEFMACRO \
	-Wno-WIDTH \
	-Wno-WIDTHCONCAT \
	-Wno-style \
	--assert

# Use --timescale to approximate timescale behavior of pre-4.034
TIMESCALE_OPTS := $(shell verilator --version | perl -lne 'if (/(\d.\d+)/ && $$1 >= 4.034) { print "--timescale 1ns/1ps"; }')
VERILATOR_NONCC_OPTS = \
	$(TIMESCALE_OPTS) \
	--top-module $(VLOG_MODEL) \
	--vpi \
	-Wno-fatal \
	$(CHIPYARD_VERILATOR_FLAGS) \
	--output-split 10000 \
	--output-split-cfuncs 100 \
	--max-num-width 1048576 \
	-f $(sim_common_files) \
	+incdir+$(OPENTITAN_DIR)/hw/ip/prim/rtl/ \
	$(sim_vsrcs)

VERILATOR_DEFINES = \
	+define+PRINTF_COND=\$$c\(\"verbose\",\"\&\&\"\,\"done_reset\"\) \
	+define+STOP_COND=\$$c\(\"done_reset\"\)

VERILATOR_OPTS = $(VERILATOR_CC_OPTS) $(VERILATOR_NONCC_OPTS) $(VERILATOR_DEFINES) $(EXTRA_SIM_SOURCES)

#########################################################################################
# verilator build paths and file names
#########################################################################################
model_dir = $(build_dir)/$(long_name)
model_dir_debug = $(build_dir)/$(long_name).debug

model_header = $(model_dir)/V$(VLOG_MODEL).h
model_header_debug = $(model_dir_debug)/V$(VLOG_MODEL).h

model_mk = $(model_dir)/V$(VLOG_MODEL).mk
model_mk_debug = $(model_dir_debug)/V$(VLOG_MODEL).mk

#########################################################################################
# build makefile fragment that builds the verilator sim rules
#########################################################################################
$(model_mk): $(sim_vsrcs) $(sim_common_files) $(EXTRA_SIM_REQS)
	rm -rf $(model_dir)
	mkdir -p $(model_dir)
	$(VERILATOR) $(VERILATOR_OPTS) -o $(sim) -Mdir $(model_dir) -CFLAGS "-include $(model_header)"
	touch $@

$(model_mk_debug): $(sim_vsrcs) $(sim_common_files) $(EXTRA_SIM_REQS)
	rm -rf $(model_dir_debug)
	mkdir -p $(model_dir_debug)
	$(VERILATOR) $(VERILATOR_OPTS) -o $(sim_debug) --trace -Mdir $(model_dir_debug) -CFLAGS "-include $(model_header_debug)"
	touch $@

#########################################################################################
# invoke make to make verilator sim rules
#########################################################################################
$(sim): $(model_mk) $(dramsim_lib)
	$(MAKE) VM_PARALLEL_BUILDS=1 -C $(model_dir) -f V$(VLOG_MODEL).mk

$(sim_debug): $(model_mk_debug) $(dramsim_lib)
	$(MAKE) VM_PARALLEL_BUILDS=1 -C $(model_dir_debug) -f V$(VLOG_MODEL).mk

#########################################################################################
# create a verilator vpd rule
#########################################################################################
.PRECIOUS: $(output_dir)/%.vpd %.vcd
$(output_dir)/%.vpd: $(output_dir)/% $(sim_debug)
	rm -f $@.vcd && mkfifo $@.vcd
	vcd2vpd $@.vcd $@ > /dev/null &
	(set -o pipefail && $(sim_debug) $(PERMISSIVE_ON) $(SIM_FLAGS) $(EXTRA_SIM_FLAGS) $(VERBOSE_FLAGS) -v$@.vcd $(PERMISSIVE_OFF) $< </dev/null 2> >(spike-dasm > $<.out) | tee $<.log)

$(output_dir)/none.vpd: $(sim_debug)
	mkdir -p $(output_dir)
	rm -f $@.vcd && mkfifo $@.vcd
	vcd2vpd $@.vcd $@ > /dev/null &
	(set -o pipefail && $(sim_debug) $(PERMISSIVE_ON) $(SIM_FLAGS) $(EXTRA_SIM_FLAGS) $(VERBOSE_FLAGS) -v$@.vcd $(PERMISSIVE_OFF) none </dev/null 2> >(spike-dasm > $(output_dir)/none.out) | tee $(output_dir)/none.log)

#########################################################################################
# general cleanup rule
#########################################################################################
.PHONY: clean $(ROMHEX)
clean:
	rm -rf $(gen_dir) $(sim_prefix)-*
