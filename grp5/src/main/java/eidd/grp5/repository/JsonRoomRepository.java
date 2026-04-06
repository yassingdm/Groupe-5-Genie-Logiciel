package eidd.grp5.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eidd.grp5.model.Room;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonRoomRepository implements Repository<Room> {
    private static final String FILE_PATH = "rooms.json";
    private final Gson gson = new Gson();
    private List<Room> rooms = new ArrayList<>();

    public JsonRoomRepository() {
        loadFromFile();
    }

    private void loadFromFile() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<ArrayList<Room>>(){}.getType();
            rooms = gson.fromJson(reader, listType);
            if (rooms == null) rooms = new ArrayList<>();
        } catch (FileNotFoundException e) {
            rooms = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(rooms, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Room save(Room entity) {
        if (entity.getId() == null) {
            entity.setId(rooms.isEmpty() ? 1L : rooms.get(rooms.size() - 1).getId() + 1);
            rooms.add(entity);
        } else {
            for (int i = 0; i < rooms.size(); i++) {
                if (rooms.get(i).getId().equals(entity.getId())) {
                    rooms.set(i, entity);
                    break;
                }
            }
        }
        saveToFile();
        return entity;
    }

    @Override
    public List<Room> findAll() { return new ArrayList<>(rooms); }

    @Override
    public Optional<Room> findById(Long id) {
        return rooms.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    @Override
    public boolean delete(Long id) {
        boolean removed = rooms.removeIf(r -> r.getId().equals(id));
        if (removed) saveToFile();
        return removed;
    }

    @Override
    public long count() { return rooms.size(); }
}