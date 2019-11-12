package uec.keystoneAcc.devices.sha3

import chisel3._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._

case class SHA3Params(
                       address: BigInt,
                       width: Int)

class SHA3PortIO extends Bundle {
}

class keccak extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val reset = Input(Bool())
    val in = Input(UInt(64.W))
    val in_ready = Input(Bool())
    val is_last = Input(Bool())
    val byte_num = Input(UInt(3.W))
    val buffer_full = Output(Bool())
    val out = Output(UInt(512.W))
    val out_ready = Output(Bool())
  })
}

abstract class SHA3(busWidthBytes: Int, val c: SHA3Params, divisorInit: Int = 0)
                   (implicit p: Parameters)
  extends IORegisterRouter(
    RegisterRouterParams(
      name = "sha3",
      compat = Seq("uec,sha3-0"),
      base = c.address,
      beatBytes = busWidthBytes),
    new SHA3PortIO
  )
    with HasInterruptSources {

  def nInterrupts = 1

  ResourceBinding {
    Resource(ResourceAnchors.aliases, "sha3").bind(ResourceAlias(device.label))
  }

  lazy val module = new LazyModuleImp(this) {
    interrupts(0) := false.B

    // The input value
    val data = Wire(UInt(64.W))
    val datas = Reg(Vec(2, UInt(32.W)))
    data := datas.asUInt
    val datab = Wire(Vec(8, UInt(8.W)))
    datab.zipWithIndex.foreach{case (b, i) => b := data((8-i)*8-1, (7-i)*8)}

    // Reg and Status
    val commit = WireInit(false.B)
    val last = WireInit(false.B)
    val byte_num = RegInit(0.U(3.W))
    val rst = WireInit(false.B)
    val busy = Wire(Bool())
    val buff_full = Wire(Bool())
    val out_notready = Wire(Bool())
    busy := buff_full || out_notready
    val reg_and_status = Seq(
      RegField(3, byte_num, RegFieldDesc("byte_num","Number of Bytes (1 to 7, 0 is all)", reset=Some(0))),
      RegField(5),
      RegField.r(1, busy, RegFieldDesc("busy","Busy", volatile=true)),
      RegField.r(1, buff_full, RegFieldDesc("buff_full","Buffer Full", volatile=true)),
      RegField.r(1, out_notready, RegFieldDesc("out_notready","Output Not Ready", volatile=true)),
      RegField(5),
      RegField(1, commit, RegFieldDesc("commit","Commit Input", volatile=true)),
      RegField(1, last, RegFieldDesc("last","Last Data", volatile=true)),
      RegField(6),
      RegField(1, rst, RegFieldDesc("reset","Reset Calculation", volatile=true)),
    )

    // Out
    val out_ready = Wire(Bool())
    out_notready := !out_ready
    val out = Wire(UInt(512.W))
    val outb = Wire(Vec(64, UInt(8.W)))
    // Reorder the output hash
    outb.zipWithIndex.foreach{case (b, i) => b := out((64-i)*8-1, (63-i)*8)}
    val out_hash = outb.asUInt holdUnless out_ready

    // The Verilog module instantiation
    val core = Module(new keccak)
    core.io.clk := clock
    core.io.reset := reset.toBool() || rst
    core.io.in := datab.asUInt
    core.io.in_ready := commit
    core.io.is_last := last
    core.io.byte_num := byte_num
    buff_full := core.io.buffer_full
    out_ready := core.io.out_ready
    out := core.io.out

    // Memory map registers
    regmap(
      SHA3CtrlRegs.data0 -> Seq(RegField(32, datas(0), RegFieldDesc("data0","Input Value 0"))),
      SHA3CtrlRegs.data1 -> Seq(RegField(32, datas(1), RegFieldDesc("data1","Input Value 1"))),
      SHA3CtrlRegs.reg_status -> reg_and_status,
      SHA3CtrlRegs.out_hash_0 -> Seq(RegField.r(32, out_hash(1*32-1,0*32), RegFieldDesc("out_hash_0","Output SHA3 hash 0"))),
      SHA3CtrlRegs.out_hash_1 -> Seq(RegField.r(32, out_hash(2*32-1,1*32), RegFieldDesc("out_hash_1","Output SHA3 hash 1"))),
      SHA3CtrlRegs.out_hash_2 -> Seq(RegField.r(32, out_hash(3*32-1,2*32), RegFieldDesc("out_hash_2","Output SHA3 hash 2"))),
      SHA3CtrlRegs.out_hash_3 -> Seq(RegField.r(32, out_hash(4*32-1,3*32), RegFieldDesc("out_hash_3","Output SHA3 hash 3"))),
      SHA3CtrlRegs.out_hash_4 -> Seq(RegField.r(32, out_hash(5*32-1,4*32), RegFieldDesc("out_hash_4","Output SHA3 hash 4"))),
      SHA3CtrlRegs.out_hash_5 -> Seq(RegField.r(32, out_hash(6*32-1,5*32), RegFieldDesc("out_hash_5","Output SHA3 hash 5"))),
      SHA3CtrlRegs.out_hash_6 -> Seq(RegField.r(32, out_hash(7*32-1,6*32), RegFieldDesc("out_hash_6","Output SHA3 hash 6"))),
      SHA3CtrlRegs.out_hash_7 -> Seq(RegField.r(32, out_hash(8*32-1,7*32), RegFieldDesc("out_hash_7","Output SHA3 hash 7"))),
      SHA3CtrlRegs.out_hash_8 -> Seq(RegField.r(32, out_hash(9*32-1,8*32), RegFieldDesc("out_hash_8","Output SHA3 hash 8"))),
      SHA3CtrlRegs.out_hash_9 -> Seq(RegField.r(32, out_hash(10*32-1,9*32), RegFieldDesc("out_hash_9","Output SHA3 hash 9"))),
      SHA3CtrlRegs.out_hash_a -> Seq(RegField.r(32, out_hash(11*32-1,10*32), RegFieldDesc("out_hash_a","Output SHA3 hash a"))),
      SHA3CtrlRegs.out_hash_b -> Seq(RegField.r(32, out_hash(12*32-1,11*32), RegFieldDesc("out_hash_b","Output SHA3 hash b"))),
      SHA3CtrlRegs.out_hash_c -> Seq(RegField.r(32, out_hash(13*32-1,12*32), RegFieldDesc("out_hash_c","Output SHA3 hash c"))),
      SHA3CtrlRegs.out_hash_d -> Seq(RegField.r(32, out_hash(14*32-1,13*32), RegFieldDesc("out_hash_d","Output SHA3 hash d"))),
      SHA3CtrlRegs.out_hash_e -> Seq(RegField.r(32, out_hash(15*32-1,14*32), RegFieldDesc("out_hash_e","Output SHA3 hash e"))),
      SHA3CtrlRegs.out_hash_f -> Seq(RegField.r(32, out_hash(16*32-1,15*32), RegFieldDesc("out_hash_f","Output SHA3 hash f"))),
    )
  }
}

