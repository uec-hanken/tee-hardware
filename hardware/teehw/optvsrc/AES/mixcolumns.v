module mixw (
	input	[31:0]	w,
	output	[31:0]	out
);

assign out[31] = w[30] ^ w[23] ^ w[22] ^ w[15] ^ w[7];
assign out[30] = w[29] ^ w[21] ^ w[22] ^ w[14] ^ w[6];
assign out[29] = w[28] ^ w[20] ^ w[21] ^ w[13] ^ w[5];
assign out[28] = w[31] ^ w[27] ^ w[23] ^ w[19] ^ w[20] ^ w[12] ^ w[4];
assign out[27] = w[31] ^ w[26] ^ w[23] ^ w[18] ^ w[19] ^ w[11] ^ w[3];
assign out[26] = w[25] ^ w[17] ^ w[18] ^ w[10] ^ w[2];
assign out[25] = w[31] ^ w[24] ^ w[23] ^ w[16] ^ w[17] ^ w[9]  ^ w[1];
assign out[24] = w[31] ^ w[23] ^ w[16] ^ w[8]  ^ w[0];
assign out[23] = w[31] ^ w[22] ^ w[14] ^ w[15] ^ w[7];
assign out[22] = w[30] ^ w[21] ^ w[13] ^ w[14] ^ w[6];
assign out[21] = w[29] ^ w[20] ^ w[12] ^ w[13] ^ w[5];
assign out[20] = w[28] ^ w[23] ^ w[19] ^ w[15] ^ w[11] ^ w[12] ^ w[4];
assign out[19] = w[27] ^ w[23] ^ w[18] ^ w[15] ^ w[10] ^ w[11] ^ w[3];
assign out[18] = w[26] ^ w[17] ^ w[9]  ^ w[10] ^ w[2];
assign out[17] = w[25] ^ w[23] ^ w[16] ^ w[15] ^ w[8]  ^ w[9]  ^ w[1];
assign out[16] = w[24] ^ w[23] ^ w[15] ^ w[8]  ^ w[0];
assign out[15] = w[31] ^ w[23] ^ w[14] ^ w[6]  ^ w[7];
assign out[14] = w[30] ^ w[22] ^ w[13] ^ w[5]  ^ w[6];
assign out[13] = w[29] ^ w[21] ^ w[12] ^ w[4]  ^ w[5];
assign out[12] = w[28] ^ w[20] ^ w[15] ^ w[11] ^ w[7]  ^ w[3]  ^ w[4];
assign out[11] = w[27] ^ w[19] ^ w[15] ^ w[10] ^ w[7]  ^ w[2]  ^ w[3];
assign out[10] = w[26] ^ w[18] ^ w[9]  ^ w[1]  ^ w[2];
assign out[9]  = w[25] ^ w[17] ^ w[15] ^ w[8]  ^ w[7]  ^ w[0]  ^ w[1];
assign out[8]  = w[24] ^ w[16] ^ w[15] ^ w[7]  ^ w[0];
assign out[7]  = w[30] ^ w[31] ^ w[23] ^ w[15] ^ w[6];
assign out[6]  = w[29] ^ w[30] ^ w[22] ^ w[14] ^ w[5];
assign out[5]  = w[28] ^ w[29] ^ w[21] ^ w[13] ^ w[4];
assign out[4]  = w[31] ^ w[27] ^ w[28] ^ w[20] ^ w[12] ^ w[7]  ^ w[3];
assign out[3]  = w[31] ^ w[26] ^ w[27] ^ w[19] ^ w[11] ^ w[7]  ^ w[2];
assign out[2]  = w[25] ^ w[26] ^ w[18] ^ w[10] ^ w[1];
assign out[1]  = w[31] ^ w[24] ^ w[25] ^ w[17] ^ w[9]  ^ w[7]  ^ w[0];
assign out[0]  = w[31] ^ w[24] ^ w[16] ^ w[8]  ^ w[7];

endmodule

module mixcolumns (
	input	[127:0]	data,
	output	[127:0]	out
);

mixw u3 (
	.w		(data[127:96]),
	.out	(out[127:96])
);

mixw u2 (
	.w		(data[95:64]),
	.out	(out[95:64])
);

mixw u1 (
	.w		(data[63:32]),
	.out	(out[63:32])
);

mixw u0 (
	.w		(data[31:0]),
	.out	(out[31:0])
);

endmodule
