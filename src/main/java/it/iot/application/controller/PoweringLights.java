package it.iot.application.controller;

import it.iot.application.DB.LightData;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;

public class PoweringLights implements Runnable {

    private static LightBrightStatus coapClient = null;

    static {
        try {
            coapClient = new LightBrightStatus();
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final double MAX_WEAR_LEVEL = 5.0;
    int light;
    String address;
    LightData lightData;

    public PoweringLights(int lightid, String addr) {
        light= lightid;
        address = addr;
    }


    @Override
    public void run() {
        boolean fulminated = false;
        Double wearLevelreceived = coapClient.getWearLevel(light);
        String res;
        if (wearLevelreceived > MAX_WEAR_LEVEL){
             fulminated= true;
        }
        String status = lightData.getLightStatus(light);
        if (status.equals("ON") && fulminated == true) {
            res = "{\"mode\": \"OFF\"}";
            System.out.println("[!] Sending PUT request (OFF) to Lights");
            coapClient.putLightsOff(address, res);
        } else if (status.equals("ON")) {
            System.out.println("[!] Lights are already on");
        } else if (status.equals("OFF") && fulminated == true) {
            System.out.println("[!] Cannot turn on lights, it's gone!");
        } else if (status.equals("OFF")) {
            res = "{\"mode\": \"ON\"}";
            System.out.println("[!] Sending PUT request (OFF) to Lights");
            coapClient.putLightsOn(address, res);
        }
    }
}