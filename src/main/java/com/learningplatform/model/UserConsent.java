package com.learningplatform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_consents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "analytics_consent")
    private Boolean analyticsConsent = false;

    @Column(name = "personalization_consent")
    private Boolean personalizationConsent = false;

    @Column(name = "data_sharing_consent")
    private Boolean dataSharingConsent = false;

    @Column(name = "marketing_consent")
    private Boolean marketingConsent = false;

    @Column(name = "data_retention_days")
    private Integer dataRetentionDays = 365;

    @Column(name = "consent_version")
    private String consentVersion; // Track version of consent agreement

    @Column(name = "consent_date")
    private LocalDateTime consentDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "ip_address")
    private String ipAddress; // For audit trail

    @Column(name = "user_agent")
    private String userAgent; // Browser/device info for audit

    @PrePersist
    protected void onCreate() {
        consentDate = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        if (consentVersion == null) {
            consentVersion = "1.0";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    /**
     * Check if user has given consent for analytics
     */
    public boolean canTrackAnalytics() {
        return analyticsConsent != null && analyticsConsent;
    }

    /**
     * Check if user has given consent for personalization
     */
    public boolean canPersonalize() {
        return personalizationConsent != null && personalizationConsent;
    }

    /**
     * Check if all required consents are given
     */
    public boolean hasMinimumConsent() {
        return analyticsConsent != null && analyticsConsent;
    }
}
