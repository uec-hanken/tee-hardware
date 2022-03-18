# TODO: This file is HIGHLY MANUAL
# Expects that all pins are named as such, and the blackbox "main" (from main.qsys) is present with the config.

set_time_format -unit ns -decimal_places 3
# #############################################################################
#  Create Input reference clocks
create_clock -name {OSC_50_BANK2_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {OSC_50_BANK2}]
create_clock -name {OSC_50_BANK3_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {OSC_50_BANK3}]
create_clock -name {OSC_50_BANK4_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {OSC_50_BANK4}]
create_clock -name {OSC_50_BANK5_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {OSC_50_BANK5}]
create_clock -name {OSC_50_BANK6_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {OSC_50_BANK6}]
create_clock -name {OSC_50_BANK7_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {OSC_50_BANK7}]
create_clock -name {GCLKIN_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {GCLKIN}]

# The JTAG clock
create_clock -name {JTAG_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {jtag_jtag_TCK}]
# #############################################################################
#  Now that we have created the custom clocks which will be base clocks,
#  derive_pll_clock is used to calculate all remaining clocks for PLLs
derive_pll_clocks -create_base_clocks
derive_clock_uncertainty

set_input_delay -clock { mod|ddr|island|blackbox|qsys_pll|sd1|pll7|clk[3] } 10.000 [all_inputs]
set_output_delay -clock { mod|ddr|island|blackbox|qsys_pll|sd1|pll7|clk[3] } 10.000 [all_outputs]
