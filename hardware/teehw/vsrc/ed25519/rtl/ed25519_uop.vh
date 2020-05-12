//======================================================================
//
// Copyright (c) 2018, NORDUnet A/S All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// - Redistributions of source code must retain the above copyright
//   notice, this list of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright
//   notice, this list of conditions and the following disclaimer in the
//   documentation and/or other materials provided with the distribution.
//
// - Neither the name of the NORDUnet nor the names of its contributors may
//   be used to endorse or promote products derived from this software
//   without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
// IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
// PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
// TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//======================================================================

localparam integer UOP_ADDR_WIDTH    = 9;    // 2 ^ 9 = max 512 instructions

localparam integer UOP_DATA_WIDTH = 5 + 1 + 3 * 6;  // opcode + banks + 3 * operand (2 * src + dst)

localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_PREPARE          = 9'd000;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_BEFORE_ROUND_K0  = 9'd009;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_BEFORE_ROUND_K1  = 9'd018;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_DURING_ROUND     = 9'd027;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_AFTER_ROUND_K0   = 9'd062;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_AFTER_ROUND_K1   = 9'd067;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_BEFORE_INVERSION = 9'd072;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_DURING_INVERSION = 9'd075;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_AFTER_INVERSION  = 9'd354;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_FINAL_REDUCTION  = 9'd358;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_HANDLE_SIGN      = 9'd361;
localparam [UOP_ADDR_WIDTH-1:0] UOP_OFFSET_OUTPUT           = 9'd363;

localparam [4:0] UOP_OPCODE_COPY    = 5'b00001;
localparam [4:0] UOP_OPCODE_ADD     = 5'b00010;
localparam [4:0] UOP_OPCODE_SUB     = 5'b00100;
localparam [4:0] UOP_OPCODE_MUL     = 5'b01000;
localparam [4:0] UOP_OPCODE_STOP    = 5'b10000;

localparam UOP_BANKS_LO2HI  = 1'b0;
localparam UOP_BANKS_HI2LO  = 1'b1;
localparam UOP_BANKS_DUMMY  = 1'bX;

localparam [5:0] UOP_OPERAND_CONST_ZERO     = 6'd00;
localparam [5:0] UOP_OPERAND_CONST_ONE      = 6'd01;

localparam [5:0] UOP_OPERAND_INVERT_R1      = 6'd02;
localparam [5:0] UOP_OPERAND_INVERT_R2      = 6'd03;

localparam [5:0] UOP_OPERAND_INVERT_T_1     = 6'd04;
localparam [5:0] UOP_OPERAND_INVERT_T_10    = 6'd05;
localparam [5:0] UOP_OPERAND_INVERT_T_1001  = 6'd06;
localparam [5:0] UOP_OPERAND_INVERT_T_1011  = 6'd07;

localparam [5:0] UOP_OPERAND_INVERT_T_X5    = 6'd08;
localparam [5:0] UOP_OPERAND_INVERT_T_X10   = 6'd09;
localparam [5:0] UOP_OPERAND_INVERT_T_X20   = 6'd10;
localparam [5:0] UOP_OPERAND_INVERT_T_X40   = 6'd11;
localparam [5:0] UOP_OPERAND_INVERT_T_X50   = 6'd12;
localparam [5:0] UOP_OPERAND_INVERT_T_X100  = 6'd13;

localparam [5:0] UOP_OPERAND_CONST_G_X      = 6'd14;
localparam [5:0] UOP_OPERAND_CONST_G_Y      = 6'd15;
localparam [5:0] UOP_OPERAND_CONST_G_T      = 6'd16;

localparam [5:0] UOP_OPERAND_CYCLE_R0_X     = 6'd17;
localparam [5:0] UOP_OPERAND_CYCLE_R0_Y     = 6'd18;
localparam [5:0] UOP_OPERAND_CYCLE_R0_Z     = 6'd19;
localparam [5:0] UOP_OPERAND_CYCLE_R0_T     = 6'd20;

localparam [5:0] UOP_OPERAND_CYCLE_R1_X     = 6'd21;
localparam [5:0] UOP_OPERAND_CYCLE_R1_Y     = 6'd22;
localparam [5:0] UOP_OPERAND_CYCLE_R1_Z     = 6'd23;
localparam [5:0] UOP_OPERAND_CYCLE_R1_T     = 6'd24;

localparam [5:0] UOP_OPERAND_CYCLE_S_X      = 6'd25;
localparam [5:0] UOP_OPERAND_CYCLE_S_Y      = 6'd26;
localparam [5:0] UOP_OPERAND_CYCLE_S_Z      = 6'd27;
localparam [5:0] UOP_OPERAND_CYCLE_S_T      = 6'd28;

localparam [5:0] UOP_OPERAND_CYCLE_T_X      = 6'd29;
localparam [5:0] UOP_OPERAND_CYCLE_T_Y      = 6'd30;
localparam [5:0] UOP_OPERAND_CYCLE_T_Z      = 6'd31;
localparam [5:0] UOP_OPERAND_CYCLE_T_T      = 6'd32;

localparam [5:0] UOP_OPERAND_CYCLE_U_X      = 6'd33;
localparam [5:0] UOP_OPERAND_CYCLE_U_Y      = 6'd34;
localparam [5:0] UOP_OPERAND_CYCLE_U_Z      = 6'd35;
localparam [5:0] UOP_OPERAND_CYCLE_U_T      = 6'd36;

localparam [5:0] UOP_OPERAND_CYCLE_V_X      = 6'd37;
localparam [5:0] UOP_OPERAND_CYCLE_V_Y      = 6'd38;
localparam [5:0] UOP_OPERAND_CYCLE_V_Z      = 6'd39;
localparam [5:0] UOP_OPERAND_CYCLE_V_T      = 6'd40;

localparam [5:0] UOP_OPERAND_PROC_A         = 6'd41;
localparam [5:0] UOP_OPERAND_PROC_B         = 6'd42;
localparam [5:0] UOP_OPERAND_PROC_C         = 6'd43;
localparam [5:0] UOP_OPERAND_PROC_D         = 6'd44;
localparam [5:0] UOP_OPERAND_PROC_E         = 6'd45;
localparam [5:0] UOP_OPERAND_PROC_F         = 6'd46;
localparam [5:0] UOP_OPERAND_PROC_G         = 6'd47;
localparam [5:0] UOP_OPERAND_PROC_H         = 6'd48;
localparam [5:0] UOP_OPERAND_PROC_I         = 6'd49;
localparam [5:0] UOP_OPERAND_PROC_J         = 6'd50;

localparam [5:0] UOP_OPERAND_DONTCARE       = 6'hxx;


//------------------------------------------------------------------------------
// End-of-File
//------------------------------------------------------------------------------
