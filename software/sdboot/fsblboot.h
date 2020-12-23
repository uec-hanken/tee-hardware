/* Copyright (c) 2018 SiFive, Inc */
/* SPDX-License-Identifier: Apache-2.0 */
/* SPDX-License-Identifier: GPL-2.0-or-later */
/* See the file LICENSE for further information */

#ifndef _LIBRARIES_UX00BOOT_H
#define _LIBRARIES_UX00BOOT_H

#ifndef __ASSEMBLER__

#include <gpt/gpt.h>

int fsblboot_load_gpt_partition(void* dst, const gpt_guid* partition_type_guid);
void fsblboot_fail(long code, int trap);

#endif /* !__ASSEMBLER__ */

#endif /* _LIBRARIES_UX00BOOT_H */


