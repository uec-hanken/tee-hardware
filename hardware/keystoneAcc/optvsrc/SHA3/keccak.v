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

/* "iLast" == 0 means byte number is 8, no matter what value "iByte_num" is. */
/* if "iReady" == 0, then "iLast" should be 0. */
/* the user switch to next "iData" only if "ack" == 1. */

module keccak(
	input				iClk,
	input				iRst,
	input		[63:0]	iData,
	input				iReady,
	input				iLast,
	input		[2:0]	iByte_num,
	output				oBuffer_full, /* to "user" module */
	output		[511:0]	oData,
	output	reg			oReady
);

/*****************************************************************************
 *                 Internal Wires and Registers Declarations                 *
 *****************************************************************************/
 
 reg			state;	/* state == 0: user will send more input data
						 * state == 1: user will not send any data */
 reg	[10:0]	i; /* gen "oReady" */
 
 wire	[575:0]	padder_oData,
				padder_oData_pre; /* before reorder byte */
 wire			padder_oReady;
 
 wire			f_oAck;
 wire	[511:0]	f_oData;
 //wire			f_oReady;
 
 wire	[511:0]	Data_out_pre; /* before reorder byte */
 wire	[63:0]	Data_out_pre_7, Data_out_pre_6, Data_out_pre_5, Data_out_pre_4,
				Data_out_pre_3, Data_out_pre_2, Data_out_pre_1, Data_out_pre_0;
 wire	[63:0]	Data_out_7, Data_out_6, Data_out_5, Data_out_4,
				Data_out_3, Data_out_2, Data_out_1, Data_out_0;

 wire	[63:0]	padder_oData_pre_8, padder_oData_pre_7, padder_oData_pre_6,
				padder_oData_pre_5, padder_oData_pre_4, padder_oData_pre_3,
				padder_oData_pre_2, padder_oData_pre_1, padder_oData_pre_0;
 wire	[63:0]	padder_oData_8, padder_oData_7, padder_oData_6,
				padder_oData_5, padder_oData_4, padder_oData_3,
				padder_oData_2, padder_oData_1, padder_oData_0;
 
