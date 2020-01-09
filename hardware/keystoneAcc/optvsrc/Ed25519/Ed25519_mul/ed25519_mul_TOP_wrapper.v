module ed25519_mul_TOP_wrapper (
	input			clk,
	input			rst_n,
	
	input			ena,
	output			rdy,
	
	output	[2:0]	k_addr,
	output	[2:0]	qy_addr,
	output			qy_wren,
	input	[31:0]	k_din,
	output	[31:0]	qy_dout
);

 ed25519_mul_TOP U1_TOP (
	.ICLK		(clk),
	.IRSTN		(rst_n),
	 
	.IEN		(ena),
	.OREADY		(rdy),
	 
	.OKADDR		(k_addr),
	.OQYADDR	(qy_addr),
	.OQYWREN	(qy_wren),
	.IK			(k_din),
	.OQY		(qy_dout)
 );

endmodule
