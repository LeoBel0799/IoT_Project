package it.iot.application.controller;

import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;

public class PoweringBrights implements Runnable{
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
    int light;
    String address;

    public PoweringBrights(int lightid, String addr) {
        light= lightid;
        address = addr;
    }

    public void run() {
        String res;
        String status = null;
        try {
            status = LightBrightStatus.getInstance().getLightsOnOff(address);
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (status.equals("ON")) {
            res = "{\"mode\": \"ON\"}";
            System.out.println("[!] Sending PUT request (ON) to Brights");
            coapClient.putBrightsOn(address, res);
        } else  {
            res = "{\"mode\": \"OFF\"}";
            System.out.println("[!] Sending PUT request (OFF) to Brights");
            coapClient.putBrightsOff(address, res);
        }
    }
}