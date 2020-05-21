module hostSlaveMuxBI (
	input		[7:0]	dataIn,
	input				address,
	input				writeEn,
	input				strobe_i,
	input				busClk,
	input				usbClk,
	output		[7:0]	dataOut,
	input				hostSlaveMuxSel,
	output	reg			hostMode,
	input				rstFromWire,
	output				rstSyncToBusClkOut,
	output	reg			rstSyncToUsbClkOut
);

	reg		[5:0]	rstShift;
	reg				rstFromBus;
	reg				rstSyncToUsbClkFirst;

	//sync write demux
	always@(posedge busClk) begin
		if(rstSyncToBusClkOut)	hostMode <= 1'b0;
		else if(writeEn&hostSlaveMuxSel&strobe_i&~address)
								hostMode <= dataIn[0];
		else					hostMode <= hostMode;
		if(writeEn&hostSlaveMuxSel&strobe_i&~address&dataIn[1])
				rstFromBus <= 1'b1;
		else	rstFromBus <= 1'b0;
	end

	// async read mux
	assign dataOut = (address) ? 8'h22 : {7'h0, hostMode};

	// reset control
	//generate 'rstSyncToBusClk'
	//assuming that 'busClk' < 5 * 'usbClk'. ie 'busClk' < 240MHz
	always@(posedge busClk) begin
		if(rstFromWire|rstFromBus)	rstShift <= 6'b111111;
		else						rstShift <= {1'b0, rstShift[5:1]};
	end

	assign rstSyncToBusClkOut = rstShift[0];

	// double sync across clock domains to generate 'forceEmptySyncToWrClk'
	always@(posedge usbClk) begin
		rstSyncToUsbClkFirst <= rstSyncToBusClkOut;
		rstSyncToUsbClkOut <= rstSyncToUsbClkFirst;
	end

endmodule
