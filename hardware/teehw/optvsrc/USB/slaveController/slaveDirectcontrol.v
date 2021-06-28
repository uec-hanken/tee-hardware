module slaveDirectControl (
	input				SCTxPortGnt,
	input				SCTxPortRdy,
	input				clk,
	input				directControlEn,
	input		[1:0]	directControlLineState,
	input				rst,
	output	reg	[7:0]	SCTxPortCntl,
	output	reg	[7:0]	SCTxPortData,
	output	reg			SCTxPortReq,
	output	reg			SCTxPortWEn
);

	reg		[7:0]	next_SCTxPortCntl;
	reg		[7:0]	next_SCTxPortData;
	reg				next_SCTxPortReq;
	reg				next_SCTxPortWEn;
	reg		[2:0]	CurrState_slvDrctCntl;
	reg		[2:0]	NextState_slvDrctCntl;

	//--------------------------------------------------------------------
	// Machine: slvDrctCntl
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_slvDrctCntl <= CurrState_slvDrctCntl;
		// Set default values for outputs and signals
		next_SCTxPortReq <= SCTxPortReq;
		next_SCTxPortWEn <= SCTxPortWEn;
		next_SCTxPortData <= SCTxPortData;
		next_SCTxPortCntl <= SCTxPortCntl;
		case(CurrState_slvDrctCntl)
			3'd0: begin
				NextState_slvDrctCntl <= 3'd1;
			end
			3'd1: begin
				if(directControlEn)	begin
					NextState_slvDrctCntl <= 3'd2;
					next_SCTxPortReq <= 1'b1; end
				else begin
					NextState_slvDrctCntl <= 3'd6;
					next_SCTxPortReq <= 1'b1; end
			end
			3'd2: begin
				if(SCTxPortGnt)	NextState_slvDrctCntl <= 3'd4;
			end
			3'd3: begin
				next_SCTxPortWEn <= 1'b0;
				if(~directControlEn) begin
					NextState_slvDrctCntl <= 3'd1;
					next_SCTxPortReq <= 1'b0; end
				else	NextState_slvDrctCntl <= 3'd4;
			end
			3'd4: begin
				if(SCTxPortRdy)	begin
					NextState_slvDrctCntl <= 3'd3;
					next_SCTxPortWEn <= 1'b1;
					next_SCTxPortData <= {6'b000000, directControlLineState};
					next_SCTxPortCntl <= 8'h00; end
			end
			3'd5: begin
				next_SCTxPortWEn <= 1'b0;
				next_SCTxPortReq <= 1'b0;
				NextState_slvDrctCntl <= 3'd1;
			end
			3'd6: begin
				if(SCTxPortGnt)	NextState_slvDrctCntl <= 3'd7;
			end
			3'd7: begin
				if(SCTxPortRdy)	begin
					NextState_slvDrctCntl <= 3'd5;
					next_SCTxPortWEn <= 1'b1;
					next_SCTxPortData <= 8'h00;
					next_SCTxPortCntl <= 8'h05; end
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_slvDrctCntl <= 3'd0;
		else	CurrState_slvDrctCntl <= NextState_slvDrctCntl;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	begin
			SCTxPortCntl <= 8'h00;
			SCTxPortData <= 8'h00;
			SCTxPortWEn <= 1'b0;
			SCTxPortReq <= 1'b0; end
		else begin
			SCTxPortCntl <= next_SCTxPortCntl;
			SCTxPortData <= next_SCTxPortData;
			SCTxPortWEn <= next_SCTxPortWEn;
			SCTxPortReq <= next_SCTxPortReq; end
	end

endmodule
