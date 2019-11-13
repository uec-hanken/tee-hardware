#include "ed25519.h"
#include "hwsha3.h"
#include "ge.h"


void ed25519_create_keypair(unsigned char *public_key, unsigned char *private_key, const unsigned char *seed) {
    ge_p3 A;

    hwsha3_init();
    hwsha3_final(private_key, seed, 32);
    private_key[0] &= 248;
    private_key[31] &= 63;
    private_key[31] |= 64;

    ge_scalarmult_base(&A, private_key);
    ge_p3_tobytes(public_key, &A);
}

#include "platform.h"

void hw_ed25519_create_keypair(unsigned char *public_key, unsigned char *private_key, const unsigned char *seed) {
    uint64_t *k = (uint64_t*)seed;
    uint32_t *pub = (uint32_t*)public_key;
    uint32_t *priv = (uint32_t*)private_key;

    SHA3_REG(SHA3_REG_STATUS) = 1 << 24; // Reset, and also put 0 in size
    SHA3_REG64(SHA3_REG_DATA_0) = *k; // 8 bytes
    SHA3_REG(SHA3_REG_STATUS) = 1 << 16;
    k++;
    SHA3_REG64(SHA3_REG_DATA_0) = *k; // 16 bytes
    SHA3_REG(SHA3_REG_STATUS) = 1 << 16;
    k++;
    SHA3_REG64(SHA3_REG_DATA_0) = *k; // 24 bytes
    SHA3_REG(SHA3_REG_STATUS) = 1 << 16;
    k++;
    SHA3_REG64(SHA3_REG_DATA_0) = *k; // 32 bytes
    SHA3_REG(SHA3_REG_STATUS) = 1 << 16;
    SHA3_REG(SHA3_REG_STATUS) = 3 << 16;
    while(SHA3_REG(SHA3_REG_STATUS) & (1 << 10)); // Wait for SHA3
    for(int i = 0; i < 8; i++) {
        ED25519_REG(ED25519_REG_ADDR_K) = i;
        if(i == 0) // TODO: This is really necessary? 
            ED25519_REG(ED25519_REG_DATA_K) = *(priv+i) = *(((uint32_t*)(SHA3_CTRL_ADDR+SHA3_REG_HASH_0)) + i) & 0xFFFFFFF8;
        else if(i == 7)
            ED25519_REG(ED25519_REG_DATA_K) = *(priv+i) = *(((uint32_t*)(SHA3_CTRL_ADDR+SHA3_REG_HASH_0)) + i) & 0x3FFFFFFF | 0x40000000;
        else
            ED25519_REG(ED25519_REG_DATA_K) = *(priv+i) = *(((uint32_t*)(SHA3_CTRL_ADDR+SHA3_REG_HASH_0)) + i);
    }
    for(int i = 8; i < 16; i++) {
        *(priv+i) = *(((uint32_t*)(SHA3_CTRL_ADDR+SHA3_REG_HASH_0)) + i);
    }
    ED25519_REG(ED25519_REG_STATUS) = 1; // Use the K memory
    while(!(ED25519_REG(ED25519_REG_STATUS) & 0x4)); // Wait
    for(int i = 0; i < 8; i++) {
        ED25519_REG(ED25519_REG_ADDR_QY) = i;
        pub[i] = ED25519_REG(ED25519_REG_DATA_QY);
    }
}
