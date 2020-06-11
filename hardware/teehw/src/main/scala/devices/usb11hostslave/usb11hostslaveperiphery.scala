package uec.teehardware.devices.usb11hs

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.util.HeterogeneousBag

case object PeripheryUSB11HSKey extends Field[List[USB11HSParams]]

trait HasPeripheryUSB11HS { this: BaseSubsystem =>
  val usb11hsDevs = p(PeripheryUSB11HSKey).map { case key =>
    USB11HSAttachParams(key).attachTo(this)
  }
}

trait HasPeripheryUSB11HSBundle {
  val usb11hs: List[USB11HSPortIO]
}

trait HasPeripheryUSB11HSModuleImp extends LazyModuleImp with HasPeripheryUSB11HSBundle {
  val outer: HasPeripheryUSB11HS
  val usb11hs = outer.usb11hsDevs.zipWithIndex.map{case (dev,i) =>
    val port = IO(new HeterogeneousBag(Seq(new USB11HSPortIO)))
    port.suggestName("usb11hs_" + i)
    port <> dev.module.io
    port.head
  }

}