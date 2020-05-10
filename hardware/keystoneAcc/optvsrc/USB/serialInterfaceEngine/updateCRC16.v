module updateCRC16 (rstCRC, CRCResult, CRCEn, dataIn, ready, clk, rst);
input   rstCRC;
input   CRCEn;
input   [7:0] dataIn;
input   clk;
input   rst;
output  [15:0] CRCResult;
output ready;

wire   rstCRC;
wire   CRCEn;
wire   [7:0] dataIn;
wire   clk;
wire   rst;
reg    [15:0] CRCResult;
reg    ready;

reg doUpdateCRC;
reg [7:0] data;
reg [3:0] i;

always @(posedge clk)
begin
  if (rst == 1'b1 || rstCRC == 1'b1) begin
    doUpdateCRC <= 1'b0;
    i <= 4'h0;
    CRCResult <= 16'hffff;
    ready <= 1'b1;
  end
  else
  begin
    if (doUpdateCRC == 1'b0)
    begin
      if (CRCEn == 1'b1) begin
        doUpdateCRC <= 1'b1;
        data <= dataIn;
        ready <= 1'b0;
    end
    end
    else begin
      i <= i + 1'b1;
      if ( (CRCResult[0] ^ data[0]) == 1'b1) begin
        CRCResult <= {1'b0, CRCResult[15:1]} ^ 16'ha001;
      end
      else begin
        CRCResult <= {1'b0, CRCResult[15:1]};
      end
      data <= {1'b0, data[7:1]};
      if (i == 4'h7)
      begin
        doUpdateCRC <= 1'b0; 
        i <= 4'h0;
        ready <= 1'b1;
      end
    end
  end
end
    

endmodule
