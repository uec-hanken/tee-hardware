// See LICENSE for license details.
#include <stdint.h>

#include <platform.h>

#include "common.h"
#include "sd.h"

#define DEBUG
#include "kprintf.h"

#define SD_CMD_GO_IDLE_STATE 0
#define SD_CMD_SEND_IF_COND 8
#define SD_CMD_STOP_TRANSMISSION 12
#define SD_CMD_SET_BLOCKLEN 16
#define SD_CMD_READ_BLOCK_MULTIPLE 18
#define SD_CMD_APP_SEND_OP_COND 41
#define SD_CMD_APP_CMD 55
#define SD_CMD_READ_OCR 58
#define SD_RESPONSE_IDLE 0x1
// Data token for commands 17, 18, 24
#define SD_DATA_TOKEN 0xfe

// SD card initialization must happen at 100-400kHz
#define SD_POWER_ON_FREQ_KHZ 400L
// SD cards normally support reading/writing at 20MHz
#define SD_POST_INIT_CLK_KHZ 20000L

// Command frame starts by asserting low and then high for first two clock edges
#define SD_CMD(cmd) (0x40 | (cmd))

// Inlining header functions in C
// https://stackoverflow.com/a/23699777/7433423

/**
 * Get smallest clock divisor that divides input_khz to a quotient less than or
 * equal to max_target_khz;
 */
inline unsigned int spi_min_clk_divisor(unsigned int input_khz, unsigned int max_target_khz)
{
  // f_sck = f_in / (2 * (div + 1)) => div = (f_in / (2*f_sck)) - 1
  //
  // The nearest integer solution for div requires rounding up as to not exceed
  // max_target_khz.
  //
  // div = ceil(f_in / (2*f_sck)) - 1
  //     = floor((f_in - 1 + 2*f_sck) / (2*f_sck)) - 1
  //
  // This should not overflow as long as (f_in - 1 + 2*f_sck) does not exceed
  // 2^32 - 1, which is unlikely since we represent frequencies in kHz.
  unsigned int quotient = (input_khz + 2 * max_target_khz - 1) / (2 * max_target_khz);
  // Avoid underflow
  if (quotient == 0) {
    return 0;
  } else {
    return quotient - 1;
  }
}

uint32_t volatile * spi = (void *)0;

static inline uint8_t spi_xfer(uint8_t d)
{
	int32_t r;

	REG32(spi, SPI_REG_TXFIFO) = d;
	do {
		r = REG32(spi, SPI_REG_RXFIFO);
	} while (r < 0);
	return r;
}

static inline uint8_t sd_dummy(void)
{
	return spi_xfer(0xFF);
}

static uint8_t sd_cmd(uint8_t cmd, uint32_t arg, uint8_t crc)
{
	unsigned long n;
	uint8_t r;

	REG32(spi, SPI_REG_CSMODE) = SPI_CSMODE_HOLD;
	sd_dummy();
	spi_xfer(cmd);
	spi_xfer(arg >> 24);
	spi_xfer(arg >> 16);
	spi_xfer(arg >> 8);
	spi_xfer(arg);
	spi_xfer(crc);

	n = 1000;
	do {
		r = sd_dummy();
		if (!(r & 0x80)) {
//			dprintf("sd:cmd: %hx\r\n", r);
			goto done;
		}
	} while (--n > 0);
	kputs("sd_cmd: timeout");
done:
	return r;
}

static inline void sd_cmd_end(void)
{
	sd_dummy();
	REG32(spi, SPI_REG_CSMODE) = SPI_CSMODE_AUTO;
}


static void sd_poweron(unsigned int input_clk_khz)
{
	long i;
	REG32(spi, SPI_REG_SCKDIV) = spi_min_clk_divisor(input_clk_khz, SD_POWER_ON_FREQ_KHZ);
	REG32(spi, SPI_REG_CSMODE) = SPI_CSMODE_OFF;
	for (i = 10; i > 0; i--) {
		sd_dummy();
	}
	REG32(spi, SPI_REG_CSMODE) = SPI_CSMODE_AUTO;
}

static int sd_cmd0(void)
{
	int rc;
	dputs("CMD0");
	rc = (sd_cmd(0x40, 0, 0x95) != 0x01);
	sd_cmd_end();
	return rc;
}

static int sd_cmd8(void)
{
	int rc;
	dputs("CMD8");
	rc = (sd_cmd(0x48, 0x000001AA, 0x87) != 0x01);
	sd_dummy(); /* command version; reserved */
	sd_dummy(); /* reserved */
	rc |= ((sd_dummy() & 0xF) != 0x1); /* voltage */
	rc |= (sd_dummy() != 0xAA); /* check pattern */
	sd_cmd_end();
	return rc;
}

static void sd_cmd55(void)
{
	sd_cmd(0x77, 0, 0x65);
	sd_cmd_end();
}

static int sd_acmd41(void)
{
	uint8_t r;
	dputs("ACMD41");
	do {
		sd_cmd55();
		r = sd_cmd(0x69, 0x40000000, 0x77); /* HCS = 1 */
	} while (r == 0x01);
	return (r != 0x00);
}

