/*
 * Copyright 2013, Homer Hsing <homer.hsing@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

module round2in1(
	input	[1599:0]	in,
	input	[6:0]		rc1,
	input	[6:0]		rc2,
	output	[1599:0]	out
);

/*****************************************************************************
 *                 Internal Wires and Registers Declarations                 *
 *****************************************************************************/
 
 /* "a ~ g" for round 1 */
 wire	[63:0]	a[4:0][4:0];
 wire	[63:0]	b[4:0];
 wire	[63:0]	c[4:0][4:0];
 wire	[63:0]	d[4:0][4:0];
 wire	[63:0]	e[4:0][4:0];
 wire	[63:0]	f[4:0][4:0];
 wire	[63:0]	g[4:0][4:0];

 /* "aa ~ gg" for round 2 */
 wire	[63:0]	bb[4:0];
 wire	[63:0]	cc[4:0][4:0];
 wire	[63:0]	dd[4:0][4:0];
 wire	[63:0]	ee[4:0][4:0];
 wire	[63:0]	ff[4:0][4:0];
 wire	[63:0]	gg[4:0][4:0];

/*****************************************************************************
 *                                 Inputs                                    *
 *****************************************************************************/
 
 assign a[0][0] = in[1599:1536];
 assign a[1][0] = in[1535:1472];
 assign a[2][0] = in[1471:1408];
 assign a[3][0] = in[1407:1344];
 assign a[4][0] = in[1343:1280];
 assign a[0][1] = in[1279:1216];
 assign a[1][1] = in[1215:1152];
 assign a[2][1] = in[1151:1088];
 assign a[3][1] = in[1087:1024];
 assign a[4][1] = in[1023:960];
 assign a[0][2] = in[959:896];
 assign a[1][2] = in[895:832];
 assign a[2][2] = in[831:768];
 assign a[3][2] = in[767:704];
 assign a[4][2] = in[703:640];
 assign a[0][3] = in[639:576];
 assign a[1][3] = in[575:512];
 assign a[2][3] = in[511:448];
 assign a[3][3] = in[447:384];
 assign a[4][3] = in[383:320];
 assign a[0][4] = in[319:256];
 assign a[1][4] = in[255:192];
 assign a[2][4] = in[191:128];
 assign a[3][4] = in[127:64];
 assign a[4][4] = in[63:0];

