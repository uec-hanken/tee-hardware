module SIEReceiver (RxWireDataIn, RxWireDataWEn, clk, connectState, rst);
input   [1:0] RxWireDataIn;
input   RxWireDataWEn;
input   clk;
input   rst;
output  [1:0] connectState;

wire    [1:0] RxWireDataIn;
wire    RxWireDataWEn;
wire    clk;
reg     [1:0] connectState, next_connectState;
wire    rst;

// diagram signals declarations
reg  [3:0]RXStMachCurrState, next_RXStMachCurrState;
reg  [7:0]RXWaitCount, next_RXWaitCount;
reg  [1:0]RxBits, next_RxBits;

reg [3:0] CurrState_rcvr;
reg [3:0] NextState_rcvr;


//--------------------------------------------------------------------
// Machine: rcvr
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (RxWireDataIn or RxBits or RXWaitCount or RxWireDataWEn or RXStMachCurrState or connectState or CurrState_rcvr)
begin : rcvr_NextState
  NextState_rcvr <= CurrState_rcvr;
  // Set default values for outputs and signals
  next_RxBits <= RxBits;
  next_RXStMachCurrState <= RXStMachCurrState;
  next_RXWaitCount <= RXWaitCount;
  next_connectState <= connectState;
  case (CurrState_rcvr)
    4'd4:
      if ((RxWireDataWEn == 1'b1) && (RXStMachCurrState == 4'd2))	
      begin
        NextState_rcvr <= 4'd1;
        next_RxBits <= RxWireDataIn;
      end
      else if ((RxWireDataWEn == 1'b1) && (RXStMachCurrState == 4'd3))	
      begin
        NextState_rcvr <= 4'd2;
        next_RxBits <= RxWireDataIn;
      end
      else if ((RxWireDataWEn == 1'b1) && (RXStMachCurrState == 4'd4))	
      begin
        NextState_rcvr <= 4'd6;
        next_RxBits <= RxWireDataIn;
      end
      else if ((RxWireDataWEn == 1'b1) && (RXStMachCurrState == 4'd5))	
      begin
        NextState_rcvr <= 4'd7;
        next_RxBits <= RxWireDataIn;
      end
      else if ((RxWireDataWEn == 1'b1) && (RXStMachCurrState == 4'd6))	
      begin
        NextState_rcvr <= 4'd8;
        next_RxBits <= RxWireDataIn;
      end
      else if ((RxWireDataWEn == 1'b1) && (RXStMachCurrState == 4'd0))	
      begin
        NextState_rcvr <= 4'd3;
        next_RxBits <= RxWireDataIn;
      end
      else if ((RxWireDataWEn == 1'b1) && (RXStMachCurrState == 4'd1))	
      begin
        NextState_rcvr <= 4'd0;
        next_RxBits <= RxWireDataIn;
      end
    4'd5:
    begin
      next_RXStMachCurrState <= 4'd0;
      next_RXWaitCount <= 8'h00;
      next_connectState <= 2'd0;
      next_RxBits <= 2'b00;
      NextState_rcvr <= 4'd4;
    end
    4'd3:
      if (RxBits == 2'b01)	
      begin
        NextState_rcvr <= 4'd4;
        next_RXStMachCurrState <= 4'd2;
        next_RXWaitCount <= 8'h00;
      end
      else if (RxBits == 2'b10)	
      begin
        NextState_rcvr <= 4'd4;
        next_RXStMachCurrState <= 4'd1;
        next_RXWaitCount <= 8'h00;
      end
      else
        NextState_rcvr <= 4'd4;
    4'd0:
    begin
      if (RxBits == 2'b10)
      begin
        next_RXWaitCount <= RXWaitCount + 1'b1;
          if (RXWaitCount == 8'd120)
          begin
          next_connectState <= 2'd2;
          next_RXStMachCurrState <= 4'd4;
          end
      end
      else
      begin
        next_RXStMachCurrState <= 4'd0;
      end
      NextState_rcvr <= 4'd4;
    end
    4'd1:
    begin
      if (RxBits == 2'b01)
      begin
        next_RXWaitCount <= RXWaitCount + 1'b1;
          if (RXWaitCount == 8'd120)
          begin
          next_connectState <= 2'd1;
          next_RXStMachCurrState <= 4'd3;
          end
      end
      else
      begin
        next_RXStMachCurrState <= 4'd0;
      end
      NextState_rcvr <= 4'd4;
    end
    4'd2:
    begin
      NextState_rcvr <= 4'd4;
      if (RxBits == 2'b00)
      begin
        next_RXStMachCurrState <= 4'd5;
        next_RXWaitCount <= 0;
      end
    end
    4'd6:
    begin
      NextState_rcvr <= 4'd4;
      if (RxBits == 2'b00)
      begin
        next_RXStMachCurrState <= 4'd6;
        next_RXWaitCount <= 0;
      end
    end
    4'd7:
    begin
      NextState_rcvr <= 4'd4;
      if (RxBits == 2'b00)
      begin
        next_RXWaitCount <= RXWaitCount + 1'b1;
          if (RXWaitCount == 8'd120)
          begin
          next_RXStMachCurrState <= 4'd0;
          next_connectState <= 2'd0;
          end
      end
      else
      begin
        next_RXStMachCurrState <= 4'd3;
      end
    end
    4'd8:
    begin
      NextState_rcvr <= 4'd4;
      if (RxBits == 2'b00)
      begin
        next_RXWaitCount <= RXWaitCount + 1'b1;
          if (RXWaitCount == 8'd120)
          begin
          next_RXStMachCurrState <= 4'd0;
          next_connectState <= 2'd0;
          end
      end
      else
      begin
        next_RXStMachCurrState <= 4'd4;
      end
    end
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : rcvr_CurrentState
  if (rst)	
    CurrState_rcvr <= 4'd5;
  else
    CurrState_rcvr <= NextState_rcvr;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : rcvr_RegOutput
  if (rst)	
  begin
    RXStMachCurrState <= 4'd0;
    RXWaitCount <= 8'h00;
    RxBits <= 2'b00;
    connectState <= 2'd0;
  end
  else 
  begin
    RXStMachCurrState <= next_RXStMachCurrState;
    RXWaitCount <= next_RXWaitCount;
    RxBits <= next_RxBits;
    connectState <= next_connectState;
  end
end

endmodule
