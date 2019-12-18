// See LICENSE for license details.

#ifndef _RANDOM_DEV_H
#define _RANDOM_DEV_H

/* Register offsets */
#define RANDOM_RNG_IMR                0x100
#define RANDOM_RNG_ISR                0x104
#define RANDOM_RNG_ICR                0x108
#define RANDOM_TRNG_CONFIG            0x10C
#define RANDOM_TRNG_VALID             0x110
#define RANDOM_EHR_DATA0              0x114
#define RANDOM_EHR_DATA1              0x118
#define RANDOM_EHR_DATA2              0x11C
#define RANDOM_EHR_DATA3              0x120
#define RANDOM_EHR_DATA4              0x124
#define RANDOM_EHR_DATA5              0x128
#define RANDOM_RND_SOURCE_ENABLE      0x12C
#define RANDOM_SAMPLE_CNT1            0x130
#define RANDOM_AUTOCORR_STATISTIC     0x134
#define RANDOM_TRNG_DEBUG_CONTROL     0x138
#define RANDOM_TRNG_SW_RESET          0x140
#define RANDOM_TRNG_BUSY              0x1B8
#define RANDOM_RST_BITS_COUNTER       0x1BC
#define RANDOM_RNG_BIST_CNTR0         0x1E0
#define RANDOM_RNG_BIST_CNTR1         0x1E4
#define RANDOM_RNG_BIST_CNTR2         0x1E8

#endif /* _RANDOM_DEV_H */
