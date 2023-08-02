package it.iot.remote;

import it.iot.remote.user.UserMenu;


public class MainClass {
    public static void main(String[] args) {

        // thread for user menu
        UserMenu userMenu = new UserMenu();
        Thread menu = new Thread(userMenu);
        menu.start();


        //thread for operator menu
    }
}