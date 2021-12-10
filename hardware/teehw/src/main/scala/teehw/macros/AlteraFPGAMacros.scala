package uec.teehardware.macros

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.util._
import chisel3.experimental.{Analog, IO, attach}
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sifive.blocks.devices.pinctrl.BasePin
import testchipip.{SerialIO, TLDesser}
import uec.teehardware._

// ** For Quartus-based FPGAs

case class QuartusDDRConfig
(
  size_ck: Int = 2,
  size_cke: Int = 2,
  size_csn: Int = 2,
  size_odt: Int = 2,
  addrbit: Int = 14,
  octmode: Int = 0,
  is_reset: Boolean = false)

class OCTBundle(val mode: Int = 0) extends Bundle {
  val rdn = (mode == 0).option(Input(Bool()))
  val rup = (mode == 0).option(Input(Bool()))
  val rzqin = (mode == 1).option(Input(Bool()))
}

class QuartusDDR(c: QuartusDDRConfig = QuartusDDRConfig()) extends Bundle {
  val memory_mem_a       = Output(Bits(c.addrbit.W))
  val memory_mem_ba      = Output(Bits(3.W))
  val memory_mem_ck      = Output(Bits(c.size_ck.W))
  val memory_mem_ck_n    = Output(Bits(c.size_ck.W))
  val memory_mem_cke     = Output(Bits(c.size_cke.W))
  val memory_mem_cs_n    = Output(Bits(c.size_csn.W))
  val memory_mem_dm      = Output(Bits(8.W))
  val memory_mem_ras_n   = Output(Bool())
  val memory_mem_cas_n   = Output(Bool())
  val memory_mem_we_n    = Output(Bool())
  val memory_mem_reset_n = if(c.is_reset) Some(Output(Bool())) else None
  val memory_mem_dq      = Analog(64.W)
  val memory_mem_dqs     = Analog(8.W)
  val memory_mem_dqs_n   = Analog(8.W)
  val memory_mem_odt     = Output(Bits(c.size_odt.W))
  val oct                = new OCTBundle(c.octmode)
}

trait QuartusClocksReset extends Bundle {
  //inputs
  //"NO_BUFFER" clock source (must be connected to IBUF outside of IP)
  val ddr_ref_clk    = Input(Bool())
  val qsys_ref_clk   = Input(Bool())
  val system_reset_n = Input(Bool())
  val qsys_clk       = Output(Clock())
  val usb_clk        = Output(Clock())
  val io_clk         = Output(Clock())
}

trait QuartusUserSignals extends Bundle {
  val mem_status_local_init_done   = Output(Bool())
  val mem_status_local_cal_success = Output(Bool())
  val mem_status_local_cal_fail    = Output(Bool())
}

class QuartusIO(c: QuartusDDRConfig = QuartusDDRConfig()) extends QuartusDDR(c) with QuartusUserSignals

class QuartusPlatformBlackBox(c: QuartusDDRConfig = QuartusDDRConfig())(implicit val p:Parameters) extends BlackBox {
  override def desiredName = "main"

  val io = IO(new QuartusIO(c) with QuartusClocksReset {
    //axi_s
    //slave interface write address ports
    val axi4_awid = Input(Bits((4).W))
    val axi4_awaddr = Input(Bits((32).W))
    val axi4_awlen = Input(Bits((8).W))
    val axi4_awsize = Input(Bits((3).W))
    val axi4_awburst = Input(Bits((2).W))
    val axi4_awlock = Input(Bits((1).W))
    val axi4_awcache = Input(Bits((4).W))
    val axi4_awprot = Input(Bits((3).W))
    val axi4_awqos = Input(Bits((4).W))
    val axi4_awvalid = Input(Bool())
    val axi4_awready = Output(Bool())
    //slave interface write data ports
    val axi4_wdata = Input(Bits((32).W))
    val axi4_wstrb = Input(Bits((4).W))
    val axi4_wlast = Input(Bool())
    val axi4_wvalid = Input(Bool())
    val axi4_wready = Output(Bool())
    //slave interface write response ports
    val axi4_bready = Input(Bool())
    val axi4_bid = Output(Bits((4).W))
    val axi4_bresp = Output(Bits((2).W))
    val axi4_bvalid = Output(Bool())
    //slave interface read address ports
    val axi4_arid = Input(Bits((4).W))
    val axi4_araddr = Input(Bits((32).W))
    val axi4_arlen = Input(Bits((8).W))
    val axi4_arsize = Input(Bits((3).W))
    val axi4_arburst = Input(Bits((2).W))
    val axi4_arlock = Input(Bits((1).W))
    val axi4_arcache = Input(Bits((4).W))
    val axi4_arprot = Input(Bits((3).W))
    val axi4_arqos = Input(Bits((4).W))
    val axi4_arvalid = Input(Bool())
    val axi4_arready = Output(Bool())
    //slave interface read data ports
    val axi4_rready = Input(Bool())
    val axi4_rid = Output(Bits((4).W))
    val axi4_rdata = Output(Bits((32).W))
    val axi4_rresp = Output(Bits((2).W))
    val axi4_rlast = Output(Bool())
    val axi4_rvalid = Output(Bool())
  })
}

