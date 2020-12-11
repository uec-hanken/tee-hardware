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

ADD_VSRC ?= 

TB ?= TestDriver

ifeq ($(SUB_PROJECT),teehardware)
	SBT_PROJECT       ?= teehardware
	MODEL             ?= TEEHWHarness
	VLOG_MODEL        ?= TEEHWHarness
	MODEL_PACKAGE     ?= uec.teehardware
	CONFIG            ?= WithSimulation_$(BOARD)Config_$(MBUS)_$(DDRCLK)_$(PCIE)_$(BOOTSRC)_$(HYBRID)_$(ISACONF)
	CONFIG_PACKAGE    ?= uec.teehardware
	GENERATOR_PACKAGE ?= uec.teehardware.exampletop
	TB                ?= TestDriver
	TOP               ?= TEEHWSystem
# Our TEE hardware
else
	SBT_PROJECT       ?= teehardware
	MODEL             ?= $(SUB_PROJECT)
	VLOG_MODEL        ?= $(SUB_PROJECT)
	MODEL_PACKAGE     ?= uec.teehardware
	CONFIG            ?= $(BOARD)Config_$(MBUS)_$(DDRCLK)_$(PCIE)_$(BOOTSRC)_$(HYBRID)_$(ISACONF)
	CONFIG_PACKAGE    ?= uec.teehardware
	GENERATOR_PACKAGE ?= uec.teehardware.exampletop
	TB                ?= TestDriver
	TOP               ?= TEEHWSoC
endif
VLOG_MODEL ?= $(MODEL)

#########################################################################################
# path to rocket-chip and testchipip
#########################################################################################
ROCKETCHIP_DIR      = $(teehw_dir)/hardware/chipyard/generators/rocket-chip
TESTCHIP_DIR        = $(teehw_dir)/hardware/chipyard/generators/testchipip
CHIPYARD_FIRRTL_DIR = $(teehw_dir)/hardware/chipyard/tools/firrtl

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
