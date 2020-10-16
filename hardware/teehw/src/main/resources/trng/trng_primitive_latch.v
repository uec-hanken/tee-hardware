// Same as Sifive's SR latch, but with Qbar
// See LICENSE for license details.
module trng_primitive_latch(
	input R,
	input S,
	output Q,
	output Qbar
);

  reg latch;

  // synopsys async_set_reset "set"
  // synopsys one_hot "set, reset"
  always @(S or R)
  begin
    if (S)
      latch = 1'b1;
    else if (R)
      latch = 1'b0;
  end

  assign Q = latch;
  assign Qbar = ~latch;
endmodule


