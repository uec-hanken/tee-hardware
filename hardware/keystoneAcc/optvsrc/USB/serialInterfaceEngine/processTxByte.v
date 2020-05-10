module processTxByte (JBit, KBit, TxByteCtrlIn, TxByteFullSpeedRateIn, TxByteIn, USBWireCtrl, USBWireData, USBWireFullSpeedRate, USBWireGnt, USBWireRdy, USBWireReq, USBWireWEn, clk, processTxByteRdy, processTxByteWEn, rst);
input   [1:0] JBit;
input   [1:0] KBit;
input   [7:0] TxByteCtrlIn;
input   TxByteFullSpeedRateIn;
input   [7:0] TxByteIn;
input   USBWireGnt;
input   USBWireRdy;
input   clk;
input   processTxByteWEn;
input   rst;
output  USBWireCtrl;
output  [1:0] USBWireData;
output  USBWireFullSpeedRate;
output  USBWireReq;
output  USBWireWEn;
output  processTxByteRdy;

wire    [1:0] JBit;
wire    [1:0] KBit;
wire    [7:0] TxByteCtrlIn;
wire    TxByteFullSpeedRateIn;
wire    [7:0] TxByteIn;
reg     USBWireCtrl, next_USBWireCtrl;
reg     [1:0] USBWireData, next_USBWireData;
reg     USBWireFullSpeedRate, next_USBWireFullSpeedRate;
wire    USBWireGnt;
wire    USBWireRdy;
reg     USBWireReq, next_USBWireReq;
reg     USBWireWEn, next_USBWireWEn;
wire    clk;
reg     processTxByteRdy, next_processTxByteRdy;
wire    processTxByteWEn;
wire    rst;

// diagram signals declarations
reg  [1:0]TXLineState, next_TXLineState;
reg  [3:0]TXOneCount, next_TXOneCount;
reg  [7:0]TxByteCtrl, next_TxByteCtrl;
reg  TxByteFullSpeedRate, next_TxByteFullSpeedRate;
reg  [7:0]TxByte, next_TxByte;
reg  [3:0]i, next_i;

reg [4:0] CurrState_prcTxB;
reg [4:0] NextState_prcTxB;


