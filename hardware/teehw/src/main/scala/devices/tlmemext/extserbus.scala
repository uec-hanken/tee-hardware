package uec.teehardware.devices.tlmemext

import chipsalliance.rocketchip.config.Field
import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.prci.{ClockSinkNode, ClockSinkParameters}
import freechips.rocketchip.subsystem.MasterPortParams
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import uec.teehardware.TEEHWBaseSubsystem
import testchipip.{SerialIO, TLSerdes}

case object ExtSerBus extends Field[Option[MemorySerialPortParams]](None)

trait HasTEEHWPeripheryExtSerBus {
  this: TEEHWBaseSubsystem =>

  val extserctl = p(ExtSerBus).map {A =>
    val device = new SimpleBus("ext_mmio".kebab, Nil)
    val mainMemParam = Seq(TLSlaveParameters.v1(
      address = AddressSet.misaligned(A.master.base, A.master.size),
      resources = device.ranges,
      regionType = RegionType.GET_EFFECTS, // Not cacheable
      executable = true,
      supportsGet = TransferSizes(1, cbus.blockBytes),
      supportsPutFull = TransferSizes(1, cbus.blockBytes),
      supportsPutPartial = TransferSizes(1, cbus.blockBytes),
      fifoId = Some(0),
      mayDenyPut = true,
      mayDenyGet = true))
    println(s"SERDES in sbus added to the system ${cbus.blockBytes}")
    val serdes = LazyModule(new TLSerdes(
      w = A.serWidth,
      params = mainMemParam,
      beatBytes = A.master.beatBytes))
    cbus.coupleTo("ser") {
      serdes.node := TLBuffer() := TLWidthWidget(cbus.beatBytes) := _
    }
    // Request a clock node
    val clkNode = ClockSinkNode(Seq(ClockSinkParameters()))
    clkNode := cbus.fixedClockNode
    InModuleBody {
      clkNode.in.foreach { case (n, _) =>
        serdes.module.clock := n.clock
        serdes.module.reset := n.reset
      }
    }
    // TODO: The clock separation for this is obviously not done
    serdes
  }
}

trait HasTEEHWPeripheryExtSerBusModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryExtSerBus

  // MMIO external serial controller
  val extSerPorts = outer.extserctl.map { A =>
    val ser = IO(new SerialIO(A.module.io.ser.head.w))
    ser <> A.module.io.ser.head
    ser
  }
  val extSourceBits = outer.extserctl.map { A =>
    A.node.in.head._1.params.sourceBits
  }
}
