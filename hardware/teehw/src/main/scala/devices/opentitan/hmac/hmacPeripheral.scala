package uec.teehardware.devices.opentitan.hmac

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.util.HeterogeneousBag
import uec.teehardware._

case object PeripheryHMACKey extends Field[List[HMACParams]](List())

trait HasPeripheryHMAC { this: TEEHWSubsystem =>
  val hmacDevs = p(PeripheryHMACKey).map { case key =>
    HMACAttachParams(key, alertnode).attachTo(this)
  }
  val hmac = hmacDevs.map {
    case i =>
      i.ioNode.makeSink()
  }
}

trait HasPeripheryHMACModuleImp extends LazyModuleImp {
  val outer: HasPeripheryHMAC
  val hmac = outer.hmac.zipWithIndex.map{
    case (n,i) => n.makeIO()(ValName(s"hmac_$i"))
  }
}