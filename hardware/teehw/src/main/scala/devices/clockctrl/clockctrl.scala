package uec.teehardware.devices.clockctrl

import chisel3._
import chisel3.experimental.{StringParam, IntParam, RawParam}
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
import freechips.rocketchip.subsystem.{Attachable, CBUS, PBUS, TLBusWrapperLocation}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._

import sys.process._

object ClockCtrlCtrlRegs {
  val drpclkfb = 0x30
  val drpclk0d = 0x34
  val drpclk0p = 0x38
  val drpsen   = 0x3c
  val drpsrdy  = 0x40
  val drpclkdiv = 0x44
  val drpcountdone = 0x48
  val drpcountervalue = 0x4c
}

case class ClockCtrlParams(address: BigInt)

case class OMClockCtrlDevice
(
  memoryRegions: Seq[OMMemoryRegion],
  interrupts: Seq[OMInterrupt],
  _types: Seq[String] = Seq("OMClockCtrlDevice", "OMDevice", "OMComponent")
) extends OMDevice

class ClockCtrlPortIO extends Bundle {
}

class DRPInterrupts extends Bundle {
  val srdy = Bool()
  val countdone = Bool()
}

class DRPCounter extends Bundle {
  val countdone = Bool()
  val countvalue =  UInt(32.W)
}

class counter_drp extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle{
    val reset = Input(Bool())
    val ref_clock = Input(Bool())
    val target_clock = Input(Bool())
    val counter_value = Output(UInt(32.W))
    val done = Output(Bool())
  })
  addResource("clockctrl/counter_drp.v")
}

class mmcme2_drp extends BlackBox( // TODO: Not parametrizable from Scala
  Map(
    "REGISTER_LOCKED" -> StringParam("NoReg"),
    "USE_REG_LOCKED" -> StringParam("No"),
    "CLKFBOUT_PHASE" -> IntParam(0),
    "CLKFBOUT_FRAC_EN" -> IntParam(1),
    "BANDWIDTH" -> StringParam("OPTIMIZED"),
    "CLKOUT0_DUTY" -> IntParam(50000),
    "CLKOUT0_FRAC_EN" -> IntParam(1),
    "CLKOUT5_DIVIDE" -> IntParam(1),
    "CLKOUT5_PHASE" -> IntParam(0),
    "CLKOUT5_DUTY" -> IntParam(50000),
    "CLKOUT6_DIVIDE" -> IntParam(1),
    "CLKOUT6_PHASE" -> IntParam(0),
    "CLKOUT6_DUTY" -> IntParam(50000),
  )
)
  with HasBlackBoxResource {
  val io = IO(new Bundle{
    val CLKFBOUT_MULT = Input(UInt(7.W))
    val CLKFBOUT_FRAC = Input(UInt(10.W))
    val CLKOUT0_DIVIDE = Input(UInt(7.W))
    val CLKOUT0_FRAC = Input(UInt(9.W))
    val CLKOUT0_PHASE = Input(UInt(18.W))

    val SRDY = Output(Bool())
  })
  addResource("clockctrl/mmcme2_drp.v")
}

class MMCME2_ADV extends BlackBox  {
  val io = IO(new Bundle{
    val reset = Input(Bool())
    val ref_clock = Input(Bool())
    val target_clock = Input(Bool())
    val counter_value = Output(UInt(32.W))
    val done = Output(Bool())
  })
}

