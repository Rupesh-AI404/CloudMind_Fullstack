package com.cloudmind.RestController;


import com.cloudmind.model.User;
import com.cloudmind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/getAll")
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @GetMapping("/getById/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        return userRepo.findById(id);
    }

    @GetMapping("/getByEmail/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userRepo.findByEmail(email);
    }

    @GetMapping("/getByRole/{role}")
    public List<User> getUsersByRole(@PathVariable String role) {
        return userRepo.findByRole(role);
    }

    @PostMapping("/create")
    public User createUser(@RequestBody User user) {
        return userRepo.save(user);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepo.deleteById(id);
        return "User deleted successfully";
    }
}