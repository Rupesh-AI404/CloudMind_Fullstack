package com.cloudmind.repository;

import com.cloudmind.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findTop10ByOrderByCreatedAtDesc();
    List<ContactMessage> findByIsReadFalseOrderByCreatedAtDesc();
    List<ContactMessage> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);
    long countByIsReadFalse();
}
