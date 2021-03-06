package uec.teehardware.devices.random

import chisel3._
import chisel3.util._
import chisel3.util.random._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.tilelink.{BasicBusBlockerParams, TLClockBlocker}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.diplomaticobjectmodel.DiplomaticObjectModelAddressing
import freechips.rocketchip.diplomaticobjectmodel.logicaltree.{LogicalModuleTree, LogicalTreeNode}
import freechips.rocketchip.diplomaticobjectmodel.model.{OMComponent, OMDevice, OMInterrupt, OMMemoryRegion}
import freechips.rocketchip.interrupts._
import freechips.rocketchip.prci.{ClockGroup, ClockSinkDomain}
import freechips.rocketchip.regmapper._
import freechips.rocketchip.subsystem.{Attachable, PBUS, TLBusWrapperLocation}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._

case class ROLocHints
(
  slice_x: Int = 45,
  slice_y: Int = 45
)

case class RandomParams
(
  address: BigInt,
  impl: Int = 0,
  nbits: Int = 14,
  board: String = "Simulation",
  path: String = "TEEHWSoC/TEEHWPlatform/sys/randomClockDomainWrapper/",
  refLoc : ROLocHints = ROLocHints(15, 158),
  rngLoc : ROLocHints = ROLocHints(15, 161)
) {
  require(nbits == 14, "TODO: This Random does not support nbits different than 14")
}

case class OMRANDOMDevice(
  memoryRegions: Seq[OMMemoryRegion],
  interrupts: Seq[OMInterrupt],
  _types: Seq[String] = Seq("OMRANDOMDevice", "OMDevice", "OMComponent")
) extends OMDevice

class RandomPortIO extends Bundle {
}

