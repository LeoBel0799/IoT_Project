
/*---------------------------------------------------------------------------*/
#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client.h"

#include <string.h>
#include <strings.h>
#include <time.h>

/*---------------------------------------------------------------------------*/
#define LOG_MODULE "mqtt-client"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif
static struct etimer led_etimer;

/*---------------------------------------------------------------------------*/
/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Default config values
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)



/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT            0 // Initial state
#define STATE_NET_OK          1 // Network is initialized
#define STATE_CONNECTING      2 // Connecting to MQTT broker
#define STATE_CONNECTED       3  // Connection successful
#define STATE_SUBSCRIBED      4  // Topics of interest subscribed
#define STATE_DISCONNECTED    5  // Disconnected from MQTT broker

// status mqtt
mqtt_status_t status;

/*---------------------------------------------------------------------------*/
PROCESS_NAME(mqtt_client_process);
AUTOSTART_PROCESSES(&mqtt_client_process);

/*---------------------------------------------------------------------------*/
/* Socket maximum size */
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64

static char broker_address[CONFIG_IP_ADDR_STR_LEN];

//Values of sensors
static int light = 0;
static int light_degree = 0;
static int brights = 0;


// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC  CLOCK_SECOND * 30
static struct etimer periodic_timer;

#define PERIODIC_TIMER 30
/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];
/*---------------------------------------------------------------------------*/
static struct mqtt_message *msg_ptr = 0;

static struct mqtt_connection conn;

/*---------------------------------------------------------------------------*/

/*---------------------------------------------------------------------------*/
/*
 * Buffers for Client ID and Topics.
 * Make sure they are large enough to hold the entire respective string
 */
#define BUFFER_SIZE 256
static char client_id[BUFFER_SIZE];

#define PUB_TOPIC "Motion"
#define SUB_TOPIC "Light"


/*---------------------------------------------------------------------------*/
PROCESS(mqtt_client_process,"MQTT Client");

/*---------------------------------------------------------------------------*/
static void pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
                        uint16_t chunk_len) {
    printf("Pub Handler: topic='%s' (len=%u), chunk_len=%u\n", topic, topic_len, chunk_len);

    if (strcmp(topic, SUB_TOPIC) == 0) {
        printf("Received Actuator command\n");
        printf("%s\n", chunk);

        return;
    }
}

/*---------------------------------------------------------------------------*/


static void mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data) {
    switch (event) {
        case MQTT_EVENT_CONNECTED:
            printf("The application has a MQTT connection\n");
            state = STATE_CONNECTED;
            break;
        case MQTT_EVENT_DISCONNECTED:
            printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *) data));
            state = STATE_DISCONNECTED;
            process_poll(&mqtt_client_process);
            break;
        case MQTT_EVENT_PUBLISH:
            msg_ptr = data;
            pub_handler(msg_ptr->topic, strlen(msg_ptr->topic),
                        msg_ptr->payload_chunk, msg_ptr->payload_length);
            break;
        case MQTT_EVENT_SUBACK:
#if MQTT_311
            mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;
            if(suback_event->success) {
            printf("Application is subscribed to topic successfully\n");
            } else {
            printf("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
            }
#else
            printf("Application is subscribed to topic successfully\n");
#endif
            break;
        case MQTT_EVENT_UNSUBACK:
            printf("Application is unsubscribed to topic successfully\n");
            break;
        case MQTT_EVENT_PUBACK:
            printf("Publishing complete.\n");
            break;
        default:
            printf("Application got a unhandled MQTT event: %i\n", event);
            break;
    }
}

static bool have_connectivity() {
    if (uip_ds6_get_global(ADDR_PREFERRED) == NULL || uip_ds6_defrt_choose() == NULL)
        return false;
    else
        return true;
}
/*---------------------------------------------------------------------------*/
//SUBSCRIBER
PROCESS_THREAD(mqtt_client_process,ev, data){
    PROCESS_BEGIN();

    printf("MQTT Client Process\n");
    leds_init();
    // Initialize the ClientID as MAC address
    snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
    linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
    linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
    linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

    mqtt_register(&conn, &mqtt_client_process, client_id, mqtt_event, MAX_TCP_SEGMENT_SIZE);

    //Setting the initial state
    state = STATE_INIT;

    // Initialize periodic timer to check the status
    etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
    int messageCounter = 0;

    /* Main loop */
    while(1) {
    PROCESS_YIELD();

    if((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) || ev == PROCESS_EVENT_POLL){
        if(state==STATE_INIT){
            if(have_connectivity()==true){
            printf("Connectivity verified!\n");
            state = STATE_NET_OK;
            }
        }

        if(state == STATE_NET_OK){
        // Connect to MQTT server
        LOG_INFO("Connecting to MQTT server\n");
        memcpy(broker_address, broker_ip, strlen(broker_ip));

        mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT, (DEFAULT_PUBLISH_INTERVAL * 3)/CLOCK_SECOND, MQTT_CLEAN_SESSION_ON);

        state = STATE_CONNECTING;
        printf("Connecting!\n");
        }

        if(state==STATE_CONNECTED){
        //Topic subscribe
        status = mqtt_subscribe(&conn, NULL, PUB_TOPIC, MQTT_QOS_LEVEL_0);
        printf("Subscribing\n");
            if (status == MQTT_STATUS_OUT_QUEUE_FULL){
                LOG_ERR("Tried to subscribe but commmand queue was full!\n");
                PROCESS_EXIT();
            }
        state = STATE_SUBSCRIBED;
        }

        if(state==STATE_CONNECTING){
            LOG_INFO("Not connected yet\n");
        }
        int light_id;
        if (state == STATE_SUBSCRIBED) {
            LOG_INFO("[!] Public message on topic Motion \n");
            //questo per avere id_ciclico
            light_id = ((messageCounter % 4) + 4) % 4 + 1;
            //light on-off
            light = (rand()%2);
            char* light_str = light ? "ON" : "OFF"; //0 acceso 1 spento
            //light Degree
            light_degree = (rand()%3);
            //brights 0-1
            brights = (rand()%2);
            char* bright_str = brights ? "ON" : "OFF";

            sprintf(app_buffer,"{\"id\":%d,\"lights\":%s,\"lightsDegree\":%d,\"brights\":%s}",light_id,light_str,
            light_degree,bright_str);
            messageCounter++;

            printf("Message: %s\n",app_buffer);
            //Publish message
            mqtt_publish(&conn, NULL, PUB_TOPIC, (uint8_t *)app_buffer,strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
            leds_on(LEDS_GREEN);
            etimer_set(&led_etimer, 2 * CLOCK_SECOND);
            PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&led_etimer));
            leds_off(LEDS_GREEN);
        }
        else if ( state == STATE_DISCONNECTED ){
               LOG_ERR("Disconnected from MQTT broker\n");
               //Error recovering
               state = STATE_INIT;
        }

        etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
        }

    }

    PROCESS_END();

}

