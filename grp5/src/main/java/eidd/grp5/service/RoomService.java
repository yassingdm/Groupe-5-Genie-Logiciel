package eidd.grp5.service;

import java.util.List;
import java.util.Optional;

import eidd.grp5.model.Room;
import eidd.grp5.repository.RoomRepository;
import eidd.grp5.util.ValidationUtils;

public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = ValidationUtils.requireNonNull(roomRepository, "roomRepository must not be null");
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Room updateRoom(Room room) {
        if (room.getId() == null) {
            throw new IllegalArgumentException("Room id must not be null for update");
        }
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public boolean deleteRoom(Long id) {
        return roomRepository.delete(id);
    }

    public long countRooms() {
        return roomRepository.count();
    }
}
