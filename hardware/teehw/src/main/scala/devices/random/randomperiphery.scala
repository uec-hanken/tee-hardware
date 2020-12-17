package uec.teehardware.devices.random

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripheryRandomKey extends Field[List[RandomParams]](List())

trait HasPeripheryRandom { this: BaseSubsystem =>
  val rndNodes = p(PeripheryRandomKey).zipWithIndex.map{ case (key, i) =>
    RandomAttachParams(key.copy(path = key.path + s"random_${i}/")).attachTo(this).ioNode.makeSink
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
