// See LICENSE.SiFive for license details.

package uec.teehardware.exampletop

import chisel3._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.debug.Debug
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.subsystem.{CacheBlockBytes, ExtMem}
import freechips.rocketchip.util.AsyncResetReg
import freechips.rocketchip.system._
import sifive.blocks.devices.uart._
import testchipip.{SerialAdapter, SimDRAM}

class TestHarness()(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val success = Output(Bool())
  })

  val ldut = LazyModule(new ExampleRocketSystemTEEHW)
  val dut = Module(ldut.module)

  // IMPORTANT NOTE:
  // The old version yields the debugger and loads using the fevsr. This does not work anymore
  // now, the people from berkeley just decided to directly load the program into a SimDRAM
  // that is just a wrapper for a simulated memory that loads the program there. So, we are
  // going to do exactly the same. That means that debugging the simulation is no longer
  // available unless you re-activate the code below. We are putting it commented, but just
  // notice that this kind of configuration only works on Rocket-only based systems.

  /* *** CUT HERE ***

  dut.reset := (reset.asBool | dut.debug.map { debug => AsyncResetReg(debug.ndreset) }.getOrElse(false.B)).asBool

  dut.dontTouchPorts()
  dut.tieOffInterrupts()
  SimAXIMem.connectMem(ldut)
  //SimAXIMem.connectMMIO(ldut)
  ldut.l2_frontend_bus_axi4.foreach(_.tieoff)
  Debug.connectDebug(dut.debug, dut.resetctrl, dut.psd, clock, reset.asBool, io.success)

  //dut.usb11hs.USBWireDataIn := 0.U
  //dut.usb11hs.vBusDetect := true.B
  //dut.usb11hs.usbClk := clock // TODO: Do an actual 48MHz clock?

  *** CUT HERE *** */

  // This is the new way (just look at chipyard.IOBinders package about details)

  // Simulated memory
  val memSize = ldut.p(ExtMem).get.master.size
  val lineSize = ldut.p(CacheBlockBytes)
  ldut.mem_axi4.zip(ldut.memAXI4Node.in).map { case (io, (_, edge)) =>
    val mem = Module(new SimDRAM(memSize, lineSize, edge.bundle))
    mem.io.axi <> io
    mem.io.clock := clock
    mem.io.reset := reset
  }

  // Debug tie off
  Debug.tieoffDebug(dut.debug, dut.resetctrl, Some(dut.psd))
  dut.debug.foreach { d =>
    d.clockeddmi.foreach({ cdmi => cdmi.dmi.req.bits := DontCare; cdmi.dmiClock := clock })
    d.dmactiveAck := DontCare
    d.clock := clock
  }

  // Common tie off and don't touch
  dut.dontTouchPorts()
  dut.tieOffInterrupts()
  //ldut.l2_frontend_bus_axi4.foreach(_.tieoff)
  io.success := false.B

  val ser_success = SerialAdapter.connectSimSerial(dut.serial, clock, reset)
  when (ser_success) { io.success := true.B }
  //UARTAdapter.connect(dut.uart, 115200)

  //dut.usb11hs.USBWireDataIn := 0.U
  //dut.usb11hs.vBusDetect := true.B
  //dut.usb11hs.usbClk := clock // TODO: Do an actual 48MHz clock?
}
