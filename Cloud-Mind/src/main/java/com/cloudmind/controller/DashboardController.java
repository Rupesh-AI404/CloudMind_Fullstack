package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import com.cloudmind.repository.SubscriberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.ArrayList;

@Controller
public class DashboardController {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @GetMapping("/user-dashboard")
    public String userDashboard(Model model, HttpSession session) {
        String userEmail = (String) session.getAttribute("email");
        String userName = (String) session.getAttribute("activeUser");
        String userRole = (String) session.getAttribute("userRole");

        if (userEmail != null) {
            try {
                // Get only active subscriptions
                List<Subscriber> activeSubscriptions = subscriberRepository
                        .findByEmailAndStatus(userEmail, "ACTIVE");

                // Get the most recent active subscription
                Subscriber activeSubscription = activeSubscriptions.isEmpty() ?
                        null : activeSubscriptions.get(0);

                // Add attributes to model
                model.addAttribute("activeUser", userName);
                model.addAttribute("userEmail", userEmail);
                model.addAttribute("userRole", userRole);
                model.addAttribute("subscriptions", activeSubscriptions);
                model.addAttribute("activeSubscription", activeSubscription);
                model.addAttribute("hasActiveSubscription", activeSubscription != null);

            } catch (Exception e) {
                System.out.println("Error fetching subscriptions: " + e.getMessage());
                model.addAttribute("subscriptions", new ArrayList<>());
                model.addAttribute("hasActiveSubscription", false);
            }
        }

        return "user-dashboard";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("activeUser") == null ||
                !"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }

        model.addAttribute("activeUser", session.getAttribute("activeUser"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "admin-dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        // Redirect based on user role
        String userRole = (String) session.getAttribute("userRole");

        if ("ADMIN".equals(userRole)) {
            return "redirect:/admin-dashboard";
        } else {
            return "redirect:/user-dashboard";
        }
    }
}