#########################################################################################
# pre-process usb11hs into a single blackbox file
#########################################################################################
USB11HS_DIR ?= $(optvsrc_dir)/USB

# name of output pre-processed verilog file
USB11HS_PREPROC_VERILOG = usb11hs.preprocessed.v

.PHONY: usb11hs $(USB11HS_PREPROC_VERILOG)
usb11hs:  $(USB11HS_PREPROC_VERILOG)

#########################################################################################
# includes and vsrcs
#########################################################################################
USB11HS_OPENTITAN_PKGS = 

USB11HS_OPENTITAN_VSRCS = \
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

USB11HS_OPENTITAN_WRAPPER = 

USB11HS_ALL_VSRCS = $(USB11HS_OPENTITAN_PKGS) $(USB11HS_OPENTITAN_VSRCS) $(USB11HS_OPENTITAN_WRAPPER)

#########################################################################################
# pre-process using verilator
#########################################################################################

lookup_dirs = $(shell find -L $(opentitan_dir) -name target -prune -o -type d -print 2> /dev/null | grep '.*/\($(1)\)$$')
USB11HS_INC_DIR_NAMES ?= include
USB11HS_INC_DIRS ?= $(foreach dir_name,$(USB11HS_INC_DIR_NAMES),$(call lookup_dirs,$(dir_name)))

# these flags are specific to Chipyard
USB11HS_EXTRA_PREPROC_DEFINES ?=
USB11HS_PREPROC_DEFINES ?= \
	WT_DCACHE \
	DISABLE_TRACER \
	SRAM_NO_INIT \
	VERILATOR \
	$(USB11HS_EXTRA_PREPROC_DEFINES)

$(USB11HS_PREPROC_VERILOG): $(USB11HS_ALL_VSRCS)
	mkdir -p $(dir $(USB11HS_PREPROC_VERILOG))
	$(foreach def,$(USB11HS_PREPROC_DEFINES),echo "\`define $(def)" >> def.v; )
	$(foreach def,$(USB11HS_PREPROC_DEFINES),echo "\`undef $(def)" >> undef.v; )
	cat def.v $(USB11HS_ALL_VSRCS) undef.v > combined.v
	sed -i '/l15.tmp.h/d' combined.v
	sed -i '/define.tmp.h/d' combined.v
	$(PREPROC_SCRIPT) combined.v $@ $(USB11HS_INC_DIRS)
	rm -rf combined.v def.v undef.v

