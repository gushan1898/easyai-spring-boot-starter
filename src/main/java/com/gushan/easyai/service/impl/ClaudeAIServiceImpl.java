package com.gushan.easyai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gushan.easyai.config.EasyAIProperties;
import com.gushan.easyai.exception.AIException;
import com.gushan.easyai.model.ChatRequest;
import com.gushan.easyai.model.ChatResponse;
import com.gushan.easyai.service.AIService;
import com.gushan.easyai.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class ClaudeAIServiceImpl implements AIService {
    private final EasyAIProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            EasyAIProperties.Claude config = properties.getClaude();
            String url = config.getBaseUrl() + "/messages";

            Map<String, Object> requestBody = buildRequestBody(request, config);
            
            String response = HttpUtils.post(url, objectMapper.writeValueAsString(requestBody), config.getApiKey());
            return parseResponse(response, config.getModel());
        } catch (Exception e) {
            log.error("Claude API调用失败", e);
            throw new AIException("Claude API调用失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildRequestBody(ChatRequest request, EasyAIProperties.Claude config) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("messages", convertToMessages(request));
        requestBody.put("system", "You are Claude, a helpful AI assistant.");
        requestBody.put("max_tokens", 2000);
        
        // 添加用户自定义选项
        if (request.getOptions() != null) {
            // Claude特定参数处理
            if (request.getOptions().containsKey("system")) {
                requestBody.put("system", request.getOptions().get("system"));
            }
            if (request.getOptions().containsKey("max_tokens")) {
                requestBody.put("max_tokens", request.getOptions().get("max_tokens"));
            }
            if (request.getOptions().containsKey("temperature")) {
                requestBody.put("temperature", request.getOptions().get("temperature"));
            }
        }
        
        return requestBody;
    }

    private List<Map<String, Object>> convertToMessages(ChatRequest request) {
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 添加历史消息
        if (request.getHistory() != null) {
            for (ChatRequest.Message msg : request.getHistory()) {
                Map<String, Object> message = new HashMap<>();
                message.put("role", convertRole(msg.getRole()));
                message.put("content", msg.getContent());
                if (msg.getTimestamp() != null) {
                    message.put("timestamp", msg.getTimestamp());
                }
                messages.add(message);
            }
        }
        
        // 添加当前消息
        Map<String, Object> currentMessage = new HashMap<>();
        currentMessage.put("role", "user");
        currentMessage.put("content", request.getPrompt());
        currentMessage.put("timestamp", System.currentTimeMillis());
        messages.add(currentMessage);
        
        return messages;
    }

    private String convertRole(String role) {
        // Claude的角色映射
        return switch (role.toLowerCase()) {
            case "assistant" -> "assistant";
            case "system" -> "system";
            default -> "user";
        };
    }

    private ChatResponse parseResponse(String response, String model) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode content = jsonNode.get("content");
        
        if (content == null) {
            throw new AIException("无效的API响应");
        }
        
        String messageId = jsonNode.get("id").asText();
        long tokens = 0;
        if (jsonNode.has("usage")) {
            tokens = jsonNode.get("usage").get("total_tokens").asLong();
        }
        
        Map<String, Object> extra = new HashMap<>();
        if (jsonNode.has("usage")) {
            JsonNode usage = jsonNode.get("usage");
            extra.put("input_tokens", usage.get("input_tokens").asLong());
            extra.put("output_tokens", usage.get("output_tokens").asLong());
        }
        extra.put("stop_reason", jsonNode.get("stop_reason").asText());
        extra.put("stop_sequence", jsonNode.get("stop_sequence").asText());
        
        return ChatResponse.builder()
                .content(content.get(0).get("text").asText())
                .messageId(messageId)
                .model(model)
                .tokens(tokens)
                .timestamp(System.currentTimeMillis())
                .extra(extra)
                .build();
    }

    @Override
    public String chat(String prompt) {
        ChatRequest request = ChatRequest.builder()
                .prompt(prompt)
                .build();
        return chat(request).getContent();
    }
} 