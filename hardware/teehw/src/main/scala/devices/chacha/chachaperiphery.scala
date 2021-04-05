package uec.teehardware.devices.chacha

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripheryChacha Key extends Field[List[Chacha Params]](List())

trait HasPeripheryChacha  { this: BaseSubsystem =>
  val rndNodes = p(PeripheryChacha Key).zipWithIndex.map{ case (key, i) =>
    Chacha AttachParams(key.copy(path = key.path + s"chacha_${i}/")).attachTo(this).ioNode.makeSink
  }
}

trait HasPeripheryChacha Bundle {
}

trait HasPeripheryChacha ModuleImp extends LazyModuleImp with HasPeripheryChacha Bundle {
  val outer: HasPeripheryChacha 
  val rnd = outer.rndNodes.zipWithIndex.map{ case (node, i) =>
    node.makeIO()(ValName(s"chacha _" + i))
  }
}
