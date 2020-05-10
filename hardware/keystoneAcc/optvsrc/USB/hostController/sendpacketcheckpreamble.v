module sendPacketCheckPreamble (clk, preAmbleEnable, rst, sendPacketCPPID, sendPacketCPReady, sendPacketCPWEn, sendPacketPID, sendPacketRdy, sendPacketWEn);
input   clk;
input   preAmbleEnable;
input   rst;
input   [3:0] sendPacketCPPID;
input   sendPacketCPWEn;
input   sendPacketRdy;
output  sendPacketCPReady;
output  [3:0] sendPacketPID;
output  sendPacketWEn;

wire    clk;
wire    preAmbleEnable;
wire    rst;
wire    [3:0] sendPacketCPPID;
reg     sendPacketCPReady, next_sendPacketCPReady;
wire    sendPacketCPWEn;
reg     [3:0] sendPacketPID, next_sendPacketPID;
wire    sendPacketRdy;
reg     sendPacketWEn, next_sendPacketWEn;

reg [3:0] CurrState_sendPktCP;
reg [3:0] NextState_sendPktCP;


//--------------------------------------------------------------------
// Machine: sendPktCP
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (sendPacketCPPID or sendPacketCPWEn or preAmbleEnable or sendPacketRdy or sendPacketCPReady or sendPacketWEn or sendPacketPID or CurrState_sendPktCP)
begin : sendPktCP_NextState
  NextState_sendPktCP <= CurrState_sendPktCP;
  // Set default values for outputs and signals
  next_sendPacketCPReady <= sendPacketCPReady;
  next_sendPacketWEn <= sendPacketWEn;
  next_sendPacketPID <= sendPacketPID;
  case (CurrState_sendPktCP)
    4'd0:
      if (sendPacketCPWEn == 1'b1)	
      begin
        NextState_sendPktCP <= 4'd2;
        next_sendPacketCPReady <= 1'b0;
      end
    4'd1:
      NextState_sendPktCP <= 4'd0;
    4'd2:
      if (preAmbleEnable == 1'b1 && sendPacketCPPID != 4'h5)	
        NextState_sendPktCP <= 4'd4;
      else
        NextState_sendPktCP <= 4'd9;
    4'd11:
    begin
      next_sendPacketCPReady <= 1'b1;
      NextState_sendPktCP <= 4'd0;
    end
    4'd3:
    begin
      next_sendPacketWEn <= 1'b1;
      next_sendPacketPID <= 4'hc;
      NextState_sendPktCP <= 4'd5;
    end
    4'd4:
      if (sendPacketRdy == 1'b1)	
        NextState_sendPktCP <= 4'd3;
    4'd5:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_sendPktCP <= 4'd12;
    end
    4'd6:
    begin
      next_sendPacketWEn <= 1'b1;
      next_sendPacketPID <= sendPacketCPPID;
      NextState_sendPktCP <= 4'd7;
    end
    4'd7:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_sendPktCP <= 4'd13;
    end
    4'd12:
      if (sendPacketRdy == 1'b1)	
        NextState_sendPktCP <= 4'd6;
    4'd13:
      if (sendPacketRdy == 1'b1)	
        NextState_sendPktCP <= 4'd11;
    4'd8:
    begin
      next_sendPacketWEn <= 1'b1;
      next_sendPacketPID <= sendPacketCPPID;
      NextState_sendPktCP <= 4'd10;
    end
    4'd9:
      if (sendPacketRdy == 1'b1)	
        NextState_sendPktCP <= 4'd8;
    4'd10:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_sendPktCP <= 4'd11;
    end
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : sendPktCP_CurrentState
  if (rst)	
    CurrState_sendPktCP <= 4'd1;
  else
    CurrState_sendPktCP <= NextState_sendPktCP;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : sendPktCP_RegOutput
  if (rst)	
  begin
    sendPacketWEn <= 1'b0;
    sendPacketPID <= 4'b0;
    sendPacketCPReady <= 1'b1;
  end
  else 
  begin
    sendPacketWEn <= next_sendPacketWEn;
    sendPacketPID <= next_sendPacketPID;
    sendPacketCPReady <= next_sendPacketCPReady;
  end
end

endmodule
