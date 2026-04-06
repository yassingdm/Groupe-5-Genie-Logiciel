package eidd.grp5.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eidd.grp5.model.Room;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
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
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<Room>>(){}.getType();
            rooms = gson.fromJson(reader, listType);
            if (rooms == null) rooms = new ArrayList<>();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Erreur de lecture du fichier", e);
        }
    }

    private void saveToFile() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8)) {
            gson.toJson(rooms, writer);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Erreur d'écriture du fichier", e);
        }
    }

    @Override
    public Room save(Room entity) {
        if (entity.getId() == null) {
            long maxId = rooms.stream().mapToLong(r -> r.getId() != null ? r.getId() : 0).max().orElse(0L);
            entity.setId(maxId + 1);
            rooms.add(entity);
        } else {
            boolean found = false;
            for (int i = 0; i < rooms.size(); i++) {
                if (entity.getId().equals(rooms.get(i).getId())) {
                    rooms.set(i, entity);
                    found = true;
                    break;
                }
            }
            if (!found) rooms.add(entity);
        }
        saveToFile();
        return entity;
    }

    @Override
    public List<Room> findAll() { return new ArrayList<>(rooms); }

    @Override
    public Optional<Room> findById(Long id) {
        return rooms.stream().filter(r -> id.equals(r.getId())).findFirst();
    }

    @Override
    public boolean delete(Long id) {
        boolean removed = rooms.removeIf(r -> id.equals(r.getId()));
        if (removed) saveToFile();
        return removed;
    }

    @Override
    public long count() { return rooms.size(); }
}