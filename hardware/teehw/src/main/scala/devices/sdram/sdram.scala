package uec.teehardware.devices.sdram

import chisel3._
import chisel3.experimental.{Analog, IntParam, StringParam, attach}
import chisel3.util.{HasBlackBoxResource, RegEnable}
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.prci.{ClockGroup, ClockSinkDomain}
import freechips.rocketchip.subsystem.{Attachable, BaseSubsystem, MBUS, SBUS}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.HeterogeneousBag
import uec.teehardware.GenericIOLibraryParams

case class sdram_bb_cfg
(
  SDRAM_HZ: BigInt = 50000000,
  SDRAM_ADDR_W: Int = 24,
  SDRAM_COL_W: Int = 9,
  SDRAM_BANK_W: Int = 2,
  SDRAM_DQM_W: Int = 2,
  SDRAM_DQ_W: Int = 16,
  SDRAM_READ_LATENCY: Int  = 3
) {
  val SDRAM_MHZ = SDRAM_HZ/1000000
  val SDRAM_BANKS = 1 << SDRAM_BANK_W
  val SDRAM_ROW_W = SDRAM_ADDR_W - SDRAM_COL_W - SDRAM_BANK_W
  val SDRAM_REFRESH_CNT = 1 << SDRAM_ROW_W
  val SDRAM_START_DELAY = 100000 / (1000 / SDRAM_MHZ) // 100 uS
  val SDRAM_REFRESH_CYCLES = (64000*SDRAM_MHZ) / SDRAM_REFRESH_CNT-1
}

trait HasSDRAMIf{
  this: Bundle =>
  val cfg: sdram_bb_cfg
  val sdram_clk_o = Output(Bool())
  val sdram_cke_o = Output(Bool())
  val sdram_cs_o = Output(Bool())
  val sdram_ras_o = Output(Bool())
  val sdram_cas_o = Output(Bool())
  val sdram_we_o = Output(Bool())
  val sdram_dqm_o = Output(UInt(cfg.SDRAM_DQM_W.W))
  val sdram_addr_o = Output(UInt(cfg.SDRAM_ROW_W.W))
  val sdram_ba_o = Output(UInt(cfg.SDRAM_BANK_W.W))
  val sdram_data_o = Output(UInt(cfg.SDRAM_DQ_W.W))
  val sdram_data_i = Input(UInt(cfg.SDRAM_DQ_W.W))
  val sdram_drive_o = Output(Bool())
}

class SDRAMIf(val cfg: sdram_bb_cfg = sdram_bb_cfg()) extends Bundle with HasSDRAMIf

trait HasWishboneIf{
  this: Bundle =>
  val stb_i = Input(Bool())
  val we_i = Input(Bool())
  val sel_i = Input(UInt(4.W))
  val cyc_i = Input(Bool())
  val addr_i = Input(UInt(32.W))
  val data_i = Input(UInt(32.W))
  val data_o = Output(UInt(32.W))
  val stall_o = Output(Bool())
  val ack_o = Output(Bool())
}

class sdram(val cfg: sdram_bb_cfg) extends BlackBox (
  Map(
    "SDRAM_MHZ" -> IntParam(cfg.SDRAM_MHZ),
    "SDRAM_DATA_W" -> IntParam(cfg.SDRAM_DQ_W),
    "SDRAM_ADDR_W" -> IntParam(cfg.SDRAM_ADDR_W),
    "SDRAM_COL_W" -> IntParam(cfg.SDRAM_COL_W),
    "SDRAM_BANK_W" -> IntParam(cfg.SDRAM_BANK_W),
    "SDRAM_DQM_W" -> IntParam(cfg.SDRAM_DQM_W),
    "SDRAM_BANKS" -> IntParam(cfg.SDRAM_BANKS),
    "SDRAM_ROW_W" -> IntParam(cfg.SDRAM_ROW_W),
    "SDRAM_REFRESH_CNT" -> IntParam(cfg.SDRAM_REFRESH_CNT),
    "SDRAM_START_DELAY" -> IntParam(cfg.SDRAM_START_DELAY),
    "SDRAM_REFRESH_CYCLES" -> IntParam(cfg.SDRAM_REFRESH_CYCLES),
    "SDRAM_READ_LATENCY" -> IntParam(cfg.SDRAM_READ_LATENCY)
  )
) with HasBlackBoxResource {
  val io = IO(new SDRAMIf(cfg) with HasWishboneIf {
    val clk_i = Input(Clock())
    val rst_i = Input(Bool())
  })
  addResource("/sdram/sdram.v")
}

