module dpMem_dc(  addrIn, addrOut, wrClk, rdClk, dataIn, writeEn, readEn, dataOut);
  //FIFO_DEPTH = ADDR_WIDTH^2
  parameter FIFO_WIDTH = 8;
  parameter FIFO_DEPTH = 64; 
  parameter ADDR_WIDTH = 6;   
  
input wrClk;
input rdClk;
input [FIFO_WIDTH-1:0] dataIn;
output [FIFO_WIDTH-1:0] dataOut;
input writeEn;
input readEn;
input [ADDR_WIDTH-1:0] addrIn;
input [ADDR_WIDTH-1:0] addrOut;

wire wrClk;
wire rdClk;
wire [FIFO_WIDTH-1:0] dataIn;
reg [FIFO_WIDTH-1:0] dataOut;
wire writeEn;
wire readEn;
wire [ADDR_WIDTH-1:0] addrIn;
wire [ADDR_WIDTH-1:0] addrOut;

reg [FIFO_WIDTH-1:0] buffer [0:FIFO_DEPTH-1];

// synchronous read. Introduces one clock cycle delay
always @(posedge rdClk) begin
  dataOut <= buffer[addrOut];
end

// synchronous write
always @(posedge wrClk) begin
  if (writeEn == 1'b1)
    buffer[addrIn] <= dataIn;
end                  


endmodule
