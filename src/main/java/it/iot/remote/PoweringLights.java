package it.iot.remote;

import java.io.IOException;

public class PoweringLights implements Runnable {

    private final static LightRemoteHandler coapClient = LightRemoteHandler.getInstance();

    String powerFlag;
    String address;

    public PoweringLights(String power, String addr) {
        powerFlag = power;
        address = addr;
    }


    @Override
    public void run() {
        String status = null;
        String res;
        try {
            status = coapClient.getLightsOnOff(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (status.equals("ON")) {
            try {
                res = "{\"mode\": \"ON\"}";
                System.out.println("[!] Sending PUT request (ON) to Lights");
                coapClient.turnLightsOn(address, res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                res = "{\"mode\": \"OFF\"}";
                System.out.println("[!] Sending PUT request (OFF) to Lights");
                coapClient.turnLightsOff(address, res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}