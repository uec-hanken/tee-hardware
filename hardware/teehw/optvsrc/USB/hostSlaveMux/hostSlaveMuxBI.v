module hostSlaveMuxBI (dataIn, dataOut, address, writeEn, strobe_i, busClk, usbClk,
  hostMode, hostSlaveMuxSel, rstFromWire, rstSyncToBusClkOut, rstSyncToUsbClkOut);

input [7:0] dataIn;
input address;
input writeEn;
input strobe_i;
input busClk;
input usbClk;
output [7:0] dataOut;
input hostSlaveMuxSel;
output hostMode;
input rstFromWire;
output rstSyncToBusClkOut;
output rstSyncToUsbClkOut;

wire [7:0] dataIn;
wire address;
wire writeEn;
wire strobe_i;
wire busClk;
wire usbClk;
reg [7:0] dataOut;
wire hostSlaveMuxSel;
reg hostMode;
wire rstFromWire;
reg rstSyncToBusClkOut;
reg rstSyncToUsbClkOut;

//internal wire and regs
reg [5:0] rstShift;
reg rstFromBus;
reg rstSyncToUsbClkFirst;

//sync write demux
always @(posedge busClk)
begin
  if (rstSyncToBusClkOut == 1'b1)
    hostMode <= 1'b0;
  else begin
    if (writeEn == 1'b1 && hostSlaveMuxSel == 1'b1 && strobe_i == 1'b1 && address == 1'b0 )
      hostMode <= dataIn[0];
    end
    if (writeEn == 1'b1 && hostSlaveMuxSel == 1'b1 && strobe_i == 1'b1 && address == 1'b0 && dataIn[1] == 1'b1 )
      rstFromBus <= 1'b1;
    else
      rstFromBus <= 1'b0;
end

// async read mux
always @(address or hostMode)
begin
  case (address)
    1'b0: dataOut <= {7'h0, hostMode};
    1'b1: dataOut <= 8'h22;
  endcase
end

// reset control
//generate 'rstSyncToBusClk'
//assuming that 'busClk' < 5 * 'usbClk'. ie 'busClk' < 240MHz
always @(posedge busClk) begin
  if (rstFromWire == 1'b1 || rstFromBus == 1'b1) 
    rstShift <= 6'b111111;
  else
    rstShift <= {1'b0, rstShift[5:1]};
end

always @(rstShift)
  rstSyncToBusClkOut <= rstShift[0];

// double sync across clock domains to generate 'forceEmptySyncToWrClk'
always @(posedge usbClk) begin
    rstSyncToUsbClkFirst <= rstSyncToBusClkOut;
    rstSyncToUsbClkOut <= rstSyncToUsbClkFirst;
end

endmodule
