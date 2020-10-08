#########################################################################################
# pre-process ibex into a single blackbox file
#########################################################################################
SHA3_DIR ?= $(optvsrc_dir)/SHA3

# name of output pre-processed verilog file
SHA3_PREPROC_VERILOG = SHA3.preprocessed.v

.PHONY: sha3 $(SHA3_PREPROC_VERILOG)
sha3:  $(SHA3_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
SHA3_OPENTITAN_PKGS = 

SHA3_OPENTITAN_VSRCS = $(SHA3_DIR)/f_permutation.v \
	$(SHA3_DIR)/round2in1.v \
	$(SHA3_DIR)/padder1.v \
	$(SHA3_DIR)/keccak.v \
	$(SHA3_DIR)/padder.v \
	$(SHA3_DIR)/rconst2in1.v \
	$(SHA3_DIR)/SHA3_TOP.v

SHA3_OPENTITAN_WRAPPER = \
	$(SHA3_DIR)/SHA3_TOP_wrapper.v

SHA3_ALL_VSRCS = $(SHA3_OPENTITAN_PKGS) $(SHA3_OPENTITAN_VSRCS) $(SHA3_OPENTITAN_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
SHA3_INC_DIR_NAMES ?= include
SHA3_INC_DIRS ?= $(foreach dir_name,$(SHA3_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
SHA3_EXTRA_PREPROC_DEFINES ?=
SHA3_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(SHA3_EXTRA_PREPROC_DEFINES)

$(SHA3_PREPROC_VERILOG): $(SHA3_ALL_VSRCS)
	mkdir -p $(dir $(SHA3_PREPROC_VERILOG))
	$(foreach def,$(SHA3_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(SHA3_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(SHA3_ALL_VSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(SHA3_INC_DIRS)
	rm -rf combined.v def.v undef.v

