package eidd.grp5.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import eidd.grp5.model.Room;

class JsonRoomRepositoryTest {

    @Test
    void shouldSaveAndLoadRoom() {
        JsonRoomRepository repo = new JsonRoomRepository();
        Room room = new Room(101, "Salle A1", 20, "Salle de test");
        
        Room saved = repo.save(room);
        assertNotNull(saved.getId());
        
        var found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Salle A1", found.get().getName());
        
        repo.delete(saved.getId());
    }
}
