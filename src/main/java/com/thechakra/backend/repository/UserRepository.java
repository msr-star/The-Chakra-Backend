package com.thechakra.backend.repository;

import com.thechakra.backend.entity.Role;
import com.thechakra.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    List<User> findByRole(Role role);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
