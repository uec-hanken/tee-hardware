package uec.teehardware.devices.aes

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripheryAESKey extends Field[List[AESParams]](List())

trait HasPeripheryAES { this: BaseSubsystem =>
  val aesNodes = p(PeripheryAESKey).map { case key =>
    AESAttachParams(key).attachTo(this).ioNode.makeSink
  }
}

trait HasPeripheryAESModuleImp extends LazyModuleImp {
  val outer: HasPeripheryAES
  val aes = outer.aesNodes.zipWithIndex.map{ case (node,i) =>
    node.makeIO()(ValName(s"aes_" + i))
  }
}
