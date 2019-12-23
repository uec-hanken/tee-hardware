// See LICENSE.SiFive for license details.

package uec.keystoneAcc.exampletop

import Chisel._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.util._
import freechips.rocketchip.system._
import freechips.rocketchip.config._
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.tile._
import uec.keystoneAcc.devices.sha3._
import uec.keystoneAcc.devices.ed25519._
import uec.keystoneAcc.devices.aes._
import uec.keystoneAcc.devices.random._
import uec.keystoneAcc.devices.usb11hs._

// Just like DefaultConfig for now, but with the peripherals
class KeystoneDefaultPeripherals extends Config((site, here, up) => {
  case PeripherySHA3Key =>
    SHA3Params(address = BigInt(0x10003000L))
  case Peripheryed25519Key =>
    ed25519Params(address = BigInt(0x10004000L))
  case PeripheryAESKey =>
    AESParams(address = BigInt(0x10007000L))
  case PeripheryUSB11HSKey =>
    USB11HSParams(address = BigInt(0x10008000L))
  case PeripheryRandomKey =>
    RandomParams(address = BigInt(0x10009000L))
  case BootROMParams =>
    BootROMParams(contentFileName = "./hardware/chipyard/generators/rocket-chip/bootrom/bootrom.img")
})

class WithAESAccel extends Config ((site, here, up) => {
  case BuildRoCC => Seq(
    (p: Parameters) => {
      val aes = LazyModule.apply(new AESROCC(OpcodeSet.custom0)(p))
      aes
    }
  )
})

class KeystoneDefaultConfig extends Config(
    new KeystoneDefaultPeripherals ++
    new WithAESAccel ++
    new WithNBigCores(1) ++
    new BaseConfig
)

class KeystoneJTAGConfig extends Config(
    new WithNBreakpoints(4) ++
    new WithJtagDTM ++
    new KeystoneDefaultConfig().alter((site,here,up) => {
      case DTSTimebase => BigInt(1000000)
      case JtagDTMKey => new JtagDTMConfig (
        idcodeVersion = 2,      // 1 was legacy (FE310-G000, Acai).
        idcodePartNum = 0x000,  // Decided to simplify.
        idcodeManufId = 0x489,  // As Assigned by JEDEC to SiFive. Only used in wrappers / test harnesses.
        debugIdleCycles = 5)    // Reasonable guess for synchronization
    })
)

/** Example Top with periphery devices and ports, and a Rocket subsystem */
class ExampleRocketSystem(implicit p: Parameters) extends RocketSubsystem
    with HasHierarchicalBusTopology
    with HasPeripheryDebug
    with HasAsyncExtInterrupts
    with CanHaveMasterAXI4MemPort
    with CanHaveMasterAXI4MMIOPort
    with CanHaveSlaveAXI4Port
    with HasPeripherySHA3
    with HasPeripheryed25519
    with HasPeripheryAES
    with HasPeripheryRandom
    with HasPeripheryUSB11HS
    with HasPeripheryBootROM {
  override lazy val module = new ExampleRocketSystemModuleImp(this)
}

class ExampleRocketSystemModuleImp[+L <: ExampleRocketSystem](_outer: L) extends RocketSubsystemModuleImp(_outer)
    with HasRTCModuleImp
    with HasPeripheryDebugModuleImp
    with HasExtInterruptsModuleImp
    with CanHaveMasterAXI4MemPortModuleImp
    with CanHaveMasterAXI4MMIOPortModuleImp
    with CanHaveSlaveAXI4PortModuleImp
    with HasPeripherySHA3ModuleImp
    with HasPeripheryed25519ModuleImp
    with HasPeripheryAESModuleImp
    with HasPeripheryBootROMModuleImp
    with HasPeripheryRandomModuleImp
    with HasPeripheryUSB11HSModuleImp
    with DontTouch
