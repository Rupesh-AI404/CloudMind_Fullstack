package com.cloudmind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/user-dashboard")
    public String userDashboard(HttpSession session) {
        if (session.getAttribute("activeUser") == null || !"USER".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }
        return "user-dashboard";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(HttpSession session) {
        if (session.getAttribute("activeUser") == null || !"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }
        return "admin-dashboard";
    }
}