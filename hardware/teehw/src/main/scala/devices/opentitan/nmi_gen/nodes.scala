package uec.teehardware.devices.opentitan.nmi_gen

import chisel3._
import chisel3.internal.sourceinfo.SourceInfo
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import uec.teehardware.devices.opentitan._

object EscImp extends NodeImp[EscSourcePortParameters, EscSinkPortParameters, EscEdgeOut, EscEdgeIn, esc_t]
{
  def edgeO(pd: EscSourcePortParameters, pu: EscSinkPortParameters, p: Parameters, sourceInfo: SourceInfo) = new EscEdgeOut(pd, pu, p, sourceInfo)
  def edgeI(pd: EscSourcePortParameters, pu: EscSinkPortParameters, p: Parameters, sourceInfo: SourceInfo) = new EscEdgeIn (pd, pu, p, sourceInfo)

  def bundleO(eo: EscEdgeOut) = new esc_t
  def bundleI(ei: EscEdgeIn)  = new esc_t

  def render(ei: EscEdgeIn) = RenderedEdge(colour = "#000000" /* black */, label = ei.source.sources.size.toString, flipped = true)

  override def mixO(pd: EscSourcePortParameters, node: OutwardNode[EscSourcePortParameters, EscSinkPortParameters, esc_t]): EscSourcePortParameters  =
    pd.copy(sources = pd.sources.map  { s => s.copy (nodePath = node +: s.nodePath) })
  override def mixI(pu: EscSinkPortParameters, node: InwardNode[EscSourcePortParameters, EscSinkPortParameters, esc_t]): EscSinkPortParameters =
    pu.copy(sinks   = pu.sinks.map    { s => s.copy (nodePath = node +: s.nodePath) })
}

trait EscFormatNode extends FormatNode[EscEdgeIn, EscEdgeOut]

case class EscSourceNode(portParams: Seq[EscSourcePortParameters])(implicit valName: ValName) extends SourceNode(EscImp)(portParams) with EscFormatNode
case class EscSinkNode(portParams: Seq[EscSinkPortParameters])(implicit valName: ValName) extends SinkNode(EscImp)(portParams) with EscFormatNode
case class EscAdapterNode
(
  sourceFn: EscSourcePortParameters => EscSourcePortParameters = { s => s },
  sinkFn:   EscSinkPortParameters   => EscSinkPortParameters   = { s => s })(
  implicit valName: ValName)
  extends AdapterNode(EscImp)(sourceFn, sinkFn) with EscFormatNode
case class EscIdentityNode()(implicit valName: ValName) extends IdentityNode(EscImp)() with EscFormatNode
case class EscEphemeralNode()(implicit valName: ValName) extends EphemeralNode(EscImp)() with EscFormatNode

object EscNameNode {
  def apply(name: ValName) = EscIdentityNode()(name)
  def apply(name: Option[String]): EscIdentityNode = apply((ValName(name.getOrElse("with_no_name"))))
  def apply(name: String): EscIdentityNode = apply(Some(name))
}

case class EscNexusNode
(
  sourceFn:       Seq[EscSourcePortParameters] => EscSourcePortParameters,
  sinkFn:         Seq[EscSinkPortParameters]   => EscSinkPortParameters,
  inputRequiresOutput: Boolean = true,
  outputRequiresInput: Boolean = true)(implicit valName: ValName)
  extends NexusNode(EscImp)(sourceFn, sinkFn, inputRequiresOutput, outputRequiresInput) with EscFormatNode
