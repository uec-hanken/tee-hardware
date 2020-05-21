module processRxBit (
	input		[1:0]	JBit,
	input		[1:0]	KBit,
	input		[1:0]	RxBitsIn,
	input				RxWireActive,
	input				clk,
	input				processRxBitsWEn,
	input				processRxByteRdy,
	input				rst,
	output	reg	[7:0]	RxCtrlOut,
	output	reg	[7:0]	RxDataOut,
	output	reg			processRxBitRdy,
	output	reg			processRxByteWEn,
	output	reg			resumeDetected,
	input				fullSpeedBitRate
);

	reg		[7:0]	next_RxCtrlOut;
	reg		[7:0]	next_RxDataOut;

	reg				next_processRxBitRdy;

	reg				next_processRxByteWEn;
	reg				next_resumeDetected;

	reg		[3:0]	RXBitCount, next_RXBitCount;
	reg		[1:0]	RXBitStMachCurrState, next_RXBitStMachCurrState;
	reg		[7:0]	RXByte, next_RXByte;
	reg		[3:0]	RXSameBitCount, next_RXSameBitCount;
	reg		[1:0]	RxBits, next_RxBits;
	reg				bitStuffError, next_bitStuffError;
	reg		[1:0]	oldRXBits, next_oldRXBits;
	reg		[4:0]	resumeWaitCnt, next_resumeWaitCnt;
	reg		[7:0]	delayCnt, next_delayCnt;

	reg		[3:0]	CurrState_prRxBit;
	reg		[3:0]	NextState_prRxBit;

	//--------------------------------------------------------------------
	// Machine: prRxBit
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_prRxBit <= CurrState_prRxBit;
		// Set default values for outputs and signals
		next_processRxByteWEn <= processRxByteWEn;
		next_RxCtrlOut <= RxCtrlOut;
		next_RxDataOut <= RxDataOut;
		next_resumeDetected <= resumeDetected;
		next_RXBitStMachCurrState <= RXBitStMachCurrState;
		next_RxBits <= RxBits;
		next_RXSameBitCount <= RXSameBitCount;
		next_RXBitCount <= RXBitCount;
		next_oldRXBits <= oldRXBits;
		next_RXByte <= RXByte;
		next_bitStuffError <= bitStuffError;
		next_resumeWaitCnt <= resumeWaitCnt;
		next_delayCnt <= delayCnt;
		next_processRxBitRdy <= processRxBitRdy;
		case(CurrState_prRxBit)
			4'd0: begin
				next_processRxByteWEn <= 1'b0;
				next_RxCtrlOut <= 8'h00;
				next_RxDataOut <= 8'h00;
				next_resumeDetected <= 1'b0;
				next_RXBitStMachCurrState <= 2'd0;
				next_RxBits <= 2'b00;
				next_RXSameBitCount <= 4'h0;
				next_RXBitCount <= 4'h0;
				next_oldRXBits <= 2'b00;
				next_RXByte <= 8'h00;
				next_bitStuffError <= 1'b0;
				next_resumeWaitCnt <= 5'h0;
				next_processRxBitRdy <= 1'b1;
				NextState_prRxBit <= 4'd2;
			end
			4'd2: begin
				if(processRxBitsWEn&(RXBitStMachCurrState==2'd2)) begin
					NextState_prRxBit <= 4'd9;
					next_RxBits <= RxBitsIn;
					next_processRxBitRdy <= 1'b0; end
				else if(processRxBitsWEn&(RXBitStMachCurrState==2'd1)) begin
					NextState_prRxBit <= 4'd5;
					next_RxBits <= RxBitsIn;
					next_processRxBitRdy <= 1'b0; end
				else if(processRxBitsWEn&(RXBitStMachCurrState==2'd0)) begin
					NextState_prRxBit <= 4'd3;
					next_RxBits <= RxBitsIn;
					next_processRxBitRdy <= 1'b0; end
				else if(processRxBitsWEn&(RXBitStMachCurrState==2'd3)) begin
					NextState_prRxBit <= 4'd11;
					next_RxBits <= RxBitsIn;
					next_processRxBitRdy <= 1'b0; end
			end
			4'd1: begin
				next_processRxByteWEn <= 1'b0;
				next_RXBitStMachCurrState <= 2'd1;
				next_RXSameBitCount <= 4'h0;
				next_RXBitCount <= 4'h1;
				next_oldRXBits <= RxBits;
				//zero is always the first RZ data bit of a new packet
				next_RXByte <= 8'h00;
				NextState_prRxBit <= 4'd2;
				next_processRxBitRdy <= 1'b1;
			end
			4'd3: begin
				if((RxBits==KBit)&RxWireActive)	NextState_prRxBit <= 4'd12;
				else begin
					NextState_prRxBit <= 4'd2;
					next_processRxBitRdy <= 1'b1; end
			end
			4'd12: begin
				if(processRxByteRdy) begin
					NextState_prRxBit <= 4'd1;
					next_RxDataOut <= 8'h00;
					//redundant data
					next_RxCtrlOut <= 8'd0;
					//4'd0 of packet
					next_processRxByteWEn <= 1'b1; end
			end
			4'd4: begin
				next_processRxByteWEn <= 1'b0;
				next_RXBitStMachCurrState <= 2'd0;
				NextState_prRxBit <= 4'd2;
				next_processRxBitRdy <= 1'b1;
			end
			4'd5: begin
				next_bitStuffError <= 1'b0;
				if(RxBits==2'b00) begin
					if(~fullSpeedBitRate) begin
						NextState_prRxBit <= 4'd15;
						next_delayCnt <= 8'h00; end
					else	NextState_prRxBit <= 4'd13; end
				else begin
					NextState_prRxBit <= 4'd6;
					if(RxBits==oldRXBits) begin		//if the current 'RxBits' are the same as the old 'RxBits', then
						next_RXSameBitCount <= RXSameBitCount + 1'b1;	//inc 'RXSameBitCount'
						if(RXSameBitCount==4'h6)	next_bitStuffError <= 1'b1;	//if 'RXSameBitCount' == 6 there has been a bit stuff error
						//flag 'bitStuffError'
						else begin		//else no bit stuffing error
							next_RXBitCount <= RXBitCount + 1'b1;
							if(RXBitCount!=4'h7)	next_processRxBitRdy <= 1'b1;	//early indication of ready
							next_RXByte <= { 1'b1, RXByte[7:1]}; end end	//RZ bit = 1 (ie no change in 'RxBits')
					else begin		//else current 'RxBits' are different from old 'RxBits'
						if(RXSameBitCount!=4'h6) begin	//if this is not the RZ 0 bit after 6 consecutive RZ 1s, then
							next_RXBitCount <= RXBitCount + 1'b1;
							if(RXBitCount!=4'h7)	next_processRxBitRdy <= 1'b1;	//early indication of ready
							next_RXByte <= {1'b0, RXByte[7:1]}; end		//RZ bit = 0 (ie current'RxBits' is different than old 'RxBits')
						next_RXSameBitCount <= 4'h0; end	//reset 'RXSameBitCount'
					next_oldRXBits <= RxBits; end
			end
			4'd13: begin
				if(processRxByteRdy) begin
					NextState_prRxBit <= 4'd4;
					next_RxDataOut <= 8'h00;
					//redundant data
					next_RxCtrlOut <= 8'd1;
					//end of packet
					next_processRxByteWEn <= 1'b1; end
			end
			4'd6: begin
				if((RXBitCount==4'h8)&~bitStuffError)	NextState_prRxBit <= 4'd8;
				else if(bitStuffError)					NextState_prRxBit <= 4'd14;
				else begin
					NextState_prRxBit <= 4'd2;
					next_processRxBitRdy <= 1'b1; end
			end
			4'd7: begin
				next_processRxByteWEn <= 1'b0;
				NextState_prRxBit <= 4'd2;
				next_processRxBitRdy <= 1'b1;
			end
			4'd8: begin
				if(processRxByteRdy) begin
					NextState_prRxBit <= 4'd7;
					next_RXBitCount <= 4'h0;
					next_RxDataOut <= RXByte;
					next_RxCtrlOut <= 8'd2;
					next_processRxByteWEn <= 1'b1; end
			end
			4'd10: begin
				next_processRxByteWEn <= 1'b0;
				if(RxBits==JBit)	next_RXBitStMachCurrState <= 2'd0;	//if current bit is a JBit, then next state is idle
				else begin
					next_RXBitStMachCurrState <= 2'd2;	//check for resume
					next_resumeWaitCnt <= 5'h0; end
				NextState_prRxBit <= 4'd2;
				next_processRxBitRdy <= 1'b1;
			end
			4'd14: begin
				if(processRxByteRdy) begin
					NextState_prRxBit <= 4'd10;
					next_RxDataOut <= 8'h00;
					//redundant data
					next_RxCtrlOut <= 8'd3;
					next_processRxByteWEn <= 1'b1; end
			end
			4'd9: begin
				if(RxBits!=KBit)	next_RXBitStMachCurrState <= 2'd0;	//can only be a resume if line remains in Kbit state
				else begin
					next_resumeWaitCnt <= resumeWaitCnt + 1'b1;
					if(resumeWaitCnt==5'd29) begin	//if we've waited long enough, then
						next_RXBitStMachCurrState <= 2'd3;
						next_resumeDetected <= 1'b1; end end	//report resume detected
				NextState_prRxBit <= 4'd2;
				next_processRxBitRdy <= 1'b1;
			end
			4'd11: begin
				if(RxBits!=KBit) begin	//line must leave KBit state for the end of resume
					next_RXBitStMachCurrState <= 2'd0;
					next_resumeDetected <= 1'b0; end	//clear resume detected flag
				NextState_prRxBit <= 4'd2;
				next_processRxBitRdy <= 1'b1;
			end
			4'd15: begin
				//turn around time must be at least 2 low speed bit periods
				next_delayCnt <= delayCnt + 1'b1;
				if(delayCnt==64)	NextState_prRxBit <= 4'd13;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_prRxBit <= 4'd0;
		else	CurrState_prRxBit <= NextState_prRxBit;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			RXBitStMachCurrState <= 2'd0;
			RxBits <= 2'b00;
			RXSameBitCount <= 4'h0;
			RXBitCount <= 4'h0;
			oldRXBits <= 2'b00;
			RXByte <= 8'h00;
			bitStuffError <= 1'b0;
			resumeWaitCnt <= 5'h0;
			delayCnt <= 8'h00;
			processRxByteWEn <= 1'b0;
			RxCtrlOut <= 8'h00;
			RxDataOut <= 8'h00;
			resumeDetected <= 1'b0;
			processRxBitRdy <= 1'b1; end
		else begin
			RXBitStMachCurrState <= next_RXBitStMachCurrState;
			RxBits <= next_RxBits;
			RXSameBitCount <= next_RXSameBitCount;
			RXBitCount <= next_RXBitCount;
			oldRXBits <= next_oldRXBits;
			RXByte <= next_RXByte;
			bitStuffError <= next_bitStuffError;
			resumeWaitCnt <= next_resumeWaitCnt;
			delayCnt <= next_delayCnt;
			processRxByteWEn <= next_processRxByteWEn;
			RxCtrlOut <= next_RxCtrlOut;
			RxDataOut <= next_RxDataOut;
			resumeDetected <= next_resumeDetected;
			processRxBitRdy <= next_processRxBitRdy; end
	end

endmodule
