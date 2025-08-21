package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import com.cloudmind.model.User;
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
            return "redirect:/signup";
        }
//        model.addAttribute("activeUser", session.getAttribute("activeUser"));
//        model.addAttribute("userRole", session.getAttribute("userRole"));
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

        //Get active user from session
        Object activeUser = session.getAttribute("activeUser");
        String userEmail = null;

        // extract email based on your user model structure
        if (activeUser instanceof User) {
            userEmail = ((User) activeUser).getEmail(); // Assuming activeUser is of type User
            System.out.println("User email from session: " + userEmail);
        } else if (activeUser instanceof String) {
            userEmail = (String) activeUser; // If it's just a String email
            System.out.println("Email string from session: " + userEmail);
        } else {
            model.addAttribute("error", "Invalid user session.");
            model.addAttribute("activeUser", activeUser);
            model.addAttribute("userRole", session.getAttribute("userRole"));
            return "subscription";
        }


        // Debug: Print both emails for comparison
        System.out.println("Session email: '" + userEmail + "'");
        System.out.println("Form email: '" + subscriber.getEmail() + "'");
        System.out.println("Emails equal: " + (userEmail != null && userEmail.equals(subscriber.getEmail())));


        // validate email match
        if(userEmail == null || !userEmail.equals(subscriber.getEmail()) ) {
            System.out.println("Invalid session type: " + activeUser.getClass());
            System.out.println("Email validation failed!");
            model.addAttribute("error", "Email must be match with your account email");
            model.addAttribute("activeUser", activeUser);
            model.addAttribute("userRole", session.getAttribute("userRole"));
            return "subscription";
        }


        System.out.println("Email validation passed!");


        // Validate subscriber data
        subscriber.setPlan(selectedPlan);
        subscriber.setBilling(selectedBilling);
        subscriber.setEmail(userEmail);

        // Store subscriber and selections in session
        session.setAttribute("subscriber", subscriber);
        session.setAttribute("selectedPlan", selectedPlan);
        session.setAttribute("selectedBilling", selectedBilling);

        return "redirect:/payment";
    }
}