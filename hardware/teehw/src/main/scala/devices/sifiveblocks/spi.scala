package uec.teehardware.devices.sifiveblocks

import chipsalliance.rocketchip.config.Field
import chisel3._
import chisel3.util.HasBlackBoxResource
import sifive.blocks.devices.spi._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.PeripheryBusKey
import uec.teehardware.{TEEHWBaseSubsystem}

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
}
