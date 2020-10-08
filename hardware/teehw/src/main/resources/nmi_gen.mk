#########################################################################################
# pre-process nmi_gen into a single blackbox file
#########################################################################################
opentitan_dir=$(base_dir)/hardware/opentitan

# name of output pre-processed verilog file
NMI_GEN_PREPROC_SVERILOG = nmi_gen.preprocessed.sv
NMI_GEN_PREPROC_VERILOG = nmi_gen.preprocessed.v

.PHONY: nmi_gen $(NMI_GEN_PREPROC_SVERILOG) $(NMI_GEN_PREPROC_VERILOG)
nmi_gen: $(NMI_GEN_PREPROC_SVERILOG) $(NMI_GEN_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
NMI_GEN_OPENTITAN_PKGS = 

NMI_GEN_OPENTITAN_VSRCS = \
	$(opentitan_dir)/hw/ip/nmi_gen/rtl/nmi_gen.sv \
	$(opentitan_dir)/hw/ip/nmi_gen/rtl/nmi_gen_reg_top.sv

NMI_GEN_OPENTITAN_VERSRCS = 

NMI_GEN_OPENTITAN_WRAPPER = \
	$(vsrc_dir)/nmi_gen/nmi_gen_wrapper.sv

NMI_GEN_ALL_VSRCS = $(NMI_GEN_OPENTITAN_PKGS) $(NMI_GEN_OPENTITAN_VSRCS) $(NMI_GEN_OPENTITAN_WRAPPER)
NMI_GEN_ALL_VERSRCS = $(NMI_GEN_OPENTITAN_VERSRCS)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
NMI_GEN_INC_DIR_NAMES ?= rtl icache
NMI_GEN_INC_DIRS ?= $(foreach dir_name,$(NMI_GEN_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
NMI_GEN_EXTRA_PREPROC_DEFINES ?=
NMI_GEN_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(NMI_GEN_EXTRA_PREPROC_DEFINES)

$(NMI_GEN_PREPROC_SVERILOG): $(NMI_GEN_ALL_VSRCS)
	mkdir -p $(dir $(NMI_GEN_PREPROC_SVERILOG))
	$(foreach def,$(NMI_GEN_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(NMI_GEN_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(NMI_GEN_ALL_VSRCS) undef.v > combined.sv
	sed -i '/l15.tmp.h/d' combined.sv
	sed -i '/define.tmp.h/d' combined.sv
	$(PREPROC_SCRIPT) combined.sv $@ $(NMI_GEN_INC_DIRS)
	rm -rf combined.sv def.v undef.v

$(NMI_GEN_PREPROC_VERILOG): $(NMI_GEN_ALL_VERSRCS)
	mkdir -p $(dir $(NMI_GEN_PREPROC_VERILOG))
	$(foreach def,$(NMI_GEN_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(NMI_GEN_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(NMI_GEN_ALL_VERSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(NMI_GEN_INC_DIRS)
	rm -rf combined.v def.v undef.v

