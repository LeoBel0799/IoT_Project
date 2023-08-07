#include "contiki.h"
#include "resource.h"

#include <string.h>
#include <stdio.h>
#include "coap-engine.h"
#include "sys/log.h"
#include "sys/etimer.h"
#include <stdlib.h>

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer,
                            uint16_t preferred_size, int32_t *offset);

static void res_event_handler(void);

static struct lights_str{
    int value;
    bool isActive;
    struct etimer temperature_etimer;
}lights_mem;

EVENT_RESOURCE(
        res_temperature,
        "title=\"lightStatus\";rt=\"lightStatus\"",
        res_get_handler,
        NULL,
        NULL,
        NULL,
        res_event_handler
        );

void initialize_lights_str(){
    lights_mem.value = 0;
    temperature_mem.isActive = false;
}

void set_temperature_etimer(){
    etimer_set(&temperature_mem.temperature_etimer, NOTIFICATION_TIME_TEMPERATURE * CLOCK_SECOND);
}

bool check_temperature_timer_expired(){
    return etimer_expired(&temperature_mem.temperature_etimer);
}

void restart_temperature_timer(){
    etimer_reset(&temperature_mem.temperature_etimer);
}

void set_air_conditioner_status(bool on){
    temperature_mem.air_conditioner_on = on;
}

void set_temperature(char msg[]){
    int temperature;
    if(temperature_mem.air_conditioner_on == false){
        temperature = (5 + rand()%35);
    }else {
        temperature = (19 + rand()%4);
    }
    temperature_mem.value = temperature;

    LOG_INFO("[+] temperature detected: %d\n", temperature);

    sprintf(msg,"{\"cmd\":\"%s\",\"value\":%d}",
            "temperature",
            temperature_mem.value
    );

    LOG_INFO(" >  %s\n", msg);
}

static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    char reply[MSG_SIZE];

    LOG_INFO(" <  GET actuator/temperature\n");
    set_temperature(reply);

    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_payload(response, buffer, snprintf((char *)buffer, preferred_size, "%s", reply));
}

static void
res_event_handler(void)
{
    // Notify all the observers
    coap_notify_observers(&res_temperature);
}