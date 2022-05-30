package uec.teehardware.devices.usb11hs

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.experimental.{Analog, attach}
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.util.HeterogeneousBag
import uec.teehardware.GenericIOLibraryParams

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

class USB11HSIO extends Bundle {
  // USB clock 48 MHz
  val usbClk = Analog(1.W)
  // USB phy signals
  val USBWireDataIn = Vec(2, Analog(1.W))
  val USBWireDataOut = Vec(2, Analog(1.W))
  //val USBWireDataOutTick = Analog(1.W)
  //val USBWireDataInTick = Analog(1.W)
  val USBWireCtrlOut = Analog(1.W)
  val USBFullSpeed = Analog(1.W)
  //val USBDPlusPullup = Analog(1.W)
  //val USBDMinusPullup = Analog(1.W)
  //val vBusDetect = Analog(1.W)
}

trait HasPeripheryUSB11HSChipImp extends RawModule {
  implicit val p: Parameters
  val clock: Clock
  val reset: Bool
  val IOGen: GenericIOLibraryParams
  val system: HasPeripheryUSB11HSModuleImp

  val usb11hs = system.usb11hs.zipWithIndex.map{case(sysusb11hs, i) =>
    val usb11hs = IO(new USB11HSIO)
    val usbClk = IOGen.gpio()
    val USBWireDataIn_0 = IOGen.gpio()
    val USBWireDataIn_1 = IOGen.gpio()
    val USBWireDataOut_0 = IOGen.gpio()
    val USBWireDataOut_1 = IOGen.gpio()
    val USBWireCtrlOut = IOGen.gpio()
    val USBFullSpeed = IOGen.gpio()

    usbClk.suggestName(s"usbClk_${i}")
    USBWireDataIn_0.suggestName(s"USBWireDataIn_0_${i}")
    USBWireDataIn_1.suggestName(s"USBWireDataIn_1_${i}")
    USBWireDataOut_0.suggestName(s"USBWireDataOut_0_${i}")
    USBWireDataOut_1.suggestName(s"USBWireDataOut_1_${i}")
    USBWireCtrlOut.suggestName(s"USBWireCtrlOut_${i}")
    USBFullSpeed.suggestName(s"USBFullSpeed_${i}")

    attach(usb11hs.usbClk, usbClk.pad) // TODO: Doing the XI/XO stuff
    attach(usb11hs.USBWireDataIn(0), USBWireDataIn_0.pad)
    attach(usb11hs.USBWireDataIn(1), USBWireDataIn_1.pad)
    attach(usb11hs.USBWireDataOut(0), USBWireDataOut_0.pad)
    attach(usb11hs.USBWireDataOut(1), USBWireDataOut_1.pad)
    attach(usb11hs.USBWireCtrlOut, USBWireCtrlOut.pad)
    attach(usb11hs.USBFullSpeed, USBFullSpeed.pad)

    sysusb11hs.usbClk := usbClk.ConnectAsClock()
    sysusb11hs.USBWireDataIn(0) := USBWireDataIn_0.ConnectAsClock()
    sysusb11hs.USBWireDataIn(1) := USBWireDataIn_1.ConnectAsClock()
    sysusb11hs.USBWireDataOut(0) := USBWireDataOut_0.ConnectAsClock()
    sysusb11hs.USBWireDataOut(1) := USBWireDataOut_1.ConnectAsClock()
    sysusb11hs.USBWireCtrlOut := USBWireCtrlOut.ConnectAsClock()
    sysusb11hs.USBFullSpeed := USBFullSpeed.ConnectAsClock()

    usb11hs
  }
}
