MEMORY
{
  debug_ctrl (rwx) : ORIGIN = 0x0, LENGTH = 0x1000
  error_mem (rw) : ORIGIN = 0x3000, LENGTH = 0x1000
  test_ctrl (rw) : ORIGIN = 0x4000, LENGTH = 0x1000
  maskrom_mem (rx) : ORIGIN = 0x10000, LENGTH = 0x4000
  clint_ctrl (rw) : ORIGIN = 0x2000000, LENGTH = 0x10000
  plic_ctrl (rw) : ORIGIN = 0xc000000, LENGTH = 0x4000000
  uart_ctrl (rw) : ORIGIN = 0x64000000, LENGTH = 0x1000
  spi_ctrl (rw) : ORIGIN = 0x64001000, LENGTH = 0x1000
  gpio_ctrl (rw) : ORIGIN = 0x64002000, LENGTH = 0x1000
  memory_mem (rwx) : ORIGIN = 0x80000000, LENGTH = 0xC00
}
