package uec.teehardware.devices.chacha


object ChachaRegs {
  val rng_imr = 0x100
  val rng_isr = 0x104
  val rng_icr = 0x108
  val trng_config = 0x10C
  val trng_valid = 0x110
  val ehr_data0 = 0x114
  val ehr_data1 = 0x118
  val ehr_data2 = 0x11C
  val ehr_data3 = 0x120
  val ehr_data4 = 0x124
  val ehr_data5 = 0x128
  val rnd_source_enable = 0x12C
  val sample_cnt1 = 0x130
  val autocorr_statistic = 0x134
  val trng_debug_control = 0x138
  val trng_sw_reset = 0x140
  val trng_busy = 0x1B8
  val rst_bits_counter = 0x1BC
  val rng_bist_cntr0 = 0x1E0
  val rng_bist_cntr1 = 0x1E4
  val rng_bist_cntr2 = 0x1E8
  val trng_debug_ctrl = 0x200
  val trng_debug_out = 0x204
  val trng_debug_stop = 0x208
  val trng_debug_counters = 0x20C
}
