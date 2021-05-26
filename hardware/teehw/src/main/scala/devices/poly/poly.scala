package uec.teehardware.devices.poly

import chisel3._
import chisel3.util._
import chisel3.util.random._
import chisel3.util.HasBlackBoxResource
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.devices.tilelink.{BasicBusBlockerParams, TLClockBlocker}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.diplomaticobjectmodel._
import freechips.rocketchip.diplomaticobjectmodel.logicaltree._
import freechips.rocketchip.diplomaticobjectmodel.model._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.prci.{ClockGroup, ClockSinkDomain}
import freechips.rocketchip.regmapper._
import freechips.rocketchip.subsystem.{Attachable, PBUS, TLBusWrapperLocation}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import sys.process._

case class PolyParams
(
  address: BigInt,
  impl: Int = 0,
  nbits: Int = 14
) {
  require(nbits == 14, "TODO: This Poly does not support nbits different than 14")
}

case class OMPOLYDevice(
                           memoryRegions: Seq[OMMemoryRegion],
                           interrupts: Seq[OMInterrupt],
                           _types: Seq[String] = Seq("OMPOLYDevice", "OMDevice", "OMComponent")
                         ) extends OMDevice

class PolyPortIO extends Bundle {
}

abstract class Poly(busWidthBytes: Int, val c: PolyParams)
                     (implicit p: Parameters)
  extends IORegisterRouter(
    RegisterRouterParams(
      name = "poly",
      compat = Seq("uec,poly-0"),
      base = c.address,
      beatBytes = busWidthBytes),
    new PolyPortIO
  )
    with HasInterruptSources {
  def nInterrupts = 4

  // The device in the dts is created here
  ResourceBinding {
    Resource(ResourceAnchors.aliases, "poly").bind(ResourceAlias(device.label))
  }

  class poly_core extends BlackBox with HasBlackBoxResource {
    override def desiredName = "poly1305_core"
    val io = IO(new Bundle {
      //Inputs
      val clk           = Input(Clock())
      val reset_n       = Input(Bool())
      val init          = Input(Bool())
      val next          = Input(Bool())
      val finish        = Input(Bool())
      val key           = Input(UInt(256.W))
      val block         = Input(UInt(128.W))
      val blocklen      = Input(UInt(5.W))
      //Outputs
      val mac           = Output(UInt(128.W))
      val ready         = Output(Bool())
    })
    // add wrapper/blackbox after it is pre-processed
    addResource("/poly1305.preprocessed.v")
  }


  lazy val module = new LazyModuleImp(this) {
    // Interrupt export
    interrupts(3) := false.B
    interrupts(2) := false.B
    interrupts(1) := false.B
    interrupts(0) := false.B

    // Key Registers
    val key_0        = RegInit(0.U(32.W))
    val key_1        = RegInit(0.U(32.W))
    val key_2        = RegInit(0.U(32.W))
    val key_3        = RegInit(0.U(32.W))
    val key_4        = RegInit(0.U(32.W))
    val key_5        = RegInit(0.U(32.W))
    val key_6        = RegInit(0.U(32.W))
    val key_7        = RegInit(0.U(32.W))

    // Block Registers
    val block_0        = RegInit(0.U(32.W))
    val block_1        = RegInit(0.U(32.W))
    val block_2        = RegInit(0.U(32.W))
    val block_3        = RegInit(0.U(32.W))

    // Block_len Registers
    val block_len      = RegInit(0.U(5.W))

    // Mac Registers
    val mac_0        = RegInit(0.U(32.W))
    val mac_1        = RegInit(0.U(32.W))
    val mac_2        = RegInit(0.U(32.W))
    val mac_3        = RegInit(0.U(32.W))

    // control Registers
    val init        = RegInit(false.B)
    val next        = RegInit(false.B)
    val finish      = RegInit(false.B)
    val reset_n     = RegInit(true.B)
    val ready       = RegInit(false.B)

    // Crypto-Core
    val poly_core = Module(new(poly_core))
    poly_core.io.reset_n := reset_n
    poly_core.io.init    := init
    poly_core.io.reset_n := reset_n
    poly_core.io.next    := next
    poly_core.io.blocklen:= block_len
    poly_core.io.clk     := clock
    poly_core.io.finish  := finish
    poly_core.io.block   := Cat(block_0,block_1,block_2,block_3)
    poly_core.io.key     := Cat(key_0,key_1,key_2,key_3,key_4,key_5,key_6,key_7)
    ready                := poly_core.io.ready
    mac_0                := poly_core.io.mac(127,96)
    mac_1                := poly_core.io.mac(95,64)
    mac_2                := poly_core.io.mac(63,32)
    mac_3                := poly_core.io.mac(31,0)

    // Tpoly register mapping
    val tpoly_map = Seq(
      PolyRegs.key_0 -> Seq(
        RegField(32, key_0, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.key_1 -> Seq(
        RegField(32, key_1, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.key_2 -> Seq(
        RegField(32, key_2, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.key_3 -> Seq(
        RegField(32, key_3, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.key_4 -> Seq(
        RegField(32, key_4, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.key_5 -> Seq(
        RegField(32, key_5, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.key_6 -> Seq(
        RegField(32, key_6, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.key_7 -> Seq(
        RegField(32, key_7, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.block_0 -> Seq(
        RegField(32, block_0, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.block_1 -> Seq(
        RegField(32, block_1, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.block_2 -> Seq(
        RegField(32, block_2, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.block_3 -> Seq(
        RegField(32, block_3, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.block_len -> Seq(
        RegField(5, block_len, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.mac_0 -> Seq(
        RegField.r(32, mac_0, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.mac_1 -> Seq(
        RegField.r(32, mac_1, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.mac_2 -> Seq(
        RegField.r(32, mac_2, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.mac_3 -> Seq(
        RegField.r(32, mac_3, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.init -> Seq(
        RegField(1, init, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.next -> Seq(
        RegField(1, next, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.finish -> Seq(
        RegField(1, finish, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.reset_n -> Seq(
        RegField(1, reset_n, RegFieldDesc("write_data", "Tpoly write data"))
      ),
      PolyRegs.ready -> Seq(
        RegField.r(1, ready, RegFieldDesc("write_data", "Tpoly write data"))
      )
    )
    regmap(
      (tpoly_map):_*
    )

  }



  val logicalTreeNode = new LogicalTreeNode(() => Some(device)) {
    def getOMComponents(resourceBindings: ResourceBindings, children: Seq[OMComponent] = Nil): Seq[OMComponent] = {
      val Description(name, mapping) = device.describe(resourceBindings)
      val memRegions = DiplomaticObjectModelAddressing.getOMMemoryRegions(name, resourceBindings, None)
      val interrupts = DiplomaticObjectModelAddressing.describeInterrupts(name, resourceBindings)
      Seq(
        OMPOLYDevice(
          memoryRegions = memRegions.map(_.copy(
            name = "poly",
            description = "POLY Push-Register Device"
          )),
          interrupts = interrupts
        )
      )
    }
  }
}

class TLPOLY(busWidthBytes: Int, params: PolyParams)(implicit p: Parameters)
  extends Poly(busWidthBytes, params) with HasTLControlRegMap

case class PolyAttachParams
(
  polypar: PolyParams,
  controlWhere: TLBusWrapperLocation = PBUS,
  blockerAddr: Option[BigInt] = None,
  controlXType: ClockCrossingType = NoCrossing,
  intXType: ClockCrossingType = NoCrossing)(implicit val p: Parameters) {

  def PolyGen(cbus: TLBusWrapper)(implicit valName: ValName): Poly with HasTLControlRegMap = {
    LazyModule(new TLPOLY(cbus.beatBytes, polypar))
  }

  def attachTo(where: Attachable)(implicit p: Parameters): Poly with HasTLControlRegMap = {
    val name = s"poly_${POLY.nextId()}"
    val cbus = where.locateTLBusWrapper(controlWhere)
    val polyClockDomainWrapper = LazyModule(new ClockSinkDomain(take = None))
    val poly = polyClockDomainWrapper { PolyGen(cbus) }
    poly.suggestName(name)

    cbus.coupleTo(s"device_named_$name") { bus =>

      val blockerOpt = blockerAddr.map { a =>
        val blocker = LazyModule(new TLClockBlocker(BasicBusBlockerParams(a, cbus.beatBytes, cbus.beatBytes)))
        cbus.coupleTo(s"bus_blocker_for_$name") { blocker.controlNode := TLFragmenter(cbus) := _ }
        blocker
      }

      polyClockDomainWrapper.clockNode := (controlXType match {
        case _: SynchronousCrossing =>
          cbus.dtsClk.map(_.bind(poly.device))
          cbus.fixedClockNode
        case _: RationalCrossing =>
          cbus.clockNode
        case _: AsynchronousCrossing =>
          val polyClockGroup = ClockGroup()
          polyClockGroup := where.asyncClockGroupsNode
          blockerOpt.map { _.clockNode := polyClockGroup } .getOrElse { polyClockGroup }
      })

      (poly.controlXing(controlXType)
        := TLFragmenter(cbus)
        := blockerOpt.map { _.node := bus } .getOrElse { bus })
    }

    (intXType match {
      case _: SynchronousCrossing => where.ibus.fromSync
      case _: RationalCrossing => where.ibus.fromRational
      case _: AsynchronousCrossing => where.ibus.fromAsync
    }) := poly.intXing(intXType)

    LogicalModuleTree.add(where.logicalTreeNode, poly.logicalTreeNode)

    poly
  }
}

object POLY {
  val nextId = {
    var i = -1; () => {
      i += 1; i
    }
  }
}




