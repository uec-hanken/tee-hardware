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
import uec.teehardware.devices.opentitan.nmi_gen._
import uec.teehardware.devices.opentitan.top_pkg._

import sys.process._

object alert_reg_pkg {
  def NAlerts = 4
  def N_ESC_SEV = 4
  def N_CLASSES = 4
  def N_LOC_ALERT = 4
  def EscCntDw = 32
  def AccuCntDw = 16
  def cstate_e_bits = 3
}

// A Bundle for representing the crash dump
class alert_crashdump_t extends Bundle {
  val alert_cause = UInt(alert_reg_pkg.NAlerts.W)
  val loc_alert_cause = UInt(alert_reg_pkg.N_LOC_ALERT.W)
  val class_accum_cnt = Vec(alert_reg_pkg.N_CLASSES, UInt(alert_reg_pkg.AccuCntDw.W))
  val class_esc_cnt = Vec(alert_reg_pkg.N_CLASSES, UInt(alert_reg_pkg.EscCntDw.W))
  val class_esc_state = Vec(alert_reg_pkg.N_CLASSES, UInt(alert_reg_pkg.cstate_e_bits.W))
}

case class AlertParams(address: BigInt)

case class OMAlertDevice
(
  memoryRegions: Seq[OMMemoryRegion],
  interrupts: Seq[OMInterrupt],
  _types: Seq[String] = Seq("OMAlertDevice", "OMDevice", "OMComponent")
) extends OMDevice

class AlertPortIO extends Bundle {
  //val esc_rx_i = Vec(alert_reg_pkg.N_ESC_SEV, Input(new esc_rx_t()))
  //val esc_tx_o = Vec(alert_reg_pkg.N_ESC_SEV, Output(new esc_tx_t()))
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
      // TODO: This parameter cannot be entered as RawParam, but there is no way to also
      // TODO: make a vector of Integers as parameters in firrtl. Kinda stuck until they add that
      // WORKAROUND: Just modify the verilog, dummy
      /*"LfsrPerm" -> RawParam(
        "{" + LfsrPerm.map("32'd" + _.toString).reduce((a,b) => a + "," + b) + "}"
      )*/
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
    val crashdump_o = Output(new alert_crashdump_t)

    // Crashdump
    val entropy_i = Input(Bool())

    // Alerts
    // TODO: Screw lowRISC ability to not make everything parameterizable. Module is fixed to NAlerts
    val alert_rx_o = Output(Vec(alert_reg_pkg.NAlerts, new alert_rx_t()))
    val alert_tx_i = Input(Vec(alert_reg_pkg.NAlerts, new alert_tx_t()))

