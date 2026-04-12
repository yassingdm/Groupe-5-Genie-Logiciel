package eidd.grp5.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    void shouldManageRoomEquipments() {
        Room room = new Room(4, "D404", 20, "Salle equipee");

        room.addEquipment("Projecteur");
        room.addEquipment("Visio");
        room.addEquipment("Projecteur");

        assertTrue(room.hasEquipment("projecteur"));
        assertTrue(room.hasEquipment("VISIO"));
        assertEquals(2, room.getEquipments().size());

        room.removeEquipment("PROJECTEUR");
        assertEquals(1, room.getEquipments().size());
        assertEquals("Visio", room.getEquipments().get(0));
    }
}
