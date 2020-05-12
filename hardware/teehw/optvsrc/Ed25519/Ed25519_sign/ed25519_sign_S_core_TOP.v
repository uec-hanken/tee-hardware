module ed25519_sign_S_core_TOP (
	input			ICLK,
	input			IRST,
	input			IEN,
	output			OREADY,
	output			ODONE,
	input	[250:0]	IHASHD_KEY,
	input	[511:0]	IHASHD_RAM,
	input	[511:0]	IHASHD_SM,
	output	[252:0]	OSIGN
);

ed25519_sign_S_core uut (
	.iClk			(ICLK),
	.iRst			(IRST),
	.iEn			(IEN),
	.oReady			(OREADY),
	.oDone			(ODONE),
	.iHashd_key		(IHASHD_KEY),
	.iHashd_ram		(IHASHD_RAM),
	.iHashd_sm		(IHASHD_SM),
	.oSign			(OSIGN)
);
 
endmodule
