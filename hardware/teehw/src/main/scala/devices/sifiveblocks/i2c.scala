package uec.teehardware.devices.sifiveblocks

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import chisel3.experimental.{Analog, attach}
import chisel3.util.HasBlackBoxResource
import sifive.blocks.devices.i2c._
import freechips.rocketchip.diplomacy._
import sifive.blocks.devices.pinctrl.EnhancedPin
import uec.teehardware.{GenericIOLibraryParams, TEEHWBaseSubsystem}

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

class I2CPIN() extends Bundle {
  val SDA = Analog(1.W)
  val SCL = Analog(1.W)
}

trait HasTEEHWPeripheryI2CChipImp extends RawModule {
  implicit val p: Parameters
  val clock: Clock
  val reset: Bool
  val IOGen: GenericIOLibraryParams
  val system: HasTEEHWPeripheryI2CModuleImp

  val i2c = system.i2c.zipWithIndex.map{case(sysi2c, i) =>
    val i2c = IO(new I2CPIN)
    val SDA = IOGen.gpio()
    val SCL = IOGen.gpio()

    SDA.suggestName(s"SDA_${i}")
    SCL.suggestName(s"SCK_${i}")

    attach(i2c.SDA, SDA.pad)
    attach(i2c.SCL, SCL.pad)

    val i2cpin = Wire(new I2CSignals(() => new EnhancedPin))
    withClockAndReset(clock, reset) { I2CPinsFromPort.apply(i2cpin, sysi2c, clock, reset, 3) }

    SDA.ConnectPin(i2cpin.sda)
    SCL.ConnectPin(i2cpin.scl)

    i2c
  }
}
