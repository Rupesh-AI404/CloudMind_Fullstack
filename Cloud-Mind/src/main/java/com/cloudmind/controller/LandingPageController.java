package com.cloudmind.controller;


import com.cloudmind.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingPageController {

    @Autowired
    private UserRepository userRepo;

//    @GetMapping("/")
//    public String landing(HttpSession session) {
//        if (session.getAttribute("activeUser") != null) {
//            String role = (String) session.getAttribute("userRole");
//            return "redirect:/" + (role != null && role.equals("ADMIN") ? "admin-dashboard" : "user-dashboard");
//        }
//        return "index";
//    }

    @GetMapping("/")
    public String landing(HttpSession session, Model model) {
        if (session.getAttribute("activeUser") != null) {
            model.addAttribute("activeUser", session.getAttribute("activeUser"));
            model.addAttribute("userRole", session.getAttribute("userRole"));
        }
        return "index";
    }


}