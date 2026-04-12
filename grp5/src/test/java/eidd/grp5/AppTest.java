package eidd.grp5;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.app.App;

class AppTest {
    @Test
    void testApp() {
        assertTrue(true);
    }

    @Test
    void shouldPrintWelcomeMessageInMain() {
        InputStream originalIn = System.in;
        Logger appLogger = Logger.getLogger("eidd.grp5.app.App");
        List<String> messages = new ArrayList<>();
        Handler testHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                messages.add(record.getMessage());
            }

            @Override
            public void flush() {
                // Nothing to flush for in-memory capture.
            }

            @Override
            public void close() {
                // Nothing to close for in-memory capture.
            }
        };
        testHandler.setLevel(Level.ALL);
        Level originalLevel = appLogger.getLevel();
        appLogger.addHandler(testHandler);
        appLogger.setLevel(Level.ALL);
        System.setIn(new ByteArrayInputStream("10\n".getBytes()));
        try {
            App.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            appLogger.removeHandler(testHandler);
            appLogger.setLevel(originalLevel);
        }

        assertTrue(messages.contains("Welcome to the EIDD Group 5 Application!"));
    }
}
