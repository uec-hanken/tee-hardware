module speedCtrlMux (
	input	directCtrlRate,
	input	directCtrlPol,
	input	sendPacketRate,
	input	sendPacketPol,
	input	sendPacketSel,
	output	fullSpeedRate,
	output	fullSpeedPol
);

	assign fullSpeedRate = (sendPacketSel) ? sendPacketRate : directCtrlRate;
	assign fullSpeedPol  = (sendPacketSel) ? sendPacketPol  : directCtrlPol;

endmodule
