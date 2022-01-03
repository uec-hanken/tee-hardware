package uec.teehardware

import chisel3._
import chisel3.util._
import chisel3.experimental._

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

