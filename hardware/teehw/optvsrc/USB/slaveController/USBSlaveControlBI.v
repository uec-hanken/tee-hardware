module USBSlaveControlBI (address, dataIn, dataOut, writeEn,
  strobe_i,
  busClk, 
  rstSyncToBusClk,
  usbClk, 
  rstSyncToUsbClk,
  SOFRxedIntOut, resetEventIntOut, resumeIntOut, transDoneIntOut, NAKSentIntOut, vBusDetIntOut,
  endP0TransTypeReg, endP0NAKTransTypeReg,
  endP1TransTypeReg, endP1NAKTransTypeReg,
  endP2TransTypeReg, endP2NAKTransTypeReg,
  endP3TransTypeReg, endP3NAKTransTypeReg,
  endP0ControlReg,
  endP1ControlReg,
  endP2ControlReg,
  endP3ControlReg,
  EP0StatusReg,
  EP1StatusReg,
  EP2StatusReg,
  EP3StatusReg,
  SCAddrReg, frameNum,
  connectStateIn,
  vBusDetectIn,
  SOFRxedIn, resetEventIn, resumeIntIn, transDoneIn, NAKSentIn,
  slaveControlSelect,
  clrEP0Ready, clrEP1Ready, clrEP2Ready, clrEP3Ready,
  TxLineState,
  LineDirectControlEn,
  fullSpeedPol, 
  fullSpeedRate,
  connectSlaveToHost,
  SCGlobalEn
  );
input [4:0] address;
input [7:0] dataIn;
input writeEn; 
input strobe_i;
input busClk; 
input rstSyncToBusClk;
input usbClk; 
input rstSyncToUsbClk;
output [7:0] dataOut;
output SOFRxedIntOut;
output resetEventIntOut;
output resumeIntOut;
output transDoneIntOut;
output NAKSentIntOut;
output vBusDetIntOut;

input [1:0] endP0TransTypeReg;
input [1:0] endP0NAKTransTypeReg;
input [1:0] endP1TransTypeReg; 
input [1:0] endP1NAKTransTypeReg;
input [1:0] endP2TransTypeReg; 
input [1:0] endP2NAKTransTypeReg;
input [1:0] endP3TransTypeReg; 
input [1:0] endP3NAKTransTypeReg;
output [4:0] endP0ControlReg;
output [4:0] endP1ControlReg;
output [4:0] endP2ControlReg;
output [4:0] endP3ControlReg;
input [7:0] EP0StatusReg;
input [7:0] EP1StatusReg;
input [7:0] EP2StatusReg;
input [7:0] EP3StatusReg;
output [6:0] SCAddrReg;
input [10:0] frameNum;
input [1:0] connectStateIn;
input vBusDetectIn;
input SOFRxedIn;
input resetEventIn;
input resumeIntIn;
input transDoneIn;
input NAKSentIn;
input slaveControlSelect;
input clrEP0Ready;
input clrEP1Ready;
input clrEP2Ready;
input clrEP3Ready;
output [1:0] TxLineState;
output LineDirectControlEn;
output fullSpeedPol; 
output fullSpeedRate;
output connectSlaveToHost;
output SCGlobalEn;

wire [4:0] address;
wire [7:0] dataIn;
wire writeEn;
wire strobe_i;
wire busClk; 
wire rstSyncToBusClk;
wire usbClk; 
wire rstSyncToUsbClk;
reg [7:0] dataOut;

reg SOFRxedIntOut;
reg resetEventIntOut;
reg resumeIntOut;
reg transDoneIntOut;
reg NAKSentIntOut;
reg vBusDetIntOut;

wire [1:0] endP0TransTypeReg;
wire [1:0] endP0NAKTransTypeReg;
wire [1:0] endP1TransTypeReg; 
wire [1:0] endP1NAKTransTypeReg;
wire [1:0] endP2TransTypeReg; 
wire [1:0] endP2NAKTransTypeReg;
wire [1:0] endP3TransTypeReg; 
wire [1:0] endP3NAKTransTypeReg;
reg [4:0] endP0ControlReg;
reg [4:0] endP0ControlReg1;
reg [4:0] endP1ControlReg;
reg [4:0] endP1ControlReg1;
reg [4:0] endP2ControlReg;
reg [4:0] endP2ControlReg1;
reg [4:0] endP3ControlReg;
reg [4:0] endP3ControlReg1;
wire [7:0] EP0StatusReg;
wire [7:0] EP1StatusReg;
wire [7:0] EP2StatusReg;
wire [7:0] EP3StatusReg;
reg [6:0] SCAddrReg;
reg [3:0] TxEndPReg;
wire [10:0] frameNum;
wire [1:0] connectStateIn;

