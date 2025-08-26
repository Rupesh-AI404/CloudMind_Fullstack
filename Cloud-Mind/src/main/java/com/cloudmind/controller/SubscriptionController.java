package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import com.cloudmind.repository.SubscriberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class SubscriptionController {

    @Autowired
    private SubscriberRepository subscriberRepository;



    @GetMapping("/subscription")
    public String subscription(HttpSession session, Model model) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/signup";
        }

        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/admin-dashboard";
        }

        model.addAttribute("subscriber", new Subscriber());
        return "subscription";
    }

    @PostMapping("/subscription")
    public String submitSubscription(Subscriber subscriber, HttpSession session, Model model,
                                     @RequestParam("plan") String selectedPlan,
                                     @RequestParam("billing") String selectedBilling,
                                     RedirectAttributes redirectAttributes) {

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            redirectAttributes.addFlashAttribute("error", "Admin users cannot subscribe to plans");
            return "redirect:/admin-dashboard";
        }

        String sessionEmail = (String) session.getAttribute("email");
        String formEmail = subscriber.getEmail();

        if (sessionEmail == null) {
            model.addAttribute("error", "Session expired. Please logout and login again.");
            model.addAttribute("subscriber", subscriber);
            return "subscription";
        }

        if (!sessionEmail.equals(formEmail)) {
            model.addAttribute("error", "Email must match your account email: " + sessionEmail);
            model.addAttribute("subscriber", subscriber);
            return "subscription";
        }

        // Set subscriber data with timestamp
        subscriber.setPlan(selectedPlan);
        subscriber.setBilling(selectedBilling);
        subscriber.setEmail(sessionEmail);
        subscriber.setSubscriptionDate(LocalDateTime.now());
        subscriber.setStatus("PENDING"); // Will be updated to ACTIVE after payment

        // Store subscription details in session for payment processing
        session.setAttribute("subscriber", subscriber);
        session.setAttribute("selectedPlan", selectedPlan);
        session.setAttribute("selectedBilling", selectedBilling);

        return "redirect:/payment";
    }

    @GetMapping("/debug-subscription")
    public String debugSubscription(HttpSession session, Model model) {
        model.addAttribute("activeUser", session.getAttribute("activeUser"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
        model.addAttribute("selectedPlan", session.getAttribute("selectedPlan"));
        model.addAttribute("selectedBilling", session.getAttribute("selectedBilling"));
        return "debug-session";
    }

    @PostMapping("/subscribe")
    public String processSubscription(@ModelAttribute Subscriber subscriber,
                                      HttpSession session, Model model) {
        try {
            String userEmail = (String) session.getAttribute("email");

            // Check if user already has an active subscription
            List<Subscriber> existingSubscriptions = subscriberRepository
                    .findByEmailAndStatus(userEmail, "ACTIVE");

            if (!existingSubscriptions.isEmpty()) {
                model.addAttribute("error", "You already have an active subscription.");
                return "subscription";
            }

            // Set user email and subscription details
            subscriber.setEmail(userEmail);
            subscriber.setSubscriptionDate(LocalDateTime.now());
            subscriber.setStatus("ACTIVE");

            // Calculate expiry date
            if ("monthly".equals(subscriber.getBilling())) {
                subscriber.setExpiryDate(LocalDateTime.now().plusMonths(1));
            } else if ("yearly".equals(subscriber.getBilling())) {
                subscriber.setExpiryDate(LocalDateTime.now().plusYears(1));
            }

            subscriberRepository.save(subscriber);
            return "redirect:/user-dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Subscription failed. Please try again.");
            return "subscription";
        }
    }

    @PostMapping("/subscription/upgrade")
    public String upgradeSubscription(@RequestParam("newPlan") String newPlan,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        try {
            String userEmail = (String) session.getAttribute("email");

            if (userEmail == null) {
                redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
                return "redirect:/login";
            }

            // Find user's active subscription
            List<Subscriber> activeSubscriptions = subscriberRepository.findByEmailAndStatus(userEmail, "ACTIVE");

            if (activeSubscriptions.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No active subscription found.");
                return "redirect:/user-dashboard";
            }

            Subscriber subscription = activeSubscriptions.get(0);
            String currentPlan = subscription.getPlan();

            // Simple upgrade validation
            if (!isValidUpgrade(currentPlan, newPlan)) {
                redirectAttributes.addFlashAttribute("error", "Invalid upgrade path.");
                return "redirect:/user-dashboard";
            }

            // Update the plan
            subscription.setPlan(newPlan);
            subscriberRepository.save(subscription);

            redirectAttributes.addFlashAttribute("success",
                    "Plan upgraded successfully to " + newPlan.toUpperCase() + "!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upgrade plan. Please try again.");
        }

        return "redirect:/user-dashboard";
    }

    @PostMapping("/subscription/downgrade")
    public String downgradeSubscription(@RequestParam("newPlan") String newPlan,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        try {
            String userEmail = (String) session.getAttribute("email");

            if (userEmail == null) {
                redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
                return "redirect:/login";
            }

            // Find user's active subscription
            List<Subscriber> activeSubscriptions = subscriberRepository.findByEmailAndStatus(userEmail, "ACTIVE");

            if (activeSubscriptions.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No active subscription found.");
                return "redirect:/user-dashboard";
            }

            Subscriber subscription = activeSubscriptions.get(0);
            String currentPlan = subscription.getPlan();

            // Simple downgrade validation
            if (!isValidDowngrade(currentPlan, newPlan)) {
                redirectAttributes.addFlashAttribute("error", "Invalid downgrade path.");
                return "redirect:/user-dashboard";
            }

            // Update the plan (immediate change for simplicity)
            subscription.setPlan(newPlan);
            subscriberRepository.save(subscription);

            redirectAttributes.addFlashAttribute("success",
                    "Plan downgraded to " + newPlan.toUpperCase() + "!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to downgrade plan. Please try again.");
        }

        return "redirect:/user-dashboard";
    }

    // Helper methods for validation
    private boolean isValidUpgrade(String currentPlan, String newPlan) {
        return (currentPlan.equals("starter") && (newPlan.equals("professional") || newPlan.equals("enterprise"))) ||
                (currentPlan.equals("professional") && newPlan.equals("enterprise"));
    }

    private boolean isValidDowngrade(String currentPlan, String newPlan) {
        return (currentPlan.equals("enterprise") && (newPlan.equals("professional") || newPlan.equals("starter"))) ||
                (currentPlan.equals("professional") && newPlan.equals("starter"));
    }

}