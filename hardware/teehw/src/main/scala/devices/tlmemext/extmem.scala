package uec.teehardware.devices.tlmemext

import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import uec.teehardware.TEEHWBaseSubsystem
import freechips.rocketchip.subsystem.ExtMem
import freechips.rocketchip.util.HeterogeneousBag

trait HasTEEHWPeripheryExtMem {
  this: TEEHWBaseSubsystem =>

  // Main memory controller (TL memory controller)
  val memctl: Option[TLManagerNode] = p(ExtMem).map { A =>
    val memdevice = new MemoryDevice
    val mainMemParam = TLSlavePortParameters.v1(
      managers = Seq(TLSlaveParameters.v1(
        address = AddressSet.misaligned(A.master.base, A.master.size),
        resources = memdevice.reg,
        regionType = RegionType.UNCACHED, // cacheable
        executable = true,
        supportsGet = TransferSizes(1, mbus.blockBytes),
        supportsPutFull = TransferSizes(1, mbus.blockBytes),
        supportsPutPartial = TransferSizes(1, mbus.blockBytes),
        fifoId = Some(0),
        mayDenyPut = true,
        mayDenyGet = true
      )),
      beatBytes = A.master.beatBytes
    )
    val memTLNode = TLManagerNode(Seq(mainMemParam))
    val buffer = LazyModule(new TLBuffer)
    memTLNode := buffer.node := mbus.toDRAMController(Some("tl"))()
    memTLNode
  }
}

trait HasTEEHWPeripheryExtMemModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryExtMem

  // Main memory controller
  val mem_tl: Option[TLBundle] = outer.memctl.map { case memTLnode: TLManagerNode =>
    val mem_tl = IO(HeterogeneousBag.fromNode(memTLnode.in)).asInstanceOf[Seq[TLBundle]]
    (mem_tl zip memTLnode.in).foreach { case (io, (bundle, _)) => io <> bundle }
    mem_tl.head
  }
}