class TLSHA3(busWidthBytes: Int, params: SHA3Params)(implicit p: Parameters)
  extends SHA3(busWidthBytes, params) with HasTLControlRegMap

case class SHA3AttachParams(
   sha3par: SHA3Params,
   controlBus: TLBusWrapper,
   intNode: IntInwardNode,
   controlXType: ClockCrossingType = NoCrossing,
   intXType: ClockCrossingType = NoCrossing,
   mclock: Option[ModuleValue[Clock]] = None,
   mreset: Option[ModuleValue[Bool]] = None)
 (implicit val p: Parameters)

object SHA3 {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }

  def attach(params: SHA3AttachParams): TLSHA3 = {
    implicit val p = params.p
    val name = s"sha3 ${nextId()}"
    val cbus = params.controlBus
    val sha3 = LazyModule(new TLSHA3(cbus.beatBytes, params.sha3par))
    sha3.suggestName(name)

    cbus.coupleTo(s"device_named_$name") {
      sha3.controlXing(params.controlXType) := TLFragmenter(cbus.beatBytes, cbus.blockBytes) := _
    }
    params.intNode := sha3.intXing(params.intXType)
    InModuleBody {
      sha3.module.clock := params.mclock.map(_.getWrappedValue).getOrElse(cbus.module.clock)
    }
    InModuleBody {
      sha3.module.reset := params.mreset.map(_.getWrappedValue).getOrElse(cbus.module.reset)
    }

    sha3
  }
}