class QuartusIsland(c : Seq[AddressSet],
                    val crossing: ClockCrossingType = AsynchronousCrossing(8),
                    ddrc: QuartusDDRConfig = QuartusDDRConfig()
                   )(implicit p: Parameters) extends LazyModule with CrossesToOnlyOneClockDomain {
  val ranges = AddressRange.fromSets(c)
  require (ranges.size == 1, "DDR range must be contiguous")
  val offset = ranges.head.base
  val depth = ranges.head.size
  require((depth<=0x100000000L),"QuartusIsland supports upto 4GB depth configuraton")

  val device = new MemoryDevice
  val node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = c,
      resources     = device.reg,
      regionType    = RegionType.UNCACHED,
      executable    = true,
      supportsWrite = TransferSizes(1, 64),
      supportsRead  = TransferSizes(1, 64))),
    beatBytes = 4
  )))

  lazy val module = new LazyRawModuleImp(this) {
    val io = IO(new Bundle {
      val port = new QuartusIO(ddrc)
      val ckrst = new Bundle with QuartusClocksReset
    })

    childClock := io.ckrst.qsys_clk
    childReset := !io.ckrst.system_reset_n

    //MIG black box instantiation
    val blackbox = Module(new QuartusPlatformBlackBox(ddrc))
    val (axi_async, _) = node.in(0)

    //pins to top level

    //inouts
    attach(io.port.memory_mem_dq,blackbox.io.memory_mem_dq)
    attach(io.port.memory_mem_dqs_n,blackbox.io.memory_mem_dqs_n)
    attach(io.port.memory_mem_dqs,blackbox.io.memory_mem_dqs)

    //outputs
    io.port.memory_mem_a            := blackbox.io.memory_mem_a
    io.port.memory_mem_ba           := blackbox.io.memory_mem_ba
    io.port.memory_mem_ras_n        := blackbox.io.memory_mem_ras_n
    io.port.memory_mem_cas_n        := blackbox.io.memory_mem_cas_n
    io.port.memory_mem_we_n         := blackbox.io.memory_mem_we_n
    if(ddrc.is_reset) io.port.memory_mem_reset_n.get := blackbox.io.memory_mem_reset_n.get
    io.port.memory_mem_ck           := blackbox.io.memory_mem_ck
    io.port.memory_mem_ck_n         := blackbox.io.memory_mem_ck_n
    io.port.memory_mem_cke          := blackbox.io.memory_mem_cke
    io.port.memory_mem_cs_n         := blackbox.io.memory_mem_cs_n
    io.port.memory_mem_dm           := blackbox.io.memory_mem_dm
    io.port.memory_mem_odt          := blackbox.io.memory_mem_odt

    //inputs
    //NO_BUFFER clock
    blackbox.io.ddr_ref_clk    := io.ckrst.ddr_ref_clk
    blackbox.io.qsys_ref_clk   := io.ckrst.qsys_ref_clk
    blackbox.io.system_reset_n := io.ckrst.system_reset_n
    io.ckrst.qsys_clk          := blackbox.io.qsys_clk
    io.ckrst.usb_clk           := blackbox.io.usb_clk
    io.ckrst.io_clk            := blackbox.io.io_clk
    (blackbox.io.oct.rdn zip io.port.oct.rdn).foreach{case (a,b) => a := b}
    (blackbox.io.oct.rup zip io.port.oct.rup).foreach{case (a,b) => a := b}
    (blackbox.io.oct.rzqin zip io.port.oct.rzqin).foreach{case (a,b) => a := b}
    io.port.mem_status_local_init_done   := blackbox.io.mem_status_local_init_done
    io.port.mem_status_local_cal_success := blackbox.io.mem_status_local_cal_success
    io.port.mem_status_local_cal_fail    := blackbox.io.mem_status_local_cal_fail

    val awaddr = axi_async.aw.bits.addr - offset.U
    val araddr = axi_async.ar.bits.addr - offset.U

    //slave AXI interface write address ports
    blackbox.io.axi4_awid    := axi_async.aw.bits.id
    blackbox.io.axi4_awaddr  := awaddr //truncated
    blackbox.io.axi4_awlen   := axi_async.aw.bits.len
    blackbox.io.axi4_awsize  := axi_async.aw.bits.size
    blackbox.io.axi4_awburst := axi_async.aw.bits.burst
    blackbox.io.axi4_awlock  := axi_async.aw.bits.lock
    blackbox.io.axi4_awcache := "b0011".U
    blackbox.io.axi4_awprot  := axi_async.aw.bits.prot
    blackbox.io.axi4_awqos   := axi_async.aw.bits.qos
    blackbox.io.axi4_awvalid := axi_async.aw.valid
    axi_async.aw.ready        := blackbox.io.axi4_awready

    //slave interface write data ports
    blackbox.io.axi4_wdata   := axi_async.w.bits.data
    blackbox.io.axi4_wstrb   := axi_async.w.bits.strb
    blackbox.io.axi4_wlast   := axi_async.w.bits.last
    blackbox.io.axi4_wvalid  := axi_async.w.valid
    axi_async.w.ready         := blackbox.io.axi4_wready

    //slave interface write response
    blackbox.io.axi4_bready  := axi_async.b.ready
    axi_async.b.bits.id       := blackbox.io.axi4_bid
    axi_async.b.bits.resp     := blackbox.io.axi4_bresp
    axi_async.b.valid         := blackbox.io.axi4_bvalid

    //slave AXI interface read address ports
    blackbox.io.axi4_arid    := axi_async.ar.bits.id
    blackbox.io.axi4_araddr  := araddr // truncated
    blackbox.io.axi4_arlen   := axi_async.ar.bits.len
    blackbox.io.axi4_arsize  := axi_async.ar.bits.size
    blackbox.io.axi4_arburst := axi_async.ar.bits.burst
    blackbox.io.axi4_arlock  := axi_async.ar.bits.lock
    blackbox.io.axi4_arcache := "b0011".U
    blackbox.io.axi4_arprot  := axi_async.ar.bits.prot
    blackbox.io.axi4_arqos   := axi_async.ar.bits.qos
    blackbox.io.axi4_arvalid := axi_async.ar.valid
    axi_async.ar.ready        := blackbox.io.axi4_arready

    //slace AXI interface read data ports
    blackbox.io.axi4_rready  := axi_async.r.ready
    axi_async.r.bits.id       := blackbox.io.axi4_rid
    axi_async.r.bits.data     := blackbox.io.axi4_rdata
    axi_async.r.bits.resp     := blackbox.io.axi4_rresp
    axi_async.r.bits.last     := blackbox.io.axi4_rlast
    axi_async.r.valid         := blackbox.io.axi4_rvalid
  }
}

