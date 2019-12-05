package uec.keystoneAcc.devices.usb11hs

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripheryUSB11HSKey extends Field[USB11HSParams]

trait HasPeripheryUSB11HS { this: BaseSubsystem =>
  val usb11hsDev = USB11HS.attach(USB11HSAttachParams(p(PeripheryUSB11HSKey), sbus, ibus.fromAsync))
}

trait HasPeripheryUSB11HSBundle {
  val usb11hs: USB11HSPortIO
}

trait HasPeripheryUSB11HSModuleImp extends LazyModuleImp with HasPeripheryUSB11HSBundle {
  val outer: HasPeripheryUSB11HS
  val usb11hs = IO(outer.usb11hsDev.module.io.cloneType)
  usb11hs.suggestName("usb11hs")
  usb11hs <> outer.usb11hsDev.module.io
}