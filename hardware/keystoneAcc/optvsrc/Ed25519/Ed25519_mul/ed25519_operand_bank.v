module ram_2kb_32x512 (
	input			iClk,
	input	[8:0]	iAddr,
	input			iWr,	//1: Write,	0: Read
	input	[31:0]	iWrData,
	output	[31:0]	oRdData
);
/*
 reg	[31:0]	mem[0:511];
 reg	[31:0]	rd_data;
 
 always@(posedge iClk) begin
	if(iWr)	mem[iAddr] <= iWrData;
 end
 
 always@(posedge iClk) begin
	if(~iWr)	rd_data <= mem[iAddr];
	else		rd_data <= rd_data;
 end
 
 assign oRdData = rd_data;
*/

 //512words x 32bits = 2KBytes
 rspb18_512x32m4_g1 #(.MES_ALL("OFF")) _512w32b (
	.CLK	(iClk),
	.ME		(1'b1),		//input			: Master Enable (ChipSelect)
	.ADR	(iAddr),	//input [8:0]	: Address
	.WE		(iWr),		//input			: Write Enable (1: Write;  0: Read)
	.WEM	(4'hF),		//input [3:0]	: Write Enable Mask
	.D		(iWrData),	//input [31:0]	: Write Data
	.OE		(1'b1),		//input			: Output Enable
	.Q		(oRdData)	//output [31:0]	: Read Data
 );

endmodule

module ed25519_operand_bank (
	input				iClk,
	
	input		[8:0]	iA_addr,
	input				iA_wr,
	input		[31:0]	iA,
	
	input		[8:0]	iB_addr,
	output	reg	[31:0]	oB
);
 
 wire	[8:0]	addr;
 reg	[8:0]	addr_reg;
 wire	[31:0]	mem_out;
 
 assign addr = (iA_wr) ? iA_addr : iB_addr;
 always@(posedge iClk) begin
	addr_reg <= addr;
 end
 
 ram_2kb_32x512 mem (
	.iClk		(iClk),
	.iAddr		(addr),
	.iWr		(iA_wr),	//1: Write,	0: Read
	.iWrData	(iA),
	.oRdData	(mem_out)
 );
 
 always@(*) begin
	case(addr_reg)
		9'd0,
		9'd1,
		9'd2,
		9'd3,
		9'd4,
		9'd5,
		9'd6,
		9'd7:	oB = 32'b0;
		9'd8:	oB = 32'd1;
		9'd9,
		9'd10,
		9'd11,
		9'd12,
		9'd13,
		9'd14,
		9'd15:	oB = 32'b0;
		9'd119:	oB = 32'h216936d3;
		9'd118:	oB = 32'hcd6e53fe;
		9'd117:	oB = 32'hc0a4e231;
		9'd116:	oB = 32'hfdd6dc5c;
		9'd115:	oB = 32'h692cc760;
		9'd114:	oB = 32'h9525a7b2;
		9'd113:	oB = 32'hc9562d60;
		9'd112:	oB = 32'h8f25d51a;
		9'd127:	oB = 32'h66666666;
		9'd126:	oB = 32'h66666666;
		9'd125:	oB = 32'h66666666;
		9'd124:	oB = 32'h66666666;
		9'd123:	oB = 32'h66666666;
		9'd122:	oB = 32'h66666666;
		9'd121:	oB = 32'h66666666;
		9'd120:	oB = 32'h66666658;
		9'd135:	oB = 32'h67875f0f;
		9'd134:	oB = 32'hd78b7665;
		9'd133:	oB = 32'h66ea4e8e;
		9'd132:	oB = 32'h64abe37d;
		9'd131:	oB = 32'h20f09f80;
		9'd130:	oB = 32'h775152f5;
		9'd129:	oB = 32'h6dde8ab3;
		9'd128:	oB = 32'ha5b7dda3;
		default: oB = mem_out;
	endcase
 end

endmodule
