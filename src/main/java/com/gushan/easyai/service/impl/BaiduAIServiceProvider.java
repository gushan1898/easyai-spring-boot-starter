package com.gushan.easyai.service.impl;

import com.gushan.easyai.config.EasyAIProperties;
import com.gushan.easyai.service.AIService;
import com.gushan.easyai.service.AIServiceProvider;

public class BaiduAIServiceProvider implements AIServiceProvider {
    @Override
    public String getName() {
        return "baidu";
    }

    @Override
    public AIService createService(EasyAIProperties properties) {
        return new BaiduAIServiceImpl(properties);
    }
} 