// Periphery

case class SDRAMConfig // Periphery Config
(
  address: BigInt,
  sdcfg: sdram_bb_cfg = sdram_bb_cfg()
) {
  val size: BigInt = (1 << sdcfg.SDRAM_ADDR_W) * sdcfg.SDRAM_DQ_W / 8
  //0x2000000L, // 32Mb (256Mbits)
  //0x4000000L, // 64Mb (512Mbits)
}

class SDRAM(cfg: SDRAMConfig, blockBytes: Int, beatBytes: Int)(implicit p: Parameters) extends LazyModule with HasClockDomainCrossing{

  val device = new MemoryDevice
  val tlcfg = TLSlaveParameters.v1(
    address             = AddressSet.misaligned(cfg.address, cfg.size),
    resources           = device.reg,
    regionType          = RegionType.UNCACHED, // cacheable
    executable          = true,
    supportsGet         = TransferSizes(1, 4), // 1, 128
    supportsPutFull     = TransferSizes(1, 4),
    supportsPutPartial  = TransferSizes(1, 4),
    fifoId              = Some(0)
  )
  val tlportcfg = TLSlavePortParameters.v1(
    managers = Seq(tlcfg),
    beatBytes = 4
  )
  val sdramnode = TLManagerNode(Seq(tlportcfg))
  val node = TLBuffer()

  // Create the IO node, and stop trying to get something from elsewhere
  val ioNode = BundleBridgeSource(() => (new SDRAMIf(cfg.sdcfg)).cloneType)
  val port = InModuleBody { ioNode.bundle }

  // Connections of the node
  sdramnode := TLFragmenter(4, blockBytes) := TLWidthWidget(beatBytes) := node

  val controlXing: TLInwardClockCrossingHelper = this.crossIn(node)

