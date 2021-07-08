package uec.teehardware

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
import sifive.fpgashells.devices.xilinx.xilinxvc707mig._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import sifive.fpgashells.devices.xilinx.xilinxvcu118mig._
import testchipip.{SerialIO, TLDesser}
//import uec.teehardware.vc707mig32._

// ******* For Xilinx FPGAs
import sifive.fpgashells.ip.xilinx.vc707mig._
import sifive.fpgashells.ip.xilinx.vcu118mig._

class SertoMIG(w: Int, idBits: Int = 6)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new XilinxVC707MIG(
      XilinxVC707MIGParams(
        AddressSet.misaligned(
          p(ExtSerMem).get.master.base,
          0x40000000L * 1 // 1GiB for the VC707DDR,
        ))))
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
  // Create the module
  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val serport = new SerialIO(w)
      val ddrport = new XilinxVC707MIGIO(ddr.depth)
    })

    val depth = ddr.depth

    // Connect the serport
    io.serport <> desser.module.io.ser.head

    // Create the actual module, and attach the DDR port
    io.ddrport <> ddr.module.io.port
  }
}

class TLULtoMIG(cacheBlockBytes: Int, TLparams: TLBundleParameters)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new XilinxVC707MIG(
      XilinxVC707MIGParams(
        AddressSet.misaligned(
          p(ExtMem).get.master.base,
          0x40000000L * 1 // 1GiB for the VC707DDR,
        ))))

  // Create a dummy node where we can attach our silly TL port
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 64) // CKDUR: The maximum ID possible goes here.
      ))
    )
  })

  // Attach to the DDR
  if(p(ExtMem).head.master.beatBytes != 8)
    ddr.node := TLWidthWidget(p(ExtMem).head.master.beatBytes) := node
  else
    ddr.node := node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlport = Flipped(new TLUL(TLparams))
      val ddrport = new XilinxVC707MIGIO(ddr.depth)
    })

    val depth = ddr.depth

    //val mem_tl = Wire(HeterogeneousBag.fromNode(node.in))
    node.out.foreach {
      case  (bundle, _) =>
        // Debug TL
        //val ilatoaxi = Module(new ilatl())
        //ilatoaxi.io.clk := clock
        //ilatoaxi.connectAxi(bundle)

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

class SertoMIGUltra(w: Int, idBits: Int = 6)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new XilinxVCU118MIG(
      XilinxVCU118MIGParams(
        AddressSet.misaligned(
          p(ExtSerMem).get.master.base,
          0x80000000L * 1 // 2GiB for the VCU118DDR,
        ))))
  // Create the desser
  val params = Seq(TLMasterParameters.v1(
    name = "tl-desser",
    sourceId = IdRange(0, 1 << idBits)))
  val desser = LazyModule(new TLDesser(w, params, true))
  // Attach nodes
  if(p(ExtSerMem).head.master.beatBytes != 8)
    ddr.node := //TLSourceShrinker(16) :=
      TLWidthWidget(p(ExtSerMem).head.master.beatBytes) :=
      //TLFragmenter(p(ExtSerMem).head.master.beatBytes, p(MemoryBusKey).blockBytes) :=
      desser.node
  else
    ddr.node := //TLSourceShrinker(16) :=
      desser.node
  // Create the module
  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val serport = new SerialIO(w)
      val ddrport = new XilinxVCU118MIGIO(ddr.depth)
    })

    val depth = ddr.depth

    // Connect the serport
    io.serport <> desser.module.io.ser.head

    // Create the actual module, and attach the DDR port
    io.ddrport <> ddr.module.io.port
  }
}

