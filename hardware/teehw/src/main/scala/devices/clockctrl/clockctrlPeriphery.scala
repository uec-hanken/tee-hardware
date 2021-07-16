package uec.teehardware.devices.clockctrl

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.{Attachable, BaseSubsystem, HasTileLinkLocations}

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
