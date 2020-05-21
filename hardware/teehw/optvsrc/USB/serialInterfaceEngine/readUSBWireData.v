module readUSBWireData (
	input		[1:0]	RxBitsIn,
	output	reg			RxDataInTick,
	input				SIERxRdyIn,
	input				clk,
	input				fullSpeedRate,
	input				rst,
	input				TxWireActiveDrive,
	output	reg	[1:0]	RxBitsOut,
	output	reg			SIERxWEn,
	output	reg			noActivityTimeOut,
	output	reg			RxWireActive,
	input				noActivityTimeOutEnable
);

	reg		[2:0]	buffer0;
	reg		[2:0]	buffer1;
	reg		[2:0]	buffer2;
	reg		[2:0]	buffer3;
	reg		[2:0]	bufferCnt;
	reg		[1:0]	bufferInIndex;
	reg		[1:0]	bufferOutIndex;
	reg				decBufferCnt;
	reg		[4:0]	sampleCnt;
	reg				incBufferCnt;
	reg		[1:0]	oldRxBitsIn;
	reg		[1:0]	RxBitsInReg;
	reg		[15:0]	timeOutCnt;
	reg		[7:0]	rxActiveCnt;
	reg				RxWireEdgeDetect;
	reg				RxWireActiveReg;
	reg				RxWireActiveReg2;
	reg		[1:0]	RxBitsInSyncReg1;
	reg		[1:0]	RxBitsInSyncReg2;
	reg		[1:0]	bufferOutStMachCurrState;

	// re-synchronize incoming bits
	always@(posedge clk) begin
		RxBitsInSyncReg1 <= RxBitsIn;
		RxBitsInSyncReg2 <= RxBitsInSyncReg1;
	end

	always @(posedge clk) begin
		if(rst)								bufferCnt <= 3'b000;
		else if(incBufferCnt&~decBufferCnt)	bufferCnt <= bufferCnt + 1'b1;
		else if(~incBufferCnt&decBufferCnt)	bufferCnt <= bufferCnt - 1'b1;
		else								bufferCnt <= bufferCnt;
	end

	//Perform line rate clock recovery
	//Recover the wire data, and store data to buffer
	always@(posedge clk) begin
		if(rst) begin
			sampleCnt <= 5'b00000;
			incBufferCnt <= 1'b0;
			bufferInIndex <= 2'b00;
			buffer0 <= 3'b000;
			buffer1 <= 3'b000;
			buffer2 <= 3'b000;
			buffer3 <= 3'b000;
			RxDataInTick <= 1'b0;
			RxWireEdgeDetect <= 1'b0;
			RxWireActiveReg <= 1'b0;
			RxWireActiveReg2 <= 1'b0; end
		else begin
			RxWireActiveReg2 <= RxWireActiveReg; //Delay 'RxWireActiveReg' until after 'sampleCnt' has been reset
			RxBitsInReg <= RxBitsInSyncReg2;    
			oldRxBitsIn <= RxBitsInReg;
			incBufferCnt <= 1'b0;         //default value
			if(~TxWireActiveDrive & (RxBitsInSyncReg2!=RxBitsInReg)) begin  //if edge detected then
				sampleCnt <= 5'b00000;        
				RxWireEdgeDetect <= 1'b1;   // flag receive activity 
				RxWireActiveReg <= 1'b1;
				rxActiveCnt <= 8'h00; end
			else begin
				sampleCnt <= sampleCnt + 1'b1;
				RxWireEdgeDetect <= 1'b0;
				rxActiveCnt <= rxActiveCnt + 1'b1;
				//clear 'RxWireActiveReg' if no RX transitions for RX_EDGE_DET_TOUT USB bit periods 
				if( (fullSpeedRate&(rxActiveCnt=='d28)) | (~fullSpeedRate&(rxActiveCnt=='d224)) ) 
					RxWireActiveReg <= 1'b0;
			end
			if( (fullSpeedRate&(sampleCnt[1:0]==2'b10)) | (~fullSpeedRate&(sampleCnt==5'b10000)) ) begin
				RxDataInTick <= !RxDataInTick;
				if (TxWireActiveDrive != 1'b1) begin  //do not read wire data when transmitter is active
					incBufferCnt <= 1'b1;
					bufferInIndex <= bufferInIndex + 1'b1;
					case (bufferInIndex)
						2'b00 : buffer0 <= {RxWireActiveReg2, oldRxBitsIn}; 
						2'b01 : buffer1 <= {RxWireActiveReg2, oldRxBitsIn};
						2'b10 : buffer2 <= {RxWireActiveReg2, oldRxBitsIn};
						2'b11 : buffer3 <= {RxWireActiveReg2, oldRxBitsIn};
					endcase
				end
			end
		end
	end

	//read from buffer, and output to SIEReceiver
	always@(posedge clk) begin
		if(rst) begin
			decBufferCnt <= 1'b0;
			bufferOutIndex <= 2'b00;
			RxBitsOut <= 2'b00;
			SIERxWEn <= 1'b0;
			bufferOutStMachCurrState <= 2'd0; end
		else begin
			case (bufferOutStMachCurrState)
			2'd0: begin
				if(bufferCnt!=3'b000)	bufferOutStMachCurrState <= 2'd1;
			end
			2'd1: begin
				if(SIERxRdyIn) begin 
					SIERxWEn <= 1'b1;
					bufferOutStMachCurrState <= 2'd2;
					decBufferCnt <= 1'b1;
					bufferOutIndex <= bufferOutIndex + 1'b1;
					case (bufferOutIndex)
						2'b00 : begin RxBitsOut <= buffer0[1:0]; RxWireActive <= buffer0[2]; end
						2'b01 : begin RxBitsOut <= buffer1[1:0]; RxWireActive <= buffer1[2]; end
						2'b10 : begin RxBitsOut <= buffer2[1:0]; RxWireActive <= buffer2[2]; end
						2'b11 : begin RxBitsOut <= buffer3[1:0]; RxWireActive <= buffer3[2]; end
					endcase
				end
			end
			2'd2: begin
				SIERxWEn <= 1'b0;
				decBufferCnt <= 1'b0;
				bufferOutStMachCurrState <= 2'd0;
			end
		endcase
	end
	end

	//generate 'noActivityTimeOut' pulse if no tx or rx activity for RX_PACKET_TOUT USB bit periods
	//'noActivityTimeOut'  pulse can only be generated when the host or slave getPacket
	//process enables via 'noActivityTimeOutEnable' signal
	//'noActivityTimeOut' pulse is used by host and slave getPacket processes to determine if 
	//there has been a response time out.
	always@(posedge clk) begin
		if(rst) begin
			timeOutCnt <= 16'h0000;
			noActivityTimeOut <= 1'b0; end
		else begin
			if(TxWireActiveDrive|RxWireEdgeDetect|~noActivityTimeOutEnable)
					timeOutCnt <= 16'h0000;
			else	timeOutCnt <= timeOutCnt + 1'b1;
			if( (fullSpeedRate&(timeOutCnt==72)) | (~fullSpeedRate&(timeOutCnt==576)) ) 
					noActivityTimeOut <= 1'b1; 
			else	noActivityTimeOut <= 1'b0;
		end
	end

endmodule
