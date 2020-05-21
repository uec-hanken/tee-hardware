module directControl (
	input				HCTxPortGnt,
	input				HCTxPortRdy,
	input				clk,
	input				directControlEn,
	input		[1:0]	directControlLineState,
	input				rst,
	output	reg	[7:0]	HCTxPortCntl,
	output	reg	[7:0]	HCTxPortData,
	output	reg			HCTxPortReq,
	output	reg			HCTxPortWEn
);

	reg		[7:0]	next_HCTxPortCntl;
	reg		[7:0]	next_HCTxPortData;
	reg				next_HCTxPortReq;
	reg				next_HCTxPortWEn;

	reg		[2:0]	CurrState_drctCntl;
	reg		[2:0]	NextState_drctCntl;

	//--------------------------------------------------------------------
	// Machine: drctCntl
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_drctCntl <= CurrState_drctCntl;
		// Set default values for outputs and signals
		next_HCTxPortReq <= HCTxPortReq;
		next_HCTxPortWEn <= HCTxPortWEn;
		next_HCTxPortData <= HCTxPortData;
		next_HCTxPortCntl <= HCTxPortCntl;
		case(CurrState_drctCntl)
			3'd0: begin
				NextState_drctCntl <= 3'd1;
			end
			3'd1: begin
				if(directControlEn) begin
					NextState_drctCntl <= 3'd2;
					next_HCTxPortReq <= 1'b1; end
				else begin
					NextState_drctCntl <= 3'd6;
					next_HCTxPortReq <= 1'b1; end
			end
			3'd2: begin
				if(HCTxPortGnt)	NextState_drctCntl <= 3'd4;
			end
			3'd3: begin
				next_HCTxPortWEn <= 1'b0;
				if(~directControlEn) begin
						NextState_drctCntl <= 3'd1;
						next_HCTxPortReq <= 1'b0; end
				else	NextState_drctCntl <= 3'd4;
			end
			3'd4: begin
				if(HCTxPortRdy) begin
					NextState_drctCntl <= 3'd3;
					next_HCTxPortWEn <= 1'b1;
					next_HCTxPortData <= {6'b000000, directControlLineState};
					next_HCTxPortCntl <= 8'h00; end
			end
			3'd5: begin
				next_HCTxPortWEn <= 1'b0;
				next_HCTxPortReq <= 1'b0;
				NextState_drctCntl <= 3'd1;
			end
			3'd6: begin
				if(HCTxPortGnt)	NextState_drctCntl <= 3'd7;
			end
			3'd7: begin
				if(HCTxPortRdy) begin
					NextState_drctCntl <= 3'd5;
					next_HCTxPortWEn <= 1'b1;
					next_HCTxPortData <= 8'h00;
					next_HCTxPortCntl <= 8'h05; end
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_drctCntl <= 3'd0;
		else	CurrState_drctCntl <= NextState_drctCntl;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			HCTxPortCntl <= 8'h00;
			HCTxPortData <= 8'h00;
			HCTxPortWEn <= 1'b0;
			HCTxPortReq <= 1'b0; end
		else begin
			HCTxPortCntl <= next_HCTxPortCntl;
			HCTxPortData <= next_HCTxPortData;
			HCTxPortWEn <= next_HCTxPortWEn;
			HCTxPortReq <= next_HCTxPortReq; end
	end

endmodule
