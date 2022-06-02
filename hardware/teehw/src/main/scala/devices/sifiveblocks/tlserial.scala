package uec.teehardware.devices.sifiveblocks

import chipsalliance.rocketchip.config.Parameters
import chisel3._
import freechips.rocketchip.diplomacy._
import testchipip.{CanHavePeripheryTLSerial, ClockedIO, SerialIO}
import uec.teehardware.{GenericIOLibraryParams}

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
  // NOTE: cannot be serial_tl.get.bits.w. Is SERIAL_TSI_WIDTH because is the side of the TSI
  val tlserial = system.serial_tl.map(A => IO(new ClockedIO(new SerialIO(A.bits.w))).suggestName("serial_tl") )
  (system.serial_tl zip tlserial).foreach { case (ioser, outer_io) =>
    outer_io <> ioser
  }

  val serdesser = system.outer.serdesser
}
