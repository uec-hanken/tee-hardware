# Copied from sifive/freedom
# See LICENSE (in freedom) for license details.

# Optional variables:
# - EXTRA_FPGA_VSRCS

# export to fpga-shells
export FPGA_TOP_SYSTEM=$(MODEL)
export FPGA_BUILD_DIR=$(BUILD_DIR)/$(FPGA_TOP_SYSTEM)
export fpga_common_script_dir=$(FPGA_DIR)/common/tcl
export fpga_board_script_dir=$(FPGA_DIR)/$(FPGA_BOARD)/tcl

export BUILD_DIR

EXTRA_FPGA_VSRCS ?=
PATCHVERILOG ?= ""

VSRCS := \
	$(EXTRA_FPGA_VSRCS) \
	$(TOP_FILE) \
	$(HARNESS_FILE) \
	$(ROM_FILE)

f := $(BUILD_DIR)/$(long_name).vsrcs.F
$(f): $(VSRCS) $(sim_top_blackboxes) $(sim_harness_blackboxes)
	echo $(VSRCS) > $@
	awk '{print $1;}' $(sim_top_blackboxes) $(sim_harness_blackboxes) | sort -u | grep -v '.*\.\(svh\|h\)$$' > $@

# This simply copies XDC
xdc_file := $(BUILD_DIR)/$(long_name).xdc
$(xdc_file): $(XDC)
	cp -v $(XDC) $(xdc_file)

bit := $(BUILD_DIR)/obj/$(MODEL).bit
$(bit): $(romgen) $(f) $(xdc_file)
	cd $(BUILD_DIR); vivado \
		-nojournal -mode batch \
		-source $(fpga_common_script_dir)/vivado.tcl \
		-tclargs \
		-top-module "$(MODEL)" \
		-F "$(f)" \
		-ip-vivado-tcls "$(shell find '$(BUILD_DIR)' -name '*.vivado.tcl')" \
		-board "$(FPGA_BOARD)"

# Build .mcs
mcs := $(BUILD_DIR)/obj/$(MODEL).mcs
$(mcs): $(bit)
	cd $(BUILD_DIR); vivado -nojournal -mode batch -source $(fpga_common_script_dir)/write_cfgmem.tcl -tclargs $(FPGA_BOARD) $@ $<

.PHONY: mcs
mcs: $(mcs)

# Build Libero project
prjx := $(BUILD_DIR)/libero/$(MODEL).prjx
$(prjx): $(verilog)
	cd $(BUILD_DIR); libero SCRIPT:$(fpga_common_script_dir)/libero.tcl SCRIPT_ARGS:"$(BUILD_DIR) $(MODEL) $(PROJECT) $(CONFIG) $(FPGA_BOARD)"

.PHONY: prjx
prjx: $(prjx)

