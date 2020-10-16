package uec.teehardware.devices.random

import scala.math._
import chisel3._
import chisel3.util._

class clock_enable_ring() extends Module{
  val io = IO(new Bundle{
    //Inputs
    val reset 	= Input(Bool())
    //Outputs
    val out 	= Output(Bool())
  })
  withReset(io.reset || reset.asBool()) {
    val count = new Counter(100000000)
    count.inc()
    io.out := false.B
    when(count.value < 50000000.U || io.reset) {
      io.out := true.B
    }
  }
}

class Source_And_Reference(val nref: Int = 3, val nsrc: Int = 9) extends Module {
  val io = IO(new Bundle{
    //Inputs
    val enable   = Input(Bool())
    //Outputs
    val pulse_ref= Output(Bool())
    val pulse_rng= Output(Bool())
  })
  val RO_ref = Module(new RingOscillator_top(nref))
  val RO_rng = Module(new RingOscillator_top(nsrc))

  RO_ref.io.enable := io.enable
  RO_rng.io.enable := io.enable
  io.pulse_ref     := RO_ref.io.pulse
  io.pulse_rng     := RO_rng.io.pulse
}

class TRNG(val nbits: Int = 8, val nref: Int = 3, val nsrc: Int = 9) extends Module {
  val io = IO(new Bundle {
    //Inputs
    val enable   = Input(Bool())
    val reset    = Input(Bool())

    //Outputs
    //	val data_out = Output(Bool())
    //	val data_out_n = Output(Bool())
    val out = Output(UInt(nbits.W))
    val d_out = Output(Bool())
    val d     = Output(Bool())
    //	val pulse_second = Output(Bool())
  })

  //val clk_divider = Module(new clock_enable_ring())
  //clk_divider.io.reset := io.reset
  val RO = Module(new Source_And_Reference(nref, nsrc))
  RO.io.enable := io.enable
  //io.pulse_second := clk_divider.io.out
  //RO.io.enable := clk_divider.io.out
  val capture_top = Module(new PFD_top())
  capture_top.io.clock_A := RO.io.pulse_rng.asClock
  capture_top.io.clock_B := RO.io.pulse_ref.asClock
  //io.data_out := capture_top.io.up
  //io.data_out_n := capture_top.io.down
  val counter_all = Module(new counter_ring(nbits))
  counter_all.io.enable := capture_top.io.down
  counter_all.io.reset  := io.reset
  io.out := counter_all.io.out
  io.d_out := capture_top.io.down
  io.d     := capture_top.io.up
}

object TRNGMain extends App {
  println("Generating TRNG")
  chisel3.Driver.execute(Array("--no-check-comb-loops","--target-dir", "generated"), () => new TRNG(3))
}




