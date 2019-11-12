package uec.keystoneAcc.devices.ed25519

import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem

case object Peripheryed25519Key extends Field[ed25519Params]

trait HasPeripheryed25519 { this: BaseSubsystem =>
  val ed25519Node = ed25519.attach(ed25519AttachParams(p(Peripheryed25519Key), pbus, ibus.fromAsync)).ioNode.makeSink
}

trait HasPeripheryed25519Bundle {
}

trait HasPeripheryed25519ModuleImp extends LazyModuleImp with HasPeripheryed25519Bundle {
  val outer: HasPeripheryed25519
  val ed25519 = outer.ed25519Node.makeIO()(ValName(s"ed25519"))
}
