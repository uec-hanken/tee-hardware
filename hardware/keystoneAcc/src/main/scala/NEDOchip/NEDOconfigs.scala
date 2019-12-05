package uec.keystoneAcc.nedochip

import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.diplomacy._
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
//import sifive.freedom.unleashed.DevKitFPGAFrequencyKey

// The number of gpios that we want as input
case object GPIOInKey extends Field[Int]

// Frequency
case object FreqKeyMHz extends Field[Double]

// Default Config
class ChipDefaultConfig extends Config(
  new WithJtagDTM            ++
  new WithNMemoryChannels(1) ++
  new WithNBigCores(2)       ++
  new BaseConfig
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
  case ExtMem => Some(MemoryPortParams(MasterPortParams(
    base = x"0_8000_0000",
    size = x"0_4000_0000",
    beatBytes = 4,//site(MemoryBusKey).beatBytes,
    idBits = 4), 1))
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
    case FreqKeyMHz => 50.0
  })
)

class ChipConfigDE4 extends Config(
  new ChipConfig().alter((site,here,up) => {
    case FreqKeyMHz => 100.0
  })
)