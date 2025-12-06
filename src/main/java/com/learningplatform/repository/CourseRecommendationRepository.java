package com.learningplatform.repository;

import com.learningplatform.model.CourseRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseRecommendationRepository extends JpaRepository<CourseRecommendation, Long> {

    List<CourseRecommendation> findByUserIdOrderByRecommendationScoreDesc(Long userId);

    List<CourseRecommendation> findByUserIdAndClickedFalseOrderByRecommendationScoreDesc(Long userId);

    @Query("SELECT cr FROM CourseRecommendation cr WHERE cr.userId = :userId " +
            "AND cr.createdAt >= :since ORDER BY cr.recommendationScore DESC")
    List<CourseRecommendation> findRecentRecommendations(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since);

    @Query("SELECT AVG(cr.feedbackRating) FROM CourseRecommendation cr " +
            "WHERE cr.userId = :userId AND cr.feedbackRating IS NOT NULL")
    Double getAverageFeedbackRating(@Param("userId") Long userId);

    @Query("SELECT COUNT(cr) FROM CourseRecommendation cr " +
            "WHERE cr.userId = :userId AND cr.clicked = true")
    Long getClickedCount(@Param("userId") Long userId);

    @Query("SELECT COUNT(cr) FROM CourseRecommendation cr " +
            "WHERE cr.userId = :userId AND cr.enrolled = true")
    Long getEnrolledCount(@Param("userId") Long userId);
}
