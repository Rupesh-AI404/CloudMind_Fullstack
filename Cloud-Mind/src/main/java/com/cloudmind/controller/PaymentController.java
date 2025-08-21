package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import com.cloudmind.model.User;
import com.cloudmind.repository.SubscriberRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaymentController {

    private final SubscriberRepository repo;

    public PaymentController(SubscriberRepository repo) { this.repo = repo; }

    @Value("${app.hash-salt:cmind-default-salt-change-me}")
    private String hashSalt;

    @GetMapping("/payment")
    public String payment(HttpSession session, Model model, @RequestParam(value="success", required=false) String success) {
        if (session.getAttribute("activeUser") == null) return "redirect:/login";

        Subscriber subscriber = (Subscriber) session.getAttribute("subscriber");
        if (subscriber == null) return "redirect:/subscription";

        model.addAttribute("subscriber", subscriber);
        model.addAttribute("selectedPlan", session.getAttribute("selectedPlan"));
        model.addAttribute("selectedBilling", session.getAttribute("selectedBilling"));
        model.addAttribute("activeUser", session.getAttribute("activeUser"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
        if ("true".equals(success)) model.addAttribute("success", true);
        return "payment";
    }

    @PostMapping("/payment")
    public String processPayment(
            HttpSession session,
            Model model,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value="cardNumber",  required=false) String cardNumber,
            @RequestParam(value="cardName",    required=false) String cardName,
            @RequestParam(value="expiryDate",  required=false) String expiryDate,
            @RequestParam(value="cvv",         required=false) String cvv,   // never store
            @RequestParam(value="walletPhone", required=false) String walletPhone,
            @RequestParam(value="walletPin",   required=false) String walletPin // never store
    ) {
        if (session.getAttribute("activeUser") == null) return "redirect:/login";

        Subscriber s = (Subscriber) session.getAttribute("subscriber");
        if (s == null) {
            return "redirect:/subscription";
        }

        // validate email matches active users
        Object activeUser = session.getAttribute("activeUser");
        String userEmail = extractUserEmail(activeUser);


        if(!userEmail.equals(s.getEmail()) ) {
            model.addAttribute("error", "Security validation failed");
            return "redirect:/subscription";
        }

        // attach plan/billing chosen earlier
        Object planObj = session.getAttribute("selectedPlan");
        Object billObj = session.getAttribute("selectedBilling");
        if (planObj != null) s.setPlan(planObj.toString());
        if (billObj != null) s.setBilling(billObj.toString());

        s.setPaymentMethod(paymentMethod);

        try{
        // store minimal, safe data
        if ("stripe".equalsIgnoreCase(paymentMethod) || "paypal".equalsIgnoreCase(paymentMethod)) {
            String digits = (cardNumber == null) ? "" : cardNumber.replaceAll("\\D", "");
            if (digits.length() >= 4) s.setCardLast4(digits.substring(digits.length()-4));
            if (!digits.isEmpty()) {
                s.setCardFingerprintHash(DigestUtils.sha256Hex(hashSalt + digits));
            }
        } else if ("esewa".equalsIgnoreCase(paymentMethod) || "khalti".equalsIgnoreCase(paymentMethod)) {
            if (walletPhone != null && !walletPhone.isBlank()) {
                String masked = maskPhone(walletPhone);
                s.setWalletPhoneMasked(masked);
                s.setWalletPhoneHash(DigestUtils.sha256Hex(hashSalt + walletPhone));
            }
        }

        // save subscriber row
        repo.save(s);

        // ✅ success modal trigger
        model.addAttribute("success", true);
        model.addAttribute("plan", s.getPlan());
        model.addAttribute("paymentMethod", s.getPaymentMethod());
        model.addAttribute("activeUser", session.getAttribute("activeUser"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        // ✅ clear sensitive session after payment is done
        session.removeAttribute("subscriber");
        session.removeAttribute("selectedPlan");
        session.removeAttribute("selectedBilling");

        // ✅ stay on payment.html instead of redirect
        return "payment";
    } catch (Exception e) {
            // Handle any exceptions that may occur during processing
            model.addAttribute("error", "Payment processing failed: " + e.getMessage());
            return "payment";
        }
    }

    private String extractUserEmail(Object activeUser) {
        if (activeUser instanceof User) {
            return ((User) activeUser).getEmail(); // If it's just a String email
        } else if (activeUser instanceof String) {
            return (String) activeUser; // Assuming activeUser is of type User
        }
        return null; // Invalid user session
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }

        // Remove all non-digits
        String digits = phone.replaceAll("\\D", "");

        if (digits.length() <= 4) {
            return "*".repeat(digits.length());
        }

        // Show last 4 digits, mask the rest
        String masked = "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
        return masked;
    }


}