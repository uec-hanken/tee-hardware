//======================================================================
//
// aes_encipher_block.v
// --------------------
// The AES encipher round. A pure combinational module that implements
// the initial round, main round and final round logic for
// enciper operations.
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

module aes_encipher_block(
	input			iClk,
	input			iRstn,
	
	input			iNext,
	
	input			iKeylen,
	output	[3:0]	oRound,
	input	[127:0]	iRound_key,
	
	output	[31:0]	oSboxw,
	input	[31:0]	iNew_sboxw,
	
	input	[127:0]	iBlock,
	output	[127:0]	oNew_block,
	output			oReady
);

 //----------------------------------------------------------------
 // Registers including update variables and write enable.
 //----------------------------------------------------------------
 reg	[1:0]	sword_ctr_reg;
 wire	[1:0]	sword_ctr_new;
 wire			sword_ctr_we;
 wire			sword_ctr_inc;
 wire			sword_ctr_rst;

 reg	[3:0]	round_ctr_reg;
 wire	[3:0]	round_ctr_new;
 wire			round_ctr_we;
 wire			round_ctr_rst;
 wire			round_ctr_inc;

 wire	[127:0]	block_new;
 reg	[31:0]	block_w0_reg;
 reg	[31:0]	block_w1_reg;
 reg	[31:0]	block_w2_reg;
 reg	[31:0]	block_w3_reg;
 wire			block_w0_we;
 wire			block_w1_we;
 wire			block_w2_we;
 wire			block_w3_we;

 reg			ready_reg;
 wire			ready_new;
 wire			ready_we;

 reg	[1:0]	enc_ctrl_reg;
 wire	[1:0]	enc_ctrl_new;
 wire			enc_ctrl_we;

 wire	[2:0]	update_type;
 wire	[31:0]	muxed_sboxw;
 wire	[3:0]	num_rounds;
 
 wire			sword_ctr0, sword_ctr1, sword_ctr2, sword_ctr3;
 wire			enc_ctrl0, enc_ctrl1, enc_ctrl2, enc_ctrl3;
 //wire			update_type0;
 wire			update_type1, update_type2, update_type3, update_type4;
 wire			round_ctr_reg_num_rounds;
 
 //----------------------------------------------------------------
 // Concurrent connectivity for ports etc.
 //----------------------------------------------------------------
 assign oRound     = round_ctr_reg;
 assign oSboxw     = muxed_sboxw;
 assign oNew_block = {block_w0_reg, block_w1_reg, block_w2_reg, block_w3_reg};
 assign oReady     = ready_reg;
 
 /*
 always@(*) begin
	if(iKeylen)	num_rounds = 4'he;	//AES256_ROUNDS
	else		num_rounds = 4'ha;	//AES128_ROUNDS
 end
 */
 assign num_rounds[3] = 1'b1;
 assign num_rounds[2] = iKeylen;
 assign num_rounds[1] = 1'b1;
 assign num_rounds[0] = 1'b0;
 
 assign sword_ctr0 = ~sword_ctr_reg[1] & ~sword_ctr_reg[0];
 assign sword_ctr1 = ~sword_ctr_reg[1] &  sword_ctr_reg[0];
 assign sword_ctr2 =  sword_ctr_reg[1] & ~sword_ctr_reg[0];
 assign sword_ctr3 =  sword_ctr_reg[1] &  sword_ctr_reg[0];
 
 assign enc_ctrl0 = ~enc_ctrl_reg[1] & ~enc_ctrl_reg[0];
 assign enc_ctrl1 = ~enc_ctrl_reg[1] &  enc_ctrl_reg[0];
 assign enc_ctrl2 =  enc_ctrl_reg[1] & ~enc_ctrl_reg[0];
 assign enc_ctrl3 =  enc_ctrl_reg[1] &  enc_ctrl_reg[0];
 
 //assign update_type0 = ~update_type[2] & ~update_type[1] & ~update_type[0];
 assign update_type1 = ~update_type[2] & ~update_type[1] &  update_type[0];
 assign update_type2 = ~update_type[2] &  update_type[1] & ~update_type[0];
 assign update_type3 = ~update_type[2] &  update_type[1] &  update_type[0];
 assign update_type4 =  update_type[2] & ~update_type[1] & ~update_type[0];

 assign round_ctr_reg_num_rounds = (round_ctr_reg < num_rounds);
 
 //----------------------------------------------------------------
 // reg_update
 //
 // Update functionality for all registers in the core.
 // All registers are positive edge triggered with asynchronous
 // active low reset. All registers have write enable.
 //----------------------------------------------------------------
 /*
 always@(posedge iClk or negedge iRstn) begin
	if(~iRstn) begin
		block_w0_reg  <= 32'h0;
		block_w1_reg  <= 32'h0;
		block_w2_reg  <= 32'h0;
		block_w3_reg  <= 32'h0;
		sword_ctr_reg <= 2'h0;
		round_ctr_reg <= 4'h0;
		ready_reg     <= 1'b1;
		enc_ctrl_reg  <= 2'd0;
	end
	else begin
		if(block_w0_we)		block_w0_reg <= block_new[127:96];
		if(block_w1_we)		block_w1_reg <= block_new[95:64];
		if(block_w2_we)		block_w2_reg <= block_new[63:32];
		if(block_w3_we)		block_w3_reg <= block_new[31:0];
		if(sword_ctr_we)	sword_ctr_reg <= sword_ctr_new;
		if(round_ctr_we)	round_ctr_reg <= round_ctr_new;
		if(ready_we)		ready_reg <= ready_new;
		if(enc_ctrl_we)		enc_ctrl_reg <= enc_ctrl_new;
	end
 end // reg_update
 */
 always@(posedge iClk) begin
	if(~iRstn)			block_w0_reg <= 32'b0;
	else if(block_w0_we)	block_w0_reg <= block_new[127:96];
	else					block_w0_reg <= block_w0_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)			block_w1_reg <= 32'b0;
	else if(block_w1_we)	block_w1_reg <= block_new[95:64];
	else					block_w1_reg <= block_w1_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)			block_w2_reg <= 32'b0;
	else if(block_w2_we)	block_w2_reg <= block_new[63:32];
	else					block_w2_reg <= block_w2_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)			block_w3_reg <= 32'b0;
	else if(block_w3_we)	block_w3_reg <= block_new[31:0];
	else					block_w3_reg <= block_w3_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)			sword_ctr_reg <= 2'b0;
	else if(sword_ctr_we)	sword_ctr_reg <= sword_ctr_new;
	else					sword_ctr_reg <= sword_ctr_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)			round_ctr_reg <= 4'b0;
	else if(round_ctr_we)	round_ctr_reg <= round_ctr_new;
	else					round_ctr_reg <= round_ctr_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)		ready_reg <= 1'b1;
	else if(ready_we)	ready_reg <= ready_new;
	else				ready_reg <= ready_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)			enc_ctrl_reg <= 2'd0;
	else if(enc_ctrl_we)	enc_ctrl_reg <= enc_ctrl_new;
	else					enc_ctrl_reg <= enc_ctrl_reg;
 end
 
 //----------------------------------------------------------------
 // round_logic
 //
 // The logic needed to implement init, main and final rounds.
 //----------------------------------------------------------------
 wire [127:0] mixcolumns_shiftrows_block;
 wire [127:0] shiftrows_block;
 assign shiftrows_block = {block_w0_reg[31:24], block_w1_reg[23:16], block_w2_reg[15:8], block_w3_reg[7:0],
						   block_w1_reg[31:24], block_w2_reg[23:16], block_w3_reg[15:8], block_w0_reg[7:0],
						   block_w2_reg[31:24], block_w3_reg[23:16], block_w0_reg[15:8], block_w1_reg[7:0],
						   block_w3_reg[31:24], block_w0_reg[23:16], block_w1_reg[15:8], block_w2_reg[7:0]};
 mixcolumns mixcolumns_inst (
	.data	(shiftrows_block),
	.out	(mixcolumns_shiftrows_block)
 );
 
 /*
 always@(*) begin: roundlogic
	block_new   = 128'h0;
	muxed_sboxw = 32'h0;
	block_w0_we = 1'b0;
	block_w1_we = 1'b0;
	block_w2_we = 1'b0;
	block_w3_we = 1'b0;
	case(update_type)
		3'd1: begin
			block_new   = iBlock ^ iRound_key;
			block_w0_we = 1'b1;
			block_w1_we = 1'b1;
			block_w2_we = 1'b1;
			block_w3_we = 1'b1;
		end
		3'd2: begin
			block_new = {iNew_sboxw, iNew_sboxw, iNew_sboxw, iNew_sboxw};
			case(sword_ctr_reg)
				2'h0: begin
					muxed_sboxw = block_w0_reg;
					block_w0_we = 1'b1;
				end
				2'h1: begin
					muxed_sboxw = block_w1_reg;
					block_w1_we = 1'b1;
				end
				2'h2: begin
					muxed_sboxw = block_w2_reg;
					block_w2_we = 1'b1;
				end
				2'h3: begin
					muxed_sboxw = block_w3_reg;
					block_w3_we = 1'b1;
				end
			endcase
		end
		3'd3: begin
			block_new   = mixcolumns_shiftrows_block ^ iRound_key;
			block_w0_we = 1'b1;
			block_w1_we = 1'b1;
			block_w2_we = 1'b1;
			block_w3_we = 1'b1;
		end
		3'd4: begin
			block_new   = shiftrows_block ^ iRound_key;
			block_w0_we = 1'b1;
			block_w1_we = 1'b1;
			block_w2_we = 1'b1;
			block_w3_we = 1'b1;
		end
	endcase
 end
 */
 
 /*
 always@(*) begin
	case(update_type)
		3'd1: begin
			block_new = iBlock ^ iRound_key;
		end
		3'd2: begin
			block_new = {iNew_sboxw, iNew_sboxw, iNew_sboxw, iNew_sboxw};
		end
		3'd3: begin
			block_new = mixcolumns_shiftrows_block ^ iRound_key;
		end
		3'd4: begin
			block_new = shiftrows_block ^ iRound_key;
		end
	endcase
 end
 */
 assign block_new = (update_type1) ? (iBlock ^ iRound_key) :
					(update_type2) ? {iNew_sboxw, iNew_sboxw, iNew_sboxw, iNew_sboxw} :
					(update_type3) ? (mixcolumns_shiftrows_block ^ iRound_key) :
									 (shiftrows_block ^ iRound_key);
 
 /*
 always@(*) begin
	case(update_type)
		3'd1: begin
			muxed_sboxw = 32'h0;
		end
		3'd2: begin
			case(sword_ctr_reg)
				2'h0: begin
					muxed_sboxw = block_w0_reg;
				end
				2'h1: begin
					muxed_sboxw = block_w1_reg;
				end
				2'h2: begin
					muxed_sboxw = block_w2_reg;
				end
				2'h3: begin
					muxed_sboxw = block_w3_reg;
				end
			endcase
		end
		3'd3: begin
			muxed_sboxw = 32'h0;
		end
		3'd4: begin
			muxed_sboxw = 32'h0;
		end
	endcase
 end
 */
 assign muxed_sboxw = (sword_ctr0) ? block_w0_reg :
					  (sword_ctr1) ? block_w1_reg :
					  (sword_ctr2) ? block_w2_reg : block_w3_reg;
 
 /*
 always@(*) begin
	block_w0_we = 1'b0;
	block_w1_we = 1'b0;
	block_w2_we = 1'b0;
	block_w3_we = 1'b0;
	case(update_type)
		3'd1: begin
			block_w0_we = 1'b1;
			block_w1_we = 1'b1;
			block_w2_we = 1'b1;
			block_w3_we = 1'b1;
		end
		3'd2: begin
			case(sword_ctr_reg)
				2'h0: begin
					block_w0_we = 1'b1;
				end
				2'h1: begin
					block_w1_we = 1'b1;
				end
				2'h2: begin
					block_w2_we = 1'b1;
				end
				2'h3: begin
					block_w3_we = 1'b1;
				end
			endcase
		end
		3'd3: begin
			block_w0_we = 1'b1;
			block_w1_we = 1'b1;
			block_w2_we = 1'b1;
			block_w3_we = 1'b1;
		end
		3'd4: begin
			block_w0_we = 1'b1;
			block_w1_we = 1'b1;
			block_w2_we = 1'b1;
			block_w3_we = 1'b1;
		end
	endcase
 end
 */
 assign block_w0_we = update_type1 | (update_type2 & sword_ctr0) | update_type3 | update_type4;
 assign block_w1_we = update_type1 | (update_type2 & sword_ctr1) | update_type3 | update_type4;
 assign block_w2_we = update_type1 | (update_type2 & sword_ctr2) | update_type3 | update_type4;
 assign block_w3_we = update_type1 | (update_type2 & sword_ctr3) | update_type3 | update_type4;
 
 //----------------------------------------------------------------
 // sword_ctr
 //
 // The subbytes word counter with reset and increase logic.
 //----------------------------------------------------------------
 /*
 always@(*) begin
	sword_ctr_new = 2'h0;
	sword_ctr_we  = 1'b0;
	if(sword_ctr_rst) begin
		sword_ctr_new = 2'h0;
		sword_ctr_we  = 1'b1;
	end
	else if(sword_ctr_inc) begin
		sword_ctr_new = sword_ctr_reg + 1'b1;
		sword_ctr_we  = 1'b1;
	end
 end
 */
 wire [1:0] sword_ctr_reg_p1;
 assign sword_ctr_reg_p1 = sword_ctr_reg + 1'b1;
 assign sword_ctr_new = {(2){sword_ctr_inc}} & sword_ctr_reg_p1;
 assign sword_ctr_we = sword_ctr_rst | sword_ctr_inc;
 
 //----------------------------------------------------------------
 // round_ctr
 //
 // The round counter with reset and increase logic.
 //----------------------------------------------------------------
 /*
 always@(*) begin
	round_ctr_new = 4'h0;
	round_ctr_we  = 1'b0;
	if(round_ctr_rst) begin
		round_ctr_new = 4'h0;
		round_ctr_we  = 1'b1;
	end
	else if(round_ctr_inc) begin
		round_ctr_new = round_ctr_reg + 1'b1;
		round_ctr_we  = 1'b1;
	end
 end
 */
 wire [3:0] round_ctr_reg_p1;
 assign round_ctr_reg_p1 = round_ctr_reg + 1'b1;
 assign round_ctr_new = {(4){round_ctr_inc}} & round_ctr_reg_p1;
 assign round_ctr_we = round_ctr_rst | round_ctr_inc;
 
 //----------------------------------------------------------------
 // encipher_ctrl
 //
 // The FSM that controls the encipher operations.
 //----------------------------------------------------------------
 /*
 always@(*) begin: encipherctrl
	reg [3:0] num_rounds;
	
	// Default assignments.
	sword_ctr_inc = 1'b0;
	sword_ctr_rst = 1'b0;
	round_ctr_inc = 1'b0;
	round_ctr_rst = 1'b0;
	ready_new     = 1'b0;
	ready_we      = 1'b0;
	update_type   = 3'd0;
	enc_ctrl_new  = 2'd0;
	enc_ctrl_we   = 1'b0;
	
	if(iKeylen)	num_rounds = 4'he;	//AES256_ROUNDS
	else		num_rounds = 4'ha;	//AES128_ROUNDS

	case(enc_ctrl_reg)
		2'd0: begin
			if(iNext) begin
				round_ctr_rst = 1'b1;
				ready_new     = 1'b0;
				ready_we      = 1'b1;
				enc_ctrl_new  = 2'd1;
				enc_ctrl_we   = 1'b1;
			end
		end
		2'd1: begin
			round_ctr_inc = 1'b1;
			sword_ctr_rst = 1'b1;
			update_type   = 3'd1;
			enc_ctrl_new  = 2'd2;
			enc_ctrl_we   = 1'b1;
		end
		2'd2: begin
			sword_ctr_inc = 1'b1;
			update_type   = 3'd2;
			if(sword_ctr3) begin
				enc_ctrl_new  = 2'd3;
				enc_ctrl_we   = 1'b1;
			end
		end
		2'd3: begin
			sword_ctr_rst = 1'b1;
			round_ctr_inc = 1'b1;
			if(round_ctr_reg_num_rounds) begin
				update_type   = 3'd3;
				enc_ctrl_new  = 2'd2;
				enc_ctrl_we   = 1'b1;
			end
			else begin
				update_type  = 3'd4;
				ready_new    = 1'b1;
				ready_we     = 1'b1;
				enc_ctrl_new = 2'd0;
				enc_ctrl_we  = 1'b1;
			end
		end
	endcase
 end
 */
 
 /*
 always@(*) begin
	case(enc_ctrl_reg)
		2'd0: begin
			sword_ctr_inc = 1'b0;
			sword_ctr_rst = 1'b0;
		end
		2'd1: begin
			sword_ctr_inc = 1'b0;
			sword_ctr_rst = 1'b1;
		end
		2'd2: begin
			sword_ctr_inc = 1'b1;
			sword_ctr_rst = 1'b0;
		end
		2'd3: begin
			sword_ctr_inc = 1'b0;
			sword_ctr_rst = 1'b1;
		end
	endcase
 end
 */
 assign sword_ctr_inc = enc_ctrl2;
 assign sword_ctr_rst = enc_ctrl1 | enc_ctrl3;
 
 /*
 always@(*) begin
	case(enc_ctrl_reg)
		2'd0: begin
			round_ctr_inc = 1'b0;
			if(iNext)	round_ctr_rst = 1'b1;
			else		round_ctr_rst = 1'b0;
		end
		2'd1: begin
			round_ctr_inc = 1'b1;
			round_ctr_rst = 1'b0;
		end
		2'd2: begin
			round_ctr_inc = 1'b0;
			round_ctr_rst = 1'b0;
		end
		2'd3: begin
			round_ctr_inc = 1'b1;
			round_ctr_rst = 1'b0;
		end
	endcase
 end
 */
 assign round_ctr_inc = enc_ctrl1 | enc_ctrl3;
 assign round_ctr_rst = enc_ctrl0 & iNext;
 
 /*
 always@(*) begin
	case(enc_ctrl_reg)
		2'd0: begin
			ready_new = 1'b0;
			if(iNext)	ready_we = 1'b1;
			else		ready_we = 1'b0;
		end
		2'd1: begin
			ready_new = 1'b0;
			ready_we  = 1'b0;
		end
		2'd2: begin
			ready_new = 1'b0;
			ready_we  = 1'b0;
		end
		2'd3: begin
			if(round_ctr_reg_num_roundss) begin
				ready_new = 1'b0;
				ready_we  = 1'b0;
			end
			else begin
				ready_new = 1'b1;
				ready_we  = 1'b1;
			end
		end
	endcase
 end
 */
 assign ready_new = enc_ctrl3 & ~round_ctr_reg_num_rounds;
 assign ready_we = (enc_ctrl0 & iNext) | ready_new;
 
 /*
 always@(*) begin
	case(enc_ctrl_reg)
		2'd0: begin
			update_type = 3'd0;
		end
		2'd1: begin
			update_type = 3'd1;
		end
		2'd2: begin
			update_type = 3'd2;
		end
		2'd3: begin
			if(round_ctr_reg_num_rounds)	update_type = 3'd3;
			else							update_type = 3'd4;
		end
	endcase
 end
 */
 assign update_type[0] = enc_ctrl1 | (enc_ctrl3 & round_ctr_reg_num_rounds);
 assign update_type[1] = enc_ctrl2 | (enc_ctrl3 & round_ctr_reg_num_rounds);
 assign update_type[2] = enc_ctrl3 & ~round_ctr_reg_num_rounds;
 
 /*
 always@(*) begin
	case(enc_ctrl_reg)
		2'd0: begin
			if(iNext) begin
				enc_ctrl_new = 2'd1;
				enc_ctrl_we  = 1'b1;
			end
			else begin
				enc_ctrl_new = 2'd0;
				enc_ctrl_we  = 1'b0;
			end
		end
		2'd1: begin
			enc_ctrl_new = 2'd2;
			enc_ctrl_we  = 1'b1;
		end
		2'd2: begin
			if(sword_ctr3) begin
				enc_ctrl_new = 2'd3;
				enc_ctrl_we  = 1'b1;
			end
			else begin
				enc_ctrl_new = 2'd0;
				enc_ctrl_we  = 1'b0;
			end
		end
		2'd3: begin
			enc_ctrl_we = 1'b1;
			if(round_ctr_reg_num_rounds)	enc_ctrl_new = 2'd2;
			else							enc_ctrl_new = 2'd0;
		end
	endcase
 end
 */
 assign enc_ctrl_new[0] = (enc_ctrl0 & iNext) | (enc_ctrl2 & sword_ctr3);
 assign enc_ctrl_new[1] = enc_ctrl1 | (enc_ctrl2 & sword_ctr3) | (enc_ctrl3 & round_ctr_reg_num_rounds);
 assign enc_ctrl_we = (enc_ctrl0 & iNext) | enc_ctrl1 | (enc_ctrl2 & sword_ctr3) | enc_ctrl3;
 
endmodule
