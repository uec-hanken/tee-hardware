//======================================================================
//
// aes_core.v
// ----------
// The AES core. This core supports key size of 128, and 256 bits.
// Most of the functionality is within the submodules.
//
//
// Author: Joachim Strombergson
// Copyright (c) 2013, 2014, Secworks Sweden AB
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or
// without modification, are permitted provided that the following
// conditions are met:
//
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in
//    the documentation and/or other materials provided with the
//    distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//======================================================================

module aes_core(
	input			iClk,
	input			iRstn,
	
	input			iEncdec,
	input			iInit,
	input			iNext,
	output			oReady,
	
	input	[255:0]	iKey,
	input			iKeylen,
	
	input	[127:0]	iBlock,
	output	[127:0]	oResult,
	output			oResult_valid
);
 
 //----------------------------------------------------------------
 // Registers including update variables and write enable.
 //----------------------------------------------------------------
 reg	[1:0]	aes_core_ctrl_reg;
 wire	[1:0]	aes_core_ctrl_new;
 wire			aes_core_ctrl_we;

 reg			result_valid_reg;
 wire			result_valid_new;
 wire			result_valid_we;

 reg			ready_reg;
 wire			ready_new;
 wire			ready_we;

 //----------------------------------------------------------------
 // Wires.
 //----------------------------------------------------------------
 wire			State0, State1, State2;
 wire			init_state;

 wire	[127:0]	round_key;
 wire			key_ready;

 wire			enc_next;
 wire	[3:0]	enc_round_nr;
 wire	[127:0]	enc_new_block;
 wire			enc_ready;
 wire	[31:0]	enc_sboxw;

 wire			dec_next;
 wire	[3:0]	dec_round_nr;
 wire	[127:0]	dec_new_block;
 wire			dec_ready;

 wire	[127:0]	muxed_new_block;
 wire	[3:0]	muxed_round_nr;
 wire			muxed_ready;

 wire	[31:0]	keymem_sboxw;

 wire	[31:0]	muxed_sboxw;
 wire	[31:0]	new_sboxw;
 
 assign State0 = ~aes_core_ctrl_reg[1] & ~aes_core_ctrl_reg[0];
 assign State1 = ~aes_core_ctrl_reg[1] &  aes_core_ctrl_reg[0];
 assign State2 =  aes_core_ctrl_reg[1] & ~aes_core_ctrl_reg[0];

 //----------------------------------------------------------------
 // Instantiations.
 //----------------------------------------------------------------
 aes_encipher_block enc_block(
	.iClk		(iClk),
	.iRstn		(iRstn),
	
	.iNext		(enc_next),
	
	.iKeylen	(iKeylen),
	.oRound		(enc_round_nr),
	.iRound_key	(round_key),
	
	.oSboxw		(enc_sboxw),
	.iNew_sboxw	(new_sboxw),
	
	.iBlock		(iBlock),
	.oNew_block	(enc_new_block),
	.oReady		(enc_ready)
 );

 aes_decipher_block dec_block(
	.iClk		(iClk),
	.iRstn		(iRstn),
	
	.iNext		(dec_next),
	
	.iKeylen	(iKeylen),
	.oRound		(dec_round_nr),
	.iRound_key	(round_key),
	
	.iBlock		(iBlock),
	.oNew_block	(dec_new_block),
	.oReady		(dec_ready)
 );

 aes_key_mem keymem(
	.iClk		(iClk),
	.iRstn		(iRstn),
	
	.iKey		(iKey),
	.iKeylen	(iKeylen),
	.iInit		(iInit),
	
	.iRound		(muxed_round_nr),
	.oRound_key	(round_key),
	.oReady		(key_ready),
	
	.oSboxw		(keymem_sboxw),
	.iNew_sboxw	(new_sboxw)
 );

 aes_sbox sbox_inst(
	.in		(muxed_sboxw),
	.out	(new_sboxw)
 );

 //----------------------------------------------------------------
 // Concurrent connectivity for ports etc.
 //----------------------------------------------------------------
 assign oReady        = ready_reg;
 assign oResult       = muxed_new_block;
 assign oResult_valid = result_valid_reg;

 //----------------------------------------------------------------
 // reg_update
 //
 // Update functionality for all registers in the core.
 // All registers are positive edge triggered with asynchronous
 // active low reset. All registers have write enable.
 //----------------------------------------------------------------
 always@(posedge iClk) begin
	if(~iRstn)					result_valid_reg  <= 1'b0;
	else if(result_valid_we)	result_valid_reg <= result_valid_new;
	else						result_valid_reg <= result_valid_reg;
 end

 always@(posedge iClk) begin
	if(~iRstn) 			ready_reg <= 1'b1;
	else if(ready_we)	ready_reg <= ready_new;
	else				ready_reg <= ready_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)					aes_core_ctrl_reg <= 2'd0;
	else if(aes_core_ctrl_we)	aes_core_ctrl_reg <= aes_core_ctrl_new;
	else						aes_core_ctrl_reg <= aes_core_ctrl_reg;
 end

 //----------------------------------------------------------------
 // sbox_mux
 //
 // Controls which of the encipher datapath or the key memory
 // that gets access to the sbox.
 //----------------------------------------------------------------
 assign muxed_sboxw = (init_state) ? keymem_sboxw : enc_sboxw;

 //----------------------------------------------------------------
 // encdex_mux
 //
 // Controls which of the datapaths that get the iNext signal, have
 // access to the memory as well as the block processing result.
 //----------------------------------------------------------------
 assign enc_next = iEncdec & iNext;
 assign dec_next = ~iEncdec & iNext;
 assign muxed_round_nr = (iEncdec) ? enc_round_nr : dec_round_nr;
 assign muxed_new_block = (iEncdec) ? enc_new_block : dec_new_block;
 assign muxed_ready = (iEncdec) ? enc_ready : dec_ready;

 //----------------------------------------------------------------
 // aes_core_ctrl
 //
 // Control FSM for aes core. Basically tracks if we are in
 // key init, encipher or decipher modes and connects the
 // different submodules to shared resources and interface ports.
 //----------------------------------------------------------------
 /*
 always@(*) begin
	init_state        = 1'b0;
	ready_new         = 1'b0;
	ready_we          = 1'b0;
	result_valid_new  = 1'b0;
	result_valid_we   = 1'b0;
	aes_core_ctrl_new = 2'd0;
	aes_core_ctrl_we  = 1'b0;
	
	case(aes_core_ctrl_reg)
		2'd0: begin
			if(iInit) begin
				init_state        = 1'b1;
				ready_new         = 1'b0;
				ready_we          = 1'b1;
				result_valid_new  = 1'b0;
				result_valid_we   = 1'b1;
				aes_core_ctrl_new = 2'd1;
				aes_core_ctrl_we  = 1'b1;
			end
			else if(iNext) begin
				init_state        = 1'b0;
				ready_new         = 1'b0;
				ready_we          = 1'b1;
				result_valid_new  = 1'b0;
				result_valid_we   = 1'b1;
				aes_core_ctrl_new = 2'd2;
				aes_core_ctrl_we  = 1'b1;
			end
		end
		2'd1: begin
			init_state = 1'b1;
			result_valid_new = 1'b0;
			result_valid_we = 1'b0;
			if(key_ready) begin
				ready_new         = 1'b1;
				ready_we          = 1'b1;
				aes_core_ctrl_new = 2'd0;
				aes_core_ctrl_we  = 1'b1;
			end
		end
		2'd2: begin
			init_state = 1'b0;
			if(muxed_ready) begin
				ready_new         = 1'b1;
				ready_we          = 1'b1;
				result_valid_new  = 1'b1;
				result_valid_we   = 1'b1;
				aes_core_ctrl_new = 2'd0;
				aes_core_ctrl_we  = 1'b1;
			end
		end
	endcase
 end
 */
 
 /*
 always@(*) begin
	init_state = 1'b0;
	case(aes_core_ctrl_reg)
		2'd0: begin
			if(iInit)	init_state = 1'b1;
			else		init_state = 1'b0;
		end
		2'd1: begin
			init_state = 1'b1;
		end
		2'd2: begin
			init_state = 1'b0;
		end
	endcase
 end
 */
 assign init_state = (State0 & iInit) | State1;
 
 /*
 always@(*) begin
	ready_new = 1'b0;
	case(aes_core_ctrl_reg)
		2'd0: begin
			ready_new = 1'b0;
		end
		2'd1: begin
			if(key_ready) 	ready_new = 1'b1;
			else			ready_new = 1'b0;
		end
		2'd2: begin
			if(muxed_ready) ready_new = 1'b1;
			else			ready_new = 1'b0;
		end
	endcase
 end
 */
 assign ready_new = (State1 & key_ready) | (State2 & muxed_ready);
 
 /*
 always@(*) begin
	ready_we = 1'b0;
	case(aes_core_ctrl_reg)
		2'd0: begin
			if(iInit|iNext) 	ready_we = 1'b1;
			else				ready_we = 1'b0;
		end
		2'd1: begin
			if(key_ready) 	ready_we = 1'b1;
			else			ready_we = 1'b0;
		end
		2'd2: begin
			if(muxed_ready) ready_we = 1'b1;
			else			ready_we = 1'b0;
		end
	endcase
 end
 */
 assign ready_we = (State0 & (iInit|iNext)) | (State1 & key_ready) | (State2 & muxed_ready);
 
 /*
 always@(*) begin
	result_valid_new = 1'b0;
	case(aes_core_ctrl_reg)
		2'd0: begin
			result_valid_new = 1'b0;
		end
		2'd1: begin
			result_valid_new = 1'b0;
		end
		2'd2: begin
			if(muxed_ready) result_valid_new = 1'b1;
			else			result_valid_new = 1'b0;
		end
	endcase
 end
 */
 assign result_valid_new = State2 & muxed_ready;
 
 /*
 always@(*) begin
	result_valid_we = 1'b0;
	case(aes_core_ctrl_reg)
		2'd0: begin
			if(iInit|iNext) 	result_valid_we = 1'b1;
			else				result_valid_we = 1'b0;
		end
		2'd1: begin
			result_valid_we = 1'b0;
		end
		2'd2: begin
			if(muxed_ready)	result_valid_we = 1'b1;
			else			result_valid_we = 1'b0;
		end
	endcase
 end
 */
 assign result_valid_we = (State0 & (iInit|iNext)) | (State2 & muxed_ready);
 
 /*
 always@(*) begin
	aes_core_ctrl_new = 2'd0;
	case(aes_core_ctrl_reg)
		2'd0: begin
			if(iInit) 		aes_core_ctrl_new = 2'd1;
			else if(iNext) 	aes_core_ctrl_new = 2'd2;
			else			aes_core_ctrl_new = 2'd0;
		end
		2'd1: begin
			aes_core_ctrl_new = 2'd0;
		end
		2'd2: begin
			aes_core_ctrl_new = 2'd0;
		end
	endcase
 end
 */
 assign aes_core_ctrl_new[0] = State0 & iInit;
 assign aes_core_ctrl_new[1] = State0 & ~iInit & iNext;
 
 /*
 always@(*) begin
	aes_core_ctrl_we = 1'b0;
	case(aes_core_ctrl_reg)
		2'd0: begin
			if(iInit|iNext) 	aes_core_ctrl_we = 1'b1;
			else				aes_core_ctrl_we = 1'b0;
		end
		2'd1: begin
			if(key_ready) 	aes_core_ctrl_we = 1'b1;
			else			aes_core_ctrl_we = 1'b0;
		end
		2'd2: begin
			if(muxed_ready) aes_core_ctrl_we = 1'b1;
			else			aes_core_ctrl_we = 1'b0;
		end
	endcase
 end
 */
 assign aes_core_ctrl_we = (State0 & (iInit|iNext)) | (State1 & key_ready) | (State2 & muxed_ready);
 
endmodule
