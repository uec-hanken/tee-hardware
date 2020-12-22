// Jump directly to XIP_TARGET_ADDR

#ifndef XIP_TARGET_ADDR
#define XIP_TARGET_ADDR 0x20000000
#endif

#ifndef XIP_IMPL
#define XIP_IMPL 0
#endif

.section .text.start, "ax", @progbits
.globl _start
_start:
  j _start
// boot all cores (only hart 0) and jump to main program execution
  li a1, 0x2000000 // base address of clint
  csrr a0, mhartid
  bnez a0, boot_core // If mhartid is not 0, directly go to boot_core
  addi a2, a1, 1 << 2 // Starts with interrupting hart #1
  addi a4, a1, NUM_CORES << 2 // Ends with hart #NUM_CORES
  li a3, 1
interrupt_loop: // trigger MSIP interrupts for other harts so that they also boot
  sw a3, 0(a2)
  addi a2, a2, 4
  blt a2, a4, interrupt_loop // If current hart interrupt is less that total ones, do the next one
  j boot_core_hart0

// this boot ROM doesn't know about any boot devices, so it just spins,
// waiting for the serial interface to load the program and interrupt it
.section .text.hang, "ax", @progbits
.globl _hang_jmp
_hang_jmp: // Other-system reset vector
  j _hang_jmp

.section .text.ibexboot, "ax", @progbits
.globl _hang
_hang: // reset vector
  csrr a0, mhartid
  li a1, NUM_CORES
1:bge a0, a1, 1b     // If the hart ID is greater or equals the NUMCORES, that means is a ghost
  la a0, _start      // on MSIP interrupt, go to _start
  csrw mtvec, a0
  //csrwi 0x7c1, 0x8   // Disables the Out-of-Order execution of boom
  li a0, 8           // MIE or MSIP bit
  csrw mie, a0       // set only MSIP in MIE CSR
  csrs mstatus, a0   // set MIE in mstatus CSR
#ifdef SKIP_HANG
  csrr a0, mhartid
  bnez a0, wfi_loop
  li a1, 0x2000000 // base address of clint
  li a3, 1         // Write 1 to the MSIP of the hart 0
  sw a3, 0(a1)
#endif
wfi_loop: // wait for MSIP interrupt to start program execution
  wfi
  j wfi_loop

// other harts must wait until MSIP of hart 0 is cleared to continue
boot_core:
  lw t0, 0(a1)       // load hart 0 MSIP
  bnez t0, boot_core // block until hart 0 clears own MSIP
  sll a0, a0, 2      // offset for hart MSIP
  add a1, a0, a1
boot_core_hart0:   // begin executing code at XIP_TARGET_ADDR
  sw zero, 0(a1)   // clear the interrupt
  li a0, XIP_TARGET_ADDR // program reset vector
  csrw mepc, a0    // return from interrupt to start of user program
  csrr a0, mhartid // hartid for next level bootloader
  la a1, _dtb      // dtb address for next level bootloader
  li a2, 0x80      // set mstatus MPIE to 0
  csrc mstatus, a2
#if XIP_IMPL==0
  mret
#else
  li t0, XIP_TARGET_ADDR
  jr t0
#endif

_dtb:
//  .incbin DEVICE_TREE
