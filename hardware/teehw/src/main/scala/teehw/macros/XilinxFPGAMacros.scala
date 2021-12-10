package uec.teehardware.shell

import chisel3._
import chisel3.util._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sifive.fpgashells.devices.xilinx.xilinxvc707mig._
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1._
import sifive.fpgashells.devices.xilinx.xilinxvcu118mig._
import sifive.fpgashells.devices.xilinx.xilinxarty100tmig._
import testchipip.{SerialIO, TLDesser}
import uec.teehardware._

// ******* For Xilinx FPGAs
class SertoMIG(w: Int, idBits: Int = 6)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new XilinxVC707MIG(
      XilinxVC707MIGParams(
        AddressSet.misaligned(
          p(ExtSerMem).get.master.base,
          0x40000000L * 1 // 1GiB for the VC707DDR,
        ))))
  // Create the desser
  val params = Seq(TLMasterParameters.v1(
    name = "tl-desser",
    sourceId = IdRange(0, 1 << idBits)))
  val desser = LazyModule(new TLDesser(w, params, true)) // Attach to the DDR
  // Attach nodes
  if(p(ExtSerMem).head.master.beatBytes != 8)
    ddr.node := //TLSourceShrinker(16) :=
      TLWidthWidget(p(ExtSerMem).head.master.beatBytes) :=
      //TLFragmenter(p(ExtSerMem).head.master.beatBytes, p(MemoryBusKey).blockBytes) :=
      desser.node
  else
    ddr.node := //TLSourceShrinker(16) :=
      desser.node
  // Create the module
  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val serport = new SerialIO(w)
      val ddrport = new XilinxVC707MIGIO(ddr.depth)
    })

    val depth = ddr.depth

    // Connect the serport
    io.serport <> desser.module.io.ser.head

    // Create the actual module, and attach the DDR port
    io.ddrport <> ddr.module.io.port
  }
}

class TLULtoMIG(TLparams: TLBundleParameters)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new XilinxVC707MIG(
      XilinxVC707MIGParams(
        AddressSet.misaligned(
          p(ExtMem).get.master.base,
          0x40000000L * 1 // 1GiB for the VC707DDR,
        ))))

  // Create a dummy node where we can attach our silly TL port
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 1 << TLparams.sourceBits) // CKDUR: The maximum ID possible goes here.
      ))
    )
  })

  // Attach to the DDR
  if(p(ExtMem).head.master.beatBytes != 8)
    ddr.node := TLWidthWidget(p(ExtMem).head.master.beatBytes) := node
  else
    ddr.node := node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlport = Flipped(new TLUL(TLparams))
      val ddrport = new XilinxVC707MIGIO(ddr.depth)
    })

    val depth = ddr.depth

    //val mem_tl = Wire(HeterogeneousBag.fromNode(node.in))
    node.out.foreach {
      case  (bundle, _) =>
        // Debug TL
        //val ilatoaxi = Module(new ilatl())
        //ilatoaxi.io.clk := clock
        //ilatoaxi.connectAxi(bundle)

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

    // Create the actual module, and attach the DDR port
    io.ddrport <> ddr.module.io.port
  }

}

class SertoMIGUltra(w: Int, idBits: Int = 6)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new XilinxVCU118MIG(
      XilinxVCU118MIGParams(
        AddressSet.misaligned(
          p(ExtSerMem).get.master.base,
          0x80000000L * 1 // 2GiB for the VCU118DDR,
        ))))
  // Create the desser
  val params = Seq(TLMasterParameters.v1(
    name = "tl-desser",
    sourceId = IdRange(0, 1 << idBits)))
  val desser = LazyModule(new TLDesser(w, params, true))
  // Attach nodes
  if(p(ExtSerMem).head.master.beatBytes != 8)
    ddr.node := //TLSourceShrinker(16) :=
      TLWidthWidget(p(ExtSerMem).head.master.beatBytes) :=
      //TLFragmenter(p(ExtSerMem).head.master.beatBytes, p(MemoryBusKey).blockBytes) :=
      desser.node
  else
    ddr.node := //TLSourceShrinker(16) :=
      desser.node
  // Create the module
  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val serport = new SerialIO(w)
      val ddrport = new XilinxVCU118MIGIO(ddr.depth)
    })

    val depth = ddr.depth

    // Connect the serport
    io.serport <> desser.module.io.ser.head

    // Create the actual module, and attach the DDR port
    io.ddrport <> ddr.module.io.port
  }
}

