package uec.teehardware.devices.sifiveblocks

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import freechips.rocketchip.diplomacy._
import testchipip.{CanHavePeripheryTLSerial, ClockedIO, SerialAdapter, SerialIO}
import uec.teehardware.{GenericIOLibraryParams, TEEHWBaseSubsystem}

trait CanHavePeripheryTLSerialModuleImp extends LazyModuleImp {
  val outer: CanHavePeripheryTLSerial

  // Explicitly export the tlserial port
  val serial_tl = outer.serial_tl
}

trait CanHavePeripheryTLSerialChipImp extends RawModule {
  implicit val p: Parameters
  val clock: Clock
  val reset: Bool
  val IOGen: GenericIOLibraryParams
  val system: CanHavePeripheryTLSerialModuleImp

  (system.serial_tl zip system.outer.serdesser).map{ case(ioser, serdesser) =>
    val outer_io = IO(new SerialIO(ioser.bits.w)).suggestName("serial_tl")
    val bits = SerialAdapter.asyncQueue(ioser, clock, reset)
    val ram = withClockAndReset(clock, reset) {
      SerialAdapter.connectHarnessRAM(serdesser, bits, reset)
    }
    outer_io <> ram.module.io.tsi_ser
  }
}
