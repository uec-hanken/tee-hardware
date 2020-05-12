module SOFTransmit (SOFEnable, SOFSent, SOFSyncEn, SOFTimerClr, SOFTimer, clk, rst, sendPacketArbiterGnt, sendPacketArbiterReq, sendPacketRdy, sendPacketWEn, fullSpeedRate);
input   SOFEnable;		// After host software asserts SOFEnable, must wait TBD time before asserting SOFSyncEn
input   SOFSyncEn;
input   [15:0] SOFTimer;
input   clk;
input   rst;
input   sendPacketArbiterGnt;
input   sendPacketRdy;
output  SOFSent;		// single cycle pulse
output  SOFTimerClr;		// Single cycle pulse
output  sendPacketArbiterReq;
output  sendPacketWEn;
input   fullSpeedRate;

wire    SOFEnable;
reg     SOFSent, next_SOFSent;
wire    SOFSyncEn;
reg     SOFTimerClr, next_SOFTimerClr;
wire    [15:0] SOFTimer;
wire    clk;
wire    rst;
wire    sendPacketArbiterGnt;
reg     sendPacketArbiterReq, next_sendPacketArbiterReq;
wire    sendPacketRdy;
reg     sendPacketWEn, next_sendPacketWEn;
reg     [15:0] SOFNearTime;

// diagram signals declarations
reg  [7:0]i, next_i;

reg [2:0] CurrState_SOFTx;
reg [2:0] NextState_SOFTx;


//--------------------------------------------------------------------
// Machine: SOFTx
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (i or SOFTimer or SOFSyncEn or SOFEnable or sendPacketArbiterGnt or sendPacketRdy or sendPacketArbiterReq or sendPacketWEn or SOFTimerClr or SOFSent or CurrState_SOFTx)
begin : SOFTx_NextState
  NextState_SOFTx <= CurrState_SOFTx;
  // Set default values for outputs and signals
  next_sendPacketArbiterReq <= sendPacketArbiterReq;
  next_sendPacketWEn <= sendPacketWEn;
  next_SOFTimerClr <= SOFTimerClr;
  next_SOFSent <= SOFSent;
  next_i <= i;
  case (CurrState_SOFTx)
    3'd0:
      NextState_SOFTx <= 3'd1;
    3'd1:
      if (SOFTimer >= SOFNearTime  ||
        (SOFSyncEn == 1'b1 &&
        SOFEnable == 1'b1))	
      begin
        NextState_SOFTx <= 3'd2;
        next_sendPacketArbiterReq <= 1'b1;
      end
    3'd2:
      if (sendPacketArbiterGnt == 1'b1 && sendPacketRdy == 1'b1)	
        NextState_SOFTx <= 3'd3;
    3'd3:
      if (SOFTimer >= 16'hbb79)	
      begin
        NextState_SOFTx <= 3'd4;
        next_sendPacketWEn <= 1'b1;
        next_SOFTimerClr <= 1'b1;
        next_SOFSent <= 1'b1;
      end
      else if (SOFEnable == 1'b0)	
      begin
        NextState_SOFTx <= 3'd4;
        next_SOFTimerClr <= 1'b1;
      end
    3'd4:
    begin
      next_sendPacketWEn <= 1'b0;
      next_SOFTimerClr <= 1'b0;
      next_SOFSent <= 1'b0;
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_SOFTx <= 3'd5;
        next_i <= 8'h00;
      end
    end
    3'd5:
    begin
      next_i <= i + 1'b1;
      if (i==8'hff)	
      begin
        NextState_SOFTx <= 3'd6;
        next_sendPacketArbiterReq <= 1'b0;
        next_i <= 8'h00;
      end
    end
    3'd6:
    begin
      next_i <= i + 1'b1;
      if (i==8'hff)	
        NextState_SOFTx <= 3'd1;
    end
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : SOFTx_CurrentState
  if (rst)	
    CurrState_SOFTx <= 3'd0;
  else
    CurrState_SOFTx <= NextState_SOFTx;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : SOFTx_RegOutput
  if (rst)	
  begin
    i <= 8'h00;
    SOFSent <= 1'b0;
    SOFTimerClr <= 1'b0;
    sendPacketArbiterReq <= 1'b0;
    sendPacketWEn <= 1'b0;
    SOFNearTime <= 16'h0000;
  end
  else 
  begin
    i <= next_i;
    SOFSent <= next_SOFSent;
    SOFTimerClr <= next_SOFTimerClr;
    sendPacketArbiterReq <= next_sendPacketArbiterReq;
    sendPacketWEn <= next_sendPacketWEn;
    if (fullSpeedRate == 1'b1)
      SOFNearTime <= 16'hbb79 - 16'h0c80;
    else
      SOFNearTime <= 16'hbb79 - 16'h6400;
  end
end

endmodule
