package uec.teehardware.devices.opentitan.alert

import chisel3._
import chisel3.experimental._
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
import uec.teehardware.devices.opentitan._
import uec.teehardware.devices.opentitan.top_pkg._
import sys.process._

object alert_reg_pkg {
  def NAlerts = 4
  def N_ESC_SEV = 4
}

case class AlertParams(address: BigInt)

case class OMAlertDevice
(
  memoryRegions: Seq[OMMemoryRegion],
  interrupts: Seq[OMInterrupt],
  _types: Seq[String] = Seq("OMAlertDevice", "OMDevice", "OMComponent")
) extends OMDevice

class AlertPortIO extends Bundle {
  val esc_rx_i = Vec(alert_reg_pkg.N_ESC_SEV, Input(new esc_rx_t()))
  val esc_tx_o = Vec(alert_reg_pkg.N_ESC_SEV, Output(new esc_tx_t()))
}

class alert_wrapper
(
  NumAlerts: Int,
  LfsrSeed: Int = 1,
  LfsrPerm: Seq[Int] = Seq(
     4, 11, 25,  3, 15, 16,  1, 10,
     2, 22,  7,  0, 23, 28, 30, 19,
    27, 12, 24, 26, 14, 21, 18,  5,
    13,  8, 29, 31, 20,  6,  9, 17)
)
  extends BlackBox(
    Map(
      "LfsrSeed" -> IntParam(LfsrSeed), // TODO: Maybe this is okay?
      "LfsrPerm" -> RawParam(
        "{" + LfsrPerm.map("32'd" + _.toString).reduce((a,b) => a + "," + b) + "}"
      )
    )
  )
    with HasBlackBoxResource  with HasBlackBoxInline {

  val io = IO(new Bundle {
    // Clock and Reset
    val clk_i = Input(Clock())
    val rst_ni = Input(Bool())

    // Bus interface
    val tl = Flipped(new TLBundle(OpenTitanTLparams))

    // Interrupts
    val intr_classa_o = Output(Bool())
    val intr_classb_o = Output(Bool())
    val intr_classc_o = Output(Bool())
    val intr_classd_o = Output(Bool())

    // Entropy
    val crashdump_o = Output(Bool())

    // Crashdump
    val entropy_i = Input(Bool())

    // Alerts
    // TODO: Screw lowRISC ability to not make everything parameterizable. Module is fixed to NAlerts
    val alert_rx_i = Vec(alert_reg_pkg.NAlerts, Output(new alert_rx_t()))
    val alert_tx_o = Vec(alert_reg_pkg.NAlerts, Input(new alert_tx_t()))

    // Escalates, they are always 4
    val esc_rx_i = Vec(alert_reg_pkg.N_ESC_SEV, Input(new esc_rx_t()))
    val esc_tx_o = Vec(alert_reg_pkg.N_ESC_SEV, Output(new esc_tx_t()))
  })

  // pre-process the verilog to remove "includes" and combine into one file
  val make = "make -C hardware/teehw/src/main/resources alert"
  val proc = make
  require (proc.! == 0, "Failed to run preprocessing step")

  // add wrapper/blackbox after it is pre-processed
  addResource("/alert.preprocessed.sv")
  addResource("/alert.preprocessed.v")

  // TODO: Do the inline
  setInline("alert_wrapper.v",
    s"""
       |module alert_wrapper(
       |);
       |endmodule
    """.stripMargin)
}

class Alert(blockBytes: Int, beatBytes: Int, params: AlertParams)(implicit p: Parameters) extends LazyModule {

