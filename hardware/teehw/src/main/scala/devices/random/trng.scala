package uec.teehardware.devices.random

import scala.math._
import chisel3._
import chisel3.util._
import freechips.rocketchip.util.DontTouch

class clock_enable_ring() extends Module{
  val io = IO(new Bundle{
    //Inputs
    val reset 	= Input(Bool())
    //Outputs
    val out 	= Output(Bool())
  })
  val reg = RegInit(0.U(26.W))
  when(io.reset === true.B){
    reg := 0.U
    io.out := true.B
  }.elsewhen(reg < 50000000.U){   //50000 //50000000
    reg    := reg + 1.U
    io.out := true.B
  }.elsewhen(reg > 50000000.U){ //50000 //50000000
    reg    := reg + 1.U
    io.out := false.B
  }.elsewhen(reg === 100000000.U){ //100000 //100000000
    reg    := 0.U
    io.out := false.B
  }.otherwise{
    reg    := reg + 1.U
    io.out := false.B
  }
}

class Source_And_Reference(val nref: Int = 27, val nsrc: Int = 9, val impl: String = "Simulation") extends Module {
  val io = IO(new Bundle{
    //Inputs
    val enable   = Input(Bool())
    //Outputs
    val pulse_ref= Output(Bool())
    val pulse_rng= Output(Bool())
  })

  // TODO: The location of the hints are fixed
  val RO_ref = Module(new RingOscillator_top(nref, "RO_ref", impl, ROLocHints(15, 158)))
  val RO_rng = Module(new RingOscillator_top(nsrc, "RO_rng", impl, ROLocHints(15, 161)))

  RO_ref.io.enable := io.enable
  RO_rng.io.enable := io.enable
  io.pulse_ref     := RO_ref.io.pulse
  io.pulse_rng     := RO_rng.io.pulse
}

class TRNG(val nbits: Int = 8, val nref: Int = 27, val nsrc: Int = 9, val impl: String = "Simulation") extends Module with DontTouch {
  val io = IO(new Bundle {
    //Inputs
    val enable   = Input(Bool())
    val reset    = Input(Bool())

    //Outputs
    val out_post = Output(UInt(nbits.W))
    val out_trng = Output(UInt(nbits.W))
    val d_out = Output(Bool())
    val d     = Output(Bool())
  })

  val RO = Module(new Source_And_Reference(nref, nsrc, impl))
  RO.io.enable := io.enable
  val capture_top = Module(new PFD_top())
  capture_top.io.clock_A := RO.io.pulse_rng.asClock
  capture_top.io.clock_B := RO.io.pulse_ref.asClock
  val counter_all = Module(new counter_ring(10)) // TODO: This is fixed
  counter_all.io.enable := capture_top.io.up
  counter_all.io.reset  := io.reset
  io.out_trng := counter_all.io.out
  io.d_out := capture_top.io.down
  io.d     := capture_top.io.up
  val post = Module(new LFSR(nbits))
  post.io.data_input:= counter_all.io.out
  post.io.enable    := capture_top.io.down
  io.out_post 		  := post.io.data_out
}

object TRNGMain extends App {
  println("Generating TRNG")
  chisel3.Driver.execute(Array("--no-check-comb-loops","--target-dir", "generated"), () => new TRNG(3))
}




