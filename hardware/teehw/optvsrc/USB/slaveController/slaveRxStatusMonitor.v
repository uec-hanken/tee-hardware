module slaveRxStatusMonitor(
	input		[1:0]	connectStateIn,
	input				resumeDetectedIn,
	input				clk,
	input				rst,
	output	reg			resetEventOut,
	output		[1:0]	connectStateOut,
	output	reg			resumeIntOut
);

	reg		[1:0]	oldConnectState;
	reg				oldResumeDetected;

	assign connectStateOut = connectStateIn;

	always@(posedge clk) begin
		if(rst) begin
			oldConnectState <= connectStateIn;
			oldResumeDetected <= resumeDetectedIn; end
		else begin
			oldConnectState <= connectStateIn;
			oldResumeDetected <= resumeDetectedIn;
			if(oldConnectState!=connectStateIn)	resetEventOut <= 1'b1;
			else								resetEventOut <= 1'b0;
			if(resumeDetectedIn&~oldResumeDetected)	resumeIntOut <= 1'b1;
			else									resumeIntOut <= 1'b0;
		end
	end

endmodule
