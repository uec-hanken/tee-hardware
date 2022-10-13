//******************************************************************************
// Copyright (c) 2019 - 2019, The Regents of the University of California (Regents).
// All Rights Reserved. See LICENSE and LICENSE.SiFive for license details.
// This is the same "Subsystems.scala" file, but we add the Ibex core
//------------------------------------------------------------------------------

package uec.teehardware

import chisel3._
import freechips.rocketchip.config.{Field, Parameters}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.diplomaticobjectmodel.model.OMInterrupt
import freechips.rocketchip.tile._
import freechips.rocketchip.tilelink.{TLWidthWidget, _}
import freechips.rocketchip.util._
import freechips.rocketchip.subsystem._
import boom.common.BoomTile
import uec.teehardware.ibex._
import testchipip.DromajoHelper
import uec.teehardware.devices.opentitan.alert._
import uec.teehardware.devices.opentitan.nmi_gen._
import uec.teehardware.devices.sifiveblocks.DebugJTAGOnlyModuleImp

case object WithAlertAndNMI extends Field[Boolean](false)

trait HasTEEHWTiles extends HasTiles { this: TEEHWBaseSubsystem =>
  def coreMonitorBundles = tiles.map {
    case r: RocketTile => r.module.core.rocketImpl.coreMonitorBundle
    case b: BoomTile => b.module.core.coreMonitorBundle
  }.toList

  // Escalation node attachmenet (TODO)
  tiles.foreach {
    case i: IbexTile => i.escnode := escnode
    case _ => /*Nothing*/
  }

  // If the Ibex is not present, esc be connected to nothing
  val IbexExists = tileAttachParams.exists { case _: IbexTileParams => true; case _ => false }
  if(!IbexExists) EscEmpty.applySink := escnode

  val cryptobus = if(p(IsCryptoBus)) {
    // Creation of the Crypto-core bus
    // We do not use the regular instantiate() and connect() functions from the configuration (see BaseSubsystem.scala:63)
    // The reason is to modify the bus tree as little as possible
    p(CryptoBusKey).instantiate(this, CRYPTOBUS)
    val cryptobus = locateTLBusWrapper(CRYPTOBUS)
    //cryptobus.clockGroupNode := asyncMux(p(CbusToCryptoBusXTypeKey), this.asyncClockGroupsNode, locateTLBusWrapper(CBUS).clockGroupNode) // BusWrapper.scala:182
    cryptobus.crossInHelper(p(CbusToCryptoBusXTypeKey))(p) :=
      TLWidthWidget(locateTLBusWrapper(CBUS).beatBytes) :=
      locateTLBusWrapper(CBUS).outwardNode // BusWrapper.scala:125 & BusWrapper.scala:187

    cryptobus.clockGroupNode := asyncClockGroupsNode

    cryptobus
  } else {
    pbus
  }

  // Relying on [[TLBusWrapperConnection]].driveClockFromMaster for
  // bus-couplings that are not asynchronous strips the bus name from the sink
  // ClockGroup. This makes it impossible to determine which clocks are driven
  // by which bus based on the member names, which is problematic when there is
  // a rational crossing between two buses. Instead, provide all bus clocks
  // directly from the asyncClockGroupsNode in the subsystem to ensure bus
  // names are always preserved in the top-level clock names.
  //
  // For example, using a RationalCrossing between the Sbus and Cbus, and
  // driveClockFromMaster = Some(true) results in all cbus-attached device and
  // bus clocks to be given names of the form "subsystem_sbus_[0-9]*".
  // Conversly, if an async crossing is used, they instead receive names of the
  // form "subsystem_cbus_[0-9]*". The assignment below provides the latter names in all cases.
  // NOTE: This relies on the fact of using HierarchicalMulticlockBusTopologyParams inside of the
  // TLNetworkTopologyLocated Key (See CustomBusTopologies.scala:38 & 56 inside chipyard)
  Seq(PBUS, FBUS, MBUS, CBUS).foreach { loc =>
    tlBusWrapperLocationMap.lift(loc).foreach { _.clockGroupNode := asyncClockGroupsNode }
  }

