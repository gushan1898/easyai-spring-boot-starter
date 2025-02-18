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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class BaiduAIServiceImpl implements AIService {
    private final EasyAIProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            EasyAIProperties.Baidu config = properties.getBaidu();
            String accessToken = getAccessToken(config);
            String url = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions?access_token=" + accessToken;

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", convertToMessages(request));
            
            String response = HttpUtils.post(url, objectMapper.writeValueAsString(requestBody), null);
            return parseResponse(response, config.getModel());
        } catch (Exception e) {
            log.error("百度文心一言API调用失败", e);
            throw new AIException("百度文心一言API调用失败: " + e.getMessage(), e);
        }
    }

    private String getAccessToken(EasyAIProperties.Baidu config) throws Exception {
        String url = "https://aip.baidubce.com/oauth/2.0/token";
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");
        params.put("client_id", config.getAccessKey());
        params.put("client_secret", config.getSecretKey());

        String response = HttpUtils.post(url, objectMapper.writeValueAsString(params), null);
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("access_token").asText();
    }

    private List<Map<String, String>> convertToMessages(ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        if (request.getHistory() != null) {
            for (ChatRequest.Message msg : request.getHistory()) {
                Map<String, String> message = new HashMap<>();
                message.put("role", msg.getRole());
                message.put("content", msg.getContent());
                messages.add(message);
            }
        }
        
        Map<String, String> currentMessage = new HashMap<>();
        currentMessage.put("role", "user");
        currentMessage.put("content", request.getPrompt());
        messages.add(currentMessage);
        
        return messages;
    }

    private ChatResponse parseResponse(String response, String model) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        JsonNode result = jsonNode.get("result");
        
        if (result == null) {
            throw new AIException("无效的API响应");
        }
        
        return ChatResponse.builder()
                .content(result.asText())
                .messageId("baidu-" + System.currentTimeMillis())
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
} 