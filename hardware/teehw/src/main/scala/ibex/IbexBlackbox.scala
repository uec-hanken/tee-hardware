package uec.teehardware.ibex

import sys.process._
import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, RawParam, StringParam}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import uec.teehardware._
import uec.teehardware.devices.opentitan._
import uec.teehardware.devices.opentitan.top_pkg._

class IbexBlackbox
(
  PMPEnable: Boolean = false,
  PMPGranularity: Int = 0,
  PMPNumRegions: Int = 4,
  MHPMCounterNum: Int = 10,
  MHPMCounterWidth: Int = 32,
  RV32E: Boolean = false,
  RV32M: Int = 3, // "ibex_pkg::RV32MSingleCycle",
  RV32B: Int = 0, // "ibex_pkg::RV32BNone",
  RegFile: Int = 0, // "ibex_pkg::RegFileFF",
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
  Synth: Boolean = false,
  SynthFlavor: String = "IbexSecureDefault",
)
  extends BlackBox(
    if(!Synth)
      Map(
        "PMPEnable" -> IntParam(if(PMPEnable) 1 else 0),
        "PMPGranularity" -> IntParam(PMPGranularity),
        "PMPNumRegions" -> IntParam(PMPNumRegions),
        "MHPMCounterNum" -> IntParam(MHPMCounterNum),
        "MHPMCounterWidth" -> IntParam(MHPMCounterWidth),
        "RV32E" -> IntParam(if(RV32E) 1 else 0),
        "RV32M" -> IntParam(RV32M),
        "RV32B" -> IntParam(RV32B),
        "RegFile" -> IntParam(RegFile),
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
    else Map()
  )
    with HasBlackBoxResource
{
  // The TLparams. Those are very specitic
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

  // add wrapper/blackbox after it is pre-processed
  if(Synth) addResource(s"/ibex_syn/IbexBlackbox.$SynthFlavor.v")
  else {
    addResource("/aaaa_pkgs.preprocessed.sv")
    addResource("/tlul.preprocessed.sv")
    addResource("/prim.preprocessed.sv")
    addResource("/Ibex.preprocessed.sv")
    addResource("/Ibex.preprocessed.v")
    addResource("/IbexBlackbox.preprocessed.sv")
  }
}
