package uec.teehardware.devices.opentitan.otp_ctrl

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

object otp_ctrl_reg_pkg {
  // From reg_pkg
  def OtpByteAddrWidth = 11;
  def NumErrorEntries = 9;
  def NumDaiWords = 2;
  def NumDigestWords = 2;
  def NumLcHalfwords = 12;
  def NumCreatorSwCfgWindowWords = 256;
  def NumOwnerSwCfgWindowWords = 256;
  def NumDebugWindowWords = 512;
  def NumAlerts = 2;
  
  def OtpWidth = 16
  def OtpAddrWidth = OtpByteAddrWidth - log2Ceil(OtpWidth/8)
  def OtpDepth = 1 << OtpAddrWidth
  def OtpCmdWidth = 2
  def OtpSizeWidth = 2
  def OtpErrWidth = 4
  def OtpIfWidth = 1 << OtpSizeWidth*OtpWidth
  def OtpAddrShift = OtpByteAddrWidth - OtpAddrWidth
  
  def LcValueWidth = OtpWidth
  def LcTokenWidth = 128
  def NumLcStateValues = 12
  def LcStateWidth = NumLcStateValues * LcValueWidth
  def NumLcCountValues = 32

  ////////////////////////////////
  // Typedefs for Key Broadcast //
  ////////////////////////////////

  def FlashKeySeedWidth = 256;
  def SramKeySeedWidth  = 128;
  def KeyMgrKeyWidth   = 256;
  def FlashKeyWidth    = 128;
  def SramKeyWidth     = 128;
  def SramNonceWidth   = 64;
  def OtbnKeyWidth     = 128;
  def OtbnNonceWidth   = 256;

  // Configuration
  def NumHwCfgBits = 176*8 // PartInfo[HwCfgIdx].size
}

case class OTPCtrlParams(address: BigInt)

case class OMOTPCtrlDevice
(
  memoryRegions: Seq[OMMemoryRegion],
  interrupts: Seq[OMInterrupt],
  _types: Seq[String] = Seq("OMOTPCtrlDevice", "OMDevice", "OMComponent")
) extends OMDevice

class OTPCtrlPortIO(val NumSramKeyReqSlots:Int = 2) extends Bundle {
  // TODO: EDN interface for entropy updates
  val edn_otp_up_i = Input(new edn_otp_up_t())

  // TODO: EDN interface for requesting entropy
  val otp_edn_req_o = Output(new otp_edn_req_t())
  val otp_edn_rsp_i = Input(new otp_edn_rsp_t())

  // Power manager interface
  val pwr_otp_init_req_i = Input(new pwr_otp_init_req_t())
  val pwr_otp_init_rsp_o = Output(new pwr_otp_init_rsp_t())
  val otp_pwr_state_o = Output(new otp_pwr_state_t())

  // Lifecycle transition command interface
  val lc_otp_program_req_i = Input(new lc_otp_program_req_t())
  val lc_otp_program_rsp_o = Output(new lc_otp_program_rsp_t())

  // Lifecycle hashing interface for raw unlock
  val lc_otp_token_req_i = Input(new lc_otp_token_req_t())
  val lc_otp_token_rsp_o = Output(new lc_otp_token_rsp_t())

  // Lifecycle broadcast inputs
  val lc_escalate_en_i = Input(new lc_tx_t())
  val lc_provision_en_i = Input(new lc_tx_t())
  val lc_test_en_i = Input(new lc_tx_t())

  // OTP broadcast outputs
  val otp_lc_data_o = Output(new otp_lc_data_t())
  val otp_keymgr_key_o = Output(new otp_keymgr_key_t())

  // Scrambling key requests
  val flash_otp_key_req_i = Input(new flash_otp_key_req_t())
  val flash_otp_key_rsp_o = Output(new flash_otp_key_rsp_t())
  val sram_otp_key_req_i = Input(Vec(NumSramKeyReqSlots, new sram_otp_key_req_t()))
  val sram_otp_key_rsp_o = Output(Vec(NumSramKeyReqSlots, new sram_otp_key_rsp_t()))
  val otbn_otp_key_req_i = Input(new otbn_otp_key_req_t())
  val otbn_otp_key_rsp_o = Output(new otbn_otp_key_rsp_t())

  // Hardware configuration bits
  val hw_cfg_o = Output(UInt(otp_ctrl_reg_pkg.NumHwCfgBits.W))
}

class OTPCtrl(blockBytes: Int, beatBytes: Int, params: OTPCtrlParams)(implicit p: Parameters) extends LazyModule {