wire SOFRxedIn;
wire resetEventIn;
wire resumeIntIn;
wire transDoneIn;
wire NAKSentIn;
wire slaveControlSelect;
wire clrEP0Ready;
wire clrEP1Ready;
wire clrEP2Ready;
wire clrEP3Ready;
reg [1:0] TxLineState;
reg [1:0] TxLineState_reg1;
reg LineDirectControlEn;
reg LineDirectControlEn_reg1;
reg fullSpeedPol; 
reg fullSpeedPol_reg1; 
reg fullSpeedRate;
reg fullSpeedRate_reg1;
reg connectSlaveToHost;
reg connectSlaveToHost_reg1;
reg SCGlobalEn;
reg SCGlobalEn_reg1;

//internal wire and regs
reg [6:0] SCControlReg;
reg clrVBusDetReq;
reg clrNAKReq;
reg clrSOFReq;
reg clrResetReq;
reg clrResInReq;
reg clrTransDoneReq;
reg SOFRxedInt;
reg resetEventInt;
reg resumeInt;
reg transDoneInt;
reg vBusDetInt;
reg NAKSentInt;
reg [5:0] interruptMaskReg;
reg EP0SetReady;
reg EP1SetReady;
reg EP2SetReady;
reg EP3SetReady;
reg EP0SendStall;
reg EP1SendStall;
reg EP2SendStall;
reg EP3SendStall;
reg EP0IsoEn;
reg EP1IsoEn;
reg EP2IsoEn;
reg EP3IsoEn;
reg EP0DataSequence;
reg EP1DataSequence;
reg EP2DataSequence;
reg EP3DataSequence;
reg EP0Enable;
reg EP1Enable;
reg EP2Enable;
reg EP3Enable;
reg EP0Ready;
reg EP1Ready;
reg EP2Ready;
reg EP3Ready;
reg [2:0] SOFRxedInExtend;
reg [2:0] resetEventInExtend;
reg [2:0] resumeIntInExtend;
reg [2:0] transDoneInExtend;
reg [2:0] NAKSentInExtend;
reg [2:0] clrEP0ReadyExtend;
reg [2:0] clrEP1ReadyExtend;
reg [2:0] clrEP2ReadyExtend;
reg [2:0] clrEP3ReadyExtend;


