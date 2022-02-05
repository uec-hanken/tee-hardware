# These should be defined by the Makefile
#teehw_sdboot := $(abspath ../../tee-hardware/software/sdboot)
#BUILD_DIR=
#ADD_OPTS=
#elf := $(BUILD_DIR)/sdbootfull.elf
#CSRC := $(teehw_sdboot)/main.c

ISACONF?=RV64GC
CROSSCOMPILE?=riscv64-unknown-elf
CC=$(CROSSCOMPILE)-gcc
OBJCOPY=$(CROSSCOMPILE)-objcopy
OBJDUMP=$(CROSSCOMPILE)-objdump

CFLAGS:=-I$(teehw_sdboot)/include -I$(teehw_sdboot) -I$(teehw_sdboot)/kprintf -I$(BUILD_DIR) $(ADD_OPTS) $(CFLAGS)
CFLAGS:=-O2 -std=gnu11 -Wall -nostartfiles -fno-common -g -DENTROPY=0 $(CFLAGS)
ifeq ($(ISACONF),RV64GC)
CFLAGS:=-march=rv64imafdc -mabi=lp64d -mcmodel=medany $(CFLAGS)
else ifeq ($(ISACONF),RV64IMAC)
CFLAGS:=-march=rv64imac -mabi=lp64 -mcmodel=medany $(CFLAGS)
else ifeq ($(ISACONF),RV32GC)
CFLAGS:=-march=rv32imafdc -mabi=ilp32d -mcmodel=medany $(CFLAGS)
else ifeq ($(ISACONF),RV32IMC)
CFLAGS:=-march=rv32imc -mabi=ilp32 -mcmodel=medany $(CFLAGS)
else #RV32IMAC
CFLAGS:=-march=rv32imac -mabi=ilp32 -mcmodel=medany $(CFLAGS)
endif
LFLAGS:=-static -nostdlib -L $(teehw_sdboot)/linker -T sdboot.elf.lds
SDBOOT_TARGET_ADDR?=0x82000000UL
SDBOOT_SOURCE_ADDR?=0x20000000

ifneq ($(dtb),)
DEVICE_TREE_CFLAG:=-DDEVICE_TREE='"$(dtb)"'
endif

ifneq ($(clk),)
INCLUDE_CLK_CFLAG:=-include $(clk)
endif

ifeq ($(all_src),)
all_src:=$(teehw_sdboot)/head.S $(teehw_sdboot)/kprintf/kprintf.c $(CSRC)
endif

# ELF rules
$(elf): $(all_src) $(clk) $(dtb) $(m_key_h) $(dev_key_h)
	$(CC) $(CFLAGS) $(INCLUDE_CLK_CFLAG) $(DEVICE_TREE_CFLAG) -DSDBOOT_TARGET_ADDR=$(SDBOOT_TARGET_ADDR) $(LFLAGS) -o $@ $(all_src)

.PHONY: elf
elf: $(elf)

$(bin): $(elf)
	$(OBJCOPY) -O binary $< $@
	$(OBJDUMP) -d $^ > $@.dump

.PHONY: bin
bin: $(bin)

$(hex): $(bin)
	od -t x4 -An -w4 -v $< > $@

.PHONY: hex
hex: $(hex)

.PHONY: clean
clean::
	rm -rf $(hex) $(elf) $(bin) $(bin).dump

