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
public class DeepSeekAIServiceImpl implements AIService {
    private final EasyAIProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            EasyAIProperties.DeepSeek config = properties.getDeepseek();
            String url = config.getBaseUrl() + "/chat/completions";

            Map<String, Object> requestBody = buildRequestBody(request, config);
            
            String response = HttpUtils.post(url, objectMapper.writeValueAsString(requestBody), config.getApiKey());
            return parseResponse(response, config.getModel());
        } catch (Exception e) {
            log.error("DeepSeek API调用失败", e);
            throw new AIException("DeepSeek API调用失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildRequestBody(ChatRequest request, EasyAIProperties.DeepSeek config) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("messages", convertToMessages(request));
        requestBody.put("temperature", config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokens());
        
        // 添加用户自定义选项
        if (request.getOptions() != null) {
            requestBody.putAll(request.getOptions());
        }
        
        return requestBody;
    }

    private List<Map<String, String>> convertToMessages(ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加历史消息
        if (request.getHistory() != null) {
            for (ChatRequest.Message msg : request.getHistory()) {
                Map<String, String> message = new HashMap<>();
                message.put("role", msg.getRole());
                message.put("content", msg.getContent());
                messages.add(message);
            }
        }
        
        // 添加当前消息
        Map<String, String> currentMessage = new HashMap<>();
        currentMessage.put("role", "user");
        currentMessage.put("content", request.getPrompt());
        messages.add(currentMessage);
        
        return messages;
    }

    private ChatResponse parseResponse(String response, String model) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode choices = jsonNode.get("choices");
        
        if (choices == null || choices.isEmpty()) {
            throw new AIException("无效的API响应");
        }
        
        String content = choices.get(0).get("message").get("content").asText();
        String messageId = jsonNode.get("id").asText();
        long tokens = jsonNode.get("usage").get("total_tokens").asLong();
        
        Map<String, Object> extra = new HashMap<>();
        extra.put("prompt_tokens", jsonNode.get("usage").get("prompt_tokens").asLong());
        extra.put("completion_tokens", jsonNode.get("usage").get("completion_tokens").asLong());
        
        return ChatResponse.builder()
                .content(content)
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