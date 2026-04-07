package com.thechakra.backend.service;

import com.thechakra.backend.entity.SystemLog;
import com.thechakra.backend.repository.SystemLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemAuditService {

    private final SystemLogRepository systemLogRepository;

    public void logAction(String actionType, String actionDetails, String performedBy, String targetEntity) {
        try {
            SystemLog auditLog = SystemLog.builder()
                    .timestamp(LocalDateTime.now(ZoneId.of("UTC")))
                    .actionType(actionType)
                    .actionDetails(actionDetails)
                    .performedBy(performedBy)
                    .targetEntity(targetEntity)
                    .build();
            systemLogRepository.save(auditLog);
            log.info("AUDIT LOG: [{}] - {}", actionType, actionDetails);
        } catch (Exception e) {
            log.error("Failed to write to System Audit Log: {}", e.getMessage(), e);
        }
    }
}
