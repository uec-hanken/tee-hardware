package uec.teehardware.devices.tlmemext

import chipsalliance.rocketchip.config.Field
import chisel3._
import chisel3.experimental.{Analog, IO, attach}
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.prci.{ClockGroup, ClockSinkDomain, ClockSinkNode, ClockSinkParameters}
import freechips.rocketchip.subsystem.MasterPortParams
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import uec.teehardware.{GET, GenericIOLibraryParams, HasDigitalizable, PUT, TEEHWBaseSubsystem}
import testchipip.{SerialIO, TLSerdes}

case object ExtSerBus extends Field[Option[MemorySerialPortParams]](None)
case object ExtSerBusDirect extends Field[Boolean](false)
case object MbusToExtSerBusXTypeKey extends Field[ClockCrossingType](SynchronousCrossing())

trait HasTEEHWPeripheryExtSerBus {
  this: TEEHWBaseSubsystem =>

  val (extserctl, extser_io) = p(ExtSerBus).map {A =>
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
    val serdesXType = p(MbusToExtSerBusXTypeKey)
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
        val serdesClockGroup = ClockGroup()(p, ValName("extserbus_clock"))
        serdesClockGroup := asyncClockGroupsNode
        serdesClockGroup
    })

    cbus.coupleTo("serbus") {
      (serdesDomainWrapper.crossIn(serdes.node)(ValName("extserbus_serCross")))(serdesXType) :=
        TLBuffer() := TLSourceShrinker(1 << A.master.idBits) := TLWidthWidget(cbus.beatBytes) := _
    }
    val inner_io = serdesDomainWrapper { InModuleBody {
      val inner_io = IO(new SerialIO(serdes.module.io.ser.head.w)).suggestName("extser_inner_io")
      inner_io <> serdes.module.io.ser.head
      inner_io
    } }
    val outer_io = InModuleBody {
      val outer_io = IO(new SerialIO(serdes.module.io.ser.head.w)).suggestName("extser_outer_io")
      outer_io <> inner_io
      outer_io
    }
    (Some(serdes), Some(outer_io))
  }.getOrElse(None, None)
}

trait HasTEEHWPeripheryExtSerBusModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryExtSerBus

  // MMIO external serial controller
  val extSerPorts = outer.extser_io.map(_.getWrappedValue)
  val extSourceBits = outer.extserctl.map { A =>
    A.node.in.head._1.params.sourceBits
  }
}

class SerialIOModChip(val w: Int) extends Bundle {
  val valid = Analog(1.W)
  val ready = Analog(1.W)
  val bits = Vec(w, Analog(1.W))
}

class SerialIOChip(val w: Int) extends Bundle {
  val in = new SerialIOModChip(w)
  val out = new SerialIOModChip(w)
  def ConnectIn(bundle: SerialIO): Unit = {
    bundle.out.valid := GET(out.valid)
    PUT(bundle.out.ready, out.ready)
    bundle.out.bits := VecInit(out.bits.map{a => GET(a)}).asUInt
    PUT(bundle.in.valid, in.valid)
    bundle.in.ready := GET(out.ready)
    (bundle.in.bits.asBools zip in.bits).foreach{ case (a, b) => PUT(a, b)}
  }
  def flipConnect(other: SerialIO) {
    val bundle = Wire(new SerialIO(other.w))
    ConnectIn(bundle)
    other.flipConnect(bundle)
  }
}

trait HasTEEHWPeripheryExtSerBusChipImp extends RawModule {
  implicit val p: Parameters
  val clock: Clock
  val reset: Bool
  val IOGen: GenericIOLibraryParams
  val system: HasTEEHWPeripheryExtSerBusModuleImp

  val extser = system.extSerPorts.map{sysextser =>
    val extser = IO(new SerialIOChip(sysextser.w))

    val out_valid: HasDigitalizable = if(p(ExtSerBusDirect)) IOGen.analog() else IOGen.output()
    out_valid.suggestName("out_valid")
    attach(out_valid.pad, extser.out.valid)
    out_valid.ConnectAsOutput(sysextser.out.valid)
    val out_ready: HasDigitalizable = if(p(ExtSerBusDirect)) IOGen.analog() else IOGen.input()
    out_ready.suggestName("a_ready")
    attach(out_ready.pad, extser.out.ready)
    sysextser.out.ready := out_ready.ConnectAsInput()
    (sysextser.out.bits.asBools zip extser.out.bits).zipWithIndex.foreach{ case((a, b), i) =>
      val pad: HasDigitalizable = if(p(ExtSerBusDirect)) IOGen.analog() else IOGen.output()
      pad.suggestName(s"out_bits_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }

    val in_valid: HasDigitalizable = if(p(ExtSerBusDirect)) IOGen.analog() else IOGen.input()
    in_valid.suggestName("in_valid")
    attach(in_valid.pad, extser.in.valid)
    sysextser.in.valid := in_valid.ConnectAsInput()
    val in_ready: HasDigitalizable = if(p(ExtSerBusDirect)) IOGen.analog() else IOGen.output()
    in_ready.suggestName("in_ready")
    attach(in_ready.pad, extser.in.ready)
    in_ready.ConnectAsOutput(sysextser.in.ready)
    sysextser.in.bits := VecInit(extser.in.bits.zipWithIndex.map{ case(b, i) =>
      val pad: HasDigitalizable = if(p(ExtSerBusDirect)) IOGen.analog() else IOGen.input()
      pad.suggestName(s"in_bits_${i}")
      attach(pad.pad, b)
      pad.ConnectAsInput()
    }).asUInt

    extser
  }
}
