package uec.teehardware.devices.opentitan.alert

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.util._
import uec.teehardware._

case object PeripheryAlertKey extends Field[AlertParams](AlertParams(address = BigInt(0x64100000L)))

trait HasPeripheryAlert { this: TEEHWBaseSubsystem =>
  //println(alertnode.toString)
  val alertDev = p(WithAlertAndNMI).option(
    AlertAttachParams(p(PeripheryAlertKey), alertnode, escnode).attachTo(this))
  val alert =
      alertDev.map(_.ioNode.makeSink())
  def isAlert: Boolean = if( p(WithAlertAndNMI) ) alertDev.get.isAlert else false
}

trait HasPeripheryAlertModuleImp extends LazyModuleImp {
  val outer: HasPeripheryAlert
  val alert = outer.alert.map(_.makeIO()(ValName(s"alert")))
}