#########################################################################################
# pre-process hmac into a single blackbox file
#########################################################################################
opentitan_dir=$(base_dir)/hardware/opentitan

# name of output pre-processed verilog file
HMAC_PREPROC_SVERILOG = hmac.preprocessed.sv
HMAC_PREPROC_VERILOG = hmac.preprocessed.v

.PHONY: hmac $(HMAC_PREPROC_SVERILOG) $(HMAC_PREPROC_VERILOG)
hmac: $(HMAC_PREPROC_SVERILOG) $(HMAC_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
HMAC_OPENTITAN_PKGS = 

HMAC_OPENTITAN_VSRCS = \
	$(opentitan_dir)/hw/ip/hmac/rtl/sha2_pad.sv \
	$(opentitan_dir)/hw/ip/hmac/rtl/sha2.sv \
	$(opentitan_dir)/hw/ip/hmac/rtl/hmac_core.sv \
	$(opentitan_dir)/hw/ip/hmac/rtl/hmac_reg_top.sv \
	$(opentitan_dir)/hw/ip/hmac/rtl/hmac.sv

HMAC_OPENTITAN_VERSRCS = 

HMAC_OPENTITAN_WRAPPER = \
	$(vsrc_dir)/hmac/hmac_wrapper.sv

HMAC_ALL_VSRCS = $(HMAC_OPENTITAN_PKGS) $(HMAC_OPENTITAN_VSRCS) $(HMAC_OPENTITAN_WRAPPER)
HMAC_ALL_VERSRCS = $(HMAC_OPENTITAN_VERSRCS)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
HMAC_INC_DIR_NAMES ?= rtl icache
HMAC_INC_DIRS ?= $(foreach dir_name,$(HMAC_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
HMAC_EXTRA_PREPROC_DEFINES ?=
HMAC_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(HMAC_EXTRA_PREPROC_DEFINES)

$(HMAC_PREPROC_SVERILOG): $(HMAC_ALL_VSRCS)
	mkdir -p $(dir $(HMAC_PREPROC_SVERILOG))
	$(foreach def,$(HMAC_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(HMAC_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(HMAC_ALL_VSRCS) undef.v > combined.sv
	sed -i '/l15.tmp.h/d' combined.sv
	sed -i '/define.tmp.h/d' combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(HMAC_INC_DIRS)
	rm -rf combined.sv def.v undef.v

$(HMAC_PREPROC_VERILOG): $(HMAC_ALL_VERSRCS)
	mkdir -p $(dir $(HMAC_PREPROC_VERILOG))
	$(foreach def,$(HMAC_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(HMAC_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(HMAC_ALL_VERSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(HMAC_INC_DIRS)
	rm -rf combined.v def.v undef.v

