package uec.keystoneAcc.devices.usb11hs

import chisel3._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import uec.keystoneAcc.devices.wb2axip._

case class USB11HSParams(address: BigInt)

class USB11HSPortIO extends Bundle {
  // USB clock 48 MHz
  val usbClk = Input(Clock())
  // USB phy signals
  val USBWireDataIn = Input(Bits(2.W))
  val USBWireDataOut = Output(Bits(2.W))
  val USBWireDataOutTick = Output(Bool())
  val USBWireDataInTick = Output(Bool())
  val USBWireCtrlOut = Output(Bool())
  val USBFullSpeed = Output(Bool())
  val USBDPlusPullup = Output(Bool())
  val USBDMinusPullup = Output(Bool())
  val vBusDetect = Input(Bool())
}

class usbHostSlave extends BlackBox {
  val io = IO(new USB11HSPortIO {
    // Wishbone group
    val clk_i = Input(Clock())
    val rst_i = Input(Bool())
    val address_i = Input(UInt(8.W))
    val data_i = Input(UInt(8.W))
    val data_o = Output(UInt(8.W))
    val we_i = Input(Bool())
    val strobe_i = Input(Bool())
    val ack_o = Output(Bool())
    // USB interrupts
    val hostSOFSentIntOut = Output(Bool())
    val hostConnEventIntOut = Output(Bool())
    val hostResumeIntOut = Output(Bool())
    val hostTransDoneIntOut = Output(Bool())
    val slaveSOFRxedIntOut = Output(Bool())
    val slaveResetEventIntOut = Output(Bool())
    val slaveResumeIntOut = Output(Bool())
    val slaveTransDoneIntOut = Output(Bool())
    val slaveNAKSentIntOut = Output(Bool())
    val slaveVBusDetIntOut = Output(Bool())
  })
}

class USB11HS(blockBytes: Int, params: USB11HSParams)(implicit p: Parameters) extends LazyModule {