class QuartusPlatform(c : Seq[AddressSet],
                      ddrc: QuartusDDRConfig = QuartusDDRConfig())(implicit p: Parameters) extends LazyModule {
  val ranges = AddressRange.fromSets(c)
  require (ranges.size == 1, "DDR range must be contiguous")
  val offset = ranges.head.base
  val depth = ranges.head.size

  //val buffer  = LazyModule(new TLBuffer)
  val buffer  = LazyModule(new TLBuffer)
  val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("mem")))
  val indexer = LazyModule(new AXI4IdIndexer(idBits = 4))
  val deint   = LazyModule(new AXI4Deinterleaver(p(CacheBlockBytes)))
  val yank    = LazyModule(new AXI4UserYanker)
  val island  = LazyModule(new QuartusIsland(c, ddrc = ddrc))

  val node: TLInwardNode =
    island.crossAXI4In(island.node) := yank.node := deint.node := indexer.node := toaxi4.node := buffer.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val port = new QuartusIO(ddrc)
      val ckrst = new Bundle with QuartusClocksReset
    })

    io.port <> island.module.io.port
    io.ckrst <> island.module.io.ckrst
  }
}

class SertoQuartusPlatform(w: Int, idBits: Int = 6,
                           ddrc: QuartusDDRConfig = QuartusDDRConfig()
                           )(implicit p :Parameters)
  extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new QuartusPlatform(
      AddressSet.misaligned(
        p(ExtMem).get.master.base,
        0x40000000L * 1 // 1GiB for the VC707DDR
      ),
      ddrc = ddrc
    ))
  // Create the desser
  val params = Seq(TLMasterParameters.v1(
    name = "tl-desser",
    sourceId = IdRange(0, 1 << idBits)))
  val desser = LazyModule(new TLDesser(w, params, true)) // Attach to the DDR
  // Attach nodes
  if(p(ExtSerMem).head.master.beatBytes != 8)
    ddr.node := //TLSourceShrinker(16) :=
      TLWidthWidget(p(ExtSerMem).head.master.beatBytes) :=
      //TLFragmenter(p(ExtSerMem).head.master.beatBytes, p(MemoryBusKey).blockBytes) :=
      desser.node
  else
    ddr.node := //TLSourceShrinker(16) :=
      desser.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val serport = new SerialIO(w)
      val qport = new QuartusIO(ddrc)
      val ckrst = new Bundle with QuartusClocksReset
    })

    val depth = ddr.depth

    // Connect the serport
    io.serport <> desser.module.io.ser.head

    // Create the actual module, and attach the port
    io.qport <> ddr.module.io.port
    io.ckrst <> ddr.module.io.ckrst
  }

}

