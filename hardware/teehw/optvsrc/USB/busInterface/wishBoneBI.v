module wishBoneBI (
  address, dataIn, dataOut, writeEn, 
  strobe_i,
  ack_o,
  clk, rst,
  hostControlSel, 
  hostRxFifoSel, hostTxFifoSel,
  slaveControlSel,
  slaveEP0RxFifoSel, slaveEP1RxFifoSel, slaveEP2RxFifoSel, slaveEP3RxFifoSel, 
  slaveEP0TxFifoSel, slaveEP1TxFifoSel, slaveEP2TxFifoSel, slaveEP3TxFifoSel, 
  hostSlaveMuxSel,
  dataFromHostControl,
  dataFromHostRxFifo,
  dataFromHostTxFifo,
  dataFromSlaveControl,
  dataFromEP0RxFifo, dataFromEP1RxFifo, dataFromEP2RxFifo, dataFromEP3RxFifo,
  dataFromEP0TxFifo, dataFromEP1TxFifo, dataFromEP2TxFifo, dataFromEP3TxFifo,
  dataFromHostSlaveMux
   );
input clk;
input rst;
input [7:0] address;
input [7:0] dataIn;
output [7:0] dataOut;
input strobe_i;
output ack_o;
input writeEn;
output hostControlSel;
output hostRxFifoSel;
output hostTxFifoSel;
output slaveControlSel;
output slaveEP0RxFifoSel, slaveEP1RxFifoSel, slaveEP2RxFifoSel, slaveEP3RxFifoSel; 
output slaveEP0TxFifoSel, slaveEP1TxFifoSel, slaveEP2TxFifoSel, slaveEP3TxFifoSel; 
output hostSlaveMuxSel;
input [7:0] dataFromHostControl;
input [7:0] dataFromHostRxFifo;
input [7:0] dataFromHostTxFifo;
input [7:0] dataFromSlaveControl;
input [7:0] dataFromEP0RxFifo, dataFromEP1RxFifo, dataFromEP2RxFifo, dataFromEP3RxFifo;
input [7:0] dataFromEP0TxFifo, dataFromEP1TxFifo, dataFromEP2TxFifo, dataFromEP3TxFifo;
input [7:0] dataFromHostSlaveMux;


wire clk;
wire rst;
wire [7:0] address;
wire [7:0] dataIn;
reg [7:0] dataOut;
wire writeEn;
wire strobe_i;
reg ack_o;
reg hostControlSel;
reg hostRxFifoSel;
reg hostTxFifoSel;
reg slaveControlSel;
reg slaveEP0RxFifoSel, slaveEP1RxFifoSel, slaveEP2RxFifoSel, slaveEP3RxFifoSel; 
reg slaveEP0TxFifoSel, slaveEP1TxFifoSel, slaveEP2TxFifoSel, slaveEP3TxFifoSel; 
reg hostSlaveMuxSel;
wire [7:0] dataFromHostControl;
wire [7:0] dataFromHostRxFifo;
wire [7:0] dataFromHostTxFifo;
wire [7:0] dataFromSlaveControl;
wire [7:0] dataFromEP0RxFifo, dataFromEP1RxFifo, dataFromEP2RxFifo, dataFromEP3RxFifo;
wire [7:0] dataFromEP0TxFifo, dataFromEP1TxFifo, dataFromEP2TxFifo, dataFromEP3TxFifo;
wire [7:0] dataFromHostSlaveMux;

//internal wires and regs
reg ack_delayed;
reg ack_immediate;

//address decode and data mux
always @(address or
  dataFromHostControl or
  dataFromHostRxFifo or
  dataFromHostTxFifo or
  dataFromSlaveControl or
  dataFromEP0RxFifo or 
  dataFromEP1RxFifo or
  dataFromEP2RxFifo or
  dataFromEP3RxFifo or
  dataFromHostSlaveMux or 
  dataFromEP0TxFifo or
  dataFromEP1TxFifo or
  dataFromEP2TxFifo or
  dataFromEP3TxFifo)
