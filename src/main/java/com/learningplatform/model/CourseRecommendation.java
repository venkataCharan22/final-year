package com.learningplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_recommendations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "recommendation_score") // 0-100
    private Double recommendationScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_type")
    private RecommendationType recommendationType;

    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning; // Why this course was recommended

    @Column(name = "learning_style_match") // 0-100
    private Double learningStyleMatch;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "clicked")
    private Boolean clicked = false;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Column(name = "enrolled")
    private Boolean enrolled = false;

    @Column(name = "enrolled_at")
    private LocalDateTime enrolledAt;

    @Column(name = "dismissed")
    private Boolean dismissed = false;

    @Column(name = "feedback_rating") // 1-5 stars
    private Integer feedbackRating;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum RecommendationType {
        RULE_BASED, // Cold-start recommendations
        COLLABORATIVE, // Based on similar users
        CONTENT_BASED, // Based on course similarity
        HYBRID, // Combination of methods
        AI_GENERATED, // Gemini AI recommendations
        LEARNING_PATH, // Part of a suggested learning path
        TRENDING, // Popular courses
        PREREQUISITE // Required for other courses
    }

    /**
     * Mark recommendation as clicked
     */
    public void markAsClicked() {
        this.clicked = true;
        this.clickedAt = LocalDateTime.now();
    }

    /**
     * Mark recommendation as enrolled
     */
    public void markAsEnrolled() {
        this.enrolled = true;
        this.enrolledAt = LocalDateTime.now();
        if (!this.clicked) {
            markAsClicked();
        }
    }
}
