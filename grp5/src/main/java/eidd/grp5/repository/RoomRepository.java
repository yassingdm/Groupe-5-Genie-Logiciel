package eidd.grp5.repository;

import eidd.grp5.model.Room;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomRepository implements Repository<Room> {

    private List<Room> rooms = new ArrayList<>();
    
    @Override
    public Room save(Room entity) {
        if (entity.getId() == null) {
            // Nouvelle salle
            entity.setId((long) (rooms.size() + 1));
            rooms.add(entity);
        } else {
            // Mise à jour d'une salle existante
            for (int i = 0; i < rooms.size(); i++) {
                if (rooms.get(i).getId().equals(entity.getId())) {
                    rooms.set(i, entity);
                    break;
                }
            }
        }
        return entity;
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(rooms);
    }

    @Override
    public Optional<Room> findById(Long id) {
        return rooms.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean delete(Long id) {
        return rooms.removeIf(r -> r.getId().equals(id));
    }

    @Override
    public long count() {
        return rooms.size();
    }
}
