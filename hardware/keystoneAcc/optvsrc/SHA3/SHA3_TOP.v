module SHA3_TOP (
	input			ICLK,
	input			IRST,
	input	[63:0]	IDATA,
	input			IREADY,
	input			ILAST,
	input	[2:0]	IBYTE_NUM,
	output			OBUFFER_FULL,
	output	[511:0]	ODATA,
	output			OREADY
);

keccak uut (
	.iClk			(ICLK),
	.iRst			(IRST),
	.iData			(IDATA),
	.iReady			(IREADY),
	.iLast			(ILAST),
	.iByte_num		(IBYTE_NUM),
	.oBuffer_full	(OBUFFER_FULL),
	.oData			(ODATA),
	.oReady			(OREADY)
);

endmodule
