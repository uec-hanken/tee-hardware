//------------------------------------------------------------------------------
//
// ed25519_uop_worker.v
// -----------------------------------------------------------------------------
// Ed25519 uOP Worker.
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

module ed25519_uop_worker (
	input			iClk,			// system clock
	input			iRstn,			// active-low async reset
	
	input			iEn,			// enable input
	output			oReady,			// ready output
	
	input	[8:0]	iUOP_offset,	// starting offset
	
	input			iFinal_reduce,	// use regular (not double) modulus
	input			iHandle_sign,	// handle sign of x
	input			oValid,     	// produce output    
	
	output	[2:0]	oY_addr,
	output	[31:0]	oY,
	output			oY_wren
);

 reg	[1:0]	fsm_state;
 wire	[1:0]	fsm_state_next;
 wire			State0, State1, State2, State3;

 // Microcode
 reg	[8:0]	uop_addr;
 wire	[23:0]	uop_data;

 wire	[4:0]	uop_data_opcode;
 wire			uop_data_banks;
 wire	[5:0]	uop_data_operand_src1;
 wire	[5:0]	uop_data_operand_src2;
 wire	[5:0]	uop_data_operand_dst;

 wire			uop_data_opcode_is_stop;
 wire			uop_data_opcode_is_mul;
 wire			uop_data_opcode_is_sub;
 wire			uop_data_opcode_is_add;
 wire			uop_data_opcode_is_copy;
 
 // Multi-Word Mover
 reg			mw_mover_ena;
 wire			mw_mover_rdy;
 wire	[2:0]	mw_mover_x_addr;
 wire	[2:0]	mw_mover_y_addr;
 wire	[31:0]	mw_mover_x_din;
 wire	[31:0]	mw_mover_y_dout;
 wire			mw_mover_y_wren;
 
 // Modular Multiplier
 reg			mod_mul_ena;
 wire			mod_mul_rdy;
 wire	[2:0]	mod_mul_a_addr;
 wire	[2:0]	mod_mul_b_addr;
 wire	[2:0]	mod_mul_p_addr;
 wire	[31:0]	mod_mul_a_din;
 wire	[31:0]	mod_mul_b_din;
 wire	[31:0]	mod_mul_p_dout;
 wire			mod_mul_p_wren;
 
 // Modular Adder
 reg			mod_add_ena;
 wire			mod_add_rdy;
 wire	[2:0]	mod_add_ab_addr;
 wire	[2:0]	mod_add_n_addr;
 wire	[2:0]	mod_add_s_addr;
 wire	[31:0]	mod_add_a_din;
 wire	[31:0]	mod_add_b_din;
 wire	[31:0]	mod_add_n_din;
 wire	[31:0]	mod_add_s_dout;
 wire			mod_add_s_wren;
 wire			mod_add_n_addr_ne0, mod_add_n_addr_e7;
 
 // Modular Subtractor
 reg			mod_sub_ena;
 wire			mod_sub_rdy;
 wire	[2:0]	mod_sub_ab_addr;
 wire	[2:0]	mod_sub_n_addr;
 wire	[2:0]	mod_sub_d_addr;
 wire	[31:0]	mod_sub_a_din;
 wire	[31:0]	mod_sub_b_din;
 wire	[31:0]	mod_sub_n_din;
 wire	[31:0]	mod_sub_d_dout;
 wire			mod_sub_d_wren;
 wire			mod_sub_n_addr_ne0;
 
 // Double/Single Modulus
 reg			mod_sub_n_bit_lower;
 reg			mod_add_n_bit_upper;
 reg			mod_add_n_bit_lower0;
 reg			mod_add_n_bit_lower1;
 wire	[2:0]	mod_sub_n_bit_w;
 
 // uOP Completion Detector
 wire			fsm_exit_from_busy;
 
 // Banks
 wire	[2:0]	banks_src1_addr;
 wire	[2:0]	banks_src2_addr;
 wire	[2:0]	banks_dst_addr;
 wire			banks_dst_wren;
 wire	[31:0]	banks_dst_din;
 wire	[31:0]	banks_src1_dout;
 wire	[31:0]	banks_src2_dout;
 
 // Sign Handler
 reg			sign_x_int;
 wire	[31:0]	mw_mover_y_dout_with_x_sign;
 
 // Ready Flag Logic
 reg			rdy_reg;
 
 // Output Logic
 reg	[2:0]	y_addr_reg;
 reg	[31:0]	y_dout_reg;
 reg			y_wren_reg;
 
 assign uop_data_opcode       = uop_data[23:19];
 assign uop_data_banks        = uop_data[18];
 assign uop_data_operand_src1 = uop_data[17:12];
 assign uop_data_operand_src2 = uop_data[11:6];
 assign uop_data_operand_dst  = uop_data[5:0];
 
 assign uop_data_opcode_is_stop = uop_data_opcode[4];
 assign uop_data_opcode_is_mul  = uop_data_opcode[3];
 assign uop_data_opcode_is_sub  = uop_data_opcode[2];
 assign uop_data_opcode_is_add  = uop_data_opcode[1];
 assign uop_data_opcode_is_copy = uop_data_opcode[0];
 
 assign mw_mover_y_dout_with_x_sign = {((&mw_mover_y_addr) ? sign_x_int : mw_mover_y_dout[31]), mw_mover_y_dout[30:0]};
 
 assign State0 = ~fsm_state[1] & ~fsm_state[0];
 assign State1 = ~fsm_state[1] &  fsm_state[0];
 assign State2 =  fsm_state[1] & ~fsm_state[0];
 assign State3 =  fsm_state[1] &  fsm_state[0];
 
 assign mod_add_n_addr_ne0 = |mod_add_n_addr;
 assign mod_add_n_addr_e7 = &mod_add_n_addr;
 assign mod_sub_n_addr_ne0 = |mod_sub_n_addr;
 
 ed25519_microcode_rom microcode_rom (
	.iClk	(iClk),
	.iAddr	(uop_addr),
	.oData	(uop_data)
 );

 // Microcode Address Increment Logic
 always@(posedge iClk) begin
	if(~fsm_state_next[1] & fsm_state_next[0])
			uop_addr <= (State0) ? iUOP_offset : (uop_addr + 1'b1);
	else	uop_addr <= uop_addr;
 end
 
 // Multi-Word Mover
 multiword_mover mw_mover_inst (
	.iClk		(iClk),
	.iRstn		(iRstn),
	.iEn		(mw_mover_ena),
	.oReady		(mw_mover_rdy),
	.oX_addr	(mw_mover_x_addr),
	.oY_addr	(mw_mover_y_addr),
	.oY_wren	(mw_mover_y_wren),
	.iX			(mw_mover_x_din),
	.oY			(mw_mover_y_dout)
 );

 // Modular Multiplier
 curve25519_modular_multiplier mod_mul_inst (
	.iClk		(iClk),
	.iRstn		(iRstn),
	.iEn		(mod_mul_ena),
	.oReady		(mod_mul_rdy),
	.oA_addr	(mod_mul_a_addr),
	.oB_addr	(mod_mul_b_addr),
	.oP_addr	(mod_mul_p_addr),
	.oP_wren	(mod_mul_p_wren),
	.iA			(mod_mul_a_din),
	.iB			(mod_mul_b_din),
	.oP			(mod_mul_p_dout)
 );

 // Modular Adder
 modular_adder mod_add_inst (
	.iClk		(iClk),
	.iRstn		(iRstn),
	.iEn		(mod_add_ena),
	.oReady		(mod_add_rdy),
	.oAB_addr	(mod_add_ab_addr),
	.oN_addr	(mod_add_n_addr),
	.oS_addr	(mod_add_s_addr),
	.oS_wren	(mod_add_s_wren),
	.iA			(mod_add_a_din),
	.iB			(mod_add_b_din),
	.iN			(mod_add_n_din),
	.oS			(mod_add_s_dout)
 );

 // Modular Subtractor
 modular_subtractor mod_sub_inst (
	.iClk		(iClk),
	.iRstn		(iRstn),
	.iEn		(mod_sub_ena),
	.oReady		(mod_sub_rdy),
	.oAB_addr	(mod_sub_ab_addr),
	.oN_addr	(mod_sub_n_addr),
	.oD_addr	(mod_sub_d_addr),
	.oD_wren	(mod_sub_d_wren),
	.iA			(mod_sub_a_din),
	.iB			(mod_sub_b_din),
	.iN			(mod_sub_n_din),
	.oD			(mod_sub_d_dout)
 );

 // Double/Single Modulus
 assign mod_sub_n_din = {26'h3ff_ffff, mod_sub_n_bit_lower, 2'b11, mod_sub_n_bit_lower, 1'b1, mod_sub_n_bit_lower};
 assign mod_add_n_din = {mod_add_n_bit_upper, 25'h1ff_ffff, mod_add_n_bit_lower0, mod_add_n_bit_lower1, 1'b1, mod_add_n_bit_lower0, mod_add_n_bit_lower1, mod_add_n_bit_lower0};

//assign mod_sub_n_bit_w = (~(|mod_add_n_addr)) ? ((iFinal_reduce) ? 3'b101 : 3'b110) :
//						  (&mod_add_n_addr) ? ((iFinal_reduce) ? 3'b011 : 3'b111) : 3'b111;
 assign mod_sub_n_bit_w[2] = ~mod_add_n_addr_ne0 | ~mod_add_n_addr_e7 | ~iFinal_reduce;
 assign mod_sub_n_bit_w[1] = mod_add_n_addr_ne0 | ~iFinal_reduce;
 assign mod_sub_n_bit_w[0] = mod_add_n_addr_ne0 | iFinal_reduce;
 always@(posedge iClk) begin
	{mod_add_n_bit_upper, mod_add_n_bit_lower1, mod_add_n_bit_lower0} <= mod_sub_n_bit_w;
 end
 
 always@(posedge iClk) begin
	if(mod_sub_n_addr_ne0)	mod_sub_n_bit_lower <= 1'b1;
	else					mod_sub_n_bit_lower <= 1'b0;
 end

 // uOP Trigger Logic
 always@(posedge iClk) begin
	if(State2) begin
		mw_mover_ena <= uop_data_opcode_is_copy;
		mod_mul_ena  <= uop_data_opcode_is_mul;
		mod_add_ena  <= uop_data_opcode_is_add;
		mod_sub_ena  <= uop_data_opcode_is_sub;
	end
	else begin
		mw_mover_ena <= 1'b0;
		mod_mul_ena  <= 1'b0;
		mod_add_ena  <= 1'b0;
		mod_sub_ena  <= 1'b0;
	end
 end

 // uOP Completion Detector
 /*
 always@(*) begin
	fsm_exit_from_busy = 0;
	if(uop_data_opcode_is_copy)	fsm_exit_from_busy = ~mw_mover_ena & mw_mover_rdy;
	if(uop_data_opcode_is_mul)	fsm_exit_from_busy = ~mod_mul_ena  & mod_mul_rdy;
	if(uop_data_opcode_is_add)	fsm_exit_from_busy = ~mod_add_ena  & mod_add_rdy;
	if(uop_data_opcode_is_sub)	fsm_exit_from_busy = ~mod_sub_ena  & mod_sub_rdy;
 end
 */
 assign fsm_exit_from_busy = (uop_data_opcode_is_copy) ? (~mw_mover_ena & mw_mover_rdy) :
							 (uop_data_opcode_is_mul) ? (~mod_mul_ena & mod_mul_rdy) :
							 (uop_data_opcode_is_add) ? (~mod_add_ena & mod_add_rdy) :
														(~mod_sub_ena & mod_sub_rdy);
 
 // Banks
 ed25519_banks_array banks_array (
	.iClk			(iClk),
	.iBanks			(uop_data_banks),
	.iSrc1_op		(uop_data_operand_src1),
	.iSrc2_op		(uop_data_operand_src2),
	.iDst_op		(uop_data_operand_dst),
	.iSrc1_addr		(banks_src1_addr),
	.iSrc2_addr		(banks_src2_addr),
	.iDst_addr		(banks_dst_addr),
	.iDst_wren		(banks_dst_wren),
	.oSrc1			(banks_src1_dout),
	.oSrc2			(banks_src2_dout),
	.iDst			(banks_dst_din)
 );

 assign mw_mover_x_din = banks_src1_dout;
 assign mod_mul_a_din  = banks_src1_dout;
 assign mod_mul_b_din  = banks_src2_dout;
 assign mod_add_a_din  = banks_src1_dout;
 assign mod_add_b_din  = banks_src2_dout;
 assign mod_sub_a_din  = banks_src1_dout;
 assign mod_sub_b_din  = banks_src2_dout;
 
 /*
 always@(*) begin
	case(uop_data_opcode)
		5'd1: begin
			banks_src1_addr = mw_mover_x_addr;
			banks_src2_addr = {3{1'bX}};
			banks_dst_addr  = mw_mover_y_addr;
			banks_dst_wren  = mw_mover_y_wren;
			banks_dst_din   = mw_mover_y_dout;
		end
		5'd2: begin
			banks_src1_addr = mod_add_ab_addr;
			banks_src2_addr = mod_add_ab_addr;
			banks_dst_addr  = mod_add_s_addr;
			banks_dst_wren  = mod_add_s_wren;
			banks_dst_din   = mod_add_s_dout;
		end
		5'd4: begin
			banks_src1_addr = mod_sub_ab_addr;
			banks_src2_addr = mod_sub_ab_addr;
			banks_dst_addr  = mod_sub_d_addr;
			banks_dst_wren  = mod_sub_d_wren;
			banks_dst_din   = mod_sub_d_dout;
		end
		5'd8: begin
			banks_src1_addr = mod_mul_a_addr;
			banks_src2_addr = mod_mul_b_addr;
			banks_dst_addr  = mod_mul_p_addr;
			banks_dst_wren  = mod_mul_p_wren;
			banks_dst_din   = mod_mul_p_dout;               
		end
		default: begin
			banks_src1_addr = {3{1'bX}};
			banks_src2_addr = {3{1'bX}};
			banks_dst_addr  = {3{1'bX}};
			banks_dst_wren  = 1'b0;
			banks_dst_din   = {32{1'bX}};
		end
	endcase
 end
 */
 assign banks_src1_addr = (uop_data_opcode[0]) ? mw_mover_x_addr :
						  (uop_data_opcode[1]) ? mod_add_ab_addr :
						  (uop_data_opcode[2]) ? mod_sub_ab_addr : mod_mul_a_addr;
 assign banks_src2_addr = (uop_data_opcode[1]) ? mod_add_ab_addr :
						  (uop_data_opcode[2]) ? mod_sub_ab_addr : mod_mul_b_addr;
 assign banks_dst_addr  = (uop_data_opcode[0]) ? mw_mover_y_addr :
						  (uop_data_opcode[1]) ? mod_add_s_addr  :
						  (uop_data_opcode[2]) ? mod_sub_d_addr  : mod_mul_p_addr;
 assign banks_dst_wren  = (uop_data_opcode[0]) ? mw_mover_y_wren :
						  (uop_data_opcode[1]) ? mod_add_s_wren  :
						  (uop_data_opcode[2]) ? mod_sub_d_wren  :
						  (uop_data_opcode[3] & mod_mul_p_wren);
 assign banks_dst_din   = (uop_data_opcode[0]) ? mw_mover_y_dout :
						  (uop_data_opcode[1]) ? mod_add_s_dout  :
						  (uop_data_opcode[2]) ? mod_sub_d_dout  : mod_mul_p_dout;
 
 // Sign Handler
 always@(posedge iClk) begin
	if(iHandle_sign & mw_mover_y_wren & ~(|mw_mover_y_addr))
			sign_x_int <= mw_mover_y_dout[0];
	else	sign_x_int <= sign_x_int;
 end
    
 // FSM Process
 always@(posedge iClk) begin
	if(~iRstn)	fsm_state <= 2'd0;
	else		fsm_state <= fsm_state_next;
 end

 // FSM Transition Logic
 /*
 always@(*) begin
	fsm_state_next = 2'd0;
	case(fsm_state)
		2'd0:	fsm_state_next = (iEn) ? 2'd1 : 2'd0;
		2'd1:	fsm_state_next = 2'd2;
		2'd2:	fsm_state_next = (uop_data_opcode_is_stop) ? 2'd0 : 2'd3;
		2'd3:	fsm_state_next = (fsm_exit_from_busy) ? 2'd1 : 2'd3;
	endcase
 end
 */
 assign fsm_state_next[0] = (State0 & iEn) | (State2 & ~uop_data_opcode_is_stop) | State3;
 assign fsm_state_next[1] = State1 | (State2 & ~uop_data_opcode_is_stop) | (State3 & ~fsm_exit_from_busy);
 
 // Ready Flag Logic
 assign oReady = rdy_reg;

 always@(posedge iClk) begin
	if(~iRstn)		rdy_reg <= 1'b1;
	else if(State0)	rdy_reg <= ~iEn;
	else if(State2)	rdy_reg <= uop_data_opcode_is_stop;
	else			rdy_reg <= rdy_reg;
 end

 // Output Logic
 assign oY_addr = y_addr_reg;
 assign oY      = y_dout_reg;
 assign oY_wren = y_wren_reg;

 always@(posedge iClk) begin
	if(oValid & mw_mover_y_wren) begin
		y_addr_reg <= mw_mover_y_addr;
		y_dout_reg <= mw_mover_y_dout_with_x_sign;
		y_wren_reg <= 1'b1;
	end
	else begin
		y_addr_reg <= 3'b000;
		y_dout_reg <= 32'h00000000;
		y_wren_reg <= 1'b0;
	end
 end

endmodule
