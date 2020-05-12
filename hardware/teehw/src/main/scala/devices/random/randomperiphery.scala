package uec.teehardware.devices.random

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripheryRandomKey extends Field[RandomParams]

trait HasPeripheryRandom { this: BaseSubsystem =>
  val rndNode = Random.attach(RandomAttachParams(p(PeripheryRandomKey), pbus, ibus.fromAsync)).ioNode.makeSink
}

trait HasPeripheryRandomBundle {
}

trait HasPeripheryRandomModuleImp extends LazyModuleImp with HasPeripheryRandomBundle {
  val outer: HasPeripheryRandom
  val rnd = outer.rndNode.makeIO()(ValName(s"random"))
}
