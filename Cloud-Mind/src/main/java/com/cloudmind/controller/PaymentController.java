package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import com.cloudmind.model.User;
import com.cloudmind.repository.SubscriberRepository;
import com.cloudmind.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

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
    public String payment(Model model, HttpSession session) {
        return "payment";
    }

    @GetMapping("/payment/success")
    public String processPayment(HttpSession session, Model model, @RequestParam(value="success", required=false) String success) {
        if (session.getAttribute("activeUser") == null) return "redirect:/login";

        Subscriber subscriber = (Subscriber) session.getAttribute("subscriber");
        if (subscriber == null) return "redirect:/subscription";

        model.addAttribute("subscriber", subscriber);
        model.addAttribute("selectedPlan", session.getAttribute("selectedPlan"));
        model.addAttribute("selectedBilling", session.getAttribute("selectedBilling"));
        model.addAttribute("activeUser", session.getAttribute("activeUser"));
        model.addAttribute("userRole", session.getAttribute("userRole"));
        if ("true".equals(success)) model.addAttribute("success", true);
        return "payment-success";
    }

    @PostMapping("/payment")
    public String processPayment(
            HttpSession session,
            Model model,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value="cardNumber",  required=false) String cardNumber,
            @RequestParam(value="cardName",    required=false) String cardName,
            @RequestParam(value="expiryDate",  required=false) String cardExpiryDate,
            @RequestParam(value="cvv",         required=false) String cvv,
            @RequestParam(value="walletPhone", required=false) String walletPhone,
            @RequestParam(value="walletPin",   required=false) String walletPin
    ) {
        if (session.getAttribute("activeUser") == null) return "redirect:/login";

        Subscriber s = (Subscriber) session.getAttribute("subscriber");
        if (s == null) {
            return "redirect:/subscription";
        }

        // Get the actual email from session - NOT from activeUser
        String sessionEmail = (String) session.getAttribute("email");
        String subscriberEmail = s.getEmail();

        System.out.println("Session email: '" + sessionEmail + "'");
        System.out.println("Subscriber email: '" + subscriberEmail + "'");

        // Validate email matches
        if (sessionEmail == null || subscriberEmail == null || !sessionEmail.equals(subscriberEmail)) {
            System.out.println("Security validation failed - email mismatch");
            model.addAttribute("error", "Security validation failed - email mismatch");
            return "redirect:/subscription";
        }

        System.out.println("Email validation passed!");

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

            // Set subscription as active and calculate expiry
            s.setStatus("ACTIVE");
            LocalDateTime subscriptionExpiryDate = calculateExpiryDate(s.getBilling());
            s.setExpiryDate(subscriptionExpiryDate);

            // save subscriber row
            repo.save(s);

            try{
                emailService.sendPaymentConfirmationEmail(
                        s.getEmail(), s.getPlan(), s.getBilling(), s.getPaymentMethod(), "N/A"
                );
                System.out.println("Payment confirmation email sent successfully");
            }catch (Exception e){
                System.out.println("Critical: Failure to send payment confirmation email: " + e.getMessage());
            }

            // success modal trigger
            model.addAttribute("success", true);
            model.addAttribute("plan", s.getPlan());
            model.addAttribute("selectedPlan", s.getPlan());
            model.addAttribute("paymentMethod", s.getPaymentMethod());
            model.addAttribute("activeUser", session.getAttribute("activeUser"));
            model.addAttribute("userRole", session.getAttribute("userRole"));

            // clear sensitive session after payment is done
            session.removeAttribute("subscriber");
            session.removeAttribute("selectedPlan");
            session.removeAttribute("selectedBilling");

            System.out.println("Payment processed successfully, staying on payment page");
            // stay on payment.html instead of redirect
            return "payment";
        } catch (Exception e) {
            System.out.println("Payment processing failed: " + e.getMessage());
            model.addAttribute("error", "Payment processing failed: " + e.getMessage());
            return "payment";
        }
    }

    @PostMapping("/payment-success")
    public String processPaymentSuccess(
            @RequestParam("razorpay_payment_id") String paymentId,
            @RequestParam("razorpay_order_id") String orderId,
            @RequestParam(value = "plan", required = false) String planName,
            @RequestParam(value = "amount", required = false) String amount,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // Get user details from session
            String userEmail = (String) session.getAttribute("email");
            String userName = (String) session.getAttribute("activeUser");
            String selectedPlan = (String) session.getAttribute("selectedPlan");
            String selectedBilling = (String) session.getAttribute("selectedBilling");

            if (userEmail == null || userName == null) {
                redirectAttributes.addFlashAttribute("error", "Session expired. Please login again.");
                return "redirect:/login";
            }

            // Use session plan if not provided in request
            if (planName == null) {
                planName = selectedPlan;
            }

            // Calculate amount if not provided
            if (amount == null) {
                amount = calculateAmount(selectedPlan, selectedBilling);
            }

            // Send payment confirmation email
            emailService.sendPaymentConfirmationEmail(userEmail, userName, planName, amount, paymentId);

            // Clear subscription data from session
            session.removeAttribute("subscriber");
            session.removeAttribute("selectedPlan");
            session.removeAttribute("selectedBilling");

            redirectAttributes.addFlashAttribute("success",
                    "Payment successful! Confirmation email sent to " + userEmail);

            return "redirect:/user-dashboard";

        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Payment processed but email failed to send.");
            return "redirect:/user-dashboard";
        }
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
}