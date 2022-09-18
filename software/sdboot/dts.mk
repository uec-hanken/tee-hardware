# Device & clock include file rules
dts := $(BUILD_DIR)/$(long_name).dts
dtb := $(BUILD_DIR)/$(long_name).dtb
clk := $(BUILD_DIR)/$(long_name).tl_clock.h
$(clk): $(dts)
	awk '/tlclk {/ && !f{f=1; next}; f && match($$0, /^.*clock-frequency.*<(.*)>.*/, arr) { print "#define TL_CLK " arr[1] "UL"}' $< > $@.tmp
	awk '/cpus {/ && !f{f=1; next}; f && match($$0, /^.*timebase-frequency.*<(.*)>.*/, arr) { print "#define TIMEBASE_FREQ " arr[1] "UL"; exit}' $< >> $@.tmp
	awk '/cpu@/{++cnt} END {print "#define NUM_CORES",cnt, "\n#define MAX_HART_ID",cnt*2}' $< >> $@.tmp
	awk 'BEGIN {cnt = 0} /sha3@/{++cnt} END {print "#define NUM_SHA3",cnt}' $< >> $@.tmp
	mv $@.tmp $@

$(dtb): $(dts)
	dtc -I dts -O dtb -o $@ $<

.PHONY: dtb
dtb: $(dtb)

