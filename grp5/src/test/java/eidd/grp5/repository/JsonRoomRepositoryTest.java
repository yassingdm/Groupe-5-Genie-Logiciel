package eidd.grp5.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import eidd.grp5.model.Room;

class JsonRoomRepositoryTest {
    @Test
    void shouldSaveAndFindRoom() {
        JsonRoomRepository repo = new JsonRoomRepository();
        Room room = new Room(101, "Salle Polyvalente", 50, "Grande salle");
        
        Room saved = repo.save(room);
        assertNotNull(saved.getId());
        
        assertTrue(repo.findById(saved.getId()).isPresent());
        assertEquals("Salle Polyvalente", repo.findById(saved.getId()).get().getName());
        
        repo.delete(saved.getId());
    }
}
