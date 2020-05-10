module hostcontroller (RXStatus, clearTXReq, clk, getPacketREn, getPacketRdy, isoEn, rst, sendPacketArbiterGnt, sendPacketArbiterReq, sendPacketPID, sendPacketRdy, sendPacketWEn, transDone, transReq, transType);
input   [7:0] RXStatus;
input   clk;
input   getPacketRdy;
input   isoEn;
input   rst;
input   sendPacketArbiterGnt;
input   sendPacketRdy;
input   transReq;
input   [1:0] transType;
output  clearTXReq;
output  getPacketREn;
output  sendPacketArbiterReq;
output  [3:0] sendPacketPID;
output  sendPacketWEn;
output  transDone;

wire    [7:0] RXStatus;
reg     clearTXReq, next_clearTXReq;
wire    clk;
reg     getPacketREn, next_getPacketREn;
wire    getPacketRdy;
wire    isoEn;
wire    rst;
wire    sendPacketArbiterGnt;
reg     sendPacketArbiterReq, next_sendPacketArbiterReq;
reg     [3:0] sendPacketPID, next_sendPacketPID;
wire    sendPacketRdy;
reg     sendPacketWEn, next_sendPacketWEn;
reg     transDone, next_transDone;
wire    transReq;
wire    [1:0] transType;

// diagram signals declarations
reg  [3:0]delCnt, next_delCnt;

reg [5:0] CurrState_hstCntrl;
reg [5:0] NextState_hstCntrl;


//--------------------------------------------------------------------
// Machine: hstCntrl
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (delCnt or transReq or transType or sendPacketArbiterGnt or getPacketRdy or sendPacketRdy or isoEn or RXStatus or sendPacketArbiterReq or transDone or clearTXReq or sendPacketWEn or getPacketREn or sendPacketPID or CurrState_hstCntrl)
begin : hstCntrl_NextState
  NextState_hstCntrl <= CurrState_hstCntrl;
  // Set default values for outputs and signals
  next_sendPacketArbiterReq <= sendPacketArbiterReq;
  next_transDone <= transDone;
  next_clearTXReq <= clearTXReq;
  next_delCnt <= delCnt;
  next_sendPacketWEn <= sendPacketWEn;
  next_getPacketREn <= getPacketREn;
  next_sendPacketPID <= sendPacketPID;
  case (CurrState_hstCntrl) // synopsys parallel_case full_case
    6'd0:
      NextState_hstCntrl <= 6'd1;
    6'd1:
      if (transReq == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd10;
        next_sendPacketArbiterReq <= 1'b1;
      end
    6'd2:
      if (transType == 1)	
        NextState_hstCntrl <= 6'd17;
      else if (transType == 2)	
        NextState_hstCntrl <= 6'd19;
      else if (transType == 3)	
        NextState_hstCntrl <= 6'd29;
      else if (transType == 0)	
        NextState_hstCntrl <= 6'd16;
    6'd3:
    begin
      next_transDone <= 1'b1;
      next_clearTXReq <= 1'b1;
      next_sendPacketArbiterReq <= 1'b0;
      next_delCnt <= 4'h0;
      NextState_hstCntrl <= 6'd9;
    end
    6'd9:
    begin
      next_clearTXReq <= 1'b0;
      next_transDone <= 1'b0;
      next_delCnt <= delCnt + 1'b1;
      //now wait for 'transReq' to clear
      if (delCnt == 4'hf)	
        NextState_hstCntrl <= 6'd1;
    end
    6'd10:
      if (sendPacketArbiterGnt == 1'b1)	
        NextState_hstCntrl <= 6'd2;
    6'd7:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_hstCntrl <= 6'd20;
    end
    6'd8:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_hstCntrl <= 6'd21;
    end
    6'd11:
    begin
      next_getPacketREn <= 1'b0;
      if (getPacketRdy == 1'b1)	
        NextState_hstCntrl <= 6'd3;
    end
    6'd16:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd7;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'hd;
      end
    6'd20:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd8;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'h3;
      end
    6'd21:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd11;
        next_getPacketREn <= 1'b1;
      end
    6'd4:
    begin
      next_getPacketREn <= 1'b0;
      if (getPacketRdy == 1'b1)	
        NextState_hstCntrl <= 6'd5;
    end
    6'd5:
      if (isoEn == 1'b1)	
        NextState_hstCntrl <= 6'd3;
      else if (RXStatus [0] == 1'b0 &&
        RXStatus [1] == 1'b0 &&
        RXStatus [2] == 1'b0 &&
        RXStatus [4] == 1'b0 &&
        RXStatus [5] == 1'b0 &&
        RXStatus [3] == 1'b0)	
        NextState_hstCntrl <= 6'd18;
      else
        NextState_hstCntrl <= 6'd3;
    6'd6:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_hstCntrl <= 6'd23;
    end
    6'd12:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd4;
        next_getPacketREn <= 1'b1;
      end
    6'd17:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd22;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'h9;
      end
    6'd18:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd6;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'h2;
      end
    6'd22:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_hstCntrl <= 6'd12;
    end
    6'd23:
      if (sendPacketRdy == 1'b1)	
        NextState_hstCntrl <= 6'd3;
    6'd13:
    begin
      next_getPacketREn <= 1'b0;
      if (getPacketRdy == 1'b1)	
        NextState_hstCntrl <= 6'd3;
    end
    6'd14:
      if (sendPacketRdy == 1'b1)	
        NextState_hstCntrl <= 6'd32;
    6'd15:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd25;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'h3;
      end
    6'd19:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd24;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'h1;
      end
    6'd24:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_hstCntrl <= 6'd15;
    end
    6'd25:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_hstCntrl <= 6'd14;
    end
    6'd32:
      if (isoEn == 1'b0)	
      begin
        NextState_hstCntrl <= 6'd13;
        next_getPacketREn <= 1'b1;
      end
      else
        NextState_hstCntrl <= 6'd3;
    6'd26:
    begin
      next_getPacketREn <= 1'b0;
      if (getPacketRdy == 1'b1)	
        NextState_hstCntrl <= 6'd3;
    end
    6'd27:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd31;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'hb;
      end
    6'd28:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd26;
        next_getPacketREn <= 1'b1;
      end
    6'd29:
      if (sendPacketRdy == 1'b1)	
      begin
        NextState_hstCntrl <= 6'd30;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'h1;
      end
    6'd30:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_hstCntrl <= 6'd27;
    end
    6'd31:
    begin
      next_sendPacketWEn <= 1'b0;
      NextState_hstCntrl <= 6'd28;
    end
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : hstCntrl_CurrentState
  if (rst)	
    CurrState_hstCntrl <= 6'd0;
  else
    CurrState_hstCntrl <= NextState_hstCntrl;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : hstCntrl_RegOutput
  if (rst)	
  begin
    delCnt <= 4'h0;
    transDone <= 1'b0;
    clearTXReq <= 1'b0;
    getPacketREn <= 1'b0;
    sendPacketArbiterReq <= 1'b0;
    sendPacketWEn <= 1'b0;
    sendPacketPID <= 4'b0;
  end
  else 
  begin
    delCnt <= next_delCnt;
    transDone <= next_transDone;
    clearTXReq <= next_clearTXReq;
    getPacketREn <= next_getPacketREn;
    sendPacketArbiterReq <= next_sendPacketArbiterReq;
    sendPacketWEn <= next_sendPacketWEn;
    sendPacketPID <= next_sendPacketPID;
  end
end

endmodule