  // Create a simple device for this peripheral
  val device = new SimpleDevice("usb11hs", Seq("uec,usb11hs-0")) {
    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      Description(name, mapping ++ extraResources(resources))
    }
  }
  // Allow this device to extend the DTS mapping
  def extraResources(resources: ResourceBindings) = Map[String, Seq[ResourceValue]]()

  // Create our interrupt node
  val intnode = IntSourceNode(IntSourcePortSimple(num = 10, resources = Seq(Resource(device, "int"))))

  // Create the tilelink node
  /*val peripheralParam = TLManagerPortParameters(
    managers = Seq(TLManagerParameters(
      address = AddressSet.misaligned(params.address,  0x1000),
      resources = device.reg,
      regionType = RegionType.GET_EFFECTS, // NOT cacheable
      executable = false,
      supportsGet = TransferSizes(1, blockBytes),
      supportsPutFull = TransferSizes(1, blockBytes),
      supportsPutPartial = TransferSizes(1, blockBytes),
      fifoId             = Some(0)
    )),
    beatBytes = 1 // Because I will connect a 8-bit AXI-4-lite here
  )
  val peripheralNode = TLManagerNode(Seq(peripheralParam))*/

  // Create the axi4 node
  val axi4peripheralParam = AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = AddressSet.misaligned(params.address,  0x1000),
      resources     = device.reg,
      regionType    = RegionType.GET_EFFECTS,
      executable    = true,
      supportsWrite = TransferSizes(1, blockBytes),
      supportsRead  = TransferSizes(1, blockBytes)
    )),
    beatBytes = 1 // 8-bit AXI-4-lite here
  )
  val axi4peripheralNode = AXI4SlaveNode(Seq(axi4peripheralParam))

  lazy val module = new LazyModuleImp(this) {

    val io = IO(new USB11HSPortIO)
    val (interrupts, _) = intnode.out(0) // Expose the interrupt signals

    // Instance the USB black box
    val blackbox = Module(new usbHostSlave)

    // Instance also the AXI4 -> WB converter
    val (axi_async, _) = axi4peripheralNode.in(0) // Extract the port from the node
    val axlite2wbsp = Module(new axlite2wbsp(axi_async.params))

    // Clocks and resets
    blackbox.io.clk_i := clock
    blackbox.io.rst_i := reset
    axlite2wbsp.io.i_clk := clock
    axlite2wbsp.io.i_axi_reset_n := !reset.asBool()

    // Connect the phy to the outer
    blackbox.io.USBWireDataIn := io.USBWireDataIn
    io.USBWireDataOut := blackbox.io.USBWireDataOut
    io.USBWireDataOutTick := blackbox.io.USBWireDataOutTick
    io.USBWireDataInTick := blackbox.io.USBWireDataInTick
    io.USBWireCtrlOut := blackbox.io.USBWireCtrlOut
    io.USBFullSpeed := blackbox.io.USBFullSpeed
    io.USBDPlusPullup := blackbox.io.USBDPlusPullup
    io.USBDMinusPullup := blackbox.io.USBDMinusPullup
    blackbox.io.vBusDetect := io.vBusDetect
    blackbox.io.usbClk := io.usbClk

    // Connect the interrupts
    interrupts(0) := blackbox.io.hostSOFSentIntOut
    interrupts(1) := blackbox.io.hostConnEventIntOut
    interrupts(2) := blackbox.io.hostResumeIntOut
    interrupts(3) := blackbox.io.hostTransDoneIntOut
    interrupts(4) := blackbox.io.slaveSOFRxedIntOut
    interrupts(5) := blackbox.io.slaveResetEventIntOut
    interrupts(6) := blackbox.io.slaveResumeIntOut
    interrupts(7) := blackbox.io.slaveTransDoneIntOut
    interrupts(8) := blackbox.io.slaveNAKSentIntOut
    interrupts(9) := blackbox.io.slaveVBusDetIntOut

    // Connect the AXI4 node to the converter
    axi_async.aw.ready := axlite2wbsp.io.o_axi_awready
    axlite2wbsp.io.i_axi_awaddr := axi_async.aw.bits.addr
    axlite2wbsp.io.i_axi_awcache := axi_async.aw.bits.cache
    axlite2wbsp.io.i_axi_awprot := axi_async.aw.bits.prot
    axlite2wbsp.io.i_axi_awvalid := axi_async.aw.valid

    axi_async.w.ready := axlite2wbsp.io.o_axi_wready
    axlite2wbsp.io.i_axi_wdata := axi_async.w.bits.data
    axlite2wbsp.io.i_axi_wstrb := axi_async.w.bits.strb
    axlite2wbsp.io.i_axi_wvalid := axi_async.w.valid

    axlite2wbsp.io.i_axi_bready := axi_async.b.ready
    axi_async.b.bits.resp := axlite2wbsp.io.o_axi_bresp
    axi_async.b.valid := axlite2wbsp.io.o_axi_bvalid

    axi_async.ar.ready := axlite2wbsp.io.o_axi_arready
    axlite2wbsp.io.i_axi_araddr := axi_async.ar.bits.addr
    axlite2wbsp.io.i_axi_arcache := axi_async.ar.bits.cache
    axlite2wbsp.io.i_axi_arprot := axi_async.ar.bits.prot
    axlite2wbsp.io.i_axi_arvalid := axi_async.ar.valid

    axlite2wbsp.io.i_axi_rready := axi_async.r.ready
    axi_async.r.bits.data := axlite2wbsp.io.o_axi_rdata
    axi_async.r.bits.resp := axlite2wbsp.io.o_axi_rresp
    axi_async.r.valid := axlite2wbsp.io.o_axi_rvalid

    // Connect the WB to the blackbox
    // axlite2wbsp.io.o_reset ignored
    // axlite2wbsp.io.o_wb_cyc ignored (Already fragmented transactions)
    blackbox.io.strobe_i := axlite2wbsp.io.o_wb_stb
    blackbox.io.we_i := axlite2wbsp.io.o_wb_we
    blackbox.io.address_i := axlite2wbsp.io.o_wb_addr
    blackbox.io.data_i := axlite2wbsp.io.o_wb_data
    // axlite2wbsp.io.o_wb_sel ignored (1-byte transactions)
    axlite2wbsp.io.i_wb_ack := blackbox.io.ack_o
    axlite2wbsp.io.i_wb_data := blackbox.io.data_o
    axlite2wbsp.io.i_wb_err := false.B // No error transactions
  }
}

case class USB11HSAttachParams(
                             usbpar: USB11HSParams,
                             sysBus: SystemBus,
                             intNode: IntInwardNode,
                             controlXType: ClockCrossingType = NoCrossing,
                             intXType: ClockCrossingType = NoCrossing,
                             mclock: Option[ModuleValue[Clock]] = None,
                             mreset: Option[ModuleValue[Bool]] = None)
                           (implicit val p: Parameters)

object USB11HS {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }

  def attach(params: USB11HSAttachParams): USB11HS = {
    implicit val p = params.p
    val name = s"usb11hs ${nextId()}"
    val sbus = params.sysBus
    val usb11hs = LazyModule(new USB11HS(sbus.blockBytes, params.usbpar))
    //sha3.suggestName(name)

    // Connect the nodes to the control bus
    usb11hs.axi4peripheralNode := sbus.toFixedWidthPort(Some(s"device_named_$name")) {
      (AXI4Buffer() :=
        //AXI4Fragmenter() :=
        AXI4UserYanker() :=
        AXI4Deinterleaver(sbus.blockBytes) :=
        AXI4IdIndexer(4) :=
        TLToAXI4())
    }

    // Connect the interruptions
    params.intNode := usb11hs.intnode

    usb11hs
  }
}