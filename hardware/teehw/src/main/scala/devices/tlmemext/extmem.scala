package uec.teehardware.devices.tlmemext

import chisel3._
import chisel3.experimental.{Analog, attach}
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import uec.teehardware.{GET, PUT, GenericIOLibraryParams, TEEHWBaseSubsystem}
import freechips.rocketchip.subsystem.ExtMem
import freechips.rocketchip.util.{BundleMap, HeterogeneousBag}
import sifive.blocks.devices.pinctrl.EnhancedPin

trait HasTEEHWPeripheryExtMem {
  this: TEEHWBaseSubsystem =>

  // Main memory controller (TL memory controller)
  val memctl: Option[TLManagerNode] = p(ExtMem).map { A =>
    val memdevice = new MemoryDevice
    val mainMemParam = TLSlavePortParameters.v1(
      managers = Seq(TLSlaveParameters.v1(
        address = AddressSet.misaligned(A.master.base, A.master.size),
        resources = memdevice.reg,
        regionType = RegionType.UNCACHED, // cacheable
        executable = true,
        supportsGet = TransferSizes(1, mbus.blockBytes),
        supportsPutFull = TransferSizes(1, mbus.blockBytes),
        supportsPutPartial = TransferSizes(1, mbus.blockBytes),
        fifoId = Some(0),
        mayDenyPut = true,
        mayDenyGet = true
      )),
      beatBytes = A.master.beatBytes
    )
    val memTLNode = TLManagerNode(Seq(mainMemParam))
    val buffer = LazyModule(new TLBuffer)
    memTLNode := buffer.node := mbus.toDRAMController(Some("tl"))()
    memTLNode
  }
}

trait HasTEEHWPeripheryExtMemModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryExtMem

  // Main memory controller
  val mem_tl: Option[TLBundle] = outer.memctl.map { case memTLnode: TLManagerNode =>
    val mem_tl = IO(HeterogeneousBag.fromNode(memTLnode.in)).asInstanceOf[Seq[TLBundle]]
    (mem_tl zip memTLnode.in).foreach { case (io, (bundle, _)) => io <> bundle }
    mem_tl.head
  }
}

class TLBundleChipA(val params: TLBundleParameters) extends Bundle {
  val opcode  = Vec(3, Analog(1.W))
  val param   = Vec(List(TLAtomics.width, TLPermissions.aWidth, TLHints.width).max, Analog(1.W))
  val size    = Vec(params.sizeBits, Analog(1.W))
  val source  = Vec(params.sourceBits, Analog(1.W))
  val address = Vec(params.addressBits, Analog(1.W))
  val user    = BundleMap(params.requestFields) // TODO: Making this also pins in analog?
  val echo    = BundleMap(params.echoFields) // TODO: Making this also pins in analog?
  val mask    = Vec(params.dataBits/8, Analog(1.W))
  val data    = Vec(params.dataBits, Analog(1.W))
  val corrupt = Analog(1.W)
  val valid = Analog(1.W)
  val ready = Analog(1.W)
}

class TLBundleChipD(val params: TLBundleParameters) extends Bundle {
  val opcode  = Vec(3, Analog(1.W))
  val param   = Vec(TLPermissions.bdWidth, Analog(1.W))
  val size    = Vec(params.sizeBits, Analog(1.W))
  val source  = Vec(params.sourceBits, Analog(1.W))
  val sink    = Vec(params.sinkBits, Analog(1.W))
  val denied  = Analog(1.W)
  val user    = BundleMap(params.responseFields) // TODO: Making this also pins in analog?
  val echo    = BundleMap(params.echoFields) // TODO: Making this also pins in analog?
  val data    = Vec(params.dataBits, Analog(1.W))
  val corrupt = Analog(1.W)
  val valid = Analog(1.W)
  val ready = Analog(1.W)
}

class TLULChip(val params: TLBundleParameters) extends Bundle {
  val a = new TLBundleChipA(params)
  val d = new TLBundleChipD(params)

  def ConnectTLIn(bundle: TLBundle): Unit = {
    // A channel
    bundle.a.valid := GET(a.valid)
    PUT(bundle.a.ready, a.ready)
    bundle.a.bits.opcode := VecInit(a.opcode.map(GET(_))).asUInt
    bundle.a.bits.param := VecInit(a.param.map(GET(_))).asUInt
    bundle.a.bits.size := VecInit(a.size.map(GET(_))).asUInt
    bundle.a.bits.address := VecInit(a.address.map(GET(_))).asUInt
    bundle.a.bits.source := VecInit(a.source.map(GET(_))).asUInt
    bundle.a.bits.user := a.user
    bundle.a.bits.echo := a.echo
    bundle.a.bits.mask := VecInit(a.mask.map(GET(_))).asUInt
    bundle.a.bits.data := VecInit(a.data.map(GET(_))).asUInt
    bundle.a.bits.corrupt := GET(a.corrupt)

    // D channel
    PUT(bundle.d.valid, d.valid)
    bundle.d.ready := GET(d.ready)
    (bundle.d.bits.opcode.asBools zip d.opcode).foreach{case(a, b) => PUT(a, b)}
    (bundle.d.bits.param.asBools zip d.param).foreach{case(a, b) => PUT(a, b)}
    (bundle.d.bits.size.asBools zip d.size).foreach{case(a, b) => PUT(a, b)}
    (bundle.d.bits.source.asBools zip d.source).foreach{case(a, b) => PUT(a, b)}
    (bundle.d.bits.sink.asBools zip d.sink).foreach{case(a, b) => PUT(a, b)}
    PUT(bundle.d.bits.denied, d.denied)
    a.user := bundle.d.bits.user
    a.echo := bundle.d.bits.echo
    (bundle.d.bits.data.asBools zip d.data).foreach{case(a, b) => PUT(a, b)}
    PUT(bundle.d.bits.corrupt, d.corrupt)

    // B,C,E channels
    //bundle.b.bits := (new TLBundleB(TLparams)).fromBits(0.U)
    bundle.b.ready := true.B
    bundle.c.valid := false.B
    //bundle.c.bits := 0.U.asTypeOf(new TLBundleC(TLparams))
    bundle.e.valid := false.B
    //bundle.e.bits := 0.U.asTypeOf(new TLBundleE(TLparams))
  }
}

