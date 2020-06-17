// See LICENSE for license details.
#include <encoding.h>
#include <smp.h>

	.section .init
	.globl _start
	.type _start,@function

_start:
	.cfi_startproc
	.cfi_undefined ra
.option push
.option norelax
  la t0, trap_entry
  csrw mtvec, t0
  li t0, MSTATUS_FS | MSTATUS_XS
  csrs mstatus, t0
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
	csrr t0, mhartid
  slli t0, t0, 12
  la sp, _sp
  sub sp, sp, t0
	mv s0, sp
	
  smp_pause(s1, s2)

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

  smp_resume(s1, s2)

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

#define STR1(x) #x
#define STR(x) STR1(x)
#define EXTRACT(a, size, offset) (((~(~0 << size) << offset) & a) >> offset)

#define CUSTOMX_OPCODE(x) CUSTOM_ ## x
#define CUSTOM_0 0b0001011
#define CUSTOM_1 0b0101011
#define CUSTOM_2 0b1011011
#define CUSTOM_3 0b1111011

#define CUSTOMX(X, xd, xs1, xs2, rd, rs1, rs2, funct) \
  CUSTOMX_OPCODE(X)                     |             \
  (rd                 << (7))           |             \
  (xs2                << (7+5))         |             \
  (xs1                << (7+5+1))       |             \
  (xd                 << (7+5+2))       |             \
  (rs1                << (7+5+3))       |             \
  (rs2                << (7+5+3+5))     |             \
  (EXTRACT(funct, 7, 0) << (7+5+3+5+5))

	.globl do_sbox
do_sbox:
  //      X, xd, xs1, xs2, rd, rs1, rs2, funct
  .word CUSTOMX(0,  1,   1,   0, 10,  10,   0,     0)
  ret
.align 2
trap_entry:
  call handle_trap

	.cfi_endproc

.section ".tohost","aw",@progbits
.align 6
.globl tohost
tohost: .dword 0
.align 6
.globl fromhost
fromhost: .dword 0
