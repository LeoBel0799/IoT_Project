# IoT_Project

Classi per COAP: Motion e LightStatus
Classi per MQTT: MotionHandler e Light StatusHandler

MOTION
Attributi: lights, lightsDegree, brights, lightsOffCount e lightOnCount
In questa classe viene dichiarato un oggetto di tipo mqtClient per passare il riferimento dell'MQTT di LIghtStatusHandler.
Gli ultimi due attributi serviranno per calcolare il wearLeel delle luci così da poterle eventualmente sostiuire.
In HandleMqttMessagge viene ricevuto il calcolo dell'usura che verrà effettuato nella parte mqtt e verrà poi trasmesso al Motion. Motion trasmettera questo dato all'MQTT di Light
Inoltre ogni volta che le luci si accendono e spengono i contatori lightsoff e lightson si aggiornano. Questi contatori vengono passati, tramite un'implementazione di una interfaccia 
che fa da metodo callback, alla parte MQTT(MotionHandler) che calcolerà con una formula il livello di usare. Di regola i contatori vengono trasmessi ogni qual volta vengono aggiornati.
Una volta calcolato il valore di usura il messaggio viene pubblicato sul broker di LightStatusHandler (MQTT).
Il metodo executeQuery fa la query di aggiornamento di tutti i parametri nella tabella coapMotion in SQL basadosi sul grado di usura in input.
Tabella aggiornata e gestita ----> CoapMotion

MOTIONHANDLER
Parte MQTT che integra la parte COAP per la trasmissione dei messaggi. viene dichirato un oggetto LightStatusHandler. In questa classe, nello specifico nel metodo messaggeArrived
tramite la lettura del Json che arriva dalla parteCoap viene calcolata l'usura tramite una formula c
Qui viene implementato l'unico metodo dell'interfaccia costruita ad hoc per il mantenimento dei contatori.
In più viene creato il payload da mandare a coap con 3 attributi incluso il wearLevel appena calcolato.
in più viene crato un metodo per madare il valore dell'usura all'MQTT LightStatusHandler. Il metodo si chiama HandleWearLevel.

LIGHTSTATUS
Atributi: costante di massima usura, livello di usura e luce fulminata
Ricevuto il messaggio da MQTT verifica se il livello di usura ricevuto dalla parte mqtt lightstatus ricevuto a sua volta dall'mqtt di motion è accettabile o meno se l'usura è uguale alla soglia
massima allora l'attributo fulminato viene messo su true e fatta una query di aggiornamento alle due tabelle coaplisghtstauts e coapalarm.
in execute querylight una tabella scrive i valori di usura e rottura della luce e l'altra si salva attivazione dell'allarme e livello di usura

LIGHTSTATUSHANDLER
Simile all'altro ma si incentra sulla Luce fulimnata.
