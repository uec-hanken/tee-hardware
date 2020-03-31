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
//import sifive.freedom.unleashed.DevKitFPGAFrequencyKey

// The number of gpios that we want as input
case object GPIOInKey extends Field[Int]

// Frequency
case object FreqKeyMHz extends Field[Double]

// Include the PCIe
case object IncludePCIe extends Field[Boolean]

// Default Config
class ChipDefaultConfig extends Config(
  new WithJtagDTM            ++
  new WithNMemoryChannels(1) ++
  //new WithNBigCores(2)       ++
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
      List.tabulate(2)(i => big.copy(hartId = i))
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
  case PeripherySHA3Key =>
    SHA3Params(address = BigInt(0x64003000L))
  case Peripheryed25519Key =>
    ed25519Params(address = BigInt(0x64004000L))
  case PeripherySPIFlashKey => List(
    SPIFlashParams(
      fAddress = 0x20000000,
      rAddress = 0x64005000,
      defaultSampleDel = 3))
  case PeripheryI2CKey => List(
    I2CParams(address = 0x64006000))
  case PeripheryAESKey =>
    AESParams(address = BigInt(0x64007000L))
  case PeripheryUSB11HSKey =>
    USB11HSParams(address = BigInt(0x64008000L))
  case PeripheryRandomKey =>
    RandomParams(address = BigInt(0x64009000L))
  case ExtMem => Some(MemoryPortParams(MasterPortParams(
    base = x"0_8000_0000",
    size = x"0_4000_0000",
    beatBytes = 4,// This is for supporting 32 bits outside. BEFORE: site(MemoryBusKey).beatBytes,
    idBits = 4), 1))
  case IncludePCIe => false
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
  })
)

class ChipConfigDE4 extends Config(
  new ChipConfig().alter((site,here,up) => {
    case FreqKeyMHz => 100.0
  })
)

class ChipConfigVC707 extends Config(
  new ChipConfig().alter((site,here,up) => {
    case FreqKeyMHz => 100.0
    case PeripherySPIFlashKey => List() // No external flash. There is no pins to put them
    case PeripheryMaskROMKey => List( // TODO: The software is not compilable on 0x10000
      MaskROMParams(address = BigInt(0x20000000), depth = 8192, name = "BootROM"))
    case IncludePCIe => true // This is for including the PCIe
  })
)