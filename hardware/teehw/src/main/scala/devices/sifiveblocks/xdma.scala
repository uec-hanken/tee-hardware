package uec.teehardware.devices.sifiveblocks

import chisel3._
import chisel3.util.HasBlackBoxResource
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.PeripheryBusKey
import sifive.fpgashells.devices.xilinx.xdma._
import uec.teehardware.TEEHWBaseSubsystem
import chipsalliance.rocketchip.config.Field
import freechips.rocketchip.tilelink.TLIdentityNode
import sifive.fpgashells.ip.xilinx.IBUFDS_GTE4
import sifive.fpgashells.shell.xilinx.XDMATopPads


// NOTE: We need an external reset for this PCIe
class XDMATopPadswReset(n: Int) extends XDMATopPads(n) {
  val erst_n = Input(Bool())
}

// Include the PCIe
case object XDMAPCIe extends Field[Option[XDMAParams]](None)

trait HasTEEHWPeripheryXDMA {
  this: TEEHWBaseSubsystem =>

  val xdma = p(XDMAPCIe).map { cfg =>
    val xdma = LazyModule(new XDMA(cfg))
    val nodeSlave = TLIdentityNode()
    val nodeMaster = TLIdentityNode()

    // Attach to the PCIe. NOTE: For some reason, they use TLIdentityNode here. Not sure why tho.
    // Maybe is the fact of just doing a crosstalk here
    xdma.crossTLIn(xdma.slave) := nodeSlave
    xdma.crossTLIn(xdma.control) := nodeSlave
    nodeMaster := xdma.crossTLOut(xdma.master)

    val pciename = Some(s"xdma_0")
    sbus.fromMaster(pciename) {
      nodeMaster
    }
    sbus.toFixedWidthSlave(pciename) {
      nodeSlave
    }
    ibus.fromSync := xdma.intnode

    xdma
  }
}

trait HasTEEHWPeripheryXDMAModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryXDMA

  val xdmaPorts: Option[XDMATopPadswReset] = outer.xdma.map { xdma =>
    // Exteriorize and connect ports
    val io = IO(new XDMATopPadswReset(p(XDMAPCIe).get.lanes))
    val ibufds = Module(new IBUFDS_GTE4)
    ibufds.suggestName(s"${name}_refclk_ibufds")
    ibufds.io.CEB := false.B
    ibufds.io.I   := io.refclk.p
    ibufds.io.IB  := io.refclk.n
    xdma.module.io.clocks.sys_clk_gt := ibufds.io.O
    xdma.module.io.clocks.sys_clk := ibufds.io.ODIV2
    xdma.module.io.clocks.sys_rst_n := io.erst_n
    io.lanes <> xdma.module.io.pads

    // Attach the child clock and reset
    // We do not need to use ChildClock and ChildReset for this one
    // I know.. weird...
    xdma.module.clock := xdma.module.io.clocks.axi_aclk
    xdma.module.reset := !io.erst_n // TODO: Not sure if works. Needs to be wrangled

    // Put this as the public member
    io
  }
}
