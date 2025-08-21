package com.cloudmind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ContactController {

//    @GetMapping("/contact")
//    public String contact(HttpSession session) {
//        if (session.getAttribute("activeUser") != null) {
//            String role = (String) session.getAttribute("userRole");
//            return "redirect:/" + (role != null && role.equals("ADMIN") ? "admin-dashboard" : "user-dashboard");
//        }
//        return "contact";
//    }

    @GetMapping("/contact")
    public String contact(HttpSession session, Model model) {
//        if (session.getAttribute("activeUser") != null) {
//            model.addAttribute("activeUser", session.getAttribute("activeUser"));
//            model.addAttribute("userRole", session.getAttribute("userRole"));
//        }
        return "contact";
    }


    @PostMapping("/contact")
    public String submitContact(HttpSession session) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        // Add form processing logic here (e.g., save to DB)




        return "redirect:/contact?success";
    }
}