#########################################################################################
# pre-process ibex into a single blackbox file
#########################################################################################
opentitan_dir=$(base_dir)/hardware/opentitan

# name of output pre-processed verilog file
IBEX_PREPROC_SVERILOG = IbexBlackbox.preprocessed.sv
IBEX_PREPROC_VERILOG = IbexBlackbox.preprocessed.v

.PHONY: ibex $(IBEX_PREPROC_SVERILOG) $(IBEX_PREPROC_VERILOG)
ibex: $(IBEX_PREPROC_SVERILOG) $(IBEX_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
IBEX_OPENTITAN_PKGS = 

IBEX_OPENTITAN_VSRCS = \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_alu.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_compressed_decoder.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_controller.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_counter.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_cs_registers.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_decoder.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_dummy_instr.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_ex_block.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_icache.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_id_stage.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_if_stage.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_load_store_unit.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_multdiv_slow.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_multdiv_fast.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_prefetch_buffer.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_fetch_fifo.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_register_file_ff.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_wb_stage.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_pmp.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_core.sv \
	$(opentitan_dir)/hw/ip/rv_core_ibex/rtl/rv_core_ibex.sv

IBEX_OPENTITAN_VERSRCS = \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/syn/rtl/prim_clock_gating.v

IBEX_OPENTITAN_WRAPPER = \
	$(vsrc_dir)/rv_core_ibex_blackbox/IbexBlackbox.sv

IBEX_ALL_VSRCS = $(IBEX_OPENTITAN_PKGS) $(IBEX_OPENTITAN_VSRCS) $(IBEX_OPENTITAN_WRAPPER)
IBEX_ALL_VERSRCS = $(IBEX_OPENTITAN_VERSRCS)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
IBEX_INC_DIR_NAMES ?= rtl icache
IBEX_INC_DIRS ?= $(foreach dir_name,$(IBEX_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
IBEX_EXTRA_PREPROC_DEFINES ?=
IBEX_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(IBEX_EXTRA_PREPROC_DEFINES)

$(IBEX_PREPROC_SVERILOG): $(IBEX_ALL_VSRCS)
	mkdir -p $(dir $(IBEX_PREPROC_SVERILOG))
	$(foreach def,$(IBEX_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(IBEX_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(IBEX_ALL_VSRCS) undef.v > combined.sv
	sed -i '/l15.tmp.h/d' combined.sv
	sed -i '/define.tmp.h/d' combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(IBEX_INC_DIRS)
	rm -rf combined.sv def.v undef.v

$(IBEX_PREPROC_VERILOG): $(IBEX_ALL_VERSRCS)
	mkdir -p $(dir $(IBEX_PREPROC_VERILOG))
	$(foreach def,$(IBEX_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(IBEX_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(IBEX_ALL_VERSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(IBEX_INC_DIRS)
	rm -rf combined.v def.v undef.v

