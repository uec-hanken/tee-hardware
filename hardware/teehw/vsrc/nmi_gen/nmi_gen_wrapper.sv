// Copyright lowRISC contributors.
// Licensed under the Apache License, Version 2.0, see LICENSE for details.
// SPDX-License-Identifier: Apache-2.0

/**
 */
module nmi_gen_wrapper #(
  localparam int unsigned N_ESC_SEV = 3
) (
  // Clock and Reset
  input  logic        clk_i,
  input  logic        rst_ni,
  
  // Instruction memory interface
  input  logic                         tl_a_valid,
  output logic                         tl_a_ready,
  input  logic                  [2:0]  tl_a_bits_opcode,
  input  logic                  [2:0]  tl_a_bits_param,
  input  logic  [top_pkg::TL_SZW-1:0]  tl_a_bits_size,
  input  logic  [top_pkg::TL_AIW-1:0]  tl_a_bits_source,
  input  logic   [top_pkg::TL_AW-1:0]  tl_a_bits_address,
  input  logic  [top_pkg::TL_DBW-1:0]  tl_a_bits_mask,
  input  logic   [top_pkg::TL_DW-1:0]  tl_a_bits_data,
  input  logic                  [6:0]  tl_a_bits_user_tl_a_user_t_rsvd1,
  input  logic                         tl_a_bits_user_tl_a_user_t_parity_en,
  input  logic                  [7:0]  tl_a_bits_user_tl_a_user_t_parity,
  input  logic                         tl_a_bits_corrupt,

  output logic                         tl_d_valid,
  input  logic                         tl_d_ready,
  output logic                  [2:0]  tl_d_bits_opcode,
  output logic                  [2:0]  tl_d_bits_param,
  output logic  [top_pkg::TL_SZW-1:0]  tl_d_bits_size,   // Bouncing back a_size
  output logic  [top_pkg::TL_AIW-1:0]  tl_d_bits_source,
  output logic  [top_pkg::TL_DIW-1:0]  tl_d_bits_sink,
  output logic   [top_pkg::TL_DW-1:0]  tl_d_bits_data,
  output logic  [top_pkg::TL_DUW-1:0]  tl_d_bits_user_uint,
  output logic                         tl_d_bits_corrupt,
  output logic                         tl_d_bits_denied,
  
  // Interrupt Requests
  output logic                    intr_esc0_o,
  output logic                    intr_esc1_o,
  output logic                    intr_esc2_o,
  
  // Reset Requests
  output logic                    nmi_rst_req_o,
  
  // Escalation outputs

  input  logic                         esc_tx_i_0_esc_p,
  input  logic                         esc_tx_i_0_esc_n,

  output logic                         esc_rx_o_0_resp_p,
  output logic                         esc_rx_o_0_resp_n,

  input  logic                         esc_tx_i_1_esc_p,
  input  logic                         esc_tx_i_1_esc_n,

  output logic                         esc_rx_o_1_resp_p,
  output logic                         esc_rx_o_1_resp_n,

  input  logic                         esc_tx_i_2_esc_p,
  input  logic                         esc_tx_i_2_esc_n,

  output logic                         esc_rx_o_2_resp_p,
  output logic                         esc_rx_o_2_resp_n

);

  // TL connections

  wire tlul_pkg::tl_h2d_t tl_i;
  wire tlul_pkg::tl_d2h_t tl_o;
  
  assign tl_i.a_valid = tl_a_valid; 
  assign tl_i.a_opcode = tl_a_bits_opcode;
  assign tl_i.a_param = tl_a_bits_param;
  assign tl_i.a_size = tl_a_bits_size;
  assign tl_i.a_source = tl_a_bits_source;
  assign tl_i.a_address = tl_a_bits_address;
  assign tl_i.a_mask = tl_a_bits_mask;
  assign tl_i.a_data = tl_a_bits_data;
  assign tl_i.a_user.rsvd1 = tl_a_bits_user_tl_a_user_t_rsvd1;
  assign tl_i.a_user.parity_en = tl_a_bits_user_tl_a_user_t_parity_en;
  assign tl_i.a_user.parity = tl_a_bits_user_tl_a_user_t_parity;

  assign tl_i.d_ready = tl_d_ready;
  
  // tl_a_bits_corrupt; // ignored
  
  assign tl_d_valid = tl_o.d_valid;
  assign tl_d_bits_opcode = tl_o.d_opcode;
  assign tl_d_bits_param = tl_o.d_param;
  assign tl_d_bits_size = tl_o.d_size;
  assign tl_d_bits_source = tl_o.d_source;
  assign tl_d_bits_sink = tl_o.d_sink;
  assign tl_d_bits_data = tl_o.d_data;
  assign tl_d_bits_user_uint = tl_o.d_user;
  assign tl_d_bits_corrupt = tl_o.d_error; // Seems legit
  assign tl_d_bits_denied = 1'b0; // Seems also legit

  assign tl_a_ready = tl_o.a_ready;
  
  // That "weird" esc port connections

  wire prim_esc_pkg::esc_tx_t[N_ESC_SEV:0] esc_tx_i;
  wire prim_esc_pkg::esc_rx_t[N_ESC_SEV:0] esc_rx_o;
  
  assign esc_tx_i[0].esc_p = esc_tx_i_0_esc_p;
  assign esc_tx_i[0].esc_n = esc_tx_i_0_esc_n;
  
  assign esc_rx_o_0_resp_p = esc_rx_o[0].resp_p;
  assign esc_rx_o_0_resp_n = esc_rx_o[0].resp_n;
  
  assign esc_tx_i[1].esc_p = esc_tx_i_1_esc_p;
  assign esc_tx_i[1].esc_n = esc_tx_i_1_esc_n;
  
  assign esc_rx_o_1_resp_p = esc_rx_o[1].resp_p;
  assign esc_rx_o_1_resp_n = esc_rx_o[1].resp_n;
  
  assign esc_tx_i[2].esc_p = esc_tx_i_2_esc_p;
  assign esc_tx_i[2].esc_n = esc_tx_i_2_esc_n;
  
  assign esc_rx_o_2_resp_p = esc_rx_o[2].resp_p;
  assign esc_rx_o_2_resp_n = esc_rx_o[2].resp_n;

  nmi_gen u_nmi_gen (
    // clock and reset
    .clk_i                (clk_i),
    .rst_ni               (rst_ni),
    // TL-UL buses
    .tl_o                 (tl_o),
    .tl_i                 (tl_i),
    // Interrupt Requests
    .intr_esc0_o          (intr_esc0_o),
    .intr_esc1_o          (intr_esc1_o),
    .intr_esc2_o          (intr_esc2_o),
    // Reset Requests
    .nmi_rst_req_o        (nmi_rst_req_o),
    // Alert
    .esc_rx_o             (esc_rx_o),
    .esc_tx_i             (esc_tx_i)
  );

endmodule
