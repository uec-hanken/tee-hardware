module slavecontroller (
	input				CRCError,
	input		[7:0]	RxByte,
	input				RxDataWEn,
	input				RxOverflow,
	input		[7:0]	RxStatus,
	input				RxTimeOut,
	input				SCGlobalEn,
	input		[4:0]	USBEndPControlReg,
	input		[6:0]	USBTgtAddress,
	input				bitStuffError,
	input				clk,
	input				getPacketRdy,
	input				rst,
	input				sendPacketRdy,
	output	reg			NAKSent,
	output	reg			SOFRxed,
	output	reg	[1:0]	USBEndPNakTransTypeReg,
	output	reg	[1:0]	USBEndPTransTypeReg,
	output	reg	[3:0]	USBEndP,
	output	reg			clrEPRdy,
	output	reg			endPMuxErrorsWEn,
	output	reg			endPointReadyToGetPkt,
	output	reg	[10:0]	frameNum,
	output	reg			getPacketREn,
	output	reg	[3:0]	sendPacketPID,
	output	reg			sendPacketWEn,
	output	reg			stallSent,
	output	reg			transDone
);

	reg				next_NAKSent;
	reg				next_SOFRxed;
	reg		[1:0]	next_USBEndPNakTransTypeReg;
	reg		[1:0]	next_USBEndPTransTypeReg;
	reg		[3:0]	next_USBEndP;
	reg				next_clrEPRdy;
	reg				next_endPMuxErrorsWEn;
	reg				next_endPointReadyToGetPkt;
	reg		[10:0]	next_frameNum;
	reg				next_getPacketREn;
	reg		[3:0]	next_sendPacketPID;
	reg				next_sendPacketWEn;
	reg				next_stallSent;
	reg				next_transDone;

	reg		[7:0]	PIDByte, next_PIDByte;
	reg		[6:0]	USBAddress, next_USBAddress;
	reg		[4:0]	USBEndPControlRegCopy, next_USBEndPControlRegCopy;
	reg		[7:0]	addrEndPTemp, next_addrEndPTemp;
	reg		[7:0]	endpCRCTemp, next_endpCRCTemp;
	reg		[1:0]	tempUSBEndPTransTypeReg, next_tempUSBEndPTransTypeReg;

	reg		[4:0]	CurrState_slvCntrl;
	reg		[4:0]	NextState_slvCntrl;

	//--------------------------------------------------------------------
	// Machine: slvCntrl
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
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
		case(CurrState_slvCntrl)
			5'd0: begin
				next_stallSent <= 1'b0;
				next_NAKSent <= 1'b0;
				next_SOFRxed <= 1'b0;
				if(RxDataWEn&(RxStatus==8'b0)&(RxByte[1:0]==2'b01)) begin
					NextState_slvCntrl <= 5'd3;
					next_PIDByte <= RxByte; end
			end
			5'd1: begin
				next_transDone <= 1'b0;
				next_clrEPRdy <= 1'b0;
				next_endPMuxErrorsWEn <= 1'b0;
				NextState_slvCntrl <= 5'd0;
			end
			5'd5: begin
				if(PIDByte[3:0]==4'hd) begin
					NextState_slvCntrl <= 5'd13;
					next_tempUSBEndPTransTypeReg <= 0;
					next_getPacketREn <= 1'b1; end
				else if(PIDByte[3:0]==4'h1) begin
					NextState_slvCntrl <= 5'd13;
					next_tempUSBEndPTransTypeReg <= 2;
					next_getPacketREn <= 1'b1; end
				else if((PIDByte[3:0]==4'h9)&~USBEndPControlRegCopy[4]) begin
					NextState_slvCntrl <= 5'd10;
					next_tempUSBEndPTransTypeReg <= 1; end
				else if((PIDByte[3:0]==4'h9)&USBEndPControlRegCopy[1]&~USBEndPControlRegCopy[2]) begin
					NextState_slvCntrl <= 5'd18;
					next_tempUSBEndPTransTypeReg <= 1;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'h3; end
				else if((PIDByte[3:0]==4'h9)&USBEndPControlRegCopy[1]) begin
					NextState_slvCntrl <= 5'd18;
					next_tempUSBEndPTransTypeReg <= 1;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'hb; end
				else if(PIDByte[3:0]==4'h9) begin
					NextState_slvCntrl <= 5'd8;
					next_tempUSBEndPTransTypeReg <= 1; end
				else	NextState_slvCntrl <= 5'd7;
			end
			5'd7: begin
				NextState_slvCntrl <= 5'd0;
			end
			5'd8: begin
				if(USBEndPControlRegCopy[1]) begin
					NextState_slvCntrl <= 5'd1;
					next_transDone <= 1'b1;
					next_clrEPRdy <= 1'b1;
					next_USBEndPTransTypeReg <= tempUSBEndPTransTypeReg;
					next_endPMuxErrorsWEn <= 1'b1; end
				else if(NAKSent) begin
					NextState_slvCntrl <= 5'd1;
					next_USBEndPNakTransTypeReg <= tempUSBEndPTransTypeReg;
					next_endPMuxErrorsWEn <= 1'b1; end
				else	NextState_slvCntrl <= 5'd1;
			end
			5'd11: begin
				if(~USBEndPControlRegCopy[1]) begin
					NextState_slvCntrl <= 5'd12;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'ha;
					next_NAKSent <= 1'b1; end
				else if(USBEndPControlRegCopy[3]) begin
					NextState_slvCntrl <= 5'd12;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'he;
					next_stallSent <= 1'b1; end
				else begin
					NextState_slvCntrl <= 5'd12;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'h2; end
			end
			5'd12: begin
				next_sendPacketWEn <= 1'b0;
				if(sendPacketRdy)	NextState_slvCntrl <= 5'd8;
			end
			5'd13: begin
				next_getPacketREn <= 1'b0;
				if(getPacketRdy&USBEndPControlRegCopy[4])	NextState_slvCntrl <= 5'd8;
				else if(getPacketRdy&~CRCError&~bitStuffError&~RxOverflow&~RxTimeOut)
															NextState_slvCntrl <= 5'd11;
				else if(getPacketRdy)						NextState_slvCntrl <= 5'd8;
			end
			5'd9: begin
				next_sendPacketWEn <= 1'b0;
				if(sendPacketRdy)	NextState_slvCntrl <= 5'd8;
			end
			5'd10: begin
				if(~USBEndPControlRegCopy[1]) begin
					NextState_slvCntrl <= 5'd9;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'ha;
					next_NAKSent <= 1'b1; end
				else if(USBEndPControlRegCopy[3]) begin
					NextState_slvCntrl <= 5'd9;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'he;
					next_stallSent <= 1'b1; end
				else if(~USBEndPControlRegCopy[2]) begin
					NextState_slvCntrl <= 5'd18;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'h3; end
				else begin
					NextState_slvCntrl <= 5'd18;
					next_sendPacketWEn <= 1'b1;
					next_sendPacketPID <= 4'hb; end
			end
			5'd17: begin
				next_getPacketREn <= 1'b0;
				if(getPacketRdy)	NextState_slvCntrl <= 5'd8;
			end
			5'd18: begin
				next_sendPacketWEn <= 1'b0;
				if(sendPacketRdy)	NextState_slvCntrl <= 5'd19;
			end
			5'd19: begin
				if(USBEndPControlRegCopy[4])	NextState_slvCntrl <= 5'd8;
				else begin
					NextState_slvCntrl <= 5'd17;
					next_getPacketREn <= 1'b1; end
			end
			5'd14: begin
				NextState_slvCntrl <= 5'd0;
			end
			5'd2: begin
				if(RxDataWEn&(RxStatus==8'd1)) begin
					NextState_slvCntrl <= 5'd4;
					next_endpCRCTemp <= RxByte; end
				else if(RxDataWEn&(RxStatus!=8'd1))	NextState_slvCntrl <= 5'd0;
			end
			5'd3: begin
				if(RxDataWEn&(RxStatus==8'd1)) begin
					NextState_slvCntrl <= 5'd2;
					next_addrEndPTemp <= RxByte; end
				else if(RxDataWEn&(RxStatus!=8'd1))	NextState_slvCntrl <= 5'd0;
			end
			5'd4: begin
				if(RxDataWEn&~RxByte[0]&~RxByte[1]&~RxByte[2])	NextState_slvCntrl <= 5'd6;
				else if(RxDataWEn)								NextState_slvCntrl <= 5'd0;
			end
			5'd6: begin
				if(PIDByte[3:0]==4'h5) begin
					NextState_slvCntrl <= 5'd0;
					next_frameNum <= {endpCRCTemp[2:0],addrEndPTemp};
					next_SOFRxed <= 1'b1; end
				else begin
					NextState_slvCntrl <= 5'd15;
					next_USBAddress <= addrEndPTemp[6:0];
					next_USBEndP <= { endpCRCTemp[2:0], addrEndPTemp[7]}; end
			end
			5'd15: begin	// Insert delay to allow USBEndP etc to update
				NextState_slvCntrl <= 5'd16;
			end
			5'd16: begin
				if((USBEndP<4'd4)&(USBAddress==USBTgtAddress)&SCGlobalEn&USBEndPControlReg[0]) begin
					NextState_slvCntrl <= 5'd5;
					next_USBEndPControlRegCopy <= USBEndPControlReg;
					next_endPointReadyToGetPkt <= USBEndPControlReg[1]; end
				else	NextState_slvCntrl <= 5'd0;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_slvCntrl <= 5'd14;
		else	CurrState_slvCntrl <= NextState_slvCntrl;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
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
			endPointReadyToGetPkt <= 1'b0; end
		else begin
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
			endPointReadyToGetPkt <= next_endPointReadyToGetPkt; end
	end

endmodule
