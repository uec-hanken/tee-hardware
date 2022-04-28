// See LICENSE

package uec.teehardware.ibex

import java.lang.reflect.InvocationTargetException

import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, StringParam}

import scala.collection.mutable.ListBuffer
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.LogicalTreeNode
import freechips.rocketchip.rocket._
import freechips.rocketchip.subsystem.RocketCrossingParams
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.util._
import freechips.rocketchip.tile._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.prci.ClockSinkParameters
import uec.teehardware.devices.opentitan._
import uec.teehardware.devices.opentitan.nmi_gen._
import uec.teehardware.devices.opentitan.top_pkg._
import uec.teehardware.tile._

case class IbexCoreParams
(
  bootFreqHz: BigInt = BigInt(1000000000),
  nPMPs: Int = 1,
  pmpGranularity: Int = 0,
  useDebug: Boolean = true,
  nLocalInterrupts: Int = 0,
  nPerfCounters: Int = 0,
  RV32M: Int = 1, // "ibex_pkg::RV32MSlow",
  RV32B: Int = 0, // "ibex_pkg::RV32BNone",
  RegFile: Int = 0, // "ibex_pkg::RegFileFF",
  BranchTargetALU: Boolean = false,
  BranchPredictor: Boolean = false,
  WritebackStage: Boolean = false,
  SecureIbex: Boolean = false,
  PipeLine: Boolean = false,
  useRVE: Boolean = false,
  Synth: Boolean = false,
  SynthFlavor: String = "IbexSecureDefault"
) extends CoreParams {
  /* DO NOT CHANGE BELOW THIS */
  val useVM: Boolean = false // TODO: Check
  val useUser: Boolean = true // Checked
  val useSupervisor: Boolean = false // Checked
  val useAtomics: Boolean = false // Checked
  val useAtomicsOnlyForIO: Boolean = false // copied from Rocket
  val useCompressed: Boolean = true // Checked. Also forced
  override val useVector: Boolean = false // Checked
  val useSCIE: Boolean = false // TODO: What?
  val mulDiv: Option[MulDivParams] =
    if(RV32M == 0) None
    else if (RV32M == 3) Some(MulDivParams(1, 1, false, false, 1))
    else if (RV32M == 2) Some(MulDivParams(3, 3, false, false, 4))
    else if (RV32M == 1) Some(MulDivParams(8, 8, false, false, 8)) // TODO: Check
    else {
      throw new IllegalArgumentException(s"The following statement is not valid in RV32M parameters in Ibex (Did you forgot to append ibex_pkg::): ${RV32M}")
      None
    }
  val fpu: Option[FPUParams] = None // Checked
  val useNMI: Boolean = false // TODO: Check
  val nBreakpoints: Int = 0 // TODO: Check
  val useBPWatch: Boolean = false // TODO: What?
  val mcontextWidth: Int = 0 // TODO: Check
  val scontextWidth: Int = 0 // TODO: Check
  val haveBasicCounters: Boolean = true // Checked
  val haveFSDirty: Boolean = false // TODO: What?
  val misaWritable: Boolean = false // Checked
  val haveCFlush: Boolean = false // Checked
  val nL2TLBEntries: Int = 512 // TODO: At this point, I am afraid to ask, but maybe is related to the L2 cache observation from the processor
  val nL2TLBWays: Int = 1 // TODO: Same as above
  val mtvecInit: Option[BigInt] = Some(BigInt(1)) // Checked
  val mtvecWritable: Boolean = true // Checked
  val instBits: Int = if (useCompressed) 16 else 32
  val lrscCycles: Int = 80 // TODO: What?
  val decodeWidth: Int = 1 // TODO: Check
  val fetchWidth: Int = 1 // TODO: Check
  val retireWidth: Int = 2 // TODO: Check
  val nPTECacheEntries: Int = 0 // TODO: Check
  val useHypervisor: Boolean = false // TODO: Check
}

case class IbexTileAttachParams
(
  tileParams: IbexTileParams,
  crossingParams: RocketCrossingParams
) extends CanAttachTile {
  type TileType = IbexTile
  val lookup = PriorityMuxHartIdFromSeq(Seq(tileParams))
}

// IMPORTANT NOTE: For the full-version of this, please keep the TypeIbex. This is capable of extend the IbexTile
abstract class AnyIbexTileParams[TypeIbex <: IbexTile] extends InstantiableTileParams[TypeIbex] {
  val ICacheECC: Boolean
  val boundaryBuffers: Boolean
  val core: IbexCoreParams
}

