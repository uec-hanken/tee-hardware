package uec.teehardware.devices.tlmemext

import chipsalliance.rocketchip.config.Field
import chisel3._
import chisel3.experimental.{Analog, attach}
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.prci.{ClockSinkNode, ClockSinkParameters}
import freechips.rocketchip.subsystem.MasterPortParams
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import uec.teehardware.{GET, GenericIOLibraryParams, HasDigitalizable, PUT, TEEHWBaseSubsystem}
import testchipip.{SerialIO, TLSerdes}

case object ExtSerBus extends Field[Option[MemorySerialPortParams]](None)
case object ExtSerBusDirect extends Field[Boolean](false)

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
    PUT(bundle.out.valid, out.valid)
    bundle.out.bits := VecInit(out.bits.map{a => GET(a)}).asUInt
    PUT(bundle.in.valid, in.valid)
    (bundle.in.bits.asBools zip in.bits).foreach{ case (a, b) => PUT(a, b)}
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