class TLULtoMIGUltra(cacheBlockBytes: Int, TLparams: TLBundleParameters)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new XilinxVCU118MIG(
      XilinxVCU118MIGParams(
        AddressSet.misaligned(
          p(ExtMem).get.master.base,
          0x80000000L * 1 // 2GiB for the VCU118DDR,
        ))))

  // Create a dummy node where we can attach our silly TL port
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 64) // CKDUR: The maximum ID possible goes here.
      ))
    )
  })

  // Attach to the DDR
  if(p(ExtMem).head.master.beatBytes != 8)
    ddr.node := TLWidthWidget(p(ExtMem).head.master.beatBytes) := node
  else
    ddr.node := node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlport = Flipped(new TLUL(TLparams))
      val ddrport = new XilinxVCU118MIGIO(ddr.depth)
    })

    val depth = ddr.depth

    //val mem_tl = Wire(HeterogeneousBag.fromNode(node.in))
    node.out.foreach {
      case  (bundle, _) =>
        // Debug TL
        //val ilatoaxi = Module(new ilatl())
        //ilatoaxi.io.clk := clock
        //ilatoaxi.connectAxi(bundle)

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

class TLtoPCIe(cacheBlockBytes: Int,
               TLparamsMaster: TLBundleParameters,
               TLparamsSlave: TLBundleParameters)(implicit p :Parameters)
  extends LazyModule {
  // Create the pcie

  val pcie = LazyModule(new XilinxVC707PCIeX1)

  // Create dummy nodes where we can attach our silly TL ports
  val nodeSlave = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 64), // CKDUR: The maximum ID possible goes here.
      ))
    )
  })

  // TODO: MODIFY EVERYTHING
  val device = new MemoryDevice
  val nodeMaster = TLManagerNode(Seq(TLSlavePortParameters.v1(
    managers = Seq(TLManagerParameters(
      address = AddressSet.misaligned(0x0, -1), // TODO The whole space, I think
      resources = pcie.axi_to_pcie_x1.device.reg,
      regionType = RegionType.UNCACHED, // cacheable
      executable = true,
      supportsGet = TransferSizes(1, cacheBlockBytes),
      supportsPutFull = TransferSizes(1, cacheBlockBytes),
      supportsPutPartial = TransferSizes(1, cacheBlockBytes),
      fifoId             = Some(0),
      mayDenyPut         = true,
      mayDenyGet         = true
    )),
    beatBytes = p(SystemBusKey).beatBytes
  )))

  // Attach to the PCIe
  pcie.crossTLIn(pcie.slave) := nodeSlave
  pcie.crossTLIn(pcie.control) := nodeSlave
  nodeMaster := pcie.crossTLOut(pcie.master)

  // The interrupt node
  val intnode = pcie.crossIntOut(pcie.intnode)

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlportMaster = Flipped(new TLBundle(TLparamsMaster))
      val tlportSlave = Flipped(new TLBundle(TLparamsSlave))
      var port = new XilinxVC707PCIeX1IO
    })

    nodeSlave.out.foreach {
      case  (bundle, _) =>
        bundle.a.valid := io.tlportSlave.a.valid
        io.tlportSlave.a.ready := bundle.a.ready
        bundle.a.bits := io.tlportSlave.a.bits

        io.tlportSlave.b.valid := bundle.b.valid
        bundle.b.ready := io.tlportSlave.b.ready
        io.tlportSlave.b.bits := bundle.b.bits
        
        bundle.c.valid := io.tlportSlave.c.valid
        io.tlportSlave.c.ready := bundle.c.ready
        bundle.c.bits := io.tlportSlave.c.bits

        io.tlportSlave.d.valid := bundle.d.valid
        bundle.d.ready := io.tlportSlave.d.ready
        io.tlportSlave.d.bits := bundle.d.bits

        bundle.e.valid := io.tlportSlave.e.valid
        io.tlportSlave.e.ready := bundle.e.ready
        bundle.e.bits := io.tlportSlave.e.bits
    }

    nodeMaster.out.foreach {
      case  (bundle, _) =>
        io.tlportMaster.a.valid := bundle.a.valid
        bundle.a.ready := io.tlportMaster.a.ready
        io.tlportMaster.a.bits := bundle.a.bits

        bundle.b.valid := io.tlportMaster.b.valid
        io.tlportMaster.b.ready := bundle.b.ready
        bundle.b.bits := io.tlportMaster.b.bits

        io.tlportMaster.c.valid := bundle.c.valid
        bundle.c.ready := io.tlportMaster.c.ready
        io.tlportMaster.c.bits := bundle.c.bits

        bundle.d.valid := io.tlportMaster.d.valid
        io.tlportMaster.d.ready := bundle.d.ready
        bundle.d.bits := io.tlportMaster.d.bits

        io.tlportMaster.e.valid := bundle.e.valid
        bundle.e.ready := io.tlportMaster.e.ready
        io.tlportMaster.e.bits := bundle.e.bits
    }

    // Create the actual module, and attach the port
    io.port <> pcie.module.io.port
  }

}

// ** For Quartus-based FPGAs

case class QuartusDDRConfig (size_ck: Int = 2, is_reset: Boolean = false)

class QuartusDDR(c: QuartusDDRConfig = QuartusDDRConfig()) extends Bundle {
  val memory_mem_a       = Output(Bits((14).W))
  val memory_mem_ba      = Output(Bits((3).W))
  val memory_mem_ck      = Output(Bits((c.size_ck).W))
  val memory_mem_ck_n    = Output(Bits((c.size_ck).W))
  val memory_mem_cke     = Output(Bits((2).W))
  val memory_mem_cs_n    = Output(Bits((2).W))
  val memory_mem_dm      = Output(Bits((8).W))
  val memory_mem_ras_n   = Output(Bool())
  val memory_mem_cas_n   = Output(Bool())
  val memory_mem_we_n    = Output(Bool())
  val memory_mem_reset_n = if(c.is_reset) Some(Output(Bool())) else None
  val memory_mem_dq      = Analog(64.W)
  val memory_mem_dqs     = Analog(8.W)
  val memory_mem_dqs_n   = Analog(8.W)
  val memory_mem_odt     = Output(Bits((2).W))
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
  val oct_rdn                      = Input(Bool())
  val oct_rup                      = Input(Bool())
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
    blackbox.io.oct_rdn        := io.port.oct_rdn
    blackbox.io.oct_rup        := io.port.oct_rup
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
                      cacheBlockBytes: Int,
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


class TLULtoQuartusPlatform( cacheBlockBytes: Int,
                             TLparams: TLBundleParameters,
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
      cacheBlockBytes,
      ddrc = ddrc
    )
  )

  // Create a dummy node where we can attach our silly TL port
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 64), // CKDUR: The maximum ID possible goes here.
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
