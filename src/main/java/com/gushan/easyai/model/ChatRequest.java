package com.gushan.easyai.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ChatRequest {
    private String prompt;
    private List<Message> history;
    private Map<String, Object> options;
    
    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
        private Long timestamp;
    }
} 