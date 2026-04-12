package eidd.grp5.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.model.Room;
import eidd.grp5.repository.RoomRepository;

class RoomServiceTest {

    @Test
    void shouldCreateReadUpdateDeleteAndCountRooms() {
        RoomRepository repository = new RoomRepository();
        RoomService service = new RoomService(repository);

        Room room = new Room(0, "A101", 20, "Salle A101");
        room.setId(null);

        Room created = service.createRoom(room);

        assertEquals(1L, created.getId());
        assertEquals(1, service.getAllRooms().size());
        assertTrue(service.getRoomById(created.getId()).isPresent());
        assertEquals(1L, service.countRooms());

        created.setCapacity(30);
        Room updated = service.updateRoom(created);

        assertEquals(30, updated.getCapacity());
        assertEquals(30, service.getRoomById(created.getId()).orElseThrow().getCapacity());

        assertTrue(service.deleteRoom(created.getId()));
        assertFalse(service.getRoomById(created.getId()).isPresent());
        assertEquals(0L, service.countRooms());
    }

    @Test
    void shouldThrowWhenUpdatingRoomWithoutId() {
        RoomRepository repository = new RoomRepository();
        RoomService service = new RoomService(repository);

        Room room = new Room(0, "B201", 10, "Salle B201");
        room.setId(null);

        assertThrows(IllegalArgumentException.class, () -> service.updateRoom(room));
    }

    @Test
    void shouldManageRoomEquipmentsAndSearchByEquipment() {
        RoomRepository repository = new RoomRepository();
        RoomService service = new RoomService(repository);

        Room roomA = new Room(0, "A101", 20, "Salle A101");
        roomA.setId(null);
        Room savedA = service.createRoom(roomA);

        Room roomB = new Room(0, "B201", 12, "Salle B201");
        roomB.setId(null);
        Room savedB = service.createRoom(roomB);

        Room updatedA = service.addEquipmentToRoom(savedA.getId(), "Projecteur");
        assertTrue(updatedA.hasEquipment("projecteur"));

        Room updatedB = service.addEquipmentToRoom(savedB.getId(), "Visio");
        assertTrue(updatedB.hasEquipment("visio"));

        List<Room> roomsWithProjector = service.getRoomsByEquipment("PROJECTEUR");
        assertEquals(1, roomsWithProjector.size());
        assertEquals(savedA.getId(), roomsWithProjector.get(0).getId());

        Room afterRemoval = service.removeEquipmentFromRoom(savedA.getId(), "PROJECTEUR");
        assertFalse(afterRemoval.hasEquipment("projecteur"));

        assertNotNull(afterRemoval.getEquipments());
    }
}
