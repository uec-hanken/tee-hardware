# RISCV environment variable must be set

CC=$(RISCV)/bin/riscv64-unknown-elf-gcc
OBJCOPY=$(RISCV)/bin/riscv64-unknown-elf-objcopy
CFLAGS=-march=rv64imac -mabi=lp64 -O2 -std=gnu11 -Wall -I. -nostartfiles -fno-common -g
LFLAGS=-static -nostdlib
XIP_TARGET_ADDR?=0x20000000

dtb := $(BUILD_DIR)/$(long_name).dtb
$(dtb): $(BUILD_DIR)/$(long_name).dts
	dtc -I dts -O dtb -o $@ $<

.PHONY: dtb
dtb: $(dtb)

elf := $(BUILD_DIR)/xip.elf
$(elf): xip.S $(dtb)
	$(CC) $(CFLAGS) -DXIP_TARGET_ADDR=$(XIP_TARGET_ADDR) -DDEVICE_TREE='"$(dtb)"' $(LFLAGS) -o $@ $<

.PHONY: elf
elf: $(elf)

bin := $(BUILD_DIR)/xip.bin
$(bin): $(elf)
	$(OBJCOPY) -O binary $< $@

.PHONY: bin
bin: $(bin)

hex := $(BUILD_DIR)/xip.hex
$(hex): $(bin)
	od -t x4 -An -w4 -v $< > $@

.PHONY: hex
hex: $(hex)

romgen := $(BUILD_DIR)/rom.v
$(romgen): $(hex)
	$(rocketchip_dir)/scripts/vlsi_rom_gen $(ROMCONF) $< > $@

.PHONY: romgen
romgen: $(romgen)

.PHONY: clean
clean::
	rm -rf $(hex) $(elf) 
