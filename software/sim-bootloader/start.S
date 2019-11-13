// See LICENSE for license details.
#include <encoding.h>

	.section .init
	.globl _start
	.type _start,@function

_start:
	.cfi_startproc
	.cfi_undefined ra
.option push
.option norelax
  // A LOT of memory, but helps to have consistent the simulations
  addi x1, zero, 0
  addi x2, zero, 0
  addi x3, zero, 0
  addi x4, zero, 0
  addi x5, zero, 0
  addi x6, zero, 0
  addi x7, zero, 0
  addi x8, zero, 0
  addi x9, zero, 0
  addi x10, zero, 0
  addi x11, zero, 0
  addi x12, zero, 0
  addi x13, zero, 0
  addi x14, zero, 0
  addi x15, zero, 0
  addi x16, zero, 0
  addi x17, zero, 0
  addi x18, zero, 0
  addi x19, zero, 0
  addi x20, zero, 0
  addi x21, zero, 0
  addi x22, zero, 0
  addi x23, zero, 0
  addi x24, zero, 0
  addi x25, zero, 0
  addi x26, zero, 0
  addi x27, zero, 0
  addi x28, zero, 0
  addi x29, zero, 0
  addi x30, zero, 0
  addi x31, zero, 0

  // Initialize the global pointer
	la gp, __global_pointer$
.option pop
	la sp, _sp
	mv s0, sp

#ifdef TEXT_IN_RAM
	/* Load text and read-only sections */
	la a0, _text_lma
	la a1, _text
	la a2, _etext
	bgeu a1, a2, 2f
1:
	lw t0, (a0)
	sw t0, (a1)
	lw t0, (a1)
	addi a0, a0, 4
	addi a1, a1, 4
	bltu a1, a2, 1b
2:
#endif // TEXT_IN_RAM

	/* Load data section */
	la a0, _data_lma
	la a1, _data
	la a2, _edata
	bgeu a1, a2, 2f
1:
	lw t0, (a0)
	sw t0, (a1)
	lw t0, (a1)
	addi a0, a0, 4
	addi a1, a1, 4
	bltu a1, a2, 1b
2:

	/* Clear bss section */
	la a0, __bss_start
	la a1, _end
	bgeu a0, a1, 2f
1:
	sw zero, (a0)
	lw zero, (a0)
	addi a0, a0, 4
	bltu a0, a1, 1b
2:

  // Prepare the execution environment
  auipc ra, 0         // Put ra here, the debugger understands this as end of stack
	addi sp, sp, -16    // Make this "_start" like a function
#if __riscv_xlen == 32
	sw ra, 8(sp)
#else
	sd ra, 8(sp)
#endif

	/* argc = argv = 0 */
	li a0, 0
	li a1, 0
	
	// Just call main
	call main
	
	// Try to call power off in htif
  //call htif_poweroff
	// Infinite loop if exited of main 
1:
	j 1b

	.cfi_endproc

.section ".tohost","aw",@progbits
.align 6
.globl tohost
tohost: .dword 0
.align 6
.globl fromhost
fromhost: .dword 0
