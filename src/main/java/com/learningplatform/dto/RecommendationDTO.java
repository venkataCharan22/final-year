package com.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {
    private Long courseId;
    private String courseTitle;
    private String courseDescription;
    private Double recommendationScore;
    private String recommendationType;
    private String reasoning;
    private Double learningStyleMatch;
    private List<String> tags;
    private String difficulty;
    private Integer estimatedDuration;
}
