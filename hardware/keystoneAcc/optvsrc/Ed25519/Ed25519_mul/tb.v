//------------------------------------------------------------------------------
//
// tb_base_point_multiplier.v
// -----------------------------------------------------------------------------
// Testbench for Ed25519 base point scalar multiplier.
//
// Authors: Pavel Shatov
//
// Copyright (c) 2018, NORDUnet A/S
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice,
//   this list of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice,
//   this list of conditions and the following disclaimer in the documentation
//   and/or other materials provided with the distribution.
//
// - Neither the name of the NORDUnet nor the names of its contributors may be
//   used to endorse or promote products derived from this software without
//   specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
//
//------------------------------------------------------------------------------
`timescale 1ns/1ps
module tb_base_point_multiplier;

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


        //
        // Core Parameters
        //
    localparam  WORD_COUNTER_WIDTH  = 3;
    localparam  OPERAND_NUM_WORDS   = 8;


        //
        // Clock (100 MHz)
        //
`define CLOCK_PERIOD        10.0
`define CLOCK_HALF_PERIOD   (0.5 * `CLOCK_PERIOD)

    reg clk = 1'b0;
    always #`CLOCK_HALF_PERIOD clk = ~clk;


        //
        // Inputs, Outputs
        //
    reg     rst_n;
    reg     ena;
    wire    rdy;


        //
        // Buffers (K, QY)
        //
    wire    [WORD_COUNTER_WIDTH-1:0]    core_k_addr;
    wire    [WORD_COUNTER_WIDTH-1:0]    core_qy_addr;
    
    wire                                core_qy_wren;
    
    wire    [                32-1:0]    core_k_data;
    wire    [                32-1:0]    core_qy_data;
    
    reg     [WORD_COUNTER_WIDTH-1:0]    tb_k_addr;
    reg     [WORD_COUNTER_WIDTH-1:0]    tb_qy_addr;
    
    reg                                 tb_k_wren;
    
    reg     [                32-1:0]    tb_k_data;
    wire    [                32-1:0]    tb_qy_data;
    
    bram_1rw_1ro_readfirst # (.MEM_WIDTH(32), .MEM_ADDR_BITS(WORD_COUNTER_WIDTH))
    bram_k
    (   .clk(clk),
        .a_addr(tb_k_addr),   .a_wr(tb_k_wren), .a_in(tb_k_data), .a_out(),
        .b_addr(core_k_addr), .b_out(core_k_data)
    );
        
    bram_1rw_1ro_readfirst # (.MEM_WIDTH(32), .MEM_ADDR_BITS(WORD_COUNTER_WIDTH))
    bram_qy
    (   .clk(clk),
        .a_addr(core_qy_addr), .a_wr(core_qy_wren), .a_in(core_qy_data), .a_out(),
        .b_addr(tb_qy_addr),   .b_out(tb_qy_data)
    );
    
    
        //
        // UUT
        //
    ed25519_base_point_multiplier uut (
        .iClk		(clk),
        .iRstn		(rst_n),
        
        .iEn		(ena),
        .oReady		(rdy),
        
        .oK_addr	(core_k_addr),
        .oQy_addr	(core_qy_addr),
        
        .oQy_wren	(core_qy_wren),
        
        .iK			(core_k_data),
        .oQy		(core_qy_data)
    );
        
        
        //
        // Testbench Routine
        //
    reg ok = 1;
    initial begin
        
            /* initialize control inputs */
        rst_n   = 0;
        ena     = 0;
        
            /* wait for some time */
        #200;
        
            /* de-assert reset */
        rst_n   = 1;
        
            /* wait for some time */
        #100;
        
            /* run tests */
        $display("1. Q = d * G...");
        test_base_point_multiplier(ED25519_D_HASHED_LSB_1, ED25519_Q_Y_1);
        
        $display("2. Q = d * G...");
        test_base_point_multiplier(ED25519_D_HASHED_LSB_2, ED25519_Q_Y_2);
        
        $display("3. Q = d * G...");
        test_base_point_multiplier(ED25519_D_HASHED_LSB_3, ED25519_Q_Y_3);
        
        $display("4. Q = d * G...");
        test_base_point_multiplier(ED25519_D_HASHED_LSB_4, ED25519_Q_Y_4);
        
        $display("5. Q = d * G...");
        test_base_point_multiplier(ED25519_D_HASHED_LSB_5, ED25519_Q_Y_5);

        //$display("6. Q = d * G...");
        //test_base_point_multiplier(ED25519_D_HASHED_LSB_6, ED25519_Q_Y_6);
        
            /* print result */
        if (ok) $display("tb_base_point_multiplier: SUCCESS");
        else    $display("tb_base_point_multiplier: FAILURE");
        //
        #10000;
        //
        $finish;
        //
    end
    
    
        //
        // Test Task
        //  
    task test_base_point_multiplier;
    
        input   [255:0] k;
        input   [255:0] qy;
        
        reg     [255:0] k_shreg;
        reg     [255:0] qy_shreg;
        reg             qy_ok;
        reg     [255:0] qy_shreg_rev;
        
        integer         w;

        begin
        
                /* initialize result */
            qy_ok = 0;

                /* initialize shift registers */
            k_shreg = k;

                /* start filling memories */
            tb_k_wren = 1;
            
                /* write all the words */
            for (w=0; w<OPERAND_NUM_WORDS; w=w+1) begin
                
                    /* set addresses */
                tb_k_addr = w[WORD_COUNTER_WIDTH-1:0];
                
                    /* set data words */
                tb_k_data   = k_shreg[31:0];
                
                    /* shift inputs */
                k_shreg = {{32{1'bX}}, k_shreg[255:32]};
                
                    /* wait for 1 clock tick */
                #`CLOCK_PERIOD;
                
            end
            
                /* stop filling memories */
            tb_k_wren = 0;

                /* wipe addresses */
            tb_k_addr = {WORD_COUNTER_WIDTH{1'bX}};
            
                /* wipe data words */
            tb_k_data = {32{1'bX}};
                        
                /* start operation */
            ena = 1;
            
                /* clear flag */
            #`CLOCK_PERIOD ena = 0;
            
                /* wait for operation to complete */
            while (!rdy) #`CLOCK_PERIOD;
            
                /* read result */
            for (w=0; w<OPERAND_NUM_WORDS; w=w+1) begin
                
                    /* set address */
                tb_qy_addr = w[WORD_COUNTER_WIDTH-1:0];
                
                    /* wait for 1 clock tick */
                #`CLOCK_PERIOD
                
                    /* store data word */
                qy_shreg = {tb_qy_data, qy_shreg[255:32]};

            end
            
                /* for some reason reference values in the RFC have different
                 * byte order, thus we need to reverse our result */
                 
            #`CLOCK_PERIOD;
                 
            for (w=0; w<4*OPERAND_NUM_WORDS; w=w+1) begin
                
                /* shift right by 8 bits */
                qy_shreg     = {qy_shreg[7:0], qy_shreg[255:8]};
                
                /* shift left by 8 bits */
                qy_shreg_rev = {qy_shreg_rev[255-8:0], qy_shreg[255-:8]};
            end
            
            
                /* compare */
            qy_ok = (qy_shreg_rev == qy);

                /* display results */
            if (qy_ok)  $display("test_base_point_multiplier(): CORRECT RESULT");
            else begin
                $display("test_base_point_multiplier(): WRONG RESULT");
                $display("REF: %x", qy);
                $display("OUT: %x", qy_shreg_rev);
                $display("XOR: %x", qy_shreg_rev ^ qy);
            end
            
                /* update global flag */
            ok = ok & qy_ok;
        
        end
        
    endtask
    
endmodule


//------------------------------------------------------------------------------
// End-of-File
//------------------------------------------------------------------------------
