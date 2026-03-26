package eidd.grp5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.app.App;

public class AppTest {
    @Test
    void testApp() {
    	assertTrue(false);
    }

    @Test
    void shouldPrintWelcomeMessageInMain() {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        System.setIn(new ByteArrayInputStream("8\n".getBytes()));
        try {
            App.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        String output = outputStream.toString();
        assertTrue(output.contains("Welcome to the EIDD Group 5 Application!"));
    }
}
