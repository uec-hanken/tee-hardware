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

module ed25519_wrapper
(
    input           clk,
    input           rst_n,

    input           cs,
    input           we,

    input   [ 4:0]  address,
    input   [31:0]  write_data,
    output  [31:0]  read_data
);


    //
    // Address Decoder
    //
    localparam ADDR_MSB_REGS = 1'b0;
    localparam ADDR_MSB_CORE = 1'b1;

    wire [0:0] addr_msb = address[4];
    wire [3:0] addr_lsb = address[3:0];


    //
    // Output Mux
    //
    wire [31: 0] read_data_regs;
    wire [31: 0] read_data_core;


    //
    // Registers
    //
    localparam ADDR_NAME0        = 4'h0;
    localparam ADDR_NAME1        = 4'h1;
    localparam ADDR_VERSION      = 4'h2;

    localparam ADDR_CONTROL      = 4'h8;               // {next, init}
    localparam ADDR_STATUS       = 4'h9;               // {valid, ready}
    localparam ADDR_DUMMY        = 4'hF;               // don't care

    // localparam CONTROL_INIT_BIT  = 0; -- not used
    localparam CONTROL_NEXT_BIT  = 1;

    // localparam STATUS_READY_BIT  = 0; -- hardcoded to always read 1
    localparam STATUS_VALID_BIT  = 1;

    localparam CORE_NAME0        = 32'h65643235; // "ed25"
    localparam CORE_NAME1        = 32'h35313920; // "519 "
    localparam CORE_VERSION      = 32'h302E3130; // "0.10"


    //
    // Registers
    //
    reg        reg_control = 1'b0;
    reg [31:0] reg_dummy;


    //
    // Wires
    //
    wire    reg_status;


    //
    // Ed25519
    //
    ed25519_core_top ed25519_inst
    (
        .clk            (clk),
        .rst_n          (rst_n),

        .next           (reg_control),
        .valid          (reg_status),

        .bus_cs         (cs && (addr_msb == ADDR_MSB_CORE)),
        .bus_we         (we),
        .bus_addr       (addr_lsb),
        .bus_data_wr    (write_data),
        .bus_data_rd    (read_data_core)
    );


    //
    // Read Latch
    //
    reg [31: 0] tmp_read_data;


    //
    // Read/Write Interface
    //
    always @(posedge clk)
        //
        if (!rst_n) begin
            //
            reg_control <= 1'b0;
            //
        end else if (cs && (addr_msb == ADDR_MSB_REGS)) begin
            //
            if (we) begin
                //
                // Write Handler
                //
                case (addr_lsb)
                    //
                    ADDR_CONTROL: reg_control <= write_data[CONTROL_NEXT_BIT];
                    ADDR_DUMMY:   reg_dummy   <= write_data;
                    //
                endcase
                //
            end else begin
                //
                // Read Handler
                //
                case (address)
                    //
                    ADDR_NAME0:        tmp_read_data <= CORE_NAME0;
                    ADDR_NAME1:        tmp_read_data <= CORE_NAME1;
                    ADDR_VERSION:      tmp_read_data <= CORE_VERSION;
                    ADDR_CONTROL:      tmp_read_data <= {{30{1'b0}}, reg_control, 1'b0};
                    ADDR_STATUS:       tmp_read_data <= {{30{1'b0}}, reg_status,  1'b1};
                    ADDR_DUMMY:        tmp_read_data <= reg_dummy;
                    //
                    default:           tmp_read_data <= 32'h00000000;
                    //
                endcase
                //
            end
            //
        end


    //
    // Register / Core Memory Selector
    //
    reg addr_msb_last;
    
    always @(posedge clk)   
        addr_msb_last <= addr_msb;

    assign read_data = (addr_msb_last == ADDR_MSB_REGS) ? tmp_read_data : read_data_core;


endmodule
