
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

#include <stdint.h>
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

/*---------------------------------------------------------------------------*/
/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Default config values
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)
#define MOTION_TOPIC "motion"
#define LIGHT_TOPIC "light"


/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;
static uint8_t state_light;

#define STATE_INIT            0 // Initial state
#define STATE_NET_OK          1 // Network is initialized
#define STATE_CONNECTING      2 // Connecting to MQTT broker
#define STATE_CONNECTED       3  // Connection successful
#define STATE_SUBSCRIBED      4  // Topics of interest subscribed
#define STATE_DISCONNECTED    5  // Disconnected from MQTT broker

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
/*
char topics[2][BUFFER_SIZE];
strcpy(topics[0], "motion");
strcpy(topics[1], "light");
*/

// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC  CLOCK_SECOND * 30
static struct etimer periodic_timer;

#define PERIODIC_TIMER 30
/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 1024
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
static char pub_topic[BUFFER_SIZE];


// Periodic timer to check the state of the MQTT client

/*---------------------------------------------------------------------------*/
PROCESS(mqtt_client_process,"MQTT Client");

/*---------------------------------------------------------------------------*/
static void pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
                        uint16_t chunk_len) {
    printf("Pub Handler: topic='%s' (len=%u), chunk_len=%u\n", topic, topic_len, chunk_len);

    if(strcmp(topic, MOTION_TOPIC) == 0) {
        // gestione messaggio da topic MOTION_TOPIC

    } else if(strcmp(topic, LIGHT_TOPIC) == 0) {
        // gestione messaggio da topic LIGHT_TOPIC

    } else {
       // gestione generica altri topic
    }
    /*if (strcmp(topic, "actuator") == 0) {
        printf("Received Actuator command\n");
        printf("%s\n", chunk);

        return;
    }*/
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

/*-----------------------------------*/
//RICEZIONE DEL VALORE WEARLEVEL DAL TOPIC LIGHT, IL QUALE HA RICEVUTO
//IL VALORE DA MOTION, E ADESSO VIENE RIPUBBLICATO SULLO STESSO TOPIC
//"LIGHT" A DISPOSIZIONE DEL SOECONDO COAP OVVERO LIGHT.

void messageReceived(char* topic, byte* payload) {

        if(strcmp(topic, "light") == 0) {

            // Converte payload in stringa
            char* payload_str = (char*) payload;

            // Analizza JSON
            JSON_Value* root = json_parse_string(payload_str);

            // Estrai wearLevel
            double wearLevel = json_object_get_number(root, "wearLevel");

          // Crea il nuovo payload JSON
            char new_payload[BUFFER_SIZE];
            sprintf(new_payload, "{\"wearLevel\": %.2f}", wearLevel);

            // Pubblica il nuovo payload sul topic "light"
            mqtt_publish(&client, "light", new_payload, strlen(new_payload), QoS, RETAIN);
        }
}


/*-------------------------------------------*/


/*---------------------------------------------------------------------------*/
//SUBSCRIBER
PROCESS_THREAD(mqtt_client_process,ev, data){
    PROCESS_BEGIN();


    printf("MQTT Client Process\n");

    // Initialize the ClientID as MAC address
    snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
    linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
    linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
    linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

    mqtt_register(&conn, &mqtt_client_process, client_id, mqtt_event, MAX_TCP_SEGMENT_SIZE);

    //Setting the initial state
    state = STATE_INIT;
    state_light = STATE_INIT;
    // Initialize periodic timer to check the status
    etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);

    /* Main loop */
    while(1) {
        PROCESS_YIELD();

        if((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) || ev == PROCESS_EVENT_POLL){
            if(state==STATE_INIT && state_light==STATE_INIT){
                if(have_connectivity()==true){
                printf("Connectivity verified for both states!\n");
                state = STATE_NET_OK;
                state_light = STATE_NET_OK
                }
            }
            if(state == STATE_NET_OK && state_light== STATE_NET_OK){
            // Connect to MQTT server
            LOG_INFO("Connecting to MQTT server\n");
            memcpy(broker_address, broker_ip, strlen(broker_ip));

            mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT, (DEFAULT_PUBLISH_INTERVAL * 3)/CLOCK_SECOND, MQTT_CLEAN_SESSION_ON);

            state = STATE_CONNECTING;
            state_light = STATE_CONNECTING;
            printf("Connecting!\n");
            }
            if(state==STATE_CONNECTED && state_light==STATE_CONNECTED){
                //Topic subscribe
                strcpy(pub_topic,"motion");
                state = mqtt_subscribe(&conn, NULL, pub_topic,MQTT_QOS_LEVEL_0);
                printf("Subscribing to motion\n");
                strcpy(pub_topic_light,"light");
                state_light = mqtt_subscribe(&conn, pub_topic_light, QoS);
                printf("Subscribing to light\n");
                mqtt_set_message_callback(messageReceived);

                if (state == MQTT_STATUS_OUT_QUEUE_FULL){
                    LOG_ERR("Tried to subscribe but commmand queue was full!\n");
                    PROCESS_EXIT();
                }
                state = STATE_SUBSCRIBED;
                state_light =  STATE_SUBSCRIBED
            }

            if(state==STATE_CONNECTING && state_light==STATE_CONNECTING ){
                LOG_INFO("Not connected yet\n");
            }

            if (state == STATE_SUBSCRIBED && state_light = STATE_SUBSCRIBED) {
                sprintf(pub_topic,"%s", "motion");
                //light on-off
                light = (rand()%2);
                char* light_str = light ? "ON" : "OFF";
                //light Degree
                light_degree = (rand()%3);
                //brights 0-1
                brights = (rand()%2);
                sprintf(app_buffer,"{\"lights\":%s,\"lightsDegree\":%d,\"brights\":%d}",light_str,
                        light_degree,brights);
                printf("Message: %s\n",app_buffer);

                // Costruzione del payload JSON
                char payload[BUFFER_SIZE];
                snprintf(payload, BUFFER_SIZE, "{\"lights\":%s,\"lightsDegree\":%d,\"brights\":%d}", light_str, light_degree, brights);

                //Publish message
                mqtt_publish(&conn, NULL, "motion", (uint8_t *)app_buffer,strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

                }else if ( state == STATE_DISCONNECTED ){
                    LOG_ERR("Disconnected from MQTT broker\n");
                    //Error recovering
                    state = STATE_INIT;
                }
            etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
        }
    }
    PROCESS_END();
}


