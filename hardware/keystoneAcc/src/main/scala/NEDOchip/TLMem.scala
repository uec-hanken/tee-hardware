// See LICENSE.SiFive for license details.

package uec.rocketchip.subsystem

import Chisel._
import freechips.rocketchip.config.{Field, Parameters}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.util._
import freechips.rocketchip.subsystem._

/* I seriously wonder why there is no port invocation for TL memories? */

/** Adds a port to the system intended to master an TileLink DRAM controller. */
trait CanHaveMasterTLMemPort { this: BaseSubsystem =>
  val module: CanHaveMasterTLMemPortModuleImp
  val memTLNode = p(ExtMem).map { case MemoryPortParams(memPortParams, nMemoryChannels) =>
    val portName = "tl"
    val device = new MemoryDevice

    val memTLNode = TLManagerNode(Seq.tabulate(nMemoryChannels) { channel =>
      val base = AddressSet(memPortParams.base, memPortParams.size - 1)
      val filter = AddressSet(channel * 128, ~((nMemoryChannels - 1) * 128))

      TLManagerPortParameters(
        managers = Seq(TLManagerParameters(
          address = base.intersect(filter).toList,
          resources = device.reg,
          regionType = RegionType.UNCACHED, // cacheable
          executable = true,
          supportsGet = TransferSizes(1, 128),
          supportsPutFull = TransferSizes(1, 128),
          supportsPutPartial = TransferSizes(1, 128)
        )),
        beatBytes = memPortParams.beatBytes
      )
    })

    memTLNode := mbus.toDRAMController(Some(portName))()
    memTLNode
  }
}

/** Actually generates the corresponding IO in the concrete Module */
trait CanHaveMasterTLMemPortModuleImp extends LazyModuleImp {
  val outer: CanHaveMasterTLMemPort
  val mem_tl = outer.memTLNode.map(x => IO(HeterogeneousBag.fromNode(x.in)))
  (mem_tl zip outer.memTLNode) foreach { case (io, node) =>
    (io zip node.in).foreach { case (io, (bundle, _)) => io <> bundle }
  }
}
