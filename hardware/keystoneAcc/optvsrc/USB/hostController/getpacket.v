module getPacket (RXDataIn, RXDataValid, RXFifoData, RXFifoFull, RXFifoWEn, RXPacketRdy, RXPktStatus, RXStreamStatusIn, RxPID, SIERxTimeOut, SIERxTimeOutEn, clk, getPacketEn, rst);
input   [7:0] RXDataIn;
input   RXDataValid;
input   RXFifoFull;
input   [7:0] RXStreamStatusIn;
input   SIERxTimeOut;		// Single cycle pulse
input   clk;
input   getPacketEn;
input   rst;
output  [7:0] RXFifoData;
output  RXFifoWEn;
output  RXPacketRdy;
output  [7:0] RXPktStatus;
output  [3:0] RxPID;
output  SIERxTimeOutEn;

wire    [7:0] RXDataIn;
wire    RXDataValid;
reg     [7:0] RXFifoData, next_RXFifoData;
wire    RXFifoFull;
reg     RXFifoWEn, next_RXFifoWEn;
reg     RXPacketRdy, next_RXPacketRdy;
reg     [7:0] RXPktStatus;
wire    [7:0] RXStreamStatusIn;
reg     [3:0] RxPID, next_RxPID;
wire    SIERxTimeOut;
reg     SIERxTimeOutEn, next_SIERxTimeOutEn;
wire    clk;
wire    getPacketEn;
wire    rst;

// diagram signals declarations
reg  ACKRxed, next_ACKRxed;
reg  CRCError, next_CRCError;
reg  NAKRxed, next_NAKRxed;
reg  [7:0]RXByteOld, next_RXByteOld;
reg  [7:0]RXByteOldest, next_RXByteOldest;
reg  [7:0]RXByte, next_RXByte;
reg  RXOverflow, next_RXOverflow;
reg  [7:0]RXStreamStatus, next_RXStreamStatus;
reg  RXTimeOut, next_RXTimeOut;
reg  bitStuffError, next_bitStuffError;
reg  dataSequence, next_dataSequence;
reg  stallRxed, next_stallRxed;

reg [4:0] CurrState_getPkt;
reg [4:0] NextState_getPkt;

// Diagram actions (continuous assignments allowed only: assign ...)

always @
(CRCError or bitStuffError or
  RXOverflow or RXTimeOut or
  NAKRxed or stallRxed or
  ACKRxed or dataSequence)
begin
    RXPktStatus <= {
    dataSequence, ACKRxed,
    stallRxed, NAKRxed,
    RXTimeOut, RXOverflow,
    bitStuffError, CRCError};
end

