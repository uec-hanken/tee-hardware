package uec.keystoneAcc.nedochip

import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket._
import freechips.rocketchip.system._
import freechips.rocketchip.tile._
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.blocks.devices.uart._
import sifive.blocks.devices.i2c._
import uec.keystoneAcc.devices.aes._
import uec.keystoneAcc.devices.ed25519._
import uec.keystoneAcc.devices.sha3._
import uec.keystoneAcc.devices.usb11hs._
import uec.keystoneAcc.devices.random._
import boom.common._
import boom.exu._
import boom.ifu._
import boom.bpu._
//import sifive.freedom.unleashed.DevKitFPGAFrequencyKey

// The number of gpios that we want as input
case object GPIOInKey extends Field[Int]

// Frequency
case object FreqKeyMHz extends Field[Double]

// Include the PCIe
case object IncludePCIe extends Field[Boolean]

// When external the DDR port, has to run at another freq
case object DDRPortOther extends Field[Boolean]

// Default Config
class ChipDefaultConfig extends Config(
  new WithJtagDTM            ++
  new WithNMemoryChannels(1) ++
  new boom.common.WithRenumberHarts(rocketFirst = false) ++
  //new boom.common.WithSmallBooms ++
  //new boom.common.WithNBoomCores(1) ++
  // new WithNBigCores(2)       ++
  new BaseConfig().alter((site,here,up) => {
    case RocketTilesKey => {
      val big = RocketTileParams(
        core = RocketCoreParams(mulDiv = Some(MulDivParams(
          mulUnroll = 8,
          mulEarlyOut = true,
          divEarlyOut = true))),
        // Cache size = nSets * nWays * CacheBlockBytes
        // nSets = (default) 64;
        // nWays = (default) 4;
        // CacheBlockBytes = (default) 64;
        // => default cache size = 64 * 4 * 64 = 16KBytes
        dcache = Some(DCacheParams(
          // => dcache size = 16 * 1 * 64 = 1KBytes
          nSets = 16,
          nWays = 1,
          rowBits = site(SystemBusKey).beatBits,
          nMSHRs = 0,
          blockBytes = site(CacheBlockBytes))),
        icache = Some(ICacheParams(
          // => icache size = 16 * 1 * 64 = 1KBytes
          nSets = 16,
          nWays = 1,
          rowBits = site(SystemBusKey).beatBits,
          blockBytes = site(CacheBlockBytes))))
      List.tabulate(1)(i => big.copy(hartId = i+1)) // TODO: Make it dependent of up(BoomTilesKey, site).length
    }
    case BoomTilesKey => {
      val mini = BoomTileParams(
        core = BoomCoreParams(
          fetchWidth = 4, // was 4
          useCompressed = true,
          decodeWidth = 1,
          numRobEntries = 16, // was 32
          issueParams = Seq( // In all, numEntries was 8
            IssueParams(issueWidth=1, numEntries=2, iqType=IQT_MEM.litValue, dispatchWidth=1),
            IssueParams(issueWidth=1, numEntries=2, iqType=IQT_INT.litValue, dispatchWidth=1),
            IssueParams(issueWidth=1, numEntries=2, iqType=IQT_FP.litValue , dispatchWidth=1)),
          numIntPhysRegisters = 52,
          numFpPhysRegisters = 48,
          numLdqEntries = 4, // was 8
          numStqEntries = 4, // was 8
          maxBrCount = 2, // was 4
          numFetchBufferEntries = 8, // was 8
          ftq = FtqParameters(nEntries=4), // was 16
          btb = BoomBTBParameters(
            btbsa=true, densebtb=false, bypassCalls=false, rasCheckForEmpty=false,
            nSets=16, // was 64
            nWays=1, // was 2
            nRAS=4, // was 8
            tagSz=10), // was 20
          bpdBaseOnly = None,
          gshare = Some(GShareParameters(
            historyLength=11,
            numSets=1024)), // was 2048
          tage = None,
          bpdRandom = None,
          nPerfCounters = 1, // was 2
          fpu = Some(freechips.rocketchip.tile.FPUParams(sfmaLatency=4, dfmaLatency=4, divSqrt=true))),
        // Cache size = nSets * nWays * CacheBlockBytes
        // nSets = (default) 64;
        // nWays = (default) 4;
        // CacheBlockBytes = (default) 64;
        // => default cache size = 64 * 4 * 64 = 16KBytes
        dcache = Some(DCacheParams(
          // => dcache size = 16 * 1 * 64 = 1KBytes
          nSets = 16, // was 64
          nWays = 1, // was 4
          rowBits = site(SystemBusKey).beatBits,
          nMSHRs = 2, // was 2, NOTE: Cannot be strictly 0 (restriction), and cannot be 1 (width error) in boom
          blockBytes = site(CacheBlockBytes),
          nTLBEntries=8)),
        icache = Some(ICacheParams(
          // => icache size = 16 * 1 * 64 = 1KBytes
          nSets = 16, // was 64
          nWays = 1, // was 4
          rowBits = site(SystemBusKey).beatBits,
          blockBytes = site(CacheBlockBytes),
          fetchBytes=2*4)) // was 2*4, 2 = instrWidth (bytes), 4 = fetchWidth, has to be instrWidt*fetchWidth
        /*
        NOTES:
        1) Cannot set fetchBytes in the icache to 2*2 in fetchWidth = 2 because
           it requires that the cache width only difers 1 bank. Either we disable Compressed, or fetch 4.
           Why compressed? In compressed, the instruction width is 2.
         */
      )
      val small = BoomTileParams(
        core = BoomCoreParams(
          fetchWidth = 4,
          useCompressed = true,
          decodeWidth = 1,
          numRobEntries = 32,
          issueParams = Seq(
            IssueParams(issueWidth=1, numEntries=8, iqType=IQT_MEM.litValue, dispatchWidth=1),
            IssueParams(issueWidth=1, numEntries=8, iqType=IQT_INT.litValue, dispatchWidth=1),
            IssueParams(issueWidth=1, numEntries=8, iqType=IQT_FP.litValue , dispatchWidth=1)),
          numIntPhysRegisters = 52,
          numFpPhysRegisters = 48,
          numLdqEntries = 8,
          numStqEntries = 8,
          maxBrCount = 4,
          numFetchBufferEntries = 8,
          ftq = FtqParameters(nEntries=16),
          btb = BoomBTBParameters(btbsa=true, densebtb=false, nSets=64, nWays=2,
            nRAS=8, tagSz=20, bypassCalls=false, rasCheckForEmpty=false),
          bpdBaseOnly = None,
          gshare = Some(GShareParameters(historyLength=11, numSets=2048)),
          tage = None,
          bpdRandom = None,
          nPerfCounters = 2,
          fpu = Some(freechips.rocketchip.tile.FPUParams(sfmaLatency=4, dfmaLatency=4, divSqrt=true))),
        dcache = Some(DCacheParams(rowBits = site(SystemBusKey).beatBits,
        nSets=64, nWays=4, nMSHRs=2, nTLBEntries=8)),
        icache = Some(ICacheParams(rowBits = site(SystemBusKey).beatBits, nSets=64, nWays=4, fetchBytes=2*4))
      )
      List.tabulate(1)(i => mini.copy(hartId = i)) // TODO: Make it dependent of up(RocketTilesKey, site).length
    }
  })
)

