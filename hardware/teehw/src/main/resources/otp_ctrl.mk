#########################################################################################
# pre-process otp_ctrl into a single blackbox file
#########################################################################################
opentitan_dir=$(base_dir)/hardware/opentitan

# name of output pre-processed verilog file
OTP_CTRL_PREPROC_SVERILOG = otp_ctrl.preprocessed.sv
OTP_CTRL_PREPROC_VERILOG = otp_ctrl.preprocessed.v

.PHONY: otp_ctrl $(OTP_CTRL_PREPROC_SVERILOG) $(OTP_CTRL_PREPROC_VERILOG)
otp_ctrl: $(OTP_CTRL_PREPROC_SVERILOG) $(OTP_CTRL_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
OTP_CTRL_OPENTITAN_PKGS = 

OTP_CTRL_OPENTITAN_VSRCS =  \
	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_reg_top.sv \
	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_dai.sv \
	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_kdi.sv \
	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_lci.sv \
	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_lfsr_timer.sv \
	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_parity_reg.sv \
	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_part_buf.sv \
	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_part_unbuf.sv \
	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl_scrmbl.sv \
	$(opentitan_dir)/hw/ip/otp_ctrl/rtl/otp_ctrl.sv

OTP_CTRL_OPENTITAN_VERSRCS = 

OTP_CTRL_OPENTITAN_WRAPPER = 

OTP_CTRL_ALL_VSRCS = $(OTP_CTRL_OPENTITAN_PKGS) $(OTP_CTRL_OPENTITAN_VSRCS) $(OTP_CTRL_OPENTITAN_WRAPPER)
OTP_CTRL_ALL_VERSRCS = $(OTP_CTRL_OPENTITAN_VERSRCS)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
OTP_CTRL_INC_DIR_NAMES ?= rtl icache
OTP_CTRL_INC_DIRS ?= $(foreach dir_name,$(OTP_CTRL_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
OTP_CTRL_EXTRA_PREPROC_DEFINES ?=
OTP_CTRL_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(OTP_CTRL_EXTRA_PREPROC_DEFINES)

$(OTP_CTRL_PREPROC_SVERILOG): $(OTP_CTRL_ALL_VSRCS)
	mkdir -p $(dir $(OTP_CTRL_PREPROC_SVERILOG))
	$(foreach def,$(OTP_CTRL_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(OTP_CTRL_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(OTP_CTRL_ALL_VSRCS) undef.v > combined.sv
	sed -i '/l15.tmp.h/d' combined.sv
	sed -i '/define.tmp.h/d' combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(OTP_CTRL_INC_DIRS)
	rm -rf combined.sv def.v undef.v

$(OTP_CTRL_PREPROC_VERILOG): $(OTP_CTRL_ALL_VERSRCS)
	mkdir -p $(dir $(OTP_CTRL_PREPROC_VERILOG))
	$(foreach def,$(OTP_CTRL_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(OTP_CTRL_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(OTP_CTRL_ALL_VERSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(OTP_CTRL_INC_DIRS)
	rm -rf combined.v def.v undef.v

