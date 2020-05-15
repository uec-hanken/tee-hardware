package uec.keystoneAcc.devices.usb11hs

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripheryUSB11HSKey extends Field[List[USB11HSParams]]

trait HasPeripheryUSB11HS { this: BaseSubsystem =>
  val usb11hsDevs = p(PeripheryUSB11HSKey).map { case key =>
    USB11HS.attach(USB11HSAttachParams(key, pbus, ibus.fromAsync))
  }
}

trait HasPeripheryUSB11HSBundle {
  val usb11hs: List[USB11HSPortIO]
}

trait HasPeripheryUSB11HSModuleImp extends LazyModuleImp with HasPeripheryUSB11HSBundle {
  val outer: HasPeripheryUSB11HS
  val usb11hs = outer.usb11hsDevs.zipWithIndex.map{case (dev,i) =>
    val usb11hs = IO(dev.module.io.cloneType)
    usb11hs.suggestName("usb11hs_" + i)
    usb11hs <> dev.module.io
    usb11hs
  }

}