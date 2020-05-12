/*
Edited from the original ed25519_sign_core
the reason is that we trim the SHA512 and the scalar multipliers
This core only computes S from the R,S pair in the sign
for calculating R, take r and use the base-point multiplier
*/

module ed25519_sign_S_core (
	input			iClk,
	input			iRst,
	
	/* Control signals */
	input			iEn,
	output			oReady,
	output			oDone,
	
	/* Secret key hashed S = H(k) */
	input	[250:0]	iHashd_key,
	/* H(R, A, M) */
	input	[511:0]	iHashd_ram,
	/* H(S, M). */
	input	[511:0]	iHashd_sm,
	
	/* A signature is a pair, but we only output 1 part (R,S) */
	output	[252:0]	oSign
);

/*****************************************************************************
 *                            Combinational Logic                            *
 *****************************************************************************/

 reg	[2:0]	State;
 wire	[2:0]	nextState;
 
 wire			State0, State1, State2, State3,
				State4, State5, State6, State7;
 
 reg			FSM_mult_Start;
 wire			FSM_mult_Start_w;
 
 //Multipliers
 wire	[255:0]	mult_iA;
 wire	[255:0]	mult_iB;
 wire			mult_oDone;
 wire	[511:0]	mult_oX;
 
 //barrett_reduce
 wire			red_iEn;
 wire	[511:0]	red_iIn;
 wire			red_oDone;
 wire	[252:0]	red_oResult;
 wire			red_mult_Start;
 wire	[255:0]	red_mult_A;
 wire	[255:0]	red_mult_B;
 
 //registers
 reg	[511:0]	HRAM_A;
 reg	[255:0]	R;
 reg	[255:0]	HRAM;
 
 //Inputs & Outputs
 wire	[255:0]	Pre_HashKey;
 wire	[255:0]	HashKey;
 wire	[252:0]	Pre_Sign;
 
 //Optimize
 wire			State246;
 wire			State1246;
 wire			State56;
 wire			State0_En;
 wire			State1_redDone;
 wire			State1_redDoneN;
 wire			State2_redDone;
 wire			State3_mulDone;
 wire			State3_mulDoneN;
 wire			State4_redDone;
 wire			State6_redDone;
 
/*****************************************************************************
 *                            Combinational Logic                            *
 *****************************************************************************/

 assign State0  = ~State[2] & ~State[1] & ~State[0];
 assign State1  = ~State[2] & ~State[1] &  State[0];
 assign State2  = ~State[2] &  State[1] & ~State[0];
 assign State3  = ~State[2] &  State[1] &  State[0];
 assign State4  =  State[2] & ~State[1] & ~State[0];
 assign State5  =  State[2] & ~State[1] &  State[0];
 assign State6  =  State[2] &  State[1] & ~State[0];
 assign State7  =  State[2] &  State[1] &  State[0];
 
 //Optimize
 assign State246 = State2 | State4 | State6;
 assign State1246 = State1 | State246;
 assign State56 = State5 | State6;
 assign State0_En = State0 & iEn;
 assign State1_redDone = State1 & red_oDone;
 assign State1_redDoneN = State1 & ~red_oDone;
 assign State2_redDone = State2 & red_oDone;
 assign State3_mulDone = State3 & mult_oDone;
 assign State3_mulDoneN = State3 & ~mult_oDone;
 assign State4_redDone = State4 & red_oDone;
 assign State6_redDone = State6 & red_oDone;
 
 //Multipliers
 assign mult_iA = (State1246) ? red_mult_A : HRAM;
 assign mult_iB = (State1246) ? red_mult_B : HashKey;
 
 //assign Pre_HashKey = {iHashd_key[511:507], 3'b0, iHashd_key[503:264],2'b01,iHashd_key[261:256]};
 assign Pre_HashKey = {iHashd_key[250:246], 3'b0, iHashd_key[245:6],2'b01,iHashd_key[5:0]};
 assign HashKey = {Pre_HashKey[7:0]    , Pre_HashKey[15:8]   , Pre_HashKey[23:16]  , Pre_HashKey[31:24]  ,
				   Pre_HashKey[39:32]  , Pre_HashKey[47:40]  , Pre_HashKey[55:48]  , Pre_HashKey[63:56]  ,
				   Pre_HashKey[71:64]  , Pre_HashKey[79:72]  , Pre_HashKey[87:80]  , Pre_HashKey[95:88]  ,
				   Pre_HashKey[103:96] , Pre_HashKey[111:104], Pre_HashKey[119:112], Pre_HashKey[127:120],
				   Pre_HashKey[135:128], Pre_HashKey[143:136], Pre_HashKey[151:144], Pre_HashKey[159:152],
				   Pre_HashKey[167:160], Pre_HashKey[175:168], Pre_HashKey[183:176], Pre_HashKey[191:184],
				   Pre_HashKey[199:192], Pre_HashKey[207:200], Pre_HashKey[215:208], Pre_HashKey[223:216],
				   Pre_HashKey[231:224], Pre_HashKey[239:232], Pre_HashKey[247:240], Pre_HashKey[255:248]};
 
 //barrett_reduce controls
 assign red_iEn = State1246;
 assign red_iIn = (State1) ? iHashd_sm :
				  (State2) ? iHashd_ram :
				  (State4) ? HRAM_A : HRAM;
 
 //Outputs
 assign oReady = State0;
 assign oDone = State7;
 
 assign Pre_Sign = HRAM[252:0];
 assign oSign = {Pre_Sign[7:0]    , Pre_Sign[15:8]   , Pre_Sign[23:16]  , Pre_Sign[31:24]  ,
				 Pre_Sign[39:32]  , Pre_Sign[47:40]  , Pre_Sign[55:48]  , Pre_Sign[63:56]  ,
				 Pre_Sign[71:64]  , Pre_Sign[79:72]  , Pre_Sign[87:80]  , Pre_Sign[95:88]  ,
				 Pre_Sign[103:96] , Pre_Sign[111:104], Pre_Sign[119:112], Pre_Sign[127:120],
				 Pre_Sign[135:128], Pre_Sign[143:136], Pre_Sign[151:144], Pre_Sign[159:152],
				 Pre_Sign[167:160], Pre_Sign[175:168], Pre_Sign[183:176], Pre_Sign[191:184],
				 Pre_Sign[199:192], Pre_Sign[207:200], Pre_Sign[215:208], Pre_Sign[223:216],
				 Pre_Sign[231:224], Pre_Sign[239:232], Pre_Sign[247:240], Pre_Sign[252:248]};

