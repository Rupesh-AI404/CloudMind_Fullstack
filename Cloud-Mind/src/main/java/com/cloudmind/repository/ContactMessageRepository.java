package com.cloudmind.repository;

import com.cloudmind.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    // Find unread messages
    List<ContactMessage> findByIsReadFalseOrderByCreatedAtDesc();

    // Find all messages ordered by newest first
    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    // Count unread messages
    long countByIsReadFalse();

    // Find by priority
    List<ContactMessage> findByPriorityOrderByCreatedAtDesc(String priority);
}