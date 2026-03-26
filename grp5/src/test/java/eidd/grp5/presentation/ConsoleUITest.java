package eidd.grp5.presentation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ConsoleUITest {

    @Test
    void shouldCreateAndListReservationFromConsoleFlow() {
        String input = String.join("\n",
                "2", "Alice", "alice@mail.com",
                "4", "Room A", "20", "Salle principale",
                "6", "1", "1", "2026-03-26T14:00", "2026-03-26T15:00",
                "7",
                "8") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("Utilisateur cree avec l'ID: 1"));
        assertTrue(output.contains("Salle creee avec l'ID: 1"));
        assertTrue(output.contains("Reservation creee avec l'ID: 1"));
        assertTrue(output.contains("--- Reservations ---"));
        assertTrue(output.contains("client=Alice"));
        assertTrue(output.contains("salle=Room A"));
    }

    @Test
    void shouldShowBasicValidationMessages() {
        String input = String.join("\n",
                "9",
                "6",
                "2", "", "john@mail.com",
                "4", "Salle TP", "abc", "description",
                "3",
                "5",
                "7",
                "8") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("Choix invalide. Reessaie."));
        assertTrue(output.contains("Ajoute d'abord au moins un utilisateur et une salle."));
        assertTrue(output.contains("Nom et email sont obligatoires."));
        assertTrue(output.contains("Capacite invalide."));
        assertTrue(output.contains("Aucun utilisateur enregistre."));
        assertTrue(output.contains("Aucune salle enregistree."));
        assertTrue(output.contains("Aucune reservation enregistree."));
    }

    @Test
    void shouldRetryOnInvalidIdsAndDateFormats() {
        String input = String.join("\n",
                "2", "Bob", "bob@mail.com",
                "4", "Room B", "10", "Desc",
                "6", "x", "1", "y", "1", "bad-date", "2026-03-26 14:00", "oops", "2026-03-26 15:00",
                "8") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("Valeur invalide, entre un nombre entier."));
        assertTrue(output.contains("Format invalide. Exemples: 2026-04-01T10:00 ou 2026-04-01 10:00"));
        assertTrue(output.contains("Date de debut enregistree."));
        assertTrue(output.contains("Reservation creee avec l'ID: 1"));
    }

    @Test
    void shouldRejectReservationWhenEndIsBeforeStart() {
        String input = String.join("\n",
                "2", "Carol", "carol@mail.com",
                "4", "Room C", "8", "Desc",
                "6", "1", "1", "2026-03-26T15:00", "2026-03-26T14:00",
                "8") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("La date de fin doit etre apres la date de debut."));
    }

    private String runConsoleSession(String input) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
            ConsoleUI.createDefault().start();
            return outputStream.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }
}
