#!/bin/bash
iverilog -o tb_base_point_multiplier.vvp -s tb_base_point_multiplier -I ../rtl/ tb_base_point_multiplier.v \
../rtl/mac16_generic.v \
../rtl/ed25519_microcode_rom.v \
../rtl/ed25519_operand_bank.v \
../rtl/adder47_generic.v \
../rtl/ed25519_uop_worker.v \
../rtl/subtractor32_generic.v \
../rtl/ed25519_wrapper.v \
../rtl/ed25519_core_top.v \
../rtl/bram_1rw_1ro_readfirst.v \
../rtl/bram_1rw_readfirst.v \
../rtl/bram_1wo_1ro_readfirst.v \
../rtl/ed25519_base_point_multiplier.v \
../rtl/curve25519_modular_multiplier.v \
../rtl/ed25519_banks_array.v \
../rtl/modular_adder.v \
../rtl/modular_subtractor.v \
../rtl/adder32_generic.v \
../rtl/multiword_mover.v

iverilog -o tb_modular_multiplier.vvp -s tb_modular_multiplier -I ../rtl/ tb_modular_multiplier.v \
../rtl/mac16_generic.v \
../rtl/ed25519_microcode_rom.v \
../rtl/ed25519_operand_bank.v \
../rtl/adder47_generic.v \
../rtl/ed25519_uop_worker.v \
../rtl/subtractor32_generic.v \
../rtl/ed25519_wrapper.v \
../rtl/ed25519_core_top.v \
../rtl/bram_1rw_1ro_readfirst.v \
../rtl/bram_1rw_readfirst.v \
../rtl/bram_1wo_1ro_readfirst.v \
../rtl/ed25519_base_point_multiplier.v \
../rtl/curve25519_modular_multiplier.v \
../rtl/ed25519_banks_array.v \
../rtl/modular_adder.v \
../rtl/modular_subtractor.v \
../rtl/adder32_generic.v \
../rtl/multiword_mover.v
