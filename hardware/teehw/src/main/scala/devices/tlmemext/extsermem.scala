package uec.teehardware.devices.tlmemext

import chipsalliance.rocketchip.config.Field
import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.prci.{ClockSinkNode, ClockSinkParameters}
import freechips.rocketchip.subsystem.MasterPortParams
import freechips.rocketchip.tilelink._
import uec.teehardware.{TEEHWBaseSubsystem}
import testchipip.{SerialIO, TLSerdes}



case class MemorySerialPortParams(master: MasterPortParams, nMemoryChannels: Int, serWidth: Int)
case object ExtSerMem extends Field[Option[MemorySerialPortParams]](None)

trait HasTEEHWPeripheryExtSerMem {
  this: TEEHWBaseSubsystem =>

  // Main memory serialized controller (TL serial memory controller)
  val memserctl = p(ExtSerMem).map {A =>
    val memdevice = new MemoryDevice
    val mainMemParam = Seq(TLSlaveParameters.v1(
      address = AddressSet.misaligned(A.master.base, A.master.size),
      resources = memdevice.reg,
      regionType = RegionType.UNCACHED, // cacheable
      executable = true,
      supportsGet = TransferSizes(1, mbus.blockBytes),
      supportsPutFull = TransferSizes(1, mbus.blockBytes),
      supportsPutPartial = TransferSizes(1, mbus.blockBytes),
      fifoId = Some(0),
      mayDenyPut = true,
      mayDenyGet = true))
    println(s"SERDES in mbus added to the system ${mbus.blockBytes}")
    val serdes = LazyModule(new TLSerdes(
      w = A.serWidth,
      params = mainMemParam,
      beatBytes = A.master.beatBytes))
    serdes.node := TLBuffer() := mbus.toDRAMController(Some("ser"))()
    // Request a clock node
    val clkNode = ClockSinkNode(Seq(ClockSinkParameters()))
    clkNode := mbus.fixedClockNode
    InModuleBody {
      clkNode.in.foreach { case (n, _) =>
        serdes.module.clock := n.clock
        serdes.module.reset := n.reset
      }
    }
    // The clock separation for memser is done through the aclocks
    serdes
  }
}

trait HasTEEHWPeripheryExtSerMemModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryExtSerMem

  // Main memory serial controller
  val memSerPorts = outer.memserctl.map { A =>
    val ser = IO(new SerialIO(A.module.io.ser.head.w))
    ser <> A.module.io.ser.head
    ser
  }
  val serSourceBits = outer.memserctl.map { A =>
    A.node.in.head._1.params.sourceBits
  }
}
