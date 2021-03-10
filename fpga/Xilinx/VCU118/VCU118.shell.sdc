# TODO: This file do not need to get added. The XDC alternative is enough
# ------------------------- Base Clocks --------------------
create_clock -name sys_clock -period 5.0 [get_ports {sys_clock_p}]
set_input_jitter sys_clock 0.5
create_clock -name JTCK -period 100.0 [get_ports {jtag_jtag_TCK}]
set_input_jitter JTCK 0.5
# ------------------------- Clock Groups -------------------
set_clock_groups -asynchronous \
  -group [list [get_clocks { \
      clk_pll_i \
    }]] \
  -group [list [get_clocks -of_objects [get_pins { \
      corePLL/clk_out1 \
    }]]] \
  -group [list [get_clocks { \
      JTCK \
    }]]
# ------------------------- False Paths --------------------
set_false_path -through [get_pins {fpga_power_on/power_on_reset}]
# ------------------------- IO Timings ---------------------

