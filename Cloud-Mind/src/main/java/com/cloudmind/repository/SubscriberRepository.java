// SubscriptionRepository.java
package com.cloudmind.repository;

import com.cloudmind.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    // Change from findByEmail to findFirstByEmail to get the latest subscription
    Subscriber findFirstByEmailOrderByIdDesc(String email);

    // Keep the original for compatibility
    List<Subscriber> findByEmail(String email);

    // Optional: Get all subscriptions for an email
    List<Subscriber> findAllByEmailOrderByIdDesc(String email);

    List<Subscriber> findByEmailAndStatus(String userEmail, String active);
}