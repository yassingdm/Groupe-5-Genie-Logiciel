package eidd.grp5.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import eidd.grp5.model.Reservation;
import eidd.grp5.model.Room;
import java.time.LocalDateTime;

class JsonReservationRepositoryTest {

    @Test
    void shouldSaveAndLoadReservation() {
        JsonReservationRepository repo = new JsonReservationRepository();
        Room room = new Room(1, "Test", 10, "Desc");
        
        
        Reservation res = new Reservation(room, LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        res.setReference("TEST-JSON");
        
        Reservation saved = repo.save(res);
        assertNotNull(saved.getId());
        
        
        var found = repo.findByReference("TEST-JSON");
        assertTrue(found.isPresent());
        assertEquals("TEST-JSON", found.get().getReference());
        
        
        repo.delete(saved.getId());
    }
}
