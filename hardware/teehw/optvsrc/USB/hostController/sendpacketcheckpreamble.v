module sendPacketCheckPreamble (
	input				clk,
	input				preAmbleEnable,
	input				rst,
	input		[3:0]	sendPacketCPPID,
	input				sendPacketCPWEn,
	input				sendPacketRdy,
	output	reg			sendPacketCPReady,
	output	reg	[3:0]	sendPacketPID,
	output	reg			sendPacketWEn
);

	reg				next_sendPacketCPReady;
	reg		[3:0]	next_sendPacketPID;
	reg				next_sendPacketWEn;

	reg		[3:0]	CurrState_sendPktCP;
	reg		[3:0]	NextState_sendPktCP;

	//--------------------------------------------------------------------
	// Machine: sendPktCP
	//--------------------------------------------------------------------
	//----------------------------------
	// Next State Logic (combinatorial)
	//----------------------------------
	always@(*) begin
		NextState_sendPktCP <= CurrState_sendPktCP;
		// Set default values for outputs and signals
		next_sendPacketCPReady <= sendPacketCPReady;
		next_sendPacketWEn <= sendPacketWEn;
		next_sendPacketPID <= sendPacketPID;
		case(CurrState_sendPktCP)
			4'd0: begin
				if(sendPacketCPWEn) begin
					NextState_sendPktCP <= 4'd2;
					next_sendPacketCPReady <= 1'b0; end
			end
			4'd1: begin
				NextState_sendPktCP <= 4'd0;
			end
			4'd2: begin
				if(preAmbleEnable&(sendPacketCPPID!=4'h5))	NextState_sendPktCP <= 4'd4;
				else										NextState_sendPktCP <= 4'd9;
			end
			4'd11: begin
				next_sendPacketCPReady <= 1'b1;
				NextState_sendPktCP <= 4'd0;
			end
			4'd3: begin
				next_sendPacketWEn <= 1'b1;
				next_sendPacketPID <= 4'hc;
				NextState_sendPktCP <= 4'd5;
			end
			4'd4: begin
				if(sendPacketRdy)	NextState_sendPktCP <= 4'd3;
			end
			4'd5: begin
				next_sendPacketWEn <= 1'b0;
				NextState_sendPktCP <= 4'd12;
			end
			4'd6: begin
				next_sendPacketWEn <= 1'b1;
				next_sendPacketPID <= sendPacketCPPID;
				NextState_sendPktCP <= 4'd7;
			end
			4'd7: begin
				next_sendPacketWEn <= 1'b0;
				NextState_sendPktCP <= 4'd13;
			end
			4'd12: begin
				if(sendPacketRdy)	NextState_sendPktCP <= 4'd6;
			end
			4'd13: begin
				if(sendPacketRdy)	NextState_sendPktCP <= 4'd11;
			end
			4'd8: begin
				next_sendPacketWEn <= 1'b1;
				next_sendPacketPID <= sendPacketCPPID;
				NextState_sendPktCP <= 4'd10;
			end
			4'd9: begin
				if(sendPacketRdy)	NextState_sendPktCP <= 4'd8;
			end
			4'd10: begin
				next_sendPacketWEn <= 1'b0;
				NextState_sendPktCP <= 4'd11;
			end
		endcase
	end

	//----------------------------------
	// Current State Logic (sequential)
	//----------------------------------
	always@(posedge clk) begin
		if(rst)	CurrState_sendPktCP <= 4'd1;
		else	CurrState_sendPktCP <= NextState_sendPktCP;
	end

	//----------------------------------
	// Registered outputs logic
	//----------------------------------
	always@(posedge clk) begin
		if(rst) begin
			sendPacketWEn <= 1'b0;
			sendPacketPID <= 4'b0;
			sendPacketCPReady <= 1'b1; end
		else begin
			sendPacketWEn <= next_sendPacketWEn;
			sendPacketPID <= next_sendPacketPID;
			sendPacketCPReady <= next_sendPacketCPReady; end
	end

endmodule
