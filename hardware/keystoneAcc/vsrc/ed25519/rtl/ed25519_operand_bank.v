//======================================================================
//
// Copyright (c) 2015, NORDUnet A/S All rights reserved.
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

module ed25519_operand_bank
(
    input               clk,

    input   [ 9-1:0]    a_addr,
    input               a_wr,
    input   [32-1:0]    a_in,

    input   [ 9-1:0]    b_addr,
    output  [32-1:0]    b_out
);


    //
    // BRAM
    //
    reg [31:0] bram[0:64*8-1];


    //
    // Initialization
    //
    initial begin
        
	end


    //
    // Output Register
    //
    reg [32-1:0] bram_reg_b;

    assign b_out = bram_reg_b;


    //
    // Write Port A
    //
    always @(posedge clk)
        //
        if (a_wr) bram[a_addr] <= a_in;


    //
    // Read Port B
    //
    always @(posedge clk)
        //
        case (b_addr)
        //
        // CONST_ZERO 
        //
        9'd007: bram_reg_b <= 32'h00000000;
        9'd006: bram_reg_b <= 32'h00000000;
        9'd005: bram_reg_b <= 32'h00000000;
        9'd004: bram_reg_b <= 32'h00000000;
        9'd003: bram_reg_b <= 32'h00000000;
        9'd002: bram_reg_b <= 32'h00000000;
        9'd001: bram_reg_b <= 32'h00000000;
        9'd000: bram_reg_b <= 32'h00000000;
        //
        // CONST_ONE
        //
        9'd015: bram_reg_b <= 32'h00000000;
        9'd014: bram_reg_b <= 32'h00000000;
        9'd013: bram_reg_b <= 32'h00000000;
        9'd012: bram_reg_b <= 32'h00000000;
        9'd011: bram_reg_b <= 32'h00000000;
        9'd010: bram_reg_b <= 32'h00000000;
        9'd009: bram_reg_b <= 32'h00000000;
        9'd008: bram_reg_b <= 32'h00000001;
        //
        // G_X
        //
        9'd119: bram_reg_b <= 32'h216936d3;
        9'd118: bram_reg_b <= 32'hcd6e53fe;
        9'd117: bram_reg_b <= 32'hc0a4e231;
        9'd116: bram_reg_b <= 32'hfdd6dc5c;
        9'd115: bram_reg_b <= 32'h692cc760;
        9'd114: bram_reg_b <= 32'h9525a7b2;
        9'd113: bram_reg_b <= 32'hc9562d60;
        9'd112: bram_reg_b <= 32'h8f25d51a;
        //
        // G_Y
        //
        9'd127: bram_reg_b <= 32'h66666666;
        9'd126: bram_reg_b <= 32'h66666666;
        9'd125: bram_reg_b <= 32'h66666666;
        9'd124: bram_reg_b <= 32'h66666666;
        9'd123: bram_reg_b <= 32'h66666666;
        9'd122: bram_reg_b <= 32'h66666666;
        9'd121: bram_reg_b <= 32'h66666666;
        9'd120: bram_reg_b <= 32'h66666658;
        //
        // G_T
        //
        9'd135: bram_reg_b <= 32'h67875f0f;
        9'd134: bram_reg_b <= 32'hd78b7665;
        9'd133: bram_reg_b <= 32'h66ea4e8e;
        9'd132: bram_reg_b <= 32'h64abe37d;
        9'd131: bram_reg_b <= 32'h20f09f80;
        9'd130: bram_reg_b <= 32'h775152f5;
        9'd129: bram_reg_b <= 32'h6dde8ab3;
        9'd128: bram_reg_b <= 32'ha5b7dda3;
        //
        // default
        //
        default: bram_reg_b <= bram[b_addr];
        endcase


endmodule
