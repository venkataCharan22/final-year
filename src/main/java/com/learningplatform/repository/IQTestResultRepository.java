package com.learningplatform.repository;

import com.learningplatform.model.IQTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IQTestResultRepository extends JpaRepository<IQTestResult, Long> {
    List<IQTestResult> findByUserIdOrderByDateDesc(Long userId);
}
