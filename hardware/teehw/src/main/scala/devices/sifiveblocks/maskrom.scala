package uec.teehardware.devices.sifiveblocks

import uec.teehardware.TEEHWBaseSubsystem
import freechips.rocketchip.subsystem.CBUS
import freechips.rocketchip.devices.tilelink._

trait HasTEEHWPeripheryMaskROM {
  this: TEEHWBaseSubsystem =>

  // add ROM devices
  val maskROMs = p(MaskROMLocated(location)).map { MaskROM.attach(_, this, CBUS) }
}