  // Connect the global reset vector
  // In BootROM scenario: 0x20000000
  // In QSPI & sim scenarios: 0x10040
  // NOTE: I do not get the people of RISCV. BootROM ONLY gets to assign this reset vector
  // Then, to put it, needs to be buried deep in the resources.
  // What if I want to modify it from configuration? Only the glorified Rocket BootROM can? This is BS
  // This code is copied from BootROM.scala
  val maskROMResetVectorSourceNode = BundleBridgeSource[UInt]()
  tileResetVectorNexusNode := maskROMResetVectorSourceNode

  def getOMInterruptDevice(resourceBindingsMap: ResourceBindingsMap): Seq[OMInterrupt] = Nil
}

trait HasTEEHWTilesModuleImp extends LazyModuleImp with DebugJTAGOnlyModuleImp {
  val outer: HasTEEHWTiles with HasTileInterruptSources with HasTileInputConstants

  // NOTE: Just a copy of HasTilesModuleImp
  val reset_vector = outer.tileResetVectorIONodes.zipWithIndex.map { case (n, i) => n.makeIO(s"reset_vector_$i") }
  val tile_hartids = outer.tileHartIdIONodes.zipWithIndex.map { case (n, i) => n.makeIO(s"tile_hartids_$i") }

  val meip = if(outer.meipNode.isDefined) Some(IO(Input(Vec(outer.meipNode.get.out.size, Bool())))) else None
  meip.foreach { m =>
    m.zipWithIndex.foreach{ case (pin, i) =>
      (outer.meipNode.get.out(i)._1)(0) := pin
    }
  }
  val seip = if(outer.seipNode.isDefined) Some(IO(Input(Vec(outer.seipNode.get.out.size, Bool())))) else None
  seip.foreach { s =>
    s.zipWithIndex.foreach{ case (pin, i) =>
      (outer.seipNode.get.out(i)._1)(0) := pin
    }
  }
  val nmi = outer.tiles.zip(outer.tileNMIIONodes).zipWithIndex.map { case ((tile, n), i) => tile.tileParams.core.useNMI.option(n.makeIO(s"nmi_$i")) }
  // End: Just a copy of HasTilesModuleImp

  // TODO: The reset_vector and tile_hartids are exported as IO.
  // create file with core params
  ElaborationArtefacts.add("""core.config""", outer.tiles.map(x => x.module.toString).mkString("\n"))
  // Generate C header with relevant information for Dromajo
  // This is included in the `dromajo_params.h` header file
  DromajoHelper.addArtefacts(InSubsystem)

  // NOTE: Continuation of the maskROM reset vector
  outer.maskROMResetVectorSourceNode.bundle := p(TEEHWResetVector).U
}

// The base subsystem for the TEE system. Just contains the alerts for now
abstract class TEEHWBaseSubsystem(implicit p: Parameters) extends BaseSubsystem {
  override val module: TEEHWBaseSubsystemModuleImp[TEEHWBaseSubsystem]

  // The alert nexus
  val alertnode = AlertXbar.apply
  // The esc nexus
  val escnode = EscXbar.apply
}

abstract class TEEHWBaseSubsystemModuleImp[+L <: TEEHWBaseSubsystem](_outer: L) extends BaseSubsystemModuleImp(_outer) {
}

class TEEHWSubsystem(implicit p: Parameters) extends TEEHWBaseSubsystem
  with HasTEEHWTiles
  with HasPeripheryAlert
  with HasPeripheryNmiGen
{
  override lazy val module = new TEEHWSubsystemModuleImp(this)
}

class TEEHWSubsystemModuleImp[+L <: TEEHWSubsystem](_outer: L) extends TEEHWBaseSubsystemModuleImp(_outer)
  with HasTEEHWTilesModuleImp // Put the tiles in the System, not in the Subsystem
  with HasPeripheryAlertModuleImp
  with HasPeripheryNmiGenModuleImp
{
}
