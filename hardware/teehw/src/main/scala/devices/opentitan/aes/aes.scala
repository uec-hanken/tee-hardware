package uec.teehardware.devices.opentitan.aes

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
import uec.teehardware.devices.opentitan.alert._
import uec.teehardware.devices.opentitan.top_pkg._

import sys.process._

object aes_reg_pkg {
  def NumRegsKey = 8
  def NumRegsIv = 4
  def NumRegsData = 4
  def NumAlerts = 2
}

case class AESOTParams(address: BigInt)

case class OMAESOTDevice
(
  memoryRegions: Seq[OMMemoryRegion],
  interrupts: Seq[OMInterrupt],
  _types: Seq[String] = Seq("OMAESOTDevice", "OMDevice", "OMComponent")
) extends OMDevice

class AESOTPortIO extends Bundle {
}

class aes_wrapper
(
  AES192Enable: Boolean = true,
  Masking: Boolean = false, // If true, needs a masked sbox
  SBoxImpl: String = "aes_pkg::SBoxImplLut",
  SecStartTriggerDelay: Int = 0, // Has to be unsigned no?
  AlertAsyncOn: Seq[Boolean] = Seq.fill(aes_reg_pkg.NumAlerts)(true)
)
  extends BlackBox(
    Map(
      "AES192Enable" -> IntParam(if(AES192Enable) 1 else 0),
      "Masking" -> IntParam(if(Masking) 1 else 0),
      "SBoxImpl" -> RawParam(SBoxImpl),
      "SecStartTriggerDelay" -> IntParam(SecStartTriggerDelay),
      "AlertAsyncOn" -> RawParam( // Default: 2'b11
        aes_reg_pkg.NumAlerts.toString + "'b" +
        AlertAsyncOn.map(if(_) "1" else "0").reduce((a,b) => b+a)
      )
    )
  )
    with HasBlackBoxResource {

  require(SecStartTriggerDelay >= 0, "SecStartTriggerDelay needs to be positive")

  val io = IO(new Bundle {
    // Clock and Reset
    val clk_i = Input(Clock())
    val rst_ni = Input(Bool())

    // Idle indicator for clock manager
    val idle_o = Output(Bool())

    // Bus interface
    val tl = Flipped(new TLBundle(OpenTitanTLparams))

    // Alerts
    val alert_rx_i = Vec(aes_reg_pkg.NumAlerts, Input(new alert_rx_t()))
    val alert_tx_o = Vec(aes_reg_pkg.NumAlerts, Output(new alert_tx_t()))
  })

  // pre-process the verilog to remove "includes" and combine into one file
  val make = "make -C hardware/teehw/src/main/resources aesot"
  val proc = make
  require (proc.! == 0, "Failed to run preprocessing step")

  // add wrapper/blackbox after it is pre-processed
  addResource("/aesot.preprocessed.sv")
  addResource("/aesot.preprocessed.v")
}

class AESOT(blockBytes: Int, beatBytes: Int, params: AESOTParams)(implicit p: Parameters) extends LazyModule {

  // Create a simple device for this peripheral
  val device = new SimpleDevice("aesot", Seq("lowRISC,aes-1.0")) {
    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      Description(name, mapping ++ extraResources(resources))
    }
  }

  val ioNode = BundleBridgeSource(() => (new AESOTPortIO).cloneType)
  val port = InModuleBody { ioNode.bundle }

  // Allow this device to extend the DTS mapping
  def extraResources(resources: ResourceBindings) = Map[String, Seq[ResourceValue]]()

  // Create the alert node
  val alertnode = AlertSourceNode(AlertSourcePortSimple(num = aes_reg_pkg.NumAlerts, resources = Seq(Resource(device, "alert"))))

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
    val (alerts, _) = alertnode.out(0) // Expose the alert signals

    // Instance the AES black box
    val blackbox = Module(new aes_wrapper)

    // Obtain the TL bundle
    val (tl_in, tl_edge) = peripheralNode.in(0) // Extract the port from the node

    // Clocks and resets
    blackbox.io.clk_i := clock
    blackbox.io.rst_ni := !reset.asBool

    // Connect the external ports
    (alerts zip blackbox.io.alert_rx_i).foreach{ case(j, i) =>
      i.ack_n := j.alert_rx.ack_n
      i.ack_p := j.alert_rx.ack_p
      i.ping_n := j.alert_rx.ping_n
      i.ping_p := j.alert_rx.ping_p
    }
    (alerts zip blackbox.io.alert_tx_o).foreach{ case(j, i) =>
      j.alert_tx.alert_n := i.alert_n
      j.alert_tx.alert_p := i.alert_p
    }
    // TODO: idle_o not used, for the clock manager

    // Connect the interrupts

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
        OMAESOTDevice(
          memoryRegions = memRegions.map(_.copy(
            name = "aesot",
            description = "OpenTitan AES HWIP"
          )),
          interrupts = interrupts
        )
      )
    }
  }
}

case class AESOTAttachParams
(
  par: AESOTParams,
  alertNode: AlertInwardNode,
  controlWhere: TLBusWrapperLocation = PBUS,
  blockerAddr: Option[BigInt] = None,
  controlXType: ClockCrossingType = NoCrossing,
  intXType: ClockCrossingType = NoCrossing)
(implicit val p: Parameters) {

  def attachTo(where: Attachable)(implicit p: Parameters): AESOT = {
    val name = s"aesot_${AESOT.nextId()}"
    val cbus = where.locateTLBusWrapper(controlWhere)
    val clockDomainWrapper = LazyModule(new ClockSinkDomain(take = None))
    val per = clockDomainWrapper { LazyModule(new AESOT(cbus.blockBytes, cbus.beatBytes, par)) }
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

    alertNode := per.alertnode

    LogicalModuleTree.add(where.logicalTreeNode, per.logicalTreeNode)

    per
  }
}

object AESOT {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }
}