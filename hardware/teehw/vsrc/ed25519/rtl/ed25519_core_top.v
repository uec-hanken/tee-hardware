//======================================================================
//
// Copyright (c) 2018, NORDUnet A/S All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// - Redistributions of source code must retain the above copyright
//   notice, this list of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright
//   notice, this list of conditions and the following disclaimer in the
//   documentation and/or other materials provided with the distribution.
//
// - Neither the name of the NORDUnet nor the names of its contributors may
//   be used to endorse or promote products derived from this software
//   without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
// IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
// PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
// TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//======================================================================

module ed25519_core_top
(
    input    wire        clk,
    input    wire        rst_n,

    input    wire        next,
    output   wire        valid,

    input    wire        bus_cs,
    input    wire        bus_we,
    input    wire [ 3:0] bus_addr,
    input    wire [31:0] bus_data_wr,
    output   wire [31:0] bus_data_rd
);


    //
    // Memory Banks
    //
    localparam   [0:0] BUS_ADDR_BANK_K     = 1'b0;
    localparam   [0:0] BUS_ADDR_BANK_QY    = 1'b1;

    wire         [0:0] bus_addr_upper = bus_addr[3:3];
    wire         [2:0] bus_addr_lower = bus_addr[2:0];


    //
    // Memories
    //
    wire [31:0] user_bram_k_rw_dout;
    wire [31:0] user_bram_qy_ro_dout;

    wire [31:0] user_bram_k_rw_din;
    
    wire [ 2:0] core_bram_k_ro_addr;
    wire [ 2:0] core_bram_qy_rw_addr;

    wire        core_bram_qy_rw_wren;

    wire [31:0] core_bram_k_ro_dout;
//  wire [31:0] core_bram_qy_rw_dout_unused;
    
    wire [31:0] core_bram_qy_rw_din;

    assign user_bram_k_rw_din = bus_data_wr;
    assign user_bram_k_rw_wren = bus_cs && bus_we && (bus_addr_upper == BUS_ADDR_BANK_K);
    
    /* write-only memory here to prevent readback of the private key */
    bram_1wo_1ro_readfirst #
    (
        .MEM_WIDTH(32),
        .MEM_ADDR_BITS(3)
    )
    bram_k
    (   .clk(clk),
        .a_addr(bus_addr_lower),      .a_out(user_bram_k_rw_dout), .a_wr(user_bram_k_rw_wren), .a_in(user_bram_k_rw_din),
        .b_addr(core_bram_k_ro_addr), .b_out(core_bram_k_ro_dout)
    );

    /* read-write memory here */
    bram_1rw_1ro_readfirst #
    (
        .MEM_WIDTH(32),
        .MEM_ADDR_BITS(3)
    )
    bram_qy
    (
        .clk(clk),
        .a_addr(core_bram_qy_rw_addr), .a_out(                    ), .a_wr(core_bram_qy_rw_wren), .a_in(core_bram_qy_rw_din),
        .b_addr(bus_addr_lower),       .b_out(user_bram_qy_ro_dout)
    );


    //
    // Curve Base Point Multiplier
    //
    reg next_dly;
    always @(posedge clk) next_dly <= next;
    wire next_trig = next && !next_dly;

    ed25519_base_point_multiplier ed25519_base_point_multiplier_inst
    (
        .clk        (clk),
        .rst_n      (rst_n),

        .ena        (next_trig),
        .rdy        (valid),

        .k_addr     (core_bram_k_ro_addr),
        .qy_addr    (core_bram_qy_rw_addr),

        .qy_wren    (core_bram_qy_rw_wren),

        .k_din      (core_bram_k_ro_dout),
        .qy_dout    (core_bram_qy_rw_din)
    );


    //
    // Output Selector
    //
    reg [0:0] bus_addr_upper_dly;
    always @(posedge clk) bus_addr_upper_dly <= bus_addr_upper;

    reg [31: 0] bus_data_rd_mux;
    assign bus_data_rd = bus_data_rd_mux;

    always @(*)
        //
        case (bus_addr_upper_dly)
            //
            BUS_ADDR_BANK_K:    bus_data_rd_mux = user_bram_k_rw_dout;
            BUS_ADDR_BANK_QY:   bus_data_rd_mux = user_bram_qy_ro_dout;
            //
        endcase


endmodule
