package eidd.grp5.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void shouldCreateUserWithConstructorValues() {
        User user = new User("Alice", "alice@mail.com");

        assertNotNull(user);
        assertEquals("Alice", user.getName());
        assertEquals("alice@mail.com", user.getEmail());
    }

    @Test
    void shouldUpdateUserFieldsInNominalCase() {
        User user = new User("Bob", "bob@mail.com");

        user.setId(10L);
        user.setName("Bobby");
        user.setEmail("bobby@mail.com");

        assertEquals(10L, user.getId());
        assertEquals("Bobby", user.getName());
        assertEquals("bobby@mail.com", user.getEmail());
    }

    @Test
    void shouldThrowWhenNameIsEmpty() {
        
        assertThrows(IllegalArgumentException.class, () -> new User("", "test@mail.com"));
    }
}