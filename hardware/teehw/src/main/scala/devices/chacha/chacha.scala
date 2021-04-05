package uec.teehardware.devices.chacha

import chisel3._
import chisel3.util._
import chisel3.util.random._
import chisel3.util.HasBlackBoxResource
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.tilelink.{BasicBusBlockerParams, TLClockBlocker}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.diplomaticobjectmodel._
import freechips.rocketchip.diplomaticobjectmodel.logicaltree._
import freechips.rocketchip.diplomaticobjectmodel.model._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.prci.{ClockGroup, ClockSinkDomain}
import freechips.rocketchip.regmapper._
import freechips.rocketchip.subsystem.{Attachable, PBUS, TLBusWrapperLocation}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sys.process._

case class ChachaParams
(
  address: BigInt,
  impl: Int = 0,
  nbits: Int = 14
) {
  require(nbits == 14, "TODO: This Chacha does not support nbits different than 14")
}

case class OMCHACHADevice(
                           memoryRegions: Seq[OMMemoryRegion],
                           interrupts: Seq[OMInterrupt],
                           _types: Seq[String] = Seq("OMCHACHADevice", "OMDevice", "OMComponent")
                         ) extends OMDevice

class ChachaPortIO extends Bundle {
}

abstract class Chacha(busWidthBytes: Int, val c: ChachaParams)
                     (implicit p: Parameters)
  extends IORegisterRouter(
    RegisterRouterParams(
      name = "chacha",
      compat = Seq("uec,chacha"),
      base = c.address,
      beatBytes = busWidthBytes),
    new ChachaPortIO
  )
    with HasInterruptSources {
  def nInterrupts = 4

  // The device in the dts is created here
  ResourceBinding {
    Resource(ResourceAnchors.aliases, "chacha").bind(ResourceAlias(device.label))
  }

  lazy val module = new LazyModuleImp(this) {
    // Interrupt export
    interrupts(3) := false.B
    interrupts(2) := false.B
    interrupts(1) := false.B
    interrupts(0) := false.B

    // Registers
    val dummy = RegInit(false.B)

    // Tcha register mapping
    val tcha_map = Seq(
      ChachaRegs.dummy -> Seq(
        RegField(1, dummy, RegFieldDesc("cha_src_sel", "Tcha loop select", reset = Some(0)))
      )
    )
    regmap(
      (tcha_map):_*
    )

  }



  val logicalTreeNode = new LogicalTreeNode(() => Some(device)) {
    def getOMComponents(resourceBindings: ResourceBindings, children: Seq[OMComponent] = Nil): Seq[OMComponent] = {
      val Description(name, mapping) = device.describe(resourceBindings)
      val memRegions = DiplomaticObjectModelAddressing.getOMMemoryRegions(name, resourceBindings, None)
      val interrupts = DiplomaticObjectModelAddressing.describeInterrupts(name, resourceBindings)
      Seq(
        OMCHACHADevice(
          memoryRegions = memRegions.map(_.copy(
            name = "chacha",
            description = "CHACHA Push-Register Device"
          )),
          interrupts = interrupts
        )
      )
    }
  }
}

class TLCHACHA(busWidthBytes: Int, params: ChachaParams)(implicit p: Parameters)
  extends Chacha(busWidthBytes, params) with HasTLControlRegMap

case class ChachaAttachParams
(
  chachapar: ChachaParams,
  controlWhere: TLBusWrapperLocation = PBUS,
  blockerAddr: Option[BigInt] = None,
  controlXType: ClockCrossingType = NoCrossing,
  intXType: ClockCrossingType = NoCrossing)(implicit val p: Parameters) {

  def ChachaGen(cbus: TLBusWrapper)(implicit valName: ValName): Chacha with HasTLControlRegMap = {
    LazyModule(new TLCHACHA(cbus.beatBytes, chachapar))
  }

  def attachTo(where: Attachable)(implicit p: Parameters): Chacha with HasTLControlRegMap = {
    val name = s"chacha_${CHACHA.nextId()}"
    val cbus = where.locateTLBusWrapper(controlWhere)
    val chachaClockDomainWrapper = LazyModule(new ClockSinkDomain(take = None))
    val chacha = chachaClockDomainWrapper { ChachaGen(cbus) }
    chacha.suggestName(name)

    cbus.coupleTo(s"device_named_$name") { bus =>

      val blockerOpt = blockerAddr.map { a =>
        val blocker = LazyModule(new TLClockBlocker(BasicBusBlockerParams(a, cbus.beatBytes, cbus.beatBytes)))
        cbus.coupleTo(s"bus_blocker_for_$name") { blocker.controlNode := TLFragmenter(cbus) := _ }
        blocker
      }

      chachaClockDomainWrapper.clockNode := (controlXType match {
        case _: SynchronousCrossing =>
          cbus.dtsClk.map(_.bind(chacha.device))
          cbus.fixedClockNode
        case _: RationalCrossing =>
          cbus.clockNode
        case _: AsynchronousCrossing =>
          val chachaClockGroup = ClockGroup()
          chachaClockGroup := where.asyncClockGroupsNode
          blockerOpt.map { _.clockNode := chachaClockGroup } .getOrElse { chachaClockGroup }
      })

      (chacha.controlXing(controlXType)
        := TLFragmenter(cbus)
        := blockerOpt.map { _.node := bus } .getOrElse { bus })
    }

    (intXType match {
      case _: SynchronousCrossing => where.ibus.fromSync
      case _: RationalCrossing => where.ibus.fromRational
      case _: AsynchronousCrossing => where.ibus.fromAsync
    }) := chacha.intXing(intXType)

    LogicalModuleTree.add(where.logicalTreeNode, chacha.logicalTreeNode)

    chacha
  }
}

object CHACHA {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }
}




