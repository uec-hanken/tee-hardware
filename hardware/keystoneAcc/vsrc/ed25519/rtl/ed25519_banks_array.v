//------------------------------------------------------------------------------
//
// ed25519_banks_array.v
// -----------------------------------------------------------------------------
// Ed25519 Operand Banks Array
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

module ed25519_banks_array
(
    input           clk,
    
    input           banks,  // 0: LO -> HI, 1: HI -> LO
    
    input   [ 5:0]  src1_operand,
    input   [ 5:0]  src2_operand,
    input   [ 5:0]  dst_operand,
    
    input   [ 2:0]  src1_addr,
    input   [ 2:0]  src2_addr,
    input   [ 2:0]  dst_addr,
    
    input           dst_wren,
    
    output  [31:0]  src1_dout,
    output  [31:0]  src2_dout,
    
    input   [31:0]  dst_din
);


    //
    // Banks
    //
    wire [31:0] bank_lo1_dout;
    wire [31:0] bank_lo2_dout;
    wire [31:0] bank_hi1_dout;
    wire [31:0] bank_hi2_dout;
    
    assign src1_dout = !banks ? bank_lo1_dout : bank_hi1_dout;
    assign src2_dout = !banks ? bank_lo2_dout : bank_hi2_dout;
    
    ed25519_operand_bank bank_operand_lo1
    (
        .clk     (clk),
        .a_addr  ({dst_operand, dst_addr}),
        .a_wr    (dst_wren & banks),
        .a_in    (dst_din),
        .b_addr  ({src1_operand, src1_addr}),
        .b_out   (bank_lo1_dout)
    );
    
    ed25519_operand_bank bank_operand_lo2
    (
        .clk     (clk),
        .a_addr  ({dst_operand, dst_addr}),
        .a_wr    (dst_wren & banks),
        .a_in    (dst_din),
        .b_addr  ({src2_operand, src2_addr}),
        .b_out   (bank_lo2_dout)
    );

    ed25519_operand_bank bank_operand_hi1
    (
        .clk     (clk),
        .a_addr  ({dst_operand, dst_addr}),
        .a_wr    (dst_wren & ~banks),
        .a_in    (dst_din),
        .b_addr  ({src1_operand, src1_addr}),
        .b_out   (bank_hi1_dout)
    );

    ed25519_operand_bank bank_operand_hi2
    (
        .clk     (clk),
        .a_addr  ({dst_operand, dst_addr}),
        .a_wr    (dst_wren & ~banks),
        .a_in    (dst_din),
        .b_addr  ({src2_operand, src2_addr}),
        .b_out   (bank_hi2_dout)
    );

    
endmodule


//------------------------------------------------------------------------------
// End-of-File
//------------------------------------------------------------------------------
