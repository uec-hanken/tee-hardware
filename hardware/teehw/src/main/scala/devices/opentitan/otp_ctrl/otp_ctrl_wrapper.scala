package uec.teehardware.devices.opentitan.otp_ctrl

import chisel3._
import chisel3.experimental._
import chisel3.util._
import freechips.rocketchip.tilelink._
import uec.teehardware.devices.opentitan._
import uec.teehardware.devices.opentitan.top_pkg._

import sys.process._

class otp_ast_req_t extends Bundle {
  val pwr_seq = UInt(otp_ctrl_reg_pkg.OtpPwrSeqWidth.W)
}

class otp_ast_rsp_t extends Bundle {
  val pwr_seq_h = UInt(otp_ctrl_reg_pkg.OtpPwrSeqWidth.W)
}

class otp_edn_req_t extends Bundle {
  val req = Bool()
}

class otp_edn_rsp_t extends Bundle {
  val ack = Bool()
  val data = UInt(otp_ctrl_reg_pkg.EdnDataWidth.W)
}

class pwr_otp_init_req_t extends Bundle {
  val init = Bool()
}

class pwr_otp_init_rsp_t extends Bundle {
  val done = Bool()
}

// TODO: Those should be in pwrmg
class pwr_otp_req_t extends Bundle {
  val otp_init = Bool()
}

class pwr_otp_rsp_t extends Bundle {
  val otp_done = Bool()
  val otp_idle = Bool()
}

class otp_pwr_state_t extends Bundle {
  val idle = Bool()
}

class lc_otp_program_req_t extends Bundle {
  val req = Bool()
  val state_delta = UInt(otp_ctrl_reg_pkg.LcStateWidth.W)
  val count_delta = Vec(otp_ctrl_reg_pkg.NumLcCountValues, UInt(otp_ctrl_reg_pkg.LcValueWidth.W))
}

class lc_otp_program_rsp_t extends Bundle {
  val err = Bool()
  val ack = Bool()
}

class lc_otp_token_req_t extends Bundle {
  val req = Bool()
  val token_input = UInt(otp_ctrl_reg_pkg.LcTokenWidth.W)
}

class lc_otp_token_rsp_t extends Bundle {
  val ack = Bool()
  val hashed_token = UInt(otp_ctrl_reg_pkg.LcTokenWidth.W)
}

// TODO: should be in lc_ctrl_pkg
class lc_tx_t extends Bundle {
  val state = UInt(3.W)
}

class otp_lc_data_t extends Bundle {
  val valid = Bool()
  val state = UInt(otp_ctrl_reg_pkg.LcStateWidth.W)
  val count = Vec(otp_ctrl_reg_pkg.NumLcCountValues, UInt(otp_ctrl_reg_pkg.LcValueWidth.W))
  val test_unlock_token = UInt(otp_ctrl_reg_pkg.LcTokenWidth.W)
  val test_exit_token = UInt(otp_ctrl_reg_pkg.LcTokenWidth.W)
  val rma_token = UInt(otp_ctrl_reg_pkg.LcTokenWidth.W)
  val id_state = UInt(otp_ctrl_reg_pkg.LcValueWidth.W)
}

class otp_keymgr_key_t extends Bundle {
  val valid = Bool()
  val key_share0 = UInt(otp_ctrl_reg_pkg.KeyMgrKeyWidth.W)
  val key_share1 = UInt(otp_ctrl_reg_pkg.KeyMgrKeyWidth.W)
}

class flash_otp_key_req_t extends Bundle {
  val data_req = Bool()
  val addr_req = Bool()
}

class flash_otp_key_rsp_t extends Bundle {
  val data_ack = Bool()
  val addr_ack = Bool()
  val key = UInt(otp_ctrl_reg_pkg.FlashKeyWidth.W)
  val seed_valid = Bool()
}

class sram_otp_key_req_t extends Bundle {
  val req = Bool()
}

