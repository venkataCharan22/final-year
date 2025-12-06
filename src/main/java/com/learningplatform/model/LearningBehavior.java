package com.learningplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_behaviors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "total_time_spent") // in seconds
    private Long totalTimeSpent;

    @Column(name = "avg_session_duration") // in seconds
    private Integer avgSessionDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_content_type")
    private ContentType preferredContentType;

    @Column(name = "engagement_score")
    private Double engagementScore; // 0-100

    @Column(name = "quiz_performance_pattern", columnDefinition = "TEXT")
    private String quizPerformancePattern; // JSON string

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Column(name = "courses_completed")
    private Integer coursesCompleted;

    @Column(name = "courses_in_progress")
    private Integer coursesInProgress;

    @Column(name = "average_quiz_score")
    private Double averageQuizScore;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public enum ContentType {
        VIDEO,
        TEXT,
        INTERACTIVE,
        AUDIO,
        MIXED
    }
}
