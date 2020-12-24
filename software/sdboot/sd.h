/* See the file LICENSE for information */

#ifndef _LIBRARIES_SD_H
#define _LIBRARIES_SD_H

#define SD_COPY_ERROR_CMD18 1
#define SD_COPY_ERROR_CMD18_CRC 2

#ifndef __ASSEMBLER__

#include <stdint.h>
#include <stddef.h>

extern uint32_t * spi;
int sd_init(unsigned int input_clk_khz);
int sd_copy(void* dst, uint32_t src_lba, size_t size);
int copy(void);

#endif /* !__ASSEMBLER__ */

#endif /* _LIBRARIES_SD_H */
