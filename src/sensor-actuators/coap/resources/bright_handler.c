#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "bright controller"
#define LOG_LEVEL LOG_LEVEL_APP
#define MAX_ARGS 10
#define ARG_LEN 100
bool bright_on = false;


static void bright_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void bright_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);


RESOURCE(res_bright_controller,
         "title=\"bright controller\";rt=\"bright\"",
         bright_get_handler,
         NULL,
         bright_put_handler,
         NULL);

uint8_t led;

static void bright_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    size_t len = 0;
    const char *command = NULL;

    if((len = coap_get_query_variable(request, "command", &command))){
             LOG_DBG("Command received is %.*s\n", (int)len, command);
             if(strncmp(command, "ON", len) == 0){
                    leds_on(LEDS_YELLOW);
                    bright_on = true;
                    LOG_INFO("Bright ON");
             }else if (strncmp(command,"OFF",len) == 0){
                    bright_on = false;
                    leds_off(LEDS_ALL);
                    LOG_INFO("Bright OFF");
             }else{
                goto error;
             }


              LOG_INFO("Color = %s\n", command);
              if(bright_on) {
                  leds_set(led);
              }
             return;
                error:
             	    coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}


static void bright_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
  int length = 0;
  const char* message;
    usleep(100000);
  if (bright_on == true){
      length = 2;
      message = "ON";
  } else if (bright_on == false){
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
