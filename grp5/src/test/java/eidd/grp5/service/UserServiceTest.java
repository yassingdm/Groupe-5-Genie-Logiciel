package eidd.grp5.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.model.User;
import eidd.grp5.repository.UserRepository;

class UserServiceTest {

    @Test
    void shouldCreateReadUpdateDeleteAndCountUsers() {
        UserRepository repository = new UserRepository();
        UserService service = new UserService(repository);

        User user = new User("Alice", "alice@test.com");

        User created = service.createUser(user);

        assertEquals(1L, created.getId());
        assertEquals(1, service.getAllUsers().size());
        assertTrue(service.getUserById(created.getId()).isPresent());
        assertEquals(1L, service.countUsers());

        created.setName("Alice Updated");
        User updated = service.updateUser(created);

        assertEquals("Alice Updated", updated.getName());
        assertEquals("Alice Updated", service.getUserById(created.getId()).orElseThrow().getName());

        assertTrue(service.deleteUser(created.getId()));
        assertFalse(service.getUserById(created.getId()).isPresent());
        assertEquals(0L, service.countUsers());
    }

    @Test
    void shouldThrowWhenUpdatingUserWithoutId() {
        UserRepository repository = new UserRepository();
        UserService service = new UserService(repository);

        User user = new User("No Id", "noid@test.com");

        assertThrows(IllegalArgumentException.class, () -> service.updateUser(user));
    }
}
