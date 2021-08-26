SECTIONS
{
    ROM_BASE = 0x10000; /* ... but actually position independent */

    . = ROM_BASE;
    .text.start : { *(.text.start) }
    . = ROM_BASE + 0x40;
    .text.hang : { *(.text.hang) }
    . = ROM_BASE + 0x80; /* ... This will help with the boot in the Ibex */
    .text.ibexboot : { *(.text.ibexboot) }
    . = ALIGN(8);
    .rodata : { *(.rodata) }
}