  lazy val module = new LazyModuleImp(this) {
    val sdramimp = Module(new sdram(cfg.sdcfg))

    // Clock and Reset
    sdramimp.io.clk_i := clock
    sdramimp.io.rst_i := reset.asBool()

    // SDRAM side
    port.sdram_clk_o := sdramimp.io.sdram_clk_o
    port.sdram_cke_o := sdramimp.io.sdram_cke_o
    port.sdram_cs_o := sdramimp.io.sdram_cs_o
    port.sdram_ras_o := sdramimp.io.sdram_ras_o
    port.sdram_cas_o := sdramimp.io.sdram_cas_o
    port.sdram_we_o := sdramimp.io.sdram_we_o
    port.sdram_dqm_o := sdramimp.io.sdram_dqm_o
    port.sdram_addr_o := sdramimp.io.sdram_addr_o
    port.sdram_ba_o := sdramimp.io.sdram_ba_o
    sdramimp.io.sdram_data_i := port.sdram_data_i
    port.sdram_data_o := sdramimp.io.sdram_data_o
    port.sdram_drive_o := sdramimp.io.sdram_drive_o

    // WB side
    // Obtain the TL bundle
    val (tl_in, tl_edge) = sdramnode.in(0) // Extract the port from the node

    // Flow control
    val d_full = RegInit(false.B) // Transaction pending
    val d_valid_held = RegInit(false.B) // Held valid of D channel if not ready
    val d_size = Reg(UInt()) // Saved size
    val d_source = Reg(UInt()) // Saved source
    val d_hasData = Reg(Bool()) // Saved source

    // d_full logic: It is full if there is 1 transaction not completed
    // this is, of course, waiting until D responses for every individual A transaction
    when (tl_in.d.fire()) { d_full := false.B }
    when (tl_in.a.fire() && !sdramimp.io.stall_o) { d_full := true.B }

    // The D valid is the WB ack and the valid held (if D not ready yet)
    tl_in.d.valid := d_valid_held
    // Try to latch true the D valid held.
    // If we use fire for the "false" latch, it lasts at least 1 cycle
    val ack = (sdramimp.io.ack_o)
    when(ack) { d_valid_held := true.B }
    when(tl_in.d.fire()) { d_valid_held := false.B }

    // The A ready should be 1 only if there is no transaction
    tl_in.a.ready := !d_full && !sdramimp.io.stall_o

    // hasData helds if there is a write transaction
    val hasData = tl_edge.hasData(tl_in.a.bits)

    // Response data to D
    val d_data = RegEnable(sdramimp.io.data_o, ack)

    // Save the size and the source from the A channel for the D channel
    when (tl_in.a.fire()) {
      d_size   := tl_in.a.bits.size
      d_source := tl_in.a.bits.source
      d_hasData := hasData
    }

    // Response characteristics
    tl_in.d.bits := tl_edge.AccessAck(d_source, d_size, d_data)
    tl_in.d.bits.opcode := Mux(d_hasData, TLMessages.AccessAck, TLMessages.AccessAckData)

    // Connections to the wb transactions
    sdramimp.io.stb_i := tl_in.a.valid & !d_full // We trigger the transaction only here
    sdramimp.io.cyc_i := tl_in.a.valid & !d_full // We trigger the transaction only here
    sdramimp.io.addr_i := tl_in.a.bits.address
    sdramimp.io.data_i := tl_in.a.bits.data
    sdramimp.io.we_i := hasData // Is write?
    sdramimp.io.sel_i := tl_in.a.bits.mask

    // Tie off unused channels
    tl_in.b.valid := false.B
    tl_in.c.ready := true.B
    tl_in.e.ready := true.B
  }
}

object SDRAMObject {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }
}

case class SDRAMAttachParams
(
  device: SDRAMConfig,
  controlXType: ClockCrossingType = AsynchronousCrossing()
){

  def attachTo(where: Attachable)(implicit p: Parameters): SDRAM = where {
    val name = s"sdram_${SDRAMObject.nextId()}"
    val mbus = where.locateTLBusWrapper(MBUS)
    val sdramClockDomainWrapper = LazyModule(new ClockSinkDomain(take = None))
    val sdram = sdramClockDomainWrapper { LazyModule(new SDRAM(device, mbus.blockBytes, mbus.beatBytes)) }
    sdram.suggestName(name)

    mbus.coupleTo(s"mem_${name}") { bus =>
      sdramClockDomainWrapper.clockNode := (controlXType match {
        case _: SynchronousCrossing =>
          mbus.dtsClk.map(_.bind(sdram.device))
          mbus.fixedClockNode
        case _: RationalCrossing =>
          mbus.clockNode
        case _: AsynchronousCrossing =>
          val sdramClockGroup = ClockGroup()
          sdramClockGroup := where.asyncClockGroupsNode
          sdramClockGroup
      })

      sdram.controlXing(controlXType) := bus
    }

    sdram
  }
}

case object SDRAMKey extends Field[Seq[SDRAMConfig]](Nil)

trait HasSDRAM { this: BaseSubsystem =>
  val sdramNodes = p(SDRAMKey).map { ps =>
    SDRAMAttachParams(ps).attachTo(this).ioNode.makeSink()
  }
}

trait HasSDRAMModuleImp extends LazyModuleImp {
  val outer: HasSDRAM
  val sdramio = outer.sdramNodes.zipWithIndex.map { case(n,i) => n.makeIO()(ValName(s"sdram_$i")) }
}

