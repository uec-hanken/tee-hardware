package uec.teehardware.devices.random

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripheryRandomKey extends Field[List[RandomParams]]

trait HasPeripheryRandom { this: BaseSubsystem =>
  val rndNodes = p(PeripheryRandomKey).map{ case key =>
    RandomAttachParams(key).attachTo(this).ioNode.makeSink
  }
}

trait HasPeripheryRandomBundle {
}

trait HasPeripheryRandomModuleImp extends LazyModuleImp with HasPeripheryRandomBundle {
  val outer: HasPeripheryRandom
  val rnd = outer.rndNodes.zipWithIndex.map{ case (node, i) =>
    node.makeIO()(ValName(s"random_" + i))
  }
}
