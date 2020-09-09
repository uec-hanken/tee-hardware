#########################################################################################
# pre-process opentitan into a single blackbox file
#########################################################################################
base_dir=$(abspath ../../../../..)
chipyard_dir=$(base_dir)/hardware/chipyard
vsrc_dir=$(abspath .)
opentitan_dir=$(vsrc_dir)/opentitan

# name of output pre-processed verilog file
IBEX_PREPROC_VERILOG = IbexBlackbox.preprocessed.sv

.PHONY: default $(IBEX_PREPROC_VERILOG)
ibex: $(IBEX_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
IBEX_OPENTITAN_PKGS = \
	$(opentitan_dir)/hw/top_earlgrey/rtl/top_pkg.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_pkg.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_esc_pkg.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_pkg.sv

IBEX_OPENTITAN_VSRCS = \
	$(opentitan_dir)/hw/ip/rv_core_ibex/rtl/rv_core_ibex.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_alu.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_compressed_decoder.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_controller.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_counter.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_cs_registers.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_decoder.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_ex_block.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_id_stage.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_if_stage.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_load_store_unit.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_multdiv_slow.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_multdiv_fast.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_prefetch_buffer.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_fetch_fifo.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_register_file_ff.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_core.sv

IBEX_OPENTITAN_WRAPPER = \
	$(vsrc_dir)/vsrc/rv_core_ibex_blackbox/IbexBlackbox.sv

IBEX_ALL_VSRCS = $(IBEX_OPENTITAN_PKGS) $(IBEX_OPENTITAN_VSRCS) $(IBEX_OPENTITAN_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
INC_DIR_NAMES ?= rtl
INC_DIRS ?= $(foreach dir_name,$(INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
EXTRA_PREPROC_DEFINES ?=
PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(EXTRA_PREPROC_DEFINES)

PREPROC_SCRIPT = $(chipyard_dir)/scripts/insert-includes.py

$(IBEX_PREPROC_VERILOG): $(IBEX_ALL_VSRCS)
	mkdir -p $(dir $(IBEX_PREPROC_VERILOG))
	$(foreach def,$(PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(IBEX_ALL_VSRCS) undef.v > combined.sv
	sed -i '/l15.tmp.h/d' combined.sv
	sed -i '/define.tmp.h/d' combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(INC_DIRS)
	rm -rf combined.sv def.v undef.v

clean:
	rm -rf $(IBEX_PREPROC_VERILOG)
