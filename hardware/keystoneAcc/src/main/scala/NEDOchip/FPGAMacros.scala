package uec.keystoneAcc.nedochip

import chisel3._
import chisel3.util._
import chisel3.experimental._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sifive.blocks.devices.pinctrl._
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.fpgashells.clocks._

// ******* For Xilinx FPGAs
import sifive.fpgashells.ip.xilinx.vc707mig._

case class XilinxVC707MIGParams
(
  address : Seq[AddressSet]
)

class XilinxVC707MIGPads(depth : BigInt) extends VC707MIGIODDR(depth) {
  def this(c : XilinxVC707MIGParams) {
    this(AddressRange.fromSets(c.address).head.size)
  }
}

class XilinxVC707MIGIO(depth : BigInt) extends VC707MIGIODDR(depth) with VC707MIGIOClocksReset

class XilinxVC707MIG(c : XilinxVC707MIGParams, slaveParam: AXI4SlavePortParameters)(implicit p: Parameters) extends LazyModule {
  val ranges = AddressRange.fromSets(c.address)
  val offset = ranges.head.base
  val depth = ranges.head.size
  require((depth<=0x100000000L),"vc707mig supports upto 4GB depth configuraton")

  //val buffer  = LazyModule(new TLBuffer)
  val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("mem"), stripBits = 1))
  val indexer = LazyModule(new AXI4IdIndexer(idBits = 4))
  val deint   = LazyModule(new AXI4Deinterleaver(p(CacheBlockBytes)))
  val yank    = LazyModule(new AXI4UserYanker)
  val axi4node = AXI4SlaveNode(Seq(slaveParam))

  val node: TLInwardNode =
    axi4node := yank.node := deint.node := indexer.node := toaxi4.node// := buffer.node

  lazy val module = new LazyRawModuleImp(this) {
    val io = IO(new Bundle {
      val port = new XilinxVC707MIGIO(depth)
    })

    val blackbox = Module(new vc707mig(depth))
    val (axi_async, _) = axi4node.in(0) // TODO: Name wrong. Not really async but I do not want to rename

    //pins to top level

    //inouts
    attach(io.port.ddr3_dq,blackbox.io.ddr3_dq)
    attach(io.port.ddr3_dqs_n,blackbox.io.ddr3_dqs_n)
    attach(io.port.ddr3_dqs_p,blackbox.io.ddr3_dqs_p)

    //outputs
    io.port.ddr3_addr         := blackbox.io.ddr3_addr
    io.port.ddr3_ba           := blackbox.io.ddr3_ba
    io.port.ddr3_ras_n        := blackbox.io.ddr3_ras_n
    io.port.ddr3_cas_n        := blackbox.io.ddr3_cas_n
    io.port.ddr3_we_n         := blackbox.io.ddr3_we_n
    io.port.ddr3_reset_n      := blackbox.io.ddr3_reset_n
    io.port.ddr3_ck_p         := blackbox.io.ddr3_ck_p
    io.port.ddr3_ck_n         := blackbox.io.ddr3_ck_n
    io.port.ddr3_cke          := blackbox.io.ddr3_cke
    io.port.ddr3_cs_n         := blackbox.io.ddr3_cs_n
    io.port.ddr3_dm           := blackbox.io.ddr3_dm
    io.port.ddr3_odt          := blackbox.io.ddr3_odt

    //inputs
    //NO_BUFFER clock
    blackbox.io.sys_clk_i     := io.port.sys_clk_i

    io.port.ui_clk            := blackbox.io.ui_clk
    io.port.ui_clk_sync_rst   := blackbox.io.ui_clk_sync_rst
    io.port.mmcm_locked       := blackbox.io.mmcm_locked
    blackbox.io.aresetn       := io.port.aresetn
    blackbox.io.app_sr_req    := false.B
    blackbox.io.app_ref_req   := false.B
    blackbox.io.app_zq_req    := false.B
    //app_sr_active           := unconnected
    //app_ref_ack             := unconnected
    //app_zq_ack              := unconnected

    val awaddr = axi_async.aw.bits.addr - offset.U
    val araddr = axi_async.ar.bits.addr - offset.U

    //slave AXI interface write address ports
    blackbox.io.s_axi_awid    := axi_async.aw.bits.id
    blackbox.io.s_axi_awaddr  := awaddr //truncated
    blackbox.io.s_axi_awlen   := axi_async.aw.bits.len
    blackbox.io.s_axi_awsize  := axi_async.aw.bits.size
    blackbox.io.s_axi_awburst := axi_async.aw.bits.burst
    blackbox.io.s_axi_awlock  := axi_async.aw.bits.lock
    blackbox.io.s_axi_awcache := "b0011".U
    blackbox.io.s_axi_awprot  := axi_async.aw.bits.prot
    blackbox.io.s_axi_awqos   := axi_async.aw.bits.qos
    blackbox.io.s_axi_awvalid := axi_async.aw.valid
    axi_async.aw.ready        := blackbox.io.s_axi_awready

    //slave interface write data ports
    blackbox.io.s_axi_wdata   := axi_async.w.bits.data
    blackbox.io.s_axi_wstrb   := axi_async.w.bits.strb
    blackbox.io.s_axi_wlast   := axi_async.w.bits.last
    blackbox.io.s_axi_wvalid  := axi_async.w.valid
    axi_async.w.ready         := blackbox.io.s_axi_wready

    //slave interface write response
    blackbox.io.s_axi_bready  := axi_async.b.ready
    axi_async.b.bits.id       := blackbox.io.s_axi_bid
    axi_async.b.bits.resp     := blackbox.io.s_axi_bresp
    axi_async.b.valid         := blackbox.io.s_axi_bvalid

    //slave AXI interface read address ports
    blackbox.io.s_axi_arid    := axi_async.ar.bits.id
    blackbox.io.s_axi_araddr  := araddr // truncated
    blackbox.io.s_axi_arlen   := axi_async.ar.bits.len
    blackbox.io.s_axi_arsize  := axi_async.ar.bits.size
    blackbox.io.s_axi_arburst := axi_async.ar.bits.burst
    blackbox.io.s_axi_arlock  := axi_async.ar.bits.lock
    blackbox.io.s_axi_arcache := "b0011".U
    blackbox.io.s_axi_arprot  := axi_async.ar.bits.prot
    blackbox.io.s_axi_arqos   := axi_async.ar.bits.qos
    blackbox.io.s_axi_arvalid := axi_async.ar.valid
    axi_async.ar.ready        := blackbox.io.s_axi_arready

    //slace AXI interface read data ports
    blackbox.io.s_axi_rready  := axi_async.r.ready
    axi_async.r.bits.id       := blackbox.io.s_axi_rid
    axi_async.r.bits.data     := blackbox.io.s_axi_rdata
    axi_async.r.bits.resp     := blackbox.io.s_axi_rresp
    axi_async.r.bits.last     := blackbox.io.s_axi_rlast
    axi_async.r.valid         := blackbox.io.s_axi_rvalid

    //misc
    io.port.init_calib_complete := blackbox.io.init_calib_complete
    blackbox.io.sys_rst       :=io.port.sys_rst
    //mig.device_temp         :- unconnceted
  }
}

