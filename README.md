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


ESEMPIO DI FLOW:
La comunicazione tra i due handler, segue il seguente flusso:

All'interno di "MotionHandler", viene rilevato un aggiornamento dai sensori e chiamato il metodo "handleMqttMessage" di "Motion" per elaborare i dati.
In "handleMqttMessage" di "Motion", vengono aggiornati i contatori di accensioni e spegnimenti delle luci in base ai dati ricevuti. Viene anche calcolato un valore di "wearLevel" (livello di usura) in base ai dati dei sensori. Successivamente, viene chiamato il metodo "onLightsStatusUpdated" di "LightStatusListener" (che è implementato da "MotionHandler") per passare i valori aggiornati.
In "MotionHandler", il metodo "onLightsStatusUpdated" viene chiamato e riceve i valori aggiornati dei contatori di accensioni e spegnimenti delle luci. A questo punto, può anche calcolare un valore di "wearLevel" aggiornato in base ai dati dei sensori di movimento e delle luci, poiché ha accesso a queste informazioni attraverso l'istanza di "Motion".
"MotionHandler" può quindi chiamare il metodo "publishWearLevel" di "LightStatusHandler" per inviare il valore aggiornato di "wearLevel" all'MQTT broker di "LightStatusHandler".
All'interno di "LightStatusHandler", il metodo "publishWearLevel" riceve il valore di "wearLevel" e crea un payload JSON contenente questa informazione. Successivamente, pubblica il messaggio MQTT su un certo topic per consegnare i dati al sistema di "LightStatusHandler".
Infine, "LightStatusHandler" riceve il messaggio MQTT con il valore di "wearLevel" pubblicato da "MotionHandler". In base a questo valore, può decidere se attivare o meno l'allarme per indicare l'usura e il possibile guasto delle luci.
Esempio di flow di comunicazione:

Supponiamo che i sensori di movimento rilevino un aumento delle attività e, quindi, il numero di accensioni delle luci aumenti. Il valore di "wearLevel" calcolato riflette questo aumento delle attività, indicando un potenziale aumento dell'usura delle luci. "MotionHandler" chiamerà quindi "publishWearLevel" di "LightStatusHandler" per inviare il nuovo valore di "wearLevel".

Supponiamo che "wearLevel" abbia raggiunto un livello critico o vicino al valore massimo consentito (ad esempio, 4.8 su 5.0). "LightStatusHandler" riceverà il messaggio MQTT con questo valore di "wearLevel" pubblicato da "MotionHandler". "LightStatusHandler" elaborerà il valore e, poiché il livello è critico, attiverà l'allarme per segnalare il problema con le luci. Potrebbe inviare una notifica o eseguire azioni appropriate per gestire la situazione e richiedere un intervento.

In questo esempio, "MotionHandler" e "LightStatusHandler" comunicano correttamente e si scambiano informazioni per monitorare lo stato delle luci e l'usura per fornire allarmi tempestivi in caso di problemi.



Mi scuso per l'errore di battitura, sembra che ci sia una confusione nel nome della variabile "lightFulminated". Nella classe "LightsStatus", sembra che il nome corretto della variabile sia "lightFulminated" e non "lightFulimnated".

Rispondendo alla tua domanda:

"lightFulminated" viene aggiornato nel metodo "handleMqttMessage" di "LightsStatus" in base al valore di "wearLevel". Se il "wearLevel" supera o raggiunge il valore massimo consentito (MAX_WEAR_LEVEL), allora "lightFulminated" viene impostato a "T" (true), indicando che le luci sono fulminate. Altrimenti, "lightFulminated" viene impostato a "F" (false), indicando che le luci non sono fulminate.

"lightFulminated" è inserito nel database solo quando il "wearLevel" raggiunge il valore massimo consentito. Questo è fatto nel metodo "executeQueryLight" di "LightsStatus". Se "wearLevel" è maggiore o uguale a MAX_WEAR_LEVEL, allora vengono eseguite due query:

a. La prima query inserisce i dati di "lightFulminated" e "wearLevel" nella tabella "coaplightstatus".

b. La seconda query inserisce i dati di "wearLevel" e l'allarme (che è true se il "wearLevel" supera il valore massimo, altrimenti è false) nella tabella "coapalarm".

La gestione dell'allarme e l'aggiornamento di "lightFulminated" nel database avvengono nel metodo "executeQueryLight" di "LightsStatus". Questo metodo è chiamato quando il "wearLevel" raggiunge il valore massimo, quindi è il punto in cui viene presa la decisione di segnalare l'allarme e registrare lo stato di "lightFulminated" nel database.

Assicurati di utilizzare correttamente il nome della variabile "lightFulminated" in tutti i punti in cui viene utilizzata all'interno della classe "LightsStatus". Se necessario, correggi il nome nel codice per evitare errori di riferimento alla variabile.

