package uec.teehardware.devices.opentitan.aes

import chisel3._
import freechips.rocketchip.config.Field
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.BaseSubsystem
import freechips.rocketchip.util.HeterogeneousBag
import uec.teehardware._

case object PeripheryAESOTKey extends Field[List[AESOTParams]](List())

trait HasPeripheryAESOT { this: TEEHWSubsystem =>
  val aesotDevs = p(PeripheryAESOTKey).map { case key =>
    AESOTAttachParams(key, alertnode).attachTo(this)
  }
  val aesot = aesotDevs.map {
    case i =>
      i.ioNode.makeSink()
  }
}

trait HasPeripheryAESOTModuleImp extends LazyModuleImp {
  val outer: HasPeripheryAESOT
  val aesot = outer.aesot.zipWithIndex.map{
    case (n,i) => n.makeIO()(ValName(s"aesot_$i"))
  }
}