package uec.teehardware.devices.opentitan

import chisel3._
import chisel3.util._
import freechips.rocketchip.tilelink.TLBundleParameters
import freechips.rocketchip.util._
import uec.teehardware.devices.opentitan.alert.AlertEdge

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

object top_pkg {
  // from top_pkg.sv
  def TL_AW = 32
  def TL_DW = 32
  def TL_AIW = 8    // a_source, d_source
  def TL_DIW = 1    // d_sink
  def TL_DUW = 16   // d_user
  def TL_DBW = (TL_DW>>3)
  def TL_SZW = log2Ceil(log2Ceil(TL_DBW)+1)
  def OpenTitanTLparams = new TLBundleParameters(
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
}

class alert_tx_t extends Bundle{
  val alert_p = Bool()
  val alert_n = Bool()
}

class alert_rx_t extends Bundle{
  val ping_p = Bool()
  val ping_n = Bool()
  val ack_p = Bool()
  val ack_n = Bool()
}

class alert_t extends Bundle{
  val alert_rx = Input(new alert_rx_t)
  val alert_tx = Output(new alert_tx_t)
}

object alert_t {
  def apply : alert_t = new alert_t()
}

class esc_t extends Bundle {
  val esc_rx = Input(new esc_rx_t())
  val esc_tx = Output(new esc_tx_t())
}

object esc_t {
  def apply : esc_t = new esc_t()
}