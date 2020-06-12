#########################################################################################
# makefile variables shared across multiple makefiles
# This file is copied from chipyard/variables.mk
# Changes:
#   - teehardware as the default
#   - Remove the example (example depends deeply on chipyard)
#   - Make ROCKETCHIP_DIR, TESTCHIP_DIR, CHIPYARD_FIRRTL_DIR configurable
#########################################################################################

#########################################################################################
# variables to invoke the generator
# descriptions:
#   SBT_PROJECT = the SBT project that you should find the classes/packages in
#   MODEL = the top level module of the project in Chisel (normally the harness)
#   VLOG_MODEL = the top level module of the project in Firrtl/Verilog (normally the harness)
#   MODEL_PACKAGE = the scala package to find the MODEL in
#   CONFIG = the configuration class to give the parameters for the project
#   CONFIG_PACKAGE = the scala package to find the CONFIG class
#   GENERATOR_PACKAGE = the scala package to find the Generator class in
#   TB = wrapper over the TestHarness needed to simulate in a verilog simulator
#   TOP = top level module of the project (normally the module instantiated by the harness)
#
# project specific:
# 	SUB_PROJECT = use the specific subproject default variables
#########################################################################################

#########################################################################################
# subproject overrides
# description:
#   - make it so that you only change 1 param to change most or all of them!
#   - mainly intended for quick developer setup for common flags
#########################################################################################
SUB_PROJECT ?= teehardware

TEEHW_DIR ?= $(base_dir)/hardware/teehw
SHA3_DIR ?= $(TEEHW_DIR)/optvsrc/SHA3
ED25519_DIR ?= $(TEEHW_DIR)/optvsrc/Ed25519/Ed25519_mul
ED25519_SIGN_DIR ?= $(TEEHW_DIR)/optvsrc/Ed25519/Ed25519_sign
AES_DIR ?= $(TEEHW_DIR)/optvsrc/AES
USB11HS_DIR ?= $(TEEHW_DIR)/optvsrc/USB

ADD_VSRC ?= $(SHA3_DIR)/f_permutation.v \
	$(SHA3_DIR)/round2in1.v \
	$(SHA3_DIR)/padder1.v \
	$(SHA3_DIR)/keccak.v \
	$(SHA3_DIR)/padder.v \
	$(SHA3_DIR)/rconst2in1.v \
	$(SHA3_DIR)/SHA3_TOP.v \
	$(SHA3_DIR)/SHA3_TOP_wrapper.v \
	$(ED25519_DIR)/mac16_generic.v \
	$(ED25519_DIR)/ed25519_microcode_rom.v \
	$(ED25519_DIR)/ed25519_operand_bank.v \
	$(ED25519_DIR)/adder47_generic.v \
	$(ED25519_DIR)/ed25519_uop_worker.v \
	$(ED25519_DIR)/subtractor32_generic.v \
	$(ED25519_DIR)/ed25519_mul.v \
	$(ED25519_DIR)/bram_1rw_1ro_readfirst.v \
	$(ED25519_DIR)/curve25519_modular_multiplier.v \
	$(ED25519_DIR)/ed25519_banks_array.v \
	$(ED25519_DIR)/modular_adder.v \
	$(ED25519_DIR)/modular_subtractor.v \
	$(ED25519_DIR)/adder32_generic.v \
	$(ED25519_DIR)/multiword_mover.v \
	$(ED25519_DIR)/ed25519_mul_TOP.v \
	$(ED25519_DIR)/ed25519_mul_TOP_wrapper.v \
	$(ED25519_SIGN_DIR)/ed25519_sign_S_core_TOP.v \
	$(ED25519_SIGN_DIR)/ed25519_sign_S_core_TOP_wrapper.v \
	$(ED25519_SIGN_DIR)/ed25519_sign_S_core.v \
	$(ED25519_SIGN_DIR)/mult_512_byAdder.v \
	$(ED25519_SIGN_DIR)/barrett.v \
	$(AES_DIR)/aes_core.v \
	$(AES_DIR)/aes_decipher_block.v \
	$(AES_DIR)/aes_encipher_block.v \
	$(AES_DIR)/aes_sub_inv_sbox.v \
	$(AES_DIR)/aes_sub_sbox.v \
	$(AES_DIR)/aes_inv_sbox.v \
	$(AES_DIR)/aes_key_mem.v \
	$(AES_DIR)/aes_sbox.v \
	$(AES_DIR)/mixcolumns.v \
	$(AES_DIR)/inv_mixcolumns.v \
	$(AES_DIR)/aes_core_TOP.v \
	$(AES_DIR)/aes_core_TOP_wrapper.v \
	$(USB11HS_DIR)/hostController/USBHostControlBI.v \
	$(USB11HS_DIR)/hostController/usbHostControl.v \
	$(USB11HS_DIR)/hostController/speedCtrlMux.v \
	$(USB11HS_DIR)/hostController/softransmit.v \
	$(USB11HS_DIR)/hostController/sofcontroller.v \
	$(USB11HS_DIR)/hostController/sendpacketcheckpreamble.v \
	$(USB11HS_DIR)/hostController/sendpacketarbiter.v \
	$(USB11HS_DIR)/hostController/sendpacket.v \
	$(USB11HS_DIR)/hostController/rxStatusMonitor.v \
	$(USB11HS_DIR)/hostController/hostcontroller.v \
	$(USB11HS_DIR)/hostController/hctxportarbiter.v \
	$(USB11HS_DIR)/hostController/getpacket.v \
	$(USB11HS_DIR)/hostController/directcontrol.v \
	$(USB11HS_DIR)/slaveController/USBSlaveControlBI.v \
	$(USB11HS_DIR)/slaveController/usbSlaveControl.v \
	$(USB11HS_DIR)/slaveController/slaveSendpacket.v \
	$(USB11HS_DIR)/slaveController/slaveRxStatusMonitor.v \
	$(USB11HS_DIR)/slaveController/slaveGetpacket.v \
	$(USB11HS_DIR)/slaveController/slaveDirectcontrol.v \
	$(USB11HS_DIR)/slaveController/slavecontroller.v \
	$(USB11HS_DIR)/slaveController/sctxportarbiter.v \
	$(USB11HS_DIR)/slaveController/fifoMux.v \
	$(USB11HS_DIR)/slaveController/endpMux.v \
	$(USB11HS_DIR)/serialInterfaceEngine/writeUSBWireData.v \
	$(USB11HS_DIR)/serialInterfaceEngine/usbTxWireArbiter.v \
	$(USB11HS_DIR)/serialInterfaceEngine/usbSerialInterfaceEngine.v \
	$(USB11HS_DIR)/serialInterfaceEngine/updateCRC16.v \
	$(USB11HS_DIR)/serialInterfaceEngine/updateCRC5.v \
	$(USB11HS_DIR)/serialInterfaceEngine/SIETransmitter.v \
	$(USB11HS_DIR)/serialInterfaceEngine/siereceiver.v \
	$(USB11HS_DIR)/serialInterfaceEngine/readUSBWireData.v \
	$(USB11HS_DIR)/serialInterfaceEngine/processTxByte.v \
	$(USB11HS_DIR)/serialInterfaceEngine/processRxByte.v \
	$(USB11HS_DIR)/serialInterfaceEngine/processRxBit.v \
	$(USB11HS_DIR)/serialInterfaceEngine/lineControlUpdate.v \
	$(USB11HS_DIR)/hostSlaveMux/hostSlaveMuxBI.v \
	$(USB11HS_DIR)/hostSlaveMux/hostSlaveMux.v \
	$(USB11HS_DIR)/busInterface/wishBoneBI.v \
	$(USB11HS_DIR)/buffers/TxFifoBI.v \
	$(USB11HS_DIR)/buffers/TxFifo.v \
	$(USB11HS_DIR)/buffers/RxFifoBI.v \
	$(USB11HS_DIR)/buffers/RxFifo.v \
	$(USB11HS_DIR)/buffers/fifoRTL.v \
	$(USB11HS_DIR)/buffers/dpMem_dc.v \
	$(USB11HS_DIR)/wrapper/usbHostSlave.v

