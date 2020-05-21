module USBHostControlBI (
	input		[3:0]	address,
	input		[7:0]	dataIn,
	input				writeEn,
	input				strobe_i,
	input				busClk,
	input				rstSyncToBusClk,
	input				usbClk,
	input				rstSyncToUsbClk,
	output	reg	[7:0]	dataOut,
	output				SOFSentIntOut,
	output				connEventIntOut,
	output				resumeIntOut,
	output				transDoneIntOut,

	output	reg	[1:0]	TxTransTypeReg,
	output	reg			TxSOFEnableReg,
	output	reg	[6:0]	TxAddrReg,
	output	reg	[3:0]	TxEndPReg,
	input		[10:0]	frameNumIn,
	input		[7:0]	RxPktStatusIn,
	input		[3:0]	RxPIDIn,
	input		[1:0]	connectStateIn,
	input				SOFSentIn,
	input				connEventIn,
	input				resumeIntIn,
	input				transDoneIn,
	input				hostControlSelect,
	input				clrTransReq,
	output	reg			preambleEn,
	output	reg			SOFSync,
	output	reg	[1:0]	TxLineState,
	output	reg			LineDirectControlEn,
	output	reg			fullSpeedPol,
	output	reg			fullSpeedRate,
	output	reg			transReq,
	output	reg			isoEn,	//enable isochronous mode
	input		[15:0]	SOFTimer
);

	reg		[1:0]	TxTransTypeReg_reg1;
	reg				TxSOFEnableReg_reg1;
	reg		[6:0]	TxAddrReg_reg1;
	reg		[3:0]	TxEndPReg_reg1;

	reg				preambleEn_reg1;
	reg				SOFSync_reg1;
	reg		[1:0]	TxLineState_reg1;
	reg				LineDirectControlEn_reg1;
	reg				fullSpeedPol_reg1; 
	reg				fullSpeedRate_reg1;
	reg				transReq_reg1;
	reg				isoEn_reg1;

	reg		[1:0]	TxControlReg;
	reg		[4:0]	TxLineControlReg;
	reg				clrSOFReq;
	reg				clrConnEvtReq;
	reg				clrResInReq;
	reg				clrTransDoneReq;
	reg				SOFSentInt;
	reg				connEventInt;
	reg				resumeInt;
	reg				transDoneInt;
	reg		[3:0]	interruptMaskReg;
	reg				setTransReq;
	reg		[2:0]	resumeIntInExtend;
	reg		[2:0]	transDoneInExtend;
	reg		[2:0]	connEventInExtend;
	reg		[2:0]	SOFSentInExtend;
	reg		[2:0]	clrTransReqExtend;

	//clock domain crossing sync registers
	//STB = Sync To Busclk
	reg		[1:0]	TxTransTypeRegSTB;
	reg				TxSOFEnableRegSTB;
	reg		[6:0]	TxAddrRegSTB;
	reg		[3:0]	TxEndPRegSTB;
	reg				preambleEnSTB;
	reg				SOFSyncSTB;
	wire	[1:0]	TxLineStateSTB;
	wire			LineDirectControlEnSTB;
	wire			fullSpeedPolSTB; 
	wire			fullSpeedRateSTB;
	reg				transReqSTB;
	reg				isoEnSTB;   
	reg		[10:0]	frameNumInSTB;
	reg		[10:0]	frameNumInSTB_reg1;
	reg		[7:0]	RxPktStatusInSTB;
	reg		[7:0]	RxPktStatusInSTB_reg1;
	reg		[3:0]	RxPIDInSTB;
	reg		[3:0]	RxPIDInSTB_reg1;
	reg		[1:0]	connectStateInSTB;
	reg		[1:0]	connectStateInSTB_reg1;
	reg		[2:0]	SOFSentInSTB;
	reg		[2:0]	connEventInSTB;
	reg		[2:0]	resumeIntInSTB;
	reg		[2:0]	transDoneInSTB;
	reg		[2:0]	clrTransReqSTB;
	reg		[15:0]	SOFTimerSTB;
	reg		[15:0]	SOFTimerSTB_reg1;

	//sync write demux
	always@(posedge busClk) begin
		if(rstSyncToBusClk) begin
			isoEnSTB <= 1'b0;
			preambleEnSTB <= 1'b0;
			SOFSyncSTB <= 1'b0;
			TxTransTypeRegSTB <= 2'b00;
			TxLineControlReg <= 5'h00;
			TxSOFEnableRegSTB <= 1'b0;
			TxAddrRegSTB <= 7'h00;
			TxEndPRegSTB <= 4'h0;
			interruptMaskReg <= 4'h0; end
		else begin
			clrSOFReq <= 1'b0;
			clrConnEvtReq <= 1'b0;
			clrResInReq <= 1'b0;
			clrTransDoneReq <= 1'b0;
			setTransReq <= 1'b0;
			if(writeEn&strobe_i&hostControlSelect) begin
				case (address)
					4'd0: begin
						isoEnSTB <= dataIn[3];
						preambleEnSTB <= dataIn[2];
						SOFSyncSTB <= dataIn[1];
						setTransReq <= dataIn[0];
					end
					4'd1: begin
						TxTransTypeRegSTB <= dataIn[1:0];
					end
					4'd2: begin
						TxLineControlReg <= dataIn[4:0];
					end
					4'd3: begin
						TxSOFEnableRegSTB <= dataIn[0];
					end
					4'd4: begin
						TxAddrRegSTB <= dataIn[6:0];
					end
					4'd5: begin
						TxEndPRegSTB <= dataIn[3:0];
					end
					4'd8: begin
						clrSOFReq <= dataIn[3];
						clrConnEvtReq <= dataIn[2];
						clrResInReq <= dataIn[1];
						clrTransDoneReq <= dataIn[0];
					end
					4'd9: begin
						interruptMaskReg <= dataIn[3:0];
					end
				endcase
			end 
		end
	end

	//interrupt control
	always@(posedge busClk) begin
		if(rstSyncToBusClk) begin
			SOFSentInt <= 1'b0;
			connEventInt <= 1'b0;
			resumeInt <= 1'b0;
			transDoneInt <= 1'b0; end
		else begin
			if(SOFSentInSTB[1]&~SOFSentInSTB[0])	SOFSentInt <= 1'b1;
			else if(clrSOFReq)						SOFSentInt <= 1'b0;
			else									SOFSentInt <= SOFSentInt;
			if(connEventInSTB[1]&~connEventInSTB[0])	connEventInt <= 1'b1;
			else if(clrConnEvtReq)						connEventInt <= 1'b0;
			else										connEventInt <= connEventInt;
			if(resumeIntInSTB[1]&~resumeIntInSTB[0])	resumeInt <= 1'b1;
			else if(clrResInReq)						resumeInt <= 1'b0;
			else										resumeInt <= resumeInt;
			if(transDoneInSTB[1]&~transDoneInSTB[0])	transDoneInt <= 1'b1;
			else if(clrTransDoneReq)					transDoneInt <= 1'b0;
			else										transDoneInt <= transDoneInt;
		end
	end

	//mask interrupts
	assign transDoneIntOut	= transDoneInt & interruptMaskReg[0];
	assign resumeIntOut		= resumeInt & interruptMaskReg[1];
	assign connEventIntOut	= connEventInt & interruptMaskReg[2];
	assign SOFSentIntOut	= SOFSentInt & interruptMaskReg[3]; 

	//transaction request set/clear
	//Since 'busClk' can be a higher freq than 'usbClk',
	//'setTransReq' must be delayed with respect to other control signals, thus
	//ensuring that control signals have been clocked through to 'usbClk' clock
	//domain before the transaction request is asserted.
	//Not sure this is required because there is at least two 'usbClk' ticks between
	//detection of 'transReq' and sampling of related control signals.
	always@(posedge busClk) begin
		if(rstSyncToBusClk)								transReqSTB <= 1'b0;
		else if(setTransReq)							transReqSTB <= 1'b1;
		else if(clrTransReqSTB[1]&~clrTransReqSTB[0])	transReqSTB <= 1'b0;
		else											transReqSTB <= transReqSTB;
	end  
  
	//break out control signals
	assign TxLineStateSTB			= TxLineControlReg[1:0];
	assign LineDirectControlEnSTB	= TxLineControlReg[2];
	assign fullSpeedPolSTB			= TxLineControlReg[3]; 
	assign fullSpeedRateSTB			= TxLineControlReg[4];
  
	// async read mux
	always@(*) begin
		case(address)
			4'd0   : dataOut = {4'b0000, isoEnSTB, preambleEnSTB, SOFSyncSTB, transReqSTB} ;
			4'd1   : dataOut = {6'b000000, TxTransTypeRegSTB};
			4'd2   : dataOut = {3'b000, TxLineControlReg};
			4'd3   : dataOut = {7'b0000000, TxSOFEnableRegSTB};
			4'd4   : dataOut = {1'b0, TxAddrRegSTB};
			4'd5   : dataOut = {4'h0, TxEndPRegSTB};
			4'd6   : dataOut = {5'b00000, frameNumInSTB[10:8]};
			4'd7   : dataOut = frameNumInSTB[7:0];
			4'd8   : dataOut = {4'h0, SOFSentInt, connEventInt, resumeInt, transDoneInt};
			4'd9   : dataOut = {4'h0, interruptMaskReg};
			4'd10  : dataOut = RxPktStatusInSTB;
			4'd11  : dataOut = {4'b0000, RxPIDInSTB};
			4'd14  : dataOut = {6'b000000, connectStateInSTB};
			4'd15  : dataOut = SOFTimerSTB[15:8];
			default: dataOut = 8'h00;
		endcase
	end

	//re-sync from busClk to usbClk. 
	always@(posedge usbClk) begin
		if(rstSyncToUsbClk) begin
			isoEn <= 1'b0;
			isoEn_reg1 <= 1'b0;
			preambleEn <= 1'b0;
			preambleEn_reg1 <= 1'b0;
			SOFSync <= 1'b0;
			SOFSync_reg1 <= 1'b0;
			TxTransTypeReg <= 2'b00;
			TxTransTypeReg_reg1 <= 2'b00;
			TxSOFEnableReg <= 1'b0;
			TxSOFEnableReg_reg1 <= 1'b0;
			TxAddrReg <= {7{1'b0}};
			TxAddrReg_reg1 <= {7{1'b0}};
			TxEndPReg <= 4'h0;
			TxEndPReg_reg1 <= 4'h0;
			TxLineState <= 2'b00;
			TxLineState_reg1 <= 2'b00;
			LineDirectControlEn <= 1'b0;
			LineDirectControlEn_reg1 <= 1'b0;
			fullSpeedPol <= 1'b0; 
			fullSpeedPol_reg1 <= 1'b0; 
			fullSpeedRate <= 1'b0;
			fullSpeedRate_reg1 <= 1'b0;
			transReq <= 1'b0;
			transReq_reg1 <= 1'b0; end
		else begin
			isoEn_reg1 <= isoEnSTB;     
			isoEn <= isoEn_reg1;     
			preambleEn_reg1 <= preambleEnSTB;
			preambleEn <= preambleEn_reg1;
			SOFSync_reg1 <= SOFSyncSTB;
			SOFSync <= SOFSync_reg1;
			TxTransTypeReg_reg1 <= TxTransTypeRegSTB;
			TxTransTypeReg <= TxTransTypeReg_reg1;
			TxSOFEnableReg_reg1 <= TxSOFEnableRegSTB;
			TxSOFEnableReg <= TxSOFEnableReg_reg1;
			TxAddrReg_reg1 <= TxAddrRegSTB;
			TxAddrReg <= TxAddrReg_reg1;
			TxEndPReg_reg1 <= TxEndPRegSTB;
			TxEndPReg <= TxEndPReg_reg1;
			TxLineState_reg1 <= TxLineStateSTB;
			TxLineState <= TxLineState_reg1;
			LineDirectControlEn_reg1 <= LineDirectControlEnSTB;
			LineDirectControlEn <= LineDirectControlEn_reg1;
			fullSpeedPol_reg1 <= fullSpeedPolSTB; 
			fullSpeedPol <= fullSpeedPol_reg1; 
			fullSpeedRate_reg1 <= fullSpeedRateSTB;
			fullSpeedRate <= fullSpeedRate_reg1;
			transReq_reg1 <= transReqSTB;
			transReq <= transReq_reg1; end
	end

	//Extend resumeIntIn etc from 1 tick to 3 ticks
	always@(posedge usbClk) begin
		if(rstSyncToUsbClk) begin
			resumeIntInExtend <= 3'b000;
			transDoneInExtend <= 3'b000;
			connEventInExtend <= 3'b000;
			SOFSentInExtend <= 3'b000;
			clrTransReqExtend <= 3'b000; end
		else begin
			if(resumeIntIn)	resumeIntInExtend <= 3'b111;
			else			resumeIntInExtend <= {1'b0, resumeIntInExtend[2:1]};
			if(transDoneIn)	transDoneInExtend <= 3'b111;
			else			transDoneInExtend <= {1'b0, transDoneInExtend[2:1]};
			if(connEventIn)	connEventInExtend <= 3'b111;
			else			connEventInExtend <= {1'b0, connEventInExtend[2:1]};
			if(SOFSentIn)	SOFSentInExtend <= 3'b111;
			else			SOFSentInExtend <= {1'b0, SOFSentInExtend[2:1]};
			if(clrTransReq)	clrTransReqExtend <= 3'b111;
			else			clrTransReqExtend <= {1'b0, clrTransReqExtend[2:1]};
		end
	end

	//re-sync from usbClk to busClk. Since 'clrTransReq', 'transDoneIn' etc are only asserted 
	//for 3 'usbClk' ticks, busClk freq must be greater than or equal to usbClk/3 freq
	always@(posedge busClk) begin
		if(rstSyncToBusClk) begin
			SOFSentInSTB <= 3'b000;
			connEventInSTB <= 3'b000;
			resumeIntInSTB <= 3'b000;
			transDoneInSTB <= 3'b000;
			clrTransReqSTB <= 3'b000;
			frameNumInSTB <= {11{1'b0}};
			frameNumInSTB_reg1 <= {11{1'b0}};
			RxPktStatusInSTB <= 8'h00;
			RxPktStatusInSTB_reg1 <= 8'h00;
			RxPIDInSTB <= 4'h0;
			RxPIDInSTB_reg1 <= 4'h0;
			connectStateInSTB <= 2'b00;
			connectStateInSTB_reg1 <= 2'b00;
			SOFTimerSTB <= 16'h0000;
			SOFTimerSTB_reg1 <= 16'h0000; end
		else begin
			frameNumInSTB_reg1 <= frameNumIn;
			frameNumInSTB <= frameNumInSTB_reg1;
			RxPktStatusInSTB_reg1 <= RxPktStatusIn;
			RxPktStatusInSTB <= RxPktStatusInSTB_reg1;
			RxPIDInSTB_reg1 <= RxPIDIn;
			RxPIDInSTB <= RxPIDInSTB_reg1;
			connectStateInSTB_reg1 <= connectStateIn;
			connectStateInSTB <= connectStateInSTB_reg1;
			SOFSentInSTB <= {SOFSentInExtend[0], SOFSentInSTB[2:1]};
			connEventInSTB <= {connEventInExtend[0], connEventInSTB[2:1]};
			resumeIntInSTB <= {resumeIntInExtend[0], resumeIntInSTB[2:1]};
			transDoneInSTB <= {transDoneInExtend[0], transDoneInSTB[2:1]};
			clrTransReqSTB <= {clrTransReqExtend[0], clrTransReqSTB[2:1]};
			//FIXME. It is not safe to pass 'SOFTimer' multi-bit signal between clock domains this way
			//All the other multi-bit signals will be static at the time that they are
			//read, but 'SOFTimer' will not be static.
			SOFTimerSTB_reg1 <= SOFTimer; 
			SOFTimerSTB <= SOFTimerSTB_reg1; end
	end

endmodule
