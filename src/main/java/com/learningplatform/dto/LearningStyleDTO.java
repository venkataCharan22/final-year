package com.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningStyleDTO {
    private String dominantStyle;
    private Integer visualScore;
    private Integer auditoryScore;
    private Integer kinestheticScore;
    private Integer readingWritingScore;
    private Double confidence;
    private Integer dataPoints;
    private String description;
    private String recommendations;
}
