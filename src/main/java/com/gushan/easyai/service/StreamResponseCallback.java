package com.gushan.easyai.service;

import com.gushan.easyai.model.ChatStreamResponse;

public interface StreamResponseCallback {
    void onResponse(ChatStreamResponse response);
    void onError(Throwable throwable);
    void onComplete();
} 