abstract class Random(busWidthBytes: Int, val c: RandomParams)
                   (implicit p: Parameters)
  extends IORegisterRouter(
    RegisterRouterParams(
      name = "random",
      compat = Seq("uec,random-0"),
      base = c.address,
      beatBytes = busWidthBytes),
    new RandomPortIO
  )
    with HasInterruptSources {
  def nInterrupts = 4

  // The device in the dts is created here
  ResourceBinding {
    Resource(ResourceAnchors.aliases, "random").bind(ResourceAlias(device.label))
  }

  // The function for generating the RandomNumberGenerator
  def genRNG
  (
    c: RandomParams,
    rnd_reset: Bool,
    rnd_en: Bool,
    trng_debug_reset: Bool,
    trng_debug_enable: Bool,
    trng_debug_control: UInt,
    trng_debug_out: UInt) : (UInt, Bool) = {
    (LFSR(c.nbits, rnd_en), true.B)
  }

  lazy val module = new LazyModuleImp(this) {

    // Interrupt registers
    val vn_err_int_mask = RegInit(true.B)
    val crngt_err_int_mask = RegInit(true.B)
    val autocorr_err_int_mask = RegInit(true.B)
    val ehr_valid_int_mask = RegInit(true.B)

    val vn_err = RegInit(false.B)
    val crngt_err = RegInit(false.B)
    val autocorr_err = RegInit(false.B)
    val ehr_valid_int = RegInit(false.B)

    val vn_err_clear = WireInit(false.B)
    val crngt_err_clear = WireInit(false.B)
    val autocorr_err_clear = WireInit(false.B)
    val ehr_valid_clear = WireInit(false.B)

    // Clear interrupt logic
    when(vn_err_clear) { vn_err := false.B }
    when(crngt_err_clear) { crngt_err := false.B }
    when(autocorr_err_clear) { autocorr_err := false.B }
    when(ehr_valid_clear) { ehr_valid_int := false.B }

    // Interrupt export
    interrupts(3) := vn_err
    interrupts(2) := crngt_err
    interrupts(1) := autocorr_err
    interrupts(0) := ehr_valid_int

    // Interrupt register mapping
    val int_map = Seq(
      RandomRegs.rng_imr -> Seq(
        RegField(1, ehr_valid_int_mask, RegFieldDesc("ehr_valid_int_mask", "192-bit sampling interrupt mask", reset = Some(1))),
        RegField(1, autocorr_err_int_mask, RegFieldDesc("autocorr_err_int_mask", "Autocorrelation error interrupt mask", reset = Some(1))),
        RegField(1, crngt_err_int_mask, RegFieldDesc("crngt_err_int_mask", "CRNGT error interrupt mask", reset = Some(1))),
        RegField(1, vn_err_int_mask, RegFieldDesc("vn_err_int_mask", "Von Neumann error interrupt mask", reset = Some(1)))
      ),
      RandomRegs.rng_isr -> Seq(
        RegField.r(1, ehr_valid_int, RegFieldDesc("ehr_valid", "192-bit sampling interrupt status", volatile = true)),
        RegField.r(1, autocorr_err, RegFieldDesc("autocorr_err", "Autocorrelation error interrupt status", volatile = true)),
        RegField.r(1, crngt_err, RegFieldDesc("crngt_err", "CRNGT error interrupt status", volatile = true)),
        RegField.r(1, vn_err, RegFieldDesc("vn_err", "Von Neumann error interrupt status", volatile = true))
      ),
      RandomRegs.rng_icr -> Seq(
        RegField(1, ehr_valid_clear, RegFieldDesc("ehr_valid_clear", "192-bit sampling interrupt clear", reset = Some(0))),
        RegField(1, autocorr_err_clear, RegFieldDesc("autocorr_err_clear", "Autocorrelation error interrupt clear", reset = Some(0))),
        RegField(1, crngt_err_clear, RegFieldDesc("crngt_err_clear", "CRNGT error interrupt clear", reset = Some(0))),
        RegField(1, vn_err_clear, RegFieldDesc("vn_err_clear", "Von Neumann error interrupt clear", reset = Some(0)))
      )
    )

    // TRNG
    val rnd_src_sel = RegInit(0.U(2.W)) // TODO: Unused. Please use this (and maybe declare more)
    val rnd_src_en = RegInit(false.B)
    val ehr_valid = RegInit(false.B)
    val ehr_data = RegInit(0.U(192.W))
    val sample_cnt1 = RegInit(0xFFFF.U(32.W))
    val autocorr_fails = RegInit(0.U(8.W)) // TODO: Not used
    val autocorr_trys = RegInit(0.U(14.W)) // TODO: Not used
    val auto_correlate_bypass = RegInit(false.B) // TODO: Not used
    val trng_crngt_bypass = RegInit(false.B) // TODO: Not used
    val vnc_bypass = RegInit(false.B) // TODO: Not used
    val trng_sw_reset = WireInit(false.B)
    val trng_busy = RegInit(false.B)
    val rst_bits_counter = WireInit(false.B)
    val rng_bist_cntr = RegInit(VecInit(Seq.fill(3)(0.U(22.W)))) // TODO: Not used

    // Debug signals for the TRNG
    val trng_debug_reset = RegInit(false.B)
    val trng_debug_enable = RegInit(false.B)
    val trng_debug_counter = WireInit(0.U(8.W))
    val trng_debug_control =  RegInit(1.U(2.W))
    val trng_debug_stop = WireInit(false.B)
    val trng_debug_out = WireInit(0.U(c.nbits))
    val trng_number_bits = RegInit(1.U(2.W))

    // Parameters for the TRNG

    // Inputs for the TRNG
    val rnd_en = WireInit(false.B)
    val rnd_reset = WireInit(false.B)

    //Interface
    val r_edge = Module(new rising_edge())
    val FIFO = Module(new sh_register(8,24))
    val pulse_generator = Module(new register_pulse_g())
    val counter_interface = Module (new counter_trace(5))

    // Implementations
    val (rnd_gen: UInt, rnd_ready: Bool) = genRNG(
      c,
      rnd_reset,
      (rnd_src_en & pulse_generator.io.out),//rnd_src_en,//rnd_en,
      trng_debug_reset,
      trng_debug_enable,
      trng_debug_control,
      trng_debug_out)
    // For the debug implementation left, just connect it here
    trng_debug_stop := rnd_ready
    trng_debug_counter := rnd_gen

    val trng_bit_counter = RegInit(0.U(8.W))
    val trng_sample_counter = RegInit(0.U(32.W))


    r_edge.io.in := rnd_ready
    FIFO.io.data_in := rnd_gen
    FIFO.io.enable := r_edge.io.out
    FIFO.io.number := trng_number_bits
    pulse_generator.io.reset := RegNext(r_edge.io.out)
    ehr_data := FIFO.io.out
    counter_interface.io.enable := RegNext(r_edge.io.out)
    counter_interface.io.reset  := rst_bits_counter

    //Define the output bits of the platform
    when(trng_number_bits === 0.U){
      counter_interface.io.number := 1.U
      when(counter_interface.io.count_debug === 1.U ){
        rnd_src_en := false.B
        ehr_valid := true.B
        trng_busy := false.B
      }.otherwise{
        rnd_src_en := rnd_src_en
        ehr_valid := false.B
        trng_busy := true.B
      }
    }.elsewhen(trng_number_bits === 1.U){
      counter_interface.io.number := 4.U
      when(counter_interface.io.count_debug === 4.U ){
        rnd_src_en := false.B
        ehr_valid := true.B
        trng_busy := false.B
      }.otherwise{
        rnd_src_en := rnd_src_en
        ehr_valid := false.B
        trng_busy := true.B
      }
    }.elsewhen(trng_number_bits === 2.U){
      counter_interface.io.number := 8.U
      when(counter_interface.io.count_debug === 8.U ){
        rnd_src_en := false.B
        ehr_valid := true.B
        trng_busy := false.B
      }.otherwise{
        rnd_src_en := rnd_src_en
        ehr_valid := false.B
        trng_busy := true.B
      }
    }.otherwise{
      counter_interface.io.number := 24.U
      when(counter_interface.io.count_debug === 24.U ){
        rnd_src_en := false.B
        ehr_valid := true.B
        trng_busy := false.B
      }.otherwise{
        rnd_src_en := rnd_src_en
        ehr_valid := false.B
        trng_busy := true.B
      }
    }

    // TRNG register mapping
    val trng_map = Seq(
      RandomRegs.trng_config -> Seq(
        RegField(2, rnd_src_sel, RegFieldDesc("rnd_src_sel", "TRNG loop select", reset = Some(0)))
      ),
      RandomRegs.trng_valid -> Seq(
        RegField.r(1, ehr_valid, RegFieldDesc("ehr_valid", "192-bit sampling valid", volatile = true))
      ),
      RandomRegs.ehr_data0 -> Seq(
        RegField.r(32, ehr_data(31, 0), RegFieldDesc("ehr_data0", "Entrophy Holding Register [31:0]", volatile = true)),
        RegField.r(32, ehr_data(63, 32), RegFieldDesc("ehr_data1", "Entrophy Holding Register [63:32]", volatile = true)),
        RegField.r(32, ehr_data(95, 64), RegFieldDesc("ehr_data2", "Entrophy Holding Register [95:64]", volatile = true)),
        RegField.r(32, ehr_data(127, 96), RegFieldDesc("ehr_data3", "Entrophy Holding Register [127:96]", volatile = true)),
        RegField.r(32, ehr_data(159, 128), RegFieldDesc("ehr_data4", "Entrophy Holding Register [159:128]", volatile = true)),
        RegField.r(32, ehr_data(191, 160), RegFieldDesc("ehr_data5", "Entrophy Holding Register [191:160]", volatile = true))
      ),
      RandomRegs.rnd_source_enable -> Seq(
        RegField(1, rnd_src_en, RegFieldDesc("rnd_src_en", "TRNG entropy source enable", reset = Some(0)))
      ),
      RandomRegs.sample_cnt1 -> Seq(
        RegField(32, sample_cnt1, RegFieldDesc("sample_cnt1", "Sampling counter register", reset = Some(0xFFFF)))
      ),
      RandomRegs.autocorr_statistic -> Seq(
        RegField(14, autocorr_trys, RegFieldDesc("autocorr_trys", "Autocorrelation trys", reset = Some(0), volatile = true)),
        RegField(8, autocorr_fails, RegFieldDesc("autocorr_fails", "Autocorrelation fails", reset = Some(0), volatile = true))
      ),
      RandomRegs.trng_debug_control -> Seq(
        RegField(1),
        RegField(1, vnc_bypass, RegFieldDesc("vnc_bypass", "Von Neumann balancer bypass", reset = Some(0))),
        RegField(1, trng_crngt_bypass, RegFieldDesc("trng_crngt_bypass", "CRNGT test bypass", reset = Some(0))),
        RegField(1, auto_correlate_bypass, RegFieldDesc("auto_correlate_bypass", "Auto correlate bypass", reset = Some(0)))
      ),
      RandomRegs.trng_sw_reset -> Seq(
        RegField(1, trng_sw_reset, RegFieldDesc("trng_sw_reset", "TRNG internal reset", reset = Some(0)))
      ),
      RandomRegs.trng_busy -> Seq(
        RegField.r(1, trng_busy, RegFieldDesc("trng_busy", "TRNG busy", volatile = true))
      ),
      RandomRegs.rst_bits_counter -> Seq(
        RegField(1, rst_bits_counter, RegFieldDesc("rst_bits_counter", "Reset Bits Counter", reset = Some(0)))
      ),
      RandomRegs.rng_bist_cntr0 -> Seq(
        RegField(22, rng_bist_cntr(0), RegFieldDesc("rng_bist_cntr_0", "TRNG BIST counter 0", reset = Some(0)))
      ),
      RandomRegs.rng_bist_cntr1 -> Seq(
        RegField(22, rng_bist_cntr(1), RegFieldDesc("rng_bist_cntr_1", "TRNG BIST counter 1", reset = Some(0)))
      ),
      RandomRegs.rng_bist_cntr2 -> Seq(
        RegField(22, rng_bist_cntr(2), RegFieldDesc("rng_bist_cntr_2", "TRNG BIST counter 2", reset = Some(0)))
      ),
      RandomRegs.trng_debug_ctrl -> Seq(
        RegField(1, trng_debug_enable),
        RegField(1, trng_debug_reset),
        RegField(2,trng_debug_control)
      ),
      RandomRegs.trng_debug_out -> Seq(
        RegField.r(c.nbits, trng_debug_out)
      ),
      RandomRegs.trng_debug_stop -> Seq(
        RegField.r(1, trng_debug_stop)
      ),
      RandomRegs.trng_debug_counters -> Seq(
        RegField.r(8, trng_debug_counter)
      ),
      RandomRegs.trng_number_bits -> Seq(
        RegField(2, trng_number_bits)
      )
    )
    regmap(
      (int_map ++ trng_map):_*
    )
  }

  val logicalTreeNode = new LogicalTreeNode(() => Some(device)) {
    def getOMComponents(resourceBindings: ResourceBindings, children: Seq[OMComponent] = Nil): Seq[OMComponent] = {
      val Description(name, mapping) = device.describe(resourceBindings)
      val memRegions = DiplomaticObjectModelAddressing.getOMMemoryRegions(name, resourceBindings, None)
      val interrupts = DiplomaticObjectModelAddressing.describeInterrupts(name, resourceBindings)
      Seq(
        OMRANDOMDevice(
          memoryRegions = memRegions.map(_.copy(
            name = "random",
            description = "RANDOM Push-Register Device"
          )),
          interrupts = interrupts
        )
      )
    }
  }
}

