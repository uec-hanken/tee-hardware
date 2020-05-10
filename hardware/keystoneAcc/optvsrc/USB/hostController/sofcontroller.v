module SOFController (HCTxPortCntl, HCTxPortData, HCTxPortGnt, HCTxPortRdy, HCTxPortReq, HCTxPortWEn, SOFEnable, SOFTimerClr, SOFTimer, clk, rst);
input   HCTxPortGnt;
input   HCTxPortRdy;
input   SOFEnable;
input   SOFTimerClr;
input   clk;
input   rst;
output  [7:0] HCTxPortCntl;
output  [7:0] HCTxPortData;
output  HCTxPortReq;
output  HCTxPortWEn;
output  [15:0] SOFTimer;

reg     [7:0] HCTxPortCntl, next_HCTxPortCntl;
reg     [7:0] HCTxPortData, next_HCTxPortData;
wire    HCTxPortGnt;
wire    HCTxPortRdy;
reg     HCTxPortReq, next_HCTxPortReq;
reg     HCTxPortWEn, next_HCTxPortWEn;
wire    SOFEnable;
wire    SOFTimerClr;
reg     [15:0] SOFTimer, next_SOFTimer;
wire    clk;
wire    rst;

reg [2:0] CurrState_sofCntl;
reg [2:0] NextState_sofCntl;


//--------------------------------------------------------------------
// Machine: sofCntl
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (SOFTimerClr or SOFTimer or SOFEnable or HCTxPortRdy or HCTxPortGnt or HCTxPortReq or HCTxPortWEn or HCTxPortData or HCTxPortCntl or CurrState_sofCntl)
begin : sofCntl_NextState
  NextState_sofCntl <= CurrState_sofCntl;
  // Set default values for outputs and signals
  next_HCTxPortReq <= HCTxPortReq;
  next_HCTxPortWEn <= HCTxPortWEn;
  next_HCTxPortData <= HCTxPortData;
  next_HCTxPortCntl <= HCTxPortCntl;
  next_SOFTimer <= SOFTimer;
  case (CurrState_sofCntl)
    3'd0:
      NextState_sofCntl <= 3'd1;
    3'd1:
      if (SOFEnable == 1'b1)	
      begin
        NextState_sofCntl <= 3'd4;
        next_HCTxPortReq <= 1'b1;
      end
    3'd2:
      if (HCTxPortRdy == 1'b1)	
      begin
        NextState_sofCntl <= 3'd5;
        next_HCTxPortWEn <= 1'b1;
        next_HCTxPortData <= 8'h00;
        next_HCTxPortCntl <= 8'h01;
      end
    3'd3:
    begin
      next_HCTxPortReq <= 1'b0;
      if (SOFTimerClr == 1'b1)
        next_SOFTimer <= 16'h0000;
      else
        next_SOFTimer <= SOFTimer + 1'b1;
      if (SOFEnable == 1'b0)	
      begin
        NextState_sofCntl <= 3'd1;
        next_SOFTimer <= 16'h0000;
      end
    end
    3'd4:
      if (HCTxPortGnt == 1'b1)	
        NextState_sofCntl <= 3'd2;
    3'd5:
    begin
      next_HCTxPortWEn <= 1'b0;
      NextState_sofCntl <= 3'd3;
    end
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : sofCntl_CurrentState
  if (rst)	
    CurrState_sofCntl <= 3'd0;
  else
    CurrState_sofCntl <= NextState_sofCntl;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : sofCntl_RegOutput
  if (rst)	
  begin
    SOFTimer <= 16'h0000;
    HCTxPortCntl <= 8'h00;
    HCTxPortData <= 8'h00;
    HCTxPortWEn <= 1'b0;
    HCTxPortReq <= 1'b0;
  end
  else 
  begin
    SOFTimer <= next_SOFTimer;
    HCTxPortCntl <= next_HCTxPortCntl;
    HCTxPortData <= next_HCTxPortData;
    HCTxPortWEn <= next_HCTxPortWEn;
    HCTxPortReq <= next_HCTxPortReq;
  end
end

endmodule