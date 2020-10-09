package uec.teehardware.devices.opentitan.nmi_gen

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

object nmi_gen_reg_pkg {
  def N_ESC_SEV = 3
}

case class NmiGenParams(address: BigInt)

case class OMNmiGenDevice
(
  memoryRegions: Seq[OMMemoryRegion],
  interrupts: Seq[OMInterrupt],
  _types: Seq[String] = Seq("OMNmiGenDevice", "OMDevice", "OMComponent")
) extends OMDevice

class NmiGenPortIO extends Bundle {
}

class nmi_gen_wrapper
  extends BlackBox
    with HasBlackBoxResource  with HasBlackBoxInline {

  val io = IO(new Bundle {
    // Clock and Reset
    val clk_i = Input(Clock())
    val rst_ni = Input(Bool())

    // Bus interface
    val tl = Flipped(new TLBundle(OpenTitanTLparams))

    // Interrupts
    val intr_esc0_o = Output(Bool())
    val intr_esc1_o = Output(Bool())
    val intr_esc2_o = Output(Bool())

    // Reset requests
    val nmi_rst_req_o = Output(Bool())

    // Escs
    // This particular only supports 3 escalaments
    val esc_rx_o = Output(Vec(nmi_gen_reg_pkg.N_ESC_SEV, new esc_rx_t()))
    val esc_tx_i = Input(Vec(nmi_gen_reg_pkg.N_ESC_SEV, new esc_tx_t()))
  })

  // pre-process the verilog to remove "includes" and combine into one file
  val make = "make -C hardware/teehw/src/main/resources nmi_gen"
  val make_pkgs = "make -C hardware/teehw/src/main/resources pkgs"
  val make_tlul = "make -C hardware/teehw/src/main/resources tlul"
  val make_prim = "make -C hardware/teehw/src/main/resources prim"
  require (make.! == 0, "Failed to run preprocessing step")
  require (make_pkgs.! == 0, "Failed to run preprocessing step")
  require (make_tlul.! == 0, "Failed to run preprocessing step")
  require (make_prim.! == 0, "Failed to run preprocessing step")

  // add wrapper/blackbox after it is pre-processed
  addResource("/aaaa_pkgs.preprocessed.sv")
  addResource("/tlul.preprocessed.sv")
  addResource("/prim.preprocessed.sv")
  addResource("/nmi_gen.preprocessed.sv")
  addResource("/nmi_gen.preprocessed.v")
}

class NmiGen(blockBytes: Int, beatBytes: Int, params: NmiGenParams)(implicit p: Parameters) extends LazyModule {

  // Create a simple device for this peripheral
  val device = new SimpleDevice("Esc", Seq("lowRISC,nmi-gen-0.5")) {
    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      Description(name, mapping ++ extraResources(resources))
    }
  }

  val ioNode = BundleBridgeSource(() => (new NmiGenPortIO).cloneType)
  val port = InModuleBody { ioNode.bundle }

  // Allow this device to extend the DTS mapping
  def extraResources(resources: ResourceBindings) = Map[String, Seq[ResourceValue]]()

  // Create our interrupt node
  val intnode = IntSourceNode(IntSourcePortSimple(num = 3, resources = Seq(Resource(device, "int"))))

  // Then also create the nexus node for the esc
  // Note: We create just a 3-input Node, as that is all we need here
  val escnode: EscSinkNode = EscSinkNode(EscSinkPortSimple(ports = nmi_gen_reg_pkg.N_ESC_SEV, sinks = 1))
  lazy val sources: Seq[EscSourcePortParameters] = escnode.edges.in.map(_.source)
  lazy val flatSources: Seq[EscSourceParameters] = sources.flatMap(_.sources)

  // Negotiated sizes (Note: nEscNodes and nEsc are not the same)
  def nEsc: Int = escnode.edges.in.map(_.source.sources.size).sum

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

    // Actual escs here
    val (escs, _) = escnode.in.unzip

    println(s"Escalation map (${nEsc} escs):")
    flatSources.zipWithIndex.foreach { case (s, i) =>
      // +1 because 0 is reserved, +1-1 because the range is half-open
      println(s"  [${i}] => ${s.name}")
    }
    println("")

    // Instance the Esc black box
    val blackbox = Module(new nmi_gen_wrapper)

    // Obtain the TL bundle
    val (tl_in, tl_edge) = peripheralNode.in(0) // Extract the port from the node

    // Clocks and resets
    blackbox.io.clk_i := clock
    blackbox.io.rst_ni := !reset.asBool

    // Connect the escs
    require(nEsc == 3, "lowRISC nmi_gen only supports 3 esc")
    for(i <- 0 until nmi_gen_reg_pkg.N_ESC_SEV) {
      if(i < nEsc) {
        blackbox.io.esc_tx_i(i).esc_n := escs(i).esc_tx.esc_n
        blackbox.io.esc_tx_i(i).esc_p := escs(i).esc_tx.esc_p
        escs(i).esc_rx.resp_n := blackbox.io.esc_rx_o(i).resp_n
        escs(i).esc_rx.resp_p := blackbox.io.esc_rx_o(i).resp_p
      }
      else {
        blackbox.io.esc_tx_i(i).esc_n := true.B
        blackbox.io.esc_tx_i(i).esc_p := false.B
      }
    }

    // Connect the interrupts
    interrupts(0) := blackbox.io.intr_esc0_o
    interrupts(1) := blackbox.io.intr_esc1_o
    interrupts(2) := blackbox.io.intr_esc2_o
    // TODO: blackbox.io.nmi_rst_req_o is not used in this context

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
        OMNmiGenDevice(
          memoryRegions = memRegions.map(_.copy(
            name = "Esc",
            description = "OpenTitan Esc-SHA256 HWIP"
          )),
          interrupts = interrupts
        )
      )
    }
  }
}

case class NmiGenAttachParams
(
  par: NmiGenParams,
  escNode: EscOutwardNode,
  controlWhere: TLBusWrapperLocation = PBUS,
  blockerAddr: Option[BigInt] = None,
  controlXType: ClockCrossingType = NoCrossing,
  intXType: ClockCrossingType = NoCrossing)
(implicit val p: Parameters) {

  def attachTo(where: Attachable)(implicit p: Parameters): NmiGen = {
    val name = s"NmiGen_${NmiGen.nextId()}"
    val cbus = where.locateTLBusWrapper(controlWhere)
    val clockDomainWrapper = LazyModule(new ClockSinkDomain(take = None))

    // The Esc lazymodule.
    val per = clockDomainWrapper { LazyModule(new NmiGen(cbus.blockBytes, cbus.beatBytes, par)) }
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

    for(_ <- 0 until nmi_gen_reg_pkg.N_ESC_SEV)
      per.escnode := escNode

    LogicalModuleTree.add(where.logicalTreeNode, per.logicalTreeNode)

    per
  }
}

object NmiGen {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }
}