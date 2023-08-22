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
         if(strncmp(command, "ON", len) == 0){
            leds_on(LEDS_BLUE);
            light_on = true;
            LOG_INFO("Light ON\n");

         }else if (strncmp(command,"OFF",len) == 0){
                light_on = false;
             leds_off(LEDS_ALL);
             LOG_INFO("Light OFF\n");

         }else{
            goto error;
         }
         	return;
         error:
         	coap_set_status_code(response, BAD_REQUEST_4_00);
     }
}

static void light_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
  int length = 0;
  const char* message;

  if (light_on == true){
      length = 2;
      message = "ON";
  } else if (light_on == false){
       length= 3;
       message = "OFF";
  } else {
      coap_set_status_code(response, BAD_REQUEST_4_00);
      return;
  }
  memcpy(buffer, message, length);
  coap_set_header_content_format(response, TEXT_PLAIN);
  coap_set_status_code(response, CONTENT_2_05);
  coap_set_header_etag(response, (uint8_t *)&length, 1);
  coap_set_payload(response, buffer, length);
}