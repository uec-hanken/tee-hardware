package uec.teehardware.devices.sifiveblocks

import chisel3._
import chisel3.util.HasBlackBoxResource
import sifive.blocks.devices.uart._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.PeripheryBusKey
import uec.teehardware.TEEHWBaseSubsystem

trait HasTEEHWPeripheryUART {
  this: TEEHWBaseSubsystem =>

  // UART implementation. This is the same as HasPeripheryUART
  // TODO: This is done this way instead of "HasPeripheryUART" because we need to do a bind to tlclock
  val uartDevs = p(PeripheryUARTKey).map {
    val divinit = (p(PeripheryBusKey).dtsFrequency.get / 115200).toInt
    ps => UARTAttachParams(ps).attachTo(this)
  }
  val uartNodes = uartDevs.map { ps => ps.ioNode.makeSink }
  uartDevs.foreach { case ps =>
    //tlclock.bind(ps.device)
  }
}

trait HasTEEHWPeripheryUARTModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryUART

  // UART implementation
  val uart: Seq[UARTPortIO] = outer.uartNodes.zipWithIndex.map { case(n,i) => n.makeIO()(ValName(s"uart_$i")).asInstanceOf[UARTPortIO] }
}
