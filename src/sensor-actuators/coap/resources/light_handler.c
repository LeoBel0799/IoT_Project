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

static void light_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

void parse_json_light(char json[], int n_arguments, char arguments[][100]){

        int value_parsed = 0;
        int len = 0;
        bool value_detected = false;
        int i;
        for(i = 0; json[i] != '\0' && value_parsed < n_arguments; i++){

            if(json[i] == ':'){
                i++;
                value_detected = true;
                len = 0;
            }
            else if(value_detected && (json[i] == ',' || json[i] == ' ' || json[i] == '}')){
                value_detected = false;
                arguments[value_parsed][len] = '\0';
                value_parsed++;
            }
            else if(value_detected && json[i] == '{'){
                value_detected = false;
            }
            else if(value_detected){
                if(json[i] =='\'' || json[i] == '\"')
                    continue;
                arguments[value_parsed][len] = json[i];
                len++;
            }

        }
    }


RESOURCE(res_light_controller,
         "title=\"light controller\";rt=\"Controller\"",
         NULL,
         NULL,
         light_put_handler,
         NULL);

bool light_on = false;

static void light_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
	//const char *text = NULL;
    char message[300];
    int number = 2;
    char args[number][100];
	parse_json_light(message, number, args);

    if(strcmp(args[0], "light") == 0) {
            if(strcmp(args[1], "ON") == 0) {
              light_on = true;
              leds_set(led);
              LOG_INFO("Light ON\n");
            } else if (strcmp(args[1], "OFF") == 0) {
              light_on = false;
              leds_off(LEDS_ALL);
              LOG_INFO("Light OFF\n");
            } else {
              goto error;
            }
      } else {
        goto error;
      }

	return;
error:
	coap_set_status_code(response, BAD_REQUEST_4_00);
}