module slaveGetPacket (
	input		[7:0]	RXDataIn,
	input				RXDataValid,
	input				RXFifoFull,
	input		[7:0]	RXStreamStatusIn,
	input				SIERxTimeOut,		// Single cycle pulse
	input				clk,
	input				endPointReady,
	input				getPacketEn,
	input				rst,
	output	reg			ACKRxed,
	output	reg			CRCError,
	output	reg	[7:0]	RXFifoData,
	output	reg			RXFifoWEn,
	output	reg			RXOverflow,
	output	reg			RXPacketRdy,
	output	reg			RXTimeOut,
	output	reg	[3:0]	RxPID,
	output	reg			SIERxTimeOutEn,
	output	reg			bitStuffError,
	output	reg			dataSequence
);

	reg				next_ACKRxed;
	reg				next_CRCError;
	reg		[7:0]	next_RXFifoData;
	reg				next_RXFifoWEn;
	reg				next_RXOverflow;
	reg				next_RXPacketRdy;
	reg				next_RXTimeOut;
	reg		[3:0]	next_RxPID;
	reg				next_SIERxTimeOutEn;
	reg				next_bitStuffError;
	reg				next_dataSequence;

	reg		[7:0]	RXByteOld, next_RXByteOld;
	reg		[7:0]	RXByteOldest, next_RXByteOldest;
	reg		[7:0]	RXByte, next_RXByte;
	reg		[7:0]	RXStreamStatus, next_RXStreamStatus;

	reg		[4:0]	CurrState_slvGetPkt;
	reg		[4:0]	NextState_slvGetPkt;

	//--------------------------------------------------------------------
	// Machine: slvGetPkt
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_slvGetPkt <= CurrState_slvGetPkt;
		// Set default values for outputs and signals
		next_CRCError <= CRCError;
		next_bitStuffError <= bitStuffError;
		next_RXOverflow <= RXOverflow;
		next_RXTimeOut <= RXTimeOut;
		next_ACKRxed <= ACKRxed;
		next_dataSequence <= dataSequence;
		next_SIERxTimeOutEn <= SIERxTimeOutEn;
		next_RXByte <= RXByte;
		next_RXStreamStatus <= RXStreamStatus;
		next_RxPID <= RxPID;
		next_RXPacketRdy <= RXPacketRdy;
		next_RXByteOldest <= RXByteOldest;
		next_RXByteOld <= RXByteOld;
		next_RXFifoWEn <= RXFifoWEn;
		next_RXFifoData <= RXFifoData;
		case(CurrState_slvGetPkt)
			5'd12: begin
				NextState_slvGetPkt <= 5'd15;
			end
			5'd13: begin
				next_CRCError <= 1'b0;
				next_bitStuffError <= 1'b0;
				next_RXOverflow <= 1'b0;
				next_RXTimeOut <= 1'b0;
				next_ACKRxed <= 1'b0;
				next_dataSequence <= 1'b0;
				next_SIERxTimeOutEn <= 1'b1;
				if(RXDataValid) begin
					NextState_slvGetPkt <= 5'd14;
					next_RXByte <= RXDataIn;
					next_RXStreamStatus <= RXStreamStatusIn; end
				else if(SIERxTimeOut) begin
					NextState_slvGetPkt <= 5'd16;
					next_RXTimeOut <= 1'b1; end
			end
			5'd14: begin
				if(RXStreamStatus==8'b0) begin
					NextState_slvGetPkt <= 5'd0;
					next_RxPID <= RXByte[3:0]; end
				else begin
					NextState_slvGetPkt <= 5'd16;
					next_RXTimeOut <= 1'b1; end
			end
			5'd15: begin
				next_RXPacketRdy <= 1'b0;
				next_SIERxTimeOutEn <= 1'b0;
				if(getPacketEn)	NextState_slvGetPkt <= 5'd13;
			end
			5'd16: begin
				next_RXPacketRdy <= 1'b1;
				NextState_slvGetPkt <= 5'd15;
			end
			5'd0: begin
				if(RXByte[1:0]==2'b10)		NextState_slvGetPkt <= 5'd1;
				else if(RXByte[1:0]==2'b11)	NextState_slvGetPkt <= 5'd2;
				else						NextState_slvGetPkt <= 5'd16;
			end
			5'd1: begin
				if(RXDataValid) begin
					NextState_slvGetPkt <= 5'd16;
					next_RXOverflow <= RXDataIn[2];
					next_ACKRxed <= RXDataIn[5]; end
			end
			5'd2: begin
				if(RXDataValid) begin
					NextState_slvGetPkt <= 5'd3;
					next_RXByte <= RXDataIn;
					next_RXStreamStatus <= RXStreamStatusIn; end
			end
			5'd3: begin
				if(RXStreamStatus==8'd1) begin
					NextState_slvGetPkt <= 5'd4;
					next_RXByteOldest <= RXByte; end
				else	NextState_slvGetPkt <= 5'd5;
			end
			5'd4: begin
				if(RXDataValid) begin
					NextState_slvGetPkt <= 5'd6;
					next_RXByte <= RXDataIn;
					next_RXStreamStatus <= RXStreamStatusIn; end
			end
			5'd5: begin
				next_CRCError <= RXByte[0];
				next_bitStuffError <= RXByte[1];
				next_dataSequence <= RXByte[6];
				NextState_slvGetPkt <= 5'd16;
			end
			5'd6: begin
				if(RXStreamStatus==8'd1) begin
					NextState_slvGetPkt <= 5'd7;
					next_RXByteOld <= RXByte; end
				else	NextState_slvGetPkt <= 5'd5;
			end
			5'd7: begin
				if(RXDataValid) begin
					NextState_slvGetPkt <= 5'd8;
					next_RXByte <= RXDataIn;
					next_RXStreamStatus <= RXStreamStatusIn; end
			end
			5'd8: begin
				if(RXStreamStatus==8'd1)	NextState_slvGetPkt <= 5'd9;
				else						NextState_slvGetPkt <= 5'd5;
			end
			5'd9: begin
				if(~endPointReady)	NextState_slvGetPkt <= 5'd18;
				else if(RXFifoFull) begin
					NextState_slvGetPkt <= 5'd10;
					next_RXOverflow <= 1'b1; end
				else begin
					NextState_slvGetPkt <= 5'd11;
					next_RXFifoWEn <= 1'b1;
					next_RXFifoData <= RXByteOldest;
					next_RXByteOldest <= RXByteOld;
					next_RXByteOld <= RXByte; end
			end
			5'd10: begin
				NextState_slvGetPkt <= 5'd11;
			end
			5'd11: begin
				next_RXFifoWEn <= 1'b0;
				if(RXDataValid&(RXStreamStatusIn==8'd1)) begin
					NextState_slvGetPkt <= 5'd17;
					next_RXByte <= RXDataIn; end
				else if(RXDataValid) begin
					NextState_slvGetPkt <= 5'd5;
					next_RXByte <= RXDataIn; end
			end
			5'd17: begin
				NextState_slvGetPkt <= 5'd9;
			end
			5'd18: begin	// Discard data
				NextState_slvGetPkt <= 5'd11;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_slvGetPkt <= 5'd12;
		else	CurrState_slvGetPkt <= NextState_slvGetPkt;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			RXByteOld <= 8'h00;
			RXByteOldest <= 8'h00;
			RXByte <= 8'h00;
			RXStreamStatus <= 8'h00;
			RXPacketRdy <= 1'b0;
			RXFifoWEn <= 1'b0;
			RXFifoData <= 8'h00;
			CRCError <= 1'b0;
			bitStuffError <= 1'b0;
			RXOverflow <= 1'b0;
			RXTimeOut <= 1'b0;
			ACKRxed <= 1'b0;
			dataSequence <= 1'b0;
			SIERxTimeOutEn <= 1'b0;
			RxPID <= 4'h0; end
		else begin
			RXByteOld <= next_RXByteOld;
			RXByteOldest <= next_RXByteOldest;
			RXByte <= next_RXByte;
			RXStreamStatus <= next_RXStreamStatus;
			RXPacketRdy <= next_RXPacketRdy;
			RXFifoWEn <= next_RXFifoWEn;
			RXFifoData <= next_RXFifoData;
			CRCError <= next_CRCError;
			bitStuffError <= next_bitStuffError;
			RXOverflow <= next_RXOverflow;
			RXTimeOut <= next_RXTimeOut;
			ACKRxed <= next_ACKRxed;
			dataSequence <= next_dataSequence;
			SIERxTimeOutEn <= next_SIERxTimeOutEn;
			RxPID <= next_RxPID; end
	end

endmodule
