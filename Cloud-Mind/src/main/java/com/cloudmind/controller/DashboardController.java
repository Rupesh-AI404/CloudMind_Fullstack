package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import com.cloudmind.model.User;
import com.cloudmind.repository.SubscriberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private SubscriberRepository repo;

    @GetMapping("/user-dashboard")
    public String userDashboard(HttpSession session, Model model) {
        System.out.println("=== User Dashboard Access ===");

        // Check authentication
        if (session.getAttribute("activeUser") == null) {
            System.out.println("No active user, redirecting to login");
            return "redirect:/login";
        }

        // Get user email from session
        String userEmail = null;
        Object activeUser = session.getAttribute("activeUser");

        // Try different ways to get email
        if (activeUser instanceof User) {
            userEmail = ((User) activeUser).getEmail();
        } else if (session.getAttribute("email") != null) {
            userEmail = (String) session.getAttribute("email");
        } else if (activeUser instanceof String) {
            userEmail = (String) activeUser;
        }

        System.out.println("Looking for subscription with email: " + userEmail);

        if (userEmail == null) {
            System.out.println("Could not determine user email");
            return "redirect:/login";
        }

        // Fetch subscription from database
        // Fetch subscription from database
        Subscriber activeSubscription = null;
        try {
            // Use the new method that gets the latest subscription
            activeSubscription = repo.findFirstByEmailOrderByIdDesc(userEmail);

            if (activeSubscription != null) {
                System.out.println("✅ Found subscription (latest):");
                System.out.println("   ID: " + activeSubscription.getId());
                System.out.println("   Email: " + activeSubscription.getEmail());
                System.out.println("   Plan: " + activeSubscription.getPlan());
                System.out.println("   Billing: " + activeSubscription.getBilling());
                System.out.println("   Payment Method: " + activeSubscription.getPaymentMethod());
            } else {
                System.out.println("❌ No subscription found for email: " + userEmail);
            }

            // Debug: Show how many duplicate subscriptions exist
            List<Subscriber> allForEmail = repo.findByEmail(userEmail);
            if (allForEmail.size() > 1) {
                System.out.println("⚠️  Found " + allForEmail.size() + " subscription records for this email!");
                System.out.println("   Using the latest one (ID: " + (activeSubscription != null ? activeSubscription.getId() : "none") + ")");
            }

        } catch (Exception e) {
            System.out.println("Error fetching subscription: " + e.getMessage());
            e.printStackTrace();
        }

        // Add data to model
        model.addAttribute("activeUser", activeUser);
        model.addAttribute("userRole", session.getAttribute("userRole"));
        model.addAttribute("userEmail", userEmail);

        if (activeSubscription != null) {
            model.addAttribute("hasActiveSubscription", true);
            model.addAttribute("activeSubscription", activeSubscription);
            model.addAttribute("subscriptionDate", "Recently purchased");
            System.out.println("✅ Added subscription data to model");
        } else {
            model.addAttribute("hasActiveSubscription", false);
            model.addAttribute("activeSubscription", null);
            System.out.println("❌ No subscription data added to model");
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