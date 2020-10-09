package uec.teehardware.devices.opentitan.otp_ctrl

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.util.HeterogeneousBag
import uec.teehardware._

case object PeripheryOTPCtrlKey extends Field[List[OTPCtrlParams]](List())

trait HasPeripheryOTPCtrl { this: TEEHWSubsystem =>
  val OTPCtrlDevs = p(PeripheryOTPCtrlKey).map { case key =>
    OTPCtrlAttachParams(key, alertnode).attachTo(this)
  }
  val OTPCtrl = OTPCtrlDevs.map {
    case i =>
      i.ioNode.makeSink()
  }
}

trait HasPeripheryOTPCtrlModuleImp extends LazyModuleImp {
  val outer: HasPeripheryOTPCtrl
  val OTPCtrl = outer.OTPCtrl.zipWithIndex.map{
    case (n,_) =>
      // Just nullify this port
      n.bundle.edn_otp_up_i := 0.U.asTypeOf(new edn_otp_up_t)
      n.bundle.otp_edn_rsp_i := 0.U.asTypeOf(new otp_edn_rsp_t)
      n.bundle.pwr_otp_init_req_i := 0.U.asTypeOf(new pwr_otp_init_req_t)
      n.bundle.lc_otp_program_req_i := 0.U.asTypeOf(new lc_otp_program_req_t)
      n.bundle.lc_otp_token_req_i := 0.U.asTypeOf(new lc_otp_token_req_t)
      n.bundle.lc_escalate_en_i := 0.U.asTypeOf(new lc_tx_t)
      n.bundle.lc_provision_en_i := 0.U.asTypeOf(new lc_tx_t)
      n.bundle.lc_test_en_i := 0.U.asTypeOf(new lc_tx_t)
      n.bundle.flash_otp_key_req_i := 0.U.asTypeOf(new flash_otp_key_req_t)
      n.bundle.sram_otp_key_req_i.foreach(_ := 0.U.asTypeOf(new sram_otp_key_req_t))
      n.bundle.otbn_otp_key_req_i := 0.U.asTypeOf(new otbn_otp_key_req_t)
  }
}