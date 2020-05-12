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

/* if "oAck" is 1, then current input has been used. */

module f_permutation(
	input				iClk,
	input				iRst,
	input		[575:0]	iData,
	input				iReady,
	output				oAck,
	output		[511:0]	oData
	//output	reg		oReady
);

/*****************************************************************************
 *                 Internal Wires and Registers Declarations                 *
 *****************************************************************************/
 
 reg	[10:0]		i; /* select round constant */
 reg	[1599:0]	round;
 wire	[1599:0]	round_in, round_out;
 wire	[6:0]		rc1, rc2;
 wire				update;
 wire				accept;
 
 reg				calc; /* == 1: calculating rounds */

/*****************************************************************************
 *                            Combinational Logic                            *
 *****************************************************************************/

 assign accept = iReady & ~calc;	//iReady & (i==0)
 assign round_in = (accept) ? {iData^round[1599:1024], round[1023:0]} : round;
 
 assign update = calc | accept;
 assign oAck = accept;
 assign oData = round[1599:1088];

/*****************************************************************************
 *                             Sequential Logic                              *
 *****************************************************************************/
 
 always@(posedge iClk) begin
	if(iRst)	i <= 11'b0;
	else		i <= {i[9:0], accept};
 end
 
 always@(posedge iClk) begin
	if(iRst)	calc <= 1'b0;
	else		calc <= (calc & ~i[10]) | accept;
 end
/*
 always@(posedge iClk) begin
	if(iRst | accept)	oReady <= 1'b0;
	// only change at the last round
	else if(i[10])		oReady <= 1'b1;
	else				oReady <= oReady;
 end
*/
 always@(posedge iClk) begin
	if(iRst)		round <= 1600'b0;
	else if(update)	round <= round_out;
	else			round <= round;
 end

/*****************************************************************************
 *                              Internal Modules                             *
 *****************************************************************************/
 
 rconst2in1 rconst_ (
	.i		({i, accept}),
	.rc1	(rc1),
	.rc2	(rc2)
 );

 round2in1 round_ (
	.in		(round_in),
	.rc1	(rc1),
	.rc2	(rc2),
	.out	(round_out)
 );

endmodule
