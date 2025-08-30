package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import com.cloudmind.model.User;
import com.cloudmind.service.EmailService;
import com.cloudmind.repository.SubscriberRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
public class PaymentController {

    private final SubscriberRepository repo;
    private final EmailService emailService;


    @Value("${app.hash-salt:cmind-default-salt-change-me}")
    private String hashSalt;

    public PaymentController(SubscriberRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    @GetMapping("/payment")
    public String payment(
            @RequestParam(required = false) String amount,
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) String billing,
            HttpSession session,
            Model model) {

        System.out.println("=== Payment GET request ===");
        System.out.println("Amount: " + amount + ", Plan: " + plan + ", Billing: " + billing);

        if (session.getAttribute("activeUser") == null) {
            System.out.println("No active user - redirecting to login");
            return "redirect:/login";
        }

        // Check if subscriber already exists in session
        Subscriber subscriber = (Subscriber) session.getAttribute("subscriber");

        // If no subscriber in session but we have URL parameters, create one
        if (subscriber == null && plan != null) {
            System.out.println("Creating new subscriber from URL parameters");

            // Handle both String and User object types for activeUser
            Object activeUserObj = session.getAttribute("activeUser");
            String userEmail = null;

            if (activeUserObj instanceof User) {
                userEmail = ((User) activeUserObj).getEmail();
            } else if (activeUserObj instanceof String) {
                // If activeUser is stored as String, try to get email from session
                userEmail = (String) session.getAttribute("email");
                if (userEmail == null) {
                    userEmail = (String) activeUserObj; // assume it's the email
                }
            }

            if (userEmail == null) {
                System.out.println("Could not determine user email - redirecting to login");
                return "redirect:/login";
            }

            subscriber = new Subscriber();
            subscriber.setEmail(userEmail);
            subscriber.setPlan(plan);
            subscriber.setBilling(billing != null ? billing : "monthly");

            // Store in session
            session.setAttribute("subscriber", subscriber);
            session.setAttribute("selectedPlan", plan);
            session.setAttribute("selectedBilling", billing != null ? billing : "monthly");

            System.out.println("Subscriber created and stored in session: " + subscriber.getEmail());
        }

        // If still no subscriber, redirect to subscription
        if (subscriber == null) {
            System.out.println("No subscriber data available - redirecting to subscription");
            return "redirect:/subscription";
        }

        // Add data to model for the template
        model.addAttribute("selectedPlan", subscriber.getPlan());
        model.addAttribute("selectedBilling", subscriber.getBilling());
        model.addAttribute("amount", amount);

        System.out.println("Showing payment page for plan: " + subscriber.getPlan());
        return "payment";
    }

    @PostMapping("/payment")
    public String processPayment(
            HttpSession session,
            Model model,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value="cardNumber", required=false) String cardNumber,
            @RequestParam(value="cardName", required=false) String cardName,
            @RequestParam(value="expiryDate", required=false) String cardExpiryDate,
            @RequestParam(value="cvv", required=false) String cvv,
            @RequestParam(value="walletPhone", required=false) String walletPhone,
            @RequestParam(value="walletPin", required=false) String walletPin
    ) {
        System.out.println("=== Payment POST processing started ===");

        if (session.getAttribute("activeUser") == null) {
            System.out.println("No active user - redirecting to login");
            return "redirect:/login";
        }

        Subscriber s = (Subscriber) session.getAttribute("subscriber");
        if (s == null) {
            System.out.println("No subscriber in session - redirecting to subscription");
            return "redirect:/subscription";
        }

        System.out.println("Processing payment for: " + s.getEmail() + ", Plan: " + s.getPlan());

        // Validate payment method
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            model.addAttribute("error", "Please select a payment method");
            model.addAttribute("selectedPlan", s.getPlan());
            model.addAttribute("selectedBilling", s.getBilling());
            return "payment";
        }

        // Set payment details
        s.setPaymentMethod(paymentMethod);

        // Validate based on payment method
        if (paymentMethod.equals("stripe") || paymentMethod.equals("paypal")) {
            if (cardNumber == null || cardNumber.trim().isEmpty() ||
                    cardName == null || cardName.trim().isEmpty() ||
                    cardExpiryDate == null || cardExpiryDate.trim().isEmpty() ||
                    cvv == null || cvv.trim().isEmpty()) {

                model.addAttribute("error", "Please fill in all card details");
                model.addAttribute("selectedPlan", s.getPlan());
                model.addAttribute("selectedBilling", s.getBilling());
                return "payment";
            }
        } else if (paymentMethod.equals("esewa") || paymentMethod.equals("khalti")) {
            if (walletPhone == null || walletPhone.trim().isEmpty() ||
                    walletPin == null || walletPin.trim().isEmpty()) {

                model.addAttribute("error", "Please fill in all wallet details");
                model.addAttribute("selectedPlan", s.getPlan());
                model.addAttribute("selectedBilling", s.getBilling());
                return "payment";
            }
        }

        try {
            // Save subscriber
            repo.save(s);
            System.out.println("Subscriber saved successfully to database");

            // Send confirmation email
            try {
                emailService.sendPaymentConfirmationEmail(
                        s.getEmail(), s.getPlan(), s.getBilling(), s.getPaymentMethod(), "N/A"
                );
                System.out.println("Payment confirmation email sent successfully");
            } catch (Exception e) {
                System.out.println("Failed to send email: " + e.getMessage());
            }

            // Store success data BEFORE clearing session
            String planName = s.getPlan();
            String billingType = s.getBilling();
            String userEmail = s.getEmail();

            // Clear session data
            session.removeAttribute("subscriber");
            session.removeAttribute("selectedPlan");
            session.removeAttribute("selectedBilling");

            System.out.println("=== Payment completed successfully ===");
            System.out.println("Plan: " + planName + ", Billing: " + billingType + ", Email: " + userEmail);

            // Add success attributes to model instead of redirecting
            model.addAttribute("paymentSuccess", true);
            model.addAttribute("selectedPlan", planName);
            model.addAttribute("selectedBilling", billingType);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("userEmail", userEmail);

            // Return to payment page to show success modal
            return "payment";

        } catch (Exception e) {
            System.out.println("Payment processing failed: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Payment processing failed: " + e.getMessage());
            model.addAttribute("selectedPlan", s.getPlan());
            model.addAttribute("selectedBilling", s.getBilling());
            return "payment";
        }
    }

