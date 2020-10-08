package uec.teehardware.devices.opentitan.nmi_gen

import chisel3._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import uec.teehardware.devices.opentitan._

class EscEmpty()(implicit p: Parameters) extends LazyModule
{
  val escnode = EscNexusNode(
    sinkFn         = { _ => EscSinkPortParameters(Seq(EscSinkParameters())) },
    sourceFn       = { _ => EscSourcePortParameters(Seq(EscSourceParameters())) })

  lazy val module = new LazyModuleImp(this) {
    escnode.in.foreach { case (i, _) => i.esc_tx := 0.U.asTypeOf(new esc_tx_t) }
    escnode.out.foreach { case (o, _) => o.esc_rx := 0.U.asTypeOf(new esc_rx_t) }
  }
}

object EscEmpty {
  def apply(implicit p: Parameters): EscNexusNode = {
    val xbar = LazyModule(new EscEmpty)
    xbar.escnode
  }
}
