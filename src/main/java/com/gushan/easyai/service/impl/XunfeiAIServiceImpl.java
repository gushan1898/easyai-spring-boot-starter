package com.gushan.easyai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gushan.easyai.config.EasyAIProperties;
import com.gushan.easyai.exception.AIException;
import com.gushan.easyai.model.ChatRequest;
import com.gushan.easyai.model.ChatResponse;
import com.gushan.easyai.service.AIService;
import com.gushan.easyai.websocket.XunfeiWebSocketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class XunfeiAIServiceImpl implements AIService {
    private final EasyAIProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            EasyAIProperties.Xunfei config = properties.getXunfei();
            String url = buildWebsocketUrl(config);
            
            XunfeiWebSocketClient client = new XunfeiWebSocketClient(new URI(url));
            client.connectBlocking(config.getTimeout(), TimeUnit.MILLISECONDS);
            
            // 发送消息
            Map<String, Object> requestBody = buildRequestBody(request, config);
            client.send(objectMapper.writeValueAsString(requestBody));
            
            // 等待响应
            String response = client.getResponseFuture().get(config.getTimeout(), TimeUnit.MILLISECONDS);
            
            return ChatResponse.builder()
                    .content(response)
                    .messageId("xunfei-" + System.currentTimeMillis())
                    .model(config.getModel())
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("讯飞星火API调用失败", e);
            throw new AIException("讯飞星火API调用失败: " + e.getMessage(), e);
        }
    }

    private String buildWebsocketUrl(EasyAIProperties.Xunfei config) throws Exception {
        String host = "wss://spark-api.xf-yun.com/v3.5/chat";
        String date = String.valueOf(System.currentTimeMillis());
        String signature = generateSignature(host, date, config.getApiKey(), config.getApiSecret());
        
        return String.format("%s?authorization=%s&date=%s&host=%s",
                host, signature, date, "spark-api.xf-yun.com");
    }

    private String generateSignature(String host, String date, String apiKey, String apiSecret) throws Exception {
        String stringToSign = "host: " + host + "\n" +
                            "date: " + date + "\n" +
                            "GET /v3.5/chat HTTP/1.1";
        
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signData);
        
        String authorization = String.format("api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                apiKey, signature);
        
        return Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, Object> buildRequestBody(ChatRequest request, EasyAIProperties.Xunfei config) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // 构建header
        Map<String, Object> header = new HashMap<>();
        header.put("app_id", config.getAppId());
        header.put("uid", UUID.randomUUID().toString());
        requestBody.put("header", header);
        
        // 构建parameter
        Map<String, Object> parameter = new HashMap<>();
        Map<String, Object> chat = new HashMap<>();
        chat.put("domain", "general");
        chat.put("temperature", 0.7);
        chat.put("max_tokens", 2048);
        
        // 添加用户自定义选项
        if (request.getOptions() != null) {
            if (request.getOptions().containsKey("temperature")) {
                chat.put("temperature", request.getOptions().get("temperature"));
            }
            if (request.getOptions().containsKey("max_tokens")) {
                chat.put("max_tokens", request.getOptions().get("max_tokens"));
            }
        }
        
        parameter.put("chat", chat);
        requestBody.put("parameter", parameter);
        
        // 构建payload
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> message = new HashMap<>();
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加历史消息
        if (request.getHistory() != null) {
            for (ChatRequest.Message msg : request.getHistory()) {
                messages.add(Map.of(
                    "role", convertRole(msg.getRole()),
                    "content", msg.getContent()
                ));
            }
        }
        
        // 添加当前消息
        messages.add(Map.of(
            "role", "user",
            "content", request.getPrompt()
        ));
        
        message.put("text", messages);
        payload.put("message", message);
        requestBody.put("payload", payload);
        
        return requestBody;
    }

    private String convertRole(String role) {
        return switch (role.toLowerCase()) {
            case "assistant" -> "assistant";
            case "system" -> "system";
            default -> "user";
        };
    }

    @Override
    public String chat(String prompt) {
        ChatRequest request = ChatRequest.builder()
                .prompt(prompt)
                .build();
        return chat(request).getContent();
    }
} 