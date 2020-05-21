module RxFifo
#(parameter FIFO_DEPTH = 64,
  parameter ADDR_WIDTH = 6)
(	input			busClk,
	input			usbClk,
	input			rstSyncToBusClk,
	input			rstSyncToUsbClk,
	input			fifoWEn,
	output			fifoFull,
	input	[2:0]	busAddress,
	input			busWriteEn,
	input			busStrobe_i,
	input			busFifoSelect,
	input	[7:0]	busDataIn,
	output	[7:0]	busDataOut,
	input	[7:0]	fifoDataIn
);

	wire	[7:0]	dataFromFifoToBus;
	wire			fifoREn;
	wire			forceEmptySyncToBusClk;
	wire			forceEmptySyncToUsbClk;
	wire	[15:0]	numElementsInFifo;
	wire			fifoEmpty;   //not used

	fifoRTL #(8, FIFO_DEPTH, ADDR_WIDTH) u_fifo (
		.wrClk					(usbClk), 
		.rdClk					(busClk), 
		.rstSyncToWrClk			(rstSyncToUsbClk), 
		.rstSyncToRdClk			(rstSyncToBusClk), 
		.dataIn					(fifoDataIn), 
		.dataOut				(dataFromFifoToBus), 
		.fifoWEn				(fifoWEn), 
		.fifoREn				(fifoREn), 
		.fifoFull				(fifoFull), 
		.fifoEmpty				(fifoEmpty), 
		.forceEmptySyncToWrClk	(forceEmptySyncToUsbClk), 
		.forceEmptySyncToRdClk	(forceEmptySyncToBusClk), 
		.numElementsInFifo		(numElementsInFifo)
	);

	RxfifoBI u_RxfifoBI (
		.address				(busAddress), 
		.writeEn				(busWriteEn), 
		.strobe_i				(busStrobe_i),
		.busClk					(busClk), 
		.usbClk					(usbClk), 
		.rstSyncToBusClk		(rstSyncToBusClk), 
		.fifoSelect				(busFifoSelect),
		.fifoDataIn				(dataFromFifoToBus),
		.busDataIn				(busDataIn), 
		.busDataOut				(busDataOut),
		.fifoREn				(fifoREn),
		.forceEmptySyncToBusClk	(forceEmptySyncToBusClk),
		.forceEmptySyncToUsbClk	(forceEmptySyncToUsbClk),
		.numElementsInFifo		(numElementsInFifo)
	);

endmodule
