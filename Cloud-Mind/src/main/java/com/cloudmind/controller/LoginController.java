package com.cloudmind.controller;

import com.cloudmind.model.User;
import com.cloudmind.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String loginPost(@ModelAttribute User user, Model model, HttpSession session) {
        String email = user.getEmail();
        String password = user.getPassword();

        // Hash the input password to match the stored hashed password
        String hashedPassword = DigestUtils.md5Hex(password);

        // Special admin check
        if ("admin@cloudmind.com".equals(email) && "admin123".equals(password)) {
            session.setAttribute("activeUser", email);
            session.setAttribute("userRole", "ADMIN");

            session.setAttribute("email", email); // Add this line

            session.setMaxInactiveInterval(1800);
            return "redirect:/admin-dashboard";
        }

        // Find the user by email
        User existingUser = userRepo.findByEmail(email);
        if (existingUser != null && existingUser.getPassword().equals(hashedPassword)) {
            // Set session attributes individually instead of storing the whole object
            session.setAttribute("activeUser", existingUser.getFirstName() + " " + existingUser.getLastName());
            session.setAttribute("email", existingUser.getEmail()); // Add this line
            session.setAttribute("userRole", existingUser.getRole());
            session.setAttribute("userId", existingUser.getId()); // Optional: add user ID
            session.setMaxInactiveInterval(1800);

            if ("ADMIN".equals(existingUser.getRole())) {
                return "redirect:/admin-dashboard";
            } else {
                return "redirect:/user-dashboard";
            }
        } else {
            model.addAttribute("loginerror", "Username or Password Incorrect");
            return "login";
        }
    }



    @GetMapping("/auth/status")
    @ResponseBody
    public ResponseEntity<?> checkSession(HttpSession session) {
        Object activeUser = session.getAttribute("activeUser");
        String userRole = (String) session.getAttribute("userRole");

        if (activeUser != null) {
            String email = null;
            String userName = null;

            if (activeUser instanceof User) {
                User user = (User) activeUser;
                email = user.getEmail();
                userName = user.getFirstName() + " " + user.getLastName(); // Use your actual name fields
            } else if (activeUser instanceof String) {
                email = (String) activeUser;
                userName = email.split("@")[0]; // Use email prefix as name
            }

            return ResponseEntity.ok(Map.of(
                    "loggedIn", true,
                    "email", email,
                    "userName", userName != null ? userName : "User",
                    "role", userRole != null ? userRole : "USER"
            ));
        }

        return ResponseEntity.ok(Map.of("loggedIn", false));
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}