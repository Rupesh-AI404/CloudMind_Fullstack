package com.cloudmind.controller;

import com.cloudmind.model.Subscriber;
import com.cloudmind.model.User;
import com.cloudmind.repository.SubscriberRepository;
import com.cloudmind.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class SubscriptionController {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private EmailService emailService;

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


    @GetMapping("/downloadReport")
    public ResponseEntity<byte[]> downloadReport(HttpSession session) {
        try {
            String email = (String) session.getAttribute("email");
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Generate PDF report
            byte[] pdfBytes = generateUserReport(email, session);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "subscription-report.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/cancelSubscription")
    @ResponseBody
    public Map<String, Object> cancelSubscription(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userEmail = (String) session.getAttribute("email");
            String userName = (String) session.getAttribute("userName");

            if (userEmail == null) {
                Object activeUser = session.getAttribute("activeUser");
                if (activeUser instanceof User) {
                    userEmail = ((User) activeUser).getEmail();
                    userName = ((User) activeUser).getUsername(); // Get username too
                }
            }

            if (userEmail == null) {
                response.put("success", false);
                response.put("message", "Session expired");
                return response;
            }

            System.out.println("🔄 Cancelling subscription for: " + userEmail);

            // Find and cancel ALL active subscriptions
            List<Subscriber> allSubscriptions = subscriberRepository.findByEmail(userEmail);
            boolean subscriptionCancelled = false;
            String cancelledPlan = "";

            for (Subscriber sub : allSubscriptions) {
                if (!"CANCELLED".equalsIgnoreCase(sub.getStatus())) {
                    cancelledPlan = sub.getPlan();
                    sub.setStatus("CANCELLED");
                    subscriberRepository.save(sub);
                    subscriptionCancelled = true;
                    System.out.println("✅ Cancelled subscription: " + sub.getPlan() + " for " + userEmail);
                }
            }

            if (subscriptionCancelled) {
                // Set cancellation flag and clear session
                session.setAttribute("subscriptionCancelled", true);
                session.setAttribute("hasActiveSubscription", false);
                session.removeAttribute("activeSubscription");

                // 🔥 SEND CANCELLATION EMAILS - THIS WAS MISSING!
                sendCancellationEmails(userEmail, userName != null ? userName : "User", cancelledPlan);

                System.out.println("✅ Subscription cancelled successfully for: " + userEmail);

                response.put("success", true);
                response.put("message", "Subscription cancelled successfully");
            } else {
                response.put("success", false);
                response.put("message", "No active subscription found");
            }

        } catch (Exception e) {
            System.err.println("❌ Error cancelling subscription: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    private byte[] generateUserReport(String email, HttpSession session) throws Exception {
        try {
            // Get actual user subscription data
            List<Subscriber> activeSubscriptions = subscriberRepository.findByEmailAndStatus(email, "ACTIVE");
            Subscriber subscription = null;

            if (!activeSubscriptions.isEmpty()) {
                subscription = activeSubscriptions.get(0);
            }

            StringBuilder reportContent = new StringBuilder();
            reportContent.append("===============================================\n");
            reportContent.append("           CLOUD MIND SUBSCRIPTION REPORT      \n");
            reportContent.append("===============================================\n\n");

            reportContent.append("USER INFORMATION:\n");
            reportContent.append("-----------------\n");
            reportContent.append("Name: ").append(session.getAttribute("activeUser")).append("\n");
            reportContent.append("Email: ").append(email).append("\n");
            reportContent.append("Report Generated: ").append(new Date()).append("\n\n");

            reportContent.append("SUBSCRIPTION DETAILS:\n");
            reportContent.append("---------------------\n");
            if (subscription != null) {
                reportContent.append("Plan: ").append(subscription.getPlan().toUpperCase()).append("\n");
                reportContent.append("Status: ").append(subscription.getStatus()).append("\n");
                reportContent.append("Billing Cycle: Monthly\n");
                reportContent.append("Start Date: ").append(subscription.getCreatedAt() != null ? subscription.getCreatedAt() : "N/A").append("\n");

                Integer planPrice = PLAN_PRICES.get(subscription.getPlan().toLowerCase());
                reportContent.append("Monthly Fee: $").append(planPrice != null ? planPrice : "N/A").append("\n");
            } else {
                reportContent.append("Plan: No Active Subscription\n");
                reportContent.append("Status: Inactive\n");
            }

            reportContent.append("\nACCOUNT SUMMARY:\n");
            reportContent.append("----------------\n");
            reportContent.append("Member Since: January 2025\n");
            reportContent.append("Total Campaigns: 8\n");
            reportContent.append("Active Campaigns: 3\n");
            reportContent.append("Reports Generated: 1\n\n");

            reportContent.append("FEATURES INCLUDED:\n");
            reportContent.append("------------------\n");
            if (subscription != null) {
                String plan = subscription.getPlan().toLowerCase();
                switch (plan) {
                    case "starter":
                        reportContent.append("✓ Basic Dashboard\n");
                        reportContent.append("✓ 5 Campaigns\n");
                        reportContent.append("✓ Email Support\n");
                        break;
                    case "professional":
                        reportContent.append("✓ Advanced Dashboard\n");
                        reportContent.append("✓ Unlimited Campaigns\n");
                        reportContent.append("✓ Priority Support\n");
                        reportContent.append("✓ Analytics Reports\n");
                        break;
                    case "enterprise":
                        reportContent.append("✓ Enterprise Dashboard\n");
                        reportContent.append("✓ Unlimited Everything\n");
                        reportContent.append("✓ 24/7 Support\n");
                        reportContent.append("✓ Custom Integrations\n");
                        reportContent.append("✓ Dedicated Account Manager\n");
                        break;
                }
            } else {
                reportContent.append("No active subscription\n");
            }

            reportContent.append("\n===============================================\n");
            reportContent.append("         Thank you for using Cloud Mind!       \n");
            reportContent.append("===============================================\n");

            return reportContent.toString().getBytes("UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Failed to generate report: " + e.getMessage());
        }
    }

    private void sendCancellationEmails(String userEmail, String userName, String planName) {
        try {
            // Email to user
            String userSubject = "Subscription Cancelled - Cloud Mind";
            String userMessage = String.format(
                    "Dear %s,\n\n" +
                            "Your %s subscription has been successfully cancelled.\n\n" +
                            "Cancellation Details:\n" +
                            "- Account: %s\n" +
                            "- Plan: %s\n" +
                            "- Cancelled on: %s\n" +
                            "- Access expires: End of current billing period\n\n" +
                            "Your account will remain active until the end of your current billing cycle.\n" +
                            "You can resubscribe at any time by visiting our subscription page.\n\n" +
                            "If you have any questions, please contact our support team.\n\n" +
                            "Thank you for using Cloud Mind!\n\n" +
                            "Best regards,\n" +
                            "Cloud Mind Team",
                    userName, planName.toUpperCase(), userEmail, planName.toUpperCase(), new Date()
            );

            // Send email to user
            emailService.sendEmail(userEmail, userSubject, userMessage);
            System.out.println("📧 Cancellation email sent to user: " + userEmail);

            // Email to admin - FIX THE ADMIN MESSAGE
            String adminEmail = "sabiraventures1@gmail.com";
            String adminSubject = "🚨 User Subscription Cancelled - " + planName.toUpperCase();
            String adminMessage = String.format(
                    "SUBSCRIPTION CANCELLATION ALERT\n\n" +
                            "A user has cancelled their subscription:\n\n" +
                            "📋 DETAILS:\n" +
                            "- User Name: %s\n" +
                            "- Email: %s\n" +
                            "- Plan: %s\n" +
                            "- Cancelled on: %s\n\n" +
                            "💡 ACTION ITEMS:\n" +
                            "- Review cancellation reason\n" +
                            "- Consider follow-up retention email\n" +
                            "- Update billing system\n" +
                            "- Monitor churn metrics\n\n" +
                            "This is an automated notification from Cloud Mind system.\n\n" +
                            "Cloud Mind Admin Panel",
                    userName, userEmail, planName.toUpperCase(), new Date()
            );

            // Send email to admin
            emailService.sendEmail(adminEmail, adminSubject, adminMessage);
            System.out.println("📧 Cancellation notification sent to admin");

        } catch (Exception e) {
            System.err.println("❌ Failed to send cancellation emails: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @PostMapping("/refreshDashboard")
    @ResponseBody
    public Map<String, Object> refreshDashboard(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            String userEmail = (String) session.getAttribute("email");
            if (userEmail == null) {
                response.put("success", false);
                return response;
            }


            // Check for any NON-CANCELLED subscriptions
            List<Subscriber> allSubscriptions = subscriberRepository.findByEmail(userEmail);
            boolean hasActiveSubscription = false;
            Subscriber activeSubscription = null;

            for (Subscriber sub : allSubscriptions) {
                String status = sub.getStatus();
                System.out.println("Checking subscription: " + sub.getPlan() + " with status: " + status);

                if (status != null && !"CANCELLED".equalsIgnoreCase(status)) {
                    hasActiveSubscription = true;
                    activeSubscription = sub;
                    break;
                }
            }

            System.out.println("Final result - hasActiveSubscription: " + hasActiveSubscription);

            // Clear ALL session data completely
            session.removeAttribute("hasActiveSubscription");
            session.removeAttribute("activeSubscription");
            session.removeAttribute("subscriptionDetails");
            session.removeAttribute("upgradeDetails");

            // Set fresh values
            session.setAttribute("hasActiveSubscription", hasActiveSubscription);
            if (hasActiveSubscription && activeSubscription != null) {
                session.setAttribute("activeSubscription", activeSubscription);
            }

            response.put("success", true);
            response.put("hasActiveSubscription", hasActiveSubscription);
            response.put("forceReload", true);

        } catch (Exception e) {
            System.err.println("Error in refreshDashboard: " + e.getMessage());
            response.put("success", false);
        }

        return response;
    }


    @GetMapping("/checkSubscriptionStatus")
    @ResponseBody
    public Map<String, Object> checkSubscriptionStatus(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // FIRST - Check if subscription was just cancelled
            Boolean subscriptionCancelled = (Boolean) session.getAttribute("subscriptionCancelled");
            if (subscriptionCancelled != null && subscriptionCancelled) {
                System.out.println("🔄 Subscription was just cancelled - returning no subscription");

                // Clear the flag
                session.removeAttribute("subscriptionCancelled");

                response.put("hasActiveSubscription", false);
                response.put("justCancelled", true);
                return response;
            }

            String userEmail = (String) session.getAttribute("email");
            if (userEmail == null) {
                response.put("hasActiveSubscription", false);
                return response;
            }

            // Get ALL subscriptions for this email
            List<Subscriber> allSubscriptions = subscriberRepository.findByEmail(userEmail);
            boolean hasActiveSubscription = false;
            Subscriber activeSubscription = null;

            // Check each subscription's status
            for (Subscriber sub : allSubscriptions) {
                System.out.println("Found subscription: " + sub.getPlan() + " | Status: " + sub.getStatus());

                // Only consider it active if status is NOT "CANCELLED"
                if (sub.getStatus() != null && !"CANCELLED".equalsIgnoreCase(sub.getStatus())) {
                    hasActiveSubscription = true;
                    activeSubscription = sub;
                    System.out.println("✅ Active subscription found: " + sub.getPlan());
                    break;
                }
            }

            response.put("hasActiveSubscription", hasActiveSubscription);
            if (hasActiveSubscription) {
                response.put("subscription", activeSubscription);
            }

            return response;

        } catch (Exception e) {
            System.err.println("Error checking subscription: " + e.getMessage());
            response.put("hasActiveSubscription", false);
            return response;
        }
    }

    @GetMapping("/debugSubscription")
    @ResponseBody
    public Map<String, Object> debugSubscription(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        String userEmail = (String) session.getAttribute("email");
        List<Subscriber> allSubs = subscriberRepository.findByEmail(userEmail);

        System.out.println("=== DEBUG SUBSCRIPTION ===");
        for (Subscriber sub : allSubs) {
            System.out.println("Plan: " + sub.getPlan() + " | Status: " + sub.getStatus() + " | ID: " + sub.getId());
        }
        System.out.println("========================");

        response.put("subscriptions", allSubs);
        return response;
    }


}





