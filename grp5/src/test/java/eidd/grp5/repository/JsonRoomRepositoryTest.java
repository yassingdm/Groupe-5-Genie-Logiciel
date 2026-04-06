package eidd.grp5.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import eidd.grp5.model.Room;

class JsonRoomRepositoryTest {
    @Test
    void shouldCoverAllRepositoryMethods() {
        JsonRoomRepository repo = new JsonRoomRepository();
        Room room = new Room(101, "Salle A1", 20, "Salle de test");
        
        Room saved = repo.save(room);
        assertNotNull(saved.getId());
        
        
        saved.setCapacity(30);
        repo.save(saved);
        assertEquals(30, repo.findById(saved.getId()).get().getCapacity());
        
        
        assertFalse(repo.findAll().isEmpty());
        assertTrue(repo.count() > 0);
        
        assertTrue(repo.delete(saved.getId()));
    }
}
