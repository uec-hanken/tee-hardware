package uec.teehardware

import chisel3._
import chisel3.util._
import chisel3.experimental._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.{LogicalModuleTree, SubsystemLogicalTreeNode}
import freechips.rocketchip.interrupts._
import freechips.rocketchip.prci.{ClockGroupAggregator, ClockGroupEphemeralNode, ClockGroupSourceNode, ClockGroupSourceParameters}
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sifive.blocks.devices.pinctrl._
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.fpgashells.clocks._
import testchipip.{SerialIO, TLDesser}
import uec.teehardware.devices.clockctrl._
//import uec.teehardware.vc707mig32._

// The subssytem definition
class FPGAMiniSubSystem(val idBits: Int = 6)(implicit p :Parameters) extends LazyModule with Attachable with BindingScope {
  // Create the Bus
  p(ExtBusKey).instantiate(this, EXTBUS)
  val extbus = locateTLBusWrapper(EXTBUS)

  // Create some system locations
  implicit val asyncClockGroupsNode = ClockGroupEphemeralNode()(ValName("clock_sources"))
  val ibus = LazyModule(new InterruptBusWrapper)

  // Create the desser
  val params = Seq(TLMasterParameters.v1(
    name = "tl-desser",
    sourceId = IdRange(0, 1 << idBits)))
  val desser = LazyModule(new TLDesser(p(ExtSerBus).head.serWidth, params, true))
  // Attach nodes
  if(p(ExtSerBus).head.master.beatBytes != p(ExtBusKey).beatBytes)
    extbus.crossInHelper(p(CbusToExtBusXTypeKey))(p) :=
      TLWidthWidget(p(ExtSerBus).head.master.beatBytes) :=
      desser.node
  else
    extbus.crossInHelper(p(CbusToExtBusXTypeKey))(p) :=
      desser.node


  Seq(EXTBUS).foreach { loc =>
    tlBusWrapperLocationMap.lift(loc).foreach { _.clockGroupNode := asyncClockGroupsNode }
  }

  // Create a nexus node for accepting all Ints.
  val intnode: IntNexusNode = IntNexusNode(
    sourceFn = { _ => IntSourcePortParameters(Seq(IntSourceParameters(1))) },
    sinkFn   = { _ => IntSinkPortParameters(Seq(IntSinkParameters())) },
    outputRequiresInput = false,
    inputRequiresOutput = false)
  intnode :=* ibus.toPLIC
  ibus.clockNode := extbus.fixedClockNode

  // Report the ibus, just to report that is lost
  def nDevices: Int = intnode.edges.in.map(_.source.num).sum
  lazy val sources = intnode.edges.in.map(_.source)
  lazy val flatSources = (sources zip sources.map(_.num).scanLeft(0)(_ + _).init).flatMap {
    case (s, o) => s.sources.map(z => z.copy(range = z.range.offset(o)))
  }

  // Collect information for use in DTS
  lazy val topManagers = tlBusWrapperLocationMap(EXTBUS).unifyManagers
  ResourceBinding {
    val managers = topManagers
    val max = managers.flatMap(_.address).map(_.max).max
    val width = ResourceInt((log2Ceil(max)+31) / 32)
    val model = "uec,fpga-ext-soc"
    val compat = Nil
    val devCompat = (model +: compat).map(s => ResourceString(s + "-dev"))
    val socCompat = (model +: compat).map(s => ResourceString(s + "-soc"))
    devCompat.foreach { Resource(ResourceAnchors.root, "compat").bind(_) }
    socCompat.foreach { Resource(ResourceAnchors.soc,  "compat").bind(_) }
    Resource(ResourceAnchors.root, "model").bind(ResourceString(model))
    Resource(ResourceAnchors.root, "width").bind(width)
    Resource(ResourceAnchors.soc,  "width").bind(width)

    managers.foreach { case manager =>
      val value = manager.toResource
      manager.resources.foreach { case resource =>
        resource.bind(value)
      }
    }
  }

  lazy val logicalTreeNode = new SubsystemLogicalTreeNode()