class TLULtoMIG(cacheBlockBytes: Int, TLparams: TLBundleParameters, device: MemoryDevice)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val c = AddressSet.misaligned(
    p(ExtMem).get.master.base,
    0x40000000L * 1 // 1GiB for the VC707DDR
  )
  val ddr = LazyModule(
    new XilinxVC707MIG(
      XilinxVC707MIGParams(c),
      AXI4SlavePortParameters(
        slaves = Seq(AXI4SlaveParameters(
          address       = c,
          resources     = device.reg,
          regionType    = RegionType.UNCACHED,
          executable    = true,
          supportsWrite = TransferSizes(1, cacheBlockBytes),
          supportsRead  = TransferSizes(1, cacheBlockBytes)
        )),
        beatBytes = p(ExtMem).head.master.beatBytes)
    )
  )

  // Create a dummy node where we can attach our silly TL port
  //val device = new MemoryDevice
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLClientPortParameters(
      clients = Seq(TLClientParameters(
        name = "dummy",
        sourceId = IdRange(0, 64) // TODO: What is this?
      ))
    )
  })

  // Attach to the DDR
  ddr.node := node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlport = Flipped(new TLUL(TLparams))
      var ddrport = new XilinxVC707MIGIO(ddr.depth)
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

    // Create the actual module, and attach the DDR port
    io.ddrport <> ddr.module.io.port
  }

}

// ** For Quartus-based FPGAs

class QuartusDDR extends Bundle {
  val memory_mem_a                = Output(Bits((14).W))
  val memory_mem_ba               = Output(Bits((3).W))
  val memory_mem_ck               = Output(Bits((2).W))
  val memory_mem_ck_n             = Output(Bits((2).W))
  val memory_mem_cke              = Output(Bits((2).W))
  val memory_mem_cs_n             = Output(Bits((2).W))
  val memory_mem_dm               = Output(Bits((8).W))
  val memory_mem_ras_n            = Output(Bool())
  val memory_mem_cas_n            = Output(Bool())
  val memory_mem_we_n             = Output(Bool())
  val memory_mem_dq               = Analog(64.W)
  val memory_mem_dqs              = Analog(8.W)
  val memory_mem_dqs_n            = Analog(8.W)
  val memory_mem_odt              = Output(Bits((2).W))

