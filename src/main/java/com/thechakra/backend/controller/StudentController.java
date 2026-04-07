package com.thechakra.backend.controller;

import com.thechakra.backend.entity.MentorTask;
import com.thechakra.backend.entity.User;
import com.thechakra.backend.repository.MentorTaskRepository;
import com.thechakra.backend.repository.UserRepository;
import com.thechakra.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final UserRepository userRepository;
    private final MentorTaskRepository mentorTaskRepository;

    /** Returns all tasks sent to the logged-in student by their mentor */
    @GetMapping("/my-tasks")
    public ResponseEntity<List<MentorTask>> getMyTasks(
            @AuthenticationPrincipal CustomUserDetails studentDetails) {
        User student = userRepository.findByEmail(studentDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        List<MentorTask> tasks = mentorTaskRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
        return ResponseEntity.ok(tasks);
    }

    /** Returns the mentor info for the logged-in student */
    @GetMapping("/my-mentor")
    public ResponseEntity<?> getMyMentor(
            @AuthenticationPrincipal CustomUserDetails studentDetails) {
        User student = userRepository.findByEmail(studentDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (student.getAssignedAdminId() == null) {
            return ResponseEntity.ok(Map.of("message", "No mentor assigned yet"));
        }
        return ResponseEntity.ok(Map.of(
                "mentorId", student.getAssignedAdminId().toString(),
                "mentorName", student.getAssignedAdminName()));
    }
}
