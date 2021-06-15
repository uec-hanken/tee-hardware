//======================================================================
//
// poly1305_pblock.v
// -----------------
// Implementation of the polynomial processing of a block.
//
// Copyright (c) 2017, Assured AB
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

module poly1305_pblock(
                       input wire          clk,
                       input wire          reset_n,

                       input wire          start,
                       output wire         ready,

                       input wire [31 : 0] h0,
                       input wire [31 : 0] h1,
                       input wire [31 : 0] h2,
                       input wire [31 : 0] h3,
                       input wire [31 : 0] h4,

                       input wire [31 : 0] c0,
                       input wire [31 : 0] c1,
                       input wire [31 : 0] c2,
                       input wire [31 : 0] c3,
                       input wire [31 : 0] c4,

                       input wire [31 : 0] r0,
                       input wire [31 : 0] r1,
                       input wire [31 : 0] r2,
                       input wire [31 : 0] r3,

                       output wire [31 : 0] h0_new,
                       output wire [31 : 0] h1_new,
                       output wire [31 : 0] h2_new,
                       output wire [31 : 0] h3_new,
                       output wire [31 : 0] h4_new
                      );


  //----------------------------------------------------------------
  // Parameters and symbolic values.
  //----------------------------------------------------------------
  localparam PRE_CYCLES  = 4'h1;
  localparam POST_CYCLES = 4'h2;

  localparam CTRL_IDLE      = 4'h0;
  localparam CTRL_PRE_WAIT  = 4'h1;
  localparam CTRL_MULACC    = 4'h2;
  localparam CTRL_POST_WAIT = 4'h3;


  //----------------------------------------------------------------
  // Registers (Variables)
  //----------------------------------------------------------------
  reg [63 : 0] u0_reg;
  reg [63 : 0] u0_new;
  reg [63 : 0] u1_reg;
  reg [63 : 0] u1_new;
  reg [63 : 0] u2_reg;
  reg [63 : 0] u2_new;
  reg [63 : 0] u3_reg;
  reg [63 : 0] u3_new;
  reg [31 : 0] u4_reg;
  reg [31 : 0] u4_new;
  reg [63 : 0] u5_reg;
  reg [63 : 0] u5_new;

  reg [63 : 0] s0_reg;
  reg [63 : 0] s0_new;
  reg [63 : 0] s1_reg;
  reg [63 : 0] s1_new;
  reg [63 : 0] s2_reg;
  reg [63 : 0] s2_new;
  reg [63 : 0] s3_reg;
  reg [63 : 0] s3_new;
  reg [63 : 0] s4_reg;
  reg [63 : 0] s4_new;

  reg [31 : 0] rr0_reg;
  reg [31 : 0] rr0_new;
  reg [31 : 0] rr1_reg;
  reg [31 : 0] rr1_new;
  reg [31 : 0] rr2_reg;
  reg [31 : 0] rr2_new;
  reg [31 : 0] rr3_reg;
  reg [31 : 0] rr3_new;

  wire [63 : 0] x0_new;
  wire [63 : 0] x1_new;
  wire [63 : 0] x2_new;
  wire [63 : 0] x3_new;
  reg [63 : 0]  x4_reg;
  reg [63 : 0]  x4_new;

  reg [3 : 0]   cycle_ctr_reg;
  reg [3 : 0]   cycle_ctr_new;
  reg           cycle_ctr_we;
  reg           cycle_ctr_rst;
  reg           cycle_ctr_inc;

  reg           ready_reg;
  reg           ready_new;
  reg           ready_we;

  reg [3 : 0]   pblock_ctrl_reg;
  reg [3 : 0]   pblock_ctrl_new;
  reg           pblock_ctrl_we;


  //----------------------------------------------------------------
  // Wires.
  //----------------------------------------------------------------
  reg  mulacc_start;
  wire mulacc0_ready;
  wire mulacc1_ready;
  wire mulacc2_ready;
  wire mulacc3_ready;


  //----------------------------------------------------------------
  // Concurrent connectivity for ports etc.
  //----------------------------------------------------------------
  assign ready   = ready_reg;

  assign h0_new  = u0_reg[31 : 0];
  assign h1_new  = u1_reg[31 : 0];
  assign h2_new  = u2_reg[31 : 0];
  assign h3_new  = u3_reg[31 : 0];
  assign h4_new  = u4_reg;


  //----------------------------------------------------------------
  // mulacc instances.
  //----------------------------------------------------------------
  poly1305_mulacc mulacc0(
                          .clk(clk),
                          .reset_n(reset_n),
                          .start(mulacc_start),
                          .ready(mulacc0_ready),
                          .opa0(r0),
                          .opb0(s0_reg),
                          .opa1(rr3_reg),
                          .opb1(s1_reg),
                          .opa2(rr2_reg),
                          .opb2(s2_reg),
                          .opa3(rr1_reg),
                          .opb3(s3_reg),
                          .opa4(rr0_reg),
                          .opb4(s4_reg),
                          .sum(x0_new)
                          );

  poly1305_mulacc mulacc1(
                          .clk(clk),
                          .reset_n(reset_n),
                          .start(mulacc_start),
                          .ready(mulacc1_ready),
                          .opa0(r1),
                          .opb0(s0_reg),
                          .opa1(r0),
                          .opb1(s1_reg),
                          .opa2(rr3_reg),
                          .opb2(s2_reg),
                          .opa3(rr2_reg),
                          .opb3(s3_reg),
                          .opa4(rr1_reg),
                          .opb4(s4_reg),
                          .sum(x1_new)
                          );

  poly1305_mulacc mulacc2(
                          .clk(clk),
                          .reset_n(reset_n),
                          .start(mulacc_start),
                          .ready(mulacc2_ready),
                          .opa0(r2),
                          .opb0(s0_reg),
                          .opa1(r1),
                          .opb1(s1_reg),
                          .opa2(r0),
                          .opb2(s2_reg),
                          .opa3(rr3_reg),
                          .opb3(s3_reg),
                          .opa4(rr2_reg),
                          .opb4(s4_reg),
                          .sum(x2_new)
                          );

  poly1305_mulacc mulacc3(
                          .clk(clk),
                          .reset_n(reset_n),
                          .start(mulacc_start),
                          .ready(mulacc3_ready),
                          .opa0(r3),
                          .opb0(s0_reg),
                          .opa1(r2),
                          .opb1(s1_reg),
                          .opa2(r1),
                          .opb2(s2_reg),
                          .opa3(r0),
                          .opb3(s3_reg),
                          .opa4(rr3_reg),
                          .opb4(s4_reg),
                          .sum(x3_new)
                          );


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
          s0_reg          <= 64'h0;
          s1_reg          <= 64'h0;
          s2_reg          <= 64'h0;
          s3_reg          <= 64'h0;
          s4_reg          <= 64'h0;
          rr0_reg         <= 32'h0;
          rr1_reg         <= 32'h0;
          rr2_reg         <= 32'h0;
          rr3_reg         <= 32'h0;
          x4_reg          <= 64'h0;
          u0_reg          <= 64'h0;
          u1_reg          <= 64'h0;
          u2_reg          <= 64'h0;
          u3_reg          <= 64'h0;
          u4_reg          <= 32'h0;
          u5_reg          <= 64'h0;
          cycle_ctr_reg   <= 4'h0;
          ready_reg       <= 1'h1;
          pblock_ctrl_reg <= CTRL_IDLE;
        end
      else
        begin
          s0_reg  <= s0_new;
          s1_reg  <= s1_new;
          s2_reg  <= s2_new;
          s3_reg  <= s3_new;
          s4_reg  <= s4_new;

          rr0_reg <= rr0_new;
          rr1_reg <= rr1_new;
          rr2_reg <= rr2_new;
          rr3_reg <= rr3_new;

          x4_reg  <= x4_new;

          u0_reg  <= u0_new;
          u1_reg  <= u1_new;
          u2_reg  <= u2_new;
          u3_reg  <= u3_new;
          u4_reg  <= u4_new;
          u5_reg  <= u5_new;

          if (cycle_ctr_we)
            cycle_ctr_reg <= cycle_ctr_new;

          if (ready_we)
            ready_reg <= ready_new;

          if (pblock_ctrl_we)
            pblock_ctrl_reg <= pblock_ctrl_new;
        end
    end // reg_update


  //----------------------------------------------------------------
  // pblock_logic
  //----------------------------------------------------------------
  always @*
    begin : pblock_logic
      // s = h + c, no carry propagation.
      s0_new = {32'h0, h0} + {32'h0, c0};
      s1_new = {32'h0, h1} + {32'h0, c1};
      s2_new = {32'h0, h2} + {32'h0, c2};
      s3_new = {32'h0, h3} + {32'h0, c3};
      s4_new = {32'h0, h4} + {32'h0, c4};


      // Multiply r.
      rr0_new = {2'h0, r0[31 : 2]} * 32'h5;
      rr1_new = {2'h0, r1[31 : 2]} + r1;
      rr2_new = {2'h0, r2[31 : 2]} + r2;
      rr3_new = {2'h0, r3[31 : 2]} + r3;


      // x0..x3 are calculated by the mulacc modules.
      // We don't need registers for x0..x3.
      x4_new = s4_reg * {32'h0, (r0 & 32'h3)};


      // partial reduction modulo 2^130 - 5
      u5_new = x4_reg + {32'h0, x3_new[63 : 32]};
      u0_new = ({2'h0, u5_reg[31 : 2]} * 5) + {32'h0, x0_new[31 : 0]};
      u1_new = {32'h0, u0_reg[63 : 32]} + {32'h0, x1_new[31 : 0]} + {32'h0, x0_new[63 : 32]};
      u2_new = {32'h0, u1_reg[63 : 32]} + {32'h0, x2_new[31 : 0]} + {32'h0, x1_new[63 : 32]};
      u3_new = {32'h0, u2_reg[63 : 32]} + {32'h0, x3_new[31 : 0]} + {32'h0, x2_new[63 : 32]};
      u4_new = u3_reg[63 : 32] + {30'h0, (u5_reg[1 : 0] & 2'h3)};
    end // pblock_logic


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
  // pblock_ctrl
  //----------------------------------------------------------------
  always @*
    begin : pblock_ctrl
      ready_new       = 1'h1;
      ready_we        = 1'h0;
      mulacc_start    = 1'h0;
      cycle_ctr_rst   = 1'h0;
      cycle_ctr_inc   = 1'h0;
      pblock_ctrl_new = CTRL_IDLE;
      pblock_ctrl_we  = 1'h0;

      case (pblock_ctrl_reg)
        CTRL_IDLE:
          begin
            if (start)
              begin
                ready_new       = 1'h0;
                ready_we        = 1'h1;
                cycle_ctr_rst   = 1'h1;
                pblock_ctrl_new = CTRL_PRE_WAIT;
                pblock_ctrl_we  = 1'h1;
              end
          end

        CTRL_PRE_WAIT:
          begin
            cycle_ctr_inc = 1'h1;
            if (cycle_ctr_reg == PRE_CYCLES)
              begin
                mulacc_start    = 1'h1;
                pblock_ctrl_new = CTRL_MULACC;
                pblock_ctrl_we  = 1'h1;
              end
          end

        CTRL_MULACC:
          begin
            if ((mulacc0_ready) || (mulacc1_ready) ||
                (mulacc2_ready) || (mulacc3_ready))
              begin
                cycle_ctr_rst   = 1'h1;
                pblock_ctrl_new = CTRL_POST_WAIT;
                pblock_ctrl_we  = 1'h1;
              end
          end

        CTRL_POST_WAIT:
          begin
            cycle_ctr_inc = 1'h1;
            if (cycle_ctr_reg == POST_CYCLES)
              begin
                ready_new       = 1'h1;
                ready_we        = 1'h1;
                pblock_ctrl_new = CTRL_IDLE;
                pblock_ctrl_we  = 1'h1;
              end
          end

        default:
          begin
          end
      endcase // case (pblock_ctrl_reg)
    end

endmodule // poly1305_pblock

//======================================================================
// EOF poly1305_pblock.v
//======================================================================
