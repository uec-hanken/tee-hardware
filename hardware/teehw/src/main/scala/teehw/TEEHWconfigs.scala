package uec.teehardware

import chisel3._
import chisel3.util.{log2Up}

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
import uec.teehardware.devices.aes._
import uec.teehardware.devices.ed25519._
import uec.teehardware.devices.sha3._
import uec.teehardware.devices.usb11hs._
import uec.teehardware.devices.random._
import boom.common._
import boom.ifu._
import boom.exu._
import boom.lsu._
//import sifive.freedom.unleashed.DevKitFPGAFrequencyKey

// The number of gpios that we want as input
case object GPIOInKey extends Field[Int]

// Frequency
case object FreqKeyMHz extends Field[Double]

// Include the PCIe
case object IncludePCIe extends Field[Boolean]

// When external the DDR port, has to run at another freq
case object DDRPortOther extends Field[Boolean]

class RV64GC extends Config((site, here, up) => {
  case XLen => 64
})

class RV32GC extends Config((site, here, up) => {
  case XLen => 32
  /* Boom32 doesn't have FPU */
  case BoomTilesKey => up(BoomTilesKey, site) map { r =>
    r.copy(core = r.core.copy(
      issueParams = Seq( // In all, numEntries was 8
        IssueParams(issueWidth=1, numEntries=2, iqType=IQT_MEM.litValue, dispatchWidth=1),
        IssueParams(issueWidth=1, numEntries=2, iqType=IQT_INT.litValue, dispatchWidth=1)),
      fpu = None
    ))
  }
})

class RV32IMAFC extends Config((site, here, up) => {
  case XLen => 32
  case RocketTilesKey => up(RocketTilesKey, site) map { r =>
    r.copy(core = r.core.copy(fpu = r.core.fpu.map(_.copy(fLen = 32))))
  }
  /* Boom32 doesn't have FPU */
  case BoomTilesKey => up(BoomTilesKey, site) map { r =>
    r.copy(core = r.core.copy(
      issueParams = Seq( // In all, numEntries was 8
        IssueParams(issueWidth=1, numEntries=2, iqType=IQT_MEM.litValue, dispatchWidth=1),
        IssueParams(issueWidth=1, numEntries=2, iqType=IQT_INT.litValue, dispatchWidth=1)),
      fpu = None
    ))
  }
})

class RV32IMAC extends Config((site, here, up) => {
  case XLen => 32
  case RocketTilesKey => up(RocketTilesKey, site) map { r =>
    r.copy(core = r.core.copy(fpu = None))
  }
  case BoomTilesKey => up(BoomTilesKey, site) map { r =>
    r.copy(core = r.core.copy(
      issueParams = Seq( // In all, numEntries was 8
        IssueParams(issueWidth=1, numEntries=2, iqType=IQT_MEM.litValue, dispatchWidth=1),
        IssueParams(issueWidth=1, numEntries=2, iqType=IQT_INT.litValue, dispatchWidth=1)),
      fpu = None
    ))
  }
})

/* NOTE ABOUT CACHE SIZES
 Cache size = nSets * nWays * CacheBlockBytes
 nSets = (default) 64;
 nWays = (default) 4;
 CacheBlockBytes = (default) 64;
 => default cache size = 64 * 4 * 64 = 16KBytes
*/
case class BoomParams(n: Int) extends Config((site, here, up) => {
  case BoomTilesKey => {
    val mini = BoomTileParams(
      core = BoomCoreParams(
        // Defaults (or unbinded), just to have full control
        decodeWidth = 1,
        // WithSmallBooms
        fetchWidth = 4, useCompressed = true, numRobEntries = 16,
        issueParams = Seq(
          IssueParams(issueWidth = 1, numEntries = 2, iqType = IQT_MEM.litValue, dispatchWidth = 1),
          IssueParams(issueWidth = 1, numEntries = 2, iqType = IQT_INT.litValue, dispatchWidth = 1),
          IssueParams(issueWidth = 1, numEntries = 2, iqType = IQT_FP.litValue, dispatchWidth = 1)),
        numIntPhysRegisters = 52, numFpPhysRegisters = 48, numLdqEntries = 4, numStqEntries = 4,
        maxBrCount = 2, numFetchBufferEntries = 8, ftq = FtqParameters(nEntries = 4),
        nPerfCounters = 1,
        fpu = Some(freechips.rocketchip.tile.FPUParams(sfmaLatency = 4, dfmaLatency = 4, divSqrt = true)),
        // WithTAGELBPD
        bpdMaxMetaLength = 120,
        globalHistoryLength = 64,
        localHistoryLength = 1,
        localHistoryNSets = 0,
        branchPredictor = ((resp_in: BranchPredictionBankResponse, p: Parameters) => {
          val loop = Module(new LoopBranchPredictorBank()(p))
          val tage = Module(new TageBranchPredictorBank()(p))
          val btb = Module(new BTBBranchPredictorBank()(p))
          val bim = Module(new BIMBranchPredictorBank()(p))
          val ubtb = Module(new FAMicroBTBBranchPredictorBank()(p))
          val preds = Seq(loop, tage, btb, ubtb, bim)
          preds.map(_.io := DontCare)

          ubtb.io.resp_in(0)  := resp_in
          bim.io.resp_in(0)   := ubtb.io.resp
          btb.io.resp_in(0)   := bim.io.resp
          tage.io.resp_in(0)  := btb.io.resp
          loop.io.resp_in(0)  := tage.io.resp

          (preds, loop.io.resp)
        })
      ),
      dcache = Some(DCacheParams(
        nSets = 16, nWays = 1,
        rowBits = site(SystemBusKey).beatBits,
        nMSHRs = 2, // was 2, NOTE: Cannot be strictly 0 (restriction), and cannot be 1 (width error) in boom
        blockBytes = site(CacheBlockBytes), nTLBEntries = 8)),
      icache = Some(ICacheParams(
        nSets = 16, nWays = 1,
        rowBits = site(SystemBusKey).beatBits,
        blockBytes = site(CacheBlockBytes),
        fetchBytes = 2*4)) // 2 = instrWidth (bytes), 4 = fetchWidth, has to be instrWidth*fetchWidth
    ) /* NOTES: Cannot set fetchBytes in the icache to 2*2 in fetchWidth = 2 because
                it requires that the cache width only difers 1 bank. Either we disable Compressed, or fetch 4.
                Why compressed? In compressed, the instruction width is 2 */
    List.tabulate(n)(i => mini.copy(hartId = i))
  }
})

