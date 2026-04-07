package com.thechakra.backend.repository;

import com.thechakra.backend.entity.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Long> {
    List<AssessmentResult> findByUserIdOrderByTimestampDesc(UUID userId);

    void deleteByUserId(UUID userId);
}
