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
        //
        // CONST_ZERO 
        //
        bram[ 0*8 + 7] = 32'h00000000;
        bram[ 0*8 + 6] = 32'h00000000;
        bram[ 0*8 + 5] = 32'h00000000;
        bram[ 0*8 + 4] = 32'h00000000;
        bram[ 0*8 + 3] = 32'h00000000;
        bram[ 0*8 + 2] = 32'h00000000;
        bram[ 0*8 + 1] = 32'h00000000;
        bram[ 0*8 + 0] = 32'h00000000;
        //
        // CONST_ONE
        //
        bram[ 1*8 + 7] = 32'h00000000;
        bram[ 1*8 + 6] = 32'h00000000;
        bram[ 1*8 + 5] = 32'h00000000;
        bram[ 1*8 + 4] = 32'h00000000;
        bram[ 1*8 + 3] = 32'h00000000;
        bram[ 1*8 + 2] = 32'h00000000;
        bram[ 1*8 + 1] = 32'h00000000;
        bram[ 1*8 + 0] = 32'h00000001;
        //
        // G_X
        //
        bram[14*8 + 7] = 32'h216936d3;
        bram[14*8 + 6] = 32'hcd6e53fe;
        bram[14*8 + 5] = 32'hc0a4e231;
        bram[14*8 + 4] = 32'hfdd6dc5c;
        bram[14*8 + 3] = 32'h692cc760;
        bram[14*8 + 2] = 32'h9525a7b2;
        bram[14*8 + 1] = 32'hc9562d60;
        bram[14*8 + 0] = 32'h8f25d51a;
        //
        // G_Y
        //
        bram[15*8 + 7] = 32'h66666666;
        bram[15*8 + 6] = 32'h66666666;
        bram[15*8 + 5] = 32'h66666666;
        bram[15*8 + 4] = 32'h66666666;
        bram[15*8 + 3] = 32'h66666666;
        bram[15*8 + 2] = 32'h66666666;
        bram[15*8 + 1] = 32'h66666666;
        bram[15*8 + 0] = 32'h66666658;
        //
        // G_T
        //
        bram[16*8 + 7] = 32'h67875f0f;
        bram[16*8 + 6] = 32'hd78b7665;
        bram[16*8 + 5] = 32'h66ea4e8e;
        bram[16*8 + 4] = 32'h64abe37d;
        bram[16*8 + 3] = 32'h20f09f80;
        bram[16*8 + 2] = 32'h775152f5;
        bram[16*8 + 1] = 32'h6dde8ab3;
        bram[16*8 + 0] = 32'ha5b7dda3;
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
        bram_reg_b <= bram[b_addr];


endmodule
