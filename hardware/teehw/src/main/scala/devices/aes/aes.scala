package uec.teehardware.devices.aes

import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.tilelink.{BasicBusBlockerParams, TLClockBlocker}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.diplomaticobjectmodel.DiplomaticObjectModelAddressing
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.{LogicalModuleTree, LogicalTreeNode}
import freechips.rocketchip.diplomaticobjectmodel.model.{OMComponent, OMDevice, OMInterrupt, OMMemoryRegion}
import freechips.rocketchip.interrupts._
import freechips.rocketchip.prci.{ClockGroup, ClockSinkDomain}
import freechips.rocketchip.regmapper._
import freechips.rocketchip.subsystem.{Attachable, PBUS, TLBusWrapperLocation}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sys.process._

case class AESParams(address: BigInt)

case class OMAESDevice(
  memoryRegions: Seq[OMMemoryRegion],
  interrupts: Seq[OMInterrupt],
  _types: Seq[String] = Seq("OMAESDevice", "OMDevice", "OMComponent")
) extends OMDevice

class AESPortIO extends Bundle {
}

class aes_core extends BlackBox with HasBlackBoxResource {
  override def desiredName = "aes_core_TOP_wrapper"
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val reset_n = Input(Bool())

    val encdec = Input(Bool())
    val init = Input(Bool())
    val next = Input(Bool())
    val ready = Output(Bool())

    val key = Input(UInt(256.W))
    val keylen = Input(Bool())

    val block = Input(UInt(128.W))
    val result = Output(UInt(128.W))
    val result_valid = Output(Bool())
  })

  // add wrapper/blackbox after it is pre-processed
  addResource("/aes.preprocessed.v")
}

abstract class AES(busWidthBytes: Int, val c: AESParams, divisorInit: Int = 0)
                   (implicit p: Parameters)
  extends IORegisterRouter(
    RegisterRouterParams(
      name = "aes",
      compat = Seq("uec,aes3-0"),
      base = c.address,
      beatBytes = busWidthBytes),
    new AESPortIO
  )
    with HasInterruptSources {

  def nInterrupts = 1

  ResourceBinding {
    Resource(ResourceAnchors.aliases, "aes").bind(ResourceAlias(device.label))
  }

  lazy val module = new LazyModuleImp(this) {
    interrupts(0) := false.B

    // Data
    val block = Reg(Vec(4, UInt(32.W)))
    val key = Reg(Vec(8, UInt(32.W)))
    val result = Wire(UInt(128.W))

    // Configurations
    val encdec = RegInit(false.B)
    val keylen = RegInit(false.B)

    // Triggers
    val init = WireInit(false.B)
    val next = WireInit(false.B)
    val ready = Wire(Bool())

    // Core instantiation
    val aes = Module(new aes_core)
    aes.io.clk := clock
    aes.io.reset_n := !reset.asBool()
    aes.io.block := (for(i <- 0 until 16) yield block.asUInt()((1+i)*8-1, (0+i)*8)).reduce(Cat(_,_))
    aes.io.key := (for(i <- 0 until 32) yield key.asUInt()((1+i)*8-1, (0+i)*8)).reduce(Cat(_,_))
    result := aes.io.result
    aes.io.encdec := encdec
    aes.io.keylen := keylen
    aes.io.init := init
    aes.io.next := next
    ready := aes.io.ready
    // aes.io.result_valid ignored

    // Regfields
    val block_regmap: Seq[RegField] = block.map{ i => RegField(32, i) }
    val key_regmap: Seq[RegField] = key.map{ i => RegField(32, i) }
    val result_regmap: Seq[RegField] = for(i <- 0 until 16) yield RegField.r(8, result((16-i)*8-1, (15-i)*8))
    val config_regmap: Seq[RegField] = Seq(
      RegField(1, encdec, RegFieldDesc("encdec", "Encode / Decode", reset = Some(0))),
      RegField(1, keylen, RegFieldDesc("keylen", "Key length", reset = Some(0)))
    )
    val reg_and_status = Seq(
      RegField(1, init, RegFieldDesc("init", "Key Expansion Enable", reset = Some(0))),
      RegField(1, next, RegFieldDesc("next", "Data Enable", reset = Some(0))),
      RegField.r(1, ready, RegFieldDesc("ready", "Ready", volatile = true))
    )

    regmap(
      AESCtrlRegs.key -> key_regmap,
      AESCtrlRegs.block -> block_regmap,
      AESCtrlRegs.result -> result_regmap,
      AESCtrlRegs.config -> config_regmap,
      AESCtrlRegs.regstatus -> reg_and_status
    )
  }

  val logicalTreeNode = new LogicalTreeNode(() => Some(device)) {
    def getOMComponents(resourceBindings: ResourceBindings, children: Seq[OMComponent] = Nil): Seq[OMComponent] = {
      val Description(name, mapping) = device.describe(resourceBindings)
      val memRegions = DiplomaticObjectModelAddressing.getOMMemoryRegions(name, resourceBindings, None)
      val interrupts = DiplomaticObjectModelAddressing.describeInterrupts(name, resourceBindings)
      Seq(
        OMAESDevice(
          memoryRegions = memRegions.map(_.copy(
            name = "aes",
            description = "AES Push-Register Device"
          )),
          interrupts = interrupts
        )
      )
    }
  }

}

