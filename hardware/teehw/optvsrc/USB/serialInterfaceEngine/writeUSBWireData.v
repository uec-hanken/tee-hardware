module writeUSBWireData (
  TxBitsIn, 
  TxBitsOut,
   TxDataOutTick,
  TxCtrlIn, 
  TxCtrlOut, 
  USBWireRdy,
  USBWireWEn, 
  TxWireActiveDrive, 
  fullSpeedRate, 
  clk, 
  rst
   );
  
input   [1:0] TxBitsIn;
input   TxCtrlIn;
input   USBWireWEn;
input   clk;
input   fullSpeedRate;
input   rst;
output  [1:0] TxBitsOut;
output TxDataOutTick;
output  TxCtrlOut;
output  USBWireRdy;
output  TxWireActiveDrive;

wire    [1:0] TxBitsIn;
reg     [1:0] TxBitsOut;
reg     TxDataOutTick;
wire    TxCtrlIn;
reg     TxCtrlOut;
reg     USBWireRdy;
wire    USBWireWEn;
wire    clk;
wire    fullSpeedRate;
wire    rst;
reg     TxWireActiveDrive;

// local registers
reg  [3:0]buffer0;
reg  [3:0]buffer1;
reg  [3:0]buffer2;
reg  [3:0]buffer3;
reg  [2:0]bufferCnt;
reg  [1:0]bufferInIndex;
reg  [1:0]bufferOutIndex;
reg decBufferCnt;
reg  [4:0]i;
reg incBufferCnt;
reg fullSpeedTick;
reg lowSpeedTick;
reg fullSpeedRate_reg;

reg [1:0] bufferInStMachCurrState;
reg [1:0] bufferOutStMachCurrState;

// buffer control
always @(posedge clk)
begin
  if (rst == 1'b1)
  begin
    bufferCnt <= 3'b000;
  end
  else
  begin
    if (incBufferCnt == 1'b1 && decBufferCnt == 1'b0)
      bufferCnt <= bufferCnt + 1'b1;
    else if (incBufferCnt == 1'b0 && decBufferCnt == 1'b1)
      bufferCnt <= bufferCnt - 1'b1;
  end
end


//buffer input state machine 
always @(posedge clk) begin
  if (rst == 1'b1) begin
     incBufferCnt <= 1'b0;
    bufferInIndex <= 2'b00;
    buffer0 <= 4'b0000;
    buffer1 <= 4'b0000;
    buffer2 <= 4'b0000;
    buffer3 <= 4'b0000;
    USBWireRdy <= 1'b0;
    bufferInStMachCurrState <= 2'b00;
  end
  else begin
    case (bufferInStMachCurrState)
      2'b00:
      begin
        if (bufferCnt != 3'b100)  
        begin
          bufferInStMachCurrState <= 2'b01;
          USBWireRdy <= 1'b1;
        end
      end
      2'b01:
      begin
        if (USBWireWEn == 1'b1)
        begin
          incBufferCnt <= 1'b1;
          USBWireRdy <= 1'b0;
          bufferInIndex <= bufferInIndex + 1'b1;
          case (bufferInIndex)
            2'b00 : buffer0 <= {fullSpeedRate, TxBitsIn, TxCtrlIn};
            2'b01 : buffer1 <= {fullSpeedRate, TxBitsIn, TxCtrlIn};
            2'b10 : buffer2 <= {fullSpeedRate, TxBitsIn, TxCtrlIn};
            2'b11 : buffer3 <= {fullSpeedRate, TxBitsIn, TxCtrlIn};
          endcase
          bufferInStMachCurrState <= 2'b10;
        end
      end
      2'b10:
      begin
        incBufferCnt <= 1'b0;
        if (bufferCnt != (3'b100 - 1'b1) )  
        begin
          bufferInStMachCurrState <= 2'b01;
          USBWireRdy <= 1'b1;
        end
        else begin
          bufferInStMachCurrState <= 2'b00;
        end
      end
    endcase
  end
end
        
//increment counter used to generate USB bit rate
always @(posedge clk) begin
  if (rst == 1'b1)
  begin
    i <= 5'b00000;
    fullSpeedTick <= 1'b0;
    lowSpeedTick <= 1'b0;
  end
  else
  begin
    i <= i + 1'b1;
    if (i[1:0] == 2'b00)
      fullSpeedTick <= 1'b1;
    else
      fullSpeedTick <= 1'b0; 
    if (i == 5'b00000)
      lowSpeedTick <= 1'b1;
    else
      lowSpeedTick <= 1'b0;
  end
end

//buffer output state machine
//buffer is constantly emptied at either
//the full or low speed rate
//if the buffer is empty, then the output is forced to tri-state
always @(posedge clk) begin
  if (rst == 1'b1)
  begin
    bufferOutIndex <= 2'b00;
    decBufferCnt <= 1'b0;
    TxBitsOut <= 2'b00;
    TxCtrlOut <= 1'b0;
    TxDataOutTick <= 1'b0;
    bufferOutStMachCurrState <= 2'b01;
    fullSpeedRate_reg <= 1'b0;
  end
  else
  begin
    case (bufferOutIndex)
      2'b00: fullSpeedRate_reg <= buffer0[3];
      2'b01: fullSpeedRate_reg <= buffer1[3];
      2'b10: fullSpeedRate_reg <= buffer2[3];
      2'b11: fullSpeedRate_reg <= buffer3[3];
    endcase
    case (bufferOutStMachCurrState)
      2'b01:
      begin
        if ((fullSpeedRate_reg == 1'b1 && fullSpeedTick == 1'b1) || (fullSpeedRate_reg == 1'b0 && lowSpeedTick == 1'b1) )
        begin
          TxDataOutTick <= !TxDataOutTick;
          if (bufferCnt == 0) begin
            TxBitsOut <= 2'b00;
            TxCtrlOut <= 1'b0;
          end
          else begin
            bufferOutStMachCurrState <= 2'b10;
            decBufferCnt <= 1'b1;
            bufferOutIndex <= bufferOutIndex + 1'b1;
            case (bufferOutIndex)
              2'b00 :
            begin 
              TxBitsOut <= buffer0[2:1];
              TxCtrlOut <= buffer0[0];
            end
            2'b01 : 
            begin
              TxBitsOut <= buffer1[2:1];
              TxCtrlOut <= buffer1[0];
            end
            2'b10 : 
            begin 
              TxBitsOut <= buffer2[2:1];
              TxCtrlOut <= buffer2[0];
            end
            2'b11 : 
            begin
              TxBitsOut <= buffer3[2:1];
              TxCtrlOut <= buffer3[0];
            end
            endcase
          end
        end
      end
      2'b10:
      begin
        decBufferCnt <= 1'b0;
        bufferOutStMachCurrState <= 2'b01;
      end
    endcase
  end
end

// control 'TxWireActiveDrive' 
always @(TxCtrlOut)
begin  
  if (TxCtrlOut == 1'b1)
    TxWireActiveDrive <= 1'b1;
  else
    TxWireActiveDrive <= 1'b0;
end


endmodule
