package it.iot.remote;

import it.iot.remote.server.Server;
import it.iot.remote.user.UserMenu;


public class MainClass {
    public static void main(String[] args) {
        // start thread for registration server execution
        Server server = new Server();
        Thread th = new Thread(server);
        th.start();

        // thread for user menu
        UserMenu userMenu = new UserMenu();
        Thread menu = new Thread(userMenu);
        menu.start();


        //thread for operator menu
    }
}