package uec.teehardware.devices.sifiveblocks

import chisel3._
import chisel3.util.HasBlackBoxResource
import freechips.rocketchip.diplomacy._
import testchipip.CanHavePeripheryTLSerial
import uec.teehardware.TEEHWBaseSubsystem

trait CanHavePeripheryTLSerialModuleImp extends LazyModuleImp {
  val outer: CanHavePeripheryTLSerial

  // Explicitly export the tlserial port
  val serial_tl = outer.serial_tl
}
