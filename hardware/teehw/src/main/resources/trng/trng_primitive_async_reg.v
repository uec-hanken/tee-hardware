module trng_primitive_async_reg( clock, reset, d, q, q_n);
  input clock, reset, d;
  output q,q_n;

  reg q;
  wire clock, reset, d, q_n;
  
  assign q_n = ~q; 
  
  always @ (posedge clock or posedge reset)
  if (reset) begin
    q <= 1'b0;
  end else begin
    q <= d;
  end

endmodule



