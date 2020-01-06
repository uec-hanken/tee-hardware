module ed25519_sign_S_core_TOP_wrapper (
	input			clk,
	input			rst,
	input			core_ena,
	output			core_ready,
	output			core_comp_done,
	input	[511:0]	hashd_key,
	input	[511:0]	hashd_ram,
	input	[511:0]	hashd_sm,
	output	[255:0]	core_S
);

wire [252:0] sign;

ed25519_sign_S_core_TOP U1_TOP (
	.ICLK			(clk),
	.IRST			(rst),
	.IEN			(core_ena),
	.OREADY			(core_ready),
	.ODONE			(core_comp_done),
	.IHASHD_KEY		({hashd_key[511:507], hashd_key[503:264], hashd_key[261:256]}),
	.IHASHD_RAM		(hashd_ram),
	.IHASHD_SM		(hashd_sm),
	.OSIGN			(sign)
);
assign core_S = {Sign[252:5], 3'b0, Sign[4:0]};
 
endmodule
