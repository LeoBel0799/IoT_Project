#include <stdlib.h>
#include <string.h>

#include "dev/leds.h"
#include "coap-engine.h"


/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Light-Resource"
#define LOG_LEVEL LOG_LEVEL_DBG

static void res_get_handler( coap_message_t *request, coap_message_t *response,
        uint8_t *buffer, uint16_t preferred_size, int32_t *offset);


static void res_put_handler( coap_message_t *request,coap_message_t *response,
                             uint8_t *buffer,uint16_t preferred_size, int32_t *offset);

static void res_trigger(void);

EVENT_RESOURCE(res_light,
               "title=\"Light Handler\"",
               res_get_handler,
               NULL,
               res_put_handler,
               NULL,
               res_trigger
                );

static int wearLel = 0; // -1 Substitute light , 0 light works, 1 light does not work

// Red led -> The light does not work (Button)
// Yellow led -> The light is active but not working because value > threshold
// Green led -> The light is currently active

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    int length = 0;
    const char* message;

    switch (wearLel)
    {
        case -1:
            length = 8;
            message = "DISABLED";
            break;
        case 0:
            length = 5;
            message = "WORKS";
            break;
        case 1:
            length = 7;
            message = "NOWORKS";
            break;

        default:
            message = "ERROR";
            break;
    }

    if(length < 0) {
        length = 0;
    }

    if(length > REST_MAX_CHUNK_SIZE) {
        length = REST_MAX_CHUNK_SIZE;
    }

    memcpy(buffer, message, length);

    coap_set_header_content_format(response, TEXT_PLAIN); /* text/plain is the default, hence this option could be omitted. */
    coap_set_header_etag(response, (uint8_t *)&length, 1);
    coap_set_payload(response, buffer, length);
}

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    size_t len = 0;
    const char *command = NULL;
    uint8_t led = 0;
    int success = 1;

    //command si trova in java nel controller
    if((len = coap_get_query_variable(request, "command", &command)))
    {
        LOG_DBG("Command %.*s\n", (int)len, command);

        // DISATTIVO la termocoperta
        if(strncmp(command, "DISABLE", len) == 0)
        {
            led = LEDS_RED;
            wearLel = -1;
        }

        // ATTIVO la luce sostituita
        else if(strncmp(command, "WORKS", len) == 0)
        {
            // Ma solo se era DISATTIVATA
            if (wearLel < 0)
            {
                led = LEDS_GREEN;
                status = 1;
            }
        }
        // SPENGO la luce
        else if(strncmp(command, "NOWORKS", len) == 0)
        {
            // Ma solo se non era DISATTIVATA
            if(wearLel == -1)
                success = 0;
            else
            {
                led = LEDS_YELLOW;
                status = 0;
            }
        }

        // ACCENDO la termocoperta
        else if(strncmp(command, "LOWTEMP", len) == 0)
        {
            // Ma solo se non era DISATTIVATA
            if(status == -1)
                success = 0;
            else
            {
                led = LEDS_GREEN;
                status = 1;
            }
        }
        else
        {
            success = 0;
        }
    }
    else
    {
        success = 0;
    }


    if(!success) {
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
    else
    {
        coap_set_status_code(response, CONTENT_2_05);

        leds_off(LEDS_ALL);
        leds_on(led);
    }
}

static void res_trigger()
{
    uint8_t led = 0;

    // Change the light and set led
    if(wearLel < 0)
    {
        wearLel = 1;
        led = LEDS_GREEN;
    }

    //light does not work
    else
    {
        status = -1;
        led = LEDS_RED;
    }

    leds_off(LEDS_ALL);
    leds_on(led);
}