TB ?= TestDriver

ifeq ($(SUB_PROJECT),tracegen)
	SBT_PROJECT       ?= tracegen
	MODEL             ?= TestHarness
	MODEL_PACKAGE     ?= $(SBT_PROJECT)
	CONFIG            ?= TraceGenConfig
	CONFIG_PACKAGE    ?= $(SBT_PROJECT)
	GENERATOR_PACKAGE ?= $(SBT_PROJECT)
	TOP               ?= TraceGenSystem
else ifeq ($(SUB_PROJECT),rocketchip)
	SBT_PROJECT       ?= rocketchip
	MODEL             ?= TestHarness
	MODEL_PACKAGE     ?= freechips.rocketchip.system
	CONFIG            ?= DefaultConfig
	CONFIG_PACKAGE    ?= freechips.rocketchip.system
	GENERATOR_PACKAGE ?= freechips.rocketchip.system
	TOP               ?= ExampleRocketSystem
else ifeq ($(SUB_PROJECT),hwacha)
	SBT_PROJECT       ?= hwacha
	MODEL             ?= TestHarness
	MODEL_PACKAGE     ?= freechips.rocketchip.system
	CONFIG            ?= HwachaConfig
	CONFIG_PACKAGE    ?= hwacha
	GENERATOR_PACKAGE ?= hwacha
	TOP               ?= ExampleRocketSystem
else ifeq ($(SUB_PROJECT),teehardware)
	SBT_PROJECT       ?= teehardware
	MODEL             ?= TestHarness
	MODEL_PACKAGE     ?= uec.teehardware.exampletop
	CONFIG            ?= TEEHWDefaultConfig
	CONFIG_PACKAGE    ?= uec.teehardware.exampletop
	GENERATOR_PACKAGE ?= uec.teehardware.exampletop
	TOP               ?= ExampleRocketSystem
