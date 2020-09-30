package uec.teehardware.opentitan.rv_core_ibex

import sys.process._
import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, RawParam, StringParam}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import uec.teehardware._

class tl_a_user_t extends Bundle {
  val rsvd1 = UInt(7.W)
  val parity_en = Bool()
  val parity = UInt(8.W)
}

case object tl_a_user_t_Extra extends ControlKey[tl_a_user_t]("tl_a_user_t")
case class tl_a_user_t_ExtraField() extends BundleField(tl_a_user_t_Extra) {
  def data = Output(new tl_a_user_t())
  def default(x: tl_a_user_t) = {
    x.rsvd1   := 0.U
    x.parity_en := false.B
    x.parity := 0.U
  }
}

case object UIntExtra extends ControlKey[UInt]("uint")
case class UIntExtraField(size: Int) extends BundleField(UIntExtra) {
  def data = Output(UInt(size.W))
  def default(x: UInt) = {
    x := 0.U
  }
}

class esc_tx_t extends Bundle{
  val esc_p = Bool()
  val esc_n = Bool()
}

class esc_rx_t extends Bundle{
  val resp_p = Bool()
  val resp_n = Bool()
}

class IbexBlackbox
(
  PMPEnable: Boolean = false,
  PMPGranularity: Int = 0,
  PMPNumRegions: Int = 4,
  MHPMCounterNum: Int = 10,
  MHPMCounterWidth: Int = 32,
  RV32E: Boolean = false,
  RV32M: String = "ibex_pkg::RV32MSingleCycle",
  RV32B: String = "ibex_pkg::RV32BNone",
  RegFile: String = "ibex_pkg::RegFileFF",
  BranchTargetALU: Boolean = false,
  WritebackStage: Boolean = false,
  ICache: Boolean = false,
  ICacheECC: Boolean = false,
  BranchPredictor: Boolean = false,
  DbgTriggerEn: Boolean = false,
  SecureIbex: Boolean = false,
  DmHaltAddr: BigInt = BigInt("1A110800"),
  DmExceptionAddr: BigInt = BigInt("1A110808"),
  PipeLine: Boolean = false,
)
  extends BlackBox(
    Map(
      "PMPEnable" -> IntParam(if(PMPEnable) 1 else 0),
      "PMPGranularity" -> IntParam(PMPGranularity),
      "PMPNumRegions" -> IntParam(PMPNumRegions),
      "MHPMCounterNum" -> IntParam(MHPMCounterNum),
      "MHPMCounterWidth" -> IntParam(MHPMCounterWidth),
      "RV32E" -> IntParam(if(RV32E) 1 else 0),
      "RV32M" -> RawParam(RV32M),
      "RV32B" -> RawParam(RV32B),
      "RegFile" -> RawParam(RegFile),
      "BranchTargetALU" -> IntParam(if(BranchTargetALU) 1 else 0),
      "WritebackStage" -> IntParam(if(WritebackStage) 1 else 0),
      "ICache" -> IntParam(if(ICache) 1 else 0),
      "ICacheECC" -> IntParam(if(ICacheECC) 1 else 0),
      "BranchPredictor" -> IntParam(if(BranchPredictor) 1 else 0),
      "DbgTriggerEn" -> IntParam(if(DbgTriggerEn) 1 else 0),
      "SecureIbex" -> IntParam(if(SecureIbex) 1 else 0),
      "DmHaltAddr" -> IntParam(DmHaltAddr),
      "DmExceptionAddr" -> IntParam(DmExceptionAddr),
      "PipeLine" -> IntParam(if(PipeLine) 1 else 0)
    )
  )
    with HasBlackBoxResource
{
  // The TLparams. Those are very specitic
  // from top_pkg.sv
  val TL_AW = 32
  val TL_DW = 32
  val TL_AIW = 8    // a_source, d_source
  val TL_DIW = 1    // d_sink
  val TL_DUW = 16   // d_user
  val TL_DBW = (TL_DW>>3)
  val TL_SZW = log2Ceil(log2Ceil(TL_DBW)+1)
  val TLparams = new TLBundleParameters(
    addressBits = TL_AW,
    dataBits = TL_DW,
    sourceBits = TL_AIW,
    sinkBits = TL_DIW,
    sizeBits = TL_SZW,
    echoFields = Seq(),
    requestFields = Seq(
      tl_a_user_t_ExtraField()
    ),
    responseFields = Seq(
      UIntExtraField(TL_DUW)
    ),
    hasBCE = false
  )

  val io = IO(new Bundle {
    // Clock and Reset
    val clk_i = Input(Clock())
    val rst_ni = Input(Bool())

    val test_en_i = Input(Bool()) // enable all clock gates for testing

    val hart_id_i = Input(UInt(32.W))
    val boot_addr_i = Input(UInt(32.W))

    // Instruction memory interface
    val tl_i = new TLBundle(TLparams)

    // Data memory interface
    val tl_d = new TLBundle(TLparams)

    // Interrupt inputs
    val irq_software_i = Input(Bool())
    val irq_timer_i = Input(Bool())
    val irq_external_i = Input(Bool())

    // Escalation input for NMI
    val esc_tx_i = Input(new esc_tx_t())
    val esc_rx_o = Output(new esc_rx_t())

    // Debug Interface
    val debug_req_i = Input(Bool())

    // CPU Control Signals
    val fetch_enable_i = Input(Bool())
    val core_sleep_o = Output(Bool())
  })

  // pre-process the verilog to remove "includes" and combine into one file
  val make = "make -C hardware/teehw/src/main/resources ibex"
  val proc = make
  require (proc.! == 0, "Failed to run preprocessing step")

  // add wrapper/blackbox after it is pre-processed
  addResource("/IbexBlackbox.preprocessed.sv")
  addResource("/IbexBlackbox.preprocessed.v")
}