// See LICENSE

package uec.teehardware.opentitan.rv_core_ibex

import chisel3._
import chisel3.util.{log2Up}

import freechips.rocketchip.config.{Parameters, Config, Field}
import freechips.rocketchip.subsystem.{SystemBusKey, RocketTilesKey, RocketCrossingParams}
import freechips.rocketchip.devices.tilelink.{BootROMParams}
import freechips.rocketchip.diplomacy.{SynchronousCrossing, AsynchronousCrossing, RationalCrossing}
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

class WithNIbexCores(n: Int) extends Config(
  new WithNormalIbexSys ++
    new Config((site, here, up) => {
      case IbexTilesKey => {
        List.tabulate(n)(i => IbexTileParams(hartId = i))
      }
    })
)

/**
  * Setup default Ibex parameters.
  */
class WithNormalIbexSys extends Config((site, here, up) => {
  case SystemBusKey => up(SystemBusKey, site).copy(beatBytes = 4)
  case XLen => 32
  case MaxHartIdBits => log2Up(site(IbexTilesKey).size)
})
