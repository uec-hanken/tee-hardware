package uec.keystoneAcc.devices.aes

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripheryAESKey extends Field[AESParams]

trait HasPeripheryAES { this: BaseSubsystem =>
  val aesNode = AES.attach(AESAttachParams(p(PeripheryAESKey), pbus, ibus.fromAsync)).ioNode.makeSink
}

trait HasPeripheryAESBundle {
}

trait HasPeripheryAESModuleImp extends LazyModuleImp with HasPeripheryAESBundle {
  val outer: HasPeripheryAES
  val aes = outer.aesNode.makeIO()(ValName(s"aes"))
}
