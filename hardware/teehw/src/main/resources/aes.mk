#########################################################################################
# pre-process aes into a single blackbox file
#########################################################################################
AES_DIR ?= $(optvsrc_dir)/AES

# name of output pre-processed verilog file
AES_PREPROC_VERILOG = aes.preprocessed.v

.PHONY: aes $(AES_PREPROC_VERILOG)
aes:  $(AES_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
AES_OPENTITAN_PKGS = 

AES_OPENTITAN_VSRCS =  \
	$(AES_DIR)/aes_core.v \
	$(AES_DIR)/aes_decipher_block.v \
	$(AES_DIR)/aes_encipher_block.v \
	$(AES_DIR)/aes_sub_inv_sbox.v \
	$(AES_DIR)/aes_sub_sbox.v \
	$(AES_DIR)/aes_inv_sbox.v \
	$(AES_DIR)/aes_key_mem.v \
	$(AES_DIR)/aes_sbox.v \
	$(AES_DIR)/mixcolumns.v \
	$(AES_DIR)/inv_mixcolumns.v \
	$(AES_DIR)/aes_core_TOP.v

AES_OPENTITAN_WRAPPER = \
	$(AES_DIR)/aes_core_TOP_wrapper.v

AES_ALL_VSRCS = $(AES_OPENTITAN_PKGS) $(AES_OPENTITAN_VSRCS) $(AES_OPENTITAN_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
AES_INC_DIR_NAMES ?= include
AES_INC_DIRS ?= $(foreach dir_name,$(AES_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
AES_EXTRA_PREPROC_DEFINES ?=
AES_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(AES_EXTRA_PREPROC_DEFINES)

$(AES_PREPROC_VERILOG): $(AES_ALL_VSRCS)
	mkdir -p $(dir $(AES_PREPROC_VERILOG))
	$(foreach def,$(AES_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(AES_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(AES_ALL_VSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(AES_INC_DIRS)
	rm -rf combined.v def.v undef.v

