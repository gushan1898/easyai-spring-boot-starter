package com.gushan.easyai.service;

import com.gushan.easyai.model.ChatRequest;
import com.gushan.easyai.model.ChatResponse;
import com.gushan.easyai.model.ImageGenerationRequest;
import com.gushan.easyai.model.ImageGenerationResponse;

public interface AIService {
    /**
     * 聊天接口
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 简单聊天接口
     *
     * @param prompt 输入文本
     * @return AI响应文本
     */
    String chat(String prompt);

    // 新增流式响应方法
    default void chatStream(ChatRequest request, StreamResponseCallback callback) {
        throw new UnsupportedOperationException("This AI service does not support streaming");
    }

    // 新增图像生成方法
    default ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        throw new UnsupportedOperationException("This AI service does not support image generation");
    }

    default boolean supportsCaching() {
        return true;
    }

    default boolean supportsRateLimiting() {
        return true;
    }

    default boolean supportsStreaming() {
        return false;
    }
} 