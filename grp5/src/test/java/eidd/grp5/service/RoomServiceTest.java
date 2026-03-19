package eidd.grp5.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
