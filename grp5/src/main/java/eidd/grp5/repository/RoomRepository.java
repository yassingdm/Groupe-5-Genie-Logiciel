package eidd.grp5.repository;

import eidd.grp5.model.Room;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RoomRepository implements Repository<Room> {

    private final List<Room> rooms = new ArrayList<>();
    
    @Override
    public Room save(Room entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        Room safeEntity = copyRoom(entity);
        if (entity.getId() == null) {
            // New room: assign an id and store it.
            long newId = rooms.size() + 1L;
            entity.setId(newId);
            safeEntity.setId(newId);
            rooms.add(safeEntity);
        } else {
            // Existing room: replace by id.
            for (int i = 0; i < rooms.size(); i++) {
                if (rooms.get(i).getId().equals(entity.getId())) {
                    rooms.set(i, safeEntity);
                    break;
                }
            }
        }
        return copyRoom(safeEntity);
    }

    @Override
    public List<Room> findAll() {
        List<Room> result = new ArrayList<>();
        for (Room room : rooms) {
            result.add(copyRoom(room));
        }
        return List.copyOf(result);
    }

    @Override
    public Optional<Room> findById(Long id) {
        return rooms.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .map(this::copyRoom);
    }

    @Override
    public boolean delete(Long id) {
        return rooms.removeIf(r -> r.getId().equals(id));
    }

    @Override
    public long count() {
        return rooms.size();
    }

    private Room copyRoom(Room room) {
        Room copy = new Room(0, room.getName(), room.getCapacity(), room.getDescription());
        copy.setId(room.getId());
        return copy;
    }
}
