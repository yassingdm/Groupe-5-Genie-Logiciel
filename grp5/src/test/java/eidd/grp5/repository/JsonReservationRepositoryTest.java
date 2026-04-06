package eidd.grp5.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import eidd.grp5.model.Reservation;
import eidd.grp5.model.Room;
import eidd.grp5.model.User;
import java.time.LocalDateTime;

class JsonReservationRepositoryTest {
    private static final String TEST_REF = "REF-UNIT-TEST";

    @Test
    void shouldCoverAllRepositoryMethods() {
        JsonReservationRepository repo = new JsonReservationRepository();
        Room room = new Room(1, "Salle Test", 10, "Description");
        User client = new User("Jean Test", "jean@test.com");
        client.setId(50L); 
        
        LocalDateTime start = LocalDateTime.of(2026, 5, 20, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 20, 12, 0);
        
        Reservation res = new Reservation(room, start, end);
        res.setClient(client);
        res.setReference(TEST_REF);
        res.setStatus(Reservation.Status.CONFIRMED);

        Reservation saved = repo.save(res);
        
        
        saved.setStatus(Reservation.Status.CANCELLED);
        repo.save(saved);
        
        
        assertFalse(repo.findAll().isEmpty());
        assertTrue(repo.count() > 0);
        assertTrue(repo.findByReference(TEST_REF).isPresent());
        assertFalse(repo.findByClientId(50L).isEmpty());
        assertFalse(repo.findByStatus(Reservation.Status.CANCELLED).isEmpty());

        assertTrue(repo.delete(saved.getId()));
    }
}