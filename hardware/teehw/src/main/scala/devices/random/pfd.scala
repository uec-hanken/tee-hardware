package uec.teehardware.devices.random

import scala.math._
import chisel3._
import chisel3.util._

class PFD_top extends Module {
  val io = IO(new Bundle{
    val clock_A = Input(Clock())
    val clock_B = Input(Clock())
    val up      = Output(Bool())
    val down    = Output(Bool())
  })

  val PFD_capture = Module(new PFD_capture())
  PFD_capture.io.clock_A := io.clock_A
  PFD_capture.io.clock_B := io.clock_B
  val GR = VecInit(Seq.fill(2)(Module(new Glitch_removal()).io))
  GR(0).in 	    := PFD_capture.io.UP
  GR(1).in 	    := PFD_capture.io.DOWN
  val SR = VecInit(Seq.fill(2)(Module(new Shift_register()).io))
  SR(0).clock := GR(0).out.asClock
  SR(0).reset := GR(1).out
  SR(1).clock := GR(1).out.asClock
  SR(1).reset := GR(0).out
  //io.up 		    := SR(0).out
  //io.down  	    := SR(1).out
  val latch_sr = Module(new trng_primitive_latch()).io
  latch_sr.S := SR(0).out
  latch_sr.R := SR(1).out
  io.up      := latch_sr.Q
  io.down    := latch_sr.Qbar
}

class Glitch_removal () extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle{
    val in = Input(Bool())
    val out = Output(Bool())
  })
  setInline("Glitch_removal.v",
    s"""module Glitch_removal(
       |  input in,
       |  output out
       |  );
       |
       |  (* KEEP = "true", DONT_TOUCH = "yes" *) wire w1 /* synthesis keep */;
       |  assign w1 = in | in;
       |  assign out = in & w1;
       |   );
       |endmodule
       |
       |""".stripMargin)
  //val w1 = Wire(Bool())
  //dontTouch(w1)
  //w1 := io.in | io.in
  //io.out := io.in & w1

}

class Shift_register() extends Module {
  val io = IO(new Bundle{
    val reset   = Input(Bool())
    val clock   = Input(Clock())
    val out     = Output(Bool())
  })
  val flops = VecInit(Seq.fill(2)(Module(new trng_primitive_async_reg()).io))
  var n = flops.length
  (0 until n).map(i => flops(i).reset := io.reset)
  (0 until n).map(i => flops(i).clock := io.clock)
  flops(0).d := true.B
  flops(1).d := flops(0).q
  io.out     := flops(1).q

}

class PFD_capture extends Module {
  val io = IO(new Bundle{
    val clock_A = Input(Clock())
    val clock_B = Input(Clock())
    val UP      = Output(Bool())
    val DOWN    = Output(Bool())

  } )

  val reset_t = Wire(Bool())
  val flops = VecInit(Seq.fill(2)(Module(new trng_primitive_async_reg()).io))
  var n = flops.length
  (0 until n).map(i => flops(i).d := true.B)
  flops(0).clock := io.clock_A
  flops(1).clock := io.clock_B
  reset_t        := flops(0).q & flops(1).q
  (0 until n).map(i => flops(i).reset := reset_t)

  io.UP := flops(0).q & flops(1).q_n
  io.DOWN := flops(1).q & flops(0).q_n

}

class trng_primitive_async_reg(val impl: String = "Simulation") extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle() {
    val d 	= Input(Bool())
    val reset   = Input(Bool())
    val clock   = Input(Clock())
    val q 	= Output(Bool())
    val q_n 	= Output(Bool())
  })
  // TODO: Depending of "impl", should change the inline
  setInline("trng_primitive_async_reg.v",
    s"""
       |module trng_primitive_async_reg( clock, reset, d, q, q_n);
       |  input clock, reset, d;
       |  output q,q_n;
       |
       |  reg q;
       |  wire clock, reset, d, q_n;
       |
       |  assign q_n = ~q;
       |
       |  always @ (posedge clock or posedge reset)
       |  if (reset) begin
       |    q <= 1'b0;
       |  end else begin
       |    q <= d;
       |  end
       |
       |endmodule
       |""".stripMargin)
}

class trng_primitive_latch(val impl: String = "Simulation") extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle(){
    val S      = Input(Bool())
    val R      = Input(Bool())
    val Q      = Output(Bool())
    val Qbar  = Output(Bool())
  })
  setInline("trng_primitive_latch.v",
    s"""
       |// Same as Sifive's SR latch, but with Qbar
       |// See LICENSE for license details.
       |module trng_primitive_latch(
       |	input R,
       |	input S,
       |	output Q,
       |	output Qbar
       |);
       |
       |  reg latch;
       |
       |  // synopsys async_set_reset "set"
       |  // synopsys one_hot "set, reset"
       |  always @(S or R)
       |  begin
       |    if (S)
       |      latch = 1'b1;
       |    else if (R)
       |      latch = 1'b0;
       |  end
       |
       |  assign Q = latch;
       |  assign Qbar = ~latch;
       |endmodule
       |
       |
       |""".stripMargin)
}

object PFDMain extends App {
  println("Generating PFD")
  chisel3.Driver.execute(Array("--no-check-comb-loops","--target-dir", "generated"), () => new PFD_top())
}




