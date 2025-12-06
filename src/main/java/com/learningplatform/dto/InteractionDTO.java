package com.learningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractionDTO {
    private String interactionType;
    private String resourceType;
    private Long resourceId;
    private Integer duration; // in seconds
    private String metadata;
    private String sessionId;
}
