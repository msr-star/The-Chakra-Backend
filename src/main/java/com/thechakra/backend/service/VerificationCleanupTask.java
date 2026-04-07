package com.thechakra.backend.service;

import com.thechakra.backend.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationCleanupTask {

    private final VerificationTokenRepository verificationTokenRepository;

    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanExpiredTokens() {
        log.info("Running scheduled cleanup for expired verification tokens");
        try {
            verificationTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now(ZoneId.of("UTC")));
        } catch (Exception e) {
            log.error("Failed to clean expired tokens: {}", e.getMessage());
        }
    }
}