/*****************************************************************************
 *                            Combinational Logic                            *
 *****************************************************************************/
 
 /* reorder byte ~ ~ */
 assign Data_out_pre = f_oData;
 
 assign {Data_out_pre_7, Data_out_pre_6, Data_out_pre_5, Data_out_pre_4,
		 Data_out_pre_3, Data_out_pre_2, Data_out_pre_1, Data_out_pre_0} = Data_out_pre;

 assign Data_out_7 = {Data_out_pre_7[7:0]  , Data_out_pre_7[15:8] , Data_out_pre_7[23:16], Data_out_pre_7[31:24],
					  Data_out_pre_7[39:32], Data_out_pre_7[47:40], Data_out_pre_7[55:48], Data_out_pre_7[63:56]};
 assign Data_out_6 = {Data_out_pre_6[7:0]  , Data_out_pre_6[15:8] , Data_out_pre_6[23:16], Data_out_pre_6[31:24],
					  Data_out_pre_6[39:32], Data_out_pre_6[47:40], Data_out_pre_6[55:48], Data_out_pre_6[63:56]};
 assign Data_out_5 = {Data_out_pre_5[7:0]  , Data_out_pre_5[15:8] , Data_out_pre_5[23:16], Data_out_pre_5[31:24],
					  Data_out_pre_5[39:32], Data_out_pre_5[47:40], Data_out_pre_5[55:48], Data_out_pre_5[63:56]};
 assign Data_out_4 = {Data_out_pre_4[7:0]  , Data_out_pre_4[15:8] , Data_out_pre_4[23:16], Data_out_pre_4[31:24],
					  Data_out_pre_4[39:32], Data_out_pre_4[47:40], Data_out_pre_4[55:48], Data_out_pre_4[63:56]};
 assign Data_out_3 = {Data_out_pre_3[7:0]  , Data_out_pre_3[15:8] , Data_out_pre_3[23:16], Data_out_pre_3[31:24],
					  Data_out_pre_3[39:32], Data_out_pre_3[47:40], Data_out_pre_3[55:48], Data_out_pre_3[63:56]};
 assign Data_out_2 = {Data_out_pre_2[7:0]  , Data_out_pre_2[15:8] , Data_out_pre_2[23:16], Data_out_pre_2[31:24],
					  Data_out_pre_2[39:32], Data_out_pre_2[47:40], Data_out_pre_2[55:48], Data_out_pre_2[63:56]};
 assign Data_out_1 = {Data_out_pre_1[7:0]  , Data_out_pre_1[15:8] , Data_out_pre_1[23:16], Data_out_pre_1[31:24],
					  Data_out_pre_1[39:32], Data_out_pre_1[47:40], Data_out_pre_1[55:48], Data_out_pre_1[63:56]};
 assign Data_out_0 = {Data_out_pre_0[7:0]  , Data_out_pre_0[15:8] , Data_out_pre_0[23:16], Data_out_pre_0[31:24],
					  Data_out_pre_0[39:32], Data_out_pre_0[47:40], Data_out_pre_0[55:48], Data_out_pre_0[63:56]};

 assign oData = {Data_out_7, Data_out_6, Data_out_5, Data_out_4, Data_out_3, Data_out_2, Data_out_1, Data_out_0};

 assign {padder_oData_pre_8, padder_oData_pre_7, padder_oData_pre_6,
		 padder_oData_pre_5, padder_oData_pre_4, padder_oData_pre_3,
		 padder_oData_pre_2, padder_oData_pre_1, padder_oData_pre_0} = padder_oData_pre;
				 
 assign padder_oData_8 = {padder_oData_pre_8[7:0]  , padder_oData_pre_8[15:8] ,
						  padder_oData_pre_8[23:16], padder_oData_pre_8[31:24],
						  padder_oData_pre_8[39:32], padder_oData_pre_8[47:40],
						  padder_oData_pre_8[55:48], padder_oData_pre_8[63:56]};
 assign padder_oData_7 = {padder_oData_pre_7[7:0]  , padder_oData_pre_7[15:8] ,
						  padder_oData_pre_7[23:16], padder_oData_pre_7[31:24],
						  padder_oData_pre_7[39:32], padder_oData_pre_7[47:40],
						  padder_oData_pre_7[55:48], padder_oData_pre_7[63:56]};
 assign padder_oData_6 = {padder_oData_pre_6[7:0]  , padder_oData_pre_6[15:8] ,
						  padder_oData_pre_6[23:16], padder_oData_pre_6[31:24],
						  padder_oData_pre_6[39:32], padder_oData_pre_6[47:40],
						  padder_oData_pre_6[55:48], padder_oData_pre_6[63:56]};
 assign padder_oData_5 = {padder_oData_pre_5[7:0]  , padder_oData_pre_5[15:8] ,
						  padder_oData_pre_5[23:16], padder_oData_pre_5[31:24],
						  padder_oData_pre_5[39:32], padder_oData_pre_5[47:40],
						  padder_oData_pre_5[55:48], padder_oData_pre_5[63:56]};
 assign padder_oData_4 = {padder_oData_pre_4[7:0]  , padder_oData_pre_4[15:8] ,
						  padder_oData_pre_4[23:16], padder_oData_pre_4[31:24],
						  padder_oData_pre_4[39:32], padder_oData_pre_4[47:40],
						  padder_oData_pre_4[55:48], padder_oData_pre_4[63:56]};
 assign padder_oData_3 = {padder_oData_pre_3[7:0]  , padder_oData_pre_3[15:8] ,
						  padder_oData_pre_3[23:16], padder_oData_pre_3[31:24],
						  padder_oData_pre_3[39:32], padder_oData_pre_3[47:40],
						  padder_oData_pre_3[55:48], padder_oData_pre_3[63:56]};
 assign padder_oData_2 = {padder_oData_pre_2[7:0]  , padder_oData_pre_2[15:8] ,
						  padder_oData_pre_2[23:16], padder_oData_pre_2[31:24],
						  padder_oData_pre_2[39:32], padder_oData_pre_2[47:40],
						  padder_oData_pre_2[55:48], padder_oData_pre_2[63:56]};
 assign padder_oData_1 = {padder_oData_pre_1[7:0]  , padder_oData_pre_1[15:8] ,
						  padder_oData_pre_1[23:16], padder_oData_pre_1[31:24],
						  padder_oData_pre_1[39:32], padder_oData_pre_1[47:40],
						  padder_oData_pre_1[55:48], padder_oData_pre_1[63:56]};
 assign padder_oData_0 = {padder_oData_pre_0[7:0]  , padder_oData_pre_0[15:8] ,
						  padder_oData_pre_0[23:16], padder_oData_pre_0[31:24],
						  padder_oData_pre_0[39:32], padder_oData_pre_0[47:40],
						  padder_oData_pre_0[55:48], padder_oData_pre_0[63:56]};
	
 assign padder_oData = {padder_oData_8, padder_oData_7, padder_oData_6,
						padder_oData_5, padder_oData_4, padder_oData_3,
						padder_oData_2, padder_oData_1, padder_oData_0};

/*****************************************************************************
 *                             Sequential Logic                              *
 *****************************************************************************/
 
 always@(posedge iClk) begin
	if(iRst)	i <= 11'b0;
	else		i <= {i[9:0], state & f_oAck};
 end

 always@(posedge iClk) begin
	if(iRst)		state <= 1'b0;
	else if(iLast)	state <= 1'b1;
	else			state <= state;
 end

 always@(posedge iClk) begin
	if(iRst)		oReady <= 1'b0;
	else if(i[10])	oReady <= 1'b1;
	else			oReady <= oReady;
 end
 
/*****************************************************************************
 *                              Internal Modules                             *
 *****************************************************************************/
 
 padder padder_ (
	.iClk			(iClk),
	.iRst			(iRst),
	.iData			(iData),
	.iReady			(iReady),
	.iLast			(iLast),
	.iByte_num		(iByte_num),
	.oBuffer_full	(oBuffer_full),
	.oData			(padder_oData_pre),
	.oReady			(padder_oReady),
	.iF_ack			(f_oAck)
 );

 f_permutation f_permutation_ (
	.iClk		(iClk),
	.iRst		(iRst),
	.iData		(padder_oData),
	.iReady		(padder_oReady),
	.oAck		(f_oAck),
	.oData		(f_oData)
	//.oReady	(f_oReady)
 );

endmodule