  //val reset_n          = Output(Bool())
}

trait QuartusClocksReset extends Bundle {
  //inputs
  //"NO_BUFFER" clock source (must be connected to IBUF outside of IP)
  val refclk_clk               = Input(Bool())
  val reset_reset_n         = Input(Bool())
  val dimmclk_clk           = Output(Clock())
  val usb_clk_clk           = Output(Clock())
}

trait QuartusUserSignals extends Bundle {
  val oct_rdn               = Input(Bool())
  val oct_rup               = Input(Bool())
  val mem_status_local_init_done = Output(Bool())
  val mem_status_local_cal_success = Output(Bool())
  val mem_status_local_cal_fail = Output(Bool())
}

class QuartusIO extends QuartusDDR with QuartusUserSignals

class QuartusPlatformBlackBox(implicit val p:Parameters) extends BlackBox {
  override def desiredName = "main"

  val io = IO(new QuartusIO with QuartusClocksReset {
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

class QuartusIsland(c : Seq[AddressSet], cacheBlockBytes: Int, val crossing: ClockCrossingType = AsynchronousCrossing(8))(implicit p: Parameters) extends LazyModule with CrossesToOnlyOneClockDomain {
  val ranges = AddressRange.fromSets(c)
  require (ranges.size == 1, "DDR range must be contiguous")
  val offset = ranges.head.base
  val depth = ranges.head.size
  require((depth<=0x100000000L),"vc707mig supports upto 4GB depth configuraton")

  val device = new MemoryDevice
  val node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    slaves = Seq(AXI4SlaveParameters(
      address       = c,
      resources     = device.reg,
      regionType    = RegionType.UNCACHED,
      executable    = true,
      supportsWrite = TransferSizes(1, cacheBlockBytes),
      supportsRead  = TransferSizes(1, cacheBlockBytes))),
    beatBytes = p(ExtMem).head.master.beatBytes)))

  lazy val module = new LazyRawModuleImp(this) {
    val io = IO(new Bundle {
      val port = new QuartusIO
      val ckrst = new Bundle with QuartusClocksReset
    })

    childClock := io.ckrst.dimmclk_clk
    childReset := !io.ckrst.reset_reset_n

    //MIG black box instantiation
    val blackbox = Module(new QuartusPlatformBlackBox)
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
    io.port.memory_mem_ck           := blackbox.io.memory_mem_ck
    io.port.memory_mem_ck_n         := blackbox.io.memory_mem_ck_n
    io.port.memory_mem_cke          := blackbox.io.memory_mem_cke
    io.port.memory_mem_cs_n         := blackbox.io.memory_mem_cs_n
    io.port.memory_mem_dm           := blackbox.io.memory_mem_dm
    io.port.memory_mem_odt          := blackbox.io.memory_mem_odt

    //inputs
    //NO_BUFFER clock
    blackbox.io.refclk_clk       := io.ckrst.refclk_clk
    blackbox.io.reset_reset_n := io.ckrst.reset_reset_n
    io.ckrst.dimmclk_clk       := blackbox.io.dimmclk_clk
    io.ckrst.usb_clk_clk       := blackbox.io.usb_clk_clk
    blackbox.io.oct_rdn       := io.port.oct_rdn
    blackbox.io.oct_rup       := io.port.oct_rup
    io.port.mem_status_local_init_done := blackbox.io.mem_status_local_init_done
    io.port.mem_status_local_cal_success := blackbox.io.mem_status_local_cal_success
    io.port.mem_status_local_cal_fail := blackbox.io.mem_status_local_cal_fail

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

class QuartusPlatform(c : Seq[AddressSet], cacheBlockBytes: Int, crossing: ClockCrossingType = AsynchronousCrossing(8))(implicit p: Parameters) extends LazyModule {
  val ranges = AddressRange.fromSets(c)
  val depth = ranges.head.size

  //val buffer  = LazyModule(new TLBuffer)
  val toaxi4  = LazyModule(new TLToAXI4(adapterName = Some("mem"), stripBits = 1))
  val indexer = LazyModule(new AXI4IdIndexer(idBits = 4))
  val deint   = LazyModule(new AXI4Deinterleaver(p(CacheBlockBytes)))
  val yank    = LazyModule(new AXI4UserYanker)
  val island  = LazyModule(new QuartusIsland(c, cacheBlockBytes, crossing))

  val node: TLInwardNode =
    island.crossAXI4In(island.node) := yank.node := deint.node := indexer.node := toaxi4.node// := buffer.node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val port = new QuartusIO
      val ckrst = new Bundle with QuartusClocksReset
    })

