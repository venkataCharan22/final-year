package com.learningplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_styles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "visual_score") // 0-100
    private Integer visualScore;

    @Column(name = "auditory_score") // 0-100
    private Integer auditoryScore;

    @Column(name = "kinesthetic_score") // 0-100
    private Integer kinestheticScore;

    @Column(name = "reading_writing_score") // 0-100
    private Integer readingWritingScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "dominant_style")
    private Style dominantStyle;

    @Column(name = "confidence") // 0-100, how confident the model is
    private Double confidence;

    @Column(name = "data_points") // number of interactions used for inference
    private Integer dataPoints;

    @Column(name = "last_inferred")
    private LocalDateTime lastInferred;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastInferred = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastInferred = LocalDateTime.now();
    }

    public enum Style {
        VISUAL,
        AUDITORY,
        KINESTHETIC,
        READING_WRITING,
        MULTIMODAL
    }

    /**
     * Determine the dominant learning style based on scores
     */
    public void calculateDominantStyle() {
        int maxScore = Math.max(
                Math.max(visualScore, auditoryScore),
                Math.max(kinestheticScore, readingWritingScore));

        // Check if multiple styles have similar high scores (multimodal)
        int highScoreCount = 0;
        if (visualScore >= maxScore - 10)
            highScoreCount++;
        if (auditoryScore >= maxScore - 10)
            highScoreCount++;
        if (kinestheticScore >= maxScore - 10)
            highScoreCount++;
        if (readingWritingScore >= maxScore - 10)
            highScoreCount++;

        if (highScoreCount >= 2) {
            this.dominantStyle = Style.MULTIMODAL;
        } else if (visualScore == maxScore) {
            this.dominantStyle = Style.VISUAL;
        } else if (auditoryScore == maxScore) {
            this.dominantStyle = Style.AUDITORY;
        } else if (kinestheticScore == maxScore) {
            this.dominantStyle = Style.KINESTHETIC;
        } else {
            this.dominantStyle = Style.READING_WRITING;
        }
    }
}
