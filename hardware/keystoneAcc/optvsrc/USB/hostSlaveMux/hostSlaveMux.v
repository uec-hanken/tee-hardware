module hostSlaveMux (
  SIEPortCtrlInToSIE,
  SIEPortCtrlInFromHost,
  SIEPortCtrlInFromSlave,
  SIEPortDataInToSIE, 
  SIEPortDataInFromHost, 
  SIEPortDataInFromSlave, 
  SIEPortWEnToSIE, 
  SIEPortWEnFromHost, 
  SIEPortWEnFromSlave, 
  fullSpeedPolarityToSIE,
  fullSpeedPolarityFromHost,
  fullSpeedPolarityFromSlave,
  fullSpeedBitRateToSIE,
  fullSpeedBitRateFromHost,
  fullSpeedBitRateFromSlave,
  noActivityTimeOutEnableToSIE,
  noActivityTimeOutEnableFromHost,
  noActivityTimeOutEnableFromSlave,
  dataIn, 
  dataOut,
  address,
  writeEn,
  strobe_i,
  busClk, 
  usbClk, 
  hostSlaveMuxSel,
  rstFromWire,
  rstSyncToBusClkOut,
  rstSyncToUsbClkOut
);


output [7:0] SIEPortCtrlInToSIE;
input [7:0] SIEPortCtrlInFromHost;
input [7:0] SIEPortCtrlInFromSlave;
output [7:0] SIEPortDataInToSIE; 
input [7:0] SIEPortDataInFromHost; 
input [7:0] SIEPortDataInFromSlave; 
output SIEPortWEnToSIE; 
input SIEPortWEnFromHost; 
input SIEPortWEnFromSlave; 
output fullSpeedPolarityToSIE;
input fullSpeedPolarityFromHost;
input fullSpeedPolarityFromSlave;
output fullSpeedBitRateToSIE;
input fullSpeedBitRateFromHost;
input fullSpeedBitRateFromSlave;
output noActivityTimeOutEnableToSIE;
input noActivityTimeOutEnableFromHost;
input noActivityTimeOutEnableFromSlave;
//hostSlaveMuxBI
input [7:0] dataIn;
input address;
input writeEn;
input strobe_i;
input busClk;
input usbClk;
input rstFromWire;
output rstSyncToBusClkOut;
output rstSyncToUsbClkOut;
output [7:0] dataOut;
input hostSlaveMuxSel;

reg [7:0] SIEPortCtrlInToSIE;
wire [7:0] SIEPortCtrlInFromHost;
wire [7:0] SIEPortCtrlInFromSlave;
reg [7:0] SIEPortDataInToSIE; 
wire [7:0] SIEPortDataInFromHost; 
wire [7:0] SIEPortDataInFromSlave; 
reg SIEPortWEnToSIE; 
wire SIEPortWEnFromHost; 
wire SIEPortWEnFromSlave; 
reg fullSpeedPolarityToSIE;
wire fullSpeedPolarityFromHost;
wire fullSpeedPolarityFromSlave;
reg fullSpeedBitRateToSIE;
wire fullSpeedBitRateFromHost;
wire fullSpeedBitRateFromSlave;
reg noActivityTimeOutEnableToSIE;
wire noActivityTimeOutEnableFromHost;
wire noActivityTimeOutEnableFromSlave;
//hostSlaveMuxBI
wire [7:0] dataIn;
wire address;
wire writeEn;
wire strobe_i;
wire busClk;
wire usbClk;
wire rstSyncToBusClkOut;
wire rstSyncToUsbClkOut;
wire rstFromWire;
wire [7:0] dataOut;
wire hostSlaveMuxSel;

//internal wires and regs
wire hostMode;

always @(hostMode or
  SIEPortCtrlInFromHost or
  SIEPortCtrlInFromSlave or
  SIEPortDataInFromHost or 
  SIEPortDataInFromSlave or 
  SIEPortWEnFromHost or 
  SIEPortWEnFromSlave or 
  fullSpeedPolarityFromHost or
  fullSpeedPolarityFromSlave or
  fullSpeedBitRateFromHost or
  fullSpeedBitRateFromSlave or
  noActivityTimeOutEnableFromHost or
  noActivityTimeOutEnableFromSlave)
begin
  if (hostMode == 1'b1) 
  begin
    SIEPortCtrlInToSIE <= SIEPortCtrlInFromHost;
    SIEPortDataInToSIE <=  SIEPortDataInFromHost;
    SIEPortWEnToSIE <= SIEPortWEnFromHost;
    fullSpeedPolarityToSIE <= fullSpeedPolarityFromHost;
    fullSpeedBitRateToSIE <= fullSpeedBitRateFromHost;
    noActivityTimeOutEnableToSIE <= noActivityTimeOutEnableFromHost;
  end
  else
  begin
    SIEPortCtrlInToSIE <= SIEPortCtrlInFromSlave;
    SIEPortDataInToSIE <=  SIEPortDataInFromSlave;
    SIEPortWEnToSIE <= SIEPortWEnFromSlave;
    fullSpeedPolarityToSIE <= fullSpeedPolarityFromSlave;
    fullSpeedBitRateToSIE <= fullSpeedBitRateFromSlave;
    noActivityTimeOutEnableToSIE <= noActivityTimeOutEnableFromSlave;
  end
end      

hostSlaveMuxBI u_hostSlaveMuxBI (
  .dataIn(dataIn), 
  .dataOut(dataOut),
  .address(address),
  .writeEn(writeEn), 
  .strobe_i(strobe_i),
  .busClk(busClk), 
  .usbClk(usbClk), 
  .hostMode(hostMode), 
  .hostSlaveMuxSel(hostSlaveMuxSel),  
  .rstFromWire(rstFromWire),
  .rstSyncToBusClkOut(rstSyncToBusClkOut),
  .rstSyncToUsbClkOut(rstSyncToUsbClkOut) );


endmodule
