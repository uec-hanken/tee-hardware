#ifndef _SDBOOT_COMMON_H
#define _SDBOOT_COMMON_H

#define MAX_CORES 8

#ifndef TL_CLK
#error Must define TL_CLK
#endif

#ifndef SDBOOT_TARGET_ADDR
#define SDBOOT_TARGET_ADDR 0x90000000UL
#endif

#define PAYLOAD_DEST SDBOOT_TARGET_ADDR
#define PAYLOAD_SIZE	(26 << 11)

#define F_CLK TL_CLK
#define CORE_CLK_KHZ TL_CLK/1000

#endif
