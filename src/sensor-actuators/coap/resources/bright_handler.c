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
bool bright_on = 0;


static void bright_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void bright_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);


RESOURCE(res_bright_controller,
         "title=\"bright controller\";rt=\"bright\"",
         bright_get_handler,
         NULL,
         bright_put_handler,
         NULL);


static void bright_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    size_t len = 0;
    const char *command = NULL;
    uint8_t led = 10;
    led = led - 10;
    int success =1;
    if((len = coap_get_query_variable(request, "command", &command))){
        LOG_DBG("Command %.*s - WAIT!\n", (int)len, command);
        // Spengo la luce
        if(strncmp(command, "OFF", len) == 0){
            bright_on = 0;
            leds_off(LEDS_ALL);
            led = 0;
            LOG_INFO("[OK] - Bright OFF");
        }else if(strncmp(command, "ON", len) == 0){
            led = LEDS_YELLOW;
            bright_on = 1;
            LOG_INFO("[OK] - Bright ON");
        }else{
      coap_set_status_code(response, BAD_REQUEST_4_00);
    }
  }else{
    coap_set_status_code(response, BAD_REQUEST_4_00);
    led = 0;
  }

  if(success) {
    coap_set_status_code(response, CONTENT_2_05);
  }

}



static void bright_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
  int length = 0;
  const char* message;
  if (bright_on == 1){
      length = 2;
      message = "ON";
  } else if (bright_on == 0){
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
