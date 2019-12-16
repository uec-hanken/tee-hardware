package uec.keystoneAcc.devices.aes

import chisel3._
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._

case class AESParams(address: BigInt)

class AESPortIO extends Bundle {
}

class aes_core extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val reset_n = Input(Bool())

    val encdec = Input(Bool())
    val init = Input(Bool())
    val next = Input(Bool())
    val ready = Output(Bool())

    val key = Input(UInt(256.W))
    val keylen = Input(Bool())

    val block = Input(UInt(128.W))
    val result = Output(UInt(128.W))
    val result_valid = Output(Bool())
  })
}

abstract class AES(busWidthBytes: Int, val c: AESParams, divisorInit: Int = 0)
                   (implicit p: Parameters)
  extends IORegisterRouter(
    RegisterRouterParams(
      name = "aes",
      compat = Seq("uec,aes3-0"),
      base = c.address,
      beatBytes = busWidthBytes),
    new AESPortIO
  )
    with HasInterruptSources {

  def nInterrupts = 1

  ResourceBinding {
    Resource(ResourceAnchors.aliases, "aes").bind(ResourceAlias(device.label))
  }

  lazy val module = new LazyModuleImp(this) {
    interrupts(0) := false.B

    // Data
    val block = Reg(Vec(4, UInt(32.W)))
    val key = Reg(Vec(8, UInt(32.W)))
    val result = Wire(UInt(128.W))

    // Configurations
    val encdec = RegInit(false.B)
    val keylen = RegInit(false.B)

    // Triggers
    val init = WireInit(false.B)
    val next = WireInit(false.B)
    val ready = Wire(Bool())

    // Core instantiation
    val aes = Module(new aes_core)
    aes.io.clk := clock
    aes.io.reset_n := !reset.asBool()
    aes.io.block := (for(i <- 0 until 16) yield block.asUInt()((1+i)*8-1, (0+i)*8)).reduce(Cat(_,_))
    aes.io.key := (for(i <- 0 until 32) yield key.asUInt()((1+i)*8-1, (0+i)*8)).reduce(Cat(_,_))
    result := aes.io.result
    aes.io.encdec := encdec
    aes.io.keylen := keylen
    aes.io.init := init
    aes.io.next := next
    ready := aes.io.ready
    // aes.io.result_valid ignored

    // Regfields
    val block_regmap: Seq[RegField] = block.map{ i => RegField(32, i) }
    val key_regmap: Seq[RegField] = key.map{ i => RegField(32, i) }
    val result_regmap: Seq[RegField] = for(i <- 0 until 16) yield RegField.r(8, result((16-i)*8-1, (15-i)*8))
    val config_regmap: Seq[RegField] = Seq(
      RegField(1, encdec, RegFieldDesc("encdec", "Encode / Decode", reset = Some(0))),
      RegField(1, keylen, RegFieldDesc("keylen", "Key length", reset = Some(0)))
    )
    val reg_and_status = Seq(
      RegField(1, init, RegFieldDesc("init", "Key Expansion Enable", reset = Some(0))),
      RegField(1, next, RegFieldDesc("next", "Data Enable", reset = Some(0))),
      RegField.r(1, ready, RegFieldDesc("ready", "Ready", volatile = true))
    )

    regmap(
      AESCtrlRegs.key -> key_regmap,
      AESCtrlRegs.block -> block_regmap,
      AESCtrlRegs.result -> result_regmap,
      AESCtrlRegs.config -> config_regmap,
      AESCtrlRegs.regstatus -> reg_and_status
    )
  }

}

class TLAES(busWidthBytes: Int, params: AESParams)(implicit p: Parameters)
  extends AES(busWidthBytes, params) with HasTLControlRegMap

case class AESAttachParams(
                             aespar: AESParams,
                             controlBus: TLBusWrapper,
                             intNode: IntInwardNode,
                             controlXType: ClockCrossingType = NoCrossing,
                             intXType: ClockCrossingType = NoCrossing,
                             mclock: Option[ModuleValue[Clock]] = None,
                             mreset: Option[ModuleValue[Bool]] = None)
                           (implicit val p: Parameters)

object AES {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }

  def attach(params: AESAttachParams): TLAES = {
    implicit val p = params.p
    val name = s"aes ${nextId()}"
    val cbus = params.controlBus
    val aes = LazyModule(new TLAES(cbus.beatBytes, params.aespar))
    aes.suggestName(name)

    cbus.coupleTo(s"device_named_$name") {
      aes.controlXing(params.controlXType) := TLFragmenter(cbus.beatBytes, cbus.blockBytes) := _
    }
    params.intNode := aes.intXing(params.intXType)
    InModuleBody {
      aes.module.clock := params.mclock.map(_.getWrappedValue).getOrElse(cbus.module.clock)
    }
    InModuleBody {
      aes.module.reset := params.mreset.map(_.getWrappedValue).getOrElse(cbus.module.reset)
    }

    aes
  }
}