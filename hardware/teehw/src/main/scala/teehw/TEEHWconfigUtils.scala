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
import sifive.blocks.devices.spi.SPIFlashParams
import sifive.fpgashells.devices.xilinx.xdma.XDMAParams
import uec.teehardware.devices.sifiveblocks.FreqKeyMHz
import uec.teehardware.ibex._

// The number of gpios that we want as input
case object GPIOInKey extends Field[Int](8)

// When external the DDR port, has to run at another freq
// case object DDRPortOther extends Field[Boolean](false) // NOTE: Deprecated

// Our own reset vector
case object TEEHWResetVector extends Field[Int](0x10040)

// The new additional bus config traits
case object CRYPTOBUS extends TLBusWrapperLocation("subsystem_cryptobus")
case object CryptoBusKey extends Field[CryptoBusParams]
case object CbusToCryptoBusXTypeKey extends Field[ClockCrossingType](SynchronousCrossing())
case object IsCryptoBus extends Field[Boolean](true)

// The new external bus config traits
case object EXTBUS extends TLBusWrapperLocation("subsystem_extbus")
case object ExtBusKey extends Field[ExtBusParams]
case object CbusToExtBusXTypeKey extends Field[ClockCrossingType](SynchronousCrossing())

// Async clock exposure
//case object ExposeClocks extends Field[Boolean](false) // DEPRECATED
case object ExposedClocks extends Field[Seq[String]](Nil)
case class ExternConnSpec
(
  idBits: Int = 6,
  idExtBits: Int = 6,
  widthBits: Int = 32,
  sinkBits: Int = 1,
  version: Int = 0
)
case object ExternConn extends Field[ExternConnSpec](ExternConnSpec())

// TODO: This is used in the full version. Do not use here.
case object DummySPIFlashKey extends Field[Seq[SPIFlashParams]](Nil)

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
        mulUnroll = 8, mulEarlyOut = true, divEarlyOut = true))),
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

class SeparateBusClocks(depth: Int, sync: Int) extends Config((site, here, up) => {
  case CbusToPbusXTypeKey => AsynchronousCrossing(depth, sync)
  case CbusToCryptoBusXTypeKey => AsynchronousCrossing(depth, sync)
})

class SeparateCoreClocks(depth: Int, sync: Int) extends Config((site, here, up) => {
  case TilesLocated(InSubsystem) => up(TilesLocated(InSubsystem), site) map {
    // There is no way to actually use the general CanAttachTile here for copying. Maybe there is a cheat?
    // In the meantime, the cases are only done with the cores we support
    case r: RocketTileAttachParams => r.copy(crossingParams = r.crossingParams.copy(
      crossingType = AsynchronousCrossing(depth, sync)))
    case b: BoomTileAttachParams => b.copy(crossingParams = b.crossingParams.copy(
      AsynchronousCrossing(depth, sync)))
    case i: IbexTileAttachParams => i.copy(crossingParams = i.crossingParams.copy(
      AsynchronousCrossing(depth, sync)))
    case other => other
  }
})

class WithMulticlockIncoherentBusTopology extends Config((site, here, up) => {
  case TLNetworkTopologyLocated(InSubsystem) => List( // WithIncoherent, but with Clock Separation (TM)
    JustOneBusTopologyParams(sbus = site(SystemBusKey)),
    HierarchicalBusTopologyParams(
      pbus = site(PeripheryBusKey),
      fbus = site(FrontBusKey),
      cbus = site(ControlBusKey),
      xTypes = SubsystemCrossingParams(
        sbusToCbusXType = site(SbusToCbusXTypeKey),
        cbusToPbusXType = site(CbusToPbusXTypeKey),
        fbusToSbusXType = site(FbusToSbusXTypeKey)),
      driveClocksFromSBus = site(DriveClocksFromSBus)))
})

// JTAG configuration
object TEEHWJtagConfig {
  def apply() : JtagDTMConfig = {
    JtagDTMConfig (
      idcodeVersion = 2,      // 1 was legacy (FE310-G000, Acai).
      idcodePartNum = 0x000,  // Decided to simplify.
      idcodeManufId = 0x489,  // As Assigned by JEDEC to SiFive. Only used in wrappers / test harnesses.
      debugIdleCycles = 5)    // Reasonable guess for synchronization
  }
}

object TEEHWDebugConfig {
  def apply() : DebugModuleParams = DebugModuleParams (clockGate = false)
}

// Chip Configs
class ChipConfig extends Config(
  // The rest of the configurations, which are not-movable
  new WithNExtTopInterrupts(0) ++
    new TEEHWPeripherals ++
    new WithJtagDTM ++
    new WithAsynchronousRocketTiles(8,3) ++
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
      case PeripheryBusKey => up(PeripheryBusKey, site).copy(
        dtsFrequency = Some(BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt),
        errorDevice = None)
      case CryptoBusKey => CryptoBusParams(
        beatBytes = site(XLen)/8,
        blockBytes = site(CacheBlockBytes),
        dtsFrequency = Some(BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt))
      case DTSTimebase => BigInt(1000000)
      case JtagDTMKey => TEEHWJtagConfig()
      case DebugModuleKey => Some(TEEHWDebugConfig())
      case FreqKeyMHz => 100.0
    }))