case class IbexTileParams
(
  name: Option[String] = Some("ibex_tile"),
  hartId: Int = 0,
  core: IbexCoreParams = IbexCoreParams(),
  icache: Option[ICacheParams] = Some(ICacheParams()), // TODO: The existence alone is checked only. No visible way to configure sizes
  dcache: Option[DCacheParams] = None, // TODO: No actual dcache. We always check for the scratchpad
  ICacheECC: Boolean = false,
  boundaryBuffers: Boolean = false
) extends AnyIbexTileParams[IbexTile] {
  /* DO NOT CHANGE BELOW THIS */
  val btb: Option[BTBParams] = None // TODO: What?
  val beuAddr: Option[BigInt] = None // TODO: What?
  val blockerCtrlAddr: Option[BigInt] = None // TODO: What?
  val clockSinkParams: ClockSinkParameters = ClockSinkParameters() // TODO: Who?
  def instantiate(crossing: TileCrossingParamsLike, lookup: LookupByHartIdImpl)(implicit p: Parameters): IbexTile = {
    new IbexTile(this, crossing, lookup)
  }
}

class IbexTile private
(
  val ibexParams: AnyIbexTileParams[IbexTile],
  crossing: ClockCrossingType,
  lookup: LookupByHartIdImpl,
  q: Parameters
)
  extends BaseTile(ibexParams, crossing, lookup, q)
    with SinksExternalOptionalInterrupts
    with SourcesExternalNotifications
{
  /**
    * Setup parameters:
    * Private constructor ensures altered LazyModule.p is used implicitly
    */
  def this(params: AnyIbexTileParams[IbexTile], crossing: TileCrossingParamsLike, lookup: LookupByHartIdImpl)(implicit p: Parameters) =
    this(params, crossing.crossingType, lookup, p)

  val intOutwardNode = IntIdentityNode()
  val slaveNode = TLIdentityNode()
  val masterNode = visibilityNode

  tlOtherMastersNode := tlMasterXbar.node
  masterNode :=* tlOtherMastersNode
  DisableMonitors { implicit p => tlSlaveXbar.node :*= slaveNode }

  val cpuDevice: SimpleDevice = new SimpleDevice("cpu", Seq("lowRISC,ibex", "riscv")) {
    override def parent = Some( ResourceAnchors.cpus )
    override def describe(resources: ResourceBindings): Description = {
      val Description(name, mapping) = super.describe(resources)
      Description(name, mapping ++
        cpuProperties ++
        nextLevelCacheProperty ++
        tileProperties ++
        Map(
          "riscv,isa"            -> "rv32imc".asProperty
        ))
    }
  }

  def intcDevice = optIntDevice

  optIntResourceBinding(intcDevice)
  ResourceBinding {
    Resource(cpuDevice, "reg").bind(ResourceAddress(hartId))
  }

  // Create the escalaments
  val escnode = EscSinkNode(EscSinkPortSimple())

  override def makeMasterBoundaryBuffers(crossing: ClockCrossingType)(implicit p: Parameters) = crossing match {
    case _: RationalCrossing =>
      if (!ibexParams.boundaryBuffers) TLBuffer(BufferParams.none)
      else TLBuffer(BufferParams.none, BufferParams.flow, BufferParams.none, BufferParams.flow, BufferParams(1))
    case _ => TLBuffer(BufferParams.none)
  }

  override def makeSlaveBoundaryBuffers(crossing: ClockCrossingType)(implicit p: Parameters) = crossing match {
    case _: RationalCrossing =>
      if (!ibexParams.boundaryBuffers) TLBuffer(BufferParams.none)
      else TLBuffer(BufferParams.flow, BufferParams.none, BufferParams.none, BufferParams.none, BufferParams.none)
    case _ => TLBuffer(BufferParams.none)
  }

  override lazy val module = new IbexTileModuleImp(this)

  // Create the fixed TL nodes to connect the processor
  val iPortName = "ibex-imem-port-tl"
  val dPortName = "ibex-dmem-port-tl"
  val beatBytes = 4 // Because is always 32 bits

  val imemNode = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = iPortName,
        sourceId = IdRange(0, 2) // MAX_REQS(2) in rv_core_ibex
      )),
      requestFields = Seq(
        new tl_a_user_t_ExtraField()
      ),
      responseKeys = Seq(
        UIntExtra
      )
    )
  })

  val dmemNode =  TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = iPortName,
        sourceId = IdRange(0, 2) // MAX_REQS(2) in rv_core_ibex
      )),
      requestFields = Seq(
        tl_a_user_t_ExtraField()
      ),
      responseKeys = Seq(
        UIntExtra
      )
    )
  })

  val imemTap = TLIdentityNode()
  val dmemTap = TLIdentityNode()

  // Connect the ports to the bus
  (tlMasterXbar.node
    := imemTap
    := TLBuffer(3)
    := TLWidthWidget(beatBytes) // reduce size of TL
    := TLSourceShrinker(1)
    := TLFragmenter(cacheBlockBytes, beatBytes)
    := imemNode
    )
  (tlMasterXbar.node
    := dmemTap
    := TLBuffer(3)
    := TLWidthWidget(beatBytes) // reduce size of TL
    := TLSourceShrinker(1)
    := TLFragmenter(cacheBlockBytes, beatBytes)
    := dmemNode
    )

  def connectIbexInterrupts(debug: Bool, msip: Bool, mtip: Bool, meip: Bool) {
    val (interrupts, _) = intSinkNode.in(0)
    debug := interrupts(0)
    msip := interrupts(1)
    mtip := interrupts(2)
    meip := interrupts(3)
  }
}

