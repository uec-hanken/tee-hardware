package uec.teehardware

import chisel3._
import chisel3.util._
import chisel3.experimental._
import sifive.blocks.devices.pinctrl.BasePin

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
       |assign IO = I;
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
       |assign O = IO;
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
       |assign io = (ctrl) ? in : 1'bz;
       |assign out = (ctrl) ? in : (io == 1'bx) ? 1'b1 : io;
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


