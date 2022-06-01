package uec.teehardware.devices.tlmemext

import chipsalliance.rocketchip.config.Field
import chisel3._
import chisel3.experimental.attach
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.prci.{ClockSinkNode, ClockSinkParameters}
import freechips.rocketchip.subsystem.MasterPortParams
import freechips.rocketchip.tilelink._
import uec.teehardware.{GenericIOLibraryParams, TEEHWBaseSubsystem, HasDigitalizable}
import testchipip.{SerialIO, TLSerdes}

case class MemorySerialPortParams(master: MasterPortParams, nMemoryChannels: Int, serWidth: Int)
case object ExtSerMem extends Field[Option[MemorySerialPortParams]](None)
case object ExtSerMemDirect extends Field[Boolean](false)

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

trait HasTEEHWPeripheryExtSerMemChipImp extends RawModule {
  implicit val p: Parameters
  val clock: Clock
  val reset: Bool
  val IOGen: GenericIOLibraryParams
  val system: HasTEEHWPeripheryExtSerMemModuleImp

  val memser = system.memSerPorts.map{sysextser =>
    val memser = IO(new SerialIOChip(sysextser.w))

    val out_valid: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.output()
    out_valid.suggestName("out_valid")
    attach(out_valid.pad, memser.out.valid)
    out_valid.ConnectAsOutput(sysextser.out.valid)
    val out_ready: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.input()
    out_ready.suggestName("a_ready")
    attach(out_ready.pad, memser.out.ready)
    sysextser.out.ready := out_ready.ConnectAsInput()
    (sysextser.out.bits.asBools zip memser.out.bits).zipWithIndex.foreach{ case((a, b), i) =>
      val pad: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.output()
      pad.suggestName(s"out_bits_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }

    val in_valid: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.input()
    in_valid.suggestName("in_valid")
    attach(in_valid.pad, memser.in.valid)
    sysextser.in.valid := in_valid.ConnectAsInput()
    val in_ready: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.output()
    in_ready.suggestName("in_ready")
    attach(in_ready.pad, memser.in.ready)
    in_ready.ConnectAsOutput(sysextser.in.ready)
    sysextser.in.bits := VecInit(memser.in.bits.zipWithIndex.map{ case(b, i) =>
      val pad: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.input()
      pad.suggestName(s"in_bits_${i}")
      attach(pad.pad, b)
      pad.ConnectAsInput()
    }).asUInt

    memser
  }
}
