//VCS coverage exclude_file
import "DPI-C" function int sdram_tick
(
 input int clk,
 input int cke,
 input int cs_n,
 input int ras_n,
 input int cas_n,
 input int we_n,
 input int bs,
 input int addr,
 input int driv,
 input int data,
 input int dqm,
 output int datao
);

module sdramsim #(
  parameter    SDRAM_DATA_W          = 16,
  parameter    SDRAM_DQM_W           = 2
) (
  input          sdram_clk_o,
  input          sdram_cke_o,
  input          sdram_cs_o,
  input          sdram_ras_o,
  input          sdram_cas_o,
  input          sdram_we_o,
  input  [SDRAM_DQM_W-1:0]   sdram_dqm_o,
  input  [12:0]  sdram_addr_o,
  input  [1:0]   sdram_ba_o,
  output [SDRAM_DATA_W-1:0]  sdram_data_i,
  input  [SDRAM_DATA_W-1:0]  sdram_data_o,
  input          sdram_drive_o,
  input          reset
);

  int __clk;
  int __cke; 
  int __cs_n; 
  int __ras_n; 
  int __cas_n; 
  int __we_n;
  int __bs;
  int __addr; 
  int __driv; 
  int __data; 
  int __dqm;
  int __datao;
  int __ret;
  
  assign __clk = {31'd0, sdram_clk_o};
  assign __cke = {31'd0, sdram_cke_o};
  assign __cs_n = {31'd0, sdram_cs_o};
  assign __ras_n = {31'd0, sdram_ras_o};
  assign __cas_n = {31'd0, sdram_cas_o};
  assign __we_n = {31'd0, sdram_we_o};
  assign __bs = {30'd0, sdram_ba_o};
  assign __addr = {19'd0, sdram_addr_o};
  assign __driv = {31'd0, sdram_drive_o};
  assign __data = sdram_data_o;
  assign __dqm = sdram_dqm_o;
  assign sdram_data_i = __datao;
  
  always @(posedge sdram_clk_o)
    if(!reset)
      __ret = sdram_tick(__clk, __cke, __cs_n, __ras_n, __cas_n, __we_n, __bs, __addr, __driv, __data, __dqm, __datao);

endmodule
