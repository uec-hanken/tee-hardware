package uec.teehardware.devices.sifiveblocks

import chipsalliance.rocketchip.config._
import chisel3._
import chisel3.experimental.{Analog, attach}
import chisel3.util.HasBlackBoxResource
import sifive.blocks.devices.spi._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.PeripheryBusKey
import sifive.blocks.devices.pinctrl.{BasePin, EnhancedPin}
import uec.teehardware.{GenericIOLibraryParams, TEEHWBaseSubsystem}

// Frequency of SD
case object SDCardMHz extends Field[Double](20.0)

// Frequency of QSPI
case object QSPICardMHz extends Field[Double](50.0)

trait HasTEEHWPeripherySPI {
  this: TEEHWBaseSubsystem =>

  // SPI to MMC conversion.
  // TODO: There is an intention from Sifive to do MMC, but has to be manual
  // QSPI flash implementation. This is the same as HasPeripherySPIFlash
  // TODO: This is done this way instead of "HasPeripherySPIFlash" because we need to do a bind to tlclock
  val allspicfg = p(PeripherySPIKey) ++ p(PeripherySPIFlashKey)
  val spiDevs = allspicfg.map {
    case ps: SPIParams =>
      SPIAttachParams(ps).attachTo(this)
    case ps: SPIFlashParams =>
      SPIFlashAttachParams(ps, fBufferDepth = 8).attachTo(this)
    case _ =>
      throw new RuntimeException("We cannot cast a configuration of SPI?")
  }
  val spiNodes = spiDevs.map { ps => ps.ioNode.makeSink() } // TODO: Put the Isolated ones here also

  (spiDevs zip allspicfg).zipWithIndex.foreach { case ((ps, cfg), i) =>
    cfg match {
      case _ : SPIFlashParams =>
        val flash = new FlashDevice(ps.device, maxMHz = p(QSPICardMHz))
        ResourceBinding {
          Resource(flash, "reg").bind(ResourceAddress(0))
        }
      case _ : SPIParams =>
        if(i == 0) { // Only the first one is mmc
          val mmc = new MMCDevice(ps.device, p(SDCardMHz))
          ResourceBinding {
            Resource(mmc, "reg").bind(ResourceAddress(0))
          }
        }
      case _ =>
    }
    //tlclock.bind(ps.device)
  }
}

trait HasTEEHWPeripherySPIModuleImp extends LazyModuleImp {
  val outer: HasTEEHWPeripherySPI

  // SPI to MMC conversion
  val spiAll: Seq[SPIPortIO] = outer.spiNodes.zipWithIndex.map  { case(n,i) => n.makeIO()(ValName(s"spi_$i")).asInstanceOf[SPIPortIO] }
  def spi: Seq[SPIPortIO]    = spiAll
  val allspicfg              = outer.allspicfg
}

class SPIPIN(val csWidth: Int = 1) extends Bundle {
  val SCK = Analog(1.W)
  val CS = Vec(csWidth, Analog(1.W))
  val DQ = Vec(4, Analog(1.W))
}

trait HasTEEHWPeripherySPIChipImp extends RawModule {
  implicit val p: Parameters
  val clock: Clock
  val reset: Bool
  val IOGen: GenericIOLibraryParams
  val system: HasTEEHWPeripherySPIModuleImp

  val allspicfg = system.allspicfg
  val spi = system.spi.zipWithIndex.map{case(sysspi, i) =>
    val spipin = Wire(new SPISignals(() => new BasePin, sysspi.c))
    withClockAndReset(clock, reset) { SPIPinsFromPort.apply(spipin, sysspi, clock, reset, 3) }

    val spi = IO(new SPIPIN(sysspi.c.csWidth))
    val SCK = IOGen.gpio()
    SCK.suggestName(s"SCK_$i")
    attach(SCK.pad, spi.SCK)
    SCK.ConnectPin(spipin.sck)
    val CS = Seq.tabulate(sysspi.c.csWidth){j =>
      val c = IOGen.gpio()
      c.suggestName(s"CS_${i}_${j}")
      attach(c.pad, spi.CS(j))
      c.ConnectPin(spipin.cs(j))
      c
    }
    val DQ = Seq.tabulate(sysspi.dq.size){j =>
      val c = IOGen.gpio()
      c.suggestName(s"DQ_${i}_${j}")
      attach(c.pad, spi.DQ(j))
      c.ConnectPin(spipin.dq(j))
      c
    }

    spi
  }
}