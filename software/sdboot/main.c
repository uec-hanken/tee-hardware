// See LICENSE for license details.
#include <stdint.h>

#include <platform.h>

#include "common.h"
#include "sd.h"
#include "fsblboot.h"
#include <gpt/gpt.h>

#define DEBUG
#include "kprintf.h"

extern const gpt_guid gpt_guid_sifive_fsbl;
int main(void)
{
	REG32(uart, UART_REG_TXCTRL) = UART_TXEN;

	sd_init();
	
	int error = fsblboot_load_gpt_partition((void*) PAYLOAD_DEST, &gpt_guid_sifive_fsbl);
	
	if(error) {
	  copy();
	}

	kputs("BOOT");

	__asm__ __volatile__ ("fence.i" : : : "memory");
	return 0;
}
