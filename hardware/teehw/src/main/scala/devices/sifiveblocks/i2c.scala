package uec.teehardware.devices.sifiveblocks

import chisel3._
import chisel3.util.HasBlackBoxResource
import sifive.blocks.devices.i2c._
import freechips.rocketchip.diplomacy._
import uec.teehardware.TEEHWBaseSubsystem

trait HasTEEHWPeripheryI2C {
  this: TEEHWBaseSubsystem =>

  val i2cDevs = p(PeripheryI2CKey).map { ps =>
    I2CAttachParams(ps).attachTo(this)
  }
  val i2cNodes = i2cDevs.map { ps => ps.ioNode.makeSink() }
}

trait HasTEEHWPeripheryI2CModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripheryI2C
  val i2c: Seq[I2CPort] = outer.i2cNodes.zipWithIndex.map  { case(n,i) => n.makeIO()(ValName(s"i2c_$i")).asInstanceOf[I2CPort] }
}
