module sendPacket (
	input				HCTxPortGnt,
	input				HCTxPortRdy,
	input		[3:0]	PID,
	input		[6:0]	TxAddr,
	input		[3:0]	TxEndP,
	input				clk,
	input		[7:0]	fifoData,
	input				fifoEmpty,
	input				fullSpeedPolarity,
	input				rst,
	input				sendPacketWEn,
	output	reg	[7:0]	HCTxPortCntl,
	output	reg	[7:0]	HCTxPortData,
	output	reg			HCTxPortReq,
	output	reg			HCTxPortWEn,
	output	reg			fifoReadEn,
	output	reg	[10:0]	frameNum,
	output	reg			sendPacketRdy
);

	reg		[7:0]	next_HCTxPortCntl;
	reg		[7:0]	next_HCTxPortData;
	reg				next_HCTxPortReq;
	reg				next_HCTxPortWEn;
	reg				next_fifoReadEn;
	reg		[10:0]	next_frameNum;
	reg				next_sendPacketRdy;

	wire	[7:0]	PIDNotPID;
	reg		[4:0]	CurrState_sndPkt;
	reg		[4:0]	NextState_sndPkt;

	assign PIDNotPID = {(PID^4'hf), PID};

	//--------------------------------------------------------------------
	// Machine: sndPkt
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_sndPkt <= CurrState_sndPkt;
		// Set default values for outputs and signals
		next_sendPacketRdy <= sendPacketRdy;
		next_HCTxPortReq <= HCTxPortReq;
		next_HCTxPortWEn <= HCTxPortWEn;
		next_HCTxPortData <= HCTxPortData;
		next_HCTxPortCntl <= HCTxPortCntl;
		next_frameNum <= frameNum;
		next_fifoReadEn <= fifoReadEn;
		case(CurrState_sndPkt)
			5'd0: begin
				NextState_sndPkt <= 5'd1;
			end
			5'd1: begin
				if(sendPacketWEn) begin
					NextState_sndPkt <= 5'd2;
					next_sendPacketRdy <= 1'b0;
					next_HCTxPortReq <= 1'b1; end
			end
			5'd2: begin
				if(HCTxPortGnt & ((PID==4'h5)&~fullSpeedPolarity))	NextState_sndPkt <= 5'd21;
				else if(HCTxPortGnt)								NextState_sndPkt <= 5'd3;
			end
			5'd5: begin
				NextState_sndPkt <= 5'd1;
				next_sendPacketRdy <= 1'b1;
				next_HCTxPortReq <= 1'b0;
			end
			5'd3: begin
				if(HCTxPortRdy) begin
					NextState_sndPkt <= 5'd4;
					next_HCTxPortWEn <= 1'b1;
					next_HCTxPortData <= PIDNotPID;
					next_HCTxPortCntl <= 8'h02; end
			end
			5'd4: begin
				next_HCTxPortWEn <= 1'b0;
				if((PID==4'h3)|(PID==4'hb))						NextState_sndPkt <= 5'd14;
				else if(PID==4'h5)								NextState_sndPkt <= 5'd10;
				else if((PID==4'h1)|(PID==4'h9)|(PID==4'hd))	NextState_sndPkt <= 5'd6;
				else											NextState_sndPkt <= 5'd5;
			end
			5'd6: begin
				if(HCTxPortRdy) begin
					NextState_sndPkt <= 5'd17;
					next_HCTxPortWEn <= 1'b1;
					next_HCTxPortData <= {TxEndP[0], TxAddr[6:0]};
					next_HCTxPortCntl <= 8'h03; end
			end
			5'd7: begin
				if(HCTxPortRdy) begin
					NextState_sndPkt <= 5'd8;
					next_HCTxPortWEn <= 1'b1;
					next_HCTxPortData <= {5'b00000, TxEndP[3:1]};
					next_HCTxPortCntl <= 8'h03; end
			end
			5'd8: begin
				next_HCTxPortWEn <= 1'b0;
				NextState_sndPkt <= 5'd5;
			end
			5'd17: begin
				next_HCTxPortWEn <= 1'b0;
				NextState_sndPkt <= 5'd7;
			end
			5'd9: begin
				next_HCTxPortWEn <= 1'b0;
				next_frameNum <= frameNum + 1'b1;
				NextState_sndPkt <= 5'd5;
			end
			5'd10: begin
				if(HCTxPortRdy) begin
					NextState_sndPkt <= 5'd18;
					next_HCTxPortWEn <= 1'b1;
					next_HCTxPortData <= frameNum[7:0];
					next_HCTxPortCntl <= 8'h03; end
			end
			5'd11: begin
				if(HCTxPortRdy) begin
					NextState_sndPkt <= 5'd9;
					next_HCTxPortWEn <= 1'b1;
					next_HCTxPortData <= {5'b00000, frameNum[10:8]};
					next_HCTxPortCntl <= 8'h03; end
			end
			5'd18: begin
				next_HCTxPortWEn <= 1'b0;
				NextState_sndPkt <= 5'd11;
			end
			5'd12: begin
				next_HCTxPortWEn <= 1'b1;
				next_HCTxPortData <= fifoData;
				next_HCTxPortCntl <= 8'h03;
				NextState_sndPkt <= 5'd19;
			end
			5'd13: begin
				if(HCTxPortRdy) begin
					NextState_sndPkt <= 5'd20;
					next_fifoReadEn <= 1'b1; end
			end
			5'd14: begin
				if(~fifoEmpty)	NextState_sndPkt <= 5'd13;
				else			NextState_sndPkt <= 5'd16;
			end
			5'd15: begin
				next_HCTxPortWEn <= 1'b0;
				NextState_sndPkt <= 5'd5;
			end
			5'd16: begin
				if(HCTxPortRdy) begin
					NextState_sndPkt <= 5'd15;
					//Last byte is not valid data,
					//but the 'TX_PACKET_STOP' flag is required
					//by the SIE state machine to detect end of data packet
					next_HCTxPortWEn <= 1'b1;
					next_HCTxPortData <= 8'h00;
					next_HCTxPortCntl <= 8'h04; end
			end
			5'd19: begin
				next_HCTxPortWEn <= 1'b0;
				NextState_sndPkt <= 5'd14;
			end
			5'd20: begin
				next_fifoReadEn <= 1'b0;
				NextState_sndPkt <= 5'd12;
			end
			5'd21: begin
				if(HCTxPortRdy) begin
					NextState_sndPkt <= 5'd22;
					next_HCTxPortWEn <= 1'b1;
					next_HCTxPortData <= 8'h00;
					next_HCTxPortCntl <= 8'h06; end
			end
			5'd22: begin
				next_HCTxPortWEn <= 1'b0;
				NextState_sndPkt <= 5'd5;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_sndPkt <= 5'd0;
		else	CurrState_sndPkt <= NextState_sndPkt;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			sendPacketRdy <= 1'b1;
			HCTxPortReq <= 1'b0;
			HCTxPortWEn <= 1'b0;
			HCTxPortData <= 8'h00;
			HCTxPortCntl <= 8'h00;
			frameNum <= 11'h000;
			fifoReadEn <= 1'b0; end
		else begin
			sendPacketRdy <= next_sendPacketRdy;
			HCTxPortReq <= next_HCTxPortReq;
			HCTxPortWEn <= next_HCTxPortWEn;
			HCTxPortData <= next_HCTxPortData;
			HCTxPortCntl <= next_HCTxPortCntl;
			frameNum <= next_frameNum;
			fifoReadEn <= next_fifoReadEn; end
	end

endmodule
