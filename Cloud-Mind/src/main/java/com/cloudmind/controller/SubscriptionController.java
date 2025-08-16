package com.cloudmind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SubscriptionController {

//    @GetMapping("/subscription")
//    public String subscription(HttpSession session) {
//        if (session.getAttribute("activeUser") != null) {
//            String role = (String) session.getAttribute("userRole");
//            return "redirect:/" + (role != null && role.equals("ADMIN") ? "admin-dashboard" : "user-dashboard");
//        }
//        return "subscription";
//    }


    @GetMapping("/subscription")
    public String subscription(HttpSession session, Model model) {
        if (session.getAttribute("activeUser") != null) {
            model.addAttribute("activeUser", session.getAttribute("activeUser"));
            model.addAttribute("userRole", session.getAttribute("userRole"));
        }
        return "subscription";
    }
}
// This controller handles the subscription page. It checks if a user is logged in and redirects them to the appropriate dashboard if they are.