//    @PostMapping("/payment-success")
//    public String processPaymentSuccess(
//            @RequestParam("razorpay_payment_id") String paymentId,
//            @RequestParam("razorpay_order_id") String orderId,
//            @RequestParam("plan") String planName,
//            @RequestParam("amount") String amount,
//            HttpSession session,
//            RedirectAttributes redirectAttributes) {
//
//        try {
//            // Get user details from session
//            String userEmail = (String) session.getAttribute("email");
//            String userName = (String) session.getAttribute("activeUser");
//
//            if (userEmail == null || userName == null) {
//                redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
//                return "redirect:/login";
//            }
//
//            // Send payment confirmation email
//            emailService.sendPaymentConfirmationEmail(userEmail, userName, planName, amount, paymentId);
//
//            // Add success message
//            redirectAttributes.addFlashAttribute("success",
//                "Payment successful! Confirmation email sent to " + userEmail);
//
//            return "redirect:/user-dashboard";
//
//        } catch (Exception e) {
//            System.err.println("Error processing payment: " + e.getMessage());
//            redirectAttributes.addFlashAttribute("error", "Payment processed but email failed to send.");
//            return "redirect:/user-dashboard";
//        }
//    }


    @GetMapping("/payment/success")
    public String paymentSuccess(
            HttpSession session,
            Model model,
            @RequestParam(value="success", required=false) String success,
            @RequestParam(value="plan", required=false) String plan,
            @RequestParam(value="billing", required=false) String billing,
            @RequestParam(value="method", required=false) String method) {

        System.out.println("=== Payment Success Page ===");

        if (session.getAttribute("activeUser") == null) {
            return "redirect:/login";
        }

        model.addAttribute("activeUser", session.getAttribute("activeUser"));
        model.addAttribute("userRole", session.getAttribute("userRole"));

        if ("true".equals(success)) {
            model.addAttribute("success", true);
            model.addAttribute("selectedPlan", plan);
            model.addAttribute("selectedBilling", billing);
            model.addAttribute("paymentMethod", method);

            System.out.println("Showing success page for plan: " + plan);
        }

        return "payment-success";
    }

    @GetMapping("/test-email")
    @ResponseBody
    public String testEmail(HttpSession session) {
        try {
            String email = (String) session.getAttribute("email");
            String name = (String) session.getAttribute("activeUser");

            if (email == null) {
                return "Please login first to test email";
            }

            emailService.sendPaymentConfirmationEmail(email, name, "Test Plan", "999", "TEST_123");
            return "Test email sent successfully to: " + email;
        } catch (Exception e) {
            return "Email test failed: " + e.getMessage();
        }
    }

    // Helper method to calculate expiry date based on billing cycle
    private LocalDateTime calculateExpiryDate(String billing) {
        LocalDateTime now = LocalDateTime.now();
        if ("yearly".equalsIgnoreCase(billing)) {
            return now.plusYears(1);
        } else {
            return now.plusMonths(1); // monthly
        }
    }

    private String calculateAmount(String plan, String billing) {
        return "999"; // Default amount
    }

    private String extractUserEmail(Object activeUser) {
        if (activeUser instanceof User) {
            return ((User) activeUser).getEmail();
        } else if (activeUser instanceof String) {
            return (String) activeUser;
        }
        return null;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() <= 4) {
            return "*".repeat(digits.length());
        }

        String masked = "*".repeat(digits.length() - 4) + digits.substring(digits.length() - 4);
        return masked;
    }



    @PostMapping("/process-upgrade")
    public String processUpgradePayment(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> upgradeDetails = (Map<String, Object>) session.getAttribute("upgradeDetails");

            if (upgradeDetails == null) {
                redirectAttributes.addFlashAttribute("error", "Upgrade session expired");
                return "redirect:/user-dashboard";
            }

            Long subscriptionId = (Long) upgradeDetails.get("subscriptionId");
            String newPlan = (String) upgradeDetails.get("newPlan");

            Optional<Subscriber> subOpt = repo.findById(subscriptionId);
            if (subOpt.isPresent()) {
                Subscriber subscription = subOpt.get();
                subscription.setPlan(newPlan);
                repo.save(subscription);

                session.removeAttribute("upgradeDetails");

                redirectAttributes.addFlashAttribute("success",
                        "🎉 Payment successful! Your plan has been upgraded to " + newPlan.toUpperCase());
            } else {
                redirectAttributes.addFlashAttribute("error", "Subscription not found");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Payment processing error: " + e.getMessage());
            return "redirect:/upgrade-payment/upgrade";
        }

        return "redirect:/user-dashboard";
    }
}
