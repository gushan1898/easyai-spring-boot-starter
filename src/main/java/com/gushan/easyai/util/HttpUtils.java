package com.gushan.easyai.util;

import com.gushan.easyai.exception.AIException;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class HttpUtils {
    private static final int DEFAULT_TIMEOUT = 30000;

    public static String post(String url, String json, String apiKey) {
        return post(url, json, apiKey, DEFAULT_TIMEOUT);
    }

    public static String post(String url, String json, String apiKey, int timeout) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS))
                .setResponseTimeout(Timeout.of(timeout, TimeUnit.MILLISECONDS))
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {
            
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            if (apiKey != null && !apiKey.isEmpty()) {
                httpPost.setHeader("Authorization", "Bearer " + apiKey);
            }
            httpPost.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
            
            return client.execute(httpPost, response -> {
                int statusCode = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                
                if (statusCode != 200) {
                    throw new AIException("API调用失败: HTTP " + statusCode + ", " + responseBody);
                }
                return responseBody;
            });
        } catch (IOException e) {
            throw new AIException("网络请求失败: " + e.getMessage(), e);
        }
    }
} 