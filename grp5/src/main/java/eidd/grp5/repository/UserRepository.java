package eidd.grp5.repository;

import eidd.grp5.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository implements Repository<User> {

    private List<User> users = new ArrayList<>();
    
    @Override
    public User save(User entity) {
        if (entity.getId() == null) {
            // Nouvel utilisateur
            entity.setId((long) (users.size() + 1));
            users.add(entity);
        } else {
            // Mise à jour d'un utilisateur existant
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getId().equals(entity.getId())) {
                    users.set(i, entity);
                    break;
                }
            }
        }
        return entity;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    @Override
    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean delete(Long id) {
        return users.removeIf(u -> u.getId().equals(id));
    }

    @Override
    public long count() {
        return users.size();
    }
}
