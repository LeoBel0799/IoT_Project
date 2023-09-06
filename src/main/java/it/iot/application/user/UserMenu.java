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
    final int BOOTSTRAP_LIGHTS = 2;
    final int OPTION_TURN_ON_LIGHT = 3;
    final int OPTION_TURN_ON_BRIGHT = 4;
    final int SHOW_NODES = 5;
    final int SHOW_ACTUATORS = 6;
    final int SHOW_MOTION = 7;
    boolean light1Bootstrapped = false;
    boolean light2Bootstrapped = false;
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
        System.out.println("(2) Bootstrap Lights");
        System.out.println("(3) Handle Lights");
        System.out.println("(4) Handle Brights");
        System.out.println("(5) View Node records");
        System.out.println("(6) View Actuator records");
        System.out.println("(7) View Light status records");
        System.out.println("Insert a command: ");
    }


    public void menu() throws ConnectorException, IOException, InterruptedException, SQLException {
        boolean shouldExit = false;
        Scanner input = new Scanner(System.in);
        boolean optionOneExecuted = false; // Inizializza la variabile all'inizio del
        boolean lightExecuted = false;
        while (!shouldExit){
            showMenu();
            try{
                int choice = input.nextInt();
                switch (choice) {
                    /*
                    Caso inserito per evitare che possa l'utente scegliere il caso 3 quindi che ragiona sulla tabella "In-live" senza che questa tabella del DB
                    sia stata popolata almeno una volta. Quindi l'utente deve per forza scegliere questo caso prima con una luce e poi con l'altra così da mettere il primo
                    record nella tabella actuator. Dopo che le luci sono state inserite per la prima volta questa opzione di menu non sarà più eseguibile.
                     */
                    case BOOTSTRAP_LIGHTS:
                        //VERIFICATO FUNGE
                        if (light1Bootstrapped && light2Bootstrapped) {
                            System.out.println("[INFO] - Light registered. Option (2) not available anymore.");
                        } else {
                            int lightId = askForLightId();

                            if (lightData.lightExists(lightId)) {
                                PoweringLights lights = new PoweringLights(lightId, lightData, nodeData, actuatorStatus);
                                lights.setInitialStateofLight();
                            } else {
                                System.out.println("[INFO] - No Light status records with ID: " + lightId + ", still arrived!");
                            }

                            optionOneExecuted = true;
                            lightData.setBootstrappedStatus(lightId, true);

                            if (lightId == 1) {
                                light1Bootstrapped = true;
                            } else if (lightId == 2) {
                                light2Bootstrapped = true;
                            }
                        }
                        break;

                    /*CASO LIGHT FUNZIONANTE (Quando la luce è on il LED dovrebbe essere rosso invece si accende verde su cooja mentre quando è spento dovrebbe essere giallo ma è blu su cooja)
                    vedendo la console dell'applicativo combacia VEDE OFF - MANDA ON AL SENSORE - GET DAL SENSORE.*/
                    case  OPTION_TURN_ON_LIGHT:
                        //VERIFICATO FUNGE
                        if (optionOneExecuted){
                            int askForLightId = askForLightId();
                            if (lightData.getBootstrappedStatus(askForLightId)){
                                if (lightData.lightExists(askForLightId)){
                                    PoweringLights lights = new PoweringLights(askForLightId, lightData, nodeData, actuatorStatus);
                                    lights.setLightBasedOnActuatorsData(askForLightId);
                                }else{
                                    System.out.println("[INFO] - No Light status records with ID: " + askForLightId + ", still arrived!");
                                }
                                lightExecuted = true;
                            }else {
                                System.out.println("[INFO] - Light " + askForLightId + " was not bootstrapped!");
                            }
                        } else {
                            System.out.println("[INFO] - Option (2) must be executed first to turn on at least on light!");
                        }

                        break;

                    case OPTION_TURN_ON_BRIGHT:
                        if (optionOneExecuted ){
                            int brightId = askForLightId();
                            if (lightData.lightExists(brightId)) {
                                PoweringBrights brights = new PoweringBrights(brightId, actuatorStatus, nodeData);
                                brights.setBright(brightId);
                            } else {
                                System.out.println("[INFO] - No Light status records with ID: " + brightId + ", still arrived!");
                            }
                        } else {
                            System.out.println("[INFO] - Option one must be executed first to turn on at least on light!");
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