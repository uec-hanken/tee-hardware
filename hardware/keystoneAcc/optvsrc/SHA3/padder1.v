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

module padder1(
	input	[63:0]	in,
	input	[2:0]	byte_num,
	output	[63:0]	out
);
/*
 case (byte_num)
	0: out =             64'h0600000000000000;
	1: out = {in[63:56], 56'h06000000000000};
	2: out = {in[63:48], 48'h060000000000};
	3: out = {in[63:40], 40'h0600000000};
	4: out = {in[63:32], 32'h06000000};
	5: out = {in[63:24], 24'h060000};
	6: out = {in[63:16], 16'h0600};
	7: out = {in[63:8],   8'h06};
 endcase
*/
/*
 assign out[63:56] = (byte_num == 3'd0) ? 8'h06 : in[63:56];
 assign out[55:48] = (byte_num < 3'd1)  ? 8'h00 :
					 (byte_num == 3'd1) ? 8'h06 : in[55:48];
 assign out[47:40] = (byte_num < 3'd2)  ? 8'h00 :
					 (byte_num == 3'd2) ? 8'h06 : in[47:40];
 assign out[39:32] = (byte_num < 3'd3)  ? 8'h00 :
					 (byte_num == 3'd3) ? 8'h06 : in[39:32];
 assign out[31:24] = (byte_num < 3'd4)  ? 8'h00 :
					 (byte_num == 3'd4) ? 8'h06 : in[31:24];
 assign out[23:16] = (byte_num < 3'd5)  ? 8'h00 :
					 (byte_num == 3'd5) ? 8'h06 : in[23:16];
 assign out[15:8]  = (byte_num < 3'd6)  ? 8'h00 :
					 (byte_num == 3'd6) ? 8'h06 : in[15:8];
 assign out[7:0]   = (byte_num < 3'd7)  ? 8'h00 : 8'h06;
*/

 wire	byte0, byte1, byte2, byte3,
		byte4, byte5, byte6, byte7;

 wire	byte01, byte012, byte0123, byte567, byte67;

 assign byte0 = ~byte_num[2] & ~byte_num[1] & ~byte_num[0];
 assign byte1 = ~byte_num[2] & ~byte_num[1] &  byte_num[0];
 assign byte2 = ~byte_num[2] &  byte_num[1] & ~byte_num[0];
 assign byte3 = ~byte_num[2] &  byte_num[1] &  byte_num[0];
 assign byte4 =  byte_num[2] & ~byte_num[1] & ~byte_num[0];
 assign byte5 =  byte_num[2] & ~byte_num[1] &  byte_num[0];
 assign byte6 =  byte_num[2] &  byte_num[1] & ~byte_num[0];
 assign byte7 =  byte_num[2] &  byte_num[1] &  byte_num[0];
 
 assign byte01   = byte0   | byte1;
 assign byte012  = byte01  | byte2;
 assign byte0123 = byte012 | byte3;
 assign byte567  = byte5   | byte67;
 assign byte67   = byte6   | byte7;

 assign out[63:60] = {(4){~byte0}} & in[63:60];
 assign out[59]    = ~byte0 & in[59];
 assign out[58:57] = {(2){byte0}} | in[58:57];
 assign out[56]    = ~byte0 & in[56];
 assign out[55:52] = {(4){~byte01}} & in[55:52];
 assign out[51]    = ~byte01 & in[51];
 assign out[50:49] = {(2){~byte0}} & ({(2){byte1}} | in[50:49]);
 assign out[48]    = ~byte01 & in[48];
 assign out[47:44] = {(4){~byte012}} & in[47:44];
 assign out[43]    = ~byte012 & in[43];
 assign out[42:41] = {(2){~byte01}} & ({(2){byte2}} | in[42:41]);
 assign out[40]    = ~byte012 & in[40];
 assign out[39:36] = {(4){~byte0123}} & in[39:36];
 assign out[35]    = ~byte0123 & in[35];
 assign out[34:33] = {(2){~byte012}} & ({(2){byte3}} | in[34:33]);
 assign out[32]    = ~byte0123 & in[32];
 assign out[31:28] = {(4){byte567}} & in[31:28];
 assign out[27]    = byte567 & in[27];
 assign out[26:25] = {(2){byte4}} | ({(2){byte567}} & in[26:25]);
 assign out[24]    = byte567 & in[24];
 assign out[23:20] = {(4){byte67}} & in[23:20];
 assign out[19]    = byte67 & in[19];
 assign out[18:17] = {(2){byte5}} | ({(2){byte67}} & in[18:17]);
 assign out[16]    = byte67 & in[16];
 assign out[15:12] = {(4){byte7}} & in[15:12];
 assign out[11]    = byte7 & in[11];
 assign out[10:9]  = {(2){byte6}} | ({(2){byte7}} & in[10:9]);
 assign out[8]     = byte7 & in[8];
 assign out[7:3]   = 5'b00000;
 assign out[2:1]   = {(2){byte7}};
 assign out[0]     = 1'b0;

endmodule
