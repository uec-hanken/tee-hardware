module sendPacketArbiter (HCTxGnt, HCTxReq, HC_PID, HC_SP_WEn, SOFTxGnt, SOFTxReq, SOF_SP_WEn, clk, rst, sendPacketPID, sendPacketWEnable);
input   HCTxReq;
input   [3:0] HC_PID;
input   HC_SP_WEn;
input   SOFTxReq;
input   SOF_SP_WEn;
input   clk;
input   rst;
output  HCTxGnt;
output  SOFTxGnt;
output  [3:0] sendPacketPID;
output  sendPacketWEnable;

reg     HCTxGnt, next_HCTxGnt;
wire    HCTxReq;
wire    [3:0] HC_PID;
wire    HC_SP_WEn;
reg     SOFTxGnt, next_SOFTxGnt;
wire    SOFTxReq;
wire    SOF_SP_WEn;
wire    clk;
wire    rst;
reg     [3:0] sendPacketPID, next_sendPacketPID;
reg     sendPacketWEnable, next_sendPacketWEnable;

// diagram signals declarations
reg  muxSOFNotHC, next_muxSOFNotHC;

reg [1:0] CurrState_sendPktArb;
reg [1:0] NextState_sendPktArb;

// Diagram actions (continuous assignments allowed only: assign ...)

// hostController/SOFTransmit mux
always @(muxSOFNotHC or SOF_SP_WEn or HC_SP_WEn or HC_PID)
begin
    if (muxSOFNotHC  == 1'b1)
    begin
        sendPacketWEnable <= SOF_SP_WEn;
        sendPacketPID <= 4'h5;
    end
    else
    begin
        sendPacketWEnable <= HC_SP_WEn;
        sendPacketPID <= HC_PID;
    end
end

//--------------------------------------------------------------------
// Machine: sendPktArb
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (HCTxReq or SOFTxReq or HCTxGnt or SOFTxGnt or muxSOFNotHC or CurrState_sendPktArb)
begin : sendPktArb_NextState
  NextState_sendPktArb <= CurrState_sendPktArb;
  // Set default values for outputs and signals
  next_HCTxGnt <= HCTxGnt;
  next_SOFTxGnt <= SOFTxGnt;
  next_muxSOFNotHC <= muxSOFNotHC;
  case (CurrState_sendPktArb)
    2'd0:
      if (HCTxReq == 1'b0)	
      begin
        NextState_sendPktArb <= 2'd2;
        next_HCTxGnt <= 1'b0;
      end
    2'd1:
      if (SOFTxReq == 1'b0)	
      begin
        NextState_sendPktArb <= 2'd2;
        next_SOFTxGnt <= 1'b0;
      end
    2'd2:
      if (SOFTxReq == 1'b1)	
      begin
        NextState_sendPktArb <= 2'd1;
        next_SOFTxGnt <= 1'b1;
        next_muxSOFNotHC <= 1'b1;
      end
      else if (HCTxReq == 1'b1)	
      begin
        NextState_sendPktArb <= 2'd0;
        next_HCTxGnt <= 1'b1;
        next_muxSOFNotHC <= 1'b0;
      end
    2'd3:
      NextState_sendPktArb <= 2'd2;
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : sendPktArb_CurrentState
  if (rst)	
    CurrState_sendPktArb <= 2'd3;
  else
    CurrState_sendPktArb <= NextState_sendPktArb;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : sendPktArb_RegOutput
  if (rst)	
  begin
    muxSOFNotHC <= 1'b0;
    SOFTxGnt <= 1'b0;
    HCTxGnt <= 1'b0;
  end
  else 
  begin
    muxSOFNotHC <= next_muxSOFNotHC;
    SOFTxGnt <= next_SOFTxGnt;
    HCTxGnt <= next_HCTxGnt;
  end
end

endmodule