/*****************************************************************************
 *                                 Round 1                                   *
 *****************************************************************************/
 
 assign b[0] = a[0][0] ^ a[0][1] ^ a[0][2] ^ a[0][3] ^ a[0][4];
 assign b[1] = a[1][0] ^ a[1][1] ^ a[1][2] ^ a[1][3] ^ a[1][4];
 assign b[2] = a[2][0] ^ a[2][1] ^ a[2][2] ^ a[2][3] ^ a[2][4];
 assign b[3] = a[3][0] ^ a[3][1] ^ a[3][2] ^ a[3][3] ^ a[3][4];
 assign b[4] = a[4][0] ^ a[4][1] ^ a[4][2] ^ a[4][3] ^ a[4][4];

 /* calc "c == theta(a)" */
 assign c[0][0] = a[0][0] ^ b[4] ^ {b[1][62:0], b[1][63]};
 assign c[1][0] = a[1][0] ^ b[0] ^ {b[2][62:0], b[2][63]};
 assign c[2][0] = a[2][0] ^ b[1] ^ {b[3][62:0], b[3][63]};
 assign c[3][0] = a[3][0] ^ b[2] ^ {b[4][62:0], b[4][63]};
 assign c[4][0] = a[4][0] ^ b[3] ^ {b[0][62:0], b[0][63]};
 assign c[0][1] = a[0][1] ^ b[4] ^ {b[1][62:0], b[1][63]};
 assign c[1][1] = a[1][1] ^ b[0] ^ {b[2][62:0], b[2][63]};
 assign c[2][1] = a[2][1] ^ b[1] ^ {b[3][62:0], b[3][63]};
 assign c[3][1] = a[3][1] ^ b[2] ^ {b[4][62:0], b[4][63]};
 assign c[4][1] = a[4][1] ^ b[3] ^ {b[0][62:0], b[0][63]};
 assign c[0][2] = a[0][2] ^ b[4] ^ {b[1][62:0], b[1][63]};
 assign c[1][2] = a[1][2] ^ b[0] ^ {b[2][62:0], b[2][63]};
 assign c[2][2] = a[2][2] ^ b[1] ^ {b[3][62:0], b[3][63]};
 assign c[3][2] = a[3][2] ^ b[2] ^ {b[4][62:0], b[4][63]};
 assign c[4][2] = a[4][2] ^ b[3] ^ {b[0][62:0], b[0][63]};
 assign c[0][3] = a[0][3] ^ b[4] ^ {b[1][62:0], b[1][63]};
 assign c[1][3] = a[1][3] ^ b[0] ^ {b[2][62:0], b[2][63]};
 assign c[2][3] = a[2][3] ^ b[1] ^ {b[3][62:0], b[3][63]};
 assign c[3][3] = a[3][3] ^ b[2] ^ {b[4][62:0], b[4][63]};
 assign c[4][3] = a[4][3] ^ b[3] ^ {b[0][62:0], b[0][63]};
 assign c[0][4] = a[0][4] ^ b[4] ^ {b[1][62:0], b[1][63]};
 assign c[1][4] = a[1][4] ^ b[0] ^ {b[2][62:0], b[2][63]};
 assign c[2][4] = a[2][4] ^ b[1] ^ {b[3][62:0], b[3][63]};
 assign c[3][4] = a[3][4] ^ b[2] ^ {b[4][62:0], b[4][63]};
 assign c[4][4] = a[4][4] ^ b[3] ^ {b[0][62:0], b[0][63]};

 /* calc "d == rho(c)" */
 assign d[0][0] = c[0][0];
 assign d[1][0] = {c[1][0][62:0], c[1][0][63]};
 assign d[2][0] = {c[2][0][1:0] , c[2][0][63:2]};
 assign d[3][0] = {c[3][0][35:0], c[3][0][63:36]};
 assign d[4][0] = {c[4][0][36:0], c[4][0][63:37]};
 assign d[0][1] = {c[0][1][27:0], c[0][1][63:28]};
 assign d[1][1] = {c[1][1][19:0], c[1][1][63:20]};
 assign d[2][1] = {c[2][1][57:0], c[2][1][63:58]};
 assign d[3][1] = {c[3][1][8:0] , c[3][1][63:9]};
 assign d[4][1] = {c[4][1][43:0], c[4][1][63:44]};
 assign d[0][2] = {c[0][2][60:0], c[0][2][63:61]};
 assign d[1][2] = {c[1][2][53:0], c[1][2][63:54]};
 assign d[2][2] = {c[2][2][20:0], c[2][2][63:21]};
 assign d[3][2] = {c[3][2][38:0], c[3][2][63:39]};
 assign d[4][2] = {c[4][2][24:0], c[4][2][63:25]};
 assign d[0][3] = {c[0][3][22:0], c[0][3][63:23]};
 assign d[1][3] = {c[1][3][18:0], c[1][3][63:19]};
 assign d[2][3] = {c[2][3][48:0], c[2][3][63:49]};
 assign d[3][3] = {c[3][3][42:0], c[3][3][63:43]};
 assign d[4][3] = {c[4][3][55:0], c[4][3][63:56]};
 assign d[0][4] = {c[0][4][45:0], c[0][4][63:46]};
 assign d[1][4] = {c[1][4][61:0], c[1][4][63:62]};
 assign d[2][4] = {c[2][4][2:0] , c[2][4][63:3]};
 assign d[3][4] = {c[3][4][7:0] , c[3][4][63:8]};
 assign d[4][4] = {c[4][4][49:0], c[4][4][63:50]};

 /* calc "e == pi(d)" */
 assign e[0][0] = d[0][0];
 assign e[0][2] = d[1][0];
 assign e[0][4] = d[2][0];
 assign e[0][1] = d[3][0];
 assign e[0][3] = d[4][0];
 assign e[1][3] = d[0][1];
 assign e[1][0] = d[1][1];
 assign e[1][2] = d[2][1];
 assign e[1][4] = d[3][1];
 assign e[1][1] = d[4][1];
 assign e[2][1] = d[0][2];
 assign e[2][3] = d[1][2];
 assign e[2][0] = d[2][2];
 assign e[2][2] = d[3][2];
 assign e[2][4] = d[4][2];
 assign e[3][4] = d[0][3];
 assign e[3][1] = d[1][3];
 assign e[3][3] = d[2][3];
 assign e[3][0] = d[3][3];
 assign e[3][2] = d[4][3];
 assign e[4][2] = d[0][4];
 assign e[4][4] = d[1][4];
 assign e[4][1] = d[2][4];
 assign e[4][3] = d[3][4];
 assign e[4][0] = d[4][4];

 /* calc "f = chi(e)" */
 assign f[0][0] = e[0][0] ^ (~e[1][0] & e[2][0]);
 assign f[1][0] = e[1][0] ^ (~e[2][0] & e[3][0]);
 assign f[2][0] = e[2][0] ^ (~e[3][0] & e[4][0]);
 assign f[3][0] = e[3][0] ^ (~e[4][0] & e[0][0]);
 assign f[4][0] = e[4][0] ^ (~e[0][0] & e[1][0]);
 assign f[0][1] = e[0][1] ^ (~e[1][1] & e[2][1]);
 assign f[1][1] = e[1][1] ^ (~e[2][1] & e[3][1]);
 assign f[2][1] = e[2][1] ^ (~e[3][1] & e[4][1]);
 assign f[3][1] = e[3][1] ^ (~e[4][1] & e[0][1]);
 assign f[4][1] = e[4][1] ^ (~e[0][1] & e[1][1]);
 assign f[0][2] = e[0][2] ^ (~e[1][2] & e[2][2]);
 assign f[1][2] = e[1][2] ^ (~e[2][2] & e[3][2]);
 assign f[2][2] = e[2][2] ^ (~e[3][2] & e[4][2]);
 assign f[3][2] = e[3][2] ^ (~e[4][2] & e[0][2]);
 assign f[4][2] = e[4][2] ^ (~e[0][2] & e[1][2]);
 assign f[0][3] = e[0][3] ^ (~e[1][3] & e[2][3]);
 assign f[1][3] = e[1][3] ^ (~e[2][3] & e[3][3]);
 assign f[2][3] = e[2][3] ^ (~e[3][3] & e[4][3]);
 assign f[3][3] = e[3][3] ^ (~e[4][3] & e[0][3]);
 assign f[4][3] = e[4][3] ^ (~e[0][3] & e[1][3]);
 assign f[0][4] = e[0][4] ^ (~e[1][4] & e[2][4]);
 assign f[1][4] = e[1][4] ^ (~e[2][4] & e[3][4]);
 assign f[2][4] = e[2][4] ^ (~e[3][4] & e[4][4]);
 assign f[3][4] = e[3][4] ^ (~e[4][4] & e[0][4]);
 assign f[4][4] = e[4][4] ^ (~e[0][4] & e[1][4]);

 /* calc "g = iota(f)" */
 assign g[0][0][63]    = f[0][0][63] ^ rc1[6];
 assign g[0][0][62:32] = f[0][0][62:32];
 assign g[0][0][31]    = f[0][0][31] ^ rc1[5];
 assign g[0][0][30:16] = f[0][0][30:16];
 assign g[0][0][15]    = f[0][0][15] ^ rc1[4];
 assign g[0][0][14:8]  = f[0][0][14:8];
 assign g[0][0][7]     = f[0][0][7] ^ rc1[3];
 assign g[0][0][6:4]   = f[0][0][6:4];
 assign g[0][0][3]     = f[0][0][3] ^ rc1[2];
 assign g[0][0][2]     = f[0][0][2];
 assign g[0][0][1]     = f[0][0][1] ^ rc1[1];
 assign g[0][0][0]     = f[0][0][0] ^ rc1[0];

 assign g[1][0] = f[1][0];
 assign g[2][0] = f[2][0];
 assign g[3][0] = f[3][0];
 assign g[4][0] = f[4][0];
 assign g[0][1] = f[0][1];
 assign g[1][1] = f[1][1];
 assign g[2][1] = f[2][1];
 assign g[3][1] = f[3][1];
 assign g[4][1] = f[4][1];
 assign g[0][2] = f[0][2];
 assign g[1][2] = f[1][2];
 assign g[2][2] = f[2][2];
 assign g[3][2] = f[3][2];
 assign g[4][2] = f[4][2];
 assign g[0][3] = f[0][3];
 assign g[1][3] = f[1][3];
 assign g[2][3] = f[2][3];
 assign g[3][3] = f[3][3];
 assign g[4][3] = f[4][3];
 assign g[0][4] = f[0][4];
 assign g[1][4] = f[1][4];
 assign g[2][4] = f[2][4];
 assign g[3][4] = f[3][4];
 assign g[4][4] = f[4][4];

