package uec.teehardware.devices.opentitan.nmi_gen

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.util._
import uec.teehardware._
import uec.teehardware.devices.opentitan.alert._

case object PeripheryNmiGenKey extends Field[NmiGenParams](NmiGenParams(address = BigInt(0x64200000L)))

trait HasPeripheryNmiGen { this: TEEHWSubsystem with HasPeripheryAlert =>
  //println(escnode.toString)
  val nmiGenDev = p(WithAlertAndNMI).option(
    NmiGenAttachParams(p(PeripheryNmiGenKey), escnode, () => isAlert).attachTo(this))
  val nmiGen =
      nmiGenDev.map(_.ioNode.makeSink())
  if( !p(WithAlertAndNMI) ) { escnode := EscEmpty.applySource }
}

trait HasPeripheryNmiGenModuleImp extends LazyModuleImp {
  val outer: HasPeripheryNmiGen
  val nmiGen = outer.nmiGen.map(_.makeIO()(ValName(s"nmiGen")))
}