  // Create a simple device for this peripheral
  val device = new SimpleDevice("Alert", Seq("lowRISC,alert-0.5")) {
    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      Description(name, mapping ++ extraResources(resources))
    }
  }

  val ioNode = BundleBridgeSource(() => (new AlertPortIO).cloneType)
  val port = InModuleBody { ioNode.bundle }

  // Allow this device to extend the DTS mapping
  def extraResources(resources: ResourceBindings) = Map[String, Seq[ResourceValue]]()

  // Create our interrupt node
  val intnode = IntSourceNode(IntSourcePortSimple(num = 4, resources = Seq(Resource(device, "int"))))

  // Then also create the nexus node for the alert
  // TODO: We use nexus here, but the alerts does not "Input alert" and "generate alerts"
  // TODO: We only require all the alerts, and plain connect them to the original OpenTitan alert module.
  val alertnode: AlertNexusNode = AlertNexusNode(
    sinkFn   = { _ => AlertSinkPortParameters(Seq(AlertSinkParameters())) },
    sourceFn = { _ => AlertSourcePortParameters(Seq()) }, // TODO: So, no generation of alerts.
    outputRequiresInput = false,
    inputRequiresOutput = false)
  lazy val sources = alertnode.edges.in.map(_.source)
  lazy val flatSources = (sources zip sources.map(_.num).scanLeft(0)(_+_).init).map {
    case (s, o) => s.sources.map(z => z.copy(range = z.range.offset(o)))
  }.flatten

  // Negotiated sizes (Note: nAlertNodes and nAlert are not the same)
  def nAlert: Int = alertnode.edges.in.map(_.source.num).sum

  val peripheralParam = TLSlavePortParameters.v1(
    managers = Seq(TLManagerParameters(
      address = AddressSet.misaligned(params.address,  0x1000),
      resources = device.reg,
      regionType = RegionType.GET_EFFECTS, // NOT cacheable
      executable = false,
      supportsGet = TransferSizes(1, 4),
      supportsPutFull = TransferSizes(1, 4),
      supportsPutPartial = TransferSizes(1, 4),
      fifoId             = Some(0)
    )),
    beatBytes = 4 // 32-bit stuff
  )
  val peripheralNode = TLManagerNode(Seq(peripheralParam))

  // Conversion nodes
  //val axifrag = LazyModule(new AXI4Fragmenter())
  val tlwidth = LazyModule(new TLWidthWidget(beatBytes))
  val tlfrag = LazyModule(new TLFragmenter(4, blockBytes))
  val node = TLBuffer()

  peripheralNode :=
    tlwidth.node :=
    tlfrag.node :=
    node

  lazy val module = new LazyModuleImp(this) {

    val io = port
    val (interrupts, _) = intnode.out(0) // Expose the interrupt signals

    // Actual alerts here
    val (alerts, _) = alertnode.in(0)

    println(s"Alert map (${nAlert} alerts):")
    flatSources.foreach { s =>
      // +1 because 0 is reserved, +1-1 because the range is half-open
      println(s"  [${s.range.start+1}, ${s.range.end}] => ${s.name}")
    }
    println("")

    // Instance the Alert black box
    val blackbox = Module(new alert_wrapper(nAlert))

    // Obtain the TL bundle
    val (tl_in, tl_edge) = peripheralNode.in(0) // Extract the port from the node

    // Clocks and resets
    blackbox.io.clk_i := clock
    blackbox.io.rst_ni := !reset.asBool

    // Connect the alerts
    // TODO: Because lowRISC do not like to use parameters (unless is convenient),
    // TODO: we need to only support up to 4 alerts, because that is the way
    // TODO: we can do it without modifying the original OT code
    require(nAlert <= alert_reg_pkg.NAlerts, "Tell lowRISC to support more alerts")
    for(i <- 0 until alert_reg_pkg.NAlerts) {
      if(i < nAlert) {
        blackbox.io.alert_tx_o(i).alert_n := alerts(i).alert_tx.alert_n
        blackbox.io.alert_tx_o(i).alert_p := alerts(i).alert_tx.alert_p
        alerts(i).alert_rx.ping_n := blackbox.io.alert_rx_i(i).ping_n
        alerts(i).alert_rx.ping_p := blackbox.io.alert_rx_i(i).ping_p
        alerts(i).alert_rx.ack_n := blackbox.io.alert_rx_i(i).ack_n
        alerts(i).alert_rx.ack_p := blackbox.io.alert_rx_i(i).ack_p
      }
      else {
        blackbox.io.alert_tx_o(i).alert_n := true.B
        blackbox.io.alert_tx_o(i).alert_p := false.B
      }
    }

    // The escalates are connected separatelly
    (port.esc_rx_i zip blackbox.io.esc_rx_i).foreach { case (a, b) => b := a }
    (port.esc_tx_o zip blackbox.io.esc_tx_o).foreach { case (a, b) => a := b }

    // Connect the interrupts
    interrupts(0) := blackbox.io.intr_classa_o
    interrupts(1) := blackbox.io.intr_classb_o
    interrupts(2) := blackbox.io.intr_classc_o
    interrupts(3) := blackbox.io.intr_classd_o

    // Connect the TL bundle
    blackbox.io.tl.a <> tl_in.a
    tl_in.d <> blackbox.io.tl.d

    // Tie off unused channels
    tl_in.b.valid := false.B
    tl_in.c.ready := true.B
    tl_in.e.ready := true.B
  }

  val logicalTreeNode = new LogicalTreeNode(() => Some(device)) {
    def getOMComponents(resourceBindings: ResourceBindings, children: Seq[OMComponent] = Nil): Seq[OMComponent] = {
      val Description(name, mapping) = device.describe(resourceBindings)
      val memRegions = DiplomaticObjectModelAddressing.getOMMemoryRegions(name, resourceBindings, None)
      val interrupts = DiplomaticObjectModelAddressing.describeInterrupts(name, resourceBindings)
      Seq(
        OMAlertDevice(
          memoryRegions = memRegions.map(_.copy(
            name = "Alert",
            description = "OpenTitan Alert-SHA256 HWIP"
          )),
          interrupts = interrupts
        )
      )
    }
  }
}

