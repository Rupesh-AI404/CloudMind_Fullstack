package com.cloudmind.repository;

import com.cloudmind.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    List<Subscriber> findByEmail(String email);
    List<Subscriber> findByEmailAndStatus(String email, String status);
    List<Subscriber> findByEmailOrderBySubscriptionDateDesc(String email);
}