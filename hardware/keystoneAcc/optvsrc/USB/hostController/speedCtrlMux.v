module speedCtrlMux (directCtrlRate, directCtrlPol, sendPacketRate, sendPacketPol, sendPacketSel, fullSpeedRate, fullSpeedPol);
input   directCtrlRate;
input   directCtrlPol;
input   sendPacketRate;
input   sendPacketPol;
input   sendPacketSel;
output  fullSpeedRate;
output  fullSpeedPol;

wire   directCtrlRate;
wire   directCtrlPol;
wire   sendPacketRate;
wire   sendPacketPol;
wire   sendPacketSel;
reg   fullSpeedRate;
reg   fullSpeedPol;


always @(directCtrlRate or directCtrlPol or sendPacketRate or sendPacketPol or sendPacketSel)
begin
  if (sendPacketSel == 1'b1) 
  begin
  fullSpeedRate <= sendPacketRate;
  fullSpeedPol <= sendPacketPol;
  end
  else
  begin
  fullSpeedRate <= directCtrlRate;
  fullSpeedPol <= directCtrlPol;
  end
end

endmodule
