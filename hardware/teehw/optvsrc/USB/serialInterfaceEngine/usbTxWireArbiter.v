module USBTxWireArbiter (
	input				SIETxCtrl,
	input		[1:0]	SIETxData,
	input				SIETxFSRate,
	input				SIETxReq,
	input				SIETxWEn,
	input				USBWireRdyIn,
	input				clk,
	input				prcTxByteCtrl,
	input		[1:0]	prcTxByteData,
	input				prcTxByteFSRate,
	input				prcTxByteReq,
	input				prcTxByteWEn,
	input				rst,
	output	reg			SIETxGnt,
	output		[1:0]	TxBits,
	output				TxCtl,
	output				TxFSRate,
	output				USBWireRdyOut,
	output				USBWireWEn,
	output	reg			prcTxByteGnt
);

	reg		next_SIETxGnt;
	reg		next_prcTxByteGnt;

	// diagram signals declarations
	reg				muxSIENotPTXB, next_muxSIENotPTXB;
	reg		[1:0]	CurrState_txWireArb;
	reg		[1:0]	NextState_txWireArb;

	// Diagram actions (continuous assignments allowed only: assign ...)

	// processTxByte/SIETransmitter mux
	assign USBWireRdyOut = USBWireRdyIn;
	assign USBWireWEn = (muxSIENotPTXB) ? SIETxWEn    : prcTxByteWEn;
	assign TxBits     = (muxSIENotPTXB) ? SIETxData   : prcTxByteData;
	assign TxCtl      = (muxSIENotPTXB) ? SIETxCtrl   : prcTxByteCtrl;
	assign TxFSRate   = (muxSIENotPTXB) ? SIETxFSRate : prcTxByteFSRate;

	//--------------------------------------------------------------------
	// Machine: txWireArb
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_txWireArb = CurrState_txWireArb;
		// Set default values for outputs and signals
		next_prcTxByteGnt = prcTxByteGnt;
		next_muxSIENotPTXB = muxSIENotPTXB;
		next_SIETxGnt = SIETxGnt;
		case(CurrState_txWireArb)
			2'd0:	NextState_txWireArb <= 2'd1;
			2'd1:	if(prcTxByteReq) begin
						NextState_txWireArb <= 2'd2;
						next_prcTxByteGnt <= 1'b1;
						next_muxSIENotPTXB <= 1'b0; end
					else if(SIETxReq) begin
						NextState_txWireArb <= 2'd3;
						next_SIETxGnt <= 1'b1;
						next_muxSIENotPTXB <= 1'b1; end
			2'd2:	if(~prcTxByteReq) begin
						NextState_txWireArb <= 2'd1;
						next_prcTxByteGnt <= 1'b0; end
			2'd3:	if(~SIETxReq) begin
						NextState_txWireArb <= 2'd1;
						next_SIETxGnt <= 1'b0; end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_txWireArb <= 2'd0;
		else	CurrState_txWireArb <= NextState_txWireArb;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	begin
			muxSIENotPTXB <= 1'b0;
			prcTxByteGnt <= 1'b0;
			SIETxGnt <= 1'b0; end
		else begin
			muxSIENotPTXB <= next_muxSIENotPTXB;
			prcTxByteGnt <= next_prcTxByteGnt;
			SIETxGnt <= next_SIETxGnt; end
	end

endmodule

