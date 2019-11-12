// See LICENSE.SiFive for license details.

package uec.keystoneAcc.exampletop

import Chisel._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.devices.tilelink._
import freechips.rocketchip.util.DontTouch
import freechips.rocketchip.system.BaseConfig
import freechips.rocketchip.config._
import uec.keystoneAcc.devices.sha3._
import uec.keystoneAcc.devices.ed25519._

// Just like DefaultConfig for now, but with the peripherals
class KeystoneDefaultPeripherals extends Config((site, here, up) => {
  case PeripherySHA3Key =>
    SHA3Params(address = BigInt(0x10003000L), width = 0)
  case Peripheryed25519Key =>
    ed25519Params(address = BigInt(0x10004000L), width = 0)
  case BootROMParams =>
    BootROMParams(contentFileName = "./hardware/chipyard/generators/rocket-chip/bootrom/bootrom.img")
})

class KeystoneDefaultConfig extends Config(
    new WithNBigCores(1) ++
    new KeystoneDefaultPeripherals ++
    new BaseConfig
)

/** Example Top with periphery devices and ports, and a Rocket subsystem */
class ExampleRocketSystem(implicit p: Parameters) extends RocketSubsystem
    with HasHierarchicalBusTopology
    with HasAsyncExtInterrupts
    with CanHaveMasterAXI4MemPort
    with CanHaveMasterAXI4MMIOPort
    with CanHaveSlaveAXI4Port
    with HasPeripherySHA3
    with HasPeripheryed25519
    with HasPeripheryBootROM {
  override lazy val module = new ExampleRocketSystemModuleImp(this)
}

class ExampleRocketSystemModuleImp[+L <: ExampleRocketSystem](_outer: L) extends RocketSubsystemModuleImp(_outer)
    with HasRTCModuleImp
    with HasExtInterruptsModuleImp
    with CanHaveMasterAXI4MemPortModuleImp
    with CanHaveMasterAXI4MMIOPortModuleImp
    with CanHaveSlaveAXI4PortModuleImp
    with HasPeripherySHA3ModuleImp
    with HasPeripheryed25519ModuleImp
    with HasPeripheryBootROMModuleImp
    with DontTouch
