// Copyright lowRISC contributors.
// Licensed under the Apache License, Version 2.0, see LICENSE for details.
// SPDX-License-Identifier: Apache-2.0

/**
 * Ibex RISC-V core
 *
 * 32 bit RISC-V core supporting the RV32I + optionally EMC instruction sets.
 * Instruction and data bus are 32 bit wide TileLink-UL (TL-UL).
 */
module IbexBlackbox #(
  parameter bit                 PMPEnable         = 1'b0,
  parameter int unsigned        PMPGranularity    = 0,
  parameter int unsigned        PMPNumRegions     = 4,
  parameter int unsigned        MHPMCounterNum    = 10,
  parameter int unsigned        MHPMCounterWidth  = 32,
  parameter bit                 RV32E             = 0,
  parameter integer             RV32M             = 3,
  parameter integer             RV32B             = 0,
  parameter integer             RegFile           = 0,
  parameter bit                 BranchTargetALU   = 1'b1,
  parameter bit                 WritebackStage    = 1'b1,
  parameter bit                 ICache            = 1'b0,
  parameter bit                 ICacheECC         = 1'b0,
  parameter bit                 BranchPredictor   = 1'b0,
  parameter bit                 DbgTriggerEn      = 1'b1,
  parameter bit                 SecureIbex        = 1'b0,
  parameter int unsigned        DmHaltAddr        = 32'h1A110800,
  parameter int unsigned        DmExceptionAddr   = 32'h1A110808,
  parameter bit                 PipeLine          = 1'b0
) (
  // Clock and Reset
  input  logic        clk_i,
  input  logic        rst_ni,

  input  logic        test_en_i,     // enable all clock gates for testing

  input  logic [31:0] hart_id_i,
  input  logic [31:0] boot_addr_i,

  // Instruction memory interface
  output logic                         tl_i_a_valid,
  input  logic                         tl_i_a_ready,
  output logic                  [2:0]  tl_i_a_bits_opcode,
  output logic                  [2:0]  tl_i_a_bits_param,
  output logic  [top_pkg::TL_SZW-1:0]  tl_i_a_bits_size,
  output logic  [top_pkg::TL_AIW-1:0]  tl_i_a_bits_source,
  output logic   [top_pkg::TL_AW-1:0]  tl_i_a_bits_address,
  output logic  [top_pkg::TL_DBW-1:0]  tl_i_a_bits_mask,
  output logic   [top_pkg::TL_DW-1:0]  tl_i_a_bits_data,
  output logic                  [6:0]  tl_i_a_bits_user_tl_a_user_t_rsvd1,
  output logic                         tl_i_a_bits_user_tl_a_user_t_parity_en,
  output logic                  [7:0]  tl_i_a_bits_user_tl_a_user_t_parity,
  output logic                         tl_i_a_bits_corrupt,

  input  logic                         tl_i_d_valid,
  output logic                         tl_i_d_ready,
  input  logic                  [2:0]  tl_i_d_bits_opcode,
  input  logic                  [2:0]  tl_i_d_bits_param,
  input  logic  [top_pkg::TL_SZW-1:0]  tl_i_d_bits_size,   // Bouncing back a_size
  input  logic  [top_pkg::TL_AIW-1:0]  tl_i_d_bits_source,
  input  logic  [top_pkg::TL_DIW-1:0]  tl_i_d_bits_sink,
  input  logic   [top_pkg::TL_DW-1:0]  tl_i_d_bits_data,
  input  logic  [top_pkg::TL_DUW-1:0]  tl_i_d_bits_user_uint,
  input  logic                         tl_i_d_bits_corrupt,
  input  logic                         tl_i_d_bits_denied,

  // Data memory interface
  output logic                         tl_d_a_valid,
  input  logic                         tl_d_a_ready,
  output logic                  [2:0]  tl_d_a_bits_opcode,
  output logic                  [2:0]  tl_d_a_bits_param,
  output logic  [top_pkg::TL_SZW-1:0]  tl_d_a_bits_size,
  output logic  [top_pkg::TL_AIW-1:0]  tl_d_a_bits_source,
  output logic   [top_pkg::TL_AW-1:0]  tl_d_a_bits_address,
  output logic  [top_pkg::TL_DBW-1:0]  tl_d_a_bits_mask,
  output logic   [top_pkg::TL_DW-1:0]  tl_d_a_bits_data,
  output logic                  [6:0]  tl_d_a_bits_user_tl_a_user_t_rsvd1,
  output logic                         tl_d_a_bits_user_tl_a_user_t_parity_en,
  output logic                  [7:0]  tl_d_a_bits_user_tl_a_user_t_parity,
  output logic                         tl_d_a_bits_corrupt,

  input  logic                         tl_d_d_valid,
  output logic                         tl_d_d_ready,
  input  logic                  [2:0]  tl_d_d_bits_opcode,
  input  logic                  [2:0]  tl_d_d_bits_param,
  input  logic  [top_pkg::TL_SZW-1:0]  tl_d_d_bits_size,   // Bouncing back a_size
  input  logic  [top_pkg::TL_AIW-1:0]  tl_d_d_bits_source,
  input  logic  [top_pkg::TL_DIW-1:0]  tl_d_d_bits_sink,
  input  logic   [top_pkg::TL_DW-1:0]  tl_d_d_bits_data,
  input  logic  [top_pkg::TL_DUW-1:0]  tl_d_d_bits_user_uint,
  input  logic                         tl_d_d_bits_corrupt,
  input  logic                         tl_d_d_bits_denied,

  // Interrupt inputs
  input  logic        irq_software_i,
  input  logic        irq_timer_i,
  input  logic        irq_external_i,

  // Escalation input for NMI
  input  logic        esc_tx_i_esc_p,
  input  logic        esc_tx_i_esc_n,
  output logic        esc_rx_o_resp_p,
  output logic        esc_rx_o_resp_n,

  // Debug Interface
  input  logic        debug_req_i,

  // CPU Control Signals
  input  logic        fetch_enable_i,
  output logic        core_sleep_o
);

  // imem connections

  wire tlul_pkg::tl_h2d_t tl_i_o;
  wire tlul_pkg::tl_d2h_t tl_i_i;
  
  assign tl_i_a_valid = tl_i_o.a_valid;
  assign tl_i_a_bits_opcode = tl_i_o.a_opcode;
  assign tl_i_a_bits_param = tl_i_o.a_param;
  assign tl_i_a_bits_size = tl_i_o.a_size;
  assign tl_i_a_bits_source = tl_i_o.a_source;
  assign tl_i_a_bits_address = tl_i_o.a_address;
  assign tl_i_a_bits_mask = tl_i_o.a_mask;
  assign tl_i_a_bits_data = tl_i_o.a_data;
  assign tl_i_a_bits_user_tl_a_user_t_rsvd1 = tl_i_o.a_user.rsvd1;
  assign tl_i_a_bits_user_tl_a_user_t_parity_en = tl_i_o.a_user.parity_en;
  assign tl_i_a_bits_user_tl_a_user_t_parity = tl_i_o.a_user.parity;

  assign tl_i_d_ready = tl_i_o.d_ready;
  
  assign tl_i_a_bits_corrupt = 1'b0; // I mean, never corrupted from here, right?
  
  assign tl_i_i.d_valid = tl_i_d_valid;
  assign tl_i_i.d_opcode = tlul_pkg::tl_d_op_e'(tl_i_d_bits_opcode);
  assign tl_i_i.d_param = tl_i_d_bits_param;
  assign tl_i_i.d_size = tl_i_d_bits_size;
  assign tl_i_i.d_source = tl_i_d_bits_source;
  assign tl_i_i.d_sink = tl_i_d_bits_sink;
  assign tl_i_i.d_data = tl_i_d_bits_data;
  assign tl_i_i.d_user = tl_i_d_bits_user_uint;
  assign tl_i_i.d_error = tl_i_d_bits_corrupt || tl_i_d_bits_denied; // Seems legit

  assign tl_i_i.a_ready = tl_i_a_ready;
  
  // dmem connections

  wire tlul_pkg::tl_h2d_t tl_d_o;
  wire tlul_pkg::tl_d2h_t tl_d_i;
  
  assign tl_d_a_valid = tl_d_o.a_valid;
  assign tl_d_a_bits_opcode = tl_d_o.a_opcode;
  assign tl_d_a_bits_param = tl_d_o.a_param;
  assign tl_d_a_bits_size = tl_d_o.a_size;
  assign tl_d_a_bits_source = tl_d_o.a_source;
  assign tl_d_a_bits_address = tl_d_o.a_address;
  assign tl_d_a_bits_mask = tl_d_o.a_mask;
  assign tl_d_a_bits_data = tl_d_o.a_data;
  assign tl_d_a_bits_user_tl_a_user_t_rsvd1 = tl_d_o.a_user.rsvd1;
  assign tl_d_a_bits_user_tl_a_user_t_parity_en = tl_d_o.a_user.parity_en;
  assign tl_d_a_bits_user_tl_a_user_t_parity = tl_d_o.a_user.parity;

  assign tl_d_d_ready = tl_d_o.d_ready;
  
  assign tl_d_a_bits_corrupt = 1'b0; // I mean, never corrupted from here, right?
  
  assign tl_d_i.d_valid = tl_d_d_valid;
  assign tl_d_i.d_opcode = tlul_pkg::tl_d_op_e'(tl_d_d_bits_opcode);
  assign tl_d_i.d_param = tl_d_d_bits_param;
  assign tl_d_i.d_size = tl_d_d_bits_size;
  assign tl_d_i.d_source = tl_d_d_bits_source;
  assign tl_d_i.d_sink = tl_d_d_bits_sink;
  assign tl_d_i.d_data = tl_d_d_bits_data;
  assign tl_d_i.d_user = tl_d_d_bits_user_uint;
  assign tl_d_i.d_error = tl_d_d_bits_corrupt || tl_d_d_bits_denied; // Seems legit

  assign tl_d_i.a_ready = tl_d_a_ready;
  
  // That "weird" esc port connections

  wire prim_esc_pkg::esc_tx_t esc_tx_i;
  wire prim_esc_pkg::esc_rx_t esc_rx_o;
  
  assign esc_tx_i.esc_p = esc_tx_i_esc_p;
  assign esc_tx_i.esc_n = esc_tx_i_esc_n;
  
  assign esc_rx_o_resp_p = esc_rx_o.resp_p;
  assign esc_rx_o_resp_n = esc_rx_o.resp_n;

  rv_core_ibex #(
    .PMPEnable                ( PMPEnable                ),
    .PMPGranularity           ( PMPGranularity           ),
    .PMPNumRegions            ( PMPNumRegions            ),
    .MHPMCounterNum           ( MHPMCounterNum           ),
    .MHPMCounterWidth         ( MHPMCounterWidth         ),
    .RV32E                    ( RV32E                    ),
    .RV32M                    ( ibex_pkg::rv32m_e'(RV32M)),
    .RV32B                    ( ibex_pkg::rv32b_e'(RV32B)),
    .RegFile                  ( ibex_pkg::regfile_e'(RegFile)),
    .BranchTargetALU          ( BranchTargetALU          ),
    .WritebackStage           ( WritebackStage           ),
    .ICache                   ( ICache                   ),
    .ICacheECC                ( ICacheECC                ),
    .BranchPredictor          ( BranchPredictor          ),
    .DbgTriggerEn             ( DbgTriggerEn             ),
    .SecureIbex               ( SecureIbex               ),
    .DmHaltAddr               ( DmHaltAddr               ),
    .DmExceptionAddr          ( DmExceptionAddr          ),
    .PipeLine                 ( PipeLine                 )
  ) u_rv_core_ibex (
    // clock and reset
    .clk_i                (clk_i),
    .rst_ni               (rst_ni),
    .test_en_i            (test_en_i),
    // static pinning
    .hart_id_i            (hart_id_i),
    .boot_addr_i          (boot_addr_i),
    // TL-UL buses
    .tl_i_o               (tl_i_o),
    .tl_i_i               (tl_i_i),
    .tl_d_o               (tl_d_o),
    .tl_d_i               (tl_d_i),
    // interrupts
    .irq_software_i       (irq_software_i),
    .irq_timer_i          (irq_timer_i),
    .irq_external_i       (irq_external_i),
    // escalation input from alert handler (NMI)
    .esc_tx_i             (esc_tx_i),
    .esc_rx_o             (esc_rx_o),
    // debug interface
    .debug_req_i          (debug_req_i),
    // CPU control signals
    .fetch_enable_i       (fetch_enable_i),
    .core_sleep_o         (core_sleep_o)
  );

endmodule
