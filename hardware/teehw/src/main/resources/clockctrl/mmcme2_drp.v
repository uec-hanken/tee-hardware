//BA 17.02.2021 #1
module mmcme2_drp
    #(
        // Register the LOCKED signal with teh MMCME3_ADV input clock.
        // The LOCKED_IN (LOCKED from the MMCME3_ADV) is fed into a register and then
        // passed the LOCKED_OUT when REGISTER_LOCKED is set to "Reg" or when set to
        // "NoReg" LOCKED_IN is just passed on to LOCKED_OUT without being registered.
        parameter REGISTER_LOCKED       = "Reg",
        // Use the registered LOCKED signal from the MMCME3 also for the DRP state machine.
        parameter USE_REG_LOCKED        = "No",
        // Possible/allowed combinations of above two parameters:
        // | REGISTER_LOCKED | USE_REG_LOCKED |                                            |
        // |-----------------|----------------|--------------------------------------------|
        // |      "NoReg"    |     "No"       | LOCKED is just passed through mmcme3_drp   |
        // |                 |                | and is used as is with the state machine   |
        // |      "NoReg"    |     "Yes"      | NOT ALLOWED                                |
        // |       "Reg"     |     "No"       | LOCKED is registered but the unregistered  |
        // |                 |                | version is used for the state machine.     |
        // |       "Reg"     |     "Yes"      | LOCKED is registered and the registered    |
        // |                 |                | version is also used by the state machine. |
        //
        //***********************************************************************
        // State 1 Parameters - These are for the first reconfiguration state.
        //***********************************************************************
        //
        // These parameters have an effect on the feedback path. A change on
        // these parameters will effect all of the clock outputs.
        //
        // The parameters are composed of:
        //    _MULT: This can be from 2 to 64. It has an effect on the VCO
        //          frequency which consequently, effects all of the clock
        //          outputs.
        //    _PHASE: This is the phase multiplied by 1000. For example if
        //          a phase of 24.567 deg was desired the input value would be
        //          24567. The range for the phase is from -360000 to 360000.
        //    _FRAC: This can be from 0 to 875. This represents the fractional
        //          divide multiplied by 1000.
        //          M = _MULT + _FRAC / 1000
        //          e.g. M=8.125
        //               _MULT = 8
        //               _FRAC = 125
        //    _FRAC_EN: This indicates fractional divide has been enabled. If 1
        //          then the fractional divide algorithm will be used to calculate
        //          register settings. If 0 then default calculation to be used.
        // parameter S1_CLKFBOUT_MULT          = 5,
        parameter CLKFBOUT_PHASE         = 0,
        // parameter S1_CLKFBOUT_FRAC          = 125,
        parameter CLKFBOUT_FRAC_EN       = 1,
        //
        // The bandwidth parameter effects the phase error and the jitter filter
        // capability of the MMCM. For more information on this parameter see the
        // Device user guide.
        // Possible values are: "LOW", "LOW_SS", "HIGH" and "OPTIMIZED"
        parameter BANDWIDTH              = "OPTIMIZED",
        //
        // The divclk parameter allows the input clock to be divided before it
        // reaches the phase and frequency comparator. This can be set between
        // 1 and 128.
        // parameter DIVCLK_DIVIDE          = 1,

        // The following parameters describe the configuration that each clock
        // output should have once the reconfiguration for state one has
        // completed.
        //
        // The parameters are composed of:
        //    _DIVIDE: This can be from 1 to 128
        //    _PHASE: This is the phase multiplied by 1000. For example if
        //          a phase of 24.567 deg was desired the input value would be
        //          24567. The range for the phase is from -360000 to 360000.
        //    _DUTY: This is the duty cycle multiplied by 100,000.  For example if
        //          a duty cycle of .24567 was desired the input would be
        //          24567.
        //
        // parameter S1_CLKOUT0_DIVIDE         = 1,
        // parameter S1_CLKOUT0_PHASE          = 0,
        parameter CLKOUT0_DUTY           = 50000,
        // parameter S1_CLKOUT0_FRAC          = 125,
        parameter CLKOUT0_FRAC_EN       = 1,
        //
        parameter CLKOUT5_DIVIDE        = 1,
        parameter CLKOUT5_PHASE         = 0,
        parameter CLKOUT5_DUTY          = 50000,
        //
        parameter CLKOUT6_DIVIDE        = 1,
        parameter CLKOUT6_PHASE         = 0,
        parameter CLKOUT6_DUTY          = 50000
        //
    ) (
        // These signals are controlled by user logic interface and are covered
        // in more detail within the XAPP.
        // input             SADDR,  // remove this port, replace with folowings:
        input      [6:0]  CLKFBOUT_MULT,
        input      [9:0]  CLKFBOUT_FRAC,
        input      [7:0]  CLKOUT0_DIVIDE,
        input      [9:0]  CLKOUT0_FRAC,
        input      [18:0] CLKOUT0_PHASE,
        input      [6:0]  CLKDIV_DIVIDE,
        input             SEN,
        input             SCLK,
        input             RST,
        output reg        SRDY,
        //
        // These signals are to be connected to the MMCM_ADV by port name.
        // Their use matches the MMCM port description in the Device User Guide.
        input      [15:0] DO,
        input             DRDY,
        input             LOCK_REG_CLK_IN,
        input             LOCKED_IN,
        output reg        DWE,
        output reg        DEN,
        output reg [6:0]  DADDR,
        output reg [15:0] DI,
        output            DCLK,
        output reg        RST_MMCM,
        output            LOCKED_OUT
    );
