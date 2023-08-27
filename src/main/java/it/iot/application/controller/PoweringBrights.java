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

        }
    }

    public void setBright(int lightID) throws SQLException {
        String res;
        String status = actuatorStatus.getBrightStatusFromActuator(lightID);

        System.out.println("DOPO la query: "+ status);
        if (status.equals("ON")) { //se la luce con quell'id è accesa posso accendere anche gli abbaglianti
            res = "OFF";
            System.out.println("[!] Sending PUT request (OFF) to Brights");
            coapClient.putBrightsOff(address, res);
        } else  {
            res = "ON";
            System.out.println("[!] Sending PUT request (ON) to Brights");
            coapClient.putBrightsOn(address, res);
        }
        try {

            //l'usura per gli abbaglianti non è prevista, si aggancia a light. qua registstro solo il nuovo stato di bright
            String newStatus = LightBrightStatus.getInstance().getBrightsOnOff(address);
            actuatorStatus.insertActuatorData(
                    lightID,
                    actuatorStatus.getCounterForActuator(lightID),
                    actuatorStatus.getLightStatusFromActuator(lightID),
                    newStatus,
                    actuatorStatus.getWearLevelromActuator(lightID),
                    actuatorStatus.getFulminatedFromActuator(lightID));
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