static int sd_cmd58(void)
{
	int rc;
	dputs("CMD58");
	rc = (sd_cmd(0x7A, 0, 0xFD) != 0x00);
	rc |= ((sd_dummy() & 0x80) != 0x80); /* Power up status */
	sd_dummy();
	sd_dummy();
	sd_dummy();
	sd_cmd_end();
	return rc;
}

static int sd_cmd16(void)
{
	int rc;
	dputs("CMD16");
	rc = (sd_cmd(0x50, 0x200, 0x15) != 0x00);
	sd_cmd_end();
	return rc;
}

static uint16_t crc16(uint16_t crc, uint8_t data) {
	crc = (uint8_t)(crc >> 8) | (crc << 8);
	crc ^= data;
	crc ^= (uint8_t)(crc >> 4) & 0xf;
	crc ^= crc << 12;
	crc ^= (crc & 0xff) << 5;
	return crc;
}

#define SPIN_SHIFT	6
#define SPIN_UPDATE(i)	(!((i) & ((1 << SPIN_SHIFT)-1)))
#define SPIN_INDEX(i)	(((i) >> SPIN_SHIFT) & 0x3)

static const char spinner[] = { '-', '/', '|', '\\' };

static uint8_t crc7(uint8_t prev, uint8_t in)
{
  // CRC polynomial 0x89
  uint8_t remainder = prev & in;
  remainder ^= (remainder >> 4) ^ (remainder >> 7);
  remainder ^= remainder << 4;
  return remainder & 0x7f;
}

int sd_copy(void* dst, uint32_t src_lba, size_t size)
{
  volatile uint8_t *p = dst;
  long i = size;
  int rc = 0;

  uint8_t crc = 0;
  crc = crc7(crc, SD_CMD(SD_CMD_READ_BLOCK_MULTIPLE));
  crc = crc7(crc, src_lba >> 24);
  crc = crc7(crc, (src_lba >> 16) & 0xff);
  crc = crc7(crc, (src_lba >> 8) & 0xff);
  crc = crc7(crc, src_lba & 0xff);
  crc = (crc << 1) | 1;
  if (sd_cmd(SD_CMD(SD_CMD_READ_BLOCK_MULTIPLE), src_lba, crc) != 0x00) {
    sd_cmd_end();
    return SD_COPY_ERROR_CMD18;
  }
  do {
    uint16_t crc, crc_exp;
    long n;

    crc = 0;
    n = 512;
    while (sd_dummy() != SD_DATA_TOKEN);
    do {
      uint8_t x = sd_dummy();
      *p++ = x;
      crc = crc16(crc, x);
    } while (--n > 0);

    crc_exp = ((uint16_t)sd_dummy() << 8);
    crc_exp |= sd_dummy();

    if (crc != crc_exp) {
			kputs("\b- CRC mismatch ");
			rc = SD_COPY_ERROR_CMD18_CRC;
      break;
    }
    
    if (SPIN_UPDATE(i)) {
			kputc('\b');
			kputc(spinner[SPIN_INDEX(i)]);
		}
  } while (--i > 0);

  sd_cmd(SD_CMD(SD_CMD_STOP_TRANSMISSION), 0, 0x01);
  sd_cmd_end();
  return rc;
}

int sd_init(unsigned int input_clk_khz)
{
  kputs("INIT");
	sd_poweron(input_clk_khz);
	if (sd_cmd0() ||
	    sd_cmd8() ||
	    sd_acmd41() ||
	    sd_cmd58() ||
	    sd_cmd16()) {
		kputs("ERROR");
		return 1;
	}
	REG32(spi, SPI_REG_SCKDIV) = spi_min_clk_divisor(input_clk_khz, SD_POST_INIT_CLK_KHZ);
	return 0;
}

// copy() -- The original copy. It just copies the first PAYLOAD_SIZE
int copy(void)
{
	volatile uint8_t *p = (void *)(PAYLOAD_DEST);
	long i = PAYLOAD_SIZE;
	int rc = 0;

	dputs("CMD18");
	kprintf("LOADING  ");

	if (sd_cmd(0x52, 0, 0xE1) != 0x00) {
		sd_cmd_end();
		return 1;
	}
	do {
		uint16_t crc, crc_exp;
		long n;

		crc = 0;
		n = 512;
		while (sd_dummy() != 0xFE);
		do {
			uint8_t x = sd_dummy();
			*p++ = x;
			crc = crc16(crc, x);
		} while (--n > 0);

		crc_exp = ((uint16_t)sd_dummy() << 8);
		crc_exp |= sd_dummy();

		if (crc != crc_exp) {
			kputs("\b- CRC mismatch ");
			rc = 1;
			break;
		}

		if (SPIN_UPDATE(i)) {
			kputc('\b');
			kputc(spinner[SPIN_INDEX(i)]);
		}
	} while (--i > 0);
	sd_cmd_end();

	sd_cmd(0x4C, 0, 0x01);
	sd_cmd_end();
	kputs("\b ");
	return rc;
}

