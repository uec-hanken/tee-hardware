package uec.teehardware.devices.tlram

import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._


case class TLRAMParams(address: BigInt, depth: Int = 2048, beatBytes: Int = 4)

// A TL RAM. Extracted from the TLTestRAM, but is synthesizable (maybe...)
class TLRAM(c: TLRAMParams)(implicit p: Parameters) extends LazyModule
{
  val device = new MemoryDevice

  val node = TLManagerNode(Seq(TLSlavePortParameters.v1(
    Seq(TLSlaveParameters.v1(
      address            = AddressSet.misaligned(c.address, c.depth*c.beatBytes),
      resources          = device.reg,
      regionType         = RegionType.UNCACHED,
      executable         = true,
      supportsGet        = TransferSizes(1, c.beatBytes),
      supportsPutPartial = TransferSizes(1, c.beatBytes),
      supportsPutFull    = TransferSizes(1, c.beatBytes),
      fifoId             = Some(0))), // requests are handled in order
    beatBytes  = c.beatBytes)))

  lazy val module = new LazyModuleImp(this) {
    val (in, edge) = node.in(0)

    val memAddress = edge.addr_hi(in.a.bits.address - c.address.U)(log2Ceil(c.depth)-1, 0)
    val mem = SyncReadMem(c.depth, Vec(c.beatBytes, UInt(8.W)))

    val d_full = RegInit(false.B)
    val d_size = Reg(UInt())
    val d_source = Reg(UInt())
    val d_data = Wire(UInt())
    d_data := mem.readAndHold(memAddress, in.a.fire())

    // Flow control
    when (in.d.fire()) { d_full := false.B }
    when (in.a.fire()) { d_full :=true.B  }
    in.d.valid := d_full
    in.a.ready := in.d.ready || !d_full

    when (in.a.fire()) {
      d_size   := in.a.bits.size
      d_source := in.a.bits.source
    }

    val a_hasData = edge.hasData(in.a.bits)
    val d_hasData = RegEnable(a_hasData, in.a.fire())
    val wdata = VecInit(Seq.tabulate(c.beatBytes) { i => in.a.bits.data(8*(i+1)-1, 8*i) })

    in.d.bits := edge.AccessAck(d_source, d_size, d_data)
    in.d.bits.opcode := Mux(d_hasData, TLMessages.AccessAck, TLMessages.AccessAckData)
    when (in.a.fire() && d_hasData) {
      mem.write(memAddress, wdata, in.a.bits.mask.asBools)
    }

    // Tie off unused channels
    in.b.valid := false.B
    in.c.ready := true.B
    in.e.ready := true.B
  }
}