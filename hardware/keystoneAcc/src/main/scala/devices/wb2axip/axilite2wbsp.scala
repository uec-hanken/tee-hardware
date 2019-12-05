package uec.keystoneAcc.devices.wb2axip

import chisel3._
import chisel3.util._
import chisel3.experimental._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.interrupts._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._

class axlite2wbsp(par: AXI4BundleParameters) extends BlackBox (
  Map("C_AXI_DATA_WIDTH" -> par.dataBits,
    "C_AXI_ADDR_WIDTH" -> par.addrBits)) {
  val io = IO(new Bundle {
    val i_clk = Input(Clock())
    val i_axi_reset_n = Input(Bool())
    //
    val o_axi_awready = Output(Bool())
    val i_axi_awaddr = Input(UInt(par.addrBits.W))
    val i_axi_awcache = Input(UInt(4.W))
    val i_axi_awprot = Input(UInt(3.W))
    val i_axi_awvalid = Input(Bool())
    //
    val o_axi_wready = Output(Bool())
    val i_axi_wdata = Input(UInt(par.dataBits.W))
    val i_axi_wstrb = Input(UInt((par.dataBits/8).W))
    val i_axi_wvalid = Input(Bool())
    //
    val i_axi_bready = Input(Bool())
    val o_axi_bresp = Output(UInt(2.W))
    val o_axi_bvalid = Output(Bool())
    //
    val o_axi_arready = Output(Bool())
    val i_axi_araddr = Input(UInt(par.addrBits.W))
    val i_axi_arcache = Input(UInt(4.W))
    val i_axi_arprot = Input(UInt(3.W))
    val i_axi_arvalid = Input(Bool())
    //
    val i_axi_rready = Input(Bool())
    val o_axi_rdata = Output(UInt(par.dataBits.W))
    val o_axi_rresp = Output(UInt(2.W))
    val o_axi_rvalid = Output(Bool())
    //
    // Wishbone interface
    val o_reset = Output(Bool())
    val o_wb_cyc = Output(Bool())
    val o_wb_stb = Output(Bool())
    val o_wb_we = Output(Bool())
    val o_wb_addr = Output(UInt((par.addrBits-2).W))
    val o_wb_data = Output(UInt(par.dataBits.W))
    val o_wb_sel = Output(UInt((par.dataBits/8).W))
    val i_wb_ack = Input(Bool())
    val i_wb_stall = Input(Bool())
    val i_wb_data = Input(UInt(par.dataBits.W))
    val i_wb_err = Input(Bool())
  })
}