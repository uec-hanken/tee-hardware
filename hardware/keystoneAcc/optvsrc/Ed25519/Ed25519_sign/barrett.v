/* Reduce a 512 bit number mod 2**252
*                               + 27742317777372353535851937790883648493 */

module barrett_reduce (
    input				iClk,
    input				iRst,
	
    /* Control signals */
    input				iEn,
	input		[511:0]	iIn,
    output				oDone,
	output		[252:0]	oResult,
	
    /* To the multipliers */
	output	reg			oMulStart,
    output		[255:0]	oMul_D0,
    output		[255:0]	oMul_D1,
	input				iMulDone,
	input		[511:0]	iMul_Q
);

/*****************************************************************************
 *                           Parameter Declarations                          *
 *****************************************************************************/

/* For barrett reduction */
/* 2**252 + 27742317777372353535851937790883648493 */
localparam m = 253'h1000000000000000000000000000000014def9dea2f79cd65812631a5cf5d3ed;
/* 4**256 / m */
localparam mu = 260'hfffffffffffffffffffffffffffffffeb2106215d086329a7ed9ce5a30a2c131b;

/*****************************************************************************
 *                 Internal Wires and Registers Declarations                 *
 *****************************************************************************/

 reg	[3:0]	State;
 wire	[3:0]	nextState;
 
 wire			State0, State1, State2, State3,
				State4, State5, State6, State7,
				State8, State9, State10;
 
 wire			MulStart_w;

 reg			compare_flag_reg;
 reg			compare_flag_new;

 wire	[253:0]	sub_in_0;
 wire	[253:0]	sub_in_1;

 reg	[515:0]	layer2;
 reg	[253:0]	sub_out;
 
 reg	[511:0]	data_out_bram_1;
 reg	[511:0]	data_out_bram_2;
 reg	[511:0]	data_out_bram_3;
 reg	[252:0]	data_out_bram_c1;
 reg	[252:0]	data_out_bram_c2;
 
 wire			State0_En;
 wire			State1_MulDone;
 wire			State2_MulDone;
 wire			State3_MulDone;
 wire			State3_MulDoneN;
 wire			State5_or6;

/*****************************************************************************
 *                            Combinational Logic                            *
 *****************************************************************************/
 
 assign State0  = (~State[3] & ~State[2]) & (~State[1] & ~State[0]);
 assign State1  = (~State[3] & ~State[2]) & (~State[1] &  State[0]);
 assign State2  = (~State[3] & ~State[2]) & ( State[1] & ~State[0]);
 assign State3  = (~State[3] & ~State[2]) & ( State[1] &  State[0]);
 assign State4  = (~State[3] &  State[2]) & (~State[1] & ~State[0]);
 assign State5  = (~State[3] &  State[2]) & (~State[1] &  State[0]);
 assign State6  = (~State[3] &  State[2]) & ( State[1] & ~State[0]);
 assign State7  = (~State[3] &  State[2]) & ( State[1] &  State[0]);
 assign State8  = ( State[3] & ~State[2]) & (~State[1] & ~State[0]);
 assign State9  = ( State[3] & ~State[2]) & (~State[1] &  State[0]);
 assign State10 = ( State[3] & ~State[2]) & ( State[1] & ~State[0]);
 
assign oMul_D0 = (State1|State3) ? iIn[255:0] :
				 (State2|State4) ? iIn[511:256] : layer2[511:256];

assign oMul_D1 = (State1|State2) ? mu[255:0] :
				 (State3|State4) ? {252'b0, mu[259:256]} : {3'b0, m};

