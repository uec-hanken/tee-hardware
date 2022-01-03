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
#define UART_MIN_CLK_DIV (((TL_CLK + UART_BAUD - 1) / (UART_BAUD)) - 1)

extern const gpt_guid gpt_guid_sifive_fsbl;
int main(void)
{
	REG32(uart, UART_REG_TXCTRL) = UART_TXEN;
  REG32(uart, UART_REG_DIV) = UART_MIN_CLK_DIV;
	spi = (void *)(SPI_CTRL_ADDR); // Default to the SPI

	sd_init(CORE_CLK_KHZ);
	
	int error = boot_load_gpt_partition((void*) PAYLOAD_DEST, &gpt_guid_sifive_fsbl);
	
	if(error) {
	  copy();
	}

	kputs("BOOT");

	__asm__ __volatile__ ("fence.i" : : : "memory");
	return 0;
}
