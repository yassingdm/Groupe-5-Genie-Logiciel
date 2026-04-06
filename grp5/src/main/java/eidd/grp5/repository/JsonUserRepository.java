package eidd.grp5.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import eidd.grp5.model.User;

import java.io.*;
import java.lang.reflect.Type;
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
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<ArrayList<User>>(){}.getType();
            users = gson.fromJson(reader, listType);
            if (users == null) users = new ArrayList<>();
        } catch (FileNotFoundException e) {
            users = new ArrayList<>(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User save(User entity) {
        if (entity.getId() == null) {
            entity.setId(users.isEmpty() ? 1L : users.get(users.size() - 1).getId() + 1);
            users.add(entity);
        } else {
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(entity.getId())) {
                    users.set(i, entity);
                    break;
                }
            }
        }
        saveToFile(); 
        return entity;
    }

    @Override
    public List<User> findAll() { return new ArrayList<>(users); }

    @Override
    public Optional<User> findById(Long id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    @Override
    public boolean delete(Long id) {
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (removed) saveToFile();
        return removed;
    }

    @Override
    public long count() { return users.size(); }
}
