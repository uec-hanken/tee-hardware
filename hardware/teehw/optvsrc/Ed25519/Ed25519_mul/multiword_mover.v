//------------------------------------------------------------------------------
//
// multiword_mover.v
// -----------------------------------------------------------------------------
// Multi-word data mover.
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

module multiword_mover (
    input			iClk,		// system clock
    input			iRstn,		// active-low async reset

    input			iEn,		// enable input
    output			oReady,		// ready output

    output	[2:0]	oX_addr,	// address of current X word
    output	[2:0]	oY_addr,	// address of current Y word
    output			oY_wren,	// store current Y word

    input	[31:0]	iX,			// current X word
    output	[31:0]	oY			// current Y word
);

 // Word Indices
 reg	[2:0]	index_x;
 reg	[2:0]	index_y;
 
 // FSM
 reg	[9:0]	fsm_shreg;
 wire	[7:0]	fsm_shreg_inc_index_x;
 wire	[7:0]	fsm_shreg_inc_index_y;

 wire			inc_index_x;
 wire			inc_index_y;
 wire			store_word_y;
 
 // Write Enable Logic
 reg			y_wren_reg;
 
 assign fsm_shreg_inc_index_x = fsm_shreg[9:2];
 assign fsm_shreg_inc_index_y = fsm_shreg[8:1];
 
 assign inc_index_x  = |fsm_shreg_inc_index_x;
 assign inc_index_y  = |fsm_shreg_inc_index_y;
 assign store_word_y = |fsm_shreg_inc_index_x;
 
 // Output Mapping
 assign oX_addr = index_x;
 assign oY_addr = index_y;

 // FSM
 assign oReady = fsm_shreg[0];
 
 always@(posedge iClk) begin
	if(~iRstn)		fsm_shreg <= 10'd1;
	else if(oReady)	fsm_shreg <= {iEn, 8'b0, ~iEn};
	else			fsm_shreg <= {1'b0, fsm_shreg[9:1]};
 end

 // Word Index Increment Logic
 always@(posedge iClk) begin
	if(oReady)				index_x <= 3'b0;
	else if(inc_index_x)	index_x <= {(3){~(&index_x)}} & (index_x + 1'b1);
	else					index_x <= index_x;
 end
 always@(posedge iClk) begin
	if(oReady)				index_y <= 3'b0;
	else if(inc_index_y)	index_y <= {(3){~(&index_y)}} & (index_y + 1'b1);
	else					index_y <= index_y;
 end

 // Write Enable Logic
 assign oY_wren = y_wren_reg;

 always@(posedge iClk) begin
	if(~iRstn|oReady)	y_wren_reg <= 1'b0;
	else				y_wren_reg  <= store_word_y;
 end

 // Output Logic
 assign oY = iX;

endmodule
