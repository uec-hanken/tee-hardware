package uec.teehardware

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.util._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.ExtMem
import freechips.rocketchip.tilelink._
import testchipip.{SerialIO, TLDesser}

class TLULDesser
(
  w: Int,
  params: Seq[TLClientParameters],
  sparams: Seq[TLManagerParameters],
  beatBytes: Int,
  hasCorruptDenied: Boolean = true)(implicit p :Parameters) extends LazyModule {
  // Create a dummy node where we can attach our silly TL port
  val node = TLManagerNode(sparams.map(manager =>
    TLSlavePortParameters.v1(Seq(manager), beatBytes)))

  // Attach to the TLDesser
  val desser = LazyModule(new TLDesser(w, params, hasCorruptDenied))
  node := desser.node

  lazy val module = new LazyModuleImp(this) {
    val nChannels = params.size
    val io = IO(new Bundle {
      val tlport = new TLUL(node.out.head._1.params)
      val ser = Vec(nChannels, new SerialIO(w))
    })

    io.ser <> desser.module.io.ser

    node.out.foreach {
      case  (bundle, _) =>
        bundle.a.valid := io.tlport.a.valid
        io.tlport.a.ready := bundle.a.ready
        bundle.a.bits := io.tlport.a.bits

        io.tlport.d.valid := bundle.d.valid
        bundle.d.ready := io.tlport.d.ready
        io.tlport.d.bits := bundle.d.bits
        //bundle.b.bits := (new TLBundleB(TLparams)).fromBits(0.U)
        bundle.b.ready := true.B
        bundle.c.valid := false.B
        //bundle.c.bits := 0.U.asTypeOf(new TLBundleC(TLparams))
        bundle.e.valid := false.B
      //bundle.e.bits := 0.U.asTypeOf(new TLBundleE(TLparams))
    }
  }

}