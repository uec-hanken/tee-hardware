package uec.keystoneAcc.devices.ed25519

import chisel3._
import chisel3.core.SyncReadMem
import chisel3.util._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._

case class ed25519Params(
                       address: BigInt,
                       width: Int)

class ed25519PortIO extends Bundle {
}

class ed25519_base_point_multiplier extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val rst_n = Input(Bool())
    val ena = Input(Bool())
    val rdy = Output(Bool())
    val k_addr = Output(UInt(3.W))
    val qy_addr = Output(UInt(3.W))
    val qy_wren = Output(Bool())
    val k_din = Input(UInt(32.W))
    val qy_dout = Output(UInt(32.W))
  })
}

class curve25519_modular_multiplier extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val rst_n = Input(Bool())
    val ena = Input(Bool())
    val rdy = Output(Bool())
    val a_addr = Output(UInt(3.W))
    val b_addr = Output(UInt(3.W))
    val p_addr = Output(UInt(3.W))
    val p_wren = Output(Bool())
    val a_din = Input(UInt(32.W))
    val b_din = Input(UInt(32.W))
    val p_dout = Output(UInt(32.W))
  })
}


abstract class ed25519(busWidthBytes: Int, val c: ed25519Params)
                   (implicit p: Parameters)
  extends IORegisterRouter(
    RegisterRouterParams(
      name = "ed25519",
      compat = Seq("uec,ed25519-0"),
      base = c.address,
      beatBytes = busWidthBytes),
    new ed25519PortIO
  )
    with HasInterruptSources {

  def nInterrupts = 1

  ResourceBinding {
    Resource(ResourceAnchors.aliases, "ed25519").bind(ResourceAlias(device.label))
  }

  lazy val module = new LazyModuleImp(this) {
    interrupts(0) := false.B

    // First, create the utilities

    class mem32IO(val abits : Int = 3) extends Bundle {
      val d = UInt(32.W)
      val q = UInt(32.W)
      val en = Bool()
      val we = Bool()
      val addr = UInt(abits.W)
      override def cloneType = (new mem32IO(abits)).asInstanceOf[this.type]
    }

    def memAndMap(memio: mem32IO) : Seq[RegField] = {
      val adepth = 1 << memio.abits
      // Basic assigns
      memio.we := false.B
      memio.en := true.B // Always active
      memio.d := 0.U
      /*val ohaddr = WireInit(VecInit(Seq.fill(adepth)(false.B)))
      memio.addr := OHToUInt(ohaddr)
      // Creation of RegFields
      for(i <- 0 until adepth) yield {
        // Read function. We just capture the address here
        val readFcn = RegReadFn((ivalid, oready) => {
          when(ivalid) { ohaddr(i) := true.B }
          (true.B, RegNext(ivalid), memio.q)
        })
        // Write function. We also capture the addressm and put the we
        val writeFcn = RegWriteFn((ivalid, oready, data) => {
          when(ivalid) {
            ohaddr(i) := true.B
            memio.we := true.B
            memio.d := data
          }
          (true.B, RegNext(ivalid))
        })
        RegField(32, readFcn, writeFcn)
      }*/
      val addr = RegInit(0.U(memio.abits.W))
      memio.addr := addr
      // The read function. Just put q always
      val readFcn = RegReadFn((ready) => {
        (true.B, memio.q)
      })
      // Write function. We also capture the addressm and put the we
      val writeFcn = RegWriteFn((valid, data) => {
        when(valid) {
          memio.we := true.B
          memio.d := data
        }
        true.B
      })
      Seq(
        RegField(32, readFcn, writeFcn),
        RegField(memio.abits, addr)
      )
    }

    def mem32IOtomem(io: mem32IO, mem: SyncReadMem[UInt]): Unit = {
      when(io.en) {
        when(io.we){
          mem.write(io.addr, io.d)
          io.q := DontCare
        } .otherwise {
          io.q := mem.read(io.addr)
        }
      } .otherwise {
        io.q := DontCare
      }
    }

    def mem32mux(sel: Bool, io2: mem32IO, io1: mem32IO, out: mem32IO) : Unit = {
      out.addr := Mux(sel, io2.addr, io1.addr)
      out.we := Mux(sel, io2.we, io1.we)
      out.en := Mux(sel, io2.en, io1.en)
      out.d := Mux(sel, io2.d, io1.d)
      io1.q := out.q
      io2.q := out.q
    }

    // Put actual hardware
    val busy = RegInit(false.B)
    val busy2 = RegInit(false.B)

    // The memories
    val mem_k = SyncReadMem(8, UInt(32.W)) // Key memory
    val mem_k2 = SyncReadMem(8, UInt(32.W)) // Key2 memory
    val mem_qy = SyncReadMem(8, UInt(32.W)) // Result memory
    val mem_a = SyncReadMem(8, UInt(32.W)) // A memory
    val mem_b = SyncReadMem(8, UInt(32.W)) // B memory
    val mem_c = SyncReadMem(8, UInt(32.W)) // C memory

    // Ports for the memories
    val tobram_k = Wire(new mem32IO) // Mem port to Key memory
    val tobram_k2 = Wire(new mem32IO) // Mem port to Key2 memory
    val tobram_qy = Wire(new mem32IO) // Mem port to Result memory
    val tobram_a = Wire(new mem32IO) // Mem port to A memory
    val tobram_b = Wire(new mem32IO) // Mem port to B memory
    val tobram_c = Wire(new mem32IO) // Mem port to C memory
    val fromrmap_k = Wire(new mem32IO) // Register router to Key memory
    val fromrmap_k2 = Wire(new mem32IO) // Register router to Key2 memory
    val fromrmap_qy = Wire(new mem32IO) // Register router to Result memory
    val fromrmap_a = Wire(new mem32IO) // Register router to A memory
    val fromrmap_b = Wire(new mem32IO) // Register router to B memory
    val fromrmap_c = Wire(new mem32IO) // Register router to C memory
    val fromacc_k = Wire(new mem32IO) // From accelerator to Key memory
    val fromacc_k2 = Wire(new mem32IO) // From accelerator to Key2 memory
    val fromacc_qy = Wire(new mem32IO) // From accelerator to Result memory
    val fromacc_a = Wire(new mem32IO) // From accelerator to A memory
    val fromacc_b = Wire(new mem32IO) // From accelerator to B memory
    val fromacc_c = Wire(new mem32IO) // From accelerator to C memory

    // Interconnections and muxing
    mem32mux(busy, fromacc_k, fromrmap_k, tobram_k)
    mem32mux(busy, fromacc_k2, fromrmap_k2, tobram_k2)
    mem32mux(busy, fromacc_qy, fromrmap_qy, tobram_qy)
    mem32mux(busy2, fromacc_a, fromrmap_a, tobram_a)
    mem32mux(busy2, fromacc_b, fromrmap_b, tobram_b)
    mem32mux(busy2, fromacc_c, fromrmap_c, tobram_c)
    fromrmap_k.q := BigInt(0xdeadce11L).U // Make the reading inaccessible for the key
    fromrmap_k2.q := BigInt(0xdeadce11L).U // Make the reading inaccessible for the key
    mem32IOtomem(tobram_k, mem_k)
    mem32IOtomem(tobram_k2, mem_k2)
    mem32IOtomem(tobram_qy, mem_qy)
    mem32IOtomem(tobram_a, mem_a)
    mem32IOtomem(tobram_b, mem_b)
    mem32IOtomem(tobram_c, mem_c)

    // Create the RegMaps
    val k_regmap = memAndMap(fromrmap_k)
    val k2_regmap = memAndMap(fromrmap_k2)
    val qy_regmap = memAndMap(fromrmap_qy)
    val a_regmap = memAndMap(fromrmap_a)
    val b_regmap = memAndMap(fromrmap_b)
    val c_regmap = memAndMap(fromrmap_c)
    val ena = WireInit(false.B)
    val ena2 = WireInit(false.B)
    val rdy = Wire(Bool())
    val rdy2 = Wire(Bool())
    val wk = RegInit(false.B)
    val reg_and_status = Seq(
      RegField(1, ena, RegFieldDesc("ena","Enable", reset = Some(0))),
      RegField.r(1, busy, RegFieldDesc("busy","Busy", volatile=true)),
      RegField.r(1, rdy, RegFieldDesc("rdy","Ready", volatile=true)),
      RegField(1, wk, RegFieldDesc("wk","Which k", reset = Some(0))),
    )
    val reg_and_status2 = Seq(
      RegField(1, ena2, RegFieldDesc("ena2","Enable", reset = Some(0))),
      RegField.r(1, busy2, RegFieldDesc("busy2","Busy", volatile=true)),
      RegField.r(1, rdy2, RegFieldDesc("rdy2","Ready", volatile=true)),
    )

    // Busy logic
    when(ena) { busy := true.B } .elsewhen(rdy) { busy := false.B }
    when(ena2) { busy2 := true.B } .elsewhen(rdy2) { busy2 := false.B }

    // The actual base multiplier
    val mult = Module(new ed25519_base_point_multiplier)
    mult.io.clk := clock
    mult.io.rst_n := !reset.toBool
    mult.io.ena := ena
    rdy := mult.io.rdy
    fromacc_k.addr := mult.io.k_addr
    fromacc_k.en := true.B
    fromacc_k.we := false.B
    fromacc_k.d := BigInt(0xdeadbeefL).U
    fromacc_k2.addr := mult.io.k_addr
    fromacc_k2.en := true.B
    fromacc_k2.we := false.B
    fromacc_k2.d := BigInt(0xdeadbeefL).U
    mult.io.k_din := Mux(wk, fromacc_k2.q, fromacc_k.q)
    fromacc_qy.addr := mult.io.qy_addr
    fromacc_qy.we := mult.io.qy_wren
    fromacc_qy.d := mult.io.qy_dout
    fromacc_qy.en := true.B
    // fromacc_qy.q ignored

    // The actual modular multiplier
    val mult2 = Module(new curve25519_modular_multiplier)
    mult2.io.clk := clock
    mult2.io.rst_n := !reset.toBool
    mult2.io.ena := ena2
    rdy2 := mult2.io.rdy
    fromacc_a.addr := mult2.io.a_addr
    mult2.io.a_din := fromacc_a.q
    fromacc_a.en := true.B
    fromacc_a.we := false.B
    fromacc_a.d := BigInt(0xdeadbeefL).U
    fromacc_b.addr := mult2.io.b_addr
    mult2.io.b_din := fromacc_b.q
    fromacc_b.en := true.B
    fromacc_b.we := false.B
    fromacc_b.d := BigInt(0xdeadbeefL).U
    fromacc_c.addr := mult2.io.p_addr
    fromacc_c.we := mult2.io.p_wren
    fromacc_c.d := mult2.io.p_dout
    fromacc_c.en := true.B
    // fromacc_c.q ignored

    // Memory map registers
    regmap(
      ed25519CtrlRegs.key -> k_regmap,
      ed25519CtrlRegs.key2 -> k2_regmap,
      ed25519CtrlRegs.qy -> qy_regmap,
      ed25519CtrlRegs.a -> a_regmap,
      ed25519CtrlRegs.b -> b_regmap,
      ed25519CtrlRegs.c -> c_regmap,
      ed25519CtrlRegs.regstatus2 -> reg_and_status2,
      ed25519CtrlRegs.regstatus -> reg_and_status
    )
  }
}

