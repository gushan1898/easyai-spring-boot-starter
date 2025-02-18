package com.gushan.easyai.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ChatResponse {
    private String content;
    private String messageId;
    private String model;
    private long tokens;
    private long timestamp;
    private Map<String, Object> extra;
} 