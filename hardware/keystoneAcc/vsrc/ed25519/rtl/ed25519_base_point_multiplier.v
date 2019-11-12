//------------------------------------------------------------------------------
//
// ed25519_base_point_multiplier.v
// -----------------------------------------------------------------------------
// Ed25519 base point scalar multiplier.
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

module ed25519_base_point_multiplier
(
    clk, rst_n,
    ena, rdy,
    k_addr, qy_addr,
    qy_wren,
    k_din,
    qy_dout
);


    //
    // Microcode Header
    //
`include "ed25519_uop.vh"
    

    //
    // Ports
    //
    input           clk;        // system clock
    input           rst_n;      // active-low async reset

    input           ena;        // enable input
    output          rdy;        // ready output

    output  [ 2:0]  k_addr;     //
    output  [ 2:0]  qy_addr;    //
    output          qy_wren;    //
    input   [31:0]  k_din;      //
    output  [31:0]  qy_dout;    //


    //
    // FSM
    //
    localparam [4:0] FSM_STATE_IDLE                 = 5'd00;
    localparam [4:0] FSM_STATE_PREPARE_TRIG         = 5'd01;
    localparam [4:0] FSM_STATE_PREPARE_WAIT         = 5'd02;
    localparam [4:0] FSM_STATE_BEFORE_ROUND_TRIG    = 5'd03;
    localparam [4:0] FSM_STATE_BEFORE_ROUND_WAIT    = 5'd04;
    localparam [4:0] FSM_STATE_DURING_ROUND_TRIG    = 5'd05;
    localparam [4:0] FSM_STATE_DURING_ROUND_WAIT    = 5'd06;
    localparam [4:0] FSM_STATE_AFTER_ROUND_TRIG     = 5'd07;
    localparam [4:0] FSM_STATE_AFTER_ROUND_WAIT     = 5'd08;
    localparam [4:0] FSM_STATE_BEFORE_INVERT_TRIG   = 5'd09;
    localparam [4:0] FSM_STATE_BEFORE_INVERT_WAIT   = 5'd10;
    localparam [4:0] FSM_STATE_DURING_INVERT_TRIG   = 5'd11;
    localparam [4:0] FSM_STATE_DURING_INVERT_WAIT   = 5'd12;
    localparam [4:0] FSM_STATE_AFTER_INVERT_TRIG    = 5'd13;
    localparam [4:0] FSM_STATE_AFTER_INVERT_WAIT    = 5'd14;
    localparam [4:0] FSM_STATE_FINAL_REDUCE_TRIG    = 5'd15;
    localparam [4:0] FSM_STATE_FINAL_REDUCE_WAIT    = 5'd16;
    localparam [4:0] FSM_STATE_HANDLE_SIGN_TRIG     = 5'd17;
    localparam [4:0] FSM_STATE_HANDLE_SIGN_WAIT     = 5'd18;
    localparam [4:0] FSM_STATE_OUTPUT_TRIG          = 5'd19;
    localparam [4:0] FSM_STATE_OUTPUT_WAIT          = 5'd20;
    localparam [4:0] FSM_STATE_DONE                 = 5'd31;

    reg [4:0] fsm_state = FSM_STATE_IDLE;
    reg [4:0] fsm_state_next;


    //
    // Round Counter
    //
    reg  [7:0] bit_counter;
    wire [7:0] bit_counter_max  = 8'hFF;    // 255
    wire [7:0] bit_counter_zero = 8'h00;    // 0
    wire [7:0] bit_counter_next =
        (bit_counter < bit_counter_max) ? bit_counter + 1'b1 : bit_counter_zero;

    assign k_addr = bit_counter[7:5];


    //
    // Worker Trigger Logic
    //
    reg  worker_trig = 1'b0;
    wire worker_done;

    wire fsm_wait_done = !worker_trig && worker_done;

    always @(posedge clk or negedge rst_n)
        //
        if (rst_n == 1'b0)                  worker_trig <= 1'b0;
        else case (fsm_state)
            FSM_STATE_PREPARE_TRIG,
            FSM_STATE_BEFORE_ROUND_TRIG,
            FSM_STATE_DURING_ROUND_TRIG,
            FSM_STATE_AFTER_ROUND_TRIG,
            FSM_STATE_BEFORE_INVERT_TRIG,
            FSM_STATE_DURING_INVERT_TRIG,
            FSM_STATE_AFTER_INVERT_TRIG,
            FSM_STATE_FINAL_REDUCE_TRIG,
            FSM_STATE_HANDLE_SIGN_TRIG,
            FSM_STATE_OUTPUT_TRIG:          worker_trig <= 1'b1;
            default:                        worker_trig <= 1'b0;
        endcase
        
        
    //
    // Round Counter Increment Logic
    //
    always @(posedge clk)
        //
        case (fsm_state_next)
            FSM_STATE_PREPARE_TRIG:         bit_counter <= bit_counter_zero;
            FSM_STATE_AFTER_ROUND_TRIG:     bit_counter <= bit_counter_next;
            default:                        bit_counter <= bit_counter;
        endcase


    //
    // Final Round Detection Logic
    //
    wire [ 3: 0] fsm_state_after_round = (bit_counter != bit_counter_zero) ?
        FSM_STATE_BEFORE_ROUND_TRIG : FSM_STATE_BEFORE_INVERT_TRIG;
        

    //
    // K Latch
    //
    reg [31:0] k_din_shreg;
    
    wire [4:0] k_bit_index = bit_counter[4:0];
    
    always @(posedge clk)
        //
        if (fsm_state_next == FSM_STATE_BEFORE_ROUND_TRIG)
            //
            if (k_bit_index == 5'd0)
                //
                case (k_addr)
                    3'd0:       k_din_shreg <= {k_din[31:3], 3'b000};
                    3'd7:       k_din_shreg <= {2'b01, k_din[29:0]};
                    default:    k_din_shreg <= k_din;
                endcase
                //
            else                k_din_shreg <= {k_din_shreg[0], k_din_shreg[31:1]};
    

    //
    // Worker Offset Logic
    //
    reg [UOP_ADDR_WIDTH-1:0] worker_offset;
    
    always @(posedge clk)
        //
        case (fsm_state)
            FSM_STATE_PREPARE_TRIG:         worker_offset <= UOP_OFFSET_PREPARE;
            FSM_STATE_BEFORE_ROUND_TRIG:    worker_offset <= k_din_shreg[0] ? UOP_OFFSET_BEFORE_ROUND_K1 : UOP_OFFSET_BEFORE_ROUND_K0;
            FSM_STATE_DURING_ROUND_TRIG:    worker_offset <= UOP_OFFSET_DURING_ROUND;
            FSM_STATE_AFTER_ROUND_TRIG:     worker_offset <= k_din_shreg[0] ? UOP_OFFSET_AFTER_ROUND_K1 : UOP_OFFSET_AFTER_ROUND_K0;
            FSM_STATE_BEFORE_INVERT_TRIG:   worker_offset <= UOP_OFFSET_BEFORE_INVERSION;
            FSM_STATE_DURING_INVERT_TRIG:   worker_offset <= UOP_OFFSET_DURING_INVERSION;
            FSM_STATE_AFTER_INVERT_TRIG:    worker_offset <= UOP_OFFSET_AFTER_INVERSION;
            FSM_STATE_FINAL_REDUCE_TRIG:    worker_offset <= UOP_OFFSET_FINAL_REDUCTION;
            FSM_STATE_HANDLE_SIGN_TRIG:     worker_offset <= UOP_OFFSET_HANDLE_SIGN;
            FSM_STATE_OUTPUT_TRIG:          worker_offset <= UOP_OFFSET_OUTPUT;
            default:                        worker_offset <= {UOP_ADDR_WIDTH{1'bX}};
        endcase
        
    
    //
    // FSM Process
    //
    always @(posedge clk or negedge rst_n)
        //
        if (rst_n == 1'b0)  fsm_state <= FSM_STATE_IDLE;
        else                fsm_state <= fsm_state_next;


    //
    // FSM Transition Logic
    //
    always @* begin
        //
        fsm_state_next = FSM_STATE_IDLE;
        //
        case (fsm_state)

            FSM_STATE_IDLE:                 fsm_state_next = ena ? FSM_STATE_PREPARE_TRIG : FSM_STATE_IDLE;

            FSM_STATE_PREPARE_TRIG:         fsm_state_next = FSM_STATE_PREPARE_WAIT;
            FSM_STATE_PREPARE_WAIT:         fsm_state_next = fsm_wait_done ? FSM_STATE_BEFORE_ROUND_TRIG : FSM_STATE_PREPARE_WAIT;

            FSM_STATE_BEFORE_ROUND_TRIG:    fsm_state_next = FSM_STATE_BEFORE_ROUND_WAIT;
            FSM_STATE_BEFORE_ROUND_WAIT:    fsm_state_next = fsm_wait_done ? FSM_STATE_DURING_ROUND_TRIG : FSM_STATE_BEFORE_ROUND_WAIT;

            FSM_STATE_DURING_ROUND_TRIG:    fsm_state_next = FSM_STATE_DURING_ROUND_WAIT;
            FSM_STATE_DURING_ROUND_WAIT:    fsm_state_next = fsm_wait_done ? FSM_STATE_AFTER_ROUND_TRIG : FSM_STATE_DURING_ROUND_WAIT;

            FSM_STATE_AFTER_ROUND_TRIG:     fsm_state_next = FSM_STATE_AFTER_ROUND_WAIT;
            FSM_STATE_AFTER_ROUND_WAIT:     fsm_state_next = fsm_wait_done ? fsm_state_after_round : FSM_STATE_AFTER_ROUND_WAIT;

            FSM_STATE_BEFORE_INVERT_TRIG:   fsm_state_next = FSM_STATE_BEFORE_INVERT_WAIT;
            FSM_STATE_BEFORE_INVERT_WAIT:   fsm_state_next = fsm_wait_done ? FSM_STATE_DURING_INVERT_TRIG : FSM_STATE_BEFORE_INVERT_WAIT;
            
            FSM_STATE_DURING_INVERT_TRIG:   fsm_state_next = FSM_STATE_DURING_INVERT_WAIT;
            FSM_STATE_DURING_INVERT_WAIT:   fsm_state_next = fsm_wait_done ? FSM_STATE_AFTER_INVERT_TRIG : FSM_STATE_DURING_INVERT_WAIT;
            
            FSM_STATE_AFTER_INVERT_TRIG:    fsm_state_next = FSM_STATE_AFTER_INVERT_WAIT;
            FSM_STATE_AFTER_INVERT_WAIT:    fsm_state_next = fsm_wait_done ? FSM_STATE_FINAL_REDUCE_TRIG : FSM_STATE_AFTER_INVERT_WAIT;
            
            FSM_STATE_FINAL_REDUCE_TRIG:    fsm_state_next = FSM_STATE_FINAL_REDUCE_WAIT;
            FSM_STATE_FINAL_REDUCE_WAIT:    fsm_state_next = fsm_wait_done ? FSM_STATE_HANDLE_SIGN_TRIG : FSM_STATE_FINAL_REDUCE_WAIT;
            
            FSM_STATE_HANDLE_SIGN_TRIG:     fsm_state_next = FSM_STATE_HANDLE_SIGN_WAIT;
            FSM_STATE_HANDLE_SIGN_WAIT:     fsm_state_next = fsm_wait_done ? FSM_STATE_OUTPUT_TRIG : FSM_STATE_HANDLE_SIGN_WAIT;

            FSM_STATE_OUTPUT_TRIG:          fsm_state_next = FSM_STATE_OUTPUT_WAIT;
            FSM_STATE_OUTPUT_WAIT:          fsm_state_next = fsm_wait_done ? FSM_STATE_DONE : FSM_STATE_OUTPUT_WAIT;

            FSM_STATE_DONE:                 fsm_state_next = FSM_STATE_IDLE;
            default:                        fsm_state_next = FSM_STATE_IDLE;

        endcase
        //
    end



    //
    // Worker
    //
    
    wire worker_final_reduce = fsm_state == FSM_STATE_FINAL_REDUCE_WAIT;
    wire worker_handle_sign  = fsm_state == FSM_STATE_HANDLE_SIGN_WAIT;
    wire worker_output_now   = fsm_state == FSM_STATE_OUTPUT_WAIT;
    
    ed25519_uop_worker uop_worker
    (
        .clk        (clk),
        .rst_n      (rst_n),
		  
        .ena            (worker_trig),
        .rdy            (worker_done),
        .uop_offset     (worker_offset),
        .final_reduce   (worker_final_reduce),
        .handle_sign    (worker_handle_sign),
        .output_now     (worker_output_now),
        
        .y_addr         (qy_addr),
        .y_dout         (qy_dout),
        .y_wren         (qy_wren)
    );


    //
    // Ready Flag Logic
    //
    reg rdy_reg = 1'b1;
    assign rdy = rdy_reg;

    always @(posedge clk or negedge rst_n)
        //
        if (rst_n == 1'b0)              rdy_reg <= 1'b1;
        else case (fsm_state)
            FSM_STATE_IDLE: if (ena)    rdy_reg <= 1'b0;
            FSM_STATE_DONE:             rdy_reg <= 1'b1;
            default:                    rdy_reg <= rdy_reg;
        endcase


endmodule


//------------------------------------------------------------------------------
// End-of-File
//------------------------------------------------------------------------------
