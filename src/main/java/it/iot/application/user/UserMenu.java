package it.iot.application.user;

import it.iot.application.DB.ActuatorStatus;
import it.iot.application.DB.LightData;
import it.iot.application.DB.NodeData;
import it.iot.application.controller.PoweringBrights;
import it.iot.application.controller.PoweringLights;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserMenu implements Runnable {

    final int EXIT = 1;
    final int OPTION_TURN_ON_LIGHT = 2;
    final int OPTION_TURN_ON_BRIGHT = 3;
    final int SHOW_NODES = 4;
    final int SHOW_ACTUATORS = 5;
    final int SHOW_MOTION = 6;

    private static Scanner input;
    static LightData lightData;
    static NodeData nodeData;
    static ActuatorStatus actuatorStatus;

    static {
        try {
            lightData = new LightData();
            nodeData = new NodeData();
            actuatorStatus = new ActuatorStatus();
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        input = new Scanner(System.in);
        try {
            actuatorStatus.createDelete("actuator");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            menu();
        } catch (ConnectorException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }



    public void showMenu(){
        System.out.println("+++++++++++++++++++++++++++++++++");
        System.out.println("         Car Controller  (User)  ");
        System.out.println("+++++++++++++++++++++++++++++++++");
        System.out.println("(1) Exit controller");
        System.out.println("(2) Handle Lights");
        System.out.println("(3) Handle Brights");
        System.out.println("(4) View Node records");
        System.out.println("(5) View Actuator records");
        System.out.println("(6) View Light status records");
        System.out.println("Insert a command: ");
    }


    public void menu() throws ConnectorException, IOException, InterruptedException, SQLException {
        boolean shouldExit = false;
        Scanner input = new Scanner(System.in);
        while (!shouldExit){
            showMenu();
            //TESTATO TRY CATCH PER INPUTMISMATCH E FUNGE
            //Il thread viene eseguito una volta sola per tutta la durata del programma cosi il primo comando l'attuatore lo fa in base a ciò che manda MQTT
            //poi le azioni che fa l'utente sono tutte basate sulle sue azioni precedenti quindi basondosi sulle azioni fatte sull'attuatore
            try{
                int choice = input.nextInt();
                switch (choice) {
                    case OPTION_TURN_ON_LIGHT:
                        int lightId = askForLightId();
                        final boolean[] setLightExecuted = {false};
                        if (!setLightExecuted[0] && lightData.lightExists(lightId)) {
                             final PoweringLights lights = new PoweringLights(lightId, lightData, nodeData, actuatorStatus);
                            // Creazione di un pool di thread con un solo thread
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            // Esecuzione della funzione setLight() nel thread
                            executor.submit(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        lights.setInitialStateofLight(); // Chiamata alla funzione setLight()
                                        setLightExecuted[0] = true; // Imposta la variabile a true dopo l'esecuzione
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            // Chiusura del pool di thread dopo che la funzione è stata eseguita
                            executor.shutdown();
                        } else if (setLightExecuted[0]) {
                            PoweringLights lights = new PoweringLights(lightId, lightData, nodeData, actuatorStatus);
                            lights.setLightBasedOnActuatorsData(lightId);
                        } else {
                            System.out.println("[INFO] - No Light status records with ID: " + lightId + ", still arrived!");
                        }
                        break;


                    case OPTION_TURN_ON_BRIGHT:
                        //TODO: TESTARE BRIGHT
                        int brightId = askForLightId();
                        if (lightData.lightExists(brightId)){
                            PoweringBrights brights = new PoweringBrights(brightId, actuatorStatus,  nodeData );
                            brights.setBright(brightId);
                        }else{
                            System.out.println("[INFO] - No Light status records with ID: " + brightId + ", still arrived!");
                        }
                        break;

                    case SHOW_MOTION:
                        //VERIFICATO FUNGE
                        List<String> motions = lightData.selectAllMotion();
                        if(motions.isEmpty()) {
                            System.out.println("[INFO] - No Light status records found. Try again in a few!");
                        } else {
                            System.out.println("\n *************LIGHT STATUS RECORDS*************");
                            for(String row : motions) {
                                System.out.println(row);
                            }
                        }
                        break;

                    case SHOW_ACTUATORS:
                        //VERIFICATO FUNGE
                        List<String> actuators = actuatorStatus.selectAllActuatorStatus();
                        if(actuators.isEmpty()){
                            System.out.println("[INFO] - No records about Actuator actions found. Try again after some transactions on Lights!");
                        } else {
                            System.out.println("\n *************ACTIONS ON LIGHTS WITH ACTUATORS*************");
                            for (String row: actuators){
                                System.out.println(row);
                            }
                        }
                        break;

                    case SHOW_NODES:
                        //VERIFICATO FUNGE
                        List<String> nodes = nodeData.selectAllNode();
                        if (nodes.isEmpty()){
                            System.out.println("[INFO] - No Acutators records found. Try again in a few!");

                        }else {
                            System.out.println("\n *************ACTUATORS RECORDS*************");
                            for (String row: nodes){
                                System.out.println(row);
                            }
                        }

                        break;

                    case EXIT:
                        //VERIFICATO FUNGE
                        System.out.println("BYE");
                        shouldExit = true;
                        System.exit(0);
                        break;


                    default:
                        System.out.println("[FAIL] - Invalid option");
                        input.nextLine();//svoto buffer
                }
            } catch (InputMismatchException e) {
                System.out.println("[FAIL] - Insert numbers not characters");
                input.nextLine();
            }
        }
    }




    public static int askForLightId() {
        int id;
        while(true) {
            //VERIFICATO FUNGE
            try {
                System.out.print("Insert light id (1-2): ");
                id = input.nextInt();
                if(isValidLightId(id)) {
                    break;
                } else {
                    System.out.println("[INFO] - Invalid light id. Please retry.");
                }
            } catch (InputMismatchException e) {
                System.out.println("[FAIL] - Insert numbers not characters");
                input.nextLine();
            }

        }
        return id;
    }

    static boolean isValidLightId(int id) {
        return id >= 1 && id <=2;
    }

}