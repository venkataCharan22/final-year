package com.learningplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_id")
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private InteractionType interactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type")
    private ResourceType resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "duration") // in seconds
    private Integer duration;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON string for flexible data

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    public enum InteractionType {
        CLICK,
        SCROLL,
        TIME_SPENT,
        NAVIGATION,
        VIDEO_WATCH,
        QUIZ_ATTEMPT,
        LESSON_VIEW,
        SEARCH,
        BOOKMARK,
        DOWNLOAD
    }

    public enum ResourceType {
        COURSE,
        LESSON,
        QUIZ,
        ASSIGNMENT,
        VIDEO,
        DOCUMENT,
        DISCUSSION
    }
}
