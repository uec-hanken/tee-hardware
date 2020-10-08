package uec.teehardware.devices.opentitan.nmi_gen

import chisel3._
import chisel3.internal.sourceinfo.SourceInfo
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._

// Note: Is almost the same as interrupts/Parameters.scala
// But unlike Alerts, we do not need a Vector of escalaments
// because we only need 1 pure scalament per edge created
// In this fashion, we can handle the nodes independently

case class EscSourceParameters
(
  resources: Seq[Resource] = Seq(),
  nodePath:  Seq[BaseNode] = Seq())
{
  val name = nodePath.lastOption.map(_.lazyModule.name).getOrElse("disconnected")
}

case class EscSinkParameters(nodePath:  Seq[BaseNode] = Seq())
{
  val name = nodePath.lastOption.map(_.lazyModule.name).getOrElse("disconnected")
}

case class EscSourcePortParameters(sources: Seq[EscSourceParameters])

object EscSourcePortSimple
{
  def apply(ports: Int = 1, sources: Int = 1, resources: Seq[Resource] = Nil) =
   Seq.fill(ports)(EscSourcePortParameters(
     Seq.tabulate(sources)(idx => EscSourceParameters(resources = resources))))
}

case class EscSinkPortParameters(sinks: Seq[EscSinkParameters])
object EscSinkPortSimple
{
  def apply(ports: Int = 1, sinks: Int = 1) =
    Seq.fill(ports)(EscSinkPortParameters(Seq.fill(sinks)(EscSinkParameters())))
}

case class EscEdge(source: EscSourcePortParameters, sink: EscSinkPortParameters, params: Parameters, sourceInfo: SourceInfo)
  extends FormatEdge{
  // TODO: Maybe we can improve these?
  def formatEdge = "Esc node\n"
}
class EscEdgeIn(source: EscSourcePortParameters, sink: EscSinkPortParameters, params: Parameters, sourceInfo: SourceInfo)
  extends EscEdge(source, sink, params, sourceInfo)
class EscEdgeOut(source: EscSourcePortParameters, sink: EscSinkPortParameters, params: Parameters, sourceInfo: SourceInfo)
  extends EscEdge(source, sink, params, sourceInfo)
