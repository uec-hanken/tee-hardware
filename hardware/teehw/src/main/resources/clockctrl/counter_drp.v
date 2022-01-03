module counter_drp (
  input         reset,
  input         ref_clock,
  input         target_clock,
  output [31:0] counter_value,
  output        done
);
  reg [31:0] counter_ref;
  reg [31:0] counter_target;
  reg        reg_done;
  wire [31:0] tmp;
  wire tmp2;
  wire done_status;
  
  always @(posedge ref_clock) begin
    if (!reset) begin
      counter_ref <= 32'h0;
      reg_done <= 1'b0; 
    end 
    else begin
      if (!done_status) begin
        counter_ref <= counter_ref + 1;
      end
      if (!tmp2) begin
        reg_done <= 1'b1;
      end
    end
  end

  always @(posedge target_clock) begin
    if (!reset) begin
      counter_target <= 32'h0;
    end
    else begin
      if (!done_status) begin
        counter_target <= counter_target + 1;
      end
    end
  end

  assign tmp = counter_ref ^ 20000000;
  assign tmp2 = |tmp;
  assign done = done_status;
  assign counter_value = counter_target;
  assign done_status = reg_done;

endmodule
