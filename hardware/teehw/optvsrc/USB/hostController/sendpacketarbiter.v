module sendPacketArbiter (
	input				HCTxReq,
	input		[3:0]	HC_PID,
	input				HC_SP_WEn,
	input				SOFTxReq,
	input				SOF_SP_WEn,
	input				clk,
	input				rst,
	output	reg			HCTxGnt,
	output	reg			SOFTxGnt,
	output		[3:0]	sendPacketPID,
	output				sendPacketWEnable
);

	reg				next_HCTxGnt;
	reg				next_SOFTxGnt;
	reg				muxSOFNotHC, next_muxSOFNotHC;

	reg		[1:0]	CurrState_sendPktArb;
	reg		[1:0]	NextState_sendPktArb;

	// hostController/SOFTransmit mux
	assign sendPacketWEnable = (muxSOFNotHC) ? SOF_SP_WEn : HC_SP_WEn;
	assign sendPacketPID     = (muxSOFNotHC) ? 4'h5       : HC_PID;

	//--------------------------------------------------------------------
	// Machine: sendPktArb
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(HCTxReq or SOFTxReq or HCTxGnt or SOFTxGnt or muxSOFNotHC or CurrState_sendPktArb) begin
		NextState_sendPktArb <= CurrState_sendPktArb;
		// Set default values for outputs and signals
		next_HCTxGnt <= HCTxGnt;
		next_SOFTxGnt <= SOFTxGnt;
		next_muxSOFNotHC <= muxSOFNotHC;
		case(CurrState_sendPktArb)
			2'd0: begin
				if(~HCTxReq) begin
					NextState_sendPktArb <= 2'd2;
					next_HCTxGnt <= 1'b0; end
			end
			2'd1: begin
				if(~SOFTxReq) begin
					NextState_sendPktArb <= 2'd2;
					next_SOFTxGnt <= 1'b0; end
			end
			2'd2: begin
				if(SOFTxReq) begin
					NextState_sendPktArb <= 2'd1;
					next_SOFTxGnt <= 1'b1;
					next_muxSOFNotHC <= 1'b1; end
				else if(HCTxReq) begin
					NextState_sendPktArb <= 2'd0;
					next_HCTxGnt <= 1'b1;
					next_muxSOFNotHC <= 1'b0; end
			end
			2'd3: begin
				NextState_sendPktArb <= 2'd2;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_sendPktArb <= 2'd3;
		else	CurrState_sendPktArb <= NextState_sendPktArb;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	begin
			muxSOFNotHC <= 1'b0;
			SOFTxGnt <= 1'b0;
			HCTxGnt <= 1'b0; end
		else begin
			muxSOFNotHC <= next_muxSOFNotHC;
			SOFTxGnt <= next_SOFTxGnt;
			HCTxGnt <= next_HCTxGnt; end
	end

endmodule