class MicroConfig extends Config(
  new WithNExtTopInterrupts(0) ++
    new TEEHWPeripherals ++
    new WithJtagDTM ++
    new WithNoSubsystemDrivenClocks ++
    new WithDontDriveBusClocksFromSBus ++
    new WithCoherentBusTopology ++ // Will add the L2, but The L2 will be a broadcast with buffer
    new BaseConfig().alter((site,here,up) => {
      case BootROMLocated(InSubsystem) => None // No BootROM.
      case SystemBusKey => up(SystemBusKey).copy(
        errorDevice = Some(BuiltInErrorDeviceParams(DevNullParams(
          Seq(AddressSet(0x4000, 0xfff)),
          maxAtomic=site(XLen)/8,
          maxTransfer=128,
          region = RegionType.TRACKED))))
      case PeripheryBusKey => up(PeripheryBusKey, site).copy(
        dtsFrequency = Some(BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt),
        errorDevice = None)
      case CryptoBusKey => CryptoBusParams(
        beatBytes = site(XLen)/8,
        blockBytes = site(CacheBlockBytes),
        dtsFrequency = Some(BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt))
      case DTSTimebase => BigInt(1000000)
      case JtagDTMKey => TEEHWJtagConfig()
      case DebugModuleKey => Some(TEEHWDebugConfig())
      case FreqKeyMHz => 100.0
    }))

class MCUWithLinuxConfig extends Config(
  new WithNExtTopInterrupts(0) ++
    new TEEHWPeripherals ++
    new WithJtagDTM ++
    new WithNoSubsystemDrivenClocks ++
    new WithDontDriveBusClocksFromSBus ++
    new WithBufferlessBroadcastHub ++ // Will add the L2, but The L2 will be a broadcast without buffer
    new WithCoherentBusTopology ++
    new BaseConfig().alter((site,here,up) => {
      case BootROMLocated(InSubsystem) => None // No BootROM.
      case SystemBusKey => up(SystemBusKey).copy(
        beatBytes = site(XLen)/8,
        errorDevice = Some(BuiltInErrorDeviceParams(DevNullParams(
          Seq(AddressSet(0x4000, 0xfff)),
          maxAtomic=site(XLen)/8,
          maxTransfer=128,
          region = RegionType.TRACKED))))
      case PeripheryBusKey => up(PeripheryBusKey, site).copy(
        beatBytes = site(XLen)/8,
        dtsFrequency = Some(BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt),
        errorDevice = None)
      case CryptoBusKey => CryptoBusParams(
        beatBytes = site(XLen)/8,
        blockBytes = site(CacheBlockBytes),
        dtsFrequency = Some(BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt))
      case DTSTimebase => BigInt(1000000)
      case JtagDTMKey => TEEHWJtagConfig()
      case DebugModuleKey => Some(TEEHWDebugConfig())
      case FreqKeyMHz => 100.0
    }))

class MCUConfig extends Config(
  new WithNExtTopInterrupts(0) ++
    new TEEHWPeripherals ++
    new WithJtagDTM ++
    new WithNoSubsystemDrivenClocks ++
    new WithDontDriveBusClocksFromSBus ++
    new WithMulticlockIncoherentBusTopology ++
    new BaseConfig().alter((site,here,up) => {
      case BootROMLocated(InSubsystem) => None // No BootROM.
      case SystemBusKey => up(SystemBusKey).copy(
        beatBytes = site(XLen)/8,
        errorDevice = None)
      case ControlBusKey => up(ControlBusKey).copy(
        beatBytes = site(XLen)/8,
        errorDevice = None)
      case PeripheryBusKey => up(PeripheryBusKey, site).copy(
        beatBytes = site(XLen)/8,
        dtsFrequency = Some(BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt),
        errorDevice = None)
      case CryptoBusKey => CryptoBusParams(
        beatBytes = site(XLen)/8,
        blockBytes = site(CacheBlockBytes),
        dtsFrequency = Some(BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt))
      case DTSTimebase => BigInt(1000000)
      case JtagDTMKey => TEEHWJtagConfig()
      case DebugModuleKey => Some(TEEHWDebugConfig())
      case FreqKeyMHz => 100.0
    }))

// NOTE: Copied from chipyard

// The default RocketChip BaseSubsystem drives its diplomatic clock graph
// with the implicit clocks of Subsystem. Don't do that, instead we extend
// the diplomacy graph upwards into the ChipTop, where we connect it to
// our clock drivers
class WithNoSubsystemDrivenClocks extends Config((site, here, up) => {
  case SubsystemDriveAsyncClockGroupsKey => None
})
