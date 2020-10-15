module trng_primitive_latch(
	input R,
	input S,
	output Q,
	output Qbar
);
  assign
`ifndef SYNTHESIS
    #2
`endif
    Q_i = Q;
  assign 
`ifndef SYNTHESIS
    #2
`endif
    Qbar_i = Qbar;
  assign 
`ifndef SYNTHESIS
    #2
`endif
    Q = ~ (R | Qbar);
  assign 
`ifndef SYNTHESIS
   #2
`endif 
   Qbar = ~ (S | Q);
endmodule


