package uec.teehardware.devices.opentitan.nmi_gen

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.util.HeterogeneousBag
import uec.teehardware._

case object PeripheryNmiGenKey extends Field[NmiGenParams](NmiGenParams(address = BigInt(0x64200000L)))

trait HasPeripheryNmiGen { this: TEEHWSubsystem =>
  //println(escnode.toString)
  val nmiGenDev =
    NmiGenAttachParams(p(PeripheryNmiGenKey), escnode).attachTo(this)
  val nmiGen =
      nmiGenDev.ioNode.makeSink()
}

trait HasPeripheryNmiGenModuleImp extends LazyModuleImp {
  val outer: HasPeripheryNmiGen
  val nmiGen = outer.nmiGen.makeIO()(ValName(s"nmiGen"))
}