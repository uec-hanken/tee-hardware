package uec.teehardware.devices.sha3

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripherySHA3Key extends Field[List[SHA3Params]](List())

trait HasPeripherySHA3 { this: BaseSubsystem =>
  val sha3Nodes = p(PeripherySHA3Key).map{ case key =>
    SHA3AttachParams(key).attachTo(this).ioNode.makeSink
  }
}

trait HasPeripherySHA3Bundle {
}

trait HasPeripherySHA3ModuleImp extends LazyModuleImp with HasPeripherySHA3Bundle {
  val outer: HasPeripherySHA3
  val sha3 = outer.sha3Nodes.zipWithIndex.map{ case (node, i) =>
    node.makeIO()(ValName(s"sha3_" + i))
  }
}
