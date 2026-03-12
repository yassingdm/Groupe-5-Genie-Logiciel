package eidd.grp5.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class RoomTest {

    @Test
    void shouldCreateRoomWithConstructorValues() {
        Room room = new Room(1, "A101", 12, "Salle de réunion");

        assertNotNull(room);
        assertEquals(1L, room.getId());
        assertEquals("A101", room.getName());
        assertEquals(12, room.getCapacity());
        assertEquals("Salle de réunion", room.getDescription());
    }

    @Test
    void shouldUpdateRoomFieldsInNominalCase() {
        Room room = new Room(2, "B202", 8, "Initiale");

        room.setName("B203");
        room.setCapacity(16);
        room.setDescription("Grande salle");

        assertEquals("B203", room.getName());
        assertEquals(16, room.getCapacity());
        assertEquals("Grande salle", room.getDescription());
    }

    @Test
    void shouldHandleZeroCapacityAsEdgeCase() {
        Room room = new Room(3, "C303", 0, "Capacité nulle");

        assertEquals(0, room.getCapacity());
    }
}
