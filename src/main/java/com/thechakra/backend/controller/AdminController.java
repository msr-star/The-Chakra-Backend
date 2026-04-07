package com.thechakra.backend.controller;

import com.thechakra.backend.dto.UserDto;
import com.thechakra.backend.dto.UserMapper;
import com.thechakra.backend.entity.*;
import com.thechakra.backend.repository.*;
import com.thechakra.backend.security.CustomUserDetails;
import com.thechakra.backend.service.EmailService;
import com.thechakra.backend.service.SystemAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

        private final QuestionRepository questionRepository;
        private final OptionRepository optionRepository;
        private final UserRepository userRepository;
        private final AssessmentResultRepository assessmentResultRepository;
        private final VerificationTokenRepository verificationTokenRepository;
        private final MentorTaskRepository mentorTaskRepository;
        private final RefreshTokenRepository refreshTokenRepository;
        private final SystemAuditService systemAuditService;
        private final UserMapper userMapper;
        private final EmailService emailService;

        @PostMapping("/questions")
        public ResponseEntity<Question> createQuestion(
                        @AuthenticationPrincipal CustomUserDetails admin,
                        @RequestBody Question question) {
                Question saved = questionRepository.save(question);
                systemAuditService.logAction("ADMIN_CREATE_QUESTION", "Created assessment question: " + saved.getId(),
                                admin.getUsername(), "Question:" + saved.getId());
                return ResponseEntity.ok(saved);
        }

        @PostMapping("/questions/{questionId}/options")
        public ResponseEntity<Option> addOption(
                        @AuthenticationPrincipal CustomUserDetails admin,
                        @PathVariable Long questionId, @RequestBody Option option) {
                Question question = questionRepository.findById(questionId)
                                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

                option.setQuestion(question);
                Option saved = optionRepository.save(option);
                systemAuditService.logAction("ADMIN_ADD_OPTION", "Added option to question: " + questionId,
                                admin.getUsername(),
                                "Option:" + saved.getId());
                return ResponseEntity.ok(saved);
        }

        @DeleteMapping("/questions/{questionId}")
        public ResponseEntity<Void> deleteQuestion(
                        @AuthenticationPrincipal CustomUserDetails admin,
                        @PathVariable Long questionId) {
                questionRepository.deleteById(questionId);
                systemAuditService.logAction("ADMIN_DELETE_QUESTION", "Deleted assessment question: " + questionId,
                                admin.getUsername(), "Question:" + questionId);
                return ResponseEntity.ok().build();
        }

        @GetMapping("/stats")
        public ResponseEntity<Map<String, Object>> getSystemStats() {
                long totalUsers = userRepository.count();
                // Since we don't have a direct "countByTokenType" yet, we'll fetch all and
                // filter
                long pendingApprovals = verificationTokenRepository.findAll().stream()
                                .filter(token -> token.getTokenType() == VerificationToken.TokenType.ADMIN_REGISTRATION)
                                .count();

                // System Health mocked as "OPTIMAL" if DB connects
                return ResponseEntity.ok(Map.of(
                                "totalUsers", totalUsers,
                                "pendingApprovals", pendingApprovals,
                                "systemHealth", "OPTIMAL"));
        }

        @GetMapping("/students")
        public ResponseEntity<List<UserDto>> getAllStudents(
                        @AuthenticationPrincipal CustomUserDetails admin) {
                List<UserDto> students = userRepository.findByRole(Role.STUDENT).stream()
                                .map(userMapper::toDto)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(students);
        }

        @DeleteMapping("/students/{userId}")
        @Transactional
        public ResponseEntity<Void> deleteStudent(
                        @AuthenticationPrincipal CustomUserDetails admin,
                        @PathVariable UUID userId) {
                userRepository.findById(userId).ifPresent(user -> {
                        verificationTokenRepository.deleteByEmail(user.getEmail());
                        refreshTokenRepository.deleteByUser(user);
                });
                assessmentResultRepository.deleteByUserId(userId);
                mentorTaskRepository.deleteByStudentId(userId);
                userRepository.deleteById(userId);
                systemAuditService.logAction("ADMIN_DELETE_STUDENT", "Deleted student user: " + userId,
                                admin.getUsername(), "User:" + userId);
                return ResponseEntity.ok().build();
        }

        // ─── MENTORSHIP ENDPOINTS ───────────────────────────────────────────

        /** Admin assigns themselves to a student */
        @PostMapping("/assign-student/{studentId}")
        public ResponseEntity<?> assignStudent(
                        @AuthenticationPrincipal CustomUserDetails admin,
                        @PathVariable UUID studentId) {
                User student = userRepository.findById(studentId)
                                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
                User adminUser = userRepository.findByEmail(admin.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
                student.setAssignedAdminId(adminUser.getId());
                student.setAssignedAdminName(adminUser.getName());
                userRepository.save(student);
                systemAuditService.logAction("ADMIN_ASSIGN_STUDENT", "Admin assigned student: " + student.getEmail(),
                                admin.getUsername(), "Student:" + studentId);
                return ResponseEntity.ok(Map.of("message", "Student assigned successfully"));
        }

        /** Admin unassigns a student */
        @PostMapping("/unassign-student/{studentId}")
        public ResponseEntity<?> unassignStudent(
                        @AuthenticationPrincipal CustomUserDetails admin,
                        @PathVariable UUID studentId) {
                User student = userRepository.findById(studentId)
                                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
                student.setAssignedAdminId(null);
                student.setAssignedAdminName(null);
                userRepository.save(student);
                return ResponseEntity.ok(Map.of("message", "Student unassigned"));
        }

        /** Admin sends a motivational welcome email to their assigned student */
        @PostMapping("/send-mentor-email/{studentId}")
        public ResponseEntity<?> sendMentorEmail(
                        @AuthenticationPrincipal CustomUserDetails admin,
                        @PathVariable UUID studentId) {
                User student = userRepository.findById(studentId)
                                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
                User adminUser = userRepository.findByEmail(admin.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
                emailService.sendMentorWelcomeEmail(student.getEmail(), student.getName(), adminUser.getName());
                systemAuditService.logAction("ADMIN_SEND_MENTOR_EMAIL", "Sent mentor email to: " + student.getEmail(),
                                admin.getUsername(), "Student:" + studentId);
                return ResponseEntity.ok(Map.of("message", "Mentor email sent successfully"));
        }

        /** Admin sends a daily task/assignment to their student */
        @PostMapping("/send-task/{studentId}")
        public ResponseEntity<?> sendTask(
                        @AuthenticationPrincipal CustomUserDetails admin,
                        @PathVariable UUID studentId,
                        @RequestBody Map<String, String> body) {
                User student = userRepository.findById(studentId)
                                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
                User adminUser = userRepository.findByEmail(admin.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

                MentorTask task = MentorTask.builder()
                                .adminId(adminUser.getId())
                                .adminName(adminUser.getName())
                                .studentId(studentId)
                                .studentEmail(student.getEmail())
                                .title(body.getOrDefault("title", "Daily Assignment"))
                                .content(body.getOrDefault("content", ""))
                                .resourceUrl(body.get("resourceUrl"))
                                .createdAt(LocalDateTime.now(ZoneId.of("UTC")))
                                .build();
                mentorTaskRepository.save(task);
                systemAuditService.logAction("ADMIN_SEND_TASK", "Sent task to: " + student.getEmail(),
                                admin.getUsername(), "Student:" + studentId);
                return ResponseEntity.ok(Map.of("message", "Task sent successfully"));
        }

        /** Admin gets all students assigned to them */
        @GetMapping("/my-students")
        public ResponseEntity<List<UserDto>> getMyStudents(
                        @AuthenticationPrincipal CustomUserDetails admin) {
                User adminUser = userRepository.findByEmail(admin.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
                List<UserDto> students = userRepository.findByRole(Role.STUDENT).stream()
                                .filter(s -> adminUser.getId().equals(s.getAssignedAdminId()))
                                .map(userMapper::toDto)
                                .collect(Collectors.toList());
                return ResponseEntity.ok(students);
        }

}
