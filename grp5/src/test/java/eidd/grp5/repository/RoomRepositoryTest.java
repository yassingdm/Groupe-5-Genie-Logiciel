package eidd.grp5.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import eidd.grp5.model.Room;

class RoomRepositoryTest {

    @Test
    void shouldSaveFindUpdateDeleteAndCountRooms() {
        RoomRepository repository = new RoomRepository();

        Room room = new Room(0, "A101", 12, "Salle de réunion");
        room.setId(null);
        Room saved = repository.save(room);

        assertEquals(1L, saved.getId());
        assertEquals(1L, repository.count());
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(1, repository.findAll().size());

        saved.setCapacity(20);
        repository.save(saved);

        assertEquals(20, repository.findById(1L).orElseThrow().getCapacity());
        assertTrue(repository.delete(1L));
        assertEquals(0L, repository.count());
        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void shouldNotCreateNewEntryWhenUpdatingUnknownId() {
        RoomRepository repository = new RoomRepository();

        Room room = new Room(999, "Ghost", 1, "Ghost room");
        repository.save(room);

        assertEquals(0L, repository.count());
        assertFalse(repository.findById(999L).isPresent());
        assertFalse(repository.delete(999L));
    }
}
