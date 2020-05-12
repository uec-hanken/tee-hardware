module slavecontroller (CRCError, NAKSent, RxByte, RxDataWEn, RxOverflow, RxStatus, RxTimeOut, SCGlobalEn, SOFRxed, USBEndPControlReg, USBEndPNakTransTypeReg, USBEndPTransTypeReg, USBEndP, USBTgtAddress, bitStuffError, clk, clrEPRdy, endPMuxErrorsWEn, endPointReadyToGetPkt, frameNum, getPacketREn, getPacketRdy, rst, sendPacketPID, sendPacketRdy, sendPacketWEn, stallSent, transDone);
input   CRCError;
input   [7:0] RxByte;
input   RxDataWEn;
input   RxOverflow;
input   [7:0] RxStatus;
input   RxTimeOut;
input   SCGlobalEn;
input   [4:0] USBEndPControlReg;
input   [6:0] USBTgtAddress;
input   bitStuffError;
input   clk;
input   getPacketRdy;
input   rst;
input   sendPacketRdy;
output  NAKSent;
output  SOFRxed;
output  [1:0] USBEndPNakTransTypeReg;
output  [1:0] USBEndPTransTypeReg;
output  [3:0] USBEndP;
output  clrEPRdy;
output  endPMuxErrorsWEn;
output  endPointReadyToGetPkt;
output  [10:0] frameNum;
output  getPacketREn;
output  [3:0] sendPacketPID;
output  sendPacketWEn;
output  stallSent;
output  transDone;

wire    CRCError;
reg     NAKSent, next_NAKSent;
wire    [7:0] RxByte;
wire    RxDataWEn;
wire    RxOverflow;
wire    [7:0] RxStatus;
wire    RxTimeOut;
wire    SCGlobalEn;
reg     SOFRxed, next_SOFRxed;
wire    [4:0] USBEndPControlReg;
reg     [1:0] USBEndPNakTransTypeReg, next_USBEndPNakTransTypeReg;
reg     [1:0] USBEndPTransTypeReg, next_USBEndPTransTypeReg;
reg     [3:0] USBEndP, next_USBEndP;
wire    [6:0] USBTgtAddress;
wire    bitStuffError;
wire    clk;
reg     clrEPRdy, next_clrEPRdy;
reg     endPMuxErrorsWEn, next_endPMuxErrorsWEn;
reg     endPointReadyToGetPkt, next_endPointReadyToGetPkt;
reg     [10:0] frameNum, next_frameNum;
reg     getPacketREn, next_getPacketREn;
wire    getPacketRdy;
wire    rst;
reg     [3:0] sendPacketPID, next_sendPacketPID;
wire    sendPacketRdy;
reg     sendPacketWEn, next_sendPacketWEn;
reg     stallSent, next_stallSent;
reg     transDone, next_transDone;

// diagram signals declarations
reg  [7:0]PIDByte, next_PIDByte;
reg  [6:0]USBAddress, next_USBAddress;
reg  [4:0]USBEndPControlRegCopy, next_USBEndPControlRegCopy;
reg  [7:0]addrEndPTemp, next_addrEndPTemp;
reg  [7:0]endpCRCTemp, next_endpCRCTemp;
reg  [1:0]tempUSBEndPTransTypeReg, next_tempUSBEndPTransTypeReg;

reg [4:0] CurrState_slvCntrl;
reg [4:0] NextState_slvCntrl;