class TLULtoMIGUltra(TLparams: TLBundleParameters)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new XilinxVCU118MIG(
      XilinxVCU118MIGParams(
        AddressSet.misaligned(
          p(ExtMem).get.master.base,
          0x80000000L * 1 // 2GiB for the VCU118DDR,
        ))))

  // Create a dummy node where we can attach our silly TL port
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 1 << TLparams.sourceBits) // CKDUR: The maximum ID possible goes here.
      ))
    )
  })

  // Attach to the DDR
  if(p(ExtMem).head.master.beatBytes != 8)
    ddr.node := TLWidthWidget(p(ExtMem).head.master.beatBytes) := node
  else
    ddr.node := node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlport = Flipped(new TLUL(TLparams))
      val ddrport = new XilinxVCU118MIGIO(ddr.depth)
    })

    val depth = ddr.depth

    //val mem_tl = Wire(HeterogeneousBag.fromNode(node.in))
    node.out.foreach {
      case  (bundle, _) =>
        // Debug TL
        //val ilatoaxi = Module(new ilatl())
        //ilatoaxi.io.clk := clock
        //ilatoaxi.connectAxi(bundle)

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

    // Create the actual module, and attach the DDR port
    io.ddrport <> ddr.module.io.port
  }

}

class SertoMIGArtyA7(w: Int, idBits: Int = 6)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new XilinxArty100TMIG(
      XilinxArty100TMIGParams(
        AddressSet.misaligned(
          p(ExtSerMem).get.master.base,
          0x10000000L * 1 // 1GiB for the VC707DDR,
        ))))
  // Create the desser
  val params = Seq(TLMasterParameters.v1(
    name = "tl-desser",
    sourceId = IdRange(0, 1 << idBits)))
  val desser = LazyModule(new TLDesser(w, params, true)) // Attach to the DDR
  // Attach nodes
  if(p(ExtSerMem).head.master.beatBytes != 8)
    ddr.node := //TLSourceShrinker(16) :=
      TLWidthWidget(p(ExtSerMem).head.master.beatBytes) :=
      //TLFragmenter(p(ExtSerMem).head.master.beatBytes, p(MemoryBusKey).blockBytes) :=
      desser.node
  else
    ddr.node := //TLSourceShrinker(16) :=
      desser.node
  // Create the module
  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val serport = new SerialIO(w)
      val ddrport = new XilinxArty100TMIGIO(ddr.depth)
    })

    val depth = ddr.depth

    // Connect the serport
    io.serport <> desser.module.io.ser.head

    // Create the actual module, and attach the DDR port
    io.ddrport <> ddr.module.io.port
  }
}

class TLULtoMIGArtyA7(TLparams: TLBundleParameters)(implicit p :Parameters) extends LazyModule {
  // Create the DDR
  val ddr = LazyModule(
    new XilinxArty100TMIG(
      XilinxArty100TMIGParams(
        AddressSet.misaligned(
          p(ExtMem).get.master.base,
          0x10000000L * 1 // 1GiB for the VC707DDR,
        ))))

  // Create a dummy node where we can attach our silly TL port
  val node = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 1 << TLparams.sourceBits) // CKDUR: The maximum ID possible goes here.
      ))
    )
  })

  // Attach to the DDR
  if(p(ExtMem).head.master.beatBytes != 8)
    ddr.node := TLWidthWidget(p(ExtMem).head.master.beatBytes) := node
  else
    ddr.node := node

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlport = Flipped(new TLUL(TLparams))
      val ddrport = new XilinxArty100TMIGIO(ddr.depth)
    })

    val depth = ddr.depth

    //val mem_tl = Wire(HeterogeneousBag.fromNode(node.in))
    node.out.foreach {
      case  (bundle, _) =>
        // Debug TL
        //val ilatoaxi = Module(new ilatl())
        //ilatoaxi.io.clk := clock
        //ilatoaxi.connectAxi(bundle)

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

    // Create the actual module, and attach the DDR port
    io.ddrport <> ddr.module.io.port
  }

}

