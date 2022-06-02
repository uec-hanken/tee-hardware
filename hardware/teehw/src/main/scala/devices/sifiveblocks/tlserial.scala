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

  // NOTE: This is a mock. Does not represent actual IO instantiations
  val tlserial = system.serial_tl.map(A => IO(new SerialIO(A.bits.w)).suggestName("serial_tl") )

  ((system.serial_tl zip system.outer.serdesser) zip tlserial).foreach{ case((ioser, serdesser), outer_io) =>
    val bits = SerialAdapter.asyncQueue(ioser, clock, reset)
    val ram = withClockAndReset(clock, reset) {
      SerialAdapter.connectHarnessRAM(serdesser, bits, reset)
    }
    outer_io <> ram.module.io.tsi_ser
  }
}
