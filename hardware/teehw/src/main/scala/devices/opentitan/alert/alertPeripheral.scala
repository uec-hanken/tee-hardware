package uec.teehardware.devices.opentitan.alert

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.util.HeterogeneousBag
import uec.teehardware._

case object PeripheryAlertKey extends Field[AlertParams](AlertParams(address = BigInt(0x64100000L)))

trait HasPeripheryAlert { this: TEEHWSubsystem =>
  //println(alertnode.toString)
  val alertDev =
    AlertAttachParams(p(PeripheryAlertKey), alertnode, escnode).attachTo(this)
  val alert =
      alertDev.ioNode.makeSink()
}

trait HasPeripheryAlertModuleImp extends LazyModuleImp {
  val outer: HasPeripheryAlert
  val alert = outer.alert.makeIO()(ValName(s"alert"))
}