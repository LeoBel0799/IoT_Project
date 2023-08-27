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

    private static final double MAX_WEAR_LEVEL = 20.0;
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
                //QUA MI PRENDO I WEAR LEVEL E FULMINATED RESETTATI
                String[] results = coapClient.getWearAndFulminatedFromActuator(address);
                System.out.println("Risultati resettati presi da COAP freschissimi");
                for (String value : results) {
                    System.out.println(value);
                }
                float wear = Float.parseFloat(results[0]);
                boolean fulmin = Boolean.parseBoolean(results[1]);
                int count = Integer.parseInt(results[2]);

                System.out.println("Wear resettato: " + wear);
                System.out.println("Fulminated resettato: " + fulmin);
                System.out.println("Counter resettato: " + count);
                actuatorStatus.insertWearAndFulminatedResetted(actuatorID, wear, fulmin,count);
                System.out.println("INSERIMENTO DEI NUOVI DATI AVVENUTO");
                actuatorStatus.selectAllActuatorStatus();
    }



    public void setInitialStateofLight() throws SQLException {
        int counter = actuatorStatus.getCounterForActuator(light);
        int finalnewCounter = counter;
        int updCounter = 0;
        double newWearLevel = 0.0;
            String command;
            String status = lightData.getLightStatus(light);
            if (status.equals("ON")) {
                command = "OFF";
                updCounter = finalnewCounter++;
                newWearLevel = coapClient.calculateWearFromCounter(updCounter);
                System.out.println("[INFO] - Sending PUT request (OFF) to Lights");
                coapClient.putLightsOff(address, command);
            } else {
                    command =  "ON";
                    updCounter = finalnewCounter++;
                    newWearLevel = coapClient.calculateWearFromCounter(updCounter);
                    System.out.println("[INFO] - Sending PUT request (ON) to Lights");
                    coapClient.putLightsOn(address, command);
            }
            try {
                String newStatus = coapClient.getLightsOnOff(address);
                String newBrightStatus = coapClient.getBrightsOnOff(address);
                actuatorStatus.insertActuatorData(
                        light,
                        updCounter,
                        newStatus,
                        newBrightStatus,
                        newWearLevel,
                        false
                );
            } catch (ConnectorException e) {
                e.printStackTrace();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    //}




    public void setLightBasedOnActuatorsData(int actuatorID) throws SQLException {
        String lightStatus = actuatorStatus.getLightStatusFromActuator(actuatorID);
        Double wearLevel = actuatorStatus.getWearLevelromActuator(actuatorID);
        int counter = actuatorStatus.getCounterForActuator(light);
        int finalnewCounter = counter;
        int updCounter = 0;
        double newWearLevel = 0.0;
        //if (wearLevel > MAX_WEAR_LEVEL){
        //     Boolean fulminated = actuatorStatus.setFulminatedStatus(actuatorID);
        //TODO: DA TESTARE
             //callResetForLight(wearLevel,fulminated,counter)
        //} else {
        String command;
        if (lightStatus.equals("ON")) {
            command = "OFF";
            updCounter = finalnewCounter++;
            newWearLevel = coapClient.calculateWearFromCounter(updCounter);
            System.out.println("[INFO] - Sending PUT request (OFF) to Lights");
            coapClient.putLightsOff(address, command);
        } else {
            command = "ON";
            updCounter = finalnewCounter++;
            newWearLevel = coapClient.calculateWearFromCounter(updCounter);
            System.out.println("[INFO] - Sending PUT request (ON) to Lights");
            coapClient.putLightsOn(address, command);
        }
        try {
            String newStatus = coapClient.getLightsOnOff(address);
            String brightStatus = coapClient.getBrightsOnOff(address);
            actuatorStatus.insertActuatorData(
                    actuatorID,
                    updCounter,
                    newStatus,
                    brightStatus,
                    newWearLevel,
                    false
            );
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}