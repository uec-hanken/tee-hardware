module SOFController (
	input				HCTxPortGnt,
	input				HCTxPortRdy,
	input				SOFEnable,
	input				SOFTimerClr,
	input				clk,
	input				rst,
	output	reg	[7:0]	HCTxPortCntl,
	output	reg	[7:0]	HCTxPortData,
	output	reg			HCTxPortReq,
	output	reg			HCTxPortWEn,
	output	reg	[15:0]	SOFTimer
);

	reg		[7:0]	next_HCTxPortCntl;
	reg		[7:0]	next_HCTxPortData;
	reg				next_HCTxPortReq;
	reg				next_HCTxPortWEn;
	reg		[15:0]	next_SOFTimer;

	reg		[2:0]	CurrState_sofCntl;
	reg		[2:0]	NextState_sofCntl;

	//--------------------------------------------------------------------
	// Machine: sofCntl
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_sofCntl <= CurrState_sofCntl;
		// Set default values for outputs and signals
		next_HCTxPortReq <= HCTxPortReq;
		next_HCTxPortWEn <= HCTxPortWEn;
		next_HCTxPortData <= HCTxPortData;
		next_HCTxPortCntl <= HCTxPortCntl;
		next_SOFTimer <= SOFTimer;
		case(CurrState_sofCntl)
			3'd0: begin
				NextState_sofCntl <= 3'd1;
			end
			3'd1: begin
				if(SOFEnable) begin
					NextState_sofCntl <= 3'd4;
					next_HCTxPortReq <= 1'b1; end
			end
			3'd2: begin
				if(HCTxPortRdy)	begin
					NextState_sofCntl <= 3'd5;
					next_HCTxPortWEn <= 1'b1;
					next_HCTxPortData <= 8'h00;
					next_HCTxPortCntl <= 8'h01; end
			end
			3'd3: begin
				next_HCTxPortReq <= 1'b0;
				if(SOFTimerClr)	next_SOFTimer <= 16'h0000;
				else			next_SOFTimer <= SOFTimer + 1'b1;
				if(~SOFEnable) begin
					NextState_sofCntl <= 3'd1;
					next_SOFTimer <= 16'h0000; end
			end
			3'd4: begin
				if(HCTxPortGnt)	NextState_sofCntl <= 3'd2;
			end
			3'd5: begin
				next_HCTxPortWEn <= 1'b0;
				NextState_sofCntl <= 3'd3;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_sofCntl <= 3'd0;
		else	CurrState_sofCntl <= NextState_sofCntl;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	begin
			SOFTimer <= 16'h0000;
			HCTxPortCntl <= 8'h00;
			HCTxPortData <= 8'h00;
			HCTxPortWEn <= 1'b0;
			HCTxPortReq <= 1'b0; end
		else begin
			SOFTimer <= next_SOFTimer;
			HCTxPortCntl <= next_HCTxPortCntl;
			HCTxPortData <= next_HCTxPortData;
			HCTxPortWEn <= next_HCTxPortWEn;
			HCTxPortReq <= next_HCTxPortReq; end
	end

endmodule
