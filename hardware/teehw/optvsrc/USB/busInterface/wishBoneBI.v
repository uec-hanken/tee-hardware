module wishBoneBI (
	input				clk,
	input				rst,
	input		[7:0]	address,
	input		[7:0]	dataIn,
	output	reg	[7:0]	dataOut,
	input				strobe_i,
	output				ack_o,
	input				writeEn,
	output	reg			hostControlSel,
	output	reg			hostRxFifoSel,
	output	reg			hostTxFifoSel,
	output	reg			slaveControlSel,
	output	reg			slaveEP0RxFifoSel, slaveEP1RxFifoSel, slaveEP2RxFifoSel, slaveEP3RxFifoSel,
	output	reg			slaveEP0TxFifoSel, slaveEP1TxFifoSel, slaveEP2TxFifoSel, slaveEP3TxFifoSel,
	output	reg			hostSlaveMuxSel,
	input		[7:0]	dataFromHostControl,
	input		[7:0]	dataFromHostRxFifo,
	input		[7:0]	dataFromHostTxFifo,
	input		[7:0]	dataFromSlaveControl,
	input		[7:0]	dataFromEP0RxFifo, dataFromEP1RxFifo, dataFromEP2RxFifo, dataFromEP3RxFifo,
	input		[7:0]	dataFromEP0TxFifo, dataFromEP1TxFifo, dataFromEP2TxFifo, dataFromEP3TxFifo,
	input		[7:0]	dataFromHostSlaveMux
);

	reg		ack_delayed;
	wire	ack_immediate;
	wire	cond;

	//address decode and data mux
	always@(*) begin
		hostControlSel    = 1'b0;
		hostRxFifoSel     = 1'b0;
		hostTxFifoSel     = 1'b0;
		slaveControlSel   = 1'b0;
		slaveEP0RxFifoSel = 1'b0;
		slaveEP0TxFifoSel = 1'b0;
		slaveEP1RxFifoSel = 1'b0;
		slaveEP1TxFifoSel = 1'b0;
		slaveEP2RxFifoSel = 1'b0;
		slaveEP2TxFifoSel = 1'b0;
		slaveEP3RxFifoSel = 1'b0;
		slaveEP3TxFifoSel = 1'b0;
		hostSlaveMuxSel   = 1'b0;
		case(address[7:4])
			4'h0 : begin
				hostControlSel = 1'b1;
				dataOut = dataFromHostControl; end
			4'h1 : begin
				hostControlSel = 1'b1;
				dataOut = dataFromHostControl; end
			4'h2 : begin
				hostRxFifoSel = 1'b1;
				dataOut = dataFromHostRxFifo; end
			4'h3 : begin
				hostTxFifoSel = 1'b1;
				dataOut = dataFromHostTxFifo; end
			4'h4 : begin
				slaveControlSel = 1'b1;
				dataOut = dataFromSlaveControl; end
			4'h5 : begin
				slaveControlSel = 1'b1;
				dataOut = dataFromSlaveControl; end
			4'h6 : begin
				slaveEP0RxFifoSel = 1'b1;
				dataOut = dataFromEP0RxFifo; end
			4'h7 : begin
				slaveEP0TxFifoSel = 1'b1;
				dataOut = dataFromEP0TxFifo; end
			4'h8 : begin
				slaveEP1RxFifoSel = 1'b1;
				dataOut = dataFromEP1RxFifo; end
			4'h9 : begin
				slaveEP1TxFifoSel = 1'b1;
				dataOut = dataFromEP1TxFifo; end
			4'ha : begin
				slaveEP2RxFifoSel = 1'b1;
				dataOut = dataFromEP2RxFifo; end
			4'hb : begin
				slaveEP2TxFifoSel = 1'b1;
				dataOut = dataFromEP2TxFifo; end
			4'hc : begin
				slaveEP3RxFifoSel = 1'b1;
				dataOut = dataFromEP3RxFifo; end
			4'hd : begin
				slaveEP3TxFifoSel = 1'b1;
				dataOut = dataFromEP3TxFifo; end
			4'he : begin
				hostSlaveMuxSel = 1'b1; 
				dataOut = dataFromHostSlaveMux; end
			4'hf: begin
				dataOut = 8'h00; end
		endcase
	end

	//delayed ack
	always@(posedge clk) begin
		ack_delayed <= strobe_i;
	end

	//immediate ack
	assign ack_immediate = strobe_i;

	//select between immediate and delayed ack
	assign cond = ~writeEn & ((address[7:4]==4'h2)|(address[7:4]==4'h3)|(address[7:4]==4'h6)|(address[7:4]==4'h7)|
							  (address[7:4]==4'h8)|(address[7:4]==4'h9)|(address[7:4]==4'ha)|(address[7:4]==4'hb)|
							  (address[7:4]==4'hc)|(address[7:4]==4'hd)) & (address[3:0]==4'h0);
	assign ack_o = (cond) ? (ack_delayed&ack_immediate) : ack_immediate;

endmodule
