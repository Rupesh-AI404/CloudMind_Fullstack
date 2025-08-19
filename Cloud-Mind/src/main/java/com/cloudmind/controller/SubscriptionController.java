package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SubscriptionController {

    @GetMapping("/subscription")
    public String subscription(HttpSession session, Model model) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }
        model.addAttribute("activeUser", session.getAttribute("activeUser"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
        model.addAttribute("subscriber", new Subscriber()); // Ensure a new instance is available
        return "subscription";
    }

    @PostMapping("/subscription")
    public String submitSubscription(Subscriber subscriber, HttpSession session, Model model,
                                     @RequestParam("plan") String selectedPlan,
                                     @RequestParam("billing") String selectedBilling) {
        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        // Validate subscriber data
        subscriber.setPlan(selectedPlan);
        subscriber.setBilling(selectedBilling);


        // Store subscriber and selections in session
        session.setAttribute("subscriber", subscriber);
        session.setAttribute("selectedPlan", selectedPlan);
        session.setAttribute("selectedBilling", selectedBilling);

        return "redirect:/payment";
    }
}