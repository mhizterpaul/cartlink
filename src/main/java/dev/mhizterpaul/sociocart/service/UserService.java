package dev.mhizterpaul.sociocart.service;

import dev.mhizterpaul.sociocart.model.User;
import dev.mhizterpaul.sociocart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public void removeUser(User user) {
        userRepository.delete(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }
}