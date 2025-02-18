package com.gushan.easyai.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ImageGenerationRequest {
    private String prompt;
    private int width;
    private int height;
    private String style;
    private int numberOfImages;
    private Map<String, Object> options;
} 