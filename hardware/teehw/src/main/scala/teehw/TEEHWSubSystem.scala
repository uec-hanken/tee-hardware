//******************************************************************************
// Copyright (c) 2019 - 2019, The Regents of the University of California (Regents).
// All Rights Reserved. See LICENSE and LICENSE.SiFive for license details.
// This is the same "Subsystems.scala" file, but we add the Ibex core
//------------------------------------------------------------------------------

package uec.teehardware

import chisel3._
import chisel3.internal.sourceinfo.SourceInfo
import freechips.rocketchip.config.{Field, Parameters}
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.devices.debug.{HasPeripheryDebug, HasPeripheryDebugModuleImp}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.diplomaticobjectmodel.model.OMInterrupt
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.{LogicalModuleTree, RocketTileLogicalTreeNode}
import freechips.rocketchip.tile._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.util._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.amba.axi4._
import boom.common.{BoomCrossingKey, BoomTile, BoomTileParams, BoomTilesKey}
import ariane.{ArianeCrossingKey, ArianeTile, ArianeTileParams, ArianeTilesKey}
import uec.teehardware.ibex._
import testchipip.DromajoHelper
import uec.teehardware.devices.opentitan.alert._
import uec.teehardware.devices.opentitan.nmi_gen._

trait HasTEEHWTiles extends HasTiles
  with CanHavePeripheryPLIC
  with CanHavePeripheryCLINT
  with HasPeripheryDebug
  with HasPeripheryAlert
  with HasPeripheryNmiGen
{ this: TEEHWSubsystem =>

  val module: HasTEEHWTilesModuleImp

  protected val rocketTileParams = p(RocketTilesKey)
  protected val boomTileParams = p(BoomTilesKey)
  protected val arianeTileParams = p(ArianeTilesKey)
  protected val ibexTileParams = p(IbexTilesKey)

  // crossing can either be per tile or global (aka only 1 crossing specified)
  private val rocketCrossings = perTileOrGlobalSetting(p(RocketCrossingKey), rocketTileParams.size)
  private val boomCrossings = perTileOrGlobalSetting(p(BoomCrossingKey), boomTileParams.size)
  private val arianeCrossings = perTileOrGlobalSetting(p(ArianeCrossingKey), arianeTileParams.size)
  private val ibexCrossings = perTileOrGlobalSetting(p(IbexCrossingKey), ibexTileParams.size)

  val allTilesInfo = (rocketTileParams ++ boomTileParams ++ arianeTileParams ++ ibexTileParams) zip (rocketCrossings ++ boomCrossings ++ arianeCrossings ++ ibexCrossings)

  // Make a tile and wire its nodes into the system,
  // according to the specified type of clock crossing.
  // Note that we also inject new nodes into the tile itself,
  // also based on the crossing type.
  // This MUST be performed in order of hartid
  // There is something weird with registering tile-local interrupt controllers to the CLINT.
  // TODO: investigate why
  val tiles = allTilesInfo.sortWith(_._1.hartId < _._1.hartId).map {
    case (param, crossing) => {

      val tile = param match {
        case r: RocketTileParams => {
          LazyModule(new RocketTile(r, crossing, PriorityMuxHartIdFromSeq(rocketTileParams), logicalTreeNode))
        }
        case b: BoomTileParams => {
          LazyModule(new BoomTile(b, crossing, PriorityMuxHartIdFromSeq(boomTileParams), logicalTreeNode))
        }
        case a: ArianeTileParams => {
          LazyModule(new ArianeTile(a, crossing, PriorityMuxHartIdFromSeq(arianeTileParams), logicalTreeNode))
        }
        case i: IbexTileParams => {
          val t = LazyModule(new IbexTile(i, crossing, PriorityMuxHartIdFromSeq(ibexTileParams), logicalTreeNode))
          t.escnode := escnode
          t
        }
      }
      connectMasterPortsToSBus(tile, crossing)
      connectSlavePortsToCBus(tile, crossing)
      connectInterrupts(tile, debugOpt, clintOpt, plicOpt)

      tile
    }
  }

  // If the Ibex is not present, esc be connected to nothing
  val IbexExists = allTilesInfo.map(_._1).exists { case _: IbexTileParams => true; case _ => false }
  if(!IbexExists) EscEmpty.apply := escnode


  def coreMonitorBundles = tiles.map {
    case r: RocketTile => r.module.core.rocketImpl.coreMonitorBundle
    case b: BoomTile => b.module.core.coreMonitorBundle
  }.toList
}

trait HasTEEHWTilesModuleImp extends HasTilesModuleImp
  with HasPeripheryDebugModuleImp
  with HasPeripheryAlertModuleImp
  with HasPeripheryNmiGenModuleImp
{
  val outer: HasTEEHWTiles
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
{
  override lazy val module = new TEEHWSubsystemModuleImp(this)

  def getOMInterruptDevice(resourceBindingsMap: ResourceBindingsMap): Seq[OMInterrupt] = Nil
}

class TEEHWSubsystemModuleImp[+L <: TEEHWSubsystem](_outer: L) extends TEEHWBaseSubsystemModuleImp(_outer)
  with HasResetVectorWire
  with HasTEEHWTilesModuleImp
{
  tile_inputs.zip(outer.hartIdList).foreach { case(wire, i) =>
    wire.hartid := i.U
    wire.reset_vector := global_reset_vector
  }

  // create file with boom params
  ElaborationArtefacts.add("""core.config""", outer.tiles.map(x => x.module.toString).mkString("\n"))

  // Generate C header with relevant information for Dromajo
  // This is included in the `dromajo_params.h` header file
  DromajoHelper.addArtefacts
}
