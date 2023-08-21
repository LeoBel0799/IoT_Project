package it.iot.application.controller;

import it.iot.application.DB.ActuatorStatus;
import it.iot.application.DB.NodeData;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.SQLException;

public class PoweringBrights {
    private  LightBrightStatus coapClient;
    private ActuatorStatus actuatorStatus;
    NodeData nodeData;
    int light;
    String address;


    public PoweringBrights(int lightId, ActuatorStatus actuatorStatus, NodeData nodeData) {
        this.nodeData = nodeData;
        this.actuatorStatus = actuatorStatus;
        String ipv6 = nodeData.getIPv6(lightId);
        this.address = ipv6;
        this.light= lightId;
        try {
            this.coapClient = new LightBrightStatus();
        } catch (Exception e) {
            // gestisci eccezioni
        }
    }

    public void setBright() {
        String res;
        String status = null;
        try {
            status = LightBrightStatus.getInstance().getLightsOnOff(address);
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String brightsStatus;
        if(status.equals("ON")) {
            brightsStatus = "ON";
        } else {
            brightsStatus = "OFF";
        }

        if (brightsStatus.equals("ON")) {
            res = "{\"mode\": \"ON\"}";
            System.out.println("[!] Sending PUT request (ON) to Brights");
            coapClient.putBrightsOn(address, res);
        } else  {
            res = "{\"mode\": \"OFF\"}";
            System.out.println("[!] Sending PUT request (OFF) to Brights");
            coapClient.putBrightsOff(address, res);
        }
        try {
            String newStatus = LightBrightStatus.getInstance().getLightsOnOff(address);
            actuatorStatus.insertActuatorData(
                    light,
                    null,
                    newStatus,
                    null,
                    null
            );
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
