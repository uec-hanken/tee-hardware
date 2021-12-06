BOOTROM_DIR?=$(abspath .)

ISACONF?=RV64GC
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
CFLAGS=$(CFLAGS_ARCH) -mcmodel=medany -O2 -std=gnu11 -Wall -nostartfiles 
CFLAGS+= -fno-common -g -DENTROPY=0 -DNONSMP_HART=0 
CFLAGS+= -I $(BOOTROM_DIR)/include -I. -I./gpt -I./boot -I./sd -I./kprintf $(ADD_OPTS) 
LFLAGS=-static -nostdlib -L $(BOOTROM_DIR)/linker -T sdboot.elf.lds
SDBOOT_TARGET_ADDR?=0x8C000000UL
SDBOOT_SOURCE_ADDR?=0x20000000

dts := $(BUILD_DIR)/$(long_name).dts
dtb := $(BUILD_DIR)/$(long_name).dtb
clk := $(BUILD_DIR)/$(long_name).tl_clock.h

$(clk): $(dts)
	awk '/tlclk {/ && !f{f=1; next}; f && match($$0, /^.*clock-frequency.*<(.*)>.*/, arr) { print "#define TL_CLK " arr[1] "UL"}' $< > $@.tmp
	awk '/cpu@/{++cnt} END {print "#define NUM_CORES",cnt, "\n#define MAX_HART_ID",cnt*2}' $< >> $@.tmp
	mv $@.tmp $@

$(dtb): $(dts)
	dtc -I dts -O dtb -o $@ $<

.PHONY: dtb
dtb: $(dtb)

elf := $(BUILD_DIR)/sdboot.elf
$(elf): $(dtb) head.S kprintf/kprintf.c sd/sd.c boot/boot.c gpt/gpt.c main.c $(clk)
	echo MASKROM=$(SDBOOT_SOURCE_ADDR) > maskrom_loc.ld
	$(CC) $(CFLAGS) -include $(clk) -DDEVICE_TREE='"$(dtb)"' -DSDBOOT_TARGET_ADDR=$(SDBOOT_TARGET_ADDR) $(LFLAGS) -o $@ head.S sd/sd.c boot/boot.c gpt/gpt.c main.c kprintf/kprintf.c

.PHONY: elf
elf: $(elf)

bin := $(BUILD_DIR)/sdboot.bin
$(bin): $(elf)
	$(OBJCOPY) -O binary $< $@
	$(OBJDUMP) -d $^ > $@.dump
	- riscv32-unknown-elf-objdump -b binary -D $@ --adjust-vma=$(SDBOOT_SOURCE_ADDR) -m RISCV > $@.rv32.dump

.PHONY: bin
bin: $(bin)

hex := $(BUILD_DIR)/sdboot.hex
$(hex): $(bin)
	od -t x4 -An -w4 -v $< > $@

romgen := $(BUILD_DIR)/rom.v
$(romgen): $(hex)
	$(rocketchip_dir)/scripts/vlsi_rom_gen $(ROMCONF) $< > $@

.PHONY: hex
hex: $(hex)

.PHONY: clean
clean::
	rm -rf $(hex) $(elf)
