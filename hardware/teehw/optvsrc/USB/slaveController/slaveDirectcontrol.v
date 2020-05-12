module slaveDirectControl (SCTxPortCntl, SCTxPortData, SCTxPortGnt, SCTxPortRdy, SCTxPortReq, SCTxPortWEn, clk, directControlEn, directControlLineState, rst);
input   SCTxPortGnt;
input   SCTxPortRdy;
input   clk;
input   directControlEn;
input   [1:0] directControlLineState;
input   rst;
output  [7:0] SCTxPortCntl;
output  [7:0] SCTxPortData;
output  SCTxPortReq;
output  SCTxPortWEn;

reg     [7:0] SCTxPortCntl, next_SCTxPortCntl;
reg     [7:0] SCTxPortData, next_SCTxPortData;
wire    SCTxPortGnt;
wire    SCTxPortRdy;
reg     SCTxPortReq, next_SCTxPortReq;
reg     SCTxPortWEn, next_SCTxPortWEn;
wire    clk;
wire    directControlEn;
wire    [1:0] directControlLineState;
wire    rst;

reg [2:0] CurrState_slvDrctCntl;
reg [2:0] NextState_slvDrctCntl;

// Diagram actions (continuous assignments allowed only: assign ...)

// diagram ACTION

//--------------------------------------------------------------------
// Machine: slvDrctCntl
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (directControlLineState or directControlEn or SCTxPortGnt or SCTxPortRdy or SCTxPortReq or SCTxPortWEn or SCTxPortData or SCTxPortCntl or CurrState_slvDrctCntl)
begin : slvDrctCntl_NextState
  NextState_slvDrctCntl <= CurrState_slvDrctCntl;
  // Set default values for outputs and signals
  next_SCTxPortReq <= SCTxPortReq;
  next_SCTxPortWEn <= SCTxPortWEn;
  next_SCTxPortData <= SCTxPortData;
  next_SCTxPortCntl <= SCTxPortCntl;
  case (CurrState_slvDrctCntl)
    3'd0:
      NextState_slvDrctCntl <= 3'd1;
    3'd1:
      if (directControlEn == 1'b1)	
      begin
        NextState_slvDrctCntl <= 3'd2;
        next_SCTxPortReq <= 1'b1;
      end
      else
      begin
        NextState_slvDrctCntl <= 3'd6;
        next_SCTxPortReq <= 1'b1;
      end
    3'd2:
      if (SCTxPortGnt == 1'b1)	
        NextState_slvDrctCntl <= 3'd4;
    3'd3:
    begin
      next_SCTxPortWEn <= 1'b0;
      if (directControlEn == 1'b0)	
      begin
        NextState_slvDrctCntl <= 3'd1;
        next_SCTxPortReq <= 1'b0;
      end
      else
        NextState_slvDrctCntl <= 3'd4;
    end
    3'd4:
      if (SCTxPortRdy == 1'b1)	
      begin
        NextState_slvDrctCntl <= 3'd3;
        next_SCTxPortWEn <= 1'b1;
        next_SCTxPortData <= {6'b000000, directControlLineState};
        next_SCTxPortCntl <= 8'h00;
      end
    3'd5:
    begin
      next_SCTxPortWEn <= 1'b0;
      next_SCTxPortReq <= 1'b0;
      NextState_slvDrctCntl <= 3'd1;
    end
    3'd6:
      if (SCTxPortGnt == 1'b1)	
        NextState_slvDrctCntl <= 3'd7;
    3'd7:
      if (SCTxPortRdy == 1'b1)	
      begin
        NextState_slvDrctCntl <= 3'd5;
        next_SCTxPortWEn <= 1'b1;
        next_SCTxPortData <= 8'h00;
        next_SCTxPortCntl <= 8'h05;
      end
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : slvDrctCntl_CurrentState
  if (rst)	
    CurrState_slvDrctCntl <= 3'd0;
  else
    CurrState_slvDrctCntl <= NextState_slvDrctCntl;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : slvDrctCntl_RegOutput
  if (rst)	
  begin
    SCTxPortCntl <= 8'h00;
    SCTxPortData <= 8'h00;
    SCTxPortWEn <= 1'b0;
    SCTxPortReq <= 1'b0;
  end
  else 
  begin
    SCTxPortCntl <= next_SCTxPortCntl;
    SCTxPortData <= next_SCTxPortData;
    SCTxPortWEn <= next_SCTxPortWEn;
    SCTxPortReq <= next_SCTxPortReq;
  end
end

endmodule