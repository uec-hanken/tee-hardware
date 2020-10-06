#########################################################################################
# pre-process aesot into a single blackbox file
#########################################################################################
opentitan_dir=$(base_dir)/hardware/opentitan

# name of output pre-processed verilog file
AESOT_PREPROC_SVERILOG = AESOTBlackbox.preprocessed.sv
AESOT_PREPROC_VERILOG = AESOTBlackbox.preprocessed.v

.PHONY: default $(AESOT_PREPROC_SVERILOG) $(AESOT_PREPROC_VERILOG)
aesot: $(AESOT_PREPROC_SVERILOG) $(AESOT_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
AESOT_OPENTITAN_PKGS = \
	$(opentitan_dir)/hw/top_earlgrey/rtl/top_pkg.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_pkg.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_util_pkg.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_sbox_canright_pkg.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_reg_pkg.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_pkg.sv

AESOT_OPENTITAN_VSRCS = \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_adapter_host.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_fifo_sync.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_cipher_control.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_cipher_core.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_control.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_core.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_ctr.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_key_expand.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_mix_columns.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_mix_single_column.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_prng.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_reg_status.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_reg_top.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_sbox.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_sbox_canright.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_sbox_canright_masked.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_sbox_canright_masked_noreuse.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_sbox_lut.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_shift_rows.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_sub_bytes.sv

AESOT_OPENTITAN_VERSRCS = 

AESOT_OPENTITAN_WRAPPER = \
	$(vsrc_dir)/aesot/aes_wrapper.sv

AESOT_ALL_VSRCS = $(AESOT_OPENTITAN_PKGS) $(AESOT_OPENTITAN_VSRCS) $(AESOT_OPENTITAN_WRAPPER)
AESOT_ALL_VERSRCS = $(AESOT_OPENTITAN_VERSRCS)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
AESOT_INC_DIR_NAMES ?= rtl icache
AESOT_INC_DIRS ?= $(foreach dir_name,$(AESOT_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
AESOT_EXTRA_PREPROC_DEFINES ?=
AESOT_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(AESOT_EXTRA_PREPROC_DEFINES)

$(AESOT_PREPROC_SVERILOG): $(AESOT_ALL_VSRCS)
	mkdir -p $(dir $(AESOT_PREPROC_VERILOG))
	$(foreach def,$(AESOT_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(AESOT_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(AESOT_ALL_VSRCS) undef.v > combined.sv
	sed -i '/l15.tmp.h/d' combined.sv
	sed -i '/define.tmp.h/d' combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(AESOT_INC_DIRS)
	rm -rf combined.sv def.v undef.v

$(AESOT_PREPROC_VERILOG): $(AESOT_ALL_VERSRCS)
	mkdir -p $(dir $(AESOT_PREPROC_VERILOG))
	$(foreach def,$(AESOT_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(AESOT_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(AESOT_ALL_VERSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(AESOT_INC_DIRS)
	rm -rf combined.v def.v undef.v

clean:
	rm -rf $(AESOT_PREPROC_SVERILOG) $(AESOT_PREPROC_VERILOG)

