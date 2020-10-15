package uec.teehardware.devices.random

import scala.math._
import chisel3._
import chisel3.util._
import chisel3.experimental._

class RO_single() extends Module{
  val io = IO(new Bundle{
    //Inputs
    val input_1   = Input(Bool())
    val input_2   = Input(Bool())
    //Outputs
    val out       = Output(Bool())
  })

  val w1 = Wire(Bool())
  val w2 = Wire(Bool())
  val w3 = Wire(Bool())

  val nand_1 = Module(new trng_primitive_nand()).io
  val not_1  = Module(new trng_primitive_not()).io
  val not_2  = Module(new trng_primitive_not()).io

  nand_1.input_1 := io.input_1
  nand_1.input_2 := io.input_2
  w1             := nand_1.output_1
  dontTouch(w1)
  not_1.input_1  := w1
  w2             := not_1.output_1
  dontTouch(w2)
  not_2.input_1  := w2
  w3             := not_2.output_1
  dontTouch(w3)
  io.out         := w3
}

class RingOscillator_top(val edges :Int) extends Module {
  val io = IO(new Bundle {
    //Inputs
    val enable   = Input(Bool())
    //val reset_c1  = Input(Bool())
    //Outputs
    val pulse    = Output(Bool())
  })
  val RO_edges = VecInit(Seq.fill(edges)(Module(new RO_single()).io))
  var n = RO_edges.length
  (0 until n).map(i => RO_edges(i).input_1 := io.enable)
  (1 until n).map(i => RO_edges(i).input_2 := RO_edges(i-1).out)
  RO_edges(0).input_2 := RO_edges(n-1).out
  io.pulse := RO_edges(n-1).out
  //val reg = RegInit(0.U(1.W))
  //withClock(RO_edges(n-1).out.asClock){

  //val counter_one = Module(new clock_diviver_ring())
  //counter_one.io.reset := io.reset_c1
  //val counter_two = Module(new counter_ring())
  //counter_two.io.enable := counter_one.io.out
  //counter_two.io.reset  := io.reset_c1
  //io.pulse := counter_two.io.out
}

class trng_primitive_nand extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle(){
    //Inputs
    val input_1 = Input(Bool())
    val input_2 = Input(Bool())
    //Outputs
    val output_1 = Output(Bool())
  })
  setResource("/trng/trng_primitive_nand.v")
}

class trng_primitive_not extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle(){
    //Inputs
    val input_1 = Input(Bool())
    //Outputs
    val output_1 = Output(Bool())
  })
  setResource("/trng/trng_primitive_not.v")
}

class clock_diviver_ring(val wrap: Int = 100000000) extends Module{
  val io = IO(new Bundle{
    //Inputs
    val reset 	= Input(Bool())
    //Outputs
    val out 	= Output(Bool())
  })

  withReset(io.reset || reset.asBool()) {
    val count = new Counter(wrap)
    io.out := count.inc()
  }
}

class counter_ring(val n: Int = 10) extends Module {
  val io = IO(new Bundle {
    //Inputs
    val reset = Input(Bool())
    val enable = Input(Bool())
    //Outputs
    val out = Output(UInt(n.W))
  })

  withReset(io.reset || reset.asBool()) {
    val count = new Counter(1 << n)
    when(io.enable) { count.inc() }
    io.out := count.value
  }
}

object RingOscillatorMain extends App {
  println("Generating RO")
  chisel3.Driver.execute(Array("--no-check-comb-loops","--target-dir", "generated"), () => new RingOscillator_top(21))
}
