package uec.teehardware.devices.clockctrl

import chisel3._
import chisel3.experimental.{DoubleParam, IntParam, RawParam, StringParam}
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
import uec.teehardware.EXTBUS

import sys.process._

object ClockCtrlCtrlRegs_0 {
  val drpclkfb = 0x00
  val drpclk0d = 0x04
  val drpclk0p = 0x08
  val drpsen   = 0x0c
  val drpsrdy  = 0x10
  val drpclkdiv = 0x14
  val drpcountdone = 0x18
  val drpcountervalue = 0x1c
}

object ClockCtrlCtrlRegs_1 {
  val drpclkfb = 0x100
  val drpclk0d = 0x104
  val drpclk0p = 0x108
  val drpsen   = 0x10c
  val drpsrdy  = 0x110
  val drpclkdiv = 0x114
  val drpcountdone = 0x118
  val drpcountervalue = 0x11c
}

object ClockCtrlCtrlRegs_2 {
  val drpclkfb = 0x200
  val drpclk0d = 0x204
  val drpclk0p = 0x208
  val drpsen   = 0x20c
  val drpsrdy  = 0x210
  val drpclkdiv = 0x214
  val drpcountdone = 0x218
  val drpcountervalue = 0x21c
}

case class ClockCtrlParams(address: BigInt)

case class OMClockCtrlDevice
(
  memoryRegions: Seq[OMMemoryRegion],
  interrupts: Seq[OMInterrupt],
  _types: Seq[String] = Seq("OMClockCtrlDevice", "OMDevice", "OMComponent")
) extends OMDevice

class ClockCtrlPortIO extends Bundle {
  val clko_0 = Output(Clock())
  val clko_1 = Output(Clock())
  val clko_2 = Output(Clock())
}

class DRPInterrupts extends Bundle {
  val srdy = Bool()
//  val countdone = Bool()
}

class DRPCounter extends Bundle {
  val countdone = Bool()
  val countvalue =  UInt(32.W)
}

class counter_drp extends BlackBox
  with HasBlackBoxResource {
  val io = IO(new Bundle{
    val reset = Input(Bool())
    val ref_clock = Input(Bool())
    val target_clock = Input(Bool())
    val counter_value = Output(UInt(32.W))
    val done = Output(Bool())
  })
  addResource("/clockctrl/counter_drp.v")
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
    // These signals are controlled by user logic interface and are covered
    // in more detail within the XAPP.
    // Also, SADDR is replaced with following 6 signals
    val CLKFBOUT_MULT = Input(UInt(7.W))
    val CLKFBOUT_FRAC = Input(UInt(10.W))
    val CLKOUT0_DIVIDE = Input(UInt(8.W))
    val CLKOUT0_FRAC = Input(UInt(10.W))
    val CLKOUT0_PHASE = Input(UInt(19.W))
    val CLKDIV_DIVIDE = Input(UInt(7.W))
    val SEN = Input(Bool())
    val SCLK = Input(Bool())
    val RST = Input(Bool())
    val SRDY = Output(Bool())
    // These signals are to be connected to the MMCM_ADV by port name.
    // Their use matches the MMCM port description in the Device User Guide.
    val DO = Input(UInt(16.W))
    val DRDY = Input(Bool())
    val LOCK_REG_CLK_IN = Input(Bool())
    val LOCKED_IN = Input(Bool())
    val DWE = Output(Bool())
    val DEN = Output(Bool())
    val DADDR = Output(UInt(7.W))
    val DI = Output(UInt(16.W))
    val DCLK = Output(Bool())
    val RST_MMCM = Output(Bool())
    val LOCKED_OUT = Output(Bool())
  })
  addResource("/clockctrl/mmcme2_drp.v")
}

