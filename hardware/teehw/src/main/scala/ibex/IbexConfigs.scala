// See LICENSE

package uec.teehardware.ibex

import chisel3._
import chisel3.util.log2Up
import freechips.rocketchip.config.{Config, Field, Parameters}
import freechips.rocketchip.subsystem.{CacheBlockBytes, RocketCrossingParams, RocketTilesKey, SystemBusKey}
import freechips.rocketchip.devices.tilelink.BootROMParams
import freechips.rocketchip.diplomacy.{AsynchronousCrossing, RationalCrossing, SynchronousCrossing}
import freechips.rocketchip.rocket._
import freechips.rocketchip.tile._

case object IbexCrossingKey extends Field[Seq[RocketCrossingParams]](List(RocketCrossingParams()))

class WithIbexEnableICacheICC extends Config((site, here, up) => {
  case IbexTilesKey => up(IbexTilesKey) map (tile => tile.copy(ICacheECC = true))
})


class WithSecureIbex extends Config((site, here, up) => {
  case IbexTilesKey => up(IbexTilesKey, site) map { a =>
    a.copy(core = a.core.copy(
      SecureIbex = true
    ))
  }
})

class WithNIbexCores(n: Int) extends Config((site, here, up) => {
  case IbexTilesKey => {
    List.tabulate(n)(i => IbexTileParams(hartId = i))
  }
})

class WithNIbexSecureCores(n: Int) extends Config((site, here, up) => {
  case IbexTilesKey => {
    List.tabulate(n)(i => IbexTileParams(
      hartId = i,
      dcache = Some(DCacheParams(
        rowBits = site(SystemBusKey).beatBits,
        nSets = 256, // 16Kb scratchpad
        nWays = 1,
        nTLBEntries = 4,
        nMSHRs = 0,
        blockBytes = 4,
        scratch = Some(0x64300000L))),
      core = IbexCoreParams(SecureIbex = true)
    ))
  }
})

class WithNIbexSmallCacheSecureCores(n: Int) extends Config((site, here, up) => {
  case IbexTilesKey => {
    List.tabulate(n)(i => IbexTileParams(
      hartId = i,
      dcache = Some(DCacheParams(
        rowBits = site(SystemBusKey).beatBits,
        nSets = 32, // 2Kb scratchpad
        nWays = 1,
        nTLBEntries = 4,
        nMSHRs = 0,
        blockBytes = 4,
        scratch = Some(0x64300000L))),
      core = IbexCoreParams(SecureIbex = true)
    ))
  }
})
