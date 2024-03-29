package uec.teehardware.devices.ed25519

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object Peripheryed25519Key extends Field[List[ed25519Params]](List())

trait HasPeripheryed25519 { this: BaseSubsystem =>
  val ed25519Nodes = p(Peripheryed25519Key).map { case key =>
    ed25519AttachParams(key).attachTo(this).ioNode.makeSink
  }
}

trait HasPeripheryed25519ModuleImp extends LazyModuleImp {
  val outer: HasPeripheryed25519
  val ed25519 = outer.ed25519Nodes.zipWithIndex.map{ case (node, i) =>
    node.makeIO()(ValName(s"ed25519_" + i))
  }
}