    io.port <> island.module.io.port
    io.ckrst <> island.module.io.ckrst
  }
}


class TLULtoQuartusPlatform(cacheBlockBytes: Int, TLparams: TLBundleParameters)(implicit p :Parameters)
  extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new QuartusPlatform(
      AddressSet.misaligned(
          p(ExtMem).get.master.base,
          0x40000000L * 1 // 1GiB for the VC707DDR
      ),
      cacheBlockBytes
    )
  )

  // Create a dummy node where we can attach our silly TL port
  val device = new MemoryDevice
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLClientPortParameters(
      clients = Seq(TLClientParameters(
        name = "dummy",
        sourceId = IdRange(0, 64), // TODO: What is this? Maybe 64 is not the right one
      ))
    )
  })

  // Attach to the DDR
  ddr.node := node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlport = Flipped(new TLUL(TLparams))
      var qport = new QuartusIO
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
// ila
//-------------------------------------------------------------------------

case class ilaConf (id: Int, wt: IndexedSeq[(Int,Int)], depth: Int = 1024, storCtl: Boolean = false, advTrig: Boolean = false)

class ilaBuild(val conf : ilaConf)
{
  val l = conf.wt.length
  var prob      = Seq.empty[Bits]
  for(i <- 0 until l) {
    prob = prob :+ UInt(conf.wt(i)._1.W)
    prob(i).suggestName("probe" + i.toString)
  }
  val m = Module(new BlackBox {
    val io = IO(new Bundle {
      val clk       = Input(Clock())
      val probe     = Input(MixedVec(prob))
      for(i <- 0 until l) {
        probe(i).suggestName("probe" + i.toString)
      }
    })
    override def desiredName = "ila" + conf.id.toString
    suggestName("ila" + conf.id.toString)
  })
  ElaborationArtefacts.add("ila" + conf.id.toString + ".ila.vivado.tcl", toString)
  ElaborationArtefacts.add("ila" + conf.id.toString + ".ila.v", toVerilogString)
  override def toString: String = {
    var str = """# Autogenerated via ilaBuild (Olinguito)
# See LICENSE for license details.
"""
    str = str + "create_ip -vendor xilinx.com -library ip -name ila -module_name ila_" + conf.id.toString + " -dir $ipdir -force\n"
    str = str + "set_property -dict [list \\\n"
    str = str + "CONFIG.C_NUM_OF_PROBES {" + l.toString + "}\\\n"
    str = str + "CONFIG.C_TRIGOUT_EN {false} \\\n"
    str = str + "CONFIG.C_TRIGIN_EN {false} \\\n"
    str = str + "CONFIG.C_MONITOR_TYPE {Native} \\\n"
    str = str + "CONFIG.C_ENABLE_ILA_AXI_MON {false} \\\n"
    str = str + "CONFIG.C_DATA_DEPTH {" + conf.depth.toString + "}\\\n"
    if(conf.storCtl) str = str + "CONFIG.C_ADV_TRIGGER {true}\\\n"
    if(conf.advTrig) str = str + "CONFIG.C_EN_STRG_QUAL {1}\\\n"
    for(i <- 0 until l) {
      str = str + "CONFIG.C_PROBE" + i.toString + "_WIDTH {" + conf.wt(i)._1.toString + "}\\\n"
    }
    str = str + "] [get_ips ila_" + conf.id.toString + "]\n"
    str
  }
  def toVerilogString: String = {
    var str = """// Autogenerated via ilaBuild (Olinguito)
// See LICENSE for license details.
"""
    str = str + "module ila" + conf.id.toString + " (\n"
    for(i <- 0 until l) {
      str = str + "  input [" + (conf.wt(i)._1-1).toString + ":0] probe_" + i.toString + ",\n"
    }
    str = str + "  input clk\n"
    str = str + ");\n\n"
    str = str + "ila_" + conf.id.toString + " ila (\n"
    for(i <- 0 until l) {
      str = str + "  .probe" + i.toString + "(probe_" + i.toString + "),\n"
    }
    str = str + "  .clk(clk)\n"
    str = str + ");\n\n"
    str = str + "endmodule\n"
    str
  }
}
