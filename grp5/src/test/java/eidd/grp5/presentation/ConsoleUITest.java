package eidd.grp5.presentation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.model.Reservation;
import eidd.grp5.repository.ReservationRepository;
import eidd.grp5.repository.RoomRepository;
import eidd.grp5.repository.UserRepository;
import eidd.grp5.service.ReservationService;
import eidd.grp5.service.RoomService;
import eidd.grp5.service.UserService;

class ConsoleUITest {

    @Test
    void shouldCreateAndListReservationFromConsoleFlow() {
        String input = String.join("\n",
                "2", "Alice", "alice@mail.com",
                "4", "Room A", "20", "Salle principale",
                "6", "1", "1", "2026-03-26T14:00", "2026-03-26T15:00",
                "7",
            "10") + "\n";

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
            "11",
                "6",
                "2", "", "john@mail.com",
                "4", "Salle TP", "abc", "description",
                "3",
                "5",
                "7",
            "10") + "\n";

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
            "10") + "\n";

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
            "10") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("La date de fin doit etre apres la date de debut."));
    }

    @Test
    void shouldPrintStatsAndExitMessage() {
        String input = String.join("\n",
                "1",
            "10") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("Utilisateurs : 0"));
        assertTrue(output.contains("Salles       : 0"));
        assertTrue(output.contains("Reservations : 0"));
        assertTrue(output.contains("Fermeture de la vue console..."));
    }

    @Test
    void shouldRejectRoomWithNonPositiveCapacity() {
        String input = String.join("\n",
                "4", "Room D", "0", "Desc",
            "10") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("La capacite doit etre > 0."));
    }

    @Test
    void shouldRejectRoomWhenRequiredFieldsAreMissing() {
        String input = String.join("\n",
                "4", "", "", "Description ignoree",
            "10") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("Nom et capacite sont obligatoires."));
    }

    @Test
    void shouldListUsersAndRoomsFromMenuWhenDataExists() {
        String input = String.join("\n",
                "2", "Frank", "frank@mail.com",
                "4", "Room G", "30", "Grande salle",
                "3",
                "5",
            "10") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("--- Utilisateurs ---"));
        assertTrue(output.contains("Frank | frank@mail.com"));
        assertTrue(output.contains("--- Salles ---"));
        assertTrue(output.contains("Room G | cap=30 | Grande salle"));
    }

    @Test
    void shouldRejectReservationWhenUserOrRoomDoesNotExist() {
        String input = String.join("\n",
                "2", "Dan", "dan@mail.com",
                "4", "Room E", "6", "Desc",
                "6", "99", "99", "2026-03-26T10:00", "2026-03-26T11:00",
            "10") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("Utilisateur ou salle introuvable."));
    }

    @Test
    void shouldShowReservationConflictErrorFromService() {
        String input = String.join("\n",
                "2", "Eve", "eve@mail.com",
                "4", "Room F", "12", "Desc",
                "6", "1", "1", "2026-03-26T10:00", "2026-03-26T11:00",
                "6", "1", "1", "2026-03-26T10:30", "2026-03-26T11:30",
            "10") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("Reservation creee avec l'ID: 1"));
        assertTrue(output.contains("Impossible de creer la reservation: Room is not available for the requested time slot"));
    }

    @Test
    void shouldListReservationEvenWhenFieldsAreNull() {
        // On utilise explicitement la mémoire ici aussi
        UserService userService = new UserService(new UserRepository());
        RoomService roomService = new RoomService(new RoomRepository());
        ReservationRepository reservationRepository = new ReservationRepository();
        ReservationService reservationService = new ReservationService(reservationRepository);

        Reservation reservation = new Reservation();
        reservationRepository.save(reservation);

        ConsoleUI ui = new ConsoleUI(userService, roomService, reservationService);
        String input = String.join("\n",
                "7",
            "10") + "\n";

        String output = runConsoleSession(ui, input);

        assertTrue(output.contains("--- Reservations ---"));
        assertTrue(output.contains("client=-"));
        assertTrue(output.contains("salle=-"));
        assertTrue(output.contains("debut=-"));
        assertTrue(output.contains("fin=-"));
        assertTrue(output.contains("statut=-"));
    }

    @Test
    void shouldHandleReservationManagementMenuFlows() {
        String input = String.join("\n",
                "2", "Alice", "alice@mail.com",
                "4", "Room A", "10", "Desc",
                "6", "1", "1", "2026-03-26T10:00", "2026-03-26T11:00",
                "8",
                "2", "invalid",
                "3", "RES-001",
                "4", "1",
                "5", "1",
                "1", "1",
                "6",
            "10") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("Statut invalide."));
        assertTrue(output.contains("Reservation non trouvee.")); // Pour la recherche par REF inexistante
        assertTrue(output.contains("Reservation confirmee."));
        assertTrue(output.contains("Reservation annulee."));
        assertTrue(output.contains("--- Reservations du client ==="));
    }

    @Test
    void shouldHandleAvailabilityMenuBranches() {
        String input = String.join("\n",
                "2", "Bob", "bob@mail.com",
                "4", "Room B", "8", "Desc",
                "6", "1", "1", "2026-03-26T10:00", "2026-03-26T11:00",
                "9",
                "1", "1", "2026-03-26 12:00", "2026-03-26 11:00",
                "1", "1", "2026-03-26 10:30", "2026-03-26 10:45",
                "1", "1", "2026-03-26 11:30", "2026-03-26 12:00",
                "2", "1", "2026-03-26 12:00", "2026-03-26 12:30",
                "2", "1", "2026-03-26 10:30", "2026-03-26 10:45",
                "3", "1", "2026-03-26 09:00", "2026-03-26 12:00",
                "4",
            "10") + "\n";

        String output = runConsoleSession(input);

        assertTrue(output.contains("La fin doit etre apres le debut."));
        assertTrue(output.contains("La salle est OCCUPEE"));
        assertTrue(output.contains("La salle est DISPONIBLE"));
        assertTrue(output.contains("Aucun conflit pour cette periode."));
        assertTrue(output.contains("--- Reservations en conflit ==="));
        assertTrue(output.contains("--- Taux d'occupation ==="));
    }

    /**
     * CORRECTION CRUCIALE : On n'utilise plus ConsoleUI.createDefault() ici.
     * On crée manuellement une instance avec des dépôts en mémoire pour les tests.
     */
    private String runConsoleSession(String input) {
        UserRepository userRepo = new UserRepository();
        RoomRepository roomRepo = new RoomRepository();
        ReservationRepository resRepo = new ReservationRepository();
        
        ConsoleUI ui = new ConsoleUI(
            new UserService(userRepo),
            new RoomService(roomRepo),
            new ReservationService(resRepo)
        );
        return runConsoleSession(ui, input);
    }

    private String runConsoleSession(ConsoleUI ui, String input) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
            ui.start();
            return outputStream.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }
}