/*****************************************************************************
 *                                 Round 2                                   *
 *****************************************************************************/
  
 assign bb[0] = g[0][0] ^ g[0][1] ^ g[0][2] ^ g[0][3] ^ g[0][4];
 assign bb[1] = g[1][0] ^ g[1][1] ^ g[1][2] ^ g[1][3] ^ g[1][4];
 assign bb[2] = g[2][0] ^ g[2][1] ^ g[2][2] ^ g[2][3] ^ g[2][4];
 assign bb[3] = g[3][0] ^ g[3][1] ^ g[3][2] ^ g[3][3] ^ g[3][4];
 assign bb[4] = g[4][0] ^ g[4][1] ^ g[4][2] ^ g[4][3] ^ g[4][4];
 
 /* calc "cc == theta(g)" */
 assign cc[0][0] = g[0][0] ^ bb[4] ^ {bb[1][62:0], bb[1][63]};
 assign cc[1][0] = g[1][0] ^ bb[0] ^ {bb[2][62:0], bb[2][63]};
 assign cc[2][0] = g[2][0] ^ bb[1] ^ {bb[3][62:0], bb[3][63]};
 assign cc[3][0] = g[3][0] ^ bb[2] ^ {bb[4][62:0], bb[4][63]};
 assign cc[4][0] = g[4][0] ^ bb[3] ^ {bb[0][62:0], bb[0][63]};
 assign cc[0][1] = g[0][1] ^ bb[4] ^ {bb[1][62:0], bb[1][63]};
 assign cc[1][1] = g[1][1] ^ bb[0] ^ {bb[2][62:0], bb[2][63]};
 assign cc[2][1] = g[2][1] ^ bb[1] ^ {bb[3][62:0], bb[3][63]};
 assign cc[3][1] = g[3][1] ^ bb[2] ^ {bb[4][62:0], bb[4][63]};
 assign cc[4][1] = g[4][1] ^ bb[3] ^ {bb[0][62:0], bb[0][63]};
 assign cc[0][2] = g[0][2] ^ bb[4] ^ {bb[1][62:0], bb[1][63]};
 assign cc[1][2] = g[1][2] ^ bb[0] ^ {bb[2][62:0], bb[2][63]};
 assign cc[2][2] = g[2][2] ^ bb[1] ^ {bb[3][62:0], bb[3][63]};
 assign cc[3][2] = g[3][2] ^ bb[2] ^ {bb[4][62:0], bb[4][63]};
 assign cc[4][2] = g[4][2] ^ bb[3] ^ {bb[0][62:0], bb[0][63]};
 assign cc[0][3] = g[0][3] ^ bb[4] ^ {bb[1][62:0], bb[1][63]};
 assign cc[1][3] = g[1][3] ^ bb[0] ^ {bb[2][62:0], bb[2][63]};
 assign cc[2][3] = g[2][3] ^ bb[1] ^ {bb[3][62:0], bb[3][63]};
 assign cc[3][3] = g[3][3] ^ bb[2] ^ {bb[4][62:0], bb[4][63]};
 assign cc[4][3] = g[4][3] ^ bb[3] ^ {bb[0][62:0], bb[0][63]};
 assign cc[0][4] = g[0][4] ^ bb[4] ^ {bb[1][62:0], bb[1][63]};
 assign cc[1][4] = g[1][4] ^ bb[0] ^ {bb[2][62:0], bb[2][63]};
 assign cc[2][4] = g[2][4] ^ bb[1] ^ {bb[3][62:0], bb[3][63]};
 assign cc[3][4] = g[3][4] ^ bb[2] ^ {bb[4][62:0], bb[4][63]};
 assign cc[4][4] = g[4][4] ^ bb[3] ^ {bb[0][62:0], bb[0][63]};
 
 /* calc "dd == rho(cc)" */
 assign dd[0][0] = cc[0][0];
 assign dd[1][0] = {cc[1][0][62:0], cc[1][0][63]};
 assign dd[2][0] = {cc[2][0][1:0] , cc[2][0][63:2]};
 assign dd[3][0] = {cc[3][0][35:0], cc[3][0][63:36]};
 assign dd[4][0] = {cc[4][0][36:0], cc[4][0][63:37]};
 assign dd[0][1] = {cc[0][1][27:0], cc[0][1][63:28]};
 assign dd[1][1] = {cc[1][1][19:0], cc[1][1][63:20]};
 assign dd[2][1] = {cc[2][1][57:0], cc[2][1][63:58]};
 assign dd[3][1] = {cc[3][1][8:0] , cc[3][1][63:9]};
 assign dd[4][1] = {cc[4][1][43:0], cc[4][1][63:44]};
 assign dd[0][2] = {cc[0][2][60:0], cc[0][2][63:61]};
 assign dd[1][2] = {cc[1][2][53:0], cc[1][2][63:54]};
 assign dd[2][2] = {cc[2][2][20:0], cc[2][2][63:21]};
 assign dd[3][2] = {cc[3][2][38:0], cc[3][2][63:39]};
 assign dd[4][2] = {cc[4][2][24:0], cc[4][2][63:25]};
 assign dd[0][3] = {cc[0][3][22:0], cc[0][3][63:23]};
 assign dd[1][3] = {cc[1][3][18:0], cc[1][3][63:19]};
 assign dd[2][3] = {cc[2][3][48:0], cc[2][3][63:49]};
 assign dd[3][3] = {cc[3][3][42:0], cc[3][3][63:43]};
 assign dd[4][3] = {cc[4][3][55:0], cc[4][3][63:56]};
 assign dd[0][4] = {cc[0][4][45:0], cc[0][4][63:46]};
 assign dd[1][4] = {cc[1][4][61:0], cc[1][4][63:62]};
 assign dd[2][4] = {cc[2][4][2:0] , cc[2][4][63:3]};
 assign dd[3][4] = {cc[3][4][7:0] , cc[3][4][63:8]};
 assign dd[4][4] = {cc[4][4][49:0], cc[4][4][63:50]};

 /* calc "ee == pi(dd)" */
 assign ee[0][0] = dd[0][0];
 assign ee[0][2] = dd[1][0];
 assign ee[0][4] = dd[2][0];
 assign ee[0][1] = dd[3][0];
 assign ee[0][3] = dd[4][0];
 assign ee[1][3] = dd[0][1];
 assign ee[1][0] = dd[1][1];
 assign ee[1][2] = dd[2][1];
 assign ee[1][4] = dd[3][1];
 assign ee[1][1] = dd[4][1];
 assign ee[2][1] = dd[0][2];
 assign ee[2][3] = dd[1][2];
 assign ee[2][0] = dd[2][2];
 assign ee[2][2] = dd[3][2];
 assign ee[2][4] = dd[4][2];
 assign ee[3][4] = dd[0][3];
 assign ee[3][1] = dd[1][3];
 assign ee[3][3] = dd[2][3];
 assign ee[3][0] = dd[3][3];
 assign ee[3][2] = dd[4][3];
 assign ee[4][2] = dd[0][4];
 assign ee[4][4] = dd[1][4];
 assign ee[4][1] = dd[2][4];
 assign ee[4][3] = dd[3][4];
 assign ee[4][0] = dd[4][4];

 /* calc "ff = chi(ee)" */
 assign ff[0][0] = ee[0][0] ^ (~ee[1][0] & ee[2][0]);
 assign ff[1][0] = ee[1][0] ^ (~ee[2][0] & ee[3][0]);
 assign ff[2][0] = ee[2][0] ^ (~ee[3][0] & ee[4][0]);
 assign ff[3][0] = ee[3][0] ^ (~ee[4][0] & ee[0][0]);
 assign ff[4][0] = ee[4][0] ^ (~ee[0][0] & ee[1][0]);
 assign ff[0][1] = ee[0][1] ^ (~ee[1][1] & ee[2][1]);
 assign ff[1][1] = ee[1][1] ^ (~ee[2][1] & ee[3][1]);
 assign ff[2][1] = ee[2][1] ^ (~ee[3][1] & ee[4][1]);
 assign ff[3][1] = ee[3][1] ^ (~ee[4][1] & ee[0][1]);
 assign ff[4][1] = ee[4][1] ^ (~ee[0][1] & ee[1][1]);
 assign ff[0][2] = ee[0][2] ^ (~ee[1][2] & ee[2][2]);
 assign ff[1][2] = ee[1][2] ^ (~ee[2][2] & ee[3][2]);
 assign ff[2][2] = ee[2][2] ^ (~ee[3][2] & ee[4][2]);
 assign ff[3][2] = ee[3][2] ^ (~ee[4][2] & ee[0][2]);
 assign ff[4][2] = ee[4][2] ^ (~ee[0][2] & ee[1][2]);
 assign ff[0][3] = ee[0][3] ^ (~ee[1][3] & ee[2][3]);
 assign ff[1][3] = ee[1][3] ^ (~ee[2][3] & ee[3][3]);
 assign ff[2][3] = ee[2][3] ^ (~ee[3][3] & ee[4][3]);
 assign ff[3][3] = ee[3][3] ^ (~ee[4][3] & ee[0][3]);
 assign ff[4][3] = ee[4][3] ^ (~ee[0][3] & ee[1][3]);
 assign ff[0][4] = ee[0][4] ^ (~ee[1][4] & ee[2][4]);
 assign ff[1][4] = ee[1][4] ^ (~ee[2][4] & ee[3][4]);
 assign ff[2][4] = ee[2][4] ^ (~ee[3][4] & ee[4][4]);
 assign ff[3][4] = ee[3][4] ^ (~ee[4][4] & ee[0][4]);
 assign ff[4][4] = ee[4][4] ^ (~ee[0][4] & ee[1][4]);
 
 /* calc "gg = iota(ff)" */
 assign gg[0][0][63]    = ff[0][0][63] ^ rc2[6];
 assign gg[0][0][62:32] = ff[0][0][62:32];
 assign gg[0][0][31]    = ff[0][0][31] ^ rc2[5];
 assign gg[0][0][30:16] = ff[0][0][30:16];
 assign gg[0][0][15]    = ff[0][0][15] ^ rc2[4];
 assign gg[0][0][14:8]  = ff[0][0][14:8];
 assign gg[0][0][7]     = ff[0][0][7] ^ rc2[3];
 assign gg[0][0][6:4]   = ff[0][0][6:4];
 assign gg[0][0][3]     = ff[0][0][3] ^ rc2[2];
 assign gg[0][0][2]     = ff[0][0][2];
 assign gg[0][0][1]     = ff[0][0][1] ^ rc2[1];
 assign gg[0][0][0]     = ff[0][0][0] ^ rc2[0];

 assign gg[1][0] = ff[1][0];
 assign gg[2][0] = ff[2][0];
 assign gg[3][0] = ff[3][0];
 assign gg[4][0] = ff[4][0];
 assign gg[0][1] = ff[0][1];
 assign gg[1][1] = ff[1][1];
 assign gg[2][1] = ff[2][1];
 assign gg[3][1] = ff[3][1];
 assign gg[4][1] = ff[4][1];
 assign gg[0][2] = ff[0][2];
 assign gg[1][2] = ff[1][2];
 assign gg[2][2] = ff[2][2];
 assign gg[3][2] = ff[3][2];
 assign gg[4][2] = ff[4][2];
 assign gg[0][3] = ff[0][3];
 assign gg[1][3] = ff[1][3];
 assign gg[2][3] = ff[2][3];
 assign gg[3][3] = ff[3][3];
 assign gg[4][3] = ff[4][3];
 assign gg[0][4] = ff[0][4];
 assign gg[1][4] = ff[1][4];
 assign gg[2][4] = ff[2][4];
 assign gg[3][4] = ff[3][4];
 assign gg[4][4] = ff[4][4];

