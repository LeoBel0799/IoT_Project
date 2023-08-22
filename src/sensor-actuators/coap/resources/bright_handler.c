#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "bright controller"
#define LOG_LEVEL LOG_LEVEL_APP
#define MAX_ARGS 10
#define ARG_LEN 100
extern bool light_on;

static void bright_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
RESOURCE(res_bright_controller,
         "title=\"bright controller\";rt=\"bright\"",
         NULL,
         NULL,
         bright_put_handler,
         NULL);

uint8_t led = LEDS_GREEN;

static void bright_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    size_t len = 0;
    const char *command = NULL;

    if((len = coap_get_query_variable(request, "command", &command))){
             LOG_DBG("Command received is %.*s\n", (int)len, command);
             if(strncmp(command, "ON", len) == 0){
                    led = LEDS_BLUE;
                    LOG_INFO("Bright ON");
             }else if (strncmp(command,"OFF",len) == 0){
                    led = LEDS_RED;
                    LOG_INFO("Bright OFF");
             }else{
                goto error;
             }


              LOG_INFO("Color = %s\n", command);
              if(light_on) {
                  leds_set(led);
              }
             return;
                error:
             	    coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}
