// SubscriptionRepository.java
package com.cloudmind.repository;

import com.cloudmind.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    // Change from findByEmail to findFirstByEmail to get the latest subscription
    Subscriber findFirstByEmailOrderByIdDesc(String email);

    // Keep the original for compatibility
    List<Subscriber> findByEmail(String email);

    // Optional: Get all subscriptions for an email
    List<Subscriber> findAllByEmailOrderByIdDesc(String email);

    List<Subscriber> findByEmailAndStatus(String userEmail, String active);


    List<Subscriber> findAllByOrderByIdDesc();

    // If you have createdAt field, add this:
    // List<Subscriber> findByCreatedAtAfter(LocalDateTime date);

    // Method to check if email exists
    boolean existsByEmail(String email);






//    @Query("SELECT s FROM Subscriber s ORDER BY s.createdAt DESC")
//    List<Subscriber> findAllOrderByCreatedAtDesc();
//
//    @Query("SELECT s FROM Subscriber s WHERE s.createdAt > ?1")
//    List<Subscriber> findByCreatedAtAfter(LocalDateTime date);
//
//    List<Subscriber> findByPlan(String plan);
//
//    @Query("SELECT s FROM Subscriber s WHERE s.expiryDate < ?1")
//    List<Subscriber> findByExpiryDateBefore(LocalDateTime date);

}