// Chip Peripherals
class ChipPeripherals extends Config((site, here, up) => {
  case PeripheryMaskROMKey => List(
    MaskROMParams(address = 0x10000, name = "BootROM"))
  case PeripheryUARTKey => List(
    UARTParams(address = BigInt(0x64000000L)))
  case PeripherySPIKey => List(
    SPIParams(rAddress = BigInt(0x64001000L)))
  case PeripheryGPIOKey => List(
    GPIOParams(address = BigInt(0x64002000L), width = 16))
  case GPIOInKey => 8
  case PeripherySHA3Key => List(
    SHA3Params(address = BigInt(0x64003000L)))
  case Peripheryed25519Key => List(
    ed25519Params(address = BigInt(0x64004000L)))
  case PeripherySPIFlashKey => List(
    SPIFlashParams(
      fAddress = 0x20000000,
      rAddress = 0x64005000,
      defaultSampleDel = 3))
  case PeripheryI2CKey => List(
    I2CParams(address = 0x64006000))
  case PeripheryAESKey => List(
    AESParams(address = BigInt(0x64007000L)))
  case PeripheryUSB11HSKey => List(
    USB11HSParams(address = BigInt(0x64008000L)))
  case PeripheryRandomKey => List(
    RandomParams(address = BigInt(0x64009000L)))
  case ExtMem => Some(MemoryPortParams(MasterPortParams(
    base = x"0_8000_0000",
    size = x"0_4000_0000",
    beatBytes = 4,// This is for supporting 32 bits outside. BEFORE: site(MemoryBusKey).beatBytes,
    idBits = 4), 1))
  case IncludePCIe => false
  case DDRPortOther => true
})