trait HasTEEHWPeripheryExtMemChipImp extends RawModule {
  implicit val p: Parameters
  val clock: Clock
  val reset: Bool
  val IOGen: GenericIOLibraryParams
  val system: HasTEEHWPeripheryExtMemModuleImp

  val tlparam = system.mem_tl.map{sysmem => sysmem.params}
  // A helper function for alterate the number of bits of the id
  def tlparamsOtherId(n: Int) = {
    tlparam.map{param =>
      param.copy(sourceBits = n)
    }
  }
  val mem_tl = system.mem_tl.map{sysmem =>
    val mem_tl = IO(new TLULChip(sysmem.params))

    val a_valid = IOGen.gpio()
    a_valid.suggestName("a_valid")
    attach(a_valid.pad, mem_tl.a.valid)
    a_valid.ConnectAsOutput(sysmem.a.valid)
    val a_ready = IOGen.gpio()
    a_ready.suggestName("a_ready")
    attach(a_ready.pad, mem_tl.a.ready)
    sysmem.a.ready := a_ready.ConnectAsInput()
    (sysmem.a.bits.opcode.asBools zip mem_tl.a.opcode).zipWithIndex.foreach{ case((a, b), i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"a_opcode_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }
    (sysmem.a.bits.param.asBools zip mem_tl.a.param).zipWithIndex.foreach{ case((a, b), i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"a_param_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }
    (sysmem.a.bits.size.asBools zip mem_tl.a.size).zipWithIndex.foreach{ case((a, b), i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"a_size_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }
    (sysmem.a.bits.source.asBools zip mem_tl.a.source).zipWithIndex.foreach{ case((a, b), i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"a_source_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }
    (sysmem.a.bits.address.asBools zip mem_tl.a.address).zipWithIndex.foreach{ case((a, b), i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"a_address_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }
    mem_tl.a.user := sysmem.a.bits.user
    mem_tl.a.echo := sysmem.a.bits.echo
    (sysmem.a.bits.mask.asBools zip mem_tl.a.mask).zipWithIndex.foreach{ case((a, b), i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"a_mask_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }
    (sysmem.a.bits.data.asBools zip mem_tl.a.data).zipWithIndex.foreach{ case((a, b), i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"a_data_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }
    val a_corrupt = IOGen.gpio()
    a_corrupt.suggestName("a_corrupt")
    attach(a_corrupt.pad, mem_tl.a.corrupt)
    a_corrupt.ConnectAsOutput(sysmem.a.bits.corrupt)

    val d_valid = IOGen.gpio()
    d_valid.suggestName("d_valid")
    attach(d_valid.pad, mem_tl.d.valid)
    sysmem.d.valid := d_valid.ConnectAsInput()
    val d_ready = IOGen.gpio()
    d_ready.suggestName("d_ready")
    attach(d_ready.pad, mem_tl.d.ready)
    d_ready.ConnectAsOutput(sysmem.d.ready)
    sysmem.d.bits.opcode := VecInit(mem_tl.d.opcode.zipWithIndex.map{ case(b, i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"d_opcode_${i}")
      attach(pad.pad, b)
      pad.ConnectAsInput()
    }).asUInt
    sysmem.d.bits.param := VecInit(mem_tl.d.param.zipWithIndex.map{ case(b, i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"d_param_${i}")
      attach(pad.pad, b)
      pad.ConnectAsInput()
    }).asUInt
    sysmem.d.bits.size := VecInit(mem_tl.d.size.zipWithIndex.map{ case(b, i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"d_size_${i}")
      attach(pad.pad, b)
      pad.ConnectAsInput()
    }).asUInt
    sysmem.d.bits.source := VecInit(mem_tl.d.source.zipWithIndex.map{ case(b, i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"d_source_${i}")
      attach(pad.pad, b)
      pad.ConnectAsInput()
    }).asUInt
    sysmem.d.bits.sink := VecInit(mem_tl.d.sink.zipWithIndex.map{ case(b, i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"d_sink_${i}")
      attach(pad.pad, b)
      pad.ConnectAsInput()
    }).asUInt
    val d_denied = IOGen.gpio()
    d_denied.suggestName("d_denied")
    attach(d_denied.pad, mem_tl.d.denied)
    sysmem.d.bits.denied := d_denied.ConnectAsInput()
    sysmem.d.bits.user := mem_tl.d.user
    sysmem.d.bits.echo := mem_tl.d.echo
    sysmem.d.bits.data := VecInit(mem_tl.d.data.zipWithIndex.map{ case(b, i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"d_data_${i}")
      attach(pad.pad, b)
      pad.ConnectAsInput()
    }).asUInt
    val d_corrupt = IOGen.gpio()
    d_corrupt.suggestName("d_corrupt")
    attach(d_corrupt.pad, mem_tl.d.corrupt)
    sysmem.d.bits.corrupt := d_corrupt.ConnectAsInput()

    mem_tl
  }
}
