module lineControlUpdate(fullSpeedPolarity, fullSpeedBitRate, JBit, KBit);
input fullSpeedPolarity;
input fullSpeedBitRate;
output [1:0] JBit;
output [1:0] KBit;

wire fullSpeedPolarity;
wire fullSpeedBitRate;
reg [1:0] JBit;
reg [1:0] KBit;



always @(fullSpeedPolarity)
begin
    if (fullSpeedPolarity == 1'b1)
  begin
      JBit = 2'b10;
      KBit = 2'b01;
    end
    else
  begin
      JBit = 2'b01;
      KBit = 2'b10;
    end
end


endmodule
