// See LICENSE for license details.

#ifndef _ED25519_DEV_H
#define _ED25519_DEV_H

/* Register offsets */
#define ED25519_REG_DATA_K        0x00
#define ED25519_REG_DATA_K2       0x20
#define ED25519_REG_DATA_QY       0x40
#define ED25519_REG_DATA_A        0x60
#define ED25519_REG_DATA_B        0x80
#define ED25519_REG_DATA_C        0xC0
#define ED25519_REG_DATA_HKEY     0x100
#define ED25519_REG_DATA_HRAM     0x140
#define ED25519_REG_DATA_HSM      0x180
#define ED25519_REG_DATA_SIGN     0x1C0
#define ED25519_REG_STATUS_3      0xFF4
#define ED25519_REG_STATUS_2      0xFF8
#define ED25519_REG_STATUS        0xFFC

#ifndef ED25519_DIR

#define ED25519_REG_ADDR_K        0x04
#define ED25519_REG_ADDR_K2       0x24
#define ED25519_REG_ADDR_QY       0x44

#endif

#endif /* _ED25519_DEV_H */
