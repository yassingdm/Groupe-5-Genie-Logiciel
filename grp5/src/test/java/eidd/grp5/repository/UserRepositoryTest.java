package eidd.grp5.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.model.User;

class UserRepositoryTest {

    @Test
    void shouldSaveFindUpdateDeleteAndCountUsers() {
        UserRepository repository = new UserRepository();

        User user = new User("Alice", "alice@test.com");
        User saved = repository.save(user);

        assertEquals(1L, saved.getId());
        assertEquals(1L, repository.count());
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(1, repository.findAll().size());

        saved.setName("Alice Updated");
        repository.save(saved);

        assertEquals("Alice Updated", repository.findById(1L).orElseThrow().getName());
        assertTrue(repository.delete(1L));
        assertEquals(0L, repository.count());
        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void shouldNotCreateNewEntryWhenUpdatingUnknownId() {
        UserRepository repository = new UserRepository();

        User user = new User("Ghost", "ghost@test.com");
        user.setId(42L);
        repository.save(user);

        assertEquals(0L, repository.count());
        assertFalse(repository.findById(42L).isPresent());
        assertFalse(repository.delete(42L));
    }
}