    // Escalates, they are always 4
    val esc_rx_i = Input(Vec(alert_reg_pkg.N_ESC_SEV, new esc_rx_t()))
    val esc_tx_o = Output(Vec(alert_reg_pkg.N_ESC_SEV, new esc_tx_t()))
  })

  // add wrapper/blackbox after it is pre-processed
  addResource("/aaaa_pkgs.preprocessed.sv")
  addResource("/tlul.preprocessed.sv")
  addResource("/prim.preprocessed.sv")
  addResource("/alert.preprocessed.sv")
  addResource("/alert.preprocessed.v")

  // The inline generation

  // The ports for alert
  val alert_port = (for(i <- 0 until alert_reg_pkg.NAlerts) yield
    s"""
       |  output logic                         alert_rx_o_${i}_ping_p,
       |  output logic                         alert_rx_o_${i}_ping_n,
       |  output logic                         alert_rx_o_${i}_ack_p,
       |  output logic                         alert_rx_o_${i}_ack_n,
       |
       |  input  logic                         alert_tx_i_${i}_alert_p,
       |  input  logic                         alert_tx_i_${i}_alert_n,
       |""".stripMargin).reduce(_+_)
  val alert_connections =
    s"""
       |  wire prim_alert_pkg::alert_rx_t[${alert_reg_pkg.NAlerts-1}:0] alert_rx_o;
       |  wire prim_alert_pkg::alert_tx_t[${alert_reg_pkg.NAlerts-1}:0] alert_tx_i;
       |""".stripMargin +
      ( for(i <- 0 until alert_reg_pkg.NAlerts) yield
        s"""
           |  assign alert_rx_o_${i}_ping_p = alert_rx_o[${i}].ping_p;
           |  assign alert_rx_o_${i}_ping_n = alert_rx_o[${i}].ping_n;
           |  assign alert_rx_o_${i}_ack_p = alert_rx_o[${i}].ack_p;
           |  assign alert_rx_o_${i}_ack_n = alert_rx_o[${i}].ack_n;
           |  assign alert_tx_i[${i}].alert_p = alert_tx_i_${i}_alert_p;
           |  assign alert_tx_i[${i}].alert_n = alert_tx_i_${i}_alert_n;
           |""".stripMargin ).reduce(_+_)
  val esc_port = (for(i <- 0 until alert_reg_pkg.N_ESC_SEV) yield
    s"""
       |  output logic                         esc_tx_o_${i}_esc_p,
       |  output logic                         esc_tx_o_${i}_esc_n,
       |
       |  input  logic                         esc_rx_i_${i}_resp_p,
       |  input  logic                         esc_rx_i_${i}_resp_n,
       |""".stripMargin).reduce(_+_)
  val esc_connections =
    s"""
       |  wire prim_esc_pkg::esc_rx_t[${alert_reg_pkg.N_ESC_SEV-1}:0] esc_rx_i;
       |  wire prim_esc_pkg::esc_tx_t[${alert_reg_pkg.N_ESC_SEV-1}:0] esc_tx_o;
       |""".stripMargin +
      ( for(i <- 0 until alert_reg_pkg.NAlerts) yield
        s"""
           |  assign esc_rx_i[${i}].resp_p = esc_rx_i_${i}_resp_p;
           |  assign esc_rx_i[${i}].resp_n = esc_rx_i_${i}_resp_n;
           |  assign esc_tx_o_${i}_esc_p = esc_tx_o[${i}].esc_p;
           |  assign esc_tx_o_${i}_esc_n = esc_tx_o[${i}].esc_n;
           |""".stripMargin ).reduce(_+_)
  val crashdump_port =
    s"""
       |  output logic [${alert_reg_pkg.NAlerts - 1}:0] crashdump_o_alert_cause,
       |  output logic [${alert_reg_pkg.N_LOC_ALERT - 1}:0] crashdump_o_loc_alert_cause,
       |""".stripMargin +
      ( for(i <- 0 until alert_reg_pkg.N_CLASSES) yield
        s"""
           |  output logic [${alert_reg_pkg.AccuCntDw - 1}:0] crashdump_o_class_accum_cnt_${i},
           |  output logic [${alert_reg_pkg.EscCntDw - 1}:0] crashdump_o_class_esc_cnt_${i},
           |  output logic [${alert_reg_pkg.cstate_e_bits - 1}:0] crashdump_o_class_esc_state_${i},
           |""".stripMargin ).reduce(_+_)
  val crashdump_connections =
    s"""
       |  wire alert_pkg::alert_crashdump_t crashdump_o;
       |  assign crashdump_o.alert_cause = crashdump_o_alert_cause;
       |  assign crashdump_o.loc_alert_cause = crashdump_o_loc_alert_cause;
       |""".stripMargin +
      ( for(i <- 0 until alert_reg_pkg.N_ESC_SEV) yield
        s"""
           |  assign crashdump_o.class_accum_cnt [${i}] = crashdump_o_class_accum_cnt_${i};
           |  assign crashdump_o.class_esc_cnt [${i}] = crashdump_o_class_esc_cnt_${i};
           |  assign crashdump_o.class_esc_state [${i}] = crashdump_o_class_esc_state_${i};
           |""".stripMargin ).reduce(_+_)
  setInline("alert_wrapper.sv",
    s"""
       |// Copyright lowRISC contributors.
       |// Licensed under the Apache License, Version 2.0, see LICENSE for details.
       |// SPDX-License-Identifier: Apache-2.0
       |
       |// This blackbox is auto-generated
       |
       |module alert_wrapper
       |#(
       |  parameter logic [31:0]       LfsrSeed = 32'd1
       |) (
       |  // Clock and Reset
       |  input  logic        clk_i,
       |  input  logic        rst_ni,
       |
       |  // Instruction memory interface
       |  input  logic                         tl_a_valid,
       |  output logic                         tl_a_ready,
       |  input  logic                  [2:0]  tl_a_bits_opcode,
       |  input  logic                  [2:0]  tl_a_bits_param,
       |  input  logic  [top_pkg::TL_SZW-1:0]  tl_a_bits_size,
       |  input  logic  [top_pkg::TL_AIW-1:0]  tl_a_bits_source,
       |  input  logic   [top_pkg::TL_AW-1:0]  tl_a_bits_address,
       |  input  logic  [top_pkg::TL_DBW-1:0]  tl_a_bits_mask,
       |  input  logic   [top_pkg::TL_DW-1:0]  tl_a_bits_data,
       |  input  logic                  [6:0]  tl_a_bits_user_tl_a_user_t_rsvd1,
       |  input  logic                         tl_a_bits_user_tl_a_user_t_parity_en,
       |  input  logic                  [7:0]  tl_a_bits_user_tl_a_user_t_parity,
       |  input  logic                         tl_a_bits_corrupt,
       |
       |  output logic                         tl_d_valid,
       |  input  logic                         tl_d_ready,
       |  output logic                  [2:0]  tl_d_bits_opcode,
       |  output logic                  [2:0]  tl_d_bits_param,
       |  output logic  [top_pkg::TL_SZW-1:0]  tl_d_bits_size,   // Bouncing back a_size
       |  output logic  [top_pkg::TL_AIW-1:0]  tl_d_bits_source,
       |  output logic  [top_pkg::TL_DIW-1:0]  tl_d_bits_sink,
       |  output logic   [top_pkg::TL_DW-1:0]  tl_d_bits_data,
       |  output logic  [top_pkg::TL_DUW-1:0]  tl_d_bits_user_uint,
       |  output logic                         tl_d_bits_corrupt,
       |  output logic                         tl_d_bits_denied,
       |
       |  // Alerts
       |""".stripMargin +
      alert_port + crashdump_port + esc_port +
      """
       |
       |  input  logic                         entropy_i,
       |
       |  // Interrupts
       |  output logic                         intr_classa_o,
       |  output logic                         intr_classb_o,
       |  output logic                         intr_classc_o,
       |  output logic                         intr_classd_o
       |);
       |
       |  // tl connections
       |
       |  wire tlul_pkg::tl_h2d_t tl_i;
       |  wire tlul_pkg::tl_d2h_t tl_o;
       |
       |  assign tl_i.a_valid = tl_a_valid;
       |  assign tl_i.a_opcode = tl_a_bits_opcode;
       |  assign tl_i.a_param = tl_a_bits_param;
       |  assign tl_i.a_size = tl_a_bits_size;
       |  assign tl_i.a_source = tl_a_bits_source;
       |  assign tl_i.a_address = tl_a_bits_address;
       |  assign tl_i.a_mask = tl_a_bits_mask;
       |  assign tl_i.a_data = tl_a_bits_data;
       |  assign tl_i.a_user.rsvd1 = tl_a_bits_user_tl_a_user_t_rsvd1;
       |  assign tl_i.a_user.parity_en = tl_a_bits_user_tl_a_user_t_parity_en;
       |  assign tl_i.a_user.parity = tl_a_bits_user_tl_a_user_t_parity;
       |
       |  assign tl_i.d_ready = tl_d_ready;
       |
       |  // tl_a_bits_corrupt; // ignored
       |
       |  assign tl_d_valid = tl_o.d_valid;
       |  assign tl_d_bits_opcode = tl_o.d_opcode;
       |  assign tl_d_bits_param = tl_o.d_param;
       |  assign tl_d_bits_size = tl_o.d_size;
       |  assign tl_d_bits_source = tl_o.d_source;
       |  assign tl_d_bits_sink = tl_o.d_sink;
       |  assign tl_d_bits_data = tl_o.d_data;
       |  assign tl_d_bits_user_uint = tl_o.d_user;
       |  assign tl_d_bits_corrupt = tl_o.d_error; // Seems legit
       |  assign tl_d_bits_denied = 1'b0; // Seems also legit
       |
       |  assign tl_a_ready = tl_o.a_ready;

       |""".stripMargin +
      alert_connections + crashdump_connections + esc_connections +
       """
       |  alert_handler #(
       |    .LfsrSeed                ( LfsrSeed             )
       |  ) u_aes (
       |    // clock and reset
       |    .clk_i                (clk_i),
       |    .rst_ni               (rst_ni),
       |    // TL-UL buses
       |    .tl_o                 (tl_o),
       |    .tl_i                 (tl_i),
       |    // Interrupts
       |    .intr_classa_o        (intr_classa_o),
       |    .intr_classb_o        (intr_classb_o),
       |    .intr_classc_o        (intr_classc_o),
       |    .intr_classd_o        (intr_classd_o),
       |    .entropy_i            (entropy_i),
       |    // Crashdump
       |    .crashdump_o          (crashdump_o),
       |    // Alert
       |    .alert_rx_o           (alert_rx_o),
       |    .alert_tx_i           (alert_tx_i),
       |    // Esc
       |    .esc_rx_i             (esc_rx_i),
       |    .esc_tx_o             (esc_tx_o)
       |  );
       |
       |endmodule
       |""".stripMargin)
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
  lazy val flatSources = (sources zip sources.map(_.num).scanLeft(0)(_ + _).init).flatMap {
    case (s, o) => s.sources.map(z => z.copy(range = z.range.offset(o)))
  }

  // Create the escalaments
  val escnode = EscSourceNode(EscSourcePortSimple(ports = alert_reg_pkg.N_ESC_SEV, resources = Seq(Resource(device, "esc"))))

  // Negotiated sizes (Note: nAlertNodes and nAlert are not the same)
  def nAlert: Int = alertnode.edges.in.map(_.source.num).sum
  def isAlert: Boolean = nAlert != 0

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
    val (escs, _) = escnode.out.unzip // Expose the interrupt signals

    // Actual alerts here
    val alerts = alertnode.in.flatMap { case (i, e) => i.take(e.source.num) }

    // If alerts are not created, there is no point to create the alert manager
    if(!isAlert) {
      println(s"Alert handler is not instanced (No alerts generated)")
      interrupts.foreach(_ := false.B)
      escs.foreach{ case esc =>
        esc.esc_tx.esc_n := true.B
        esc.esc_tx.esc_p := false.B
      }
      alerts.foreach{ case alert =>
        alert.alert_rx.ping_n := true.B
        alert.alert_rx.ping_p := false.B
        alert.alert_rx.ack_n := true.B
        alert.alert_rx.ack_p := false.B
      }
    } else {
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
          blackbox.io.alert_tx_i(i).alert_n := alerts(i).alert_tx.alert_n
          blackbox.io.alert_tx_i(i).alert_p := alerts(i).alert_tx.alert_p
          alerts(i).alert_rx.ping_n := blackbox.io.alert_rx_o(i).ping_n
          alerts(i).alert_rx.ping_p := blackbox.io.alert_rx_o(i).ping_p
          alerts(i).alert_rx.ack_n := blackbox.io.alert_rx_o(i).ack_n
          alerts(i).alert_rx.ack_p := blackbox.io.alert_rx_o(i).ack_p
        }
        else {
          blackbox.io.alert_tx_i(i).alert_n := true.B
          blackbox.io.alert_tx_i(i).alert_p := false.B
        }
      }

      // Connect the escalates to the node
      (escs zip blackbox.io.esc_rx_i).foreach { case (a: esc_t, b: esc_rx_t) => b := a.esc_rx }
      (escs zip blackbox.io.esc_tx_o).foreach { case (a: esc_t, b: esc_tx_t) => a.esc_tx := b }

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

      // Crashdump
      // TODO: blackbox.io.crashdump_o is not used

      // Entrophy (TODO)
      blackbox.io.entropy_i := false.B
    }


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
  escNode: EscInwardNode,
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
    escNode :=* per.escnode

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
