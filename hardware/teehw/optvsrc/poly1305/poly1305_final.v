//======================================================================
//
// poly1305_final.v
// ----------------
// Implementation of the final processing.
//
// Copyright (c) 2020, Assured AB
// Joachim Strömbergson
//
// Redistribution and use in source and binary forms, with or
// without modification, are permitted provided that the following
// conditions are met:
//
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in
//    the documentation and/or other materials provided with the
//    distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//======================================================================

module poly1305_final(
                      input wire          clk,
                      input wire          reset_n,

                      input wire          start,
                      output wire         ready,

                      input wire [31 : 0] h0,
                      input wire [31 : 0] h1,
                      input wire [31 : 0] h2,
                      input wire [31 : 0] h3,
                      input wire [31 : 0] h4,

                      input wire [31 : 0] s0,
                      input wire [31 : 0] s1,
                      input wire [31 : 0] s2,
                      input wire [31 : 0] s3,

                      output wire [31 : 0] hres0,
                      output wire [31 : 0] hres1,
                      output wire [31 : 0] hres2,
                      output wire [31 : 0] hres3
                      );



  //----------------------------------------------------------------
  // Parameters and symbolic values.
  //----------------------------------------------------------------
  localparam PIPE_CYCLES    = 4'h6;

  localparam CTRL_IDLE      = 2'h0;
  localparam CTRL_PIPE_WAIT = 2'h1;


  //----------------------------------------------------------------
  // Registers.
  //----------------------------------------------------------------
  reg [63 : 0] u0_reg;
  reg [63 : 0] u0_new;
  reg [63 : 0] u1_reg;
  reg [63 : 0] u1_new;
  reg [63 : 0] u2_reg;
  reg [63 : 0] u2_new;
  reg [63 : 0] u3_reg;
  reg [63 : 0] u3_new;
  reg [63 : 0] u4_reg;
  reg [63 : 0] u4_new;

  reg [63 : 0] uu0_reg;
  reg [63 : 0] uu0_new;
  reg [63 : 0] uu1_reg;
  reg [63 : 0] uu1_new;
  reg [63 : 0] uu2_reg;
  reg [63 : 0] uu2_new;
  reg [63 : 0] uu3_reg;
  reg [63 : 0] uu3_new;

  reg [3 : 0]  cycle_ctr_reg;
  reg [3 : 0]  cycle_ctr_new;
  reg          cycle_ctr_we;
  reg          cycle_ctr_rst;
  reg          cycle_ctr_inc;

  reg          ready_reg;
  reg          ready_new;
  reg          ready_we;

  reg [1 : 0]  final_ctrl_reg;
  reg [1 : 0]  final_ctrl_new;
  reg          final_ctrl_we;


  //----------------------------------------------------------------
  // Concurrent connectivity for ports etc.
  //----------------------------------------------------------------
  assign ready = ready_reg;

  assign hres0 = uu0_reg[31 : 0];
  assign hres1 = uu1_reg[31 : 0];
  assign hres2 = uu2_reg[31 : 0];
  assign hres3 = uu3_reg[31 : 0];


  //----------------------------------------------------------------
  // reg_update
  //
  // Update functionality for all registers in the core.
  // All registers are positive edge triggered with synchronous
  // active low reset.
  //----------------------------------------------------------------
   always @ (posedge clk)
     begin : reg_update
       if (!reset_n)
         begin
           u0_reg        <= 64'h0;
           u1_reg        <= 64'h0;
           u2_reg        <= 64'h0;
           u3_reg        <= 64'h0;
           u4_reg        <= 64'h0;
           uu0_reg       <= 64'h0;
           uu1_reg       <= 64'h0;
           uu2_reg       <= 64'h0;
           uu3_reg       <= 64'h0;
          cycle_ctr_reg  <= 4'h0;
          ready_reg      <= 1'h1;
          final_ctrl_reg <= CTRL_IDLE;
         end
       else
         begin
           u0_reg  <= u0_new;
           u1_reg  <= u1_new;
           u2_reg  <= u2_new;
           u3_reg  <= u3_new;
           u4_reg  <= u4_new;

           uu0_reg <= uu0_new;
           uu1_reg <= uu1_new;
           uu2_reg <= uu2_new;
           uu3_reg <= uu3_new;

           if (cycle_ctr_we)
             cycle_ctr_reg <= cycle_ctr_new;

           if (ready_we)
             ready_reg <= ready_new;

           if (final_ctrl_we)
             final_ctrl_reg <= final_ctrl_new;
         end
     end // reg_update


   //----------------------------------------------------------------
   // final_logic
   //----------------------------------------------------------------
   always @*
     begin : final_logic
       u0_new = 64'h5                    + {32'h0, h0}; // <= 1_00000004
       u1_new = {32'h0, u0_reg[63 : 32]} + {32'h0, h1}; // <= 1_00000000
       u2_new = {32'h0, u1_reg[63 : 32]} + {32'h0, h2}; // <= 1_00000000
       u3_new = {32'h0, u2_reg[63 : 32]} + {32'h0, h3}; // <= 1_00000000
       u4_new = {32'h0, u3_reg[63 : 32]} + {32'h0, h4}; // <=          5

       uu0_new = (u4_reg[63 : 2] * 5)      + {32'h0, h0} + {32'h0, s0}; // <= 2_00000003
       uu1_new = {32'h0, uu0_reg[63 : 32]} + {32'h0, h1} + {32'h0, s1}; // <= 2_00000000
       uu2_new = {32'h0, uu1_reg[63 : 32]} + {32'h0, h2} + {32'h0, s2}; // <= 2_00000000
       uu3_new = {32'h0, uu2_reg[63 : 32]} + {32'h0, h3} + {32'h0, s3}; // <= 2_00000000
     end


  //----------------------------------------------------------------
  // cycle_ctr
  //----------------------------------------------------------------
  always @*
    begin : cycle_ctr
      cycle_ctr_new = 4'h0;
      cycle_ctr_we  = 1'h0;

      if (cycle_ctr_rst)
        begin
          cycle_ctr_new = 4'h0;
          cycle_ctr_we  = 1'h1;
        end
      else if (cycle_ctr_inc)
        begin
          cycle_ctr_new = cycle_ctr_reg + 1'h1;
          cycle_ctr_we  = 1'h1;
        end
    end


  //----------------------------------------------------------------
  // final_ctrl
  //----------------------------------------------------------------
  always @*
    begin : final_ctrl
      ready_new      = 1'h1;
      ready_we       = 1'h0;
      cycle_ctr_rst  = 1'h0;
      cycle_ctr_inc  = 1'h0;
      final_ctrl_new = CTRL_IDLE;
      final_ctrl_we  = 1'h0;

      case (final_ctrl_reg)
        CTRL_IDLE:
          begin
            if (start)
              begin
                ready_new      = 1'h0;
                ready_we       = 1'h1;
                cycle_ctr_rst  = 1'h1;
                final_ctrl_new = CTRL_PIPE_WAIT;
                final_ctrl_we  = 1'h1;
              end
          end

        CTRL_PIPE_WAIT:
          begin
            cycle_ctr_inc = 1'h1;
            if (cycle_ctr_reg == PIPE_CYCLES)
              begin
                ready_new      = 1'h1;
                ready_we       = 1'h1;
                final_ctrl_new = CTRL_IDLE;
                final_ctrl_we  = 1'h1;
              end
          end

        default:
          begin
          end
      endcase // case (final_ctrl_reg)
    end // block: final_ctrl

endmodule // poly1305_final

//======================================================================
// EOF poly1305_final.v
//======================================================================
