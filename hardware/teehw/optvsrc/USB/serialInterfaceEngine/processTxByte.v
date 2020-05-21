module processTxByte (
	input		[1:0]	JBit,
	input		[1:0]	KBit,
	input		[7:0]	TxByteCtrlIn,
	input				TxByteFullSpeedRateIn,
	input		[7:0]	TxByteIn,
	input				USBWireGnt,
	input				USBWireRdy,
	input				clk,
	input				processTxByteWEn,
	input				rst,
	output	reg			USBWireCtrl,
	output	reg	[1:0]	USBWireData,
	output	reg			USBWireFullSpeedRate,
	output	reg			USBWireReq,
	output	reg			USBWireWEn,
	output	reg			processTxByteRdy
);

	reg				next_USBWireCtrl;
	reg		[1:0]	next_USBWireData;
	reg				next_USBWireFullSpeedRate;
	reg				next_USBWireReq;
	reg				next_USBWireWEn;
	reg				next_processTxByteRdy;

	reg		[1:0]	TXLineState, next_TXLineState;
	reg		[3:0]	TXOneCount, next_TXOneCount;
	reg		[7:0]	TxByteCtrl, next_TxByteCtrl;
	reg				TxByteFullSpeedRate, next_TxByteFullSpeedRate;
	reg		[7:0]	TxByte, next_TxByte;
	reg		[3:0]	i, next_i;

	reg		[4:0]	CurrState_prcTxB;
	reg		[4:0]	NextState_prcTxB;

	//--------------------------------------------------------------------
	// Machine: prcTxB
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_prcTxB <= CurrState_prcTxB;
		// Set default values for outputs and signals
		next_processTxByteRdy <= processTxByteRdy;
		next_USBWireData <= USBWireData;
		next_USBWireCtrl <= USBWireCtrl;
		next_USBWireReq <= USBWireReq;
		next_USBWireWEn <= USBWireWEn;
		next_i <= i;
		next_TxByte <= TxByte;
		next_TxByteCtrl <= TxByteCtrl;
		next_TXLineState <= TXLineState;
		next_TXOneCount <= TXOneCount;
		next_USBWireFullSpeedRate <= USBWireFullSpeedRate;
		next_TxByteFullSpeedRate <= TxByteFullSpeedRate;
		case(CurrState_prcTxB)
			5'd0: begin
				next_processTxByteRdy <= 1'b0;
				next_USBWireData <= 2'b00;
				next_USBWireCtrl <= 1'b0;
				next_USBWireReq <= 1'b0;
				next_USBWireWEn <= 1'b0;
				next_i <= 4'h0;
				next_TxByte <= 8'h00;
				next_TxByteCtrl <= 8'h00;
				next_TXLineState <= 2'b0;
				next_TXOneCount <= 4'h0;
				next_USBWireFullSpeedRate <= 1'b0;
				next_TxByteFullSpeedRate <= 1'b0;
				NextState_prcTxB <= 5'd1;
			end
			5'd1: begin
				next_processTxByteRdy <= 1'b1;
				if(processTxByteWEn&(TxByteCtrlIn==8'd0)) begin
					NextState_prcTxB <= 5'd8;
					next_processTxByteRdy <= 1'b0;
					next_TxByte <= TxByteIn;
					next_TxByteCtrl <= TxByteCtrlIn;
					next_TxByteFullSpeedRate <= TxByteFullSpeedRateIn;
					next_USBWireFullSpeedRate <= TxByteFullSpeedRateIn;
					next_TXOneCount <= 4'h0;
					next_TXLineState <= JBit;
					next_USBWireReq <= 1'b1; end
				else if(processTxByteWEn) begin
					NextState_prcTxB <= 5'd2;
					next_processTxByteRdy <= 1'b0;
					next_TxByte <= TxByteIn;
					next_TxByteCtrl <= TxByteCtrlIn;
					next_TxByteFullSpeedRate <= TxByteFullSpeedRateIn;
					next_USBWireFullSpeedRate <= TxByteFullSpeedRateIn;
					next_i <= 4'h0; end
			end
			5'd8: begin
				if(USBWireGnt)	NextState_prcTxB <= 5'd15;
			end
			5'd15: begin
				if(USBWireRdy&~TxByteFullSpeedRate)	NextState_prcTxB <= 5'd19;
				else if(USBWireRdy) begin
					NextState_prcTxB <= 5'd16;
					//actively drive the first J bit
					next_USBWireData <= JBit;
					next_USBWireCtrl <= 1'b1;
					next_USBWireWEn <= 1'b1; end
			end
			5'd16: begin
				next_USBWireWEn <= 1'b0;
				NextState_prcTxB <= 5'd2;
				next_i <= 4'h0;
			end
			5'd2: begin
				next_i <= i + 1'b1;
				next_TxByte <= {1'b0, TxByte[7:1] };
				if(TxByte[0])	next_TXOneCount <= TXOneCount + 1'b1;	//If this bit is 1, then increment 'TXOneCount'
				else begin	//else this is a zero bit
					next_TXOneCount <= 4'h0;	//reset 'TXOneCount'
					if(TXLineState==JBit)	next_TXLineState <= KBit;	//toggle the line state
					else	next_TXLineState <= JBit; end
				NextState_prcTxB <= 5'd3;
			end
			5'd3: begin
				if(USBWireRdy) begin
					NextState_prcTxB <= 5'd4;
					next_USBWireWEn <= 1'b1;
					next_USBWireData <= TXLineState;
					next_USBWireCtrl <= 1'b1; end
			end
			5'd4: begin
				next_USBWireWEn <= 1'b0;
				if(TXOneCount==4'h6)	NextState_prcTxB <= 5'd5;
				else if (i!=4'h8)		NextState_prcTxB <= 5'd2;
				else					NextState_prcTxB <= 5'd11;
			end
			5'd5: begin
				next_TXOneCount <= 4'h0;	//reset 'TXOneCount'
				if(TXLineState==JBit)	next_TXLineState <= KBit;
				else					next_TXLineState <= JBit;	//toggle the line state
				NextState_prcTxB <= 5'd6;
			end
			5'd6: begin
				if(USBWireRdy) begin
					NextState_prcTxB <= 5'd7;
					next_USBWireWEn <= 1'b1;
					next_USBWireData <= TXLineState;
					next_USBWireCtrl <= 1'b1; end
			end
			5'd7: begin
				next_USBWireWEn <= 1'b0;
				if(i==4'h8)	NextState_prcTxB <= 5'd11;
				else			NextState_prcTxB <= 5'd2;
			end
			5'd9: begin
				next_USBWireWEn <= 1'b0;
				NextState_prcTxB <= 5'd26;
			end
			5'd10: begin
				NextState_prcTxB <= 5'd25;
			end
			5'd11: begin
				if(TxByteCtrl==8'd1)		NextState_prcTxB <= 5'd10;
				else if(TxByteCtrl==8'd4)	NextState_prcTxB <= 5'd12;
				else						NextState_prcTxB <= 5'd1;
			end
			5'd12: begin
				next_USBWireWEn <= 1'b0;
				NextState_prcTxB <= 5'd27;
			end
			5'd13: begin
				next_USBWireWEn <= 1'b0;
				NextState_prcTxB <= 5'd28;
			end
			5'd14: begin
				next_USBWireWEn <= 1'b0;
				next_USBWireReq <= 1'b0;
				//release the wire
				NextState_prcTxB <= 5'd1;
			end
			5'd25: begin
				if(USBWireRdy) begin
					NextState_prcTxB <= 5'd9;
					next_USBWireWEn <= 1'b1;
					next_USBWireData <= 2'b00;
					next_USBWireCtrl <= 1'b1; end
			end
			5'd26: begin
				if(USBWireRdy) begin
					NextState_prcTxB <= 5'd12;
					next_USBWireWEn <= 1'b1;
					next_USBWireData <= 2'b00;
					next_USBWireCtrl <= 1'b1; end
			end
			5'd27: begin
				if(USBWireRdy) begin
					NextState_prcTxB <= 5'd13;
					next_USBWireWEn <= 1'b1;
					next_USBWireData <= JBit;
					next_USBWireCtrl <= 1'b1; end
			end
			5'd28: begin
				if(USBWireRdy) begin
					NextState_prcTxB <= 5'd14;
					next_USBWireWEn <= 1'b1;
					next_USBWireData <= JBit;
					next_USBWireCtrl <= 1'b0; end
			end
			5'd17: begin
				next_USBWireWEn <= 1'b0;
				NextState_prcTxB <= 5'd23;
			end
			5'd18: begin
				next_USBWireWEn <= 1'b0;
				NextState_prcTxB <= 5'd24;
				end
			5'd19: begin
				if(USBWireRdy) begin
					NextState_prcTxB <= 5'd20;
					next_USBWireWEn <= 1'b1;
					next_USBWireData <= JBit;
					next_USBWireCtrl <= 1'b0; end
			end
			5'd20: begin
				next_USBWireWEn <= 1'b0;
				NextState_prcTxB <= 5'd22;
			end
			5'd21: begin
				next_USBWireWEn <= 1'b0;
				NextState_prcTxB <= 5'd2;
				next_i <= 4'h0;
			end
			5'd22: begin
				if(USBWireRdy) begin
					NextState_prcTxB <= 5'd17;
					next_USBWireWEn <= 1'b1;
					next_USBWireData <= JBit;
					next_USBWireCtrl <= 1'b0; end
			end
			5'd23: begin
				if(USBWireRdy) begin
					NextState_prcTxB <= 5'd18;
					next_USBWireWEn <= 1'b1;
					next_USBWireData <= JBit;
					next_USBWireCtrl <= 1'b0; end
			end
			5'd24: begin
				if(USBWireRdy) begin
					NextState_prcTxB <= 5'd21;
					//Drive the first JBit
					next_USBWireWEn <= 1'b1;
					next_USBWireData <= JBit;
					next_USBWireCtrl <= 1'b1; end
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_prcTxB <= 5'd0;
		else	CurrState_prcTxB <= NextState_prcTxB;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	begin
			i <= 4'h0;
			TxByte <= 8'h00;
			TxByteCtrl <= 8'h00;
			TXLineState <= 2'b0;
			TXOneCount <= 4'h0;
			TxByteFullSpeedRate <= 1'b0;
			processTxByteRdy <= 1'b0;
			USBWireData <= 2'b00;
			USBWireCtrl <= 1'b0;
			USBWireReq <= 1'b0;
			USBWireWEn <= 1'b0;
			USBWireFullSpeedRate <= 1'b0; end
		else begin
			i <= next_i;
			TxByte <= next_TxByte;
			TxByteCtrl <= next_TxByteCtrl;
			TXLineState <= next_TXLineState;
			TXOneCount <= next_TXOneCount;
			TxByteFullSpeedRate <= next_TxByteFullSpeedRate;
			processTxByteRdy <= next_processTxByteRdy;
			USBWireData <= next_USBWireData;
			USBWireCtrl <= next_USBWireCtrl;
			USBWireReq <= next_USBWireReq;
			USBWireWEn <= next_USBWireWEn;
			USBWireFullSpeedRate <= next_USBWireFullSpeedRate; end
	end

endmodule
