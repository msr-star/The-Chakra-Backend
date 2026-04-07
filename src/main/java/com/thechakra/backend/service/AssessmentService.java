package com.thechakra.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thechakra.backend.dto.AnswerDto;
import com.thechakra.backend.entity.AssessmentResult;
import com.thechakra.backend.entity.Option;
import com.thechakra.backend.entity.Question;
import com.thechakra.backend.entity.User;
import com.thechakra.backend.repository.AssessmentResultRepository;
import com.thechakra.backend.repository.OptionRepository;
import com.thechakra.backend.repository.QuestionRepository;
import com.thechakra.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentService {

        private static final ObjectMapper MAPPER = new ObjectMapper();

        private final QuestionRepository questionRepository;
        private final OptionRepository optionRepository;
        private final AssessmentResultRepository resultRepository;
        private final UserRepository userRepository;

        public List<Question> getAllQuestions() {
                return questionRepository.findAll();
        }

        public List<AssessmentResult> getResultsByUserId(UUID userId) {
                return resultRepository.findByUserIdOrderByTimestampDesc(userId);
        }

        @Transactional
        public AssessmentResult submitAssessment(UUID userId, List<AnswerDto> answers) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("User not found"));

                Map<String, Double> categoryScores = new HashMap<>();

                // The Chakra Algorithm: Calculate weighted average
                for (AnswerDto answer : answers) {
                        Question question = questionRepository.findById(answer.getQuestionId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Invalid question ID: " + answer.getQuestionId()));
                        Option option = optionRepository.findById(answer.getOptionId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Invalid option ID: " + answer.getOptionId()));

                        // Verify option belongs to question
                        if (!option.getQuestion().getId().equals(question.getId())) {
                                throw new IllegalArgumentException("Option does not belong to question");
                        }

                        // Baseline classification by generic Category
                        double baseScore = option.getScoreValue() * question.getWeightage();
                        categoryScores.put(question.getCategory(),
                                        categoryScores.getOrDefault(question.getCategory(), 0.0) + baseScore);

                        // The Dharma Algorithm: Inject granular Career Mappings if they exist
                        if (option.getCareerMapping() != null && !option.getCareerMapping().trim().isEmpty()) {
                                try {
                                        Map<String, Double> mapping = MAPPER.readValue(option.getCareerMapping(),
                                                        new TypeReference<Map<String, Double>>() {
                                                        });
                                        for (Map.Entry<String, Double> entry : mapping.entrySet()) {
                                                double dharmaScore = entry.getValue() * question.getWeightage();
                                                categoryScores.put(entry.getKey(),
                                                                categoryScores.getOrDefault(entry.getKey(), 0.0)
                                                                                + dharmaScore);
                                        }
                                } catch (Exception e) {
                                        log.error("Failed to parse career mapping JSON for Option {}: {}",
                                                        option.getId(), e.getMessage());
                                }
                        }
                }

                // Determine dominant chakra (suggested path)
                String suggestedPath = categoryScores.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse("Balanced");

                AssessmentResult result = AssessmentResult.builder()
                                .userId(user.getId())
                                .categoryScores(categoryScores)
                                .suggestedPath(suggestedPath)
                                .build();

                resultRepository.save(result);

                // Update user alignment
                user.setChakraAlignment(suggestedPath);
                userRepository.save(user);

                return result;
        }
}
