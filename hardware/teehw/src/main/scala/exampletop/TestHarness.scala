// See LICENSE.SiFive for license details.

package uec.teehardware.exampletop

import Chisel._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.debug.Debug
import freechips.rocketchip.diplomacy.LazyModule
import freechips.rocketchip.util.AsyncResetReg

class TestHarness()(implicit p: Parameters) extends Module {
  val io = new Bundle {
    val success = Bool(OUTPUT)
  }

  val dut = Module(LazyModule(new ExampleRocketSystem).module)
  dut.reset := reset | dut.debug.map { debug => AsyncResetReg(debug.ndreset) }.getOrElse(false.B)

  dut.dontTouchPorts()
  dut.tieOffInterrupts()
  dut.connectSimAXIMem()
  dut.connectSimAXIMMIO()
  dut.l2_frontend_bus_axi4.foreach(_.tieoff)
  Debug.connectDebug(dut.debug, dut.psd, clock, reset, io.success)

  //dut.usb11hs.USBWireDataIn := 0.U
  //dut.usb11hs.vBusDetect := true.B
  //dut.usb11hs.usbClk := clock // TODO: Do an actual 48MHz clock?
}
