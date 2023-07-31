
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

static struct etimer led_etimer;

/* Log configuration */
#define LOG_MODULE "Mqtt_node"
#define LOG_LEVEL LOG_LEVEL_APP


/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Default config values
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)


/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT    		  0
#define STATE_NET_OK    	  1
#define STATE_CONNECTING      2
#define STATE_CONNECTED       3
#define STATE_SUBSCRIBED      4
#define STATE_DISCONNECTED    5

/*---------------------------------------------------------------------------*/
PROCESS_NAME(mqtt_client_process);
AUTOSTART_PROCESSES(&mqtt_client_process);

/*---------------------------------------------------------------------------*/
/* Maximum TCP segment size for outgoing segments of our socket */
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64
/*---------------------------------------------------------------------------*/
/*
 * Buffers for Client ID and Topics.
 * Make sure they are large enough to hold the entire respective string
 */
#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
//I nostri dati
static char pub_topic_temperature[BUFFER_SIZE];
static char pub_topic_humidity[BUFFER_SIZE];
static char pub_topic_presence[BUFFER_SIZE];
/*----------------------------------------------------------------------*/
#define AREA_ID 1
#define NODE_ID 1
#define MAX_CAPACITY 50

//Defined a periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static struct etimer periodic_timer;

// Periodic timer to publish a message (every 30 sec)
#define PUB_PERIOD 30 * CLOCK_SECOND
static struct etimer pub_timer;

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

mqtt_status_t status;
char broker_address[CONFIG_IP_ADDR_STR_LEN];


/*---------------------------------------------------------------------------*/
PROCESS(mqtt_client_process, "MQTT Client");


// Function to clean the message array
void cleanArray(char* array, size_t size) {
    for (size_t i = 0; i < size; i++) {
        array[i] = '\0';
    }
}
/*---------------------------------------------------------------------------*/
static void
pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
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
static void
mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
    switch(event) {
        case MQTT_EVENT_CONNECTED: {
            printf("Application has a MQTT connection\n");

            state = STATE_CONNECTED;
            break;
        }
        case MQTT_EVENT_DISCONNECTED: {
            printf("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));

            state = STATE_DISCONNECTED;
            process_poll(&mqtt_client_process);
            break;
        }
        case MQTT_EVENT_PUBLISH: {
            msg_ptr = data;

            pub_handler(msg_ptr->topic, strlen(msg_ptr->topic),
                        msg_ptr->payload_chunk, msg_ptr->payload_length);
            break;
        }
        case MQTT_EVENT_SUBACK: {
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
        }
        case MQTT_EVENT_UNSUBACK: {
            printf("Application is unsubscribed to topic successfully\n");
            break;
        }
        case MQTT_EVENT_PUBACK: {
            printf("Publishing complete.\n");
            break;
        }
        default:
            printf("Application got a unhandled MQTT event: %i\n", event);
            break;
    }
}

static bool have_connectivity(void)
{
    if(uip_ds6_get_global(ADDR_PREFERRED) == NULL ||
       uip_ds6_defrt_choose() == NULL) {
        return false;
    }
    return true;
}


// Functions to return measurement
//CAMBIARE
void set_temperature(char msg[]){
    int temperature = (5 + rand()%35);

    LOG_INFO("[+] temperature detected: %d\n", temperature);

    sprintf(msg,"{\"cmd\":\"%s\",\"value\":%d,\"area_id\":%d,\"node_id\":%d}",
            "temperature",
            temperature,
            AREA_ID,
            NODE_ID
    );

    LOG_INFO(" >  %s\n", msg);
}
//CAMBIARE
void set_humidity(char msg[]){
    // Generazione di un numero casuale compreso tra 0 e 100
    int random = rand() % 101;
    // Mapping del numero casuale nell'intervallo 20-80
    int humidity = (random * 60 / 100) + 20;

    LOG_INFO("[+] Humidity detected: %d\n", humidity);

    sprintf(msg,"{\"cmd\":\"%s\",\"value\":%d,\"area_id\":%d,\"node_id\":%d}",
            "humidity",
            humidity,
            AREA_ID,
            NODE_ID
    );

    LOG_INFO(" >  %s\n", msg);
}
//CAMBIARE
void set_presence(char msg[]){

    int presence = rand() % MAX_CAPACITY;

    LOG_INFO("[+] presence detected: %d\n", presence);

    sprintf(msg,"{\"cmd\":\"%s\",\"value\":%d,\"area_id\":%d,\"node_id\":%d}",
            "presence",
            presence,
            AREA_ID,
            NODE_ID
    );

    LOG_INFO(" >  %s\n", msg);
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(mqtt_client_process, ev, data)
{

PROCESS_BEGIN();

// Initialize the LED
leds_init();

LOG_INFO("[!] Start MQTT node \n");

// Initialize the ClientID as MAC address
snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

// Broker registration
mqtt_register(&conn, &mqtt_client_process, client_id, mqtt_event,
MAX_TCP_SEGMENT_SIZE);

state=STATE_INIT;

// Initialize periodic timer to check the status
etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);

// Initialize pub timer to publish messages
etimer_set(&pub_timer, 1 * CLOCK_SECOND);

/* Main loop */
while(1) {

PROCESS_YIELD();

if((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) ||
ev == PROCESS_EVENT_POLL || ( ev == PROCESS_EVENT_TIMER && data == &pub_timer) ){

if(state==STATE_INIT){
if(have_connectivity()==true)
state = STATE_NET_OK;
}

if(state == STATE_NET_OK){
// Connect to MQTT server
LOG_INFO("[!] Connecting \n");

memcpy(broker_address, broker_ip, strlen(broker_ip));

mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
( DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
MQTT_CLEAN_SESSION_ON);
state = STATE_CONNECTING;
}

if(state == STATE_CONNECTED && etimer_expired(&pub_timer)){

// Pub temperature
LOG_INFO("[!] Public message on topic temperature \n");

sprintf(pub_topic_temperature, "%s", "temperature");

cleanArray(app_buffer, sizeof(app_buffer));
set_temperature(app_buffer);

mqtt_publish(&conn, NULL, pub_topic_temperature, (uint8_t *)app_buffer,
strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

leds_on(LEDS_GREEN);
etimer_set(&led_etimer, 2 * CLOCK_SECOND);
PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&led_etimer));
leds_off(LEDS_GREEN);

// Pub humidity
LOG_INFO("[!] Public message on topic humidity \n");

sprintf(pub_topic_humidity, "%s", "humidity");

cleanArray(app_buffer, sizeof(app_buffer));
set_humidity(app_buffer);

mqtt_publish(&conn, NULL, pub_topic_humidity, (uint8_t *)app_buffer,
strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

leds_on(LEDS_BLUE);
etimer_set(&led_etimer, 2 * CLOCK_SECOND);
PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&led_etimer));
leds_off(LEDS_BLUE);

// Pub presence
LOG_INFO("[!] Public message on topic presence \n");

sprintf(pub_topic_presence, "%s", "presence");

cleanArray(app_buffer, sizeof(app_buffer));
set_presence(app_buffer);

mqtt_publish(&conn, NULL, pub_topic_presence, (uint8_t *)app_buffer,
strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

leds_on(LEDS_RED);
etimer_set(&led_etimer, 2 * CLOCK_SECOND);
PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&led_etimer));
leds_off(LEDS_RED);

etimer_set(&pub_timer, PUB_PERIOD);

} else if ( state == STATE_DISCONNECTED ){
LOG_INFO("[-] Disconnected form MQTT broker \n");
// Recover from error
}

etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
}

}

PROCESS_END();
}
/*---------------------------------------------------------------------------*/