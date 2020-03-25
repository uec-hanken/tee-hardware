package uec.keystoneAcc.nedochip

import chisel3._
import chisel3.util._
import chisel3.experimental._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sifive.blocks.devices.pinctrl._
import sifive.blocks.devices.gpio._
import sifive.blocks.devices.spi._
import sifive.fpgashells.clocks._
import uec.keystoneAcc.vc707mig32._

class ilaaxi extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val probe0 = Input(UInt(4.W))
    val probe1 = Input(UInt(32.W))
    val probe2 = Input(UInt(8.W))
    val probe3 = Input(UInt(3.W))
    val probe4 = Input(UInt(2.W))
    val probe5 = Input(UInt(1.W))
    val probe6 = Input(UInt(3.W))
    val probe7 = Input(UInt(4.W))
    val probe8 = Input(UInt(1.W))
    val probe9 = Input(UInt(1.W))
    val probe10 = Input(UInt(32.W))
    val probe11 = Input(UInt(4.W))
    val probe12 = Input(UInt(1.W))
    val probe13 = Input(UInt(1.W))
    val probe14 = Input(UInt(1.W))
    val probe15 = Input(UInt(1.W))
    val probe16 = Input(UInt(4.W))
    val probe17 = Input(UInt(2.W))
    val probe18 = Input(UInt(1.W))
    val probe19 = Input(UInt(4.W))
    val probe20 = Input(UInt(32.W))
    val probe21 = Input(UInt(8.W))
    val probe22 = Input(UInt(3.W))
    val probe23 = Input(UInt(2.W))
    val probe24 = Input(UInt(1.W))
    val probe25 = Input(UInt(3.W))
    val probe26 = Input(UInt(4.W))
    val probe27 = Input(UInt(1.W))
    val probe28 = Input(UInt(1.W))
    val probe29 = Input(UInt(1.W))
    val probe30 = Input(UInt(4.W))
    val probe31 = Input(UInt(32.W))
    val probe32 = Input(UInt(2.W))
    val probe33 = Input(UInt(1.W))
    val probe34 = Input(UInt(1.W))
  })
  def connectAxi(axi: AXI4Bundle): Unit = {
    //slave AXI interface write address ports
    io.probe0 := axi.aw.bits.id
    io.probe1 := axi.aw.bits.addr //truncated
    io.probe2 := axi.aw.bits.len
    io.probe3 := axi.aw.bits.size
    io.probe4 := axi.aw.bits.burst
    io.probe5 := axi.aw.bits.lock
    io.probe6 := axi.aw.bits.prot
    io.probe7 := axi.aw.bits.qos
    io.probe8 := axi.aw.valid
    io.probe9 := axi.aw.ready

    //slave interface write data ports
    io.probe10 := axi.w.bits.data
    io.probe11 := axi.w.bits.strb
    io.probe12 := axi.w.bits.last
    io.probe13 := axi.w.valid
    io.probe14 := axi.w.ready

    //slave interface write response
    io.probe15 := axi.b.ready
    io.probe16 := axi.b.bits.id
    io.probe17 := axi.b.bits.resp
    io.probe18 := axi.b.valid

    //slave AXI interface read address ports
    io.probe19 := axi.ar.bits.id
    io.probe20 := axi.ar.bits.addr // truncated
    io.probe21 := axi.ar.bits.len
    io.probe22 := axi.ar.bits.size
    io.probe23 := axi.ar.bits.burst
    io.probe24 := axi.ar.bits.lock
    io.probe25 := axi.ar.bits.prot
    io.probe26 := axi.ar.bits.qos
    io.probe27 := axi.ar.valid
    io.probe28 := axi.ar.ready

    //slace AXI interface read data ports
    io.probe29 := axi.r.ready
    io.probe30 := axi.r.bits.id
    io.probe31 := axi.r.bits.data
    io.probe32 := axi.r.bits.resp
    io.probe33 := axi.r.bits.last
    io.probe34 := axi.r.valid
  }
}

class ilatl extends BlackBox {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val probe0 = Input(UInt(3.W))
    val probe1 = Input(UInt(3.W))
    val probe2 = Input(UInt(3.W))
    val probe3 = Input(UInt(4.W))
    val probe4 = Input(UInt(32.W))
    val probe5 = Input(UInt(4.W))
    val probe6 = Input(UInt(4.W))
    val probe7 = Input(UInt(32.W))
    val probe8 = Input(UInt(1.W))
    val probe9 = Input(UInt(1.W))
    val probe10 = Input(UInt(1.W))

    val probe11 = Input(UInt(3.W))
    val probe12 = Input(UInt(3.W))
    val probe13 = Input(UInt(3.W))
    val probe14 = Input(UInt(4.W))
    val probe15 = Input(UInt(4.W))
    val probe16 = Input(UInt(1.W))
    val probe17 = Input(UInt(4.W))
    val probe18 = Input(UInt(32.W))
    val probe19 = Input(UInt(1.W))
    val probe20 = Input(UInt(1.W))
    val probe21 = Input(UInt(1.W))
  })
  def connectAxi(tl: TLBundle): Unit = {
    //slave AXI interface write address ports
    io.probe0 := tl.a.bits.opcode
    io.probe1 := tl.a.bits.param
    io.probe2 := tl.a.bits.size
    io.probe3 := tl.a.bits.source
    io.probe4 := tl.a.bits.address
    io.probe5 := tl.a.bits.user.getOrElse(0.U)
    io.probe6 := tl.a.bits.mask
    io.probe7 := tl.a.bits.data
    io.probe8 := tl.a.bits.corrupt
    io.probe9 := tl.a.valid
    io.probe10 := tl.a.ready

    //slave interface write data ports
    io.probe11 := tl.d.bits.opcode
    io.probe12 := tl.d.bits.param
    io.probe13 := tl.d.bits.size
    io.probe14 := tl.d.bits.source
    io.probe15 := tl.d.bits.sink
    io.probe16 := tl.d.bits.denied
    io.probe17 := tl.d.bits.user.getOrElse(0.U)
    io.probe18 := tl.d.bits.data
    io.probe19 := tl.d.bits.corrupt
    io.probe20 := tl.d.ready
    io.probe21 := tl.d.valid
  }
}