//----------------------------------------------------------------------------------------
    //
    wire        IntLocked;
    wire        IntRstMmcm;
    //
    wire [38:0] powerbits_reg, clkout0_reg2, clkout0_reg1, div_reg, clkfbout_reg1, clkfbout_reg2, lock_reg1, lock_reg2, lock_reg3, filt_reg1, filt_reg2, clkout5_reg2, clkout6_reg2;

    // 100 ps delay for behavioral simulations
    // localparam  TCQ = 100;


    // Instanitiate some regs to store 
    reg [5:0]   rom_addr;
    reg [38:0]  rom_do;
    reg         next_srdy;
    reg [5:0]   next_rom_addr;
    reg [6:0]   next_daddr;
    reg         next_dwe;
    reg         next_den;
    reg         next_rst_mmcm;
    reg [15:0]  next_di;
    reg         sen_delay;
    reg         sen_init;
    
    //
    reg [37:0]  CLKFBOUT;
    reg [37:0]  CLKFBOUT_FRAC_CALC;
    reg [9:0]   DIGITAL_FILT;
    reg [39:0]  LOCK;
    reg [37:0]  DIVCLK;
    reg [37:0]  CLKOUT0;
    reg [37:0]  CLKOUT0_FRAC_CALC;
    reg [37:0]  CLKOUT5;
    reg [37:0]  CLKOUT6;
    //
    // Insert a register in LOCKED or not depending on the value given to the parameters
    // REGISTER_LOCKED. When REGISTER_LOCKED is set to "Reg" insert a register, when set
    // to "NoReg" don't insert a register but just pass the LOCKED signal from input to
    // output.
    // Use or not, under USE_REG_LOCKED parameter control, the registered version of the
    // LOCKED signal for the DRP state machine.
    // Possible/allowed combinations of the two LOCKED related parameters:
    //
    // | REGISTER_LOCKED | USE_REG_LOCKED |                                            |
    // |-----------------|----------------|--------------------------------------------|
    // |      "NoReg"    |     "No"       | LOCKED is just passed through mmcme3_drp   |
    // |                 |                | and is used as is with the state machine   |
    // |      "NoReg"    |     "Yes"      | NOT ALLOWED                                |
    // |       "Reg"     |     "No"       | LOCKED is registered but the unregistered  |
    // |                 |                | version is used for the state machine.     |
    // |       "Reg"     |     "Yes"      | LOCKED is registered and the registered    |
    // |                 |                | version is also used by the state machine. |
    //
    generate
        if (REGISTER_LOCKED == "NoReg" && USE_REG_LOCKED == "No") begin
            assign LOCKED_OUT = LOCKED_IN;
            assign IntLocked = LOCKED_IN;
        end else if (REGISTER_LOCKED == "Reg" && USE_REG_LOCKED == "No") begin
            FDRE #(
                .INIT           (0),
                .IS_C_INVERTED  (0),
                .IS_D_INVERTED  (0),
                .IS_R_INVERTED  (0)
            ) mmcme3_drp_I_Fdrp (
                .D      (LOCKED_IN),
                .CE     (1'b1),
                .R      (IntRstMmcm),
                .C      (LOCK_REG_CLK_IN),
                .Q      (LOCKED_OUT)
            );
            //
            assign IntLocked = LOCKED_IN;
        end else if (REGISTER_LOCKED == "Reg" && USE_REG_LOCKED == "Yes") begin
            FDRE #(
                .INIT           (0),
                .IS_C_INVERTED  (0),
                .IS_D_INVERTED  (0),
                .IS_R_INVERTED  (0)
            ) mmcme3_drp_I_Fdrp (
                .D  (LOCKED_IN),
                .CE (1'b1),
                .R  (IntRstMmcm),
                .C  (LOCK_REG_CLK_IN),
                .Q  (LOCKED_OUT)
            );
            //
            assign IntLocked = LOCKED_OUT;
        end
    endgenerate

    // Integer used to initialize remainder of unused ROM
    integer     ii;

    // Pass SCLK to DCLK for the MMCM
    assign DCLK = SCLK;
    assign IntRstMmcm = RST_MMCM;

    always @(posedge SCLK) begin
        case (rom_addr)
            6'h00: rom_do <=  powerbits_reg;
            6'h01: rom_do <=  clkfbout_reg1;
            6'h02: rom_do <=  clkfbout_reg2;
            6'h03: rom_do <=  clkout6_reg2;
            6'h04: rom_do <=  div_reg;
            6'h05: rom_do <=  clkout0_reg2;
            6'h06: rom_do <=  clkout0_reg1;
            6'h07: rom_do <=  clkout5_reg2;
            6'h08: rom_do <=  lock_reg1;
            6'h09: rom_do <=  lock_reg2;
            6'h0A: rom_do <=  lock_reg3;
            6'h0B: rom_do <=  filt_reg1;
            6'h0C: rom_do <=  filt_reg2;
        endcase
    end

    always @(posedge SCLK) begin
        sen_init <= 1'b0;
        sen_delay <= SEN;
        if ((SEN == 1) & (sen_delay == 0)) begin
            sen_init <= 1'b1;
        end
    end

    // Include the MMCM reconfiguration functions.  This contains the constant
    // functions that are used in the calculations below.  This file is
    // required.
    //`include "mmcme2_drp_func.h"
    
    // ********************************************************************************************
    // Start of mmcme2_drp_func.h
    // ********************************************************************************************
    
// Define debug to provide extra messages durring elaboration
`define DEBUG 1

// FRAC_PRECISION describes the width of the fractional portion of the fixed
//    point numbers.  These should not be modified, they are for development
//    only
`define FRAC_PRECISION  10
// FIXED_WIDTH describes the total size for fixed point calculations(int+frac).
// Warning: L.50 and below will not calculate properly with FIXED_WIDTHs
//    greater than 32
`define FIXED_WIDTH     32

// This function takes a fixed point number and rounds it to the nearest
//    fractional precision bit.
function [`FIXED_WIDTH:1] round_frac
   (
      // Input is (FIXED_WIDTH-FRAC_PRECISION).FRAC_PRECISION fixed point number
      input [`FIXED_WIDTH:1] decimal,

      // This describes the precision of the fraction, for example a value
      //    of 1 would modify the fractional so that instead of being a .16
      //    fractional, it would be a .1 (rounded to the nearest 0.5 in turn)
      input [`FIXED_WIDTH:1] precision
   );

   begin

   `ifdef DEBUG
      $display("round_frac - decimal: %h, precision: %h", decimal, precision);
   `endif
      // If the fractional precision bit is high then round up
      if( decimal[(`FRAC_PRECISION-precision)] == 1'b1) begin
         round_frac = decimal + (1'b1 << (`FRAC_PRECISION-precision));
      end else begin
         round_frac = decimal;
      end
   `ifdef DEBUG
      $display("round_frac: %h", round_frac);
   `endif
   end
endfunction

// This function calculates high_time, low_time, w_edge, and no_count
//    of a non-fractional counter based on the divide and duty cycle
//
// NOTE: high_time and low_time are returned as integers between 0 and 63
//    inclusive.  64 should equal 6'b000000 (in other words it is okay to
//    ignore the overflow)
function [13:0] mmcm_divider
   (
      input [7:0] divide,        // Max divide is 128
      input [31:0] duty_cycle    // Duty cycle is multiplied by 100,000
   );

   reg [`FIXED_WIDTH:1]    duty_cycle_fix;
      // min/max allowed duty cycle range calc for divide => 64
   reg [`FIXED_WIDTH:1]    duty_cycle_min;
   reg [`FIXED_WIDTH:1]    duty_cycle_max;


   // High/Low time is initially calculated with a wider integer to prevent a
   // calculation error when it overflows to 64.
   reg [6:0]               high_time;
   reg [6:0]               low_time;
   reg                     w_edge;
   reg                     no_count;

   reg [`FIXED_WIDTH:1]    temp;

   begin
      // Duty Cycle must be between 0 and 1,000
      if(duty_cycle <=0 || duty_cycle >= 100000) begin
         $display("ERROR: duty_cycle: %d is invalid", duty_cycle);
         $finish;
      end
      if (divide >= 64) begin     // DCD and frequency generation fix if O divide => 64
           duty_cycle_min = ((divide - 64) * 100_000) / divide;
           duty_cycle_max = (64 / divide) * 100_000;
           if (duty_cycle > duty_cycle_max)  duty_cycle = duty_cycle_max;
           if (duty_cycle < duty_cycle_min)  duty_cycle = duty_cycle_min;
       end
      // Convert to FIXED_WIDTH-FRAC_PRECISION.FRAC_PRECISION fixed point
      duty_cycle_fix = (duty_cycle << `FRAC_PRECISION) / 100_000;

   `ifdef DEBUG
      $display("duty_cycle_fix: %h", duty_cycle_fix);
   `endif

      // If the divide is 1 nothing needs to be set except the no_count bit.
      //    Other values are dummies
      if(divide == 7'h01) begin
         high_time   = 7'h01;
         w_edge      = 1'b0;
         low_time    = 7'h01;
         no_count    = 1'b1;
      end else begin
         temp = round_frac(duty_cycle_fix*divide, 1);

         // comes from above round_frac
         high_time   = temp[`FRAC_PRECISION+7:`FRAC_PRECISION+1];
         // If the duty cycle * divide rounded is .5 or greater then this bit
         //    is set.
         w_edge      = temp[`FRAC_PRECISION]; // comes from round_frac

         // If the high time comes out to 0, it needs to be set to at least 1
         // and w_edge set to 0
         if(high_time == 7'h00) begin
            high_time   = 7'h01;
            w_edge      = 1'b0;
         end

         if(high_time == divide) begin
            high_time   = divide - 1;
            w_edge      = 1'b1;
         end

         // Calculate low_time based on the divide setting and set no_count to
         //    0 as it is only used when divide is 1.
         low_time    = divide - high_time;
         no_count    = 1'b0;
      end

      // Set the return value.
      mmcm_divider = {w_edge,no_count,high_time[5:0],low_time[5:0]};
   end
endfunction

// This function calculates mx, delay_time, and phase_mux
//  of a non-fractional counter based on the divide and phase
//
// NOTE: The only valid value for the MX bits is 2'b00 to ensure the coarse mux
//    is used.
function [10:0] mmcm_phase
   (
      // divide must be an integer (use fractional if not)
      //  assumed that divide already checked to be valid
      input [7:0] divide, // Max divide is 128

      // Phase is given in degrees (-360,000 to 360,000)
      input signed [31:0] phase
   );

   reg [`FIXED_WIDTH:1] phase_in_cycles;
   reg [`FIXED_WIDTH:1] phase_fixed;
   reg [1:0]            mx;
   reg [5:0]            delay_time;
   reg [2:0]            phase_mux;

   reg [`FIXED_WIDTH:1] temp;

   begin
`ifdef DEBUG
      $display("mmcm_phase-divide:%d,phase:%d",
         divide, phase);
`endif

      if ((phase < -360000) || (phase > 360000)) begin
         $display("ERROR: phase of $phase is not between -360000 and 360000");
         $finish;
      end

      // If phase is less than 0, convert it to a positive phase shift
      // Convert to (FIXED_WIDTH-FRAC_PRECISION).FRAC_PRECISION fixed point
      if(phase < 0) begin
         phase_fixed = ( (phase + 360000) << `FRAC_PRECISION ) / 1000;
      end else begin
         phase_fixed = ( phase << `FRAC_PRECISION ) / 1000;
      end

      // Put phase in terms of decimal number of vco clock cycles
      phase_in_cycles = ( phase_fixed * divide ) / 360;

`ifdef DEBUG
      $display("phase_in_cycles: %h", phase_in_cycles);
`endif


	 temp  =  round_frac(phase_in_cycles, 3);

	 // set mx to 2'b00 that the phase mux from the VCO is enabled
	 mx    			=  2'b00;
	 phase_mux      =  temp[`FRAC_PRECISION:`FRAC_PRECISION-2];
	 delay_time     =  temp[`FRAC_PRECISION+6:`FRAC_PRECISION+1];

   `ifdef DEBUG
      $display("temp: %h", temp);
   `endif

      // Setup the return value
      mmcm_phase={mx, phase_mux, delay_time};
   end
endfunction

// This function takes the divide value and outputs the necessary lock values
function [39:0] mmcm_lock_lookup
   (
      input [6:0] divide // Max divide is 64
   );

   reg [2559:0]   lookup;

   begin
      lookup = {
         // This table is composed of:
         // LockRefDly_LockFBDly_LockCnt_LockSatHigh_UnlockCnt
         40'b00110_00110_1111101000_1111101001_0000000001,
         40'b00110_00110_1111101000_1111101001_0000000001,
         40'b01000_01000_1111101000_1111101001_0000000001,
         40'b01011_01011_1111101000_1111101001_0000000001,
         40'b01110_01110_1111101000_1111101001_0000000001,
         40'b10001_10001_1111101000_1111101001_0000000001,
         40'b10011_10011_1111101000_1111101001_0000000001,
         40'b10110_10110_1111101000_1111101001_0000000001,
         40'b11001_11001_1111101000_1111101001_0000000001,
         40'b11100_11100_1111101000_1111101001_0000000001,
         40'b11111_11111_1110000100_1111101001_0000000001,
         40'b11111_11111_1100111001_1111101001_0000000001,
         40'b11111_11111_1011101110_1111101001_0000000001,
         40'b11111_11111_1010111100_1111101001_0000000001,
         40'b11111_11111_1010001010_1111101001_0000000001,
         40'b11111_11111_1001110001_1111101001_0000000001,
         40'b11111_11111_1000111111_1111101001_0000000001,
         40'b11111_11111_1000100110_1111101001_0000000001,
         40'b11111_11111_1000001101_1111101001_0000000001,
         40'b11111_11111_0111110100_1111101001_0000000001,
         40'b11111_11111_0111011011_1111101001_0000000001,
         40'b11111_11111_0111000010_1111101001_0000000001,
         40'b11111_11111_0110101001_1111101001_0000000001,
         40'b11111_11111_0110010000_1111101001_0000000001,
         40'b11111_11111_0110010000_1111101001_0000000001,
         40'b11111_11111_0101110111_1111101001_0000000001,
         40'b11111_11111_0101011110_1111101001_0000000001,
         40'b11111_11111_0101011110_1111101001_0000000001,
         40'b11111_11111_0101000101_1111101001_0000000001,
         40'b11111_11111_0101000101_1111101001_0000000001,
         40'b11111_11111_0100101100_1111101001_0000000001,
         40'b11111_11111_0100101100_1111101001_0000000001,
         40'b11111_11111_0100101100_1111101001_0000000001,
         40'b11111_11111_0100010011_1111101001_0000000001,
         40'b11111_11111_0100010011_1111101001_0000000001,
         40'b11111_11111_0100010011_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001,
         40'b11111_11111_0011111010_1111101001_0000000001
      };

      // Set lookup_entry with the explicit bits from lookup with a part select
      mmcm_lock_lookup = lookup[ ((64-divide)*40) +: 40];
   `ifdef DEBUG
      $display("lock_lookup: %b", mmcm_lock_lookup);
   `endif
   end
endfunction

// This function takes the divide value and the bandwidth setting of the MMCM
//  and outputs the digital filter settings necessary.
function [9:0] mmcm_filter_lookup
  (
     input [6:0] divide, // Max divide is 64
     input [8*9:0] BANDWIDTH
  );

  reg [639:0] lookup_low;
  reg [639:0] lookup_low_ss;
  reg [639:0] lookup_high;
  reg [639:0] lookup_optimized;

  reg [9:0] lookup_entry;

  begin
    lookup_low = {
      // CP_RES_LFHF
      10'b0010_1111_00, // 1
      10'b0010_1111_00, // 2
      10'b0010_1111_00, // 3
      10'b0010_1111_00, // 4
      10'b0010_0111_00, // ....
      10'b0010_1011_00,
      10'b0010_1101_00,
      10'b0010_0011_00,
      10'b0010_0101_00,
      10'b0010_0101_00,
      10'b0010_1001_00,
      10'b0010_1110_00,
      10'b0010_1110_00,
      10'b0010_1110_00,
      10'b0010_1110_00,
      10'b0010_0001_00,
      10'b0010_0001_00,
      10'b0010_0001_00,
      10'b0010_0110_00,
      10'b0010_0110_00,
      10'b0010_0110_00,
      10'b0010_0110_00,
      10'b0010_0110_00,
      10'b0010_0110_00,
      10'b0010_0110_00,
      10'b0010_1010_00,
      10'b0010_1010_00,
      10'b0010_1010_00,
      10'b0010_1010_00,
      10'b0010_1010_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_1100_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00,
      10'b0010_0010_00, // ....
      10'b0010_0010_00, // 61
      10'b0010_0010_00, // 62
      10'b0010_0010_00, // 63
      10'b0010_0010_00  // 64
    };

    lookup_low_ss = {
      // CP_RES_LFHF
      10'b0010_1111_11, // 1
      10'b0010_1111_11, // 2
      10'b0010_1111_11, // 3
      10'b0010_1111_11, // 4
      10'b0010_0111_11, // ....
      10'b0010_1011_11,
      10'b0010_1101_11,
      10'b0010_0011_11,
      10'b0010_0101_11,
      10'b0010_0101_11,
      10'b0010_1001_11,
      10'b0010_1110_11,
      10'b0010_1110_11,
      10'b0010_1110_11,
      10'b0010_1110_11,
      10'b0010_0001_11,
      10'b0010_0001_11,
      10'b0010_0001_11,
      10'b0010_0110_11,
      10'b0010_0110_11,
      10'b0010_0110_11,
      10'b0010_0110_11,
      10'b0010_0110_11,
      10'b0010_0110_11,
      10'b0010_0110_11,
      10'b0010_1010_11,
      10'b0010_1010_11,
      10'b0010_1010_11,
      10'b0010_1010_11,
      10'b0010_1010_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_1100_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11,
      10'b0010_0010_11, // ....
      10'b0010_0010_11, // 61
      10'b0010_0010_11, // 62
      10'b0010_0010_11, // 63
      10'b0010_0010_11  // 64
    };

    lookup_high = {
      // CP_RES_LFHF
      10'b0010_1111_00, // 1
      10'b0100_1111_00, // 2
      10'b0101_1011_00, // 3
      10'b0111_0111_00, // 4
      10'b1101_0111_00, // ....
      10'b1110_1011_00,
      10'b1110_1101_00,
      10'b1111_0011_00,
      10'b1110_0101_00,
      10'b1111_0101_00,
      10'b1111_1001_00,
      10'b1101_0001_00,
      10'b1111_1001_00,
      10'b1111_1001_00,
      10'b1111_1001_00,
      10'b1111_1001_00,
      10'b1111_0101_00,
      10'b1111_0101_00,
      10'b1100_0001_00,
      10'b1100_0001_00,
      10'b1100_0001_00,
      10'b0101_1100_00,
      10'b0101_1100_00,
      10'b0101_1100_00,
      10'b0101_1100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0010_1000_00,
      10'b0010_1000_00,
      10'b0010_1000_00,
      10'b0010_1000_00,
      10'b0010_1000_00,
      10'b0111_0001_00,
      10'b0111_0001_00,
      10'b0100_1100_00,
      10'b0100_1100_00,
      10'b0100_1100_00,
      10'b0100_1100_00,
      10'b0110_0001_00,
      10'b0110_0001_00,
      10'b0101_0110_00,
      10'b0101_0110_00,
      10'b0101_0110_00,
      10'b0010_0100_00,
      10'b0010_0100_00,
      10'b0010_0100_00, // ....
      10'b0010_0100_00, // 61
      10'b0100_1010_00, // 62
      10'b0011_1100_00, // 63
      10'b0011_1100_00  // 64
    };

    lookup_optimized = {
      // CP_RES_LFHF
      10'b0010_1111_00, // 1
      10'b0100_1111_00, // 2
      10'b0101_1011_00, // 3
      10'b0111_0111_00, // 4
      10'b1101_0111_00, // ....
      10'b1110_1011_00,
      10'b1110_1101_00,
      10'b1111_0011_00,
      10'b1110_0101_00,
      10'b1111_0101_00,
      10'b1111_1001_00,
      10'b1101_0001_00,
      10'b1111_1001_00,
      10'b1111_1001_00,
      10'b1111_1001_00,
      10'b1111_1001_00,
      10'b1111_0101_00,
      10'b1111_0101_00,
      10'b1100_0001_00,
      10'b1100_0001_00,
      10'b1100_0001_00,
      10'b0101_1100_00,
      10'b0101_1100_00,
      10'b0101_1100_00,
      10'b0101_1100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0011_0100_00,
      10'b0010_1000_00,
      10'b0010_1000_00,
      10'b0010_1000_00,
      10'b0010_1000_00,
      10'b0010_1000_00,
      10'b0111_0001_00,
      10'b0111_0001_00,
      10'b0100_1100_00,
      10'b0100_1100_00,
      10'b0100_1100_00,
      10'b0100_1100_00,
      10'b0110_0001_00,
      10'b0110_0001_00,
      10'b0101_0110_00,
      10'b0101_0110_00,
      10'b0101_0110_00,
      10'b0010_0100_00,
      10'b0010_0100_00,
      10'b0010_0100_00, // ....
      10'b0010_0100_00, // 61
      10'b0100_1010_00, // 62
      10'b0011_1100_00, // 63
      10'b0011_1100_00  // 64
    };

    // Set lookup_entry with the explicit bits from lookup with a part select
    if(BANDWIDTH == "LOW") begin
      // Low Bandwidth
      mmcm_filter_lookup = lookup_low[((64-divide)*10) +: 10];
    end
    else if (BANDWIDTH == "LOW_SS") begin
      // low Spread spectrum bandwidth
      mmcm_filter_lookup = lookup_low_ss[((64-divide)*10) +: 10];
    end
    else if (BANDWIDTH == "HIGH") begin
      // High bandwidth
      mmcm_filter_lookup = lookup_high[((64-divide)*10) +: 10];
    end
    else if (BANDWIDTH == "OPTIMIZED") begin
      // Optimized bandwidth
      mmcm_filter_lookup = lookup_optimized[((64-divide)*10) +: 10];
    end

    `ifdef DEBUG
        $display("filter_lookup: %b", mmcm_filter_lookup);
    `endif
  end
endfunction

// This function takes in the divide, phase, and duty cycle
// setting to calculate the upper and lower counter registers.
function [37:0] mmcm_count_calc
   (
      input [7:0] divide, // Max divide is 128
      input signed [31:0] phase,
      input [31:0] duty_cycle // Multiplied by 100,000
   );

   reg [13:0] div_calc;
   reg [16:0] phase_calc;

   begin
   `ifdef DEBUG
      $display("mmcm_count_calc- divide:%h, phase:%d, duty_cycle:%d",
         divide, phase, duty_cycle);
   `endif

      // w_edge[13], no_count[12], high_time[11:6], low_time[5:0]
      div_calc = mmcm_divider(divide, duty_cycle);
      // mx[10:9], pm[8:6], dt[5:0]
      phase_calc = mmcm_phase(divide, phase);

      // Return value is the upper and lower address of counter
      //    Upper address is:
      //       RESERVED    [31:26]
      //       MX          [25:24]
      //       EDGE        [23]
      //       NOCOUNT     [22]
      //       DELAY_TIME  [21:16]
      //    Lower Address is:
      //       PHASE_MUX   [15:13]
      //       RESERVED    [12]
      //       HIGH_TIME   [11:6]
      //       LOW_TIME    [5:0]

   `ifdef DEBUG
      $display("div:%d dc:%d phase:%d ht:%d lt:%d ed:%d nc:%d mx:%d dt:%d pm:%d",
         divide, duty_cycle, phase, div_calc[11:6], div_calc[5:0],
         div_calc[13], div_calc[12],
         phase_calc[16:15], phase_calc[5:0], phase_calc[14:12]);
   `endif

      mmcm_count_calc =
         {
            // Upper Address
            6'h00, phase_calc[10:9], div_calc[13:12], phase_calc[5:0],
            // Lower Address
            phase_calc[8:6], 1'b0, div_calc[11:0]
         };
   end
endfunction


// This function takes in the divide, phase, and duty cycle
// setting to calculate the upper and lower counter registers.
// for fractional multiply/divide functions.
//
//
function [37:0] mmcm_frac_count_calc
   (
      input [7:0] divide, // Max divide is 128
      input signed [31:0] phase,
      input [31:0] duty_cycle, // Multiplied by 1,000
      input [9:0] frac // Multiplied by 1000
   );

	//Required for fractional divide calculations
			  reg  [7:0]     lt_frac;
			  reg  [7:0]     ht_frac;

			  reg            wf_fall_frac;
			  reg            wf_rise_frac;

			  reg [31:0]     a;
			  reg  [7:0]     pm_rise_frac_filtered ;
			  reg  [7:0]     pm_fall_frac_filtered ;
			  reg  [7:0]     clkout0_divide_int;
			  reg  [2:0]     clkout0_divide_frac;
			  reg  [7:0]     even_part_high;
			  reg  [7:0]     even_part_low;
			  reg [15:0]     drp_reg1;
			  reg [15:0]     drp_reg2;
			  reg  [5:0]     drp_regshared;

			  reg  [7:0]     odd;
			  reg  [7:0]     odd_and_frac;

			  reg  [7:0]     pm_fall;
			  reg  [7:0]     pm_rise;
			  reg  [7:0]     dt;
			  reg  [7:0]     dt_int;
			  reg [63:0]     dt_calc;

			  reg  [7:0]     pm_rise_frac;
			  reg  [7:0]     pm_fall_frac;

			  reg [31:0]     a_per_in_octets;
			  reg [31:0]     a_phase_in_cycles;

			                 parameter precision = 0.125;
			  reg [31:0]     phase_fixed; // changed to 31:0 from 32:1 jt 5/2/11
			  reg [31:0]     phase_pos;
			  reg [31:0]     phase_vco;
			  reg [31:0]     temp;// changed to 31:0 from 32:1 jt 5/2/11
			  reg [13:0]     div_calc;
			  reg [16:0]     phase_calc;

   begin
	`ifdef DEBUG
			$display("mmcm_frac_count_calc- divide:%h, phase:%d, duty_cycle:%d",
				divide, phase, duty_cycle);
	`endif

   //convert phase to fixed
   if ((phase < -360000) || (phase > 360000)) begin
      $display("ERROR: phase of $phase is not between -360000 and 360000");
      $finish;
   end


      // Return value is
      //    Shared data
      //       RESERVED     [37:36]
      //       FRAC_TIME    [35:33]
      //       FRAC_WF_FALL [32]
      //    Register 2 - Upper address is:
      //       RESERVED     [31:26]
      //       MX           [25:24]
      //       EDGE         [23]
      //       NOCOUNT      [22]
      //       DELAY_TIME   [21:16]
      //    Register 1 - Lower Address is:
      //       PHASE_MUX    [15:13]
      //       RESERVED     [12]
      //       HIGH_TIME    [11:6]
      //       LOW_TIME     [5:0]



	clkout0_divide_frac = frac / 125;
	clkout0_divide_int = divide;

	even_part_high = clkout0_divide_int >> 1;//$rtoi(clkout0_divide_int / 2);
	even_part_low = even_part_high;

	odd = clkout0_divide_int - even_part_high - even_part_low;
	odd_and_frac = (8*odd) + clkout0_divide_frac;

	lt_frac = even_part_high - (odd_and_frac <= 9);//IF(odd_and_frac>9,even_part_high, even_part_high - 1)
	ht_frac = even_part_low  - (odd_and_frac <= 8);//IF(odd_and_frac>8,even_part_low, even_part_low- 1)

	pm_fall =  {odd[6:0],2'b00} + {6'h00, clkout0_divide_frac[2:1]}; // using >> instead of clkout0_divide_frac / 2
	pm_rise = 0; //0

	wf_fall_frac = ((odd_and_frac >=2) && (odd_and_frac <=9)) || ((clkout0_divide_frac == 1) && (clkout0_divide_int == 2));//CRS610807
	wf_rise_frac = (odd_and_frac >=1) && (odd_and_frac <=8);//IF(odd_and_frac>=1,IF(odd_and_frac <= 8,1,0),0)



	//Calculate phase in fractional cycles
	a_per_in_octets		= (8 * divide) + (frac / 125) ;
	a_phase_in_cycles	= (phase+10) * a_per_in_octets / 360000 ;//Adding 1 due to rounding errors
	pm_rise_frac		= (a_phase_in_cycles[7:0] ==8'h00)?8'h00:a_phase_in_cycles[7:0] - {a_phase_in_cycles[7:3],3'b000};

	dt_calc 	= ((phase+10) * a_per_in_octets / 8 )/360000 ;//TRUNC(phase* divide / 360); //or_simply (a_per_in_octets / 8)
	dt 	= dt_calc[7:0];

	pm_rise_frac_filtered = (pm_rise_frac >=8) ? (pm_rise_frac ) - 8: pm_rise_frac ;				//((phase_fixed * (divide + frac / 1000)) / 360) - {pm_rise_frac[7:3],3'b000};//$rtoi(clkout0_phase * clkout0_divide / 45);//a;

	dt_int			= dt + (& pm_rise_frac[7:4]); //IF(pm_rise_overwriting>7,dt+1,dt)
	pm_fall_frac		= pm_fall + pm_rise_frac;
	pm_fall_frac_filtered	= pm_fall + pm_rise_frac - {pm_fall_frac[7:3], 3'b000};

	div_calc	= mmcm_divider(divide, duty_cycle); //Use to determine edge[7], no count[6]
	phase_calc	= mmcm_phase(divide, phase);// returns{mx[1:0], phase_mux[2:0], delay_time[5:0]}



      drp_regshared[5:0] = { 2'b11, pm_fall_frac_filtered[2:0], wf_fall_frac};
      drp_reg2[15:0] = { 1'b0, clkout0_divide_frac[2:0], 1'b1, wf_rise_frac, 4'h0, dt[5:0] };
      drp_reg1[15:0] = { pm_rise_frac_filtered[2], pm_rise_frac_filtered[1], pm_rise_frac_filtered[0], 1'b0, ht_frac[5:0], lt_frac[5:0] };
      mmcm_frac_count_calc[37:0] =   {drp_regshared, drp_reg2, drp_reg1} ;

   `ifdef DEBUG
      $display("DADDR Reg1 %h", drp_reg1);
      $display("DADDR Reg2 %h", drp_reg2);
      $display("DADDR Reg Shared %h", drp_regshared);
      $display("-%d.%d p%d>>  :DADDR_9_15 frac30to28.frac_en.wf_r_frac.dt:%b%d%d_%b:DADDR_7_13 pm_f_frac_filtered_29to27.wf_f_frac_26:%b%d:DADDR_8_14.pm_r_frac_filt_15to13.ht_frac.lt_frac:%b%b%b:", divide, frac, phase, clkout0_divide_frac, 1, wf_rise_frac, dt, pm_fall_frac_filtered, wf_fall_frac, pm_rise_frac_filtered, ht_frac, lt_frac);
   `endif

   end
endfunction
    
    // ********************************************************************************************
    // End of mmcme2_drp_func.h
    // ********************************************************************************************

    //**************************************************************************
    // Everything below is associated whith the state machine that is used to
    // Read/Modify/Write to the MMCM.
    //**************************************************************************

    // State Definitions
    localparam RESTART      = 4'h1;
    localparam WAIT_LOCK    = 4'h2;
    localparam WAIT_SEN     = 4'h3;
    localparam ADDRESS      = 4'h4;
    localparam WAIT_A_DRDY  = 4'h5;
    localparam BITMASK      = 4'h6;
    localparam BITSET       = 4'h7;
    localparam WRITE        = 4'h8;
    localparam WAIT_DRDY    = 4'h9;

    // State sync
    reg [3:0]  current_state   = RESTART;
    reg [3:0]  next_state      = RESTART;

    // These variables are used to keep track of the number of iterations that
    //    each state takes to reconfigure.
    // STATE_COUNT_CONST is used to reset the counters and should match the
    //    number of registers necessary to reconfigure each state.
    localparam STATE_COUNT_CONST  = 13;
    reg [4:0] state_count         = STATE_COUNT_CONST;
    reg [4:0] next_state_count    = STATE_COUNT_CONST;

    // This block assigns the next register value from the state machine below
    always @(posedge SCLK) begin
       DADDR       <=  next_daddr;
       DWE         <=  next_dwe;
       DEN         <=  next_den;
       RST_MMCM    <=  next_rst_mmcm;
       DI          <=  next_di;

       SRDY        <=  next_srdy;

       rom_addr    <=  next_rom_addr;
       state_count <=  next_state_count;
    end

    // this block perform value calculation for MMCM-ADV-REGS from input values

    always @* begin
        CLKFBOUT           = mmcm_count_calc(CLKFBOUT_MULT, CLKFBOUT_PHASE, 50000);
        CLKFBOUT_FRAC_CALC = mmcm_frac_count_calc(CLKFBOUT_MULT, CLKFBOUT_PHASE, 50000, CLKFBOUT_FRAC);
        DIGITAL_FILT       = mmcm_filter_lookup(CLKFBOUT_MULT, BANDWIDTH);
        LOCK               = mmcm_lock_lookup(CLKFBOUT_MULT);
        DIVCLK             = mmcm_count_calc(CLKDIV_DIVIDE, 0, 50000);
        CLKOUT0            = mmcm_count_calc(CLKOUT0_DIVIDE, CLKOUT0_PHASE, CLKOUT0_DUTY);
        CLKOUT0_FRAC_CALC  = mmcm_frac_count_calc(CLKOUT0_DIVIDE, CLKOUT0_PHASE, 50000, CLKOUT0_FRAC);
        CLKOUT5            = mmcm_count_calc(CLKOUT5_DIVIDE,CLKOUT5_PHASE, CLKOUT5_DUTY);
        CLKOUT6            = mmcm_count_calc(CLKOUT6_DIVIDE,CLKOUT6_PHASE, CLKOUT6_DUTY);
    end

    // This block assigns the next state, reset is syncronous.
    always @(posedge SCLK) begin
       if(RST) begin
          current_state <=  RESTART;
       end else begin
          current_state <=  next_state;
       end
    end

    always @* begin
       // Setup the default values
       next_srdy         = 1'b0;
       next_daddr        = DADDR;
       next_dwe          = 1'b0;
       next_den          = 1'b0;
       next_rst_mmcm     = RST_MMCM;
       next_di           = DI;
       next_rom_addr     = rom_addr;
       next_state_count  = state_count;

       case (current_state)
          // If RST is asserted reset the machine
          RESTART: begin
             next_daddr     = 7'h00;
             next_di        = 16'h0000;
             next_rom_addr  = 6'h00;
             next_rst_mmcm  = 1'b1;
             next_state     = WAIT_LOCK;
          end

          // Waits for the MMCM to assert IntLocked - once it does asserts SRDY
          WAIT_LOCK: begin
             // Make sure reset is de-asserted
             next_rst_mmcm   = 1'b0;
             // Reset the number of registers left to write for the next
             // reconfiguration event.
             next_state_count = STATE_COUNT_CONST ;
             // next_rom_addr = SADDR ? STATE_COUNT_CONST : 8'h00; // comment this cause next_rom_addr is still 6'h00
             next_rom_addr  = 6'h00;

             if(IntLocked) begin
                // MMCM is IntLocked, go on to wait for the SEN signal
                next_state  = WAIT_SEN;
                // Assert SRDY to indicate that the reconfiguration module is
                // ready
                next_srdy   = 1'b1;
             end else begin
                // Keep waiting, IntLocked has not asserted yet
                next_state  = WAIT_LOCK;
             end
          end

          // Wait for the next SEN pulse and set the ROM addr appropriately
          //    based on SADDR
          WAIT_SEN: begin
             next_rom_addr  = 6'h00;
             if (sen_init) begin
                next_rom_addr  = 6'h00;
                // Go on to address the MMCM
                next_state = ADDRESS;
                // next_srdy = 1'b0;
             end else begin
                // Keep waiting for SEN to be asserted
                next_state = WAIT_SEN;
             end
          end

          // Set the address on the MMCM and assert DEN to read the value
          ADDRESS: begin
             // Reset the DCM through the reconfiguration
             next_rst_mmcm  = 1'b1;
             // Enable a read from the MMCM and set the MMCM address
             next_den       = 1'b1;
             next_daddr     = rom_do[38:32];

             // Wait for the data to be ready
             next_state     = WAIT_A_DRDY;
          end

          // Wait for DRDY to assert after addressing the MMCM
          WAIT_A_DRDY: begin
             if (DRDY) begin
                // Data is ready, mask out the bits to save
                next_state = BITMASK;
             end else begin
                // Keep waiting till data is ready
                next_state = WAIT_A_DRDY;
             end
          end

          // Zero out the bits that are not set in the mask stored in rom
          BITMASK: begin
             // Do the mask
             next_di     = rom_do[31:16] & DO;
             // Go on to set the bits
             next_state  = BITSET;
          end

          // After the input is masked, OR the bits with calculated value in rom
          BITSET: begin
             // Set the bits that need to be assigned
             next_di           = rom_do[15:0] | DI;
             // Set the next address to read from ROM
             next_rom_addr     = rom_addr + 1'b1;
             // Go on to write the data to the MMCM
             next_state        = WRITE;
          end

          // DI is setup so assert DWE, DEN, and RST_MMCM.  Subtract one from the
          //    state count and go to wait for DRDY.
          WRITE: begin
             // Set WE and EN on MMCM
             next_dwe          = 1'b1;
             next_den          = 1'b1;

             // Decrement the number of registers left to write
             next_state_count  = state_count - 1'b1;
             // Wait for the write to complete
             next_state        = WAIT_DRDY;
          end

          // Wait for DRDY to assert from the MMCM.  If the state count is not 0
          //    jump to ADDRESS (continue reconfiguration).  If state count is
          //    0 wait for lock.
          WAIT_DRDY: begin
             if(DRDY) begin
                // Write is complete
                if(state_count > 0) begin
                   // If there are more registers to write keep going
                   next_state  = ADDRESS;
                end else begin
                   // There are no more registers to write so wait for the MMCM
                   // to lock
                   next_state  = WAIT_LOCK;
                end
             end else begin
                // Keep waiting for write to complete
                next_state     = WAIT_DRDY;
             end
          end

          // If in an unknown state reset the machine
          default: begin
             next_state = RESTART;
          end
       endcase
    end
    //
    assign powerbits_reg = {7'h28, 16'h0000, 16'hFFFF};
    assign clkout0_reg2  = {7'h09, 16'h8000, CLKOUT0_FRAC_CALC[31:16]};
    assign clkout0_reg1  = {7'h08, 16'h1000, CLKOUT0_FRAC_CALC[15:0]};
    assign clkout5_reg2  = {7'h07, 16'hC000, CLKOUT5[31:30], CLKOUT0_FRAC_CALC[35:32], CLKOUT5[25:16]};
    assign div_reg       = {7'h16, 16'hC000, {2'h0, DIVCLK[23:22], DIVCLK[11:0]} };
    assign clkfbout_reg1 = {7'h14, 16'h1000, CLKFBOUT_FRAC_CALC[15:0]};
    assign clkfbout_reg2 = {7'h15, 16'h8000, CLKFBOUT_FRAC_CALC[31:16]};
    assign clkout6_reg2  = {7'h13, 16'hC000, CLKOUT6[31:30], CLKFBOUT_FRAC_CALC[35:32], CLKOUT6[25:16]};
    assign lock_reg1     = {7'h18, 16'hFC00, {6'h00, LOCK[29:20]} };
    assign lock_reg2     = {7'h19, 16'h8000, {1'b0 , LOCK[34:30], LOCK[9:0]} };
    assign lock_reg3     = {7'h1A, 16'h8000, {1'b0 , LOCK[39:35], LOCK[19:10]} };
    assign filt_reg1     = {7'h4E, 16'h66FF, DIGITAL_FILT[9], 2'h0, DIGITAL_FILT[8:7], 2'h0, DIGITAL_FILT[6], 8'h00 };
    assign filt_reg2     = {7'h4F, 16'h666F, DIGITAL_FILT[5], 2'h0, DIGITAL_FILT[4:3], 2'h0, DIGITAL_FILT[2:1], 2'h0, DIGITAL_FILT[0], 4'h0 };
    // 
endmodule
