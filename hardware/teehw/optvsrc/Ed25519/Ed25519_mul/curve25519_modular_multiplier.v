//------------------------------------------------------------------------------
//
// Curve25519_modular_multiplier.v
// -----------------------------------------------------------------------------
// Curve25519 Modular Multiplier.
//
// Authors: Pavel Shatov
//
// Copyright (c) 2015-2016, 2018 NORDUnet A/S
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

module curve25519_modular_multiplier (
	input			iClk,		// system clock
	input			iRstn,		// active-low async reset
	
	input			iEn,		// enable input
	output			oReady,		// ready output
	
	output	[2:0]	oA_addr,	// index of current A word
	output	[2:0]	oB_addr,	// index of current B word
	output	[2:0]	oP_addr,	// index of current P word
	
	output			oP_wren,	// store current P word now
	
	input	[31:0]	iA,			// current word of A
	input	[31:0]	iB,			// current word of B
	output	[31:0]	oP			// current word of P
);

 // Word Indices
 reg	[2:0]	index_a;
 reg	[2:0]	index_b;
 
 // FSM
 reg	[68:0]	fsm_shreg;

 wire	[7:0]	fsm_shreg_increment_index_a;
 wire	[15:0]	fsm_shreg_decrement_index_b;
 wire	[14:0]	fsm_shreg_store_msb_si;
 wire 			fsm_shreg_store_lsb_si;
 wire	[14:0]	fsm_shreg_shift_si;
 wire			fsm_shreg_mask_sum_cw1;
 wire	[7:0]	fsm_shreg_store_lsb_c;
 wire	[7:0]	fsm_shreg_store_msb_c;
 wire			fsm_shreg_mask_b_r3;
 wire			fsm_shreg_calculate_carry_msb_s1;
 wire	[7:0]	fsm_shreg_store_lsb_s1;
 wire	[7:0]	fsm_shreg_shift_s1;
 wire			fsm_shreg_change_lsb_b_p;
 wire			fsm_shreg_select_s2_or_pn;
 wire	[7:0]	fsm_shreg_update_p_dout;

 wire			flag_increment_index_a;
 wire			flag_decrement_index_b;
 wire			flag_store_msb_si;
 wire			flag_store_lsb_si;
 wire			flag_shift_si;
 wire			flag_mask_sum_cw1;
 wire			flag_store_lsb_c;
 wire			flag_store_msb_c;
 wire			flag_mask_b_r3;
 wire			flag_calculate_carry_msb_s1;
 wire			flag_store_lsb_s1;
 wire			flag_shift_s1;
 wire			flag_change_lsb_b_p;
 wire			flag_select_s2_or_pn;
 wire			flag_update_p_dout;

 reg			flag_store_word_a;
 reg			flag_enable_mac_ab;
 reg			flag_delay_msb_c;
 reg			flag_mask_a_s2;
 reg			flag_mask_b_out_p;
 reg			flag_store_s2;
 reg			flag_store_pn;
	
 reg			index_b_ff;
 reg	[255:0]	buf_a_wide;
 reg	[15:0]	buf_b_narrow;
 reg	[15:0]	mac_clear;
 wire	[46:0]	mac_accum[0:15];
 
 reg	[704:0]	si_msb;
 reg	[751:0]	si_lsb;
 wire	[704:0]	si_msb_new;
 wire	[751:0]	si_lsb_new;
 
 wire	[46:0]	add47_cw0_s;
 wire	[46:0]	add47_cw1_s;
 wire	[14:0]	add47_cw1_s_masked;
 
 reg	[30:0]	si_prev_dly;
 reg	[15:0]	si_next_dly;
 
 wire	[46:0]	add47_cw0_a;
 wire	[46:0]	add47_cw0_b;
 wire	[46:0]	add47_cw1_a;
 wire	[46:0]	add47_cw1_b;
 
 wire	[31:0]	c_word_lower;
 
 wire	[46:0]	add47_r0_s;
 wire	[46:0]	add47_r1_s;
 wire	[46:0]	add47_r2_s;
 wire	[46:0]	add47_r3_s;
 
 reg	[255:0]	c_lsb_s1_shreg;
 reg	[31:0]	c_msb_latch;
 wire	[46:0]	add47_r3_b_masked;
 
 reg	[4:0]	c_msb_latch_upper_dly;
 reg	[31:0]	c_lsb_shreg_lower_dly;
 reg	[11:0]	carry_msb_s1;
 
 wire	[46:0]	add47_s2_a_masked;
 wire	[46:0]	add47_s2_s;
 
 reg			sub32_b_bit;
 wire	[31:0]	sub32_b;
 
 wire	[31:0]	sub32_pn_d;
 wire			sub32_b_in;
 wire			sub32_b_out;
 
 wire	[31:0]	add47_r3_s_lower;
 
 reg 			sel_pn; // 0: output in S2, 1: output in PN
 reg	[31:0]	p_dout_reg;
 
 reg			p_wren_reg;
 reg	[2:0]	p_addr_reg;
 
 // map registers to output ports
 assign oA_addr = index_a;
 assign oB_addr = index_b;
 
 assign oReady = fsm_shreg[0];
 
 assign fsm_shreg_increment_index_a      = fsm_shreg[68:61];
 assign fsm_shreg_decrement_index_b      = fsm_shreg[60:45];
 assign fsm_shreg_store_msb_si           = fsm_shreg[58:44];
 assign fsm_shreg_store_lsb_si           = fsm_shreg[43];
 assign fsm_shreg_shift_si               = fsm_shreg[42:28];
 assign fsm_shreg_mask_sum_cw1           = fsm_shreg[41];
 assign fsm_shreg_store_lsb_c            = fsm_shreg[40:33];
 assign fsm_shreg_store_msb_c            = fsm_shreg[32:25];
 assign fsm_shreg_mask_b_r3              = fsm_shreg[29];
 assign fsm_shreg_calculate_carry_msb_s1 = fsm_shreg[20];
 assign fsm_shreg_store_lsb_s1           = fsm_shreg[28:21];
 assign fsm_shreg_shift_s1               = fsm_shreg[19:12];
 assign fsm_shreg_change_lsb_b_p         = fsm_shreg[19];
 assign fsm_shreg_select_s2_or_pn        = fsm_shreg[10];
 assign fsm_shreg_update_p_dout          = fsm_shreg[9:2];
 
 assign flag_increment_index_a      = |fsm_shreg_increment_index_a;
 assign flag_decrement_index_b      = |fsm_shreg_decrement_index_b;
 assign flag_store_msb_si           = |fsm_shreg_store_msb_si;
 assign flag_store_lsb_si           = |fsm_shreg_store_lsb_si;
 assign flag_shift_si               = |fsm_shreg_shift_si;
 assign flag_mask_sum_cw1           = |fsm_shreg_mask_sum_cw1;
 assign flag_store_lsb_c            = |fsm_shreg_store_lsb_c;
 assign flag_store_msb_c            = |fsm_shreg_store_msb_c;
 assign flag_mask_b_r3              = |fsm_shreg_mask_b_r3;
 assign flag_calculate_carry_msb_s1 = |fsm_shreg_calculate_carry_msb_s1;
 assign flag_store_lsb_s1           = |fsm_shreg_store_lsb_s1;
 assign flag_shift_s1               = |fsm_shreg_shift_s1;
 assign flag_change_lsb_b_p         = |fsm_shreg_change_lsb_b_p;
 assign flag_select_s2_or_pn        = |fsm_shreg_select_s2_or_pn;
 assign flag_update_p_dout          = |fsm_shreg_update_p_dout;
 
 always@(posedge iClk) begin
	if(~iRstn) begin
		flag_store_word_a  <= 1'b0;
		flag_enable_mac_ab <= 1'b0;
		flag_delay_msb_c   <= 1'b0;
		flag_mask_a_s2     <= 1'b0;
		flag_mask_b_out_p  <= 1'b0;
		flag_store_s2      <= 1'b0;
		flag_store_pn      <= 1'b0;

	end
	else begin
		flag_store_word_a  <= flag_increment_index_a;
		flag_enable_mac_ab <= flag_decrement_index_b;
		flag_delay_msb_c   <= flag_store_msb_c;
		flag_mask_a_s2     <= flag_calculate_carry_msb_s1;
		flag_mask_b_out_p  <= flag_change_lsb_b_p;
		flag_store_s2      <= flag_shift_s1;
		flag_store_pn      <= flag_store_s2;
	end
 end

 // FSM Logic
 always@(posedge iClk) begin
	if(~iRstn)		fsm_shreg <= 69'd1;
	else if(oReady)	fsm_shreg <= {iEn, 67'b0, ~iEn};
	else			fsm_shreg <= {1'b0, fsm_shreg[68:1]};
 end

 // A Word Index Increment Logic
 always@(posedge iClk) begin
	if(oReady)	index_a <= 3'd0;
	else if(flag_increment_index_a)
				index_a <= {(3){~(index_a[2] & index_a[1] & index_a[0])}} & (index_a + 1'b1);
	else		index_a <= index_a;
 end

 // B Word Index Decrement Logic
 always@(posedge iClk) begin
	if(oReady)	index_b <= 3'd7;
	else if(flag_decrement_index_b & ~index_b_ff)
				index_b <= {(3){(~index_b[2] & ~index_b[1] & ~index_b[0])}} | (index_b - 1'b1);
	else		index_b <= index_b;
 end

 // Wide Operand Buffer
 // B Word Splitter
 // 0: store the upper 16-bit part of the current B word
 // 1: store the lower 16-bit part of the current B word
 always@(posedge iClk) begin
	if(flag_decrement_index_b)
			index_b_ff <= ~index_b_ff;
	else	index_b_ff <= 1'b0;
 end

 // Narrow Operand Buffer
 always@(posedge iClk) begin
	if(flag_decrement_index_b)
			buf_b_narrow <= ((~index_b_ff) ? iB[31:16] : iB[15:0]);
	else	buf_b_narrow <= buf_b_narrow;
 end

 // MAC Clear Logic
 always@(posedge iClk) begin
	if(~flag_enable_mac_ab)	mac_clear <= 16'hffff;
	else if(mac_clear[0])	mac_clear <= 16'h0002;
	else if(mac_clear[15])	mac_clear <= 16'hffff;
	else					mac_clear <= {mac_clear[14:0], 1'b0};
 end

 // MAC Array
 /*generate for (i=0; i<16; i=i+1)
	begin: gen_mac16_array
		mac16_generic mac16_inst (
			.iClk	(iClk),
			.iEn	(flag_enable_mac_ab),
			.iRst	(mac_clear[i]),
			.iA		(buf_a_wide[16 * i +: 16]),
			.iB		(buf_b_narrow),
			.oS		(mac_accum[i])
		);
	end
 endgenerate
 */
 mac16_generic mac16_inst0 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[0]),
	.iA		(buf_a_wide[15:0]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[0])
 );
 mac16_generic mac16_inst1 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[1]),
	.iA		(buf_a_wide[31:16]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[1])
 );
 mac16_generic mac16_inst2 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[2]),
	.iA		(buf_a_wide[47:32]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[2])
 );
 mac16_generic mac16_inst3 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[3]),
	.iA		(buf_a_wide[63:48]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[3])
 );
 mac16_generic mac16_inst4 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[4]),
	.iA		(buf_a_wide[79:64]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[4])
 );
 mac16_generic mac16_inst5 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[5]),
	.iA		(buf_a_wide[95:80]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[5])
 );
 mac16_generic mac16_inst6 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[6]),
	.iA		(buf_a_wide[111:96]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[6])
 );
 mac16_generic mac16_inst7 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[7]),
	.iA		(buf_a_wide[127:112]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[7])
 );
 mac16_generic mac16_inst8 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[8]),
	.iA		(buf_a_wide[143:128]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[8])
 );
 mac16_generic mac16_inst9 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[9]),
	.iA		(buf_a_wide[159:144]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[9])
 );
 mac16_generic mac16_inst10 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[10]),
	.iA		(buf_a_wide[175:160]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[10])
 );
 mac16_generic mac16_inst11 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[11]),
	.iA		(buf_a_wide[191:176]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[11])
 );
 mac16_generic mac16_inst12 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[12]),
	.iA		(buf_a_wide[207:192]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[12])
 );
 mac16_generic mac16_inst13 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[13]),
	.iA		(buf_a_wide[223:208]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[13])
 );
 mac16_generic mac16_inst14 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[14]),
	.iA		(buf_a_wide[239:224]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[14])
 );
 mac16_generic mac16_inst15 (
	.iClk	(iClk),
	.iEn	(flag_enable_mac_ab),
	.iRst	(mac_clear[15]),
	.iA		(buf_a_wide[255:240]),
	.iB		(buf_b_narrow),
	.oS		(mac_accum[15])
 );

 // Intermediate Words
 /*
 generate for (i=0; i<16; i=i+1)
	begin: gen_si_lsb_new
		assign si_lsb_new[47*i+:47] = mac_accum[15-i];
	end
 endgenerate
 */
 assign si_lsb_new[46:0]    = mac_accum[15];
 assign si_lsb_new[93:47]   = mac_accum[14];
 assign si_lsb_new[140:94]  = mac_accum[13];
 assign si_lsb_new[187:141] = mac_accum[12];
 assign si_lsb_new[234:188] = mac_accum[11];
 assign si_lsb_new[281:235] = mac_accum[10];
 assign si_lsb_new[328:282] = mac_accum[9];
 assign si_lsb_new[375:329] = mac_accum[8];
 assign si_lsb_new[422:376] = mac_accum[7];
 assign si_lsb_new[469:423] = mac_accum[6];
 assign si_lsb_new[516:470] = mac_accum[5];
 assign si_lsb_new[563:517] = mac_accum[4];
 assign si_lsb_new[610:564] = mac_accum[3];
 assign si_lsb_new[657:611] = mac_accum[2];
 assign si_lsb_new[704:658] = mac_accum[1];
 assign si_lsb_new[751:705] = mac_accum[0];
 
 /*
 generate for (i=1; i<16; i=i+1)
	begin: gen_si_msb_new
		assign si_msb_new[47*(15-i)+:47] = (mac_clear[i]) ? mac_accum[i] : si_msb[47*(15-i)+:47];
	end
 endgenerate
 */
 assign si_msb_new[704:658] = (mac_clear[1])  ? mac_accum[1]  : si_msb[704:658];
 assign si_msb_new[657:611] = (mac_clear[2])  ? mac_accum[2]  : si_msb[657:611];
 assign si_msb_new[610:564] = (mac_clear[3])  ? mac_accum[3]  : si_msb[610:564];
 assign si_msb_new[563:517] = (mac_clear[4])  ? mac_accum[4]  : si_msb[563:517];
 assign si_msb_new[516:470] = (mac_clear[5])  ? mac_accum[5]  : si_msb[516:470];
 assign si_msb_new[469:423] = (mac_clear[6])  ? mac_accum[6]  : si_msb[469:423];
 assign si_msb_new[422:376] = (mac_clear[7])  ? mac_accum[7]  : si_msb[422:376];
 assign si_msb_new[375:329] = (mac_clear[8])  ? mac_accum[8]  : si_msb[375:329];
 assign si_msb_new[328:282] = (mac_clear[9])  ? mac_accum[9]  : si_msb[328:282];
 assign si_msb_new[281:235] = (mac_clear[10]) ? mac_accum[10] : si_msb[281:235];
 assign si_msb_new[234:188] = (mac_clear[11]) ? mac_accum[11] : si_msb[234:188];
 assign si_msb_new[187:141] = (mac_clear[12]) ? mac_accum[12] : si_msb[187:141];
 assign si_msb_new[140:94]  = (mac_clear[13]) ? mac_accum[13] : si_msb[140:94];
 assign si_msb_new[93:47]   = (mac_clear[14]) ? mac_accum[14] : si_msb[93:47];
 assign si_msb_new[46:0]    = (mac_clear[15]) ? mac_accum[15] : si_msb[46:0];
 
 always@(posedge iClk) begin
	if(flag_shift_si)			si_msb <= {94'b0, si_msb[704:94]};
	else if(flag_store_msb_si)	si_msb <= si_msb_new;
	else						si_msb <= si_msb;
 end
 
 always@(posedge iClk) begin
	if(flag_shift_si)			si_lsb <= {si_msb[93:0], si_lsb[751:94]};
	else if(flag_store_lsb_si)	si_lsb <= si_lsb_new;
	else						si_lsb <= si_lsb;
 end

 // Accumulators
 assign add47_cw1_s_masked = {(15){~flag_mask_sum_cw1}} & add47_cw1_s[46:32];

 // cw0, cw1
 always@(posedge iClk) begin
	if(flag_shift_si)	si_prev_dly <= si_lsb[93:63];
	else				si_prev_dly <= 31'b0;
 end
 
 always@(posedge iClk) begin
	si_next_dly <= si_lsb[47+:16];
 end

 assign add47_cw0_a = si_lsb[46:0];
 assign add47_cw0_b = {16'b0, si_prev_dly};
 assign add47_cw1_a = add47_cw0_s;
 assign add47_cw1_b = {15'b0, si_next_dly, 1'b0, add47_cw1_s_masked};
 
 adder47_generic add47_cw0_inst (
	.iClk	(iClk),
	.iA		(add47_cw0_a),
	.iB		(add47_cw0_b),
	.oS		(add47_cw0_s)
 );

 adder47_generic add47_cw1_inst (
	.iClk	(iClk),
	.iA		(add47_cw1_a),
	.iB		(add47_cw1_b),
	.oS		(add47_cw1_s)
 );

 // Full-Size Product
 assign c_word_lower = add47_cw1_s[31:0];
 assign add47_r3_b_masked = {32'b0, {(15){~flag_mask_b_r3}} & add47_r3_s[46:32]};

 always@(posedge iClk) begin
	if(flag_store_msb_c)	c_msb_latch <= c_word_lower;
	else					c_msb_latch <= 32'b0;
 end

 always@(posedge iClk) begin
	if(flag_delay_msb_c)	c_msb_latch_upper_dly <= c_msb_latch[31:27];
	else					c_msb_latch_upper_dly <= 5'b0;
 end         

 always@(posedge iClk) begin
	if(flag_store_msb_c)	c_lsb_shreg_lower_dly <= c_lsb_s1_shreg[31:0];
	else					c_lsb_shreg_lower_dly <= 32'b0;
 end

 always@(posedge iClk) begin
	if(flag_calculate_carry_msb_s1)
			carry_msb_s1 <= 12'd38 * {6'b0, add47_r3_s[5:0]};
	else	carry_msb_s1 <= carry_msb_s1;
 end

 adder47_generic add47_r0 (
	.iClk	(iClk),
	.iA		({15'b0, c_msb_latch[30:0], c_msb_latch_upper_dly[4]}),
	.iB		({15'b0, c_msb_latch[29:0], c_msb_latch_upper_dly[4:3]}),
	.oS		(add47_r0_s)
 );
 adder47_generic add47_r1 (
	.iClk	(iClk),
	.iA		({15'b0, c_msb_latch[26:0], c_msb_latch_upper_dly[4:0]}),
	.iB		({15'b0, c_lsb_shreg_lower_dly}),
	.oS		(add47_r1_s)
 );
 adder47_generic add47_r2 (
	.iClk	(iClk),
	.iA		(add47_r0_s),
	.iB		(add47_r1_s),
	.oS		(add47_r2_s)
 );
 adder47_generic add47_r3 (
	.iClk	(iClk),
	.iA		(add47_r2_s),
	.iB		(add47_r3_b_masked),
	.oS		(add47_r3_s)
 );
 adder47_generic add47_s2 (
	.iClk	(iClk),
	.iA		(add47_s2_a_masked),
	.iB		({15'b0, c_lsb_s1_shreg[31:0]}),
	.oS		(add47_s2_s)
 );
 
 assign add47_s2_a_masked = {32'b0, (flag_mask_a_s2) ? {3'b000, carry_msb_s1} : add47_s2_s[46:32]};   
 assign sub32_b = {26'h3ffffff, sub32_b_bit, 2'b11, sub32_b_bit, 1'b1, sub32_b_bit};

 always@(posedge iClk) begin
	if(~fsm_shreg_change_lsb_b_p)	sub32_b_bit <= 1'b1;
	else							sub32_b_bit <= 1'b0;
 end
 
 assign sub32_b_in = sub32_b_out & ~flag_mask_b_out_p;

 subtractor32_generic sub32_pn (
	.iClk	(iClk),
	.iA		(add47_s2_s[31:0]),
	.iB		(sub32_b),
	.oD		(sub32_pn_d),
	.iC		(sub32_b_in),
	.oC		(sub32_b_out)
 );   

 assign add47_r3_s_lower = add47_r3_s[31:0];

 always@(posedge iClk) begin
	if(flag_store_word_a)		buf_a_wide <= {buf_a_wide[223:16], {iA[15:0], iA[31:16]}, buf_a_wide[239:224]};
	else if(flag_enable_mac_ab)	buf_a_wide <= {buf_a_wide[239:0], buf_a_wide[255:240]};
	else if(flag_store_s2)		buf_a_wide <= {add47_s2_s[31:0], buf_a_wide[255:32]};
	else if(flag_update_p_dout)	buf_a_wide <= {32'b0, buf_a_wide[255:32]};
	else						buf_a_wide <= buf_a_wide;
 end

 always@(posedge iClk) begin
	if(flag_store_lsb_c)					c_lsb_s1_shreg <= {c_word_lower, c_lsb_s1_shreg[255:32]};
	else if(flag_store_lsb_s1)				c_lsb_s1_shreg <= {add47_r3_s_lower, c_lsb_s1_shreg[255:32]};
	else if(flag_store_pn)					c_lsb_s1_shreg <= {sub32_pn_d, c_lsb_s1_shreg[255:32]};
	else if(flag_store_msb_c|flag_shift_s1)	c_lsb_s1_shreg <= {32'b0, c_lsb_s1_shreg[255:32]};
	else if(flag_update_p_dout)				c_lsb_s1_shreg <= {32'b0, c_lsb_s1_shreg[255:32]};
	else									c_lsb_s1_shreg <= c_lsb_s1_shreg;
 end

 always@(posedge iClk) begin
	if(flag_select_s2_or_pn)	sel_pn <= sub32_b_out & add47_s2_s[0];
	else						sel_pn <= sel_pn;
 end

 assign oP = p_dout_reg;

 always@(posedge iClk) begin
	if(flag_update_p_dout)	p_dout_reg <= (sel_pn) ? c_lsb_s1_shreg[31:0] : buf_a_wide[31:0];
	else					p_dout_reg <= 32'b0;
 end

 assign oP_wren = p_wren_reg;

 always@(posedge iClk) begin
	if(~iRstn)	p_wren_reg <= 1'b0;
	else		p_wren_reg <= flag_update_p_dout;
 end
 
 assign oP_addr = p_addr_reg;

 always@(posedge iClk) begin
	if(p_wren_reg)	p_addr_reg <= {(3){~(p_addr_reg[2] & p_addr_reg[1] & p_addr_reg[0])}} & (p_addr_reg + 1'b1);
	else			p_addr_reg <= 3'b0;
 end
        
endmodule
