package eidd.grp5.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import eidd.grp5.model.User;

class JsonUserRepositoryTest {

    @Test
    void shouldSaveAndLoadUser() {
        JsonUserRepository repo = new JsonUserRepository();
        User user = new User("Alice Test", "alice@test.com");
        
        User saved = repo.save(user);
        assertNotNull(saved.getId());
        
        var found = repo.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Alice Test", found.get().getName());
        
        repo.delete(saved.getId());
    }
}
