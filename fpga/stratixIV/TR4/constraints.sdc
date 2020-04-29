set_time_format -unit ns -decimal_places 3
# ##############################################################################  Create Input reference clocks
create_clock -name {OSC_50_BANK1_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {OSC_50_BANK1}]
create_clock -name {OSC_50_BANK4_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {OSC_50_BANK4}]
create_clock -name {jtag_jtag_TCK_clk} -period 50.000 -waveform { 0.000 10.000 } [get_ports {jtag_jtag_TCK}]
# ##############################################################################  Now that we have created the custom clocks which will be base clocks,#  derive_pll_clock is used to calculate all remaining clocks for PLLs
derive_pll_clocks -create_base_clocks
derive_clock_uncertainty