  // Create a simple device for this peripheral
  val device = new SimpleDevice("otp_ctrl", Seq("lowRISC,otp_ctrl-0.1")) {
    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      Description(name, mapping ++ extraResources(resources))
    }
  }

  val ioNode = BundleBridgeSource(() => (new OTPCtrlPortIO).cloneType)
  val port = InModuleBody { ioNode.bundle }

  // Allow this device to extend the DTS mapping
  def extraResources(resources: ResourceBindings) = Map[String, Seq[ResourceValue]]()

  // Create our interrupt node
  val intnode = IntSourceNode(IntSourcePortSimple(num = 2, resources = Seq(Resource(device, "int"))))

  // Create the alert node
  val alertnode = AlertSourceNode(AlertSourcePortSimple(num = otp_ctrl_reg_pkg.NumAlerts, resources = Seq(Resource(device, "alert"))))

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
    val (alerts, _) = alertnode.out(0) // Expose the alert signals

    // Instance the OTPCtrl black box
    val blackbox = Module(new otp_ctrl_wrapper)

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

    // Connect the interrupts
    interrupts(0) := blackbox.io.intr_otp_error_o
    interrupts(1) := blackbox.io.intr_otp_operation_done_o

    // Connect the TL bundle
    blackbox.io.tl.a <> tl_in.a
    tl_in.d <> blackbox.io.tl.d

    // Tie off unused channels
    tl_in.b.valid := false.B
    tl_in.c.ready := true.B
    tl_in.e.ready := true.B

    // Connect the ports to the outside
    blackbox.io.edn_otp_up_i := io.edn_otp_up_i

    io.otp_edn_req_o := blackbox.io.otp_edn_req_o
    blackbox.io.otp_edn_rsp_i := io.otp_edn_rsp_i

    blackbox.io.pwr_otp_init_req_i := io.pwr_otp_init_req_i
    io.pwr_otp_init_rsp_o := blackbox.io.pwr_otp_init_rsp_o
    io.pwr_otp_init_rsp_o := blackbox.io.pwr_otp_init_rsp_o

    blackbox.io.lc_otp_program_req_i := io.lc_otp_program_req_i
    io.lc_otp_program_rsp_o := blackbox.io.lc_otp_program_rsp_o

    blackbox.io.lc_otp_token_req_i := io.lc_otp_token_req_i
    io.lc_otp_token_rsp_o := blackbox.io.lc_otp_token_rsp_o

    blackbox.io.lc_escalate_en_i := io.lc_escalate_en_i
    blackbox.io.lc_provision_en_i := io.lc_provision_en_i
    blackbox.io.lc_test_en_i := io.lc_test_en_i

    io.otp_lc_data_o := blackbox.io.otp_lc_data_o
    io.otp_keymgr_key_o := blackbox.io.otp_keymgr_key_o

    blackbox.io.flash_otp_key_req_i := io.flash_otp_key_req_i
    io.flash_otp_key_rsp_o := blackbox.io.flash_otp_key_rsp_o
    blackbox.io.sram_otp_key_req_i := io.sram_otp_key_req_i
    io.sram_otp_key_rsp_o := blackbox.io.sram_otp_key_rsp_o
    blackbox.io.otbn_otp_key_req_i := io.otbn_otp_key_req_i
    io.otbn_otp_key_rsp_o := blackbox.io.otbn_otp_key_rsp_o

    io.hw_cfg_o := blackbox.io.hw_cfg_o
  }

  val logicalTreeNode = new LogicalTreeNode(() => Some(device)) {
    def getOMComponents(resourceBindings: ResourceBindings, children: Seq[OMComponent] = Nil): Seq[OMComponent] = {
      val Description(name, mapping) = device.describe(resourceBindings)
      val memRegions = DiplomaticObjectModelAddressing.getOMMemoryRegions(name, resourceBindings, None)
      val interrupts = DiplomaticObjectModelAddressing.describeInterrupts(name, resourceBindings)
      Seq(
        OMOTPCtrlDevice(
          memoryRegions = memRegions.map(_.copy(
            name = "otp_ctrl",
            description = "OpenTitan OTPCtrl HWIP"
          )),
          interrupts = interrupts
        )
      )
    }
  }
}

case class OTPCtrlAttachParams
(
  par: OTPCtrlParams,
  alertNode: AlertInwardNode,
  controlWhere: TLBusWrapperLocation = PBUS,
  blockerAddr: Option[BigInt] = None,
  controlXType: ClockCrossingType = NoCrossing,
  intXType: ClockCrossingType = NoCrossing)
(implicit val p: Parameters) {

  def attachTo(where: Attachable)(implicit p: Parameters): OTPCtrl = {
    val name = s"otp_ctrl_${OTPCtrl.nextId()}"
    val cbus = where.locateTLBusWrapper(controlWhere)
    val clockDomainWrapper = LazyModule(new ClockSinkDomain(take = None))
    val per = clockDomainWrapper { LazyModule(new OTPCtrl(cbus.blockBytes, cbus.beatBytes, par)) }
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

    alertNode := per.alertnode

    LogicalModuleTree.add(where.logicalTreeNode, per.logicalTreeNode)

    per
  }
}

object OTPCtrl {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }
}