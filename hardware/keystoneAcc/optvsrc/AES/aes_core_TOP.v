module aes_core_TOP (
	input			ICLK,
	input			IRSTN,
	input			IENCDEC,
	input			IINIT,
	input			INEXT,
	output			OREADY,
	input	[255:0]	IKEY,
	input			IKEYLEN,
	input	[127:0]	IBLOCK,
	output	[127:0]	ORESULT,
	output			ORESULT_VALID
);

aes_core U1 (
	.iClk			(ICLK),
	.iRstn			(IRSTN),
	.iEncdec		(IENCDEC),
	.iInit			(IINIT),
	.iNext			(INEXT),
	.oReady			(OREADY),
	.iKey			(IKEY),
	.iKeylen		(IKEYLEN),
	.iBlock			(IBLOCK),
	.oResult		(ORESULT),
	.oResult_valid	(ORESULT_VALID)
);

endmodule
