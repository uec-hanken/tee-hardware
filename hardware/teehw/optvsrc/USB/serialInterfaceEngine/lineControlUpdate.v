module lineControlUpdate(
	input			fullSpeedPolarity,
	input			fullSpeedBitRate,
	output	[1:0]	JBit,
	output	[1:0]	KBit
);

	assign JBit = {fullSpeedPolarity, ~fullSpeedPolarity};
	assign KBit = {~fullSpeedPolarity, fullSpeedPolarity};

endmodule
