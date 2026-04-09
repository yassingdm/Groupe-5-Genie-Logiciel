package eidd.grp5.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void shouldCreateUserWithConstructorValues() {
        User user = new User("Alice", "alice@mail.com");

        assertNotNull(user);
        assertEquals("Alice", user.getName());
        assertEquals("alice@mail.com", user.getEmail());
        assertNull(user.getId());
        assertEquals(User.Role.CUSTOMER, user.getRole());
    }

    @Test
    void shouldUpdateUserFieldsInNominalCase() {
        User user = new User("Bob", "bob@mail.com");

        user.setId(10L);
        user.setName("Bobby");
        user.setEmail("bobby@mail.com");
        user.setRole(User.Role.ADMIN);

        assertEquals(10L, user.getId());
        assertEquals("Bobby", user.getName());
        assertEquals("bobby@mail.com", user.getEmail());
        assertEquals(User.Role.ADMIN, user.getRole());
    }

    @Test
    void shouldAcceptNullAndEmptyValuesAsEdgeCase() {
        User user = new User("", null);

        assertEquals("", user.getName());
        assertNull(user.getEmail());
    }
}
