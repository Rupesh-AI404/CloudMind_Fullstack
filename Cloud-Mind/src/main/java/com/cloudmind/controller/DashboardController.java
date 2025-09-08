package com.cloudmind.controller;

import com.cloudmind.model.ContactMessage;
import com.cloudmind.model.Subscriber;
import com.cloudmind.model.User;
import com.cloudmind.repository.SubscriberRepository;
import com.cloudmind.repository.UserRepository;
import com.cloudmind.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.cloudmind.repository.ContactMessageRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private SubscriberRepository repo;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private ContactMessageRepository contactRepo;

    @Autowired
    private EmailService emailService;

    @GetMapping("/user-dashboard")
    public String userDashboard(HttpSession session, Model model, @RequestParam(required = false) String cancelled) {

        // FIRST - Check for cancellation
        Boolean subscriptionCancelled = (Boolean) session.getAttribute("subscriptionCancelled");
        boolean wasCancelled = (cancelled != null && "true".equals(cancelled)) ||
                (subscriptionCancelled != null && subscriptionCancelled);

        if (wasCancelled) {
            System.out.println("🔄 Dashboard: Subscription was cancelled - forcing no subscription state");

            // Clear all subscription-related session data
            session.removeAttribute("subscriptionCancelled");
            session.setAttribute("hasActiveSubscription", false);
            session.removeAttribute("activeSubscription");

            // Force no subscription state
            model.addAttribute("hasActiveSubscription", false);
            model.addAttribute("activeSubscription", null);
            model.addAttribute("activeUser", session.getAttribute("activeUser"));
            model.addAttribute("userRole", session.getAttribute("userRole"));
            model.addAttribute("userEmail", session.getAttribute("email"));

            return "user-dashboard";
        }

        System.out.println("=== User Dashboard Access ===");

        // Check authentication
        if (session.getAttribute("activeUser") == null) {
            System.out.println("No active user, redirecting to login");
            return "redirect:/login";
        }

        // Get user email from session
        String userEmail = null;
        Object activeUser = session.getAttribute("activeUser");

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

        // Check for NON-CANCELLED subscriptions only
        boolean hasActiveSubscription = false;
        Subscriber activeSubscription = null;

        try {
            List<Subscriber> allSubscriptions = repo.findByEmail(userEmail);
            System.out.println("Found " + allSubscriptions.size() + " total subscriptions for email: " + userEmail);

            // Find the latest NON-CANCELLED subscription
            for (int i = allSubscriptions.size() - 1; i >= 0; i--) {
                Subscriber sub = allSubscriptions.get(i);
                String status = sub.getStatus();
                System.out.println("Checking subscription: Plan=" + sub.getPlan() + ", Status=" + status);

                if (status == null || !status.equalsIgnoreCase("CANCELLED")) {
                    activeSubscription = sub;
                    hasActiveSubscription = true;
                    System.out.println("✅ Found active subscription: " + sub.getPlan());
                    break;
                }
            }

            if (!hasActiveSubscription) {
                System.out.println("❌ No active subscription found (all are cancelled or none exist)");
            }

        } catch (Exception e) {
            System.out.println("Error fetching subscription: " + e.getMessage());
            e.printStackTrace();
        }

        // Set session and model attributes
        session.setAttribute("hasActiveSubscription", hasActiveSubscription);
        model.addAttribute("hasActiveSubscription", hasActiveSubscription);
        model.addAttribute("activeUser", activeUser);
        model.addAttribute("userRole", session.getAttribute("userRole"));
        model.addAttribute("userEmail", userEmail);

        if (hasActiveSubscription && activeSubscription != null) {
            session.setAttribute("activeSubscription", activeSubscription);
            model.addAttribute("activeSubscription", activeSubscription);
            model.addAttribute("userSubscription", activeSubscription);
            model.addAttribute("subscriptionDate", "Recently purchased");
            System.out.println("✅ Added active subscription to model: " + activeSubscription.getPlan());
        } else {
            session.removeAttribute("activeSubscription");
            model.addAttribute("activeSubscription", null);
            model.addAttribute("userSubscription", null);
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

        try {
            System.out.println("=== LOADING ADMIN DASHBOARD ===");

            // Get all subscribers from database
            List<Subscriber> subscribers = repo.findAll();
            long totalUsers = userRepository.count();
            long totalSubscribers = subscribers.size();

            System.out.println("Found " + totalSubscribers + " subscribers in database");
            System.out.println("Found " + totalUsers + " total users in database");

            // Calculate revenue
            BigDecimal totalRevenue = calculateTotalRevenue(subscribers);
            BigDecimal monthlyRevenue = calculateMonthlyRevenue(subscribers);
            double conversionRate = totalUsers > 0 ? (double) totalSubscribers / totalUsers * 100 : 0;
            Map<String, Long> packageDistribution = getPackageDistribution(subscribers);


            // Add contact messages data
            List<ContactMessage> allMessages = contactRepo.findAllByOrderByCreatedAtDesc();
            List<ContactMessage> unreadMessages = contactRepo.findByIsReadFalseOrderByCreatedAtDesc();
            long totalMessages = allMessages.size();
            long unreadCount = unreadMessages.size();

            System.out.println("Found " + totalMessages + " total contact messages");
            System.out.println("Found " + unreadCount + " unread contact messages");

            // Calculate conversion rate
//            double conversionRate = totalUsers > 0 ? (double) totalSubscribers / totalUsers * 100 : 0;
//
//            // Get package distribution
//            Map<String, Long> packageDistribution = getPackageDistribution(subscribers);

            // Add session attributes
            model.addAttribute("activeUser", session.getAttribute("activeUser"));
            model.addAttribute("email", session.getAttribute("email"));
            model.addAttribute("userRole", session.getAttribute("userRole"));

            // Add dashboard data
            model.addAttribute("activeUser", session.getAttribute("activeUser"));
            model.addAttribute("email", session.getAttribute("email"));
            model.addAttribute("userRole", session.getAttribute("userRole"));
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("activeCampaigns", totalSubscribers);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("conversionRate", Math.round(conversionRate * 100.0) / 100.0);
            model.addAttribute("subscribers", subscribers);
            model.addAttribute("packageDistribution", packageDistribution);
            model.addAttribute("monthlyRevenue", monthlyRevenue);

            System.out.println("✅ Dashboard data loaded successfully");
            System.out.println("   - Total Users: " + totalUsers);
            System.out.println("   - Total Subscribers: " + totalSubscribers);
            System.out.println("   - Total Revenue: $" + totalRevenue);

            // Add contact messages attributes
            model.addAttribute("contactMessages", allMessages);
            model.addAttribute("unreadMessages", unreadMessages);
            model.addAttribute("totalMessages", totalMessages);
            model.addAttribute("unreadCount", unreadCount);

        } catch (Exception e) {
            System.out.println("❌ Error loading admin dashboard: " + e.getMessage());
            e.printStackTrace();

            // Add session attributes even on error

            // Add session attributes and empty data on error
            model.addAttribute("activeUser", session.getAttribute("activeUser"));
            model.addAttribute("email", session.getAttribute("email"));
            model.addAttribute("userRole", session.getAttribute("userRole"));
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("activeCampaigns", 0L);
            model.addAttribute("totalRevenue", BigDecimal.ZERO);
            model.addAttribute("conversionRate", 0.0);
            model.addAttribute("subscribers", new ArrayList<>());
            model.addAttribute("packageDistribution", getEmptyPackageDistribution());
            model.addAttribute("monthlyRevenue", BigDecimal.ZERO);



            // Empty contact data on error
            model.addAttribute("contactMessages", new ArrayList<>());
            model.addAttribute("unreadMessages", new ArrayList<>());
            model.addAttribute("totalMessages", 0L);
            model.addAttribute("unreadCount", 0L);

            model.addAttribute("error", "Error loading dashboard: " + e.getMessage());
        }

        return "admin-dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if ("ADMIN".equals(userRole)) {
            return "redirect:/admin-dashboard";
        } else {
            return "redirect:/user-dashboard";
        }
    }

    // Add debug endpoint
    @GetMapping("/admin/debug")
    @ResponseBody
    public String debugInfo() {
        StringBuilder debug = new StringBuilder();
        debug.append("=== ADMIN DASHBOARD DEBUG ===\n\n");

        try {
            List<Subscriber> subscribers = repo.findAll();
            debug.append("Direct repository call - Subscribers found: ").append(subscribers.size()).append("\n\n");

            if (!subscribers.isEmpty()) {
                debug.append("Subscriber details:\n");
                for (int i = 0; i < Math.min(subscribers.size(), 3); i++) {
                    Subscriber s = subscribers.get(i);
                    debug.append("Subscriber ").append(i + 1).append(":\n");
                    debug.append("  - ID: ").append(s.getId()).append("\n");
                    debug.append("  - Email: ").append(s.getEmail()).append("\n");
                    debug.append("  - Plan: ").append(s.getPlan()).append("\n");
                    debug.append("  - Status: ").append(s.getStatus()).append("\n");
                    debug.append("  - First Name: ").append(s.getFirstName()).append("\n");
                    debug.append("  - Last Name: ").append(s.getLastName()).append("\n");
                    debug.append("  ---\n");
                }
            } else {
                debug.append("❌ NO SUBSCRIBERS FOUND!\n");
            }

            long totalUsers = userRepository.count();
            debug.append("\nTotal users in database: ").append(totalUsers).append("\n");

        } catch (Exception e) {
            debug.append("ERROR: ").append(e.getMessage()).append("\n");
            debug.append("Stack trace: ").append(Arrays.toString(e.getStackTrace()));
        }

        return debug.toString();
    }

    // Helper methods for calculations
    private BigDecimal calculateTotalRevenue(List<Subscriber> subscribers) {
        return subscribers.stream()
                .map(this::getSubscriberRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateMonthlyRevenue(List<Subscriber> subscribers) {
        return subscribers.stream()
                .filter(s -> "monthly".equalsIgnoreCase(s.getBilling()))
                .map(this::getSubscriberRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getSubscriberRevenue(Subscriber subscriber) {
        String plan = subscriber.getPlan();
        if (plan == null) return BigDecimal.ZERO;

        switch (plan.toLowerCase()) {
            case "starter":
                return new BigDecimal("999");
            case "pro":
                return new BigDecimal("2499");
            case "enterprise":
                return new BigDecimal("4999");
            default:
                return BigDecimal.ZERO;
        }
    }

    private Map<String, Long> getPackageDistribution(List<Subscriber> subscribers) {
        Map<String, Long> planCounts = new HashMap<>();
        planCounts.put("starter", 0L);
        planCounts.put("pro", 0L);
        planCounts.put("enterprise", 0L);

        for (Subscriber subscriber : subscribers) {
            String plan = subscriber.getPlan();
            if (plan != null) {
                planCounts.put(plan.toLowerCase(),
                        planCounts.getOrDefault(plan.toLowerCase(), 0L) + 1);
            }
        }

        return planCounts;
    }

    private Map<String, Long> getEmptyPackageDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("starter", 0L);
        distribution.put("pro", 0L);
        distribution.put("enterprise", 0L);
        return distribution;
    }


    @GetMapping("/subscriber/{id}")
    @ResponseBody
    public ResponseEntity<Subscriber> getSubscriber(@PathVariable Long id) {
        try {
            Optional<Subscriber> subscriber = repo.findById(id);
            if (subscriber.isPresent()) {
                System.out.println("✅ Found subscriber: " + subscriber.get().getEmail());
                return ResponseEntity.ok(subscriber.get());
            } else {
                System.out.println("❌ Subscriber not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("❌ Error fetching subscriber: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/cancel-subscription/{subscriberId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> adminCancelSubscription(@PathVariable Long subscriberId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find the subscriber
            Optional<Subscriber> subscriberOpt = repo.findById(subscriberId);

            if (!subscriberOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Subscriber not found");
                return ResponseEntity.notFound().build();
            }

            Subscriber subscriber = subscriberOpt.get();

            // Delete the subscriber from database
            repo.delete(subscriber);

            System.out.println("✅ Subscriber deleted successfully: " + subscriber.getEmail());

            response.put("success", true);
            response.put("message", "Subscriber deleted successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error deleting subscriber: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error deleting subscriber: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private void sendAdminCancellationEmails(String userEmail, String userName, String planName, String cancelledBy) {
        try {
            // Email to user
            String userSubject = "Subscription Cancelled by Admin - Cloud Mind";
            String userMessage = String.format(
                    "Dear %s,\n\n" +
                            "Your %s subscription has been cancelled by our administration team.\n\n" +
                            "Cancellation Details:\n" +
                            "- Account: %s\n" +
                            "- Plan: %s\n" +
                            "- Cancelled by: %s\n" +
                            "- Cancelled on: %s\n" +
                            "- Access expires: Immediately\n\n" +
                            "If you believe this cancellation was made in error, please contact our support team immediately.\n" +
                            "You can resubscribe at any time by visiting our subscription page.\n\n" +
                            "If you have any questions, please contact our support team.\n\n" +
                            "Best regards,\n" +
                            "Cloud Mind Team",
                    userName, planName.toUpperCase(), userEmail, planName.toUpperCase(), cancelledBy, new Date()
            );

            // Send email to user - CHANGED FROM STATIC TO INSTANCE
            emailService.sendEmail(userEmail, userSubject, userMessage);
            System.out.println("📧 Admin cancellation email sent to user: " + userEmail);

            // Email to admin
            String adminEmail = "sabiraventures1@gmail.com";
            String adminSubject = "🔴 ADMIN CANCELLATION - " + planName.toUpperCase() + " Subscription";
            String adminMessage = String.format(
                    "ADMIN SUBSCRIPTION CANCELLATION COMPLETED\n\n" +
                            "An admin has cancelled a user subscription:\n\n" +
                            "📋 CANCELLATION DETAILS:\n" +
                            "- User Name: %s\n" +
                            "- Email: %s\n" +
                            "- Plan: %s\n" +
                            "- Cancelled by: %s\n" +
                            "- Cancelled on: %s\n" +
                            "- Reason: Administrative Action\n\n" +
                            "💡 FOLLOW-UP ACTIONS:\n" +
                            "- User has been notified via email\n" +
                            "- Subscription access terminated immediately\n" +
                            "- Revenue tracking updated\n" +
                            "- Consider customer retention follow-up\n\n" +
                            "📊 IMPACT:\n" +
                            "- Monthly Revenue Impact: -$%s\n" +
                            "- Active Subscriber Count: Reduced by 1\n\n" +
                            "This is an automated notification from Cloud Mind Admin Panel.\n\n" +
                            "Cloud Mind Administration",
                    userName, userEmail, planName.toUpperCase(), cancelledBy, new Date(),
                    planName.equals("enterprise") ? "4,999" : (planName.equals("pro") ? "2,499" : "999")
            );

            // Send email to admin - CHANGED FROM STATIC TO INSTANCE
            emailService.sendEmail(adminEmail, adminSubject, adminMessage);
            System.out.println("📧 Admin cancellation notification sent to admin");

        } catch (Exception e) {
            System.err.println("❌ Failed to send admin cancellation emails: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @PostMapping("/admin/send-email")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendEmailToUser(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String to = request.get("to");
            String subject = request.get("subject");
            String message = request.get("message");

            if (to == null || subject == null || message == null) {
                response.put("success", false);
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }

            // Send email using your EmailService
            emailService.sendEmail(to, subject, message);

            System.out.println("✅ Email sent successfully to: " + to);

            response.put("success", true);
            response.put("message", "Email sent successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error sending email: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



}

