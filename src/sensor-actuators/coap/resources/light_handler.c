#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "light controller"
#define LOG_LEVEL LOG_LEVEL_APP
#define MAX_ARGS 10
#define ARG_LEN 100
extern uint8_t led;
char light_mode[10];


static void light_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void light_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_light_controller,
         "title=\"light controller\";rt=\"light\"",
         light_get_handler,
         NULL,
         light_put_handler,
         NULL);

bool light_on = false;


static void light_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    size_t len = 0;
    const char *command = NULL;

     if((len = coap_get_query_variable(request, "command", &command))){
         LOG_DBG("Command received is %.*s\n", (int)len, command);
             if (len >= sizeof(light_mode)) {
               LOG_ERR("Command too long");
               return;
             }
         if(strncmp(command, "ON", len) == 0){
            light_on = true;
            LOG_INFO("Light ON\n");
            strcpy(light_mode,command);

         }else if (strncmp(command,"OFF",len) == 0){
            light_on = false;
             leds_off(LEDS_ALL);
             LOG_INFO("Light OFF\n");
             strcpy(light_mode,command);

         }else{
            goto error;
         }
         	return;
         error:
         	coap_set_status_code(response, BAD_REQUEST_4_00);
     }
}

static void light_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {

    const char * status = light_mode;

      char payload[100];
      int len = snprintf(payload, sizeof(payload), "{\"lights\": \"%s\"}", status);

      if(len < 0 || len >= sizeof(payload)) {
        LOG_ERR("Payload too long");
        return;
      }

      LOG_INFO("GET /actuator/light");
      LOG_INFO(" >  %s\n", payload);
      coap_set_header_content_format(response, APPLICATION_JSON);
      coap_set_payload(response, buffer, len);

}