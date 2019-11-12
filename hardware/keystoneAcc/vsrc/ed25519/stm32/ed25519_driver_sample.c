//
// simple driver to test "ed25519" core in hardware
//

//
// note, that the test program needs a custom bitstream where
// the core is located at offset 0 (without the core selector)
//

// stm32 headers


#include "stm-init.h"
#include "stm-led.h"
#include "stm-fmc.h"

// locations of core registers
#define CORE_ADDR_NAME0			(0x00 << 2)
#define CORE_ADDR_NAME1			(0x01 << 2)
#define CORE_ADDR_VERSION		(0x02 << 2)
#define CORE_ADDR_CONTROL		(0x08 << 2)
#define CORE_ADDR_STATUS		(0x09 << 2)

// locations of data buffers
#define CORE_ADDR_BUF_K			(0x10 << 2)
#define CORE_ADDR_BUF_QY		(0x18 << 2)

// bit maps
#define CORE_CONTROL_BIT_NEXT		0x00000002
#define CORE_STATUS_BIT_READY		0x00000002

// 256 bits
#define OPERAND_WIDTH 256

#include "../../../../user/shatov/curve25519_fpga_model/vectors/ed25519/ed25519_test_vectors_rfc8032.h"
#include "../../../../user/shatov/curve25519_fpga_model/vectors/ed25519/ed25519_test_vector_randomized.h"

#define BUF_NUM_WORDS		(OPERAND_WIDTH / (sizeof(uint32_t) << 3))	// 8

inline uint32_t htonl(uint32_t w)
{
    return
        ((w & 0x000000ff) << 24) +
        ((w & 0x0000ff00) << 8) +
        ((w & 0x00ff0000) >> 8) +
        ((w & 0xff000000) >> 24);
}

//
// test vectors
//
static const uint32_t ed25519_d1[BUF_NUM_WORDS] = ED25519_D_HASHED_LSB_1;
static const uint32_t ed25519_d2[BUF_NUM_WORDS] = ED25519_D_HASHED_LSB_2;
static const uint32_t ed25519_d3[BUF_NUM_WORDS] = ED25519_D_HASHED_LSB_3;
static const uint32_t ed25519_d4[BUF_NUM_WORDS] = ED25519_D_HASHED_LSB_4;
static const uint32_t ed25519_d5[BUF_NUM_WORDS] = ED25519_D_HASHED_LSB_5;
static const uint32_t ed25519_d6[BUF_NUM_WORDS] = ED25519_D_HASHED_LSB_6;

static const uint32_t ed25519_qy1[BUF_NUM_WORDS] = ED25519_Q_Y_1;
static const uint32_t ed25519_qy2[BUF_NUM_WORDS] = ED25519_Q_Y_2;
static const uint32_t ed25519_qy3[BUF_NUM_WORDS] = ED25519_Q_Y_3;
static const uint32_t ed25519_qy4[BUF_NUM_WORDS] = ED25519_Q_Y_4;
static const uint32_t ed25519_qy5[BUF_NUM_WORDS] = ED25519_Q_Y_5;
static const uint32_t ed25519_qy6[BUF_NUM_WORDS] = ED25519_Q_Y_6;


//
// prototypes
//
void toggle_yellow_led(void);
int test_ed25519_multiplier(const uint32_t *k,
	const uint32_t *qy);

//
// test routine
//
int main()
{
  int ok;

  stm_init();

  led_on(LED_GREEN);
  led_off(LED_RED);

  led_off(LED_YELLOW);
  led_off(LED_BLUE);	
	
  uint32_t core_name0;
  uint32_t core_name1;

  fmc_read_32(CORE_ADDR_NAME0, &core_name0);
  fmc_read_32(CORE_ADDR_NAME1, &core_name1);

  // "ed25", "519 "
	
  if ((core_name0 != 0x65643235) || (core_name1 != 0x35313920)) {
    led_off(LED_GREEN);
    led_on(LED_RED);
    while (1);
  }


  // repeat forever
  while (1)
	{
		ok = 1;
		
		ok = ok && test_ed25519_multiplier(ed25519_d1, ed25519_qy1);
		ok = ok && test_ed25519_multiplier(ed25519_d2, ed25519_qy2);
		ok = ok && test_ed25519_multiplier(ed25519_d3, ed25519_qy3);
		ok = ok && test_ed25519_multiplier(ed25519_d4, ed25519_qy4);
		ok = ok && test_ed25519_multiplier(ed25519_d5, ed25519_qy5);
		ok = ok && test_ed25519_multiplier(ed25519_d6, ed25519_qy6);

			// check
		if (!ok) {
			led_off(LED_GREEN);
			led_on(LED_RED);
		}

		toggle_yellow_led();
	}
}


//
// this routine uses the hardware multiplier to obtain ty, which is the
// y-coordinate of the scalar multiple of the base point T = k * G,
// ty is then compared to the value qy (correct result known in advance)
//
int test_ed25519_multiplier(const uint32_t *k,
	const uint32_t *qy)
{
  int i, num_cyc;
  uint32_t reg_control, reg_status;
  uint32_t k_word, ty_word;

  // fill k
  for (i=0; i<BUF_NUM_WORDS; i++) {
    k_word = k[i];
    fmc_write_32(CORE_ADDR_BUF_K + ((BUF_NUM_WORDS - (i + 1)) * sizeof(uint32_t)), k_word);
  }
	
	// as a sanity check, make sure that we can't readout the private
	// key we've just filled in
	
	for (i=0; i<BUF_NUM_WORDS; i++) {
		fmc_read_32(CORE_ADDR_BUF_K + ((BUF_NUM_WORDS - (i + 1)) * sizeof(uint32_t)), &k_word);
		if (k_word != 0xDEADCE11) return 0;
  }

  // clear 'next' control bit, then set 'next' control bit again to trigger new operation
  reg_control = 0;
  fmc_write_32(CORE_ADDR_CONTROL, reg_control);
  reg_control = CORE_CONTROL_BIT_NEXT;
  fmc_write_32(CORE_ADDR_CONTROL, reg_control);

  // wait for 'ready' status bit to be set
  num_cyc = 0;
  do {
    num_cyc++;
    fmc_read_32(CORE_ADDR_STATUS, &reg_status);
  }
  while (!(reg_status & CORE_STATUS_BIT_READY));

  // read back qy word-by-word, then compare to the reference value
  for (i=0; i<BUF_NUM_WORDS; i++) {
    fmc_read_32(CORE_ADDR_BUF_QY + (i * sizeof(uint32_t)), &ty_word);

		// match the byte order used in RFC test vectors
		ty_word = htonl(ty_word);
		
		// compare
    if ((ty_word != qy[i]))
			return 0;
  }

  // everything went just fine
  return 1;
}

//
// toggle the yellow led to indicate that we're not stuck somewhere
//
void toggle_yellow_led(void)
{
  static int led_state = 0;

  led_state = !led_state;

  if (led_state) led_on(LED_YELLOW);
  else           led_off(LED_YELLOW);
}


void SysTick_Handler(void)
{
    HAL_IncTick();
    HAL_SYSTICK_IRQHandler();
}


//
// end of file
//
