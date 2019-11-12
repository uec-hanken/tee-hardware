package uec.keystoneAcc.devices.sha3

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object PeripherySHA3Key extends Field[SHA3Params]

trait HasPeripherySHA3 { this: BaseSubsystem =>
  val sha3Node = SHA3.attach(SHA3AttachParams(p(PeripherySHA3Key), pbus, ibus.fromAsync)).ioNode.makeSink
}

trait HasPeripherySHA3Bundle {
}

trait HasPeripherySHA3ModuleImp extends LazyModuleImp with HasPeripherySHA3Bundle {
  val outer: HasPeripherySHA3
  val sha3 = outer.sha3Node.makeIO()(ValName(s"sha3"))
}
