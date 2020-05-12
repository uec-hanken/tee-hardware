/* Values taken from RFC 8032 */


/*
 * TEST 1
 *
 * private_key == 9d61b19deffd5a60ba844af492ec2cc44449c5697b326919703bac031cae7f60
 *
 */

/* lower 256 bits of SHA512(private_key) */
localparam [255:0] ED25519_D_HASHED_LSB_1 =
    {32'h0FE94D90, 32'h06F020A5, 32'hA3C080D9, 32'h6827FFFD,
     32'h3C010AC0, 32'hF12E7A42, 32'hCB33284F, 32'h86837C35};

/* corresponding public key (reverse byte order, as-is from RFC) */
localparam [255:0] ED25519_Q_Y_1 =
    {32'hd75a9801, 32'h82b10ab7, 32'hd54bfed3, 32'hc964073a,
     32'h0ee172f3, 32'hdaa62325, 32'haf021a68, 32'hf707511a};


/*
 * TEST 2
 *
 * private_key == 4ccd089b28ff96da9db6c346ec114e0f5b8a319f35aba624da8cf6ed4fb8a6fb
 *
 */

/* lower 256 bits of SHA512(private_key) */
localparam [255:0] ED25519_D_HASHED_LSB_2 =
    {32'h112e502e, 32'hb0249a25, 32'h5e1c827f, 32'h3b6b6c7f,
     32'h0a79f4ca, 32'h8575a915, 32'h28d58258, 32'hd79ebd6e};

/* corresponding public key (reverse byte order, as-is from RFC) */
localparam [255:0] ED25519_Q_Y_2 =
    {32'h3d4017c3, 32'he843895a, 32'h92b70aa7, 32'h4d1b7ebc,
     32'h9c982ccf, 32'h2ec4968c, 32'hc0cd55f1, 32'h2af4660c};


/*
 * TEST 3
 *
 * private_key == c5aa8df43f9f837bedb7442f31dcb7b166d38535076f094b85ce3a2e0b4458f7
 *
 */

/* lower 256 bits of SHA512(private_key) */
localparam [255:0] ED25519_D_HASHED_LSB_3 =
    {32'h9ca91e99, 32'h81a12513, 32'h1bf5c2c5, 32'h4e7f4dba,
     32'h113dc215, 32'h5ba52390, 32'h8402d95e, 32'h758b9a90};

/* corresponding public key (reverse byte order, as-is from RFC) */
localparam [255:0] ED25519_Q_Y_3 =
    {32'hfc51cd8e, 32'h6218a1a3, 32'h8da47ed0, 32'h0230f058,
     32'h0816ed13, 32'hba3303ac, 32'h5deb9115, 32'h48908025};


/*
 * TEST 4
 *
 * private_key == f5e5767cf153319517630f226876b86c8160cc583bc013744c6bf255f5cc0ee5
 *
 */

/* lower 256 bits of SHA512(private_key) */
localparam [255:0] ED25519_D_HASHED_LSB_4 =
    {32'hc8cc88f4, 32'h4f786eb8, 32'h6a0e2682, 32'h9ca4b304,
     32'haa44b27f, 32'hf2de6e4b, 32'hd386f80e, 32'h8d889c60};

/* corresponding public key (reverse byte order, as-is from RFC) */
localparam [255:0] ED25519_Q_Y_4 =
    {32'h278117fc, 32'h144c7234, 32'h0f67d0f2, 32'h316e8386,
     32'hceffbf2b, 32'h2428c9c5, 32'h1fef7c59, 32'h7f1d426e};


/*
 * TEST 5
 *
 * private_key == 
 *
 */

/* lower 256 bits of SHA512(private_key) */
localparam [255:0] ED25519_D_HASHED_LSB_5 =
    {32'h85b64172, 32'hc7528f1a, 32'hf4a5a85d, 32'hd6dbd872,
     32'h92a0079b, 32'hf113570b, 32'hec4be059, 32'h4fcedd30};

/* corresponding public key (reverse byte order, as-is from RFC) */
localparam [255:0] ED25519_Q_Y_5 =
    {32'hec172b93, 32'had5e563b, 32'hf4932c70, 32'he1245034,
     32'hc35467ef, 32'h2efd4d64, 32'hebf81968, 32'h3467e2bf};
