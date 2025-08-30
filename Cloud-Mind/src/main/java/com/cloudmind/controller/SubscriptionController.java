package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import com.cloudmind.repository.SubscriberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SubscriptionController {

    @Autowired
    private SubscriberRepository subscriberRepository;

    // Plan prices (in dollars)
    private static final Map<String, Integer> PLAN_PRICES = Map.of(
            "starter", 9,
            "professional", 29,
            "enterprise", 49
    );

    @GetMapping("/subscription")
    public String showSubscriptionPage(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("email");
        if (userEmail == null) {
            return "redirect:/login";
        }

        // Add empty subscriber object for the form
        model.addAttribute("subscriber", new Subscriber());

        try {
            // Get user's active subscription
            List<Subscriber> activeSubscriptions = subscriberRepository
                    .findByEmailAndStatus(userEmail, "ACTIVE");

            if (!activeSubscriptions.isEmpty()) {
                Subscriber subscription = activeSubscriptions.get(0);
                model.addAttribute("currentPlan", subscription.getPlan());
                model.addAttribute("subscription", subscription);
            }

            model.addAttribute("planPrices", PLAN_PRICES);

        } catch (Exception e) {
            model.addAttribute("error", "Error loading subscription: " + e.getMessage());
        }

        return "subscription";
    }


    @PostMapping("/subscription")
    public String processSubscription(@ModelAttribute Subscriber subscriber,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {

        String userEmail = (String) session.getAttribute("email");
        if (userEmail == null) {
            return "redirect:/login";
        }

        try {
            // Set user email and default status
            subscriber.setEmail(userEmail);
            subscriber.setStatus("PENDING");

            // Get plan price
            String plan = subscriber.getPlan();
            Integer amount = PLAN_PRICES.get(plan.toLowerCase());

            if (amount == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid plan selected");
                return "redirect:/subscription";
            }

            // Save subscriber temporarily (will be activated after payment)
            Subscriber savedSubscriber = subscriberRepository.save(subscriber);

            // Store subscription details in session for payment
            Map<String, Object> subscriptionDetails = new HashMap<>();
            subscriptionDetails.put("subscriberId", savedSubscriber.getId());
            subscriptionDetails.put("plan", plan);
            subscriptionDetails.put("amount", amount);

            session.setAttribute("subscriptionDetails", subscriptionDetails);

            // Redirect to payment
            return "redirect:/payment?amount=" + amount + "&plan=" + plan;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing subscription: " + e.getMessage());
            return "redirect:/subscription";
        }
    }

    @PostMapping("/change-plan")
    public String changePlan(@RequestParam String newPlan,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        String userEmail = (String) session.getAttribute("email");
        if (userEmail == null) {
            return "redirect:/login";
        }

        try {
            // Get user's active subscription
            List<Subscriber> activeSubscriptions = subscriberRepository
                    .findByEmailAndStatus(userEmail, "ACTIVE");

            if (!activeSubscriptions.isEmpty()) {
                Subscriber subscription = activeSubscriptions.get(0);
                String currentPlan = subscription.getPlan();

                // Check if it's an upgrade or downgrade
                if (isUpgrade(currentPlan, newPlan)) {
                    // For upgrades - redirect to payment
                    int upgradePrice = getUpgradePrice(currentPlan, newPlan);

                    // Store upgrade details in session
                    Map<String, Object> upgradeDetails = new HashMap<>();
                    upgradeDetails.put("subscriptionId", subscription.getId());
                    upgradeDetails.put("currentPlan", currentPlan);
                    upgradeDetails.put("newPlan", newPlan);
                    upgradeDetails.put("upgradePrice", upgradePrice);

                    session.setAttribute("upgradeDetails", upgradeDetails);

                    return "redirect:/upgrade-payment/upgrade?amount=" + upgradePrice + "&plan=" + newPlan;

                } else {
                    // For downgrades - no payment needed, just change
                    subscription.setPlan(newPlan);
                    subscriberRepository.save(subscription);

                    redirectAttributes.addFlashAttribute("success",
                            "✅ Plan changed to " + newPlan.toUpperCase() +
                                    "! Changes take effect immediately. Next bill will be adjusted.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "No active subscription found");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error changing plan: " + e.getMessage());
        }

        return "redirect:/user-dashboard";
    }

    // Helper method to check if it's an upgrade
    private boolean isUpgrade(String currentPlan, String newPlan) {
        if (currentPlan == null || newPlan == null) {
            return false;
        }

        Integer currentPrice = PLAN_PRICES.get(currentPlan.toLowerCase());
        Integer newPrice = PLAN_PRICES.get(newPlan.toLowerCase());

        if (currentPrice == null || newPrice == null) {
            return false;
        }

        return newPrice > currentPrice;
    }

    // Helper method to calculate upgrade price difference
    private int getUpgradePrice(String currentPlan, String newPlan) {
        if (currentPlan == null || newPlan == null) {
            return 0;
        }

        Integer currentPrice = PLAN_PRICES.get(currentPlan.toLowerCase());
        Integer newPrice = PLAN_PRICES.get(newPlan.toLowerCase());

        if (currentPrice == null || newPrice == null) {
            return 0;
        }

        return Math.max(0, newPrice - currentPrice);
    }
}