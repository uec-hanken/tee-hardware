# RISCV environment variable must be set

teehw_sdboot := $(abspath ../sdboot)
CROSSCOMPILE?=riscv64-unknown-elf
CC=$(CROSSCOMPILE)-gcc
OBJCOPY=$(CROSSCOMPILE)-objcopy
OBJDUMP=$(CROSSCOMPILE)-objdump
ifeq ($(ISACONF),RV64GC)
CFLAGS_ARCH=-march=rv64imafdc -mabi=lp64d
else ifeq ($(ISACONF),RV64IMAC)
CFLAGS_ARCH=-march=rv64imac -mabi=lp64
else ifeq ($(ISACONF),RV32GC)
CFLAGS_ARCH=-march=rv32imafdc -mabi=ilp32d
else #RV32IMAC
CFLAGS_ARCH=-march=rv32imac -mabi=ilp32
endif
CFLAGS=$(CFLAGS_ARCH) -O2 -std=gnu11 -Wall -I. -nostartfiles -fno-common -g $(ADD_OPTS)
LFLAGS=-static -nostdlib
XIP_TARGET_ADDR?=0x20000000

include $(teehw_sdboot)/dts.mk

elf := $(BUILD_DIR)/xip.elf
$(elf): xip.S linker.ld $(clk) $(dtb)
	$(CC) $(CFLAGS) -include $(clk) -DDEVICE_TREE='"$(dtb)"' -DXIP_TARGET_ADDR=$(XIP_TARGET_ADDR) $(LFLAGS) -Tlinker.ld -o $@ $<

.PHONY: elf
elf: $(elf)

bin := $(BUILD_DIR)/xip.bin
$(bin): $(elf)
	$(OBJCOPY) -O binary $< $@
	$(OBJDUMP) -d $^ > $@.dump
	- riscv32-unknown-elf-objdump -b binary -D $@ --adjust-vma=0x10000 -m RISCV > $@.rv32.dump

.PHONY: bin
bin: $(bin)

hex := $(BUILD_DIR)/xip.hex
$(hex): $(bin)
	od -t x4 -An -w4 -v $< > $@

.PHONY: hex
hex: $(hex)

.PHONY: clean
clean:
	rm -rf $(hex) $(elf) $(bin) $(romgen)
