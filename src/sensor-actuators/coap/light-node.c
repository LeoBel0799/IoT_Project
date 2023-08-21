#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"
#include "coap-blocking-api.h"

#include "node-id.h"
#include "net/ipv6/simple-udp.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uip-debug.h"
#include "routing/routing.h"

#define SERVER_EP "coap://[fd00::1]:5683"
#define CONNECTION_TRY_INTERVAL 1
#define REGISTRATION_TRY_INTERVAL 1
#define SIMULATION_INTERVAL 1

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "light"
#define LOG_LEVEL LOG_LEVEL_APP
#define NODE_1_ID 1
#define NODE_2_ID 2
#define NODE_3_ID 3
#define NODE_4_ID 4
#define INTERVAL_BETWEEN_CONNECTION_TESTS 1

//dichiarazione array ID e indice
uint16_t node_ids[] = {NODE_1_ID,NODE_2_ID,NODE_3_ID,NODE_4_ID};
uint8_t next_id = 0;
//queste sono le coap resource che in java sono gestite tramite i due thread powering light e powering bright
extern coap_resource_t res_light_controller;
extern coap_resource_t res_bright_controller;

char *service_url = "/registration";
static bool registered = false;

static struct etimer connectivity_timer;
static struct etimer wait_registration;

/* Declare and auto-start this file's process */
PROCESS(light_server, "Car controller");
AUTOSTART_PROCESSES(&light_server);

/*---------------------------------------------------------------------------*/
static bool is_connected() {
	if(NETSTACK_ROUTING.node_is_reachable()) {
		LOG_INFO("The Border Router is reachable\n");
		return true;
  	}

	LOG_INFO("Waiting for connection with the Border Router\n");
	return false;
}

void client_handler(coap_message_t *response) {
	const  char *res_text = NULL;
	if(response == NULL) {
		LOG_INFO("Request timed out\n");
		etimer_set(&wait_registration, CLOCK_SECOND* REGISTRATION_TRY_INTERVAL);
		return;
	}

	// Estrai il valore di "res" dalla risposta
      int len = coap_get_post_variable(response, "res", &res_text);

      // Controlla il valore estratto
      if(len > 0 && strcmp(res_text, "ok") == 0) {
        registered = true;
        LOG_INFO("[+] Actuator registered!\n");
      } else {
        // Registrazione fallita, riprova
        etimer_set(&wait_registration, CLOCK_SECOND*REGISTRATION_TRY_INTERVAL);
      }
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

	// try to connect to the border router
	etimer_set(&connectivity_timer, CLOCK_SECOND * INTERVAL_BETWEEN_CONNECTION_TESTS);
	PROCESS_WAIT_UNTIL(etimer_expired(&connectivity_timer));
	while(!is_connected()) {
		etimer_reset(&connectivity_timer);
		PROCESS_WAIT_UNTIL(etimer_expired(&connectivity_timer));
	}

	while(!registered) {
		//qui mi genero l'id che simboleggia gli attuatori delle luci (va da 1 a 4)
        uint16_t node_id = node_ids[next_id];
         next_id = (next_id + 1) % 4;

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