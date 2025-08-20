package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import com.cloudmind.repository.SubscriberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentController {

    private final SubscriberRepository subscriberRepo;

    public PaymentController(SubscriberRepository subscriberRepo) {
        this.subscriberRepo = subscriberRepo;
    }

    @GetMapping("/payment")
    public String showPaymentPage(@RequestParam(value = "success", required = false) String success,
                                  Model model,
                                  HttpSession session) {
        if ("true".equals(success)) {
            model.addAttribute("success", true);
        }
        Subscriber subscriber = (Subscriber) session.getAttribute("subscriber");
        if (subscriber == null) {
            System.out.println("Subscriber is null in session, redirecting to /subscription");
            return "payment";
        }

        // Debug: Log session data
        System.out.println("Subscriber: " + subscriber);
        System.out.println("Selected Plan: " + session.getAttribute("selectedPlan"));
        System.out.println("Selected Billing: " + session.getAttribute("selectedBilling"));

        model.addAttribute("subscriber", subscriber);
        model.addAttribute("selectedPlan", session.getAttribute("selectedPlan"));
        model.addAttribute("selectedBilling", session.getAttribute("selectedBilling"));
        model.addAttribute("activeUser", session.getAttribute("activeUser"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        if ("true".equals(success)) model.addAttribute("success", true);
        return "payment";
    }

    @PostMapping("/payment")
    public String handlePayment(
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String expiryDate,
            @RequestParam(required = false) String cvv,
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String walletPhone,
            @RequestParam(required = false) String walletPin,
            HttpSession session,
            Model model) {

        // retrieve selected plan + billing info from session
        String selectedPlan = (String) session.getAttribute("selectedPlan");
        String selectedBilling = (String) session.getAttribute("selectedBilling");
        String email = (String) session.getAttribute("activeUser");

        // build subscriber entity and save
        Subscriber subscriber = new Subscriber();
        subscriber.setEmail(email);
        subscriber.setPlan(selectedPlan);
        subscriber.setBilling(selectedBilling);
        subscriber.setPaymentMethod(paymentMethod);

        subscriberRepo.save(subscriber);

        model.addAttribute("success", true);
        // ✅ redirect back to payment page with success flag
        return "payment";
    }

}