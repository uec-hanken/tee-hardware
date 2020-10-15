module trng_primitive_not(
  input input_1,
  output output_1
  );
  not 
`ifndef SYNTHESIS
    #3
`endif
    (output_1,input_1) ;
endmodule

