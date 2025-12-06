package com.learningplatform.controller;

import com.learningplatform.model.IQTestResult;
import com.learningplatform.model.Quiz;
import com.learningplatform.model.User;
import com.learningplatform.repository.IQTestResultRepository;
import com.learningplatform.service.GeminiService;
import com.learningplatform.service.MLAdviceService;
import com.learningplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/iq-test")
@CrossOrigin(origins = "http://localhost:3000")
public class IQTestController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private UserService userService;

    @Autowired
    private MLAdviceService mlAdviceService;

    @Autowired
    private IQTestResultRepository iqTestResultRepository;

    @GetMapping("/generate")
    public ResponseEntity<?> generateIQTest() {
        try {
            Quiz iqTest = geminiService.generateIQTest();
            return ResponseEntity.ok(iqTest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate IQ test: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getIQHistory(HttpServletRequest request) {
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            Long userId = Long.parseLong(userIdStr);
            List<IQTestResult> history = iqTestResultRepository.findByUserIdOrderByDateDesc(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to fetch history: " + e.getMessage()));
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitIQTest(@RequestBody IQTestSubmission submission, HttpServletRequest request) {
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;
            String testType = submission.getTestType() != null ? submission.getTestType() : "IQ";

            // Use fallback test answers for consistent scoring
            Map<String, Integer> correctAnswers;

            if ("TECH".equals(testType)) {
                correctAnswers = Map.of(
                        "tech-q1", 1, // ->
                        "tech-q2", 2, // Undefined Behavior
                        "tech-q3", 1, // Linear Regression
                        "tech-q4", 0, // Overfitting definition
                        "tech-q5", 1 // Stack
                );
            } else {
                correctAnswers = Map.of(
                        "iq-q1", 1, // Some roses might be red
                        "iq-q2", 1, // 162 (pattern recognition)
                        "iq-q3", 2, // 5 minutes (mathematical logic)
                        "iq-q4", 1, // 12 cubes (spatial reasoning)
                        "iq-q5", 0 // Eating (analogical reasoning)
                );
            }

            // Calculate score based on correct answers
            int correct = 0;
            int totalQuestions = submission.getAnswers().size();

            // Compare submitted answers with correct answers
            for (Map.Entry<String, String> entry : submission.getAnswers().entrySet()) {
                String questionId = entry.getKey();
                String submittedAnswer = entry.getValue();

                if (submittedAnswer != null && correctAnswers.containsKey(questionId)) {
                    try {
                        int answerIndex = Integer.parseInt(submittedAnswer);
                        if (answerIndex == correctAnswers.get(questionId)) {
                            correct++;
                        }
                    } catch (NumberFormatException e) {
                        // Invalid answer format, skip
                    }
                }
            }

            // Calculate IQ score using a more realistic formula
            double percentage = totalQuestions > 0 ? (double) correct / totalQuestions : 0;
            int iqScore = (int) (70 + (percentage * 75));
            int randomVariation = (int) (Math.random() * 11) - 5; // -5 to +5
            iqScore = Math.max(70, Math.min(160, iqScore + randomVariation));

            // ML Analysis
            MLAdviceService.MLResult mlResult = mlAdviceService.analyzePerformance(submission.getAnswers(),
                    correctAnswers, iqScore, testType);

            // Save result
            if (userId != null) {
                try {
                    // Update latest IQ in User table only if it's an IQ test
                    if ("IQ".equals(testType)) {
                        userService.updateEstimatedIQ(userId, iqScore);
                    }

                    // Save detailed result in History
                    Optional<User> userOpt = userService.findById(userId);
                    if (userOpt.isPresent()) {
                        IQTestResult result = new IQTestResult(
                                userOpt.get(),
                                iqScore,
                                mlResult.getCategory(),
                                mlResult.getAdvice());
                        iqTestResultRepository.save(result);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to update user IQ: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(Map.of(
                    "score", iqScore,
                    "correctAnswers", correct,
                    "totalQuestions", totalQuestions,
                    "percentage", Math.round(percentage * 100),
                    "category", mlResult.getCategory(),
                    "advice", mlResult.getAdvice(),
                    "message", "Assessment completed successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to submit test: " + e.getMessage()));
        }
    }

    public static class IQTestSubmission {
        private Map<String, String> answers;
        private int timeSpent;
        private String testType; // "IQ" or "TECH"

        public Map<String, String> getAnswers() {
            return answers;
        }

        public void setAnswers(Map<String, String> answers) {
            this.answers = answers;
        }

        public int getTimeSpent() {
            return timeSpent;
        }

        public void setTimeSpent(int timeSpent) {
            this.timeSpent = timeSpent;
        }

        public String getTestType() {
            return testType;
        }

        public void setTestType(String testType) {
            this.testType = testType;
        }
    }
}