//--------------------------------------------------------------------
// Machine: slvCntrl
//--------------------------------------------------------------------
//----------------------------------
// Next State Logic (combinatorial)
//----------------------------------
always @ (RxByte or tempUSBEndPTransTypeReg or endpCRCTemp or addrEndPTemp or USBEndPControlReg or RxDataWEn or RxStatus or PIDByte or USBEndPControlRegCopy or NAKSent or sendPacketRdy or getPacketRdy or CRCError or bitStuffError or RxOverflow or RxTimeOut or USBEndP or USBAddress or USBTgtAddress or SCGlobalEn or stallSent or SOFRxed or transDone or clrEPRdy or endPMuxErrorsWEn or getPacketREn or sendPacketWEn or sendPacketPID or USBEndPTransTypeReg or USBEndPNakTransTypeReg or frameNum or endPointReadyToGetPkt or CurrState_slvCntrl)
begin : slvCntrl_NextState
  NextState_slvCntrl <= CurrState_slvCntrl;
  // Set default values for outputs and signals
  next_stallSent <= stallSent;
  next_NAKSent <= NAKSent;
  next_SOFRxed <= SOFRxed;
  next_PIDByte <= PIDByte;
  next_transDone <= transDone;
  next_clrEPRdy <= clrEPRdy;
  next_endPMuxErrorsWEn <= endPMuxErrorsWEn;
  next_tempUSBEndPTransTypeReg <= tempUSBEndPTransTypeReg;
  next_getPacketREn <= getPacketREn;
  next_sendPacketWEn <= sendPacketWEn;
  next_sendPacketPID <= sendPacketPID;
  next_USBEndPTransTypeReg <= USBEndPTransTypeReg;
  next_USBEndPNakTransTypeReg <= USBEndPNakTransTypeReg;
  next_endpCRCTemp <= endpCRCTemp;
  next_addrEndPTemp <= addrEndPTemp;
  next_frameNum <= frameNum;
  next_USBAddress <= USBAddress;
  next_USBEndP <= USBEndP;
  next_USBEndPControlRegCopy <= USBEndPControlRegCopy;
  next_endPointReadyToGetPkt <= endPointReadyToGetPkt;
  case (CurrState_slvCntrl)
    5'd0:
    begin
      next_stallSent <= 1'b0;
      next_NAKSent <= 1'b0;
      next_SOFRxed <= 1'b0;
      if (RxDataWEn == 1'b1 && 
        RxStatus == 0 && 
        RxByte[1:0] == 2'b01)	
      begin
        NextState_slvCntrl <= 5'd3;
        next_PIDByte <= RxByte;
      end
    end
    5'd1:
    begin
      next_transDone <= 1'b0;
      next_clrEPRdy <= 1'b0;
      next_endPMuxErrorsWEn <= 1'b0;
      NextState_slvCntrl <= 5'd0;
    end
    5'd5:
      if (PIDByte[3:0] == 4'hd)	
      begin
        NextState_slvCntrl <= 5'd13;
        next_tempUSBEndPTransTypeReg <= 0;
        next_getPacketREn <= 1'b1;
      end
      else if (PIDByte[3:0] == 4'h1)	
      begin
        NextState_slvCntrl <= 5'd13;
        next_tempUSBEndPTransTypeReg <= 2;
        next_getPacketREn <= 1'b1;
      end
      else if ((PIDByte[3:0] == 4'h9) && (USBEndPControlRegCopy[4] == 1'b0))	
      begin
        NextState_slvCntrl <= 5'd10;
        next_tempUSBEndPTransTypeReg <= 1;
      end
      else if (((PIDByte[3:0] == 4'h9) && (USBEndPControlRegCopy [1] == 1'b1)) && (USBEndPControlRegCopy [2] == 1'b0))	
      begin
        NextState_slvCntrl <= 5'd18;
        next_tempUSBEndPTransTypeReg <= 1;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'h3;
      end
      else if ((PIDByte[3:0] == 4'h9) && (USBEndPControlRegCopy [1] == 1'b1))	
      begin
        NextState_slvCntrl <= 5'd18;
        next_tempUSBEndPTransTypeReg <= 1;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'hb;
      end
      else if (PIDByte[3:0] == 4'h9)	
      begin
        NextState_slvCntrl <= 5'd8;
        next_tempUSBEndPTransTypeReg <= 1;
      end
      else
        NextState_slvCntrl <= 5'd7;
    5'd7:
      NextState_slvCntrl <= 5'd0;
    5'd8:
      if (USBEndPControlRegCopy [1] == 1'b1)	
      begin
        NextState_slvCntrl <= 5'd1;
        next_transDone <= 1'b1;
        next_clrEPRdy <= 1'b1;
        next_USBEndPTransTypeReg <= tempUSBEndPTransTypeReg;
        next_endPMuxErrorsWEn <= 1'b1;
      end
      else if (NAKSent == 1'b1)	
      begin
        NextState_slvCntrl <= 5'd1;
        next_USBEndPNakTransTypeReg <= tempUSBEndPTransTypeReg;
        next_endPMuxErrorsWEn <= 1'b1;
      end
      else
        NextState_slvCntrl <= 5'd1;
    5'd11:
      if (USBEndPControlRegCopy [1] == 1'b0)	
      begin
        NextState_slvCntrl <= 5'd12;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'ha;
        next_NAKSent <= 1'b1;
      end
      else if (USBEndPControlRegCopy [3] == 1'b1)	
      begin
        NextState_slvCntrl <= 5'd12;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'he;
        next_stallSent <= 1'b1;
      end
      else
      begin
        NextState_slvCntrl <= 5'd12;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'h2;
      end
    5'd12:
    begin
      next_sendPacketWEn <= 1'b0;
      if (sendPacketRdy == 1'b1)	
        NextState_slvCntrl <= 5'd8;
    end
    5'd13:
    begin
      next_getPacketREn <= 1'b0;
      if ((getPacketRdy == 1'b1) && (USBEndPControlRegCopy [4] == 1'b1))	
        NextState_slvCntrl <= 5'd8;
      else if ((getPacketRdy == 1'b1) && (CRCError == 1'b0 &&
        bitStuffError == 1'b0 && 
        RxOverflow == 1'b0 && 
        RxTimeOut == 1'b0))	
        NextState_slvCntrl <= 5'd11;
      else if (getPacketRdy == 1'b1)	
        NextState_slvCntrl <= 5'd8;
    end
    5'd9:
    begin
      next_sendPacketWEn <= 1'b0;
      if (sendPacketRdy == 1'b1)	
        NextState_slvCntrl <= 5'd8;
    end
    5'd10:
      if (USBEndPControlRegCopy [1] == 1'b0)	
      begin
        NextState_slvCntrl <= 5'd9;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'ha;
        next_NAKSent <= 1'b1;
      end
      else if (USBEndPControlRegCopy [3] == 1'b1)	
      begin
        NextState_slvCntrl <= 5'd9;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'he;
        next_stallSent <= 1'b1;
      end
      else if (USBEndPControlRegCopy [2] == 1'b0)	
      begin
        NextState_slvCntrl <= 5'd18;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'h3;
      end
      else
      begin
        NextState_slvCntrl <= 5'd18;
        next_sendPacketWEn <= 1'b1;
        next_sendPacketPID <= 4'hb;
      end
    5'd17:
    begin
      next_getPacketREn <= 1'b0;
      if (getPacketRdy == 1'b1)	
        NextState_slvCntrl <= 5'd8;
    end
    5'd18:
    begin
      next_sendPacketWEn <= 1'b0;
      if (sendPacketRdy == 1'b1)	
        NextState_slvCntrl <= 5'd19;
    end
    5'd19:
      if (USBEndPControlRegCopy [4] == 1'b1)	
        NextState_slvCntrl <= 5'd8;
      else
      begin
        NextState_slvCntrl <= 5'd17;
        next_getPacketREn <= 1'b1;
      end
    5'd14:
      NextState_slvCntrl <= 5'd0;
    5'd2:
      if (RxDataWEn == 1'b1 && 
        RxStatus == 1)	
      begin
        NextState_slvCntrl <= 5'd4;
        next_endpCRCTemp <= RxByte;
      end
      else if (RxDataWEn == 1'b1 && 
        RxStatus != 1)	
        NextState_slvCntrl <= 5'd0;
    5'd3:
      if (RxDataWEn == 1'b1 && 
        RxStatus == 1)	
      begin
        NextState_slvCntrl <= 5'd2;
        next_addrEndPTemp <= RxByte;
      end
      else if (RxDataWEn == 1'b1 && 
        RxStatus != 1)	
        NextState_slvCntrl <= 5'd0;
    5'd4:
      if ((RxDataWEn == 1'b1) && (RxByte[0] == 1'b0 &&
        RxByte[1] == 1'b0 &&
        RxByte [2] == 1'b0))	
        NextState_slvCntrl <= 5'd6;
      else if (RxDataWEn == 1'b1)	
        NextState_slvCntrl <= 5'd0;
    5'd6:
      if (PIDByte[3:0] == 4'h5)	
      begin
        NextState_slvCntrl <= 5'd0;
        next_frameNum <= {endpCRCTemp[2:0],addrEndPTemp};
        next_SOFRxed <= 1'b1;
      end
      else
      begin
        NextState_slvCntrl <= 5'd15;
        next_USBAddress <= addrEndPTemp[6:0];
        next_USBEndP <= { endpCRCTemp[2:0], addrEndPTemp[7]};
      end
    5'd15:    // Insert delay to allow USBEndP etc to update
      NextState_slvCntrl <= 5'd16;
    5'd16:
      if (USBEndP < 4  &&
        USBAddress == USBTgtAddress &&
        SCGlobalEn == 1'b1 &&
        USBEndPControlReg[0] == 1'b1)	
      begin
        NextState_slvCntrl <= 5'd5;
        next_USBEndPControlRegCopy <= USBEndPControlReg;
        next_endPointReadyToGetPkt <= USBEndPControlReg [1];
      end
      else
        NextState_slvCntrl <= 5'd0;
  endcase
end

//----------------------------------
// Current State Logic (sequential)
//----------------------------------
always @ (posedge clk)
begin : slvCntrl_CurrentState
  if (rst)	
    CurrState_slvCntrl <= 5'd14;
  else
    CurrState_slvCntrl <= NextState_slvCntrl;
end

//----------------------------------
// Registered outputs logic
//----------------------------------
always @ (posedge clk)
begin : slvCntrl_RegOutput
  if (rst)	
  begin
    tempUSBEndPTransTypeReg <= 2'b00;
    addrEndPTemp <= 8'h00;
    endpCRCTemp <= 8'h00;
    USBAddress <= 7'b0000000;
    PIDByte <= 8'h00;
    USBEndPControlRegCopy <= 5'b00000;
    transDone <= 1'b0;
    getPacketREn <= 1'b0;
    sendPacketPID <= 4'b0;
    sendPacketWEn <= 1'b0;
    clrEPRdy <= 1'b0;
    USBEndPTransTypeReg <= 2'b00;
    USBEndPNakTransTypeReg <= 2'b00;
    NAKSent <= 1'b0;
    stallSent <= 1'b0;
    SOFRxed <= 1'b0;
    endPMuxErrorsWEn <= 1'b0;
    frameNum <= 11'b00000000000;
    USBEndP <= 4'h0;
    endPointReadyToGetPkt <= 1'b0;
  end
  else 
  begin
    tempUSBEndPTransTypeReg <= next_tempUSBEndPTransTypeReg;
    addrEndPTemp <= next_addrEndPTemp;
    endpCRCTemp <= next_endpCRCTemp;
    USBAddress <= next_USBAddress;
    PIDByte <= next_PIDByte;
    USBEndPControlRegCopy <= next_USBEndPControlRegCopy;
    transDone <= next_transDone;
    getPacketREn <= next_getPacketREn;
    sendPacketPID <= next_sendPacketPID;
    sendPacketWEn <= next_sendPacketWEn;
    clrEPRdy <= next_clrEPRdy;
    USBEndPTransTypeReg <= next_USBEndPTransTypeReg;
    USBEndPNakTransTypeReg <= next_USBEndPNakTransTypeReg;
    NAKSent <= next_NAKSent;
    stallSent <= next_stallSent;
    SOFRxed <= next_SOFRxed;
    endPMuxErrorsWEn <= next_endPMuxErrorsWEn;
    frameNum <= next_frameNum;
    USBEndP <= next_USBEndP;
    endPointReadyToGetPkt <= next_endPointReadyToGetPkt;
  end
end

endmodule