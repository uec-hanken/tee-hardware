#########################################################################################
# pre-process alert into a single blackbox file
#########################################################################################
opentitan_dir=$(base_dir)/hardware/opentitan

# name of output pre-processed verilog file
ALERT_PREPROC_SVERILOG = AlertBlackbox.preprocessed.sv
ALERT_PREPROC_VERILOG = AlertBlackbox.preprocessed.v

.PHONY: default $(ALERT_PREPROC_SVERILOG) $(ALERT_PREPROC_VERILOG)
alert: $(ALERT_PREPROC_SVERILOG) $(ALERT_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
ALERT_OPENTITAN_PKGS = \
	$(opentitan_dir)/hw/top_earlgrey/rtl/top_pkg.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_pkg.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_esc_pkg.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_alert_pkg.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_util_pkg.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_pkg.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_handler_reg_pkg.sv

ALERT_OPENTITAN_VSRCS = \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_adapter_host.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_fifo_sync.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_alert_receiver.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_alert_sender.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_handler.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_handler_accu.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_handler_class.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_handler_esc_timer.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_handler_ping_timer.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_handler_reg_top.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_handler_reg_wrap.sv

ALERT_OPENTITAN_VERSRCS = 

ALERT_OPENTITAN_WRAPPER = 

ALERT_ALL_VSRCS = $(ALERT_OPENTITAN_PKGS) $(ALERT_OPENTITAN_VSRCS) $(ALERT_OPENTITAN_WRAPPER)
ALERT_ALL_VERSRCS = $(ALERT_OPENTITAN_VERSRCS)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
ALERT_INC_DIR_NAMES ?= rtl icache
ALERT_INC_DIRS ?= $(foreach dir_name,$(ALERT_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
ALERT_EXTRA_PREPROC_DEFINES ?=
ALERT_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(ALERT_EXTRA_PREPROC_DEFINES)

$(ALERT_PREPROC_SVERILOG): $(ALERT_ALL_VSRCS)
	mkdir -p $(dir $(ALERT_PREPROC_VERILOG))
	$(foreach def,$(ALERT_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(ALERT_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(ALERT_ALL_VSRCS) undef.v > combined.sv
	sed -i '/l15.tmp.h/d' combined.sv
	sed -i '/define.tmp.h/d' combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(ALERT_INC_DIRS)
	rm -rf combined.sv def.v undef.v

$(ALERT_PREPROC_VERILOG): $(ALERT_ALL_VERSRCS)
	mkdir -p $(dir $(ALERT_PREPROC_VERILOG))
	$(foreach def,$(ALERT_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(ALERT_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(ALERT_ALL_VERSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(ALERT_INC_DIRS)
	rm -rf combined.v def.v undef.v

clean:
	rm -rf $(ALERT_PREPROC_SVERILOG) $(ALERT_PREPROC_VERILOG)

