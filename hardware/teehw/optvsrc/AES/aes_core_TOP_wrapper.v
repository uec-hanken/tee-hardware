module aes_core_TOP_wrapper (
	input			clk,
	input			reset_n,
	input			encdec,
	input			init,
	input			next,
	output			ready,
	input	[255:0]	key,
	input			keylen,
	input	[127:0]	block,
	output	[127:0]	result,
	output			result_valid
);

aes_core_TOP U1_TOP (
	.ICLK			(clk),
	.IRSTN			(reset_n),
	.IENCDEC		(encdec),
	.IINIT			(init),
	.INEXT			(next),
	.OREADY			(ready),
	.IKEY			(key),
	.IKEYLEN		(keylen),
	.IBLOCK			(block),
	.ORESULT		(result),
	.ORESULT_VALID	(result_valid)
);

endmodule
