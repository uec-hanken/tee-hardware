module SCTxPortArbiter (SCTxPortCntl, SCTxPortData, SCTxPortRdyIn, SCTxPortRdyOut, SCTxPortWEnable, clk, directCntlCntl, directCntlData, directCntlGnt, directCntlReq, directCntlWEn, rst, sendPacketCntl, sendPacketData, sendPacketGnt, sendPacketReq, sendPacketWEn);
input   SCTxPortRdyIn;
input   clk;
input   [7:0] directCntlCntl;
input   [7:0] directCntlData;
input   directCntlReq;
input   directCntlWEn;
input   rst;
input   [7:0] sendPacketCntl;
input   [7:0] sendPacketData;
input   sendPacketReq;
input   sendPacketWEn;
output  [7:0] SCTxPortCntl;
output  [7:0] SCTxPortData;
output  SCTxPortRdyOut;
output  SCTxPortWEnable;
output  directCntlGnt;
output  sendPacketGnt;

reg     [7:0] SCTxPortCntl, next_SCTxPortCntl;
reg     [7:0] SCTxPortData, next_SCTxPortData;
wire    SCTxPortRdyIn;
reg     SCTxPortRdyOut, next_SCTxPortRdyOut;
reg     SCTxPortWEnable, next_SCTxPortWEnable;
wire    clk;
wire    [7:0] directCntlCntl;
wire    [7:0] directCntlData;
reg     directCntlGnt, next_directCntlGnt;
wire    directCntlReq;
wire    directCntlWEn;
wire    rst;
wire    [7:0] sendPacketCntl;
wire    [7:0] sendPacketData;
reg     sendPacketGnt, next_sendPacketGnt;
wire    sendPacketReq;
wire    sendPacketWEn;

// diagram signals declarations
reg  muxDCEn, next_muxDCEn;

reg [1:0] CurrState_SCTxArb;
reg [1:0] NextState_SCTxArb;

// Diagram actions (continuous assignments allowed only: assign ...)

// SOFController/directContol/sendPacket mux
always @(SCTxPortRdyIn)
begin
    SCTxPortRdyOut <= SCTxPortRdyIn;
end
always @(muxDCEn or
		 		 directCntlWEn or directCntlData or directCntlCntl or
                  directCntlWEn or directCntlData or directCntlCntl or
 		  		 sendPacketWEn or sendPacketData or sendPacketCntl)
begin
if (muxDCEn == 1'b1)
    begin
        SCTxPortWEnable <= directCntlWEn;
        SCTxPortData <= directCntlData;
        SCTxPortCntl <= directCntlCntl;
    end
else
    begin
        SCTxPortWEnable <= sendPacketWEn;
        SCTxPortData <= sendPacketData;
        SCTxPortCntl <= sendPacketCntl;
    end
end

//--------------------------------------------------------------------
// Machine: SCTxArb
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (sendPacketReq or directCntlReq or sendPacketGnt or muxDCEn or directCntlGnt or CurrState_SCTxArb)
begin : SCTxArb_NextState
  NextState_SCTxArb <= CurrState_SCTxArb;
  // Set default values for outputs and signals
  next_sendPacketGnt <= sendPacketGnt;
  next_muxDCEn <= muxDCEn;
  next_directCntlGnt <= directCntlGnt;
  case (CurrState_SCTxArb)
    2'b00:
      if (sendPacketReq == 1'b1)	
      begin
        NextState_SCTxArb <= 2'b01;
        next_sendPacketGnt <= 1'b1;
        next_muxDCEn <= 1'b0;
      end
      else if (directCntlReq == 1'b1)	
      begin
        NextState_SCTxArb <= 2'b10;
        next_directCntlGnt <= 1'b1;
        next_muxDCEn <= 1'b1;
      end
    2'b01:
      if (sendPacketReq == 1'b0)	
      begin
        NextState_SCTxArb <= 2'b00;
        next_sendPacketGnt <= 1'b0;
      end
    2'b10:
      if (directCntlReq == 1'b0)	
      begin
        NextState_SCTxArb <= 2'b00;
        next_directCntlGnt <= 1'b0;
      end
    2'b11:
      NextState_SCTxArb <= 2'b00;
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : SCTxArb_CurrentState
  if (rst)	
    CurrState_SCTxArb <= 2'b11;
  else
    CurrState_SCTxArb <= NextState_SCTxArb;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : SCTxArb_RegOutput
  if (rst)	
  begin
    muxDCEn <= 1'b0;
    sendPacketGnt <= 1'b0;
    directCntlGnt <= 1'b0;
  end
  else 
  begin
    muxDCEn <= next_muxDCEn;
    sendPacketGnt <= next_sendPacketGnt;
    directCntlGnt <= next_directCntlGnt;
  end
end

endmodule