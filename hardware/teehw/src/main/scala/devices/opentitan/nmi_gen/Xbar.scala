package uec.teehardware.devices.opentitan.nmi_gen

import chisel3._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import uec.teehardware.devices.opentitan._

class EscXbar()(implicit p: Parameters) extends LazyModule
{
  val escnode = EscNexusNode(
    sinkFn         = { _ => EscSinkPortParameters(Seq(EscSinkParameters())) },
    sourceFn       = { _ => EscSourcePortParameters(Seq(EscSourceParameters()))  })

  lazy val module = new LazyModuleImp(this) {
    (escnode.out zip escnode.in).foreach { case ((o: esc_t,_), (i: esc_t,_)) =>
      o.esc_tx := i.esc_tx
      i.esc_rx := o.esc_rx
    }
  }
}

object EscXbar {
  def apply(implicit p: Parameters): EscNexusNode = {
    val xbar = LazyModule(new EscXbar)
    xbar.escnode
  }
}
