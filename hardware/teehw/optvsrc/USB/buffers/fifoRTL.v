module fifoRTL
#(parameter FIFO_WIDTH = 8,
  parameter FIFO_DEPTH = 64,
  parameter ADDR_WIDTH = 6)
(	// Two clock domains within this module
	// These ports are within 'wrClk' domain
	input							wrClk,
	input							rstSyncToWrClk,
	input		[FIFO_WIDTH-1:0]	dataIn,
	input							fifoWEn,
	input							forceEmptySyncToWrClk,
	output	reg						fifoFull,
	
	// These ports are within 'rdClk' domain
	input							rdClk,
	input							rstSyncToRdClk,
	output	reg	[FIFO_WIDTH-1:0]	dataOut,
	input							fifoREn,
	input							forceEmptySyncToRdClk,
	output	reg						fifoEmpty,
	output	reg	[15:0]				numElementsInFifo //note that this implies a max fifo depth of 65536
);

	reg		[ADDR_WIDTH:0]		bufferInIndex; 
	reg		[ADDR_WIDTH:0]		bufferInIndexSyncToRdClk;
	reg		[ADDR_WIDTH:0]		bufferOutIndex;
	reg		[ADDR_WIDTH:0]		bufferOutIndexSyncToWrClk;
	wire	[ADDR_WIDTH-1:0]	bufferInIndexToMem;
	wire	[ADDR_WIDTH-1:0]	bufferOutIndexToMem;
	wire	[ADDR_WIDTH:0]		bufferCnt;
	reg							fifoREnDelayed;
	wire	[FIFO_WIDTH-1:0]	dataFromMem;

	always@(posedge wrClk) begin
		bufferOutIndexSyncToWrClk <= bufferOutIndex;
		if(rstSyncToWrClk|forceEmptySyncToWrClk) begin
			fifoFull <= 1'b0;
			bufferInIndex <= 0; end
		else begin
			if(fifoWEn)	bufferInIndex <= bufferInIndex + 1'b1;
			if( (bufferOutIndexSyncToWrClk[ADDR_WIDTH-1:0] == bufferInIndex[ADDR_WIDTH-1:0]) &
				(bufferOutIndexSyncToWrClk[ADDR_WIDTH]     != bufferInIndex[ADDR_WIDTH]    ) )
				fifoFull <= 1'b1;
			else	fifoFull <= 1'b0;
		end
	end

	assign bufferCnt = bufferInIndexSyncToRdClk - bufferOutIndex;

	always@(posedge rdClk) begin
		numElementsInFifo <= { {16-ADDR_WIDTH+1{1'b0}}, bufferCnt }; //pad bufferCnt with leading zeroes
		bufferInIndexSyncToRdClk <= bufferInIndex;
		if(rstSyncToRdClk|forceEmptySyncToRdClk) begin
			fifoEmpty <= 1'b1;
			bufferOutIndex <= 0;
			fifoREnDelayed <= 1'b0; end
		else begin
			fifoREnDelayed <= fifoREn;
			if(fifoREn&~fifoREnDelayed) begin
				dataOut <= dataFromMem;
				bufferOutIndex <= bufferOutIndex + 1'b1; end
			if(bufferInIndexSyncToRdClk==bufferOutIndex) 
					fifoEmpty <= 1'b1;
			else	fifoEmpty <= 1'b0;
		end
	end

	assign bufferInIndexToMem  = bufferInIndex[ADDR_WIDTH-1:0];
	assign bufferOutIndexToMem = bufferOutIndex[ADDR_WIDTH-1:0];

	dpMem_dc #(FIFO_WIDTH, FIFO_DEPTH, ADDR_WIDTH) u_dpMem_dc (
		.addrIn		(bufferInIndexToMem),
		.addrOut	(bufferOutIndexToMem),
		.wrClk		(wrClk),
		.rdClk		(rdClk),
		.dataIn		(dataIn),
		.writeEn	(fifoWEn),
		.readEn		(fifoREn),
		.dataOut	(dataFromMem)
	);

endmodule
