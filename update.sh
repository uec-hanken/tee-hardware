git submodule update --init
cd hardware/chipyard/scripts/
./init-submodules-no-riscv-tools.sh
cd ../../../software/freedom-u540-c000-bootloader
git submodule update --init
cd ../../hardware/chipyard/
patch -p1 < ../../patches/chipyard.patch
cd ../../
