package com.learningplatform.repository;

import com.learningplatform.model.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    List<UserInteraction> findByUserIdOrderByTimestampDesc(Long userId);

    List<UserInteraction> findByUserIdAndTimestampBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end);

    List<UserInteraction> findByUserIdAndInteractionType(
            Long userId,
            UserInteraction.InteractionType interactionType);

    @Query("SELECT ui FROM UserInteraction ui WHERE ui.userId = :userId " +
            "AND ui.resourceType = :resourceType ORDER BY ui.timestamp DESC")
    List<UserInteraction> findByUserIdAndResourceType(
            @Param("userId") Long userId,
            @Param("resourceType") UserInteraction.ResourceType resourceType);

    @Query("SELECT SUM(ui.duration) FROM UserInteraction ui " +
            "WHERE ui.userId = :userId AND ui.interactionType = 'TIME_SPENT'")
    Long getTotalTimeSpentByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(DISTINCT ui.sessionId) FROM UserInteraction ui " +
            "WHERE ui.userId = :userId")
    Long getTotalSessionsByUser(@Param("userId") Long userId);

    @Query("SELECT ui.resourceType, COUNT(ui) FROM UserInteraction ui " +
            "WHERE ui.userId = :userId AND ui.interactionType = 'TIME_SPENT' " +
            "GROUP BY ui.resourceType ORDER BY COUNT(ui) DESC")
    List<Object[]> getContentTypePreferences(@Param("userId") Long userId);
}