class TLULtoQuartusPlatform( TLparams: TLBundleParameters,
                             ddrc: QuartusDDRConfig = QuartusDDRConfig()
                           )(implicit p :Parameters)
  extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new QuartusPlatform(
      AddressSet.misaligned(
        p(ExtMem).get.master.base,
        0x40000000L * 1 // 1GiB for the VC707DDR
      ),
      ddrc = ddrc
    )
  )

  // Create a dummy node where we can attach our silly TL port
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 1 << TLparams.sourceBits), // CKDUR: The maximum ID possible goes here.
      ))
    )
  })

  // Attach to the DDR
  if(p(ExtMem).head.master.beatBytes != 4)
    ddr.node := TLWidthWidget(p(ExtMem).head.master.beatBytes) := node
  else
    ddr.node := node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlport = Flipped(new TLUL(TLparams))
      var qport = new QuartusIO(ddrc)
      val ckrst = new Bundle with QuartusClocksReset
    })

    val depth = ddr.depth

    //val mem_tl = Wire(HeterogeneousBag.fromNode(node.in))
    node.out.foreach {
      case  (bundle, _) =>
        bundle.a.valid := io.tlport.a.valid
        io.tlport.a.ready := bundle.a.ready
        bundle.a.bits := io.tlport.a.bits

        io.tlport.d.valid := bundle.d.valid
        bundle.d.ready := io.tlport.d.ready
        io.tlport.d.bits := bundle.d.bits
        //bundle.b.bits := (new TLBundleB(TLparams)).fromBits(0.U)
        bundle.b.ready := true.B
        bundle.c.valid := false.B
        //bundle.c.bits := 0.U.asTypeOf(new TLBundleC(TLparams))
        bundle.e.valid := false.B
      //bundle.e.bits := 0.U.asTypeOf(new TLBundleE(TLparams))
    }

    // Create the actual module, and attach the port
    io.qport <> ddr.module.io.port
    io.ckrst <> ddr.module.io.ckrst
  }

}

//-------------------------------------------------------------------------
// IO Lib for Altera boards
//-------------------------------------------------------------------------

class ALT_IOBUF extends BlackBox{
  val io = IO(new Bundle{
    val io = Analog(1.W)
    val oe = Input(Bool())
    val i = Input(Bool())
    val o = Output(Bool())
  })

  def asInput() : Bool = {
    io.oe := false.B
    io.i := false.B
    io.o
  }

  def asOutput(o: Bool) : Unit = {
    io.oe := true.B
    io.i := o
  }

  def fromBase(e: BasePin) : Unit = {
    io.oe := e.o.oe
    io.i := e.o.oval
    e.i.ival := io.o
  }

  def attachTo(analog: Analog) : Unit = {
    attach(analog, io.io)
  }
}

object ALT_IOBUF {
  def apply : ALT_IOBUF = {
    Module(new ALT_IOBUF)
  }

  def apply(analog: Analog) : Bool = {
    val m = Module(new ALT_IOBUF)
    m.attachTo(analog)
    m.asInput()
  }

  def apply(analog: Analog, i: Bool) : Unit = {
    val m = Module(new ALT_IOBUF)
    m.attachTo(analog)
    m.asOutput(i)
  }

  def apply(analog: Analog, e: BasePin) : Unit = {
    val m = Module(new ALT_IOBUF)
    m.attachTo(analog)
    m.fromBase(e)
  }
}