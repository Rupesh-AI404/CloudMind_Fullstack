 package com.cloudmind.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class BaseController {

    @ModelAttribute("activeUser")
    public Object getActiveUser(HttpSession session) {
        return session.getAttribute("activeUser");
    }

    @ModelAttribute("userRole")
    public Object getUserRole(HttpSession session) {
        return (String) session.getAttribute("userRole");
    }
}
