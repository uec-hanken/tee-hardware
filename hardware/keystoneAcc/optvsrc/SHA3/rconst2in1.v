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

/* round constant (2 in 1 ~ ~) */
module rconst2in1(
	input	[11:0]	i,
	output	[6:0]	rc1,
	output	[6:0]	rc2
);
/*
 always@(i) begin
	rc1 = 0;
	rc1[0] = i[0] | i[2] | i[3] | i[5] | i[6] | i[7] | i[10] | i[11];
	rc1[1] = i[1] | i[2] | i[4] | i[6] | i[8] | i[9];
	rc1[2] = i[1] | i[2] | i[4] | i[5] | i[6] | i[7] | i[9];
	rc1[3] = i[1] | i[2] | i[3] | i[4] | i[6] | i[7] | i[10];
	rc1[4] = i[1] | i[2] | i[3] | i[5] | i[6] | i[7] | i[8] | i[9] | i[10];
	rc1[5] = i[3] | i[5] | i[6] | i[10] | i[11];
	rc1[6] = i[1] | i[3] | i[7] | i[8] | i[10];
 end

 always@(i) begin
	rc2 = 0;
	rc2[0] = i[2] | i[3] | i[6] | i[7];
	rc2[1] = i[0] | i[5] | i[6] | i[7] | i[9];
	rc2[2] = i[3] | i[4] | i[5] | i[6] | i[9] | i[11];
	rc2[3] = i[0] | i[4] | i[6] | i[8] | i[10];
	rc2[4] = i[0] | i[1] | i[3] | i[7] | i[10] | i[11];
	rc2[5] = i[1] | i[2] | i[5] | i[9] | i[11];
	rc2[6] = i[1] | i[3] | i[6] | i[7] | i[8] | i[9] | i[10] | i[11];
 end
*/

 assign rc1[6] = i[1] | i[3] | i[7] | i[8] | i[10];
 assign rc1[5] = i[3] | i[5] | i[6] | i[10] | i[11];
 assign rc1[4] = i[1] | i[2] | i[3] | i[5] | i[6] | i[7] | i[8] | i[9] | i[10];
 assign rc1[3] = i[1] | i[2] | i[3] | i[4] | i[6] | i[7] | i[10];
 assign rc1[2] = i[1] | i[2] | i[4] | i[5] | i[6] | i[7] | i[9];
 assign rc1[1] = i[1] | i[2] | i[4] | i[6] | i[8] | i[9];
 assign rc1[0] = i[0] | i[2] | i[3] | i[5] | i[6] | i[7] | i[10] | i[11];
 
 assign rc2[6] = i[1] | i[3] | i[6] | i[7] | i[8] | i[9] | i[10] | i[11];
 assign rc2[5] = i[1] | i[2] | i[5] | i[9] | i[11];
 assign rc2[4] = i[0] | i[1] | i[3] | i[7] | i[10] | i[11];
 assign rc2[3] = i[0] | i[4] | i[6] | i[8] | i[10];
 assign rc2[2] = i[3] | i[4] | i[5] | i[6] | i[9] | i[11];
 assign rc2[1] = i[0] | i[5] | i[6] | i[7] | i[9];
 assign rc2[0] = i[2] | i[3] | i[6] | i[7];
 
endmodule
