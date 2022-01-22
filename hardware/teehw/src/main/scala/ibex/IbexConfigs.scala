// See LICENSE

package uec.teehardware.ibex

import chisel3._
import chisel3.util.log2Up
import freechips.rocketchip.config.{Config, Field, Parameters}
import freechips.rocketchip.subsystem.{CacheBlockBytes, InSubsystem, RocketCrossingParams, RocketTilesKey, SystemBusKey, TilesLocated}
import freechips.rocketchip.devices.tilelink.BootROMParams
import freechips.rocketchip.diplomacy.{AsynchronousCrossing, RationalCrossing, SynchronousCrossing}
import freechips.rocketchip.rocket._
import freechips.rocketchip.tile._

class WithIbexSynthesizedNoICache extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => up(TilesLocated(InSubsystem), site) map {
    case i: IbexTileAttachParams => i.copy(tileParams = i.tileParams.copy(core = i.tileParams.core.copy(
      Synth = true, SynthFlavor = "IbexSecureNoICache"
    )))
    case other => other
  }
})

class WithIbexSynthesized extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => up(TilesLocated(InSubsystem), site) map {
    case i: IbexTileAttachParams => i.copy(tileParams = i.tileParams.copy(core = i.tileParams.core.copy(
      Synth = true, SynthFlavor = "IbexSecureDefault"
    )))
    case other => other
  }
})

class WithIbexEnableICacheICC extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => up(TilesLocated(InSubsystem), site) map {
    case i: IbexTileAttachParams => i.copy(tileParams = i.tileParams.copy(ICacheECC = true))
    case other => other
  }
})

class WithSecureIbex extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => up(TilesLocated(InSubsystem), site) map {
    case i: IbexTileAttachParams => i.copy(tileParams = i.tileParams.copy(core = i.tileParams.core.copy(SecureIbex = true)))
    case other => other
  }
})

class WithNIbexCores(n: Int, overrideIdOffset: Option[Int] = None) extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => {
    val prev = up(TilesLocated(InSubsystem), site)
    val idOffset = overrideIdOffset.getOrElse(prev.size)
    (0 until n).map { i =>
      IbexTileAttachParams(
        tileParams = IbexTileParams(hartId = i + idOffset),
        crossingParams = RocketCrossingParams()
      )} ++ prev
  }
})

class WithNIbexSecureCores(n: Int, overrideIdOffset: Option[Int] = None) extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => {
    val prev = up(TilesLocated(InSubsystem), site)
    val idOffset = overrideIdOffset.getOrElse(prev.size)
    (0 until n).map { i =>
      IbexTileAttachParams(
        tileParams = IbexTileParams(
          hartId = i + idOffset,
          dcache = Some(DCacheParams(
            rowBits = site(SystemBusKey).beatBits,
            nSets = 256, // 16Kb scratchpad
            nWays = 1,
            nTLBWays = 4,
            nMSHRs = 0,
            blockBytes = 4,
            scratch = Some(0x64300000L))
          ),
          core = IbexCoreParams(SecureIbex = true)
        ),
        crossingParams = RocketCrossingParams()
    )} ++ prev
  }
})

class WithNIbexSmallCacheSecureCores(n: Int, overrideIdOffset: Option[Int] = None) extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => {
    val prev = up(TilesLocated(InSubsystem), site)
    val idOffset = overrideIdOffset.getOrElse(prev.size)
    (0 until n).map { i =>
      IbexTileAttachParams(
        tileParams = IbexTileParams(
          hartId = i + idOffset,
          icache = None, // No icache
          dcache = Some(DCacheParams(
            rowBits = site(SystemBusKey).beatBits,
            nSets = 32, // 2Kb scratchpad
            nWays = 1,
            nTLBWays = 4,
            nMSHRs = 0,
            blockBytes = 4,
            scratch = Some(0x64300000L))
          ),
          core = IbexCoreParams(SecureIbex = true, nPMPs = 1, RV32M = 1)
        ),
        crossingParams = RocketCrossingParams()
      )} ++ prev
  }
})
