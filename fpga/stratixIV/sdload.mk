# ROM Generation for ZSBL SD Load
sdload_dir=$(base_dir)/software/freedom-u540-c000-bootloader

SDLOAD_FILE = $(sdload_dir)/vc707zsbl.hex

$(SDLOAD_FILE): $(sdload_dir)/Makefile
	awk '/tlclk {/ && !f{f=1; next}; f && match($$0, /^.*clock-frequency.*<(.*)>.*/, arr) { print "#define TL_CLK " arr[1] "UL"}' $(DTS_FILE) > $(sdload_dir)/tl_clock.h
	cp $(DTS_FILE) $(sdload_dir)/zsbl/ux00_zsbl.dts
	cp $(DTS_FILE) $(sdload_dir)/fsbl/ux00_fsbl.dts
	echo "#define vc707 1" > $(sdload_dir)/board.h
	echo "#define SKIP_ECC_WIPEDOWN 1" >> $(sdload_dir)/board.h
	sed -i -e 's/INCLUDE\smemory.lds/INCLUDE memory_de4.lds/g' $(sdload_dir)/ux00_zsbl.lds
	sed -i -e 's/INCLUDE\smemory.lds/INCLUDE memory_de4.lds/g' $(sdload_dir)/ux00_fsbl.lds
	make -C $(sdload_dir) vc707zsbl.hex vc707fsbl.bin

