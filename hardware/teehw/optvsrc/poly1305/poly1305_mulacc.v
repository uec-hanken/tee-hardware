//======================================================================
//
// poly1305_mulacc.v
// -----------------
// Multiply-accumulate with five sets of operands.
//
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

module poly1305_mulacc(
                       input wire           clk,
                       input wire           reset_n,

                       input wire           start,
                       output wire          ready,

                       input wire [31 : 0]  opa0,
                       input wire [63 : 0]  opb0,

                       input wire [31 : 0]  opa1,
                       input wire [63 : 0]  opb1,

                       input wire [31 : 0]  opa2,
                       input wire [63 : 0]  opb2,

                       input wire [31 : 0]  opa3,
                       input wire [63 : 0]  opb3,

                       input wire [31 : 0]  opa4,
                       input wire [63 : 0]  opb4,

                       output wire [63 : 0] sum
                      );


  //----------------------------------------------------------------
  // Parameters and symbolic values.
  //----------------------------------------------------------------
  localparam CTRL_IDLE = 3'h0;
  localparam CTRL_OP1  = 3'h1;
  localparam CTRL_OP2  = 3'h2;
  localparam CTRL_OP3  = 3'h3;
  localparam CTRL_OP4  = 3'h4;
  localparam CTRL_SUM  = 3'h5;


  //----------------------------------------------------------------
  // Registers including update variables and write enable.
  //----------------------------------------------------------------
  reg [63 : 0] mul_reg;
  reg [63 : 0] mul_new;
  reg          mul_we;

  reg [63 : 0] sum_reg;
  reg [63 : 0] sum_new;
  reg          sum_we;

  reg          ready_reg;
  reg          ready_new;
  reg          ready_we;

  reg [2 : 0]  mulacc_ctrl_reg;
  reg [2 : 0]  mulacc_ctrl_new;
  reg          mulacc_ctrl_we;


  //----------------------------------------------------------------
  // wires
  //----------------------------------------------------------------
  reg [2 : 0]  mulop_select;
  reg          update_mul;
  reg          clear_sum;
  reg          update_sum;


  //----------------------------------------------------------------
  // Concurrent connectivity for ports etc.
  //----------------------------------------------------------------
  assign sum   = sum_reg;
  assign ready = ready_reg;


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
          mul_reg         <= 64'h0;
          sum_reg         <= 64'h0;
          ready_reg       <= 1'h0;
          mulacc_ctrl_reg <= CTRL_IDLE;
        end
      else
        begin
          if (mul_we)
            mul_reg <= mul_new;

          if (sum_we)
            sum_reg <= sum_new;

          if (ready_we)
            ready_reg <= ready_new;

          if (mulacc_ctrl_we)
            mulacc_ctrl_reg <= mulacc_ctrl_new;
        end
    end // reg_update


  //----------------------------------------------------------------
  // mulacc_logic
  //----------------------------------------------------------------
  always @*
    begin : mulacc_logic
      reg [31 : 0] mul_opa;
      reg [63 : 0] mul_opb;

      mul_opa = 32'h0;
      mul_opb = 64'h0;
      mul_new = 64'h0;
      mul_we  = 1'h0;
      sum_new = 64'h0;
      sum_we  = 1'h0;


      case (mulop_select)
        0:
          begin
            mul_opa = opa0;
            mul_opb = opb0;
          end

        1:
          begin
            mul_opa = opa1;
            mul_opb = opb1;
          end

        2:
          begin
            mul_opa = opa2;
            mul_opb = opb2;
          end

        3:
          begin
            mul_opa = opa3;
            mul_opb = opb3;
          end

        4:
          begin
            mul_opa = opa4;
            mul_opb = opb4;
          end

        default:
          begin
          end
      endcase // case (mulop_select)


      if (update_mul)
        begin
          mul_new = mul_opa * mul_opb;
          mul_we  = 1'h1;
        end

      if (clear_sum)
        begin
          sum_new = 64'h0;
          sum_we  = 1;
        end

      if (update_sum)
        begin
          sum_new = sum_reg + mul_reg;
          sum_we  = 1;
        end
    end


  //----------------------------------------------------------------
  // mulacc_ctrl
  //----------------------------------------------------------------
  always @*
    begin : mulacc_ctrl
      mulop_select    = 3'h0;
      update_mul      = 1'h0;
      clear_sum       = 1'h0;
      update_sum      = 1'h0;
      ready_new       = 1'h0;
      ready_we        = 1'h0;
      mulacc_ctrl_new = CTRL_IDLE;
      mulacc_ctrl_we  = 1'h0;

      case (mulacc_ctrl_reg)
        CTRL_IDLE:
          begin
            if (start)
              begin
                ready_new       = 1'h0;
                ready_we        = 1'h1;
                mulop_select    = 3'h0;
                update_mul      = 1'h1;
                clear_sum       = 1'h1;
                mulacc_ctrl_new = CTRL_OP1;
                mulacc_ctrl_we  = 1'h1;
              end
          end

        CTRL_OP1:
          begin
            mulop_select    = 3'h1;
            update_mul      = 1'h1;
            update_sum      = 1'h1;
            mulacc_ctrl_new = CTRL_OP2;
            mulacc_ctrl_we  = 1'h1;
          end

        CTRL_OP2:
          begin
            mulop_select    = 3'h2;
            update_mul      = 1'h1;
            update_sum      = 1'h1;
            mulacc_ctrl_new = CTRL_OP3;
            mulacc_ctrl_we  = 1'h1;
          end

        CTRL_OP3:
          begin
            mulop_select    = 3'h3;
            update_mul      = 1'h1;
            update_sum      = 1'h1;
            mulacc_ctrl_new = CTRL_OP4;
            mulacc_ctrl_we  = 1'h1;
          end

        CTRL_OP4:
          begin
            mulop_select    = 3'h4;
            update_mul      = 1'h1;
            update_sum      = 1'h1;
            mulacc_ctrl_new = CTRL_SUM;
            mulacc_ctrl_we  = 1'h1;
          end

        CTRL_SUM:
          begin
            update_sum      = 1'h1;
            ready_new       = 1'h1;
            ready_we        = 1'h1;
            mulacc_ctrl_new = CTRL_IDLE;
            mulacc_ctrl_we  = 1'h1;
          end

        default:
          begin
          end
      endcase // case (mulacc_ctrl_reg)
    end

endmodule // poly1305_mulacc

//======================================================================
// EOF poly1305_mulacc.v
//======================================================================
