package uec.teehardware.devices.aes

import chisel3._
import chisel3.util._
import freechips.rocketchip.config._
import freechips.rocketchip.tile._
import freechips.rocketchip.rocket.constants._

class  AESROCC(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(opcodes) {
  override lazy val module = new AESROCCModuleImp(this)
}

class AESROCCModuleImp(outer: AESROCC)(implicit p: Parameters) extends LazyRoCCModuleImp(outer)
  with HasCoreParameters
  with MemoryOpConstants
{
  val cmd = Queue(io.cmd)

  // The command always is ready here. Is just like an ALU
  cmd.ready := true.B
  when(cmd.valid) {
    printf("AES ROCC triggered. Sending response")
  }

  // Functions I want to save
  val rdd = RegEnable(sboxGated(cmd.bits.rs1), cmd.fire())
  val funct = RegEnable(cmd.bits.inst.funct, cmd.fire())
  val rd = RegEnable(cmd.bits.inst.rd, cmd.fire())
  val resp_valid = RegInit(false.B)

  when(cmd.fire() && !resp_valid) {
    resp_valid := true.B
  } .elsewhen(io.resp.ready && resp_valid) {
    resp_valid := false.B
  }

  // PROC RESPONSE INTERFACE
  io.resp.valid := resp_valid
  io.resp.bits.rd := rd
  io.resp.bits.data := rdd // rs2 is ignored.. totally
  io.busy := cmd.valid
  // Be busy when have pending memory requests or committed possibility of pending requests
  io.interrupt := false.B
  // Set this true to trigger an interrupt on the processor (please refer to supervisor documentation)

  // MEMORY REQUEST INTERFACE
  io.mem.req.valid := false.B
  io.mem.req.bits.addr := 0.U
  io.mem.req.bits.tag := 0.U
  io.mem.req.bits.cmd := 0.U
  io.mem.req.bits.size := 0.U
  io.mem.req.bits.signed := false.B
  io.mem.req.bits.data := 0.U // we're not performing any stores...
  io.mem.req.bits.phys := false.B
}