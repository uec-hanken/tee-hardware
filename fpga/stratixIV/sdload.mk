# ROM Generation for ZSBL SD Load
bootrom_dir=$(base_dir)/software/freedom-u540-c000-bootloader

DTS_FILE = $(build_dir)/$(long_name).dts
BOOT_FILE = $(bootrom_dir)/vc707zsbl.hex

.PHONY: $(BOOT_FILE)
$(BOOT_FILE): $(bootrom_dir)/Makefile
	awk '/tlclk {/ && !f{f=1; next}; f && match($$0, /^.*clock-frequency.*<(.*)>.*/, arr) { print "#define TL_CLK " arr[1] "UL"}' $(DTS_FILE) > $(bootrom_dir)/tl_clock.h
	cp $(DTS_FILE) $(bootrom_dir)/zsbl/ux00_zsbl.dts
	cp $(DTS_FILE) $(bootrom_dir)/fsbl/ux00_fsbl.dts
	echo "#define vc707 1" > $(bootrom_dir)/board.h
	echo "#define SKIP_ECC_WIPEDOWN 1" >> $(bootrom_dir)/board.h
	sed -i -e 's/INCLUDE\smemory.lds/INCLUDE memory_vc707.lds/g' $(bootrom_dir)/ux00_zsbl.lds
	sed -i -e 's/INCLUDE\smemory.lds/INCLUDE memory_vc707.lds/g' $(bootrom_dir)/ux00_fsbl.lds
	make -C $(bootrom_dir) vc707zsbl.hex vc707fsbl.bin

