package com.cloudmind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {



    @GetMapping("/about")
    public String about(Model model, HttpSession session) {
        // No need to manually add activeUser - BaseController does it automatically

        return "about";
    }

}