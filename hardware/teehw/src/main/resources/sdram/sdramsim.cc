#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

#include "sdramsim.h"

int	SDRAMSIM::operator()(int clk, int cke, int cs_n, int ras_n, int cas_n, int we_n,
		int bs, unsigned addr, int driv, int data, int dqm) {
	int	result = 0;

	if (driv) // If the bus is going out, reads don't make sense ... but
		result = data; // read what we output anyway
	else if (!clk) // If the clock is zero, return our last value
		return m_last_value; // Always called w/clk=1, thus never here
	if (!cke) {
		//fprintf(stderr, "This simulation only supports CKE high!\n");
		//fprintf(stderr, "\tCKE   = %d\n", cke);
		//fprintf(stderr, "\tCS_n  = %d\n", cs_n);
		//fprintf(stderr, "\tRAS_n = %d\n", ras_n);
		//fprintf(stderr, "\tCAS_n = %d\n", cas_n);
		//fprintf(stderr, "\tWE_n  = %d\n", we_n);
		//assert(cke);
	}

	if (m_pwrup < POWERED_UP_STATE) {
		//assert(dqm == 3); ISSI: This does not matter
		if (m_clocks_till_idle > 0)
			m_clocks_till_idle--;
		if (m_pwrup == 0) {
			assert((ras_n)&&(cas_n)&&(we_n));
			if (m_clocks_till_idle == 0) {
				m_pwrup++;
				printf("Successful power up wait, moving to state #1\n");
			}
		} else if (m_pwrup == 1) {
			if ((!cs_n)&&(!ras_n)&&(cas_n)&&(!we_n)&&(addr&0x0400)) { // PRECHARGE, ALLBANKS
				// Wait until a precharge all banks command
				m_pwrup++;
				printf("Successful precharge command, moving to state #2\n");
				m_clocks_till_idle = 3; // tRP, 3 cycles
				
				// Bank/Precharge All CMD
				for(int i=0; i<NBANKS; i++)
					m_bank_status[i] &= 0x03;
			}
		} else if (m_pwrup == 2) {
			// The ISSI memory needs 2 auto-refresh, separated by tRC
			if (m_clocks_till_idle == 0) {
			  if((!cs_n)&&(!ras_n)&&(!cas_n)&&(we_n)) { // REFRESH
					m_pwrup++;
					printf("Successful 1st auto-refresh, waiting for 2nd\n");
					m_clocks_till_idle = 9; // tRC, 9 cycles
					for(int i=0; i<m_nrefresh; i++)
						m_refresh_time[i] = MAX_REFRESH_TIME;
				}
			} else
				assert((ras_n)&&(cas_n)&&(we_n));
		} else if (m_pwrup == 3) {
			// The ISSI memory needs 2 auto-refresh, separated by tRC
			if (m_clocks_till_idle == 0) {
			  if((!cs_n)&&(!ras_n)&&(!cas_n)&&(we_n)) { // REFRESH
					m_pwrup++;
					printf("Successful 2nd auto-refresh, waiting for mode-set\n");
					m_clocks_till_idle = 9; // tRC, 9 cycles
					for(int i=0; i<m_nrefresh; i++)
						m_refresh_time[i] = MAX_REFRESH_TIME;
				}
			} else
				assert((ras_n)&&(cas_n)&&(we_n));
		} else if (m_pwrup == 4) {
			if (m_clocks_till_idle == 0) {
				if ((!cs_n)&&(!ras_n)&&(!cas_n)&&(!we_n)){
					// mode set
					printf("Mode set: %08x\n", addr);
					assert(addr == 0x021);
					m_pwrup++;
					printf("Successful mode set, moving to state #3\n");
					m_clocks_till_idle=2; // tMRD, 2 cycles
				}
			} else
				assert((ras_n)&&(cas_n)&&(we_n));
		} else if (m_pwrup == 5) {
			if (m_clocks_till_idle == 0) {
				m_pwrup = POWERED_UP_STATE;
				m_clocks_till_idle = 0;
				printf("Successful setup!  SDRAM switching to operational\n");
			} else if (m_clocks_till_idle == 1) {
				;
			} else assert(0 && "Should never get here!");
		} 
		m_next_wr = false;
	} else { // In operation ...
		
		for(int i=0; i<m_nrefresh; i++)
			m_refresh_time[i]--;

		if (m_refresh_time[m_refresh_loc] < 0) {
			assert(0 && "Failed refresh requirement");
		} 
		
		for(int i=0; i<NBANKS; i++) {
			m_bank_status[i] >>= 1;
			if (m_bank_status[i]&2)
				m_bank_status[i] |= 4;
			if (m_bank_status[i]&1) { // Bank is open
				m_bank_open_time[i] --;
				if (m_bank_open_time[i] < 0) {
					assert(0 && "Bank held open too long");
				}
			}
		}

		if (m_clocks_till_idle)
			m_clocks_till_idle--;

		if (m_fail > 0) {
			m_fail--;
			if (m_fail == 0) {
				fprintf(stderr, "Failing on schedule\n");
				exit(-3);
			}
		}

		if ((m_clocks_till_idle > 0)&&(m_next_wr)) {
			printf("SDRAM[%08x] <= %04x\n", m_wr_addr, data & 0x0ffff);
			int	waddr = m_wr_addr++, memval;
			memval = m_mem[waddr];
			if ((dqm&3)==0)
				memval = data;
			else if ((dqm&3)==3)
				;
			else if ((dqm&2)==0)
				memval = (memval & 0x000ff) | (data & 0x0ff00);
			else // if ((dqm&1)==0)
				memval = (memval & 0x0ff00) | (data & 0x000ff);
			m_mem[waddr] = memval;
			result = data;
			m_next_wr = false;
		}
		m_qloc = (m_qloc + 1)&m_qmask;
		result = (driv)?data:m_qdata[(m_qloc)&m_qmask];
		m_qdata[(m_qloc)&m_qmask] = 0;

		// if (result != 0)
			// printf("%d RESULT[%3d] = %04x\n", clk, m_qloc, result&0x0ffff);

		if ((!cs_n)&&(!ras_n)&&(!cas_n)&&(we_n)) {
			// Auto-refresh command
			m_refresh_time[m_refresh_loc] = MAX_REFRESH_TIME;
			m_refresh_loc++;
			if (m_refresh_loc >= m_nrefresh)
				m_refresh_loc = 0;
			assert((m_bank_status[0]&6) == 0);
			assert((m_bank_status[1]&6) == 0);
			assert((m_bank_status[2]&6) == 0);
			assert((m_bank_status[3]&6) == 0);
		} else if ((!cs_n)&&(!ras_n)&&(cas_n)&&(!we_n)) {
			if (addr&0x0400) {
				// Bank/Precharge All CMD
				for(int i=0; i<NBANKS; i++)
					m_bank_status[i] &= 0x03;
			} else {
				// Precharge/close single bank
				assert(0 == (bs & (~3))); // Assert w/in bounds
				m_bank_status[bs] &= 0x03; // Close the bank

				// printf("Precharging bank %d\n", bs);
			}
		} else if ((!cs_n)&&(!ras_n)&&(cas_n)&&(we_n)) {
			// printf("Activating bank %d\n", bs);
			// Activate a bank!
			if (0 != (bs & (~3))) {
				m_fail = 2;
				fprintf(stderr, "ERR: Activating a bank w/ more than 2 bits\n");
				// assert(0 == (bs & (~3))); // Assert w/in bounds
			} else if (m_bank_status[bs] != 0) {
				fprintf(stderr, "ERR: Status of bank [bs=%d] = %d != 0\n",
					bs, m_bank_status[bs]);
				m_fail = 4;
				// assert(m_bank_status[bs]==0); // Assert bank was closed
			}
			m_bank_status[bs] |= 4;
			m_bank_open_time[bs] = MAX_BANKOPEN_TIME;
			m_bank_row[bs] = addr;
		} else if ((!cs_n)&&(ras_n)&&(!cas_n)) {
			printf("R/W Op\n");
			if (!we_n) {
				// Initiate a write
				assert(0 == (bs & (~3))); // Assert w/in bounds
				assert(m_bank_status[bs]&1); // Assert bank is open

				m_wr_addr = m_bank_row[bs];
				m_wr_addr <<= 2;
				m_wr_addr |= bs;
				m_wr_addr <<= 9;
				m_wr_addr |= (addr & 0x01ff);

				assert(driv);
				printf("SDRAM[%08x] <= %04x\n", m_wr_addr, data & 0x0ffff);
				m_mem[m_wr_addr++] = data;
				m_clocks_till_idle = 2;
				m_next_wr = true;

				if (addr & 0x0400) { // Auto precharge
					m_bank_status[bs] &= 3;
					m_bank_open_time[bs] = MAX_BANKOPEN_TIME;
				}
			} else { // Initiate a read
				assert(0 == (bs & (~3))); // Assert w/in bounds
				assert(m_bank_status[bs]&1); // Assert bank is open

				unsigned	rd_addr;

				rd_addr = m_bank_row[bs] & 0x01fff;
				rd_addr <<= 2;
				rd_addr |= bs;
				rd_addr <<= 9;
				rd_addr |= (addr & 0x01ff);

				assert(!driv);
				printf("SDRAM.Q[%2d] %04x <= SDRAM[%08x]\n",
					(m_qloc+3)&m_qmask,
					m_mem[rd_addr] & 0x0ffff, rd_addr);
				m_qdata[(m_qloc+3)&m_qmask] = m_mem[rd_addr++];
				printf("SDRAM.Q[%2d] %04x <= SDRAM[%08x]\n",
					(m_qloc+4)&m_qmask,
					m_mem[rd_addr] & 0x0ffff, rd_addr);
				m_qdata[(m_qloc+4)&m_qmask] = m_mem[rd_addr++];
				m_clocks_till_idle = 2;

				if (addr & 0x0400) { // Auto precharge
					m_bank_status[bs] &= 3;
					m_bank_open_time[bs] = MAX_BANKOPEN_TIME;
				}
			}
		} else if (cs_n) {
			// Chips not asserted, DESELECT CMD equivalent of a NOOP
		} else if ((ras_n)&&(cas_n)&&(we_n)) {
			// NOOP command
		} else {
			fprintf(stderr, "Unrecognized memory command!\n");
			fprintf(stderr, "\tCS_n  = %d\n", cs_n);
			fprintf(stderr, "\tRAS_n = %d\n", ras_n);
			fprintf(stderr, "\tCAS_n = %d\n", cas_n);
			fprintf(stderr, "\tWE_n  = %d\n", we_n);
			assert(0 && "Unrecognizned command");
		}
	}

	return result & 0x0ffff;
}

SDRAMSIM* sdram = NULL;

extern "C" int sdram_tick(int clk, int cke, int cs_n, int ras_n, int cas_n, int we_n,
		int bs, int addr, int driv, int data, int dqm, int* datao)
{
	if(!sdram) {
		sdram = new SDRAMSIM();
	}
	*datao = (int)(*sdram)(clk, cke, cs_n, ras_n, cas_n, we_n,
		bs, (unsigned) addr, driv, (int)data, (int)dqm);
	return 0;
}



