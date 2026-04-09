package eidd.grp5.repository;

import eidd.grp5.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserRepository implements Repository<User> {

    private final List<User> users = new ArrayList<>();
    
    @Override
    public User save(User entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        User safeEntity = copyUser(entity);
        if (entity.getId() == null) {
            // New user: assign an id and store it.
            long newId = users.size() + 1L;
            entity.setId(newId);
            safeEntity.setId(newId);
            users.add(safeEntity);
        } else {
            // Existing user: replace by id.
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(entity.getId())) {
                    users.set(i, safeEntity);
                    break;
                }
            }
        }
        return copyUser(safeEntity);
    }

    @Override
    public List<User> findAll() {
        List<User> result = new ArrayList<>();
        for (User user : users) {
            result.add(copyUser(user));
        }
        return List.copyOf(result);
    }

    @Override
    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .map(this::copyUser);
    }

    @Override
    public boolean delete(Long id) {
        return users.removeIf(u -> u.getId().equals(id));
    }

    @Override
    public long count() {
        return users.size();
    }

    private User copyUser(User user) {
        User copy = new User(user.getName(), user.getEmail());
        copy.setId(user.getId());
        copy.setRole(user.getRole());
        return copy;
    }
}
