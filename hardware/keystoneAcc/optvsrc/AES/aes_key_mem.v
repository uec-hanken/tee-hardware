//======================================================================
//
// aes_key_mem.v
// -------------
// The AES key memory including round key generator.
//
//
// Author: Joachim Strombergson
// Copyright (c) 2013 Secworks Sweden AB
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

module aes_key_mem(
	input			iClk,
	input			iRstn,
	
	input	[255:0]	iKey,
	input			iKeylen,
	input			iInit,
	
	input	[3:0]	iRound,
	output	[127:0]	oRound_key,
	output			oReady,
	
	
	output	[31:0]	oSboxw,
	input	[31:0]	iNew_sboxw
);

 //----------------------------------------------------------------
 // Registers.
 //----------------------------------------------------------------
 reg	[127:0]	key_mem0,  key_mem1,  key_mem2,  key_mem3,
				key_mem4,  key_mem5,  key_mem6,  key_mem7,
				key_mem8,  key_mem9,  key_mem10, key_mem11,
				key_mem12, key_mem13, key_mem14;
 wire	[127:0]	key_mem_new;
 wire			key_mem_we;

 reg	[127:0]	prev_key0_reg;
 wire	[127:0]	prev_key0_new;
 wire			prev_key0_we;

 reg	[127:0]	prev_key1_reg;
 wire	[127:0]	prev_key1_new;
 wire			prev_key1_we;

 reg	[3:0]	round_ctr_reg;
 wire	[3:0]	round_ctr_new;
 wire			round_ctr_rst;
 wire			round_ctr_inc;
 wire			round_ctr_we;
 wire			round_ctr_0, round_ctr_1;

 reg	[1:0]	key_mem_ctrl_reg;
 wire	[1:0]	key_mem_ctrl_new;
 wire			key_mem_ctrl_we;
 wire			State0, State1, State2, State3;

 reg			ready_reg;
 wire			ready_new;
 wire			ready_we;

 reg	[7:0]	rcon_reg;
 wire	[7:0]	rcon_new;
 wire			rcon_we;
 wire			rcon_set;
 wire			rcon_next;
 
 wire	[31:0]	w0, w1, w2, w3, w4, w5, w6, w7;
 wire	[31:0]	k0, k1, k2, k3;
 wire	[31:0]	tw, trw;
 wire			round_ctr_reg_num_rounds;

 //----------------------------------------------------------------
 // Wires.
 //----------------------------------------------------------------
 wire			round_key_update;
 wire	[127:0]	tmp_round_key;

 //----------------------------------------------------------------
 // Concurrent assignments for ports.
 //----------------------------------------------------------------
 assign oRound_key = tmp_round_key;
 assign oReady     = ready_reg;
 assign oSboxw     = w7;
 
 assign State0 = ~key_mem_ctrl_reg[1] & ~key_mem_ctrl_reg[0];
 assign State1 = ~key_mem_ctrl_reg[1] &  key_mem_ctrl_reg[0];
 assign State2 =  key_mem_ctrl_reg[1] & ~key_mem_ctrl_reg[0];
 assign State3 =  key_mem_ctrl_reg[1] &  key_mem_ctrl_reg[0];
 
 assign round_ctr_0 = ~round_ctr_reg[3] & ~round_ctr_reg[2] & ~round_ctr_reg[1] & ~round_ctr_reg[0];
 assign round_ctr_1 = ~round_ctr_reg[3] & ~round_ctr_reg[2] & ~round_ctr_reg[1] &  round_ctr_reg[0];
 
 //wire [3:0] num_rounds;
 //assign num_rounds = (iKeylen) ? 14 : 10;
 //assign round_ctr_reg_num_rounds = round_ctr_reg==num_rounds;
 assign round_ctr_reg_num_rounds = round_ctr_reg[3] &
								   ~(iKeylen ^ round_ctr_reg[2]) &
								   round_ctr_reg[1] &
								   ~round_ctr_reg[0];
 
 //----------------------------------------------------------------
 // reg_update
 //
 // Update functionality for all registers in the core.
 // All registers are positive edge triggered with asynchronous
 // active low reset. All registers have write enable.
 //----------------------------------------------------------------
 /*
 always@(posedge iClk or negedge iRstn) begin: regupdate
	integer i;

	if(~iRstn) begin
		for(i=0; i<=AES_256_NUM_ROUNDS; i=i+1)
			key_mem[i] <= 128'h0;
			rcon_reg         <= 8'h0;
			ready_reg        <= 1'b0;
			round_ctr_reg    <= 4'h0;
			key_mem_ctrl_reg <= 2'd0;
		end
	else begin
		if(round_ctr_we)	round_ctr_reg <= round_ctr_new;
		if(ready_we)		ready_reg <= ready_new;
		if(rcon_we)			rcon_reg <= rcon_new;
		if(key_mem_we)		key_mem[round_ctr_reg] <= key_mem_new;
		if(prev_key0_we)	prev_key0_reg <= prev_key0_new;
		if(prev_key1_we)	prev_key1_reg <= prev_key1_new;
		if(key_mem_ctrl_we)	key_mem_ctrl_reg <= key_mem_ctrl_new;
	end
 end
 */
 always@(posedge iClk) begin
	if(~iRstn)				round_ctr_reg <= 4'h0;
	else if(round_ctr_we)	round_ctr_reg <= round_ctr_new;
	else					round_ctr_reg <= round_ctr_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)			ready_reg <= 1'b0;
	else if(ready_we)	ready_reg <= ready_new;
	else				ready_reg <= ready_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)			rcon_reg <= 8'h0;
	else if(rcon_we)	rcon_reg <= rcon_new;
	else				rcon_reg <= rcon_reg;
 end
 
 always@(posedge iClk) begin
	if(key_mem_we & ~round_ctr_reg[3] & ~round_ctr_reg[2] & ~round_ctr_reg[1] & ~round_ctr_reg[0])
			key_mem0 <= key_mem_new;
	else	key_mem0 <= key_mem0;
 end
 always@(posedge iClk) begin
	if(key_mem_we & ~round_ctr_reg[3] & ~round_ctr_reg[2] & ~round_ctr_reg[1] & round_ctr_reg[0])
			key_mem1 <= key_mem_new;
	else	key_mem1 <= key_mem1;
 end
 always@(posedge iClk) begin
	if(key_mem_we & ~round_ctr_reg[3] & ~round_ctr_reg[2] & round_ctr_reg[1] & ~round_ctr_reg[0])
			key_mem2 <= key_mem_new;
	else	key_mem2 <= key_mem2;
 end
 always@(posedge iClk) begin
	if(key_mem_we & ~round_ctr_reg[3] & ~round_ctr_reg[2] & round_ctr_reg[1] & round_ctr_reg[0])
			key_mem3 <= key_mem_new;
	else	key_mem3 <= key_mem3;
 end
 always@(posedge iClk) begin
	if(key_mem_we & ~round_ctr_reg[3] & round_ctr_reg[2] & ~round_ctr_reg[1] & ~round_ctr_reg[0])
			key_mem4 <= key_mem_new;
	else	key_mem4 <= key_mem4;
 end
 always@(posedge iClk) begin
	if(key_mem_we & ~round_ctr_reg[3] & round_ctr_reg[2] & ~round_ctr_reg[1] & round_ctr_reg[0])
			key_mem5 <= key_mem_new;
	else	key_mem5 <= key_mem5;
 end
 always@(posedge iClk) begin
	if(key_mem_we & ~round_ctr_reg[3] & round_ctr_reg[2] & round_ctr_reg[1] & ~round_ctr_reg[0])
			key_mem6 <= key_mem_new;
	else	key_mem6 <= key_mem6;
 end
 always@(posedge iClk) begin
	if(key_mem_we & ~round_ctr_reg[3] & round_ctr_reg[2] & round_ctr_reg[1] & round_ctr_reg[0])
			key_mem7 <= key_mem_new;
	else	key_mem7 <= key_mem7;
 end
 always@(posedge iClk) begin
	if(key_mem_we & round_ctr_reg[3] & ~round_ctr_reg[2] & ~round_ctr_reg[1] & ~round_ctr_reg[0])
			key_mem8 <= key_mem_new;
	else	key_mem8 <= key_mem8;
 end
 always@(posedge iClk) begin
	if(key_mem_we & round_ctr_reg[3] & ~round_ctr_reg[2] & ~round_ctr_reg[1] & round_ctr_reg[0])
			key_mem9 <= key_mem_new;
	else	key_mem9 <= key_mem9;
 end
 always@(posedge iClk) begin
	if(key_mem_we & round_ctr_reg[3] & ~round_ctr_reg[2] & round_ctr_reg[1] & ~round_ctr_reg[0])
			key_mem10 <= key_mem_new;
	else	key_mem10 <= key_mem10;
 end
 always@(posedge iClk) begin
	if(key_mem_we & round_ctr_reg[3] & ~round_ctr_reg[2] & round_ctr_reg[1] & round_ctr_reg[0])
			key_mem11 <= key_mem_new;
	else	key_mem11 <= key_mem11;
 end
 always@(posedge iClk) begin
	if(key_mem_we & round_ctr_reg[3] & round_ctr_reg[2] & ~round_ctr_reg[1] & ~round_ctr_reg[0])
			key_mem12 <= key_mem_new;
	else	key_mem12 <= key_mem12;
 end
 always@(posedge iClk) begin
	if(key_mem_we & round_ctr_reg[3] & round_ctr_reg[2] & ~round_ctr_reg[1] & round_ctr_reg[0])
			key_mem13 <= key_mem_new;
	else	key_mem13 <= key_mem13;
 end
 always@(posedge iClk) begin
	if(key_mem_we & round_ctr_reg[3] & round_ctr_reg[2] & round_ctr_reg[1])
			key_mem14 <= key_mem_new;
	else	key_mem14 <= key_mem14;
 end
 
 always@(posedge iClk) begin
	if(prev_key0_we)	prev_key0_reg <= prev_key0_new;
	else				prev_key0_reg <= prev_key0_reg;
 end
 
 always@(posedge iClk) begin
	if(prev_key1_we)	prev_key1_reg <= prev_key1_new;
	else				prev_key1_reg <= prev_key1_reg;
 end
 
 always@(posedge iClk) begin
	if(~iRstn)					key_mem_ctrl_reg <= 2'd0;
	else if(key_mem_ctrl_we)	key_mem_ctrl_reg <= key_mem_ctrl_new;
	else						key_mem_ctrl_reg <= key_mem_ctrl_reg;
 end
 
 //----------------------------------------------------------------
 // key_mem_read
 //
 // Combinational read port for the key memory.
 //----------------------------------------------------------------
 /*
 always@(*) begin
	tmp_round_key = key_mem[iRound];
 end // key_mem_read
 */
 //assign tmp_round_key = key_mem[iRound];
 assign tmp_round_key = (iRound[3]) ?
							( (iRound[2]) ?
								( (iRound[1]) ? key_mem14 :
									( (iRound[0]) ? key_mem13 : key_mem12 ) ) :
								( (iRound[1]) ?
									( (iRound[0]) ? key_mem11 : key_mem10 ) :
									( (iRound[0]) ? key_mem9 : key_mem8 ) ) ) :
							( (iRound[2]) ?
								( (iRound[1]) ?
									( (iRound[0]) ? key_mem7 : key_mem6 ) :
									( (iRound[0]) ? key_mem5 : key_mem4 ) ) :
								( (iRound[1]) ?
									( (iRound[0]) ? key_mem3 : key_mem2 ) :
									( (iRound[0]) ? key_mem1 : key_mem0 ) ) );
 
 //----------------------------------------------------------------
 // round_key_gen
 //
 // The round key generator logic for AES-128 and AES-256.
 //----------------------------------------------------------------
 assign w0 = prev_key0_reg[127:96];
 assign w1 = prev_key0_reg[95:64];
 assign w2 = prev_key0_reg[63:32];
 assign w3 = prev_key0_reg[31:0];
 assign w4 = prev_key1_reg[127:96];
 assign w5 = prev_key1_reg[95:64];
 assign w6 = prev_key1_reg[63:32];
 assign w7 = prev_key1_reg[31:0];
 
 assign trw[31:24] = iNew_sboxw[23:16] ^ rcon_reg;
 assign trw[23:0]  = {iNew_sboxw[15:0], iNew_sboxw[31:24]};
 assign tw = iNew_sboxw;
 
 /*
 always@(*) begin: roundkeygen
	reg [31:0] k0, k1, k2, k3;
	reg [31:0] rconw, rotstw, tw, trw;

	// Default assignments.
	key_mem_new   = 128'h0;
	key_mem_we    = 1'b0;
	prev_key0_new = 128'h0;
	prev_key0_we  = 1'b0;
	prev_key1_new = 128'h0;
	prev_key1_we  = 1'b0;

	k0 = 32'h0;
	k1 = 32'h0;
	k2 = 32'h0;
	k3 = 32'h0;

	rcon_set   = 1'b1;
	rcon_next  = 1'b0;

	// Extract words and calculate intermediate values.
	// Perform rotation of sbox word etc.
	
	rconw = {rcon_reg, 24'h0};
	tmp_sboxw = w7;
	rotstw = {iNew_sboxw[23:0], iNew_sboxw[31:24]};
	trw = rotstw ^ rconw;
	tw = iNew_sboxw;

	// Generate the specific round keys.
	if(round_key_update) begin
		rcon_set   = 1'b0;
		key_mem_we = 1'b1;
		case (iKeylen)
			1'b0: begin	//AES_128_BIT_KEY
				if(round_ctr_0) begin
					key_mem_new   = iKey[255:128];
					prev_key1_new = iKey[255:128];
					prev_key1_we  = 1'b1;
					rcon_next     = 1'b1;
				end
				else begin
					k0 = w4 ^ trw;
					k1 = w5 ^ w4 ^ trw;
					k2 = w6 ^ w5 ^ w4 ^ trw;
					k3 = w7 ^ w6 ^ w5 ^ w4 ^ trw;
					key_mem_new   = {k0, k1, k2, k3};
					prev_key1_new = {k0, k1, k2, k3};
					prev_key1_we  = 1'b1;
					rcon_next     = 1'b1;
				end
			end
			1'b1: begin	//AES_256_BIT_KEY
				if(round_ctr_0) begin
					key_mem_new   = iKey[255:128];
					prev_key0_new = iKey[255:128];
					prev_key0_we  = 1'b1;
				end
				else if(round_ctr_1) begin
					key_mem_new   = iKey[127:0];
					prev_key1_new = iKey[127:0];
					prev_key1_we  = 1'b1;
					rcon_next     = 1'b1;
				end
				else begin
					if(round_ctr_reg[0]==0) begin
						k0 = w0 ^ trw;
						k1 = w1 ^ w0 ^ trw;
						k2 = w2 ^ w1 ^ w0 ^ trw;
						k3 = w3 ^ w2 ^ w1 ^ w0 ^ trw;
					end
					else begin
						k0 = w0 ^ tw;
						k1 = w1 ^ w0 ^ tw;
						k2 = w2 ^ w1 ^ w0 ^ tw;
						k3 = w3 ^ w2 ^ w1 ^ w0 ^ tw;
						rcon_next = 1'b1;
					end
					// Store the generated round keys.
					key_mem_new   = {k0, k1, k2, k3};
					prev_key1_new = {k0, k1, k2, k3};
					prev_key1_we  = 1'b1;
					prev_key0_new = prev_key1_reg;
					prev_key0_we  = 1'b1;
				end
			end
		endcase // case (iKeylen)
	end
 end // round_key_gen
 */
 
 /*
 always@(*) begin
	key_mem_new = 128'h0;
	key_mem_we = 1'b0;
	// Generate the specific round keys.
	if(round_key_update) begin
		key_mem_we = 1'b1;
		case (iKeylen)
			1'b0: begin	//AES_128_BIT_KEY
				if(round_ctr_0)	key_mem_new = iKey[255:128];
				else			key_mem_new = {k0, k1, k2, k3};
			end
			1'b1: begin	//AES_256_BIT_KEY
				if(round_ctr_0)			key_mem_new = iKey[255:128];
				else if(round_ctr_1)	key_mem_new = iKey[127:0];
				else					key_mem_new = {k0, k1, k2, k3};
			end
		endcase
	end
 end
 */
 assign key_mem_new = (round_ctr_0) ? iKey[255:128] :
									  ( (iKeylen & round_ctr_1) ? iKey[127:0] : {k0, k1, k2, k3} );
 assign key_mem_we = round_key_update;
 
 /*
 always@(*) begin
	rcon_set = 1'b1;
	rcon_next = 1'b0;
	// Generate the specific round keys.
	if(round_key_update) begin
		rcon_set = 1'b0;
		case (iKeylen)
			1'b0: begin	//AES_128_BIT_KEY
				rcon_next = 1'b1;
			end
			1'b1: begin	//AES_256_BIT_KEY
				if(round_ctr_0)	rcon_next = 1'b0;
				else if(round_ctr_1 | ~round_ctr_reg[0])
						rcon_next = 1'b1;
				else	rcon_next = 1'b0;
			end
		endcase
	end
 end
 */
 assign rcon_set = ~round_key_update;
 assign rcon_next = ~iKeylen | (~round_ctr_0 & (round_ctr_1 | ~round_ctr_reg[0]));
 
 /*
 always@(*) begin
	prev_key0_new = 128'h0;
	prev_key0_we  = 1'b0;
	prev_key1_new = 128'h0;
	prev_key1_we  = 1'b0;
	// Generate the specific round keys.
	if(round_key_update) begin
		case (iKeylen)
			1'b0: begin	//AES_128_BIT_KEY
				if(round_ctr_0) begin
					prev_key1_new = iKey[255:128];
					prev_key1_we  = 1'b1;
				end
				else begin
					prev_key1_new = {k0, k1, k2, k3};
					prev_key1_we  = 1'b1;
				end
			end
			1'b1: begin	//AES_256_BIT_KEY
				if(round_ctr_0) begin
					prev_key0_new = iKey[255:128];
					prev_key0_we  = 1'b1;
				end
				else if(round_ctr_1) begin
					prev_key1_new = iKey[127:0];
					prev_key1_we  = 1'b1;
				end
				else begin
					// Store the generated round keys.
					prev_key1_new = {k0, k1, k2, k3};
					prev_key1_we  = 1'b1;
					prev_key0_new = prev_key1_reg;
					prev_key0_we  = 1'b1;
				end
			end
		endcase
	end
 end
 */
 assign prev_key0_new = {(128){iKeylen}} & {(128){~round_ctr_1}} &
						((round_ctr_0) ? iKey[255:128] : prev_key1_reg);
 assign prev_key0_we = iKeylen & ~round_ctr_1;
 assign prev_key1_new = (iKeylen) ? ({(128){~round_ctr_0}} & ((round_ctr_1) ? iKey[127:0] : {k0, k1, k2, k3})) :
						(round_ctr_0) ? iKey[255:128] : {k0, k1, k2, k3};
 assign prev_key1_we = ~iKeylen | ~round_ctr_0;
 
 /*
 always@(*) begin
	k0 = 32'h0;
	k1 = 32'h0;
	k2 = 32'h0;
	k3 = 32'h0;
	// Generate the specific round keys.
	if(round_key_update) begin
		case(iKeylen)
			1'b0: begin	//AES_128_BIT_KEY
				if(~round_ctr_0) begin
					k0 = w4 ^ trw;
					k1 = w5 ^ w4 ^ trw;
					k2 = w6 ^ w5 ^ w4 ^ trw;
					k3 = w7 ^ w6 ^ w5 ^ w4 ^ trw;
				end
			end
			1'b1: begin	//AES_256_BIT_KEY
				if(~round_ctr_0 & ~round_ctr_1) begin
					if(~round_ctr_reg[0]) begin
						k0 = w0 ^ trw;
						k1 = w1 ^ w0 ^ trw;
						k2 = w2 ^ w1 ^ w0 ^ trw;
						k3 = w3 ^ w2 ^ w1 ^ w0 ^ trw;
					end
					else begin
						k0 = w0 ^ tw;
						k1 = w1 ^ w0 ^ tw;
						k2 = w2 ^ w1 ^ w0 ^ tw;
						k3 = w3 ^ w2 ^ w1 ^ w0 ^ tw;
					end
				end
			end
		endcase
	end
 end
 */
 assign k0 = (iKeylen) ? ( (~round_ctr_0&~round_ctr_1&~round_ctr_reg[0]) ? (w0^trw) : (w0^tw) ) :
						 ( {(32){~round_ctr_0}} & (w4^trw) );
 assign k1 = (iKeylen) ? ( (~round_ctr_0&~round_ctr_1&~round_ctr_reg[0]) ? (w1^w0^trw) : (w1^w0^tw) ) :
						 ( {(32){~round_ctr_0}} & (w5^w4^trw) );
 assign k2 = (iKeylen) ? ( (~round_ctr_0&~round_ctr_1&~round_ctr_reg[0]) ? (w2^w1^w0^trw) : (w2^w1^w0^tw) ) :
						 ( {(32){~round_ctr_0}} & (w6^w5^w4^trw) );
 assign k3 = (iKeylen) ? ( (~round_ctr_0&~round_ctr_1&~round_ctr_reg[0]) ? (w3^w2^w1^w0^trw) : (w3^w2^w1^w0^tw) ) :
						 ( {(32){~round_ctr_0}} & (w7^w6^w5^w4^trw) );
 
 //----------------------------------------------------------------
 // rcon_logic
 //
 // Caclulates the rcon value for the different key expansion
 // iterations.
 //----------------------------------------------------------------
 /*
 always@(*) begin: rconlogic
	reg [7:0] tmp_rcon;
	rcon_new = 8'h00;
	rcon_we  = 1'b0;
	tmp_rcon = {rcon_reg[6:0], 1'b0} ^ (8'h1b & {8{rcon_reg[7]}});

	if(rcon_set) begin
		rcon_new = 8'h8d;
		rcon_we  = 1'b1;
	end
	
	if(rcon_next) begin
		rcon_new = tmp_rcon[7 : 0];
		rcon_we  = 1'b1;
	end
 end
 */
 assign rcon_new[7] = rcon_set | rcon_reg[6];
 assign rcon_new[6] = ~rcon_set & rcon_reg[5];
 assign rcon_new[5] = ~rcon_set & rcon_reg[4];
 assign rcon_new[4] = ~rcon_set & (rcon_reg[3] ^ rcon_reg[7]);
 assign rcon_new[3] = rcon_set | (rcon_reg[2] ^ rcon_reg[7]);
 assign rcon_new[2] = rcon_set | rcon_reg[1];
 assign rcon_new[1] = ~rcon_set & (rcon_reg[0] ^ rcon_reg[7]);
 assign rcon_new[0] = rcon_set | rcon_reg[7];
 assign rcon_we = rcon_set | rcon_next; 
 
 //----------------------------------------------------------------
 // round_ctr
 //
 // The round counter logic with increase and reset.
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
 // key_mem_ctrl
 //
 //
 // The FSM that controls the round key generation.
 //----------------------------------------------------------------
 /*
 always@(*) begin: keymemctrl
	reg [3:0] num_rounds;

	// Default assignments.
	ready_new        = 1'b0;
	ready_we         = 1'b0;
	round_key_update = 1'b0;
	round_ctr_rst    = 1'b0;
	round_ctr_inc    = 1'b0;
	key_mem_ctrl_new = 2'd0;
	key_mem_ctrl_we  = 1'b0;

	if(iKeylen)	num_rounds = AES_256_NUM_ROUNDS;
	else		num_rounds = AES_128_NUM_ROUNDS;

	case(key_mem_ctrl_reg)
		2'd0: begin
			if(iInit) begin
				ready_new        = 1'b0;
				ready_we         = 1'b1;
				round_key_update = 1'b0;
				round_ctr_rst    = 1'b0;
				round_ctr_inc    = 1'b0;
				key_mem_ctrl_new = 2'd1;
				key_mem_ctrl_we  = 1'b1;
			end
		end
		2'd1: begin
			ready_new		 = 1'b0;
			ready_we		 = 1'b0;
			round_key_update = 1'b0;
			round_ctr_rst    = 1'b1;
			round_ctr_inc    = 1'b0;
			key_mem_ctrl_new = 2'd2;
			key_mem_ctrl_we  = 1'b1;
		end
		2'd2: begin
			ready_new		 = 1'b0;
			ready_we		 = 1'b0;
			round_key_update = 1'b1;
			round_ctr_rst    = 1'b0;
			round_ctr_inc    = 1'b1;
			if(round_ctr_reg==num_rounds) begin
				key_mem_ctrl_new = 2'd3;
				key_mem_ctrl_we  = 1'b1;
			end
			else begin
				key_mem_ctrl_new = 2'd0;
				key_mem_ctrl_we  = 1'b0;
			end
		end
		2'd3: begin
			ready_new        = 1'b1;
			ready_we         = 1'b1;
			round_key_update = 1'b0;
			round_ctr_rst    = 1'b0;
			round_ctr_inc    = 1'b0;
			key_mem_ctrl_new = 2'd0;
			key_mem_ctrl_we  = 1'b1;
		end
	endcase // case (key_mem_ctrl_reg)
 end // key_mem_ctrl
 */
 
 /*
 always@(*) begin
	ready_new = 1'b0;
	case(key_mem_ctrl_reg)
		2'd0: begin
			ready_new = 1'b0;
		end
		2'd1: begin
			ready_new = 1'b0;
		end
		2'd2: begin
			ready_new = 1'b0;
		end
		2'd3: begin
			ready_new = 1'b1;
		end
	endcase
 end
 */
 assign ready_new = State3;
 
 /*
 always@(*) begin
	ready_we = 1'b0;
	case(key_mem_ctrl_reg)
		2'd0: begin
			if(iInit)	ready_we = 1'b1;
			else		ready_we = 1'b0;
		end
		2'd1: begin
			ready_we = 1'b0;
		end
		2'd2: begin
			ready_we = 1'b0;
		end
		2'd3: begin
			ready_we = 1'b1;
		end
	endcase
 end
 */
 assign ready_we = (State0 & iInit) | State3;
 
 /*
 always@(*) begin
	round_key_update = 1'b0;
	case(key_mem_ctrl_reg)
		2'd0: begin
			round_key_update = 1'b0;
		end
		2'd1: begin
			round_key_update = 1'b0;
		end
		2'd2: begin
			round_key_update = 1'b1;
		end
		2'd3: begin
			round_key_update = 1'b0;
		end
	endcase
 end
 */
 assign round_key_update = State2;
 
 /*
 always@(*) begin
	round_ctr_rst = 1'b0;
	case(key_mem_ctrl_reg)
		2'd0: begin
			round_ctr_rst = 1'b0;
		end
		2'd1: begin
			round_ctr_rst = 1'b1;
		end
		2'd2: begin
			round_ctr_rst = 1'b0;
		end
		2'd3: begin
			round_ctr_rst = 1'b0;
		end
	endcase
 end
 */
 assign round_ctr_rst = State1;
 
 /*
 always@(*) begin
	round_ctr_inc = 1'b0;
	case(key_mem_ctrl_reg)
		2'd0: begin
			round_ctr_inc = 1'b0;
		end
		2'd1: begin
			round_ctr_inc = 1'b0;
		end
		2'd2: begin
			round_ctr_inc = 1'b1;
		end
		2'd3: begin
			round_ctr_inc = 1'b0;
		end
	endcase
 end
 */
 assign round_ctr_inc = State2;
 
 /*
 always@(*) begin
	key_mem_ctrl_new = 2'd0;
	case(key_mem_ctrl_reg)
		2'd0: begin
			if(iInit)	key_mem_ctrl_new = 2'd1;
			else		key_mem_ctrl_new = 2'd0;
		end
		2'd1: begin
			key_mem_ctrl_new = 2'd2;
		end
		2'd2: begin
			if(round_ctr_reg==num_rounds)	key_mem_ctrl_new = 2'd3;
			else							key_mem_ctrl_new = 2'd0;
		end
		2'd3: begin
			key_mem_ctrl_new = 2'd0;
		end
	endcase
 end
 */
 assign key_mem_ctrl_new[0] = (State0 & iInit) | (State2 & round_ctr_reg_num_rounds);
 assign key_mem_ctrl_new[1] = State1 | (State2 & round_ctr_reg_num_rounds);
 
 /*
 always@(*) begin
	key_mem_ctrl_we = 1'b0;
	case(key_mem_ctrl_reg)
		2'd0: begin
			if(iInit)	key_mem_ctrl_we = 1'b1;
			else		key_mem_ctrl_we = 1'b0;
		end
		2'd1: begin
			key_mem_ctrl_we = 1'b1;
		end
		2'd2: begin
			if(round_ctr_reg==num_rounds)	key_mem_ctrl_we = 1'b1;
			else							key_mem_ctrl_we = 1'b0;
		end
		2'd3: begin
			key_mem_ctrl_we = 1'b1;
		end
	endcase
 end
 */
 assign key_mem_ctrl_we = (State0 & iInit) | State1 | State3 |
						  (State2 & round_ctr_reg_num_rounds);
 
endmodule
