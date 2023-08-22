package it.iot.application.controller;

import it.iot.application.DB.ActuatorStatus;
import it.iot.application.DB.LightData;
import it.iot.application.DB.NodeData;
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


    public void setLight() throws ConnectorException, IOException, SQLException {
        boolean fulminated = false;
        //Qua mi prendo il wearLevel di una determinata luce getWearLevel calcola l'usura prendendo il contatore inserito in lighthdata
        Double wearLevelreceived = coapClient.getWearLevel(light);
        System.out.println("wear level ricevuto: " + wearLevelreceived);
        String res;
        //se il wearlevel di quella luce supera la soglia massima
        //dall'attuatore mi prendo i dati resettati tramite bottone e li rimetto nel DB
        //facendo l'update di wear e fulminated per quello stesso ID
        if (wearLevelreceived > MAX_WEAR_LEVEL){
             fulminated= true;
             String[] results = coapClient.getWearAndFulminatedFromActuator(address);
            float wear = Float.parseFloat(results[0]);
            boolean fulm = Boolean.parseBoolean(results[1]);
            actuatorStatus.insertWearAndFulminatedResetted(light,wear,fulm);
        }
        String status = lightData.getLightStatus(light);
        System.out.println("status: " + status);
        System.out.println("fulminated: " + fulminated);

        if (status.equals("ON") && fulminated == true) {
            res = "{\"OFF\"}";
            System.out.println("[INFO] - Sending PUT request (OFF) to Lights");
            coapClient.putLightsOff(address, res);
        } else if (status.equals("ON")) {
            res = "{\"OFF\"}";
            System.out.println("[INFO] - Sending PUT request (OFF) to Lights");
            coapClient.putLightsOff(address, res);
        } else if (fulminated == true) {
            System.out.println("[INFO] - Cannot turn on lights, it's gone!");
        } else if (status.equals("OFF")) {
            res =  "{\"ON\"}";
            System.out.println("[INFO] - Sending PUT request (ON) to Lights");
            coapClient.putLightsOn(address, res);
        }
        try {
            String newStatus = coapClient.getLightsOnOff(address);
            actuatorStatus.insertActuatorData(
                    light,
                    newStatus,
                    null,
                    wearLevelreceived,
                    fulminated
                        );
            //qua mi prendo i dati di wear e fulminated dalla tabella e li mando al coap client
            String[] data = actuatorStatus.getActuatorData(light);
            String wearLevel = data[0];
            String fulm = data[1];
            //fulminated viene mandato come 0/1
            //qua mando al coap nel quale poi viene gestito tramite obs e bottone (vedi light-node.c)
            coapClient.sendWearLevel(address,wearLevel,fulm);

        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

    }
}