case class RocketParams(n: Int) extends Config((site, here, up) => {
  case RocketTilesKey => {
    val big = RocketTileParams(
      core = RocketCoreParams(mulDiv = Some(MulDivParams(
        mulUnroll = 8, mulEarlyOut = true, divEarlyOut = true))),
      dcache = Some(DCacheParams(
        nSets = 16, nWays = 1, nMSHRs = 0,
        rowBits = site(SystemBusKey).beatBits,
        blockBytes = site(CacheBlockBytes))),
      icache = Some(ICacheParams(
        nSets = 16, nWays = 1,
        rowBits = site(SystemBusKey).beatBits,
        blockBytes = site(CacheBlockBytes))))
    List.tabulate(n)(i => big.copy(hartId = i))
  }
})

class Boom extends Config(
  BoomParams(2) //Only Boom: 2 cores
)

class Rocket extends Config(
  RocketParams(2) //Only Rocket: 2 cores
)

class BoomRocket extends Config(
  new WithRenumberHarts(rocketFirst = false) ++ //Boom first, Rocket second
  BoomParams(1) ++
  RocketParams(1)
)

class RocketBoom extends Config(
  new WithRenumberHarts(rocketFirst = true) ++ //Rocket first, Boom second
  BoomParams(1) ++
  RocketParams(1)
)

class BOOTROM extends Config((site, here, up) => {
  case PeripheryMaskROMKey => List(
    MaskROMParams(address = BigInt(0x20000000), depth = 2048, name = "BootROM"))
  case PeripherySPIFlashKey => List() // disable SPIFlash
})

class QSPI extends Config((site, here, up) => {
  case PeripheryMaskROMKey => List( //move BootROM back to 0x10000
    MaskROMParams(address = 0x10000, depth = 16, name = "BootROM")) //smallest allowed depth is 16
  case PeripherySPIFlashKey => List(
    SPIFlashParams(fAddress = 0x20000000, rAddress = 0x64005000, defaultSampleDel = 3))
})

// Chip Peripherals
class ChipPeripherals extends Config((site, here, up) => {
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
  new WithJtagDTM            ++
  new WithNMemoryChannels(1) ++
  new chipyard.config.WithL2TLBs(entries = 1024) ++               // use L2 TLBs
  new freechips.rocketchip.subsystem.WithInclusiveCache ++        // use Sifive L2 cache
  new WithCoherentBusTopology ++                                  // This adds a L2 cache
  //new WithIncoherentBusTopology ++ // This was the previous one
  new BaseConfig().alter((site,here,up) => {
    case SystemBusKey => up(SystemBusKey).copy(
      beatBytes = 8,
      errorDevice = Some(DevNullParams(
        Seq(AddressSet(0x4000, 0xfff)),
        maxAtomic=site(XLen)/8,
        maxTransfer=128,
        region = RegionType.TRACKED)))
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
    case DDRPortOther => true
  })
)

class DE4Config extends Config(
  new ChipConfig().alter((site,here,up) => {
    case FreqKeyMHz => 100.0
    case DDRPortOther => true
  })
)

class TR4Config extends Config(
  new ChipConfig().alter((site,here,up) => {
    case FreqKeyMHz => 100.0
    /*case ExtMem => Some(MemoryPortParams(MasterPortParams( // For back to 64 bits
      base = x"0_8000_0000",
      size = x"0_4000_0000",
      beatBytes = 8,
      idBits = 4), 1))*/
    case DDRPortOther => false // For back to not external clock
  })
)

class VC707Config extends Config(
  new ChipConfig().alter((site,here,up) => {
    case FreqKeyMHz => 80.0
    /* Force to use BootROM because VC707 doesn't have enough GPIOs for QSPI */
    case PeripheryMaskROMKey => List(
      MaskROMParams(address = BigInt(0x20000000), depth = 2048, name = "BootROM"))
    case PeripherySPIFlashKey => List() // disable SPIFlash
    case IncludePCIe => false // This is for including the PCIe
    case ExtMem => Some(MemoryPortParams(MasterPortParams( // For back to 64 bits
      base = x"0_8000_0000",
      size = x"0_4000_0000",
      beatBytes = 8,
      idBits = 4), 1))
    case DDRPortOther => false // For back to not external clock
  })
)

import testchipip.SerialKey

class WithSimulation extends Config((site, here, up) => {
  // Force the DMI
  case ExportDebug => up(ExportDebug, site).copy(protocols = Set(DMI))
  // Force also the Serial interface
  case SerialKey => true
    // Change the BootROM stuff
  case BootROMParams => BootROMParams(
    contentFileName = s"./bootrom/bootrom.rv${site(XLen)}.img")
  case PeripheryMaskROMKey => List()
})