class TLed25519(busWidthBytes: Int, params: ed25519Params)(implicit p: Parameters)
  extends ed25519(busWidthBytes, params) with HasTLControlRegMap

case class ed25519AttachParams(
                             ed25519par: ed25519Params,
                             controlBus: TLBusWrapper,
                             intNode: IntInwardNode,
                             controlXType: ClockCrossingType = NoCrossing,
                             intXType: ClockCrossingType = NoCrossing,
                             mclock: Option[ModuleValue[Clock]] = None,
                             mreset: Option[ModuleValue[Bool]] = None)
                           (implicit val p: Parameters)

object ed25519 {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }

  def attach(params: ed25519AttachParams): TLed25519 = {
    implicit val p = params.p
    val name = s"ed25519 ${nextId()}"
    val cbus = params.controlBus
    val ed = LazyModule(new TLed25519(cbus.beatBytes, params.ed25519par))
    ed.suggestName(name)

    cbus.coupleTo(s"device_named_$name") {
      ed.controlXing(params.controlXType) := TLFragmenter(cbus.beatBytes, cbus.blockBytes) := _
    }
    params.intNode := ed.intXing(params.intXType)
    InModuleBody {
      ed.module.clock := params.mclock.map(_.getWrappedValue).getOrElse(cbus.module.clock)
    }
    InModuleBody {
      ed.module.reset := params.mreset.map(_.getWrappedValue).getOrElse(cbus.module.reset)
    }

    ed
  }
}