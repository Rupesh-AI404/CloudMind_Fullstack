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
    public String signupPost(@ModelAttribute User user, Model model) {
        if (userRepo.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email already exists!");
            return "signup";
        }

        String hashedPassword = DigestUtils.md5Hex(user.getPassword());
        user.setPassword(hashedPassword);

        try {
            userRepo.save(user);
            System.out.println("User signed up: " + user.getFirstName() + " " + user.getLastName() + ", Email: " + user.getEmail());

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Welcome to Cloud Mind");
            message.setText("Congratulations " + user.getFirstName() + " " + user.getLastName() + ",\n"
                    + "You have successfully signed up for Cloud Mind!");
            mailSender.send(message);
        } catch (Exception e) {
            model.addAttribute("error", "Signup failed: " + e.getMessage());
            return "signup";
        }

        return "redirect:/login";
    }
}