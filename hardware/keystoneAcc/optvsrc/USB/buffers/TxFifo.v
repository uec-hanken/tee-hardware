module TxFifo(
  busClk,
  usbClk,
  rstSyncToBusClk, 
  rstSyncToUsbClk, 
  fifoREn, 
  fifoEmpty,
  busAddress, 
  busWriteEn, 
  busStrobe_i,
  busFifoSelect,
  busDataIn,
  busDataOut,
  fifoDataOut ); 
  //FIFO_DEPTH = ADDR_WIDTH^2
  parameter FIFO_DEPTH = 64; 
  parameter ADDR_WIDTH = 6;   
  
input busClk; 
input usbClk; 
input rstSyncToBusClk; 
input rstSyncToUsbClk; 
input fifoREn; 
output fifoEmpty;
input [2:0] busAddress; 
input busWriteEn; 
input busStrobe_i;
input busFifoSelect;
input [7:0] busDataIn; 
output [7:0] busDataOut; 
output [7:0] fifoDataOut;

wire busClk; 
wire usbClk; 
wire rstSyncToBusClk; 
wire rstSyncToUsbClk; 
wire fifoREn; 
wire fifoEmpty;
wire [2:0] busAddress; 
wire busWriteEn; 
wire busStrobe_i;
wire busFifoSelect;
wire [7:0] busDataIn; 
wire [7:0] busDataOut; 
wire [7:0] fifoDataOut;

//internal wires and regs
wire fifoWEn;
wire forceEmptySyncToUsbClk;
wire forceEmptySyncToBusClk;
wire [15:0] numElementsInFifo;
wire fifoFull;

fifoRTL #(8, FIFO_DEPTH, ADDR_WIDTH) u_fifo(
  .wrClk(busClk), 
  .rdClk(usbClk), 
  .rstSyncToWrClk(rstSyncToBusClk), 
  .rstSyncToRdClk(rstSyncToUsbClk), 
  .dataIn(busDataIn), 
  .dataOut(fifoDataOut), 
  .fifoWEn(fifoWEn), 
  .fifoREn(fifoREn), 
  .fifoFull(fifoFull), 
  .fifoEmpty(fifoEmpty), 
  .forceEmptySyncToWrClk(forceEmptySyncToBusClk), 
  .forceEmptySyncToRdClk(forceEmptySyncToUsbClk), 
  .numElementsInFifo(numElementsInFifo) );
  
TxfifoBI u_TxfifoBI(
  .address(busAddress), 
  .writeEn(busWriteEn), 
  .strobe_i(busStrobe_i),
  .busClk(busClk), 
  .usbClk(usbClk), 
  .rstSyncToBusClk(rstSyncToBusClk), 
  .fifoSelect(busFifoSelect),
  .busDataIn(busDataIn), 
  .busDataOut(busDataOut), 
  .fifoWEn(fifoWEn),
  .forceEmptySyncToBusClk(forceEmptySyncToBusClk),
  .forceEmptySyncToUsbClk(forceEmptySyncToUsbClk),
  .numElementsInFifo(numElementsInFifo)
  );

endmodule
