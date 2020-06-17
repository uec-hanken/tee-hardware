# RISCV environment variable must be set

ifeq ($(ISACONF),RV64GC)
CC=riscv64-unknown-elf-gcc
OBJCOPY=riscv64-unknown-elf-objcopy
CFLAGS_ARCH=-march=rv64imafdc -mabi=lp64d
else ifeq ($(ISACONF),RV32GC)
CC=riscv32-unknown-elf-gcc
OBJCOPY=riscv32-unknown-elf-objcopy
CFLAGS_ARCH=-march=rv32imafdc -mabi=ilp32d
else #RV32IMAC
CC=riscv32-unknown-elf-gcc
OBJCOPY=riscv32-unknown-elf-objcopy
CFLAGS_ARCH=-march=rv32imac -mabi=ilp32
endif
CFLAGS=$(CFLAGS_ARCH) -O2 -std=gnu11 -Wall -I. -nostartfiles -fno-common -g $(ADD_OPTS)
LFLAGS=-static -nostdlib
XIP_TARGET_ADDR?=0x20000000

#dtb := $(BUILD_DIR)/$(long_name).dtb
#$(dtb): $(BUILD_DIR)/$(long_name).dts
#	dtc -I dts -O dtb -o $@ $<

#.PHONY: dtb
#dtb: $(dtb)

elf := xip.elf
#$(elf): xip.S $(dtb)
#	$(CC) $(CFLAGS) -DXIP_TARGET_ADDR=$(XIP_TARGET_ADDR) -DDEVICE_TREE='"$(dtb)"' $(LFLAGS) -o $@ $<
$(elf): xip.S linker.ld
	$(CC) $(CFLAGS) -DXIP_TARGET_ADDR=$(XIP_TARGET_ADDR) $(LFLAGS) -Tlinker.ld -o $@ $<

.PHONY: elf
elf: $(elf)

bin := xip.bin
$(bin): $(elf)
	$(OBJCOPY) -O binary $< $@

.PHONY: bin
bin: $(bin)

hex := xip.hex
$(hex): $(bin)
	od -t x4 -An -w4 -v $< > $@

.PHONY: hex
hex: $(hex)

romgen := rom.v
$(romgen): $(hex)
	$(ROMGEN) $(ROM_CONF_FILE) $< > $@
	cp rom.v $(ROM_FILE)

.PHONY: romgen
romgen: $(romgen)

.PHONY: clean
clean::
	rm -rf $(hex) $(elf) $(bin) $(romgen)
