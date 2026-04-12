package eidd.grp5.app;

import java.util.logging.Logger;

import eidd.grp5.presentation.ConsoleUI;

public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Welcome to the EIDD Group 5 Application!");
        ConsoleUI.createDefault().start();
    }
}