class TLRANDOM(busWidthBytes: Int, params: RandomParams)(implicit p: Parameters)
  extends Random(busWidthBytes, params) with HasTLControlRegMap

case class RandomAttachParams
(
  randompar: RandomParams,
  controlWhere: TLBusWrapperLocation = PBUS,
  blockerAddr: Option[BigInt] = None,
  controlXType: ClockCrossingType = NoCrossing,
  intXType: ClockCrossingType = NoCrossing)(implicit val p: Parameters) {

  def RandomGen(cbus: TLBusWrapper)(implicit valName: ValName): Random with HasTLControlRegMap = {
    LazyModule(new TLRANDOM(cbus.beatBytes, randompar))
  }

  def attachTo(where: Attachable)(implicit p: Parameters): Random with HasTLControlRegMap = {
    val name = s"random_${RANDOM.nextId()}"
    val cbus = where.locateTLBusWrapper(controlWhere)
    val randomClockDomainWrapper = LazyModule(new ClockSinkDomain(take = None))
    val random = randomClockDomainWrapper { RandomGen(cbus) }
    random.suggestName(name)

    cbus.coupleTo(s"device_named_$name") { bus =>

      val blockerOpt = blockerAddr.map { a =>
        val blocker = LazyModule(new TLClockBlocker(BasicBusBlockerParams(a, cbus.beatBytes, cbus.beatBytes)))
        cbus.coupleTo(s"bus_blocker_for_$name") { blocker.controlNode := TLFragmenter(cbus) := _ }
        blocker
      }

      randomClockDomainWrapper.clockNode := (controlXType match {
        case _: SynchronousCrossing =>
          cbus.dtsClk.map(_.bind(random.device))
          cbus.fixedClockNode
        case _: RationalCrossing =>
          cbus.clockNode
        case _: AsynchronousCrossing =>
          val randomClockGroup = ClockGroup()
          randomClockGroup := where.asyncClockGroupsNode
          blockerOpt.map { _.clockNode := randomClockGroup } .getOrElse { randomClockGroup }
      })

      (random.controlXing(controlXType)
        := TLFragmenter(cbus)
        := blockerOpt.map { _.node := bus } .getOrElse { bus })
    }

    (intXType match {
      case _: SynchronousCrossing => where.ibus.fromSync
      case _: RationalCrossing => where.ibus.fromRational
      case _: AsynchronousCrossing => where.ibus.fromAsync
    }) := random.intXing(intXType)

    LogicalModuleTree.add(where.logicalTreeNode, random.logicalTreeNode)

    random
  }
}

