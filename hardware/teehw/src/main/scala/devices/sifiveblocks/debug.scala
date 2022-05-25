package uec.teehardware.devices.sifiveblocks

import chipsalliance.rocketchip.config.Field
import chisel3._
import chisel3.experimental.noPrefix
import chisel3.util.HasBlackBoxResource
import freechips.rocketchip.devices.debug._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.jtag.JTAGIO
import freechips.rocketchip.prci._
import freechips.rocketchip.subsystem.{BaseSubsystem, ResetSynchronous, SubsystemResetSchemeKey}
import freechips.rocketchip.util.{AsyncResetSynchronizerShiftReg, ResetCatchAndSync, ResetSynchronizerShiftReg}

trait DebugJTAGOnlyModuleImp extends LazyModuleImp {
  val outer: HasPeripheryDebug

  val ndreset: Option[Bool] = outer.debugOpt.map { outerdebug =>
    val ndreset = IO(Output(Bool()))
    ndreset := outerdebug.module.io.ctrl.ndreset
    ndreset
  }

  val jtag: Option[JTAGIO] = noPrefix(outer.debugOpt.map { outerdebug =>

    // Reset System first (Connect to general reset)
    outerdebug.module.io.tl_reset := outer.debugTLDomainOpt.get.in.head._1.reset
    outerdebug.module.io.tl_clock := outer.debugTLDomainOpt.get.in.head._1.clock
    outerdebug.module.io.hartResetReq.foreach { rcdm => rcdm.foreach(_ := reset.asBool) }

    // Exceptions about other things that are not JTAG
    require(!p(ExportDebug).dmi,
      "You cannot have DMI interface in DebugJTAGOnlyModuleImp")
    require(!p(ExportDebug).apb,
      "You cannot have APB interface in DebugJTAGOnlyModuleImp")

    // From connectDebugClockHelper
    val debug_reset = Wire(Bool())
    val dmi_reset = Wire(Bool())
    withClockAndReset(clock, dmi_reset) {
      val debug_reset_syncd = ~AsyncResetSynchronizerShiftReg(in=true.B, sync=3, name=Some("debug_reset_sync"))
      debug_reset := debug_reset_syncd
    }
    withClockAndReset(clock, debug_reset.asAsyncReset) {
      val dmactiveAck = ResetSynchronizerShiftReg(in=outerdebug.module.io.ctrl.dmactive, sync=3, name=Some("dmactiveAck"))
      // IMPORTANT NOTE: Not using connectDebugClockAndReset, so no gated clock
      outerdebug.module.io.debug_clock := clock
      outerdebug.module.io.debug_reset := (if (p(SubsystemResetSchemeKey)==ResetSynchronous) debug_reset else debug_reset.asAsyncReset)
      outerdebug.module.io.ctrl.dmactiveAck := dmactiveAck
    }

    // Ext Trigger
    outerdebug.module.io.extTrigger.foreach { t =>
      t.in.req := false.B
      t.out.ack := t.out.req
    }

    // Debug Unavailable
    // TODO in inheriting traits: Set this to something meaningful, e.g. "component is in reset or powered down"
    outerdebug.module.io.ctrl.debugUnavail.foreach { _ := false.B }

    // JTAG connection
    val jtag = IO(Flipped(new JTAGIO(hasTRSTn = true)))

    val dtm = Module(new DebugTransportModuleJTAG(p(DebugModuleKey).get.nDMIAddrSize, p(JtagDTMKey)))
    dmi_reset := !jtag.TRSTn.get
    dtm.io.jtag.TMS := jtag.TMS // TODO: force TMS high when debug is disabled ?
    dtm.io.jtag.TCK := jtag.TCK
    dtm.io.jtag.TDI := jtag.TDI
    jtag.TDO := dtm.io.jtag.TDO
    dtm.io.jtag_clock  := jtag.TCK
    dtm.io.jtag_reset  := dmi_reset
    dtm.io.jtag_mfr_id := p(JtagDTMKey).idcodeManufId.U(11.W)
    dtm.io.jtag_part_number := p(JtagDTMKey).idcodePartNum.U(16.W)
    dtm.io.jtag_version := p(JtagDTMKey).idcodeVersion.U(4.W)
    dtm.rf_reset := dmi_reset

    outerdebug.module.io.dmi.get.dmi <> dtm.io.dmi
    outerdebug.module.io.dmi.get.dmiClock := jtag.TCK
    outerdebug.module.io.dmi.get.dmiReset := dmi_reset

    jtag
  })
}