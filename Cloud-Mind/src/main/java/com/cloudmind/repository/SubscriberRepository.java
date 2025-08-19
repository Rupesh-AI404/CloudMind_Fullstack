package com.cloudmind.repository;

import com.cloudmind.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    // Optional: add custom queries later if needed
}