class TLAES(busWidthBytes: Int, params: AESParams)(implicit p: Parameters)
  extends AES(busWidthBytes, params) with HasTLControlRegMap

case class AESAttachParams(
  aespar: AESParams,
  controlWhere: TLBusWrapperLocation = uec.teehardware.CRYPTOBUS,
  blockerAddr: Option[BigInt] = None,
  controlXType: ClockCrossingType = SynchronousCrossing(),
  intXType: ClockCrossingType = NoCrossing)
                          (implicit val p: Parameters) {

  def attachTo(where: Attachable)(implicit p: Parameters): TLAES = {
    val name = s"aes_${AES.nextId()}"
    val cbus = where.locateTLBusWrapper(controlWhere)
    val aesClockDomainWrapper = LazyModule(new ClockSinkDomain(take = None))
    val aes = aesClockDomainWrapper { LazyModule(new TLAES(cbus.beatBytes, aespar)) }
    aes.suggestName(name)

    cbus.coupleTo(s"device_named_$name") { bus =>

      val blockerOpt = blockerAddr.map { a =>
        val blocker = LazyModule(new TLClockBlocker(BasicBusBlockerParams(a, cbus.beatBytes, cbus.beatBytes)))
        cbus.coupleTo(s"bus_blocker_for_$name") { blocker.controlNode := TLFragmenter(cbus) := _ }
        blocker
      }

      aesClockDomainWrapper.clockNode := (controlXType match {
        case _: SynchronousCrossing =>
          cbus.dtsClk.map(_.bind(aes.device))
          cbus.fixedClockNode
        case _: RationalCrossing =>
          cbus.clockNode
        case _: AsynchronousCrossing =>
          val aesClockGroup = ClockGroup()
          aesClockGroup := where.asyncClockGroupsNode
          blockerOpt.map { _.clockNode := aesClockGroup } .getOrElse { aesClockGroup }
      })

      (aes.controlXing(controlXType)
        := TLFragmenter(cbus)
        := blockerOpt.map { _.node := bus } .getOrElse { bus })
    }

    (intXType match {
      case _: SynchronousCrossing => where.ibus.fromSync
      case _: RationalCrossing => where.ibus.fromRational
      case _: AsynchronousCrossing => where.ibus.fromAsync
    }) := aes.intXing(intXType)

    LogicalModuleTree.add(where.logicalTreeNode, aes.logicalTreeNode)

    aes
  }
}

object AES {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }
}
