`timescale 1ns/1ps
module ed25519_sign_core_tb;

 integer i = 0;

 // Inputs
 reg			iClk;
 reg			iRst;
 reg			iEn;
 reg	[511:0]	iHashd_key;
 reg	[511:0]	iHashd_ram;
 reg	[511:0]	iHashd_sm;

 // Outputs
 wire			oReady;
 wire			oDone;
 wire	[252:0]	Sign;
 wire	[255:0]	oSign;

 always #5 iClk = ~iClk;
 
function [511:0] changeEndian_512;
    input [511:0] value;
    changeEndian_512 = {value[7:0], value[15:8], value[23:16], value[31:24],
        value[39:32], value[47:40], value[55:48], value[63:56], value[71:64],
        value[79:72], value[87:80], value[95:88], value[103:96], value[111:104],
        value[119:112], value[127:120], value[135:128], value[143:136],
        value[151:144], value[159:152], value[167:160], value[175:168],
        value[183:176], value[191:184], value[199:192], value[207:200],
        value[215:208], value[223:216], value[231:224], value[239:232],
        value[247:240], value[255:248], value[263:256], value[271:264],
        value[279:272], value[287:280], value[295:288], value[303:296],
        value[311:304], value[319:312], value[327:320], value[335:328],
        value[343:336], value[351:344], value[359:352], value[367:360],
        value[375:368], value[383:376], value[391:384], value[399:392],
        value[407:400], value[415:408], value[423:416], value[431:424],
        value[439:432], value[447:440], value[455:448], value[463:456],
        value[471:464], value[479:472], value[487:480], value[495:488],
        value[503:496], value[511:504]};
endfunction

 // Instantiate the Unit Under Test (UUT)
 ed25519_sign_S_core_TOP uut (
	.ICLK			(iClk),
	.IRST			(iRst),
	.IEN			(iEn),
	.OREADY			(oReady),
	.ODONE			(oDone),
	.IHASHD_KEY		({iHashd_key[511:507], iHashd_key[503:264], iHashd_key[261:256]}),
	.IHASHD_RAM		(iHashd_ram),
	.IHASHD_SM		(iHashd_sm),
	.OSIGN			(Sign)
 );
 assign oSign = {Sign[252:5], 3'b0, Sign[4:0]};

 reg	[511:0]	key_mem[0:100];
 reg	[511:0]	ram_mem[0:100];
 reg	[511:0]	sm_mem[0:100];
 reg	[255:0]	s_mem[0:100];

    initial begin
        // Initialize Inputs
        iClk = 0;
        iRst = 1;
        iEn = 0;
        iHashd_key = 0;
        iHashd_ram = 0;
        iHashd_sm = 0;

        // Read test vectors into memories
        //$readmemh("keyfile.dat", key_mem);
        //$readmemh("ramfile.dat", ram_mem);
        //$readmemh("smfile.dat", sm_mem);
        //$readmemh("sfile.dat", s_mem);

        // Wait 100 ns for global reset to finish
        #100	iRst = 0;
    end

    /* Give input and enable core */
    always @* begin
        iHashd_key = changeEndian_512(key_mem[i]);
        iHashd_ram = ram_mem[i];
        iHashd_sm = sm_mem[i];
        iEn = 1'b0;
        if (oReady) begin
            iEn = 1'b1;
        end
    end

    /* Verification of result */
    always @* begin
        if (oDone) begin
            #2;
            $display("S: %064x", oSign);
            if (oSign != s_mem[i]) begin
                $display("ERROR");
                $display("key: %x", key_mem[i]);
                $display("pk: %x", ram_mem[i]);
                $display("m: %x", sm_mem[i]);
                $display("S: %x", s_mem[i]);
                $display(" : %x", oSign);
                $stop;
            end
            else begin
                $display("%d passed", i);
                i = i + 1;
            end
        end
    end

endmodule

