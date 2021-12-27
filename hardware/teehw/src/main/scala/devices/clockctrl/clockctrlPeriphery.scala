package uec.teehardware.devices.clockctrl

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tilelink._
import uec.teehardware.EXTBUS

case object PeripheryClockCtrlKey extends Field[List[ClockCtrlParams]](List())

trait HasPeripheryClockCtrl { this: Attachable =>
  val clockctrlNodes = p(PeripheryClockCtrlKey).map { case key =>
    ClockCtrlAttachParams(key).attachTo(this).ioNode.makeSink
  }
}

trait HasPeripheryClockCtrlBundle {
}

trait HasPeripheryClockCtrlModuleImp extends LazyModuleImp with HasPeripheryClockCtrlBundle {
  val outer: HasPeripheryClockCtrl
  val clockctrl = outer.clockctrlNodes.zipWithIndex.map{ case (node,i) =>
    node.makeIO()(ValName(s"clockctrl_" + i))
  }
}

trait HasPeripheryClockCtrlDummy { this: Attachable =>
  val srams = p(PeripheryClockCtrlKey).zipWithIndex.map { case(sramcfg, i) =>
    val sram = LazyModule(new TLRAM(AddressSet.misaligned(sramcfg.address, 0x1000).head, cacheable = true))
    val mbus = locateTLBusWrapper(EXTBUS)
    mbus.coupleTo(s"sram_${i}") { bus => sram.node := TLFragmenter(4, mbus.blockBytes) := bus }
    sram
  }
}