package com.gushan.easyai.service;

import com.gushan.easyai.config.EasyAIProperties;

public interface AIServiceProvider {
    /**
     * 获取提供者名称
     */
    String getName();

    /**
     * 创建AI服务实例
     */
    AIService createService(EasyAIProperties properties);
} 