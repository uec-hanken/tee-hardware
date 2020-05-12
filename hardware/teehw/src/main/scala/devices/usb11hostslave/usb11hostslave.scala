package uec.teehardware.devices.usb11hs

import chisel3._
import chisel3.util._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import uec.teehardware.devices.wb2axip._

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

class USB11HS(blockBytes: Int, beatBytes: Int, params: USB11HSParams)(implicit p: Parameters) extends LazyModule {

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

  val peripheralParam = TLManagerPortParameters(
    managers = Seq(TLManagerParameters(
      address = AddressSet.misaligned(params.address,  0x1000),
      resources = device.reg,
      regionType = RegionType.GET_EFFECTS, // NOT cacheable
      executable = false,
      supportsGet = TransferSizes(1, 1),
      supportsPutFull = TransferSizes(1, 1),
      supportsPutPartial = TransferSizes(1, 1),
      fifoId             = Some(0)
    )),
    beatBytes = 1 // Because I will connect a 8-bit AXI-4-lite here
  )
  val peripheralNode = TLManagerNode(Seq(peripheralParam))

  // Conversion nodes
  //val axifrag = LazyModule(new AXI4Fragmenter())
  val tlwidth = LazyModule(new TLWidthWidget(beatBytes))
  val tlfrag = LazyModule(new TLFragmenter(1, blockBytes))
  val node = TLBuffer()

  peripheralNode :=
    tlfrag.node :=
    tlwidth.node :=
    node

  lazy val module = new LazyModuleImp(this) {

    val io = IO(new USB11HSPortIO)
    val (interrupts, _) = intnode.out(0) // Expose the interrupt signals

    // Instance the USB black box
    val blackbox = Module(new usbHostSlave)

    // Obtain the TL bundle
    val (tl_in, tl_edge) = peripheralNode.in(0) // Extract the port from the node

    // Clocks and resets
    blackbox.io.clk_i := clock
    blackbox.io.rst_i := reset

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

    // Connect the TL bundle to the WishBone
    // Flow control
    val d_full = RegInit(false.B) // Transaction pending
    val d_valid_held = RegInit(false.B) // Held valid of D channel if not ready
    val d_size = Reg(UInt()) // Saved size
    val d_source = Reg(UInt()) // Saved source
    val d_hasData = Reg(Bool()) // Saved source

    // d_full logic: It is full if there is 1 transaction not completed
    // this is, of course, waiting until D responses for every individual A transaction
    when (tl_in.d.fire()) { d_full := false.B }
    when (tl_in.a.fire()) { d_full := true.B }

    // The D valid is the WB ack and the valid held (if D not ready yet)
    tl_in.d.valid := d_valid_held
    // Try to latch true the D valid held.
    // If we use fire for the "false" latch, it lasts at least 1 cycle
    when(blackbox.io.ack_o) { d_valid_held := true.B }
    when(tl_in.d.fire()) { d_valid_held := false.B }

    // The A ready should be 1 only if there is no transaction
    tl_in.a.ready := !d_full

    // hasData helds if there is a write transaction
    val hasData = tl_edge.hasData(tl_in.a.bits)

    // Response data to D
    val d_data = RegEnable(blackbox.io.data_o, blackbox.io.ack_o)

    // Save the size and the source from the A channel for the D channel
    when (tl_in.a.fire()) {
      d_size   := tl_in.a.bits.size
      d_source := tl_in.a.bits.source
      d_hasData := hasData
    }

    // Response characteristics
    tl_in.d.bits := tl_edge.AccessAck(d_source, d_size, d_data)
    tl_in.d.bits.opcode := Mux(d_hasData, TLMessages.AccessAck, TLMessages.AccessAckData)

    // Blackbox connections
    blackbox.io.strobe_i := tl_in.a.fire() // We trigger the transaction only here
    blackbox.io.we_i := hasData // Is write?
    blackbox.io.address_i := tl_in.a.bits.address(7, 0) // Only the LSB
    blackbox.io.data_i := tl_in.a.bits.data // The data (should be 8 bits tho)

    // Tie off unused channels
    tl_in.b.valid := false.B
    tl_in.c.ready := true.B
    tl_in.e.ready := true.B
  }
}

case class USB11HSAttachParams(
                             usbpar: USB11HSParams,
                             perBus: PeripheryBus,
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
    val pbus = params.perBus
    val usb11hs = LazyModule(new USB11HS(pbus.blockBytes, pbus.beatBytes, params.usbpar))

    // Connect the nodes to the control bus
    pbus.coupleTo( s"device_named_$name" ) {
      usb11hs.node := _
    }

    // Connect the interruptions
    params.intNode := usb11hs.intnode

    usb11hs
  }
}