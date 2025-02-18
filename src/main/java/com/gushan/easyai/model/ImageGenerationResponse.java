package com.gushan.easyai.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ImageGenerationResponse {
    private List<String> imageUrls;
    private String requestId;
    private long timestamp;
    private Map<String, Object> extra;
} 