module fifoMux (
	input		[3:0]	currEndP,
	//TxFifo
	input				TxFifoREn,
	output	reg			TxFifoEP0REn,
	output	reg			TxFifoEP1REn,
	output	reg			TxFifoEP2REn,
	output	reg			TxFifoEP3REn,
	output	reg	[7:0]	TxFifoData,
	input		[7:0]	TxFifoEP0Data,
	input		[7:0]	TxFifoEP1Data,
	input		[7:0]	TxFifoEP2Data,
	input		[7:0]	TxFifoEP3Data,
	output	reg			TxFifoEmpty,
	input				TxFifoEP0Empty,
	input				TxFifoEP1Empty,
	input				TxFifoEP2Empty,
	input				TxFifoEP3Empty,
	//RxFifo
	input				RxFifoWEn,
	output	reg			RxFifoEP0WEn,
	output	reg			RxFifoEP1WEn,
	output	reg			RxFifoEP2WEn,
	output	reg			RxFifoEP3WEn,
	output	reg			RxFifoFull,
	input				RxFifoEP0Full,
	input				RxFifoEP1Full,
	input				RxFifoEP2Full,
	input				RxFifoEP3Full
);

	//combinatorially mux TX and RX fifos for end points 0 through 3
	always@(*) begin
		case(currEndP[1:0])
			2'b00: begin
				TxFifoEP0REn	= TxFifoREn;
				TxFifoEP1REn	= 1'b0;
				TxFifoEP2REn	= 1'b0;
				TxFifoEP3REn	= 1'b0;
				TxFifoData		= TxFifoEP0Data;
				TxFifoEmpty		= TxFifoEP0Empty;
				RxFifoEP0WEn	= RxFifoWEn;
				RxFifoEP1WEn	= 1'b0;
				RxFifoEP2WEn	= 1'b0;
				RxFifoEP3WEn	= 1'b0;
				RxFifoFull		= RxFifoEP0Full; end
			2'b01: begin
				TxFifoEP0REn	= 1'b0;
				TxFifoEP1REn	= TxFifoREn;
				TxFifoEP2REn	= 1'b0;
				TxFifoEP3REn	= 1'b0;
				TxFifoData		= TxFifoEP1Data;
				TxFifoEmpty		= TxFifoEP1Empty;
				RxFifoEP0WEn	= 1'b0;
				RxFifoEP1WEn	= RxFifoWEn;
				RxFifoEP2WEn	= 1'b0;
				RxFifoEP3WEn	= 1'b0;
				RxFifoFull		= RxFifoEP1Full; end
			2'b10: begin
				TxFifoEP0REn	= 1'b0;
				TxFifoEP1REn	= 1'b0;
				TxFifoEP2REn	= TxFifoREn;
				TxFifoEP3REn	= 1'b0;
				TxFifoData		= TxFifoEP2Data;
				TxFifoEmpty		= TxFifoEP2Empty;
				RxFifoEP0WEn	= 1'b0;
				RxFifoEP1WEn	= 1'b0;
				RxFifoEP2WEn	= RxFifoWEn;
				RxFifoEP3WEn	= 1'b0;
				RxFifoFull		= RxFifoEP2Full; end
			2'b11: begin
				TxFifoEP0REn	= 1'b0;
				TxFifoEP1REn	= 1'b0;
				TxFifoEP2REn	= 1'b0;
				TxFifoEP3REn	= TxFifoREn;
				TxFifoData		= TxFifoEP3Data;
				TxFifoEmpty		= TxFifoEP3Empty;
				RxFifoEP0WEn	= 1'b0;
				RxFifoEP1WEn	= 1'b0;
				RxFifoEP2WEn	= 1'b0;
				RxFifoEP3WEn	= RxFifoWEn;
				RxFifoFull		= RxFifoEP3Full; end
		endcase  
	end      

endmodule
