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
  parameter ibex_pkg::rv32m_e   RV32M             = ibex_pkg::RV32MSingleCycle,
  parameter ibex_pkg::rv32b_e   RV32B             = ibex_pkg::RV32BNone,
  parameter ibex_pkg::regfile_e RegFile           = ibex_pkg::RegFileFF,
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
  output logic                         tl_i_a_valid;
  input  logic                         tl_i_a_ready;
  output logic                  [2:0]  tl_i_a_bits_opcode;
  output logic                  [2:0]  tl_i_a_bits_param;
  output logic  [top_pkg::TL_SZW-1:0]  tl_i_a_bits_size;
  output logic  [top_pkg::TL_AIW-1:0]  tl_i_a_bits_source;
  output logic   [top_pkg::TL_AW-1:0]  tl_i_a_bits_address;
  output logic  [top_pkg::TL_DBW-1:0]  tl_i_a_bits_mask;
  output logic   [top_pkg::TL_DW-1:0]  tl_i_a_bits_data;
  output logic                  [6:0]  tl_i_a_bits_user_tl_a_user_t_rsvd1;
  output logic                  [6:0]  tl_i_a_bits_user_tl_a_user_t_rsvd1;
  output logic                         tl_i_a_bits_user_tl_a_user_t_parity_en;
  output logic                  [7:0]  tl_i_a_bits_user_tl_a_user_t_parity;
  output logic                         tl_i_a_bits_corrupt;

  input  logic                         tl_i_d_bits_valid;
  output logic                         tl_i_d_ready;
  input  logic                  [2:0]  tl_i_d_bits_opcode;
  input  logic                  [2:0]  tl_i_d_bits_param;
  input  logic  [top_pkg::TL_SZW-1:0]  tl_i_d_bits_size;   // Bouncing back a_size
  input  logic  [top_pkg::TL_AIW-1:0]  tl_i_d_bits_source;
  input  logic  [top_pkg::TL_DIW-1:0]  tl_i_d_bits_sink;
  input  logic   [top_pkg::TL_DW-1:0]  tl_i_d_bits_data;
  input  logic  [top_pkg::TL_DUW-1:0]  tl_i_d_bits_user_uint;
  input  logic                         tl_i_d_bits_corrupt;
  input  logic                         tl_i_d_bits_denied;

  // Data memory interface
  output logic                         tl_d_a_valid;
  input  logic                         tl_d_a_ready;
  output logic                  [2:0]  tl_d_a_bits_opcode;
  output logic                  [2:0]  tl_d_a_bits_param;
  output logic  [top_pkg::TL_SZW-1:0]  tl_d_a_bits_size;
  output logic  [top_pkg::TL_AIW-1:0]  tl_d_a_bits_source;
  output logic   [top_pkg::TL_AW-1:0]  tl_d_a_bits_address;
  output logic  [top_pkg::TL_DBW-1:0]  tl_d_a_bits_mask;
  output logic   [top_pkg::TL_DW-1:0]  tl_d_a_bits_data;
  output logic                  [6:0]  tl_d_a_bits_user_tl_a_user_t_rsvd1;
  output logic                  [6:0]  tl_d_a_bits_user_tl_a_user_t_rsvd1;
  output logic                         tl_d_a_bits_user_tl_a_user_t_parity_en;
  output logic                  [7:0]  tl_d_a_bits_user_tl_a_user_t_parity;
  output logic                         tl_d_a_bits_corrupt;

  input  logic                         tl_d_d_bits_valid;
  output logic                         tl_d_d_ready;
  input  logic                  [2:0]  tl_d_d_bits_opcode;
  input  logic                  [2:0]  tl_d_d_bits_param;
  input  logic  [top_pkg::TL_SZW-1:0]  tl_d_d_bits_size;   // Bouncing back a_size
  input  logic  [top_pkg::TL_AIW-1:0]  tl_d_d_bits_source;
  input  logic  [top_pkg::TL_DIW-1:0]  tl_d_d_bits_sink;
  input  logic   [top_pkg::TL_DW-1:0]  tl_d_d_bits_data;
  input  logic  [top_pkg::TL_DUW-1:0]  tl_d_d_bits_user_uint;
  input  logic                         tl_d_d_bits_corrupt;
  input  logic                         tl_d_d_bits_denied;

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

  

  ibex_core #(
    .PMPEnable                ( PMPEnable                ),
    .PMPGranularity           ( PMPGranularity           ),
    .PMPNumRegions            ( PMPNumRegions            ),
    .MHPMCounterNum           ( MHPMCounterNum           ),
    .MHPMCounterWidth         ( MHPMCounterWidth         ),
    .RV32E                    ( RV32E                    ),
    .RV32M                    ( RV32M                    ),
    .RV32B                    ( RV32B                    ),
    .RegFile                  ( RegFile                  ),
    .BranchTargetALU          ( BranchTargetALU          ),
    .WritebackStage           ( WritebackStage           ),
    .ICache                   ( ICache                   ),
    .ICacheECC                ( ICacheECC                ),
    .BranchPredictor          ( BranchPredictor          ),
    .DbgTriggerEn             ( DbgTriggerEn             ),
    .SecureIbex               ( SecureIbex               ),
    .DmHaltAddr               ( DmHaltAddr               ),
    .DmExceptionAddr          ( DmExceptionAddr          )
  ) u_core (
    .clk_i,
    .rst_ni,

    .test_en_i,

    .hart_id_i,
    .boot_addr_i,

    .instr_req_o    ( instr_req    ),
    .instr_gnt_i    ( instr_gnt    ),
    .instr_rvalid_i ( instr_rvalid ),
    .instr_addr_o   ( instr_addr   ),
    .instr_rdata_i  ( instr_rdata  ),
    .instr_err_i    ( instr_err    ),

    .data_req_o     ( data_req     ),
    .data_gnt_i     ( data_gnt     ),
    .data_rvalid_i  ( data_rvalid  ),
    .data_we_o      ( data_we      ),
    .data_be_o      ( data_be      ),
    .data_addr_o    ( data_addr    ),
    .data_wdata_o   ( data_wdata   ),
    .data_rdata_i   ( data_rdata   ),
    .data_err_i     ( data_err     ),

    .irq_software_i,
    .irq_timer_i,
    .irq_external_i,
    .irq_fast_i     ( '0           ),
    .irq_nm_i       ( irq_nm       ),

    .debug_req_i,

    .fetch_enable_i,
    .alert_minor_o    (alert_minor),
    .alert_major_o    (alert_major),
    .core_sleep_o
  );

endmodule