assign sub_in_0 = (State7) ? iIn[253:0] : sub_out;
assign sub_in_1 = (State7) ? iMul_Q[253:0] : {1'b0, m};
				  
 //Outputs controls
 assign oDone = State10;
 
 //Optimize
 assign State0_En = State0 & iEn;
 assign State1_MulDone = State1 & iMulDone;
 assign State2_MulDone = State2 & iMulDone;
 assign State3_MulDone = State3 & iMulDone;
 assign State3_MulDoneN = State3 & ~iMulDone;
 assign State5_or6 = State5 | State6;

/*****************************************************************************
 *                             Sequential Logic                              *
 *****************************************************************************/

 always@(posedge iClk) begin
	if(State1_MulDone)	data_out_bram_1 <= iMul_Q;
	else				data_out_bram_1 <= data_out_bram_1;
 end

 always@(posedge iClk) begin
	if(State2_MulDone)	data_out_bram_2 <= iMul_Q;
	else				data_out_bram_2 <= data_out_bram_2;
 end

 always@(posedge iClk) begin
	if(State3_MulDone)	data_out_bram_3 <= iMul_Q;
	else				data_out_bram_3 <= data_out_bram_3;
 end

 always@(posedge iClk) begin
	if(State8)	data_out_bram_c1 <= sub_out[252:0];
	else		data_out_bram_c1 <= data_out_bram_c1;
 end

 always@(posedge iClk) begin
	if(State9)	data_out_bram_c2 <= sub_out[252:0];
	else		data_out_bram_c2 <= data_out_bram_c2;
 end
 
 always@(posedge iClk) begin
	layer2	<= (data_out_bram_1[511:256] + data_out_bram_2) + (data_out_bram_3 + {iMul_Q[259:0], 256'b0});
	sub_out	<= sub_in_0 - sub_in_1;
 end

 //Outputs result
 always@(posedge iClk) begin
	if(State9)	compare_flag_reg <= (data_out_bram_c1 > m);
	else		compare_flag_reg <= compare_flag_reg;
 end
 assign oResult = (compare_flag_reg) ? data_out_bram_c2 : data_out_bram_c1;

/*****************************************************************************
 *                           Finite State Machine                            *
 *****************************************************************************/
/*
 localparam STATE_IDLE			= 4'd0,
		    STATE_LONG_MULT_1	= 4'd1,
		    STATE_LONG_MULT_2	= 4'd2,
		    STATE_LONG_MULT_3	= 4'd3,
		    STATE_LONG_MULT_4	= 4'd4,
		    STATE_ADD			= 4'd5,
		    STATE_SHORT_MULT	= 4'd6,
		    STATE_SUB			= 4'd7,
		    STATE_SAVE_SUB		= 4'd8,
		    STATE_SAVE_COMPARE	= 4'd9,
		    STATE_OUTPUT		= 4'd10,
		    STATE_DONE			= 4'd11;
*/
/*
 always@(posedge iClk) begin
	if(iRst) begin
		State <= 4'd0;
		oMulStart <= 1'b0;
	end
	else begin
		case(State)
			4'd0: begin
				if(iEn) begin
					State		<= 4'd1;
					oMulStart	<= 1'b1; end
				else begin
					State		<= State;
					oMulStart	<= 1'b0; end
			end
			4'd1: begin
				if(iMulDone) begin
					State		<= 4'd2;
					oMulStart	<= 1'b1; end
				else begin
					State		<= State;
					oMulStart	<= 1'b0; end
			end
			4'd2: begin
				if(iMulDone) begin
					State		<= 4'd3;
					oMulStart	<= 1'b1; end
				else begin
					State		<= State;
					oMulStart	<= 1'b0; end
			end
			4'd3: begin
				if(iMulDone) begin
					State		<= 4'd4;
					oMulStart	<= 1'b1; end
				else begin
					State		<= State;
					oMulStart	<= 1'b0; end
			end
			4'd4: begin
				oMulStart				<= 1'b0;
				if(iMulDone)	State	<= 4'd5;
				else			State	<= State;
			end
			4'd5: begin
				State		<= 4'd6;
				oMulStart	<= 1'b1;
			end
			4'd6: begin
				oMulStart				<= 1'b0;
				if(iMulDone)	State	<= 4'd7;
				else			State	<= State;
			end
			4'd7: begin
				State		<= 4'd8;
				oMulStart	<= 1'b0;
			end
			4'd8: begin
				State		<= 4'd9;
				oMulStart	<= 1'b0;
			end
			4'd9: begin
				State		<= 4'd10;
				oMulStart	<= 1'b0;
			end
			4'd10: begin
				State		<= 4'd0;
				oMulStart	<= 1'b0;
			end
			default: begin
				State		<= State;
				oMulStart	<= oMulStart;
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
		    STATE_LONG_MULT_1	= 4'd1,
		    STATE_LONG_MULT_2	= 4'd2,
		    STATE_LONG_MULT_3	= 4'd3,
		    STATE_LONG_MULT_4	= 4'd4,
		    STATE_ADD			= 4'd5,
		    STATE_SHORT_MULT	= 4'd6,
		    STATE_SUB			= 4'd7,
		    STATE_SAVE_SUB		= 4'd8,
		    STATE_SAVE_COMPARE	= 4'd9,
		    STATE_OUTPUT		= 4'd10,
		    STATE_DONE			= 4'd11;
*/
/*
 always@(posedge iClk) begin
	if(iRst)	State <= 4'd0;
	else begin
		case(State)
			4'd0: begin
				if(iEn)	State <= 4'd1;
				else		State <= State;
			end
			4'd1: begin
				if(iMulDone)	State <= 4'd2;
				else			State <= State;
			end
			4'd2: begin
				if(iMulDone)	State <= 4'd3;
				else			State <= State;
			end
			4'd3: begin
				if(iMulDone)	State <= 4'd4;
				else			State <= State;
			end
			4'd4: begin
				if(iMulDone)	State <= 4'd5;
				else			State <= State;
			end
			4'd5: begin
				State <= 4'd6;
			end
			4'd6: begin
				if(iMulDone)	State <= 4'd7;
				else			State <= State;
			end
			4'd7: begin
				State <= 4'd8;
			end
			4'd8: begin
				State <= 4'd9;
			end
			4'd9: begin
				State <= 4'd10;
			end
			4'd10: begin
				State <= 4'd0;
			end
			default: begin
				State <= State;
			end
		endcase
	end
 end
*/
 assign nextState[0] = ( (State0_En | State2_MulDone) | (State3_MulDoneN | State8) ) |
					   ( (State1 & ~iMulDone) | (State4 & iMulDone) | (State6 & iMulDone) );
 assign nextState[1] = ( State1_MulDone  | State2 ) |
					   ( State3_MulDoneN | State9 | State5_or6 );
 assign nextState[2] = State3_MulDone | State4 | State5_or6;
 assign nextState[3] = State7 | State8 | State9;
 always@(posedge iClk) begin
	if(iRst)	State <= 4'b0;
	else		State <= nextState;
 end

/*
 always@(posedge iClk) begin
	if(iRst)	oMulStart <= 1'b0;
	else begin
		case(State)
			4'd0: begin
				if(iEn)	oMulStart <= 1'b1;
				else		oMulStart <= 1'b0;
			end
			4'd1: begin
				if(iMulDone)	oMulStart <= 1'b1;
				else			oMulStart <= 1'b0;
			end
			4'd2: begin
				if(iMulDone)	oMulStart <= 1'b1;
				else			oMulStart <= 1'b0;
			end
			4'd3: begin
				if(iMulDone)	oMulStart <= 1'b1;
				else			oMulStart <= 1'b0;
			end
			4'd4: begin
				oMulStart <= 1'b0;
			end
			4'd5: begin
				oMulStart <= 1'b1;
			end
			4'd6: begin
				oMulStart <= 1'b0;
			end
			4'd7: begin
				oMulStart <= 1'b0;
			end
			4'd8: begin
				oMulStart <= 1'b0;
			end
			4'd9: begin
				oMulStart <= 1'b0;
			end
			4'd10: begin
				oMulStart <= 1'b0;
			end
			default: begin
				oMulStart <= oMulStart;
			end
		endcase
	end
end
*/
 assign MulStart_w = (State0_En | State1_MulDone) |
					 (State2_MulDone | State3_MulDone | State5);
 always@(posedge iClk) begin
	if(iRst)	oMulStart <= 1'b0;
	else		oMulStart <= MulStart_w;
 end

endmodule
