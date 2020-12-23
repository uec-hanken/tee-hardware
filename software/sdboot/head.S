// See LICENSE for license details.
#include <platform.h>
#include <smp.h>
#include "common.h"

  .section .text.init
  .option norvc
  .globl _prog_start
_prog_start:
  csrr a0, mhartid
  li a1, NUM_CORES
1:bge a0, a1, 1b     // If the hart ID is greater or equals the NUMCORES, that means is a ghost
  smp_pause(s1, s2)
  li sp, (MEMORY_MEM_ADDR + MEMORY_MEM_SIZE - 0x1000)
  call main
  smp_resume(s1, s2)
  csrr a0, mhartid
  la a1, dtb
  li s1, PAYLOAD_DEST
  jr s1

  .section .rodata
dtb:
  .incbin DEVICE_TREE
