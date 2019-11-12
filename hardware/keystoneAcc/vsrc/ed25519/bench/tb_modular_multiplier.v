//------------------------------------------------------------------------------
//
// tb_modular_multiplier.v
// -----------------------------------------------------------------------------
// Testbench for Curve25519 modular multiplier.
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

module tb_modular_multiplier;


        //
        // Test Vectors
        //
    localparam A1 = 256'h216936d3_cd6e53fe_c0a4e231_fdd6dc5c_692cc760_9525a7b2_c9562d60_8f25d51a;   // GX
    localparam B1 = 256'h66666666_66666666_66666666_66666666_66666666_66666666_66666666_66666658;   // GY
    localparam C1 = 256'h67875f0f_d78b7665_66ea4e8e_64abe37d_20f09f80_775152f5_6dde8ab3_a5b7dda3;   // GT

    localparam F  = 256'hFFFFFFFF_FFFFFFFF_FFFFFFFF_FFFFFFFF_FFFFFFFF_FFFFFFFF_FFFFFFFF_FFFFFFFF;   // FFF...F


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
        // Buffers (A, B, P)
        //
    wire    [WORD_COUNTER_WIDTH-1:0]    core_a_addr;
    wire    [WORD_COUNTER_WIDTH-1:0]    core_b_addr;
    wire    [WORD_COUNTER_WIDTH-1:0]    core_p_addr;
    
    wire                                core_p_wren;
    
    wire    [                32-1:0]    core_a_data;
    wire    [                32-1:0]    core_b_data;
    wire    [                32-1:0]    core_p_data;
    
    reg     [WORD_COUNTER_WIDTH-1:0]    tb_ab_addr;
    reg     [WORD_COUNTER_WIDTH-1:0]    tb_p_addr;
    
    reg                                 tb_ab_wren;
    
    reg     [                32-1:0]    tb_a_data;
    reg     [                32-1:0]    tb_b_data;
    wire    [                32-1:0]    tb_p_data;
    
    bram_1rw_1ro_readfirst # (.MEM_WIDTH(32), .MEM_ADDR_BITS(WORD_COUNTER_WIDTH))
    bram_a
    (   .clk(clk),
        .a_addr(tb_ab_addr),  .a_wr(tb_ab_wren), .a_in(tb_a_data), .a_out(),
        .b_addr(core_a_addr), .b_out(core_a_data)
    );

    bram_1rw_1ro_readfirst # (.MEM_WIDTH(32), .MEM_ADDR_BITS(WORD_COUNTER_WIDTH))
    bram_b
    (   .clk(clk),
        .a_addr(tb_ab_addr),  .a_wr(tb_ab_wren), .a_in(tb_b_data), .a_out(),
        .b_addr(core_b_addr), .b_out(core_b_data)
    );
        
    bram_1rw_1ro_readfirst # (.MEM_WIDTH(32), .MEM_ADDR_BITS(WORD_COUNTER_WIDTH))
    bram_p
    (   .clk(clk),
        .a_addr(core_p_addr), .a_wr(core_p_wren), .a_in(core_p_data), .a_out(),
        .b_addr(tb_p_addr),   .b_out(tb_p_data)
    );
    
    
        //
        // UUT
        //
    curve25519_modular_multiplier uut
    (
        .clk        (clk),
        .rst_n      (rst_n),
        
        .ena        (ena),
        .rdy        (rdy),
        
        .a_addr     (core_a_addr),
        .b_addr     (core_b_addr),
        .p_addr     (core_p_addr),
        
        .p_wren     (core_p_wren),
        
        .a_din      (core_a_data),
        .b_din      (core_b_data),
        .p_dout     (core_p_data)
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
        $display("1. A1 * A1 = ...");
        test_modular_multiplier(A1   , B1);
        test_modular_multiplier(A1+B1, C1+C1);
        test_modular_multiplier(F,     F);
        
            /* print result */
        if (ok) $display("tb_modular_multiplier: SUCCESS");
        else    $display("tb_modular_multiplier: FAILURE");
        //
        #10000;
        //
        $finish;
        //
    end
    
    
        //
        // Test Task
        //  
    task test_modular_multiplier;
    
        input   [255:0] a;
        input   [255:0] b;
        
        reg     [255:0] a_shreg;
        reg     [255:0] b_shreg;
        reg     [255:0] p_shreg;
        reg             p_ok;
        reg     [511:0] ab;
        reg     [255:0] p_ref;
        integer         w;

        begin
        
                /* calculate reference value */
            ab = {{256{1'b0}}, a} * {{256{1'b0}}, b};
            p_ref = ab % {{31{8'hFF}}, 8'hDA};
        
                /* initialize result */
            p_ok = 0;

                /* initialize shift registers */
            a_shreg = a;
            b_shreg = b;

                /* start filling memories */
            tb_ab_wren = 1;
            
                /* write all the words */
            for (w=0; w<OPERAND_NUM_WORDS; w=w+1) begin
                
                    /* set addresses */
                tb_ab_addr = w[WORD_COUNTER_WIDTH-1:0];
                
                    /* set data words */
                tb_a_data = a_shreg[31:0];
                tb_b_data = b_shreg[31:0];
                
                    /* shift inputs */
                a_shreg = {{32{1'bX}}, a_shreg[255:32]};
                b_shreg = {{32{1'bX}}, b_shreg[255:32]};
                
                    /* wait for 1 clock tick */
                #`CLOCK_PERIOD;
                
            end
            
                /* stop filling memories */
            tb_ab_wren = 0;

                /* wipe addresses */
            tb_ab_addr = {WORD_COUNTER_WIDTH{1'bX}};
            
                /* wipe data words */
            tb_a_data = {32{1'bX}};
            tb_b_data = {32{1'bX}};
                        
                /* start operation */
            ena = 1;
            
                /* clear flag */
            #`CLOCK_PERIOD ena = 0;
            
                /* wait for operation to complete */
            while (!rdy) #`CLOCK_PERIOD;
            
                /* read result */
            for (w=0; w<OPERAND_NUM_WORDS; w=w+1) begin
                
                    /* set address */
                tb_p_addr = w[WORD_COUNTER_WIDTH-1:0];
                
                    /* wait for 1 clock tick */
                #`CLOCK_PERIOD;
                
                    /* store data word */
                p_shreg = {tb_p_data, p_shreg[255:32]};

            end
            
                /* compare */
            p_ok = (p_shreg === p_ref);

                /* display results */
            if (p_ok) $display("test_modular_multiplier(): CORRECT RESULT");
            else begin
                $display("test_modular_multiplier(): WRONG RESULT");
                $display("XOR: %x", p_shreg ^ p_ref);
            end
            
                /* update global flag */
            ok = ok & p_ok;
        
        end
        
    endtask
    
endmodule


//------------------------------------------------------------------------------
// End-of-File
//------------------------------------------------------------------------------
