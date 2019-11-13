/* Values taken from RFC 8032 */


/*
 * TEST 1
 *
 * private_key == 9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60
 *
 */

/* lower 256 bits of SHA512(private_key) */
#define ED25519_D_HASHED_LSB_1 \
	{0x0FE94D90, 0x06F020A5, 0xA3C080D9, 0x6827FFFD, \
	 0x3C010AC0, 0xF12E7A42, 0xCB33284F, 0x86837C35}

/* corresponding public key (reverse byte order, as-is from RFC) */
#define ED25519_Q_Y_1 \
	{0xd75a9801, 0x82b10ab7, 0xd54bfed3, 0xc964073a, \
	 0x0ee172f3, 0xdaa62325, 0xaf021a68, 0xf707511a}


/*
 * TEST 2
 *
 * private_key == 4ccd089b28ff96da9db6c346ec114e0f5b8a319f35aba624da8cf6ed4fb8a6fb
 *
 */

/* lower 256 bits of SHA512(private_key) */
#define ED25519_D_HASHED_LSB_2 \
	{0x112e502e, 0xb0249a25, 0x5e1c827f, 0x3b6b6c7f, \
	 0x0a79f4ca, 0x8575a915, 0x28d58258, 0xd79ebd6e}

/* corresponding public key (reverse byte order, as-is from RFC) */
#define ED25519_Q_Y_2 \
	{0x3d4017c3, 0xe843895a, 0x92b70aa7, 0x4d1b7ebc, \
	 0x9c982ccf, 0x2ec4968c, 0xc0cd55f1, 0x2af4660c}


/*
 * TEST 3
 *
 * private_key == c5aa8df43f9f837bedb7442f31dcb7b166d38535076f094b85ce3a2e0b4458f7
 *
 */

/* lower 256 bits of SHA512(private_key) */
#define ED25519_D_HASHED_LSB_3 \
	{0x9ca91e99, 0x81a12513, 0x1bf5c2c5, 0x4e7f4dba, \
	 0x113dc215, 0x5ba52390, 0x8402d95e, 0x758b9a90}

/* corresponding public key (reverse byte order, as-is from RFC) */
#define ED25519_Q_Y_3 \
	{0xfc51cd8e, 0x6218a1a3, 0x8da47ed0, 0x0230f058, \
	 0x0816ed13, 0xba3303ac, 0x5deb9115, 0x48908025}


/*
 * TEST 4
 *
 * private_key == f5e5767cf153319517630f226876b86c8160cc583bc013744c6bf255f5cc0ee5
 *
 */

/* lower 256 bits of SHA512(private_key) */
#define ED25519_D_HASHED_LSB_4 \
	{0xc8cc88f4, 0x4f786eb8, 0x6a0e2682, 0x9ca4b304, \
	 0xaa44b27f, 0xf2de6e4b, 0xd386f80e, 0x8d889c60}

/* corresponding public key (reverse byte order, as-is from RFC) */
#define ED25519_Q_Y_4 \
	{0x278117fc, 0x144c7234, 0x0f67d0f2, 0x316e8386, \
     0xceffbf2b, 0x2428c9c5, 0x1fef7c59, 0x7f1d426e}


/*
 * TEST 5
 *
 * private_key == 833fe62409237b9d62ec77587520911e9a759cec1d19755b7da901b96dca3d42
 *
 */

/* lower 256 bits of SHA512(private_key) */
#define ED25519_D_HASHED_LSB_5 \
	{0x85b64172, 0xc7528f1a, 0xf4a5a85d, 0xd6dbd872, \
	 0x92a0079b, 0xf113570b, 0xec4be059, 0x4fcedd30}

/* corresponding public key (reverse byte order, as-is from RFC) */
#define ED25519_Q_Y_5 \
	{0xec172b93, 0xad5e563b, 0xf4932c70, 0xe1245034, \
	 0xc35467ef, 0x2efd4d64, 0xebf81968, 0x3467e2bf}
