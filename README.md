# IoT_Project

**TODO**: 
- Testare funzionamento effettivo della PUT su sensore (fino ad ora richiesta va a buon fine ma ne LED e ne LOG INFO si vedono su COOJA nonostate la loro presenza nel codice) ---- PROBABILE ERRORE IN MQTT?



- Testare tutta la casistica derivante da wearLevelRecevied>Soglia nel metodo setLight() in PoweringLights.java (questa casistica attiva la risorsa obs tramite POST dal Java al C e in più richiede reset valori wear e fulminated tramite bottone e poi rimanda i valori resettatti nel JAVA che dovrebbe aggiornare i valori nella tabella actuator per quel deterimanto id la cui luce era fulminata)




- In ultimo, testare tutta la casistica BRIGHT del menu che si appoggia su LIGHT. Testato il corretto funziomento di LIGHT, correggere BRIGHT dovrebbe essere veloce.