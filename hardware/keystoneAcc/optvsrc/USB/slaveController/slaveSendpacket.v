module slaveSendPacket (PID, SCTxPortCntl, SCTxPortData, SCTxPortGnt, SCTxPortRdy, SCTxPortReq, SCTxPortWEn, clk, fifoData, fifoEmpty, fifoReadEn, rst, sendPacketRdy, sendPacketWEn);
input   [3:0] PID;
input   SCTxPortGnt;
input   SCTxPortRdy;
input   clk;
input   [7:0] fifoData;
input   fifoEmpty;
input   rst;
input   sendPacketWEn;
output  [7:0] SCTxPortCntl;
output  [7:0] SCTxPortData;
output  SCTxPortReq;
output  SCTxPortWEn;
output  fifoReadEn;
output  sendPacketRdy;

wire    [3:0] PID;
reg     [7:0] SCTxPortCntl, next_SCTxPortCntl;
reg     [7:0] SCTxPortData, next_SCTxPortData;
wire    SCTxPortGnt;
wire    SCTxPortRdy;
reg     SCTxPortReq, next_SCTxPortReq;
reg     SCTxPortWEn, next_SCTxPortWEn;
wire    clk;
wire    [7:0] fifoData;
wire    fifoEmpty;
reg     fifoReadEn, next_fifoReadEn;
wire    rst;
reg     sendPacketRdy, next_sendPacketRdy;
wire    sendPacketWEn;

// diagram signals declarations
reg  [7:0]PIDNotPID;

reg [3:0] CurrState_slvSndPkt;
reg [3:0] NextState_slvSndPkt;

// Diagram actions (continuous assignments allowed only: assign ...)

always @(PID)
begin
    PIDNotPID <=  { (PID ^ 4'hf), PID };
end

//--------------------------------------------------------------------
// Machine: slvSndPkt
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (PIDNotPID or fifoData or sendPacketWEn or SCTxPortGnt or SCTxPortRdy or PID or fifoEmpty or sendPacketRdy or SCTxPortReq or SCTxPortWEn or SCTxPortData or SCTxPortCntl or fifoReadEn or CurrState_slvSndPkt)
begin : slvSndPkt_NextState
  NextState_slvSndPkt <= CurrState_slvSndPkt;
  // Set default values for outputs and signals
  next_sendPacketRdy <= sendPacketRdy;
  next_SCTxPortReq <= SCTxPortReq;
  next_SCTxPortWEn <= SCTxPortWEn;
  next_SCTxPortData <= SCTxPortData;
  next_SCTxPortCntl <= SCTxPortCntl;
  next_fifoReadEn <= fifoReadEn;
  case (CurrState_slvSndPkt)
    4'd0:
      NextState_slvSndPkt <= 4'd1;
    4'd1:
      if (sendPacketWEn == 1'b1)	
      begin
        NextState_slvSndPkt <= 4'd2;
        next_sendPacketRdy <= 1'b0;
        next_SCTxPortReq <= 1'b1;
      end
    4'd2:
      if (SCTxPortGnt == 1'b1)	
        NextState_slvSndPkt <= 4'd3;
    4'd5:
    begin
      NextState_slvSndPkt <= 4'd1;
      next_sendPacketRdy <= 1'b1;
      next_SCTxPortReq <= 1'b0;
    end
    4'd11:
      NextState_slvSndPkt <= 4'd5;
    4'd3:
      if (SCTxPortRdy == 1'b1)	
      begin
        NextState_slvSndPkt <= 4'd4;
        next_SCTxPortWEn <= 1'b1;
        next_SCTxPortData <= PIDNotPID;
        next_SCTxPortCntl <= 8'h02;
      end
    4'd4:
    begin
      next_SCTxPortWEn <= 1'b0;
      if (PID == 4'h3 || PID == 4'hb)	
        NextState_slvSndPkt <= 4'd8;
      else
        NextState_slvSndPkt <= 4'd11;
    end
    4'd6:
    begin
      next_SCTxPortWEn <= 1'b1;
      next_SCTxPortData <= fifoData;
      next_SCTxPortCntl <= 8'h03;
      NextState_slvSndPkt <= 4'd12;
    end
    4'd7:
      if (SCTxPortRdy == 1'b1)	
      begin
        NextState_slvSndPkt <= 4'd13;
        next_fifoReadEn <= 1'b1;
      end
    4'd8:
      if (fifoEmpty == 1'b0)	
        NextState_slvSndPkt <= 4'd7;
      else
        NextState_slvSndPkt <= 4'd10;
    4'd9:
    begin
      next_SCTxPortWEn <= 1'b0;
      NextState_slvSndPkt <= 4'd5;
    end
    4'd10:
      if (SCTxPortRdy == 1'b1)	
      begin
        NextState_slvSndPkt <= 4'd9;
        //Last byte is not valid data,
        //but the 'TX_PACKET_STOP' flag is required
        //by the SIE state machine to detect end of data packet
        next_SCTxPortWEn <= 1'b1;
        next_SCTxPortData <= 8'h00;
        next_SCTxPortCntl <= 8'h04;
      end
    4'd12:
    begin
      next_SCTxPortWEn <= 1'b0;
      NextState_slvSndPkt <= 4'd8;
    end
    4'd13:
    begin
      next_fifoReadEn <= 1'b0;
      NextState_slvSndPkt <= 4'd6;
    end
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : slvSndPkt_CurrentState
  if (rst)	
    CurrState_slvSndPkt <= 4'd0;
  else
    CurrState_slvSndPkt <= NextState_slvSndPkt;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : slvSndPkt_RegOutput
  if (rst)	
  begin
    sendPacketRdy <= 1'b1;
    SCTxPortReq <= 1'b0;
    SCTxPortWEn <= 1'b0;
    SCTxPortData <= 8'h00;
    SCTxPortCntl <= 8'h00;
    fifoReadEn <= 1'b0;
  end
  else 
  begin
    sendPacketRdy <= next_sendPacketRdy;
    SCTxPortReq <= next_SCTxPortReq;
    SCTxPortWEn <= next_SCTxPortWEn;
    SCTxPortData <= next_SCTxPortData;
    SCTxPortCntl <= next_SCTxPortCntl;
    fifoReadEn <= next_fifoReadEn;
  end
end

endmodule