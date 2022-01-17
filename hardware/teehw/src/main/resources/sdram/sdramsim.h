#ifndef	SDRAMSIM_H

#define	NBANKS	4
#define	POWERED_UP_STATE	6
#define	CLK_RATE_HZ		100000000 // = 100 MHz = 100 * 10^6
#define	PWRUP_WAIT_CKS		((int)(.000100 * CLK_RATE_HZ))
#define	MAX_BANKOPEN_TIME	((int)(.000100 * CLK_RATE_HZ))
#define	MAX_REFRESH_TIME	((int)(.064 * CLK_RATE_HZ))
#define	SDRAM_QSZ		16

#define	LGSDRAMSZB	28
#define	SDRAMSZB	(1<<LGSDRAMSZB)

class	SDRAMSIM {
	int	m_pwrup;
	int	*m_mem;
	int	m_last_value, m_qmem[4];
	int	m_bank_status[NBANKS];
	int	m_bank_row[NBANKS];
	int	m_bank_open_time[NBANKS];
	unsigned	*m_refresh_time;
	int		m_refresh_loc, m_nrefresh;
	int	m_qloc, m_qdata[SDRAM_QSZ], m_qmask, m_wr_addr;
	int	m_clocks_till_idle;
	bool	m_next_wr;
	unsigned	m_fail;
public:
	SDRAMSIM(void) {
		m_mem = new int[SDRAMSZB/2]; // 256 MB (or 512MB), or 128 Mints

		m_nrefresh = 1<<13;
		m_refresh_time = new unsigned[m_nrefresh];
		for(int i=0; i<m_nrefresh; i++)
			m_refresh_time[i] = 0;
		m_refresh_loc = 0;

		m_pwrup = 0;
		m_clocks_till_idle = 0;

		m_last_value = 0;
		m_clocks_till_idle = PWRUP_WAIT_CKS;
		m_wr_addr = 0;

		m_qloc  = 0;
		m_qmask = SDRAM_QSZ-1;

		m_next_wr = true;
		m_fail = 0;
	}

	~SDRAMSIM(void) {
		delete m_mem;
	}

	int operator()(int clk, int cke,
			int cs_n, int ras_n, int cas_n, int we_n, int bs, 
				unsigned addr,
			int driv, int data, int dqm);
	int	pwrup(void) const { return m_pwrup; }

	void	load(unsigned addr, const char *data, size_t len) {
		int		*dp;
		const char	*sp = data;
		unsigned	base;

		assert((addr&1)==0);
		base = addr & (SDRAMSZB-1);
		assert((len&1)==0);
		assert(addr + len < SDRAMSZB);
		dp = &m_mem[(base>>1)];
		for(unsigned k=0; k<len/2; k++) {
			int	v;
			v = (sp[0]<<8)|(sp[1]&0x0ff);
			sp+=2;
			*dp++ = v;
		}
	}
};

#endif
