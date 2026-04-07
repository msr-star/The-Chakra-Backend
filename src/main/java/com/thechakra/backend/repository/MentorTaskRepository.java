package com.thechakra.backend.repository;

import com.thechakra.backend.entity.MentorTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MentorTaskRepository extends JpaRepository<MentorTask, UUID> {
    List<MentorTask> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<MentorTask> findByAdminIdOrderByCreatedAtDesc(UUID adminId);

    void deleteByStudentId(UUID studentId);
}