abstract class ClockCtrl(busWidthBytes: Int, val c: ClockCtrlParams, divisorInit: Int = 0)
                        (implicit p: Parameters)
  extends IORegisterRouter(
    RegisterRouterParams(
      name = "clockctrl",
      compat = Seq("uec,clockctrl"),
      base = c.address,
      beatBytes = busWidthBytes),
    new ClockCtrlPortIO
  )
    with HasInterruptSources {

  def nInterrupts = 1

  ResourceBinding {
    Resource(ResourceAnchors.aliases, "clockctrl").bind(ResourceAlias(device.label))
  }

  lazy val module = new LazyModuleImp(this) {
    interrupts(0) := false.B

    //BA 31/10/2019 part1
    // val clkdivselReg = Reg(init = UInt(0,3))
    // val clkdivvalReg = Reg(init = UInt(0,3))
    //end BA 31/10/2019 part 1

    // BA 15/02/2021 #2
    val drpclkfbReg = RegInit(0.U(17.W))
    val drpclk0dReg = RegInit(0.U(18.W))
    val drpclk0pReg = RegInit(0.U(19.W))
    val drpsenReg   = RegInit(false.B)
    val drpsrdyReg  = Wire(new DRPInterrupts)
    val drpclkdivReg = RegInit(0.U(7.W))
    val drpcounterReg = Wire(new DRPCounter)

    // BA 15/02/2021 #2 end
    drpcounterReg.countvalue := Cat(drpclkfbReg(15,0),drpclk0dReg(15,0))

    // Behaviour
    val counter = Module(new counter_drp)
    counter.io.reset := reset.asBool()
    counter.io.ref_clock := false.B // TODO: Not assigned
    counter.io.target_clock := false.B // TODO: Not assigned
    drpcounterReg.countvalue := counter.io.counter_value
    drpcounterReg.countdone := counter.io.done

    val mmcme2 = Module(new mmcme2_drp)

    // Regfields
    regmap(
      //BA 31/10/2019 part 3
      // UARTCtrlRegs.clkdivsel -> Seq(RegField(3, clkdivselReg, RegFieldDesc("clkdivsel","Input of Clock divider select",reset=Some(0)))),
      // UARTCtrlRegs.clkdivval -> Seq(RegField(3, clkdivvalReg, RegFieldDesc("clkdivval","Input of Clock divider value",reset=Some(0)))),
      //end BA 31/10/2019 part 3
      // BA 15/02/2021 #3
      ClockCtrlCtrlRegs.drpclkfb -> Seq(RegField(17, drpclkfbReg, RegFieldDesc("drpclkfb","DRP_MMCM clkfbout Mult and Frac",reset=Some(0)))),
      ClockCtrlCtrlRegs.drpclk0d -> Seq(RegField(18, drpclk0dReg, RegFieldDesc("drpclk0d","DRP_MMCM clkout0 Divide and Frac",reset=Some(0)))),
      ClockCtrlCtrlRegs.drpclk0p -> Seq(RegField(19, drpclk0pReg, RegFieldDesc("drpclk0p","DRP_MMCM clkout0 Phase",reset=Some(0)))),
      ClockCtrlCtrlRegs.drpsen   -> Seq(RegField(1, drpsenReg, RegFieldDesc("drpsen","DRP_MMCM SEN enable signal",reset=Some(0)))),
      ClockCtrlCtrlRegs.drpclkdiv -> Seq(RegField(7, drpclkdivReg, RegFieldDesc("drpclkdiv","DRP_MMCM clkin Divide",reset=Some(0)))),
      ClockCtrlCtrlRegs.drpsrdy -> RegFieldGroup("drpsrdyReg",Some("Serial interrupt pending"),Seq(
        RegField.r(1, drpsrdyReg.srdy,
          RegFieldDesc("drpsrdy","DRP_MMCM SRDY _output clk is ready_ signal", volatile=true)))),

      ClockCtrlCtrlRegs.drpcountervalue -> Seq(
        RegField.r(8, drpcounterReg.countvalue(31, 24),RegFieldDesc("countvalue4","1st MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg.countvalue(23, 16),RegFieldDesc("countvalue3","2nd MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg.countvalue(15, 8),RegFieldDesc("countvalue2","3rd MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg.countvalue(7, 0),RegFieldDesc("countvalue1","4th MSB count value", volatile=true)),
      ),
      ClockCtrlCtrlRegs.drpcountdone -> RegFieldGroup("drpsrdyReg",Some("Serial interrupt pending"),Seq(
        RegField.r(1, drpcounterReg.countdone,
          RegFieldDesc("drpcountdone","Clock counter is ready_ signal", volatile=true)))),
      // BA 15/02/2021 #3 end
    )
  }

  val logicalTreeNode = new LogicalTreeNode(() => Some(device)) {
    def getOMComponents(resourceBindings: ResourceBindings, children: Seq[OMComponent] = Nil): Seq[OMComponent] = {
      val Description(name, mapping) = device.describe(resourceBindings)
      val memRegions = DiplomaticObjectModelAddressing.getOMMemoryRegions(name, resourceBindings, None)
      val interrupts = DiplomaticObjectModelAddressing.describeInterrupts(name, resourceBindings)
      Seq(
        OMClockCtrlDevice(
          memoryRegions = memRegions.map(_.copy(
            name = "clockctrl",
            description = "Clock Controller Device"
          )),
          interrupts = interrupts
        )
      )
    }
  }
}

class TLClockCtrl(busWidthBytes: Int, params: ClockCtrlParams)(implicit p: Parameters)
  extends ClockCtrl(busWidthBytes, params) with HasTLControlRegMap

case class ClockCtrlAttachParams
(
  clockctrlpar: ClockCtrlParams,
  controlWhere: TLBusWrapperLocation = CBUS,
  blockerAddr: Option[BigInt] = None,
  controlXType: ClockCrossingType = NoCrossing,
  intXType: ClockCrossingType = NoCrossing)
(implicit val p: Parameters) {

  def attachTo(where: Attachable)(implicit p: Parameters): TLClockCtrl = {
    val name = s"clockctrl_${ClockCtrl.nextId()}"
    val cbus = where.locateTLBusWrapper(controlWhere)
    val clockctrlClockDomainWrapper = LazyModule(new ClockSinkDomain(take = None))
    val clockctrl = clockctrlClockDomainWrapper { LazyModule(new TLClockCtrl(cbus.beatBytes, clockctrlpar)) }
    clockctrl.suggestName(name)

    cbus.coupleTo(s"device_named_$name") { bus =>

      val blockerOpt = blockerAddr.map { a =>
        val blocker = LazyModule(new TLClockBlocker(BasicBusBlockerParams(a, cbus.beatBytes, cbus.beatBytes)))
        cbus.coupleTo(s"bus_blocker_for_$name") { blocker.controlNode := TLFragmenter(cbus) := _ }
        blocker
      }

      clockctrlClockDomainWrapper.clockNode := (controlXType match {
        case _: SynchronousCrossing =>
          cbus.dtsClk.map(_.bind(clockctrl.device))
          cbus.fixedClockNode
        case _: RationalCrossing =>
          cbus.clockNode
        case _: AsynchronousCrossing =>
          val clockctrlClockGroup = ClockGroup()
          clockctrlClockGroup := where.asyncClockGroupsNode
          blockerOpt.map { _.clockNode := clockctrlClockGroup } .getOrElse { clockctrlClockGroup }
      })

      (clockctrl.controlXing(controlXType)
        := TLFragmenter(cbus)
        := blockerOpt.map { _.node := bus } .getOrElse { bus })
    }

    (intXType match {
      case _: SynchronousCrossing => where.ibus.fromSync
      case _: RationalCrossing => where.ibus.fromRational
      case _: AsynchronousCrossing => where.ibus.fromAsync
    }) := clockctrl.intXing(intXType)

    LogicalModuleTree.add(where.logicalTreeNode, clockctrl.logicalTreeNode)

    clockctrl
  }
}

object ClockCtrl {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }
}
