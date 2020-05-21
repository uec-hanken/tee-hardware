module RxfifoBI (
	input		[2:0]	address,
	input				writeEn,
	input				strobe_i,
	input				busClk,
	input				usbClk,
	input				rstSyncToBusClk,
	input		[7:0]	fifoDataIn,
	input		[7:0]	busDataIn,
	output	reg	[7:0]	busDataOut,
	output				fifoREn,
	output				forceEmptySyncToUsbClk,
	output				forceEmptySyncToBusClk,
	input		[15:0]	numElementsInFifo,
	input				fifoSelect
);

	reg				forceEmptyReg;
	reg				forceEmpty;
	reg				forceEmptyToggle;
	reg		[2:0]	forceEmptyToggleSyncToUsbClk;

	//sync write
	always@(posedge busClk) begin
		forceEmpty <= (writeEn & fifoSelect & (address==3'b100) & strobe_i & busDataIn[0]);
	end

	//detect rising edge of 'forceEmpty', and generate toggle signal
	always@(posedge busClk) begin
		if(rstSyncToBusClk) begin
			forceEmptyReg <= 1'b0;
			forceEmptyToggle <= 1'b0; end
		else begin
			if(forceEmpty)	forceEmptyReg <= 1'b1;
			else			forceEmptyReg <= 1'b0;
			if(forceEmpty & ~forceEmptyReg)	forceEmptyToggle <= ~forceEmptyToggle;
		end
	end
	assign forceEmptySyncToBusClk = (forceEmpty & ~forceEmptyReg);

	// double sync across clock domains to generate 'forceEmptySyncToUsbClk'
	always@(posedge usbClk) begin
		forceEmptyToggleSyncToUsbClk <= {forceEmptyToggleSyncToUsbClk[1:0], forceEmptyToggle};
	end
	assign forceEmptySyncToUsbClk = forceEmptyToggleSyncToUsbClk[2] ^ forceEmptyToggleSyncToUsbClk[1];

	// async read mux
	always@(*) begin
		case (address)
			3'b000 : busDataOut <= fifoDataIn;
			3'b010 : busDataOut <= numElementsInFifo[15:8];
			3'b011 : busDataOut <= numElementsInFifo[7:0];
			default: busDataOut <= 8'h00; 
		endcase
	end

	//generate fifo read strobe
	assign fifoREn = (address==3'b000) & ~writeEn & strobe_i & fifoSelect;

endmodule
