module SCTxPortArbiter (
	input				SCTxPortRdyIn,
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
	output		[7:0]	SCTxPortCntl,
	output		[7:0]	SCTxPortData,
	output				SCTxPortRdyOut,
	output				SCTxPortWEnable,
	output	reg			directCntlGnt,
	output	reg			sendPacketGnt
);

	reg				next_directCntlGnt;
	reg				next_sendPacketGnt;
	
	reg				muxDCEn, next_muxDCEn;
	
	reg		[1:0]	CurrState_SCTxArb;
	reg		[1:0]	NextState_SCTxArb;

	// SOFController/directContol/sendPacket mux
	assign SCTxPortRdyOut  = SCTxPortRdyIn;
	assign SCTxPortWEnable = (muxDCEn) ? directCntlWEn  : sendPacketWEn;
	assign SCTxPortData    = (muxDCEn) ? directCntlData : sendPacketData;
	assign SCTxPortCntl    = (muxDCEn) ? directCntlCntl : sendPacketCntl;

	//--------------------------------------------------------------------
	// Machine: SCTxArb
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_SCTxArb <= CurrState_SCTxArb;
		// Set default values for outputs and signals
		next_sendPacketGnt <= sendPacketGnt;
		next_muxDCEn <= muxDCEn;
		next_directCntlGnt <= directCntlGnt;
		case (CurrState_SCTxArb)
			2'b00: begin
				if(sendPacketReq) begin
					NextState_SCTxArb <= 2'b01;
					next_sendPacketGnt <= 1'b1;
					next_muxDCEn <= 1'b0; end
				else if(directCntlReq) begin
					NextState_SCTxArb <= 2'b10;
					next_directCntlGnt <= 1'b1;
					next_muxDCEn <= 1'b1; end
			end
			2'b01: begin
				if(~sendPacketReq) begin
					NextState_SCTxArb <= 2'b00;
					next_sendPacketGnt <= 1'b0; end
			end
			2'b10: begin
				if(~directCntlReq) begin
					NextState_SCTxArb <= 2'b00;
					next_directCntlGnt <= 1'b0; end
			end
			2'b11: begin
				NextState_SCTxArb <= 2'b00;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_SCTxArb <= 2'b11;
		else	CurrState_SCTxArb <= NextState_SCTxArb;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			muxDCEn <= 1'b0;
			sendPacketGnt <= 1'b0;
			directCntlGnt <= 1'b0; end
		else begin
			muxDCEn <= next_muxDCEn;
			sendPacketGnt <= next_sendPacketGnt;
			directCntlGnt <= next_directCntlGnt; end
	end

endmodule
