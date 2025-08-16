package com.cloudmind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

//    @GetMapping("/about")
//    public String about(HttpSession session) {
//        if (session.getAttribute("activeUser") != null) {
//            String role = (String) session.getAttribute("userRole");
//            return "redirect:/" + (role != null && role.equals("ADMIN") ? "admin-dashboard" : "user-dashboard");
//        }
//        return "about";
//    }

    @GetMapping("/about")
    public String about(HttpSession session, Model model) {
        if (session.getAttribute("activeUser") != null) {
            model.addAttribute("activeUser", session.getAttribute("activeUser"));
            model.addAttribute("userRole", session.getAttribute("userRole"));
        }
        return "about";
    }
}