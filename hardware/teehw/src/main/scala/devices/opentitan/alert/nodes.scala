package uec.teehardware.devices.opentitan.alert

import chisel3._
import chisel3.internal.sourceinfo.SourceInfo
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import uec.teehardware.devices.opentitan._

object AlertImp extends NodeImp[AlertSourcePortParameters, AlertSinkPortParameters, AlertEdgeOut, AlertEdgeIn, Vec[alert_t]]
{
  def edgeO(pd: AlertSourcePortParameters, pu: AlertSinkPortParameters, p: Parameters, sourceInfo: SourceInfo) = new AlertEdgeOut(pd, pu, p, sourceInfo)
  def edgeI(pd: AlertSourcePortParameters, pu: AlertSinkPortParameters, p: Parameters, sourceInfo: SourceInfo) = new AlertEdgeIn (pd, pu, p, sourceInfo)

  def bundleO(eo: AlertEdgeOut) = Vec(eo.source.num, new alert_t)
  def bundleI(ei: AlertEdgeIn)  = Vec(ei.source.num, new alert_t)

  def render(ei: AlertEdgeIn) = RenderedEdge(colour = "#000000" /* black */, label = ei.source.sources.map(_.range.size).sum.toString, flipped = true)

  override def mixO(pd: AlertSourcePortParameters, node: OutwardNode[AlertSourcePortParameters, AlertSinkPortParameters, Vec[alert_t]]): AlertSourcePortParameters  =
    pd.copy(sources = pd.sources.map  { s => s.copy (nodePath = node +: s.nodePath) })
  override def mixI(pu: AlertSinkPortParameters, node: InwardNode[AlertSourcePortParameters, AlertSinkPortParameters, Vec[alert_t]]): AlertSinkPortParameters =
    pu.copy(sinks   = pu.sinks.map    { s => s.copy (nodePath = node +: s.nodePath) })
}

trait AlertFormatNode extends FormatNode[AlertEdgeIn, AlertEdgeOut]

case class AlertSourceNode(portParams: Seq[AlertSourcePortParameters])(implicit valName: ValName) extends SourceNode(AlertImp)(portParams) with AlertFormatNode
case class AlertSinkNode(portParams: Seq[AlertSinkPortParameters])(implicit valName: ValName) extends SinkNode(AlertImp)(portParams) with AlertFormatNode
case class AlertAdapterNode
(
  sourceFn: AlertSourcePortParameters => AlertSourcePortParameters = { s => s },
  sinkFn:   AlertSinkPortParameters   => AlertSinkPortParameters   = { s => s })(
  implicit valName: ValName)
  extends AdapterNode(AlertImp)(sourceFn, sinkFn) with AlertFormatNode
case class AlertIdentityNode()(implicit valName: ValName) extends IdentityNode(AlertImp)() with AlertFormatNode
case class AlertEphemeralNode()(implicit valName: ValName) extends EphemeralNode(AlertImp)() with AlertFormatNode

object AlertNameNode {
  def apply(name: ValName) = AlertIdentityNode()(name)
  def apply(name: Option[String]): AlertIdentityNode = apply((ValName(name.getOrElse("with_no_name"))))
  def apply(name: String): AlertIdentityNode = apply(Some(name))
}

case class AlertNexusNode
(
  sourceFn:       Seq[AlertSourcePortParameters] => AlertSourcePortParameters,
  sinkFn:         Seq[AlertSinkPortParameters]   => AlertSinkPortParameters,
  inputRequiresOutput: Boolean = true,
  outputRequiresInput: Boolean = true)(implicit valName: ValName)
  extends NexusNode(AlertImp)(sourceFn, sinkFn, inputRequiresOutput, outputRequiresInput) with AlertFormatNode
