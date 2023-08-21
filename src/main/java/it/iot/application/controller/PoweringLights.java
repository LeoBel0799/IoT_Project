package it.iot.application.controller;

import it.iot.application.DB.ActuatorStatus;
import it.iot.application.DB.DB;
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


    public void setLight() {
        boolean fulminated = false;
        Double wearLevelreceived = coapClient.getWearLevel(light);
        System.out.println("wear level ricevuto: " + wearLevelreceived);
        String res;
        if (wearLevelreceived > MAX_WEAR_LEVEL){
             fulminated= true;
        }
        String status = lightData.getLightStatus(light);
        System.out.println("status: " + status);
        System.out.println("fulminated: " + fulminated);

        if (status.equals("ON") && fulminated == true) {
            res = "{\"mode\": \"OFF\"}";
            System.out.println("[INFO] - Sending PUT request (OFF) to Lights");
            coapClient.putLightsOff(address, res);
        } else if (status.equals("ON")) {
            System.out.println("[INFO] - Lights are already on");
        } else if (fulminated == true) {
            System.out.println("[INFO] - Cannot turn on lights, it's gone!");
        } else if (status.equals("OFF")) {
            res = "{\"mode\": \"ON\"}";
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
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

    }
}