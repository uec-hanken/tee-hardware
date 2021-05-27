// See LICENSE.SiFive for license details.
// Basically, this is just a PeripheryBus

package uec.teehardware

import freechips.rocketchip.config.{Parameters}
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import freechips.rocketchip.subsystem._

case class CryptoBusParams
(
  beatBytes: Int,
  blockBytes: Int,
  atomics: Option[BusAtomics] = Some(BusAtomics()),
  dtsFrequency: Option[BigInt] = None,
  zeroDevice: Option[BuiltInZeroDeviceParams] = None,
  errorDevice: Option[BuiltInErrorDeviceParams] = None,
  replication: Option[ReplicatedRegion] = None)
  extends HasTLBusParams
    with HasBuiltInDeviceParams
    with HasRegionReplicatorParams
    with TLBusWrapperInstantiationLike
{
  def instantiate(context: HasTileLinkLocations, loc: Location[TLBusWrapper])(implicit p: Parameters): CryptoBus = {
    val crbus = LazyModule(new CryptoBus(this, loc.name))
    crbus.suggestName(loc.name)
    context.tlBusWrapperLocationMap += (loc -> crbus)
    crbus
  }
}

class CryptoBus(params: CryptoBusParams, name: String)(implicit p: Parameters)
  extends TLBusWrapper(params, name)
{
  private val replicator = params.replication.map(r => LazyModule(new RegionReplicator(r)))
  val prefixNode = replicator.map { r =>
    r.prefix := addressPrefixNexusNode
    addressPrefixNexusNode
  }

  private val fixer = LazyModule(new TLFIFOFixer(TLFIFOFixer.all))
  private val node: TLNode = params.atomics.map { pa =>
    val in_xbar = LazyModule(new TLXbar)
    val out_xbar = LazyModule(new TLXbar)
    val fixer_node = replicator.map(fixer.node :*= _.node).getOrElse(fixer.node)
    (out_xbar.node
      :*= fixer_node
      :*= TLBuffer(pa.buffer)
      :*= (pa.widenBytes.filter(_ > beatBytes).map { w =>
      TLWidthWidget(w) :*= TLAtomicAutomata(arithmetic = pa.arithmetic)
    } .getOrElse { TLAtomicAutomata(arithmetic = pa.arithmetic) })
      :*= in_xbar.node)
  } .getOrElse { TLXbar() :*= fixer.node }

  def inwardNode: TLInwardNode = node
  def outwardNode: TLOutwardNode = node
  def busView: TLEdge = fixer.node.edges.in.head

  val builtInDevices: BuiltInDevices = BuiltInDevices.attach(params, outwardNode)
}
