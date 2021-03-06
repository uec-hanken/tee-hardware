module hostSlaveMux (
	output	[7:0]	SIEPortCtrlInToSIE,
	input	[7:0]	SIEPortCtrlInFromHost,
	input	[7:0]	SIEPortCtrlInFromSlave,
	output	[7:0]	SIEPortDataInToSIE,
	input	[7:0]	SIEPortDataInFromHost,
	input	[7:0]	SIEPortDataInFromSlave,
	output			SIEPortWEnToSIE,
	input			SIEPortWEnFromHost,
	input			SIEPortWEnFromSlave,
	output			fullSpeedPolarityToSIE,
	input			fullSpeedPolarityFromHost,
	input			fullSpeedPolarityFromSlave,
	output			fullSpeedBitRateToSIE,
	input			fullSpeedBitRateFromHost,
	input			fullSpeedBitRateFromSlave,
	output			noActivityTimeOutEnableToSIE,
	input			noActivityTimeOutEnableFromHost,
	input			noActivityTimeOutEnableFromSlave,
	
	//hostSlaveMuxBI
	input	[7:0]	dataIn,
	input			address,
	input			writeEn,
	input			strobe_i,
	input			busClk,
	input			usbClk,
	input			rstFromWire,
	output			rstSyncToBusClkOut,
	output			rstSyncToUsbClkOut,
	output	[7:0]	dataOut,
	input			hostSlaveMuxSel
);

	wire	hostMode;

	assign SIEPortCtrlInToSIE			= (hostMode) ? SIEPortCtrlInFromHost			: SIEPortCtrlInFromSlave;
	assign SIEPortDataInToSIE			= (hostMode) ? SIEPortDataInFromHost			: SIEPortDataInFromSlave;
	assign SIEPortWEnToSIE				= (hostMode) ? SIEPortWEnFromHost				: SIEPortWEnFromSlave;
	assign fullSpeedPolarityToSIE		= (hostMode) ? fullSpeedPolarityFromHost		: fullSpeedPolarityFromSlave;
	assign fullSpeedBitRateToSIE		= (hostMode) ? fullSpeedBitRateFromHost			: fullSpeedBitRateFromSlave;
	assign noActivityTimeOutEnableToSIE	= (hostMode) ? noActivityTimeOutEnableFromHost	: noActivityTimeOutEnableFromSlave;

	hostSlaveMuxBI u_hostSlaveMuxBI (
		.dataIn					(dataIn), 
		.dataOut				(dataOut),
		.address				(address),
		.writeEn				(writeEn), 
		.strobe_i				(strobe_i),
		.busClk					(busClk), 
		.usbClk					(usbClk), 
		.hostMode				(hostMode), 
		.hostSlaveMuxSel		(hostSlaveMuxSel),  
		.rstFromWire			(rstFromWire),
		.rstSyncToBusClkOut		(rstSyncToBusClkOut),
		.rstSyncToUsbClkOut		(rstSyncToUsbClkOut)
	);

endmodule
