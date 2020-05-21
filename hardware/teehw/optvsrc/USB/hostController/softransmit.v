module SOFTransmit (
	input				SOFEnable,			// After host software asserts SOFEnable, must wait TBD time before asserting SOFSyncEn
	input				SOFSyncEn,
	input		[15:0]	SOFTimer,
	input				clk,
	input				rst,
	input				sendPacketArbiterGnt,
	input				sendPacketRdy,
	output	reg			SOFSent,			// single cycle pulse
	output	reg			SOFTimerClr,		// Single cycle pulse
	output	reg			sendPacketArbiterReq,
	output	reg			sendPacketWEn,
	input				fullSpeedRate
);

	reg				next_SOFSent;
	reg				next_SOFTimerClr;
	reg				next_sendPacketArbiterReq;
	reg				next_sendPacketWEn;
	reg		[15:0]	SOFNearTime;

	// diagram signals declarations
	reg		[7:0]	i, next_i;
	reg		[2:0]	CurrState_SOFTx;
	reg		[2:0]	NextState_SOFTx;

	//--------------------------------------------------------------------
	// Machine: SOFTx
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_SOFTx <= CurrState_SOFTx;
		// Set default values for outputs and signals
		next_sendPacketArbiterReq <= sendPacketArbiterReq;
		next_sendPacketWEn <= sendPacketWEn;
		next_SOFTimerClr <= SOFTimerClr;
		next_SOFSent <= SOFSent;
		next_i <= i;
		case (CurrState_SOFTx)
			3'd0: begin
				NextState_SOFTx <= 3'd1;
			end
			3'd1: begin
				if((SOFTimer>=SOFNearTime) | (SOFSyncEn&SOFEnable)) begin
					NextState_SOFTx <= 3'd2;
					next_sendPacketArbiterReq <= 1'b1; end
			end
			3'd2: begin
				if(sendPacketArbiterGnt&sendPacketRdy)	NextState_SOFTx <= 3'd3;
			end
			3'd3: begin
				if(SOFTimer>=16'hbb79) begin
					NextState_SOFTx <= 3'd4;
					next_sendPacketWEn <= 1'b1;
					next_SOFTimerClr <= 1'b1;
					next_SOFSent <= 1'b1; end
				else if(~SOFEnable) begin
					NextState_SOFTx <= 3'd4;
					next_SOFTimerClr <= 1'b1; end
			end
			3'd4: begin
				next_sendPacketWEn <= 1'b0;
				next_SOFTimerClr <= 1'b0;
				next_SOFSent <= 1'b0;
				if(sendPacketRdy) begin
					NextState_SOFTx <= 3'd5;
					next_i <= 8'h00; end
			end
			3'd5:			begin
				next_i <= i + 1'b1;
				if(i==8'hff) begin
					NextState_SOFTx <= 3'd6;
					next_sendPacketArbiterReq <= 1'b0;
					next_i <= 8'h00; end
			end
			3'd6: begin
				next_i <= i + 1'b1;
				if(i==8'hff)	NextState_SOFTx <= 3'd1;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_SOFTx <= 3'd0;
		else	CurrState_SOFTx <= NextState_SOFTx;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			i <= 8'h00;
			SOFSent <= 1'b0;
			SOFTimerClr <= 1'b0;
			sendPacketArbiterReq <= 1'b0;
			sendPacketWEn <= 1'b0;
			SOFNearTime <= 16'h0000; end
		else begin
			i <= next_i;
			SOFSent <= next_SOFSent;
			SOFTimerClr <= next_SOFTimerClr;
			sendPacketArbiterReq <= next_sendPacketArbiterReq;
			sendPacketWEn <= next_sendPacketWEn;
			if(fullSpeedRate)	SOFNearTime <= 16'hbb79 - 16'h0c80;
			else				SOFNearTime <= 16'hbb79 - 16'h6400; end
	end

endmodule