case class AlertAttachParams
(
  par: AlertParams,
  alertNode: AlertOutwardNode,
  controlWhere: TLBusWrapperLocation = PBUS,
  blockerAddr: Option[BigInt] = None,
  controlXType: ClockCrossingType = NoCrossing,
  intXType: ClockCrossingType = NoCrossing)
(implicit val p: Parameters) {

  def attachTo(where: Attachable)(implicit p: Parameters): Alert = {
    val name = s"Alert_${Alert.nextId()}"
    val cbus = where.locateTLBusWrapper(controlWhere)
    val clockDomainWrapper = LazyModule(new ClockSinkDomain(take = None))

    // The Alert lazymodule.
    val per = clockDomainWrapper { LazyModule(new Alert(cbus.blockBytes, cbus.beatBytes, par)) }
    per.suggestName(name)

    cbus.coupleTo(s"device_named_$name") { bus =>

      val blockerOpt = blockerAddr.map { a =>
        val blocker = LazyModule(new TLClockBlocker(BasicBusBlockerParams(a, cbus.beatBytes, cbus.beatBytes)))
        cbus.coupleTo(s"bus_blocker_for_$name") { blocker.controlNode := TLFragmenter(cbus) := _ }
        blocker
      }

      clockDomainWrapper.clockNode := (controlXType match {
        case _: SynchronousCrossing =>
          cbus.dtsClk.foreach(_.bind(per.device))
          cbus.fixedClockNode
        case _: RationalCrossing =>
          cbus.clockNode
        case _: AsynchronousCrossing =>
          val clockGroup = ClockGroup()
          clockGroup := where.asyncClockGroupsNode
          blockerOpt.map { _.clockNode := clockGroup } .getOrElse { clockGroup }
      })

      (per.node
        := blockerOpt.map { _.node := bus } .getOrElse { bus })
    }

    (intXType match {
      case _: SynchronousCrossing => where.ibus.fromSync
      case _: RationalCrossing => where.ibus.fromRational
      case _: AsynchronousCrossing => where.ibus.fromAsync
    }) := per.intnode

    per.alertnode :=* alertNode

    LogicalModuleTree.add(where.logicalTreeNode, per.logicalTreeNode)

    per
  }
}

object Alert {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }
}