package uec.teehardware

import chisel3.util.Decoupled
import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy.{LazyModule, LazyModuleImp}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.ResetCatchAndSync
import uec.teehardware.devices.aes._
import uec.teehardware.devices.ed25519._
import uec.teehardware.devices.random._
import uec.teehardware.devices.sha3._
import uec.teehardware.devices.usb11hs._
import uec.teehardware.devices.sdram._
import uec.teehardware.devices.opentitan.aes._
import uec.teehardware.devices.opentitan.hmac._
import uec.teehardware.devices.opentitan.otp_ctrl._
import uec.teehardware.devices.sifiveblocks._
import uec.teehardware.devices.tlmemext._
import testchipip._

class TEEHWSystem(implicit p: Parameters) extends TEEHWSubsystem
  // Sifive-Rocket components
  with HasTEEHWPeripheryTLRAM
  with HasTEEHWPeripheryExtMem
  with HasTEEHWPeripheryExtSerMem
  with HasTEEHWPeripheryExtSerBus
  with HasTEEHWPeripheryMaskROM
  with HasTEEHWPeripheryGPIO
  with HasTEEHWPeripheryI2C
  with HasTEEHWPeripheryUART
  with HasTEEHWPeripherySPI
  with HasTEEHWPeripheryXilinxVC707PCIeX1
  with HasTEEHWPeripheryXDMA
  // The TEEHW components
  with HasPeripherySHA3
  with HasPeripheryed25519
  with HasPeripheryAES
  with HasPeripheryUSB11HS
  with HasPeripheryRandom
  // The opentitan components
  with HasPeripheryAESOT
  with HasPeripheryHMAC
  with HasPeripheryOTPCtrl
  // Additional stuff
  with HasSDRAM
  with CanHavePeripheryTLSerial // ONLY for simulations
  with HasTEEHWClockGroup
  with HasTEEHWPeripheryRTC
{
  // System module creation
  override lazy val module = new TEEHWSystemModule(this)
}

class TEEHWSystemModule[+L <: TEEHWSystem](_outer: L) extends TEEHWSubsystemModuleImp(_outer)
  // Sifive-Rocket components
  with HasTEEHWPeripheryExtMemModuleImp
  with HasTEEHWPeripheryExtSerMemModuleImp
  with HasTEEHWPeripheryExtSerBusModuleImp
  with HasTEEHWPeripheryGPIOModuleImp
  with HasTEEHWPeripheryI2CModuleImp
  with HasTEEHWPeripheryUARTModuleImp
  with HasTEEHWPeripherySPIModuleImp
  with HasTEEHWPeripheryXilinxVC707PCIeX1ModuleImp
  with HasTEEHWPeripheryXDMAModuleImp
  // The TEEHW components
  with HasPeripherySHA3ModuleImp
  with HasPeripheryed25519ModuleImp
  with HasPeripheryAESModuleImp
  with HasPeripheryUSB11HSModuleImp
  with HasPeripheryRandomModuleImp
  // The opentitan components
  with HasPeripheryAESOTModuleImp
  with HasPeripheryHMACModuleImp
  with HasPeripheryOTPCtrlModuleImp
  // Additional stuff
  with HasSDRAMModuleImp
  with CanHavePeripheryTLSerialModuleImp
  with HasTEEHWClockGroupModuleImp

// TODO: Why this does not exist?
class TLUL(val params: TLBundleParameters) extends Bundle {
  val a = Decoupled(new TLBundleA(params))
  val d = Flipped(Decoupled(new TLBundleD(params)))

  def ConnectTLIn(bundle: TLBundle): Unit = {
    bundle.a.valid := a.valid
    a.ready := bundle.a.ready
    bundle.a.bits := a.bits

    d.valid := bundle.d.valid
    bundle.d.ready := d.ready
    d.bits := bundle.d.bits
    //bundle.b.bits := (new TLBundleB(TLparams)).fromBits(0.U)
    bundle.b.ready := true.B
    bundle.c.valid := false.B
    //bundle.c.bits := 0.U.asTypeOf(new TLBundleC(TLparams))
    bundle.e.valid := false.B
    //bundle.e.bits := 0.U.asTypeOf(new TLBundleE(TLparams))
  }

  def ConnectTLOut(ioi: TLBundle): Unit = {
    // Connect outside the ones that can be untied
    a.valid := ioi.a.valid
    ioi.a.ready := a.ready
    a.bits := ioi.a.bits

    ioi.d.valid := d.valid
    d.ready := ioi.d.ready
    ioi.d.bits := d.bits

    // Tie off the channels we dont need...
    // ... I mean, we did tell the TLNodeParams that we only want Get and Put
    ioi.b.bits := 0.U.asTypeOf(new TLBundleB(ioi.params))
    ioi.b.valid := false.B
    ioi.c.ready := false.B
    ioi.e.ready := false.B
    // Important NOTE: We did check connections until the mbus in verilog
    // and there is no usage of channels B, C and E (except for some TL Monitors)
  }
}
