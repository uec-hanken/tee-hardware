#BUILD_DIR=
#ADD_OPTS=
teehw_sdboot := $(abspath .)
elf := $(BUILD_DIR)/sdboot.elf
bin := $(BUILD_DIR)/sdboot.bin
hex := $(BUILD_DIR)/sdboot.hex
CSRC := $(teehw_sdboot)/sd/sd.c $(teehw_sdboot)/boot/boot.c $(teehw_sdboot)/gpt/gpt.c $(teehw_sdboot)/main.c
CFLAGS= -I$(teehw_sdboot)/sd -I$(teehw_sdboot)/boot -I$(teehw_sdboot)/gpt

include $(teehw_sdboot)/dts.mk
include $(teehw_sdboot)/common.mk