object RANDOM {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }
}

class sh_register(val nbits_counter :Int,val size :Int) extends Module {
  val io = IO(new Bundle {
    //Input's
    val data_in   = Input(UInt(nbits_counter.W))
    val enable    = Input(Bool())
    val number    = Input(UInt(2.W))
    //Output's
    val out    	= Output(UInt((nbits_counter*size).W))
  })
  val s_regs = VecInit(Seq.tabulate(size){ case i =>
    val m = Module(new register_size(nbits_counter))
    m.io
  })
  (0 until (size)).map(i =>
    if(i==0){
      s_regs(0).data_in := io.data_in
      s_regs(0).enable  := io.enable
    }
    else {
      s_regs(i).data_in := s_regs(i-1).out
      s_regs(i).enable  := io.enable
    }
  )

  when(io.number === 0.U){
    io.out := Cat(s_regs(0).out)
  }.elsewhen(io.number === 1.U){
    io.out := Cat(s_regs(3).out,s_regs(2).out,s_regs(1).out,s_regs(0).out)
  }.elsewhen(io.number === 2.U){
    io.out := Cat(s_regs(7)out,s_regs(6).out,s_regs(5).out,s_regs(4).out,s_regs(3).out,s_regs(2).out,s_regs(1).out,s_regs(0).out)
  }.otherwise {
    io.out := Cat(s_regs(23).out, s_regs(22).out, s_regs(21).out, s_regs(20).out, s_regs(19).out, s_regs(18).out, s_regs(17).out, s_regs(16).out, s_regs(15).out, s_regs(14).out, s_regs(13).out, s_regs(12).out, s_regs(11).out, s_regs(10).out, s_regs(9).out, s_regs(8).out, s_regs(7).out, s_regs(6).out, s_regs(5).out, s_regs(4).out, s_regs(3).out, s_regs(2).out, s_regs(1).out, s_regs(0).out)
  }

  }

