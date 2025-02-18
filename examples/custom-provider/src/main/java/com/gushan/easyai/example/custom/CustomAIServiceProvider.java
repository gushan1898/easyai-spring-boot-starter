package com.gushan.easyai.example.custom;

import com.gushan.easyai.config.EasyAIProperties;
import com.gushan.easyai.service.AIService;
import com.gushan.easyai.service.AIServiceProvider;
import org.springframework.stereotype.Component;

@Component
public class CustomAIServiceProvider implements AIServiceProvider {
    @Override
    public String getName() {
        return "custom";
    }

    @Override
    public AIService createService(EasyAIProperties properties) {
        return new CustomAIServiceImpl(properties);
    }
} 