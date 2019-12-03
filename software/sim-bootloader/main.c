#include <string.h>
#include "sha3.h"
#include "platform.h"
#include "ed25519.h"
#include "ge.h"
#include "ed25519_test_vectors_rfc8032.h"
#include "encoding.h"

typedef unsigned char byte;

// HTIF stuff

extern volatile uint64_t tohost;
extern volatile uint64_t fromhost;

void __attribute__((noreturn)) tohost_exit(uintptr_t code)
{
  tohost = (code << 1) | 1;
  while (1);
}

static uintptr_t syscall(uintptr_t which, uint64_t arg0, uint64_t arg1, uint64_t arg2)
{
  volatile uint64_t magic_mem[8] __attribute__((aligned(64)));
  magic_mem[0] = which;
  magic_mem[1] = arg0;
  magic_mem[2] = arg1;
  magic_mem[3] = arg2;
  __sync_synchronize();

  tohost = (uintptr_t)magic_mem;
  while (fromhost == 0)
    ;
  fromhost = 0;

  __sync_synchronize();
  return magic_mem[0];
}

#define SYS_write 64
void printstr(const char* s)
{
  syscall(SYS_write, 1, (uintptr_t)s, strlen(s));
}

#undef putchar
int putchar(int ch)
{
  static __thread char buf[64] __attribute__((aligned(64)));
  static __thread int buflen = 0;

  buf[buflen++] = ch;

  if (ch == '\n' || buflen == sizeof(buf))
  {
    syscall(SYS_write, 1, (uintptr_t)buf, buflen);
    buflen = 0;
  }

  return 0;
}

void printhex(uint64_t x)
{
  char str[17];
  int i;
  for (i = 0; i < 16; i++)
  {
    str[15-i] = (x & 0xF) + ((x & 0xF) < 10 ? '0' : 'a'-10);
    x >>= 4;
  }
  str[16] = 0;

  printstr(str);
}

void printhex32(uint32_t x)
{
  char str[9];
  int i;
  for (i = 0; i < 8; i++)
  {
    str[7-i] = (x & 0xF) + ((x & 0xF) < 10 ? '0' : 'a'-10);
    x >>= 4;
  }
  str[8] = 0;

  printstr(str);
}

// HW SHA-3 stuff

void hwsha3_init() {
  SHA3_REG(SHA3_REG_STATUS) = 1 << 24; // Reset, and also put 0 in size
}

void hwsha3_update(void* data, size_t size) {
  uint64_t* d = (uint64_t*)data;
  SHA3_REG(SHA3_REG_STATUS) = 0;
  while(size >= 8) {
    SHA3_REG64(SHA3_REG_DATA_0) = *d;
    SHA3_REG(SHA3_REG_STATUS) = 1 << 16;
    size -= 8;
    d += 1;
  }
  if(size > 0) {
    SHA3_REG64(SHA3_REG_DATA_0) = *d;
    SHA3_REG(SHA3_REG_STATUS) = size & 0x7;
    SHA3_REG(SHA3_REG_STATUS) = 1 << 16;
  }
}

void hwsha3_final(byte* hash, void* data, size_t size) {
  uint64_t* d = (uint64_t*)data;
  SHA3_REG(SHA3_REG_STATUS) = 0;
  while(size >= 8) {
    size -= 8;
    SHA3_REG64(SHA3_REG_DATA_0) = *d;
    SHA3_REG(SHA3_REG_STATUS) = 1 << 16;
    d += 1;
  }
  /*if(size > 0)*/ {
    if(size > 0) SHA3_REG64(SHA3_REG_DATA_0) = *d;
    SHA3_REG(SHA3_REG_STATUS) = size & 0x7;
    SHA3_REG(SHA3_REG_STATUS) = 3 << 16;
  }
  while(SHA3_REG(SHA3_REG_STATUS) & (1 << 10));
  for(int i = 0; i < 8; i++) {
    *(((uint64_t*)hash) + i) = *(((uint64_t*)(SHA3_CTRL_ADDR+SHA3_REG_HASH_0)) + i);
  }
}

// Trap handler
void handle_trap(void) {
  printstr("Trap: ");
  printhex32(read_csr(mcause));
  tohost_exit(0);
}

// Declaration of the sbox program
uint64_t do_sbox(uint64_t a);

// Main program

