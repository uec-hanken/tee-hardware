package uec.teehardware.devices.poly

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripheryPolyKey extends Field[List[PolyParams]](List())

trait HasPeripheryPoly  { this: BaseSubsystem =>
  val poNodes = p(PeripheryPolyKey).map{ case key =>
    PolyAttachParams(key).attachTo(this).ioNode.makeSink
  }
}

trait HasPeripheryPolyBundle {
}

trait HasPeripheryPolyModuleImp extends LazyModuleImp with HasPeripheryPolyBundle {
  val outer: HasPeripheryPoly
  val poly = outer.poNodes.zipWithIndex.map{ case (node, i) =>
    node.makeIO()(ValName(s"poly_" + i))
  }
}
