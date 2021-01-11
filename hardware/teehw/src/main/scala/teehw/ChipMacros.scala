package uec.teehardware

import chisel3._
import chisel3.util._
import chisel3.experimental._
import sifive.blocks.devices.pinctrl._

// The port of the GPIOs
class GPIO_24_A_PORT extends Bundle {
  val IE = Input(Bool())
  val OE = Input(Bool())
  val DS = Input(Bool())
  val PE = Input(Bool())
  val O = Input(Bool())
  val I = Output(Bool())
  val PAD = Analog(1.W)
}

// The blackbox instantiation for the GPIOs
class GPIO_24_A extends BlackBox {
  val io = IO(new GPIO_24_A_PORT)
}

// BasePin or ExtendedPin to the GPIO port conversion
object PinToGPIO_24_A {
  def apply(io: GPIO_24_A_PORT, pin: EnhancedPin): Unit = {
    io.DS := pin.o.ds
    io.PE := pin.o.pue
    io.O := pin.o.oval
    io.OE := pin.o.oe
    io.IE := pin.o.ie
    pin.i.ival := io.I
    pin.i.po.foreach(_ := false.B) // TODO: What is this? This is new, and there is no info in GPIO or similar
  }
  def apply(io: GPIO_24_A_PORT, pin: BasePin, pullup: Boolean = false): Unit = {
    io.DS := false.B
    io.PE := pullup.B
    io.O := pin.o.oval
    io.OE := pin.o.oe
    io.IE := pin.o.ie
    pin.i.ival := io.I
    pin.i.po.foreach(_ := false.B) // TODO: What is this? This is new, and there is no info in GPIO or similar
  }
  def asOutput(io: GPIO_24_A_PORT, in: Bool): Unit = {
    io.DS := false.B
    io.PE := false.B
    io.O := in
    io.OE := true.B
    io.IE := false.B
    //io.I ignored
  }
  def asInput(io: GPIO_24_A_PORT, pullup: Boolean = false): Bool = {
    io.DS := false.B
    io.PE := pullup.B
    io.O := false.B
    io.OE := false.B
    io.IE := true.B
    io.I
  }
}

class XTAL_DRV extends BlackBox {
  val io = IO(new Bundle{
    val E = Input(Bool())
    val C = Output(Clock())
    val XP = Analog(1.W)
    val XN = Analog(1.W)
  })
}

object BasePinToRegular {
  def apply(pin: BasePin) : Bool = {
    pin.i.ival := false.B
    pin.i.po.foreach(_ := false.B) // TODO: What is this? This is new, and there is no info in GPIO or similar
    pin.o.oval
  }
  def apply(pin: BasePin, b: Bool) = {
    pin.i.ival := b
    pin.i.po.foreach(_ := false.B) // TODO: What is this? This is new, and there is no info in GPIO or similar
  }
  def asVec(pins: Vec[BasePin]) : Vec[Bool] = {
    val bools = Wire(Vec(pins.length, Bool()))
    (bools zip pins).foreach{
      case (b, pin) =>
        b := apply(pin)
    }
    bools
  }
  def fromVec(pins: Vec[BasePin], bools: Vec[Bool]): Unit = {
    (bools zip pins).foreach{
      case (b, pin) =>
        apply(pin, b)
    }
  }
  def apply(pins: Vec[BasePin]) : UInt = {
    val bools: Vec[Bool] = asVec(pins)
    bools.asUInt()
  }
  def apply(pins: Vec[BasePin], bools: UInt) : Unit = {
    fromVec(pins, VecInit(bools.toBools))
  }
}
