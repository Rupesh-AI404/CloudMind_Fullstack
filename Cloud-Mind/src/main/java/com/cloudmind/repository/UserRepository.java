
package com.cloudmind.repository;

import com.cloudmind.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    boolean existsByEmail(String email);


    Optional<Object> findByUsername(String username);


    // last approach
    User findByEmailAndPassword(String email, String password);

    List<User> findByRole(String role);
}