/*****************************************************************************
 *                                 Outputs                                   *
 *****************************************************************************/
 
 assign out[1599:1536] = gg[0][0];
 assign out[1535:1472] = gg[1][0];
 assign out[1471:1408] = gg[2][0];
 assign out[1407:1344] = gg[3][0];
 assign out[1343:1280] = gg[4][0];
 assign out[1279:1216] = gg[0][1];
 assign out[1215:1152] = gg[1][1];
 assign out[1151:1088] = gg[2][1];
 assign out[1087:1024] = gg[3][1];
 assign out[1023:960]  = gg[4][1];
 assign out[959:896]   = gg[0][2];
 assign out[895:832]   = gg[1][2];
 assign out[831:768]   = gg[2][2];
 assign out[767:704]   = gg[3][2];
 assign out[703:640]   = gg[4][2];
 assign out[639:576]   = gg[0][3];
 assign out[575:512]   = gg[1][3];
 assign out[511:448]   = gg[2][3];
 assign out[447:384]   = gg[3][3];
 assign out[383:320]   = gg[4][3];
 assign out[319:256]   = gg[0][4];
 assign out[255:192]   = gg[1][4];
 assign out[191:128]   = gg[2][4];
 assign out[127:64]    = gg[3][4];
 assign out[63:0]      = gg[4][4];

endmodule