class SDRAMIO(val cfg: sdram_bb_cfg = sdram_bb_cfg()) extends Bundle  {
  val sdram_clk_o = Analog(1.W)
  val sdram_cke_o = Analog(1.W)
  val sdram_cs_o = Analog(1.W)
  val sdram_ras_o = Analog(1.W)
  val sdram_cas_o = Analog(1.W)
  val sdram_we_o = Analog(1.W)
  val sdram_dqm_o = Vec(cfg.SDRAM_DQM_W, Analog(1.W))
  val sdram_addr_o = Vec(cfg.SDRAM_ROW_W, Analog(1.W))
  val sdram_ba_o = Vec(cfg.SDRAM_BANK_W, Analog(1.W))
  val sdram_data = Vec(cfg.SDRAM_DQ_W, Analog(1.W))
}

trait HasSDRAMChipImp extends RawModule {
  implicit val p: Parameters
  val clock: Clock
  val reset: Bool
  val IOGen: GenericIOLibraryParams
  val system: HasSDRAMModuleImp

  val sdramio = system.sdramio.map { syssdramio =>
    val sdramio = IO(new SDRAMIO(syssdramio.cfg))

    val sdram_clk_o = IOGen.gpio()
    sdram_clk_o.suggestName("sdram_clk_o")
    attach(sdram_clk_o.pad, sdramio.sdram_clk_o)
    sdram_clk_o.ConnectAsOutput(syssdramio.sdram_clk_o)
    val sdram_cke_o = IOGen.gpio()
    sdram_cke_o.suggestName("sdram_cke_o")
    attach(sdram_cke_o.pad, sdramio.sdram_cke_o)
    sdram_cke_o.ConnectAsOutput(syssdramio.sdram_cke_o)
    val sdram_cs_o = IOGen.gpio()
    sdram_cs_o.suggestName("sdram_cs_o")
    attach(sdram_cs_o.pad, sdramio.sdram_cs_o)
    sdram_cs_o.ConnectAsOutput(syssdramio.sdram_cs_o)
    val sdram_ras_o = IOGen.gpio()
    sdram_ras_o.suggestName("sdram_ras_o")
    attach(sdram_ras_o.pad, sdramio.sdram_ras_o)
    sdram_ras_o.ConnectAsOutput(syssdramio.sdram_ras_o)
    val sdram_cas_o = IOGen.gpio()
    sdram_cas_o.suggestName("sdram_cas_o")
    attach(sdram_cas_o.pad, sdramio.sdram_cas_o)
    sdram_cas_o.ConnectAsOutput(syssdramio.sdram_cas_o)
    val sdram_we_o = IOGen.gpio()
    sdram_we_o.suggestName("sdram_we_o")
    attach(sdram_we_o.pad, sdramio.sdram_we_o)
    sdram_we_o.ConnectAsOutput(syssdramio.sdram_we_o)
    (syssdramio.sdram_dqm_o.asBools zip sdramio.sdram_dqm_o).zipWithIndex.foreach{ case((a, b), i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"sdram_dqm_o_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }
    (syssdramio.sdram_addr_o.asBools zip sdramio.sdram_addr_o).zipWithIndex.foreach{ case((a, b), i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"sdram_addr_o_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }
    (syssdramio.sdram_ba_o.asBools zip sdramio.sdram_ba_o).zipWithIndex.foreach{ case((a, b), i) =>
      val pad = IOGen.gpio()
      pad.suggestName(s"sdram_ba_o_${i}")
      attach(pad.pad, b)
      pad.ConnectAsOutput(a)
    }
    syssdramio.sdram_data_i := VecInit((syssdramio.sdram_data_o.asBools zip sdramio.sdram_data).zipWithIndex.map{
      case((a, b), i) =>
        val pad = IOGen.gpio()
        pad.suggestName(s"sdram_data_${i}")
        attach(pad.pad, b)
        pad.ConnectTristate(a, syssdramio.sdram_drive_o)
    }).asUInt

    sdramio
  }
}
