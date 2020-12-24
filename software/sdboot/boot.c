// See LICENSE for license details.
#include <stdint.h>

#include <platform.h>

#include "common.h"
#include "sd.h"
#include "boot.h"
#include <gpt/gpt.h>

#define DEBUG
#include "kprintf.h"

#define GPT_BLOCK_SIZE 512

static int decode_sd_copy_error(int error)
{
  switch (error) {
    case SD_COPY_ERROR_CMD18: kputs("ERROR: CMD18\n");
    case SD_COPY_ERROR_CMD18_CRC: kputs("ERROR: CMD18_CRC\n");
    default: kputs("ERROR: UNKNOWN\n");
  }
  return error;
}

static gpt_partition_range find_sd_gpt_partition(
  uint64_t partition_entries_lba,
  uint32_t num_partition_entries,
  uint32_t partition_entry_size,
  const gpt_guid* partition_type_guid,
  void* block_buf  // Used to temporarily load blocks of SD card
)
{
  // Exclusive end
  uint64_t partition_entries_lba_end = (
    partition_entries_lba +
    (num_partition_entries * partition_entry_size + GPT_BLOCK_SIZE - 1) / GPT_BLOCK_SIZE
  );
  for (uint64_t i = partition_entries_lba; i < partition_entries_lba_end; i++) {
    sd_copy(block_buf, i, 1);
    gpt_partition_range range = gpt_find_partition_by_guid(
      block_buf, partition_type_guid, GPT_BLOCK_SIZE / partition_entry_size
    );
    if (gpt_is_valid_partition_range(range)) {
      return range;
    }
  }
  return gpt_invalid_partition_range();
}

static int load_sd_gpt_partition(void* dst, const gpt_guid* partition_type_guid)
{
  uint8_t gpt_buf[GPT_BLOCK_SIZE];
  int error;
  error = sd_copy(gpt_buf, GPT_HEADER_LBA, 1);
  if (error) return decode_sd_copy_error(error);

  gpt_partition_range part_range;
  {
    // header will be overwritten by find_sd_gpt_partition(), so locally
    // scope it.
    gpt_header* header = (gpt_header*) gpt_buf;
    part_range = find_sd_gpt_partition(
      header->partition_entries_lba,
      header->num_partition_entries,
      header->partition_entry_size,
      partition_type_guid,
      gpt_buf
    );
  }

  if (!gpt_is_valid_partition_range(part_range)) {
    kputs("ERROR: GPT partition not found\n");
    return 1;
  }

  error = sd_copy(
    dst,
    part_range.first_lba,
    part_range.last_lba + 1 - part_range.first_lba
  );
  if (error) return decode_sd_copy_error(error);
  return 0;
}

//==============================================================================
// Public functions
//==============================================================================

/**
 * Load GPT partition match specified partition type into specified memory.
 *
 * Read from mode select device to determine which bulk storage medium to read
 * GPT image from, and properly initialize the bulk storage based on type.
 */
int boot_load_gpt_partition(void* dst, const gpt_guid* partition_type_guid)
{
  unsigned int error = 0;

  // At this point, the SD MUST be activated
  error = load_sd_gpt_partition(dst, partition_type_guid);

  if (error) {
    boot_fail(error, 0);
  }
  
  return error;
}

void boot_fail(long code, int trap)
{
  kputs("BOOT BY GPT FAILED\n");
}