/*****************************************************************************
 *                             Sequential Logic                              *
 *****************************************************************************/
 
 always@(posedge iClk) begin
	if(State3_mulDone)	HRAM_A <= mult_oX;
	else				HRAM_A <= HRAM_A;
 end

 always@(posedge iClk) begin
	if(State1_redDone)	R <= red_oResult;
	else				R <= R;
 end

 always@(posedge iClk) begin 
	if(State5)	HRAM <= R + HRAM;
	else if((red_oDone & State246))
				HRAM <= red_oResult;
	else		HRAM <= HRAM;
 end

/*****************************************************************************
 *                              Internal Modules                             *
 *****************************************************************************/
 
 mult_512_byAdder mult (
	.iClk		(iClk),
	.iRst		(iRst),
	.iStart		(FSM_mult_Start | red_mult_Start),
	.iA			(mult_iA),
	.iB			(mult_iB),
	.oDone		(mult_oDone),
	.oX			(mult_oX)
 );

 barrett_reduce red (
	.iClk		(iClk),
	.iRst		(iRst),
	.iIn		(red_iIn),
	.iEn		(red_iEn),
	.oDone		(red_oDone),
	.oResult	(red_oResult),
	.oMulStart	(red_mult_Start),
	.oMul_D0	(red_mult_A),
	.oMul_D1	(red_mult_B),
	.iMulDone	(mult_oDone),
	.iMul_Q		(mult_oX)
 );

/*****************************************************************************
 *                           Finite State Machine                            *
 *****************************************************************************/
