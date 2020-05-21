module SIETransmitter (
	input		[15:0]	CRC16Result,
	input				CRC16UpdateRdy,
	input		[4:0]	CRC5Result,
	input				CRC5UpdateRdy,
	input		[1:0]	JBit,
	input		[1:0]	KBit,
	input		[7:0]	SIEPortCtrlIn,
	input		[7:0]	SIEPortDataIn,
	input				SIEPortWEn,
	input				USBWireGnt,
	input				USBWireRdy,
	input				clk,
	input				fullSpeedRateIn,
	input				processTxByteRdy,
	input				rst,
	output	reg			CRC16En,
	output	reg			CRC5En,
	output	reg			CRC5_8Bit,
	output	reg	[7:0]	CRCData,
	output	reg			SIEPortTxRdy,
	output	reg	[7:0]	TxByteOutCtrl,
	output	reg			TxByteOutFullSpeedRate,
	output	reg	[7:0]	TxByteOut,
	output	reg			USBWireCtrl,
	output	reg	[1:0]	USBWireData,
	output	reg			USBWireFullSpeedRate,
	output	reg			USBWireReq,
	output	reg			USBWireWEn,
	output	reg			processTxByteWEn,
	output	reg			rstCRC
);

	reg				next_CRC16En;
	reg				next_CRC5En;
	reg				next_CRC5_8Bit;
	reg		[7:0]	next_CRCData;
	reg				next_SIEPortTxRdy;
	reg		[7:0]	next_TxByteOutCtrl;
	reg				next_TxByteOutFullSpeedRate;
	reg		[7:0]	next_TxByteOut;
	reg				next_USBWireCtrl;
	reg		[1:0]	next_USBWireData;
	reg				next_USBWireFullSpeedRate;
	reg				next_USBWireReq;
	reg				next_USBWireWEn;
	reg				next_processTxByteWEn;
	reg				next_rstCRC;

	reg		[7:0]	SIEPortCtrl, next_SIEPortCtrl;
	reg		[7:0]	SIEPortData, next_SIEPortData;
	reg		[2:0]	i, next_i;
	reg		[15:0]	resumeCnt, next_resumeCnt;

	reg		[5:0]	CurrState_SIETx;
	reg		[5:0]	NextState_SIETx;

	//--------------------------------------------------------------------
	// Machine: SIETx
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_SIETx <= CurrState_SIETx;
		// Set default values for outputs and signals
		next_processTxByteWEn <= processTxByteWEn;
		next_TxByteOut <= TxByteOut;
		next_TxByteOutCtrl <= TxByteOutCtrl;
		next_USBWireData <= USBWireData;
		next_USBWireCtrl <= USBWireCtrl;
		next_USBWireReq <= USBWireReq;
		next_USBWireWEn <= USBWireWEn;
		next_rstCRC <= rstCRC;
		next_CRCData <= CRCData;
		next_CRC5En <= CRC5En;
		next_CRC5_8Bit <= CRC5_8Bit;
		next_CRC16En <= CRC16En;
		next_SIEPortTxRdy <= SIEPortTxRdy;
		next_SIEPortData <= SIEPortData;
		next_SIEPortCtrl <= SIEPortCtrl;
		next_i <= i;
		next_resumeCnt <= resumeCnt;
		next_TxByteOutFullSpeedRate <= TxByteOutFullSpeedRate;
		next_USBWireFullSpeedRate <= USBWireFullSpeedRate;
		case(CurrState_SIETx)
			6'd4: begin
				NextState_SIETx <= 6'd20;
			end
			6'd18: begin
				next_processTxByteWEn <= 1'b0;
				next_TxByteOut <= 8'h00;
				next_TxByteOutCtrl <= 8'h00;
				next_USBWireData <= 2'b00;
				next_USBWireCtrl <= 1'b0;
				next_USBWireReq <= 1'b0;
				next_USBWireWEn <= 1'b0;
				next_rstCRC <= 1'b0;
				next_CRCData <= 8'h00;
				next_CRC5En <= 1'b0;
				next_CRC5_8Bit <= 1'b0;
				next_CRC16En <= 1'b0;
				next_SIEPortTxRdy <= 1'b0;
				next_SIEPortData <= 8'h00;
				next_SIEPortCtrl <= 8'h00;
				next_i <= 3'h0;
				next_resumeCnt <= 16'h0000;
				next_TxByteOutFullSpeedRate <= 1'b0;
				next_USBWireFullSpeedRate <= 1'b0;
				NextState_SIETx <= 6'd20;
			end
			6'd19: begin
				if((SIEPortCtrl==8'h02)&((SIEPortData[3:0]==4'h5)|(SIEPortData[3:0]==4'hc))) begin
					NextState_SIETx <= 6'd40;
					next_TxByteOutFullSpeedRate <= 1'b1; end
					//SOF and PRE always at full speed
				else if(SIEPortCtrl==8'h02)	NextState_SIETx <= 6'd40;
				else if(SIEPortCtrl==8'h06) begin
					NextState_SIETx <= 6'd45;
					next_USBWireReq <= 1'b1; end
				else if(SIEPortCtrl==8'h00) begin
					NextState_SIETx <= 6'd27;
					next_USBWireReq <= 1'b1; end
				else if(SIEPortCtrl==8'h05)	NextState_SIETx <= 6'd4;
				else if(SIEPortCtrl==8'h01) begin
					NextState_SIETx <= 6'd28;
					next_USBWireReq <= 1'b1;
					next_resumeCnt <= 16'h0000;
					next_USBWireFullSpeedRate <= 1'b0; end
					//resume always uses low speed timing
			end
			6'd20: begin
				next_SIEPortTxRdy <= 1'b1;
				if(SIEPortWEn) begin
					NextState_SIETx <= 6'd19;
					next_SIEPortData <= SIEPortDataIn;
					next_SIEPortCtrl <= SIEPortCtrlIn;
					next_SIEPortTxRdy <= 1'b0;
					next_TxByteOutFullSpeedRate <= fullSpeedRateIn;
					next_USBWireFullSpeedRate <= fullSpeedRateIn; end
			end
			6'd0: begin
				next_USBWireWEn <= 1'b0;
				next_i <= i + 1'b1;
				if(i==3'h7) begin
					NextState_SIETx <= 6'd20;
					next_USBWireReq <= 1'b0; end
				else	NextState_SIETx <= 6'd56;
			end
			6'd27: begin
				next_i <= 3'h0;
				if(USBWireGnt)	NextState_SIETx <= 6'd30;
			end
			6'd30: begin
				if(USBWireRdy) begin
					NextState_SIETx <= 6'd0;
					next_USBWireData <= SIEPortData[1:0];
					next_USBWireCtrl <= 1'b1;
					next_USBWireWEn <= 1'b1; end
			end
			6'd56: begin
				NextState_SIETx <= 6'd30;
			end
			6'd2: begin
				next_processTxByteWEn <= 1'b0;
				if(SIEPortData[1:0]==2'b01)			NextState_SIETx <= 6'd33;
				else if(SIEPortData[1:0]==2'b10)	NextState_SIETx <= 6'd29;
				else if(SIEPortData[1:0]==2'b11)	NextState_SIETx <= 6'd37;
				else if(SIEPortData[1:0]==2'b00)	NextState_SIETx <= 6'd31;
			end
			6'd40: begin
				if(processTxByteRdy) begin
					NextState_SIETx <= 6'd2;
					next_processTxByteWEn <= 1'b1;
					next_TxByteOut <= 8'h80;
					next_TxByteOutCtrl <= 8'd0; end
			end
			6'd11: begin
				next_processTxByteWEn <= 1'b0;
				NextState_SIETx <= 6'd39;
			end
			6'd13: begin
				next_processTxByteWEn <= 1'b0;
				NextState_SIETx <= 6'd20;
			end
			6'd38: begin
				if(processTxByteRdy) begin
					NextState_SIETx <= 6'd11;
					next_processTxByteWEn <= 1'b1;
					next_TxByteOut <= ~CRC16Result[7:0];
					next_TxByteOutCtrl <= 8'd2; end
			end
			6'd39: begin
				if(processTxByteRdy) begin
					NextState_SIETx <= 6'd13;
					next_processTxByteWEn <= 1'b1;
					next_TxByteOut <= ~CRC16Result[15:8];
					next_TxByteOutCtrl <= 8'd1; end
			end
			6'd3: begin
				if(SIEPortCtrl==8'h04)	NextState_SIETx <= 6'd38;
				else					NextState_SIETx <= 6'd43;
			end
			6'd5: begin
				next_processTxByteWEn <= 1'b0;
				NextState_SIETx <= 6'd26;
			end
			6'd23: begin
				next_CRCData <= SIEPortData;
				next_CRC16En <= 1'b1;
				NextState_SIETx <= 6'd34;
			end
			6'd26: begin
				next_SIEPortTxRdy <= 1'b1;
				if(SIEPortWEn) begin
					NextState_SIETx <= 6'd3;
					next_SIEPortData <= SIEPortDataIn;
					next_SIEPortCtrl <= SIEPortCtrlIn;
					next_SIEPortTxRdy <= 1'b0; end
			end
			6'd34: begin
				next_CRC16En <= 1'b0;
				if(processTxByteRdy) begin
					NextState_SIETx <= 6'd5;
					next_processTxByteWEn <= 1'b1;
					next_TxByteOut <= SIEPortData;
					next_TxByteOutCtrl <= 8'd2; end
			end
			6'd43: begin
				if(CRC16UpdateRdy)	NextState_SIETx <= 6'd23;
			end
			6'd6: begin
				next_processTxByteWEn <= 1'b0;
				next_rstCRC <= 1'b0;
				NextState_SIETx <= 6'd26;
			end
			6'd37: begin
				if(processTxByteRdy) begin
					NextState_SIETx <= 6'd6;
					next_processTxByteWEn <= 1'b1;
					next_TxByteOut <= SIEPortData;
					next_TxByteOutCtrl <= 8'd2;
					next_rstCRC <= 1'b1; end
			end
			6'd7: begin
				next_processTxByteWEn <= 1'b0;
				NextState_SIETx <= 6'd20;
			end
			6'd29: begin
				if(processTxByteRdy) begin
					NextState_SIETx <= 6'd7;
					next_processTxByteWEn <= 1'b1;
					next_TxByteOut <= SIEPortData;
					next_TxByteOutCtrl <= 8'd1; end
			end
			6'd10: begin
				next_processTxByteWEn <= 1'b0;
				NextState_SIETx <= 6'd20;
			end
			6'd31: begin
				if(processTxByteRdy) begin
					NextState_SIETx <= 6'd10;
					next_processTxByteWEn <= 1'b1;
					next_TxByteOut <= SIEPortData;
					if(SIEPortData[3:0]==4'hc)	next_TxByteOutCtrl <= 8'd4;
					else						next_TxByteOutCtrl <= 8'd1; end
			end
			6'd12: begin
				next_processTxByteWEn <= 1'b0;
				NextState_SIETx <= 6'd24;
			end
			6'd22: begin
				next_CRCData <= SIEPortData;
				next_CRC5_8Bit <= 1'b1;
				next_CRC5En <= 1'b1;
				NextState_SIETx <= 6'd36;
			end
			6'd25: begin
				next_SIEPortTxRdy <= 1'b1;
				if(SIEPortWEn) begin
					NextState_SIETx <= 6'd44;
					next_SIEPortData <= SIEPortDataIn;
					next_SIEPortCtrl <= SIEPortCtrlIn;
					next_SIEPortTxRdy <= 1'b0; end
			end
			6'd36: begin
				next_CRC5En <= 1'b0;
				if(processTxByteRdy) begin
					NextState_SIETx <= 6'd12;
					next_processTxByteWEn <= 1'b1;
					next_TxByteOut <= SIEPortData;
					next_TxByteOutCtrl <= 8'd2; end
			end
			6'd44: begin
				if(CRC5UpdateRdy)	NextState_SIETx <= 6'd22;
			end
			6'd8: begin
				next_processTxByteWEn <= 1'b0;
				NextState_SIETx <= 6'd20;
			end
			6'd21: begin
				next_CRCData <= SIEPortData;
				next_CRC5_8Bit <= 1'b0;
				next_CRC5En <= 1'b1;
				NextState_SIETx <= 6'd32;
			end
			6'd24: begin
				next_SIEPortTxRdy <= 1'b1;
				if(SIEPortWEn) begin
					NextState_SIETx <= 6'd42;
					next_SIEPortData <= SIEPortDataIn;
					next_SIEPortCtrl <= SIEPortCtrlIn;
					next_SIEPortTxRdy <= 1'b0; end
			end
			6'd32: begin
				next_CRC5En <= 1'b0;
				if(processTxByteRdy) begin
					NextState_SIETx <= 6'd8;
					next_processTxByteWEn <= 1'b1;
					next_TxByteOut <= {~CRC5Result, SIEPortData[2:0] };
					next_TxByteOutCtrl <= 8'd1; end
			end
			6'd42: begin
				if(CRC5UpdateRdy)	NextState_SIETx <= 6'd21;
			end
			6'd9: begin
				next_processTxByteWEn <= 1'b0;
				next_rstCRC <= 1'b0;
				NextState_SIETx <= 6'd25;
			end
			6'd33: begin
				if(processTxByteRdy) begin
					NextState_SIETx <= 6'd9;
					next_processTxByteWEn <= 1'b1;
					next_TxByteOut <= SIEPortData;
					next_TxByteOutCtrl <= 8'd2;
					next_rstCRC <= 1'b1; end
			end
			6'd1: begin
				next_USBWireWEn <= 1'b0;
				if(resumeCnt==16'd30000)	NextState_SIETx <= 6'd41;
				else						NextState_SIETx <= 6'd52;
			end
			6'd14: begin
				next_USBWireWEn <= 1'b0;
				NextState_SIETx <= 6'd55;
			end
			6'd15: begin
				next_USBWireWEn <= 1'b0;
				next_USBWireReq <= 1'b0;
				NextState_SIETx <= 6'd20;
				next_USBWireFullSpeedRate <= fullSpeedRateIn;
			end
			6'd16: begin
				next_USBWireWEn <= 1'b0;
				NextState_SIETx <= 6'd53;
			end
			6'd17: begin
				next_USBWireWEn <= 1'b0;
				NextState_SIETx <= 6'd54;
			end
			6'd28: begin
				if(USBWireGnt)	NextState_SIETx <= 6'd35;
			end
			6'd35: begin
				if(USBWireRdy) begin
					NextState_SIETx <= 6'd1;
					next_USBWireData <= KBit;
					next_USBWireCtrl <= 1'b1;
					next_USBWireWEn <= 1'b1;
					next_resumeCnt <= resumeCnt  + 1'b1; end
			end
			6'd41: begin
				if(USBWireRdy) begin
					NextState_SIETx <= 6'd16;
					next_USBWireData <= 2'b00;
					next_USBWireCtrl <= 1'b1;
					next_USBWireWEn <= 1'b1; end
			end
			6'd52: begin
				NextState_SIETx <= 6'd35;
			end
			6'd53: begin
				if(USBWireRdy) begin
					NextState_SIETx <= 6'd17;
					next_USBWireData <= 2'b00;
					next_USBWireCtrl <= 1'b1;
					next_USBWireWEn <= 1'b1; end
			end
			6'd54: begin
				if(USBWireRdy) begin
					NextState_SIETx <= 6'd14;
					next_USBWireData <= JBit;
					next_USBWireCtrl <= 1'b1;
					next_USBWireWEn <= 1'b1; end
			end
			6'd55: begin
				if(USBWireRdy) begin
					NextState_SIETx <= 6'd15;
					next_USBWireData <= JBit;
					next_USBWireCtrl <= 1'b0;
					next_USBWireWEn <= 1'b1; end
			end
			6'd45: begin
				if(USBWireGnt)	NextState_SIETx <= 6'd48;
			end
			6'd46: begin
				next_USBWireWEn <= 1'b0;
				NextState_SIETx <= 6'd51;
			end
			6'd47: begin
				next_USBWireWEn <= 1'b0;
				NextState_SIETx <= 6'd50;
			end
			6'd48: begin
				if(USBWireRdy) begin
					NextState_SIETx <= 6'd47;
					next_USBWireData <= 2'b00;
					next_USBWireCtrl <= 1'b1;
					next_USBWireWEn <= 1'b1; end
			end
			6'd49: begin
				next_USBWireWEn <= 1'b0;
				next_USBWireReq <= 1'b0;
				NextState_SIETx <= 6'd20;
			end
			6'd50: begin
				if(USBWireRdy) begin
					NextState_SIETx <= 6'd46;
					next_USBWireData <= 2'b00;
					next_USBWireCtrl <= 1'b1;
					next_USBWireWEn <= 1'b1; end
			end
			6'd51: begin
				if(USBWireRdy) begin
					NextState_SIETx <= 6'd49;
					next_USBWireData <= JBit;
					next_USBWireCtrl <= 1'b1;
					next_USBWireWEn <= 1'b1; end
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_SIETx <= 6'd18;
		else	CurrState_SIETx <= NextState_SIETx;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			SIEPortData <= 8'h00;
			SIEPortCtrl <= 8'h00;
			i <= 3'h0;
			resumeCnt <= 16'h0000;
			processTxByteWEn <= 1'b0;
			TxByteOut <= 8'h00;
			TxByteOutCtrl <= 8'h00;
			USBWireData <= 2'b00;
			USBWireCtrl <= 1'b0;
			USBWireReq <= 1'b0;
			USBWireWEn <= 1'b0;
			rstCRC <= 1'b0;
			CRCData <= 8'h00;
			CRC5En <= 1'b0;
			CRC5_8Bit <= 1'b0;
			CRC16En <= 1'b0;
			SIEPortTxRdy <= 1'b0;
			TxByteOutFullSpeedRate <= 1'b0;
			USBWireFullSpeedRate <= 1'b0; end
		else begin
			SIEPortData <= next_SIEPortData;
			SIEPortCtrl <= next_SIEPortCtrl;
			i <= next_i;
			resumeCnt <= next_resumeCnt;
			processTxByteWEn <= next_processTxByteWEn;
			TxByteOut <= next_TxByteOut;
			TxByteOutCtrl <= next_TxByteOutCtrl;
			USBWireData <= next_USBWireData;
			USBWireCtrl <= next_USBWireCtrl;
			USBWireReq <= next_USBWireReq;
			USBWireWEn <= next_USBWireWEn;
			rstCRC <= next_rstCRC;
			CRCData <= next_CRCData;
			CRC5En <= next_CRC5En;
			CRC5_8Bit <= next_CRC5_8Bit;
			CRC16En <= next_CRC16En;
			SIEPortTxRdy <= next_SIEPortTxRdy;
			TxByteOutFullSpeedRate <= next_TxByteOutFullSpeedRate;
			USBWireFullSpeedRate <= next_USBWireFullSpeedRate; end
	end

endmodule
