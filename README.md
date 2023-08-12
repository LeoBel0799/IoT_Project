# IoT_Project
In mqtt-node.c there are some field with values generated randomly. 
Fields are:
- Light_Id that is an integer included between 1 and 4 to realize the 4 lights
- Light that is boolean field ON OFF to indicate if lights are ON OFF
- Light Degree a field that does not matter in further manipolation
- Bright that is a boolean filed ON OFF to indicate if brights are ON OFF.

These fields with their data are published every some seconds on topic "Motion". A java class is subscribed to it for taking data for further manipulations. The class is LightHandler
whose aim is to subscribe itself to topic "Motion"and by means of MessageArrived() method it has to parse MQTT message and put those field and values in DB calling insertMotionData() method in LightDataClass.
At the beginning MessageArrived() method had to take one message, parsing it and put its field into DB causing a resource conflicts because
meanwhile other messaging were arriving from MQTT. So from a technical point, implementation was changed. A queue was implemented to store sequentially MQTT messages and
then this queue is processed by a thread that as a second entity, every 15 second (time is editable), takes the first message in the queue, parsing it and
put it in DB calling insertMotionData() and so on. Extreme case is taken in consideration with a quantity threshold, if queue reaches this treshold (75% editable) tot
messages are deleted from queue (older).


This is the first app required from specification.

The RegistrationLightResource class handles the registration of new light nodes. When a new node sends a POST request to /registration, this class extracts the node ID and IP address from the request payload. It saves this information to the database using the NodeData class. It then creates PoweringLights and PoweringBrights threads for that node, passing the ID and IP, and starts these threads.

The PoweringLights and PoweringBrights threads are responsible for controlling the lights and brights of a registered node. They use the LightBrightStatus class to send CoAP requests to get the current on/off status of the lights and brights from the node. Based on this status, they send PUT requests to turn the lights/brights on or off. The LightBrightStatus class abstracts away the CoAP client logic.

The LightBrightStatus class handles the actual sending of CoAP GET and PUT requests to the light nodes. It provides methods like getLightsOnOff and putLightsOn/Off. Under the hood, it uses the CoapClient class from Californium to send the CoAP messages. The LightBrightStatus caches the CoapClient for re-use.
It's important to note that brights can light on or lightoff only based on light state.
When the threads call methods on LightBrightStatus, it sends the appropriate CoAP request to the node, parses the response, and returns the status or result. The threads then use this information to update the node state.

The classes work together to allow registering new nodes, starting background threads to control them, querying their state via CoAP, and updating their lights/brights accordingly. The different responsibilities are divided between:

- RegistrationLightResource: node registration
- PoweringLights/Brights: node control logic
- LightBrightStatus: network communication
- NodeData: database access

Moreover there is a UserMenu class that is a fake "Car controller" trough whih user 
can activate or deactivate lights.
Coap part in C has to link reosurces to java part and then handle ON OFF request

**TODO**: Figure out if COAP part is written correctly and if it's works.