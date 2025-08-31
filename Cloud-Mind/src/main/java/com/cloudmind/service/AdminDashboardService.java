package com.cloudmind.service;

import com.cloudmind.model.Subscriber;
import com.cloudmind.model.User;
import com.cloudmind.repository.SubscriberRepository;
import com.cloudmind.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminDashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        try {
            // Get all data
            List<Subscriber> subscribers = subscriberRepository.findAll();
            long totalUsers = userRepository.count();
            long totalSubscribers = subscribers.size();

            // Debug logging
            System.out.println("=== DASHBOARD SERVICE DEBUG ===");
            System.out.println("Total Users from DB: " + totalUsers);
            System.out.println("Total Subscribers from DB: " + totalSubscribers);
            System.out.println("Subscribers found: " + subscribers.size());

            if (!subscribers.isEmpty()) {
                System.out.println("First subscriber details:");
                Subscriber first = subscribers.get(0);
                System.out.println("- ID: " + first.getId());
                System.out.println("- Email: " + first.getEmail());
                System.out.println("- Plan: " + first.getPlan());
                System.out.println("- Status: " + first.getStatus());
                System.out.println("- First Name: " + first.getFirstName());
                System.out.println("- Last Name: " + first.getLastName());
            }

            // Calculate metrics
            BigDecimal totalRevenue = calculateTotalRevenue(subscribers);
            BigDecimal monthlyRevenue = calculateMonthlyRevenue(subscribers);
            double conversionRate = totalUsers > 0 ? (double) totalSubscribers / totalUsers * 100 : 0;

            // Get package distribution
            Map<String, Object> packageDistribution = getPackageDistribution(subscribers);

            // Add all data to map
            data.put("totalUsers", totalUsers);
            data.put("activeCampaigns", totalSubscribers);
            data.put("totalRevenue", totalRevenue);
            data.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);
            data.put("subscribers", subscribers);
            data.put("packageDistribution", packageDistribution);
            data.put("monthlyRevenue", monthlyRevenue);

            System.out.println("Data being sent to template:");
            System.out.println("- totalUsers: " + data.get("totalUsers"));
            System.out.println("- activeCampaigns: " + data.get("activeCampaigns"));
            System.out.println("- subscribers size: " + ((List<?>) data.get("subscribers")).size());

        } catch (Exception e) {
            System.out.println("Error in getDashboardData: " + e.getMessage());
            e.printStackTrace();

            // Return empty data on error
            data.put("totalUsers", 0L);
            data.put("activeCampaigns", 0L);
            data.put("totalRevenue", BigDecimal.ZERO);
            data.put("conversionRate", 0.0);
            data.put("subscribers", new ArrayList<>());
            data.put("packageDistribution", getEmptyPackageDistribution());
            data.put("monthlyRevenue", BigDecimal.ZERO);
        }

        return data;
    }

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

        BigDecimal amount = BigDecimal.ZERO;
        switch (plan.toLowerCase()) {
            case "starter":
                amount = new BigDecimal("999");
                break;
            case "pro":
                amount = new BigDecimal("2499");
                break;
            case "enterprise":
                amount = new BigDecimal("4999");
                break;
            default:
                amount = BigDecimal.ZERO;
                break;
        }

        return amount;
    }

    private Map<String, Object> getPackageDistribution(List<Subscriber> subscribers) {
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

        Map<String, Object> distribution = new HashMap<>();
        distribution.put("starter", planCounts.get("starter"));
        distribution.put("pro", planCounts.get("pro"));
        distribution.put("enterprise", planCounts.get("enterprise"));

        return distribution;
    }

    private Map<String, Object> getEmptyPackageDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("starter", 0L);
        distribution.put("pro", 0L);
        distribution.put("enterprise", 0L);
        return distribution;
    }

    public String formatSubscriberName(Subscriber subscriber) {
        if (subscriber.getFirstName() != null && subscriber.getLastName() != null) {
            return subscriber.getFirstName() + " " + subscriber.getLastName();
        } else if (subscriber.getFirstName() != null) {
            return subscriber.getFirstName();
        } else if (subscriber.getEmail() != null) {
            return subscriber.getEmail().split("@")[0];
        }
        return "Unknown User";
    }

    public String formatSubscriptionDate(LocalDateTime date) {
        if (date == null) return "N/A";
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}