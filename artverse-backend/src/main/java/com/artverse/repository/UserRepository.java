package com.artverse.repository;

import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {


    List<User> findByFullNameContainingIgnoreCase(String keyword);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
