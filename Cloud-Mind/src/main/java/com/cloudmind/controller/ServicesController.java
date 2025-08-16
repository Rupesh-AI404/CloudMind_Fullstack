package com.cloudmind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ServicesController {

//    @GetMapping("/services")
//    public String services(HttpSession session) {
//        if (session.getAttribute("activeUser") != null) {
//            String role = (String) session.getAttribute("userRole");
//            return "redirect:/" + (role != null && role.equals("ADMIN") ? "admin-dashboard" : "user-dashboard");
//        }
//        return "services";
//    }


    @GetMapping("/services")
    public String services(HttpSession session, Model model) {
        if (session.getAttribute("activeUser") != null) {
            model.addAttribute("activeUser", session.getAttribute("activeUser"));
            model.addAttribute("userRole", session.getAttribute("userRole"));
        }
        return "services";
    }
}