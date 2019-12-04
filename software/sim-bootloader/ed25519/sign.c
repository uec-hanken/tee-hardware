#include "ed25519.h"
//#include "sha3/sha3.h"
#include "hwsha3.h"
#include "ge.h"
#include "sc.h"


void ed25519_sign(unsigned char *signature, const unsigned char *message, size_t message_len, const unsigned char *public_key, const unsigned char *private_key) {
    //sha3_ctx_t hash;
    unsigned char hram[64];
    unsigned char r[64];
    ge_p3 R;


    hwsha3_init();
    hwsha3_update(private_key + 32, 32);
    hwsha3_final(r, message, message_len);

    sc_reduce(r);
    ge_scalarmult_base(&R, r);
    ge_p3_tobytes(signature, &R);

    hwsha3_init();
    hwsha3_update(signature, 32);
    hwsha3_update(public_key, 32);
    hwsha3_final(hram, message, message_len);

    sc_reduce(hram);
    sc_muladd(signature + 32, hram, private_key, r);
}

#include "platform.h"

void hw_ed25519_sign(unsigned char *signature, const unsigned char *message, size_t message_len, const unsigned char *public_key, const unsigned char *private_key, char red) {
    unsigned char hram[64];
    unsigned char r[64];
    unsigned char rred[64];
    uint32_t *sig = (uint32_t*)signature;
    
    // Remember that private_key is already hashed (hashkey)

    // Hash S and M (hashsm)
    hwsha3_init();
    hwsha3_update(private_key + 32, 32);
    hwsha3_final(r, message, message_len);
    for(int i = 0; i < 16; i++) {
        *(((uint32_t*)(rred)) + i) = *(((uint32_t*)(r)) + i);
    }
    if(red) sc_reduce(rred);
    
    // Calculate the R part with a base-point multiplier (in hw)
#ifndef ED25519_DIR
    for(int i = 0; i < 8; i++) {
        ED25519_REG(ED25519_REG_ADDR_K) = i;
        ED25519_REG(ED25519_REG_DATA_K) = *(((uint32_t*)(rred)) + i);
    }
    ED25519_REG(ED25519_REG_STATUS) = 1; // Use the K memory
    while(!(ED25519_REG(ED25519_REG_STATUS) & 0x4)); // Wait
    for(int i = 0; i < 8; i++) {
        ED25519_REG(ED25519_REG_ADDR_QY) = i;
        sig[i] = ED25519_REG(ED25519_REG_DATA_QY);
    }
#else
    for(int i = 0; i < 8; i++) {
        ED25519_REG(ED25519_REG_DATA_K + i*4) = *(((uint32_t*)(r)) + i);
    }
    ED25519_REG(ED25519_REG_STATUS) = 1; // Use the K memory
    while(!(ED25519_REG(ED25519_REG_STATUS) & 0x4)); // Wait
    for(int i = 0; i < 8; i++) {
        sig[i] = ED25519_REG(ED25519_REG_DATA_QY + i*4);
    }
#endif

    // Calculate the H(R, A, M)
    hwsha3_init();
    hwsha3_update(signature, 32);
    hwsha3_update(public_key, 32);
    hwsha3_final(hram, message, message_len);
    
    // Calculate the S part with the addmult in hw
    for(int i = 0; i < 16; i++) {
        ED25519_REG(ED25519_REG_DATA_HKEY + i*4) = *(((uint32_t*)(private_key)) + i);
    }
    for(int i = 0; i < 16; i++) {
        ED25519_REG(ED25519_REG_DATA_HRAM + i*4) = *(((uint32_t*)(hram)) + i);
    }
    for(int i = 0; i < 16; i++) {
        ED25519_REG(ED25519_REG_DATA_HSM + i*4) = *(((uint32_t*)(r)) + i);
    }
    ED25519_REG(ED25519_REG_STATUS_3) = 1;
    while(!(ED25519_REG(ED25519_REG_STATUS_3) & 0x4)); // Wait
    for(int i = 0; i < 8; i++) {
        sig[i+8] = ED25519_REG(ED25519_REG_DATA_SIGN + i*4);
    }
    
}

