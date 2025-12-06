package com.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsentDTO {
    private Boolean analyticsConsent;
    private Boolean personalizationConsent;
    private Boolean dataSharingConsent;
    private Boolean marketingConsent;
    private Integer dataRetentionDays;
}
