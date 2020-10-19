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

f := $(BUILD_DIR)/$(long_name).vsrcs.F
$(f):
	make -C $(sim_dir) default
	echo -n $(VSRCS) " " > $@
	awk '{print $1;}' $(TOP_F) | sort -u | grep -v '.*\.\(svh\|h\)$$' | awk 'BEGIN { ORS=" " }; { print $1 }' >> $@

# This simply copies the shell XDC and TCL to the generated folder
xdc_shell_file := $(BUILD_DIR)/$(long_name).shell.xdc
tcl_shell_file := $(BUILD_DIR)/$(long_name).shell.vivado.tcl
$(xdc_shell_file): $(XDC_SHELL)
	cp -v $(XDC_SHELL) $(xdc_shell_file)
$(tcl_shell_file): $(TCL_SHELL)
	cp -v $(TCL_SHELL) $(tcl_shell_file)

bit := $(BUILD_DIR)/obj/$(MODEL).bit
$(bit): $(f) $(xdc_shell_file) $(tcl_shell_file)
	cd $(BUILD_DIR); vivado \
		-nojournal -mode batch \
		-source $(fpga_common_script_dir)/vivado.tcl \
		-tclargs \
		-top-module "$(MODEL)" \
		-F "$(f)" \
		-ip-vivado-tcls "$(shell find '$(BUILD_DIR)' -name '*.vivado.tcl')" \
		-board "$(FPGA_BOARD)"
bit: $(bit)

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

