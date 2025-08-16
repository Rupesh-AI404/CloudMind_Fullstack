package com.cloudmind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class LogoutController {

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Invalidate the session to log out the user
        // Assuming you have access to the HttpSession object
        session.invalidate();

        // Redirect to the login page after logout
        return "redirect:/login";
    }
}
