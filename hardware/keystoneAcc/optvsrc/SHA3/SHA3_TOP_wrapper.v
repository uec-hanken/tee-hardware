module SHA3_TOP_wrapper (
	input			clk,
	input			reset,
	input	[63:0]	in,
	input			in_ready,
	input			is_last,
	input	[2:0]	byte_num,
	output			buffer_full,
	output	[511:0]	out,
	output			out_ready
);

SHA3_TOP U1_TOP (
	.ICLK			(clk),
	.IRST			(reset),
	.IDATA			(in),
	.IREADY			(in_ready),
	.ILAST			(is_last),
	.IBYTE_NUM		(byte_num),
	.OBUFFER_FULL	(buffer_full),
	.ODATA			(out),
	.OREADY			(out_ready)
);

endmodule