class sram_otp_key_rsp_t extends Bundle {
  val ack = Bool()
  val key = UInt(otp_ctrl_reg_pkg.SramKeyWidth.W)
  val nonce = UInt(otp_ctrl_reg_pkg.SramNonceWidth.W)
  val seed_valid = Bool()
}

class otbn_otp_key_req_t extends Bundle {
  val req = Bool()
}

class otbn_otp_key_rsp_t extends Bundle {
  val ack = Bool()
  val key = UInt(otp_ctrl_reg_pkg.OtbnKeyWidth.W)
  val nonce = UInt(otp_ctrl_reg_pkg.OtbnNonceWidth.W)
  val seed_valid = Bool()
}

class otp_ctrl_wrapper
(
  AlertAsyncOn: Seq[Boolean] = Seq.fill(otp_ctrl_reg_pkg.NumAlerts)(true),
  TimerLfsrSeed: Int = 1,
  LfsrPerm: Seq[Int] = Seq(
    13, 17, 29, 11, 28, 12,  33, 27,
    5, 39, 31, 21, 15,  1, 24, 37,
    32, 38, 26, 34,  8, 10,  4,  2,
    19,  0, 20,  6, 25, 22,  3, 35,
    16, 14, 23,  7, 30,  9, 18, 36)
)
  extends BlackBox(
    Map(
      "TimerLfsrSeed" -> IntParam(TimerLfsrSeed), // TODO: Maybe this is okay?
      "AlertAsyncOn" -> IntParam( // Default: all ones
        AlertAsyncOn.map(if(_) 1 else 0).fold(0)((a,b) => a<<1+b)
      ),
      // TODO: This parameter cannot be entered as RawParam, but there is no way to also
      // TODO: make a vector of Integers as parameters in firrtl. Kinda stuck until they add that
      // WORKAROUND: Just modify the verilog, dummy
      /*"LfsrPerm" -> RawParam(
        "{" + LfsrPerm.map("32'd" + _.toString).reduce((a,b) => a + "," + b) + "}"
      )*/
    )
  )
    with HasBlackBoxResource with HasBlackBoxInline {

  val io = IO(new Bundle {
    // Clock and Reset
    val clk_i = Input(Clock())
    val rst_ni = Input(Bool())

    // Bus interface
    val tl = Flipped(new TLBundle(OpenTitanTLparams))

    // Interrupt Requests
    val intr_otp_operation_done_o = Output(Bool())
    val intr_otp_error_o = Output(Bool())

    // Alerts
    val alert_rx_i = Input(Vec(otp_ctrl_reg_pkg.NumAlerts, new alert_rx_t()))
    val alert_tx_o = Output(Vec(otp_ctrl_reg_pkg.NumAlerts, new alert_tx_t()))

    // AST Interface
    val otp_ast_pwr_seq_o = Output(new otp_ast_req_t())
    val otp_ast_pwr_seq_h_i = Input(new otp_ast_rsp_t())

    // TODO: EDN interface for requesting entropy
    val otp_edn_o = Output(new otp_edn_req_t())
    val otp_edn_i = Input(new otp_edn_rsp_t())

    // Power manager interface
    val pwr_otp_i = Input(new pwr_otp_req_t())
    val pwr_otp_o = Output(new pwr_otp_rsp_t())

    // Lifecycle transition command interface
    val lc_otp_program_i = Input(new lc_otp_program_req_t())
    val lc_otp_program_o = Output(new lc_otp_program_rsp_t())

    // Lifecycle hashing interface for raw unlock
    val lc_otp_token_i = Input(new lc_otp_token_req_t())
    val lc_otp_token_o = Output(new lc_otp_token_rsp_t())

    // Lifecycle broadcast inputs
    val lc_escalate_en_i = Input(new lc_tx_t())
    val lc_provision_en_i = Input(new lc_tx_t())
    val lc_dft_en_i = Input(new lc_tx_t())

    // OTP broadcast outputs
    val otp_lc_data_o = Output(new otp_lc_data_t())
    val otp_keymgr_key_o = Output(new otp_keymgr_key_t())

    // Scrambling key requests
    val flash_otp_key_i = Input(new flash_otp_key_req_t())
    val flash_otp_key_o = Output(new flash_otp_key_rsp_t())
    val sram_otp_key_i = Input(Vec(otp_ctrl_reg_pkg.NumSramKeyReqSlots, new sram_otp_key_req_t()))
    val sram_otp_key_o = Output(Vec(otp_ctrl_reg_pkg.NumSramKeyReqSlots, new sram_otp_key_rsp_t()))
    val otbn_otp_key_i = Input(new otbn_otp_key_req_t())
    val otbn_otp_key_o = Output(new otbn_otp_key_rsp_t())

    // Hardware configuration bits
    val hw_cfg_o = Output(UInt(otp_ctrl_reg_pkg.NumHwCfgBits.W))
  })

  // add wrapper/blackbox after it is pre-processed
  addResource("/aaaa_pkgs.preprocessed.sv")
  addResource("/tlul.preprocessed.sv")
  addResource("/prim.preprocessed.sv")
  addResource("/otp_ctrl.preprocessed.sv")
  addResource("/otp_ctrl.preprocessed.v")

  // The inline generation

  // The ports for alert
  val alert_port = (for(i <- 0 until otp_ctrl_reg_pkg.NumAlerts) yield
    s"""
       |  input  logic                         alert_rx_i_${i}_ping_p,
       |  input  logic                         alert_rx_i_${i}_ping_n,
       |  input  logic                         alert_rx_i_${i}_ack_p,
       |  input  logic                         alert_rx_i_${i}_ack_n,
       |
       |  output logic                         alert_tx_o_${i}_alert_p,
       |  output logic                         alert_tx_o_${i}_alert_n,
       |""".stripMargin).reduce(_+_)
  val alert_connections =
    s"""
       |  wire prim_alert_pkg::alert_rx_t[${otp_ctrl_reg_pkg.NumAlerts-1}:0] alert_rx_i;
       |  wire prim_alert_pkg::alert_tx_t[${otp_ctrl_reg_pkg.NumAlerts-1}:0] alert_tx_o;
       |""".stripMargin +
      ( for(i <- 0 until otp_ctrl_reg_pkg.NumAlerts) yield
        s"""
           |  assign alert_rx_i[${i}].ping_p = alert_rx_i_${i}_ping_p;
           |  assign alert_rx_i[${i}].ping_n = alert_rx_i_${i}_ping_n;
           |  assign alert_rx_i[${i}].ack_p = alert_rx_i_${i}_ack_p;
           |  assign alert_rx_i[${i}].ack_n = alert_rx_i_${i}_ack_n;
           |  assign alert_tx_o_${i}_alert_p = alert_tx_o[${i}].alert_p;
           |  assign alert_tx_o_${i}_alert_n = alert_tx_o[${i}].alert_n;
           |""".stripMargin ).reduce(_+_)
  val lc_otp_program_i_port =
    s"""
       |  input logic lc_otp_program_i_req,
       |  input logic [${otp_ctrl_reg_pkg.LcStateWidth - 1}:0] lc_otp_program_i_state_delta,
       |""".stripMargin +
      ( for(i <- 0 until otp_ctrl_reg_pkg.NumLcCountValues) yield
        s"""
           |  output logic [${otp_ctrl_reg_pkg.LcValueWidth - 1}:0] lc_otp_program_i_count_delta_${i},
           |""".stripMargin ).reduce(_+_)
  val lc_otp_program_i_connections =
    s"""
       |  wire otp_ctrl_pkg::lc_otp_program_req_t lc_otp_program_i;
       |  assign lc_otp_program_i.req = lc_otp_program_i_req;
       |  assign lc_otp_program_i.state_delta = lc_otp_program_i_state_delta;
       |""".stripMargin +
      ( for(i <- 0 until otp_ctrl_reg_pkg.NumLcCountValues) yield
        s"""
           |  assign lc_otp_program_i.count_delta[${i}] = lc_otp_program_i_count_delta_${i};
           |""".stripMargin ).reduce(_+_)
  val otp_lc_data_o_port =
    s"""
       |  output logic otp_lc_data_o_valid,
       |  output logic [${otp_ctrl_reg_pkg.LcStateWidth - 1}:0] otp_lc_data_o_state,
       |  output logic [${otp_ctrl_reg_pkg.LcTokenWidth - 1}:0] otp_lc_data_o_test_unlock_token,
       |  output logic [${otp_ctrl_reg_pkg.LcTokenWidth - 1}:0] otp_lc_data_o_test_exit_token,
       |  output logic [${otp_ctrl_reg_pkg.LcTokenWidth - 1}:0] otp_lc_data_o_rma_token,
       |  output logic [${otp_ctrl_reg_pkg.LcValueWidth - 1}:0] otp_lc_data_o_id_state,
       |""".stripMargin +
      ( for(i <- 0 until otp_ctrl_reg_pkg.NumLcCountValues) yield
        s"""
           |  output logic [${otp_ctrl_reg_pkg.LcValueWidth - 1}:0] otp_lc_data_o_count_${i},
           |""".stripMargin ).reduce(_+_)
  val otp_lc_data_o_connections =
    s"""
       |  wire otp_ctrl_pkg::otp_lc_data_t otp_lc_data_o;
       |  assign otp_lc_data_o_valid = otp_lc_data_o.valid;
       |  assign otp_lc_data_o_state = otp_lc_data_o.state;
       |  assign otp_lc_data_o_test_unlock_token = otp_lc_data_o.test_unlock_token;
       |  assign otp_lc_data_o_test_exit_token = otp_lc_data_o.test_exit_token;
       |  assign otp_lc_data_o_rma_token = otp_lc_data_o.rma_token;
       |  assign otp_lc_data_o_id_state = otp_lc_data_o.id_state;
       |""".stripMargin +
      ( for(i <- 0 until otp_ctrl_reg_pkg.NumLcCountValues) yield
        s"""
           |  assign otp_lc_data_o.count[${i}] = otp_lc_data_o_count_${i};
           |""".stripMargin ).reduce(_+_)
  val sram_otp_key_port =
    ( for(i <- 0 until otp_ctrl_reg_pkg.NumSramKeyReqSlots) yield
      s"""
         |  input logic sram_otp_key_i_${i}_req,
         |  output logic sram_otp_key_o_${i}_ack,
         |  output logic [${otp_ctrl_reg_pkg.SramKeyWidth - 1}:0] sram_otp_key_o_${i}_key,
         |  output logic [${otp_ctrl_reg_pkg.SramKeyWidth - 1}:0] sram_otp_key_o_${i}_nonce,
         |  output logic sram_otp_key_o_${i}_seed_valid,
         |""".stripMargin ).reduce(_+_)
  val sram_otp_key_connections =
    s"""
       |  wire otp_ctrl_pkg::sram_otp_key_req_t [${otp_ctrl_reg_pkg.NumSramKeyReqSlots - 1}:0] sram_otp_key_i;
       |  wire otp_ctrl_pkg::sram_otp_key_rsp_t [${otp_ctrl_reg_pkg.NumSramKeyReqSlots - 1}:0] sram_otp_key_o;
       |""".stripMargin +
      ( for(i <- 0 until otp_ctrl_reg_pkg.NumSramKeyReqSlots) yield
        s"""
           |  assign sram_otp_key_i[${i}].req = sram_otp_key_i_${i}_req;
           |  assign sram_otp_key_o_${i}_ack = sram_otp_key_o[${i}].ack;
           |  assign sram_otp_key_o_${i}_key = sram_otp_key_o[${i}].key;
           |  assign sram_otp_key_o_${i}_nonce = sram_otp_key_o[${i}].nonce;
           |  assign sram_otp_key_o_${i}_seed_valid = sram_otp_key_o[${i}].seed_valid;
           |""".stripMargin ).reduce(_+_)

  setInline("otp_ctrl_wrapper.sv",
    s"""
       |// Copyright lowRISC contributors.
       |// Licensed under the Apache License, Version 2.0, see LICENSE for details.
       |// SPDX-License-Identifier: Apache-2.0
       |
       |// This blackbox is auto-generated
       |
       |module otp_ctrl_wrapper
       |#(
       |  // TODO: set this when integrating the module into the top-level.
       |  // There is no limit on the number of SRAM key request generation slots,
       |  // since each requested key is ephemeral.
       |  parameter int                          NumSramKeyReqSlots = 2,
       |  // Enable asynchronous transitions on alerts.
       |  parameter logic [otp_ctrl_reg_pkg::NumAlerts-1:0]        AlertAsyncOn = {otp_ctrl_reg_pkg::NumAlerts{1'b1}},
       |  // TODO: These constants have to be replaced by the silicon creator before taping out.
       |  parameter logic [otp_ctrl_pkg::TimerWidth-1:0]       TimerLfsrSeed     = otp_ctrl_pkg::TimerWidth'(1'b1),
       |  parameter logic [otp_ctrl_pkg::TimerWidth-1:0][31:0] LfsrPerm     = {
       |    32'd13, 32'd17, 32'd29, 32'd11, 32'd28, 32'd12, 32'd33, 32'd27,
       |    32'd05, 32'd39, 32'd31, 32'd21, 32'd15, 32'd01, 32'd24, 32'd37,
       |    32'd32, 32'd38, 32'd26, 32'd34, 32'd08, 32'd10, 32'd04, 32'd02,
       |    32'd19, 32'd00, 32'd20, 32'd06, 32'd25, 32'd22, 32'd03, 32'd35,
       |    32'd16, 32'd14, 32'd23, 32'd07, 32'd30, 32'd09, 32'd18, 32'd36
       |  }
       |) (
       |  // Clock and Reset
       |  input  logic        clk_i,
       |  input  logic        rst_ni,
       |
       |  // Instruction memory interface
       |  input  logic                         tl_a_valid,
       |  output logic                         tl_a_ready,
       |  input  logic                  [2:0]  tl_a_bits_opcode,
       |  input  logic                  [2:0]  tl_a_bits_param,
       |  input  logic  [top_pkg::TL_SZW-1:0]  tl_a_bits_size,
       |  input  logic  [top_pkg::TL_AIW-1:0]  tl_a_bits_source,
       |  input  logic   [top_pkg::TL_AW-1:0]  tl_a_bits_address,
       |  input  logic  [top_pkg::TL_DBW-1:0]  tl_a_bits_mask,
       |  input  logic   [top_pkg::TL_DW-1:0]  tl_a_bits_data,
       |  input  logic                  [6:0]  tl_a_bits_user_tl_a_user_t_rsvd1,
       |  input  logic                         tl_a_bits_user_tl_a_user_t_parity_en,
       |  input  logic                  [7:0]  tl_a_bits_user_tl_a_user_t_parity,
       |  input  logic                         tl_a_bits_corrupt,
       |
       |  output logic                         tl_d_valid,
       |  input  logic                         tl_d_ready,
       |  output logic                  [2:0]  tl_d_bits_opcode,
       |  output logic                  [2:0]  tl_d_bits_param,
       |  output logic  [top_pkg::TL_SZW-1:0]  tl_d_bits_size,   // Bouncing back a_size
       |  output logic  [top_pkg::TL_AIW-1:0]  tl_d_bits_source,
       |  output logic  [top_pkg::TL_DIW-1:0]  tl_d_bits_sink,
       |  output logic   [top_pkg::TL_DW-1:0]  tl_d_bits_data,
       |  output logic  [top_pkg::TL_DUW-1:0]  tl_d_bits_user_uint,
       |  output logic                         tl_d_bits_corrupt,
       |  output logic                         tl_d_bits_denied,
       |
       |  // Interrupts
       |  output logic                         intr_otp_operation_done_o,
       |  output logic                         intr_otp_error_o,
       |
       |  // Alerts
       |""".stripMargin +
      alert_port + lc_otp_program_i_port + otp_lc_data_o_port + sram_otp_key_port +
      s"""
         |  output logic                 [${otp_ctrl_reg_pkg.OtpPwrSeqWidth - 1}:0]  otp_ast_pwr_seq_o_pwr_seq,
         |  input  logic                 [${otp_ctrl_reg_pkg.OtpPwrSeqWidth - 1}:0]  otp_ast_pwr_seq_h_i_pwr_seq_h,
         |
         |  output logic                         otp_edn_o_req,
         |  input  logic                         otp_edn_i_ack,
         |  input  logic                 [${otp_ctrl_reg_pkg.EdnDataWidth - 1}:0]  otp_edn_i_data,
         |
         |  input  logic                         pwr_otp_i_otp_init,
         |  output logic                         pwr_otp_o_otp_done,
         |  output logic                         pwr_otp_o_otp_idle,
         |
         |  output logic                         lc_otp_program_o_err,
         |  output logic                         lc_otp_program_o_ack,
         |
         |  input  logic                         lc_otp_token_i_req,
         |  input  logic                 [${otp_ctrl_reg_pkg.LcTokenWidth - 1}:0]  lc_otp_token_i_token_input,
         |  output logic                         lc_otp_token_o_ack,
         |  output logic                 [${otp_ctrl_reg_pkg.LcTokenWidth - 1}:0]  lc_otp_token_o_hashed_token,
         |
         |  input  logic                  [2:0]  lc_escalate_en_i_state,
         |  input  logic                  [2:0]  lc_provision_en_i_state,
         |  input  logic                  [2:0]  lc_dft_en_i_state,
         |
         |  output logic                         otp_keymgr_key_o_valid,
         |  output logic                  [${otp_ctrl_reg_pkg.KeyMgrKeyWidth - 1}:0]  otp_keymgr_key_o_key_share0,
         |  output logic                  [${otp_ctrl_reg_pkg.KeyMgrKeyWidth - 1}:0]  otp_keymgr_key_o_key_share1,
         |
         |  input  logic                         flash_otp_key_i_data_req,
         |  input  logic                         flash_otp_key_i_addr_req,
         |  output logic                         flash_otp_key_o_data_ack,
         |  output logic                         flash_otp_key_o_addr_ack,
         |  output logic                  [${otp_ctrl_reg_pkg.FlashKeyWidth - 1}:0]  flash_otp_key_o_key,
         |  output logic                         flash_otp_key_o_seed_valid,
         |
         |  input  logic                         otbn_otp_key_i_req,
         |  output logic                         otbn_otp_key_o_ack,
         |  output logic                  [${otp_ctrl_reg_pkg.OtbnKeyWidth - 1}:0]  otbn_otp_key_o_key,
         |  output logic                  [${otp_ctrl_reg_pkg.OtbnNonceWidth - 1}:0]  otbn_otp_key_o_nonce,
         |  output logic                         otbn_otp_key_o_seed_valid,
         |
         |  output logic                  [${otp_ctrl_reg_pkg.NumHwCfgBits - 1}:0]  hw_cfg_o
         |);
         |
         |  // tl connections
         |
         |  wire tlul_pkg::tl_h2d_t tl_i;
         |  wire tlul_pkg::tl_d2h_t tl_o;
         |
         |  assign tl_i.a_valid = tl_a_valid;
         |  assign tl_i.a_opcode = tl_a_bits_opcode;
         |  assign tl_i.a_param = tl_a_bits_param;
         |  assign tl_i.a_size = tl_a_bits_size;
         |  assign tl_i.a_source = tl_a_bits_source;
         |  assign tl_i.a_address = tl_a_bits_address;
         |  assign tl_i.a_mask = tl_a_bits_mask;
         |  assign tl_i.a_data = tl_a_bits_data;
         |  assign tl_i.a_user.rsvd1 = tl_a_bits_user_tl_a_user_t_rsvd1;
         |  assign tl_i.a_user.parity_en = tl_a_bits_user_tl_a_user_t_parity_en;
         |  assign tl_i.a_user.parity = tl_a_bits_user_tl_a_user_t_parity;
         |
         |  assign tl_i.d_ready = tl_d_ready;
         |
         |  // tl_a_bits_corrupt; // ignored
         |
         |  assign tl_d_valid = tl_o.d_valid;
         |  assign tl_d_bits_opcode = tl_o.d_opcode;
         |  assign tl_d_bits_param = tl_o.d_param;
         |  assign tl_d_bits_size = tl_o.d_size;
         |  assign tl_d_bits_source = tl_o.d_source;
         |  assign tl_d_bits_sink = tl_o.d_sink;
         |  assign tl_d_bits_data = tl_o.d_data;
         |  assign tl_d_bits_user_uint = tl_o.d_user;
         |  assign tl_d_bits_corrupt = tl_o.d_error; // Seems legit
         |  assign tl_d_bits_denied = 1'b0; // Seems also legit
         |
         |  assign tl_a_ready = tl_o.a_ready;

         |""".stripMargin +
      alert_connections + lc_otp_program_i_connections + otp_lc_data_o_connections + sram_otp_key_connections +
      """
        |  wire otp_ctrl_pkg::otp_ast_req_t otp_ast_pwr_seq_o;
        |  wire otp_ctrl_pkg::otp_ast_rsp_t otp_ast_pwr_seq_h_i;
        |  assign otp_ast_pwr_seq_o_pwr_seq = otp_ast_pwr_seq_o.pwr_seq;
        |  assign otp_ast_pwr_seq_h_i.pwr_seq_h = otp_ast_pwr_seq_h_i_pwr_seq_h;
        |
        |  wire otp_ctrl_pkg::otp_edn_req_t otp_edn_o;
        |  wire otp_ctrl_pkg::otp_edn_rsp_t otp_edn_i;
        |  assign otp_edn_o_req = otp_edn_o.req;
        |  assign otp_edn_i.ack = otp_edn_i_ack;
        |  assign otp_edn_i.data = otp_edn_i_data;
        |
        |  wire pwrmgr_pkg::pwr_otp_req_t pwr_otp_i;
        |  wire pwrmgr_pkg::pwr_otp_rsp_t pwr_otp_o;
        |  assign pwr_otp_i.otp_init = pwr_otp_i_otp_init;
        |  assign pwr_otp_o_otp_done = pwr_otp_o.otp_done;
        |  assign pwr_otp_o_otp_idle = pwr_otp_o.otp_idle;
        |
        |  wire otp_ctrl_pkg::lc_otp_program_rsp_t lc_otp_program_o;
        |  assign lc_otp_program_o_err = lc_otp_program_o.err;
        |  assign lc_otp_program_o_ack = lc_otp_program_o.ack;
        |
        |  wire otp_ctrl_pkg::lc_otp_token_req_t lc_otp_token_i;
        |  wire otp_ctrl_pkg::lc_otp_token_rsp_t lc_otp_token_o;
        |  assign lc_otp_token_i.req = lc_otp_token_i_req;
        |  assign lc_otp_token_i.token_input = lc_otp_token_i_token_input;
        |  assign lc_otp_token_o_ack = lc_otp_token_o.ack;
        |  assign lc_otp_token_o_hashed_token = lc_otp_token_o.hashed_token;
        |
        |  wire lc_ctrl_pkg::lc_tx_t lc_escalate_en_i;
        |  wire lc_ctrl_pkg::lc_tx_t lc_provision_en_i;
        |  wire lc_ctrl_pkg::lc_tx_t lc_dft_en_i;
        |  assign lc_escalate_en_i.state = lc_escalate_en_i_state;
        |  assign lc_provision_en_i.state = lc_provision_en_i_state;
        |  assign lc_dft_en_i.state = lc_dft_en_i_state;
        |
        |  wire otp_ctrl_pkg::otp_keymgr_key_t otp_keymgr_key_o;
        |  assign otp_keymgr_key_o_valid = otp_keymgr_key_o.valid;
        |  assign otp_keymgr_key_o_key_share0 = otp_keymgr_key_o.key_share0;
        |  assign otp_keymgr_key_o_key_share1 = otp_keymgr_key_o.key_share1;
        |
        |  wire otp_ctrl_pkg::flash_otp_key_req_t flash_otp_key_i;
        |  wire otp_ctrl_pkg::flash_otp_key_rsp_t flash_otp_key_o;
        |  assign flash_otp_key_i.data_req = flash_otp_key_i_data_req;
        |  assign flash_otp_key_i.addr_req = flash_otp_key_i_addr_req;
        |  assign flash_otp_key_o_data_ack = flash_otp_key_o.data_ack;
        |  assign flash_otp_key_o_addr_ack = flash_otp_key_o.addr_ack;
        |  assign flash_otp_key_o_key = flash_otp_key_o.key;
        |  assign flash_otp_key_o_seed_valid = flash_otp_key_o.seed_valid;
        |
        |  wire otp_ctrl_pkg::otbn_otp_key_req_t otbn_otp_key_i;
        |  wire otp_ctrl_pkg::otbn_otp_key_rsp_t otbn_otp_key_o;
        |  assign otbn_otp_key_i.req = otbn_otp_key_i_req;
        |  assign otbn_otp_key_o_ack = otbn_otp_key_o.ack;
        |  assign otbn_otp_key_o_key = otbn_otp_key_o.key;
        |  assign otbn_otp_key_o_nonce = otbn_otp_key_o.nonce;
        |  assign otbn_otp_key_o_seed_valid = otbn_otp_key_o.seed_valid;
        |
        |  otp_ctrl #(
        |    .AlertAsyncOn              ( AlertAsyncOn       ),
        |    .TimerLfsrSeed                  ( TimerLfsrSeed           )
        |  ) u_otp_ctrl (
        |    // clock and reset
        |    .clk_i                     (clk_i),
        |    .rst_ni                    (rst_ni),
        |    // TL-UL buses
        |    .tl_o                      (tl_o),
        |    .tl_i                      (tl_i),
        |    // Interrupts
        |    .intr_otp_operation_done_o (intr_otp_operation_done_o),
        |    .intr_otp_error_o          (intr_otp_error_o),
        |    // Alerts
        |    .alert_rx_i                (alert_rx_i),
        |    .alert_tx_o                (alert_tx_o),
        |    // AST Interface
        |    .otp_ast_pwr_seq_o         (otp_ast_pwr_seq_o),
        |    .otp_ast_pwr_seq_h_i       (otp_ast_pwr_seq_h_i),
        |    // TODO: EDN interface for requesting entropy
        |    .otp_edn_o                 (otp_edn_o),
        |    .otp_edn_i                 (otp_edn_i),
        |    // Power manager interface
        |    .pwr_otp_i                 (pwr_otp_i),
        |    .pwr_otp_o                 (pwr_otp_o),
        |    // Lifecycle transition command interface
        |    .lc_otp_program_i      (lc_otp_program_i),
        |    .lc_otp_program_o      (lc_otp_program_o),
        |    // Lifecycle hashing interface for raw unlock
        |    .lc_otp_token_i        (lc_otp_token_i),
        |    .lc_otp_token_o        (lc_otp_token_o),
        |    // Lifecycle broadcast inputs
        |    .lc_escalate_en_i          (lc_escalate_en_i),
        |    .lc_provision_en_i         (lc_provision_en_i),
        |    .lc_dft_en_i              (lc_dft_en_i),
        |    // OTP broadcast outputs
        |    .otp_lc_data_o             (otp_lc_data_o),
        |    .otp_keymgr_key_o          (otp_keymgr_key_o),
        |    // Scrambling key requests
        |    .flash_otp_key_i       (flash_otp_key_i),
        |    .flash_otp_key_o       (flash_otp_key_o),
        |    .sram_otp_key_i        (sram_otp_key_i),
        |    .sram_otp_key_o        (sram_otp_key_o),
        |    .otbn_otp_key_i        (otbn_otp_key_i),
        |    .otbn_otp_key_o        (otbn_otp_key_o),
        |    // Hardware config bits
        |    .hw_cfg_o                  (hw_cfg_o)
        |  );
        |
        |endmodule
        |""".stripMargin)
}
