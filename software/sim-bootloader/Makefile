#######################################
# Makefile for simulated bootloader
#######################################

CC = riscv64-unknown-elf-gcc
LD = riscv64-unknown-elf-gcc
MAKE = make
OBJDUMP = riscv64-unknown-elf-objdump
OBJCOPY = riscv64-unknown-elf-objcopy

ifeq ($(TEEHW),)
CFLAGS_ARCH=-march=rv64imafdc -mabi=lp64d
else ifeq ($(ISACONF),RV64GC)
CFLAGS_ARCH=-march=rv64imafdc -mabi=lp64d
else ifeq ($(ISACONF),RV64IMAC)
CFLAGS_ARCH=-march=rv64imac -mabi=lp64
else ifeq ($(ISACONF),RV32GC)
CFLAGS_ARCH=-march=rv32imafdc -mabi=ilp32d
else #RV32IMAC
CFLAGS_ARCH=-march=rv32imac -mabi=ilp32
endif

exe = sim-bootloader.elf
OBJS = start.o main.o memcpy.o strlen.o tiny-AES-c/aes.o sha3/sha3.o ed25519/fe.o ed25519/ge.o ed25519/keypair.o ed25519/sc.o ed25519/sign.o ed25519/verify.o uart/uart.o
INCLUDE = -I. -I./tiny-AES-c -I./sha3 -I./ed25519 -I../../hardware/chipyard/tests
CFLAGS = $(CFLAGS_ARCH) -mcmodel=medany
LDFLAGS = $(CFLAGS_ARCH) -Os -nostartfiles -nostdlib -Wl,-Bstatic,-T,./ram.lds,-Map,$(addsuffix .map, $(basename $(exe))),--strip-debug

all: $(exe)

$(exe): $(OBJS)
	$(LD) $(OBJS) -o $(exe) $(LDFLAGS)
	$(OBJDUMP) -D $(exe) > $(addsuffix .dump, $(basename $(exe)))
	$(OBJDUMP) -D $(exe) -M no-aliases,numeric > $(addsuffix .noaliases.dump, $(basename $(exe)))
	$(OBJCOPY) -O binary $(exe) $(addsuffix .bin, $(basename $(exe)))

%.o: %.S
	$(CC) $(INCLUDE) -c $< -o $@ $(CFLAGS)

%.o: %.c
	$(CC) $(INCLUDE) -c $< -o $@ $(CFLAGS)

clean:
	rm -rfv sha3/*.o ed25519/*.o *.o $(exe) *.dump *.bin *.hex *.map