class register_size(val nbits_counter :Int) extends Module {
  val io = IO(new Bundle {
    //Input's
    val data_in   = Input(UInt(nbits_counter.W))
    val enable    = Input(Bool())
    //Output's
    val out    	  = Output(UInt(nbits_counter.W))
  })
  val reg = RegInit(0.U(nbits_counter.W))
  when(io.enable===true.B){
    reg := io.data_in
  }.otherwise{
    reg := reg
  }
  io.out := reg
}


class rising_edge() extends Module {
  val io = IO(new Bundle {
    //Input's
    val in        = Input(Bool())
    //Output's
    val out    	  = Output(Bool())
  })
  val reg = RegNext(io.in)
  val reg_2 = RegNext(reg)
  val reg_3 = RegNext(reg_2)//
  val reg_4 = RegNext(reg_3)//
  val reg_5 = RegNext(reg_4)//
  val reg_6 = RegNext(reg_5)//
  val reg_7 = RegNext(reg_6)//
  val reg_8 = RegNext(reg_7)//
  io.out := (io.in)&&(!reg)&&(!reg_2)&&(!reg_3)&&(!reg_4)&&(!reg_5)&&(!reg_6)&&(!reg_7)&&(!reg_8)
}


class counter_trace(val nbits_counter :Int) extends Module {
  val io = IO(new Bundle {
    //Input's
    val enable = Input(Bool())
    val reset  = Input(Bool())
    val number = Input(UInt(5.W))
    //Output's
    val count_debug = Output(UInt(nbits_counter.W))
  })
  val reg = RegInit(0.U(nbits_counter.W))
  when((reg === io.number)&(io.enable === true.B)){
    reg := 0.U
  }.elsewhen(io.enable === true.B){
    reg := reg + 1.U
  }.elsewhen(io.reset === true.B){
    reg := 0.U
  }.otherwise{
    reg := reg
  }
  io.count_debug := reg
}

class register_pulse_g() extends Module {
  val io = IO(new Bundle{
    //Input's
    val reset  = Input(Bool())
    //Output's
    val out = Output(Bool())
  })
  withClockAndReset(clock,io.reset){
    val reg = RegInit(0.U(6.W))
    when(reg< ((math.pow(2, 6) - 1).toInt).asUInt ){
      reg := reg+1.U
      io.out := false.B
    }.elsewhen(reg===((math.pow(2, 6) - 1).toInt).asUInt){
      reg := reg
      io.out := true.B
    }.otherwise{
      reg := reg
      io.out := true.B
    }
  }
}






