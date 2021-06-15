#########################################################################################
# pre-process poly1305 into a single blackbox file
#########################################################################################
POLY1305_DIR ?= $(optvsrc_dir)/poly1305

# name of output pre-processed verilog file
POLY1305_PREPROC_VERILOG = poly1305.preprocessed.v

.PHONY: poly1305 $(POLY1305_PREPROC_VERILOG)
poly1305:  $(POLY1305_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
POLY1305_OPENTITAN_PKGS = 

POLY1305_OPENTITAN_VSRCS = $(POLY1305_DIR)/poly1305_final.v \
	$(POLY1305_DIR)/poly1305_mulacc.v \
	$(POLY1305_DIR)/poly1305_pblock.v 

POLY1305_OPENTITAN_WRAPPER = \
	$(POLY1305_DIR)/poly1305_core.v

POLY1305_ALL_VSRCS = $(POLY1305_OPENTITAN_PKGS) $(POLY1305_OPENTITAN_VSRCS) $(POLY1305_OPENTITAN_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
POLY1305_INC_DIR_NAMES ?= include
POLY1305_INC_DIRS ?= $(foreach dir_name,$(POLY1305_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
POLY1305_EXTRA_PREPROC_DEFINES ?=
POLY1305_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(POLY1305_EXTRA_PREPROC_DEFINES)

$(POLY1305_PREPROC_VERILOG): $(POLY1305_ALL_VSRCS)
	mkdir -p $(dir $(POLY1305_PREPROC_VERILOG))
	$(foreach def,$(POLY1305_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(POLY1305_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(POLY1305_ALL_VSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(POLY1305_INC_DIRS)
	rm -rf combined.v def.v undef.v

