#ifndef HWSHA3
#define HWSHA3

typedef unsigned char byte;

void hwsha3_init() ;
void hwsha3_update(const unsigned char* data, size_t size) ;
void hwsha3_final(byte* hash, const unsigned char* data, size_t size);

#endif
