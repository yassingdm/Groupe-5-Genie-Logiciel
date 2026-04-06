package eidd.grp5.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eidd.grp5.model.User;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonUserRepository implements Repository<User> {
    private static final String FILE_PATH = "users.json";
    private final Gson gson = new Gson();
    private List<User> users = new ArrayList<>();

    public JsonUserRepository() {
        loadFromFile();
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<User>>(){}.getType();
            users = gson.fromJson(reader, listType);
            if (users == null) users = new ArrayList<>();
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Erreur de lecture du fichier", e);
        }
    }

    private void saveToFile() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            throw new java.io.UncheckedIOException("Erreur d'écriture du fichier", e);
        }
    }

    @Override
    public User save(User entity) {
        if (entity.getId() == null) {
            long maxId = users.stream().mapToLong(u -> u.getId() != null ? u.getId() : 0).max().orElse(0L);
            entity.setId(maxId + 1);
            users.add(entity);
        } else {
            boolean found = false;
            for (int i = 0; i < users.size(); i++) {
                if (entity.getId().equals(users.get(i).getId())) {
                    users.set(i, entity);
                    found = true;
                    break;
                }
            }
            if (!found) users.add(entity);
        }
        saveToFile();
        return entity;
    }

    @Override
    public List<User> findAll() { return new ArrayList<>(users); }

    @Override
    public Optional<User> findById(Long id) {
        return users.stream().filter(u -> id.equals(u.getId())).findFirst();
    }

    @Override
    public boolean delete(Long id) {
        boolean removed = users.removeIf(u -> id.equals(u.getId()));
        if (removed) saveToFile();
        return removed;
    }

    @Override
    public long count() { return users.size(); }
}