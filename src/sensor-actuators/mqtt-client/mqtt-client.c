/*---------------------------------------------------------------------------*/
#include "contiki.h"
#include "net/routing/routing.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt.h"
#include <string.h>
#include <strings.h>
#include <time.h>
#include <stdint.h>

/*---------------------------------------------------------------------------*/
//Configuration of Log
#define LOG_MODULE "mqtt-client"
#define LOG_LEVEL LOG_LEVEL_APP

/*---------------------------------------------------------------------------*/
/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Defaukt config values
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)
/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT    	      0
#define STATE_NET_OK   	      1
#define STATE_CONNECTING      2  // Connecting to MQTT broker
#define STATE_CONNECTED       3
#define STATE_SUBSCRIBED      4  // Topics of interest subscribed
#define STATE_DISCONNECTED    5  // Disconnected from MQTT broker

/*---------------------------------------------------------------------------*/
//Define process name
PROCESS_NAME(mqtt_client_process);
AUTOSTART_PROCESSES(&mqtt_client_process);

/*---------------------------------------------------------------------------*/
/* Maximum TCP segment size for outgoing segments of our socket */
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64


// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC  (CLOCK_SECOND >> 1)
#define PUBLISH_PERIOD  (CLOCK_SECOND * 30)
static struct etimer periodic_timer;
//Define a new timer to handle the phase of publish
static struct etimer pub_timer;

/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 64
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
/*---------------------------------------------------------------------------*/
//QUI INSERIRE I VAALORI DA INVIARE VIA MQTT
//light statuhandler
//motionHandler

#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
static char pub_topic_light_id [BUFFER_SIZE];                //Id per identificare Luce
static char pub_topic_lights_degree[BUFFER_SIZE]; //Posizione, anabbagliante, abbagliante
static char pub_topic_lights[BUFFER_SIZE]; //Acceso, Spento



/*---------------------------------------------------------------------------*/
PROCESS(mqtt_client_process, "MQTT Client");
/*---------------------------------------------------------------------------*/
// Function to clean the message array
void cleanArray(char* array, size_t size) {
    for (size_t i = 0; i < size; i++) {
        array[i] = '\0';
    }
}
static void pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
                        uint16_t chunk_len)
{
    printf("Pub Handler: topic='%s' (len=%u), chunk_len=%u\n", topic,
           topic_len, chunk_len);

    if(strcmp(topic, "actuator") == 0) {
        printf("Received Actuator command\n");
        printf("%s\n", chunk);
        // Do something :)
        return;
    }
}
/*---------------------------------------------------------------------------*/


static void mqtt_event(struct mqtt_connection* m, mqtt_event_t event, void* data){
    switch(event){
        case MQTT_EVENT_CONNECTED:
            printf("The application has a MQTT connection\n");
            state = STATE_CONNECTED;
            break;
        case MQTT_EVENT_DISCONNECTED:
            printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));
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
			}
            else {
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

static bool have_connectivity(){
    if(uip_ds6_get_global(ADDR_PREFERRED) == NULL || uip_ds6_defrt_choose() == NULL)
        return false;
    else
        return true;
}
/*---------------------------------------------------------------------------*/
//Inserire funzioni di set dei valori misurati
void set_valueLights(char msg[]){
    int lightsValue =(rand()%2); //0 acceso, 1 spento

    LOG_INFO("[+] Lights Status detected %d\n", lightsValue);

    sprintf(msg,"{\"cmd\":\"%s\",\"value\":%d}",
            "Is On?",
            lightsValue
    );

    LOG_INFO(" >  %s\n", msg);
}
void set_valueLightDegree(char msg[]){
    int lightsDegree =(rand()%3);  //0 posizione, 1 anabbagliante, 2 abbagliante


    LOG_INFO("[+] Lights Degree detected %d\n", lightsValue);

    sprintf(msg,"{\"cmd\":\"%s\",\"value\":%d}",
            "Lights Degree is ?",
            lightsDegree
    );

    LOG_INFO(" >  %s\n", msg);
}
void set_LightId(char msg[]){
    int lightId =(rand()%100);


    LOG_INFO("[+] LightsId detected %d\n", lightsValue);

    sprintf(msg,"{\"cmd\":\"%s\",\"value\":%d}",
            "Lights Degree is ?",
            lightsDegree
    );

    LOG_INFO(" >  %s\n", msg);
}

/*---------------------------------------------------------------------------*/

//SUBSCRIBER
PROCESS_THREAD(mqtt_client_process, ev, data)
{
  PROCESS_BEGIN();
  //mqtt_status_t status;
  char broker_address[CONFIG_IP_ADDR_STR_LEN];

  printf("MQTT Client Process\n");
  LOG_INFO("[!] Start MQTT Client Process \n");
  // Initialize the ClientID as MAC address
  snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
    linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
    linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
    linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

  mqtt_register(&conn, &mqtt_client_process, client_id, mqtt_event, MAX_TCP_SEGMENT_SIZE);

  //Setting the initial state
  state=STATE_INIT;

  // Initialize periodic timer to check the status
  etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
  etimer_set(&pub_timer, 1 * CLOCK_SECOND);


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
            mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
                         (DEFAULT_PUBLISH_INTERVAL * 3)/CLOCK_SECOND,
                         MQTT_CLEAN_SESSION_ON);
            state = STATE_CONNECTING;
            printf("Connecting!\n");
        }
        if(state==STATE_CONNECTING){
            LOG_INFO("Not connected yet\n");
        }
        if(state == STATE_CONNECTED  && etimer_expired(&pub_timer)){
            // Pub temperature
            LOG_INFO("[!] Public message on LightId \n");

            sprintf(pub_topic_light_id, "%s", "LightId");
            cleanArray(app_buffer, sizeof(app_buffer));
            set_LightId(app_buffer);

            mqtt_publish(&conn, NULL, pub_topic_light_id, (uint8_t *)app_buffer,
            strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

             // Pub humidity
            LOG_INFO("[!] Public message on Light Degree \n");

            sprintf(pub_topic_lights_degree, "%s", "lightDegree");

            cleanArray(app_buffer, sizeof(app_buffer));
            set_valueLightDegree(app_buffer);

            mqtt_publish(&conn, NULL, pub_topic_lights_degree, (uint8_t *)app_buffer,
            strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

            // Pub presence
            LOG_INFO("[!] Public message on light is On \n");

            sprintf(pub_topic_lights, "%s", "presence");

            cleanArray(app_buffer, sizeof(app_buffer));
            set_valueLights(app_buffer);

            mqtt_publish(&conn, NULL, pub_topic_lights, (uint8_t *)app_buffer,
                         strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

            etimer_set(&pub_timer, PUBLISH_PERIOD);
        }
        else if ( state == STATE_DISCONNECTED ){
            LOG_ERR("Disconnected from MQTT broker\n");
            // Recover from error
            state = STATE_INIT;
        }
        etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
    }
  }
  PROCESS_END();
}

