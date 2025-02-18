package com.gushan.easyai.service.impl;

import com.gushan.easyai.config.EasyAIProperties;
import com.gushan.easyai.service.AIService;
import com.gushan.easyai.service.AIServiceProvider;

public class ClaudeAIServiceProvider implements AIServiceProvider {
    @Override
    public String getName() {
        return "claude";
    }

    @Override
    public AIService createService(EasyAIProperties properties) {
        return new ClaudeAIServiceImpl(properties);
    }
} 