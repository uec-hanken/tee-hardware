// See LICENSE for license details.
#include <stdint.h>

#include <platform.h>

#include "common.h"
#include "sd.h"
#include "boot.h"
#include <gpt/gpt.h>

#define DEBUG
#include "kprintf.h"

#define UART_BAUD 115200

#define DERIVE_FROM_TIMESCALE

#ifdef DERIVE_FROM_TIMESCALE
#include "encoding.h"
static unsigned long mtime_lo(void)
{
  return *(volatile unsigned long *)(CLINT_CTRL_ADDR + CLINT_MTIME);
}

unsigned long get_timer_freq()
{
  return TIMEBASE_FREQ;
}

static unsigned long __attribute__((noinline)) measure_cpu_freq(size_t n)
{
  unsigned long start_mtime, delta_mtime;
  unsigned long mtime_freq = get_timer_freq();

  // Don't start measuruing until we see an mtime tick
  unsigned long tmp = mtime_lo();
  do {
    start_mtime = mtime_lo();
  } while (start_mtime == tmp);

  unsigned long start_mcycle = read_csr(mcycle);

  do {
    delta_mtime = mtime_lo() - start_mtime;
  } while (delta_mtime < n);

  unsigned long delta_mcycle = read_csr(mcycle) - start_mcycle;

  return (delta_mcycle / delta_mtime) * mtime_freq
         + ((delta_mcycle % delta_mtime) * mtime_freq) / delta_mtime;
}

unsigned long get_cpu_freq()
{
  uint32_t cpu_freq = 0;

  if (!cpu_freq) {
    // warm up I$
    measure_cpu_freq(1);
    // measure for real
    cpu_freq = measure_cpu_freq(10);
  }

  return cpu_freq;
}
#define UART_MIN_CLK_DIV (((freq + UART_BAUD - 1) / (UART_BAUD)) - 1)
#else
#define UART_MIN_CLK_DIV (((TL_CLK + UART_BAUD - 1) / (UART_BAUD)) - 1)
#endif // DERIVE_FROM_TIMESCALE

extern const gpt_guid gpt_guid_sifive_fsbl;
int main(void)
{
#ifdef DERIVE_FROM_TIMESCALE
  int freq = get_cpu_freq();
#endif
	REG32(uart, UART_REG_TXCTRL) = UART_TXEN;
  REG32(uart, UART_REG_DIV) = UART_MIN_CLK_DIV;
	spi = (void *)(SPI_CTRL_ADDR); // Default to the SPI

#ifdef DERIVE_FROM_TIMESCALE
  sd_init(freq/1000);
#else
	sd_init(CORE_CLK_KHZ);
#endif
	
	int error = boot_load_gpt_partition((void*) PAYLOAD_DEST, &gpt_guid_sifive_fsbl);
	
	if(error) {
	  copy();
	}

	kputs("BOOT");

	__asm__ __volatile__ ("fence.i" : : : "memory");
	return 0;
}
