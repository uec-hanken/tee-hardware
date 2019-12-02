package uec.keystoneAcc.devices.ed25519

object ed25519CtrlRegs {
  val key = 0x00
  val key2 = 0x20
  val qy = 0x40
  val a = 0x60
  val b = 0x80
  val c = 0xC0
  val hkey = 0x100
  val hram = 0x140
  val hsm = 0x180
  val sign = 0x1C0
  val regstatus3 = 0xFF4
  val regstatus2 = 0xFF8
  val regstatus = 0xFFC
}