begin
  hostControlSel <= 1'b0;
  hostRxFifoSel <= 1'b0;
  hostTxFifoSel <= 1'b0;
  slaveControlSel <= 1'b0;
  slaveEP0RxFifoSel <= 1'b0;
  slaveEP0TxFifoSel <= 1'b0;
  slaveEP1RxFifoSel <= 1'b0;
  slaveEP1TxFifoSel <= 1'b0;
  slaveEP2RxFifoSel <= 1'b0;
  slaveEP2TxFifoSel <= 1'b0;
  slaveEP3RxFifoSel <= 1'b0;
  slaveEP3TxFifoSel <= 1'b0;
  hostSlaveMuxSel <= 1'b0;
  case (address & 8'hf0)
    8'h00 : begin
      hostControlSel <= 1'b1;
      dataOut <= dataFromHostControl;
    end
    8'h10 : begin
      hostControlSel <= 1'b1;
      dataOut <= dataFromHostControl;
    end
    8'h20 : begin
      hostRxFifoSel <= 1'b1;
      dataOut <= dataFromHostRxFifo;
    end
    8'h30 : begin
      hostTxFifoSel <= 1'b1;
      dataOut <= dataFromHostTxFifo;
    end
    8'h40 : begin
      slaveControlSel <= 1'b1;
      dataOut <= dataFromSlaveControl;
    end
    8'h50 : begin
      slaveControlSel <= 1'b1;
      dataOut <= dataFromSlaveControl;
    end
    8'h60 : begin
      slaveEP0RxFifoSel <= 1'b1;
      dataOut <= dataFromEP0RxFifo;
    end
    8'h70 : begin
      slaveEP0TxFifoSel <= 1'b1;
      dataOut <= dataFromEP0TxFifo;
    end
    8'h80 : begin
      slaveEP1RxFifoSel <= 1'b1;
      dataOut <= dataFromEP1RxFifo;
    end
    8'h90 : begin
      slaveEP1TxFifoSel <= 1'b1;
      dataOut <= dataFromEP1TxFifo;
    end
    8'ha0 : begin
      slaveEP2RxFifoSel <= 1'b1;
      dataOut <= dataFromEP2RxFifo;
    end
    8'hb0 : begin
      slaveEP2TxFifoSel <= 1'b1;
      dataOut <= dataFromEP2TxFifo;
    end
    8'hc0 : begin
      slaveEP3RxFifoSel <= 1'b1;
      dataOut <= dataFromEP3RxFifo;
    end
    8'hd0 : begin
      slaveEP3TxFifoSel <= 1'b1;
      dataOut <= dataFromEP3TxFifo;
    end
    8'he0 : begin
      hostSlaveMuxSel <= 1'b1; 
      dataOut <= dataFromHostSlaveMux;
    end
    default: 
      dataOut <= 8'h00;
  endcase
end

//delayed ack
always @(posedge clk) begin
  ack_delayed <= strobe_i;
end

//immediate ack
always @(strobe_i) begin
  ack_immediate <= strobe_i;
end 

//select between immediate and delayed ack
always @(writeEn or address or ack_delayed or ack_immediate) begin
  if (writeEn == 1'b0 &&
      (address == 8'h20 + 3'b000 ||
       address == 8'h30 + 3'b000 ||
       address == 8'h60 + 3'b000 ||
       address == 8'h70 + 3'b000 ||
       address == 8'h80 + 3'b000 ||
       address == 8'h90 + 3'b000 ||
       address == 8'ha0 + 3'b000 ||
       address == 8'hb0 + 3'b000 ||
       address == 8'hc0 + 3'b000 ||
       address == 8'hd0 + 3'b000) )
  begin
    ack_o <= ack_delayed & ack_immediate;
  end
  else
  begin
    ack_o <= ack_immediate;
  end
end

endmodule