// Chip Configs
class ChipConfig extends Config(
  new WithNExtTopInterrupts(0)   ++
  new WithNBreakpoints(4)    ++
  new ChipPeripherals ++
  new ChipDefaultConfig().alter((site,here,up) => {
    case SystemBusKey => up(SystemBusKey).copy(
      errorDevice = Some(DevNullParams(
        Seq(AddressSet(0x4000, 0xfff)),
        maxAtomic=site(XLen)/8,
        maxTransfer=128,
        region = RegionType.TRACKED)))
    case PeripheryBusKey => up(PeripheryBusKey, site).copy(frequency =
      BigDecimal(site(FreqKeyMHz)*1000000).setScale(0, BigDecimal.RoundingMode.HALF_UP).toBigInt,
      errorDevice = None)
    case DTSTimebase => BigInt(1000000)
    case JtagDTMKey => new JtagDTMConfig (
      idcodeVersion = 2,      // 1 was legacy (FE310-G000, Acai).
      idcodePartNum = 0x000,  // Decided to simplify.
      idcodeManufId = 0x489,  // As Assigned by JEDEC to SiFive. Only used in wrappers / test harnesses.
      debugIdleCycles = 5)    // Reasonable guess for synchronization
    case FreqKeyMHz => 100.0
    case DDRPortOther => true
  })
)

class ChipConfigDE4 extends Config(
  new ChipConfig().alter((site,here,up) => {
    case FreqKeyMHz => 100.0
    case DDRPortOther => true
  })
)

class ChipConfigTR4 extends Config(
  new ChipConfig().alter((site,here,up) => {
    //case XLen => 32
    case FreqKeyMHz => 100.0
    /*case ExtMem => Some(MemoryPortParams(MasterPortParams( // For back to 64 bits
      base = x"0_8000_0000",
      size = x"0_4000_0000",
      beatBytes = 8,
      idBits = 4), 1))*/
    case PeripherySPIFlashKey => List() // No external flash. There is no pins to put them
    case PeripheryMaskROMKey => List( // TODO: The software is not compilable on 0x10000
      MaskROMParams(address = BigInt(0x20000000), depth = 8192, name = "BootROM"))
    case DDRPortOther => false // For back to not external clock
    /*case RocketTilesKey => up(RocketTilesKey, site) map { r =>
      r.copy(core = r.core.copy(fpu = None))
    }*/
    /*case BoomTilesKey => up(BoomTilesKey, site) map { r =>
      r.copy(core = r.core.copy(
        fpu = None,
        issueParams = Seq( // In all, numEntries was 8
          IssueParams(issueWidth=1, numEntries=2, iqType=IQT_MEM.litValue, dispatchWidth=1),
          IssueParams(issueWidth=1, numEntries=2, iqType=IQT_INT.litValue, dispatchWidth=1))
      ))
    }*/
    /*case RocketTilesKey => {
      val big = RocketTileParams(
        core   = RocketCoreParams(mulDiv = Some(MulDivParams(
          mulUnroll = 8,
          mulEarlyOut = true,
          divEarlyOut = true))),
        dcache = Some(DCacheParams(
          rowBits = site(SystemBusKey).beatBits,
          nMSHRs = 0,
          blockBytes = site(CacheBlockBytes))),
        icache = Some(ICacheParams(
          rowBits = site(SystemBusKey).beatBits,
          blockBytes = site(CacheBlockBytes))))
      List.tabulate(1)(i => big.copy(hartId = i+1)) // TODO: Make it dependent of up(BoomTilesKey, site).length
    }*/
    //case BoomTilesKey => {
    //  List.tabulate(1)(i => BoomTileParams(hartId = i)) // TODO: Make it dependent of up(RocketTilesKey, site).length
    //}
  })
)

class ChipConfigVC707 extends Config(
  new ChipConfig().alter((site,here,up) => {
    case FreqKeyMHz => 80.0
    case PeripherySPIFlashKey => List() // No external flash. There is no pins to put them
    case PeripheryMaskROMKey => List( // TODO: The software is not compilable on 0x10000
      MaskROMParams(address = BigInt(0x20000000), depth = 8192, name = "BootROM"))
    case IncludePCIe => false // This is for including the PCIe
    case ExtMem => Some(MemoryPortParams(MasterPortParams( // For back to 64 bits
      base = x"0_8000_0000",
      size = x"0_4000_0000",
      beatBytes = 8,
      idBits = 4), 1))
    case DDRPortOther => false // For back to not external clock
    case RocketTilesKey => {
      val big = RocketTileParams(
        core   = RocketCoreParams(mulDiv = Some(MulDivParams(
          mulUnroll = 8,
          mulEarlyOut = true,
          divEarlyOut = true))),
        dcache = Some(DCacheParams(
          rowBits = site(SystemBusKey).beatBits,
          nMSHRs = 0,
          blockBytes = site(CacheBlockBytes))),
        icache = Some(ICacheParams(
          rowBits = site(SystemBusKey).beatBits,
          blockBytes = site(CacheBlockBytes))))
      List.tabulate(1)(i => big.copy(hartId = i+1)) // TODO: Make it dependent of up(BoomTilesKey, site).length
    }
    //case BoomTilesKey => {
    //  List.tabulate(1)(i => BoomTileParams(hartId = i)) // TODO: Make it dependent of up(RocketTilesKey, site).length
    //}
  })
)
