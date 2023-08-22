#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"
#include "coap-blocking-api.h"
#include "coap-observe.h"
#include "coap-separate.h"
#include "node-id.h"
#include "net/ipv6/simple-udp.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uip-debug.h"
#include "routing/routing.h"
#include "dev/button-hal.h"

#define SERVER_EP "coap://[fd00::1]:5683"
#define CONNECTION_TRY_INTERVAL 1
#define SIMULATION_INTERVAL 1

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "light"
#define LOG_LEVEL LOG_LEVEL_APP
#define NODE_1_ID 1
#define NODE_2_ID 2

#define INTERVAL_BETWEEN_CONNECTION_TESTS 1
static struct etimer pub_timer;
//dichiarazione array ID e indice
uint16_t node_ids[] = {NODE_1_ID,NODE_2_ID};
static uint8_t next_id = 0;
//queste sono le coap resource che in java sono gestite tramite i due thread powering light e powering bright
extern coap_resource_t res_light_controller;
extern coap_resource_t res_bright_controller;
extern coap_resource_t res_wearLevel_observer;

char *service_url = "/registration";
static bool registered = false;
size_t payload_length = 0;  // Lunghezza del payload ricevuto
bool fulminated = false;    // Flag per indicare se è fulminato
int wear_level = 0;     // Livello di usura
static struct etimer connectivity_timer;
static struct etimer wait_registration;

//dati che sono due processi servono due dichiarazioni e partono con autostart
PROCESS(light_server, "Car controller");
PROCESS(wear_controller, "COAP Wear obs");
AUTOSTART_PROCESSES(&light_server,&wear_controller);


// Dichiarazione delle variabili globali

static void res_event_handler(void);
static void res_get_handler_coap_values(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);


EVENT_RESOURCE(res_wearLevel_observer,
         "title=\"wearLevel observer\";obs",
         res_get_handler_coap_values,
         res_post_handler,
         res_post_handler,
         NULL,
         res_event_handler);

// Funzione di gestione della richiesta POST CoAP
static void res_post_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    const uint8_t *payload = NULL;
    size_t payload_length = coap_get_payload(request, &payload);

    char payload_str[payload_length + 1];
    memcpy(payload_str, payload, payload_length);
    payload_str[payload_length] = '\0';

    // Tokenizza la stringa
    char* token = strtok(payload_str, ",");
    if (token != NULL) {
        wear_level = atof(token);

        // Leggi secondo valore e converte in bool
        token = strtok(NULL, ",");
        if (token != NULL) {
            fulminated = (*token == '1');
        }
    }


}

static void res_get_handler_coap_values(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset) {
    char response_payload[64];
    int length = snprintf(response_payload, sizeof(response_payload), "wearLevel=%.2d,fulminated=%s",
                          wear_level, fulminated ? "true" : "false");

    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_payload(response, response_payload, length);
}


static void res_event_handler(void){
	coap_notify_observers(&res_wearLevel_observer);
}

/*---------------------------------------------------------------------------*/
static bool is_connected() {
	if(NETSTACK_ROUTING.node_is_reachable()) {
		LOG_INFO("The Border Router is reachable\n");
		return true;
  	}

	LOG_INFO("Waiting for connection with the Border Router\n");
	return false;
}

	void parse_json(char json[], int n_arguments, char arguments[][100]){

        int value_parsed = 0;
        int len = 0;
        bool value_detected = false;
        int i;
        for(i = 0; json[i] != '\0' && value_parsed < n_arguments; i++){

            if(json[i] == ':'){
                i++; //there is the space after ':'
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


void client_handler(coap_message_t *response) {
	const  uint8_t *res_text;
	if(response == NULL) {
		LOG_INFO("Request timed out\n");
		etimer_set(&wait_registration, CLOCK_SECOND* 10);
		return;
	}

	// Estrai il valore di "res" dalla risposta
	  coap_get_payload(response, &res_text);
	  char message[300];
      sprintf(message,"%s",(char*)res_text);

      int number = 1;
      char args[number][100];
      parse_json(message, number, args );
      // Controlla il valore estratto
      if(strcmp(args[0], "ok") == 0) {
        registered = true;
        LOG_INFO("[+] Actuator registered!\n");
      } else {
        // Registrazione fallita, riprova
        etimer_set(&wait_registration, CLOCK_SECOND*10);
      }
}



PROCESS_THREAD(wear_controller, ev, data) {
    PROCESS_BEGIN();

    // Inizializza il bottone
    button_hal_init();

    while (1) {  // Loop infinito
        // Accendo il LED ROSSO quando ricevo i dati di wearLevel e fulminated
            leds_on(LEDS_RED);  // Accendi il LED rosso
            etimer_set(&pub_timer, CLOCK_SECOND);  // Attendi 1 secondo
            PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&pub_timer));
            leds_off(LEDS_RED);  // Spegni il LED rosso

        // Se il bottone viene premuto vuol dire che la luce è stata sostituita quindi wear e fulminated si resettando
        //questi nuovi dati resettati devono essere mandati nel java e nel fb
        if (ev == button_hal_release_event) {
            fulminated = false;
            wear_level = 0;
            LOG_INFO("[OK] - Item replaced\n");
            leds_on(LEDS_BLUE);
            //questo è per notificare agli osservatori
            res_event_handler();
        }
    }

    PROCESS_END();
}




PROCESS_THREAD(light_server, ev, data){
	PROCESS_BEGIN();
    next_id=0;
	static coap_endpoint_t server_ep;
	static coap_message_t request; // This way the packet can be treated as pointer as usual

	PROCESS_PAUSE();

	LOG_INFO("Starting Light CoAP-Server\n");
	//qui avviene il collegamenti ai due thread, infatti le due stringhe actuator/lights
	//e actuator brights sono esattamente le stesse usate dai metodi di put e get usate nella
	//classe java LightBrightStatus
	coap_activate_resource(&res_bright_controller, "actuator/brights");
	coap_activate_resource(&res_light_controller, "actuator/lights");
     // Abilita obs su wear level
    coap_activate_resource(&res_wearLevel_observer, "actuator/data");
	etimer_set(&connectivity_timer, CLOCK_SECOND * INTERVAL_BETWEEN_CONNECTION_TESTS);
	PROCESS_WAIT_UNTIL(etimer_expired(&connectivity_timer));
	while(!is_connected()) {
		etimer_reset(&connectivity_timer);
		PROCESS_WAIT_UNTIL(etimer_expired(&connectivity_timer));
	}

	while(!registered) {
		//qui mi genero l'id che simboleggia gli attuatori delle luci (va da 1 a 4)

        next_id = next_id + 1;
        uint16_t node_id = next_id ;

		LOG_INFO("Sending registration message\n");
		//qui prendo gli id che mi sono generato randomicamente e li mando al java,
		//questo id che mando al java lo prende tramite una get json
		//nella classe registrationLightResource.java e lo stesso id viene mandato
		//ai due thread
		char payload[50];
        sprintf(payload, "{\"id\": %d}", node_id);
		coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
		// Prepare the message
		coap_init_message(&request, COAP_TYPE_CON, COAP_POST, 0);
		coap_set_header_uri_path(&request, service_url);
		coap_set_payload(&request, payload, strlen(payload));

		COAP_BLOCKING_REQUEST(&server_ep, &request, client_handler);

		PROCESS_WAIT_UNTIL(etimer_expired(&wait_registration));

	}
	PROCESS_END();



}