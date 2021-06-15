//======================================================================
//
// poly1305.v
// ----------
// Top level wrapper for the Poly1305 MAC.
//
//
// Author: Joachim Strombergson
// Copyright (c) 2018 Assured AB
// All rights reserved.
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

module poly1305(
                input wire           clk,
                input wire           reset_n,

                input wire           cs,
                input wire           we,

                input wire  [7 : 0]  address,
                input wire  [31 : 0] write_data,
                output wire [31 : 0] read_data
               );

  //----------------------------------------------------------------
  // Internal constant and parameter definitions.
  //----------------------------------------------------------------
  localparam ADDR_NAME0       = 8'h00;
  localparam ADDR_NAME1       = 8'h01;
  localparam ADDR_VERSION     = 8'h02;

  localparam ADDR_CTRL        = 8'h08;
  localparam CTRL_INIT_BIT    = 0;
  localparam CTRL_NEXT_BIT    = 1;
  localparam CTRL_FINISH_BIT  = 2;

  localparam ADDR_STATUS      = 8'h09;
  localparam STATUS_READY_BIT = 0;

  localparam ADDR_BLOCKLEN    = 8'h0a;

  localparam ADDR_KEY0        = 8'h10;
  localparam ADDR_KEY7        = 8'h17;

  localparam ADDR_BLOCK0      = 8'h20;
  localparam ADDR_BLOCK1      = 8'h21;
  localparam ADDR_BLOCK2      = 8'h22;
  localparam ADDR_BLOCK3      = 8'h23;

  localparam ADDR_MAC0        = 8'h30;
  localparam ADDR_MAC1        = 8'h31;
  localparam ADDR_MAC2        = 8'h32;
  localparam ADDR_MAC3        = 8'h33;

  localparam CORE_NAME0       = 32'h706f6c79; // "poly"
  localparam CORE_NAME1       = 32'h31333035; // "1305"
  localparam CORE_VERSION     = 32'h312e3030; // "1.00"


  //----------------------------------------------------------------
  // Registers including update variables and write enable.
  //----------------------------------------------------------------
  reg init_reg;
  reg init_new;

  reg next_reg;
  reg next_new;


  reg finish_reg;
  reg finish_new;

  reg [4 : 0]   blocklen_reg;
  reg           blocklen_we;

  reg [31 : 0]  block_reg [0 : 3];
  reg           block_we;

  reg [31 : 0]  key_reg [0 : 7];
  reg           key_we;

  reg           ready_reg;


  //----------------------------------------------------------------
  // Wires.
  //----------------------------------------------------------------
  reg [31 : 0]   tmp_read_data;

  wire           core_ready;
  wire [255 : 0] core_key;
  wire [127 : 0] core_block;
  wire [127 : 0] core_mac;


  //----------------------------------------------------------------
  // Concurrent connectivity for ports etc.
  //----------------------------------------------------------------
  assign read_data = tmp_read_data;

  assign core_key = {key_reg[0], key_reg[1], key_reg[2], key_reg[3],
                     key_reg[4], key_reg[5], key_reg[6], key_reg[7]};

  assign core_block = {block_reg[0], block_reg[1],
                       block_reg[2], block_reg[3]};


  //----------------------------------------------------------------
  // core instantiation.
  //----------------------------------------------------------------
  poly1305_core core(
                     .clk(clk),
                     .reset_n(reset_n),
                     .init(init_reg),
                     .next(next_reg),
                     .finish(finish_reg),
                     .ready(core_ready),
                     .key(core_key),
                     .block(core_block),
                     .blocklen(blocklen_reg),
                     .mac(core_mac)
                    );


  //----------------------------------------------------------------
  // reg_update
  // Update functionality for all registers in the core.
  // All registers are positive edge triggered with asynchronous
  // active low reset.
  //----------------------------------------------------------------
  always @ (posedge clk)
    begin : reg_update
      integer i;

      if (!reset_n)
        begin
          for (i = 0 ; i < 4 ; i = i + 1)
            block_reg[i] <= 32'h0;

          for (i = 0 ; i < 8 ; i = i + 1)
            key_reg[i] <= 32'h0;

          blocklen_reg <= 5'h0;
          init_reg     <= 1'b0;
          next_reg     <= 1'b0;
          ready_reg    <= 1'b0;
        end
      else
        begin
          ready_reg  <= core_ready;
          init_reg   <= init_new;
          next_reg   <= next_new;
          finish_reg <= finish_new;

          if (blocklen_we)
            blocklen_reg <= write_data[4 : 0];

          if (key_we)
            key_reg[address[2 : 0]] <= write_data;

          if (block_we)
            block_reg[address[1 : 0]] <= write_data;
        end
    end // reg_update


  //----------------------------------------------------------------
  // api
  //
  // The interface command decoding logic.
  //----------------------------------------------------------------
  always @*
    begin : api
      init_new      = 1'b0;
      next_new      = 1'b0;
      finish_new    = 1'b0;
      blocklen_we   = 1'b0;
      key_we        = 1'b0;
      block_we      = 1'b0;
      tmp_read_data = 32'h0;

      if (cs)
        begin
          if (we)
            begin
              if (address == ADDR_CTRL)
                begin
                  init_new   = write_data[CTRL_INIT_BIT];
                  next_new   = write_data[CTRL_NEXT_BIT];
                  finish_new = write_data[CTRL_FINISH_BIT];
                end

              if (address == ADDR_BLOCKLEN)
                blocklen_we = 1'h1;

              if ((address >= ADDR_KEY0) && (address <= ADDR_KEY7))
                key_we = 1'b1;

              if ((address >= ADDR_BLOCK0) && (address <= ADDR_BLOCK3))
                block_we = 1'b1;
            end // if (we)

          else
            begin
              if (address == ADDR_NAME0)
                tmp_read_data = CORE_NAME0;

              if (address == ADDR_NAME1)
                tmp_read_data = CORE_NAME1;

              if (address == ADDR_VERSION)
                tmp_read_data = CORE_VERSION;

              if (address == ADDR_STATUS)
                tmp_read_data = {31'h0, ready_reg};

              if ((address >= ADDR_MAC0) && (address <= ADDR_MAC3))
                tmp_read_data = core_mac[(3 - (address - ADDR_MAC0)) * 32 +: 32];
            end
        end
    end // addr_decoder
endmodule // poly1305

//======================================================================
// EOF poly1305.v
//======================================================================
