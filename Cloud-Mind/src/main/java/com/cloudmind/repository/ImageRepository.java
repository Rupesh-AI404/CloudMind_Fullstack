package com.cloudmind.repository;

import com.cloudmind.model.ImageClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageClass, Long> {

    List<ImageClass> findByUserEmail(String userEmail);

    Optional<ImageClass> findByUserEmailAndFileName(String userEmail, String fileName);

    long countByUserEmail(String userEmail);
}