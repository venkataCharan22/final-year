package com.learningplatform.repository;

import com.learningplatform.model.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    Optional<UserConsent> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
