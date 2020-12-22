# Inspired from the sifive/freedom

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

# This simply copies the shell TCL to the generated folder
tcl_shell_file := $(BUILD_DIR)/$(long_name).shell.quartus.tcl
$(tcl_shell_file): $(TCL_SHELL)
	cp -v $(TCL_SHELL) $(tcl_shell_file)

# TODO: Copies the single main qsys tcl also. We need to create one for each qsys.
tcl_qsys_main_file := $(BUILD_DIR)/$(long_name).main.qsys.tcl
$(tcl_qsys_main_file): $(TCL_QSYS_MAIN)
	cp -v $(TCL_QSYS_MAIN) $(tcl_qsys_main_file)

sof := $(BUILD_DIR)/obj/$(MODEL).sof
$(sof): $(f) $(xdc_shell_file) $(tcl_shell_file) $(tcl_qsys_main_file)
	cd $(BUILD_DIR); quartus_sh \
		-t $(fpga_common_script_dir)/quartus.tcl \
		-top-module "$(MODEL)" \
		-F "$(f)" \
		-ip-quartus-tcls "$(shell find '$(BUILD_DIR)' -name '*.quartus.tcl')" \
		-ip-quartus-qsys "$(shell find '$(BUILD_DIR)' -name '*.qsys.tcl')" \
		-board "$(FPGA_BOARD)"
sof: $(sof)

