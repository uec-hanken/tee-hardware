//------------------------------------------------------------------------------
//
// Curve25519_modular_multiplier.v
// -----------------------------------------------------------------------------
// Curve25519 Modular Multiplier.
//
// Authors: Pavel Shatov
//
// Copyright (c) 2015-2016, 2018 NORDUnet A/S
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


module curve25519_modular_multiplier
(
    clk, rst_n,
    ena, rdy,
    a_addr, b_addr, p_addr, p_wren,
    a_din, b_din, p_dout
);


    //
    // Settings
    //
    `include "cryptech_primitive_switch.vh"


    //
    // Constants
    //
    localparam integer OPERAND_NUM_WORDS    = 8;
    localparam integer WORD_COUNTER_WIDTH   = 3;


    //
    // Handy Numbers
    //
    localparam [WORD_COUNTER_WIDTH-1:0] WORD_INDEX_ZERO = 0;
    localparam [WORD_COUNTER_WIDTH-1:0] WORD_INDEX_LAST = OPERAND_NUM_WORDS - 1;


    //
    // Handy Functions
    //
    function [WORD_COUNTER_WIDTH-1:0] WORD_INDEX_NEXT_OR_ZERO;
    input    [WORD_COUNTER_WIDTH-1:0] WORD_INDEX_CURRENT;
    begin
        WORD_INDEX_NEXT_OR_ZERO = (WORD_INDEX_CURRENT < WORD_INDEX_LAST) ?
            WORD_INDEX_CURRENT + 1'b1 : WORD_INDEX_ZERO;
    end
    endfunction

    function [WORD_COUNTER_WIDTH-1:0] WORD_INDEX_PREVIOUS_OR_LAST;
    input    [WORD_COUNTER_WIDTH-1:0] WORD_INDEX_CURRENT;
    begin
        WORD_INDEX_PREVIOUS_OR_LAST = (WORD_INDEX_CURRENT > WORD_INDEX_ZERO) ?
            WORD_INDEX_CURRENT - 1'b1 : WORD_INDEX_LAST;
    end
    endfunction


    //
    // Ports
    //
    input   clk;    // system clock
    input   rst_n;  // active-low async reset

    input   ena;    // enable input
    output  rdy;    // ready output

    output  [WORD_COUNTER_WIDTH-1:0]    a_addr;    // index of current A word
    output  [WORD_COUNTER_WIDTH-1:0]    b_addr;    // index of current B word
    output  [WORD_COUNTER_WIDTH-1:0]    p_addr;    // index of current P word
    
    output  p_wren;    // store current P word now

    input   [31:0]  a_din;      // current word of A
    input   [31:0]  b_din;      // current word of B
    output  [31:0]  p_dout;     // current word of P


    //
    // Word Indices
    //
    reg [WORD_COUNTER_WIDTH-1:0] index_a;
    reg [WORD_COUNTER_WIDTH-1:0] index_b;

    // map registers to output ports
    assign a_addr    = index_a;
    assign b_addr    = index_b;


    //
    // FSM
    //

    
    localparam integer PHASE_INCREMENT_INDEX_A_OFFSET           =   0;
    localparam integer PHASE_INCREMENT_INDEX_A_DURATION         =   OPERAND_NUM_WORDS;
    
    localparam integer PHASE_DECREMENT_INDEX_B_OFFSET           =   PHASE_INCREMENT_INDEX_A_DURATION;
    localparam integer PHASE_DECREMENT_INDEX_B_DURATION         =   OPERAND_NUM_WORDS * 2;

    localparam integer PHASE_STORE_MSB_SI_OFFSET                =   PHASE_DECREMENT_INDEX_B_OFFSET + 2;
    localparam integer PHASE_STORE_MSB_SI_DURATION              =   OPERAND_NUM_WORDS * 2 - 1;

    localparam integer PHASE_STORE_LSB_SI_OFFSET                =   PHASE_STORE_MSB_SI_OFFSET +
                                                                    PHASE_STORE_MSB_SI_DURATION;
    localparam integer PHASE_STORE_LSB_SI_DURATION              =   1;
    
    localparam integer PHASE_SHIFT_SI_OFFSET                    =   PHASE_STORE_LSB_SI_OFFSET + 1;
    localparam integer PHASE_SHIFT_SI_DURATION                  =   OPERAND_NUM_WORDS * 2 - 1;

    localparam integer PHASE_MASK_SUM_CW1_OFFSET                =   PHASE_SHIFT_SI_OFFSET + 1;
    localparam integer PHASE_MASK_SUM_CW1_DURATION              =   1;    
    
    localparam integer PHASE_STORE_LSB_C_OFFSET                 =   PHASE_MASK_SUM_CW1_OFFSET + 1;
    localparam integer PHASE_STORE_LSB_C_DURATION               =   OPERAND_NUM_WORDS;

    localparam integer PHASE_STORE_MSB_C_OFFSET                 =   PHASE_STORE_LSB_C_OFFSET +
                                                                    PHASE_STORE_LSB_C_DURATION;
    localparam integer PHASE_STORE_MSB_C_DURATION               =   OPERAND_NUM_WORDS;

    localparam integer PHASE_MASK_B_R3_OFFSET                   =   PHASE_STORE_MSB_C_OFFSET + 3;
    localparam integer PHASE_MASK_B_R3_DURATION                 =   1;

    localparam integer PHASE_CALCULATE_CARRY_MSB_S1_OFFSET      =   PHASE_STORE_MSB_C_OFFSET +
                                                                    PHASE_STORE_MSB_C_DURATION + 4;
    localparam integer PHASE_CALCULATE_CARRY_MSB_S1_DURATION    =   1;
    
    localparam integer PHASE_STORE_LSB_S1_OFFSET                =   PHASE_STORE_MSB_C_OFFSET + 4;
    localparam integer PHASE_STORE_LSB_S1_DURATION              =   OPERAND_NUM_WORDS;
    
    localparam integer PHASE_SHIFT_S1_OFFSET                    =   PHASE_STORE_LSB_S1_OFFSET +
                                                                    PHASE_STORE_LSB_S1_DURATION + 1;
    localparam integer PHASE_SHIFT_S1_DURATION                  =   OPERAND_NUM_WORDS;

    localparam integer PHASE_CHANGE_LSB_B_P_OFFSET              =   PHASE_SHIFT_S1_OFFSET;
    localparam integer PHASE_CHANGE_LSB_B_P_DURATION            =   1;

    localparam integer PHASE_SELECT_S2_OR_PN_OFFSET             =   PHASE_SHIFT_S1_OFFSET +
                                                                    PHASE_SHIFT_S1_DURATION + 1;
    localparam integer PHASE_SELECT_S2_OR_PN_DURATION           =   1;

    localparam integer PHASE_UPDATE_P_DOUT_OFFSET               =   PHASE_SHIFT_S1_OFFSET +
                                                                    PHASE_SHIFT_S1_DURATION + 2;
    localparam integer PHASE_UPDATE_P_DOUT_DURATION             =   OPERAND_NUM_WORDS;

    
    
    localparam integer FSM_SHREG_WIDTH =    PHASE_INCREMENT_INDEX_A_DURATION +
                                            PHASE_DECREMENT_INDEX_B_DURATION +
                                            1 +
                                            PHASE_STORE_LSB_SI_DURATION +
                                            PHASE_SHIFT_SI_DURATION +
                                            -1 +
                                            PHASE_STORE_LSB_S1_DURATION +
                                            PHASE_CALCULATE_CARRY_MSB_S1_DURATION +
                                            PHASE_SHIFT_S1_DURATION +
                                            1 +
                                            PHASE_SELECT_S2_OR_PN_DURATION +
                                            PHASE_UPDATE_P_DOUT_DURATION +
                                            2;

    localparam [FSM_SHREG_WIDTH-1:0] FSM_SHREG_INIT = {{(FSM_SHREG_WIDTH-1){1'b0}}, 1'b1};

    reg [FSM_SHREG_WIDTH-1:0] fsm_shreg = FSM_SHREG_INIT;

    assign rdy = fsm_shreg[0];

    
    
    
    
    
    
    wire [PHASE_INCREMENT_INDEX_A_DURATION     -1:0] fsm_shreg_increment_index_a      = fsm_shreg[FSM_SHREG_WIDTH - PHASE_INCREMENT_INDEX_A_OFFSET      - 1 -: PHASE_INCREMENT_INDEX_A_DURATION];
    wire [PHASE_DECREMENT_INDEX_B_DURATION     -1:0] fsm_shreg_decrement_index_b      = fsm_shreg[FSM_SHREG_WIDTH - PHASE_DECREMENT_INDEX_B_OFFSET      - 1 -: PHASE_DECREMENT_INDEX_B_DURATION];
    wire [PHASE_STORE_MSB_SI_DURATION          -1:0] fsm_shreg_store_msb_si           = fsm_shreg[FSM_SHREG_WIDTH - PHASE_STORE_MSB_SI_OFFSET           - 1 -: PHASE_STORE_MSB_SI_DURATION];
    wire [PHASE_STORE_LSB_SI_DURATION          -1:0] fsm_shreg_store_lsb_si           = fsm_shreg[FSM_SHREG_WIDTH - PHASE_STORE_LSB_SI_OFFSET           - 1 -: PHASE_STORE_LSB_SI_DURATION];
    wire [PHASE_SHIFT_SI_DURATION              -1:0] fsm_shreg_shift_si               = fsm_shreg[FSM_SHREG_WIDTH - PHASE_SHIFT_SI_OFFSET               - 1 -: PHASE_SHIFT_SI_DURATION];
    wire [PHASE_MASK_SUM_CW1_DURATION          -1:0] fsm_shreg_mask_sum_cw1           = fsm_shreg[FSM_SHREG_WIDTH - PHASE_MASK_SUM_CW1_OFFSET           - 1 -: PHASE_MASK_SUM_CW1_DURATION];
    wire [PHASE_STORE_LSB_C_DURATION           -1:0] fsm_shreg_store_lsb_c            = fsm_shreg[FSM_SHREG_WIDTH - PHASE_STORE_LSB_C_OFFSET            - 1 -: PHASE_STORE_LSB_C_DURATION];
    wire [PHASE_STORE_MSB_C_DURATION           -1:0] fsm_shreg_store_msb_c            = fsm_shreg[FSM_SHREG_WIDTH - PHASE_STORE_MSB_C_OFFSET            - 1 -: PHASE_STORE_MSB_C_DURATION];
    wire [PHASE_MASK_B_R3_DURATION             -1:0] fsm_shreg_mask_b_r3              = fsm_shreg[FSM_SHREG_WIDTH - PHASE_MASK_B_R3_OFFSET              - 1 -: PHASE_MASK_B_R3_DURATION];
    wire [PHASE_CALCULATE_CARRY_MSB_S1_DURATION-1:0] fsm_shreg_calculate_carry_msb_s1 = fsm_shreg[FSM_SHREG_WIDTH - PHASE_CALCULATE_CARRY_MSB_S1_OFFSET - 1 -: PHASE_CALCULATE_CARRY_MSB_S1_DURATION];
    wire [PHASE_STORE_LSB_S1_DURATION          -1:0] fsm_shreg_store_lsb_s1           = fsm_shreg[FSM_SHREG_WIDTH - PHASE_STORE_LSB_S1_OFFSET           - 1 -: PHASE_STORE_LSB_S1_DURATION];
    wire [PHASE_SHIFT_S1_DURATION              -1:0] fsm_shreg_shift_s1               = fsm_shreg[FSM_SHREG_WIDTH - PHASE_SHIFT_S1_OFFSET               - 1 -: PHASE_SHIFT_S1_DURATION];
    wire [PHASE_CHANGE_LSB_B_P_DURATION        -1:0] fsm_shreg_change_lsb_b_p         = fsm_shreg[FSM_SHREG_WIDTH - PHASE_CHANGE_LSB_B_P_OFFSET         - 1 -: PHASE_CHANGE_LSB_B_P_DURATION];
    wire [PHASE_SELECT_S2_OR_PN_DURATION       -1:0] fsm_shreg_select_s2_or_pn        = fsm_shreg[FSM_SHREG_WIDTH - PHASE_SELECT_S2_OR_PN_OFFSET        - 1 -: PHASE_SELECT_S2_OR_PN_DURATION];
    wire [PHASE_UPDATE_P_DOUT_DURATION         -1:0] fsm_shreg_update_p_dout          = fsm_shreg[FSM_SHREG_WIDTH - PHASE_UPDATE_P_DOUT_OFFSET          - 1 -: PHASE_UPDATE_P_DOUT_DURATION];

    wire flag_increment_index_a      = |fsm_shreg_increment_index_a;
    wire flag_decrement_index_b      = |fsm_shreg_decrement_index_b;
    wire flag_store_msb_si           = |fsm_shreg_store_msb_si;
    wire flag_store_lsb_si           = |fsm_shreg_store_lsb_si;
    wire flag_shift_si               = |fsm_shreg_shift_si;
    wire flag_mask_sum_cw1           = |fsm_shreg_mask_sum_cw1;
    wire flag_store_lsb_c            = |fsm_shreg_store_lsb_c;
    wire flag_store_msb_c            = |fsm_shreg_store_msb_c;
    wire flag_mask_b_r3              = |fsm_shreg_mask_b_r3;
    wire flag_calculate_carry_msb_s1 = |fsm_shreg_calculate_carry_msb_s1;
    wire flag_store_lsb_s1           = |fsm_shreg_store_lsb_s1;
    wire flag_shift_s1               = |fsm_shreg_shift_s1;
    wire flag_change_lsb_b_p         = |fsm_shreg_change_lsb_b_p;
    wire flag_select_s2_or_pn        = |fsm_shreg_select_s2_or_pn;
    wire flag_update_p_dout          = |fsm_shreg_update_p_dout;
    
    reg flag_store_word_a   = 0;
    reg flag_enable_mac_ab  = 0;
    reg flag_delay_msb_c    = 0;
    reg flag_mask_a_s2      = 0;
    reg flag_mask_b_out_p   = 0;
    reg flag_store_s2       = 0;
    reg flag_store_pn       = 0;
    
    always @(posedge clk) begin
        flag_store_word_a   <= flag_increment_index_a;
        flag_enable_mac_ab  <= flag_decrement_index_b;
        flag_delay_msb_c    <= flag_store_msb_c;
        flag_mask_a_s2      <= flag_calculate_carry_msb_s1;
        flag_mask_b_out_p   <= flag_change_lsb_b_p;
        flag_store_s2       <= flag_shift_s1;
        flag_store_pn       <= flag_store_s2;
    end    


    //
    // FSM Logic
    //    
    always @(posedge clk or negedge rst_n)
        //
        if (rst_n == 1'b0)
            //
            fsm_shreg <= FSM_SHREG_INIT;
        //
        else begin
            //
            if (rdy) fsm_shreg <= {ena, {FSM_SHREG_WIDTH-2{1'b0}}, ~ena};
            else     fsm_shreg <= {1'b0, fsm_shreg[FSM_SHREG_WIDTH-1:1]};
        end


    //
    // A Word Index Increment Logic
    //
    always @(posedge clk)
        //
        if (rdy)              index_a <= WORD_INDEX_ZERO;
        else if (flag_increment_index_a) index_a <= WORD_INDEX_NEXT_OR_ZERO(index_a);


    //
    // B Word Index Decrement Logic
    //
    always @(posedge clk)
        //
        if (rdy)                                index_b <= WORD_INDEX_LAST;
        else if (flag_decrement_index_b && !index_b_ff)    index_b <= WORD_INDEX_PREVIOUS_OR_LAST(index_b);

        
    //
    // Wide Operand Buffer
    //
    reg [255:0] buf_a_wide;

    

    //
    // B Word Splitter
    //
    
    /*
     * 0: store the upper 16-bit part of the current B word
     * 1: store the lower 16-bit part of the current B word
     */
    
    reg index_b_ff = 1'b0;

    always @(posedge clk)
        //
        if (flag_decrement_index_b)    index_b_ff <= ~index_b_ff;
        else                index_b_ff <= 1'b0;
        

    //
    // Narrow Operand Buffer
    //
    reg [15:0] buf_b_narrow;
    
    always @(posedge clk)
        //
        if (flag_decrement_index_b) buf_b_narrow <= !index_b_ff ? b_din[31:16] : b_din[15:0];


    //
    // MAC Clear Logic
    //
    reg  [15:0] mac_clear;

    always @(posedge clk)
        //
        if (!flag_enable_mac_ab) mac_clear <= {16{1'b1}};
        else begin
            if (mac_clear[0])       mac_clear <= 16'b0000000000000010;
            else if (mac_clear[15]) mac_clear <= 16'b1111111111111111;
            else                    mac_clear <= {mac_clear[14:0], 1'b0};
        end


    //
    // MAC Array
    //
    wire [46:0] mac_accum[0:15];

    genvar i;
    
    generate for (i=0; i<16; i=i+1)
        //
        begin : gen_mac16_array
            //
            `CRYPTECH_PRIMITIVE_MAC16 mac16_inst
            (
                .clk    (clk),
                .ce     (flag_enable_mac_ab),

                .clr    (mac_clear[i]),

                .a      (buf_a_wide[16 * i +: 16]),
                .b      (buf_b_narrow),
                .s      (mac_accum[i])
            );
            //
        end
        //
    endgenerate


    //
    // Intermediate Words
    //
    reg [47*(2*OPERAND_NUM_WORDS-1)-1:0] si_msb;
    reg [47*(2*OPERAND_NUM_WORDS-0)-1:0] si_lsb;

    wire [47*(2*OPERAND_NUM_WORDS-1)-1:0] si_msb_new;
    wire [47*(2*OPERAND_NUM_WORDS-0)-1:0] si_lsb_new;
    
    generate for (i=0; i<16; i=i+1)
        begin : gen_si_lsb_new
            assign si_lsb_new[47*i+:47] = mac_accum[15-i];
        end
    endgenerate

    generate for (i=1; i<16; i=i+1)
        begin : gen_si_msb_new
            assign si_msb_new[47*(15-i)+:47] = mac_clear[i] ? mac_accum[i] : si_msb[47*(15-i)+:47];
        end
    endgenerate

    always @(posedge clk)
        //
        if (flag_shift_si) begin
            si_msb <= {{2*47{1'b0}}, si_msb[15*47-1:2*47]};
            si_lsb <= {si_msb[2*47-1:0], si_lsb[16*47-1:2*47]};
        end else begin
            if (flag_store_msb_si)   si_msb <= si_msb_new;
            if (flag_store_lsb_si)   si_lsb <= si_lsb_new;
        end


    //
    // Accumulators
    //
    wire [46:0] add47_cw0_s;
    wire [46:0] add47_cw1_s;
    wire [14:0] add47_cw1_s_masked = flag_mask_sum_cw1 ? {15{1'b0}} : add47_cw1_s[32+:15];
    
    wire [46:0] add47_r3_b_masked = {{32{1'b0}}, flag_mask_b_r3 ? {15{1'b0}} : add47_r3_s[46:32]};

    
    //
    // cw0, cw1
    //
    reg [30: 0] si_prev_dly;
    reg [15: 0] si_next_dly;

    always @(posedge clk)
        //
        if (flag_shift_si) si_prev_dly <= si_lsb[93:63];
        else          si_prev_dly <= {31{1'b0}};

    always @(posedge clk)
        //
        si_next_dly <= si_lsb[47+:16];

    wire [46:0] add47_cw0_a = si_lsb[46:0];
    wire [46:0] add47_cw0_b = {{16{1'b0}}, si_prev_dly};

    wire [46:0] add47_cw1_a = add47_cw0_s;
    wire [46:0] add47_cw1_b = {{15{1'b0}}, si_next_dly, 1'b0, add47_cw1_s_masked};

    `CRYPTECH_PRIMITIVE_ADD47 add47_cw0_inst
    (
        .clk    (clk),
        .a      (add47_cw0_a),
        .b      (add47_cw0_b),
        .s      (add47_cw0_s)
    );

    `CRYPTECH_PRIMITIVE_ADD47 add47_cw1_inst
    (
        .clk    (clk),
        .a      (add47_cw1_a),
        .b      (add47_cw1_b),
        .s      (add47_cw1_s)
    );
    
    
    //
    // Full-Size Product
    //
    wire [31:0] c_word_lower = add47_cw1_s[31:0];

     
    wire [46:0] add47_r0_s;
    wire [46:0] add47_r1_s;
    wire [46:0] add47_r2_s;
    wire [46:0] add47_r3_s;
    
    reg [255:0] c_lsb_s1_shreg;
    reg [ 31:0] c_msb_latch;
            
    
        

        
    always @(posedge clk)
        //
        if (flag_store_msb_c) c_msb_latch <= c_word_lower;
        else             c_msb_latch <= {32{1'b0}};
    
        
    reg [4:0] c_msb_latch_upper_dly;
    reg [31:0] c_lsb_shreg_lower_dly;
    
    always @(posedge clk)
        //
        if (flag_delay_msb_c) c_msb_latch_upper_dly <= c_msb_latch[31:27];
        else             c_msb_latch_upper_dly <= {5{1'b0}};
            
    
    always @(posedge clk)
        //
        if (flag_store_msb_c) c_lsb_shreg_lower_dly <= c_lsb_s1_shreg[31:0];
        else             c_lsb_shreg_lower_dly <= {32{1'b0}};
    
    
    
    reg [11:0] carry_msb_s1;
    
    always @(posedge clk)
        //
        if (flag_calculate_carry_msb_s1) carry_msb_s1 <= {{6{1'b0}}, 6'd38} * {{6{1'b0}}, add47_r3_s[5:0]};
    
    
    wire [46:0] add47_s2_a_masked = {{32{1'b0}}, flag_mask_a_s2 ? {3'b000, carry_msb_s1} : add47_s2_s[46:32]};
    
    `CRYPTECH_PRIMITIVE_ADD47 add47_r0
    (
        .clk    (clk),
        .a      ({{15{1'b0}}, c_msb_latch[30:0], c_msb_latch_upper_dly[4]}),
        .b      ({{15{1'b0}}, c_msb_latch[29:0], c_msb_latch_upper_dly[4:3]}),
        .s      (add47_r0_s)
    );
    `CRYPTECH_PRIMITIVE_ADD47 add47_r1
    (
        .clk    (clk),
        .a      ({{15{1'b0}}, c_msb_latch[26:0], c_msb_latch_upper_dly[4:0]}),
        .b      ({{15{1'b0}}, c_lsb_shreg_lower_dly}),
        .s      (add47_r1_s)
    );
    `CRYPTECH_PRIMITIVE_ADD47 add47_r2
    (
        .clk    (clk),
        .a      (add47_r0_s),
        .b      (add47_r1_s),
        .s      (add47_r2_s)
    );
    `CRYPTECH_PRIMITIVE_ADD47 add47_r3
    (
        .clk    (clk),
        .a      (add47_r2_s),
        .b      (add47_r3_b_masked),
        .s      (add47_r3_s)
    );
    
          
    
    wire [46:0] add47_s2_s;
    `CRYPTECH_PRIMITIVE_ADD47 add47_s2
    (
        .clk    (clk),
        .a      (add47_s2_a_masked),
        .b      ({{15{1'b0}}, c_lsb_s1_shreg[31:0]}),
        .s      (add47_s2_s)
    );
    
    
    reg sub32_b_bit;
    
    wire [31:0] sub32_b = {{26{1'b1}},  // ...*11*1*
        sub32_b_bit, {2{1'b1}}, sub32_b_bit, 1'b1, sub32_b_bit};
    
    always @(posedge clk)
        //
        if (!fsm_shreg_change_lsb_b_p) sub32_b_bit <= 1'b1;
        else                           sub32_b_bit <= 1'b0;
    
    wire [31:0] sub32_pn_d;
    wire        sub32_b_in;
    wire        sub32_b_out;

    assign sub32_b_in = sub32_b_out & !flag_mask_b_out_p;
    
    `CRYPTECH_PRIMITIVE_SUB32 sub32_pn
    (
        .clk    (clk),
        .a      (add47_s2_s[31:0]),
        .b      (sub32_b),
        .d      (sub32_pn_d),
        .b_in   (sub32_b_in),
        .b_out  (sub32_b_out)
    );
    

    wire [31:0] add47_r3_s_lower = add47_r3_s[31:0];
    

    always @(posedge clk)
        //
        if (flag_store_word_a)       buf_a_wide <= {buf_a_wide[16+:256-3*16], {a_din[15:0], a_din[31:16]}, buf_a_wide[256-2*16+:16]};
        else if (flag_enable_mac_ab) buf_a_wide <= {buf_a_wide[256-(16+1):0], buf_a_wide[256-16+:16]};
        else if (flag_store_s2)      buf_a_wide <= {add47_s2_s[31:0], buf_a_wide[255:32]};
        else if (flag_update_p_dout)  buf_a_wide <= {{32{1'bX}}, buf_a_wide[255:32]};

        
    always @(posedge clk)
        //
        if      (flag_store_lsb_c)                  c_lsb_s1_shreg <= {c_word_lower,     c_lsb_s1_shreg[255:32]};
        else if (flag_store_lsb_s1)                 c_lsb_s1_shreg <= {add47_r3_s_lower, c_lsb_s1_shreg[255:32]};
        else if (flag_store_pn)                     c_lsb_s1_shreg <= {sub32_pn_d,       c_lsb_s1_shreg[255:32]};
        else if (flag_store_msb_c || flag_shift_s1) c_lsb_s1_shreg <= {{32{1'b0}},       c_lsb_s1_shreg[255:32]};
        else if (flag_update_p_dout)                 c_lsb_s1_shreg <= {{32{1'b0}},       c_lsb_s1_shreg[255:32]};


    reg sel_pn; // 0: output in S2, 1: output in PN
    
    always @(posedge clk)
        //
        if (flag_select_s2_or_pn) sel_pn <= sub32_b_out & add47_s2_s[0];
        
        
    reg [31:0] p_dout_reg;
    
    assign p_dout = p_dout_reg;
    
    always @(posedge clk)
        //
        if (flag_update_p_dout) p_dout_reg <= sel_pn ? c_lsb_s1_shreg[31:0] : buf_a_wide[31:0];
        else                    p_dout_reg <= {32{1'bX}};
        

    reg p_wren_reg = 0;
    
    assign p_wren = p_wren_reg;
    
    always @(posedge clk)
        //
        p_wren_reg <= flag_update_p_dout;

        
    reg [WORD_COUNTER_WIDTH-1:0] p_addr_reg;
        
    assign p_addr = p_addr_reg;
        
    always @(posedge clk)
        //
        if (p_wren_reg) p_addr_reg <= WORD_INDEX_NEXT_OR_ZERO(p_addr_reg);
        else            p_addr_reg <= WORD_INDEX_ZERO;
    
        
endmodule


//------------------------------------------------------------------------------
// End-of-File
//------------------------------------------------------------------------------
