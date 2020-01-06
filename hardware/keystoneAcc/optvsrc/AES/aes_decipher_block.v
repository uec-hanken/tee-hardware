//======================================================================
//
// aes_decipher_block.v
// --------------------
// The AES decipher round. A pure combinational module that implements
// the initial round, main round and final round logic for
// decciper operations.
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

module aes_decipher_block(
	input			iClk,
	input			iRstn,
	
	input			iNext,
	
	input			iKeylen,
	output	[3:0]	oRound,
	input	[127:0]	iRound_key,
	
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
 wire			round_ctr_set;
 wire			round_ctr_dec;

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

 reg	[1:0]	dec_ctrl_reg;
 wire	[1:0]	dec_ctrl_new;
 wire			dec_ctrl_we;

 wire	[31:0]	tmp_sboxw;
 wire	[31:0]	new_sboxw;
 wire	[2:0]	update_type;
 
 wire			sword_ctr0, sword_ctr1, sword_ctr2, sword_ctr3;
 wire			dec_ctrl0, dec_ctrl1, dec_ctrl2, dec_ctrl3;
 //wire			update_type0;
 wire			update_type1, update_type2, update_type3, update_type4;
 wire			round_ctr_g0;

 //----------------------------------------------------------------
 // Instantiations.
 //----------------------------------------------------------------
 aes_inv_sbox inv_sbox_inst(
	.in		(tmp_sboxw),
	.out	(new_sboxw)
 );

 //----------------------------------------------------------------
 // Concurrent connectivity for ports etc.
 //----------------------------------------------------------------
 assign oRound     = round_ctr_reg;
 assign oNew_block = {block_w0_reg, block_w1_reg, block_w2_reg, block_w3_reg};
 assign oReady     = ready_reg;
 
 assign sword_ctr0 = ~sword_ctr_reg[1] & ~sword_ctr_reg[0];
 assign sword_ctr1 = ~sword_ctr_reg[1] &  sword_ctr_reg[0];
 assign sword_ctr2 =  sword_ctr_reg[1] & ~sword_ctr_reg[0];
 assign sword_ctr3 =  sword_ctr_reg[1] &  sword_ctr_reg[0];
 
 assign dec_ctrl0 = ~dec_ctrl_reg[1] & ~dec_ctrl_reg[0];
 assign dec_ctrl1 = ~dec_ctrl_reg[1] &  dec_ctrl_reg[0];
 assign dec_ctrl2 =  dec_ctrl_reg[1] & ~dec_ctrl_reg[0];
 assign dec_ctrl3 =  dec_ctrl_reg[1] &  dec_ctrl_reg[0];
 
 //assign update_type0 = ~update_type[2] & ~update_type[1] & ~update_type[0];
 assign update_type1 = ~update_type[2] & ~update_type[1] &  update_type[0];
 assign update_type2 = ~update_type[2] &  update_type[1] & ~update_type[0];
 assign update_type3 = ~update_type[2] &  update_type[1] &  update_type[0];
 assign update_type4 =  update_type[2] & ~update_type[1] & ~update_type[0];
 
 assign round_ctr_g0 = round_ctr_reg[3] | round_ctr_reg[2] | round_ctr_reg[1] | round_ctr_reg[0];

 //----------------------------------------------------------------
 // reg_update
 //
 // Update functionality for all registers in the core.
 // All registers are positive edge triggered with synchronous
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
		dec_ctrl_reg  <= 2'd0;
	end
	else begin
		if(block_w0_we)		block_w0_reg <= block_new[127:96];
		if(block_w1_we)		block_w1_reg <= block_new[95:64];
		if(block_w2_we)		block_w2_reg <= block_new[63:32];
		if(block_w3_we)		block_w3_reg <= block_new[31:0];
		if(sword_ctr_we)	sword_ctr_reg <= sword_ctr_new;
		if(round_ctr_we)	round_ctr_reg <= round_ctr_new;
		if(ready_we)		ready_reg <= ready_new;
		if(dec_ctrl_we)		dec_ctrl_reg <= dec_ctrl_new;
	end
 end
 */
 always@(posedge iClk) begin
	if(~iRstn)				block_w0_reg <= 32'b0;
	else if(block_w0_we)	block_w0_reg <= block_new[127:96];
 end
 
 always@(posedge iClk) begin
	if(~iRstn)				block_w1_reg <= 32'b0;
	else if(block_w1_we)	block_w1_reg <= block_new[95:64];
 end
 
 always@(posedge iClk) begin
	if(~iRstn)				block_w2_reg <= 32'b0;
	else if(block_w2_we)	block_w2_reg <= block_new[63:32];
 end
 
 always@(posedge iClk) begin
	if(~iRstn)				block_w3_reg <= 32'b0;
	else if(block_w3_we)	block_w3_reg <= block_new[31:0];
 end
 
 always@(posedge iClk) begin
	if(~iRstn)				sword_ctr_reg <= 2'b0;
	else if(sword_ctr_we)	sword_ctr_reg <= sword_ctr_new;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)				round_ctr_reg <= 4'b0;
	else if(round_ctr_we)	round_ctr_reg <= round_ctr_new;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)			ready_reg <= 1'b1;
	else if(ready_we)	ready_reg <= ready_new;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)				dec_ctrl_reg <= 2'b0;
	else if(dec_ctrl_we)	dec_ctrl_reg <= dec_ctrl_new;
 end
 
 //----------------------------------------------------------------
 // round_logic
 //
 // The logic needed to implement init, main and final rounds.
 //----------------------------------------------------------------
 wire [127:0] inv_mixcolumns_addkey_block;
 wire [127:0] addkey_block;
 inv_mixcolumns inv_mixcolumns_inst (
	.data	(addkey_block),
	.out	(inv_mixcolumns_addkey_block)
 );
 
 /*
 always@(*) begin
	addkey_block = 128'h0;
	block_new    = 128'h0;
	tmp_sboxw    = 32'h0;
	block_w0_we  = 1'b0;
	block_w1_we  = 1'b0;
	block_w2_we  = 1'b0;
	block_w3_we  = 1'b0;
	// Update based on update type.
	case(update_type)
		// InitRound
		3'd1: begin
			addkey_block = iBlock ^ iRound_key;
			block_new    = {addkey_block[127:120], addkey_block[23:16]  , addkey_block[47:40]  , addkey_block[71:64] ,
						    addkey_block[95:88]  , addkey_block[119:112], addkey_block[15:8]   , addkey_block[39:32] ,
						    addkey_block[63:56]  , addkey_block[87:80]  , addkey_block[111:104], addkey_block[7:0]   ,
						    addkey_block[31:24]  , addkey_block[55:48]  , addkey_block[79:72]  , addkey_block[103:96]};
			block_w0_we = 1'b1;
			block_w1_we = 1'b1;
			block_w2_we = 1'b1;
			block_w3_we = 1'b1;
		end
		3'd2: begin
			block_new = {new_sboxw, new_sboxw, new_sboxw, new_sboxw};
			case(sword_ctr_reg)
				2'h0: begin
					tmp_sboxw   = block_w0_reg;
					block_w0_we = 1'b1;
				end
				2'h1: begin
					tmp_sboxw   = block_w1_reg;
					block_w1_we = 1'b1;
				end
				2'h2: begin
					tmp_sboxw   = block_w2_reg;
					block_w2_we = 1'b1;
				end
				2'h3: begin
					tmp_sboxw   = block_w3_reg;
					block_w3_we = 1'b1;
				end
			endcase
		end
		3'd3: begin
			addkey_block = {block_w0_reg, block_w1_reg, block_w2_reg, block_w3_reg} ^ iRound_key;
			block_new    = {inv_mixcolumns_addkey_block[127:120], inv_mixcolumns_addkey_block[23:16]  ,
							inv_mixcolumns_addkey_block[47:40]  , inv_mixcolumns_addkey_block[71:64]  ,
							inv_mixcolumns_addkey_block[95:88]  , inv_mixcolumns_addkey_block[119:112],
							inv_mixcolumns_addkey_block[15:8]   , inv_mixcolumns_addkey_block[39:32]  ,
							inv_mixcolumns_addkey_block[63:56]  , inv_mixcolumns_addkey_block[87:80]  ,
							inv_mixcolumns_addkey_block[111:104], inv_mixcolumns_addkey_block[7:0]    ,
							inv_mixcolumns_addkey_block[31:24]  , inv_mixcolumns_addkey_block[55:48]  ,
							inv_mixcolumns_addkey_block[79:72]  , inv_mixcolumns_addkey_block[103:96]};
			block_w0_we = 1'b1;
			block_w1_we = 1'b1;
			block_w2_we = 1'b1;
			block_w3_we = 1'b1;
		end
		3'd4: begin
			block_new   = {block_w0_reg, block_w1_reg, block_w2_reg, block_w3_reg} ^ iRound_key;
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
	// Update based on update type.
	case(update_type)
		// InitRound
		3'd1: begin
			addkey_block = iBlock ^ iRound_key;
		end
		3'd2: begin
			addkey_block = 128'h0;
		end
		3'd3: begin
			addkey_block = {block_w0_reg, block_w1_reg, block_w2_reg, block_w3_reg} ^ iRound_key;
		end
		3'd4: begin
			addkey_block = 128'h0;
		end
	endcase
 end
 */
 assign addkey_block = (update_type1) ? (iBlock ^ iRound_key) :
										({block_w0_reg, block_w1_reg, block_w2_reg, block_w3_reg} ^ iRound_key);
 
 /*
 always@(*) begin
	// Update based on update type.
	case(update_type)
		// InitRound
		3'd1: begin
			block_new = {addkey_block[127:120], addkey_block[23:16]  ,
						 addkey_block[47:40]  , addkey_block[71:64]  ,
						 addkey_block[95:88]  , addkey_block[119:112],
						 addkey_block[15:8]   , addkey_block[39:32]  ,
						 addkey_block[63:56]  , addkey_block[87:80]  ,
						 addkey_block[111:104], addkey_block[7:0]    ,
						 addkey_block[31:24]  , addkey_block[55:48]  ,
						 addkey_block[79:72]  , addkey_block[103:96]};
		end
		3'd2: begin
			block_new = {new_sboxw, new_sboxw, new_sboxw, new_sboxw};
		end
		3'd3: begin
			block_new = {inv_mixcolumns_addkey_block[127:120], inv_mixcolumns_addkey_block[23:16]  ,
						 inv_mixcolumns_addkey_block[47:40]  , inv_mixcolumns_addkey_block[71:64]  ,
						 inv_mixcolumns_addkey_block[95:88]  , inv_mixcolumns_addkey_block[119:112],
						 inv_mixcolumns_addkey_block[15:8]   , inv_mixcolumns_addkey_block[39:32]  ,
						 inv_mixcolumns_addkey_block[63:56]  , inv_mixcolumns_addkey_block[87:80]  ,
						 inv_mixcolumns_addkey_block[111:104], inv_mixcolumns_addkey_block[7:0]    ,
						 inv_mixcolumns_addkey_block[31:24]  , inv_mixcolumns_addkey_block[55:48]  ,
						 inv_mixcolumns_addkey_block[79:72]  , inv_mixcolumns_addkey_block[103:96]};
		end
		3'd4: begin
			block_new = {block_w0_reg, block_w1_reg, block_w2_reg, block_w3_reg} ^ iRound_key;
		end
	endcase
 end
 */
 assign block_new = (update_type1) ? {addkey_block[127:120], addkey_block[23:16]  , addkey_block[47:40]  , addkey_block[71:64]  ,
									  addkey_block[95:88]  , addkey_block[119:112], addkey_block[15:8]   , addkey_block[39:32]  ,
									  addkey_block[63:56]  , addkey_block[87:80]  , addkey_block[111:104], addkey_block[7:0]    ,
									  addkey_block[31:24]  , addkey_block[55:48]  , addkey_block[79:72]  , addkey_block[103:96]} :
					(update_type2) ? {new_sboxw, new_sboxw, new_sboxw, new_sboxw} :
					(update_type3) ? {inv_mixcolumns_addkey_block[127:120], inv_mixcolumns_addkey_block[23:16]  ,
									  inv_mixcolumns_addkey_block[47:40]  , inv_mixcolumns_addkey_block[71:64]  ,
									  inv_mixcolumns_addkey_block[95:88]  , inv_mixcolumns_addkey_block[119:112],
									  inv_mixcolumns_addkey_block[15:8]   , inv_mixcolumns_addkey_block[39:32]  ,
									  inv_mixcolumns_addkey_block[63:56]  , inv_mixcolumns_addkey_block[87:80]  ,
									  inv_mixcolumns_addkey_block[111:104], inv_mixcolumns_addkey_block[7:0]    ,
									  inv_mixcolumns_addkey_block[31:24]  , inv_mixcolumns_addkey_block[55:48]  ,
									  inv_mixcolumns_addkey_block[79:72]  , inv_mixcolumns_addkey_block[103:96]} :
									 ({block_w0_reg, block_w1_reg, block_w2_reg, block_w3_reg} ^ iRound_key);
 
 /*
 always@(*) begin
	// Update based on update type.
	case(update_type)
		// InitRound
		3'd1: begin
			tmp_sboxw = 32'h0;
		end
		3'd2: begin
			case(sword_ctr_reg)
				2'h0: begin
					tmp_sboxw = block_w0_reg;
				end
				2'h1: begin
					tmp_sboxw = block_w1_reg;
				end
				2'h2: begin
					tmp_sboxw = block_w2_reg;
				end
				2'h3: begin
					tmp_sboxw = block_w3_reg;
				end
			endcase
		end
		3'd3: begin
			tmp_sboxw = 32'h0;
		end
		3'd4: begin
			tmp_sboxw = 32'h0;
		end
	endcase
 end
 */
 assign tmp_sboxw = (sword_ctr0) ? block_w0_reg :
					(sword_ctr1) ? block_w1_reg :
					(sword_ctr2) ? block_w2_reg : block_w3_reg;
 
 /*
 always@(*) begin
	block_w0_we = 1'b0;
	block_w1_we = 1'b0;
	block_w2_we = 1'b0;
	block_w3_we = 1'b0;
	// Update based on update type.
	case(update_type)
		// InitRound
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

	if(round_ctr_set) begin
		round_ctr_we  = 1'b1;
		if(iKeylen)	round_ctr_new = 4'b1110;	//AES256_ROUNDS
		else		round_ctr_new = 4'b1010;	//AES128_ROUNDS
	end
	else if(round_ctr_dec) begin
		round_ctr_new = round_ctr_reg - 1'b1;
		round_ctr_we  = 1'b1;
	end
 end
 */
 wire [3:0] round_ctr_reg_m1;
 assign round_ctr_reg_m1 = round_ctr_reg - 1'b1;
 assign round_ctr_new[3] = round_ctr_set | (round_ctr_dec&round_ctr_reg_m1[3]);
 assign round_ctr_new[2] = (round_ctr_set) ? (iKeylen) : (round_ctr_dec&round_ctr_reg_m1[2]);
 assign round_ctr_new[1] = round_ctr_set | (round_ctr_dec&round_ctr_reg_m1[1]);
 assign round_ctr_new[0] = ~round_ctr_set & round_ctr_dec & round_ctr_reg_m1[0];
 assign round_ctr_we = round_ctr_set | round_ctr_dec;
 
 //----------------------------------------------------------------
 // decipher_ctrl
 //
 // The FSM that controls the decipher operations.
 //----------------------------------------------------------------
 /*
 always@(*) begin
	sword_ctr_inc = 1'b0;
	sword_ctr_rst = 1'b0;
	round_ctr_dec = 1'b0;
	round_ctr_set = 1'b0;
	ready_new     = 1'b0;
	ready_we      = 1'b0;
	update_type   = 3'd0;
	dec_ctrl_new  = 2'd0;
	dec_ctrl_we   = 1'b0;

	case(dec_ctrl_reg)
		2'd0: begin
			if(iNext) begin
				round_ctr_set = 1'b1;
				ready_new     = 1'b0;
				ready_we      = 1'b1;
				dec_ctrl_new  = 2'd1;
				dec_ctrl_we   = 1'b1;
			end
		end
		2'd1: begin
			sword_ctr_rst = 1'b1;
			update_type   = 3'd1;
			dec_ctrl_new  = 2'd2;
			dec_ctrl_we   = 1'b1;
		end
		2'd2: begin
			sword_ctr_inc = 1'b1;
			update_type   = 3'd2;
			if(sword_ctr3) begin
				round_ctr_dec = 1'b1;
				dec_ctrl_new  = 2'd3;
				dec_ctrl_we   = 1'b1;
			end
		end
		2'd3: begin
			sword_ctr_rst = 1'b1;
			dec_ctrl_we  = 1'b1;
			if(round_ctr_g0) begin
				update_type   = 3'd3;
				dec_ctrl_new  = 2'd2;
			end
			else begin
				update_type  = 3'd4;
				ready_new    = 1'b1;
				ready_we     = 1'b1;
				dec_ctrl_new = 2'd0;
			end
		end
	endcase
 end
 */
 
 /*
 always@(*) begin
	case(dec_ctrl_reg)
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
 assign sword_ctr_inc = dec_ctrl2;
 assign sword_ctr_rst = dec_ctrl1 | dec_ctrl3;
 
 /*
 always@(*) begin
	case(dec_ctrl_reg)
		2'd0: begin
			round_ctr_dec = 1'b0;
			if(iNext)	round_ctr_set = 1'b1;
			else		round_ctr_set = 1'b0;
		end
		2'd1: begin
			round_ctr_dec = 1'b0;
			round_ctr_set = 1'b0;
		end
		2'd2: begin
			round_ctr_set = 1'b0;
			if(sword_ctr3)	round_ctr_dec = 1'b1;
			else			round_ctr_dec = 1'b0;
		end
		2'd3: begin
			round_ctr_dec = 1'b0;
			round_ctr_set = 1'b0;
		end
	endcase
 end
 */
 assign round_ctr_dec = dec_ctrl2 & sword_ctr3;
 assign round_ctr_set = dec_ctrl0 & iNext;
 
 /*
 always@(*) begin
	case(dec_ctrl_reg)
		2'd0: begin
			ready_new = 1'b0;
			if(iNext) 	ready_we  = 1'b1;
			else 		ready_we  = 1'b0;
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
			if(round_ctr_g0) begin
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
 assign ready_new = dec_ctrl3 & ~round_ctr_g0;
 assign ready_we = (dec_ctrl0 & iNext) | ready_new;
 
 /*
 always@(*) begin
	case(dec_ctrl_reg)
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
			if(round_ctr_g0)	update_type = 3'd3;
			else				update_type = 3'd4;
		end
	endcase
 end
 */
 assign update_type[0] = dec_ctrl1 | (dec_ctrl3 & round_ctr_g0);
 assign update_type[1] = dec_ctrl2 | (dec_ctrl3 & round_ctr_g0);
 assign update_type[2] = dec_ctrl3 & ~round_ctr_g0;
 
 /*
 always@(*) begin
	case(dec_ctrl_reg)
		2'd0: begin
			if(iNext) begin
				dec_ctrl_new = 2'd1;
				dec_ctrl_we  = 1'b1;
			end
			else begin
				dec_ctrl_new = 2'd0;
				dec_ctrl_we  = 1'b0;
			end
		end
		2'd1: begin
			dec_ctrl_new = 2'd2;
			dec_ctrl_we  = 1'b1;
		end
		2'd2: begin
			if(sword_ctr3) begin
				dec_ctrl_new = 2'd3;
				dec_ctrl_we  = 1'b1;
			end
			else begin
				dec_ctrl_new = 2'd0;
				dec_ctrl_we  = 1'b0;
			end
		end
		2'd3: begin
			dec_ctrl_we  = 1'b1;
			if(round_ctr_g0)	dec_ctrl_new = 2'd2;
			else 				dec_ctrl_new = 2'd0;
		end
	endcase
 end
 */
 assign dec_ctrl_new[0] = (dec_ctrl0 & iNext) | (dec_ctrl2 & sword_ctr3);
 assign dec_ctrl_new[1] = dec_ctrl1 | (dec_ctrl2 & sword_ctr3) | (dec_ctrl3 & round_ctr_g0);
 assign dec_ctrl_we = (dec_ctrl0 & iNext) | dec_ctrl1 | (dec_ctrl2 & sword_ctr3) | dec_ctrl3;
 
endmodule
