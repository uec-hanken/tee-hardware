package uec.teehardware.devices.sifiveblocks

import chisel3._
import chisel3.util.HasBlackBoxResource
import sifive.blocks.devices.gpio._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.PeripheryBusKey
import uec.teehardware.TEEHWBaseSubsystem

trait HasTEEHWPeripheryGPIO {
  this: TEEHWBaseSubsystem =>

  // GPIO implementation. This is the same as HasPeripheryGPIO
  // TODO: This is done this way instead of "HasPeripheryGPIO" because we need to do a bind to tlclock
  val gpioDevs = p(PeripheryGPIOKey).map {
    ps => GPIOAttachParams(ps).attachTo(this)
  }
  val (gpioNodes, gpioIofs) = gpioDevs.map { ps => (ps.ioNode.makeSink, ps.iofPort) }.unzip
  gpioDevs.foreach { case ps =>
    //tlclock.bind(ps.device)
  }
}

trait HasTEEHWPeripheryGPIOModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryGPIO

  // GPIO implementation
  val gpio: Seq[GPIOPortIO] = outer.gpioNodes.zipWithIndex.map { case(n,i) => n.makeIO()(ValName(s"gpio_$i")).asInstanceOf[GPIOPortIO] }

  outer.gpioIofs.foreach{iofOpt => iofOpt.foreach{iof =>
    iof.getWrappedValue.iof_0.foreach(_.default())
    iof.getWrappedValue.iof_1.foreach(_.default())
  }}
}
