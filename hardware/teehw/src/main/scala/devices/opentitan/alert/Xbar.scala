package uec.teehardware.devices.opentitan.alert

import chisel3._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._

class AlertXbar()(implicit p: Parameters) extends LazyModule
{
  val alertnode = AlertNexusNode(
    sinkFn         = { _ => AlertSinkPortParameters(Seq(AlertSinkParameters())) },
    sourceFn       = { seq =>
      AlertSourcePortParameters((seq zip seq.map(_.num).scanLeft(0)(_ + _).init).flatMap {
        case (s, o) => s.sources.map(z => z.copy(range = z.range.offset(o)))
      })
    })

  lazy val module = new LazyModuleImp(this) {
    val cat = alertnode.in.flatMap { case (i, e) => i.take(e.source.num) }
    alertnode.out.foreach { case (o, _) =>
      (o zip cat).foreach{ case (i, j) =>
          i.alert_tx := j.alert_tx
          j.alert_rx := i.alert_rx
      }
    }
  }
}

object AlertXbar {
  def apply(implicit p: Parameters): AlertNexusNode = {
    val xbar = LazyModule(new AlertXbar)
    xbar.alertnode
  }
}
