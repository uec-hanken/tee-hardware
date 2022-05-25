package uec.teehardware.devices.tlmemext

import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import uec.teehardware.{CRYPTOBUS, TEEHWBaseSubsystem}
import chipsalliance.rocketchip.config.Field

// SRAM Configuration
case class SRAMConfig
(
  address: BigInt,
  size: BigInt
)
case object SRAMKey extends Field[Seq[SRAMConfig]](Nil)

trait HasTEEHWPeripheryTLRAM {
  this: TEEHWBaseSubsystem =>

  // SRAMs
  val srams = p(SRAMKey).zipWithIndex.map { case(sramcfg, i) =>
    val sram = LazyModule(new TLRAM(AddressSet.misaligned(sramcfg.address, sramcfg.size).head, cacheable = true))
    val mbus = locateTLBusWrapper(CRYPTOBUS)
    mbus.coupleTo(s"sram_${i}") { bus => sram.node := TLFragmenter(4, mbus.blockBytes) := bus }
    sram
  }
}
