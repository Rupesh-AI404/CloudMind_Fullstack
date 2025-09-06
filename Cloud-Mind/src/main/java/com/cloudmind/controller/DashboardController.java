package com.cloudmind.controller;

import com.cloudmind.model.ContactMessage;
import com.cloudmind.model.Subscriber;
import com.cloudmind.model.User;
import com.cloudmind.repository.SubscriberRepository;
import com.cloudmind.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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

    @GetMapping("/user-dashboard")
    public String userDashboard(HttpSession session, Model model) {
        System.out.println("=== User Dashboard Access ===");

        // Check authentication
        if (session.getAttribute("activeUser") == null) {
            System.out.println("No active user, redirecting to login");
            return "redirect:/login";
        }

        // Get user email from session
        String userEmail = null;
        Object activeUser = session.getAttribute("activeUser");

        // Try different ways to get email
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

        // Fetch subscription from database
        // Fetch subscription from database
        Subscriber activeSubscription = null;
        try {
            // Use the new method that gets the latest subscription
            activeSubscription = repo.findFirstByEmailOrderByIdDesc(userEmail);

            if (activeSubscription != null) {
                System.out.println("✅ Found subscription (latest):");
                System.out.println("   ID: " + activeSubscription.getId());
                System.out.println("   Email: " + activeSubscription.getEmail());
                System.out.println("   Plan: " + activeSubscription.getPlan());
                System.out.println("   Billing: " + activeSubscription.getBilling());
                System.out.println("   Payment Method: " + activeSubscription.getPaymentMethod());
            } else {
                System.out.println("❌ No subscription found for email: " + userEmail);
            }

            // Debug: Show how many duplicate subscriptions exist
            List<Subscriber> allForEmail = repo.findByEmail(userEmail);
            if (allForEmail.size() > 1) {
                System.out.println("⚠️  Found " + allForEmail.size() + " subscription records for this email!");
                System.out.println("   Using the latest one (ID: " + (activeSubscription != null ? activeSubscription.getId() : "none") + ")");
            }

        } catch (Exception e) {
            System.out.println("Error fetching subscription: " + e.getMessage());
            e.printStackTrace();
        }

        // Add data to model
        model.addAttribute("activeUser", activeUser);
        model.addAttribute("userRole", session.getAttribute("userRole"));
        model.addAttribute("userEmail", userEmail);

        if (activeSubscription != null) {
            model.addAttribute("hasActiveSubscription", true);
            model.addAttribute("activeSubscription", activeSubscription);
            model.addAttribute("subscriptionDate", "Recently purchased");
            System.out.println("✅ Added subscription data to model");
        } else {
            model.addAttribute("hasActiveSubscription", false);
            model.addAttribute("activeSubscription", null);
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
}