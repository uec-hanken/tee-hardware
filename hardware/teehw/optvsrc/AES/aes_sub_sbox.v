module aes_sub_sbox(
	input		[7:0]	in,
	output	reg	[7:0]	out
);

 always@(*) begin
	case(in)
		8'h00: begin
			out = 8'h63;
		end
		8'h01: begin
			out = 8'h7c;
		end
		8'h02: begin
			out = 8'h77;
		end
		8'h03: begin
			out = 8'h7b;
		end
		8'h04: begin
			out = 8'hf2;
		end
		8'h05: begin
			out = 8'h6b;
		end
		8'h06: begin
			out = 8'h6f;
		end
		8'h07: begin
			out = 8'hc5;
		end
		8'h08: begin
			out = 8'h30;
		end
		8'h09: begin
			out = 8'h01;
		end
		8'h0a: begin
			out = 8'h67;
		end
		8'h0b: begin
			out = 8'h2b;
		end
		8'h0c: begin
			out = 8'hfe;
		end
		8'h0d: begin
			out = 8'hd7;
		end
		8'h0e: begin
			out = 8'hab;
		end
		8'h0f: begin
			out = 8'h76;
		end
		8'h10: begin
			out = 8'hca;
		end
		8'h11: begin
			out = 8'h82;
		end
		8'h12: begin
			out = 8'hc9;
		end
		8'h13: begin
			out = 8'h7d;
		end
		8'h14: begin
			out = 8'hfa;
		end
		8'h15: begin
			out = 8'h59;
		end
		8'h16: begin
			out = 8'h47;
		end
		8'h17: begin
			out = 8'hf0;
		end
		8'h18: begin
			out = 8'had;
		end
		8'h19: begin
			out = 8'hd4;
		end
		8'h1a: begin
			out = 8'ha2;
		end
		8'h1b: begin
			out = 8'haf;
		end
		8'h1c: begin
			out = 8'h9c;
		end
		8'h1d: begin
			out = 8'ha4;
		end
		8'h1e: begin
			out = 8'h72;
		end
		8'h1f: begin
			out = 8'hc0;
		end
		8'h20: begin
			out = 8'hb7;
		end
		8'h21: begin
			out = 8'hfd;
		end
		8'h22: begin
			out = 8'h93;
		end
		8'h23: begin
			out = 8'h26;
		end
		8'h24: begin
			out = 8'h36;
		end
		8'h25: begin
			out = 8'h3f;
		end
		8'h26: begin
			out = 8'hf7;
		end
		8'h27: begin
			out = 8'hcc;
		end
		8'h28: begin
			out = 8'h34;
		end
		8'h29: begin
			out = 8'ha5;
		end
		8'h2a: begin
			out = 8'he5;
		end
		8'h2b: begin
			out = 8'hf1;
		end
		8'h2c: begin
			out = 8'h71;
		end
		8'h2d: begin
			out = 8'hd8;
		end
		8'h2e: begin
			out = 8'h31;
		end
		8'h2f: begin
			out = 8'h15;
		end
		8'h30: begin
			out = 8'h04;
		end
		8'h31: begin
			out = 8'hc7;
		end
		8'h32: begin
			out = 8'h23;
		end
		8'h33: begin
			out = 8'hc3;
		end
		8'h34: begin
			out = 8'h18;
		end
		8'h35: begin
			out = 8'h96;
		end
		8'h36: begin
			out = 8'h05;
		end
		8'h37: begin
			out = 8'h9a;
		end
		8'h38: begin
			out = 8'h07;
		end
		8'h39: begin
			out = 8'h12;
		end
		8'h3a: begin
			out = 8'h80;
		end
		8'h3b: begin
			out = 8'he2;
		end
		8'h3c: begin
			out = 8'heb;
		end
		8'h3d: begin
			out = 8'h27;
		end
		8'h3e: begin
			out = 8'hb2;
		end
		8'h3f: begin
			out = 8'h75;
		end
		8'h40: begin
			out = 8'h09;
		end
		8'h41: begin
			out = 8'h83;
		end
		8'h42: begin
			out = 8'h2c;
		end
		8'h43: begin
			out = 8'h1a;
		end
		8'h44: begin
			out = 8'h1b;
		end
		8'h45: begin
			out = 8'h6e;
		end
		8'h46: begin
			out = 8'h5a;
		end
		8'h47: begin
			out = 8'ha0;
		end
		8'h48: begin
			out = 8'h52;
		end
		8'h49: begin
			out = 8'h3b;
		end
		8'h4a: begin
			out = 8'hd6;
		end
		8'h4b: begin
			out = 8'hb3;
		end
		8'h4c: begin
			out = 8'h29;
		end
		8'h4d: begin
			out = 8'he3;
		end
		8'h4e: begin
			out = 8'h2f;
		end
		8'h4f: begin
			out = 8'h84;
		end
		8'h50: begin
			out = 8'h53;
		end
		8'h51: begin
			out = 8'hd1;
		end
		8'h52: begin
			out = 8'h00;
		end
		8'h53: begin
			out = 8'hed;
		end
		8'h54: begin
			out = 8'h20;
		end
		8'h55: begin
			out = 8'hfc;
		end
		8'h56: begin
			out = 8'hb1;
		end
		8'h57: begin
			out = 8'h5b;
		end
		8'h58: begin
			out = 8'h6a;
		end
		8'h59: begin
			out = 8'hcb;
		end
		8'h5a: begin
			out = 8'hbe;
		end
		8'h5b: begin
			out = 8'h39;
		end
		8'h5c: begin
			out = 8'h4a;
		end
		8'h5d: begin
			out = 8'h4c;
		end
		8'h5e: begin
			out = 8'h58;
		end
		8'h5f: begin
			out = 8'hcf;
		end
		8'h60: begin
			out = 8'hd0;
		end
		8'h61: begin
			out = 8'hef;
		end
		8'h62: begin
			out = 8'haa;
		end
		8'h63: begin
			out = 8'hfb;
		end
		8'h64: begin
			out = 8'h43;
		end
		8'h65: begin
			out = 8'h4d;
		end
		8'h66: begin
			out = 8'h33;
		end
		8'h67: begin
			out = 8'h85;
		end
		8'h68: begin
			out = 8'h45;
		end
		8'h69: begin
			out = 8'hf9;
		end
		8'h6a: begin
			out = 8'h02;
		end
		8'h6b: begin
			out = 8'h7f;
		end
		8'h6c: begin
			out = 8'h50;
		end
		8'h6d: begin
			out = 8'h3c;
		end
		8'h6e: begin
			out = 8'h9f;
		end
		8'h6f: begin
			out = 8'ha8;
		end
		8'h70: begin
			out = 8'h51;
		end
		8'h71: begin
			out = 8'ha3;
		end
		8'h72: begin
			out = 8'h40;
		end
		8'h73: begin
			out = 8'h8f;
		end
		8'h74: begin
			out = 8'h92;
		end
		8'h75: begin
			out = 8'h9d;
		end
		8'h76: begin
			out = 8'h38;
		end
		8'h77: begin
			out = 8'hf5;
		end
		8'h78: begin
			out = 8'hbc;
		end
		8'h79: begin
			out = 8'hb6;
		end
		8'h7a: begin
			out = 8'hda;
		end
		8'h7b: begin
			out = 8'h21;
		end
		8'h7c: begin
			out = 8'h10;
		end
		8'h7d: begin
			out = 8'hff;
		end
		8'h7e: begin
			out = 8'hf3;
		end
		8'h7f: begin
			out = 8'hd2;
		end
		8'h80: begin
			out = 8'hcd;
		end
		8'h81: begin
			out = 8'h0c;
		end
		8'h82: begin
			out = 8'h13;
		end
		8'h83: begin
			out = 8'hec;
		end
		8'h84: begin
			out = 8'h5f;
		end
		8'h85: begin
			out = 8'h97;
		end
		8'h86: begin
			out = 8'h44;
		end
		8'h87: begin
			out = 8'h17;
		end
		8'h88: begin
			out = 8'hc4;
		end
		8'h89: begin
			out = 8'ha7;
		end
		8'h8a: begin
			out = 8'h7e;
		end
		8'h8b: begin
			out = 8'h3d;
		end
		8'h8c: begin
			out = 8'h64;
		end
		8'h8d: begin
			out = 8'h5d;
		end
		8'h8e: begin
			out = 8'h19;
		end
		8'h8f: begin
			out = 8'h73;
		end
		8'h90: begin
			out = 8'h60;
		end
		8'h91: begin
			out = 8'h81;
		end
		8'h92: begin
			out = 8'h4f;
		end
		8'h93: begin
			out = 8'hdc;
		end
		8'h94: begin
			out = 8'h22;
		end
		8'h95: begin
			out = 8'h2a;
		end
		8'h96: begin
			out = 8'h90;
		end
		8'h97: begin
			out = 8'h88;
		end
		8'h98: begin
			out = 8'h46;
		end
		8'h99: begin
			out = 8'hee;
		end
		8'h9a: begin
			out = 8'hb8;
		end
		8'h9b: begin
			out = 8'h14;
		end
		8'h9c: begin
			out = 8'hde;
		end
		8'h9d: begin
			out = 8'h5e;
		end
		8'h9e: begin
			out = 8'h0b;
		end
		8'h9f: begin
			out = 8'hdb;
		end
		8'ha0: begin
			out = 8'he0;
		end
		8'ha1: begin
			out = 8'h32;
		end
		8'ha2: begin
			out = 8'h3a;
		end
		8'ha3: begin
			out = 8'h0a;
		end
		8'ha4: begin
			out = 8'h49;
		end
		8'ha5: begin
			out = 8'h06;
		end
		8'ha6: begin
			out = 8'h24;
		end
		8'ha7: begin
			out = 8'h5c;
		end
		8'ha8: begin
			out = 8'hc2;
		end
		8'ha9: begin
			out = 8'hd3;
		end
		8'haa: begin
			out = 8'hac;
		end
		8'hab: begin
			out = 8'h62;
		end
		8'hac: begin
			out = 8'h91;
		end
		8'had: begin
			out = 8'h95;
		end
		8'hae: begin
			out = 8'he4;
		end
		8'haf: begin
			out = 8'h79;
		end
		8'hb0: begin
			out = 8'he7;
		end
		8'hb1: begin
			out = 8'hc8;
		end
		8'hb2: begin
			out = 8'h37;
		end
		8'hb3: begin
			out = 8'h6d;
		end
		8'hb4: begin
			out = 8'h8d;
		end
		8'hb5: begin
			out = 8'hd5;
		end
		8'hb6: begin
			out = 8'h4e;
		end
		8'hb7: begin
			out = 8'ha9;
		end
		8'hb8: begin
			out = 8'h6c;
		end
		8'hb9: begin
			out = 8'h56;
		end
		8'hba: begin
			out = 8'hf4;
		end
		8'hbb: begin
			out = 8'hea;
		end
		8'hbc: begin
			out = 8'h65;
		end
		8'hbd: begin
			out = 8'h7a;
		end
		8'hbe: begin
			out = 8'hae;
		end
		8'hbf: begin
			out = 8'h08;
		end
		8'hc0: begin
			out = 8'hba;
		end
		8'hc1: begin
			out = 8'h78;
		end
		8'hc2: begin
			out = 8'h25;
		end
		8'hc3: begin
			out = 8'h2e;
		end
		8'hc4: begin
			out = 8'h1c;
		end
		8'hc5: begin
			out = 8'ha6;
		end
		8'hc6: begin
			out = 8'hb4;
		end
		8'hc7: begin
			out = 8'hc6;
		end
		8'hc8: begin
			out = 8'he8;
		end
		8'hc9: begin
			out = 8'hdd;
		end
		8'hca: begin
			out = 8'h74;
		end
		8'hcb: begin
			out = 8'h1f;
		end
		8'hcc: begin
			out = 8'h4b;
		end
		8'hcd: begin
			out = 8'hbd;
		end
		8'hce: begin
			out = 8'h8b;
		end
		8'hcf: begin
			out = 8'h8a;
		end
		8'hd0: begin
			out = 8'h70;
		end
		8'hd1: begin
			out = 8'h3e;
		end
		8'hd2: begin
			out = 8'hb5;
		end
		8'hd3: begin
			out = 8'h66;
		end
		8'hd4: begin
			out = 8'h48;
		end
		8'hd5: begin
			out = 8'h03;
		end
		8'hd6: begin
			out = 8'hf6;
		end
		8'hd7: begin
			out = 8'h0e;
		end
		8'hd8: begin
			out = 8'h61;
		end
		8'hd9: begin
			out = 8'h35;
		end
		8'hda: begin
			out = 8'h57;
		end
		8'hdb: begin
			out = 8'hb9;
		end
		8'hdc: begin
			out = 8'h86;
		end
		8'hdd: begin
			out = 8'hc1;
		end
		8'hde: begin
			out = 8'h1d;
		end
		8'hdf: begin
			out = 8'h9e;
		end
		8'he0: begin
			out = 8'he1;
		end
		8'he1: begin
			out = 8'hf8;
		end
		8'he2: begin
			out = 8'h98;
		end
		8'he3: begin
			out = 8'h11;
		end
		8'he4: begin
			out = 8'h69;
		end
		8'he5: begin
			out = 8'hd9;
		end
		8'he6: begin
			out = 8'h8e;
		end
		8'he7: begin
			out = 8'h94;
		end
		8'he8: begin
			out = 8'h9b;
		end
		8'he9: begin
			out = 8'h1e;
		end
		8'hea: begin
			out = 8'h87;
		end
		8'heb: begin
			out = 8'he9;
		end
		8'hec: begin
			out = 8'hce;
		end
		8'hed: begin
			out = 8'h55;
		end
		8'hee: begin
			out = 8'h28;
		end
		8'hef: begin
			out = 8'hdf;
		end
		8'hf0: begin
			out = 8'h8c;
		end
		8'hf1: begin
			out = 8'ha1;
		end
		8'hf2: begin
			out = 8'h89;
		end
		8'hf3: begin
			out = 8'h0d;
		end
		8'hf4: begin
			out = 8'hbf;
		end
		8'hf5: begin
			out = 8'he6;
		end
		8'hf6: begin
			out = 8'h42;
		end
		8'hf7: begin
			out = 8'h68;
		end
		8'hf8: begin
			out = 8'h41;
		end
		8'hf9: begin
			out = 8'h99;
		end
		8'hfa: begin
			out = 8'h2d;
		end
		8'hfb: begin
			out = 8'h0f;
		end
		8'hfc: begin
			out = 8'hb0;
		end
		8'hfd: begin
			out = 8'h54;
		end
		8'hfe: begin
			out = 8'hbb;
		end
		8'hff: begin
			out = 8'h16;
		end
	endcase
 end

endmodule
