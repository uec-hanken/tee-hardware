module HCTxPortArbiter (
	input		[7:0]	SOFCntlCntl,
	input		[7:0]	SOFCntlData,
	input				SOFCntlReq,
	input				SOFCntlWEn,
	input				clk,
	input		[7:0]	directCntlCntl,
	input		[7:0]	directCntlData,
	input				directCntlReq,
	input				directCntlWEn,
	input				rst,
	input		[7:0]	sendPacketCntl,
	input		[7:0]	sendPacketData,
	input				sendPacketReq,
	input				sendPacketWEn,
	output		[7:0]	HCTxPortCntl,
	output		[7:0]	HCTxPortData,
	output				HCTxPortWEnable,
	output	reg			SOFCntlGnt,
	output	reg			directCntlGnt,
	output	reg			sendPacketGnt
);

	reg		next_SOFCntlGnt;
	reg		next_directCntlGnt;
	reg		next_sendPacketGnt;

	// diagram signals declarations
	reg		[1:0]	muxCntl, next_muxCntl;

	reg		[2:0]	CurrState_HCTxArb;
	reg		[2:0]	NextState_HCTxArb;

	// SOFController/directContol/sendPacket mux
	assign HCTxPortWEnable = (muxCntl[1]) ?
								( ~muxCntl[0] & directCntlWEn) :
								( (muxCntl[0]) ? SOFCntlWEn : sendPacketWEn );
	assign HCTxPortData    = (muxCntl[1]) ?
								( {(9){~muxCntl[0]}} & directCntlData) :
								( (muxCntl[0]) ? SOFCntlData : sendPacketData);
	assign HCTxPortCntl    = (muxCntl[1]) ?
								( {(8){~muxCntl[0]}} & directCntlCntl) :
								( (muxCntl[0]) ? SOFCntlCntl : sendPacketCntl);

	//--------------------------------------------------------------------
	// Machine: HCTxArb
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_HCTxArb <= CurrState_HCTxArb;
		// Set default values for outputs and signals
		next_SOFCntlGnt <= SOFCntlGnt;
		next_muxCntl <= muxCntl;
		next_sendPacketGnt <= sendPacketGnt;
		next_directCntlGnt <= directCntlGnt;
		case(CurrState_HCTxArb)
			3'd0: begin
				NextState_HCTxArb <= 3'd1;
			end
			3'd1: begin
				if(SOFCntlReq) begin
					NextState_HCTxArb <= 3'd2;
					next_SOFCntlGnt <= 1'b1;
					next_muxCntl <= 2'b01; end
				else if(sendPacketReq) begin
					NextState_HCTxArb <= 3'd3;
					next_sendPacketGnt <= 1'b1;
					next_muxCntl <= 2'b00; end
				else if(directCntlReq) begin
					NextState_HCTxArb <= 3'd4;
					next_directCntlGnt <= 1'b1;
					next_muxCntl <= 2'b10; end
			end
			3'd2: begin
				if(~SOFCntlReq) begin
					NextState_HCTxArb <= 3'd1;
					next_SOFCntlGnt <= 1'b0; end
			end
			3'd3: begin
				if(~sendPacketReq) begin
					NextState_HCTxArb <= 3'd1;
					next_sendPacketGnt <= 1'b0; end
			end
			3'd4: begin
				if(~directCntlReq) begin
					NextState_HCTxArb <= 3'd1;
					next_directCntlGnt <= 1'b0; end
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_HCTxArb <= 3'd0;
		else	CurrState_HCTxArb <= NextState_HCTxArb;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			muxCntl <= 2'b00;
			SOFCntlGnt <= 1'b0;
			sendPacketGnt <= 1'b0;
			directCntlGnt <= 1'b0; end
		else begin
			muxCntl <= next_muxCntl;
			SOFCntlGnt <= next_SOFCntlGnt;
			sendPacketGnt <= next_sendPacketGnt;
			directCntlGnt <= next_directCntlGnt; end
	end

endmodule
