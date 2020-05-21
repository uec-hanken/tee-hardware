module hostcontroller (
	input		[7:0]	RXStatus,
	input				clk,
	input				getPacketRdy,
	input				isoEn,
	input				rst,
	input				sendPacketArbiterGnt,
	input				sendPacketRdy,
	input				transReq,
	input		[1:0]	transType,
	output	reg			clearTXReq,
	output	reg			getPacketREn,
	output	reg			sendPacketArbiterReq,
	output	reg	[3:0]	sendPacketPID,
	output	reg			sendPacketWEn,
	output	reg			transDone
);

	reg				next_clearTXReq;
	reg				next_getPacketREn;
	reg				next_sendPacketArbiterReq;
	reg		[3:0]	next_sendPacketPID;
	reg				next_sendPacketWEn;
	reg				next_transDone;

	reg		[3:0]	delCnt, next_delCnt;

	reg		[5:0]	CurrState_hstCntrl;
	reg		[5:0]	NextState_hstCntrl;


	//--------------------------------------------------------------------
	// Machine: hstCntrl
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_hstCntrl <= CurrState_hstCntrl;
		// Set default values for outputs and signals
		next_sendPacketArbiterReq <= sendPacketArbiterReq;
		next_transDone <= transDone;
		next_clearTXReq <= clearTXReq;
		next_delCnt <= delCnt;
		next_sendPacketWEn <= sendPacketWEn;
		next_getPacketREn <= getPacketREn;
		next_sendPacketPID <= sendPacketPID;
		case(CurrState_hstCntrl) // synopsys parallel_case full_case
			6'd0: begin
				NextState_hstCntrl <= 6'd1;
			end
			6'd1: begin
				if(transReq) begin
					NextState_hstCntrl <= 6'd10;
					next_sendPacketArbiterReq <= 1'b1; end
			end
			6'd2: begin
				if(transType==2'b01)		NextState_hstCntrl <= 6'd17;
				else if(transType==2'b10)	NextState_hstCntrl <= 6'd19;
				else if(transType==2'b11)	NextState_hstCntrl <= 6'd29;
				else if(transType==2'b00)	NextState_hstCntrl <= 6'd16;
			end
			6'd3: begin
				next_transDone <= 1'b1;
				next_clearTXReq <= 1'b1;
				next_sendPacketArbiterReq <= 1'b0;
				next_delCnt <= 4'h0;
				NextState_hstCntrl <= 6'd9;
			end
			6'd9: begin
				next_clearTXReq <= 1'b0;
				next_transDone <= 1'b0;
				next_delCnt <= delCnt + 1'b1;
				//now wait for 'transReq' to clear
				if(delCnt==4'hf)	NextState_hstCntrl <= 6'd1;
			end
			6'd10: begin
				if(sendPacketArbiterGnt)	NextState_hstCntrl <= 6'd2;
			end
			6'd7: begin
				next_sendPacketWEn <= 1'b0;
				NextState_hstCntrl <= 6'd20;
			end
			6'd8: begin
				next_sendPacketWEn <= 1'b0;
				NextState_hstCntrl <= 6'd21;
			end
			6'd11: begin
				next_getPacketREn <= 1'b0;
				if(getPacketRdy)	NextState_hstCntrl <= 6'd3;
			end
			6'd16: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd7;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'hd; end
			end
			6'd20: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd8;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'h3; end
			end
			6'd21: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd11;
					next_getPacketREn <= 1'b1; end
			end
			6'd4: begin
				next_getPacketREn <= 1'b0;
				if(getPacketRdy)	NextState_hstCntrl <= 6'd5;
			end
			6'd5: begin
				if(isoEn)						NextState_hstCntrl <= 6'd3;
				else if(RXStatus[5:0]==6'b0)	NextState_hstCntrl <= 6'd18;
				else							NextState_hstCntrl <= 6'd3;
			end
			6'd6: begin
				next_sendPacketWEn <= 1'b0;
				NextState_hstCntrl <= 6'd23;
			end
			6'd12: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd4;
					next_getPacketREn <= 1'b1; end
			end
			6'd17: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd22;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'h9; end
			end
			6'd18: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd6;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'h2; end
			end
			6'd22: begin
				next_sendPacketWEn <= 1'b0;
				NextState_hstCntrl <= 6'd12;
			end
			6'd23: begin
				if(sendPacketRdy)	NextState_hstCntrl <= 6'd3;
			end
			6'd13: begin
				next_getPacketREn <= 1'b0;
				if(getPacketRdy)	NextState_hstCntrl <= 6'd3;
			end
			6'd14: begin
				if(sendPacketRdy)	NextState_hstCntrl <= 6'd32;
			end
			6'd15: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd25;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'h3; end
			end
			6'd19: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd24;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'h1; end
			end
			6'd24: begin
				next_sendPacketWEn <= 1'b0;
				NextState_hstCntrl <= 6'd15;
			end
			6'd25: begin
				next_sendPacketWEn <= 1'b0;
				NextState_hstCntrl <= 6'd14;
			end
			6'd32: begin
				if(~isoEn) begin
					NextState_hstCntrl <= 6'd13;
					next_getPacketREn <= 1'b1; end
				else	NextState_hstCntrl <= 6'd3;
			end
			6'd26: begin
				next_getPacketREn <= 1'b0;
				if(getPacketRdy)	NextState_hstCntrl <= 6'd3;
			end
			6'd27: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd31;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'hb; end
			end
			6'd28: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd26;
					next_getPacketREn <= 1'b1; end
			end
			6'd29: begin
				if(sendPacketRdy) begin
					NextState_hstCntrl <= 6'd30;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'h1; end
			end
			6'd30: begin
				next_sendPacketWEn <= 1'b0;
				NextState_hstCntrl <= 6'd27;
			end
			6'd31: begin
				next_sendPacketWEn <= 1'b0;
				NextState_hstCntrl <= 6'd28;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_hstCntrl <= 6'd0;
		else	CurrState_hstCntrl <= NextState_hstCntrl;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			delCnt <= 4'h0;
			transDone <= 1'b0;
			clearTXReq <= 1'b0;
			getPacketREn <= 1'b0;
			sendPacketArbiterReq <= 1'b0;
			sendPacketWEn <= 1'b0;
			sendPacketPID <= 4'b0; end
		else begin
			delCnt <= next_delCnt;
			transDone <= next_transDone;
			clearTXReq <= next_clearTXReq;
			getPacketREn <= next_getPacketREn;
			sendPacketArbiterReq <= next_sendPacketArbiterReq;
			sendPacketWEn <= next_sendPacketWEn;
			sendPacketPID <= next_sendPacketPID; end
	end

endmodule
