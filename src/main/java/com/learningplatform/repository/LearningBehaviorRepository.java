package com.learningplatform.repository;

import com.learningplatform.model.LearningBehavior;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningBehaviorRepository extends JpaRepository<LearningBehavior, Long> {

    Optional<LearningBehavior> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
