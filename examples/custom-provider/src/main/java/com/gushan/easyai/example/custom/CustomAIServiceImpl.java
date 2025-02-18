package com.gushan.easyai.example.custom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gushan.easyai.config.EasyAIProperties;
import com.gushan.easyai.exception.AIException;
import com.gushan.easyai.model.ChatRequest;
import com.gushan.easyai.model.ChatResponse;
import com.gushan.easyai.model.ChatStreamResponse;
import com.gushan.easyai.service.AIService;
import com.gushan.easyai.service.StreamResponseCallback;
import com.gushan.easyai.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CustomAIServiceImpl implements AIService {
    private final EasyAIProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            EasyAIProperties.Custom config = properties.getCustom();
            String url = config.getBaseUrl() + "/chat";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", request.getPrompt());
            requestBody.put("model", config.getModel());
            
            if (request.getHistory() != null) {
                requestBody.put("history", request.getHistory());
            }
            
            if (request.getOptions() != null) {
                requestBody.putAll(request.getOptions());
            }

            String response = HttpUtils.post(url, objectMapper.writeValueAsString(requestBody), config.getApiKey());
            return parseResponse(response, config.getModel());
        } catch (Exception e) {
            log.error("自定义AI服务调用失败", e);
            throw new AIException("自定义AI服务调用失败: " + e.getMessage(), e);
        }
    }

    private ChatResponse parseResponse(String response, String model) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        
        return ChatResponse.builder()
                .content((String) responseMap.get("response"))
                .messageId("custom-" + System.currentTimeMillis())
                .model(model)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Override
    public String chat(String prompt) {
        ChatRequest request = ChatRequest.builder()
                .prompt(prompt)
                .build();
        return chat(request).getContent();
    }

    @Override
    public boolean supportsCaching() {
        return true;
    }

    @Override
    public boolean supportsRateLimiting() {
        return true;
    }

    @Override
    public boolean supportsStreaming() {
        return false;
    }
} 