int main(int argc, char** argv) {
  printstr("Hello world, FSBL\r\n");
  
  // Do the SBOX acc
  uint64_t k = do_sbox((uint64_t) 0xdeadbeef);
  printstr("SBOX of 0xdeadbeef: ");
  printhex32(k);
  printstr("\r\n");
  
  // Test the hardware with the software SHA3
  byte hash[64];
  uint32_t* hs = (uint32_t*)hash;
  /*sha3_init(&hash_ctx, 64);
  sha3_update(&hash_ctx, (void*)"FOX1FOX2", 8);
  sha3_update(&hash_ctx, (void*)"FOX3FOX4", 8);
  sha3_final(hash, &hash_ctx);
  for(int i = 0; i < 16; i++) 
     printhex32(*(hs+i));
  printstr("\r\n");*/
    
  hwsha3_init();
  hwsha3_update((void*)"FOX1FOX2", 8);
  hwsha3_final(hash, (void*)"FOX3FOX4", 8);
  printstr("Seed:\r\n");
  for(int i = 0; i < 16; i++) 
      printhex32(*(hs+i));
  
  unsigned char *seed = (unsigned char*) hash;
  
  unsigned int key[16];
  unsigned char *private_key = (unsigned char*) key;
  
  unsigned int key_2[16];
  unsigned char *private_key_2 = (unsigned char*) key_2;
  
  unsigned int pub1[8];
  unsigned char *public_key_1 = (unsigned char*) pub1;
  
  unsigned int pub2[8];
  unsigned char *public_key_2 = (unsigned char*) pub2;
  
  unsigned int sign1[16];
  unsigned char *signature_1 = (unsigned char*) sign1;
  
  unsigned int sign2[16];
  unsigned char *signature_2 = (unsigned char*) sign2;
  
  // Software keypair
  unsigned long start_mcycle = read_csr(mcycle);
  //ed25519_create_keypair(public_key_1, private_key, seed);
  unsigned long delta_mcycle = read_csr(mcycle) - start_mcycle;
  /*printstr("\r\nSoftware public key\r\n");
  for(int i = 0; i < 8; i++) 
    printhex32(*(pub1+i));
  printstr("\r\nSoftware private key\r\n");
  for(int i = 0; i < 16; i++) 
    printhex32(*(key+i));
  printstr("\r\nTime calculation: ");
  printhex32(delta_mcycle);*/
  
  // Hardware keypair
  start_mcycle = read_csr(mcycle);
  hw_ed25519_create_keypair(public_key_2, private_key_2, seed);
  delta_mcycle = read_csr(mcycle) - start_mcycle;
  printstr("\r\nHardware public key\r\n");
  for(int i = 0; i < 8; i++) 
    printhex32(*(pub2+i));
  printstr("\r\nHardware private key\r\n");
  for(int i = 0; i < 16; i++) 
    printhex32(*(key_2+i));
  printstr("\r\nTime calculation: ");
  printhex32(delta_mcycle);
  
  // Software sign
  /*start_mcycle = read_csr(mcycle);
  ed25519_sign(signature_1, "hello", 5, public_key_1, private_key);
  delta_mcycle = read_csr(mcycle) - start_mcycle;
  printstr("\r\nSoftware signature\r\n");
  for(int i = 0; i < 16; i++) 
    printhex32(*(sign1+i));
  printstr("\r\nTime calculation: ");
  printhex32(delta_mcycle);*/
  
  // Hardware sign
  start_mcycle = read_csr(mcycle);
  hw_ed25519_sign(signature_2, "hello", 5, public_key_2, private_key_2, 0);
  delta_mcycle = read_csr(mcycle) - start_mcycle;
  printstr("\r\nHardware signature (not reduced)\r\n");
  for(int i = 0; i < 16; i++) 
    printhex32(*(sign2+i));
  printstr("\r\nTime calculation: ");
  printhex32(delta_mcycle);
  
  start_mcycle = read_csr(mcycle);
  hw_ed25519_sign(signature_2, "hello", 5, public_key_2, private_key_2, 1);
  delta_mcycle = read_csr(mcycle) - start_mcycle;
  printstr("\r\nHardware signature (reduced)\r\n");
  for(int i = 0; i < 16; i++) 
    printhex32(*(sign2+i));
  printstr("\r\nTime calculation: ");
  printhex32(delta_mcycle);
  
  // f0ba04da24b52e7f682a1bc048ff68fc6725ac216a0b83523ccdb94855226af32ab8f448f958200aeec5f55cfca6427ea1aa81627059cf08544d1309f727ce59
  ED25519_REG(ED25519_REG_DATA_HKEY + 0*4) = 0xf727ce59;
  ED25519_REG(ED25519_REG_DATA_HKEY + 1*4) = 0x544d1309;
  ED25519_REG(ED25519_REG_DATA_HKEY + 2*4) = 0x7059cf08;
  ED25519_REG(ED25519_REG_DATA_HKEY + 3*4) = 0xa1aa8162;
  ED25519_REG(ED25519_REG_DATA_HKEY + 4*4) = 0xfca6427e;
  ED25519_REG(ED25519_REG_DATA_HKEY + 5*4) = 0xeec5f55c;
  ED25519_REG(ED25519_REG_DATA_HKEY + 6*4) = 0xf958200a;
  ED25519_REG(ED25519_REG_DATA_HKEY + 7*4) = 0x2ab8f448;
  ED25519_REG(ED25519_REG_DATA_HKEY + 8*4) = 0x55226af3;
  ED25519_REG(ED25519_REG_DATA_HKEY + 9*4) = 0x3ccdb948;
  ED25519_REG(ED25519_REG_DATA_HKEY + 10*4) = 0x6a0b8352;
  ED25519_REG(ED25519_REG_DATA_HKEY + 11*4) = 0x6725ac21;
  ED25519_REG(ED25519_REG_DATA_HKEY + 12*4) = 0x48ff68fc;
  ED25519_REG(ED25519_REG_DATA_HKEY + 13*4) = 0x682a1bc0;
  ED25519_REG(ED25519_REG_DATA_HKEY + 14*4) = 0x24b52e7f;
  ED25519_REG(ED25519_REG_DATA_HKEY + 15*4) = 0xf0ba04da;
  // 7d0d7f78d7b240d4ec9b6007572ed94c4d3c25a6b9cd2e2bcb12bbfc9a6438743be7f165835fb15f8acc92fd69476b4e41cb71ac88a03d90ed5e4492e208dfaf
  ED25519_REG(ED25519_REG_DATA_HRAM + 0*4) = 0xe208dfaf;
  ED25519_REG(ED25519_REG_DATA_HRAM + 1*4) = 0xed5e4492;
  ED25519_REG(ED25519_REG_DATA_HRAM + 2*4) = 0x88a03d90;
  ED25519_REG(ED25519_REG_DATA_HRAM + 3*4) = 0x41cb71ac;
  ED25519_REG(ED25519_REG_DATA_HRAM + 4*4) = 0x69476b4e;
  ED25519_REG(ED25519_REG_DATA_HRAM + 5*4) = 0x8acc92fd;
  ED25519_REG(ED25519_REG_DATA_HRAM + 6*4) = 0x835fb15f;
  ED25519_REG(ED25519_REG_DATA_HRAM + 7*4) = 0x3be7f165;
  ED25519_REG(ED25519_REG_DATA_HRAM + 8*4) = 0x9a643874;
  ED25519_REG(ED25519_REG_DATA_HRAM + 9*4) = 0xcb12bbfc;
  ED25519_REG(ED25519_REG_DATA_HRAM + 10*4) = 0xb9cd2e2b;
  ED25519_REG(ED25519_REG_DATA_HRAM + 11*4) = 0x4d3c25a6;
  ED25519_REG(ED25519_REG_DATA_HRAM + 12*4) = 0x572ed94c;
  ED25519_REG(ED25519_REG_DATA_HRAM + 13*4) = 0xec9b6007;
  ED25519_REG(ED25519_REG_DATA_HRAM + 14*4) = 0xd7b240d4;
  ED25519_REG(ED25519_REG_DATA_HRAM + 15*4) = 0x7d0d7f78;
  // 3e8dff2fd83ca76077db690ede6e6e446b1f01a247540764b203f0283dbe0597edb40c8f09cbacdc94640d6ac8590f2e07cc2d4180bac6b2adf132b88cd95a04
  ED25519_REG(ED25519_REG_DATA_HSM + 0*4) = 0x8cd95a04;
  ED25519_REG(ED25519_REG_DATA_HSM + 1*4) = 0xadf132b8;
  ED25519_REG(ED25519_REG_DATA_HSM + 2*4) = 0x80bac6b2;
  ED25519_REG(ED25519_REG_DATA_HSM + 3*4) = 0x07cc2d41;
  ED25519_REG(ED25519_REG_DATA_HSM + 4*4) = 0xc8590f2e;
  ED25519_REG(ED25519_REG_DATA_HSM + 5*4) = 0x94640d6a;
  ED25519_REG(ED25519_REG_DATA_HSM + 6*4) = 0x09cbacdc;
  ED25519_REG(ED25519_REG_DATA_HSM + 7*4) = 0xedb40c8f;
  ED25519_REG(ED25519_REG_DATA_HSM + 8*4) = 0x3dbe0597;
  ED25519_REG(ED25519_REG_DATA_HSM + 9*4) = 0xb203f028;
  ED25519_REG(ED25519_REG_DATA_HSM + 10*4) = 0x47540764;
  ED25519_REG(ED25519_REG_DATA_HSM + 11*4) = 0x6b1f01a2;
  ED25519_REG(ED25519_REG_DATA_HSM + 12*4) = 0xde6e6e44;
  ED25519_REG(ED25519_REG_DATA_HSM + 13*4) = 0x77db690e;
  ED25519_REG(ED25519_REG_DATA_HSM + 14*4) = 0xd83ca760;
  ED25519_REG(ED25519_REG_DATA_HSM + 15*4) = 0x3e8dff2f;
  ED25519_REG(ED25519_REG_STATUS_3) = 1;
  while(!(ED25519_REG(ED25519_REG_STATUS_3) & 0x2)); // Wait
  printstr("\r\n4e9dc2d496dcd2c1f5b6d79e6a06591f7189a59540dca01304506dfbb6c61f0d\r\n");
  for(int i = 0; i < 8; i++) {
    printhex32(ED25519_REG(ED25519_REG_DATA_SIGN + i*4));
  }
  
  
  printstr("\r\n");
  
  tohost_exit(0);
  return 0;
}