//--------------------------------------------------------------------
// Machine: prcTxB
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (TxByteIn or TxByteCtrlIn or TxByteFullSpeedRateIn or JBit or i or TxByte or TXOneCount or TXLineState or KBit or processTxByteWEn or USBWireGnt or USBWireRdy or TxByteFullSpeedRate or TxByteCtrl or processTxByteRdy or USBWireData or USBWireCtrl or USBWireReq or USBWireWEn or USBWireFullSpeedRate or CurrState_prcTxB)
begin : prcTxB_NextState
  NextState_prcTxB <= CurrState_prcTxB;
  // Set default values for outputs and signals
  next_processTxByteRdy <= processTxByteRdy;
  next_USBWireData <= USBWireData;
  next_USBWireCtrl <= USBWireCtrl;
  next_USBWireReq <= USBWireReq;
  next_USBWireWEn <= USBWireWEn;
  next_i <= i;
  next_TxByte <= TxByte;
  next_TxByteCtrl <= TxByteCtrl;
  next_TXLineState <= TXLineState;
  next_TXOneCount <= TXOneCount;
  next_USBWireFullSpeedRate <= USBWireFullSpeedRate;
  next_TxByteFullSpeedRate <= TxByteFullSpeedRate;
  case (CurrState_prcTxB)
    5'd0:
    begin
      next_processTxByteRdy <= 1'b0;
      next_USBWireData <= 2'b00;
      next_USBWireCtrl <= 1'b0;
      next_USBWireReq <= 1'b0;
      next_USBWireWEn <= 1'b0;
      next_i <= 4'h0;
      next_TxByte <= 8'h00;
      next_TxByteCtrl <= 8'h00;
      next_TXLineState <= 2'b0;
      next_TXOneCount <= 4'h0;
      next_USBWireFullSpeedRate <= 1'b0;
      next_TxByteFullSpeedRate <= 1'b0;
      NextState_prcTxB <= 5'd1;
    end
    5'd1:
    begin
      next_processTxByteRdy <= 1'b1;
      if ((processTxByteWEn == 1'b1) && (TxByteCtrlIn == 8'd0))	
      begin
        NextState_prcTxB <= 5'd8;
        next_processTxByteRdy <= 1'b0;
        next_TxByte <= TxByteIn;
        next_TxByteCtrl <= TxByteCtrlIn;
        next_TxByteFullSpeedRate <= TxByteFullSpeedRateIn;
        next_USBWireFullSpeedRate <= TxByteFullSpeedRateIn;
        next_TXOneCount <= 4'h0;
        next_TXLineState <= JBit;
        next_USBWireReq <= 1'b1;
      end
      else if (processTxByteWEn == 1'b1)	
      begin
        NextState_prcTxB <= 5'd2;
        next_processTxByteRdy <= 1'b0;
        next_TxByte <= TxByteIn;
        next_TxByteCtrl <= TxByteCtrlIn;
        next_TxByteFullSpeedRate <= TxByteFullSpeedRateIn;
        next_USBWireFullSpeedRate <= TxByteFullSpeedRateIn;
        next_i <= 4'h0;
      end
    end
    5'd8:
      if (USBWireGnt == 1'b1)	
        NextState_prcTxB <= 5'd15;
    5'd15:
      if ((USBWireRdy == 1'b1) && (TxByteFullSpeedRate  == 1'b0))	
        NextState_prcTxB <= 5'd19;
      else if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd16;
        //actively drive the first J bit
        next_USBWireData <= JBit;
        next_USBWireCtrl <= 1'b1;
        next_USBWireWEn <= 1'b1;
      end
    5'd16:
    begin
      next_USBWireWEn <= 1'b0;
      NextState_prcTxB <= 5'd2;
      next_i <= 4'h0;
    end
    5'd2:
    begin
      next_i <= i + 1'b1;
      next_TxByte <= {1'b0, TxByte[7:1] };
      if (TxByte[0] == 1'b1)                      //If this bit is 1, then
        next_TXOneCount <= TXOneCount + 1'b1;
          //increment 'TXOneCount'
      else                                        //else this is a zero bit
      begin
        next_TXOneCount <= 4'h0;
          //reset 'TXOneCount'
          if (TXLineState == JBit)
          next_TXLineState <= KBit;
              //toggle the line state
          else
          next_TXLineState <= JBit;
      end
      NextState_prcTxB <= 5'd3;
    end
    5'd3:
      if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd4;
        next_USBWireWEn <= 1'b1;
        next_USBWireData <= TXLineState;
        next_USBWireCtrl <= 1'b1;
      end
    5'd4:
    begin
      next_USBWireWEn <= 1'b0;
      if (TXOneCount == 4'h6)	
        NextState_prcTxB <= 5'd5;
      else if (i != 4'h8)	
        NextState_prcTxB <= 5'd2;
      else
        NextState_prcTxB <= 5'd11;
    end
    5'd5:
    begin
      next_TXOneCount <= 4'h0;
      //reset 'TXOneCount'
      if (TXLineState == JBit)
        next_TXLineState <= KBit;
          //toggle the line state
      else
        next_TXLineState <= JBit;
      NextState_prcTxB <= 5'd6;
    end
    5'd6:
      if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd7;
        next_USBWireWEn <= 1'b1;
        next_USBWireData <= TXLineState;
        next_USBWireCtrl <= 1'b1;
      end
    5'd7:
    begin
      next_USBWireWEn <= 1'b0;
      if (i == 4'h8)	
        NextState_prcTxB <= 5'd11;
      else
        NextState_prcTxB <= 5'd2;
    end
    5'd9:
    begin
      next_USBWireWEn <= 1'b0;
      NextState_prcTxB <= 5'd26;
    end
    5'd10:
      NextState_prcTxB <= 5'd25;
    5'd11:
      if (TxByteCtrl == 8'd1)	
        NextState_prcTxB <= 5'd10;
      else if (TxByteCtrl == 8'd4)	
        NextState_prcTxB <= 5'd12;
      else
        NextState_prcTxB <= 5'd1;
    5'd12:
    begin
      next_USBWireWEn <= 1'b0;
      NextState_prcTxB <= 5'd27;
    end
    5'd13:
    begin
      next_USBWireWEn <= 1'b0;
      NextState_prcTxB <= 5'd28;
    end
    5'd14:
    begin
      next_USBWireWEn <= 1'b0;
      next_USBWireReq <= 1'b0;
      //release the wire
      NextState_prcTxB <= 5'd1;
    end
    5'd25:
      if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd9;
        next_USBWireWEn <= 1'b1;
        next_USBWireData <= 2'b00;
        next_USBWireCtrl <= 1'b1;
      end
    5'd26:
      if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd12;
        next_USBWireWEn <= 1'b1;
        next_USBWireData <= 2'b00;
        next_USBWireCtrl <= 1'b1;
      end
    5'd27:
      if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd13;
        next_USBWireWEn <= 1'b1;
        next_USBWireData <= JBit;
        next_USBWireCtrl <= 1'b1;
      end
    5'd28:
      if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd14;
        next_USBWireWEn <= 1'b1;
        next_USBWireData <= JBit;
        next_USBWireCtrl <= 1'b0;
      end
    5'd17:
    begin
      next_USBWireWEn <= 1'b0;
      NextState_prcTxB <= 5'd23;
    end
    5'd18:
    begin
      next_USBWireWEn <= 1'b0;
      NextState_prcTxB <= 5'd24;
    end
    5'd19:
      if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd20;
        next_USBWireWEn <= 1'b1;
        next_USBWireData <= JBit;
        next_USBWireCtrl <= 1'b0;
      end
    5'd20:
    begin
      next_USBWireWEn <= 1'b0;
      NextState_prcTxB <= 5'd22;
    end
    5'd21:
    begin
      next_USBWireWEn <= 1'b0;
      NextState_prcTxB <= 5'd2;
      next_i <= 4'h0;
    end
    5'd22:
      if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd17;
        next_USBWireWEn <= 1'b1;
        next_USBWireData <= JBit;
        next_USBWireCtrl <= 1'b0;
      end
    5'd23:
      if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd18;
        next_USBWireWEn <= 1'b1;
        next_USBWireData <= JBit;
        next_USBWireCtrl <= 1'b0;
      end
    5'd24:
      if (USBWireRdy == 1'b1)	
      begin
        NextState_prcTxB <= 5'd21;
        //Drive the first JBit
        next_USBWireWEn <= 1'b1;
        next_USBWireData <= JBit;
        next_USBWireCtrl <= 1'b1;
      end
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : prcTxB_CurrentState
  if (rst)	
    CurrState_prcTxB <= 5'd0;
  else
    CurrState_prcTxB <= NextState_prcTxB;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : prcTxB_RegOutput
  if (rst)	
  begin
    i <= 4'h0;
    TxByte <= 8'h00;
    TxByteCtrl <= 8'h00;
    TXLineState <= 2'b0;
    TXOneCount <= 4'h0;
    TxByteFullSpeedRate <= 1'b0;
    processTxByteRdy <= 1'b0;
    USBWireData <= 2'b00;
    USBWireCtrl <= 1'b0;
    USBWireReq <= 1'b0;
    USBWireWEn <= 1'b0;
    USBWireFullSpeedRate <= 1'b0;
  end
  else 
  begin
    i <= next_i;
    TxByte <= next_TxByte;
    TxByteCtrl <= next_TxByteCtrl;
    TXLineState <= next_TXLineState;
    TXOneCount <= next_TXOneCount;
    TxByteFullSpeedRate <= next_TxByteFullSpeedRate;
    processTxByteRdy <= next_processTxByteRdy;
    USBWireData <= next_USBWireData;
    USBWireCtrl <= next_USBWireCtrl;
    USBWireReq <= next_USBWireReq;
    USBWireWEn <= next_USBWireWEn;
    USBWireFullSpeedRate <= next_USBWireFullSpeedRate;
  end
end

endmodule
