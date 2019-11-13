#######################################
# Makefile for simulated bootloader
#######################################

CC = riscv64-unknown-elf-gcc
LD = riscv64-unknown-elf-gcc
MAKE = make
OBJDUMP = riscv64-unknown-elf-objdump
OBJCOPY = riscv64-unknown-elf-objcopy

exe = sim-bootloader.elf
OBJS = start.o main.o memcpy.o strlen.o sha3/sha3.o ed25519/fe.o ed25519/ge.o ed25519/keypair.o ed25519/sc.o ed25519/sign.o ed25519/verify.o
INCLUDE = -I. -I./sha3 -I./ed25519
CFLAGS = -march=rv64imafdc -mabi=lp64d -mcmodel=medany
LDFLAGS = -march=rv64imafdc -mabi=lp64d -Os -nostartfiles -nostdlib -Wl,-Bstatic,-T,./ram.lds,-Map,$(addsuffix .map, $(basename $(exe))),--strip-debug

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

