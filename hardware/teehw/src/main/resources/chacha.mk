#########################################################################################
# pre-process chacha into a single blackbox file
#########################################################################################
CHACHA_DIR ?= $(vsrc_dir)/chacha/src/rtl

# name of output pre-processed verilog file
CHACHA_PREPROC_VERILOG = chacha.preprocessed.v

.PHONY: chacha $(CHACHA_PREPROC_VERILOG)
chacha:  $(CHACHA_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
CHACHA_PKGS = 

CHACHA_VSRCS = $(CHACHA_DIR)/chacha_core.v \
	$(CHACHA_DIR)/chacha_qr.v 

CHACHA_WRAPPER = \
	$(CHACHA_DIR)/chacha.v

CHACHA_ALL_VSRCS = $(CHACHA_PKGS) $(CHACHA_VSRCS) $(CHACHA_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(vsrc_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
CHACHA_INC_DIR_NAMES ?= include
CHACHA_INC_DIRS ?= $(foreach dir_name,$(CHACHA_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
CHACHA_EXTRA_PREPROC_DEFINES ?=
CHACHA_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(CHACHA_EXTRA_PREPROC_DEFINES)

$(CHACHA_PREPROC_VERILOG): $(CHACHA_ALL_VSRCS)
	mkdir -p $(dir $(CHACHA_PREPROC_VERILOG))
	$(foreach def,$(CHACHA_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(CHACHA_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(CHACHA_ALL_VSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(CHACHA_INC_DIRS)
	rm -rf combined.v def.v undef.v

