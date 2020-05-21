module TxfifoBI (
	input	[2:0]	address,
	input			writeEn,
	input			strobe_i,
	input			busClk,
	input			usbClk,
	input			rstSyncToBusClk,
	input	[7:0]	busDataIn,
	output	[7:0]	busDataOut,
	output			fifoWEn,
	output			forceEmptySyncToUsbClk,
	output			forceEmptySyncToBusClk,
	input	[15:0]	numElementsInFifo,
	input			fifoSelect
);

	reg				forceEmptyReg;
	reg				forceEmpty;
	reg				forceEmptyToggle;
	reg		[2:0]	forceEmptyToggleSyncToUsbClk;

	//sync write
	always @(posedge busClk) begin
		forceEmpty <= (writeEn & fifoSelect & (address == 3'b100) & strobe_i & busDataIn[0]);
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
	assign forceEmptySyncToBusClk = forceEmpty & ~forceEmptyReg;

	// double sync across clock domains to generate 'forceEmptySyncToUsbClk'
	always@(posedge usbClk) begin
		forceEmptyToggleSyncToUsbClk <= {forceEmptyToggleSyncToUsbClk[1:0], forceEmptyToggle};
	end
	assign forceEmptySyncToUsbClk = forceEmptyToggleSyncToUsbClk[2] ^ forceEmptyToggleSyncToUsbClk[1];

	// async read mux
	assign busDataOut = 8'h00;
	//always @(address or fifoFull or numElementsInFifo)
	//begin
	//  case (address)
	//      3'b001 : busDataOut <= {7'b0000000, fifoFull};
	//      3'b010 : busDataOut <= numElementsInFifo[15:8];
	//      3'b011 : busDataOut <= numElementsInFifo[7:0];
	//      default: busDataOut <= 8'h00;
	//  endcase
	//end

	//generate fifo write strobe
	assign fifoWEn = (address==3'b000) & writeEn & strobe_i & fifoSelect;

endmodule
