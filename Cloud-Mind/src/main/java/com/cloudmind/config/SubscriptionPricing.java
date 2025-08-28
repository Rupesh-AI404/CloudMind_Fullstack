package com.cloudmind.config;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class SubscriptionPricing {

    public static final Map<String, Integer> MONTHLY_PRICES = Map.of(
            "starter", 999,      // $9.99 in cents
            "professional", 2999, // $29.99 in cents
            "enterprise", 4999   // $49.99 in cents
    );

    public static final Map<String, Integer> YEARLY_PRICES = Map.of(
            "starter", 9999,     // $99.99 in cents (2 months free)
            "professional", 29999, // $299.99 in cents
            "enterprise", 49999   // $499.99 in cents
    );

    public int getPrice(String plan, String billing) {
        if ("yearly".equals(billing)) {
            return YEARLY_PRICES.get(plan.toLowerCase());
        }
        return MONTHLY_PRICES.get(plan.toLowerCase());
    }

    public int getUpgradePrice(String currentPlan, String newPlan, String billing) {
        int currentPrice = getPrice(currentPlan, billing);
        int newPrice = getPrice(newPlan, billing);
        return Math.max(0, newPrice - currentPrice); // Only charge if upgrading
    }
}