class IbexTileModuleImp(outer: IbexTile) extends BaseTileModuleImp(outer){
  // annotate the parameters
  //Annotated.params(this, outer.ibexParams)

  require(p(SubsystemResetSchemeKey)  == ResetSynchronous,
    "Ibex only supports synchronous reset at this time")

  //require(p(XLen) == 32, "Ibex only suports RV32")

  val debugBaseAddr = BigInt(0x0) // CONSTANT: based on default debug module
  val debugSz = BigInt(0x1000) // CONSTANT: based on default debug module
  val tohostAddr = BigInt(0x80001000L) // CONSTANT: based on default sw (assume within extMem region)
  val fromhostAddr = BigInt(0x80001040L) // CONSTANT: based on default sw (assume within extMem region)


  // connect the ibex core
  val core = Module(new IbexBlackbox(
    PMPEnable = outer.ibexParams.core.nPMPs != 0,
    PMPGranularity = outer.ibexParams.core.pmpGranularity,
    PMPNumRegions = if(outer.ibexParams.core.nPMPs == 0) 1 else outer.ibexParams.core.nPMPs,
    MHPMCounterNum = outer.ibexParams.core.nPerfCounters,
    MHPMCounterWidth = 32,
    RV32E = outer.ibexParams.core.useRVE,
    RV32M = outer.ibexParams.core.RV32M,
    RV32B = outer.ibexParams.core.RV32B,
    RegFile = outer.ibexParams.core.RegFile,
    BranchTargetALU = outer.ibexParams.core.BranchTargetALU,
    WritebackStage = outer.ibexParams.core.WritebackStage,
    ICache = outer.ibexParams.icache.nonEmpty,
    ICacheECC = outer.ibexParams.ICacheECC,
    BranchPredictor = outer.ibexParams.core.BranchPredictor,
    DbgTriggerEn = true, // Always debugger
    SecureIbex = outer.ibexParams.core.SecureIbex,
    DmHaltAddr = debugBaseAddr + BigInt(0x800),
    DmExceptionAddr = debugBaseAddr + BigInt(0x808),
    PipeLine = outer.ibexParams.core.PipeLine,
    Synth = outer.ibexParams.core.Synth,
    SynthFlavor = outer.ibexParams.core.SynthFlavor
  ))

  core.io.clk_i := clock
  core.io.rst_ni := ~reset.asBool
  core.io.boot_addr_i := outer.resetVectorSinkNode.bundle
  core.io.hart_id_i := outer.hartIdSinkNode.bundle

  outer.connectIbexInterrupts(core.io.debug_req_i, core.io.irq_software_i, core.io.irq_timer_i, core.io.irq_external_i)

  // No trace
  outer.traceSourceNode.bundle := DontCare
  outer.traceSourceNode.bundle map (t => t.valid := false.B)

  // connect the TL interfaces
  outer.imemNode.out foreach {
    case (out, edgeOut) =>
      out.a <> core.io.tl_i.a
      core.io.tl_i.d <> out.d
      out.b.ready := true.B
      out.c.valid := false.B
      out.e.valid := false.B

      //assert(!(out.a.fire() && out.a.bits.opcode === TLMessages.PutPartialData), "It happened! In imem")
  }
  outer.dmemNode.out foreach {
    case (out, edgeOut) =>
      out.a <> core.io.tl_d.a
      core.io.tl_d.d <> out.d
      out.b.ready := true.B
      out.c.valid := false.B
      out.e.valid := false.B

      //assert(!(out.a.fire() && out.a.bits.opcode === TLMessages.PutPartialData), "It happened! In dmem")
  }

  // Escalaments connections
  val (esc, _) = outer.escnode.in(0)
  core.io.esc_tx_i := esc.esc_tx
  esc.esc_rx := core.io.esc_rx_o

  // Miscellaneous connections
  core.io.fetch_enable_i := true.B
  core.io.test_en_i := false.B // TODO Always enable clock gating. No Latches!
}
