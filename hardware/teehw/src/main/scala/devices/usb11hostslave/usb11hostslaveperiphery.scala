package uec.teehardware.devices.usb11hs

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.util.HeterogeneousBag

case object PeripheryUSB11HSKey extends Field[List[USB11HSParams]](List())

trait HasPeripheryUSB11HS { this: BaseSubsystem =>
  val usb11hsDevs = p(PeripheryUSB11HSKey).map { case key =>
    USB11HSAttachParams(key).attachTo(this)
  }
  val usb11hs = usb11hsDevs.map {
    case i =>
      i.ioNode.makeSink()
  }
}

trait HasPeripheryUSB11HSModuleImp extends LazyModuleImp {
  val outer: HasPeripheryUSB11HS
  val usb11hs = outer.usb11hs.zipWithIndex.map{
    case (n,i) => n.makeIO()(ValName(s"usb1_$i"))
  }
}