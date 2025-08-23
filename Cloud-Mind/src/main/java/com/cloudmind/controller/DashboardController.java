package com.cloudmind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

//    @GetMapping("/user-dashboard")
//    public String userDashboard(HttpSession session) {
//        if (session.getAttribute("activeUser") == null || !"USER".equals(session.getAttribute("userRole"))) {
//            return "redirect:/login";
//        }
//        return "user-dashboard";
//    }

    @GetMapping("/user-dashboard")
    public String userDashboard(HttpSession session, Model model) {
        // Check if user is logged in
        if (session.getAttribute("activeUser") == null || session.getAttribute("email") == null) {
            return "redirect:/login";
        }

        // Add session attributes to model for Thymeleaf
        model.addAttribute("activeUser", session.getAttribute("activeUser"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "user-dashboard";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("activeUser") == null || !"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        model.addAttribute("activeUser", session.getAttribute("activeUser"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "admin-dashboard";
    }
}