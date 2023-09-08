package it.iot.application.controller;

import it.iot.application.DB.ActuatorStatus;
import it.iot.application.DB.LightData;
import it.iot.application.DB.NodeData;
import it.iot.application.user.UserMenu;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.SQLException;

public class PoweringLights {

    private LightBrightStatus coapClient;
    private int finalnewCounter = 0; // Variabile di istanza per memorizzare il contatore
    private double currentWearLevel = 0.0; // Variabile di istanza per memorizzare il wear level attuale


    private static final double MAX_WEAR_LEVEL = 10.0;
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


    public void callResetForLight(int actuatorID, double wearLevel, boolean fulminated, int counter) throws SQLException, ConnectorException, IOException {
        //MANDO I DATI A COOAP PER RESET TRAMITE BOTTONE NEL C
        coapClient.sendWearLevel(address, wearLevel, fulminated, counter);
        //Timer per dare il tempo di resettare la luce nel C tramite bottone
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.out.println("[FAIL] - Error in delay operation");
        }
        String[] results = coapClient.getWearAndFulminatedFromActuator(address);
        for (String value : results) {
            System.out.println(value);
        }
        float wear = Float.parseFloat(results[0]);
        boolean fulmin = Boolean.parseBoolean(results[1]);
        int count = Integer.parseInt(results[2]);

        actuatorStatus.insertWearAndFulminatedResetted(actuatorID, wear, fulmin, count);
        System.out.println("[INFO] - New light inserted...data reset in DB!");
        actuatorStatus.selectAllActuatorStatus();
    }


    public void setInitialStateofLight() throws SQLException, ConnectorException, IOException {
        int counter = 0;
        Double wear = 0.0;
        finalnewCounter = counter;
        currentWearLevel = wear;

        String command;
        String status = lightData.getLightStatus(light);
        if (status.equals("ON")) {
            command = "OFF";
            int updCounter = finalnewCounter + 1;
            double newWearLevel = coapClient.calculateWearFromCounter(updCounter);
            System.out.println("[INFO] - Sending PUT request (OFF) to Lights");
            coapClient.putLightsOff(address, command);
            String newStatus = coapClient.getLightsOnOff(address);
            String newBrightStatus = coapClient.getBrightsOnOff(address);
            actuatorStatus.insertActuatorData(light, updCounter, newStatus, newBrightStatus, newWearLevel, false);
            finalnewCounter = updCounter; // Aggiorna il contatore
            currentWearLevel = newWearLevel; // Aggiorna il wear level
        } else {
            command = "ON";
            int updCounter = finalnewCounter + 1;
            double newWearLevel = coapClient.calculateWearFromCounter(updCounter);
            System.out.println("[INFO] - Sending PUT request (ON) to Lights");
            coapClient.putLightsOn(address, command);
            String newStatus = coapClient.getLightsOnOff(address);
            String newBrightStatus = coapClient.getBrightsOnOff(address);
            actuatorStatus.insertActuatorData(light, updCounter, newStatus, newBrightStatus, newWearLevel, false);
            finalnewCounter = updCounter; // Aggiorna il contatore
            currentWearLevel = newWearLevel; // Aggiorna il wear level
        }
    }


    public void setLightBasedOnActuatorsData(int actuatorID) throws SQLException, ConnectorException, IOException {
        String lightStatus = actuatorStatus.getLightStatusFromActuator(actuatorID);
        Double wearLevel = actuatorStatus.getWearLevelromActuator(actuatorID);
        int counter = actuatorStatus.getCounterForActuator(actuatorID); // Usare actuatorID invece di "light"
        finalnewCounter = counter;
        currentWearLevel = wearLevel;

        if (wearLevel == MAX_WEAR_LEVEL){
            System.out.println("[INFO] - Reset Light " + actuatorID + " to use it. " + wearLevel + " equal treshold: " + MAX_WEAR_LEVEL + " . CRITICAL!");
        }

        if (wearLevel > MAX_WEAR_LEVEL) {
            System.out.println("[INFO] - Reset Light " + actuatorID + " to use it. " + wearLevel + " over: " + MAX_WEAR_LEVEL + " . FULMINATED!");
            actuatorStatus.setFulminatedStatus(actuatorID);
            Boolean fulminated = actuatorStatus.getFulminatedStatus(actuatorID);
            callResetForLight(actuatorID, wearLevel, fulminated, counter);
        } else {
            try {
                String command;
                if (lightStatus.equals("ON")) {
                    command = "OFF";
                    int updCounter = finalnewCounter + 1;
                    double newWearLevel = coapClient.calculateWearFromCounter(updCounter);
                    System.out.println("[INFO] - Sending PUT request (OFF) to Lights");
                    coapClient.putLightsOff(address, command);
                    String newStatus = coapClient.getLightsOnOff(address);
                    String newBrightStatus = coapClient.getBrightsOnOff(address);
                    actuatorStatus.insertActuatorData(actuatorID, updCounter, newStatus, newBrightStatus, newWearLevel, false);
                    finalnewCounter = updCounter; // Aggiorna il contatore
                    currentWearLevel = newWearLevel; // Aggiorna il wear level
                } else {
                    command = "ON";
                    int updCounter = finalnewCounter + 1;
                    double newWearLevel = coapClient.calculateWearFromCounter(updCounter);
                    System.out.println("[INFO] - Sending PUT request (ON) to Lights");
                    coapClient.putLightsOn(address, command);
                    String newStatus = coapClient.getLightsOnOff(address);
                    String newBrightStatus = coapClient.getBrightsOnOff(address);
                    actuatorStatus.insertActuatorData(actuatorID, updCounter, newStatus, newBrightStatus, newWearLevel, false);
                    finalnewCounter = updCounter; // Aggiorna il contatore
                    currentWearLevel = newWearLevel; // Aggiorna il wear level
                }
            }catch (NumberFormatException e){
                System.out.println("[INFO] - Reset light " + actuatorID + " to use it!");
            }
        }
    }
}