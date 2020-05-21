module updateCRC5 (
	input			rstCRC,
	input			CRCEn,
	input			CRC5_8BitIn,
	input	[7:0]	dataIn,
	input			clk,
	input			rst,
	output	reg	[4:0]	CRCResult,
	output	reg			ready
);

	reg				doUpdateCRC;
	reg		[7:0]	data;
	reg		[3:0]	loopEnd;
	reg		[3:0]	i;

	always@(posedge clk) begin
		if(rst|rstCRC) begin
			doUpdateCRC <= 1'b0;
			i <= 4'h0;
			CRCResult <= 5'h1f;
			ready <= 1'b1; end
		else if(~doUpdateCRC) begin
			if(CRCEn) begin
				ready <= 1'b0;
				doUpdateCRC <= 1'b1;
				data <= dataIn;
				if(CRC5_8BitIn)	loopEnd <= 4'h7; 
				else			loopEnd <= 4'h2;
			end end
		else begin
			i <= i + 1'b1;
			if(CRCResult[0]^data[0])	CRCResult <= {1'b0, CRCResult[4:1]} ^ 5'h14;
			else						CRCResult <= {1'b0, CRCResult[4:1]};
			data <= {1'b0, data[7:1]};
			if(i==loopEnd) begin
				doUpdateCRC <= 1'b0; 
				i <= 4'h0;
				ready <= 1'b1;
			end
		end
	end

endmodule
