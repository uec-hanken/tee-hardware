module mult_512_byAdder (
	input				iClk,
	input				iRst,
	input				iStart,
	input		[255:0]	iA,
	input		[255:0]	iB,
	output	reg			oDone,
	output	reg	[511:0]	oX
);

/*****************************************************************************
 *                 Internal Wires and Registers Declarations                 *
 *****************************************************************************/
 
 reg	[255:0]	A_in;
 wire	[255:0]	A_in_w;
 reg	[511:0]	B_in;
 wire	[511:0]	B_in_w;
 
 reg	[8:0]	counter;
 wire	[8:0]	counter_w;
 
 reg			State;
 wire			nextState;
 
 wire	[511:0]	X_w;
 
 wire			StateN_iStart;
 wire			StateN_iStart_n;
 wire			State_counter8N;
 wire			State_counter8;

/*****************************************************************************
 *                            Combinational Logic                            *
 *****************************************************************************/
 
 assign StateN_iStart = ~State & iStart;
 assign StateN_iStart_n = ~StateN_iStart;
 assign State_counter8N = State & ~counter[8];
 assign State_counter8 = State & counter[8];
 
/*****************************************************************************
 *                             Sequential Logic                              *
 *****************************************************************************/
 
 /*
 always@(posedge iClk) begin
	if(iStart)		A_in <= iA;
	else if(State)	A_in <= {A_in[0], A_in[255:1]};
	else			A_in <= A_in;
 end
 */
 assign A_in_w = (iStart) ? iA :
				 (State) ? {A_in[0], A_in[255:1]} : A_in;
 always@(posedge iClk) begin
	A_in <= A_in_w;
 end
 
 /*
 always@(posedge iClk) begin
	if(iStart)		B_in <= {256'b0,iB};
	else if(State)	B_in <= {B_in[510:0], 1'b0};
	else			B_in <= B_in;
 end
 */
 assign B_in_w[511:256] = {(256){~iStart}} & ( (State) ? B_in[510:255] : B_in[511:256] );
 assign B_in_w[255:1]   = (iStart) ? iB[255:1] : ( (State) ? B_in[254:0] : B_in[255:1] );
 assign B_in_w[0]       = (iStart) ? iB[0]     : ( ~State & B_in[0] );
 always@(posedge iClk) begin
	B_in <= B_in_w;
 end
 
/*****************************************************************************
 *                           Finite State Machine                            *
 *****************************************************************************/
 /*
 always@(posedge iClk) begin
	if(iRst) begin
		State <= 1'b0;
		counter <= 9'b0;
		oDone <= 1'b0;
		oX <= 512'b0; end
	else begin
		case(State)
			1'b0: begin
				oDone <= 1'b0;
				if(iStart) begin
					State <= 1'b1;
					counter <= 9'b0;
					oX <= 512'b0; end
				else begin
					State <= 1'b0;
					counter <= counter;
					oX <= oX; end
			end
			1'b1: begin
				if(counter[8]) begin
					State <= 1'b0;
					counter <= 9'b0;
					oDone <= 1'b1;
					oX <= oX; end
				else begin
					State <= 1'b1;
					counter <= counter + 1'b1;
					oDone <= 1'b0;
					if(A_in[0])	oX <= oX + B_in;
					else		oX <= oX; end
			end
		endcase
	end
 end
 */
 
/*****************************************************************************
 *                           Finite State Machine                            *
 *****************************************************************************/
 /*
 always@(posedge iClk) begin
	if(iRst)	State <= 1'b0;
	else begin
		case(State)
			1'b0: begin
				if(iStart)		State <= 1'b1;
				else			State <= 1'b0;
			end
			1'b1: begin
				if(counter[8])	State <= 1'b0;
				else 			State <= 1'b1;
			end
		endcase
	end
 end
 */
 assign nextState = StateN_iStart | State_counter8N;
 always@(posedge iClk) begin
	if(iRst)	State <= 1'b0;
	else		State <= nextState;
 end
 
 /*
 always@(posedge iClk) begin
	if(iRst)	counter <= 9'b0;
	else begin
		case(State)
			1'b0: begin
				if(iStart)		counter <= 9'b0;
				else			counter <= counter;
			end
			1'b1: begin
				if(counter[8])	counter <= 9'b0;
				else			counter <= counter + 1'b1;
			end
		endcase
	end
 end
 */
 assign counter_w = {(9){StateN_iStart_n}} & {(9){~State_counter8}} &
					( (State_counter8N) ? (counter + 1'b1) : counter );
 always@(posedge iClk) begin
	if(iRst)	counter <= 9'b0;
	else		counter <= counter_w;
 end
 
 /*
 always@(posedge iClk) begin
	if(iRst)	oDone <= 1'b0;
	else if(State & counter[8])
				oDone <= 1'b1;
	else		oDone <= 1'b0;
 end
 */
 always@(posedge iClk) begin
	if(iRst)	oDone <= 1'b0;
	else		oDone <= State_counter8;
 end
 
 /*
 always@(posedge iClk) begin
	if(iRst)	oX <= 512'b0;
	else begin
		case(State)
			1'b0: begin
				if(iStart)	oX <= 512'b0;
				else		oX <= oX;
			end
			1'b1: begin
				if(counter[8])		oX <= oX;
				else if(A_in[0])	oX <= oX + B_in;
				else				oX <= oX;
			end
		endcase
	end
 end
 */
 assign X_w = {(512){StateN_iStart_n}} &
			  ( (State_counter8N & A_in[0]) ? (oX + B_in) : oX );
 always@(posedge iClk) begin
	if(iRst)	oX <= 512'b0;
	else		oX <= X_w;
 end
 
endmodule
