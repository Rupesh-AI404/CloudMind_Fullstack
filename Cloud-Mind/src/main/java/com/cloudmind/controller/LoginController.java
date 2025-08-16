package com.cloudmind.controller;

import com.cloudmind.model.User;
import com.cloudmind.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/login")
    public String loginPage() {
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
            // Admin login
            session.setAttribute("activeUser", email);
            session.setMaxInactiveInterval(1800); // 30 minutes
            return "admin-dashboard"; // Redirect to admin dashboard
        }

        // Find the user by email
        User existingUser = userRepo.findByEmail(email);
        if (existingUser != null && existingUser.getPassword().equals(hashedPassword)) {
            // Successful login
            session.setAttribute("activeUser", email);

            session.setAttribute("userRole", existingUser.getRole());
            session.setMaxInactiveInterval(1800); // 30 minutes

//            List<User> uList = userRepo.findAll();
//            model.addAttribute("uList", uList);


            if ("ADMIN".equals(existingUser.getRole())) {
                return "admin-dashboard";
            } else {
                return "user-dashboard";
            }
        } else {
            model.addAttribute("loginerror", "Username or Password Incorrect");
            return "login";
        }
    }
}