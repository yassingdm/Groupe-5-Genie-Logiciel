package eidd.grp5.service;

import java.util.List;
import java.util.Optional;

import eidd.grp5.model.User;
import eidd.grp5.repository.UserRepository;
import eidd.grp5.util.ValidationUtils;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = ValidationUtils.requireNonNull(userRepository, "userRepository must not be null");
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User id must not be null for update");
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public boolean deleteUser(Long id) {
        return userRepository.delete(id);
    }

    public long countUsers() {
        return userRepository.count();
    }
}
