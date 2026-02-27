package com.group.SwapSmart.controller;

import com.group.SwapSmart.entity.User;
import com.group.SwapSmart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // retrieves all users in table
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // grabbing a user by id
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // basic function to create a user (left simple for now)
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }
}