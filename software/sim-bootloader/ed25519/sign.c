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
