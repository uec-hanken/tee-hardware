package uec.teehardware

import chisel3._
import chisel3.util.log2Up
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket._
import freechips.rocketchip.system._
import freechips.rocketchip.tile._
import boom.common._
import uec.teehardware.ibex._

// The number of gpios that we want as input
case object GPIOInKey extends Field[Int](8)

// Frequency
case object FreqKeyMHz extends Field[Double](100.0)

// Include the PCIe
case object IncludePCIe extends Field[Boolean](false)

// When external the DDR port, has to run at another freq
case object DDRPortOther extends Field[Boolean](false)

// Our own reset vector
case object TEEHWResetVector extends Field[Int](0x10040)

/**
  * Class to renumber BOOM + Rocket harts so that there are no overlapped harts
  * This fragment assumes Rocket tiles are numbered before BOOM tiles
  * Also makes support for multiple harts depend on Rocket + BOOM
  * Note: Must come after all harts are assigned for it to apply
  * NOTE2: The Ibex always goes last
  * NOTE3: Deprecated. Now the order of the invocation sets the hartId.
  */
/*class WithRenumberHartsWithIbex(rocketFirst: Boolean = false) extends Config((site, here, up) => {
  case RocketTilesKey => up(RocketTilesKey, site).zipWithIndex map { case (r, i) =>
    r.copy(hartId = i + (if(rocketFirst) 0 else up(BoomTilesKey, site).length))
  }
  case BoomTilesKey => up(BoomTilesKey, site).zipWithIndex map { case (b, i) =>
    b.copy(hartId = i + (if(rocketFirst) up(RocketTilesKey, site).length else 0))
  }
  case IbexTilesKey => up(IbexTilesKey, site).zipWithIndex map { case (b, i) =>
    b.copy(hartId = i + (up(BoomTilesKey, site).size + up(RocketTilesKey, site).size))
  }
  case MaxHartIdBits => log2Up(up(BoomTilesKey, site).size + up(RocketTilesKey, site).size + up(IbexTilesKey, site).size)
})*/

/* NOTE ABOUT CACHE SIZES
 Cache size = nSets * nWays * CacheBlockBytes
 nSets = (default) 64;
 nWays = (default) 4;
 CacheBlockBytes = (default) 64;
 => default cache size = 64 * 4 * 64 = 16KBytes
*/
class WithSmallCacheBigCore(n: Int, overrideIdOffset: Option[Int] = None) extends Config((site, here, up) => {
  case RocketTilesKey => {
    val prev = up(RocketTilesKey, site)
    val idOffset = overrideIdOffset.getOrElse(prev.size)
    val big = RocketTileParams(
      core = RocketCoreParams(mulDiv = Some(MulDivParams(
        mulUnroll = 8, mulEarlyOut = true, divEarlyOut = true)),
        nL2TLBEntries = 256),
      dcache = Some(DCacheParams(
        rowBits = site(SystemBusKey).beatBits,
        nSets = 16, nWays = 1, nMSHRs = 0,
        blockBytes = site(CacheBlockBytes))),
      icache = Some(ICacheParams(
        rowBits = site(SystemBusKey).beatBits,
        nSets = 16, nWays = 1,
        blockBytes = site(CacheBlockBytes))))
    List.tabulate(n)(i => big.copy(hartId = i + idOffset)) ++ prev
  }
})

