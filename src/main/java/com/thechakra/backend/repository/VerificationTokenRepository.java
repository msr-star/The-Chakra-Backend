package com.thechakra.backend.repository;

import com.thechakra.backend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByEmailAndTokenType(String email, VerificationToken.TokenType tokenType);

    void deleteByExpiryDateBefore(LocalDateTime now);

    void deleteByEmailAndTokenType(String email, VerificationToken.TokenType tokenType);

    void deleteByEmail(String email);
}