/*
 localparam STATE_IDLE			= 4'd0,
			STATE_REDUCE_R		= 4'd1,
			STATE_REDUCE_HRAM	= 4'd2,
			STATE_MULT_HRAM_A	= 4'd3,
			STATE_REDUCE_HRAM_A	= 4'd4,
			STATE_ADD_R_HRAMA	= 4'd5,
			STATE_REDUCE_FINAL	= 4'd6,
			STATE_OUTPUT		= 4'd7;
*/
/*
 always@(posedge iClk) begin
	if(iRst) begin
		State <= 4'd0;
		FSM_mult_Start <= 1'b0;
	end
	else begin
		case(State)
			4'd0: begin
				FSM_mult_Start <= 1'b0;
				if(iEn)	State <= 4'd1;
				else	State <= State;
			end
			4'd1: begin
				FSM_mult_Start <= 1'b0;
				if(red_oDone)	State <= 4'd2;
				else			State <= State;
			end
			4'd2: begin
				if(red_oDone) begin
					State <= 4'd3;
					FSM_mult_Start <= 1'b1; end
				else begin
					State <= State;
					FSM_mult_Start <= 1'b0; end
			end
			4'd3: begin
				if(mult_oDone) begin
					State <= 4'd4;
					FSM_mult_Start <= 1'b0; end
				else begin
					State <= State;
					FSM_mult_Start <= 1'b0; end
			end
			4'd4: begin
				FSM_mult_Start <= 1'b0;
				if(red_oDone)	State <= 4'd5;
				else			State <= State;
			end
			4'd5: begin
				State <= 4'd6;
				FSM_mult_Start <= 1'b0;
			end
			4'd6: begin
				FSM_mult_Start <= 1'b0;
				if(red_oDone)	State <= 4'd7;
				else			State <= State;
			end
			4'd7: begin
				State <= 4'd0;
				FSM_mult_Start <= 1'b0;
			end
			default: begin
				State <= 4'd0;
				FSM_mult_Start <= 1'b0;
			end
		endcase
	end
 end
*/

/*****************************************************************************
 *                           Finite State Machine                            *
 *****************************************************************************/
/*
 localparam STATE_IDLE			= 4'd0,
			STATE_REDUCE_R		= 4'd1,
			STATE_REDUCE_HRAM	= 4'd2,
			STATE_MULT_HRAM_A	= 4'd3,
			STATE_REDUCE_HRAM_A	= 4'd4,
			STATE_ADD_R_HRAMA	= 4'd5,
			STATE_REDUCE_FINAL	= 4'd6,
			STATE_OUTPUT		= 4'd7;
*/
/*
 always@(posedge iClk) begin
	if(iRst)	State <= 4'd0;
	else begin
		case(State)
			4'd0: begin
				if(iEn)	State <= 4'd1;
				else	State <= State;
			end
			4'd1: begin
				if(red_oDone)	State <= 4'd2;
				else			State <= State;
			end
			4'd2: begin
				if(red_oDone)	State <= 4'd3;
				else 			State <= State;
			end
			4'd3: begin
				if(mult_oDone) 	State <= 4'd4;
				else 			State <= State;
			end
			4'd4: begin
				if(red_oDone)	State <= 4'd5;
				else			State <= State;
			end
			4'd5: begin
				State <= 4'd6;
			end
			4'd6: begin
				if(red_oDone)	State <= 4'd7;
				else			State <= State;
			end
			4'd7: begin
				State <= 4'd0;
			end
			default: begin
				State <= State;
			end
		endcase
	end
 end
*/
 assign nextState[0] = (State0_En | State1_redDoneN) |
					   (State2_redDone | State3_mulDoneN) |
					   (State4_redDone | State6_redDone);
 assign nextState[1] = (State1_redDone | State3_mulDoneN) | (State2 | State56);
 assign nextState[2] = State3_mulDone | State4 | State56;
 always@(posedge iClk) begin
	if(iRst)	State <= 3'b0;
	else		State <= nextState;
 end

/*
 always@(posedge iClk) begin
	if(iRst)	FSM_mult_Start <= 1'b0;
	else begin
		case(State)
			4'd0: begin
				FSM_mult_Start <= 1'b0;
			end
			4'd1: begin
				FSM_mult_Start <= 1'b0;
			end
			4'd2: begin
				if(red_oDone)	FSM_mult_Start <= 1'b1;
				else 			FSM_mult_Start <= 1'b0;
			end
			4'd3: begin
				if(mult_oDone)	FSM_mult_Start <= 1'b0;
				else			FSM_mult_Start <= 1'b0;
			end
			4'd4: begin
				FSM_mult_Start <= 1'b0;
			end
			4'd5: begin
				FSM_mult_Start <= 1'b0;
			end
			4'd6: begin
				FSM_mult_Start <= 1'b0;
			end
			4'd7: begin
				FSM_mult_Start <= 1'b0;
			end
			default: begin
				FSM_mult_Start <= FSM_mult_Start;
			end
		endcase
	end
 end
*/
 assign FSM_mult_Start_w = State2_redDone;
 always@(posedge iClk) begin
	if(iRst)	FSM_mult_Start <= 1'b0;
	else		FSM_mult_Start <= FSM_mult_Start_w;
 end

endmodule
