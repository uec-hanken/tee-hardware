module slaveSendPacket (
	input		[3:0]	PID,
	input				SCTxPortGnt,
	input				SCTxPortRdy,
	input				clk,
	input		[7:0]	fifoData,
	input				fifoEmpty,
	input				rst,
	input				sendPacketWEn,
	output	reg	[7:0]	SCTxPortCntl,
	output	reg	[7:0]	SCTxPortData,
	output	reg			SCTxPortReq,
	output	reg			SCTxPortWEn,
	output	reg			fifoReadEn,
	output	reg			sendPacketRdy
);

	reg		[7:0]	next_SCTxPortCntl;
	reg		[7:0]	next_SCTxPortData;
	reg				next_SCTxPortReq;
	reg				next_SCTxPortWEn;
	reg				next_fifoReadEn;
	reg				next_sendPacketRdy;

	wire	[7:0]	PIDNotPID;
	reg		[3:0]	CurrState_slvSndPkt;
	reg		[3:0]	NextState_slvSndPkt;

	assign PIDNotPID = {(PID^4'hf), PID};

	//--------------------------------------------------------------------
	// Machine: slvSndPkt
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_slvSndPkt <= CurrState_slvSndPkt;
		// Set default values for outputs and signals
		next_sendPacketRdy <= sendPacketRdy;
		next_SCTxPortReq <= SCTxPortReq;
		next_SCTxPortWEn <= SCTxPortWEn;
		next_SCTxPortData <= SCTxPortData;
		next_SCTxPortCntl <= SCTxPortCntl;
		next_fifoReadEn <= fifoReadEn;
		case (CurrState_slvSndPkt)
			4'd0: begin
				NextState_slvSndPkt <= 4'd1;
			end
			4'd1: begin
				if(sendPacketWEn) begin
					NextState_slvSndPkt <= 4'd2;
					next_sendPacketRdy <= 1'b0;
					next_SCTxPortReq <= 1'b1; end
			end
			4'd2: begin
				if(SCTxPortGnt)	NextState_slvSndPkt <= 4'd3;
			end
			4'd5: begin
				NextState_slvSndPkt <= 4'd1;
				next_sendPacketRdy <= 1'b1;
				next_SCTxPortReq <= 1'b0;
			end
			4'd11: begin
				NextState_slvSndPkt <= 4'd5;
			end
			4'd3: begin
				if(SCTxPortRdy) begin
					NextState_slvSndPkt <= 4'd4;
					next_SCTxPortWEn <= 1'b1;
					next_SCTxPortData <= PIDNotPID;
					next_SCTxPortCntl <= 8'h02; end
			end
			4'd4: begin
				next_SCTxPortWEn <= 1'b0;
				if((PID==4'h3)|(PID==4'hb))	NextState_slvSndPkt <= 4'd8;
				else						NextState_slvSndPkt <= 4'd11;
			end
			4'd6: begin
				next_SCTxPortWEn <= 1'b1;
				next_SCTxPortData <= fifoData;
				next_SCTxPortCntl <= 8'h03;
				NextState_slvSndPkt <= 4'd12;
			end
			4'd7: begin
				if(SCTxPortRdy) begin
					NextState_slvSndPkt <= 4'd13;
					next_fifoReadEn <= 1'b1; end
			end
			4'd8: begin
				if(~fifoEmpty)	NextState_slvSndPkt <= 4'd7;
				else			NextState_slvSndPkt <= 4'd10;
			end
			4'd9: begin
				next_SCTxPortWEn <= 1'b0;
				NextState_slvSndPkt <= 4'd5;
			end
			4'd10: begin
				if(SCTxPortRdy) begin
					NextState_slvSndPkt <= 4'd9;
					//Last byte is not valid data,
					//but the 'TX_PACKET_STOP' flag is required
					//by the SIE state machine to detect end of data packet
					next_SCTxPortWEn <= 1'b1;
					next_SCTxPortData <= 8'h00;
					next_SCTxPortCntl <= 8'h04; end
			end
			4'd12: begin
				next_SCTxPortWEn <= 1'b0;
				NextState_slvSndPkt <= 4'd8;
			end
			4'd13: begin
				next_fifoReadEn <= 1'b0;
				NextState_slvSndPkt <= 4'd6;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_slvSndPkt <= 4'd0;
		else	CurrState_slvSndPkt <= NextState_slvSndPkt;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			sendPacketRdy <= 1'b1;
			SCTxPortReq <= 1'b0;
			SCTxPortWEn <= 1'b0;
			SCTxPortData <= 8'h00;
			SCTxPortCntl <= 8'h00;
			fifoReadEn <= 1'b0; end
		else begin
			sendPacketRdy <= next_sendPacketRdy;
			SCTxPortReq <= next_SCTxPortReq;
			SCTxPortWEn <= next_SCTxPortWEn;
			SCTxPortData <= next_SCTxPortData;
			SCTxPortCntl <= next_SCTxPortCntl;
			fifoReadEn <= next_fifoReadEn; end
	end

endmodule
