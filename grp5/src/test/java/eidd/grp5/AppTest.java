package eidd.grp5;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.app.App;

public class AppTest {
    @Test
    void testApp() {
        assertTrue(true);
    }

    @Test
    void shouldPrintWelcomeMessageInMain() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        try {
            App.main(new String[0]);
        } finally {
            System.setOut(originalOut);
        }

        String output = outputStream.toString();
        assertTrue(output.contains("Welcome to the EIDD Group 5 Application!"));
    }
}
