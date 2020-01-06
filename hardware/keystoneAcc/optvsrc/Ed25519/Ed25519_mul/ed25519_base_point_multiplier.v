//------------------------------------------------------------------------------
//
// ed25519_base_point_multiplier.v
// -----------------------------------------------------------------------------
// Ed25519 base point scalar multiplier.
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

module ed25519_base_point_multiplier (
	input			iClk,	// system clock
	input			iRstn,	// active-low async reset
	
	input			iEn,	// enable input
	output			oReady,	// ready output
	
	output	[2:0]	oK_addr,
	output	[2:0]	oQy_addr,
	output			oQy_wren,
	input	[31:0]	iK,
	output	[31:0]	oQy
);

 // FSM
 reg	[4:0]	fsm_state;
 wire	[4:0]	fsm_state_next;
 wire			State0,  State1,  State2,  State3,
				State4,  State5,  State6,  State7,
				State8,  State9,  State10, State11,
				State12, State13, State14, State15,
				State16, State17, State18, State19,
				State20, State21;
 wire			State_odd;
 wire			State511, State911, State1314, State121314, State1315, State1719;
 wire			State3_cond, State3_cond_n, State7_cond, State7_cond_n;
 wire			State5_State7condn;
 wire			nextState1, nextState3, nextState7;
 
 // Round Counter
 reg	[7:0]	bit_counter;
 wire	[7:0]	bit_counter_p1;
 wire	[7:0]	bit_counter_next;
 wire			bit_counter_s255, bit_counter_ne0;
 
 // Worker Trigger Logic
 reg			worker_trig;
 wire			worker_done;
 wire			fsm_wait_done;
 
 // Final Round Detection Logic
 //wire	[3:0]	fsm_state_after_round;
 reg	[31:0]	k_din_shreg;
 wire	[4:0]	k_bit_index;
 wire			k_bit_index_e0;
 
 // Worker Offset Logic
 reg	[8:0]	worker_offset;
 wire	[8:0]	worker_offset_w;
 
 // Ready Flag Logic
 reg			rdy_reg;
 
 // Worker
 wire			worker_final_reduce;
 wire			worker_handle_sign;
 wire			worker_output_now;
 
 
 
 assign State0  = ~fsm_state[4] & ~fsm_state[3] & ~fsm_state[2] & ~fsm_state[1] & ~fsm_state[0];
 assign State1  = ~fsm_state[4] & ~fsm_state[3] & ~fsm_state[2] & ~fsm_state[1] &  fsm_state[0];
 assign State2  = ~fsm_state[4] & ~fsm_state[3] & ~fsm_state[2] &  fsm_state[1] & ~fsm_state[0];
 assign State3  = ~fsm_state[4] & ~fsm_state[3] & ~fsm_state[2] &  fsm_state[1] &  fsm_state[0];
 assign State4  = ~fsm_state[4] & ~fsm_state[3] &  fsm_state[2] & ~fsm_state[1] & ~fsm_state[0];
 assign State5  = ~fsm_state[4] & ~fsm_state[3] &  fsm_state[2] & ~fsm_state[1] &  fsm_state[0];
 assign State6  = ~fsm_state[4] & ~fsm_state[3] &  fsm_state[2] &  fsm_state[1] & ~fsm_state[0];
 assign State7  = ~fsm_state[4] & ~fsm_state[3] &  fsm_state[2] &  fsm_state[1] &  fsm_state[0];
 assign State8  = ~fsm_state[4] &  fsm_state[3] & ~fsm_state[2] & ~fsm_state[1] & ~fsm_state[0];
 assign State9  = ~fsm_state[4] &  fsm_state[3] & ~fsm_state[2] & ~fsm_state[1] &  fsm_state[0];
 assign State10 = ~fsm_state[4] &  fsm_state[3] & ~fsm_state[2] &  fsm_state[1] & ~fsm_state[0];
 assign State11 = ~fsm_state[4] &  fsm_state[3] & ~fsm_state[2] &  fsm_state[1] &  fsm_state[0];
 assign State12 = ~fsm_state[4] &  fsm_state[3] &  fsm_state[2] & ~fsm_state[1] & ~fsm_state[0];
 assign State13 = ~fsm_state[4] &  fsm_state[3] &  fsm_state[2] & ~fsm_state[1] &  fsm_state[0];
 assign State14 = ~fsm_state[4] &  fsm_state[3] &  fsm_state[2] &  fsm_state[1] & ~fsm_state[0];
 assign State15 = ~fsm_state[4] &  fsm_state[3] &  fsm_state[2] &  fsm_state[1] &  fsm_state[0];
 assign State16 =  fsm_state[4] & ~fsm_state[3] & ~fsm_state[2] & ~fsm_state[1] & ~fsm_state[0];
 assign State17 =  fsm_state[4] & ~fsm_state[3] & ~fsm_state[2] & ~fsm_state[1] &  fsm_state[0];
 assign State18 =  fsm_state[4] & ~fsm_state[3] & ~fsm_state[2] &  fsm_state[1] & ~fsm_state[0];
 assign State19 =  fsm_state[4] & ~fsm_state[3] & ~fsm_state[2] &  fsm_state[1] &  fsm_state[0];
 assign State20 =  fsm_state[4] & ~fsm_state[3] &  fsm_state[2] & ~fsm_state[1] & ~fsm_state[0];
 assign State21 =  fsm_state[4] & ~fsm_state[3] &  fsm_state[2] & ~fsm_state[1] &  fsm_state[0];
 
 /* fsm_state == 1 | 3 | 5 | 7 | 9 | 11 | 13 | 15 | 17 | 19 */
 assign State_odd = fsm_state[0] & ~(fsm_state[2] & fsm_state[4]);
 
 assign State1719   = State17 | State19;
 assign State1314   = State13 | State14;
 assign State121314 = State12 | State1314;
 assign State1315   = State13 | State15;
 assign State911    = State9  | State11;
 assign State511    = State5  | State11;
 
 assign State3_cond   = State3 & k_din_shreg[0];
 assign State3_cond_n = State3 & ~k_din_shreg[0];
 assign State7_cond   = State7 & k_din_shreg[0];
 assign State7_cond_n = State7 & ~k_din_shreg[0];
 
 assign State5_State7condn = State5 | State7_cond_n;
 
 assign nextState1 = ~fsm_state_next[4] & ~fsm_state_next[3] & ~fsm_state_next[2] & ~fsm_state_next[1] & fsm_state_next[0];
 assign nextState3 = ~fsm_state_next[4] & ~fsm_state_next[3] & ~fsm_state_next[2] &  fsm_state_next[1] & fsm_state_next[0];
 assign nextState7 = ~fsm_state_next[4] & ~fsm_state_next[3] &  fsm_state_next[2] &  fsm_state_next[1] & fsm_state_next[0];
 
 assign k_bit_index_e0 = ~(|k_bit_index);
 
 assign bit_counter_s255 = ~(&bit_counter);
 assign bit_counter_ne0 = |bit_counter;
 
 assign bit_counter_p1 = bit_counter + 1'b1;
 assign bit_counter_next = {(8){bit_counter_s255}} & bit_counter_p1;

 assign oK_addr = bit_counter[7:5];
 
 assign fsm_wait_done = ~worker_trig & worker_done;
								/* (bit_counter_ne0) ? 5'd3 : 5'd9; */
 //assign fsm_state_after_round = {~bit_counter_ne0, 1'b0, bit_counter_ne0, 1'b1};
 assign k_bit_index = bit_counter[4:0];
 
 assign worker_final_reduce = State16;
 assign worker_handle_sign  = State18;
 assign worker_output_now   = State20;
 
 // Worker Trigger Logic
 always@(posedge iClk) begin
	if(~iRstn)			worker_trig <= 1'b0;
	else if(State_odd)	worker_trig <= 1'b1;
	else				worker_trig <= 1'b0;
 end

 // Round Counter Increment Logic
 always@(posedge iClk) begin
	if(nextState1)		bit_counter <= 8'b0;
	else if(nextState7)	bit_counter <= bit_counter_next;
	else				bit_counter <= bit_counter;
 end

 // Final Round Detection Logic
 always@(posedge iClk) begin
	if(nextState3) begin
		if(k_bit_index_e0)
			/*case(oK_addr)
				3'd0:		k_din_shreg <= {iK[31:3], 3'b000};
				3'd7:		k_din_shreg <= {2'b01, iK[29:0]};
				default:	k_din_shreg <= iK;
			endcase*/
							k_din_shreg <= iK; // NOTE: CKDUR: The bit filter will be made on software
		else				k_din_shreg <= {k_din_shreg[0], k_din_shreg[31:1]};
	end
	else	k_din_shreg <= k_din_shreg;
 end

 // Worker Offset Logic
 /*
 always@(posedge iClk) begin
	case(fsm_state)
		5'd1:		worker_offset <= 9'd0;
		5'd3:		worker_offset <= k_din_shreg[0] ? 9'd18 : 9'd9;
		5'd5:		worker_offset <= 9'd27;
		5'd7:		worker_offset <= k_din_shreg[0] ? 9'd67 : 9'd62;
		5'd9:		worker_offset <= 9'd72;
		5'd11:		worker_offset <= 9'd75;
		5'd13:		worker_offset <= 9'd354;
		5'd15:		worker_offset <= 9'd358;
		5'd17:		worker_offset <= 9'd361;
		5'd19:		worker_offset <= 9'd363;
		default:	worker_offset <= worker_offset;
	endcase
 end
 */
 assign worker_offset_w[8] = State1315 | State1719;
 assign worker_offset_w[7] = 1'b0;
 assign worker_offset_w[6] = State7_cond | State911 | worker_offset_w[8];
 assign worker_offset_w[5] = State7_cond_n | worker_offset_w[8];
 assign worker_offset_w[4] = State3_cond | State5_State7condn;
 assign worker_offset_w[3] = State3_cond_n | State5_State7condn | State911 | State1719;
 assign worker_offset_w[2] = State7_cond_n | State15;
 assign worker_offset_w[1] = State3_cond | State511 | State7 | State1315 | State19;
 assign worker_offset_w[0] = State3_cond_n | State511 | State7_cond | State1719;
 always@(posedge iClk) begin
	if(State_odd)	worker_offset <= worker_offset_w;
	else			worker_offset <= worker_offset;
 end
 
 // FSM Process
 always@(posedge iClk) begin
	if(~iRstn)	fsm_state <= 5'd0;
	else		fsm_state <= fsm_state_next;
 end

 // FSM Transition Logic
 /*
 always@(*) begin
	case(fsm_state)
		5'd0:		fsm_state_next = iEn ? 5'd1 : 5'd0;
		5'd1:		fsm_state_next = 5'd2;
		5'd2:		fsm_state_next = fsm_wait_done ? 5'd3 : 5'd2;
		5'd3:		fsm_state_next = 5'd4;
		5'd4:		fsm_state_next = fsm_wait_done ? 5'd5 : 5'd4;
		5'd5:		fsm_state_next = 5'd6;
		5'd6:		fsm_state_next = fsm_wait_done ? 5'd7 : 5'd6;
		5'd7:		fsm_state_next = 5'd8;
		5'd8:		fsm_state_next = fsm_wait_done ? fsm_state_after_round : 5'd8;
		5'd9:		fsm_state_next = 5'd10;
		5'd10:		fsm_state_next = fsm_wait_done ? 5'd11 : 5'd10;
		5'd11:		fsm_state_next = 5'd12;
		5'd12:		fsm_state_next = fsm_wait_done ? 5'd13 : 5'd12;
		5'd13:		fsm_state_next = 5'd14;
		5'd14:		fsm_state_next = fsm_wait_done ? 5'd15 : 5'd14;
		5'd15:		fsm_state_next = 5'd16;
		5'd16:		fsm_state_next = fsm_wait_done ? 5'd17 : 5'd16;
		5'd17:		fsm_state_next = 5'd18;
		5'd18:		fsm_state_next = fsm_wait_done ? 5'd19 : 5'd18;
		5'd19:		fsm_state_next = 5'd20;
		5'd20:		fsm_state_next = fsm_wait_done ? 5'd21 : 5'd20;
		5'd21:		fsm_state_next = 5'd0;
		default:	fsm_state_next = 5'd0;
	endcase
 end
 */
 assign fsm_state_next[4] = State15 | State16 | State1719 | State18 | State20;
 assign fsm_state_next[3] = (State8 & fsm_wait_done) ? ~bit_counter_ne0 : (State7 | State8 | State911 | State10 | State121314);
 assign fsm_state_next[2] = State3 | State4 | State511 | State6 | State121314 | State19 | State20;
 assign fsm_state_next[1] = (State8 & fsm_wait_done) ? bit_counter_ne0 : (State1 | State2 | State5 | State6 | State9 | State10 | State1314 | State17 | State18);
 assign fsm_state_next[0] = (State0 & iEn) | (fsm_wait_done & ~fsm_state[0] & ~State0);
 
 // Worker
 ed25519_uop_worker uop_worker (
	.iClk			(iClk),
	.iRstn			(iRstn),
	.iEn			(worker_trig),
	.oReady			(worker_done),
	.iUOP_offset	(worker_offset),
	.iFinal_reduce	(worker_final_reduce),
	.iHandle_sign	(worker_handle_sign),
	.oValid			(worker_output_now),
	.oY_addr		(oQy_addr),
	.oY				(oQy),
	.oY_wren		(oQy_wren)
 );

 // Ready Flag Logic
 assign oReady = rdy_reg;
 always@(posedge iClk) begin
	if(~iRstn)				rdy_reg <= 1'b1;
	else if(State0 & iEn)	rdy_reg <= 1'b0;
	else if(State21)		rdy_reg <= 1'b1;
	else					rdy_reg <= rdy_reg;
 end

endmodule
