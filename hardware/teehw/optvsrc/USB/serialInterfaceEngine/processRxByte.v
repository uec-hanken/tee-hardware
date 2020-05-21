module processRxByte (
	input		[15:0]	CRC16Result,
	input				CRC16UpdateRdy,
	input		[4:0]	CRC5Result,
	input				CRC5UpdateRdy,
	input		[7:0]	RxByteIn,
	input		[7:0]	RxCtrlIn,
	input				clk,
	input				processRxDataInWEn,
	input				rst,
	output	reg			CRC16En,
	output	reg			CRC5En,
	output	reg			CRC5_8Bit,
	output	reg	[7:0]	CRCData,
	output	reg	[7:0]	RxCtrlOut,
	output	reg			RxDataOutWEn,
	output	reg	[7:0]	RxDataOut,
	output	reg			processRxByteRdy,
	output	reg			rstCRC
);

	reg				next_CRC16En;
	reg				next_CRC5En;
	reg				next_CRC5_8Bit;
	reg		[7:0]	next_CRCData;
	reg		[7:0]	next_RxCtrlOut;
	reg				next_RxDataOutWEn;
	reg		[7:0]	next_RxDataOut;
	reg				next_processRxByteRdy;
	reg				next_rstCRC;

	reg				ACKRxed, next_ACKRxed;
	reg				CRCError, next_CRCError;
	reg				NAKRxed, next_NAKRxed;
	reg		[2:0]	RXByteStMachCurrState, next_RXByteStMachCurrState;
	reg		[9:0]	RXDataByteCnt, next_RXDataByteCnt;
	reg		[7:0]	RxByte, next_RxByte;
	reg		[7:0]	RxCtrl, next_RxCtrl;
	reg				RxOverflow, next_RxOverflow;
	wire	[7:0]	RxStatus;
	reg				RxTimeOut, next_RxTimeOut;
	reg				Signal1, next_Signal1;
	reg				bitStuffError, next_bitStuffError;
	reg				dataSequence, next_dataSequence;
	reg				stallRxed, next_stallRxed;

	reg		[3:0]	CurrState_prRxByte;
	reg		[3:0]	NextState_prRxByte;

	assign RxStatus = {1'b0, next_dataSequence, next_ACKRxed, next_stallRxed, next_NAKRxed, next_RxOverflow, next_bitStuffError, next_CRCError};

	//--------------------------------------------------------------------
	// Machine: prRxByte
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_prRxByte <= CurrState_prRxByte;
		// Set default values for outputs and signals
		next_RxByte <= RxByte;
		next_RxCtrl <= RxCtrl;
		next_RXByteStMachCurrState <= RXByteStMachCurrState;
		next_CRCError <= CRCError;
		next_bitStuffError <= bitStuffError;
		next_RxOverflow <= RxOverflow;
		next_RxTimeOut <= RxTimeOut;
		next_NAKRxed <= NAKRxed;
		next_stallRxed <= stallRxed;
		next_ACKRxed <= ACKRxed;
		next_dataSequence <= dataSequence;
		next_RxDataOut <= RxDataOut;
		next_RxCtrlOut <= RxCtrlOut;
		next_RxDataOutWEn <= RxDataOutWEn;
		next_rstCRC <= rstCRC;
		next_CRCData <= CRCData;
		next_CRC5En <= CRC5En;
		next_CRC5_8Bit <= CRC5_8Bit;
		next_CRC16En <= CRC16En;
		next_RXDataByteCnt <= RXDataByteCnt;
		next_processRxByteRdy <= processRxByteRdy;
		case(CurrState_prRxByte)
			4'd0: begin
				if(RXByteStMachCurrState==3'd3)	NextState_prRxByte <= 4'd8;
				else if(RXByteStMachCurrState==3'd4)	NextState_prRxByte <= 4'd13;
				else if(RXByteStMachCurrState==3'd5)	NextState_prRxByte <= 4'd14;
				else if(RXByteStMachCurrState==3'd0)	NextState_prRxByte <= 4'd3;
				else if(RXByteStMachCurrState==3'd1)	NextState_prRxByte <= 4'd4;
				else if(RXByteStMachCurrState==3'd2)	NextState_prRxByte <= 4'd5;
			end
			4'd1: begin
				next_RxByte <= 8'h00;
				next_RxCtrl <= 8'h00;
				next_RXByteStMachCurrState <= 3'd0;
				next_CRCError <= 1'b0;
				next_bitStuffError <= 1'b0;
				next_RxOverflow <= 1'b0;
				next_RxTimeOut <= 1'b0;
				next_NAKRxed <= 1'b0;
				next_stallRxed <= 1'b0;
				next_ACKRxed <= 1'b0;
				next_dataSequence <= 1'b0;
				next_RxDataOut <= 8'h00;
				next_RxCtrlOut <= 8'h00;
				next_RxDataOutWEn <= 1'b0;
				next_rstCRC <= 1'b0;
				next_CRCData <= 8'h00;
				next_CRC5En <= 1'b0;
				next_CRC5_8Bit <= 1'b0;
				next_CRC16En <= 1'b0;
				next_RXDataByteCnt <= 10'h00;
				next_processRxByteRdy <= 1'b1;
				NextState_prRxByte <= 4'd2;
			end
			4'd2: begin
				if(processRxDataInWEn) begin
					NextState_prRxByte <= 4'd0;
					next_RxByte <= RxByteIn;
					next_RxCtrl <= RxCtrlIn;
					next_processRxByteRdy <= 1'b0; end
			end
			4'd7: begin
				next_RxDataOutWEn <= 1'b0;
				next_RXByteStMachCurrState <= 3'd0;
				NextState_prRxByte <= 4'd2;
				next_processRxByteRdy <= 1'b1;
			end
			4'd8: begin
				NextState_prRxByte <= 4'd7;
				if(RxCtrl!=8'd1)	next_RxOverflow <= 1'b1;	//If more than PID rxed, then report error
				next_RxDataOut <= RxStatus;
				next_RxCtrlOut <= 2;
				next_RxDataOutWEn <= 1'b1;
			end
			4'd5: begin
				if((RxByte[7:4]^RxByte[3:0])!=4'hf) begin
					NextState_prRxByte <= 4'd2;
					next_RXByteStMachCurrState <= 3'd0;
					next_processRxByteRdy <= 1'b1; end
				else begin
					NextState_prRxByte <= 4'd6;
					next_CRCError <= 1'b0;
					next_bitStuffError <= 1'b0;
					next_RxOverflow <= 1'b0;
					next_NAKRxed <= 1'b0;
					next_stallRxed <= 1'b0;
					next_ACKRxed <= 1'b0;
					next_dataSequence <= 1'b0;
					next_RxTimeOut <= 1'b0;
					next_RXDataByteCnt <= 10'h000;
					next_RxDataOut <= RxByte;
					next_RxCtrlOut <= 0;
					next_RxDataOutWEn <= 1'b1;
					next_rstCRC <= 1'b1; end
			end
			4'd6: begin
				next_rstCRC <= 1'b0;
				next_RxDataOutWEn <= 1'b0;
				case(RxByte[1:0] )
					2'b00: begin	//Special PID.
						next_RXByteStMachCurrState <= 3'd0;
					end
					2'b01: begin	//Token PID
						next_RXByteStMachCurrState <= 3'd4;
						next_RXDataByteCnt <= 0;
					end
					2'b10: begin	//Handshake PID
						case(RxByte[3:2])
							2'b00:	next_ACKRxed <= 1'b1;
							2'b10:	next_NAKRxed <= 1'b1;
							2'b11:	next_stallRxed <= 1'b1;
							default:;
						endcase
						next_RXByteStMachCurrState <= 3'd3;
					end
					2'b11: begin	//Data PID
						case(RxByte[3:2])
							2'b00:	next_dataSequence <= 1'b0;
							2'b10:	next_dataSequence <= 1'b1;
							default:;
						endcase
						next_RXByteStMachCurrState <= 3'd5;
						next_RXDataByteCnt <= 0;
					end
				endcase
				NextState_prRxByte <= 4'd2;
				next_processRxByteRdy <= 1'b1;
			end
			4'd11: begin
				next_CRC16En <= 1'b0;
				next_RxDataOutWEn <= 1'b0;
				NextState_prRxByte <= 4'd2;
				next_processRxByteRdy <= 1'b1;
			end
			4'd12: begin
				next_RXDataByteCnt <= RXDataByteCnt + 1'b1;
				case(RxCtrl)
					8'd1: begin
						if(CRC16Result!=16'hb001)	next_CRCError <= 1'b1;
						next_RxDataOut <= RxStatus;
						next_RxCtrlOut <= 2;
						next_RXByteStMachCurrState <= 3'd0;
					end
					8'd3: begin
						next_bitStuffError <= 1'b1;
						next_RxDataOut <= RxStatus;
						next_RxCtrlOut <= 2;
						next_RXByteStMachCurrState <= 3'd0;
					end
					8'd2: begin
						next_RxDataOut <= RxByte;
						next_RxCtrlOut <= 1;
						next_CRCData <= RxByte;
						next_CRC16En <= 1'b1;
					end
					default: begin
						next_RXByteStMachCurrState <= 3'd0;
					end
				endcase
				next_RxDataOutWEn <= 1'b1;
				NextState_prRxByte <= 4'd11;
			end
			4'd14: begin
				if(CRC16UpdateRdy)	NextState_prRxByte <= 4'd12;
			end
			4'd9: begin
				next_RXDataByteCnt <= RXDataByteCnt + 1'b1;
				case(RxCtrl)
					8'd1: begin
						if(CRC5Result!=5'h6)	next_CRCError <= 1'b1;
						next_RxDataOut <= RxStatus;
						next_RxCtrlOut <= 2;
						next_RXByteStMachCurrState <= 3'd0;
					end
					8'd3: begin
						next_bitStuffError <= 1'b1;
						next_RxDataOut <= RxStatus;
						next_RxCtrlOut <= 2;
						next_RXByteStMachCurrState <= 3'd0;
					end
					8'd2: begin
						if(RXDataByteCnt>10'h2) begin
							next_RxOverflow <= 1'b1;
							next_RxDataOut <= RxStatus;
							next_RxCtrlOut <= 2;
							next_RXByteStMachCurrState <= 3'd0; end
						else begin
							next_RxDataOut <= RxByte;
							next_RxCtrlOut <= 1;
							next_CRCData <= RxByte;
							next_CRC5_8Bit <= 1'b1;
							next_CRC5En <= 1'b1; end
					end
					default: begin
						next_RXByteStMachCurrState <= 3'd0;
					end
				endcase
				next_RxDataOutWEn <= 1'b1;
				NextState_prRxByte <= 4'd10;
			end
			4'd10: begin
				next_CRC5En <= 1'b0;
				next_RxDataOutWEn <= 1'b0;
				NextState_prRxByte <= 4'd2;
				next_processRxByteRdy <= 1'b1;
			end
			4'd13: begin
				if(CRC5UpdateRdy)	NextState_prRxByte <= 4'd9;
			end
			4'd4: begin
				if(RxByte==8'h80)	next_RXByteStMachCurrState <= 3'd2;
				else				next_RXByteStMachCurrState <= 3'd0;
				NextState_prRxByte <= 4'd2;
				next_processRxByteRdy <= 1'b1;
			end
			4'd3: begin
				if(RxCtrl==8'd0)	next_RXByteStMachCurrState <= 3'd1;
				NextState_prRxByte <= 4'd2;
				next_processRxByteRdy <= 1'b1;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_prRxByte <= 4'd1;
		else	CurrState_prRxByte <= NextState_prRxByte;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			RxByte <= 8'h00;
			RxCtrl <= 8'h00;
			RXByteStMachCurrState <= 3'd0;
			CRCError <= 1'b0;
			bitStuffError <= 1'b0;
			RxOverflow <= 1'b0;
			RxTimeOut <= 1'b0;
			NAKRxed <= 1'b0;
			stallRxed <= 1'b0;
			ACKRxed <= 1'b0;
			dataSequence <= 1'b0;
			RXDataByteCnt <= 10'h00;
			RxDataOut <= 8'h00;
			RxCtrlOut <= 8'h00;
			RxDataOutWEn <= 1'b0;
			rstCRC <= 1'b0;
			CRCData <= 8'h00;
			CRC5En <= 1'b0;
			CRC5_8Bit <= 1'b0;
			CRC16En <= 1'b0;
			processRxByteRdy <= 1'b1; end
		else begin
			RxByte <= next_RxByte;
			RxCtrl <= next_RxCtrl;
			RXByteStMachCurrState <= next_RXByteStMachCurrState;
			CRCError <= next_CRCError;
			bitStuffError <= next_bitStuffError;
			RxOverflow <= next_RxOverflow;
			RxTimeOut <= next_RxTimeOut;
			NAKRxed <= next_NAKRxed;
			stallRxed <= next_stallRxed;
			ACKRxed <= next_ACKRxed;
			dataSequence <= next_dataSequence;
			RXDataByteCnt <= next_RXDataByteCnt;
			RxDataOut <= next_RxDataOut;
			RxCtrlOut <= next_RxCtrlOut;
			RxDataOutWEn <= next_RxDataOutWEn;
			rstCRC <= next_rstCRC;
			CRCData <= next_CRCData;
			CRC5En <= next_CRC5En;
			CRC5_8Bit <= next_CRC5_8Bit;
			CRC16En <= next_CRC16En;
			processRxByteRdy <= next_processRxByteRdy; end
	end

endmodule
