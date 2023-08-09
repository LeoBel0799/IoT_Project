#include "contiki.h"

#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uiplib.h"


#include "sys/ctimer.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"

#include "sys/etimer.h"
#include "dev/leds.h"
#include "dev/button-hal.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Light-Actuator"
#define LOG_LEVEL LOG_LEVEL_DBG

//Resources to expose
extern coap_resource_t  res_light;

static struct etimer periodic_state_timer;

#define STATE_TIMER (CLOCK_SECOND * 10)

#define SERVER_IP "coap://[fd00::1]"
#define ID_PAIR 1

static bool registered = false;
static bool check = false;

static char msg_size[40];

static bool have_conn(void)
{
    //Ritorna true solo se il nodo corrente ha un Public IP
    if(uip_ds6_get_global(ADDR_PREFERRED) == NULL || uip_ds6_defrt_choose() == NULL)
    {
        return false;
    }
    return true;
}

void handler(coap_message_t *response){
    const uint8_t *chunk;

    if(response != NULL){
        int len = coap_get_payload(response, &chunk);
        LOG_INFO("APPLICATION RESPONSE: %.*s\n", len, (char*)chunk);
        if(strcmp((char*)chunk, "OK") == 0)
        {
            registered = true;
            check = true;
        }
        else
        {
            LOG_DBG("Tyre already in use\n");
        }
    }
    else{
        LOG_DBG("Error\n");
    }
}

// Se mi risponde prima che scada il timer resetto
void checker(coap_message_t *response){
    if(response != NULL){
        check = true;
    }
    else
    {
        check = false;
        registered = false;
        LOG_DBG("Connection lost\n");
    }
}

PROCESS(coap_server, "Tyrewarmer actuator");
AUTOSTART_PROCESSES(&coap_server);

PROCESS_THREAD(coap_server, ev, data)
{
    PROCESS_BEGIN();

    static coap_endpoint_t server_ep;
    static coap_message_t request[1];

    coap_activate_resource(&res_tyrewarmer_toggle, "tyrewarmer");
    leds_on(LEDS_RED);

    etimer_set(&periodic_state_timer, STATE_TIMER);
    coap_endpoint_parse(SERVER_IP, strlen(SERVER_IP), &server_ep);

    while(1)
    {
        PROCESS_YIELD();
        // Evento bottone
        if(ev == button_hal_release_event)
        {
            if(isRegistered == 1)
            {
                LOG_DBG("Tyrewarmer toggled\n");
                res_tyrewarmer_toggle.trigger();
            }
        }
        else if(ev == PROCESS_EVENT_TIMER && data == &periodic_state_timer)
        {
        // Registra
        if(have_conn())
            {
            // Invia una richiesta di registrazione BLOCCANTE
            if(!isRegistered)
                {
                int leng = sprintf(msg_size,"type=REG1&tyre=%d", ID_PAIR);

                coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
                coap_set_header_uri_path(request, "registrator");
                coap_set_payload(request, msg_size, leng);

                printf("Send a request of registration...\n");
                COAP_BLOCKING_REQUEST(&server_ep, request, handler);
            }
            //Check if is register
            else
            {
                if(check)
                {
                    check = false;

                    coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
                    coap_set_header_uri_path(request, "registrator");
                    COAP_BLOCKING_REQUEST(&server_ep, request, checker);
                }
            }
        }
        else
            {
            LOG_DBG("Connecting to Border Router\n");
            }
        }
        etimer_reset(&periodic_state_timer);
    }
    PROCESS_END();
}