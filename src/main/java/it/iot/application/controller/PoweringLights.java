package it.iot.application.controller;

import it.iot.application.DB.ActuatorStatus;
import it.iot.application.DB.LightData;
import it.iot.application.DB.NodeData;
import it.iot.application.user.UserMenu;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.SQLException;

public class PoweringLights  {

    private LightBrightStatus coapClient;

    private static final double MAX_WEAR_LEVEL = 5.0;
    int light;
    String address;
    LightData lightData;
    NodeData nodeData;
    private ActuatorStatus actuatorStatus;

    public PoweringLights(int lightId, LightData lightData, NodeData nodeData, ActuatorStatus actuatorStatus) throws ConnectorException, IOException {
        this.lightData = lightData;
        this.nodeData = nodeData;
        this.actuatorStatus = actuatorStatus;


        String ipv6 = nodeData.getIPv6(lightId);
        this.light = lightId;
        this.address = ipv6;
        try {
            this.coapClient = new LightBrightStatus();
        } catch (Exception e) {
            // gestisci eccezioni
        }
    }


    /*
     Il metodo riceve come parametro un booleano fulminated che indica se la lampadina è fulminata.
    Tramite la classe ActuatorStatus si recuperano dal database i valori correnti di wear e fulminated per la luce passata come parametro, salvandoli in un array di stringhe data.
    Questi valori vengono stampati per debug.
    Dal data array si estraggono le stringhe wearLevel e fulm che contengono i valori letti dal DB.
    Queste stringhe vengono passate al client CoAP tramite il metodo sendWearLevel, che si occuperà di resettare i valori sull'attuatore fisico.
    Dopo il reset, il client CoAP restituisce i nuovi valori di wear e fulminated in un array results.
    I nuovi valori vengono letti dall'array, convertiti nei tipi corretti (float e boolean) e stampati.
    I nuovi valori resettati vengono inseriti nel database tramite il metodo insertWearAndFulminatedResetted, passando l'ID luce e i nuovi valori.
    (Solo per debuggare) Infine, per verificare l'inserimento, viene richiamato il metodo selectAllActuatorStatus che stampa tutti i record presenti nella tabella degli attuatori.
     */

    public void callResetForLight(boolean fulminated, int lightID) throws SQLException, ConnectorException, IOException {
        if (fulminated){
            //qua mi prendo i dati di wear e fulminated dalla tabella e li mando al coap client
            String[] data = actuatorStatus.getActuatorData(lightID);
            //TODO: IL CONTROLLO SULL'ESISTENZA DELL ATTUATORE NELLA TABELLA CON IL COMANDO ASSOCIATO MANDA TUTTO A PUTTANE (DA CONTROLLARE)

            //System.out.println("[INFO] - Still no records for this actuator. RETRY!");
            //if (data[0].equals("[INFO] - Still no records for this actuator. RETRY!")){
            //    lightID = UserMenu.askForLightId(); // richiama la funzione di input
            //    data = actuatorStatus.getActuatorData(lightID); // riprova
            //}else {
                for (String value : data) {
                    System.out.println("Wear and fulminated  presi da tabella attuatore");
                    System.out.println(value);
                }
                String wearLevel = data[0];
                String fulm = data[1];
                System.out.println("wearLevel prima di essere mandato al coap:" + wearLevel);
                System.out.println("fulminated prima di essere mandato al coap:" + fulm);

                //fulminated viene mandato come 0/1
                //qua mando al coap nel quale poi viene gestito tramite obs e bottone (vedi light-node.c)
                coapClient.sendWearLevel(address, wearLevel, fulm);
                System.out.println("Wear leve per la luce " + lightID + " >  " + MAX_WEAR_LEVEL);
                String[] results = coapClient.getWearAndFulminatedFromActuator(address);
                System.out.println("Risultati resettati presi da COAP freschissimi");
                for (String value : results) {
                    System.out.println(value);
                }
                float wear = Float.parseFloat(results[0]);
                boolean fulmin = Boolean.parseBoolean(results[1]);
                System.out.println("Wear resettato: " + wear);
                System.out.println("Fulminated resettato: " + fulmin);
                actuatorStatus.insertWearAndFulminatedResetted(lightID, wear, fulmin);
                System.out.println("INSERIMENTO DEI NUOVI DATI AVVENUTO");
                actuatorStatus.selectAllActuatorStatus();
            }
        //}
    }



    /*
    Viene letto il wearLevel corrente per la luce tramite il client CoAP chiamando il metodo getWearLevel.
    Si controlla se il wearLevel è superiore alla soglia massima MAX_WEAR_LEVEL.
    Se è superiore, viene chiamato il metodo callResetForLight passando true come parametro per indicare che la lampadina è fulminata.
    Il metodo callResetForLight recupera i dati di wear e fulminated per quella luce dal database tramite la classe ActuatorStatus.
    Questi dati vengono inviati al client CoAP che si occuperà di resettarli sull'attuatore tramite il metodo sendWearLevel.
    Dopo il reset, il client CoAP ritorna i nuovi valori di wear e fulminated che vengono stampati.
    I nuovi valori resettati vengono inseriti nel database tramite il metodo insertWearAndFulminatedResetted di ActuatorStatus.
    Se invece il wearLevel non supera la soglia, si procede ad accendere/spegnere la luce in base al suo stato corrente e al valore di fulminated, inviando una richiesta PUT al client CoAP.
    Dopo aver acceso/spento, si legge lo stato corrente dal client CoAP e lo si inserisce nel database insieme agli altri dati tramite il metodo insertActuatorData.
    Il metodo callResetForLight viene chiamato solo se il wearLevel supera la soglia
    In entrambi i casi (reset o no) i nuovi dati vengono inseriti nel database

     */
    public void setLight() throws ConnectorException, IOException, SQLException {
        boolean fulminated = false;
        //Qua mi prendo il wearLevel di una determinata luce getWearLevel calcola l'usura prendendo il contatore inserito in lighthdata
        Double wearLevelreceived = coapClient.getWearLevel(light);
        //TODO: PER ATTIVARE LA PARTE DI INOLTRO A COAP PER OBS E USO TASTO SBLOCCARE LE 3 RIGHE DI SOTTO
        //if (wearLevelreceived >= MAX_WEAR_LEVEL){
        //    callResetForLight(true, light);
        //}else{
            System.out.println("wear level ricevuto: " + wearLevelreceived);
            String command;
            String status = lightData.getLightStatus(light);
            System.out.println("status: " + status);
            System.out.println("fulminated: " + false);

            if (status.equals("ON")) {
                command = "OFF";
                System.out.println("[INFO] - Sending PUT request (OFF) to Lights");
                coapClient.putLightsOff(address, command);
            } else if (fulminated == true) {
                System.out.println("[INFO] - Cannot turn on lights, it's gone!");
            } else if (status.equals("OFF")) {
                command =  "ON";
                System.out.println("[INFO] - Sending PUT request (ON) to Lights");
                coapClient.putLightsOn(address, command);
            }
            try {
                String newStatus = coapClient.getLightsOnOff(address);
                actuatorStatus.insertActuatorData(
                        light,
                        newStatus,
                        null,
                        wearLevelreceived,
                        false
                );
            } catch (ConnectorException e) {
                e.printStackTrace();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    //}
}