package com.cloudmind.controller;

import com.cloudmind.model.User;
import com.cloudmind.repository.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpSession;

@Controller
public class SignupController {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signupPost(@ModelAttribute User user, Model model, HttpSession session) {
        if (userRepo.existsByEmail(user.getEmail())) {
            model.addAttribute("emailerror", "Email already exists!");
            return "signup";
        }

        // Hash the password before saving
        String hashedPassword = DigestUtils.md5Hex(user.getPassword());
        user.setPassword(hashedPassword);

        // Set default role if not set
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        try {
            // Save the user
            User savedUser = userRepo.save(user);
            System.out.println("User signed up: " + savedUser.getFirstName() + " " + savedUser.getLastName() + ", Email: " + savedUser.getEmail());

            // Auto-login: Create session with EXACT same attributes as LoginController
            session.setAttribute("activeUser", savedUser.getFirstName() + " " + savedUser.getLastName());
            session.setAttribute("email", savedUser.getEmail());
            session.setAttribute("userRole", savedUser.getRole());
            session.setAttribute("userId", savedUser.getId());
            session.setMaxInactiveInterval(1800); // Same session timeout as login

            System.out.println("✅ User auto-logged in after signup: " + savedUser.getEmail());

            // Send welcome email
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(savedUser.getEmail());
                message.setSubject("Welcome to Cloud Mind");
                message.setText("Congratulations " + savedUser.getFirstName() + " " + savedUser.getLastName() + ",\n"
                        + "You have successfully signed up for Cloud Mind!\n\n"
                        + "You are now logged in and can start exploring our services.\n\n"
                        + "Best regards,\nCloud Mind Team");
                mailSender.send(message);
            } catch (Exception emailError) {
                System.out.println("Email sending failed: " + emailError.getMessage());
                // Don't fail signup if email fails
            }

            // Redirect based on role - exactly like LoginController
            if ("ADMIN".equals(savedUser.getRole())) {
                return "redirect:/admin-dashboard";
            } else {
                return "redirect:/";
            }

        } catch (Exception e) {
            System.out.println("Signup failed: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Signup failed: " + e.getMessage());
            return "signup";
        }
    }
}