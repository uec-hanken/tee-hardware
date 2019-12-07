# ROM Generation for jumping to the qspi
bootrom_dir=$(base_dir)/software/xip

DTS_FILE = $(build_dir)/$(long_name).dts
BOOT_FILE = $(build_dir)/xip.hex

.PHONY: $(BOOT_FILE)
$(BOOT_FILE): $(bootrom_dir)/Makefile
	make -C $(bootrom_dir) BUILD_DIR=$(build_dir) long_name=$(long_name) hex

