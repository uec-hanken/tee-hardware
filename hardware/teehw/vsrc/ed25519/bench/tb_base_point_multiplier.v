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

module tb_base_point_multiplier;


        //
        // Test Vectors
        //
    `include "ed25519_test_vectors_rfc8032.vh"
    //`include "ed25519_test_vector_randomized.vh"


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
    ed25519_base_point_multiplier uut
    (
        .clk        (clk),
        .rst_n      (rst_n),
        
        .ena        (ena),
        .rdy        (rdy),
        
        .k_addr     (core_k_addr),
        .qy_addr    (core_qy_addr),
        
        .qy_wren    (core_qy_wren),
        
        .k_din      (core_k_data),
        .qy_dout    (core_qy_data)
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