// Chip Configs
class ChipConfig extends Config(
  // The rest of the configurations, which are not-movable
  new WithNExtTopInterrupts(0) ++
    new TEEHWPeripherals ++
    new WithJtagDTM ++
    new WithNoSubsystemDrivenClocks ++
    new WithDontDriveBusClocksFromSBus ++
    new WithCoherentBusTopology ++                                  // This adds a L2 cache ++ ++
    new chipyard.config.WithL2TLBs(entries = 1024) ++               // Will add the TLBs for the L2
    new freechips.rocketchip.subsystem.WithInclusiveCache(capacityKB = 512) ++ // And the actual L2 (Do not deceive! This is form sifive-cache)
    //new WithIncoherentBusTopology ++ // This was the previous one
    new BaseConfig().alter((site,here,up) => {
      case BootROMLocated(InSubsystem) => None // No BootROM.
      case SystemBusKey => up(SystemBusKey).copy(
        errorDevice = Some(BuiltInErrorDeviceParams(DevNullParams(
          Seq(AddressSet(0x4000, 0xfff)),
          maxAtomic=site(XLen)/8,
          maxTransfer=128,
          region = RegionType.TRACKED))))
      case PeripheryBusKey => up(PeripheryBusKey, site).copy(dtsFrequency =
        Some(BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt),
        errorDevice = None)
      case DTSTimebase => BigInt(1000000)
      case JtagDTMKey => new JtagDTMConfig (
        idcodeVersion = 2,      // 1 was legacy (FE310-G000, Acai).
        idcodePartNum = 0x000,  // Decided to simplify.
        idcodeManufId = 0x489,  // As Assigned by JEDEC to SiFive. Only used in wrappers / test harnesses.
        debugIdleCycles = 5)    // Reasonable guess for synchronization
      case FreqKeyMHz => 100.0
      //case MaxHartIdBits => log2Up(site(BoomTilesKey).size + site(RocketTilesKey).size + site(IbexTilesKey).size)
    }))

class MicroConfig extends Config(
  new WithNExtTopInterrupts(0) ++
    new TEEHWPeripherals ++
    new WithJtagDTM ++
    new WithNoSubsystemDrivenClocks ++
    new WithDontDriveBusClocksFromSBus ++
    //new chipyard.config.WithBroadcastManager ++ // An utility from chipyard that forces the broadcast manager (Overkill?)
    new WithCoherentBusTopology ++ // Will add the L2, but The L2 will be a broadcast
    // new freechips.rocketchip.subsystem.WithInclusiveCache(capacityKB = 128) ++ // As long as you do not use this
    // new chipyard.config.WithL2TLBs(entries = 256) ++               // Will add the TLBs for the L2, but I wonder if we need this in micro
    //new WithIncoherentBusTopology ++ // This makes AMO kill each core
    //new WithJustOneBus ++ // This is even worse
    new BaseConfig().alter((site,here,up) => {
      case BootROMLocated(InSubsystem) => None // No BootROM.
      case SystemBusKey => up(SystemBusKey).copy(
        errorDevice = Some(BuiltInErrorDeviceParams(DevNullParams(
          Seq(AddressSet(0x4000, 0xfff)),
          maxAtomic=site(XLen)/8,
          maxTransfer=128,
          region = RegionType.TRACKED))))
      case PeripheryBusKey => up(PeripheryBusKey, site).copy(dtsFrequency =
        Some(BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt),
        errorDevice = None)
      case DTSTimebase => BigInt(1000000)
      case JtagDTMKey => new JtagDTMConfig (
        idcodeVersion = 2,      // 1 was legacy (FE310-G000, Acai).
        idcodePartNum = 0x000,  // Decided to simplify.
        idcodeManufId = 0x489,  // As Assigned by JEDEC to SiFive. Only used in wrappers / test harnesses.
        debugIdleCycles = 5)    // Reasonable guess for synchronization
      case FreqKeyMHz => 100.0
      //case MaxHartIdBits => log2Up(site(BoomTilesKey).size + site(RocketTilesKey).size + site(IbexTilesKey).size)
    }))

// NOTE: Copied from chipyard

// The default RocketChip BaseSubsystem drives its diplomatic clock graph
// with the implicit clocks of Subsystem. Don't do that, instead we extend
// the diplomacy graph upwards into the ChipTop, where we connect it to
// our clock drivers
class WithNoSubsystemDrivenClocks extends Config((site, here, up) => {
  case SubsystemDriveAsyncClockGroupsKey => None
})
