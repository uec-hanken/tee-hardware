package uec.teehardware.devices.sifiveblocks

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.experimental.{Analog, attach}
import chisel3.util.HasBlackBoxResource
import sifive.blocks.devices.uart._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.PeripheryBusKey
import sifive.blocks.devices.pinctrl.{BasePin, EnhancedPin}
import uec.teehardware.{GenericIOLibraryParams, TEEHWBaseSubsystem}

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

class UARTPIN() extends Bundle {
  val TXD = Analog(1.W)
  val RXD = Analog(1.W)
}

trait HasTEEHWPeripheryUARTChipImp extends RawModule {
  implicit val p: Parameters
  val clock: Clock
  val reset: Bool
  val IOGen: GenericIOLibraryParams
  val system: HasTEEHWPeripheryUARTModuleImp

  val uart = system.uart.zipWithIndex.map{case(sysuart, i) =>
    val uart = IO(new UARTPIN)
    val TXD = IOGen.gpio()
    val RXD = IOGen.gpio()

    TXD.suggestName(s"TXD_${i}")
    RXD.suggestName(s"RXD_${i}")

    attach(uart.TXD, TXD.pad)
    attach(uart.RXD, RXD.pad)

    val uartpin = Wire(new UARTSignals(() => new BasePin))
    withClockAndReset(clock, reset) { UARTPinsFromPort.apply(uartpin, sysuart, clock, reset, 3) }

    TXD.ConnectPin(uartpin.txd)
    RXD.ConnectPin(uartpin.rxd)

    uart
  }
}