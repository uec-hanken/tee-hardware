//------------------------------------------------------------------------------
//
// modular_subtractor.v
// -----------------------------------------------------------------------------
// Modular subtractor.
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

module modular_subtractor (
	input			iClk,		// system clock
	input			iRstn,		// active-low async reset
	
	input			iEn,		// enable input
	output			oReady,		// ready output
	
	output	[2:0]	oAB_addr,	// index of current A and B words
	output	[2:0]	oN_addr,	// index of current N word
	output	[2:0]	oD_addr,	// index of current D word
	output			oD_wren,	// store current D word now
	
	input	[31:0]	iA,			// A
	input	[31:0]	iB,			// B
	input	[31:0]	iN,			// N
	output	[31:0]	oD			// D = (A - B) mod N
);

 // Word Indices
 reg	[2:0]	index_ab;
 reg	[2:0]	index_n;
 reg	[2:0]	index_d;
 
 // Subtractor
 wire	[31:0]	sub32_d;
 wire			sub32_b_in;
 wire			sub32_b_out;
 
 // Adder
 wire	[31:0]	add32_s;
 wire			add32_c_in;
 wire			add32_c_out;
 
 // FSM
 reg	[20:0]	fsm_shreg;
 
 wire	[7:0]	fsm_shreg_inc_index_ab;
 wire	[7:0]	fsm_shreg_inc_index_n;
 wire	[7:0]	fsm_shreg_store_dif_ab;
 wire	[7:0]	fsm_shreg_store_dif_ab_n;
 wire	[7:0]	fsm_shreg_store_data_d;
 wire	[7:0]	fsm_shreg_inc_index_d;
 wire			fsm_latch_msb_borrow;
 
 wire			inc_index_ab;
 wire			inc_index_n;
 wire			store_dif_ab;
 wire			store_dif_ab_n;
 wire			store_data_d;
 wire			inc_index_d;
 
 // Borrow & Carry Masking Logic
 reg			sub32_b_mask;
 reg			add32_c_mask;
 
 // Borrow Latch Logic
 reg			sub32_borrow_latch;
 
 // Intermediate Results
 reg	[255:0]	d_ab;
 reg	[255:0]	d_ab_n;
 
 // Output
 wire			mux_select_ab_n;
 reg			d_wren_reg;
 reg	[31:0]	d_dout_reg;
 wire	[31:0]	d_dout_mux;
 
 // map registers to output ports
 assign oAB_addr = index_ab;
 assign oN_addr  = index_n;
 assign oD_addr  = index_d;

 // Subtractor
 subtractor32_generic sub32 (
	.iClk	(iClk),
	.iA		(iA),
	.iB		(iB),
	.oD		(sub32_d),
	.iC		(sub32_b_in),
	.oC		(sub32_b_out)
 );

 // Adder
 adder32_generic add32 (
	.iClk	(iClk),
	.iA		(sub32_d),
	.iB		(iN),
	.oS		(add32_s),
	.iC		(add32_c_in),
	.oC		(add32_c_out)
 );

 // FSM
 assign oReady = fsm_shreg[0];

 assign fsm_shreg_inc_index_ab   = fsm_shreg[20:13];
 assign fsm_shreg_inc_index_n    = fsm_shreg[19:12];
 assign fsm_shreg_store_dif_ab   = fsm_shreg[18:11];
 assign fsm_shreg_store_dif_ab_n = fsm_shreg[17:10];
 assign fsm_shreg_store_data_d   = fsm_shreg[9:2];
 assign fsm_shreg_inc_index_d    = fsm_shreg[8:1];
 assign fsm_latch_msb_borrow     = fsm_shreg[11];

 assign inc_index_ab   = |fsm_shreg_inc_index_ab;
 assign inc_index_n    = |fsm_shreg_inc_index_n;
 assign store_dif_ab   = |fsm_shreg_store_dif_ab;
 assign store_dif_ab_n = |fsm_shreg_store_dif_ab_n;
 assign store_data_d   = |fsm_shreg_store_data_d;
 assign inc_index_d    = |fsm_shreg_inc_index_d;

 always@(posedge iClk) begin
	if(~iRstn)		fsm_shreg <= 21'd1;
	else if(oReady)	fsm_shreg <= {iEn, 19'b0, ~iEn};
	else			fsm_shreg <= {1'b0, fsm_shreg[20:1]};
 end

 // Borrow & Carry Masking Logic
 always@(posedge iClk) begin
	sub32_b_mask <= ~(|index_ab);
	add32_c_mask <= ~(|index_n);
 end

 assign sub32_b_in = sub32_b_out & ~sub32_b_mask;
 assign add32_c_in = add32_c_out & ~add32_c_mask;

 // Borrow Latch Logic
 always@(posedge iClk) begin
	if(fsm_latch_msb_borrow)	sub32_borrow_latch <= sub32_b_out;
	else						sub32_borrow_latch <= sub32_borrow_latch;
 end

 // Intermediate Results
 always@(posedge iClk) begin
	if(store_data_d)		d_ab <= {32'b0, d_ab[255:32]};
	else if (store_dif_ab)	d_ab <= {sub32_d, d_ab[255:32]};
	else					d_ab <= d_ab;
 end
 always@(posedge iClk) begin
	if(store_data_d)		d_ab_n <= {32'b0, d_ab_n[255:32]};
	else if(store_dif_ab_n)	d_ab_n <= {add32_s, d_ab_n[255:32]};
	else					d_ab_n <= d_ab_n;
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
	if(oReady)				index_d <= 3'b0;
	else if(inc_index_d)	index_d <= {(3){~(&index_d)}} & (index_d + 1'b1);
	else					index_d <= index_d;
 end

 // Output
 assign mux_select_ab_n = sub32_borrow_latch;
 assign d_dout_mux = (mux_select_ab_n) ? d_ab_n[31:0] : d_ab[31:0];
 assign oD_wren = d_wren_reg;
 assign oD = d_dout_reg;

 always@(posedge iClk) begin
	if(oReady)	d_wren_reg <= 1'b0;
	else		d_wren_reg <= store_data_d;
 end
 always@(posedge iClk) begin
	d_dout_reg <= d_dout_mux;
 end

endmodule
