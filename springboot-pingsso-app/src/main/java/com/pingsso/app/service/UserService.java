package com.pingsso.app.service;

import com.pingsso.app.entity.User;
import com.pingsso.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User createOrUpdateUser(String email, String name, String picture, String pingSsoId) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setName(name);
            user.setPicture(picture);
            user.setPingSsoId(pingSsoId);
            user.setUpdatedAt(LocalDateTime.now());
        } else {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setPicture(picture);
            user.setPingSsoId(pingSsoId);
            user.setRoles(new HashSet<>(Collections.singletonList("ROLE_USER")));
            user.setActive(true);
        }

        return userRepository.save(user);
    }

    public User updateUser(Long id, String name, String picture) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User u = user.get();
            u.setName(name);
            u.setPicture(picture);
            u.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(u);
        }
        return null;
    }

    public User recordLogin(Long id, String ipAddress) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User u = user.get();
            u.setLastLoginTime(LocalDateTime.now());
            u.setLastLoginIp(ipAddress);
            return userRepository.save(u);
        }
        return null;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
