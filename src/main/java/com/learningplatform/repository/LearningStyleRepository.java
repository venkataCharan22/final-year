package com.learningplatform.repository;

import com.learningplatform.model.LearningStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningStyleRepository extends JpaRepository<LearningStyle, Long> {

    Optional<LearningStyle> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
