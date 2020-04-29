git submodule update --init
cd hardware/hardware/chipyard/scripts/
./init-submodules-no-riscv-tools.sh --no-firesim
cd ../../../software/freedom-u540-c000-bootloader
git submodule update --init
cd ../../
