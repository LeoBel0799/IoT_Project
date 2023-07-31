CONTIKI_PROJECT = coap_node
all: $(CONTIKI_PROJECT)

# Include CoAP resources /// Attualmente non serve
#MODULES_REL += ./resources

CONTIKI = ../../..

# Include the CoAP implementation
include $(CONTIKI)/Makefile.dir-variables
MODULES += $(CONTIKI_NG_APP_LAYER_DIR)/coap

-include $(CONTIKI)/Makefile.identify-target

MODULES_REL += arch/platform/$(TARGET)

include $(CONTIKI)/Makefile.include