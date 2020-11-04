#########################################################################################
# pre-process prim into a single blackbox file
#########################################################################################
opentitan_dir=$(base_dir)/hardware/opentitan

# name of output pre-processed verilog file
PRIM_PREPROC_SVERILOG = prim.preprocessed.sv

.PHONY: default $(PRIM_PREPROC_SVERILOG)
prim: $(PRIM_PREPROC_SVERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
PRIM_OPENTITAN_PKGS = 

PRIM_OPENTITAN_VSRCS = \
	$(vsrc_dir)/prim/prim_flop.sv \
	$(vsrc_dir)/prim/prim_flop_2sync.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_alert_receiver.sv \
	$(vsrc_dir)/prim/prim_ram_1p.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_esc_receiver.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_diff_decode.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_fifo_sync.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_fifo_async.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_alert_sender.sv

# TODO: Include them when the time comes
#	$(vsrc_dir)/prim/prim_flash.sv \
#	$(vsrc_dir)/prim/prim_otp.sv \

PRIM_OPENTITAN_WRAPPER = 

PRIM_ALL_VSRCS = $(PRIM_OPENTITAN_PKGS) $(PRIM_OPENTITAN_VSRCS) $(PRIM_OPENTITAN_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
PRIM_INC_DIR_NAMES ?= rtl icache
PRIM_INC_DIRS ?= $(foreach dir_name,$(PRIM_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
PRIM_EXTRA_PREPROC_DEFINES ?=
PRIM_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(PRIM_EXTRA_PREPROC_DEFINES)

$(PRIM_PREPROC_SVERILOG): $(PRIM_ALL_VSRCS)
	mkdir -p $(dir $(PRIM_PREPROC_SVERILOG))
	$(foreach def,$(PRIM_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(PRIM_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(PRIM_ALL_VSRCS) undef.v > combined.sv
	sed -i '/l15.tmp.h/d' combined.sv
	sed -i '/define.tmp.h/d' combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(PRIM_INC_DIRS)
	rm -rf combined.sv def.v undef.v

