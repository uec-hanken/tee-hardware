/*
 * Copyright 2013, Homer Hsing <homer.hsing@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

`timescale 1ns / 1ps
`define P 10

module test_keccak;

 // Inputs
 reg			iClk;
 reg			iRst;
 reg	[63:0]	iData;
 reg			iReady;
 reg			iLast;
 reg	[2:0]	iByte_num;

 // Outputs
 wire			oBuffer_full;
 wire	[511:0]	oData;
 wire			oReady;

 integer i;

 // Instantiate the Unit Under Test (UUT)
 SHA3_TOP uut (
	.ICLK			(iClk),
	.IRST			(iRst),
	.IDATA			(iData),
	.IREADY			(iReady),
	.ILAST			(iLast),
	.IBYTE_NUM		(iByte_num),
	.OBUFFER_FULL	(oBuffer_full),
	.ODATA			(oData),
	.OREADY			(oReady)
 );

 always #(`P/2) iClk = ~ iClk;
 
 initial begin
	$dumpfile("test_keccak.vcd");
	$dumpvars;
	
	// Initialize Inputs
	iClk = 0;
	iRst = 0;
	iData = 0;
	iReady = 0;
	iLast = 0;
	iByte_num = 0;

	// Wait 100 ns for global iRst to finish
	#100;

	// Add stimulus here
	@(negedge iClk);

	// SHA3-512("The quick brown fox jumps over the lazy dog")
	iRst = 1; #(`P); iRst = 0;
	iReady = 1; iLast = 0;
	iData = "The quic"; #(`P);
	iData = "k brown "; #(`P);
	iData = "fox jump"; #(`P);
	iData = "s over t"; #(`P);
	iData = "he lazy "; #(`P);
	iData = "dog     "; iByte_num = 3; iLast = 1; #(`P); /* !!! not iData = "dog" */
	iReady = 0; iLast = 0;
	while(oReady !== 1)
		#(`P);
	check(512'h01dedd5de4ef14642445ba5f5b97c15e47b9ad931326e4b0727cd94cefc44fff23f07bf543139939b49128caf436dc1bdee54fcb24023a08d9403f9b4bf0d450);

	#((`P)*100);

	// SHA3-512("The quick brown fox jumps over the lazy dog.")
	iRst = 1; #(`P); iRst = 0;
	iReady = 1; iLast = 0;
	iData = "The quic"; #(`P);
	iData = "k brown "; #(`P);
	iData = "fox jump"; #(`P);
	iData = "s over t"; #(`P);
	iData = "he lazy "; #(`P);
	iData = "dog.    "; iByte_num = 4; iLast = 1; #(`P); /* !!! not iData = "dog." */
	iReady = 0; iLast = 0;
	while(oReady !== 1)
		#(`P);
	check(512'h18f4f4bd419603f95538837003d9d254c26c23765565162247483f65c50303597bc9ce4d289f21d1c2f1f458828e33dc442100331b35e7eb031b5d38ba6460f8);

	// hash an string "\xA1\xA2\xA3\xA4\xA5", len == 5
	iRst = 1; #(`P); iRst = 0;
	#(7*`P); // wait some cycles
	iData = 64'hA1A2A3A4A5000000;
	iByte_num = 5;
	iReady = 1;
	iLast = 1;
	#(`P);
	iData = 64'h12345678; // next input
	iReady = 1;
	iLast = 1;
	#(`P/2);
	if (oBuffer_full === 1) error; // should be 0
	#(`P/2);
	iReady = 0;
	iLast = 0;

	while(oReady !== 1)
		#(`P);
	check(512'hedc8d5dd93da576838a856c71c5ba87d359445b0589e75e6f67bb8e41a05e78876835d5254d27e0b1445ab49599ff30952a83765858f1e47332835eee6af43f9);
	for(i=0; i<5; i=i+1) begin
		#(`P);
		if(oBuffer_full !== 0) error; // should keep 0
	end

	// hash an empty string, should not eat next input
	iRst = 1; #(`P); iRst = 0;
	#(7*`P); // wait some cycles
	iData = 64'h12345678; // should not be eat
	iByte_num = 0;
	iReady = 1;
	iLast = 1;
	#(`P);
	iData = 64'hddddd; // should not be eat
	iReady = 1; // next input
	iLast = 1;
	#(`P);
	iReady = 0;
	iLast = 0;

	while(oReady !== 1)
		#(`P);
	check(512'ha69f73cca23a9ac5c8b567dc185a756e97c982164fe25859e0d1dcc1475c80a615b2123af1f5f94c11e3e9402c3ac558f500199d95b6d3e301758586281dcd26);
	for(i=0; i<5; i=i+1) begin
		#(`P);
		if(oBuffer_full !== 0) error; // should keep 0
	end

	// hash an (576-8) bit string
	iRst = 1; #(`P); iRst = 0;
	#(4*`P); // wait some cycles
	iReady = 1;
	iByte_num = 7; /* should have no effect */
	iLast = 0;
	for(i=0; i<8; i=i+1) begin
		iData = 64'hEFCDAB9078563412;
		#(`P);
	end
	iLast = 1;
	#(`P);
	iReady = 0;
	iLast = 0;
	while(oReady !== 1)
		#(`P);
	check(512'h6297e8688a3be8cd99b244147f001b0f1ad4667868e8ddfbc58ec0236bd8b2ad99418ba5fec47c3f0f787243958229da6eb48ce7c6c78a929497fddd098d1a28);

	// pad an (576-64) bit string
	iRst = 1; #(`P); iRst = 0;
	// don't wait any cycle
	iReady = 1;
	iByte_num = 7; /* should have no effect */
	iLast = 0;
	for(i=0; i<8; i=i+1) begin
		iData = 64'hEFCDAB9078563412;
		#(`P);
	end
	iLast = 1;
	iByte_num = 0;
	#(`P);
	iReady = 0;
	iLast = 0;
	iData = 0;
	while(oReady !== 1)
		#(`P);
	check(512'h2b276100c85f018d06c4549073e849e39eec1d0c2a4e9b1a98b1411d0b1ca86570201b284c0d9bf4680c5507fa28db6952d957e200b231ca878a7f2db0d1b851);

	// pad an (576*2-16) bit string
	iRst = 1; #(`P); iRst = 0;
	iReady = 1;
	iByte_num = 1; /* should have no effect */
	iLast = 0;
	for(i=0; i<9; i=i+1) begin
		iData = 64'hEFCDAB9078563412; #(`P);
	end
	#(`P/2);
	if(oBuffer_full !== 1) error; // should not eat
	#(`P/2);
	iData = 64'h999; // should not eat this
	iReady = 0;
	#(`P/2);
	if(oBuffer_full !== 0) error; // should not eat, but buffer should not be full
	#(`P/2);
	#(`P);
	// feed next (576-16) bit
	iReady = 1;
	for(i=0; i<8; i=i+1) begin
		iData = 64'hEFCDAB9078563412; #(`P);
	end
	iByte_num = 6;
	iLast = 1;
	iData = 64'hEFCDAB9078563412;
	#(`P);
	iLast = 0;
	iReady = 0;
	while(oReady !== 1)
		#(`P);
	check(512'h2d9bb7afb83773be6d0d5a5518198b416bf283850bcaa8237a71a006558956ff1f8824eab7bf9b549cd273cc05adccd7e888ed2dda17cf07c32e0db1ffa1d3df);

	$display("Good!");
	//$finish;
 end

 task error;
 begin
	$display("E");
	//$finish;
 end
 endtask

 task check;
 input [511:0] wish;
 begin
	if(oData !== wish) begin
		$display("%h %h", oData, wish); error;
	end
 end
 endtask
 
endmodule

`undef P
