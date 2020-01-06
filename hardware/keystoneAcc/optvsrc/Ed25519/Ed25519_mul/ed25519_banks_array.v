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

module ed25519_banks_array (
    input			iClk,
    
    input			iBanks,		// 0: LO -> HI, 1: HI -> LO
    
    input	[5:0]	iSrc1_op,
    input	[5:0]	iSrc2_op,
    input	[5:0]	iDst_op,
    
    input	[2:0]	iSrc1_addr,
    input	[2:0]	iSrc2_addr,
    input	[2:0]	iDst_addr,
    
    input			iDst_wren,
    
    output	[31:0]	oSrc1,
    output	[31:0]	oSrc2,
    
    input	[31:0]	iDst
);

 // Banks
 wire	[31:0]	bank_lo1_dout;
 wire	[31:0]	bank_lo2_dout;
 wire	[31:0]	bank_hi1_dout;
 wire	[31:0]	bank_hi2_dout;

 assign oSrc1 = (iBanks) ? bank_hi1_dout : bank_lo1_dout;
 assign oSrc2 = (iBanks) ? bank_hi2_dout : bank_lo2_dout;

 ed25519_operand_bank bank_operand_lo1 (
	.iClk		(iClk),
	.iA_addr	({iDst_op, iDst_addr}),
	.iA_wr		(iDst_wren & iBanks),
	.iA			(iDst),
	.iB_addr	({iSrc1_op, iSrc1_addr}),
	.oB			(bank_lo1_dout)
 );

 ed25519_operand_bank bank_operand_lo2 (
	.iClk		(iClk),
	.iA_addr	({iDst_op, iDst_addr}),
	.iA_wr		(iDst_wren & iBanks),
	.iA			(iDst),
	.iB_addr	({iSrc2_op, iSrc2_addr}),
	.oB			(bank_lo2_dout)
 );

 ed25519_operand_bank bank_operand_hi1 (
	.iClk		(iClk),
	.iA_addr	({iDst_op, iDst_addr}),
	.iA_wr		(iDst_wren & ~iBanks),
	.iA			(iDst),
	.iB_addr	({iSrc1_op, iSrc1_addr}),
	.oB			(bank_hi1_dout)
 );

 ed25519_operand_bank bank_operand_hi2 (
	.iClk		(iClk),
	.iA_addr	({iDst_op, iDst_addr}),
	.iA_wr		(iDst_wren & ~iBanks),
	.iA			(iDst),
	.iB_addr	({iSrc2_op, iSrc2_addr}),
	.oB			(bank_hi2_dout)
 );
    
endmodule
