#########################################################################################
# pre-process tlul into a single blackbox file
#########################################################################################
opentitan_dir=$(base_dir)/hardware/opentitan

# name of output pre-processed verilog file
TLUL_PREPROC_SVERILOG = tlul.preprocessed.sv

.PHONY: tlul $(TLUL_PREPROC_SVERILOG)
tlul: $(TLUL_PREPROC_SVERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
TLUL_OPENTITAN_PKGS = 

TLUL_OPENTITAN_VSRCS = \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_socket_1n.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_socket_m1.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_adapter_reg.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_adapter_host.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_adapter_sram.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_err.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_err_resp.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_fifo_sync.sv

TLUL_OPENTITAN_WRAPPER = 

TLUL_ALL_VSRCS = $(TLUL_OPENTITAN_PKGS) $(TLUL_OPENTITAN_VSRCS) $(TLUL_OPENTITAN_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
TLUL_INC_DIR_NAMES ?= rtl icache
TLUL_INC_DIRS ?= $(foreach dir_name,$(TLUL_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
TLUL_EXTRA_PREPROC_DEFINES ?=
TLUL_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(TLUL_EXTRA_PREPROC_DEFINES)

$(TLUL_PREPROC_SVERILOG): $(TLUL_ALL_VSRCS)
	mkdir -p $(dir $(TLUL_PREPROC_SVERILOG))
	$(foreach def,$(TLUL_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(TLUL_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(TLUL_ALL_VSRCS) undef.v > combined.sv
	sed -i '/l15.tmp.h/d' combined.sv
	sed -i '/define.tmp.h/d' combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(TLUL_INC_DIRS)
	rm -rf combined.sv def.v undef.v


