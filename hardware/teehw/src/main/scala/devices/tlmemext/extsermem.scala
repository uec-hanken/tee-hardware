package uec.teehardware.devices.tlmemext

import chipsalliance.rocketchip.config.Field
import chisel3._
import chisel3.experimental.{IO, attach}
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.prci.{ClockGroup, ClockSinkDomain, ClockSinkNode, ClockSinkParameters}
import freechips.rocketchip.subsystem.MasterPortParams
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.ResetCatchAndSync
import uec.teehardware.{GenericIOLibraryParams, HasDigitalizable, TEEHWBaseSubsystem}
import testchipip.{ClockedIO, SerialAdapter, SerialIO, TLSerdes}

case class MemorySerialPortParams(master: MasterPortParams, nMemoryChannels: Int, serWidth: Int)
case object ExtSerMem extends Field[Option[MemorySerialPortParams]](None)
case object ExtSerMemDirect extends Field[Boolean](false)
case object MbusToExtSerMemXTypeKey extends Field[ClockCrossingType](SynchronousCrossing())

trait HasTEEHWPeripheryExtSerMem {
  this: TEEHWBaseSubsystem =>

  // Main memory serialized controller (TL serial memory controller)
  val (memserctl, memser_io) = p(ExtSerMem).map {A =>
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
    val serdesXType = p(MbusToExtSerMemXTypeKey)
    val serdesDomainWrapper = LazyModule(new ClockSinkDomain(take = None))
    val serdes = serdesDomainWrapper {
      LazyModule(new TLSerdes(
        w = A.serWidth,
        params = mainMemParam,
        beatBytes = A.master.beatBytes))
    }
    serdesDomainWrapper.clockNode := (serdesXType match {
      case _: SynchronousCrossing =>
        mbus.fixedClockNode
      case _: RationalCrossing =>
        mbus.clockNode
      case _: AsynchronousCrossing =>
        val serdesClockGroup = ClockGroup()(p, ValName("extsermem_clock"))
        serdesClockGroup := asyncClockGroupsNode
        serdesClockGroup
    })
    (serdesDomainWrapper.crossIn(serdes.node)(ValName("extsermem_serCross")))(serdesXType) :=
      TLBuffer() := TLSourceShrinker(1 << A.master.idBits) := mbus.toDRAMController(Some("ser"))()
    val inner_io = serdesDomainWrapper { InModuleBody {
      val inner_io = IO(new SerialIO(serdes.module.io.ser.head.w)).suggestName("memser_inner_io")
      inner_io <> serdes.module.io.ser.head
      inner_io
    } }
    val outer_io = InModuleBody {
      val outer_io = IO(new SerialIO(serdes.module.io.ser.head.w)).suggestName("memser_outer_io")
      outer_io <> inner_io
      outer_io
    }
    (Some(serdes), Some(outer_io))
  }.getOrElse(None, None)
}

trait HasTEEHWPeripheryExtSerMemModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryExtSerMem

  // Main memory serial controller
  val memSerPorts = outer.memser_io.map(_.getWrappedValue)
  val serSourceBits = outer.memserctl.map { A =>
    println(s"Publishing Serial Memory with ${A.node.in.head._1.params.sourceBits} source bits")
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
    out_valid.suggestName("pad_out_valid")
    attach(out_valid.pad, memser.out.valid)
    out_valid.ConnectAsOutput(sysextser.out.valid)
    val out_ready: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.input()
    out_ready.suggestName("pad_out_ready")
    attach(out_ready.pad, memser.out.ready)
    sysextser.out.ready := out_ready.ConnectAsInput()
    (sysextser.out.bits.asBools zip memser.out.bits).zipWithIndex.foreach{ case((a, b), i) =>
      val pad: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.output()
      pad.suggestName(s"pad_out_bits_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }

    val in_valid: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.input()
    in_valid.suggestName("pad_in_valid")
    attach(in_valid.pad, memser.in.valid)
    sysextser.in.valid := in_valid.ConnectAsInput()
    val in_ready: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.output()
    in_ready.suggestName("pad_in_ready")
    attach(in_ready.pad, memser.in.ready)
    in_ready.ConnectAsOutput(sysextser.in.ready)
    sysextser.in.bits := VecInit(memser.in.bits.zipWithIndex.map{ case(b, i) =>
      val pad: HasDigitalizable = if(p(ExtSerMemDirect)) IOGen.analog() else IOGen.input()
      pad.suggestName(s"pad_in_bits_${i}")
      attach(pad.pad, b)
      pad.ConnectAsInput()
    }).asUInt

    memser
  }
}
