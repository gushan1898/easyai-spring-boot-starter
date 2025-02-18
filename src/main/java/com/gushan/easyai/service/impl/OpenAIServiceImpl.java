package com.gushan.easyai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gushan.easyai.config.EasyAIProperties;
import com.gushan.easyai.exception.AIException;
import com.gushan.easyai.model.ChatRequest;
import com.gushan.easyai.model.ChatResponse;
import com.gushan.easyai.model.ChatStreamResponse;
import com.gushan.easyai.model.ImageGenerationRequest;
import com.gushan.easyai.model.ImageGenerationResponse;
import com.gushan.easyai.service.AIService;
import com.gushan.easyai.service.StreamResponseCallback;
import com.gushan.easyai.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class OpenAIServiceImpl implements AIService {
    private final EasyAIProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            EasyAIProperties.OpenAI config = properties.getOpenai();
            if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
                throw new AIException("OpenAI API key is missing");
            }

            String url = config.getBaseUrl() + "/chat/completions";
            Map<String, Object> requestBody = buildRequestBody(request, config);
            
            String response = HttpUtils.post(url, objectMapper.writeValueAsString(requestBody), config.getApiKey());
            return parseResponse(response, config.getModel());
        } catch (Exception e) {
            log.error("OpenAI API调用失败", e);
            throw new AIException("OpenAI API调用失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildRequestBody(ChatRequest request, EasyAIProperties.OpenAI config) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("messages", convertToMessages(request));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);
        return requestBody;
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
        JsonNode choices = jsonNode.get("choices");
        
        if (choices == null || choices.isEmpty()) {
            throw new AIException("无效的API响应");
        }
        
        String content = choices.get(0).get("message").get("content").asText();
        String messageId = jsonNode.get("id").asText();
        long tokens = jsonNode.get("usage").get("total_tokens").asLong();
        
        return ChatResponse.builder()
                .content(content)
                .messageId(messageId)
                .model(model)
                .tokens(tokens)
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
    public void chatStream(ChatRequest request, StreamResponseCallback callback) {
        try {
            EasyAIProperties.OpenAI config = properties.getOpenai();
            String url = config.getBaseUrl() + "/chat/completions";
            
            Map<String, Object> requestBody = buildRequestBody(request, config);
            requestBody.put("stream", true);
            
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Authorization", "Bearer " + config.getApiKey());
                httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody), StandardCharsets.UTF_8));
                
                client.execute(httpPost, response -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                                String jsonStr = line.substring(6);
                                JsonNode jsonNode = objectMapper.readTree(jsonStr);
                                
                                String content = jsonNode.get("choices").get(0).get("delta").get("content").asText("");
                                String messageId = jsonNode.get("id").asText();
                                
                                callback.onResponse(ChatStreamResponse.builder()
                                        .content(content)
                                        .messageId(messageId)
                                        .model(config.getModel())
                                        .isEnd(false)
                                        .timestamp(System.currentTimeMillis())
                                        .build());
                            }
                        }
                        callback.onComplete();
                    }
                    return null;
                });
            }
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    @Override
    public ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        try {
            EasyAIProperties.OpenAI config = properties.getOpenai();
            String url = config.getBaseUrl() + "/images/generations";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", request.getPrompt());
            requestBody.put("n", request.getNumberOfImages());
            requestBody.put("size", request.getWidth() + "x" + request.getHeight());
            
            if (request.getOptions() != null) {
                requestBody.putAll(request.getOptions());
            }
            
            String response = HttpUtils.post(url, objectMapper.writeValueAsString(requestBody), config.getApiKey());
            JsonNode jsonNode = objectMapper.readTree(response);
            
            List<String> imageUrls = new ArrayList<>();
            jsonNode.get("data").forEach(item -> imageUrls.add(item.get("url").asText()));
            
            return ImageGenerationResponse.builder()
                    .imageUrls(imageUrls)
                    .requestId(jsonNode.get("created").asText())
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            throw new AIException("图像生成失败: " + e.getMessage(), e);
        }
    }
} 