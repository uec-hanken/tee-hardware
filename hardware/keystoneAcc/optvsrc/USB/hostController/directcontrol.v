module directControl (HCTxPortCntl, HCTxPortData, HCTxPortGnt, HCTxPortRdy, HCTxPortReq, HCTxPortWEn, clk, directControlEn, directControlLineState, rst);
input   HCTxPortGnt;
input   HCTxPortRdy;
input   clk;
input   directControlEn;
input   [1:0] directControlLineState;
input   rst;
output  [7:0] HCTxPortCntl;
output  [7:0] HCTxPortData;
output  HCTxPortReq;
output  HCTxPortWEn;

reg     [7:0] HCTxPortCntl, next_HCTxPortCntl;
reg     [7:0] HCTxPortData, next_HCTxPortData;
wire    HCTxPortGnt;
wire    HCTxPortRdy;
reg     HCTxPortReq, next_HCTxPortReq;
reg     HCTxPortWEn, next_HCTxPortWEn;
wire    clk;
wire    directControlEn;
wire    [1:0] directControlLineState;
wire    rst;

reg [2:0] CurrState_drctCntl;
reg [2:0] NextState_drctCntl;

// Diagram actions (continuous assignments allowed only: assign ...)

// diagram ACTION

//--------------------------------------------------------------------
// Machine: drctCntl
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (directControlLineState or directControlEn or HCTxPortGnt or HCTxPortRdy or HCTxPortReq or HCTxPortWEn or HCTxPortData or HCTxPortCntl or CurrState_drctCntl)
begin : drctCntl_NextState
  NextState_drctCntl <= CurrState_drctCntl;
  // Set default values for outputs and signals
  next_HCTxPortReq <= HCTxPortReq;
  next_HCTxPortWEn <= HCTxPortWEn;
  next_HCTxPortData <= HCTxPortData;
  next_HCTxPortCntl <= HCTxPortCntl;
  case (CurrState_drctCntl)
    3'd0:
      NextState_drctCntl <= 3'd1;
    3'd1:
      if (directControlEn == 1'b1)	
      begin
        NextState_drctCntl <= 3'd2;
        next_HCTxPortReq <= 1'b1;
      end
      else
      begin
        NextState_drctCntl <= 3'd6;
        next_HCTxPortReq <= 1'b1;
      end
    3'd2:
      if (HCTxPortGnt == 1'b1)	
        NextState_drctCntl <= 3'd4;
    3'd3:
    begin
      next_HCTxPortWEn <= 1'b0;
      if (directControlEn == 1'b0)	
      begin
        NextState_drctCntl <= 3'd1;
        next_HCTxPortReq <= 1'b0;
      end
      else
        NextState_drctCntl <= 3'd4;
    end
    3'd4:
      if (HCTxPortRdy == 1'b1)	
      begin
        NextState_drctCntl <= 3'd3;
        next_HCTxPortWEn <= 1'b1;
        next_HCTxPortData <= {6'b000000, directControlLineState};
        next_HCTxPortCntl <= 8'h00;
      end
    3'd5:
    begin
      next_HCTxPortWEn <= 1'b0;
      next_HCTxPortReq <= 1'b0;
      NextState_drctCntl <= 3'd1;
    end
    3'd6:
      if (HCTxPortGnt == 1'b1)	
        NextState_drctCntl <= 3'd7;
    3'd7:
      if (HCTxPortRdy == 1'b1)	
      begin
        NextState_drctCntl <= 3'd5;
        next_HCTxPortWEn <= 1'b1;
        next_HCTxPortData <= 8'h00;
        next_HCTxPortCntl <= 8'h05;
      end
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : drctCntl_CurrentState
  if (rst)	
    CurrState_drctCntl <= 3'd0;
  else
    CurrState_drctCntl <= NextState_drctCntl;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : drctCntl_RegOutput
  if (rst)	
  begin
    HCTxPortCntl <= 8'h00;
    HCTxPortData <= 8'h00;
    HCTxPortWEn <= 1'b0;
    HCTxPortReq <= 1'b0;
  end
  else 
  begin
    HCTxPortCntl <= next_HCTxPortCntl;
    HCTxPortData <= next_HCTxPortData;
    HCTxPortWEn <= next_HCTxPortWEn;
    HCTxPortReq <= next_HCTxPortReq;
  end
end

endmodule