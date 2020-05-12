//------------------------------------------------------------------------------
//
// modular_adder.v
// -----------------------------------------------------------------------------
// Modular adder.
//
// Authors: Pavel Shatov
//
// Copyright (c) 2016, 2018 NORDUnet A/S
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

module modular_adder (
	input			iClk,		// system clock
	input			iRstn,		// active-low async reset
	
	input			iEn,		// enable input
	output			oReady,		// ready output
	
	output	[2:0]	oAB_addr,	// index of current A and B words
	output	[2:0]	oN_addr,	// index of current N word
	output	[2:0]	oS_addr,	// index of current S word
	output			oS_wren,	// store current S word now
	
	input	[31:0]	iA,			// A
	input	[31:0]	iB,			// B
	input	[31:0]	iN,			// N
	output	[31:0]	oS			// S = (A + B) mod N
);

 // Word Indices
 reg	[2:0]	index_ab;
 reg	[2:0]	index_n;
 reg	[2:0]	index_s;
 
 // Adder
 wire	[31:0]	add32_s;
 wire			add32_c_in;
 wire			add32_c_out;
 
 // Subtractor
 wire	[31:0]	sub32_d;
 wire			sub32_b_in;
 wire			sub32_b_out;
 
 // FSM
 reg	[20:0]	fsm_shreg;
 
 wire	[7:0]	fsm_shreg_inc_index_ab;
 wire	[7:0]	fsm_shreg_inc_index_n;
 wire	[7:0]	fsm_shreg_store_sum_ab;
 wire	[7:0]	fsm_shreg_store_sum_ab_n;
 wire	[7:0]	fsm_shreg_store_data_s;
 wire	[7:0]	fsm_shreg_inc_index_s;
 wire			fsm_latch_msb_carry;
 wire			fsm_latch_msb_borrow;
 
 wire			inc_index_ab;
 wire			inc_index_n;
 wire			store_sum_ab;
 wire			store_sum_ab_n;
 wire			store_data_s;
 wire			inc_index_s;
 
 // Carry & Borrow Latch Logic
 reg			add32_carry_latch;
 reg			sub32_borrow_latch;
 
 // Carry & Borrow Masking Logic
 reg			add32_c_mask;
 reg			sub32_b_mask;
 
 // Intermediate Results
 reg	[255:0]	s_ab;
 reg	[255:0]	s_ab_n;
 
 // Output
 wire			mux_select_ab;
 reg			s_wren_reg;
 reg	[31:0]	s_dout_reg;
 wire	[31:0]	s_dout_mux;
 
 assign fsm_shreg_inc_index_ab   = fsm_shreg[20:13];
 assign fsm_shreg_inc_index_n    = fsm_shreg[19:12];
 assign fsm_shreg_store_sum_ab   = fsm_shreg[18:11];
 assign fsm_shreg_store_sum_ab_n = fsm_shreg[17:10];
 assign fsm_shreg_store_data_s   = fsm_shreg[9:2];
 assign fsm_shreg_inc_index_s    = fsm_shreg[8:1];
 assign fsm_latch_msb_carry      = fsm_shreg[11];
 assign fsm_latch_msb_borrow     = fsm_shreg[10];
 
 assign inc_index_ab   = |fsm_shreg_inc_index_ab;
 assign inc_index_n    = |fsm_shreg_inc_index_n;
 assign store_sum_ab   = |fsm_shreg_store_sum_ab;
 assign store_sum_ab_n = |fsm_shreg_store_sum_ab_n;
 assign store_data_s   = |fsm_shreg_store_data_s;
 assign inc_index_s    = |fsm_shreg_inc_index_s;
 
 assign mux_select_ab = sub32_borrow_latch & ~add32_carry_latch;
 assign s_dout_mux = (mux_select_ab) ? s_ab[31:0] : s_ab_n[31:0];
 
 // map registers to output ports
 assign oAB_addr = index_ab;
 assign oN_addr  = index_n;
 assign oS_addr  = index_s;

 // Adder
 adder32_generic add32 (
	.iClk	(iClk),
	.iA		(iA),
	.iB		(iB),
	.oS		(add32_s),
	.iC		(add32_c_in),
	.oC		(add32_c_out)
 );

 // Subtractor
 subtractor32_generic sub (
	.iClk	(iClk),
	.iA		(add32_s),
	.iB		(iN),
	.oD		(sub32_d),
	.iC		(sub32_b_in),
	.oC		(sub32_b_out)
 );

 // FSM
 assign oReady = fsm_shreg[0];
 
 always@(posedge iClk) begin
	if(~iRstn)		fsm_shreg <= 21'd1;
	else if(oReady)	fsm_shreg <= {iEn, 19'b0, ~iEn};
	else			fsm_shreg <= {1'b0, fsm_shreg[20:1]};
 end

 // Carry & Borrow Masking Logic
 always@(posedge iClk) begin
	add32_c_mask <= ~(|index_ab);
	sub32_b_mask <= ~(|index_n);
 end

 assign add32_c_in = add32_c_out & ~add32_c_mask;
 assign sub32_b_in = sub32_b_out & ~sub32_b_mask;
 
 // Carry & Borrow Latch Logic
 always@(posedge iClk) begin
	if(fsm_latch_msb_carry)		add32_carry_latch <= add32_c_out;
	else						add32_carry_latch <= add32_carry_latch;
 end
 always@(posedge iClk) begin
	if(fsm_latch_msb_borrow)	sub32_borrow_latch <= sub32_b_out;
	else						sub32_borrow_latch <= sub32_borrow_latch;
 end

 // Intermediate Results
 always@(posedge iClk) begin
	if(store_data_s)		s_ab <= {32'b0, s_ab[255:32]};
	else if(store_sum_ab)	s_ab <= {add32_s, s_ab[255:32]};
	else					s_ab <= s_ab;
 end
 always@(posedge iClk) begin
	if(store_data_s)		s_ab_n <= {32'b0, s_ab_n[255:32]};
	else if(store_sum_ab_n)	s_ab_n <= {sub32_d, s_ab_n[255:32]};
	else					s_ab_n <= s_ab_n;
 end

 // Word Index Increment Logic
 always@(posedge iClk) begin
	if(oReady)				index_ab <= 3'b0;
	else if(inc_index_ab)	index_ab <= {(3){~(&index_ab)}} & (index_ab + 1'b1);
	else					index_ab <= index_ab;
 end
 always@(posedge iClk) begin
	if(oReady)				index_n <= 3'b0;
	else if(inc_index_n)	index_n <= {(3){~(&index_n)}} & (index_n + 1'b1);
	else					index_n <= index_n;
 end
 always@(posedge iClk) begin
	if(oReady)				index_s <= 3'b0;
	else if(inc_index_s)	index_s <= {(3){~(&index_s)}} & (index_s + 1'b1);
	else					index_s <= index_s;
 end

 // Output
 assign oS_wren = s_wren_reg;
 assign oS = s_dout_reg;

 always@(posedge iClk) begin
	if(oReady)	s_wren_reg <= 1'b0;
	else		s_wren_reg <= store_data_s;
 end
 always@(posedge iClk) begin
	s_dout_reg <= s_dout_mux;
 end

endmodule
