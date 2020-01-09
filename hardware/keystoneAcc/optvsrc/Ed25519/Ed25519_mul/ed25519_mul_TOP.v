module ed25519_mul_TOP (
	input			ICLK,
	input			IRSTN,
	
	input			IEN,
	output			OREADY,
	
	output	[2:0]	OKADDR,
	output	[2:0]	OQYADDR,
	output			OQYWREN,
	input	[31:0]	IK,
	output	[31:0]	OQY
);

 ed25519_mul U1 (
	.iClk		(ICLK),
	.iRstn		(IRSTN),
	
	.iEn		(IEN),
	.oReady		(OREADY),
	
	.oK_addr	(OKADDR),
	.oQy_addr	(OQYADDR),
	.oQy_wren	(OQYWREN),
	.iK			(IK),
	.oQy		(OQY)
 );

endmodule
