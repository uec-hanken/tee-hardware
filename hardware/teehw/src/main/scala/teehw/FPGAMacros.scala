package uec.teehardware

import chisel3._
import chisel3.util._
import chisel3.experimental._
import sifive.blocks.devices.pinctrl.BasePin
import sifive.fpgashells.ip.xilinx.{IBUFG, IOBUF}
import uec.teehardware.macros.ALT_IOBUF

//-------------------------------------------------------------------------
// GET and PUT for interfacing analog
//-------------------------------------------------------------------------

class PUT extends BlackBox with HasBlackBoxInline{
  val io = IO(new Bundle{
    val I = Input(Bool())
    val IO = Analog(1.W)
  })

  setInline("PUT.v",
    s"""module PUT  (I, IO);
       |	input I;
       |	inout IO;
       |
       |  assign IO = I;
       |
       |endmodule
       |""".stripMargin)
}

object PUT {
  def apply(i: Bool, o: Analog): Unit = {
    val io = Module(new PUT).io
    io.I := i
    attach(o, io.IO)
  }
}

class GET extends BlackBox with HasBlackBoxInline{
  val io = IO(new Bundle{
    val O = Output(Bool())
    val IO = Analog(1.W)
  })

  setInline("GET.v",
    s"""module GET  (O, IO);
       |	output O;
       |	inout IO;
       |
       |  assign O = IO;
       |
       |endmodule
       |""".stripMargin)
}

object GET {
  def apply(o: Analog): Bool = {
    val io = Module(new GET).io
    attach(o, io.IO)
    io.O
  }
}

class IOSim extends BlackBox with HasBlackBoxInline{
  val io = IO(new Bundle {
    val in = Input(Bool())
    val ctrl = Input(Bool())
    val out = Output(Bool())
    val io = Analog(1.W)
  })
  setInline("IOSim.v",
    s"""module IOSim  (ctrl, in, out, io);
       |	input in;
       |	input ctrl;
       |	output out;
       |	inout io;
       |
       |  assign io = (ctrl) ? in : 1'bz;
       |  assign out = (ctrl) ? in : (io == 1'bx) ? 1'b1 : io;
       |
       |endmodule
       |""".stripMargin)
}

object IOSim {
  def apply(io: Analog, port: BasePin): Unit = {
    val iosim = Module(new IOSim)
    attach(iosim.io.io, io)
    iosim.io.ctrl := port.o.oe
    iosim.io.in := port.o.oval
    port.i.ival := iosim.io.out
    port.i.po.foreach(_ := false.B)
  }
}

class XilinxGPIO extends RawModule with GenericTEEHWGPIO {
  val io = IO(new Bundle {
    val T = Input(Bool())
    val I = Input(Bool())
    val O = Output(Bool())
    val IO = Analog(1.W)
  })
  val pad = io.IO
  val i = io.I
  val o = io.O
  val oe = io.T
  val ie = None
  val ds = None
  val pue = None
  val mod = Module(new IOBUF)
  attach(mod.io.IO, io.IO)
  mod.io.I := io.I
  mod.io.T := !io.T // TODO: This is opposite. If going to do a Blackbox, need to override all functions
  io.O := mod.io.O
  override def ConnectAsClock(): Clock = {
    i := false.B
    oe := true.B
    IBUFG(o.asClock)
  }
}

class XilinxXTAL extends RawModule with GenericTEEHWXTAL {
  val io = IO(new Bundle {
    val O = Output(Clock())
    val I = Analog(1.W)
  })
  val xi = io.I
  val xo = None
  val xc = io.O
  val xe = None
  io.O := IBUFG(IOBUF(io.I).asClock)
}

case class XilinxIOLibraryParams() extends GenericIOLibraryParams {
  def analog() = Module(new TEEHWANALOG)
  def gpio() = Module(new XilinxGPIO)
  def input() = Module(new XilinxGPIO)
  def output() = Module(new XilinxGPIO)
  def crystal() = Module(new TEEHWXTAL)
}

class AlteraGPIO extends RawModule with GenericTEEHWGPIO {
  val io = IO(new Bundle {
    val oe = Input(Bool())
    val i = Input(Bool())
    val o = Output(Bool())
    val io = Analog(1.W)
  })
  val pad = io.io
  val i = io.i
  val o = io.o
  val oe = io.oe
  val ie = None
  val ds = None
  val pue = None
  val mod = Module(new ALT_IOBUF)
  attach(mod.io.io, io.io)
  mod.io.i := io.i
  mod.io.oe := io.oe
  io.o := mod.io.o
}

class AlteraXTAL extends RawModule with GenericTEEHWXTAL {
  val io = IO(new Bundle {
    val O = Output(Clock())
    val I = Analog(1.W)
  })
  val xi = io.I
  val xo = None
  val xc = io.O
  val xe = None
  io.O := ALT_IOBUF(io.I).asClock
}

case class AlteraIOLibraryParams() extends GenericIOLibraryParams {
  def analog() = Module(new TEEHWANALOG)
  def gpio() = Module(new AlteraGPIO)
  def input() = Module(new AlteraGPIO)
  def output() = Module(new AlteraGPIO)
  def crystal() = Module(new TEEHWXTAL)
}

class AntiBump(val n: Int) extends Module {
  val io = IO(new Bundle{
    val data = Input(Bool())
    val out = Output(Bool())
  })

  val regs = Reg(Vec(n, Bool()))
  regs(n-1) := io.data
  (0 until n-1) foreach (x => regs(x) := regs(x+1))
  val regs_and = regs.foldLeft(true.B)(_ && _)
  val regs_or = regs.foldLeft(false.B)(_ || _)
  val out_latch = Reg(Bool())
  val out_next = Wire(Bool())
  when(out_latch) {
    out_next := regs_or
  }.otherwise {
    out_next := regs_and
  }
  out_latch := out_next
  io.out := out_latch // Or out_next?
}

object AntiBump {
  def apply(clock: Clock, data: Bool, n: Int = 10, name: Option[String] = None): Bool = {
    withClockAndReset(clock, false.B) {
      val m = Module(new AntiBump(n))
      name.foreach(m.suggestName(_))
      m.io.data := data
      m.io.out
    }
  }
}