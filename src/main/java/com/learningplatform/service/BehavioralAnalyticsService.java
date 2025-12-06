package com.learningplatform.service;

import com.learningplatform.dto.InteractionDTO;
import com.learningplatform.dto.LearningStyleDTO;
import com.learningplatform.model.*;
import com.learningplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BehavioralAnalyticsService {

    @Autowired
    private UserInteractionRepository interactionRepository;

    @Autowired
    private LearningBehaviorRepository behaviorRepository;

    @Autowired
    private LearningStyleRepository styleRepository;

    @Autowired
    private UserConsentRepository consentRepository;

    @Autowired
    private GeminiAIService geminiAIService;

    /**
     * Track user interaction
     */
    @Transactional
    public void trackInteraction(Long userId, InteractionDTO interactionDTO) {
        // Check consent
        if (!hasAnalyticsConsent(userId)) {
            return; // Don't track if user hasn't consented
        }

        UserInteraction interaction = new UserInteraction();
        interaction.setUserId(userId);
        interaction.setSessionId(interactionDTO.getSessionId());
        interaction.setInteractionType(
                UserInteraction.InteractionType.valueOf(interactionDTO.getInteractionType()));

        if (interactionDTO.getResourceType() != null) {
            interaction.setResourceType(
                    UserInteraction.ResourceType.valueOf(interactionDTO.getResourceType()));
        }

        interaction.setResourceId(interactionDTO.getResourceId());
        interaction.setDuration(interactionDTO.getDuration());
        interaction.setMetadata(interactionDTO.getMetadata());
        interaction.setTimestamp(LocalDateTime.now());

        interactionRepository.save(interaction);

        // Update learning behavior asynchronously
        updateLearningBehavior(userId);
    }

    /**
     * Update learning behavior based on interactions
     */
    @Transactional
    public void updateLearningBehavior(Long userId) {
        LearningBehavior behavior = behaviorRepository.findByUserId(userId)
                .orElse(new LearningBehavior());

        behavior.setUserId(userId);

        // Calculate total time spent
        Long totalTime = interactionRepository.getTotalTimeSpentByUser(userId);
        behavior.setTotalTimeSpent(totalTime != null ? totalTime : 0L);

        // Calculate total sessions
        Long totalSessions = interactionRepository.getTotalSessionsByUser(userId);
        behavior.setTotalSessions(totalSessions != null ? totalSessions.intValue() : 0);

        // Calculate average session duration
        if (totalSessions != null && totalSessions > 0) {
            behavior.setAvgSessionDuration((int) (totalTime / totalSessions));
        }

        // Determine preferred content type
        List<Object[]> contentPreferences = interactionRepository.getContentTypePreferences(userId);
        if (!contentPreferences.isEmpty()) {
            Object[] topPreference = contentPreferences.get(0);
            UserInteraction.ResourceType resourceType = (UserInteraction.ResourceType) topPreference[0];
            behavior.setPreferredContentType(mapToContentType(resourceType));
        }

        // Calculate engagement score (0-100)
        behavior.setEngagementScore(calculateEngagementScore(userId));

        behaviorRepository.save(behavior);
    }

    /**
     * Infer learning style from user interactions
     */
    @Transactional
    public LearningStyleDTO inferLearningStyle(Long userId) {
        // Check consent
        if (!hasPersonalizationConsent(userId)) {
            return null;
        }

        // Get user interactions
        List<UserInteraction> interactions = interactionRepository.findByUserIdOrderByTimestampDesc(userId);

        if (interactions.size() < 20) {
            // Not enough data for reliable inference
            return createDefaultLearningStyle(userId, interactions.size());
        }

        // Calculate scores for each learning style
        Map<String, Integer> scores = calculateLearningStyleScores(interactions);

        // Create or update learning style
        LearningStyle style = styleRepository.findByUserId(userId)
                .orElse(new LearningStyle());

        style.setUserId(userId);
        style.setVisualScore(scores.get("visual"));
        style.setAuditoryScore(scores.get("auditory"));
        style.setKinestheticScore(scores.get("kinesthetic"));
        style.setReadingWritingScore(scores.get("readingWriting"));
        style.setDataPoints(interactions.size());

        // Calculate confidence based on data points
        style.setConfidence(calculateConfidence(interactions.size()));

        // Determine dominant style
        style.calculateDominantStyle();

        styleRepository.save(style);

        return convertToDTO(style);
    }

    /**
     * Calculate learning style scores based on interactions
     */
    private Map<String, Integer> calculateLearningStyleScores(List<UserInteraction> interactions) {
        int visualScore = 0;
        int auditoryScore = 0;
        int kinestheticScore = 0;
        int readingWritingScore = 0;

        for (UserInteraction interaction : interactions) {
            if (interaction.getResourceType() == null)
                continue;

            int weight = interaction.getDuration() != null ? interaction.getDuration() / 60 : 1;

            switch (interaction.getResourceType()) {
                case VIDEO:
                    visualScore += weight * 3;
                    auditoryScore += weight * 2;
                    break;
                case DOCUMENT:
                    readingWritingScore += weight * 3;
                    visualScore += weight * 1;
                    break;
                case QUIZ:
                case ASSIGNMENT:
                    kinestheticScore += weight * 3;
                    break;
                default:
                    break;
            }

            // Analyze interaction patterns
            if (interaction.getInteractionType() == UserInteraction.InteractionType.VIDEO_WATCH) {
                visualScore += 2;
            } else if (interaction.getInteractionType() == UserInteraction.InteractionType.QUIZ_ATTEMPT) {
                kinestheticScore += 2;
            }
        }

        // Normalize scores to 0-100
        int maxScore = Math.max(
                Math.max(visualScore, auditoryScore),
                Math.max(kinestheticScore, readingWritingScore));

        if (maxScore > 0) {
            visualScore = (visualScore * 100) / maxScore;
            auditoryScore = (auditoryScore * 100) / maxScore;
            kinestheticScore = (kinestheticScore * 100) / maxScore;
            readingWritingScore = (readingWritingScore * 100) / maxScore;
        }

        Map<String, Integer> scores = new HashMap<>();
        scores.put("visual", visualScore);
        scores.put("auditory", auditoryScore);
        scores.put("kinesthetic", kinestheticScore);
        scores.put("readingWriting", readingWritingScore);

        return scores;
    }

    /**
     * Calculate engagement score (0-100)
     */
    private Double calculateEngagementScore(Long userId) {
        List<UserInteraction> recentInteractions = interactionRepository
                .findByUserIdAndTimestampBetween(
                        userId,
                        LocalDateTime.now().minusDays(30),
                        LocalDateTime.now());

        if (recentInteractions.isEmpty()) {
            return 0.0;
        }

        // Factors for engagement score
        int interactionCount = recentInteractions.size();
        long totalTime = recentInteractions.stream()
                .mapToLong(i -> i.getDuration() != null ? i.getDuration() : 0)
                .sum();

        Set<String> uniqueDays = new HashSet<>();
        for (UserInteraction interaction : recentInteractions) {
            uniqueDays.add(interaction.getTimestamp().toLocalDate().toString());
        }

        // Calculate score components
        double frequencyScore = Math.min(interactionCount / 100.0 * 100, 100);
        double timeScore = Math.min(totalTime / 3600.0 * 10, 100);
        double consistencyScore = (uniqueDays.size() / 30.0) * 100;

        // Weighted average
        return (frequencyScore * 0.3 + timeScore * 0.4 + consistencyScore * 0.3);
    }

    /**
     * Calculate confidence based on number of data points
     */
    private Double calculateConfidence(int dataPoints) {
        if (dataPoints < 20)
            return 0.0;
        if (dataPoints < 50)
            return 50.0;
        if (dataPoints < 100)
            return 70.0;
        if (dataPoints < 200)
            return 85.0;
        return 95.0;
    }

    /**
     * Create default learning style for new users
     */
    private LearningStyleDTO createDefaultLearningStyle(Long userId, int dataPoints) {
        LearningStyleDTO dto = new LearningStyleDTO();
        dto.setDominantStyle("MULTIMODAL");
        dto.setVisualScore(50);
        dto.setAuditoryScore(50);
        dto.setKinestheticScore(50);
        dto.setReadingWritingScore(50);
        dto.setConfidence(0.0);
        dto.setDataPoints(dataPoints);
        dto.setDescription("Not enough data yet. Keep using the platform!");
        dto.setRecommendations("Try different types of content to help us understand your learning style.");
        return dto;
    }

    /**
     * Convert LearningStyle entity to DTO
     */
    private LearningStyleDTO convertToDTO(LearningStyle style) {
        LearningStyleDTO dto = new LearningStyleDTO();
        dto.setDominantStyle(style.getDominantStyle().toString());
        dto.setVisualScore(style.getVisualScore());
        dto.setAuditoryScore(style.getAuditoryScore());
        dto.setKinestheticScore(style.getKinestheticScore());
        dto.setReadingWritingScore(style.getReadingWritingScore());
        dto.setConfidence(style.getConfidence());
        dto.setDataPoints(style.getDataPoints());
        dto.setDescription(getStyleDescription(style.getDominantStyle()));
        dto.setRecommendations(getStyleRecommendations(style.getDominantStyle()));
        return dto;
    }

    /**
     * Get description for learning style
     */
    private String getStyleDescription(LearningStyle.Style style) {
        switch (style) {
            case VISUAL:
                return "You learn best through visual aids like diagrams, charts, and videos.";
            case AUDITORY:
                return "You learn best through listening to lectures, discussions, and audio content.";
            case KINESTHETIC:
                return "You learn best through hands-on practice, experiments, and interactive exercises.";
            case READING_WRITING:
                return "You learn best through reading texts and writing notes.";
            case MULTIMODAL:
                return "You benefit from a variety of learning methods and adapt well to different formats.";
            default:
                return "Your learning style is being analyzed.";
        }
    }

    /**
     * Get recommendations for learning style
     */
    private String getStyleRecommendations(LearningStyle.Style style) {
        switch (style) {
            case VISUAL:
                return "Focus on video lectures, infographics, and visual demonstrations.";
            case AUDITORY:
                return "Try podcasts, audio lectures, and participate in discussions.";
            case KINESTHETIC:
                return "Engage with coding exercises, labs, and practical projects.";
            case READING_WRITING:
                return "Take detailed notes, read documentation, and write summaries.";
            case MULTIMODAL:
                return "Mix different learning methods for optimal results.";
            default:
                return "Keep exploring different content types.";
        }
    }

    /**
     * Map ResourceType to ContentType
     */
    private LearningBehavior.ContentType mapToContentType(UserInteraction.ResourceType resourceType) {
        switch (resourceType) {
            case VIDEO:
                return LearningBehavior.ContentType.VIDEO;
            case DOCUMENT:
                return LearningBehavior.ContentType.TEXT;
            case QUIZ:
            case ASSIGNMENT:
                return LearningBehavior.ContentType.INTERACTIVE;
            default:
                return LearningBehavior.ContentType.MIXED;
        }
    }

    /**
     * Check if user has given analytics consent
     */
    private boolean hasAnalyticsConsent(Long userId) {
        return consentRepository.findByUserId(userId)
                .map(UserConsent::canTrackAnalytics)
                .orElse(false);
    }

    /**
     * Check if user has given personalization consent
     */
    private boolean hasPersonalizationConsent(Long userId) {
        return consentRepository.findByUserId(userId)
                .map(UserConsent::canPersonalize)
                .orElse(false);
    }

    /**
     * Get learning behavior for user
     */
    public LearningBehavior getLearningBehavior(Long userId) {
        return behaviorRepository.findByUserId(userId).orElse(null);
    }

    /**
     * Get learning style for user
     */
    public LearningStyleDTO getLearningStyle(Long userId) {
        return styleRepository.findByUserId(userId)
                .map(this::convertToDTO)
                .orElse(null);
    }
}
