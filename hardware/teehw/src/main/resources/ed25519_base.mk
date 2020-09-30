#########################################################################################
# pre-process ed25519_base into a single blackbox file
#########################################################################################
ED25519_DIR ?= $(optvsrc_dir)/Ed25519/Ed25519_mul

# name of output pre-processed verilog file
ED25519_BASE_PREPROC_VERILOG = ed25519_base.preprocessed.v

.PHONY: default $(ED25519_BASE_PREPROC_VERILOG)
ed25519_base:  $(ED25519_BASE_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
ED25519_BASE_OPENTITAN_PKGS = 

ED25519_BASE_OPENTITAN_VSRCS = $(ED25519_DIR)/mac16_generic.v \
	$(ED25519_DIR)/ed25519_microcode_rom.v \
	$(ED25519_DIR)/ed25519_operand_bank.v \
	$(ED25519_DIR)/adder47_generic.v \
	$(ED25519_DIR)/ed25519_uop_worker.v \
	$(ED25519_DIR)/subtractor32_generic.v \
	$(ED25519_DIR)/ed25519_mul.v \
	$(ED25519_DIR)/bram_1rw_1ro_readfirst.v \
	$(ED25519_DIR)/curve25519_modular_multiplier.v \
	$(ED25519_DIR)/ed25519_banks_array.v \
	$(ED25519_DIR)/modular_adder.v \
	$(ED25519_DIR)/modular_subtractor.v \
	$(ED25519_DIR)/adder32_generic.v \
	$(ED25519_DIR)/multiword_mover.v \
	$(ED25519_DIR)/ed25519_mul_TOP.v

ED25519_BASE_OPENTITAN_WRAPPER = \
	$(ED25519_DIR)/ed25519_mul_TOP_wrapper.v

ED25519_BASE_ALL_VSRCS = $(ED25519_BASE_OPENTITAN_PKGS) $(ED25519_BASE_OPENTITAN_VSRCS) $(ED25519_BASE_OPENTITAN_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
ED25519_BASE_INC_DIR_NAMES ?= include
ED25519_BASE_INC_DIRS ?= $(foreach dir_name,$(ED25519_BASE_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
ED25519_BASE_EXTRA_PREPROC_DEFINES ?=
ED25519_BASE_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(ED25519_BASE_EXTRA_PREPROC_DEFINES)

$(ED25519_BASE_PREPROC_VERILOG): $(ED25519_BASE_ALL_VSRCS)
	mkdir -p $(dir $(ED25519_BASE_PREPROC_VERILOG))
	$(foreach def,$(ED25519_BASE_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(ED25519_BASE_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(ED25519_BASE_ALL_VSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(ED25519_BASE_INC_DIRS)
	rm -rf combined.v def.v undef.v

clean:
	rm -rf $(ED25519_BASE_PREPROC_VERILOG)