class TLtoPCIe(cacheBlockBytes: Int,
               TLparamsMaster: TLBundleParameters,
               TLparamsSlave: TLBundleParameters)(implicit p :Parameters)
  extends LazyModule {
  // Create the pcie

  val pcie = LazyModule(new XilinxVC707PCIeX1)

  // Create dummy nodes where we can attach our silly TL ports
  val nodeSlave = TLClientNode(Seq.tabulate(1) { channel =>
    TLMasterPortParameters.v1(
      clients = Seq(TLMasterParameters.v1(
        name = "dummy",
        sourceId = IdRange(0, 1 << TLparamsMaster.sourceBits), // CKDUR: The maximum ID possible goes here.
      ))
    )
  })

  // TODO: MODIFY EVERYTHING
  val device = new MemoryDevice
  val nodeMaster = TLManagerNode(Seq(TLSlavePortParameters.v1(
    managers = Seq(TLManagerParameters(
      address = AddressSet.misaligned(0x0, -1), // TODO The whole space, I think
      resources = pcie.axi_to_pcie_x1.device.reg,
      regionType = RegionType.UNCACHED, // cacheable
      executable = true,
      supportsGet = TransferSizes(1, cacheBlockBytes),
      supportsPutFull = TransferSizes(1, cacheBlockBytes),
      supportsPutPartial = TransferSizes(1, cacheBlockBytes),
      fifoId             = Some(0),
      mayDenyPut         = true,
      mayDenyGet         = true
    )),
    beatBytes = p(SystemBusKey).beatBytes
  )))

  // Attach to the PCIe
  pcie.crossTLIn(pcie.slave) := nodeSlave
  pcie.crossTLIn(pcie.control) := nodeSlave
  nodeMaster := pcie.crossTLOut(pcie.master)

  // The interrupt node
  val intnode = pcie.crossIntOut(pcie.intnode)

  lazy val module = new LazyModuleImp(this) {
    val io = IO(new Bundle {
      val tlportMaster = Flipped(new TLBundle(TLparamsMaster))
      val tlportSlave = Flipped(new TLBundle(TLparamsSlave))
      var port = new XilinxVC707PCIeX1IO
    })

    nodeSlave.out.foreach {
      case  (bundle, _) =>
        bundle.a.valid := io.tlportSlave.a.valid
        io.tlportSlave.a.ready := bundle.a.ready
        bundle.a.bits := io.tlportSlave.a.bits

        io.tlportSlave.b.valid := bundle.b.valid
        bundle.b.ready := io.tlportSlave.b.ready
        io.tlportSlave.b.bits := bundle.b.bits

        bundle.c.valid := io.tlportSlave.c.valid
        io.tlportSlave.c.ready := bundle.c.ready
        bundle.c.bits := io.tlportSlave.c.bits

        io.tlportSlave.d.valid := bundle.d.valid
        bundle.d.ready := io.tlportSlave.d.ready
        io.tlportSlave.d.bits := bundle.d.bits

        bundle.e.valid := io.tlportSlave.e.valid
        io.tlportSlave.e.ready := bundle.e.ready
        bundle.e.bits := io.tlportSlave.e.bits
    }

    nodeMaster.out.foreach {
      case  (bundle, _) =>
        io.tlportMaster.a.valid := bundle.a.valid
        bundle.a.ready := io.tlportMaster.a.ready
        io.tlportMaster.a.bits := bundle.a.bits

        bundle.b.valid := io.tlportMaster.b.valid
        io.tlportMaster.b.ready := bundle.b.ready
        bundle.b.bits := io.tlportMaster.b.bits

        io.tlportMaster.c.valid := bundle.c.valid
        bundle.c.ready := io.tlportMaster.c.ready
        io.tlportMaster.c.bits := bundle.c.bits

        bundle.d.valid := io.tlportMaster.d.valid
        io.tlportMaster.d.ready := bundle.d.ready
        bundle.d.bits := io.tlportMaster.d.bits

        io.tlportMaster.e.valid := bundle.e.valid
        bundle.e.ready := io.tlportMaster.e.ready
        io.tlportMaster.e.bits := bundle.e.bits
    }

    // Create the actual module, and attach the port
    io.port <> pcie.module.io.port
  }

}