//--------------------------------------------------------------------
// Machine: getPkt
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (RXDataIn or RXStreamStatusIn or RXByte or RXByteOldest or RXByteOld or SIERxTimeOut or RXDataValid or RXStreamStatus or getPacketEn or RXFifoFull or CRCError or bitStuffError or RXOverflow or RXTimeOut or NAKRxed or stallRxed or ACKRxed or dataSequence or SIERxTimeOutEn or RxPID or RXPacketRdy or RXFifoWEn or RXFifoData or CurrState_getPkt)
begin : getPkt_NextState
  NextState_getPkt <= CurrState_getPkt;
  // Set default values for outputs and signals
  next_CRCError <= CRCError;
  next_bitStuffError <= bitStuffError;
  next_RXOverflow <= RXOverflow;
  next_RXTimeOut <= RXTimeOut;
  next_NAKRxed <= NAKRxed;
  next_stallRxed <= stallRxed;
  next_ACKRxed <= ACKRxed;
  next_dataSequence <= dataSequence;
  next_SIERxTimeOutEn <= SIERxTimeOutEn;
  next_RXByte <= RXByte;
  next_RXStreamStatus <= RXStreamStatus;
  next_RxPID <= RxPID;
  next_RXPacketRdy <= RXPacketRdy;
  next_RXByteOldest <= RXByteOldest;
  next_RXByteOld <= RXByteOld;
  next_RXFifoWEn <= RXFifoWEn;
  next_RXFifoData <= RXFifoData;
  case (CurrState_getPkt)
    5'd12:
      NextState_getPkt <= 5'd15;
    5'd13:
    begin
      next_CRCError <= 1'b0;
      next_bitStuffError <= 1'b0;
      next_RXOverflow <= 1'b0;
      next_RXTimeOut <= 1'b0;
      next_NAKRxed <= 1'b0;
      next_stallRxed <= 1'b0;
      next_ACKRxed <= 1'b0;
      next_dataSequence <= 1'b0;
      next_SIERxTimeOutEn <= 1'b1;
      if (SIERxTimeOut == 1'b1)	
      begin
        NextState_getPkt <= 5'd16;
        next_RXTimeOut <= 1'b1;
      end
      else if (RXDataValid == 1'b1)	
      begin
        NextState_getPkt <= 5'd14;
        next_RXByte <= RXDataIn;
        next_RXStreamStatus <= RXStreamStatusIn;
      end
    end
    5'd14:
      if (RXStreamStatus == 0)	
      begin
        NextState_getPkt <= 5'd0;
        next_RxPID <= RXByte[3:0];
      end
      else
      begin
        NextState_getPkt <= 5'd16;
        next_RXTimeOut <= 1'b1;
      end
    5'd15:
    begin
      next_RXPacketRdy <= 1'b0;
      next_SIERxTimeOutEn <= 1'b0;
      if (getPacketEn == 1'b1)	
        NextState_getPkt <= 5'd13;
    end
    5'd16:
    begin
      next_RXPacketRdy <= 1'b1;
      NextState_getPkt <= 5'd15;
    end
    5'd0:
      if (RXByte[1:0] == 2'b10)	
        NextState_getPkt <= 5'd1;
      else if (RXByte[1:0] == 2'b11)	
        NextState_getPkt <= 5'd2;
      else
        NextState_getPkt <= 5'd16;
    5'd1:
      if (RXDataValid == 1'b1)	
      begin
        NextState_getPkt <= 5'd16;
        next_RXOverflow <= RXDataIn[2];
        next_NAKRxed <= RXDataIn[3];
        next_stallRxed <= RXDataIn[4];
        next_ACKRxed <= RXDataIn[5];
      end
    5'd2:
      if (RXDataValid == 1'b1)	
      begin
        NextState_getPkt <= 5'd3;
        next_RXByte <= RXDataIn;
        next_RXStreamStatus <= RXStreamStatusIn;
      end
    5'd3:
      if (RXStreamStatus == 1)	
      begin
        NextState_getPkt <= 5'd4;
        next_RXByteOldest <= RXByte;
      end
      else
        NextState_getPkt <= 5'd5;
    5'd4:
      if (RXDataValid == 1'b1)	
      begin
        NextState_getPkt <= 5'd6;
        next_RXByte <= RXDataIn;
        next_RXStreamStatus <= RXStreamStatusIn;
      end
    5'd5:
    begin
      next_CRCError <= RXByte[0];
      next_bitStuffError <= RXByte[1];
      next_dataSequence <= RXByte[6];
      NextState_getPkt <= 5'd16;
    end
    5'd6:
      if (RXStreamStatus == 1)	
      begin
        NextState_getPkt <= 5'd7;
        next_RXByteOld <= RXByte;
      end
      else
        NextState_getPkt <= 5'd5;
    5'd7:
      if (RXDataValid == 1'b1)	
      begin
        NextState_getPkt <= 5'd8;
        next_RXByte <= RXDataIn;
        next_RXStreamStatus <= RXStreamStatusIn;
      end
    5'd8:
      if (RXStreamStatus == 1)	
        NextState_getPkt <= 5'd9;
      else
        NextState_getPkt <= 5'd5;
    5'd9:
      if (RXFifoFull == 1'b1)	
      begin
        NextState_getPkt <= 5'd10;
        next_RXOverflow <= 1'b1;
      end
      else
      begin
        NextState_getPkt <= 5'd11;
        next_RXFifoWEn <= 1'b1;
        next_RXFifoData <= RXByteOldest;
        next_RXByteOldest <= RXByteOld;
        next_RXByteOld <= RXByte;
      end
    5'd10:
      NextState_getPkt <= 5'd11;
    5'd11:
    begin
      next_RXFifoWEn <= 1'b0;
      if ((RXDataValid == 1'b1) && (RXStreamStatusIn == 1))	
      begin
        NextState_getPkt <= 5'd17;
        next_RXByte <= RXDataIn;
        next_RXStreamStatus <= RXStreamStatusIn;
      end
      else if (RXDataValid == 1'b1)	
      begin
        NextState_getPkt <= 5'd5;
        next_RXByte <= RXDataIn;
        next_RXStreamStatus <= RXStreamStatusIn;
      end
    end
    5'd17:
      NextState_getPkt <= 5'd9;
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : getPkt_CurrentState
  if (rst)	
    CurrState_getPkt <= 5'd12;
  else
    CurrState_getPkt <= NextState_getPkt;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : getPkt_RegOutput
  if (rst)	
  begin
    RXByteOld <= 8'h00;
    RXByteOldest <= 8'h00;
    CRCError <= 1'b0;
    bitStuffError <= 1'b0;
    RXOverflow <= 1'b0;
    RXTimeOut <= 1'b0;
    NAKRxed <= 1'b0;
    stallRxed <= 1'b0;
    ACKRxed <= 1'b0;
    dataSequence <= 1'b0;
    RXByte <= 8'h00;
    RXStreamStatus <= 8'h00;
    RXPacketRdy <= 1'b0;
    RXFifoWEn <= 1'b0;
    RXFifoData <= 8'h00;
    RxPID <= 4'h0;
    SIERxTimeOutEn <= 1'b0;
  end
  else 
  begin
    RXByteOld <= next_RXByteOld;
    RXByteOldest <= next_RXByteOldest;
    CRCError <= next_CRCError;
    bitStuffError <= next_bitStuffError;
    RXOverflow <= next_RXOverflow;
    RXTimeOut <= next_RXTimeOut;
    NAKRxed <= next_NAKRxed;
    stallRxed <= next_stallRxed;
    ACKRxed <= next_ACKRxed;
    dataSequence <= next_dataSequence;
    RXByte <= next_RXByte;
    RXStreamStatus <= next_RXStreamStatus;
    RXPacketRdy <= next_RXPacketRdy;
    RXFifoWEn <= next_RXFifoWEn;
    RXFifoData <= next_RXFifoData;
    RxPID <= next_RxPID;
    SIERxTimeOutEn <= next_SIERxTimeOutEn;
  end
end

endmodule