class MMCME2_ADV extends BlackBox(
  Map(
    "BANDWIDTH" -> StringParam("OPTIMIZED"),
    "DIVCLK_DIVIDE" -> IntParam(2),
    "CLKFBOUT_MULT_F" -> IntParam(60),
    "CLKFBOUT_PHASE" -> IntParam(0),
    "CLKFBOUT_USE_FINE_PS" -> StringParam("FALSE"),
    "CLKIN1_PERIOD" -> IntParam(10),
    "REF_JITTER1" -> DoubleParam(0.01),
    "CLKIN2_PERIOD" -> IntParam(10),
    "REF_JITTER2" -> DoubleParam(0.01),
    "CLKOUT0_DIVIDE_F" -> IntParam(30),
    "CLKOUT0_DUTY_CYCLE" -> DoubleParam(0.5),
    "CLKOUT0_PHASE" -> IntParam(0),
    "CLKOUT0_USE_FINE_PS" -> StringParam("FALSE"),
    "CLKOUT1_DIVIDE" -> IntParam(6),
    "CLKOUT1_DUTY_CYCLE" -> DoubleParam(0.5),
    "CLKOUT1_PHASE" -> IntParam(0),
    "COMPENSATION" -> StringParam("ZHOLD"),
    "STARTUP_WAIT" -> StringParam("FALSE"),
  )
)  {

  val io = IO(new Bundle{
    val CLKFBOUT = Output(Bool())
    val CLKFBOUTB = Output(Bool())
    val CLKFBSTOPPED = Output(Bool())
    val CLKINSTOPPED = Output(Bool())
    val CLKOUT0 = Output(Bool())
    val CLKOUT0B = Output(Bool())
    val CLKOUT1 = Output(Bool())
    val CLKOUT1B = Output(Bool())
    val CLKOUT2 = Output(Bool())
    val CLKOUT2B = Output(Bool())
    val CLKOUT3 = Output(Bool())
    val CLKOUT3B = Output(Bool())
    val CLKOUT4 = Output(Bool())
    val CLKOUT5 = Output(Bool())
    val CLKOUT6 = Output(Bool())
    val DO = Output(UInt(16.W))
    val DRDY = Output(Bool())
    val DADDR = Input(UInt(7.W))
    val DCLK = Input(Bool())
    val DEN = Input(Bool())
    val DI = Input(UInt(16.W))
    val DWE = Input(Bool())
    val LOCKED = Output(Bool())
    val CLKFBIN = Input(Bool())
    val CLKIN1 = Input(Bool())
    val CLKIN2 = Input(Bool())
    val CLKINSEL = Input(Bool())
    val PSDONE = Output(Bool())
    val PSCLK = Input(Bool())
    val PSEN = Input(Bool())
    val PSINCDEC = Input(Bool())
    val PWRDWN = Input(Bool())
    val RST = Input(Bool())
  })
  ElaborationArtefacts.add(
    "MMCME2_ADV.xdc",
    {
      val master = pathName.split("\\.").drop(1).mkString("/")
      s"""create_clock -name MMCME2_ADV_CLKOUT0 -period 10.0 [get_ports {${master}/CLKOUT0}]
         |set_input_jitter MMCME2_ADV_CLKOUT0 0.5
         |
         |""".stripMargin
    }
  )
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

// Registers for ClockCtrl_0
    val drpclkfbReg_0 = RegInit(0.U(17.W))
    val drpclk0dReg_0 = RegInit(0.U(18.W))
    val drpclk0pReg_0 = RegInit(0.U(19.W))
    val drpsenReg_0   = RegInit(false.B)
    val drpsrdyReg_0  = Wire(new DRPInterrupts)
    val drpclkdivReg_0 = RegInit(0.U(7.W))
    val drpcounterReg_0 = Wire(new DRPCounter)

    // Registers for ClockCtrl_1
    val drpclkfbReg_1 = RegInit(0.U(17.W))
    val drpclk0dReg_1 = RegInit(0.U(18.W))
    val drpclk0pReg_1 = RegInit(0.U(19.W))
    val drpsenReg_1   = RegInit(false.B)
    val drpsrdyReg_1  = Wire(new DRPInterrupts)
    val drpclkdivReg_1 = RegInit(0.U(7.W))
    val drpcounterReg_1 = Wire(new DRPCounter)

    // Registers for ClockCtrl_2
    val drpclkfbReg_2 = RegInit(0.U(17.W))
    val drpclk0dReg_2 = RegInit(0.U(18.W))
    val drpclk0pReg_2 = RegInit(0.U(19.W))
    val drpsenReg_2   = RegInit(false.B)
    val drpsrdyReg_2  = Wire(new DRPInterrupts)
    val drpclkdivReg_2 = RegInit(0.U(7.W))
    val drpcounterReg_2 = Wire(new DRPCounter)

    //======== Behaviour ============
    // modules for ClockCtrl_0
    val mmcme2_drp_inst_0 = Module(new mmcme2_drp)
    val counter_drp_inst_0 = Module(new counter_drp)
    val mmcme2_adv_inst_0 = Module(new MMCME2_ADV)
    // wiring MMIO register - mmcme2_drp
    mmcme2_drp_inst_0.io.CLKFBOUT_MULT := drpclkfbReg_0(16,10)
    mmcme2_drp_inst_0.io.CLKFBOUT_FRAC := drpclkfbReg_0(9,0)
    mmcme2_drp_inst_0.io.CLKOUT0_DIVIDE := drpclk0dReg_0(17,10)
    mmcme2_drp_inst_0.io.CLKOUT0_FRAC := drpclk0dReg_0(9,0)
    mmcme2_drp_inst_0.io.CLKOUT0_PHASE := drpclk0pReg_0(18,0)
    mmcme2_drp_inst_0.io.CLKDIV_DIVIDE := drpclkdivReg_0(6,0)
    mmcme2_drp_inst_0.io.SEN := drpsenReg_0
    mmcme2_drp_inst_0.io.RST := reset.asBool()
    //    drpsrdyReg.srdy := mmcme2_drp_inst.SRDY
    mmcme2_drp_inst_0.io.SCLK := clock.asBool()
    mmcme2_drp_inst_0.io.DO := mmcme2_adv_inst_0.io.DO
    mmcme2_drp_inst_0.io.DRDY := mmcme2_adv_inst_0.io.DRDY
    mmcme2_drp_inst_0.io.LOCK_REG_CLK_IN := clock.asBool()
    mmcme2_drp_inst_0.io.LOCKED_IN := mmcme2_adv_inst_0.io.LOCKED
    mmcme2_adv_inst_0.io.DWE := mmcme2_drp_inst_0.io.DWE
    mmcme2_adv_inst_0.io.DEN := mmcme2_drp_inst_0.io.DEN
    mmcme2_adv_inst_0.io.DADDR := mmcme2_drp_inst_0.io.DADDR
    mmcme2_adv_inst_0.io.DI := mmcme2_drp_inst_0.io.DI
    mmcme2_adv_inst_0.io.DCLK := mmcme2_drp_inst_0.io.DCLK
    mmcme2_adv_inst_0.io.RST := mmcme2_drp_inst_0.io.RST_MMCM
    drpsrdyReg_0.srdy := mmcme2_drp_inst_0.io.LOCKED_OUT
    // wiring mmcm_drp and mmcm_adv
    mmcme2_adv_inst_0.io.CLKFBIN := mmcme2_adv_inst_0.io.CLKFBOUT
    counter_drp_inst_0.io.target_clock := mmcme2_adv_inst_0.io.CLKOUT0
    mmcme2_adv_inst_0.io.CLKIN1 := clock.asBool() //TODO: should be a new clock of 800MHz
    mmcme2_adv_inst_0.io.CLKINSEL := true.B
    mmcme2_adv_inst_0.io.PSCLK := false.B
    mmcme2_adv_inst_0.io.PSEN := false.B
    mmcme2_adv_inst_0.io.PSINCDEC := false.B
    mmcme2_adv_inst_0.io.PWRDWN := false.B
    // wiring counter_drp
    counter_drp_inst_0.io.reset := mmcme2_drp_inst_0.io.LOCKED_OUT
    counter_drp_inst_0.io.ref_clock := clock.asBool() //TODO: could be the on-board 200MHz clk
    drpcounterReg_0.countvalue := counter_drp_inst_0.io.counter_value
    drpcounterReg_0.countdone := counter_drp_inst_0.io.done

    // modules for ClockCtrl_1
    val mmcme2_drp_inst_1 = Module(new mmcme2_drp)
    val counter_drp_inst_1 = Module(new counter_drp)
    val mmcme2_adv_inst_1 = Module(new MMCME2_ADV)
    // wiring MMIO register - mmcme2_drp
    mmcme2_drp_inst_1.io.CLKFBOUT_MULT := drpclkfbReg_1(16,10)
    mmcme2_drp_inst_1.io.CLKFBOUT_FRAC := drpclkfbReg_1(9,0)
    mmcme2_drp_inst_1.io.CLKOUT0_DIVIDE := drpclk0dReg_1(17,10)
    mmcme2_drp_inst_1.io.CLKOUT0_FRAC := drpclk0dReg_1(9,0)
    mmcme2_drp_inst_1.io.CLKOUT0_PHASE := drpclk0pReg_1(18,0)
    mmcme2_drp_inst_1.io.CLKDIV_DIVIDE := drpclkdivReg_1(6,0)
    mmcme2_drp_inst_1.io.SEN := drpsenReg_1
    mmcme2_drp_inst_1.io.RST := reset.asBool()
    //    drpsrdyReg.srdy := mmcme2_drp_inst.SRDY
    mmcme2_drp_inst_1.io.SCLK := clock.asBool()
    mmcme2_drp_inst_1.io.DO := mmcme2_adv_inst_1.io.DO
    mmcme2_drp_inst_1.io.DRDY := mmcme2_adv_inst_1.io.DRDY
    mmcme2_drp_inst_1.io.LOCK_REG_CLK_IN := clock.asBool()
    mmcme2_drp_inst_1.io.LOCKED_IN := mmcme2_adv_inst_1.io.LOCKED
    mmcme2_adv_inst_1.io.DWE := mmcme2_drp_inst_1.io.DWE
    mmcme2_adv_inst_1.io.DEN := mmcme2_drp_inst_1.io.DEN
    mmcme2_adv_inst_1.io.DADDR := mmcme2_drp_inst_1.io.DADDR
    mmcme2_adv_inst_1.io.DI := mmcme2_drp_inst_1.io.DI
    mmcme2_adv_inst_1.io.DCLK := mmcme2_drp_inst_1.io.DCLK
    mmcme2_adv_inst_1.io.RST := mmcme2_drp_inst_1.io.RST_MMCM
    drpsrdyReg_1.srdy := mmcme2_drp_inst_1.io.LOCKED_OUT
    // wiring mmcm_drp and mmcm_adv
    mmcme2_adv_inst_1.io.CLKFBIN := mmcme2_adv_inst_1.io.CLKFBOUT
    counter_drp_inst_1.io.target_clock := mmcme2_adv_inst_1.io.CLKOUT0
    mmcme2_adv_inst_1.io.CLKIN1 := clock.asBool() //TODO: should be a new clock of 800MHz
    mmcme2_adv_inst_1.io.CLKINSEL := true.B
    mmcme2_adv_inst_1.io.PSCLK := false.B
    mmcme2_adv_inst_1.io.PSEN := false.B
    mmcme2_adv_inst_1.io.PSINCDEC := false.B
    mmcme2_adv_inst_1.io.PWRDWN := false.B
    // wiring counter_drp
    counter_drp_inst.io.reset := mmcme2_drp_inst.io.LOCKED_OUT
    counter_drp_inst.io.ref_clock := clock.asBool() //TODO: could be the on-board 200MHz clk
    drpcounterReg.countvalue := counter_drp_inst.io.counter_value
    drpcounterReg.countdone := counter_drp_inst.io.done

    // Export the clock to the module
    val iop: ClockCtrlPortIO = port.getWrappedValue.asInstanceOf[ClockCtrlPortIO]
    iop.clko_0 := mmcme2_adv_inst_0.io.CLKOUT0.asClock()
    iop.clko_1 := mmcme2_adv_inst_1.io.CLKOUT0.asClock()
    iop.clko_2 := mmcme2_adv_inst_2.io.CLKOUT0.asClock()

    // Regfields
    regmap(
      // regmap for ClockCtrl_0
      ClockCtrlCtrlRegs_0.drpclkfb -> Seq(RegField(17, drpclkfbReg_0, RegFieldDesc("drpclkfb","DRP_MMCM clkfbout Mult and Frac",reset=Some(0)))),
      ClockCtrlCtrlRegs_0.drpclk0d -> Seq(RegField(18, drpclk0dReg_0, RegFieldDesc("drpclk0d","DRP_MMCM clkout0 Divide and Frac",reset=Some(0)))),
      ClockCtrlCtrlRegs_0.drpclk0p -> Seq(RegField(19, drpclk0pReg_0, RegFieldDesc("drpclk0p","DRP_MMCM clkout0 Phase",reset=Some(0)))),
      ClockCtrlCtrlRegs_0.drpsen   -> Seq(RegField(1, drpsenReg_0, RegFieldDesc("drpsen","DRP_MMCM SEN enable signal",reset=Some(0)))),
      ClockCtrlCtrlRegs_0.drpclkdiv -> Seq(RegField(7, drpclkdivReg_0, RegFieldDesc("drpclkdiv","DRP_MMCM clkin Divide",reset=Some(0)))),
      ClockCtrlCtrlRegs_0.drpsrdy -> RegFieldGroup("drpsrdyReg",Some("Serial interrupt pending"),Seq(
        RegField.r(1, drpsrdyReg_0.srdy,
          RegFieldDesc("drpsrdy","DRP_MMCM SRDY _output clk is ready_ signal", volatile=true)))),
      ClockCtrlCtrlRegs_0.drpcountervalue -> Seq(
        RegField.r(8, drpcounterReg_0.countvalue(31, 24),RegFieldDesc("countvalue4","1st MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg_0.countvalue(23, 16),RegFieldDesc("countvalue3","2nd MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg_0.countvalue(15, 8),RegFieldDesc("countvalue2","3rd MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg_0.countvalue(7, 0),RegFieldDesc("countvalue1","4th MSB count value", volatile=true)),
      ),
      ClockCtrlCtrlRegs_0.drpcountdone -> RegFieldGroup("drpsrdyReg",Some("Serial interrupt pending"),Seq(
        RegField.r(1, drpcounterReg_0.countdone,
          RegFieldDesc("drpcountdone","Clock counter is ready_ signal", volatile=true)))),

      // regmap for ClockCtrl_1
      ClockCtrlCtrlRegs_1.drpclkfb -> Seq(RegField(17, drpclkfbReg_1, RegFieldDesc("drpclkfb","DRP_MMCM clkfbout Mult and Frac",reset=Some(0)))),
      ClockCtrlCtrlRegs_1.drpclk0d -> Seq(RegField(18, drpclk0dReg_1, RegFieldDesc("drpclk0d","DRP_MMCM clkout0 Divide and Frac",reset=Some(0)))),
      ClockCtrlCtrlRegs_1.drpclk0p -> Seq(RegField(19, drpclk0pReg_1, RegFieldDesc("drpclk0p","DRP_MMCM clkout0 Phase",reset=Some(0)))),
      ClockCtrlCtrlRegs_1.drpsen   -> Seq(RegField(1, drpsenReg_1, RegFieldDesc("drpsen","DRP_MMCM SEN enable signal",reset=Some(0)))),
      ClockCtrlCtrlRegs_1.drpclkdiv -> Seq(RegField(7, drpclkdivReg_1, RegFieldDesc("drpclkdiv","DRP_MMCM clkin Divide",reset=Some(0)))),
      ClockCtrlCtrlRegs_1.drpsrdy -> RegFieldGroup("drpsrdyReg",Some("Serial interrupt pending"),Seq(
        RegField.r(1, drpsrdyReg_1.srdy,
          RegFieldDesc("drpsrdy","DRP_MMCM SRDY _output clk is ready_ signal", volatile=true)))),
      ClockCtrlCtrlRegs_1.drpcountervalue -> Seq(
        RegField.r(8, drpcounterReg_1.countvalue(31, 24),RegFieldDesc("countvalue4","1st MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg_1.countvalue(23, 16),RegFieldDesc("countvalue3","2nd MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg_1.countvalue(15, 8),RegFieldDesc("countvalue2","3rd MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg_1.countvalue(7, 0),RegFieldDesc("countvalue1","4th MSB count value", volatile=true)),
      ),
      ClockCtrlCtrlRegs_1.drpcountdone -> RegFieldGroup("drpsrdyReg",Some("Serial interrupt pending"),Seq(
        RegField.r(1, drpcounterReg_1.countdone,
          RegFieldDesc("drpcountdone","Clock counter is ready_ signal", volatile=true)))),

      // regmap for ClockCtrl_2
      ClockCtrlCtrlRegs_2.drpclkfb -> Seq(RegField(17, drpclkfbReg_2, RegFieldDesc("drpclkfb","DRP_MMCM clkfbout Mult and Frac",reset=Some(0)))),
      ClockCtrlCtrlRegs_2.drpclk0d -> Seq(RegField(18, drpclk0dReg_2, RegFieldDesc("drpclk0d","DRP_MMCM clkout0 Divide and Frac",reset=Some(0)))),
      ClockCtrlCtrlRegs_2.drpclk0p -> Seq(RegField(19, drpclk0pReg_2, RegFieldDesc("drpclk0p","DRP_MMCM clkout0 Phase",reset=Some(0)))),
      ClockCtrlCtrlRegs_2.drpsen   -> Seq(RegField(1, drpsenReg_2, RegFieldDesc("drpsen","DRP_MMCM SEN enable signal",reset=Some(0)))),
      ClockCtrlCtrlRegs_2.drpclkdiv -> Seq(RegField(7, drpclkdivReg_2, RegFieldDesc("drpclkdiv","DRP_MMCM clkin Divide",reset=Some(0)))),
      ClockCtrlCtrlRegs_2.drpsrdy -> RegFieldGroup("drpsrdyReg",Some("Serial interrupt pending"),Seq(
        RegField.r(1, drpsrdyReg_2.srdy,
          RegFieldDesc("drpsrdy","DRP_MMCM SRDY _output clk is ready_ signal", volatile=true)))),
      ClockCtrlCtrlRegs_2.drpcountervalue -> Seq(
        RegField.r(8, drpcounterReg_2.countvalue(31, 24),RegFieldDesc("countvalue4","1st MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg_2.countvalue(23, 16),RegFieldDesc("countvalue3","2nd MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg_2.countvalue(15, 8),RegFieldDesc("countvalue2","3rd MSB count value", volatile=true)),
        RegField.r(8, drpcounterReg_2.countvalue(7, 0),RegFieldDesc("countvalue1","4th MSB count value", volatile=true)),
      ),
      ClockCtrlCtrlRegs_2.drpcountdone -> RegFieldGroup("drpsrdyReg",Some("Serial interrupt pending"),Seq(
        RegField.r(1, drpcounterReg_2.countdone,
          RegFieldDesc("drpcountdone","Clock counter is ready_ signal", volatile=true)))),
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
  controlWhere: TLBusWrapperLocation = EXTBUS,
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
