#########################################################################################
# pre-process ed25519_sign into a single blackbox file
#########################################################################################
ED25519_SIGN_DIR ?= $(optvsrc_dir)/Ed25519/Ed25519_sign

# name of output pre-processed verilog file
ED25519_SIGN_PREPROC_VERILOG = ed25519_sign.preprocessed.v

.PHONY: ed25519_sign $(ED25519_SIGN_PREPROC_VERILOG)
ed25519_sign:  $(ED25519_SIGN_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
ED25519_SIGN_OPENTITAN_PKGS = 

ED25519_SIGN_OPENTITAN_VSRCS = \
	$(ED25519_SIGN_DIR)/ed25519_sign_S_core_TOP.v \
	$(ED25519_SIGN_DIR)/ed25519_sign_S_core.v \
	$(ED25519_SIGN_DIR)/mult_512_byAdder.v \
	$(ED25519_SIGN_DIR)/barrett.v

ED25519_SIGN_OPENTITAN_WRAPPER = \
	$(ED25519_SIGN_DIR)/ed25519_sign_S_core_TOP_wrapper.v

ED25519_SIGN_ALL_VSRCS = $(ED25519_SIGN_OPENTITAN_PKGS) $(ED25519_SIGN_OPENTITAN_VSRCS) $(ED25519_SIGN_OPENTITAN_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
ED25519_SIGN_INC_DIR_NAMES ?= include
ED25519_SIGN_INC_DIRS ?= $(foreach dir_name,$(ED25519_SIGN_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
ED25519_SIGN_EXTRA_PREPROC_DEFINES ?=
ED25519_SIGN_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(ED25519_SIGN_EXTRA_PREPROC_DEFINES)

$(ED25519_SIGN_PREPROC_VERILOG): $(ED25519_SIGN_ALL_VSRCS)
	mkdir -p $(dir $(ED25519_SIGN_PREPROC_VERILOG))
	$(foreach def,$(ED25519_SIGN_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(ED25519_SIGN_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(ED25519_SIGN_ALL_VSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(ED25519_SIGN_INC_DIRS)
	rm -rf combined.v def.v undef.v

