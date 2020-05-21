module endpMux (
	input				clk,
	input				rst,
	input		[3:0]	currEndP,
	input				NAKSent,
	input				stallSent,
	input				CRCError,
	input				bitStuffError,
	input				RxOverflow,
	input				RxTimeOut,
	input				dataSequence,
	input				ACKRxed,
	input		[1:0]	transType,
	input		[1:0]	transTypeNAK,
	output	reg	[4:0]	endPControlReg,
	input				clrEPRdy,
	input				endPMuxErrorsWEn,
	input		[4:0]	endP0ControlReg,
	input		[4:0]	endP1ControlReg,
	input		[4:0]	endP2ControlReg,
	input		[4:0]	endP3ControlReg,
	output	reg	[7:0]	endP0StatusReg,
	output	reg	[7:0]	endP1StatusReg,
	output	reg	[7:0]	endP2StatusReg,
	output	reg	[7:0]	endP3StatusReg,
	output	reg	[1:0]	endP0TransTypeReg,
	output	reg	[1:0]	endP1TransTypeReg,
	output	reg	[1:0]	endP2TransTypeReg,
	output	reg	[1:0]	endP3TransTypeReg,
	output	reg	[1:0]	endP0NAKTransTypeReg,
	output	reg	[1:0]	endP1NAKTransTypeReg,
	output	reg	[1:0]	endP2NAKTransTypeReg,
	output	reg	[1:0]	endP3NAKTransTypeReg,
	output	reg			clrEP0Rdy,
	output	reg			clrEP1Rdy,
	output	reg			clrEP2Rdy,
	output	reg			clrEP3Rdy
);

	wire	[7:0]	endPStatusCombine;

	//mux endPControlReg and clrEPRdy
	always@(posedge clk) begin
		case(currEndP[1:0])
			2'b00: begin
				endPControlReg <= endP0ControlReg;
				clrEP0Rdy <= clrEPRdy; end
			2'b01: begin
				endPControlReg <= endP1ControlReg;
				clrEP1Rdy <= clrEPRdy; end
			2'b10: begin
				endPControlReg <= endP2ControlReg;
				clrEP2Rdy <= clrEPRdy; end
			2'b11: begin
				endPControlReg <= endP3ControlReg;
				clrEP3Rdy <= clrEPRdy; end
		endcase  
	end      

	//mux endPNAKTransType, endPTransType, endPStatusReg
	//If there was a NAK sent then set the NAKSent bit, and leave the other status reg bits untouched.
	//else update the entire status reg
	always @(posedge clk) begin
		if(rst) begin
			endP0NAKTransTypeReg <= 2'b00;
			endP1NAKTransTypeReg <= 2'b00;
			endP2NAKTransTypeReg <= 2'b00;
			endP3NAKTransTypeReg <= 2'b00;
			endP0TransTypeReg <= 2'b00;
			endP1TransTypeReg <= 2'b00;
			endP2TransTypeReg <= 2'b00;
			endP3TransTypeReg <= 2'b00;
			endP0StatusReg <= 4'h0;
			endP1StatusReg <= 4'h0;
			endP2StatusReg <= 4'h0;
			endP3StatusReg <= 4'h0; end
		else begin
			if(endPMuxErrorsWEn) begin
				if(NAKSent) begin
					case (currEndP[1:0])
						2'b00: begin
							endP0NAKTransTypeReg <= transTypeNAK;
							endP0StatusReg <= {endP0StatusReg[7:5],1'b1,endP0StatusReg[3:0]}; end
						2'b01: begin
							endP1NAKTransTypeReg <= transTypeNAK;
							endP1StatusReg <= {endP1StatusReg[7:5],1'b1,endP1StatusReg[3:0]}; end
						2'b10: begin
							endP2NAKTransTypeReg <= transTypeNAK;
							endP2StatusReg <= {endP2StatusReg[7:5],1'b1,endP2StatusReg[3:0]}; end
						2'b11: begin
							endP3NAKTransTypeReg <= transTypeNAK;
							endP3StatusReg <= {endP3StatusReg[7:5],1'b1,endP3StatusReg[3:0]}; end
					endcase end
				else begin
					case (currEndP[1:0])
						2'b00: begin
							endP0TransTypeReg <= transType;
							endP0StatusReg <= endPStatusCombine; end
						2'b01: begin
							endP1TransTypeReg <= transType;
							endP1StatusReg <= endPStatusCombine; end
						2'b10: begin
							endP2TransTypeReg <= transType;
							endP2StatusReg <= endPStatusCombine; end
						2'b11: begin
							endP3TransTypeReg <= transType;
							endP3StatusReg <= endPStatusCombine; end
					endcase
				end
			end
		end
	end

	//combine status bits into a single word
	assign endPStatusCombine = {dataSequence, ACKRxed, stallSent, 1'b0, RxTimeOut, RxOverflow, bitStuffError, CRCError};

endmodule
