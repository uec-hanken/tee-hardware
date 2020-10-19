package uec.teehardware.devices.random


import chisel3._
import chisel3.util._

class LFSR(val n_lfsr :Int) extends Module{
  val io = IO(new Bundle {
    //Input's
    val data_input  = Input(UInt(n_lfsr.W))
    val enable	= Input(Bool())
    //Output's
    val data_out    = Output(UInt(n_lfsr.W))
  })

  val data = LFSR8(io.enable,io.data_input)
  //val data_simple = data.asTypeOf(new Descon)
  //io.data_out := Cat(data_simple.data_0b,data_simple.data_1b,data_simple.data_2b,data_simple.data_3b,data_simple.data_4b,data_simple.data_5b,data_simple.data_6b,data_simple.data_7b)
  io.data_out := data
}

object LFSR8 {
  def apply(change_seed: Bool, seed : UInt): UInt = {
    val lfsr = RegInit(204.U(8.W))
    when (change_seed===true.B){ lfsr := seed
    }.otherwise{lfsr := Cat((lfsr(0)^lfsr(4)), lfsr(8-1,1))}
    lfsr
  }
}


class Descon extends Bundle{
  val data_0a = UInt(7.W)
  val data_0b = UInt(1.W)
  val data_1a = UInt(7.W)
  val data_1b = UInt(1.W)
  val data_2a = UInt(7.W)
  val data_2b = UInt(1.W)
  val data_3a = UInt(7.W)
  val data_3b = UInt(1.W)
  val data_4a = UInt(7.W)
  val data_4b = UInt(1.W)
  val data_5a = UInt(7.W)
  val data_5b = UInt(1.W)
  val data_6a = UInt(7.W)
  val data_6b = UInt(1.W)
  val data_7a = UInt(7.W)
  val data_7b = UInt(1.W)
}


object LFSRMain extends App {
  println("Generating test")
  chisel3.Driver.execute(Array("--no-check-comb-loops","--target-dir", "generated"), () => new LFSR(8))
}


