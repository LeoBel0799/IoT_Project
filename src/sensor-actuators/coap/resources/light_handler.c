#include <stdlib.h>
#include <string.h>
#include <unistd.h>


#include "contiki.h"
#include "coap-engine.h"
#include "os/dev/leds.h"
#include "sys/ctimer.h"
/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "light controller"
#define LOG_LEVEL LOG_LEVEL_APP
#define MAX_ARGS 10
#define ARG_LEN 100
#define TIME_MS (CLOCK_SECOND * 0.1) // 100ms

static void light_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void light_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_light_controller,
         "title=\"light controller\";rt=\"light\"",
         light_get_handler,
         NULL,
         light_put_handler,
         NULL);

static int light_on = 0; //0 spento- 1 acceso


static void light_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    size_t len = 0;
    const char *command = NULL;
    uint8_t led = 0;
    int success =1;
    if((len = coap_get_query_variable(request, "command", &command))){
        LOG_DBG("Command %.*s\n", (int)len, command);
        // Spengo la luce
        if(strncmp(command, "OFF", len) == 0){
            light_on = 0;
            leds_off(LEDS_ALL);
            led = 0;
            LOG_INFO("[OK] - Light OFF");
        }else if(strncmp(command, "ON", len) == 0){
            led = LEDS_RED;
            light_on = 1;
            LOG_INFO("[OK] - Light ON");
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


static void light_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
  int length = 0;
  const char* message;

  if (light_on == 1){
      length = 2;
      message = "ON";
  } else if (light_on == 0){
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
