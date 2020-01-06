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

module ed25519_operand_bank_wconst
(
    input               clk,

    input   [ 9-1:0]    a_addr,
    input               a_wr,
    input   [32-1:0]    a_in,

    input   [ 9-1:0]    b_addr,
    output  [32-1:0]    b_out
);
    wire    [32-1:0]    b_out_a;
    ed25519_operand_bank opbank(.clk(clk), .a_addr(a_addr), .a_wr(a_wr), .a_in(a_in), .b_addr(b_addr), .b_out(b_out_a));
    
    //
    // Read Port B
    //
    reg const;
    reg [32-1:0] bram_reg_b;
    assign b_out = const ? bram_reg_b : b_out_a;
    always @(posedge clk)
        //
        case (b_addr)
        //
        // CONST_ZERO 
        //
        9'd007: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd006: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd005: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd004: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd003: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd002: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd001: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd000: begin bram_reg_b <= 32'h00000000; const <= 1; end
        //
        // CONST_ONE
        //
        9'd015: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd014: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd013: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd012: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd011: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd010: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd009: begin bram_reg_b <= 32'h00000000; const <= 1; end
        9'd008: begin bram_reg_b <= 32'h00000001; const <= 1; end
        //
        // G_X
        //
        9'd119: begin bram_reg_b <= 32'h216936d3; const <= 1; end
        9'd118: begin bram_reg_b <= 32'hcd6e53fe; const <= 1; end
        9'd117: begin bram_reg_b <= 32'hc0a4e231; const <= 1; end
        9'd116: begin bram_reg_b <= 32'hfdd6dc5c; const <= 1; end
        9'd115: begin bram_reg_b <= 32'h692cc760; const <= 1; end
        9'd114: begin bram_reg_b <= 32'h9525a7b2; const <= 1; end
        9'd113: begin bram_reg_b <= 32'hc9562d60; const <= 1; end
        9'd112: begin bram_reg_b <= 32'h8f25d51a; const <= 1; end
        //
        // G_Y
        //
        9'd127: begin bram_reg_b <= 32'h66666666; const <= 1; end
        9'd126: begin bram_reg_b <= 32'h66666666; const <= 1; end
        9'd125: begin bram_reg_b <= 32'h66666666; const <= 1; end
        9'd124: begin bram_reg_b <= 32'h66666666; const <= 1; end
        9'd123: begin bram_reg_b <= 32'h66666666; const <= 1; end
        9'd122: begin bram_reg_b <= 32'h66666666; const <= 1; end
        9'd121: begin bram_reg_b <= 32'h66666666; const <= 1; end
        9'd120: begin bram_reg_b <= 32'h66666658; const <= 1; end
        //
        // G_T
        //
        9'd135: begin bram_reg_b <= 32'h67875f0f; const <= 1; end
        9'd134: begin bram_reg_b <= 32'hd78b7665; const <= 1; end
        9'd133: begin bram_reg_b <= 32'h66ea4e8e; const <= 1; end
        9'd132: begin bram_reg_b <= 32'h64abe37d; const <= 1; end
        9'd131: begin bram_reg_b <= 32'h20f09f80; const <= 1; end
        9'd130: begin bram_reg_b <= 32'h775152f5; const <= 1; end
        9'd129: begin bram_reg_b <= 32'h6dde8ab3; const <= 1; end
        9'd128: begin bram_reg_b <= 32'ha5b7dda3; const <= 1; end
        //
        // default
        //
        default: begin bram_reg_b <= bram[b_addr]; const <= 0; end
        endcase


endmodule