//-------------------------------------------------------------------------
// ila
//-------------------------------------------------------------------------

case class ilaConf (id: Int, wt: IndexedSeq[(Int,Int)], depth: Int = 1024, storCtl: Boolean = false, advTrig: Boolean = false)

class ilaBuild(val conf : ilaConf)
{
  val l = conf.wt.length
  var prob      = Seq.empty[Bits]
  for(i <- 0 until l) {
    prob = prob :+ UInt(conf.wt(i)._1.W)
    prob(i).suggestName("probe" + i.toString)
  }
  val m = Module(new BlackBox {
    val io = IO(new Bundle {
      val clk       = Input(Clock())
      val probe     = Input(MixedVec(prob))
      for(i <- 0 until l) {
        probe(i).suggestName("probe" + i.toString)
      }
    })
    override def desiredName = "ila" + conf.id.toString
    suggestName("ila" + conf.id.toString)
  })
  ElaborationArtefacts.add("ila" + conf.id.toString + ".ila.vivado.tcl", toString)
  ElaborationArtefacts.add("ila" + conf.id.toString + ".ila.v", toVerilogString)
  override def toString: String = {
    var str = """# Autogenerated via ilaBuild (Olinguito)
# See LICENSE for license details.
"""
    str = str + "create_ip -vendor xilinx.com -library ip -name ila -module_name ila_" + conf.id.toString + " -dir $ipdir -force\n"
    str = str + "set_property -dict [list \\\n"
    str = str + "CONFIG.C_NUM_OF_PROBES {" + l.toString + "}\\\n"
    str = str + "CONFIG.C_TRIGOUT_EN {false} \\\n"
    str = str + "CONFIG.C_TRIGIN_EN {false} \\\n"
    str = str + "CONFIG.C_MONITOR_TYPE {Native} \\\n"
    str = str + "CONFIG.C_ENABLE_ILA_AXI_MON {false} \\\n"
    str = str + "CONFIG.C_DATA_DEPTH {" + conf.depth.toString + "}\\\n"
    if(conf.storCtl) str = str + "CONFIG.C_ADV_TRIGGER {true}\\\n"
    if(conf.advTrig) str = str + "CONFIG.C_EN_STRG_QUAL {1}\\\n"
    for(i <- 0 until l) {
      str = str + "CONFIG.C_PROBE" + i.toString + "_WIDTH {" + conf.wt(i)._1.toString + "}\\\n"
    }
    str = str + "] [get_ips ila_" + conf.id.toString + "]\n"
    str
  }
  def toVerilogString: String = {
    var str = """// Autogenerated via ilaBuild (Olinguito)
// See LICENSE for license details.
"""
    str = str + "module ila" + conf.id.toString + " (\n"
    for(i <- 0 until l) {
      str = str + "  input [" + (conf.wt(i)._1-1).toString + ":0] probe_" + i.toString + ",\n"
    }
    str = str + "  input clk\n"
    str = str + ");\n\n"
    str = str + "ila_" + conf.id.toString + " ila (\n"
    for(i <- 0 until l) {
      str = str + "  .probe" + i.toString + "(probe_" + i.toString + "),\n"
    }
    str = str + "  .clk(clk)\n"
    str = str + ");\n\n"
    str = str + "endmodule\n"
    str
  }
}
