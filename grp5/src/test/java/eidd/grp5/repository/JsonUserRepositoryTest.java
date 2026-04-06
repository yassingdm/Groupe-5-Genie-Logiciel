package eidd.grp5.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import eidd.grp5.model.User;

class JsonUserRepositoryTest {
    @Test
    void shouldCoverAllRepositoryMethods() {
        JsonUserRepository repo = new JsonUserRepository();
        User user = new User("Alice Test", "alice@test.com");
        
        
        User saved = repo.save(user);
        assertNotNull(saved.getId());
        
        
        assertTrue(repo.findById(saved.getId()).isPresent());
        
        
        saved.setName("Alice Updated");
        repo.save(saved);
        assertEquals("Alice Updated", repo.findById(saved.getId()).get().getName());
        
        
        assertFalse(repo.findAll().isEmpty());
        assertTrue(repo.count() > 0);
        
       
        assertTrue(repo.delete(saved.getId()));
    }
}
