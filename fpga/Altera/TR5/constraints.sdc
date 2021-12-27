# TODO: This file is HIGHLY MANUAL
# Expects that all pins are named as such, and the blackbox "main" (from main.qsys) is present with the config.

#**************************************************************
# Create Clock
#**************************************************************
# CLOCK
create_clock -period 20.000ns [get_ports OSC_50_B3B]
create_clock -period 20.000ns [get_ports OSC_50_B4A]
create_clock -period 20.000ns [get_ports OSC_50_B4D]
create_clock -period 20.000ns [get_ports OSC_50_B7A]
create_clock -period 20.000ns [get_ports OSC_50_B7D]
create_clock -period 20.000ns [get_ports OSC_50_B8A]
create_clock -period 20.000ns [get_ports OSC_50_B8D]

# The JTAG clock
create_clock -name {JTAG_clk} -period 20.000 -waveform { 0.000 10.000 } [get_ports {jtag_jtag_TCK}]

# #############################################################################
#  Now that we have created the custom clocks which will be base clocks,
#  derive_pll_clock is used to calculate all remaining clocks for PLLs
derive_pll_clocks
derive_clock_uncertainty