  tlBusWrapperLocationMap.values.foreach { bus =>
    val builtIn = bus.builtInDevices
    builtIn.errorOpt.foreach { error =>
      LogicalModuleTree.add(logicalTreeNode, error.logicalTreeNode)
    }
    builtIn.zeroOpt.foreach { zero =>
      LogicalModuleTree.add(logicalTreeNode, zero.logicalTreeNode)
    }
  }

  // Build the DTS. Just a mock DTS
  lazy val dts = DTS(bindingTree)

  // Create the module
  lazy val module = new FPGAMiniSubSystemModule(this)
}

class FPGAMiniSubSystemModule[+L <: FPGAMiniSubSystem](_outer: L) extends LazyModuleImp(_outer) {
  val outer = _outer
  val serport = IO(new SerialIO(p(ExtSerBus).head.serWidth))

  // Connect the serport
  serport <> outer.desser.module.io.ser.head

  // Report the ibus, just to report that is lost
  println(s"Interrupt map lost in FPGA piece, ${outer.nDevices} interrupts):")
  outer.flatSources.foreach { s =>
    // +1 because 0 is reserved, +1-1 because the range is half-open
    println(s"  [${s.range.start+1}, ${s.range.end}] => ${s.name}")
  }
  println("")

  // Report the bus connections
  println("EXT: Show DTS")
  println(outer.dts)

  private val mapping: Seq[AddressMapEntry] = Annotated.addressMapping(this, {
    outer.collectResourceAddresses.groupBy(_._2).toList.flatMap { case (key, seq) =>
      AddressRange.fromSets(key.address).map { r => AddressMapEntry(r, key.permissions, seq.map(_._1)) }
    }.sortBy(_.range)
  })

  Annotated.addressMapping(this, mapping)

  println("EXT: Generated Address Map")
  mapping.map(entry => println(entry.toString((outer.extbus.busView.bundle.addressBits-1)/4 + 1)))
  println("")

  // Compact the interrupts... and do absolutely nothing
  val interrupts = outer.intnode.in.flatMap { case (i, e) => i.take(e.source.num) }
}

class FPGAMiniSystem(idBits: Int = 6)(implicit p :Parameters) extends FPGAMiniSubSystem(idBits)(p)
  with HasPeripheryClockCtrl {

  // Create the ClockGroupSource (only 1...)
  val clockGroup = ClockGroupSourceNode(List.fill(1) { ClockGroupSourceParameters() })
  // Create the Aggregator. This will just take the SourceNode, then just replicate it in a Nexus
  val clocksAggregator = LazyModule(new ClockGroupAggregator("allClocks")).node
  // Connect it to the asyncClockGroupsNode, with the aggregator
  asyncClockGroupsNode :*= clocksAggregator := clockGroup

  override lazy val module = new FPGAMiniSystemModule(this)
}

class FPGAMiniSystemModule[+L <: FPGAMiniSystem](_outer: L) extends FPGAMiniSubSystemModule(_outer)
  with HasPeripheryClockCtrlModuleImp {

  // Connect the clock to the clockgroup (all of them)
  outer.clockGroup.out.flatMap(_._1.member.data).foreach { o =>
    o.clock := clock
    o.reset := reset
  }
}

// The dummy without the Xilinx clock
class FPGAMiniSystemDummy(idBits: Int = 6)(implicit p :Parameters) extends FPGAMiniSubSystem(idBits)(p)
  with HasPeripheryClockCtrlDummy {

  // Create the ClockGroupSource (only 1...)
  val clockGroup = ClockGroupSourceNode(List.fill(1) { ClockGroupSourceParameters() })
  // Create the Aggregator. This will just take the SourceNode, then just replicate it in a Nexus
  val clocksAggregator = LazyModule(new ClockGroupAggregator("allClocks")).node
  // Connect it to the asyncClockGroupsNode, with the aggregator
  asyncClockGroupsNode :*= clocksAggregator := clockGroup

  override lazy val module = new FPGAMiniSystemDummyModule(this)
}

class FPGAMiniSystemDummyModule[+L <: FPGAMiniSystemDummy](_outer: L) extends FPGAMiniSubSystemModule(_outer) {

  // Connect the clock to the clockgroup (all of them)
  outer.clockGroup.out.flatMap(_._1.member.data).foreach { o =>
    o.clock := clock
    o.reset := reset
  }
}