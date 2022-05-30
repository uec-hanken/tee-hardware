package uec.teehardware.devices.sifiveblocks

import chisel3._
import chisel3.util.HasBlackBoxResource
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.PeripheryBusKey
import freechips.rocketchip.tilelink.TLIdentityNode
import sifive.fpgashells.devices.xilinx.xilinxvc707pciex1.{XilinxVC707PCIeX1, XilinxVC707PCIeX1IO}
import chipsalliance.rocketchip.config.{Field, Parameters}
import uec.teehardware.TEEHWBaseSubsystem

case class XilinxVC707PCIeX1Params(dummy: Int = 0)
case object XilinxVC707PCIe extends Field[Option[XilinxVC707PCIeX1Params]](None)

trait HasTEEHWPeripheryXilinxVC707PCIeX1 {
  this: TEEHWBaseSubsystem =>

  // PCIe port export
  val pcie = p(XilinxVC707PCIe).map { A =>
    val pcie = LazyModule(new XilinxVC707PCIeX1)
    val nodeSlave = TLIdentityNode()
    val nodeMaster = TLIdentityNode()

    // Attach to the PCIe. NOTE: For some reason, they use TLIdentityNode here. Not sure why tho.
    // Maybe is the fact of just doing a crosstalk here
    pcie.crossTLIn(pcie.slave) := nodeSlave
    pcie.crossTLIn(pcie.control) := nodeSlave
    nodeMaster := pcie.crossTLOut(pcie.master)

    val pciename = Some(s"pcie_0")
    sbus.fromMaster(pciename) {
      nodeMaster
    }
    sbus.toFixedWidthSlave(pciename) {
      nodeSlave
    }
    ibus.fromSync := pcie.intnode

    pcie
  }
}

trait HasTEEHWPeripheryXilinxVC707PCIeX1ModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryXilinxVC707PCIeX1

  val pciePorts: Option[XilinxVC707PCIeX1IO] = outer.pcie.map { pcie =>
    val port = IO(new XilinxVC707PCIeX1IO)
    port <> pcie.module.io.port
    port
  }
}

trait HasTEEHWPeripheryXilinxVC707PCIeX1ChipImp extends RawModule {
  implicit val p: Parameters
  val clock: Clock
  val reset: Bool
  val system: HasTEEHWPeripheryXilinxVC707PCIeX1ModuleImp

  val pcie: Option[XilinxVC707PCIeX1IO] = system.pciePorts.map{syspcie =>
    // Exteriorize and connect ports
    val pcie = IO(new XilinxVC707PCIeX1IO)
    pcie <> syspcie
    pcie
  }
}
