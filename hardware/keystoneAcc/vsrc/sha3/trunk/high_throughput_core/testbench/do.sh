#!/bin/bash
iverilog -o test_keccak.vvp -s test_keccak test_f_permutation.v  test_keccak.v  test_padder1.v  test_padder.v  test_rconst2in1.v ../rtl/f_permutation.v ../rtl/round2in1.v ../rtl/padder1.v ../rtl/keccak.v ../rtl/padder.v ../rtl/rconst2in1.v
