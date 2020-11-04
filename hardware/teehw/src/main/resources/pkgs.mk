#########################################################################################
# pre-process pkgs into a single blackbox file
#########################################################################################
opentitan_dir=$(base_dir)/hardware/opentitan

# name of output pre-processed verilog file
# NOTE: The "aaaa" are because they use a sort -u in the .f generation file. This is intended
# to be first, so thats why
PKGS_PREPROC_SVERILOG = aaaa_pkgs.preprocessed.sv

.PHONY: pkgs $(PKGS_PREPROC_SVERILOG)
pkgs: $(PKGS_PREPROC_SVERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
PKGS_OPENTITAN_PKGS = \
	$(opentitan_dir)/hw/top_earlgrey/rtl/top_pkg.sv \
	$(opentitan_dir)/hw/ip/tlul/rtl/tlul_pkg.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_util_pkg.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_alert_pkg.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_esc_pkg.sv \
	$(opentitan_dir)/hw/ip/prim/rtl/prim_cipher_pkg.sv \
	$(opentitan_dir)/hw/ip/hmac/rtl/hmac_reg_pkg.sv \
	$(opentitan_dir)/hw/ip/hmac/rtl/hmac_pkg.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_sbox_canright_pkg.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_reg_pkg.sv \
	$(opentitan_dir)/hw/ip/aes/rtl/aes_pkg.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_handler_reg_pkg.sv \
	$(opentitan_dir)/hw/ip/alert_handler/rtl/alert_pkg.sv \
	$(opentitan_dir)/hw/ip/pwrmgr/rtl/pwrmgr_reg_pkg.sv \
	$(opentitan_dir)/hw/ip/pwrmgr/rtl/pwrmgr_pkg.sv \
	$(opentitan_dir)/hw/ip/lc_ctrl/rtl/lc_ctrl_reg_pkg.sv \
	$(opentitan_dir)/hw/ip/lc_ctrl/rtl/lc_ctrl_pkg.sv \
	$(opentitan_dir)/hw/vendor/lowrisc_ibex/rtl/ibex_pkg.sv

# TODO: Include afterwards, when flash and otp are completed
#	$(opentitan_dir)/hw/ip/flash_ctrl/rtl/flash_ctrl_reg_pkg.sv \
#	$(opentitan_dir)/hw/ip/flash_ctrl/rtl/flash_ctrl_pkg.sv \
#	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_reg_pkg.sv \
#	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_pkg.sv \
#	$(opentitan_dir)/hw/ip/nmi_gen/rtl/nmi_gen_reg_pkg.sv

PKGS_OPENTITAN_VSRCS = 

PKGS_OPENTITAN_WRAPPER = 

PKGS_ALL_VSRCS = $(PKGS_OPENTITAN_PKGS) $(PKGS_OPENTITAN_VSRCS) $(PKGS_OPENTITAN_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
PKGS_INC_DIR_NAMES ?= rtl icache
PKGS_INC_DIRS ?= $(foreach dir_name,$(PKGS_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
PKGS_EXTRA_PREPROC_DEFINES ?=
PKGS_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(PKGS_EXTRA_PREPROC_DEFINES)

$(PKGS_PREPROC_SVERILOG): $(PKGS_ALL_VSRCS)
	mkdir -p $(dir $(PKGS_PREPROC_SVERILOG))
	$(foreach def,$(PKGS_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(PKGS_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(PKGS_ALL_VSRCS) undef.v > combined.sv
	sed -i '/l15.tmp.h/d' combined.sv
	sed -i '/define.tmp.h/d' combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(PKGS_INC_DIRS)
	rm -rf combined.sv def.v undef.v

