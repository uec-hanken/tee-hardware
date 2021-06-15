package uec.teehardware.devices.chacha

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripheryChachaKey extends Field[List[ChachaParams]](List())

trait HasPeripheryChacha  { this: BaseSubsystem =>
  val chaNodes = p(PeripheryChachaKey).zipWithIndex.map{ case (key, i) =>
    ChachaAttachParams(key).attachTo(this).ioNode.makeSink
  }
}

trait HasPeripheryChachaBundle {
}

trait HasPeripheryChachaModuleImp extends LazyModuleImp with HasPeripheryChachaBundle {
  val outer: HasPeripheryChacha 
  val cha = outer.chaNodes.zipWithIndex.map{ case (node, i) =>
    node.makeIO()(ValName(s"chacha_" + i))
  }
}
