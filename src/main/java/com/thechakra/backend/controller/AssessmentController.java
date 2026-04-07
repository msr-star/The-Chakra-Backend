package com.thechakra.backend.controller;

import com.thechakra.backend.dto.AnswerDto;
import com.thechakra.backend.entity.AssessmentResult;
import com.thechakra.backend.entity.Question;
import com.thechakra.backend.security.CustomUserDetails;
import com.thechakra.backend.service.AssessmentService;
import com.thechakra.backend.service.SystemAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final SystemAuditService systemAuditService;

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getQuestions() {
        return ResponseEntity.ok(assessmentService.getAllQuestions());
    }

    @GetMapping("/my-results")
    public ResponseEntity<List<AssessmentResult>> getMyResults(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AssessmentResult> results = assessmentService.getResultsByUserId(userDetails.getUser().getId());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/submit")
    public ResponseEntity<AssessmentResult> submitAssessment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<AnswerDto> answers) {

        AssessmentResult result = assessmentService.submitAssessment(userDetails.getUser().getId(), answers);

        systemAuditService.logAction("TEST_SUBMITTED",
                "User submitted the Career Assessment with " + answers.size() + " answers",
                userDetails.getUsername(),
                "AssessmentResult:" + result.getId());

        return ResponseEntity.ok(result);
    }
}
