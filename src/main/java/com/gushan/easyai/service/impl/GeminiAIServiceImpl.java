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
public class GeminiAIServiceImpl implements AIService {
    private final EasyAIProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            EasyAIProperties.Gemini config = properties.getGemini();
            String url = String.format("%s/models/%s:generateContent?key=%s",
                    config.getBaseUrl(), config.getModel(), config.getApiKey());

            Map<String, Object> requestBody = buildRequestBody(request, config);
            
            String response = HttpUtils.post(url, objectMapper.writeValueAsString(requestBody), null);
            return parseResponse(response, config.getModel());
        } catch (Exception e) {
            log.error("Gemini API调用失败", e);
            throw new AIException("Gemini API调用失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildRequestBody(ChatRequest request, EasyAIProperties.Gemini config) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // 构建内容部分
        List<Map<String, Object>> contents = new ArrayList<>();
        
        // 添加历史消息
        if (request.getHistory() != null) {
            for (ChatRequest.Message msg : request.getHistory()) {
                contents.add(buildContent(msg.getRole(), msg.getContent()));
            }
        }
        
        // 添加当前消息
        contents.add(buildContent("user", request.getPrompt()));
        
        requestBody.put("contents", contents);
        
        // 添加生成配置
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 2048);
        
        // 添加用户自定义选项
        if (request.getOptions() != null) {
            if (request.getOptions().containsKey("temperature")) {
                generationConfig.put("temperature", request.getOptions().get("temperature"));
            }
            if (request.getOptions().containsKey("maxOutputTokens")) {
                generationConfig.put("maxOutputTokens", request.getOptions().get("maxOutputTokens"));
            }
            if (request.getOptions().containsKey("topK")) {
                generationConfig.put("topK", request.getOptions().get("topK"));
            }
            if (request.getOptions().containsKey("topP")) {
                generationConfig.put("topP", request.getOptions().get("topP"));
            }
        }
        
        requestBody.put("generationConfig", generationConfig);
        
        // 添加安全设置
        Map<String, Object> safetySettings = new HashMap<>();
        safetySettings.put("category", "HARM_CATEGORY_HARASSMENT");
        safetySettings.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        
        List<Map<String, Object>> safetySettingsList = new ArrayList<>();
        safetySettingsList.add(safetySettings);
        requestBody.put("safetySettings", safetySettingsList);
        
        return requestBody;
    }

    private Map<String, Object> buildContent(String role, String content) {
        Map<String, Object> contentMap = new HashMap<>();
        
        List<Map<String, Object>> parts = new ArrayList<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", content);
        parts.add(part);
        
        contentMap.put("role", convertRole(role));
        contentMap.put("parts", parts);
        
        return contentMap;
    }

    private String convertRole(String role) {
        return switch (role.toLowerCase()) {
            case "assistant" -> "model";
            case "system" -> "user";
            default -> "user";
        };
    }

    private ChatResponse parseResponse(String response, String model) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode candidates = jsonNode.get("candidates");
        
        if (candidates == null || candidates.isEmpty()) {
            throw new AIException("无效的API响应");
        }
        
        JsonNode content = candidates.get(0).get("content");
        String text = content.get("parts").get(0).get("text").asText();
        
        Map<String, Object> extra = new HashMap<>();
        if (candidates.get(0).has("safetyRatings")) {
            extra.put("safetyRatings", candidates.get(0).get("safetyRatings"));
        }
        
        // Gemini API不直接提供token计数，这里使用简单估算
        long estimatedTokens = (long) (text.length() * 1.3);
        
        return ChatResponse.builder()
                .content(text)
                .messageId("gemini-" + System.currentTimeMillis())
                .model(model)
                .tokens(estimatedTokens)
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