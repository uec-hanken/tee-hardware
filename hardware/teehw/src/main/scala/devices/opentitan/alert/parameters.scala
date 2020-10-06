package uec.teehardware.devices.opentitan.alert

import chisel3._
import chisel3.internal.sourceinfo.SourceInfo
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._

// Note: Is almost the same as interrupts/Parameters.scala

// A potentially empty half-open range; [start, end)
case class AlertRange(start: Int, end: Int)
{
  require (start >= 0)
  require (start <= end)
  def size = end - start
  def overlaps(x: AlertRange) = start < x.end && x.start < end
  def offset(x: Int) = AlertRange(x+start, x+end)
}

object AlertRange
{
  implicit def apply(end: Int): AlertRange = apply(0, end)
}

case class AlertSourceParameters
(
  range:     AlertRange,
  resources: Seq[Resource] = Seq(),
  nodePath:  Seq[BaseNode] = Seq())
{
  val name = nodePath.lastOption.map(_.lazyModule.name).getOrElse("disconnected")
}

case class AlertSinkParameters(nodePath:  Seq[BaseNode] = Seq())
{
  val name = nodePath.lastOption.map(_.lazyModule.name).getOrElse("disconnected")
}

case class AlertSourcePortParameters(sources: Seq[AlertSourceParameters])
{
  val num = sources.map(_.range.size).sum
  // The interrupts mapping must not overlap
  sources.map(_.range).combinations(2).foreach { case Seq(a, b) => require (!a.overlaps(b)) }
  // The interrupts must perfectly cover the range
  require (sources.isEmpty || sources.map(_.range.end).max == num)
}
object AlertSourcePortSimple
{
  def apply(num: Int = 1, ports: Int = 1, sources: Int = 1, resources: Seq[Resource] = Nil) =
    if (num == 0) Nil else
      Seq.fill(ports)(AlertSourcePortParameters(
        Seq.tabulate(sources)(idx => AlertSourceParameters(range = AlertRange(idx*num, idx*num+num), resources = resources))))
}

case class AlertSinkPortParameters(sinks: Seq[AlertSinkParameters])
object AlertSinkPortSimple
{
  def apply(ports: Int = 1, sinks: Int = 1) =
    Seq.fill(ports)(AlertSinkPortParameters(Seq.fill(sinks)(AlertSinkParameters())))
}

case class AlertEdge(source: AlertSourcePortParameters, sink: AlertSinkPortParameters, params: Parameters, sourceInfo: SourceInfo)
  extends FormatEdge{
  // TODO: Maybe we can improve these?
  def formatEdge = "Alert node\n"
}
class AlertEdgeIn(source: AlertSourcePortParameters, sink: AlertSinkPortParameters, params: Parameters, sourceInfo: SourceInfo)
  extends AlertEdge(source, sink, params, sourceInfo)
class AlertEdgeOut(source: AlertSourcePortParameters, sink: AlertSinkPortParameters, params: Parameters, sourceInfo: SourceInfo)
  extends AlertEdge(source, sink, params, sourceInfo)