else
	SBT_PROJECT       ?= teehardware
	MODEL             ?= $(SUB_PROJECT)
	MODEL_PACKAGE     ?= uec.teehardware
	CONFIG            ?= $(ISACONF)_$(BOARD)Config_$(BOOTSRC)_$(HYBRID)
	CONFIG_PACKAGE    ?= uec.teehardware
	GENERATOR_PACKAGE ?= uec.teehardware.exampletop
	TOP               ?= TEEHWSoC
endif
VLOG_MODEL ?= $(MODEL)
# Stand-in firechip variables:
# TODO: need a seperate generator and test harnesses for each target
#ifeq ($(SUB_PROJECT),firechip)
#	SBT_PROJECT       ?= $(SUB_PROJECT)
#	MODEL             ?= TestHarness
#	VLOG_MODEL        ?= TestHarness
#	MODEL_PACKAGE     ?= freechips.rocketchip.system
#	CONFIG            ?= FireSimRocketChipConfig
#	CONFIG_PACKAGE    ?= firesim.firesim
#	GENERATOR_PACKAGE ?= firesim.firesim
#	TOP               ?= FireSimNoNIC
#endif

#########################################################################################
# path to rocket-chip and testchipip
#########################################################################################
ROCKETCHIP_DIR      = $(base_dir)/hardware/chipyard/generators/rocket-chip
TESTCHIP_DIR        = $(base_dir)/hardware/chipyard/generators/testchipip
CHIPYARD_FIRRTL_DIR = $(base_dir)/hardware/chipyard/tools/firrtl

#########################################################################################
# names of various files needed to compile and run things
#########################################################################################
long_name = $(MODEL_PACKAGE).$(MODEL).$(CONFIG)
ifeq ($(GENERATOR_PACKAGE),hwacha)
	long_name=$(MODEL_PACKAGE).$(CONFIG)
endif

FIRRTL_FILE ?= $(build_dir)/$(long_name).fir
ANNO_FILE   ?= $(build_dir)/$(long_name).anno.json

TOP_FILE       ?= $(build_dir)/$(long_name).top.v
TOP_FIR        ?= $(build_dir)/$(long_name).top.fir
TOP_ANNO       ?= $(build_dir)/$(long_name).top.anno.json
TOP_SMEMS_FILE ?= $(build_dir)/$(long_name).top.mems.v
TOP_SMEMS_CONF ?= $(build_dir)/$(long_name).top.mems.conf
TOP_SMEMS_FIR  ?= $(build_dir)/$(long_name).top.mems.fir

HARNESS_FILE       ?= $(build_dir)/$(long_name).harness.v
HARNESS_FIR        ?= $(build_dir)/$(long_name).harness.fir
HARNESS_ANNO       ?= $(build_dir)/$(long_name).harness.anno.json
HARNESS_SMEMS_FILE ?= $(build_dir)/$(long_name).harness.mems.v
HARNESS_SMEMS_CONF ?= $(build_dir)/$(long_name).harness.mems.conf
HARNESS_SMEMS_FIR  ?= $(build_dir)/$(long_name).harness.mems.fir

# files that contain lists of files needed for VCS or Verilator simulation
sim_files              ?= $(build_dir)/sim_files.f
sim_top_blackboxes     ?= $(build_dir)/firrtl_black_box_resource_files.top.f
sim_harness_blackboxes ?= $(build_dir)/firrtl_black_box_resource_files.harness.f
# single file that contains all files needed for VCS or Verilator simulation (unique and without .h's)
sim_common_files       ?= $(build_dir)/sim_files.common.f

#########################################################################################
# java arguments used in sbt
#########################################################################################
JAVA_HEAP_SIZE ?= 8G
JAVA_ARGS ?= -Xmx$(JAVA_HEAP_SIZE) -Xss8M -XX:MaxPermSize=256M

#########################################################################################
# default sbt launch command
#########################################################################################
SCALA_VERSION=2.12.10
SCALA_VERSION_MAJOR=$(basename $(SCALA_VERSION))

SBT ?= java $(JAVA_ARGS) -jar $(ROCKETCHIP_DIR)/sbt-launch.jar

#########################################################################################
# output directory for tests
#########################################################################################
output_dir=$(sim_dir)/output/$(long_name)

#########################################################################################
# helper variables to run binaries
#########################################################################################
PERMISSIVE_ON=+permissive
PERMISSIVE_OFF=+permissive-off
BINARY ?=
override SIM_FLAGS += +dramsim +max-cycles=$(timeout_cycles)
VERBOSE_FLAGS ?= +verbose
sim_out_name = $(subst $() $(),_,$(notdir $(basename $(BINARY))).$(long_name))

#########################################################################################
# build output directory for compilation
#########################################################################################
gen_dir=$(build_dir)

#########################################################################################
# vsrcs needed to run projects
#########################################################################################
rocketchip_vsrc_dir = $(ROCKETCHIP_DIR)/src/main/resources/vsrc

#########################################################################################
# sources needed to run simulators
#########################################################################################
sim_vsrcs = \
	$(TOP_FILE) \
	$(HARNESS_FILE) \
	$(TOP_SMEMS_FILE) \
	$(HARNESS_SMEMS_FILE)

#########################################################################################
# assembly/benchmark variables
#########################################################################################
timeout_cycles = 10000000
bmark_timeout_cycles = 100000000
