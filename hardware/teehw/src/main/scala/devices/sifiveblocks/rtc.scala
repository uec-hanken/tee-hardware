package uec.teehardware.devices.sifiveblocks

import chisel3._
import chisel3.util.Counter
import freechips.rocketchip.prci._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import uec.teehardware.TEEHWBaseSubsystem
import chipsalliance.rocketchip.config.Field
import freechips.rocketchip.subsystem.{BaseSubsystem, PBUS, PeripheryBusKey}
import freechips.rocketchip.devices.tilelink._

case object RTCPort extends Field[Boolean](false)

trait HasTEEHWPeripheryRTC {
  this: TEEHWBaseSubsystem =>

  // The RTC clock node (if needed)
  val RTCNode = p(RTCPort).option{
    val RTCclockGroup = ClockSinkNode(Seq(ClockSinkParameters(name = Some("rtc_clock"))))
    RTCclockGroup := ClockGroup() := asyncClockGroupsNode
    RTCclockGroup
  }
}

trait HasTEEHWPeripheryRTCModuleImp extends LazyModuleImp {
  val outer: BaseSubsystem with CanHavePeripheryCLINT with HasTEEHWPeripheryRTC

  // RTC clock (if enabled)
  val pbus = outer.locateTLBusWrapper(PBUS)

  outer.RTCNode match {
    case Some(node) =>
      chisel3.withClockAndReset(pbus.module.clock, pbus.module.reset) {
        val (rtcBundle, _) = node.in(0)
        // Synchronize the external toggle into the clint
        val rtc_in = rtcBundle.clock.asBool
        val rtc_sync = SynchronizerShiftReg(rtc_in, 3, Some("rtc"))
        val rtc_last = RegNext(rtc_sync, false.B)
        val rtc_tick = RegNext(rtc_sync && (!rtc_last), false.B)
        outer.clintOpt.foreach { clint =>
          clint.module.io.rtcTick := rtc_tick
        }
      }
    case None =>
      // NOTE: Same as RTC.scala
      val pbusFreq = outer.p(PeripheryBusKey).dtsFrequency.get
      val rtcFreq = outer.p(DTSTimebase)
      val internalPeriod: BigInt = pbusFreq / rtcFreq

      // check whether pbusFreq >= rtcFreq
      require(internalPeriod > 0)
      // check wehther the integer division is within 5% of the real division
      require((pbusFreq - rtcFreq * internalPeriod) * 100 / pbusFreq <= 5)

      // Use the static period to toggle the RTC
      chisel3.withClockAndReset(pbus.module.clock, pbus.module.reset) {
        val (_, int_rtc_tick) = Counter(true.B, internalPeriod.toInt)
        outer.clintOpt.foreach { clint =>
          clint.module.io.rtcTick := int_rtc_tick
        }
      }
  }
}
