//======================================================================
//
// Copyright (c) 2018, NORDUnet A/S All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// - Redistributions of source code must retain the above copyright
//   notice, this list of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright
//   notice, this list of conditions and the following disclaimer in the
//   documentation and/or other materials provided with the distribution.
//
// - Neither the name of the NORDUnet nor the names of its contributors may
//   be used to endorse or promote products derived from this software
//   without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
// IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
// PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
// TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
// PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
// NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//======================================================================

/* IMPORTANT: The scope of `define in Verilog is somewhat unevident (one
              "compilation unit"), thus all `defines are prefixed with
              CRYPTECH_ to prevent any potentical conflicts.
              */
              
/* NOTE: You can comment the following line when simulating to use generic
         primitives and get some speedup, otherwise un-comment it to take
         advantage of vendor-specific hardware math slices when building the
         bitstream.
         */
   

    // generic/vendor switch
//`define CRYPTECH_USE_VENDOR_PRIMITIVES


    //
    // Generic Math Primitives
    //
`define CRYPTECH_PRIMITIVE_MAC16_GENERIC mac16_generic
`define CRYPTECH_PRIMITIVE_ADD32_GENERIC adder32_generic
`define CRYPTECH_PRIMITIVE_ADD47_GENERIC adder47_generic
`define CRYPTECH_PRIMITIVE_SUB32_GENERIC subtractor32_generic

`define CRYPTECH_PRIMITIVE_ADD32_CE_GENERIC adder32_ce_generic
`define CRYPTECH_PRIMITIVE_SUB32_CE_GENERIC subtractor32_ce_generic

`define CRYPTECH_PRIMITIVE_MODEXP_SYSTOLIC_PE_GENERIC modexp_systolic_pe_generic
    

    //
    // Xilinx Math Primitives for Artix-7 Family
    //
`define CRYPTECH_PRIMITIVE_MAC16_VENDOR  mac16_artix7
`define CRYPTECH_PRIMITIVE_ADD32_VENDOR  adder32_artix7
`define CRYPTECH_PRIMITIVE_ADD47_VENDOR  adder47_artix7
`define CRYPTECH_PRIMITIVE_SUB32_VENDOR  subtractor32_artix7

`define CRYPTECH_PRIMITIVE_ADD32_CE_VENDOR adder32_ce_artix7
`define CRYPTECH_PRIMITIVE_SUB32_CE_VENDOR subtractor32_ce_artix7

`define CRYPTECH_PRIMITIVE_MODEXP_SYSTOLIC_PE_VENDOR modexp_systolic_pe_artix7



/* map CRYPTECH_PRIMITIVE_* to either CRYPTECH_PRIMITIVE_*_GENERIC or 
   CRYPTECH_PRIMITIVE_*_VENDOR based on the value of the earlier generic/vendor
   switch.
   */
   
`ifndef CRYPTECH_USE_VENDOR_PRIMITIVES

    // generic primitives
`define CRYPTECH_PRIMITIVE_MAC16 `CRYPTECH_PRIMITIVE_MAC16_GENERIC
`define CRYPTECH_PRIMITIVE_ADD32 `CRYPTECH_PRIMITIVE_ADD32_GENERIC
`define CRYPTECH_PRIMITIVE_ADD47 `CRYPTECH_PRIMITIVE_ADD47_GENERIC
`define CRYPTECH_PRIMITIVE_SUB32 `CRYPTECH_PRIMITIVE_SUB32_GENERIC

`define CRYPTECH_PRIMITIVE_ADD32_CE `CRYPTECH_PRIMITIVE_ADD32_CE_GENERIC
`define CRYPTECH_PRIMITIVE_SUB32_CE `CRYPTECH_PRIMITIVE_SUB32_CE_GENERIC

`define CRYPTECH_PRIMITIVE_MODEXP_SYSTOLIC_PE `CRYPTECH_PRIMITIVE_MODEXP_SYSTOLIC_PE_GENERIC

`else

    // vendor-specific primitives
`define CRYPTECH_PRIMITIVE_MAC16 `CRYPTECH_PRIMITIVE_MAC16_VENDOR
`define CRYPTECH_PRIMITIVE_ADD47 `CRYPTECH_PRIMITIVE_ADD47_VENDOR
`define CRYPTECH_PRIMITIVE_ADD32 `CRYPTECH_PRIMITIVE_ADD32_VENDOR
`define CRYPTECH_PRIMITIVE_SUB32 `CRYPTECH_PRIMITIVE_SUB32_VENDOR

`define CRYPTECH_PRIMITIVE_ADD32_CE `CRYPTECH_PRIMITIVE_ADD32_CE_VENDOR
`define CRYPTECH_PRIMITIVE_SUB32_CE `CRYPTECH_PRIMITIVE_SUB32_CE_VENDOR

`define CRYPTECH_PRIMITIVE_MODEXP_SYSTOLIC_PE `CRYPTECH_PRIMITIVE_MODEXP_SYSTOLIC_PE_VENDOR

`endif


//------------------------------------------------------------------------------
// End-of-File
//------------------------------------------------------------------------------
