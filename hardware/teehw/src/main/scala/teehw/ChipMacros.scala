package uec.teehardware

import chipsalliance.rocketchip.config._
import chisel3._
import chisel3.util._
import chisel3.experimental._
import sifive.blocks.devices.pinctrl._

trait GenericTEEHWGPIO extends BaseModule {
  val pad: Analog
  val i: Bool
  val o: Bool
  val oe: Bool
  val ie: Option[Bool]
  val ds: Option[Bool]
  val pue: Option[Bool]
  def ConnectPin(pin: BasePin, pullup: Boolean = false): Unit = {
    i := pin.o.oval
    oe := pin.o.oe
    pin.i.ival := o
    ds.foreach(_ := false.B)
    pue.foreach(_ := pullup.B)
    ie.foreach(_ := pin.o.ie)
    pin.i.po.foreach(_ := false.B)
  }
  def ConnectPin(pin: EnhancedPin): Unit = {
    i := pin.o.oval
    oe := pin.o.oe
    pin.i.ival := o
    ds.foreach(_ := pin.o.ds)
    pue.foreach(_ := pin.o.pue)
    ie.foreach(_ := pin.o.ie)
    pin.i.po.foreach(_ := false.B)
  }
  def ConnectAsOutput(in: Bool): Unit = {
    i := in
    oe := true.B
    // o is ignored
    ds.foreach(_ := false.B)
    pue.foreach(_ := false.B)
    ie.foreach(_ := false.B)
  }
  def ConnectTristate(in: Bool, en: Bool): UInt = {
    i := in
    oe := en
    ds.foreach(_ := false.B)
    pue.foreach(_ := false.B)
    ie.foreach(_ := false.B)
    o
  }
  def ConnectAsInput(pullup: Boolean = false): Bool = {
    i := false.B
    oe := true.B
    ds.foreach(_ := false.B)
    pue.foreach(_ := pullup.B)
    ie.foreach(_ := true.B)
    o
  }
  def ConnectAsClock(): Clock = {
    i := false.B
    oe := true.B
    ds.foreach(_ := false.B)
    pue.foreach(_ := false.B)
    ie.foreach(_ := true.B)
    o.asClock
  }
  def ConnectAsClock(en: Bool): Clock = {
    i := false.B
    oe := true.B
    ds.foreach(_ := false.B)
    pue.foreach(_ := false.B)
    ie.foreach(_ := en)
    o.asClock
  }
}

trait GenericTEEHWXTAL extends BaseModule {
  val xi: Analog
  val xo: Option[Analog]
  val xc: Clock
  val xe: Option[Bool]
  def ConnectPin(pin: BasePin): Unit = {
    //pin.o.oval
    //pin.o.oe
    pin.i.ival := xc.asBool
    xe.foreach(_ := pin.o.ie)
    pin.i.po.foreach(_ := false.B)
  }
  def ConnectPin(pin: EnhancedPin): Unit = {
    pin.i.ival := xc.asBool
    xe.foreach(_ := pin.o.ie)
    pin.i.po.foreach(_ := false.B)
  }
  def ConnectAsInput(): Bool = {
    xe.foreach(_ := true.B)
    xc.asBool
  }
  def ConnectAsClock(): Clock = {
    xe.foreach(_ := true.B)
    xc
  }
  def ConnectAsClock(en: Bool): Clock = {
    xe.foreach(_ := en)
    xc
  }
}

trait GenericTEEHWAnalog extends BaseModule {
  val pad: Analog
  val core: Option[Analog]
  def ConnectCore(ana: Analog): Unit = {
    if(core.isDefined) core.foreach(attach(_, ana)) else attach(pad, ana)
  }
}

trait GenericIOLibraryParams {
  def analog(): GenericTEEHWAnalog
  def gpio():   GenericTEEHWGPIO
  def input():  GenericTEEHWGPIO
  def output(): GenericTEEHWGPIO
  def crystal(): GenericTEEHWXTAL
}

class TEEHWGPIO extends BlackBox with HasBlackBoxInline with GenericTEEHWGPIO {
  val io = IO(new Bundle {
    val IE = Input(Bool())
    val OE = Input(Bool())
    val DS = Input(Bool())
    val PE = Input(Bool())
    val I = Input(Bool())
    val O = Output(Bool())
    val PAD = Analog(1.W)
  })
  val pad = io.PAD
  val i = io.I
  val o = io.O
  val oe = io.OE
  val ie = Some(io.IE)
  val ds = Some(io.DS)
  val pue = Some(io.PE)

  setInline("TEEHWGPIO.v",
    s"""module TEEHWGPIO (IE, OE, DS, PE, O, I, PAD);
       |	input IE, OE, DS, PE, I;
       |	output O;
       |	inout PAD;
       |
       |  assign PAD = OE? I : 1'bz;
       |  assign O   = PAD;
       |
       |endmodule
       |""".stripMargin)
}

class TEEHWANALOG extends BlackBox with HasBlackBoxInline with GenericTEEHWAnalog {
  val io = IO(new Bundle {
    val PAD = Analog(1.W)
  })
  val pad = io.PAD
  val core = None

  setInline("TEEHWANALOG.v",
    s"""module TEEHWANALOG (PAD);
       |	inout PAD;
       |endmodule
       |""".stripMargin)
}

class TEEHWXTAL extends BlackBox with HasBlackBoxInline with GenericTEEHWXTAL {
  val io = IO(new Bundle {
    val XE = Input(Bool())
    val XC = Output(Clock())
    val XI = Analog(1.W)
    val XO = Analog(1.W)
  })
  val xi = io.XI
  val xo = Some(io.XO)
  val xc = io.XC
  val xe = Some(io.XE)

  setInline("TEEHWXTAL.v",
    s"""module TEEHWXTAL (XE, XC, XI, XO);
       |	input XE;
       |	output XC;
       |	inout XI, XO;
       |
       |  assign XC  = XE ? XI : 1'b0;
       |  assign XO  = XE ? !XI : 1'b0;
       |
       |endmodule
       |""".stripMargin)
}

case class IOLibraryParams() extends GenericIOLibraryParams {
  def analog() = Module(new TEEHWANALOG)
  def gpio() = Module(new TEEHWGPIO)
  def input() = Module(new TEEHWGPIO)
  def output() = Module(new TEEHWGPIO)
  def crystal() = Module(new TEEHWXTAL)
}

case object IOLibrary extends Field[GenericIOLibraryParams](IOLibraryParams())

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
    bools.asUInt
  }
  def apply(pins: Vec[BasePin], bools: UInt) : Unit = {
    fromVec(pins, VecInit(bools.asBools))
  }
}
