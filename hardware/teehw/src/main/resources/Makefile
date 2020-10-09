#########################################################################################
# pre-process opentitan into a single blackbox file
#########################################################################################
base_dir=$(abspath ../../../../..)
chipyard_dir=$(base_dir)/hardware/chipyard
vsrc_dir=$(base_dir)/hardware/teehw/vsrc/
optvsrc_dir=$(base_dir)/hardware/teehw/optvsrc/

PREPROC_SCRIPT = $(chipyard_dir)/scripts/insert-includes.py

include ibex.mk
include sha3.mk
include ed25519_base.mk
include ed25519_sign.mk
include aes.mk
include usb11.mk
include aesot.mk
include hmac.mk
include alert.mk
include nmi_gen.mk
include prim.mk
include tlul.mk
include pkgs.mk
include otp_ctrl.mk

