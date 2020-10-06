package uec.teehardware.devices.opentitan.alert

import chisel3._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._

class AlertXbar()(implicit p: Parameters) extends LazyModule
{
  val alertnode = AlertNexusNode(
    sinkFn         = { _ => AlertSinkPortParameters(Seq(AlertSinkParameters())) },
    sourceFn       = { seq =>
      AlertSourcePortParameters((seq zip seq.map(_.num).scanLeft(0)(_+_).init).map {
        case (s, o) => s.sources.map(z => z.copy(range = z.range.offset(o)))
      }.flatten)
    })

  lazy val module = new LazyModuleImp(this) {
    val cat = alertnode.in.map { case (i, e) => i.take(e.source.num) }.flatten
    alertnode.out.foreach { case (o, _) => o := cat }
  }
}

object AlertXbar {
  def apply(implicit p: Parameters): AlertNexusNode = {
    val xbar = LazyModule(new AlertXbar)
    xbar.alertnode
  }
}
