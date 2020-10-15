module trng_primitive_nand(
  input input_1,
  input input_2,
  output output_1
  );
  nand 
`ifndef SYNTHESIS
    #2
`endif
    (output_1,input_1,input_2) ;
endmodule

