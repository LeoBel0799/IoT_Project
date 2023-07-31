package it.iot.remote;

import it.iot.remote.server.Server;


public class MainClass {
    public static void main(String[] args) {
        // start thread for registration server execution
        Server server = new Server();
        Thread th = new Thread(server);
        th.start();

        // start thread for user command listener
        /*UserInputController userInputController = new UserInputController();
        Thread userInputControllerThread = new Thread(userInputController);
        userInputControllerThread.start();*/
    }
}