//clock domain crossing sync registers
//STB = Sync To Busclk
reg [4:0] endP0ControlRegSTB;
reg [4:0] endP1ControlRegSTB;
reg [4:0] endP2ControlRegSTB;
reg [4:0] endP3ControlRegSTB;
reg [2:0] NAKSentInSTB;
reg [2:0] SOFRxedInSTB;
reg [2:0] resetEventInSTB;
reg [2:0] resumeIntInSTB;
reg [2:0] transDoneInSTB;
reg [2:0] clrEP0ReadySTB;
reg [2:0] clrEP1ReadySTB;
reg [2:0] clrEP2ReadySTB;
reg [2:0] clrEP3ReadySTB;
reg SCGlobalEnSTB;
reg [1:0] TxLineStateSTB;
reg LineDirectControlEnSTB;
reg fullSpeedPolSTB; 
reg fullSpeedRateSTB;
reg connectSlaveToHostSTB;
reg [7:0] EP0StatusRegSTB;
reg [7:0] EP0StatusRegSTB_reg1;
reg [7:0] EP1StatusRegSTB;
reg [7:0] EP1StatusRegSTB_reg1;
reg [7:0] EP2StatusRegSTB;
reg [7:0] EP2StatusRegSTB_reg1;
reg [7:0] EP3StatusRegSTB;
reg [7:0] EP3StatusRegSTB_reg1;
reg [1:0] endP0TransTypeRegSTB;
reg [1:0] endP0TransTypeRegSTB_reg1;
reg [1:0] endP0NAKTransTypeRegSTB;
reg [1:0] endP0NAKTransTypeRegSTB_reg1;
reg [1:0] endP1TransTypeRegSTB; 
reg [1:0] endP1TransTypeRegSTB_reg1; 
reg [1:0] endP1NAKTransTypeRegSTB;
reg [1:0] endP1NAKTransTypeRegSTB_reg1;
reg [1:0] endP2TransTypeRegSTB; 
reg [1:0] endP2TransTypeRegSTB_reg1; 
reg [1:0] endP2NAKTransTypeRegSTB;
reg [1:0] endP2NAKTransTypeRegSTB_reg1;
reg [1:0] endP3TransTypeRegSTB; 
reg [1:0] endP3TransTypeRegSTB_reg1; 
reg [1:0] endP3NAKTransTypeRegSTB;
reg [1:0] endP3NAKTransTypeRegSTB_reg1;
reg [10:0] frameNumSTB;
reg [10:0] frameNumSTB_reg1;
reg [2:0] vBusDetectInSTB;
reg [1:0] connectStateInSTB;
reg [1:0] connectStateInSTB_reg1;

  
//sync write demux
always @(posedge busClk)
begin   
  if (rstSyncToBusClk == 1'b1) begin
    EP0IsoEn <= 1'b0;
    EP0SendStall <= 1'b0;
    EP0DataSequence <= 1'b0;
    EP0Enable <= 1'b0;
    EP1IsoEn <= 1'b0;
    EP1SendStall <= 1'b0;
    EP1DataSequence <= 1'b0;
    EP1Enable <= 1'b0;
    EP2IsoEn <= 1'b0;
    EP2SendStall <= 1'b0;
    EP2DataSequence <= 1'b0;
    EP2Enable <= 1'b0;
    EP3IsoEn <= 1'b0;
    EP3SendStall <= 1'b0;
    EP3DataSequence <= 1'b0;
    EP3Enable <= 1'b0;
    SCControlReg <= 7'h00;
    SCAddrReg <= 7'h00;
    interruptMaskReg <= 6'h00;
  end
  else begin
    clrVBusDetReq <= 1'b0;
    clrNAKReq <= 1'b0;
    clrSOFReq <= 1'b0;
    clrResetReq <= 1'b0;
    clrResInReq <= 1'b0;
    clrTransDoneReq <= 1'b0;
    EP0SetReady <= 1'b0;
    EP1SetReady <= 1'b0;
    EP2SetReady <= 1'b0;
    EP3SetReady <= 1'b0;
    if (writeEn == 1'b1 && strobe_i == 1'b1 && slaveControlSelect == 1'b1)
    begin
      case (address)
        5'd0 : begin
          EP0IsoEn <= dataIn[4];
          EP0SendStall <= dataIn[3];
          EP0DataSequence <= dataIn[2];
          EP0SetReady <= dataIn[1];
          EP0Enable <= dataIn[0];
        end
        5'd4 : begin
          EP1IsoEn <= dataIn[4];
          EP1SendStall <= dataIn[3];
          EP1DataSequence <= dataIn[2];
          EP1SetReady <= dataIn[1];
          EP1Enable <= dataIn[0];
        end
        5'd8 : begin
          EP2IsoEn <= dataIn[4];
          EP2SendStall <= dataIn[3];
          EP2DataSequence <= dataIn[2];
          EP2SetReady <= dataIn[1];
          EP2Enable <= dataIn[0];
        end
        5'd12 : begin
          EP3IsoEn <= dataIn[4];
          EP3SendStall <= dataIn[3];
          EP3DataSequence <= dataIn[2];
          EP3SetReady <= dataIn[1];
          EP3Enable <= dataIn[0];
        end
        5'd16 : SCControlReg <= dataIn[6:0];
        5'd20 : SCAddrReg <= dataIn[6:0];
        5'd18 : begin
          clrVBusDetReq <= dataIn[5];
          clrNAKReq <= dataIn[4];
          clrSOFReq <= dataIn[3];
          clrResetReq <= dataIn[2];
          clrResInReq <= dataIn[1];
          clrTransDoneReq <= dataIn[0];
        end
        5'd19  : interruptMaskReg <= dataIn[5:0];
      endcase
    end
  end
end

//interrupt control 
always @(posedge busClk)
begin
  if (rstSyncToBusClk == 1'b1) begin
    vBusDetInt <= 1'b0;
    NAKSentInt <= 1'b0;
    SOFRxedInt <= 1'b0;
    resetEventInt <= 1'b0;
    resumeInt <= 1'b0;
    transDoneInt <= 1'b0;
  end
  else begin
    if (vBusDetectInSTB[0] != vBusDetectInSTB[1])
      vBusDetInt <= 1'b1;
    else if (clrVBusDetReq == 1'b1)
      vBusDetInt <= 1'b0; 

    if (NAKSentInSTB[1] == 1'b1 && NAKSentInSTB[0] == 1'b0)
      NAKSentInt <= 1'b1;
    else if (clrNAKReq == 1'b1)
      NAKSentInt <= 1'b0; 
    
    if (SOFRxedInSTB[1] == 1'b1 && SOFRxedInSTB[0] == 1'b0)
      SOFRxedInt <= 1'b1;
    else if (clrSOFReq == 1'b1)
      SOFRxedInt <= 1'b0;
    
    if (resetEventInSTB[1] == 1'b1 && resetEventInSTB[0] == 1'b0)
      resetEventInt <= 1'b1;
    else if (clrResetReq == 1'b1)
      resetEventInt <= 1'b0;
    
    if (resumeIntInSTB[1] == 1'b1 && resumeIntInSTB[0] == 1'b0)
      resumeInt <= 1'b1;
    else if (clrResInReq == 1'b1)
      resumeInt <= 1'b0;

    if (transDoneInSTB[1] == 1'b1 && transDoneInSTB[0] == 1'b0)
      transDoneInt <= 1'b1;
    else if (clrTransDoneReq == 1'b1)
      transDoneInt <= 1'b0;
  end
end

//mask interrupts
always @(*) begin
  transDoneIntOut <= transDoneInt & interruptMaskReg[0];
  resumeIntOut <= resumeInt & interruptMaskReg[1];
  resetEventIntOut <= resetEventInt & interruptMaskReg[2];
  SOFRxedIntOut <= SOFRxedInt & interruptMaskReg[3];
  NAKSentIntOut <= NAKSentInt & interruptMaskReg[4];
  vBusDetIntOut <= vBusDetInt & interruptMaskReg[5];
end  

//end point ready, set/clear
//Since 'busClk' can be a higher freq than 'usbClk',
//'EP0SetReady' etc must be delayed with respect to other control signals, thus
//ensuring that control signals have been clocked through to 'usbClk' clock
//domain before the ready is asserted.
//Not sure this is required because there is at least two 'usbClk' ticks between
//detection of 'EP0Ready' and sampling of related control signals.
always @(posedge busClk)
begin
  if (rstSyncToBusClk == 1'b1) begin
    EP0Ready <= 1'b0;
    EP1Ready <= 1'b0;
    EP2Ready <= 1'b0;
    EP3Ready <= 1'b0;
  end
  else begin
    if (EP0SetReady == 1'b1)
      EP0Ready <= 1'b1;
    else if (clrEP0ReadySTB[1] == 1'b1 && clrEP0ReadySTB[0] == 1'b0)
      EP0Ready <= 1'b0;
    
    if (EP1SetReady == 1'b1)
      EP1Ready <= 1'b1;
    else if (clrEP1ReadySTB[1] == 1'b1 && clrEP1ReadySTB[0] == 1'b0)
      EP1Ready <= 1'b0;
    
    if (EP2SetReady == 1'b1)
      EP2Ready <= 1'b1;
    else if (clrEP2ReadySTB[1] == 1'b1 && clrEP2ReadySTB[0] == 1'b0)
      EP2Ready <= 1'b0;
    
    if (EP3SetReady == 1'b1)
      EP3Ready <= 1'b1;
    else if (clrEP3ReadySTB[1] == 1'b1 && clrEP3ReadySTB[0] == 1'b0)
      EP3Ready <= 1'b0;
  end
end  
  
//break out control signals
always @(SCControlReg) begin
  SCGlobalEnSTB <= SCControlReg[0];
  TxLineStateSTB <= SCControlReg[2:1];
  LineDirectControlEnSTB <= SCControlReg[3];
  fullSpeedPolSTB <= SCControlReg[4]; 
  fullSpeedRateSTB <= SCControlReg[5];
  connectSlaveToHostSTB <= SCControlReg[6];
end

//combine endpoint control signals 
always @(*) 
begin
  endP0ControlRegSTB <= {EP0IsoEn, EP0SendStall, EP0DataSequence, EP0Ready, EP0Enable};
  endP1ControlRegSTB <= {EP1IsoEn, EP1SendStall, EP1DataSequence, EP1Ready, EP1Enable};
  endP2ControlRegSTB <= {EP2IsoEn, EP2SendStall, EP2DataSequence, EP2Ready, EP2Enable};
  endP3ControlRegSTB <= {EP3IsoEn, EP3SendStall, EP3DataSequence, EP3Ready, EP3Enable};
end
      
      
// async read mux
always @(*)
begin
  case (address)
      5'd0 : dataOut <= endP0ControlRegSTB;
      5'd1 : dataOut <= EP0StatusRegSTB;
      5'd2 : dataOut <= endP0TransTypeRegSTB;
      5'd3 : dataOut <= endP0NAKTransTypeRegSTB;
      5'd4 : dataOut <= endP1ControlRegSTB;
      5'd5 :  dataOut <= EP1StatusRegSTB;
      5'd6 : dataOut <= endP1TransTypeRegSTB;
      5'd7 : dataOut <= endP1NAKTransTypeRegSTB;
      5'd8 : dataOut <= endP2ControlRegSTB;
      5'd9 :  dataOut <= EP2StatusRegSTB;
      5'd10 : dataOut <= endP2TransTypeRegSTB;
      5'd11 : dataOut <= endP2NAKTransTypeRegSTB;
      5'd12 : dataOut <= endP3ControlRegSTB;
      5'd13 :  dataOut <= EP3StatusRegSTB;
      5'd14 : dataOut <= endP3TransTypeRegSTB;
      5'd15 : dataOut <= endP3NAKTransTypeRegSTB;
      5'd16 : dataOut <= SCControlReg;
      5'd17 : dataOut <= {5'b00000, vBusDetectInSTB[0], connectStateInSTB}; 
      5'd18 :  dataOut <= {2'b00, vBusDetInt, NAKSentInt, SOFRxedInt, resetEventInt, resumeInt, transDoneInt};
      5'd19  : dataOut <= {2'b00, interruptMaskReg};
      5'd20 : dataOut <= {1'b0, SCAddrReg};
      5'd21 : dataOut <= {5'b00000, frameNumSTB[10:8]};
      5'd22 : dataOut <= frameNumSTB[7:0];
      default: dataOut <= 8'h00;
  endcase
end


//Extend SOFRxedIn, resetEventIn, resumeIntIn, transDoneIn, NAKSentIn from 1 tick
//pulses to 3 tick pulses
always @(posedge usbClk) begin
  if (rstSyncToUsbClk == 1'b1) begin
    SOFRxedInExtend <= 3'b000;
    resetEventInExtend <= 3'b000;
    resumeIntInExtend <= 3'b000;
    transDoneInExtend <= 3'b000;
    NAKSentInExtend <= 3'b000;
    clrEP0ReadyExtend <= 3'b000;
    clrEP1ReadyExtend <= 3'b000;
    clrEP2ReadyExtend <= 3'b000;
    clrEP3ReadyExtend <= 3'b000;
  end
  else begin
    if (SOFRxedIn == 1'b1)
      SOFRxedInExtend <= 3'b111;
    else
      SOFRxedInExtend <= {1'b0, SOFRxedInExtend[2:1]};
    if (resetEventIn == 1'b1)
      resetEventInExtend <= 3'b111;
    else
      resetEventInExtend <= {1'b0, resetEventInExtend[2:1]};
    if (resumeIntIn == 1'b1)
      resumeIntInExtend <= 3'b111;
    else
      resumeIntInExtend <= {1'b0, resumeIntInExtend[2:1]};
    if (transDoneIn == 1'b1)
      transDoneInExtend <= 3'b111;
    else
      transDoneInExtend <= {1'b0, transDoneInExtend[2:1]};
    if (NAKSentIn == 1'b1)
      NAKSentInExtend <= 3'b111;
    else
      NAKSentInExtend <= {1'b0, NAKSentInExtend[2:1]};
    if (clrEP0Ready == 1'b1)
      clrEP0ReadyExtend <= 3'b111;
    else
      clrEP0ReadyExtend <= {1'b0, clrEP0ReadyExtend[2:1]};
    if (clrEP1Ready == 1'b1)
      clrEP1ReadyExtend <= 3'b111;
    else
      clrEP1ReadyExtend <= {1'b0, clrEP1ReadyExtend[2:1]};
    if (clrEP2Ready == 1'b1)
      clrEP2ReadyExtend <= 3'b111;
    else
      clrEP2ReadyExtend <= {1'b0, clrEP2ReadyExtend[2:1]};
    if (clrEP3Ready == 1'b1)
      clrEP3ReadyExtend <= 3'b111;
    else
      clrEP3ReadyExtend <= {1'b0, clrEP3ReadyExtend[2:1]};
  end
end

//re-sync from busClk to usbClk. 
always @(posedge usbClk) begin
  if (rstSyncToUsbClk == 1'b1) begin
    endP0ControlReg <= {5{1'b0}};
    endP0ControlReg1 <= {5{1'b0}};
    endP1ControlReg <= {5{1'b0}};
    endP1ControlReg1 <= {5{1'b0}};
    endP2ControlReg <= {5{1'b0}};
    endP2ControlReg1 <= {5{1'b0}};
    endP3ControlReg <= {5{1'b0}};
    endP3ControlReg1 <= {5{1'b0}};
    SCGlobalEn <= 1'b0;
    SCGlobalEn_reg1 <= 1'b0;
    TxLineState <= 2'b00;
    TxLineState_reg1 <= 2'b00;
    LineDirectControlEn <= 1'b0;
    LineDirectControlEn_reg1 <= 1'b0;
    fullSpeedPol <= 1'b0;
    fullSpeedPol_reg1 <= 1'b0;
    fullSpeedRate <= 1'b0;
    fullSpeedRate_reg1 <= 1'b0;
    connectSlaveToHost <= 1'b0;
    connectSlaveToHost_reg1 <= 1'b0;
  end
  else begin
    endP0ControlReg1 <= endP0ControlRegSTB;
    endP0ControlReg <= endP0ControlReg1;
    endP1ControlReg1 <= endP1ControlRegSTB;
    endP1ControlReg <= endP1ControlReg1;
    endP2ControlReg1 <= endP2ControlRegSTB;
    endP2ControlReg <= endP2ControlReg1;
    endP3ControlReg1 <= endP3ControlRegSTB;
    endP3ControlReg <= endP3ControlReg1;
    SCGlobalEn_reg1 <= SCGlobalEnSTB;
    SCGlobalEn <= SCGlobalEn_reg1;
    TxLineState_reg1 <= TxLineStateSTB;
    TxLineState <= TxLineState_reg1;
    LineDirectControlEn_reg1 <= LineDirectControlEnSTB;
    LineDirectControlEn <= LineDirectControlEn_reg1;
    fullSpeedPol_reg1 <= fullSpeedPolSTB; 
    fullSpeedPol <= fullSpeedPol_reg1; 
    fullSpeedRate_reg1 <= fullSpeedRateSTB;
    fullSpeedRate <= fullSpeedRate_reg1;
    connectSlaveToHost_reg1 <= connectSlaveToHostSTB;
    connectSlaveToHost <= connectSlaveToHost_reg1;
  end
end

//re-sync from usbClk and async inputs to busClk. Since 'NAKSentIn', 'SOFRxedIn' etc 
//are only asserted for 3 usbClk ticks
//busClk freq must be greater than usbClk/3 (plus some allowance for setup and hold) freq
always @(posedge busClk) begin
  if (rstSyncToBusClk == 1'b1) begin
    vBusDetectInSTB <= 3'b000;
    NAKSentInSTB <= 3'b000;
    SOFRxedInSTB <= 3'b000;
    resetEventInSTB <= 3'b000;
    resumeIntInSTB <= 3'b000;
    transDoneInSTB <= 3'b000;
    clrEP0ReadySTB <= 3'b000;
    clrEP1ReadySTB <= 3'b000;
    clrEP2ReadySTB <= 3'b000;
    clrEP3ReadySTB <= 3'b000;
    EP0StatusRegSTB <= 8'h00;
    EP0StatusRegSTB_reg1 <= 8'h00;
    EP1StatusRegSTB <= 8'h00;
    EP1StatusRegSTB_reg1 <= 8'h00;
    EP2StatusRegSTB <= 8'h00;
    EP2StatusRegSTB_reg1 <= 8'h00;
    EP3StatusRegSTB <= 8'h00;
    EP3StatusRegSTB_reg1 <= 8'h00;
    endP0TransTypeRegSTB <= 2'b00;
    endP0TransTypeRegSTB_reg1 <= 2'b00;
    endP1TransTypeRegSTB <= 2'b00;
    endP1TransTypeRegSTB_reg1 <= 2'b00;
    endP2TransTypeRegSTB <= 2'b00;
    endP2TransTypeRegSTB_reg1 <= 2'b00;
    endP3TransTypeRegSTB <= 2'b00;
    endP3TransTypeRegSTB_reg1 <= 2'b00;
    endP0NAKTransTypeRegSTB <= 2'b00;
    endP0NAKTransTypeRegSTB_reg1 <= 2'b00;
    endP1NAKTransTypeRegSTB <= 2'b00;
    endP1NAKTransTypeRegSTB_reg1 <= 2'b00;
    endP2NAKTransTypeRegSTB <= 2'b00;
    endP2NAKTransTypeRegSTB_reg1 <= 2'b00;
    endP3NAKTransTypeRegSTB <= 2'b00;
    endP3NAKTransTypeRegSTB_reg1 <= 2'b00;
    frameNumSTB <= {11{1'b0}};
    frameNumSTB_reg1 <= {11{1'b0}};
    connectStateInSTB <= 2'b00;
    connectStateInSTB_reg1 <= 2'b00;
  end
  else begin
    vBusDetectInSTB <= {vBusDetectIn, vBusDetectInSTB[2:1]};
    NAKSentInSTB <= {NAKSentInExtend[0], NAKSentInSTB[2:1]};
    SOFRxedInSTB <= {SOFRxedInExtend[0], SOFRxedInSTB[2:1]};
    resetEventInSTB <= {resetEventInExtend[0], resetEventInSTB[2:1]};
    resumeIntInSTB <= {resumeIntInExtend[0], resumeIntInSTB[2:1]};
    transDoneInSTB <= {transDoneInExtend[0], transDoneInSTB[2:1]};
    clrEP0ReadySTB <= {clrEP0ReadyExtend[0], clrEP0ReadySTB[2:1]};
    clrEP1ReadySTB <= {clrEP1ReadyExtend[0], clrEP1ReadySTB[2:1]};
    clrEP2ReadySTB <= {clrEP2ReadyExtend[0], clrEP2ReadySTB[2:1]};
    clrEP3ReadySTB <= {clrEP3ReadyExtend[0], clrEP3ReadySTB[2:1]};
    EP0StatusRegSTB_reg1 <= EP0StatusReg;
    EP0StatusRegSTB <= EP0StatusRegSTB_reg1;
    EP1StatusRegSTB_reg1 <= EP1StatusReg;
    EP1StatusRegSTB <= EP1StatusRegSTB_reg1;
    EP2StatusRegSTB_reg1 <= EP2StatusReg;
    EP2StatusRegSTB <= EP2StatusRegSTB_reg1;
    EP3StatusRegSTB_reg1 <= EP3StatusReg;
    EP3StatusRegSTB <= EP3StatusRegSTB_reg1;
    endP0TransTypeRegSTB_reg1 <= endP0TransTypeReg;
    endP0TransTypeRegSTB <= endP0TransTypeRegSTB_reg1;
    endP1TransTypeRegSTB_reg1 <= endP1TransTypeReg;
    endP1TransTypeRegSTB <= endP1TransTypeRegSTB_reg1;
    endP2TransTypeRegSTB_reg1 <= endP2TransTypeReg;
    endP2TransTypeRegSTB <= endP2TransTypeRegSTB_reg1;
    endP3TransTypeRegSTB_reg1 <= endP3TransTypeReg;
    endP3TransTypeRegSTB <= endP3TransTypeRegSTB_reg1;
    endP0NAKTransTypeRegSTB_reg1 <= endP0NAKTransTypeReg;
    endP0NAKTransTypeRegSTB <= endP0NAKTransTypeRegSTB_reg1;
    endP1NAKTransTypeRegSTB_reg1 <= endP1NAKTransTypeReg;
    endP1NAKTransTypeRegSTB <= endP1NAKTransTypeRegSTB_reg1;
    endP2NAKTransTypeRegSTB_reg1 <= endP2NAKTransTypeReg;
    endP2NAKTransTypeRegSTB <= endP2NAKTransTypeRegSTB_reg1;
    endP3NAKTransTypeRegSTB_reg1 <= endP3NAKTransTypeReg;
    endP3NAKTransTypeRegSTB <= endP3NAKTransTypeRegSTB_reg1;
    frameNumSTB_reg1 <= frameNum;
    frameNumSTB <= frameNumSTB_reg1;
    connectStateInSTB_reg1 <= connectStateIn;
    connectStateInSTB <= connectStateInSTB_reg1;
  end
end


endmodule
