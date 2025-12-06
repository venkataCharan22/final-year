package com.learningplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiAIService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate personalized course recommendations using Gemini AI
     */
    public String generateCourseRecommendations(
            String learningStyle,
            List<String> completedCourses,
            String userGoal,
            String experienceLevel) {
        String prompt = buildRecommendationPrompt(learningStyle, completedCourses, userGoal, experienceLevel);
        return callGeminiAPI(prompt);
    }

    /**
     * Generate personalized learning path
     */
    public String generateLearningPath(
            String targetSkill,
            String currentLevel,
            String learningStyle,
            int timeAvailableWeeks) {
        String prompt = String.format(
                "Create a personalized %d-week learning path for someone who wants to learn %s. " +
                        "Current level: %s. Learning style: %s. " +
                        "Provide a week-by-week breakdown with specific topics, resources, and milestones. " +
                        "Format as JSON with structure: {weeks: [{week: 1, topics: [], resources: [], milestone: ''}]}",
                timeAvailableWeeks, targetSkill, currentLevel, learningStyle);
        return callGeminiAPI(prompt);
    }

    /**
     * Adapt content for specific learning style
     */
    public String adaptContentForLearningStyle(String content, String learningStyle) {
        String prompt = String.format(
                "Adapt the following educational content for a %s learner. " +
                        "Make it more engaging and effective for their learning style:\n\n%s",
                learningStyle, content);
        return callGeminiAPI(prompt);
    }

    /**
     * Generate personalized explanation for struggling students
     */
    public String generatePersonalizedExplanation(
            String concept,
            String learningStyle,
            int attemptCount,
            String previousErrors) {
        String prompt = String.format(
                "A student is struggling with the concept of '%s' (attempt #%d). " +
                        "Their learning style is %s. Previous errors: %s. " +
                        "Provide a clear, personalized explanation using their preferred learning style. " +
                        "Include analogies, examples, and practice suggestions.",
                concept, attemptCount, learningStyle, previousErrors);
        return callGeminiAPI(prompt);
    }

    /**
     * Predict course completion and suggest interventions
     */
    public String predictProgressAndSuggestInterventions(
            String courseName,
            double currentProgress,
            int daysElapsed,
            int targetDays,
            String engagementPattern) {
        String prompt = String.format(
                "Analyze this student's progress: Course '%s', %.1f%% complete after %d days (target: %d days). " +
                        "Engagement pattern: %s. " +
                        "Predict likelihood of completion and suggest specific interventions if needed. " +
                        "Format as JSON: {completionProbability: 0-100, riskLevel: 'low/medium/high', interventions: []}",
                courseName, currentProgress, daysElapsed, targetDays, engagementPattern);
        return callGeminiAPI(prompt);
    }

    /**
     * Generate quiz questions based on learning style
     */
    public String generateAdaptiveQuiz(
            String topic,
            String difficulty,
            String learningStyle,
            int questionCount) {
        String prompt = String.format(
                "Generate %d %s-level quiz questions about '%s' optimized for %s learners. " +
                        "Format as JSON: {questions: [{question: '', options: [], correctAnswer: 0, explanation: ''}]}",
                questionCount, difficulty, topic, learningStyle);
        return callGeminiAPI(prompt);
    }

    /**
     * Analyze learning behavior and provide insights
     */
    public String analyzeLearningBehavior(
            Map<String, Object> behaviorData) {
        try {
            String behaviorJson = objectMapper.writeValueAsString(behaviorData);
            String prompt = String.format(
                    "Analyze this student's learning behavior data and provide insights: %s. " +
                            "Identify patterns, strengths, areas for improvement, and personalized recommendations. " +
                            "Format as JSON: {insights: [], strengths: [], improvements: [], recommendations: []}",
                    behaviorJson);
            return callGeminiAPI(prompt);
        } catch (Exception e) {
            return "{\"error\": \"Failed to analyze behavior\"}";
        }
    }

    /**
     * Build recommendation prompt
     */
    private String buildRecommendationPrompt(
            String learningStyle,
            List<String> completedCourses,
            String userGoal,
            String experienceLevel) {
        return String.format(
                "Based on the following student profile, recommend 5 courses:\n" +
                        "- Learning Style: %s\n" +
                        "- Completed Courses: %s\n" +
                        "- Goal: %s\n" +
                        "- Experience Level: %s\n\n" +
                        "Provide recommendations with reasoning. Format as JSON: " +
                        "{recommendations: [{title: '', reasoning: '', learningStyleMatch: 0-100}]}",
                learningStyle,
                completedCourses.isEmpty() ? "None" : String.join(", ", completedCourses),
                userGoal != null ? userGoal : "General skill development",
                experienceLevel);
    }

    /**
     * Call Gemini API
     */
    private String callGeminiAPI(String prompt) {
        try {
            // Check if API key is configured
            if (apiKey == null || apiKey.isEmpty()) {
                return generateFallbackResponse(prompt);
            }

            // Build request
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", Collections.singletonList(part));
            requestBody.put("contents", Collections.singletonList(content));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Make request
            String url = apiUrl + "?key=" + apiKey;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);

            // Parse response
            if (response.getStatusCode() == HttpStatus.OK) {
                return extractTextFromResponse(response.getBody());
            } else {
                return generateFallbackResponse(prompt);
            }

        } catch (Exception e) {
            System.err.println("Gemini API error: " + e.getMessage());
            return generateFallbackResponse(prompt);
        }
    }

    /**
     * Extract text from Gemini API response
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            return responseBody;
        } catch (Exception e) {
            return responseBody;
        }
    }

    /**
     * Generate fallback response when API is unavailable
     */
    private String generateFallbackResponse(String prompt) {
        // Simple rule-based fallback
        if (prompt.contains("recommend")) {
            return "{\"recommendations\": [{\"title\": \"Introduction to Programming\", \"reasoning\": \"Great starting point for beginners\", \"learningStyleMatch\": 85}]}";
        } else if (prompt.contains("learning path")) {
            return "{\"weeks\": [{\"week\": 1, \"topics\": [\"Basics\"], \"resources\": [\"Online tutorials\"], \"milestone\": \"Complete fundamentals\"}]}";
        } else {
            return "{\"message\": \"AI service temporarily unavailable. Using default recommendations.\"}";
        }
    }
}
